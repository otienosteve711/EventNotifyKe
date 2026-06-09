package com.sc.eventnotifyke.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.sc.eventnotifyke.viewmodel.AuthState
import com.sc.eventnotifyke.viewmodel.AuthViewModel

// Short, clean zone names for uniform chip layout
val zones = listOf(
    "Nairobi CBD",
    "Nairobi East",
    "Nairobi West",
    "Westlands",
    "Nairobi North",
    "Dagoretti"
)

// Dynamic subtext mapping to guide the user on selection
val zoneSubtext = mapOf(
    "Nairobi CBD" to "Town, Down Town, Ngara",
    "Nairobi East" to "Umoja, Kayole, Pipeline, Donholm, Embakasi",
    "Nairobi West" to "Lang'ata, Karen, Madaraka, Kibera, South C",
    "Westlands" to "Kilimani, Kileleshwa, Lavington, Parklands",
    "Nairobi North" to "Kasarani, Roysambu, Githurai, Zimmerman",
    "Dagoretti" to "Ngong Rd, Kawangware, Riruta, Satellite"
)


@Composable
fun RegisterScreen(navController: NavController, authViewModel: AuthViewModel = viewModel()){
    // Form state variables matching your attributes
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var localValidationError by remember { mutableStateOf<String?>(null) }
    var selectedZone by remember { mutableStateOf("") }

    // Auth view model state references
    val authState by authViewModel.authState.collectAsState()
    val isLoading = authState is AuthState.Loading

    // Combining local matching verification checks and backend errors cleanly
    val displayErrorMessage = localValidationError ?: (authState as? AuthState.Error)?.message

    // Navigation on registration success
    LaunchedEffect(authState) {
        if (authState is AuthState.Success){
            navController.navigate(Screen.Home.route){
                popUpTo(Screen.Login.route){ inclusive = true }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(
        MaterialTheme.colorScheme.background
    )){
        Column(modifier = Modifier.fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally){

            // Header Icon section with brand branding color
            Icon(Icons.Default.Event,
                contentDescription = null,
                tint = Color(0xFF00B39F),
                modifier = Modifier.size(56.dp))
            Spacer(Modifier.height(8.dp))
            Text(text = "Create Account",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary)
            Text(text = "Join your local Nairobi zone",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            Spacer(Modifier.height(32.dp))

            // Full name field
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name") },
                leadingIcon = { Icon(Icons.Default.Person, null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Email field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Default.Email, null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                enabled = !isLoading
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Phone field
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone Number") },
                leadingIcon = { Icon(Icons.Default.Phone, null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                enabled = !isLoading
            )
            Spacer(modifier = Modifier.height(20.dp))

            // --- NAIROBI ZONES CHIPS SELECTION ---
            Text("Select your Zone:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.Start),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))

            Text(
                text = if (selectedZone.isEmpty()) "Tap a zone to see covered estates" else zoneSubtext[selectedZone] ?: "",
                fontWeight = if (selectedZone.isEmpty()) FontWeight.Normal else FontWeight.Medium,
                style = MaterialTheme.typography.bodySmall,
                color = if (selectedZone.isEmpty()) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f) else MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(top = 2.dp, bottom = 10.dp)
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                zones.forEach { zone ->
                    val selected = selectedZone == zone
                    FilterChip(
                        selected = selected,
                        onClick = { if (!isLoading) selectedZone = zone },
                        label = { Text(zone) },
                        enabled = !isLoading,
                        shape = RoundedCornerShape(8.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        leadingIcon = if (selected) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else null
                    )
                }
            }
            // ------------------------------------

            Spacer(Modifier.height(20.dp))

            // Password field
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    localValidationError = null // Clears validation alerts instantly as user corrects typos
                },
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Default.Lock, null) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            null
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )
            Spacer(Modifier.height(12.dp))

            // Confirm Password field
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    localValidationError = null
                },
                label = { Text("Confirm Password") },
                leadingIcon = { Icon(Icons.Default.Lock, null) },
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            null
                        )
                    }
                },
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )
            Spacer(Modifier.height(20.dp))

            // Error banner logic spacing setup
            if (displayErrorMessage != null) {
                Text(
                    text = displayErrorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.Start).padding(start = 4.dp)
                )
                Spacer(Modifier.height(8.dp))
            } else {
                Spacer(Modifier.height(28.dp))
            }

            // Lecturer-compliant Submit Action Button
            Button(
                onClick = {
                    if (password != confirmPassword) {
                        localValidationError = "Passwords do not match!"
                    } else {
                        localValidationError = null
                        authViewModel.register(
                            fullName = name.trim(),
                            email = email.trim(),
                            phone = phone.trim(),
                            estate = selectedZone, // Maps short chip identifier seamlessly to backend data schema
                            password = password.trim(),
                            role = "user"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = name.isNotBlank() &&
                        email.isNotBlank() &&
                        phone.isNotBlank() &&
                        selectedZone.isNotBlank() &&
                        password.isNotBlank() &&
                        confirmPassword.isNotBlank() &&
                        !isLoading
            ) {
                if (isLoading){
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text ("Create Account", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(16.dp))

            // Redirection linkage navigation
            TextButton(
                onClick = {
                    authViewModel.resetState()
                    navController.popBackStack()
                },
                modifier = Modifier.align(Alignment.CenterHorizontally),
                enabled = !isLoading
            ) {
                Row {
                    Text(
                        "Already have an account? ",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        "Sign In",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview(){
    EventNotifyKETheme {
        RegisterScreen(rememberNavController())
    }
}