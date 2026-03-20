package com.example.medicinetracker.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.example.medicinetracker.R
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
                    },
                    viewModel = viewModel,
                    doseHistory = history
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
    val searchFilter by viewModel.searchFilter.collectAsState()
    val keyboardController = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current

    Column(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            Column {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.performDedicatedSearch(it) },
                    placeholder = { Text("Search 20,000+ medicines...", style = MaterialTheme.typography.bodyLarge) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.performDedicatedSearch("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                )
                
                // Filter Chips
                androidx.compose.foundation.lazy.LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val filters = listOf("All", "Brand", "Generic")
                    items(filters.size) { index ->
                        val filter = filters[index]
                        FilterChip(
                            selected = searchFilter == filter,
                            onClick = { viewModel.setSearchFilter(filter) },
                            label = { Text(filter) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }
        }

        if (isSearching) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth().height(2.dp))
        }

        if (searchQuery.length >= 2 && searchResults.isEmpty() && !isSearching) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.SearchOff, 
                        contentDescription = null, 
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No results for \"$searchQuery\"",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else if (searchQuery.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                    Icon(
                        Icons.Default.MedicalInformation, 
                        contentDescription = null, 
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Search by Brand or Generic Name",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(
                    items = searchResults,
                    key = { "${it.id}-${it.name}" }
                ) { brand ->
                    SearchMedicineCard(
                        brand = brand, 
                        onClick = { 
                            keyboardController?.hide()
                            focusManager.clearFocus()
                            onBrandClick(brand) 
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SearchMedicineCard(
    brand: com.example.medicinetracker.data.model.MedicineBrand,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = androidx.compose.foundation.shape.CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = brand.name.take(1).uppercase(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = brand.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${brand.generic} • ${brand.strength}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Text(
                    text = brand.manufacturer,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    maxLines = 1
                )
            }
            
            Icon(
                Icons.Default.ChevronRight, 
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun MedicineList(
    medicines: List<Medicine>, 
    onMedicineClick: (Medicine) -> Unit,
    onMedicineDelete: (Medicine) -> Unit,
    onMedicineLongClick: (Medicine) -> Unit,
    viewModel: MedicineViewModel,
    doseHistory: List<com.example.medicinetracker.data.model.DoseRecord>
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
        val nextDose = remember(medicines, doseHistory) { findNextDose(medicines, doseHistory) }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (nextDose != null) {
                item {
                    NextDoseCard(nextDose, onTakeNow = { viewModel.logDose(nextDose.medicine, "TAKEN") })
                }
            }

            item {
                Text(
                    "All Medications",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }
            items(
                items = medicines,
                key = { it.id }
            ) { medicine ->
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

data class NextDoseInfo(
    val medicine: Medicine,
    val time: LocalTime,
    val label: String
)

private fun findNextDose(
    medicines: List<Medicine>, 
    doseHistory: List<com.example.medicinetracker.data.model.DoseRecord>
): NextDoseInfo? {
    val now = LocalDateTime.now()
    val today = LocalDate.now()
    val upcomingDoses = mutableListOf<LocalDateTimeInfo>()

    for (medicine in medicines) {
        // Look ahead 14 days to catch next weekly dose reliably
        for (dayOffset in 0..14) {
            val checkDate = today.plusDays(dayOffset.toLong())
            
            // Skip if medicine hasn't started yet
            if (checkDate.isBefore(medicine.startDate)) continue
            
            // Refined day validation
            val isValidDay = when (medicine.frequency) {
                com.example.medicinetracker.data.model.FrequencyType.DAILY -> true
                com.example.medicinetracker.data.model.FrequencyType.WEEKLY -> {
                    // If user picked specific days, use them. Otherwise fallback to start date's day.
                    if (!medicine.daysOfWeek.isNullOrEmpty()) {
                        medicine.daysOfWeek.contains(checkDate.dayOfWeek)
                    } else {
                        checkDate.dayOfWeek == medicine.startDate.dayOfWeek
                    }
                }
                com.example.medicinetracker.data.model.FrequencyType.SPECIFIC_DAYS -> {
                    medicine.daysOfWeek?.contains(checkDate.dayOfWeek) == true
                }
                com.example.medicinetracker.data.model.FrequencyType.AS_NEEDED -> false
            }

            if (isValidDay) {
                for (slotTime in medicine.timesPerDay) {
                    val doseDateTime = LocalDateTime.of(checkDate, slotTime)
                    
                    // Check if this specific slot was already taken today
                    val isTaken = doseHistory.any { record ->
                        if (record.medicineId != medicine.id) return@any false
                        val loggedTime = try { LocalDateTime.parse(record.dateTimeString) } catch(e: Exception) { null }
                        
                        loggedTime != null && 
                        loggedTime.toLocalDate() == checkDate && 
                        // If taken within 4 hours of the slot, consider it done for this slot
                        java.time.Duration.between(loggedTime.toLocalTime(), slotTime).abs().toMinutes() < 240
                    }

                    if (isTaken) continue

                    // If NOT taken, check if it's still in the future or very recent past (grace period)
                    // If we just clicked "Take Now", the record above will catch it.
                    // If we HAVEN'T taken it, we only show it if it's not too far in the past.
                    if (doseDateTime.isAfter(now.minusMinutes(5))) {
                        upcomingDoses.add(LocalDateTimeInfo(medicine, doseDateTime))
                    }
                }
            }
        }
    }

    val next = upcomingDoses.minByOrNull { it.dateTime } ?: return null
    
    val label = when {
        next.dateTime.toLocalDate() == today -> "Today"
        next.dateTime.toLocalDate() == today.plusDays(1) -> "Tomorrow"
        else -> next.dateTime.toLocalDate().dayOfWeek.getDisplayName(java.time.format.TextStyle.FULL, Locale.getDefault())
    }

    return NextDoseInfo(next.medicine, next.dateTime.toLocalTime(), label)
}

data class LocalDateTimeInfo(
    val medicine: Medicine,
    val dateTime: LocalDateTime
)

@Composable
fun NextDoseCard(info: NextDoseInfo, onTakeNow: () -> Unit) {
    val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a")
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "NEXT DOSE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    info.medicine.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Schedule, 
                        contentDescription = null, 
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "${info.label} at ${info.time.format(timeFormatter)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
            
            Button(
                onClick = onTakeNow,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Take Now")
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

                items(
                    items = records,
                    key = { "${it.medicineId}-${it.dateTimeString}" }
                ) { record ->
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
    
    // Category-based colors for a beautiful dashboard
    val (containerColor, contentColor, icon) = when (medicine.type.lowercase()) {
        "tablet" -> Triple(Color(0xFFF5F5F5), Color(0xFF616161), Icons.Default.MedicalServices)
        "capsule" -> Triple(Color(0xFFF5F5F5), Color(0xFF616161), Icons.Default.Science)
        "syrup" -> Triple(Color(0xFFE8F5E9), Color(0xFF388E3C), Icons.Default.Opacity)
        "injection" -> Triple(Color(0xFFFFF3E0), Color(0xFFF57C00), Icons.Default.Vaccines)
        else -> Triple(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), MaterialTheme.colorScheme.primary, Icons.Default.Medication)
    }

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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, containerColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Iconic leading element
            Surface(
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                color = containerColor,
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    when (medicine.type.lowercase()) {
                        "capsule" -> Image(
                            painter = painterResource(id = R.drawable.ic_capsule),
                            contentDescription = null,
                            modifier = Modifier.size(32.dp)
                        )
                        "tablet" -> Image(
                            painter = painterResource(id = R.drawable.ic_tablet),
                            contentDescription = null,
                            modifier = Modifier.size(32.dp)
                        )
                        else -> Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = contentColor,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = medicine.name, 
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDeleteClick, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.4f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                
                Text(
                    text = "${medicine.dosage} • ${medicine.type}", 
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(Modifier.height(8.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val (freqContainer, freqContent) = when (medicine.frequency) {
                        com.example.medicinetracker.data.model.FrequencyType.DAILY -> Color(0xFFE3F2FD) to Color(0xFF1976D2)
                        com.example.medicinetracker.data.model.FrequencyType.WEEKLY -> Color(0xFFFFF3E0) to Color(0xFFF57C00)
                        com.example.medicinetracker.data.model.FrequencyType.AS_NEEDED -> Color(0xFFF3E5F5) to Color(0xFF7B1FA2)
                        com.example.medicinetracker.data.model.FrequencyType.SPECIFIC_DAYS -> Color(0xFFE8F5E9) to Color(0xFF388E3C)
                    }
                    
                    Surface(
                        color = freqContainer,
                        shape = androidx.compose.foundation.shape.CircleShape
                    ) {
                        Text(
                            text = medicine.frequency.displayName,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = freqContent,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "${medicine.durationValue} ${medicine.durationUnit.displayName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}
