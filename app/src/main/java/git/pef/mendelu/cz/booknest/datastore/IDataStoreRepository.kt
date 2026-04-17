package com.mooder.app.datastore

interface IDataStoreRepository {
    suspend fun setFirstRun()
    suspend fun getFirstRun(): Boolean
}