package git.pef.mendelu.cz.booknest.database.repository

import git.pef.mendelu.cz.booknest.database.dao.HistoryDao
import git.pef.mendelu.cz.booknest.database.entities.HistoryEntryEntity
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

interface IHistoryRepository {
    suspend fun upsertAll(items: List<HistoryEntryEntity>)
    suspend fun upsert(item: HistoryEntryEntity)
    fun observeAll(): Flow<List<HistoryEntryEntity>>
    fun observeByType(type: String): Flow<List<HistoryEntryEntity>>
    suspend fun clearAll()
}

class HistoryRepositoryImpl @Inject constructor(
    private val dao: HistoryDao
) : IHistoryRepository {
    override suspend fun upsertAll(items: List<HistoryEntryEntity>) {
        dao.upsertAll(items)
    }

    override suspend fun upsert(item: HistoryEntryEntity) {
        dao.upsert(item)
    }

    override fun observeAll(): Flow<List<HistoryEntryEntity>> {
        return dao.observeAll()
    }

    override fun observeByType(type: String): Flow<List<HistoryEntryEntity>> {
        return dao.observeByType(type)
    }

    override suspend fun clearAll() {
        dao.clearAll()
    }
}
