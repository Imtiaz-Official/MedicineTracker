package com.example.medicinetracker.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.medicinetracker.data.model.Medicine
import com.example.medicinetracker.ui.MedicineViewModel
import androidx.compose.ui.text.font.FontWeight
import java.time.format.TextStyle
import java.util.Locale
import java.time.LocalDateTime
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import java.time.YearMonth
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DashboardScreen(
    viewModel: MedicineViewModel,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onAddMedicineClick: () -> Unit,
    onMedicineClick: (Medicine) -> Unit,
    onBrandClick: (com.example.medicinetracker.data.model.MedicineBrand) -> Unit
) {
    val medicines by viewModel.allMedicines.collectAsState()
    val history by viewModel.doseHistory.collectAsState()
    var recordToDelete by remember { mutableStateOf<com.example.medicinetracker.data.model.DoseRecord?>(null) }
    var medicineToDelete by remember { mutableStateOf<Medicine?>(null) }
    var medicineWithOptions by remember { mutableStateOf<Medicine?>(null) }
    var showOptionsSheet by remember { mutableStateOf(false) }

    if (showOptionsSheet && medicineWithOptions != null) {
        ModalBottomSheet(
            onDismissRequest = { showOptionsSheet = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    text = medicineWithOptions?.name ?: "",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                ListItem(
                    headlineContent = { Text("Take Now") },
                    leadingContent = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50)) },
                    modifier = Modifier.clickable {
                        showOptionsSheet = false
                        medicineWithOptions?.let { viewModel.logDose(it, "TAKEN") }
                    }
                )

                ListItem(
                    headlineContent = { Text("Skip Dose") },
                    leadingContent = { Icon(Icons.Default.Cancel, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                    modifier = Modifier.clickable {
                        showOptionsSheet = false
                        medicineWithOptions?.let { viewModel.logDose(it, "SKIPPED") }
                    }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                ListItem(
                    headlineContent = { Text("Edit Details") },
                    leadingContent = { Icon(Icons.Default.Edit, contentDescription = null) },
                    modifier = Modifier.clickable {
                        showOptionsSheet = false
                        medicineWithOptions?.let { onMedicineClick(it) }
                    }
                )
                
                ListItem(
                    headlineContent = { Text("Delete Medicine", color = MaterialTheme.colorScheme.error) },
                    leadingContent = { 
                        Icon(
                            Icons.Default.Delete, 
                            contentDescription = null, 
                            tint = MaterialTheme.colorScheme.error 
                        ) 
                    },
                    modifier = Modifier.clickable {
                        showOptionsSheet = false
                        medicineToDelete = medicineWithOptions
                    }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    if (medicineToDelete != null) {
        AlertDialog(
            onDismissRequest = { medicineToDelete = null },
            title = { Text("Delete Medicine") },
            text = { Text("Are you sure you want to delete ${medicineToDelete?.name}? This will also cancel all scheduled alarms for this medicine.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        medicineToDelete?.let { viewModel.delete(it) }
                        medicineToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { medicineToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (recordToDelete != null) {
        AlertDialog(
            onDismissRequest = { recordToDelete = null },
            title = { Text("Delete History Record") },
            text = { Text("This will permanently remove this record from your history.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        recordToDelete?.let { viewModel.deleteDoseRecord(it) }
                        recordToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { recordToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Medicine Tracker",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.MedicalServices, contentDescription = null) },
                    label = { Text("Medicines") },
                    selected = selectedTab == 0,
                    onClick = { onTabSelected(0) }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.History, contentDescription = null) },
                    label = { Text("History") },
                    selected = selectedTab == 1,
                    onClick = { onTabSelected(1) }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                    label = { Text("Calendar") },
                    selected = selectedTab == 2,
                    onClick = { onTabSelected(2) }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Search, contentDescription = null) },
                    label = { Text("Search") },
                    selected = selectedTab == 3,
                    onClick = { onTabSelected(3) }
                )
            }
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = onAddMedicineClick,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Medicine")
                }
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                0 -> MedicineList(
                    medicines = medicines, 
                    onMedicineClick = onMedicineClick, 
                    onMedicineDelete = { medicineToDelete = it },
                    onMedicineLongClick = {
                        medicineWithOptions = it
                        showOptionsSheet = true
                    }
                )
                1 -> HistoryList(history, onRemoveRecord = { recordToDelete = it })
                2 -> CalendarView(history)
                3 -> SearchMedicineScreen(viewModel, onBrandClick = onBrandClick)
            }
        }
    }
}

@Composable
fun SearchMedicineScreen(viewModel: MedicineViewModel, onBrandClick: (com.example.medicinetracker.data.model.MedicineBrand) -> Unit) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                viewModel.performDedicatedSearch(it)
            },
            label = { Text("Search by Brand or Generic Name") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { 
                        viewModel.performDedicatedSearch("")
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (isSearching) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (searchQuery.length >= 2 && searchResults.isEmpty()) {
            Text(
                "No medicines found for \"$searchQuery\"",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 32.dp).fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(searchResults) { brand ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onBrandClick(brand) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "${brand.name} ${brand.strength}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = brand.generic,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = brand.dosageForm,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MedicineList(
    medicines: List<Medicine>, 
    onMedicineClick: (Medicine) -> Unit,
    onMedicineDelete: (Medicine) -> Unit,
    onMedicineLongClick: (Medicine) -> Unit
) {
    if (medicines.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Your cabinet is empty",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Tap + to add your first medicine",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    "Ongoing Medications",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            items(medicines) { medicine ->
                MedicineCard(
                    medicine = medicine, 
                    onClick = { onMedicineClick(medicine) },
                    onLongClick = { onMedicineLongClick(medicine) },
                    onDeleteClick = { onMedicineDelete(medicine) }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HistoryList(
    history: List<com.example.medicinetracker.data.model.DoseRecord>,
    onRemoveRecord: (com.example.medicinetracker.data.model.DoseRecord) -> Unit
) {
    if (history.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "No dose history yet.\nRecords will appear here after you take or skip a dose.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        val groupedHistory = remember(history) {
            history.groupBy { 
                LocalDateTime.parse(it.dateTimeString).toLocalDate() 
            }.toSortedMap(compareByDescending { it })
        }

        val totalTaken = history.count { it.status == "TAKEN" }
        val adherence = if (history.isNotEmpty()) (totalTaken.toFloat() / history.size * 100).toInt() else 0

        val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a")
        val dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMM dd")

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Overall Adherence", style = MaterialTheme.typography.labelLarge)
                            Text(
                                "$adherence%", 
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("$totalTaken Taken", style = MaterialTheme.typography.bodyMedium)
                            Text("${history.size - totalTaken} Skipped", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            groupedHistory.forEach { (date, records) ->
                stickyHeader {
                    val headerText = when (date) {
                        LocalDate.now() -> "Today"
                        LocalDate.now().minusDays(1) -> "Yesterday"
                        else -> date.format(dateFormatter)
                    }
                    Text(
                        text = headerText,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(vertical = 8.dp)
                    )
                }

                items(records) { record ->
                    val dateTime = LocalDateTime.parse(record.dateTimeString)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (record.status == "TAKEN") Icons.Default.CheckCircle else Icons.Default.Cancel,
                                contentDescription = null,
                                tint = if (record.status == "TAKEN") 
                                    MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(24.dp)
                            )
                            
                            Spacer(Modifier.width(16.dp))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = record.medicineName, 
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = dateTime.format(timeFormatter),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            
                            IconButton(onClick = { onRemoveRecord(record) }) {
                                Icon(
                                    Icons.Default.Delete, 
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarView(history: List<com.example.medicinetracker.data.model.DoseRecord>) {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    
    val daysInMonth = currentMonth.lengthOfMonth()
    val firstOfMonth = currentMonth.atDay(1)
    val firstDayOfWeek = firstOfMonth.dayOfWeek.value % 7 // Sunday = 0

    val historyByDate = remember(history) {
        history.groupBy { LocalDateTime.parse(it.dateTimeString).toLocalDate() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(androidx.compose.foundation.rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 500.dp) // Professional limit for tablet/desktop
                .fillMaxWidth()
        ) {
            // Month Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Month")
                }
                Text(
                    text = "${currentMonth.month.getDisplayName(java.time.format.TextStyle.FULL, Locale.getDefault())} ${currentMonth.year}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack, 
                        modifier = Modifier.rotate(180f), 
                        contentDescription = "Next Month"
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Weekdays Header
            Row(modifier = Modifier.fillMaxWidth()) {
                val weekdays = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
                weekdays.forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Calendar Grid
            val totalCells = ((daysInMonth + firstDayOfWeek + 6) / 7) * 7
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                for (row in 0 until totalCells / 7) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        for (col in 0 until 7) {
                            val dayNum = row * 7 + col - firstDayOfWeek + 1
                            Box(modifier = Modifier.weight(1f).aspectRatio(1f), contentAlignment = Alignment.Center) {
                                if (dayNum in 1..daysInMonth) {
                                    val date = currentMonth.atDay(dayNum)
                                    val records = historyByDate[date] ?: emptyList()
                                    
                                    val statusColor = when {
                                        records.isEmpty() -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                        records.isNotEmpty() && records.all { it.status == "TAKEN" } -> Color(0xFF4CAF50) // Green
                                        records.isNotEmpty() && records.all { it.status == "SKIPPED" } -> MaterialTheme.colorScheme.error
                                        else -> Color(0xFFFFC107) // Yellow/Amber for partial
                                    }

                                    Surface(
                                        modifier = Modifier.fillMaxSize(0.8f),
                                        shape = androidx.compose.foundation.shape.CircleShape,
                                        color = statusColor,
                                        tonalElevation = 2.dp
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = dayNum.toString(),
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = if (date == LocalDate.now()) FontWeight.ExtraBold else FontWeight.Normal,
                                                color = if (records.isNotEmpty()) Color.White else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            Spacer(Modifier.height(32.dp))
            
            // Legend
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Legend", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    LegendItem(Color(0xFF4CAF50), "All Doses Taken")
                    LegendItem(Color(0xFFFFC107), "Partial Adherence")
                    LegendItem(MaterialTheme.colorScheme.error, "All Doses Skipped")
                }
            }
        }
    }
}

@Composable
fun LegendItem(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(12.dp).background(color, androidx.compose.foundation.shape.CircleShape))
        Spacer(Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.bodySmall)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MedicineCard(
    medicine: Medicine, 
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClick()
                }
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = medicine.name, 
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Medicine",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                    )
                }
            }
            Text(text = "${medicine.dosage} - ${medicine.type}", style = MaterialTheme.typography.bodyMedium)
            
            Spacer(Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Frequency", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(medicine.frequency.displayName, style = MaterialTheme.typography.bodySmall)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Duration", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${medicine.durationValue} ${medicine.durationUnit.displayName}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
