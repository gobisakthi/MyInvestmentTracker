package com.personal.investmenttracker

import android.app.Application
import androidx.room.Room
import com.personal.investmenttracker.data.local.InvestmentDatabase
import com.personal.investmenttracker.data.repository.AccountRepository
import com.personal.investmenttracker.di.AppContainer
import com.personal.investmenttracker.di.InvestmentViewModelFactory

class InvestmentTrackerApplication : Application() {

    lateinit var container: AppContainer
        private set

    lateinit var viewModelFactory: InvestmentViewModelFactory
        private set

    override fun onCreate() {
        super.onCreate()
        val database = Room.databaseBuilder(
            applicationContext,
            InvestmentDatabase::class.java,
            InvestmentDatabase.DATABASE_NAME,
        )
            .addMigrations(*com.personal.investmenttracker.data.local.Migrations.ALL)
            .build()
        container = AppContainer(database)
        viewModelFactory = InvestmentViewModelFactory(container)
    }
}
