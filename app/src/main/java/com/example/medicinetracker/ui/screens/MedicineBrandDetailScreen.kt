package com.example.medicinetracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.toArgb
import com.example.medicinetracker.data.model.GenericInfo
import com.example.medicinetracker.data.model.MedicineBrand
import com.example.medicinetracker.ui.MedicineViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicineBrandDetailScreen(
    brand: MedicineBrand,
    viewModel: MedicineViewModel,
    onBack: () -> Unit
) {
    val genericInfo by viewModel.selectedGenericInfo.collectAsState()
    val isLoading by viewModel.isLoadingGenericInfo.collectAsState()
    var hasStartedLoading by remember { mutableStateOf(false) }

    LaunchedEffect(brand) {
        hasStartedLoading = true
        viewModel.getGenericInfo(brand)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(brand.name, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        MedicineBrandDetailView(
            brand = brand,
            genericInfo = genericInfo,
            isLoading = isLoading || !hasStartedLoading,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
fun MedicineBrandDetailView(
    brand: MedicineBrand,
    genericInfo: GenericInfo?,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = brand.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = brand.strength,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.secondary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    DetailItem(label = "Generic Name", value = brand.generic, valueStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(12.dp))
                    DetailItem(label = "Dosage Form", value = brand.dosageForm)
                    Spacer(modifier = Modifier.height(12.dp))
                    DetailItem(label = "Manufacturer", value = brand.manufacturer)
                }
            }
            
            if (!brand.packageContainer.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                DetailItem(label = "Package Info & Price", value = brand.packageContainer)
            }
            
            if (!brand.packageSize.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                DetailItem(label = "Pack Size", value = brand.packageSize)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Clinical Monograph",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.outline
            )
            HorizontalDivider(modifier = Modifier.padding(top = 8.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
        }

        if (isLoading) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp), strokeWidth = 3.dp)
                }
            }
        } else if (genericInfo != null) {
            item {
                MonographSection(title = "Indications", content = genericInfo.indication)
                MonographSection(title = "Therapeutic Class", content = genericInfo.therapeuticClass)
                MonographSection(title = "Pharmacology", content = genericInfo.pharmacology)
                MonographSection(title = "Dosage & Administration", content = genericInfo.dosage ?: genericInfo.administration)
                MonographSection(title = "Interaction", content = genericInfo.interaction)
                MonographSection(title = "Contraindications", content = genericInfo.contraindications)
                MonographSection(title = "Side Effects", content = genericInfo.sideEffects)
                MonographSection(title = "Pregnancy & Lactation", content = genericInfo.pregnancyLactation)
                MonographSection(title = "Precautions", content = genericInfo.precautions)
                MonographSection(title = "Storage Conditions", content = genericInfo.storage)
            }
        } else {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Text(
                        "No clinical monograph available for this generic.",
                        modifier = Modifier.padding(24.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info, 
                        contentDescription = null, 
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Disclaimer: This information is for educational purposes and is not a substitute for professional medical advice.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun MonographSection(title: String, content: String?) {
    if (content.isNullOrBlank()) return
    
    val textColor = MaterialTheme.colorScheme.onSurface.toArgb()
    
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        
        androidx.compose.ui.viewinterop.AndroidView(
            factory = { context ->
                android.widget.TextView(context).apply {
                    setTextColor(textColor)
                    textSize = 14f
                }
            },
            update = { textView ->
                textView.setTextColor(textColor)
                textView.text = android.text.Html.fromHtml(content, android.text.Html.FROM_HTML_MODE_COMPACT)
            }
        )
    }
}

@Composable
fun DetailItem(label: String, value: String, valueStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyLarge) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = valueStyle,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
