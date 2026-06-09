package com.sc.eventnotifyke.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.sc.eventnotifyke.navigation.Screen
import com.sc.eventnotifyke.viewmodel.AuthViewModel
@Composable
fun HomeScreen (navController: NavController, authViewModel: AuthViewModel= viewModel()){
    val authState by authViewModel.authState.collectAsState()
    Box(modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center) {

        Button(onClick = {
            authViewModel.logout()
            navController.navigate(Screen.Login.route) {
                popUpTo(Screen.Home.route) { inclusive = true }
            }

        }) {
            Text("Logout")
        }
    }
}
