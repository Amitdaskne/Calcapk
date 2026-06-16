package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.db.CalcDatabase
import com.example.ui.CalculatorApp
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.CalculatorViewModel
import com.example.viewmodel.CalculatorViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Setup systems edge-to-edge full screen drawing
        enableEdgeToEdge()

        // Initialize persistent DB layers
        val database = CalcDatabase.getDatabase(applicationContext)
        val historyDao = database.historyDao()
        val variableDao = database.variableDao()

        // Create state ViewModel with custom Factory
        val factory = CalculatorViewModelFactory(historyDao, variableDao)
        val viewModel = ViewModelProvider(this, factory)[CalculatorViewModel::class.java]

        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    CalculatorApp(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
