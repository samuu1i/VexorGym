package com.example.vexorgym.di

import com.example.vexorgym.data.repository.GymRepository
import com.example.vexorgym.data.repository.RemoteGymRepository

/**
 * Composición mínima de dependencias. Los ViewModels siguen usando [gymRepository];
 * acá se elige la implementación (mock vs red).
 */
object AppContainer {
    val gymRepository: GymRepository = RemoteGymRepository()
}
