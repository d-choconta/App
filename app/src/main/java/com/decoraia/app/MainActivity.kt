package com.decoraia.app

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.decoraia.app.ui.nav.AppNavGraph
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // (Opcional) Si ya usas google-services, esto será no-op,
        // pero ayuda a evitar dudas en pruebas locales/emulador.
        FirebaseApp.initializeApp(this)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Ping/health-check a Firestore al montar la UI
                    LaunchedEffect(Unit) {
                        try {
                            val db = FirebaseFirestore.getInstance()
                            db.collection("healthcheck")
                                .document("ping")
                                .set(mapOf("time" to System.currentTimeMillis()))
                                .addOnSuccessListener {
                                    Log.d("FIREBASE", "✅ Firestore OK (healthcheck/ping escrito)")
                                }
                                .addOnFailureListener { e ->
                                    Log.e("FIREBASE", "❌ Error Firestore: ${e.message}", e)
                                }
                        } catch (t: Throwable) {
                            Log.e("FIREBASE", "❌ Error inicializando Firebase", t)
                        }
                    }

                    val navController = rememberNavController()
                    AppNavGraph(navController = navController)
                }
            }
        }
    }
}
