package com.sc.eventnotifyke.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@Composable
fun ForgotPasswordScreen(navController: NavController){
    // data state def.
    var email by remember { mutableStateOf("") }
    // this will reference whether a reset password email
    // has been sent to the user or not
    var sent by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()
        .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center){

        Column(modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ){

            IconButton(onClick = {navController.popBackStack()},
                modifier = Modifier.align(Alignment.Start)) {
                Icon(Icons.Default.ArrowBack,
                    "back",
                    tint= MaterialTheme.colorScheme.onBackground)
            }

            Spacer(Modifier.height(16.dp))

            // Integrated App Identity Icon to match Login/Register themes
            Icon(
                imageVector = Icons.Default.LockReset,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(56.dp)
            )

            Spacer(Modifier.height(12.dp))

            Text("Enter your email and we will send you a reset link",
                style = MaterialTheme.typography.bodyLarge,
                color= MaterialTheme.colorScheme.onSurface
                    .copy(alpha = 0.5f))

            Spacer(Modifier.height(32.dp))

            // if email has been sent show msg else show form
            if(sent){
                Text("Reset Link has already been sent. Kindly check your inbox!!",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary)
            } else {
                OutlinedTextField(
                    value = email,
                    onValueChange = {email = it},
                    label = {Text("Email")},
                    leadingIcon = {
                        Icon(Icons.Default.Email, null)
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email
                    )
                )

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = {sent=true},
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape= RoundedCornerShape(12.dp),
                    enabled = email.isNotBlank()
                ){
                    Text("Send Reset Link", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ForgotPasswordPreview(){
    // Updated to match your custom theme wrapper styling
    MaterialTheme {
        ForgotPasswordScreen(
            rememberNavController())
    }
}