package git.pef.mendelu.cz.booknest.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import git.pef.mendelu.cz.booknest.database.entities.PendingLibraryEntity

@Dao
interface PendingLibraryDao {
    @Upsert
    suspend fun upsert(item: PendingLibraryEntity)

    @Query("SELECT * FROM pending_libraries ORDER BY createdAtMillis ASC")
    suspend fun getAll(): List<PendingLibraryEntity>

    @Query("DELETE FROM pending_libraries WHERE localId = :localId")
    suspend fun deleteById(localId: String)
}
