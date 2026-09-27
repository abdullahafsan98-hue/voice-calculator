package com.example.data.repository

import com.example.data.db.CalculationDao
import com.example.data.model.CalculationRecord
import kotlinx.coroutines.flow.Flow

class CalculatorRepository(private val dao: CalculationDao) {

    val allHistory: Flow<List<CalculationRecord>> = dao.getAllHistory()
    val favoriteHistory: Flow<List<CalculationRecord>> = dao.getFavoriteHistory()

    suspend fun saveRecord(record: CalculationRecord): Long {
        return dao.insert(record)
    }

    suspend fun toggleFavorite(record: CalculationRecord) {
        dao.update(record.copy(isFavorite = !record.isFavorite))
    }

    suspend fun deleteRecord(id: Long) {
        dao.deleteById(id)
    }

    suspend fun clearHistory() {
        dao.clearAll()
    }
}
