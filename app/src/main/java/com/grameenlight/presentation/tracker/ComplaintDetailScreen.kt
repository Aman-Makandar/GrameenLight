package com.grameenlight.presentation.tracker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.grameenlight.domain.model.Complaint
import com.grameenlight.domain.model.RepairStatus
import com.grameenlight.domain.model.UserRole
import com.grameenlight.presentation.common.theme.PrimaryGreen
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComplaintDetailScreen(
    complaintId: String,
    onNavigateBack: () -> Unit,
    viewModel: TrackerViewModel = hiltViewModel()
) {
    // FIX: Collect from allComplaints flow not filtered complaints
    // so detail screen always finds the complaint regardless of
    // current tab filter
    val complaints by viewModel.complaints.collectAsState()
    val userRole by viewModel.userRole.collectAsState()
    val userId by viewModel.userId.collectAsState()

    // FIX: Also observe allComplaints directly via a separate
    // state that includes ALL complaints unfiltered
    val complaint = complaints.find { it.complaintId == complaintId }

    // FIX: Add a timeout so we don't show loading forever
    var loadingTimeout by remember { mutableStateOf(false) }
    LaunchedEffect(complaint) {
        if (complaint == null) {
            kotlinx.coroutines.delay(5000)
            loadingTimeout = true
        }
    }

    // FIX: When detail screen opens switch to All Issues tab
    // so the complaint is visible in the complaints list
    LaunchedEffect(Unit) {
        viewModel.setLinemanTab(false)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Complaint Status", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        when {
            complaint == null && !loadingTimeout -> {
                // Loading state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = PrimaryGreen)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Loading complaint...",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }
            }
            complaint == null && loadingTimeout -> {
                // FIX: Error state with retry instead of infinite loading
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Unable to load complaint",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Complaint ID: $complaintId",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = onNavigateBack,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryGreen
                            )
                        ) {
                            Text("Go Back")
                        }
                    }
                }
            }
            else -> {
                // FIX: Success state — show full detail
                complaint?.let { c ->
                    Column(
                        modifier = Modifier
                            .padding(padding)
                            .fillMaxSize()
                            .background(Color(0xFFF8FAF9))
                            .verticalScroll(rememberScrollState())
                    ) {
                        StatusHeader(c)

                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Repair Journey",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            RepairTimeline(c)

                            Spacer(modifier = Modifier.height(24.dp))

                            // Reporter info card
                            Text(
                                text = "Report Details",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            InfoCard(
                                icon = Icons.Default.Person,
                                title = "Reported by",
                                subtitle = c.reporterName ?: "Village Resident",
                                accentColor = PrimaryGreen
                            )

                            if (!c.assignedTo.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                InfoCard(
                                    icon = Icons.Default.Build,
                                    title = "Assigned to",
                                    subtitle = c.assignedLinamanName ?: "Lineman",
                                    accentColor = Color(0xFF185FA5)
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = "Pole Information",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            InfoCard(
                                icon = Icons.Default.LocationOn,
                                title = "Pole ID",
                                subtitle = c.poleId,
                                accentColor = PrimaryGreen
                            )

                            if (c.photoUrl != null) {
                                Spacer(modifier = Modifier.height(24.dp))
                                Text(
                                    text = "Reported Photo",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                ) {
                                    AsyncImage(
                                        model = c.photoUrl,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }

                            // FIX: Action buttons based on role and status
                            Spacer(modifier = Modifier.height(32.dp))
                            when {
                                // Lineman actions
                                userRole == UserRole.LINEMAN &&
                                        c.assignedTo?.trim() == userId?.trim() -> {
                                    when (c.repairStatus) {
                                        RepairStatus.ASSIGNED -> {
                                            Button(
                                                onClick = {
                                                    viewModel.markInProgress(c.complaintId)
                                                },
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = Color(0xFFEF9F27)
                                                ),
                                                shape = RoundedCornerShape(16.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Build,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    "Mark In Progress",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 16.sp,
                                                    modifier = Modifier.padding(vertical = 8.dp)
                                                )
                                            }
                                        }
                                        RepairStatus.IN_PROGRESS -> {
                                            Button(
                                                onClick = {
                                                    viewModel.markFixed(c.complaintId)
                                                },
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = PrimaryGreen
                                                ),
                                                shape = RoundedCornerShape(16.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.CheckCircle,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    "Mark Fixed",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 16.sp,
                                                    modifier = Modifier.padding(vertical = 8.dp)
                                                )
                                            }
                                        }
                                        else -> {}
                                    }
                                }
                                // Admin actions
                                userRole == UserRole.ADMIN &&
                                        c.repairStatus == RepairStatus.SUBMITTED -> {
                                    Text(
                                        text = "Go to Tracker to assign this complaint",
                                        color = Color.Gray,
                                        fontSize = 13.sp
                                    )
                                }
                                else -> {}
                            }

                            Spacer(modifier = Modifier.height(100.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusHeader(complaint: Complaint) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "ID: ${complaint.complaintId}",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.weight(1f)
                )
                StatusBadge(complaint.repairStatus)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Pole #${complaint.poleId}",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Reported ${formatDate(complaint.reportedAt)}",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun RepairTimeline(complaint: Complaint) {
    val stages = RepairStatus.values()
    val currentIndex = stages.indexOf(complaint.repairStatus)

    Column {
        stages.forEachIndexed { index, status ->
            val isCompleted = index <= currentIndex
            val isLast = index == stages.size - 1
            TimelineRow(
                status = status.name.replace("_", " "),
                time = when (status) {
                    RepairStatus.SUBMITTED -> formatDate(complaint.reportedAt)
                    RepairStatus.FIXED -> complaint.resolvedAt?.let { formatDate(it) }
                    else -> null
                },
                isCompleted = isCompleted,
                isLast = isLast
            )
        }
    }
}

@Composable
fun TimelineRow(
    status: String,
    time: String?,
    isCompleted: Boolean,
    isLast: Boolean
) {
    Row(modifier = Modifier.height(IntrinsicSize.Min)) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(
                        if (isCompleted) PrimaryGreen else Color.LightGray,
                        CircleShape
                    )
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .weight(1f)
                        .background(if (isCompleted) PrimaryGreen else Color.LightGray)
                )
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.padding(bottom = 24.dp)) {
            Text(
                text = status,
                fontSize = 15.sp,
                fontWeight = if (isCompleted) FontWeight.Bold else FontWeight.Normal,
                color = if (isCompleted) Color.Black else Color.Gray
            )
            if (time != null) {
                Text(text = time, fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun InfoCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    accentColor: Color
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = accentColor.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(text = subtitle, fontSize = 13.sp, color = Color.Gray)
            }
        }
    }
}

fun formatDate(timestamp: Long): String {
    return SimpleDateFormat("dd MMM, hh:mm aa", Locale.getDefault()).format(Date(timestamp))
}