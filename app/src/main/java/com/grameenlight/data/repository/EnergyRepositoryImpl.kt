package com.grameenlight.data.repository

import com.grameenlight.data.local.ComplaintDao
import com.grameenlight.domain.model.RepairStatus
import com.grameenlight.domain.repository.EnergyRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import java.util.Calendar

class EnergyRepositoryImpl @Inject constructor(
    private val complaintDao: ComplaintDao
) : EnergyRepository {

    override suspend fun getEnergySavedThisMonth(): Double {
        val complaints = complaintDao.getComplaintsByStatus(RepairStatus.FIXED).first()
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)
        
        var resolvedBurningDayCount = 0
        complaints.forEach { c ->
            if (c.reportedStatus == com.grameenlight.domain.model.PoleStatus.BURNING_DAY && c.resolvedAt != null) {
                calendar.timeInMillis = c.resolvedAt
                if (calendar.get(Calendar.MONTH) == currentMonth && calendar.get(Calendar.YEAR) == currentYear) {
                    resolvedBurningDayCount++
                }
            }
        }
        
        return resolvedBurningDayCount * 0.4 * 10.0
    }

    override suspend fun getVillageRanking(): Int {
        return 1
    }
}
