package git.pef.mendelu.cz.booknest.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import git.pef.mendelu.cz.booknest.database.entities.NearbyLibraryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NearbyLibraryDao {
    @Upsert
    suspend fun upsertAll(items: List<NearbyLibraryEntity>)

    @Query("SELECT * FROM nearby_libraries ORDER BY distanceMeters ASC")
    fun observeAll(): Flow<List<NearbyLibraryEntity>>

    @Query("SELECT * FROM nearby_libraries WHERE distanceMeters <= :maxDistanceMeters ORDER BY distanceMeters ASC")
    fun observeWithinDistance(maxDistanceMeters: Double): Flow<List<NearbyLibraryEntity>>

    @Query("SELECT * FROM nearby_libraries WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): NearbyLibraryEntity?

    @Query("DELETE FROM nearby_libraries")
    suspend fun clearAll()
}
