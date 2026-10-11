package com.example.aprendoiasantiago.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aprendoiasantiago.data.local.CapsulaEntity
import com.example.aprendoiasantiago.data.repository.JuegoRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class EscanerViewModel(juego: JuegoRepository) : ViewModel() {
    val capsulas: StateFlow<List<CapsulaEntity>> = juego.capsulas.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )
}
