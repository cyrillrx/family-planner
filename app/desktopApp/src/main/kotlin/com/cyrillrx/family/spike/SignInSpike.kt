package com.cyrillrx.family.spike

import android.app.Application
import com.google.firebase.FirebasePlatform
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.app
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.initialize
import kotlinx.coroutines.runBlocking
import java.io.File
import java.nio.file.Files
import java.util.Properties
import kotlin.system.exitProcess

private const val USER_KEY = "com.google.firebase.auth.FIREBASE_USER"

private class TempPlatform(private val directory: File) : FirebasePlatform() {
    val stored = Properties()
    override fun store(key: String, value: String) { stored.setProperty(key, value) }
    override fun retrieve(key: String): String? = stored.getProperty(key)
    override fun clear(key: String) { stored.remove(key) }
    override fun log(msg: String) = Unit
    override fun getDatabasePath(name: String): File = directory.resolve(name)
}

// Throwaway: on a fresh device, can a failed link fall back to signing in to the existing account?
fun main() {
    val props = Properties().apply { File("local.properties").reader().use(::load) }
    fun p(key: String) = props.getProperty(key).trim()
    val apiKey = p("firebase.jvmApiKey")
    val options = FirebaseOptions(
        applicationId = p("firebase.jvmApplicationId"),
        apiKey = apiKey,
        projectId = p("firebase.projectId"),
    )
    val platform = TempPlatform(Files.createTempDirectory("fp-spike").toFile())
    FirebasePlatform.initializeFirebasePlatform(platform)
    Firebase.initialize(Application(), options)

    runBlocking {
        val fresh = Firebase.auth.signInAnonymously().user!!
        println("1. Fresh device, anonymous uid=${fresh.uid}")

        val googleIdToken = googleSignIn(p("google.desktopClientId"), p("google.desktopClientSecret"))
        val postBody = "id_token=$googleIdToken&providerId=google.com"

        val link = post(
            "https://identitytoolkit.googleapis.com/v1/accounts:signInWithIdp?key=$apiKey",
            """{"postBody":"$postBody","requestUri":"http://localhost","idToken":"${fresh.getIdToken(false)}","returnSecureToken":true}""",
        )
        println("2. Link attempt -> HTTP ${link.first} error=${field(link.second, "message")} localId=${field(link.second, "localId")}")

        val signIn = post(
            "https://identitytoolkit.googleapis.com/v1/accounts:signInWithIdp?key=$apiKey",
            """{"postBody":"$postBody","requestUri":"http://localhost","returnSecureToken":true}""",
        )
        val existingUid = field(signIn.second, "localId")
        println("3. Sign-in -> HTTP ${signIn.first} localId=$existingUid")

        val session = """{"uid":"$existingUid","idToken":"${field(signIn.second, "idToken")}",""" +
            """"refreshToken":"${field(signIn.second, "refreshToken")}","expiresIn":${field(signIn.second, "expiresIn") ?: "3600"},""" +
            """"isAnonymous":false,"createdAt":${System.currentTimeMillis()}}"""
        platform.store(USER_KEY, session)

        com.google.firebase.FirebaseApp.getInstance().delete()
        Firebase.initialize(Application(), options)
        val adopted = Firebase.auth.currentUser
        println("4. After re-initialising: uid=${adopted?.uid} anonymous=${adopted?.isAnonymous}")
        val refreshed = adopted?.getIdToken(true)
        println("   token refresh OK=${refreshed != null}")

        val doc = runCatching { Firebase.firestore.collection("users").document(existingUid!!).get() }
        println("5. Firestore users/{uid} read: ${doc.map { "ok, exists=${it.exists}" }.getOrElse { "FAILED ${it.message}" }}")
        println("RESULT: adopted existing account = ${adopted?.uid == existingUid && existingUid != fresh.uid}")
    }
    exitProcess(0)
}
