package com.sc.eventnotifyke.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.sc.eventnotifyke.navigation.Screen
import com.sc.eventnotifyke.ui.theme.EventNotifyKETheme
import com.sc.eventnotifyke.viewmodel.AuthViewModel
import com.sc.eventnotifyke.viewmodel.AuthState

@Composable
fun LoginScreen(navController: NavController, authViewModel: AuthViewModel = viewModel()){
    //data to be maintained in the screen
    //login info : email x password
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    // auth view model references
    val authState by authViewModel.authState.collectAsState()
    val isLoading = authState is AuthState.Loading
    val errorMessage = (authState as? AuthState.Error)?.message

    // AUTOMATIC SAFETY NET: Wipes state when the user leaves via physical system back swipe gesture
    DisposableEffect(Unit) {
        onDispose {
            authViewModel.resetState()
        }
    }

    // when a user logins successfully take to the dashboard
    LaunchedEffect(authState) {
        if (authState is AuthState.Success){
            // Changed target route to look for your home dashboard feed screen
            navController.navigate(Screen.Home.route){
                popUpTo(Screen.Login.route){inclusive=true}
            }
        }
    }
    // controlling visibility of password showcase
    var passwordVisible by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()
        .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center){
        Column(modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally){

            // Fixed: Customized Icon to show an event schedule asset for EventNotify KE
            Icon(Icons.Default.Event,
                contentDescription = null,
                tint=MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp))
            Spacer(Modifier.height(8.dp))

            // Fixed: Customized Text label anchors for your specific platform
            Text(text="Sign In To EventNotify KE",
                style=MaterialTheme.typography.bodyLarge,
                color=MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(40.dp))

            // form inputs
            OutlinedTextField(
                value = email, onValueChange = {email = it},
                label = {Text("Email")},
                leadingIcon = {Icon(
                    Icons.Default.Email,
                    null
                )},
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email
                ),
                singleLine = true,
                modifier= Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading // Lock field while querying Firebase
            )
            Spacer(modifier =Modifier.height(12.dp))

            // form inputs
            OutlinedTextField(
                value = password, onValueChange = {password = it},
                label = {Text("Password")},
                singleLine = true,
                modifier= Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = {Icon(
                    Icons.Default.Lock,
                    null
                )},
                trailingIcon = {
                    IconButton(
                        onClick = { passwordVisible = !passwordVisible },
                        enabled = !isLoading
                    ) {
                        Icon(if(passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,null)
                    }
                },
                visualTransformation = if(passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password
                ),
                enabled = !isLoading // Lock field while querying Firebase
            )
            Spacer(modifier = Modifier.height(8.dp))

            // link to forgot password screen
            TextButton(
                onClick = {
                    authViewModel.resetState() //  MANUAL STATE CLEAN ON BUTTON CLICK
                    navController.navigate(Screen.ForgotPassword.route)
                },
                modifier = Modifier.align(Alignment.End),
                enabled = !isLoading
            ) {
                Text("Forgot Password",
                    color= MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier=Modifier.height(20.dp))

            // Error messaging display layout mapping if registration or auth triggers failures
            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Button(
                onClick = {
                    authViewModel.resetState() // 👈 MANUAL STATE CLEAN ON BUTTON CLICK
                    authViewModel.Login(email.trim(), password.trim())
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = email.isNotBlank() && password.isNotBlank() && !isLoading
            ) {
                if (isLoading){
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                }else{
                    Text ("Sign In", style = MaterialTheme.typography.bodyLarge)
                }
            }

            Spacer(Modifier.height(16.dp))

            // To link to the register string
            TextButton(
                onClick = {
                    authViewModel.resetState() // 👈 MANUAL STATE CLEAN ON BUTTON CLICK
                    navController.navigate(Screen.Register.route)
                },
                modifier = Modifier.align(Alignment.End),
                enabled = !isLoading
            ) {
                Text("Don't have an account? Register",
                    color= MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview(){
    EventNotifyKETheme {
        LoginScreen(rememberNavController())
    }
}