package com.grameenlight.presentation.report

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.grameenlight.domain.model.PoleStatus
import com.grameenlight.presentation.common.theme.PrimaryGreen
import com.grameenlight.presentation.common.theme.SecondaryGreen
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickReportScreen(
    onNavigateBack: () -> Unit,
    onNavigateToTracker: () -> Unit,
    viewModel: ReportViewModel = hiltViewModel()
) {
    val pole by viewModel.pole.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    
    val context = LocalContext.current
    var selectedStatus by remember { mutableStateOf<PoleStatus?>(null) }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }
    
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) photoUri = tempPhotoUri
    }

    fun createTempImageUri(): Uri {
        val tempFile = File(context.cacheDir, "images").apply { mkdirs() }
        val file = File.createTempFile("complaint_", ".jpg", tempFile)
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    if (uiState is ReportUiState.Success) {
        SuccessDialog(
            complaintId = (uiState as ReportUiState.Success).complaintId,
            onDismiss = onNavigateBack,
            onTrack = onNavigateToTracker
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quick Report", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryGreen)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF8FAF9))
        ) {
            // Green Header Info
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PrimaryGreen)
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Column {
                    Text(text = "Reporting Pole", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                    Text(text = pole?.poleId ?: "...", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text(text = pole?.streetName ?: "...", color = Color.White, fontSize = 16.sp)
                }
            }

            Column(modifier = Modifier.padding(24.dp)) {
                Text(text = "What is the issue?", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                StatusCard(
                    title = "Working",
                    icon = Icons.Default.CheckCircle,
                    isSelected = selectedStatus == PoleStatus.WORKING,
                    color = Color(0xFF4CAF50),
                    onClick = { selectedStatus = PoleStatus.WORKING }
                )
                Spacer(modifier = Modifier.height(12.dp))
                StatusCard(
                    title = "Fused / Not Lighting",
                    icon = Icons.Default.Lightbulb,
                    isSelected = selectedStatus == PoleStatus.FUSED,
                    color = Color(0xFFE24B4A),
                    onClick = { selectedStatus = PoleStatus.FUSED }
                )
                Spacer(modifier = Modifier.height(12.dp))
                StatusCard(
                    title = "Burning in Day",
                    icon = Icons.Default.WbSunny,
                    isSelected = selectedStatus == PoleStatus.BURNING_DAY,
                    color = Color(0xFFEF9F27),
                    onClick = { selectedStatus = PoleStatus.BURNING_DAY }
                )

                Spacer(modifier = Modifier.height(32.dp))
                Text(text = "Capture Photo (Optional)", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.Gray)
                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
                        .clickable {
                            tempPhotoUri = createTempImageUri()
                            cameraLauncher.launch(tempPhotoUri!!)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (photoUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(photoUri),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = PrimaryGreen)
                            Text("Tap to add photo", fontSize = 12.sp, color = PrimaryGreen)
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = { selectedStatus?.let { viewModel.submitReport(it, photoUri) } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(0.dp),
                    enabled = selectedStatus != null && uiState !is ReportUiState.Loading
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.horizontalGradient(listOf(SecondaryGreen, PrimaryGreen)),
                                shape = RoundedCornerShape(28.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (uiState is ReportUiState.Loading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Submit Report", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusCard(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) color else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) color.copy(alpha = 0.1f) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) color else Color.Gray,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) color else Color.Black
            )
            Spacer(modifier = Modifier.weight(1f))
            if (isSelected) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = color)
            }
        }
    }
}

@Composable
fun SuccessDialog(
    complaintId: String,
    onDismiss: () -> Unit,
    onTrack: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onTrack,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
            ) {
                Text("Track in Tracker")
            }
        },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Report Submitted!", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text("Your complaint has been registered.")
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = Color.LightGray.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = complaintId,
                        modifier = Modifier.padding(8.dp),
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray
                    )
                }
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White
    )
}
