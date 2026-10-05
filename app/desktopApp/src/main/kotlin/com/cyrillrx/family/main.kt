package com.cyrillrx.family

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.cyrillrx.core.desktop.SingleInstance
import com.cyrillrx.family.app.App
import com.cyrillrx.family.desktop.applicationDataDirectory
import com.cyrillrx.family.firebase.initializeFirebase

fun main() {
    val directory = applicationDataDirectory()
    val instance = directory?.let(SingleInstance::claim)
    if (directory != null && instance == null) return SingleInstance.requestActivation(directory)

    initializeFirebase()

    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "FamilyPlanner",
        ) {
            LaunchedEffect(instance) {
                instance?.activationRequests?.collect { window.bringToFront() }
            }
            App()
        }
    }
}

private fun ComposeWindow.bringToFront() {
    isMinimized = false
    toFront()
    requestFocus()
}
