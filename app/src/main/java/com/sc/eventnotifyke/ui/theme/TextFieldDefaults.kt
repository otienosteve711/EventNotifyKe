package com.sc.eventnotifyke.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable

@Composable
fun appTextFieldColors(): TextFieldColors = OutlinedTextFieldDefaults.colors(
    focusedTextColor           = MaterialTheme.colorScheme.onBackground,
    unfocusedTextColor         = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
    focusedLabelColor          = MaterialTheme.colorScheme.primary,
    unfocusedLabelColor        = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
    focusedLeadingIconColor    = MaterialTheme.colorScheme.primary,
    unfocusedLeadingIconColor  = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
    focusedTrailingIconColor   = MaterialTheme.colorScheme.primary,
    unfocusedTrailingIconColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
    focusedBorderColor         = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor       = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
    cursorColor                = MaterialTheme.colorScheme.primary,
    disabledTextColor          = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
    disabledBorderColor        = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f),
    disabledLeadingIconColor   = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
    disabledLabelColor         = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
)