package com.example.vexorgym.ui.login

import com.example.vexorgym.data.model.Exercise
import com.example.vexorgym.data.model.Routine
import com.example.vexorgym.data.model.WeekDay
import com.example.vexorgym.data.repository.GymRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `register should show error when password is too short`() {
        // Arrange
        val repository = FakeGymRepository()
        val viewModel = LoginViewModel(repository)
        
        viewModel.onEmailChange("test@example.com")
        viewModel.onPasswordChange("12345") // Less than MIN_PASSWORD_LENGTH (6)

        // Act
        viewModel.register()

        // Assert
        val uiState = viewModel.uiState.value
        assertEquals("La contraseña es demasiado corta. Debe tener al menos 6 caracteres.", uiState.errorMessage)
    }

    @Test
    fun `register should proceed when password length is exactly the minimum`() {
        // Arrange
        val repository = FakeGymRepository()
        val viewModel = LoginViewModel(repository)
        
        viewModel.onEmailChange("test@example.com")
        viewModel.onPasswordChange("123456") // Exactly MIN_PASSWORD_LENGTH (6)

        // Act
        viewModel.register()

        // Assert
        val uiState = viewModel.uiState.value
        // Validation error is null, the viewmodel should be in loading state
        assertNull(uiState.errorMessage)
    }

    @Test
    fun `register should show error when email is invalid`() {
        // Arrange
        val repository = FakeGymRepository()
        val viewModel = LoginViewModel(repository)
        
        viewModel.onEmailChange("invalid-email")
        viewModel.onPasswordChange("password123")

        // Act
        viewModel.register()

        // Assert
        val uiState = viewModel.uiState.value
        assertEquals("Ingresá un email con formato válido.", uiState.errorMessage)
    }
}

class FakeGymRepository : GymRepository {
    override suspend fun login(email: String, password: String): Result<Unit> = Result.success(Unit)
    override suspend fun register(email: String, password: String): Result<Boolean> = Result.success(true)
    override suspend fun logout(): Result<Unit> = Result.success(Unit)
    override suspend fun hasValidSession(): Boolean = false
    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> = Result.success(Unit)
    override suspend fun updatePassword(newPassword: String): Result<Unit> = Result.success(Unit)
    override fun observeRoutine(): Flow<Routine> = emptyFlow()
    override fun observeCatalog(): Flow<List<Exercise>> = emptyFlow()
    override fun observeExercise(exerciseId: String): Flow<Exercise?> = emptyFlow()
    override suspend fun addExerciseToDay(day: WeekDay, exerciseId: String): Result<Unit> = Result.success(Unit)
    override suspend fun createExerciseForDay(day: WeekDay, name: String, muscleGroup: String): Result<Unit> = Result.success(Unit)
    override suspend fun removeExerciseFromDay(day: WeekDay, exerciseId: String): Result<Unit> = Result.success(Unit)
    override suspend fun updateRoutineOrder(day: WeekDay, orderedIds: List<String>): Result<Unit> = Result.success(Unit)
    override suspend fun addSession(exerciseId: String): Result<Unit> = Result.success(Unit)
    override suspend fun deleteSession(exerciseId: String, sessionId: String): Result<Unit> = Result.success(Unit)
    override suspend fun addSet(exerciseId: String, sessionId: String, weightKg: Double, repetitions: Int): Result<Unit> = Result.success(Unit)
    override suspend fun deleteSet(exerciseId: String, sessionId: String, setId: String): Result<Unit> = Result.success(Unit)
}
