package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CalculatorMode
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.viewmodel.CalculatorViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorApp(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    var showModeSelector by remember { mutableStateOf(false) }
    var showHelpSheet by remember { mutableStateOf(false) }
    var showVarSheet by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "ClassWiz fx-991EX",
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            letterSpacing = 1.sp,
                            color = ShiftAmber
                        )
                        Text(
                            text = "High Fidelity Scientific Emulator",
                            fontSize = 9.sp,
                            color = Color.LightGray
                        )
                    }
                },
                actions = {
                    // Settings/Memory toggle
                    IconButton(
                        onClick = { showVarSheet = true },
                        modifier = Modifier.testTag("var_sheet_btn")
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(AlphaViolet)
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text(
                                "RCL",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // Help panel key
                    IconButton(
                        onClick = { showHelpSheet = true },
                        modifier = Modifier.testTag("help_icon_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "User Reference Guide",
                            tint = Color.White
                        )
                    }

                    // Angle Toggle direct quick trigger (DEG -> RAD -> GRA)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(FunctionCharcoal)
                            .clickable {
                                viewModel.angleUnit = when (viewModel.angleUnit) {
                                    "DEG" -> "RAD"
                                    "RAD" -> "GRA"
                                    else -> "DEG"
                                }
                            }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = viewModel.angleUnit,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = ShiftAmber,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF131517),
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF131517)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .widthIn(max = 600.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Retro LCD Display Panel
            RetroLCD(
                viewModel = viewModel,
                modifier = Modifier.padding(12.dp)
            )

            // Dynamic Action Panel (Arrows & Setup control)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Quick Controls
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // SHIFT Toggle Button
                    Box(
                        modifier = Modifier
                            .size(width = 62.dp, height = 36.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (viewModel.isShiftActive) ShiftAmber else FunctionCharcoal)
                            .border(1.dp, ShiftAmber.copy(0.3f), RoundedCornerShape(6.dp))
                            .clickable { viewModel.isShiftActive = !viewModel.isShiftActive }
                            .testTag("shift_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("SHIFT", fontSize = 10.sp, fontWeight = FontWeight.Black, color = if (viewModel.isShiftActive) Color.Black else ShiftAmber)
                        }
                    }

                    // ALPHA Toggle Button
                    Box(
                        modifier = Modifier
                            .size(width = 62.dp, height = 36.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (viewModel.isAlphaActive) AlphaViolet else FunctionCharcoal)
                            .border(1.dp, AlphaViolet.copy(0.3f), RoundedCornerShape(6.dp))
                            .clickable { viewModel.isAlphaActive = !viewModel.isAlphaActive }
                            .testTag("alpha_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("ALPHA", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (viewModel.isAlphaActive) Color.Black else AlphaViolet)
                    }
                }

                // 4-Way D-Pad Arrows
                DpadArrows(viewModel = viewModel)

                // Right Quick Controls
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // MODE/SETUP trigger button
                    Box(
                        modifier = Modifier
                            .size(width = 62.dp, height = 36.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(FunctionCharcoal)
                            .border(1.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                            .clickable { showModeSelector = !showModeSelector }
                            .testTag("mode_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("MODE", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    }

                    // Reset AC button
                    Box(
                        modifier = Modifier
                            .size(width = 62.dp, height = 36.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(ClearCrimson)
                            .clickable { viewModel.clearAll() }
                            .testTag("on_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("ON", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Sub-Screen / View according to Mode and overlays
            AnimatedContent(
                targetState = if (showModeSelector) "SELECTOR" else viewModel.activeMode.name
            ) { target ->
                when (target) {
                    "SELECTOR" -> {
                        ModeSelectorMenu(
                            viewModel = viewModel,
                            onModeSelected = { chosenMode ->
                                viewModel.activeMode = chosenMode
                                showModeSelector = false
                                viewModel.clearAll()
                                
                                // Specific initial values triggers
                                if (chosenMode == CalculatorMode.TABLE) {
                                    viewModel.tableFunction.value = "X^2 − X + 2"
                                }
                            }
                        )
                    }
                    CalculatorMode.COMP.name, CalculatorMode.CMPLX.name -> {
                        // Renders normal list history and variable registry in COMP
                        Column {
                            COMPKeyGrid(viewModel)
                            HistoryListView(viewModel)
                        }
                    }
                    CalculatorMode.BASE_N.name -> {
                        BaseNView(viewModel)
                    }
                    CalculatorMode.MATRIX.name -> {
                        MatrixView(viewModel)
                    }
                    CalculatorMode.EQN.name -> {
                        EquationView(viewModel)
                    }
                    CalculatorMode.TABLE.name -> {
                        TableView(viewModel)
                    }
                    CalculatorMode.STAT.name -> {
                        StatisticsView(viewModel)
                    }
                    CalculatorMode.CONV.name -> {
                        ConversionView(viewModel)
                    }
                    CalculatorMode.CONST.name -> {
                        ConstantsView(viewModel)
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    // --- 1. POPUP: Variable Memory List Sheet ---
    if (showVarSheet) {
        AlertDialog(
            onDismissRequest = { showVarSheet = false },
            title = {
                Text(
                    "Variable Register Memory (RCL)",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = ShiftAmber
                )
            },
            text = {
                val vars by viewModel.variableMap.collectAsState()
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Here are the current values saved in the persistent registers. Tap to insert the variable symbol inside your COMP expression, or click STO to program a value.",
                        fontSize = 10.sp, color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    vars.forEach { (name, value) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.Black.copy(0.2f))
                                .clickable {
                                    viewModel.insertText(name)
                                    showVarSheet = false
                                }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Variable $name",
                                color = AlphaViolet,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp
                            )
                            Text(
                                value.toString(),
                                color = Color.White,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showVarSheet = false },
                    colors = ButtonDefaults.buttonColors(containerColor = ShiftAmber, contentColor = Color.Black)
                ) {
                    Text("OK")
                }
            },
            containerColor = HardwareBody
        )
    }

    // --- 2. POPUP: Reference Guide Sheet ---
    if (showHelpSheet) {
        AlertDialog(
            onDismissRequest = { showHelpSheet = false },
            title = {
                Text(
                    "Quick Reference User Guide",
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    color = ShiftAmber
                )
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "Welcome to the authentic ClassWiz fx-991EX Emulator. Here is a description of keys and indicators:",
                        fontSize = 11.sp, color = Color.LightGray
                    )

                    // Description items
                    HelpGuideItem("SHIFT Key", "Activates yellow operations, labeled on top of buttons. Labeled [SHIFT] on screen when active.")
                    HelpGuideItem("ALPHA Key", "Activates violet operations (variables A to F, X, Y, M). Labeled [ALPHA] when active.")
                    HelpGuideItem("S⇔D Key", "Toggles the computed output result between continued fractions form (e.g. 1/3) and standard Decimal format.")
                    HelpGuideItem("MODE Key", "Accesses the 3x3 Advanced Mode selectors covering Statistics, Equations, Base-N, and Tables.")
                    HelpGuideItem("Memory (STO/M+)", "Saves variables. Press M+ to accumulate answer to memory, or pull the RCL sheet from the header to insert variables.")
                    HelpGuideItem("Implicit Multiplication", "Supports native implicit arithmetic format (e.g. you can write 2A, 3π, 4(3+2), or 2sin(30)).")
                    HelpGuideItem("Degree / Radian Mode", "Click on the DEG/RAD bar at top right to instantly swap angle units.")
                }
            },
            confirmButton = {
                Button(
                    onClick = { showHelpSheet = false },
                    colors = ButtonDefaults.buttonColors(containerColor = ShiftAmber, contentColor = Color.Black)
                ) {
                    Text("Close Guide")
                }
            },
            containerColor = HardwareBody
        )
    }
}

@Composable
fun HelpGuideItem(title: String, desc: String) {
    Column {
        Text(title, color = ShiftAmber, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        Text(desc, color = Color.White, fontSize = 11.sp, lineHeight = 14.sp)
        Spacer(modifier = Modifier.height(4.dp))
    }
}

// --- STANDARD SCIENTIFIC KEYPAD DESIGN (Row 1-5 grid) ---
@Composable
fun COMPKeyGrid(viewModel: CalculatorViewModel) {
    val keysList = listOf(
        // Row 1
        KeyData("sin", "asin"), KeyData("cos", "acos"), KeyData("tan", "atan"), KeyData("i", "i"), KeyData("x²", "³"), KeyData("x^y", "^"),
        // Row 2
        KeyData("√", "³√"), KeyData("log", "logbase"), KeyData("ln", "e^"), KeyData("(", "!"), KeyData(")", "%"), KeyData("S⇔D", "S⇔D"),
        // Row 3
        KeyData("const", "const"), KeyData("conv", "conv"), KeyData("( - )", "Pol"), KeyData("o'''", "Rec"), KeyData("STO", "STO"), KeyData("M+", "M-"),
        // Row 4 (Numbers grid + operations)
        KeyData("7", ""), KeyData("8", ""), KeyData("9", ""), KeyData("DEL", "", isCrimson = true), KeyData("AC", "", isCrimson = true),
        // Row 5
        KeyData("4", ""), KeyData("5", ""), KeyData("6", ""), KeyData("×", "nPr"), KeyData("÷", "nCr"),
        // Row 6
        KeyData("1", ""), KeyData("2", ""), KeyData("3", ""), KeyData("+", ""), KeyData("-", ""),
        // Row 7
        KeyData("0", ""), KeyData(".", ""), KeyData("EXP", "*10^"), KeyData("ANS", ""), KeyData("=", "", isEquals = true)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(KeypadBackground)
            .padding(8.dp)
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(5),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.height(340.dp)
        ) {
            items(keysList.size) { i ->
                val key = keysList[i]
                CalculatorButton(
                    key = key,
                    onClick = {
                        if (key.label == "const") {
                            viewModel.activeMode = CalculatorMode.CONST
                        } else if (key.label == "conv") {
                            viewModel.activeMode = CalculatorMode.CONV
                        } else {
                            viewModel.onKeyPress(key.label)
                        }
                    }
                )
            }
        }
    }
}

data class KeyData(
    val label: String,
    val shiftLabel: String,
    val isCrimson: Boolean = false,
    val isEquals: Boolean = false
)

@Composable
fun CalculatorButton(
    key: KeyData,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerBg = when {
        key.isCrimson -> ClearCrimson
        key.isEquals -> ShiftAmber
        key.label in listOf("0","1","2","3","4","5","6","7","8","9",".") -> NumberSlate
        else -> FunctionCharcoal
    }

    val labelTextCol = when {
        key.isEquals -> Color.Black
        else -> Color.White
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(42.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(containerBg)
            .clickable { onClick() }
            .testTag("key_${key.label}"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(2.dp)
        ) {
            // Shift sublabel showing golden physical indicator
            if (key.shiftLabel.isNotEmpty()) {
                Text(
                    text = key.shiftLabel,
                    color = ShiftAmber,
                    fontSize = 7.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1
                )
            }
            Text(
                text = key.label,
                color = labelTextCol,
                fontSize = if (key.label.length > 3) 10.sp else 14.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace,
                maxLines = 1
            )
        }
    }
}
