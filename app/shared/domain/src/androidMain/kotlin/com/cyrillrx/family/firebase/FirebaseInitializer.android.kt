package com.cyrillrx.family.firebase

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.apps
import dev.gitlive.firebase.initialize

actual fun initializeFirebase(context: Any?) {
    val options = androidOptions() ?: return

    if (Firebase.apps(context).isEmpty()) Firebase.initialize(context, options)
}

private fun androidOptions(): FirebaseOptions? {
    val applicationId = FirebaseConfig.androidApplicationId
    val apiKey = FirebaseConfig.androidApiKey
    if (applicationId.isEmpty() || apiKey.isEmpty() || FirebaseConfig.projectId.isEmpty()) return null

    return FirebaseOptions(
        applicationId = applicationId,
        apiKey = apiKey,
        projectId = FirebaseConfig.projectId,
        gcmSenderId = FirebaseConfig.gcmSenderId.ifEmpty { null },
        storageBucket = FirebaseConfig.storageBucket.ifEmpty { null },
    )
}
