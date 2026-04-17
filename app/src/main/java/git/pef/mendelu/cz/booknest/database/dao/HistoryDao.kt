package git.pef.mendelu.cz.booknest.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import git.pef.mendelu.cz.booknest.database.entities.HistoryEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Upsert
    suspend fun upsertAll(items: List<HistoryEntryEntity>)

    @Upsert
    suspend fun upsert(item: HistoryEntryEntity)

    @Query("SELECT * FROM history_entries ORDER BY createdAtMillis DESC")
    fun observeAll(): Flow<List<HistoryEntryEntity>>

    @Query("SELECT * FROM history_entries WHERE type = :type ORDER BY createdAtMillis DESC")
    fun observeByType(type: String): Flow<List<HistoryEntryEntity>>

    @Query("DELETE FROM history_entries")
    suspend fun clearAll()
}
