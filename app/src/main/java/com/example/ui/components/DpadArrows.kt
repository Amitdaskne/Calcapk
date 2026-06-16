package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.FunctionCharcoal
import com.example.ui.theme.ThemePrimary
import com.example.viewmodel.CalculatorViewModel

@Composable
fun DpadArrows(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    // Renders as a physical circular D-Pad overlay button group
    Box(
        modifier = modifier
            .size(100.dp)
            .clip(CircleShape)
            .background(Color(0xFF141618))
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        // Inner silver layout disk
        Box(
            modifier = Modifier
                .size(92.dp)
                .clip(CircleShape)
                .background(FunctionCharcoal)
        ) {
            // NORTH: UP Arrow (Navigate History previous)
            IconButton(
                onClick = {
                    val hist = viewModel.calculationHistory.value
                    if (hist.isNotEmpty()) {
                        // Select first history item as entry point
                        viewModel.selectHistoryItem(hist.first())
                    }
                },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .size(34.dp)
                    .testTag("dpad_up")
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = "Up: Previous History",
                    tint = Color.White.copy(alpha = 0.9f)
                )
            }

            // SOUTH: DOWN Arrow (Clear current expression or cycle history)
            IconButton(
                onClick = {
                    viewModel.clearAll()
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .size(34.dp)
                    .testTag("dpad_down")
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Down: Clear Display",
                    tint = Color.White.copy(alpha = 0.9f)
                )
            }

            // WEST: LEFT Arrow (Step cursor back)
            IconButton(
                onClick = {
                    if (viewModel.cursorPosition > 0) {
                        viewModel.cursorPosition--
                    }
                },
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(34.dp)
                    .testTag("dpad_left")
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Left: Move Cursor",
                    tint = Color.White.copy(alpha = 0.9f)
                )
            }

            // EAST: RIGHT Arrow (Step cursor forward)
            IconButton(
                onClick = {
                    if (viewModel.cursorPosition < viewModel.expression.length) {
                        viewModel.cursorPosition++
                    }
                },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(34.dp)
                    .testTag("dpad_right")
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Right: Move Cursor",
                    tint = Color.White.copy(alpha = 0.9f)
                )
            }

            // Center metallic center cap core
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.Center)
                    .clip(CircleShape)
                    .background(Color(0xFF141618))
            )
        }
    }
}
