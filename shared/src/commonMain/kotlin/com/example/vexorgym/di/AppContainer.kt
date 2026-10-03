package com.example.vexorgym.di

import com.example.vexorgym.data.repository.GymRepository
import com.example.vexorgym.data.repository.MockGymRepository

/**
 * Composición mínima de dependencias. Para Firebase/Supabase,
 * cambiá únicamente la instancia de [gymRepository].
 */
object AppContainer {
    val gymRepository: GymRepository = MockGymRepository()
}
