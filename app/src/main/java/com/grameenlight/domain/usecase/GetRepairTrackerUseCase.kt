package com.grameenlight.domain.usecase

import com.grameenlight.domain.model.Complaint
import com.grameenlight.domain.repository.ComplaintRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRepairTrackerUseCase @Inject constructor(
    private val complaintRepository: ComplaintRepository
) {
    operator fun invoke(): Flow<List<Complaint>> {
        return complaintRepository.getComplaints()
    }
}
