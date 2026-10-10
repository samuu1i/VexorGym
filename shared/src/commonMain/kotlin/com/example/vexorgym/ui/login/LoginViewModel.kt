package com.example.vexorgym.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vexorgym.data.repository.GymRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val repository: GymRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            com.example.vexorgym.ui.navigation.DeepLinkManager.deepLinkError.collect { error ->
                if (error != null) {
                    _uiState.update { it.copy(errorMessage = error) }
                    com.example.vexorgym.ui.navigation.DeepLinkManager.onErrorHandled()
                }
            }
        }
        viewModelScope.launch {
            if (repository.hasValidSession()) {
                _uiState.update { it.copy(isLoggedIn = true) }
            }
        }
    }

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, errorMessage = null, successMessage = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, errorMessage = null, successMessage = null) }
    }

    fun login() {
        val current = _uiState.value
        if (current.isLoading) return

        val email = current.email.trim()
        val password = current.password
        val validationError = validateCredentials(email, password, isRegister = false)
        if (validationError != null) {
            _uiState.update { it.copy(errorMessage = validationError) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            repository.login(email, password)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false, isLoggedIn = true) }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "No se pudo iniciar sesión.",
                        )
                    }
                }
        }
    }

    fun register() {
        val current = _uiState.value
        if (current.isLoading) return

        val email = current.email.trim()
        val password = current.password
        val validationError = validateCredentials(email, password, isRegister = true)
        if (validationError != null) {
            _uiState.update { it.copy(errorMessage = validationError) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            repository.register(email, password)
                .onSuccess { isLogged ->
                    if (isLogged) {
                        _uiState.update { it.copy(isLoading = false, isLoggedIn = true) }
                    } else {
                        _uiState.update { 
                            it.copy(
                                isLoading = false, 
                                successMessage = "Revisá tu correo para confirmar tu cuenta." 
                            ) 
                        }
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "No se pudo crear la cuenta.",
                        )
                    }
                }
        }
    }

    private fun validateCredentials(email: String, password: String, isRegister: Boolean): String? = when {
        !EMAIL_REGEX.matches(email) -> "Ingresá un email con formato válido."
        password.isBlank() -> "La contraseña no puede estar vacía."
        isRegister && password.length < MIN_PASSWORD_LENGTH -> "La contraseña es demasiado corta. Debe tener al menos $MIN_PASSWORD_LENGTH caracteres."
        else -> null
    }

    private companion object {
        val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        
        // Constante alineada con la configuración predeterminada de longitud de contraseña de Supabase Auth
        const val MIN_PASSWORD_LENGTH = 6
    }
}
