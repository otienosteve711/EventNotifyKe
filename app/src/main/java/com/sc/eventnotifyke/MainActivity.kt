package com.sc.eventnotifyke

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.sc.eventnotifyke.navigation.NavGraph
import com.sc.eventnotifyke.ui.theme.EventNotifyKETheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EventNotifyKETheme {
                // create the nav controller
                val navController = rememberNavController()
                // pass it to the nav graph
                NavGraph(navController = navController)
            }
        }
    }
}

