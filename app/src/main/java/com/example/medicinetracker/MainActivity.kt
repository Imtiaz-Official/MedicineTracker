package com.example.medicinetracker

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.BackHandler
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.medicinetracker.data.MedicineRepository
import com.example.medicinetracker.data.local.MedicineDatabase
import com.example.medicinetracker.data.local.MedicinePrefsManager
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import com.example.medicinetracker.data.model.Medicine
import com.example.medicinetracker.data.model.MedicineBrand
import com.example.medicinetracker.ui.MedicineViewModel
import com.example.medicinetracker.ui.MedicineViewModelFactory
import com.example.medicinetracker.ui.screens.AddMedicineScreen
import com.example.medicinetracker.ui.screens.DashboardScreen
import com.example.medicinetracker.ui.screens.MedicineBrandDetailScreen
import com.example.medicinetracker.ui.theme.MedicineTrackerTheme

class MainActivity : ComponentActivity() {

    private val database by lazy { MedicineDatabase.getDatabase(this) }
    private val prefsManager by lazy { MedicinePrefsManager(this) }
    private val repository by lazy { MedicineRepository(database.medicineDao(), prefsManager) }
    private val viewModel: MedicineViewModel by viewModels {
        MedicineViewModelFactory(application, repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var hasNotificationPermission by remember {
                mutableStateOf(
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED
                    } else true
                )
            }

            val permissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission(),
                onResult = { isGranted ->
                    hasNotificationPermission = isGranted
                }
            )

            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }

            MedicineTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MedicineApp(viewModel)
                }
            }
        }
    }
}

@Composable
fun MedicineApp(viewModel: MedicineViewModel) {
    var currentScreen by remember { mutableStateOf("dashboard") }
    var selectedMedicine by remember { mutableStateOf<Medicine?>(null) }
    var selectedBrand by remember { mutableStateOf<MedicineBrand?>(null) }
    var selectedTab by remember { mutableIntStateOf(0) }

    Crossfade(targetState = currentScreen, animationSpec = tween(400), label = "screenTransition") { screen ->
        when (screen) {
            "dashboard" -> DashboardScreen(
                viewModel = viewModel,
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                onAddMedicineClick = { 
                    selectedMedicine = null
                    currentScreen = "add_medicine" 
                },
                onMedicineClick = { medicine ->
                    selectedMedicine = medicine
                    currentScreen = "add_medicine"
                },
                onBrandClick = { brand ->
                    selectedBrand = brand
                    currentScreen = "medicine_brand_detail"
                }
            )
            "add_medicine" -> {
                BackHandler {
                    currentScreen = "dashboard"
                }
                AddMedicineScreen(
                    viewModel = viewModel,
                    medicine = selectedMedicine,
                    onBack = { currentScreen = "dashboard" }
                )
            }
            "medicine_brand_detail" -> {
                BackHandler {
                    currentScreen = "dashboard"
                }
                selectedBrand?.let { brand ->
                    MedicineBrandDetailScreen(
                        brand = brand,
                        viewModel = viewModel,
                        onBack = { currentScreen = "dashboard" }
                    )
                }
            }
        }
    }
}
