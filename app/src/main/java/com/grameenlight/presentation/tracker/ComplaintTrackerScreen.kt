package com.grameenlight.presentation.tracker

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.grameenlight.domain.model.Complaint
import com.grameenlight.domain.model.RepairStatus
import com.grameenlight.domain.model.User
import com.grameenlight.domain.model.UserRole
import com.grameenlight.presentation.common.theme.PrimaryGreen
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComplaintTrackerScreen(
    onNavigateToDetail: (String) -> Unit,
    viewModel: TrackerViewModel = hiltViewModel()
) {
    val complaints by viewModel.complaints.collectAsState()
    val filter by viewModel.filter.collectAsState()
    val userRole by viewModel.userRole.collectAsState()
    val linemen by viewModel.linemen.collectAsState()
    val showMyJobsTab by viewModel.showMyJobsTab.collectAsState()

    var showAssignDialogFor by remember { mutableStateOf<String?>(null) }

    if (showAssignDialogFor != null) {
        AssignLinemanDialog(
            linemen = linemen,
            onDismiss = { showAssignDialogFor = null },
            onAssign = { linemanId ->
                viewModel.assignComplaint(showAssignDialogFor!!, linemanId)
                showAssignDialogFor = null
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Repair Tracker", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF8FAF9))
        ) {
            // === LINEMAN TABS ===
            if (userRole == UserRole.LINEMAN) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { viewModel.setLinemanTab(true) },
                        color = if (showMyJobsTab) PrimaryGreen else Color.White,
                        shape = RoundedCornerShape(12.dp),
                        border = if (!showMyJobsTab) androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray) else null
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Work,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (showMyJobsTab) Color.White else Color.Gray
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "My Jobs",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (showMyJobsTab) Color.White else Color.Gray
                            )
                        }
                    }
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { viewModel.setLinemanTab(false) },
                        color = if (!showMyJobsTab) PrimaryGreen else Color.White,
                        shape = RoundedCornerShape(12.dp),
                        border = if (showMyJobsTab) androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray) else null
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.List,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (!showMyJobsTab) Color.White else Color.Gray
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "All Issues",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (!showMyJobsTab) Color.White else Color.Gray
                            )
                        }
                    }
                }
            }

            // Summary Stats Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryStatCard(
                    label = "Pending",
                    count = complaints.count { it.repairStatus == RepairStatus.SUBMITTED }.toString(),
                    color = Color.Gray,
                    modifier = Modifier.weight(1f)
                )
                SummaryStatCard(
                    label = "In Progress",
                    count = complaints.count { it.repairStatus == RepairStatus.ASSIGNED || it.repairStatus == RepairStatus.IN_PROGRESS }.toString(),
                    color = Color(0xFF2196F3),
                    modifier = Modifier.weight(1f)
                )
                SummaryStatCard(
                    label = "Fixed",
                    count = complaints.count { it.repairStatus == RepairStatus.FIXED }.toString(),
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f)
                )
            }

            // Filter Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChipModern(
                        selected = filter == null,
                        onClick = { viewModel.setFilter(null) },
                        label = "All"
                    )
                }
                items(RepairStatus.values()) { status ->
                    FilterChipModern(
                        selected = filter == status,
                        onClick = { viewModel.setFilter(status) },
                        label = status.name.replace("_", " ")
                    )
                }
            }

            if (complaints.isEmpty()) {
                EmptyTracker(
                    isLinemanMyJobs = userRole == UserRole.LINEMAN && showMyJobsTab
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    items(complaints) { complaint ->
                        // Lineman can only take actions on My Jobs tab items
                        val isLinemanAssigned = userRole == UserRole.LINEMAN && showMyJobsTab
                        ModernComplaintCard(
                            complaint = complaint,
                            userRole = userRole,
                            isLinemanOwned = isLinemanAssigned,
                            onClick = { onNavigateToDetail(complaint.complaintId) },
                            onAssign = { showAssignDialogFor = complaint.complaintId },
                            onMarkFixed = { viewModel.markFixed(complaint.complaintId) },
                            onMarkInProgress = { viewModel.markInProgress(complaint.complaintId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryStatCard(label: String, count: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = count, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
            Text(text = label, fontSize = 11.sp, color = Color.Gray)
        }
    }
}

@Composable
fun FilterChipModern(selected: Boolean, onClick: () -> Unit, label: String) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() },
        color = if (selected) PrimaryGreen else Color.White,
        shape = RoundedCornerShape(20.dp),
        border = if (selected) null else androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = if (selected) Color.White else Color.Gray,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun ModernComplaintCard(
    complaint: Complaint,
    userRole: UserRole,
    isLinemanOwned: Boolean,
    onClick: () -> Unit,
    onAssign: () -> Unit,
    onMarkFixed: () -> Unit,
    onMarkInProgress: () -> Unit
) {
    val statusColor = when (complaint.repairStatus) {
        RepairStatus.SUBMITTED -> Color.Gray
        RepairStatus.ASSIGNED -> Color(0xFF2196F3)
        RepairStatus.IN_PROGRESS -> Color(0xFFEF9F27)
        RepairStatus.FIXED -> Color(0xFF4CAF50)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(6.dp)
                    .background(statusColor)
            )
            Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = complaint.complaintId.takeLast(8), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    StatusBadge(complaint.repairStatus)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Pole: ${complaint.poleId}", color = Color.Gray, fontSize = 13.sp)

                if (complaint.reporterName.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color.LightGray)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reported by: ${complaint.reporterName}", fontSize = 12.sp, color = Color.Gray)
                    }
                }

                if (complaint.assignedLinamanName != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color(0xFF2196F3))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Assigned to: ${complaint.assignedLinamanName}", fontSize = 12.sp, color = Color(0xFF2196F3))
                    }
                }

                // ADMIN: can assign any SUBMITTED complaint
                if (userRole == UserRole.ADMIN && complaint.repairStatus == RepairStatus.SUBMITTED) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onAssign,
                        modifier = Modifier.fillMaxWidth().height(40.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                    ) {
                        Icon(Icons.Default.AssignmentInd, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Assign Lineman", fontSize = 13.sp)
                    }
                }

                // LINEMAN: show action buttons only on My Jobs tab (isLinemanOwned)
                if (userRole == UserRole.LINEMAN && isLinemanOwned && complaint.repairStatus != RepairStatus.FIXED) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (complaint.repairStatus == RepairStatus.ASSIGNED) {
                            OutlinedButton(
                                onClick = onMarkInProgress,
                                modifier = Modifier.weight(1f).height(40.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF9F27)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF9F27))
                            ) {
                                Text("In Progress", fontSize = 12.sp)
                            }
                        }
                        Button(
                            onClick = onMarkFixed,
                            modifier = Modifier.weight(1f).height(40.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Mark Fixed", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: RepairStatus) {
    val color = when (status) {
        RepairStatus.SUBMITTED -> Color.Gray
        RepairStatus.ASSIGNED -> Color(0xFF2196F3)
        RepairStatus.IN_PROGRESS -> Color(0xFFEF9F27)
        RepairStatus.FIXED -> Color(0xFF4CAF50)
    }
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = status.name.replace("_", " "),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            color = color,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp
        )
    }
}

@Composable
fun EmptyTracker(isLinemanMyJobs: Boolean = false) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                if (isLinemanMyJobs) Icons.Default.WorkOff else Icons.Default.Task,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color.LightGray
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = if (isLinemanMyJobs) "No jobs assigned yet.\nCheck back later." else "No reports found",
                color = Color.Gray,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun AssignLinemanDialog(
    linemen: List<User>,
    onDismiss: () -> Unit,
    onAssign: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Assign Lineman", fontWeight = FontWeight.Bold) },
        text = {
            if (linemen.isEmpty()) {
                Text("No linemen registered yet.", color = Color.Gray)
            } else {
                Column {
                    linemen.forEach { lineman ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onAssign(lineman.uid) }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(modifier = Modifier.size(32.dp), shape = CircleShape, color = Color(0xFF2196F3)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(lineman.name.take(1).uppercase(), fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(lineman.name, fontWeight = FontWeight.Medium)
                                Text(lineman.email, fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                        HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.5f))
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(24.dp)
    )
}

fun Modifier.size(size: androidx.compose.ui.unit.Dp) = this.then(Modifier.width(size).height(size))
