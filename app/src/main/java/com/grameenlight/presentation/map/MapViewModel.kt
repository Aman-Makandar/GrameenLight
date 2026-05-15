package com.grameenlight.presentation.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grameenlight.domain.model.Pole
import com.grameenlight.domain.usecase.GetPolesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val getPolesUseCase: GetPolesUseCase
) : ViewModel() {

    private val _poles = MutableStateFlow<List<Pole>>(emptyList())
    val poles: StateFlow<List<Pole>> = _poles

    init {
        viewModelScope.launch {
            // Trigger sync
            try {
                getPolesUseCase.syncFromFirebase()
            } catch(e: Exception) {
                e.printStackTrace()
            }
            
            getPolesUseCase().collect { poleList ->
                _poles.value = poleList
            }
        }
    }
}
