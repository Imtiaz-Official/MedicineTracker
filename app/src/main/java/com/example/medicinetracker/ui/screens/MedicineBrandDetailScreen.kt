package com.example.medicinetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
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
    var currentBrand by remember { mutableStateOf(brand) }
    val genericInfo by viewModel.selectedGenericInfo.collectAsState()
    val isLoading by viewModel.isLoadingGenericInfo.collectAsState()
    val combinedAlternates by viewModel.combinedAlternateBrands.collectAsState()

    LaunchedEffect(currentBrand.id) {
        viewModel.getGenericInfo(currentBrand)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Medicine Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        MedicineBrandDetailView(
            brand = currentBrand,
            genericInfo = genericInfo,
            alternateBrands = combinedAlternates,
            isLoading = isLoading,
            viewModel = viewModel,
            onBrandClick = { newBrand ->
                currentBrand = newBrand
            },
            modifier = Modifier.padding(innerPadding)
        )
    }
}
    @Composable
    fun AlternateBrandCard(
    brand: MedicineBrand,
    onClick: () -> Unit
    ) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .padding(end = 8.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = brand.name,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            Text(
                text = brand.strength,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1
            )
            Text(
                text = brand.manufacturer,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
    }

    @Composable
    fun ShimmerItem(

    brush: Brush,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(brush, shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
    )
}

@Composable
fun MonographSkeleton() {
    val shimmerColors = listOf(
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
    )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslation"
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnim.value, y = translateAnim.value)
    )

    Column(modifier = Modifier.padding(vertical = 16.dp)) {
        repeat(5) {
            ShimmerItem(brush = brush, modifier = Modifier.width(100.dp).height(20.dp))
            Spacer(modifier = Modifier.height(12.dp))
            ShimmerItem(brush = brush, modifier = Modifier.fillMaxWidth().height(14.dp))
            Spacer(modifier = Modifier.height(6.dp))
            ShimmerItem(brush = brush, modifier = Modifier.fillMaxWidth(0.7f).height(14.dp))
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun MedicineBrandDetailView(
    brand: MedicineBrand,
    genericInfo: GenericInfo?,
    alternateBrands: List<MedicineBrand>,
    isLoading: Boolean,
    viewModel: MedicineViewModel,
    onBrandClick: (MedicineBrand) -> Unit,
    modifier: Modifier = Modifier
) {
    val isLiveLoading by viewModel.isLiveLoading.collectAsState()
    
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
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
                }
                
                // Live Refresh Button
                FilledTonalIconButton(
                    onClick = { viewModel.refreshFromMedex(brand) },
                    enabled = !isLiveLoading && !isLoading
                ) {
                    if (isLiveLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh from Medex")
                    }
                }
            }
            
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

            // Unified Alternates Section
            if (alternateBrands.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Other Alternatives",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                androidx.compose.foundation.lazy.LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(alternateBrands.size) { index ->
                        val altBrand = alternateBrands[index]
                        AlternateBrandCard(
                            brand = altBrand,
                            onClick = { onBrandClick(altBrand) }
                        )
                    }
                }
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
                MonographSkeleton()
            }
        } else if (genericInfo != null) {
            item {
                androidx.compose.animation.AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(animationSpec = tween(500))
                ) {
                    Column {
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
                }
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
    val primaryColor = MaterialTheme.colorScheme.primary
    
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = primaryColor
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        androidx.compose.ui.viewinterop.AndroidView(
            factory = { context ->
                android.widget.TextView(context).apply {
                    setTextColor(textColor)
                    textSize = 15f
                    // Add line spacing for better readability
                    setLineSpacing(0f, 1.2f)
                    // Ensure links are clickable
                    movementMethod = android.text.method.LinkMovementMethod.getInstance()
                }
            },
            update = { textView ->
                textView.setTextColor(textColor)
                val formattedHtml = formatHtmlContent(content)
                textView.text = android.text.Html.fromHtml(formattedHtml, android.text.Html.FROM_HTML_MODE_COMPACT)
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Pre-processes HTML from Medex to ensure proper spacing and bullet points.
 */
private fun formatHtmlContent(html: String): String {
    return html
        // 1. Add extra line breaks before list items to prevent "clumping"
        .replace("<li>", "•  ")
        .replace("</li>", "<br/>")
        // 2. Add gaps between paragraphs/sections
        .replace("</div>", "</div><br/>")
        .replace("<br>", "<br/>")
        .replace("<br/>", "<br/><br/>") // Double break for actual gaps
        // 3. Clean up excessive double breaks we might have created
        .replace("<br/><br/><br/><br/>", "<br/><br/>")
        // 4. Ensure bold tags look good
        .replace("<strong>", "<b>")
        .replace("</strong>", "</b>")
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
