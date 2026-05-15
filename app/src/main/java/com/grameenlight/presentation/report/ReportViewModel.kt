package com.grameenlight.presentation.report

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grameenlight.domain.model.Complaint
import com.grameenlight.domain.model.Pole
import com.grameenlight.domain.model.PoleStatus
import com.grameenlight.domain.model.RepairStatus
import com.grameenlight.domain.repository.AuthRepository
import com.grameenlight.domain.repository.PoleRepository
import com.grameenlight.domain.repository.StorageRepository
import com.grameenlight.domain.usecase.GenerateComplaintIdUseCase
import com.grameenlight.domain.usecase.SubmitComplaintUseCase
import com.grameenlight.domain.usecase.UpdatePoleStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val poleRepository: PoleRepository,
    private val authRepository: AuthRepository,
    private val storageRepository: StorageRepository,
    private val submitComplaintUseCase: SubmitComplaintUseCase,
    private val generateComplaintIdUseCase: GenerateComplaintIdUseCase,
    private val updatePoleStatusUseCase: UpdatePoleStatusUseCase
) : ViewModel() {

    private val poleId: String = checkNotNull(savedStateHandle["poleId"])

    private val _pole = MutableStateFlow<Pole?>(null)
    val pole: StateFlow<Pole?> = _pole

    private val _uiState = MutableStateFlow<ReportUiState>(ReportUiState.Idle)
    val uiState: StateFlow<ReportUiState> = _uiState

    init {
        viewModelScope.launch {
            _pole.value = poleRepository.getPoleById(poleId)
        }
    }

    fun submitReport(selectedStatus: PoleStatus, photoUri: Uri?) {
        viewModelScope.launch {
            _uiState.value = ReportUiState.Loading
            android.util.Log.d("SUBMIT", "Starting submission for pole: $poleId")
            try {
                val currentUser = authRepository.getCurrentUser()
                val reportedBy = currentUser?.uid ?: "Unknown"
                val reporterName = currentUser?.name ?: "Unknown"
                android.util.Log.d("SUBMIT", "Reported by: $reporterName ($reportedBy)")

                val complaintId = generateComplaintIdUseCase()
                android.util.Log.d("SUBMIT", "Generated complaint ID: $complaintId")

                var photoUrl: String? = null
                if (photoUri != null) {
                    android.util.Log.d("SUBMIT", "Uploading photo: $photoUri")
                    val uploadResult = storageRepository.uploadComplaintPhoto(complaintId, photoUri)
                    if (uploadResult.isSuccess) {
                        photoUrl = uploadResult.getOrNull()
                        android.util.Log.d("SUBMIT", "Photo upload success: $photoUrl")
                    } else {
                        android.util.Log.w("SUBMIT", "Photo upload failed: ${uploadResult.exceptionOrNull()?.message}")
                    }
                }

                val complaint = Complaint(
                    complaintId = complaintId,
                    poleId = poleId,
                    reportedStatus = selectedStatus,
                    photoUrl = photoUrl,
                    reportedAt = System.currentTimeMillis(),
                    reportedBy = reportedBy,
                    reporterName = reporterName,
                    assignedTo = null,
                    repairStatus = RepairStatus.SUBMITTED,
                    resolvedAt = null,
                    isSynced = false 
                )

                android.util.Log.d("SUBMIT", "Calling submitComplaintUseCase")
                submitComplaintUseCase(complaint)
                
                android.util.Log.d("SUBMIT", "Calling updatePoleStatusUseCase")
                updatePoleStatusUseCase(poleId, selectedStatus)

                android.util.Log.d("SUBMIT", "Submission success!")
                _uiState.value = ReportUiState.Success(complaintId)
            } catch (e: Exception) {
                android.util.Log.e("SUBMIT", "Submission failed: ${e.message}", e)
                _uiState.value = ReportUiState.Error(e.message ?: "Failed to submit report")
            }
        }
    }
    
    fun resetState() {
        _uiState.value = ReportUiState.Idle
    }
}

sealed class ReportUiState {
    object Idle : ReportUiState()
    object Loading : ReportUiState()
    data class Success(val complaintId: String) : ReportUiState()
    data class Error(val message: String) : ReportUiState()
}
