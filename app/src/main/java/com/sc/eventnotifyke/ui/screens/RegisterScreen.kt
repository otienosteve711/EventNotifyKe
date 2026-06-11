package com.sc.eventnotifyke.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
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
import com.sc.eventnotifyke.ui.theme.AmberAccent
import com.sc.eventnotifyke.ui.theme.EventNotifyKETheme
import com.sc.eventnotifyke.ui.theme.NavyDark
import com.sc.eventnotifyke.ui.theme.NavyMid
import com.sc.eventnotifyke.ui.theme.appTextFieldColors
import com.sc.eventnotifyke.viewmodel.AuthState
import com.sc.eventnotifyke.viewmodel.AuthViewModel

val zones = listOf(
    "Nairobi CBD", "Nairobi East", "Nairobi West",
    "Westlands", "Nairobi North", "Dagoretti"
)

val zoneSubtext = mapOf(
    "Nairobi CBD"   to "Town, Down Town, Ngara",
    "Nairobi East"  to "Umoja, Kayole, Pipeline, Donholm, Embakasi",
    "Nairobi West"  to "Lang'ata, Karen, Madaraka, Kibera, South C",
    "Westlands"     to "Kilimani, Kileleshwa, Lavington, Parklands",
    "Nairobi North" to "Kasarani, Roysambu, Githurai, Zimmerman",
    "Dagoretti"     to "Ngong Rd, Kawangware, Riruta, Satellite"
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RegisterScreen(navController: NavController, authViewModel: AuthViewModel = viewModel()) {

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var localValidationError by remember { mutableStateOf<String?>(null) }
    var selectedZone by remember { mutableStateOf("") }

    val authState by authViewModel.authState.collectAsState()
    val isLoading = authState is AuthState.Loading
    val displayErrorMessage = localValidationError ?: (authState as? AuthState.Error)?.message

    DisposableEffect(Unit) {
        onDispose { authViewModel.resetState() }
    }

    // ADDED: Navigation logic to redirect to Login upon success
    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            navController.navigate(Screen.Login.route) {
                // Remove the registration screen from the backstack
                popUpTo(Screen.Register.route) { inclusive = true }
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ── Hero ──────────────────────────────────
            Icon(
                Icons.Default.Event,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(56.dp)
            )
            Spacer(Modifier.height(8.dp))
            Row {
                Text(
                    text = "EventNotify",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = " KE",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = AmberAccent
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Join your local Nairobi zone",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )
            Spacer(Modifier.height(32.dp))

            // ── Form ──────────────────────────────────
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name") },
                leadingIcon = { Icon(Icons.Default.Person, null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading,
                colors = appTextFieldColors()
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Default.Email, null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                enabled = !isLoading,
                colors = appTextFieldColors()
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone Number") },
                leadingIcon = { Icon(Icons.Default.Phone, null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                enabled = !isLoading,
                colors = appTextFieldColors()
            )
            Spacer(Modifier.height(20.dp))

            // ── Zone Picker ───────────────────────────
            Text(
                text = "Select your Zone:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.Start),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
            Text(
                text = if (selectedZone.isEmpty()) "Tap a zone to see covered estates"
                else zoneSubtext[selectedZone] ?: "",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (selectedZone.isEmpty()) FontWeight.Normal else FontWeight.Medium,
                color = if (selectedZone.isEmpty())
                    MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                else MaterialTheme.colorScheme.primary,
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
                        label = { Text(zone, style = MaterialTheme.typography.labelMedium) },
                        enabled = !isLoading,
                        shape = RoundedCornerShape(8.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f),
                            labelColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        leadingIcon = if (selected) {
                            { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) }
                        } else null
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Passwords ─────────────────────────────
            OutlinedTextField(
                value = password,
                onValueChange = { password = it; localValidationError = null },
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Default.Lock, null) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }, enabled = !isLoading) {
                        Icon(if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, null)
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                colors = appTextFieldColors()
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it; localValidationError = null },
                label = { Text("Confirm Password") },
                leadingIcon = { Icon(Icons.Default.Lock, null) },
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }, enabled = !isLoading) {
                        Icon(if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, null)
                    }
                },
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                colors = appTextFieldColors()
            )
            Spacer(Modifier.height(20.dp))

            // ── Error / Button ────────────────────────
            if (displayErrorMessage != null) {
                Text(
                    text = displayErrorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(Modifier.height(8.dp))
            } else {
                Spacer(Modifier.height(28.dp))
            }

            Button(
                onClick = {
                    authViewModel.resetState()
                    if (password != confirmPassword) localValidationError = "Passwords do not match!"
                    else authViewModel.register(name.trim(), email.trim(), phone.trim(), selectedZone, password.trim(), "user")
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = name.isNotBlank() && email.isNotBlank() && phone.isNotBlank() && selectedZone.isNotBlank() && password.isNotBlank() && confirmPassword.isNotBlank() && !isLoading
            ) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                else Text("Create Account", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(16.dp))

            TextButton(onClick = { authViewModel.resetState(); navController.popBackStack() }, enabled = !isLoading) {
                Text(
                    text = "Already have an account? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
                Text(
                    text = "Sign In",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    EventNotifyKETheme {
        RegisterScreen(rememberNavController())
    }
}