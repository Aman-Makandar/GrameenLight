package com.grameenlight.domain.usecase

import com.grameenlight.domain.model.Complaint
import com.grameenlight.domain.repository.ComplaintRepository
import javax.inject.Inject

class SubmitComplaintUseCase @Inject constructor(
    private val complaintRepository: ComplaintRepository
) {
    suspend operator fun invoke(complaint: Complaint) {
        complaintRepository.submitComplaint(complaint)
    }
}
