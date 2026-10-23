package com.cyrillrx.family.spike

import com.cyrillrx.family.firebase.initializeFirebase
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.runBlocking
import java.awt.Desktop
import java.io.File
import java.net.InetAddress
import java.net.ServerSocket
import java.net.URI
import java.net.URLDecoder
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import java.util.Properties
import kotlin.system.exitProcess

// Throwaway: does linking Google to the anonymous account keep the UID on the JVM target?
fun main() {
    val props = Properties().apply { File("local.properties").reader().use(::load) }
    val clientId = props.getProperty("google.desktopClientId").trim()
    val clientSecret = props.getProperty("google.desktopClientSecret").trim()
    val apiKey = props.getProperty("firebase.jvmApiKey").trim()

    initializeFirebase()

    runBlocking {
        val auth = Firebase.auth
        val before = auth.currentUser ?: auth.signInAnonymously().user!!
        println("1. Firebase user before: uid=${before.uid} anonymous=${before.isAnonymous}")

        val googleIdToken = googleSignIn(clientId, clientSecret)
        println("2. Google id_token received (${googleIdToken.length} chars)")

        val firebaseIdToken = before.getIdToken(false)!!
        val link = post(
            "https://identitytoolkit.googleapis.com/v1/accounts:signInWithIdp?key=$apiKey",
            """{"postBody":"id_token=$googleIdToken&providerId=google.com","requestUri":"http://localhost",""" +
                """"idToken":"$firebaseIdToken","returnSecureToken":true}""",
        )
        println("3. signInWithIdp -> HTTP ${link.first}")
        println("   localId=${field(link.second, "localId")} providerId=${field(link.second, "providerId")} " +
            "email=${field(link.second, "email")} error=${field(link.second, "message")}")

        val refreshed = auth.currentUser!!.getIdToken(true)!!
        val lookup = post(
            "https://identitytoolkit.googleapis.com/v1/accounts:lookup?key=$apiKey",
            """{"idToken":"$refreshed"}""",
        )
        println("4. SDK session after link: uid=${auth.currentUser!!.uid} (token refresh OK)")
        println("   providers=${Regex("\"providerId\":\\s*\"([^\"]+)\"").findAll(lookup.second).map { it.groupValues[1] }.toList()}")
        println("RESULT: uid kept = ${field(link.second, "localId") == before.uid}")
    }
    exitProcess(0)
}

internal fun googleSignIn(clientId: String, clientSecret: String): String {
    val server = ServerSocket(0, 1, InetAddress.getLoopbackAddress())
    val redirect = "http://127.0.0.1:${server.localPort}"
    val verifier = randomUrlSafe(48)
    val challenge = Base64.getUrlEncoder().withoutPadding()
        .encodeToString(MessageDigest.getInstance("SHA-256").digest(verifier.toByteArray()))
    val state = randomUrlSafe(16)
    val url = "https://accounts.google.com/o/oauth2/v2/auth?client_id=${enc(clientId)}&redirect_uri=${enc(redirect)}" +
        "&response_type=code&scope=${enc("openid email profile")}&code_challenge=$challenge" +
        "&code_challenge_method=S256&state=$state"
    println("   Opening the browser for Google sign-in…")
    Desktop.getDesktop().browse(URI(url))

    val query = server.accept().use { socket ->
        val requestLine = socket.getInputStream().bufferedReader().readLine()
        socket.getOutputStream().write(
            "HTTP/1.1 200 OK\r\nContent-Type: text/html; charset=utf-8\r\nConnection: close\r\n\r\n<p>Done, back to the terminal.</p>"
                .toByteArray(),
        )
        requestLine.substringAfter("?").substringBefore(" ")
    }
    server.close()
    val params = query.split("&").associate { it.substringBefore("=") to URLDecoder.decode(it.substringAfter("="), "UTF-8") }
    check(params["state"] == state) { "state mismatch: $params" }
    val code = params["code"] ?: error("no code: $params")

    val token = postForm(
        "https://oauth2.googleapis.com/token",
        "code=${enc(code)}&client_id=${enc(clientId)}&client_secret=${enc(clientSecret)}&redirect_uri=${enc(redirect)}" +
            "&grant_type=authorization_code&code_verifier=$verifier",
    )
    return field(token.second, "id_token") ?: error("token exchange failed: HTTP ${token.first} ${token.second}")
}

private val http = HttpClient.newHttpClient()

internal fun post(url: String, json: String) = send(url, json, "application/json")
private fun postForm(url: String, form: String) = send(url, form, "application/x-www-form-urlencoded")
private fun send(url: String, body: String, type: String): Pair<Int, String> {
    val response = http.send(
        HttpRequest.newBuilder(URI(url)).header("Content-Type", type).POST(HttpRequest.BodyPublishers.ofString(body)).build(),
        HttpResponse.BodyHandlers.ofString(),
    )
    return response.statusCode() to response.body()
}

internal fun field(json: String, name: String) = Regex("\"$name\":\\s*\"([^\"]*)\"").find(json)?.groupValues?.get(1)
private fun enc(value: String) = URLEncoder.encode(value, "UTF-8")
private fun randomUrlSafe(bytes: Int) =
    Base64.getUrlEncoder().withoutPadding().encodeToString(ByteArray(bytes).also(SecureRandom()::nextBytes))
