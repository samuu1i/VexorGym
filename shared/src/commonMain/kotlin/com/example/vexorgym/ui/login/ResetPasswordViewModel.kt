package com.example.vexorgym.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vexorgym.data.repository.GymRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ResetPasswordUiState(
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isUpdated: Boolean = false,
)

class ResetPasswordViewModel(
    private val repository: GymRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResetPasswordUiState())
    val uiState: StateFlow<ResetPasswordUiState> = _uiState.asStateFlow()

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, errorMessage = null, successMessage = null) }
    }

    fun onConfirmPasswordChange(value: String) {
        _uiState.update { it.copy(confirmPassword = value, errorMessage = null, successMessage = null) }
    }

    fun resetPassword() {
        val current = _uiState.value
        if (current.isLoading) return

        if (current.password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "La contraseña no puede estar vacía.") }
            return
        }
        
        if (current.password != current.confirmPassword) {
            _uiState.update { it.copy(errorMessage = "Las contraseñas no coinciden.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            repository.updatePassword(current.password)
                .onSuccess {
                    // Cierra sesión obligatoriamente para forzar al usuario a hacer login de nuevo, pero no es estrictamente necesario,
                    // De todos modos loguearse de nuevo después de la recupreración es buena práctica.
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            isUpdated = true,
                            successMessage = "Contraseña actualizada correctamente. Ya podés iniciar sesión con tu nueva contraseña."
                        ) 
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "No se pudo actualizar la contraseña."
                        )
                    }
                }
        }
    }
}