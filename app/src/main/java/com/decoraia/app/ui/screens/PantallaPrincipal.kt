package com.decoraia.app.ui.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.decoraia.app.ui.components.PrincipalScreenUI
import com.decoraia.app.ui.theme.InriaSans
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun PantallaPrincipal(navController: NavHostController) {
    val auth = FirebaseAuth.getInstance()

    PrincipalScreenUI(
        onGoIA = { navController.navigate("chatia") },
        onGoRA = { navController.navigate("raestilos") },
        onGoPerfil = { navController.navigate("perfil") },

        onLogoutConfirmed = {
            auth.signOut()
            navController.navigate("inicio") {
                popUpTo("principal") { inclusive = true }
            }
            }
        )
}