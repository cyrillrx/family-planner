package com.cyrillrx.family

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.cyrillrx.family.app.App
import com.cyrillrx.family.firebase.initializeFirebase

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        // TODO(#55): move to an Application subclass before an entry point other than this activity lands.
        initializeFirebase(applicationContext)
        super.onCreate(savedInstanceState)

        setContent {
            App()
        }
    }
}
