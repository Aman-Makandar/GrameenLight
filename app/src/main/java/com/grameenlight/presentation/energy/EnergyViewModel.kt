package com.grameenlight.presentation.energy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grameenlight.domain.repository.EnergyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EnergyViewModel @Inject constructor(
    private val energyRepository: com.grameenlight.domain.repository.EnergyRepository,
    private val complaintRepository: com.grameenlight.domain.repository.ComplaintRepository
) : ViewModel() {

    private val _energyState = MutableStateFlow(EnergyState())
    val energyState: StateFlow<EnergyState> = _energyState

    init {
        loadEnergyData()
    }

    private fun loadEnergyData() {
        viewModelScope.launch {
            complaintRepository.getComplaints().collect { complaints ->
                val burningDayComplaints = complaints.filter { it.reportedStatus == com.grameenlight.domain.model.PoleStatus.BURNING_DAY }
                val resolvedBurningDay = burningDayComplaints.filter { it.repairStatus == com.grameenlight.domain.model.RepairStatus.FIXED }
                
                // FIX 6.1: Calculate energy saved correctly
                val kwhSaved = resolvedBurningDay.size * 4.0 // 4 kWh per resolved burning day complaint
                val co2Saved = kwhSaved * 0.82
                
                val percentage = if (burningDayComplaints.isNotEmpty()) {
                    (resolvedBurningDay.size.toFloat() / burningDayComplaints.size.toFloat()) * 100f
                } else 0f

                _energyState.value = EnergyState(
                    kwhSaved = kwhSaved,
                    co2Saved = co2Saved,
                    totalBurningDayCount = burningDayComplaints.size,
                    resolvedCount = resolvedBurningDay.size,
                    resolvedPercentage = percentage,
                    monthlyTrend = listOf("Oct" to 12f, "Nov" to 16f, "Dec" to 8f, "Jan" to 20f, "Feb" to 16f, "Mar" to kwhSaved.toFloat())
                )
            }
        }
    }
}

data class EnergyState(
    val kwhSaved: Double = 0.0,
    val co2Saved: Double = 0.0,
    val totalBurningDayCount: Int = 0,
    val resolvedCount: Int = 0,
    val villageRanking: Int = 0,
    val monthlyTrend: List<Pair<String, Float>> = emptyList(),
    val resolvedPercentage: Float = 0f
)
