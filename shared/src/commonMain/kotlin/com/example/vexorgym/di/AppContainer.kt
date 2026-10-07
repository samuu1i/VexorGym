package com.example.vexorgym.di

import app.cash.sqldelight.db.SqlDriver
import com.example.vexorgym.data.local.GymDatabase
import com.example.vexorgym.data.local.LocalGymDataSource
import com.example.vexorgym.data.repository.GymRepository
import com.example.vexorgym.data.repository.RemoteGymRepository

object AppContainer {
    private var _gymRepository: GymRepository? = null

    val gymRepository: GymRepository
        get() = _gymRepository ?: error("AppContainer no fue inicializado")

    fun init(driver: SqlDriver) {
        if (_gymRepository == null) {
            val database = GymDatabase(driver)
            val localDataSource = LocalGymDataSource(database)
            _gymRepository = RemoteGymRepository(localDataSource = localDataSource)
        }
    }
}
