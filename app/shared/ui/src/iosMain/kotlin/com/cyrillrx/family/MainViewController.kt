package com.cyrillrx.family

import androidx.compose.ui.window.ComposeUIViewController
import com.cyrillrx.family.app.App
import com.cyrillrx.family.firebase.initializeFirebase
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
    initializeFirebase()

    return ComposeUIViewController { App() }
}
