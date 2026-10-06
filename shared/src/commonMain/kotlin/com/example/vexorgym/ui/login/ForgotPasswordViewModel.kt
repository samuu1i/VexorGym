package com.example.vexorgym.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vexorgym.data.repository.GymRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ForgotPasswordUiState(
    val email: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
)

class ForgotPasswordViewModel(
    private val repository: GymRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, errorMessage = null, successMessage = null) }
    }

    fun sendResetLink() {
        val current = _uiState.value
        if (current.isLoading) return

        val email = current.email.trim()
        if (!EMAIL_REGEX.matches(email)) {
            _uiState.update { it.copy(errorMessage = "Ingresá un email con formato válido.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            repository.sendPasswordResetEmail(email)
                .onSuccess {
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            successMessage = "Revisá tu correo electrónico. Te enviamos un enlace para restablecer tu contraseña."
                        ) 
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "No se pudo enviar el enlace."
                        )
                    }
                }
        }
    }

    private companion object {
        val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$")
    }
}