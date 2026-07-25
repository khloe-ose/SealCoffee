package com.mobdeve.s15.group4.sealcoffee

import android.app.Application
import com.mobdeve.s15.group4.sealcoffee.data.SealCoffeeRepository
import com.mobdeve.s15.group4.sealcoffee.data.local.AppDatabase

class SealCoffeeApplication : Application() {
    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
    val repository: SealCoffeeRepository by lazy { SealCoffeeRepository(database) }
    val session: SessionManager by lazy { SessionManager(this) }
}
