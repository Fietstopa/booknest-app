package git.pef.mendelu.cz.booknest.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import git.pef.mendelu.cz.booknest.database.entities.AddedBookEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AddedBookDao {
    @Upsert
    suspend fun upsertAll(items: List<AddedBookEntity>)

    @Upsert
    suspend fun upsert(item: AddedBookEntity)

    @Query("SELECT * FROM added_books ORDER BY createdAtMillis DESC")
    fun observeAll(): Flow<List<AddedBookEntity>>

    @Query("SELECT * FROM added_books WHERE libraryId = :libraryId ORDER BY createdAtMillis DESC")
    fun observeByLibrary(libraryId: String): Flow<List<AddedBookEntity>>

    @Query("DELETE FROM added_books WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM added_books")
    suspend fun clearAll()
}
