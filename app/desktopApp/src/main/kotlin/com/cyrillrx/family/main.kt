package com.cyrillrx.family

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.cyrillrx.family.app.App
import com.cyrillrx.family.firebase.initializeFirebase

fun main() {
    initializeFirebase()

    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "FamilyPlanner",
        ) {
            App()
        }
    }
}
