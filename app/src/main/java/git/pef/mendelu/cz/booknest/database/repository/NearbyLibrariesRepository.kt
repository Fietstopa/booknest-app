package git.pef.mendelu.cz.booknest.database.repository

import git.pef.mendelu.cz.booknest.database.dao.NearbyLibraryDao
import git.pef.mendelu.cz.booknest.database.entities.NearbyLibraryEntity
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

interface INearbyLibrariesRepository {
    suspend fun upsertAll(items: List<NearbyLibraryEntity>)
    fun observeAll(): Flow<List<NearbyLibraryEntity>>
    fun observeWithinDistance(maxDistanceMeters: Double): Flow<List<NearbyLibraryEntity>>
    suspend fun getById(id: String): NearbyLibraryEntity?
    suspend fun clearAll()
}

class NearbyLibrariesRepositoryImpl @Inject constructor(
    private val dao: NearbyLibraryDao
) : INearbyLibrariesRepository {
    override suspend fun upsertAll(items: List<NearbyLibraryEntity>) {
        dao.upsertAll(items)
    }

    override fun observeAll(): Flow<List<NearbyLibraryEntity>> {
        return dao.observeAll()
    }

    override fun observeWithinDistance(maxDistanceMeters: Double): Flow<List<NearbyLibraryEntity>> {
        return dao.observeWithinDistance(maxDistanceMeters)
    }

    override suspend fun getById(id: String): NearbyLibraryEntity? {
        return dao.getById(id)
    }

    override suspend fun clearAll() {
        dao.clearAll()
    }
}
