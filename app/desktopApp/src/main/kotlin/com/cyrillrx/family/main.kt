package com.cyrillrx.family

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.cyrillrx.core.desktop.SingleInstance
import com.cyrillrx.family.app.App
import com.cyrillrx.family.desktop.applicationDataDirectory
import com.cyrillrx.family.firebase.initializeFirebase
import java.awt.Desktop
import java.awt.Desktop.Action.APP_REQUEST_FOREGROUND

fun main() {
    val directory = applicationDataDirectory()
    val instance = directory?.let(SingleInstance::claim)
    if (directory != null && instance == null) return SingleInstance.requestActivation(directory)

    // application() ends in exitProcess(), so releasing the instance around it would never run.
    instance?.let { held -> Runtime.getRuntime().addShutdownHook(Thread(held::close)) }

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
    requestApplicationForeground()
    isMinimized = false
    toFront()
    requestFocus()
}

// The window manager refuses toFront() to an application that is not the active one, which is the
// only case this feature exists for.
private fun requestApplicationForeground() {
    if (!Desktop.isDesktopSupported()) return

    val desktop = Desktop.getDesktop()
    if (desktop.isSupported(APP_REQUEST_FOREGROUND)) desktop.requestForeground(true)
}
