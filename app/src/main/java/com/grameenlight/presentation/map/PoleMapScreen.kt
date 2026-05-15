package com.grameenlight.presentation.map

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.*
import com.grameenlight.domain.model.Pole
import com.grameenlight.domain.model.PoleStatus
import com.grameenlight.domain.model.UserRole
import com.grameenlight.presentation.auth.AuthViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PoleMapScreen(
    onNavigateToReport: (String) -> Unit,
    onThemeToggle: () -> Unit,
    viewModel: MapViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val poles by viewModel.poles.collectAsState()
    val authState by authViewModel.authState.collectAsState()
    val currentUser = (authState as? com.grameenlight.domain.repository.AuthState.Success)?.user

    var selectedPole by remember { mutableStateOf<Pole?>(null) }
    var showProfileSheet by remember { mutableStateOf(false) }
    val poleSheetState = rememberModalBottomSheetState()
    val profileSheetState = rememberModalBottomSheetState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasLocationPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(20.5937, 78.9629), 15f)
    }

    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            val fusedLocationClient = com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(context)
            try {
                val location = fusedLocationClient.lastLocation.await()
                if (location != null) {
                    cameraPositionState.animate(
                        com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(
                            LatLng(location.latitude, location.longitude),
                            15f
                        )
                    )
                }
            } catch (e: Exception) { }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = false),
            properties = MapProperties(
                isMyLocationEnabled = hasLocationPermission,
                mapStyleOptions = if (androidx.compose.foundation.isSystemInDarkTheme()) {
                    MapStyleOptions.loadRawResourceStyle(context, com.grameenlight.R.raw.map_dark)
                } else null
            )
        ) {
            poles.forEach { pole ->
                val markerColor = when (pole.currentStatus) {
                    PoleStatus.WORKING -> Color(0xFF4CAF50)
                    PoleStatus.FUSED -> Color(0xFFE24B4A)
                    PoleStatus.BURNING_DAY -> Color(0xFFEF9F27)
                    PoleStatus.UNKNOWN -> Color(0xFF9E9E9E)
                }

                // Custom marker logic would require a bitmap. For now using default with closest colors.
                Marker(
                    state = MarkerState(position = LatLng(pole.latitude, pole.longitude)),
                    icon = BitmapDescriptorFactory.defaultMarker(
                        when (pole.currentStatus) {
                            PoleStatus.WORKING -> BitmapDescriptorFactory.HUE_GREEN
                            PoleStatus.FUSED -> BitmapDescriptorFactory.HUE_RED
                            PoleStatus.BURNING_DAY -> BitmapDescriptorFactory.HUE_ORANGE
                            else -> BitmapDescriptorFactory.HUE_AZURE
                        }
                    ),
                    onClick = {
                        selectedPole = pole
                        coroutineScope.launch { poleSheetState.show() }
                        true
                    }
                )
            }
        }

        // Frosted TopAppBar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color.White.copy(alpha = 0.9f),
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Grameen-Light",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1D9E75),
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onThemeToggle) {
                    Icon(
                        imageVector = if (androidx.compose.foundation.isSystemInDarkTheme()) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = "Toggle Theme",
                        tint = Color(0xFF1D9E75)
                    )
                }
                Surface(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .clickable { showProfileSheet = true },
                    color = when (currentUser?.role) {
                        UserRole.RESIDENT -> Color(0xFF1D9E75)
                        UserRole.LINEMAN -> Color(0xFF185FA5)
                        UserRole.ADMIN -> Color(0xFFBA7517)
                        else -> Color.Gray
                    }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = currentUser?.name?.firstOrNull()?.toString()?.uppercase() ?: "?",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }

        // Pill Search Bar
        Surface(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 100.dp)
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .height(48.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray)
                Spacer(modifier = Modifier.width(12.dp))
                Text("Search poles or streets...", color = Color.Gray, fontSize = 14.sp)
            }
        }

        // Legend
        var legendExpanded by remember { mutableStateOf(false) }
        Surface(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            shadowElevation = 4.dp
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                if (legendExpanded) {
                    LegendItem(Color(0xFF4CAF50), "Working")
                    LegendItem(Color(0xFFE24B4A), "Fused")
                    LegendItem(Color(0xFFEF9F27), "Burning Day")
                    LegendItem(Color(0xFF9E9E9E), "Unknown")
                    IconButton(onClick = { legendExpanded = false }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.ExpandLess, contentDescription = null)
                    }
                } else {
                    IconButton(onClick = { legendExpanded = true }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF1D9E75))
                    }
                }
            }
        }

        // Profile Bottom Sheet
        if (showProfileSheet) {
            ModalBottomSheet(
                onDismissRequest = { showProfileSheet = false },
                sheetState = profileSheetState,
                containerColor = Color.White,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val roleColor = when (currentUser?.role) {
                        UserRole.RESIDENT -> Color(0xFF1D9E75)
                        UserRole.LINEMAN -> Color(0xFF185FA5)
                        UserRole.ADMIN -> Color(0xFFBA7517)
                        else -> Color.Gray
                    }
                    
                    Surface(
                        modifier = Modifier.size(80.dp),
                        shape = CircleShape,
                        color = roleColor
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = currentUser?.name?.firstOrNull()?.toString()?.uppercase() ?: "?",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 32.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = currentUser?.name ?: "User", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = roleColor.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = currentUser?.role?.name ?: "No Role",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            color = roleColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                    OutlinedButton(
                        onClick = { 
                            authViewModel.logout()
                            showProfileSheet = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Sign Out")
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        // Pole Detail Bottom Sheet
        if (selectedPole != null) {
            ModalBottomSheet(
                onDismissRequest = { selectedPole = null },
                sheetState = poleSheetState,
                containerColor = Color.White,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = selectedPole!!.poleId,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        StatusBadge(selectedPole!!.currentStatus)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(selectedPole!!.streetName, color = Color.Gray, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            coroutineScope.launch { poleSheetState.hide() }.invokeOnCompletion {
                                onNavigateToReport(selectedPole!!.poleId)
                                selectedPole = null
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D9E75))
                    ) {
                        Text("Report This Pole", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: PoleStatus) {
    val (color, label) = when (status) {
        PoleStatus.WORKING -> Color(0xFF4CAF50) to "WORKING"
        PoleStatus.FUSED -> Color(0xFFE24B4A) to "FUSED"
        PoleStatus.BURNING_DAY -> Color(0xFFEF9F27) to "BURNING DAY"
        PoleStatus.UNKNOWN -> Color(0xFF9E9E9E) to "UNKNOWN"
    }
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = color,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp
        )
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(label, fontSize = 12.sp, color = Color.Gray)
    }
}
