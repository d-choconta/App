package com.decoraia.app

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.decoraia.app.ui.nav.AppNavGraph
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)

        val db = FirebaseFirestore.getInstance()
        db.collection("test").document("ping")
            .set(mapOf("time" to System.currentTimeMillis()))
            .addOnSuccessListener {
                Log.d("FIREBASE", "✅ Conectado a Firestore")
            }
            .addOnFailureListener { e ->
                Log.e("FIREBASE", "❌ Error: ${e.message}")
            }

        setContent {
            DecoraIAApp()
        }
    }
}

@Composable
fun DecoraIAApp() {
    val navController = rememberNavController()
    MaterialTheme {
        AppNavGraph(navController = navController)
    }
}
