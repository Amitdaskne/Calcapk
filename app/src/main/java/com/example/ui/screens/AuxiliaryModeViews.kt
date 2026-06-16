package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CalculatorMode
import com.example.ui.theme.*
import com.example.viewmodel.CalculatorViewModel
import kotlin.math.abs

// --- 1. BASE-N VIEW ---
@Composable
fun BaseNView(viewModel: CalculatorViewModel) {
    val focusManager = LocalFocusManager.current
    Card(
        modifier = Modifier.fillMaxWidth().padding(12.dp),
        colors = CardDefaults.cardColors(containerColor = HardwareBody),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                "Base-N Numeral Conversions",
                color = ShiftAmber,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Display of conversions
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Black.copy(alpha = 0.4f))
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Active System:", color = Color.Gray, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        Text(viewModel.baseNNumberSystem, color = ShiftAmber, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                    Text(
                        viewModel.baseNInput,
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.End,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Radio system select
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf("DEC", "HEX", "BIN", "OCT").forEach { base ->
                    val active = viewModel.baseNNumberSystem == base
                    Button(
                        onClick = { viewModel.onKeyPress(base) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (active) ShiftAmber else FunctionCharcoal,
                            contentColor = if (active) Color.Black else Color.White
                        ),
                        modifier = Modifier.weight(1f).padding(horizontal = 2.dp).height(36.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(base, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Text("Hex Inputs (A-F Enabled in HEX System):", color = Color.Gray, fontSize = 10.sp)
            Spacer(modifier = Modifier.height(6.dp))

            // Extra Hex Keys row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf("A", "B", "C", "D", "E", "F").forEach { letter ->
                    val isHex = viewModel.baseNNumberSystem == "HEX"
                    Button(
                        onClick = { viewModel.onBaseNKeyPress(letter) },
                        enabled = isHex,
                        colors = ButtonDefaults.buttonColors(containerColor = NumberSlate, contentColor = Color.White),
                        modifier = Modifier.weight(1f).height(38.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(letter, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Binary key pad
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { viewModel.onBaseNKeyPress("0") },
                    colors = ButtonDefaults.buttonColors(containerColor = NumberSlate, contentColor = Color.White),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("0", fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = { viewModel.onBaseNKeyPress("1") },
                    colors = ButtonDefaults.buttonColors(containerColor = NumberSlate, contentColor = Color.White),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("1", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Extra inputs text field
            OutlinedTextField(
                value = viewModel.baseNInput,
                onValueChange = {
                    viewModel.baseNInput = it.uppercase()
                },
                modifier = Modifier.fillMaxWidth().testTag("base_n_input"),
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White, fontFamily = FontFamily.Monospace),
                label = { Text("Manual Input / Result System Values", color = Color.Gray) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ShiftAmber,
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = ShiftAmber
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                trailingIcon = {
                    IconButton(onClick = { viewModel.onKeyPress("AC") }) {
                        Text("AC", color = ClearCrimson, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            )
        }
    }
}


// --- 2. MATRIX SENSOR VIEW ---
@Composable
fun MatrixView(viewModel: CalculatorViewModel) {
    val mNames = listOf("A", "B", "C")
    var inputVal by remember { mutableStateOf("") }
    
    Card(
        modifier = Modifier.fillMaxWidth().padding(12.dp),
        colors = CardDefaults.cardColors(containerColor = HardwareBody),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text("Matrix Operations (3x3 Matrix Registry)", color = ShiftAmber, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))

            // Selection tabs
            Row(modifier = Modifier.fillMaxWidth()) {
                mNames.forEach { name ->
                    val selected = viewModel.selectedMatrixName == name
                    Button(
                        onClick = { viewModel.selectedMatrixName = name },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selected) ShiftAmber else FunctionCharcoal,
                            contentColor = if (selected) Color.Black else Color.White
                        ),
                        modifier = Modifier.weight(1f).padding(horizontal = 2.dp)
                    ) {
                        Text("Mat $name", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3x3 editable cells
            val matrixData = when (viewModel.selectedMatrixName) {
                "A" -> viewModel.matrixA.collectAsState().value
                "B" -> viewModel.matrixB.collectAsState().value
                else -> viewModel.matrixC.collectAsState().value
            }

            Text("Edit Cells for Matrix ${viewModel.selectedMatrixName}:", color = Color.Gray, fontSize = 11.sp)
            Spacer(modifier = Modifier.height(6.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                for (r in 0..2) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        for (c in 0..2) {
                            val activeCell = viewModel.activeMatrixCellRow == r && viewModel.activeMatrixCellCol == c
                            val valStr = String.format("%.2f", matrixData[r][c]).replace(".00", "")
                            
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (activeCell) Color(0xFF333A42) else Color.Black.copy(alpha = 0.3f))
                                    .border(1.dp, if (activeCell) ShiftAmber else Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                                    .clickable {
                                        viewModel.activeMatrixCellRow = r
                                        viewModel.activeMatrixCellCol = c
                                        inputVal = matrixData[r][c].toString().replace("0.0", "")
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = valStr,
                                    color = if (activeCell) ShiftAmber else Color.White,
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Input editor row
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = inputVal,
                    onValueChange = {
                        inputVal = it
                        val dVal = it.toDoubleOrNull() ?: 0.0
                        viewModel.updateMatrixCell(
                            viewModel.selectedMatrixName,
                            viewModel.activeMatrixCellRow,
                            viewModel.activeMatrixCellCol,
                            dVal
                        )
                    },
                    modifier = Modifier.weight(1.0f).padding(end = 8.dp).testTag("matrix_cell_input"),
                    textStyle = TextStyle(color = Color.White, fontFamily = FontFamily.Monospace),
                    label = { Text("Cell [${viewModel.activeMatrixCellRow + 1},${viewModel.activeMatrixCellCol + 1}] Value", color = Color.LightGray) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ShiftAmber,
                        unfocusedBorderColor = Color.DarkGray
                    )
                )

                Button(
                    onClick = {
                        val row = viewModel.activeMatrixCellRow
                        val col = viewModel.activeMatrixCellCol
                        if (col < 2) {
                            viewModel.activeMatrixCellCol++
                        } else if (row < 2) {
                            viewModel.activeMatrixCellRow++
                            viewModel.activeMatrixCellCol = 0
                        }
                        inputVal = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FunctionCharcoal),
                    modifier = Modifier.height(52.dp)
                ) {
                    Text("OK", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Text("Matrix Analysis Tools:", color = Color.Gray, fontSize = 11.sp)
            Spacer(modifier = Modifier.height(6.dp))

            // Solver operators matrix buttons
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Button(
                    onClick = { viewModel.calculateMatrixDeterminant(viewModel.selectedMatrixName) },
                    colors = ButtonDefaults.buttonColors(containerColor = FunctionCharcoal),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(2.dp)
                ) { Text("Det", fontSize = 10.sp) }
                Button(
                    onClick = { viewModel.calculateMatrixTranspose(viewModel.selectedMatrixName) },
                    colors = ButtonDefaults.buttonColors(containerColor = FunctionCharcoal),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(2.dp)
                ) { Text("Trn", fontSize = 10.sp) }
                Button(
                    onClick = { viewModel.calculateInverseMatrix(viewModel.selectedMatrixName) },
                    colors = ButtonDefaults.buttonColors(containerColor = FunctionCharcoal),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(2.dp)
                ) { Text("Inv", fontSize = 10.sp) }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Button(
                    onClick = { viewModel.multiplyMatrices("A", "B") },
                    colors = ButtonDefaults.buttonColors(containerColor = NumberSlate),
                    modifier = Modifier.weight(1f)
                ) { Text("Mat A × Mat B", fontSize = 10.sp) }
                Button(
                    onClick = { viewModel.multiplyMatrices("A", "C") },
                    colors = ButtonDefaults.buttonColors(containerColor = NumberSlate),
                    modifier = Modifier.weight(1f)
                ) { Text("Mat A × Mat C", fontSize = 10.sp) }
            }

            // Calculations Output Matrix Box
            val out = viewModel.matrixResultDisplay
            if (out != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Black.copy(alpha = 0.5f))
                        .border(1.dp, ShiftAmber.copy(0.4f), RoundedCornerShape(6.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("MATRIX RESOLUTION", color = ShiftAmber, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("Clear", color = Color.Gray, fontSize = 10.sp, modifier = Modifier.clickable { viewModel.matrixResultDisplay = null })
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            out,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}


// --- 3. EQN EQUATION SOLVER ---
@Composable
fun EquationView(viewModel: CalculatorViewModel) {
    val coeffData = viewModel.eqnCoefficients.collectAsState().value
    
    Card(
        modifier = Modifier.fillMaxWidth().padding(12.dp),
        colors = CardDefaults.cardColors(containerColor = HardwareBody),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text("Equation Solver (EQN)", color = ShiftAmber, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))

            // Select Equation Types
            Row(modifier = Modifier.fillMaxWidth()) {
                val types = listOf(
                    "LINEAR2" to "2 Var Linear",
                    "LINEAR3" to "3 Var Linear",
                    "QUADRATIC" to "Quadratic ax²",
                    "CUBIC" to "Cubic ax³"
                )
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.height(86.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(count = types.size) { i ->
                        val (code, name) = types[i]
                        val selected = viewModel.eqnType == code
                        Button(
                            onClick = {
                                viewModel.eqnType = code
                                viewModel.eqnResults = emptyList()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selected) ShiftAmber else FunctionCharcoal,
                                contentColor = if (selected) Color.Black else Color.White
                            ),
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.height(38.dp)
                        ) {
                            Text(name, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Rendering coefficient matrix depending on selected equation type
            val (cols, rows) = when (viewModel.eqnType) {
                "LINEAR2" -> 3 to 2 // a1 b1 c1, a2 b2 c2
                "LINEAR3" -> 4 to 3 // a1 b1 c1 d1, etc.
                "QUADRATIC" -> 3 to 1 // a b c
                else -> 4 to 1 // a b c d
            }

            Text("Enter Equation Coefficients:", color = Color.Gray, fontSize = 11.sp, modifier = Modifier.padding(bottom = 6.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                if (viewModel.eqnType == "LINEAR2") {
                    EqnCoeffRow(r = 0, cols = 3, coeffData, viewModel)
                    EqnCoeffRow(r = 1, cols = 3, coeffData, viewModel)
                } else if (viewModel.eqnType == "LINEAR3") {
                    EqnCoeffRow(r = 0, cols = 4, coeffData, viewModel)
                    EqnCoeffRow(r = 1, cols = 4, coeffData, viewModel)
                    EqnCoeffRow(r = 2, cols = 4, coeffData, viewModel)
                } else if (viewModel.eqnType == "QUADRATIC") {
                    EqnCoeffRow(r = 0, cols = 3, coeffData, viewModel)
                } else {
                    EqnCoeffRow(r = 0, cols = 4, coeffData, viewModel)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        // Reset all coefficients
                        for (r in 0..3) {
                            for (c in 0..3) {
                                viewModel.updateEqnCoefficient(r, c, 0.0)
                            }
                        }
                        viewModel.eqnResults = emptyList()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ClearCrimson),
                    modifier = Modifier.weight(0.4f)
                ) {
                    Text("Clear", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Button(
                    onClick = {
                        viewModel.solveEquation()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ShiftAmber, contentColor = Color.Black),
                    modifier = Modifier.weight(1f).testTag("eqn_solve_button")
                ) {
                    Text("SOLVE SYSTEM", fontWeight = FontWeight.Black)
                }
            }

            // Equation Output listing
            val roots = viewModel.eqnResults
            if (roots.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Black.copy(alpha = 0.45f))
                        .border(1.6.dp, ShiftAmber.copy(0.4f), RoundedCornerShape(6.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Text("RESOLVED ROOTS", color = ShiftAmber, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                        Spacer(modifier = Modifier.height(6.dp))
                        roots.forEach { root ->
                            Text(
                                root,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EqnCoeffRow(r: Int, cols: Int, coeffData: Array<DoubleArray>, viewModel: CalculatorViewModel) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Eq ${r+1}:",
            color = Color.LightGray,
            fontSize = 11.sp,
            modifier = Modifier.width(42.dp)
        )

        EqnCell(r, 0, coeffData, viewModel)
        EqnCell(r, 1, coeffData, viewModel)
        EqnCell(r, 2, coeffData, viewModel)
        if (cols > 3) {
            EqnCell(r, 3, coeffData, viewModel)
        }
    }
}

@Composable
fun RowScope.EqnCell(r: Int, c: Int, coeffData: Array<DoubleArray>, viewModel: CalculatorViewModel) {
    val activeCell = coeffData[r][c]
    val label = when (c) {
        0 -> "a"
        1 -> "b"
        2 -> "c"
        else -> "d"
    }
    var cellTextState = remember(viewModel.eqnType, r, c) { mutableStateOf(activeCell.toString().replace("0.0", "")) }

    OutlinedTextField(
        value = cellTextState.value,
        onValueChange = { newValue ->
            cellTextState.value = newValue
            val numeric = newValue.toDoubleOrNull() ?: 0.0
            viewModel.updateEqnCoefficient(r, c, numeric)
        },
        textStyle = TextStyle(color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 12.sp, textAlign = TextAlign.Center),
        modifier = Modifier.weight(1f).testTag("eqn_coeff_input_${r}_${c}"),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        label = { Text(label, color = ShiftAmber, fontSize = 8.sp) },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = ShiftAmber,
            unfocusedBorderColor = Color.Gray.copy(0.4f)
        )
    )
}


// --- 4. TABLE MODE VIEW ---
@Composable
fun TableView(viewModel: CalculatorViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(12.dp),
        colors = CardDefaults.cardColors(containerColor = HardwareBody),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text("Table Range Generator (TABLE)", color = ShiftAmber, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = viewModel.tableFunction.value,
                onValueChange = { viewModel.tableFunction.value = it },
                label = { Text("Function f(X) (Must contain variable X)", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth().testTag("table_fn_input"),
                textStyle = TextStyle(color = Color.White, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ShiftAmber,
                    unfocusedBorderColor = Color.Gray
                ),
                trailingIcon = {
                    Text("X", color = AlphaViolet, fontWeight = FontWeight.Black, modifier = Modifier.padding(end = 8.dp))
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Parameters row
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                var startStr by remember { mutableStateOf(viewModel.tableStart.toString()) }
                var endStr by remember { mutableStateOf(viewModel.tableEnd.toString()) }
                var stepStr by remember { mutableStateOf(viewModel.tableStep.toString()) }

                OutlinedTextField(
                    value = startStr,
                    onValueChange = {
                        startStr = it
                        viewModel.tableStart = it.toDoubleOrNull() ?: viewModel.tableStart
                    },
                    label = { Text("Start", fontSize = 10.sp, color = Color.LightGray) },
                    modifier = Modifier.weight(1f).testTag("table_start_val"),
                    textStyle = TextStyle(color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 12.sp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ShiftAmber)
                )

                OutlinedTextField(
                    value = endStr,
                    onValueChange = {
                        endStr = it
                        viewModel.tableEnd = it.toDoubleOrNull() ?: viewModel.tableEnd
                    },
                    label = { Text("End", fontSize = 10.sp, color = Color.LightGray) },
                    modifier = Modifier.weight(1f).testTag("table_end_val"),
                    textStyle = TextStyle(color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 12.sp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ShiftAmber)
                )

                OutlinedTextField(
                    value = stepStr,
                    onValueChange = {
                        stepStr = it
                        viewModel.tableStep = it.toDoubleOrNull() ?: viewModel.tableStep
                    },
                    label = { Text("Step", fontSize = 10.sp, color = Color.LightGray) },
                    modifier = Modifier.weight(1f).testTag("table_step_val"),
                    textStyle = TextStyle(color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 12.sp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ShiftAmber)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { viewModel.generateTable() },
                colors = ButtonDefaults.buttonColors(containerColor = ShiftAmber, contentColor = Color.Black),
                modifier = Modifier.fillMaxWidth().testTag("table_gen_btn")
            ) {
                Text("GENERATE VALUES TABLE", fontWeight = FontWeight.Bold)
            }

            val tableResults = viewModel.tableRows
            if (tableResults.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                
                // Header Table
                Row(
                    modifier = Modifier.fillMaxWidth().background(Color(0xFF141618)).padding(vertical = 4.dp, horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Index", color = Color.Gray, fontSize = 10.sp, modifier = Modifier.width(36.dp), fontFamily = FontFamily.Monospace)
                    Text("x", color = ShiftAmber, fontSize = 10.sp, modifier = Modifier.weight(1f), fontFamily = FontFamily.Monospace, textAlign = TextAlign.Center)
                    Text("f(x)", color = Color.Cyan, fontSize = 10.sp, modifier = Modifier.weight(1f), fontFamily = FontFamily.Monospace, textAlign = TextAlign.End)
                }

                Box(modifier = Modifier.height(160.dp)) {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(tableResults.size) { idx ->
                            val item = tableResults[idx]
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(0.6.dp, Color.Gray.copy(0.15f))
                                    .padding(vertical = 6.dp, horizontal = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text((idx + 1).toString(), color = Color.Gray, fontSize = 11.sp, modifier = Modifier.width(36.dp), fontFamily = FontFamily.Monospace)
                                Text(item.first.toString(), color = Color.White, fontSize = 12.sp, modifier = Modifier.weight(1f), fontFamily = FontFamily.Monospace, textAlign = TextAlign.Center)
                                Text(item.second, color = Color.LightGray, fontSize = 12.sp, modifier = Modifier.weight(1f), fontFamily = FontFamily.Monospace, textAlign = TextAlign.End, maxLines = 1)
                            }
                        }
                    }
                }
            }
        }
    }
}


// --- 5. STAT STATS DATA VIEW ---
@Composable
fun StatisticsView(viewModel: CalculatorViewModel) {
    val statListByFlow = viewModel.statDataSet.collectAsState().value
    var inputX by remember { mutableStateOf("") }
    var inputY by remember { mutableStateOf("") }
    var enableRegression by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().padding(12.dp),
        colors = CardDefaults.cardColors(containerColor = HardwareBody),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Statistics Registry (STAT)", color = ShiftAmber, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                
                // Toggle mode 1-Var or Regression
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Reg (A+Bx)", color = Color.Gray, fontSize = 9.sp)
                    Checkbox(
                        checked = enableRegression,
                        onCheckedChange = {
                            enableRegression = it
                            viewModel.clearStatDataSet()
                        },
                        colors = CheckboxDefaults.colors(checkedColor = ShiftAmber)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))

            // Form entry
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = inputX,
                    onValueChange = { inputX = it },
                    label = { Text("Value X", color = Color.LightGray, fontSize = 11.sp) },
                    modifier = Modifier.weight(1f).padding(end = 4.dp).testTag("stat_x_input"),
                    textStyle = TextStyle(color = Color.White, fontFamily = FontFamily.Monospace),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ShiftAmber)
                )

                if (enableRegression) {
                    OutlinedTextField(
                        value = inputY,
                        onValueChange = { inputY = it },
                        label = { Text("Value Y", color = Color.LightGray, fontSize = 11.sp) },
                        modifier = Modifier.weight(1f).padding(end = 4.dp).testTag("stat_y_input"),
                        textStyle = TextStyle(color = Color.White, fontFamily = FontFamily.Monospace),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ShiftAmber)
                    )
                }

                Button(
                    onClick = {
                        val xVal = inputX.toDoubleOrNull()
                        val yVal = if (enableRegression) (inputY.toDoubleOrNull() ?: 0.0) else 0.0
                        if (xVal != null) {
                            viewModel.addStatDataPoint(xVal, yVal)
                            inputX = ""
                            inputY = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ShiftAmber, contentColor = Color.Black),
                    modifier = Modifier.height(52.dp).testTag("stat_add_button")
                ) {
                    Text("+ ADD", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { viewModel.clearStatDataSet() },
                    colors = ButtonDefaults.buttonColors(containerColor = ClearCrimson),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Clear All Data", fontSize = 11.sp)
                }
            }

            // Scroll list of entered points
            if (statListByFlow.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text("Collected Dataset (N = ${statListByFlow.size}):", color = Color.Gray, fontSize = 11.sp, modifier = Modifier.padding(bottom = 4.dp))

                Box(modifier = Modifier.height(100.dp)) {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(statListByFlow) { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(0.5.dp, Color.Gray.copy(alpha = 0.15f))
                                    .padding(vertical = 4.dp, horizontal = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("x = ${item.first}", color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                                if (enableRegression) {
                                    Text("y = ${item.second}", color = Color.Cyan, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                // Summary calculations display
                val summary = viewModel.statSummaryResult
                if (summary.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.Black.copy(0.4f))
                            .border(1.dp, ShiftAmber.copy(0.3f), RoundedCornerShape(6.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Text("STATISTICAL SUMMARY DIAGNOSTICS", color = ShiftAmber, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            
                            // 2 Column details
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                summary.forEach { (key, value) ->
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(key, color = Color.Gray, fontSize = 11.sp)
                                        Text(value, color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


// --- 6. CONVERSION VIEW (CONV) ---
@Composable
fun ConversionView(viewModel: CalculatorViewModel) {
    val categories = listOf("Length", "Mass", "Volume", "Speed", "Temperature")
    var selectedCat by remember { mutableStateOf("Length") }
    var rawInputText by remember { mutableStateOf("") }
    
    // Struct representing options
    data class UnitOption(val label: String, val fromUnit: String, val toUnit: String, val convert: (Double) -> Double)
    val conversionLibrary = mapOf(
        "Length" to listOf(
            UnitOption("Inches ⇄ Centimeters", "in", "cm") { it * 2.54 },
            UnitOption("Centimeters ⇄ Inches", "cm", "in") { it / 2.54 },
            UnitOption("Feet ⇄ Meters", "ft", "m") { it * 0.3048 },
            UnitOption("Meters ⇄ Feet", "m", "ft") { it / 0.3048 },
            UnitOption("Yards ⇄ Meters", "yd", "m") { it * 0.9144 },
            UnitOption("Meters ⇄ Yards", "m", "yd") { it / 0.9144 },
            UnitOption("Miles ⇄ Kilometers", "mile", "km") { it * 1.60934 },
            UnitOption("Kilometers ⇄ Miles", "km", "mile") { it / 1.60934 }
        ),
        "Mass" to listOf(
            UnitOption("Pounds ⇄ Kilograms", "lb", "kg") { it * 0.453592 },
            UnitOption("Kilograms ⇄ Pounds", "kg", "lb") { it / 0.453592 },
            UnitOption("Ounces ⇄ Grams", "oz", "g") { it * 28.3495 },
            UnitOption("Grams ⇄ Ounces", "g", "oz") { it / 28.3495 }
        ),
        "Volume" to listOf(
            UnitOption("US Gallons ⇄ Liters", "gal", "L") { it * 3.78541 },
            UnitOption("Liters ⇄ US Gallons", "L", "gal") { it / 3.78541 }
        ),
        "Speed" to listOf(
            UnitOption("Km/h ⇄ Meters/sec", "km/h", "m/s") { it / 3.6 },
            UnitOption("Meters/sec ⇄ Km/h", "m/s", "km/h") { it * 3.6 }
        ),
        "Temperature" to listOf(
            UnitOption("Celsius ⇄ Fahrenheit", "°C", "°F") { it * 1.8 + 32.0 },
            UnitOption("Fahrenheit ⇄ Celsius", "°F", "°C") { (it - 32.0) / 1.8 }
        )
    )

    var currentOpt by remember(selectedCat) { mutableStateOf(conversionLibrary[selectedCat]!![0]) }

    Card(
        modifier = Modifier.fillMaxWidth().padding(12.dp),
        colors = CardDefaults.cardColors(containerColor = HardwareBody),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text("Scientific Unit Transformations (CONV)", color = ShiftAmber, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            // Cat selectors
            Row(modifier = Modifier.fillMaxWidth()) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.height(80.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(count = categories.size) { i ->
                        val catName = categories[i]
                        val active = selectedCat == catName
                        Button(
                            onClick = { selectedCat = catName },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (active) ShiftAmber else FunctionCharcoal,
                                contentColor = if (active) Color.Black else Color.White
                            ),
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text(catName, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Dropdown selection (we simulate using elegant scroll lists when clicked)
            Text("Select Specific Rule Conversion:", color = Color.Gray, fontSize = 10.sp)
            Spacer(modifier = Modifier.height(4.dp))
            
            Box(modifier = Modifier.height(80.dp).fillMaxWidth()) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    val list = conversionLibrary[selectedCat]!!
                    items(list) { opt ->
                        val isSel = currentOpt.label == opt.label
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isSel) Color(0xFF333A42) else Color.Transparent)
                                .clickable { currentOpt = opt }
                                .padding(vertical = 6.dp, horizontal = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(opt.label, color = if (isSel) ShiftAmber else Color.White, fontSize = 12.sp)
                            Text("[${opt.fromUnit} ➜ ${opt.toUnit}]", color = if (isSel) ShiftAmber else Color.Gray, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = rawInputText,
                    onValueChange = { rawInputText = it },
                    label = { Text("Magnitude to Convert (${currentOpt.fromUnit})", color = Color.Gray) },
                    textStyle = TextStyle(color = Color.White, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold),
                    modifier = Modifier.weight(1.0f).padding(end = 8.dp).testTag("conv_input_val"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ShiftAmber,
                        unfocusedBorderColor = Color.DarkGray
                    )
                )

                Button(
                    onClick = { rawInputText = "" },
                    colors = ButtonDefaults.buttonColors(containerColor = ClearCrimson),
                    modifier = Modifier.height(52.dp)
                ) {
                    Text("CL")
                }
            }

            val parsedMagnitude = rawInputText.toDoubleOrNull()
            if (parsedMagnitude != null) {
                val converted = currentOpt.convert(parsedMagnitude)
                
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Black.copy(0.4f))
                        .border(1.dp, ShiftAmber.copy(0.3f), RoundedCornerShape(6.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "$parsedMagnitude ${currentOpt.fromUnit}",
                            color = Color.Gray,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            "equals",
                            color = ShiftAmber,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "${String.format("%.6f", converted).replace(Regex("0+$"), "").replace(Regex("\\.$"), "")} ${currentOpt.toUnit}",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}


// --- 7. CONSTANTS VIEW (CONST) ---
@Composable
fun ConstantsView(viewModel: CalculatorViewModel) {
    data class PhysConstant(val symbol: String, val name: String, val value: String, val description: String)
    val constantsList = listOf(
        PhysConstant("c", "Speed of Light", "299792458", "Speed of EM wave in vacuum (m/s)"),
        PhysConstant("h", "Planck Constant", "6.62607015e-34", "Action quantum constant (J·s)"),
        PhysConstant("G", "Gravitational Constant", "6.6743e-11", "Newtonian gravity coeff (m³/kg·s²)"),
        PhysConstant("g", "Standard Gravity", "9.80665", "Acceleration on Earth (m/s²)"),
        PhysConstant("e", "Elementary Charge", "1.60217663e-19", "Charge of a single electron (C)"),
        PhysConstant("m_e", "Electron Mass", "9.1093837e-31", "Rest mass of electron (kg)"),
        PhysConstant("m_p", "Proton Mass", "1.67262192e-27", "Rest mass of proton (kg)"),
        PhysConstant("m_n", "Neutron Mass", "1.67492749e-27", "Rest mass of neutron (kg)"),
        PhysConstant("N_A", "Avogadro Constant", "6.02214076e23", "Constituents per mole (mol⁻¹)"),
        PhysConstant("R", "Molar Gas Constant", "8.31446261", "Universal gas ideal constant (J/mol·K)"),
        PhysConstant("k_B", "Boltzmann Constant", "1.380649e-23", "Energy particle temp scaling (J/K)"),
        PhysConstant("F", "Faraday Constant", "96485.33212", "Electric charge per mole (C/mol)")
    )

    Card(
        modifier = Modifier.fillMaxWidth().padding(12.dp),
        colors = CardDefaults.cardColors(containerColor = HardwareBody),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text("Physical Constants Library (CONST)", color = ShiftAmber, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Tap any constant to load its exact mathematical value directly inside your active COMP screen expression calculation!", color = Color.Gray, fontSize = 9.sp)
            
            Spacer(modifier = Modifier.height(10.dp))

            Box(modifier = Modifier.height(210.dp)) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(constantsList) { const ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(0.5.dp, Color.Gray.copy(alpha = 0.15f))
                                .clickable {
                                    viewModel.insertText(const.value)
                                }
                                .padding(vertical = 10.dp, horizontal = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        const.symbol,
                                        color = ShiftAmber,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontFamily = FontFamily.Serif,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                    Text(
                                        const.name,
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(const.description, color = Color.Gray, fontSize = 9.sp)
                            }

                            Text(
                                const.value,
                                color = Color.Cyan,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
