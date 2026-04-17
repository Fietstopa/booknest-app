package git.pef.mendelu.cz.booknest.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import git.pef.mendelu.cz.booknest.database.entities.PendingBookEntity

@Dao
interface PendingBookDao {
    @Upsert
    suspend fun upsert(item: PendingBookEntity)

    @Query("SELECT * FROM pending_books ORDER BY createdAtMillis ASC")
    suspend fun getAll(): List<PendingBookEntity>

    @Query("DELETE FROM pending_books WHERE localId = :localId")
    suspend fun deleteById(localId: String)
}
