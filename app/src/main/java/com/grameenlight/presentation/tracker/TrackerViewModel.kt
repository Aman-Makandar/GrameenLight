package com.grameenlight.presentation.tracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.grameenlight.domain.model.Complaint
import com.grameenlight.domain.model.RepairStatus
import com.grameenlight.domain.model.UserRole
import com.grameenlight.domain.model.User
import com.grameenlight.domain.repository.AuthRepository
import com.grameenlight.domain.usecase.AssignComplaintUseCase
import com.grameenlight.domain.usecase.GetRepairTrackerUseCase
import com.grameenlight.domain.usecase.MarkComplaintFixedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrackerViewModel @Inject constructor(
    private val getRepairTrackerUseCase: GetRepairTrackerUseCase,
    private val assignComplaintUseCase: AssignComplaintUseCase,
    private val markComplaintFixedUseCase: MarkComplaintFixedUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _filter = MutableStateFlow<RepairStatus?>(null)
    val filter: StateFlow<RepairStatus?> = _filter

    private val _userRole = MutableStateFlow<UserRole>(UserRole.RESIDENT)
    val userRole: StateFlow<UserRole> = _userRole

    // FIX: Always use FirebaseAuth.currentUser?.uid directly
    // to ensure we get the real-time UID not a stale cached value
    private val _userId = MutableStateFlow<String?>(
        FirebaseAuth.getInstance().currentUser?.uid
    )
    val userId: StateFlow<String?> = _userId

    private val _linemen = MutableStateFlow<List<User>>(emptyList())
    val linemen: StateFlow<List<User>> = _linemen

    private val _showMyJobsTab = MutableStateFlow(true)
    val showMyJobsTab: StateFlow<Boolean> = _showMyJobsTab

    init {
        viewModelScope.launch {
            // FIX: Get UID from FirebaseAuth directly first
            // then also fetch from repository for role
            val firebaseUid = FirebaseAuth.getInstance().currentUser?.uid
            _userId.value = firebaseUid
            android.util.Log.d("TRACKER", "Firebase UID on init: $firebaseUid")

            val user = authRepository.getCurrentUser()
            if (user != null) {
                _userRole.value = user.role
                // Double check UID matches Firebase
                if (user.uid != firebaseUid) {
                    android.util.Log.w("TRACKER",
                        "UID mismatch! Firebase: $firebaseUid, DB: ${user.uid}")
                    // Trust Firebase Auth UID
                    _userId.value = firebaseUid
                } else {
                    _userId.value = user.uid
                }
                android.util.Log.d("TRACKER",
                    "User loaded: ${user.name}, role: ${user.role}, uid: ${user.uid}")
            }
            fetchLinemen()
        }
    }

    private fun fetchLinemen() {
        viewModelScope.launch {
            authRepository.getAllLinemen().onSuccess {
                _linemen.value = it
                android.util.Log.d("TRACKER",
                    "Fetched ${it.size} linemen: ${it.map { l -> "${l.name}:${l.uid}" }}")
            }
        }
    }

    fun setLinemanTab(showMyJobs: Boolean) {
        _showMyJobsTab.value = showMyJobs
    }

    private val allComplaints: StateFlow<List<Complaint>> = getRepairTrackerUseCase()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val complaints: StateFlow<List<Complaint>> = combine(
        allComplaints,
        _filter,
        _userRole,
        _userId,
        _showMyJobsTab
    ) { complaints, currentFilter, role, uid, showMyJobs ->

        // FIX: Log every time filter runs so we can debug
        android.util.Log.d("TRACKER",
            "Filter run — role: $role, uid: $uid, showMyJobs: $showMyJobs, " +
                    "total complaints: ${complaints.size}")

        complaints.forEach { c ->
            android.util.Log.d("TRACKER",
                "Complaint ${c.complaintId}: assignedTo=${c.assignedTo}, status=${c.repairStatus}")
        }

        val roleFiltered = when (role) {
            UserRole.RESIDENT -> complaints
            UserRole.LINEMAN -> if (showMyJobs) {
                // FIX: Trim both values before comparing to avoid
                // whitespace or case mismatch issues
                val filtered = complaints.filter {
                    it.assignedTo?.trim() == uid?.trim()
                }
                android.util.Log.d("TRACKER",
                    "My Jobs filter: uid=$uid, matched ${filtered.size} complaints")
                filtered
            } else {
                complaints
            }
            UserRole.ADMIN -> complaints
        }

        if (currentFilter == null) roleFiltered
        else roleFiltered.filter { it.repairStatus == currentFilter }

    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun setFilter(status: RepairStatus?) {
        _filter.value = status
    }

    fun assignComplaint(complaintId: String, linemanId: String) {
        viewModelScope.launch {
            val linemanName = _linemen.value.find { it.uid == linemanId }?.name ?: "Lineman"
            android.util.Log.d("TRACKER",
                "Assigning complaint $complaintId to lineman $linemanId ($linemanName)")
            val result = assignComplaintUseCase(complaintId, linemanId, linemanName)
            if (result.isSuccess) {
                android.util.Log.d("TRACKER", "Assignment successful")
            } else {
                android.util.Log.e("TRACKER",
                    "Assignment failed: ${result.exceptionOrNull()?.message}")
            }
        }
    }

    fun markFixed(complaintId: String) {
        viewModelScope.launch {
            val result = markComplaintFixedUseCase(complaintId)
            if (result.isSuccess) {
                android.util.Log.d("TRACKER", "Complaint $complaintId marked as fixed")
            } else {
                android.util.Log.e("TRACKER",
                    "Failed to mark fixed: ${result.exceptionOrNull()?.message}")
            }
        }
    }

    fun markInProgress(complaintId: String) {
        viewModelScope.launch {
            try {
                com.google.firebase.database.FirebaseDatabase.getInstance()
                    .getReference("complaints")
                    .child(complaintId)
                    .child("repairStatus")
                    .setValue(RepairStatus.IN_PROGRESS.name)
                android.util.Log.d("TRACKER", "Complaint $complaintId marked IN_PROGRESS")
            } catch (e: Exception) {
                android.util.Log.e("TRACKER", "Failed to mark in progress: ${e.message}")
            }
        }
    }
}