package com.example.medicinetracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import com.example.medicinetracker.data.model.FrequencyType
import com.example.medicinetracker.data.model.Medicine
import com.example.medicinetracker.ui.MedicineViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.TextStyle
import java.util.Locale

import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Add
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddMedicineScreen(
    viewModel: MedicineViewModel,
    medicine: Medicine? = null,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf(medicine?.name ?: "") }
    var dosage by remember { mutableStateOf(medicine?.dosage ?: "") }
    var durationValue by remember { mutableStateOf(medicine?.durationValue?.toString() ?: "7") }
    var selectedDurationUnit by remember { mutableStateOf(medicine?.durationUnit ?: com.example.medicinetracker.data.model.DurationUnit.DAYS) }
    var selectedType by remember { mutableStateOf(medicine?.type ?: "Tablet") }
    var selectedFrequency by remember { mutableStateOf(medicine?.frequency ?: FrequencyType.DAILY) }
    var selectedDays by remember { mutableStateOf(medicine?.daysOfWeek?.toSet() ?: emptySet()) }
    var selectedTimes by remember { 
        mutableStateOf(medicine?.timesPerDay ?: listOf(LocalTime.now().withSecond(0).withNano(0))) 
    }

    val suggestions by viewModel.suggestions.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val dosageForms by viewModel.dosageForms.collectAsState()

    var showTypeDropdown by remember { mutableStateOf(false) }
    var showDurationUnitDropdown by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf<Int?>(null) } // Index of time being edited

    val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a")

    if (showTimePicker != null) {
        val index = showTimePicker!!
        val initialTime = selectedTimes.getOrElse(index) { LocalTime.now() }
        val timePickerState = rememberTimePickerState(
            initialHour = initialTime.hour,
            initialMinute = initialTime.minute
        )
        var showKeyboardInput by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showTimePicker = null },
            confirmButton = {
                TextButton(onClick = {
                    val newTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                    val newList = selectedTimes.toMutableList()
                    if (index < newList.size) {
                        newList[index] = newTime
                    } else {
                        newList.add(newTime)
                    }
                    selectedTimes = newList.sorted()
                    showTimePicker = null
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = null }) {
                    Text("Cancel")
                }
            },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Text(if (showKeyboardInput) "Enter Time" else "Select Time")
                    IconButton(onClick = { showKeyboardInput = !showKeyboardInput }) {
                        Icon(
                            imageVector = if (showKeyboardInput) Icons.Default.Schedule else Icons.Default.Edit,
                            contentDescription = "Toggle Input Mode"
                        )
                    }
                }
            },
            text = {
                Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                    if (showKeyboardInput) {
                        TimeInput(state = timePickerState)
                    } else {
                        TimePicker(state = timePickerState)
                    }
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (medicine == null) "Add Medicine" else "Edit Medicine") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (medicine != null) {
                        IconButton(onClick = {
                            viewModel.delete(medicine)
                            onBack()
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { 
                        name = it
                        viewModel.searchMedicine(it)
                    },
                    label = { Text("Medicine Name") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        if (isSearching) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        }
                    }
                )
                
                if (suggestions.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 250.dp)
                            .padding(vertical = 4.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                            suggestions.forEach { suggestion ->
                                Surface(
                                    onClick = { 
                                        name = suggestion.name
                                        dosage = suggestion.strength ?: ""
                                        selectedType = suggestion.dosageForm ?: "Tablet"
                                        viewModel.searchMedicine("") 
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = suggestion.name, 
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.weight(1f)
                                            )
                                            if (!suggestion.isLocal) {
                                                Surface(
                                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                                    shape = MaterialTheme.shapes.extraSmall
                                                ) {
                                                    Text(
                                                        text = "LIVE",
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                                    )
                                                }
                                            }
                                        }
                                        Text(
                                            text = "${suggestion.generic} - ${suggestion.strength} (${suggestion.dosageForm})",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 12.dp),
                                    thickness = 0.5.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )
                            }
                        }
                    }
                }
            }

            OutlinedTextField(
                value = dosage,
                onValueChange = { dosage = it },
                label = { Text("Dosage (e.g., 500mg, 2 drops)") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = durationValue,
                    onValueChange = { durationValue = it },
                    label = { Text("Duration") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    )
                )

                ExposedDropdownMenuBox(
                    expanded = showDurationUnitDropdown,
                    onExpandedChange = { showDurationUnitDropdown = !showDurationUnitDropdown },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = selectedDurationUnit.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Unit") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showDurationUnitDropdown) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = showDurationUnitDropdown,
                        onDismissRequest = { showDurationUnitDropdown = false }
                    ) {
                        com.example.medicinetracker.data.model.DurationUnit.values().forEach { unit ->
                            DropdownMenuItem(
                                text = { Text(unit.displayName) },
                                onClick = {
                                    selectedDurationUnit = unit
                                    showDurationUnitDropdown = false
                                }
                            )
                        }
                    }
                }
            }

            Text("Type (Dosage Form):", style = MaterialTheme.typography.titleMedium)
            
            ExposedDropdownMenuBox(
                expanded = showTypeDropdown,
                onExpandedChange = { showTypeDropdown = !showTypeDropdown }
            ) {
                OutlinedTextField(
                    value = selectedType,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showTypeDropdown) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = showTypeDropdown,
                    onDismissRequest = { showTypeDropdown = false }
                ) {
                    dosageForms.forEach { form ->
                        DropdownMenuItem(
                            text = { Text(form.name) },
                            onClick = {
                                selectedType = form.name
                                showTypeDropdown = false
                            }
                        )
                    }
                }
            }

            Text("Frequency:", style = MaterialTheme.typography.titleMedium)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FrequencyType.values().forEach { freq ->
                    FilterChip(
                        selected = selectedFrequency == freq,
                        onClick = { selectedFrequency = freq },
                        label = { Text(freq.displayName) }
                    )
                }
            }

            if (selectedFrequency == FrequencyType.WEEKLY || selectedFrequency == FrequencyType.SPECIFIC_DAYS) {
                Text("Select Days:", style = MaterialTheme.typography.titleMedium)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DayOfWeek.values().forEach { day ->
                        val isSelected = selectedDays.contains(day)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedDays = if (isSelected) {
                                    selectedDays - day
                                } else {
                                    selectedDays + day
                                }
                            },
                            label = { 
                                Text(day.getDisplayName(TextStyle.SHORT, Locale.getDefault())) 
                            }
                        )
                    }
                }
            }

            Text("Reminder Times:", style = MaterialTheme.typography.titleMedium)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                selectedTimes.forEachIndexed { index, time ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { showTimePicker = index },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(time.format(timeFormatter))
                        }
                        if (selectedTimes.size > 1) {
                            IconButton(onClick = {
                                selectedTimes = selectedTimes.toMutableList().apply { removeAt(index) }
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove time")
                            }
                        }
                    }
                }
                TextButton(
                    onClick = { showTimePicker = selectedTimes.size },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Add Reminder Time")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (name.isNotBlank() && dosage.isNotBlank()) {
                        val newMedicine = Medicine(
                            id = medicine?.id ?: 0L,
                            name = name,
                            type = selectedType,
                            dosage = dosage,
                            startDateString = (medicine?.startDate ?: LocalDate.now()).toString(),
                            durationValue = durationValue.toIntOrNull() ?: 7,
                            durationUnit = selectedDurationUnit,
                            frequency = selectedFrequency,
                            daysOfWeek = if (selectedFrequency == FrequencyType.WEEKLY || selectedFrequency == FrequencyType.SPECIFIC_DAYS) {
                                selectedDays.toList()
                            } else null,
                            timesPerDayStrings = selectedTimes.map { it.toString() }
                        )
                        viewModel.insertAndSchedule(newMedicine)
                        onBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank() && dosage.isNotBlank()
            ) {
                Text(if (medicine == null) "Save Medicine" else "Update Medicine")
            }
        }
    }
}
