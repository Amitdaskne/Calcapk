package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.CalculatorViewModel
import kotlinx.coroutines.delay

@Composable
fun RetroLCD(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    // Blinking cursor state
    var cursorOn by remember { mutableStateOf(true) }
    LaunchedEffect(viewModel.expression, viewModel.cursorPosition) {
        cursorOn = true
        while (true) {
            delay(530)
            cursorOn = !cursorOn
        }
    }

    // Outer plastic screen frame
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF1E2124))
            .border(2.dp, Color(0xFF333A42), RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        // Inner Glass LCD Display Screen
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .background(LCDBackground)
                .border(1.5.dp, Color(0xFF86927E), RoundedCornerShape(4.dp))
                .padding(vertical = 6.dp, horizontal = 10.dp)
                .heightIn(min = 106.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. LCD Status Indicators / Symbols Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Leftside Indicators (SHIFT, ALPHA)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (viewModel.isShiftActive) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(3.dp))
                                .background(ShiftAmber)
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "SHIFT",
                                color = Color.Black,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(30.dp)) // Maintain layout spacing
                    }

                    if (viewModel.isAlphaActive) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(3.dp))
                                .background(AlphaViolet)
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "ALPHA",
                                color = Color.White,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                // Middle indicator: Active Calculator Mode name
                Text(
                    text = "[ ${viewModel.activeMode.codeName} ]",
                    color = LCDText,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                // Rightside indicator: Setup state (DEG, RAD, GRA)
                Box(
                    modifier = Modifier
                        .border(1.dp, LCDText.copy(alpha = 0.5f), RoundedCornerShape(2.dp))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = viewModel.angleUnit,
                        color = LCDText,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Separator thin line (like old dot-matrix split)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .height(0.6.dp)
                    .background(LCDText.copy(alpha = 0.12f))
            )

            // 2. Formula Expression Input Area (With text caret/cursor)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.0f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Center
            ) {
                val expr = viewModel.expression
                val cPos = viewModel.cursorPosition

                if (expr.isEmpty()) {
                    // Draw blinking block cursor when empty
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (cursorOn) {
                            Box(
                                modifier = Modifier
                                    .width(7.dp)
                                    .height(13.dp)
                                    .background(LCDText)
                            )
                        } else {
                            Spacer(modifier = Modifier.width(7.dp))
                        }
                        Text(
                            text = "0",
                            color = LCDText.copy(alpha = 0.35f),
                            fontSize = 16.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(start = 2.dp)
                        )
                    }
                } else {
                    // Split and render with inline blinking custom cursor
                    val safeCPos = cPos.coerceIn(0, expr.length)
                    val before = expr.substring(0, safeCPos)
                    val after = expr.substring(safeCPos)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = before,
                            color = LCDText,
                            fontSize = 15.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        
                        // Caret/cursor drawing
                        if (cursorOn) {
                            Box(
                                modifier = Modifier
                                    .width(2.dp)
                                    .height(16.dp)
                                    .background(LCDText)
                            )
                        } else {
                            Spacer(modifier = Modifier.width(2.dp))
                        }

                        Text(
                            text = after,
                            color = LCDText,
                            fontSize = 15.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            // Spacer
            Spacer(modifier = Modifier.height(2.dp))

            // 3. Evaluation Result / Output Line (Large right-aligned)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                // Badge indicating fraction switch support
                if (viewModel.isShowingFraction) {
                    Box(
                        modifier = Modifier
                            .border(0.8.dp, LCDText, RoundedCornerShape(2.dp))
                            .background(LCDText.copy(alpha = 0.08f))
                            .padding(horizontal = 3.dp, vertical = 0.5.dp)
                    ) {
                        Text(
                            text = "S⇔D",
                            color = LCDText,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                // Render result text centered or right-aligned
                val resultText = viewModel.resultDisplay
                val errorMsg = viewModel.activeError

                if (errorMsg != null) {
                    Text(
                        text = errorMsg,
                        color = Color(0xFFA33B3B), // Soft high contrast math error maroon
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Text(
                        text = resultText,
                        color = LCDText,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
