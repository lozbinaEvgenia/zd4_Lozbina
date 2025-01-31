package com.example.crimeintent

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Room
import java.util.UUID

class CrimeRepository private constructor(context: Context) {

    private val database: CrimeDatabase = Room.databaseBuilder(
        context.applicationContext,
        CrimeDatabase::class.java,
        CrimeDatabase.DATABASE_NAME
    )
        .addMigrations(CrimeDatabase.migration_1_2)
        .build()

    private val crimeDao = database.crimeDao()

    fun getCrimes(): LiveData<List<Crime>> {
        return crimeDao.getCrimes()
    }

    fun getCrime(id: UUID): LiveData<Crime?> {
        return crimeDao.getCrime(id)
    }

    fun addCrime(crime: Crime) {
        crimeDao.addCrime(crime)
    }

    fun updateCrime(crime: Crime) {
        crimeDao.updateCrime(crime)
    }

    companion object {
        private var INSTANCE: CrimeRepository? = null

        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = CrimeRepository(context)
            }
        }

        fun get(): CrimeRepository {
            return INSTANCE ?: throw IllegalStateException("CrimeRepository must be initialized")
        }
    }
}