package git.pef.mendelu.cz.booknest.database.repository

import git.pef.mendelu.cz.booknest.database.dao.NearbyLibraryDao
import git.pef.mendelu.cz.booknest.database.entities.NearbyLibraryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class NearbyLibrariesRepositoryTest {
    @Test
    fun observeWithinDistance_filtersItems() = runBlocking {
        val dao = FakeNearbyLibraryDao()
        val repo = NearbyLibrariesRepositoryImpl(dao)
        repo.upsertAll(
            listOf(
                NearbyLibraryEntity(
                    id = "lib1",
                    name = "Near Library",
                    latitude = 49.0,
                    longitude = 16.0,
                    size = "M",
                    imageUrl = null,
                    imageLocalPath = null,
                    createdByName = null,
                    distanceMeters = 500.0,
                    updatedAtMillis = 1L
                ),
                NearbyLibraryEntity(
                    id = "lib2",
                    name = "Far Library",
                    latitude = 49.0,
                    longitude = 16.0,
                    size = "M",
                    imageUrl = null,
                    imageLocalPath = null,
                    createdByName = null,
                    distanceMeters = 15000.0,
                    updatedAtMillis = 1L
                )
            )
        )

        val result = repo.observeWithinDistance(10000.0).first()
        assertEquals(1, result.size)
        assertEquals("lib1", result.first().id)
    }

    @Test
    fun getById_returnsItem() = runBlocking {
        val dao = FakeNearbyLibraryDao()
        val repo = NearbyLibrariesRepositoryImpl(dao)
        val item = NearbyLibraryEntity(
            id = "lib1",
            name = "Near Library",
            latitude = 49.0,
            longitude = 16.0,
            size = "M",
            imageUrl = null,
            imageLocalPath = null,
            createdByName = null,
            distanceMeters = 500.0,
            updatedAtMillis = 1L
        )
        repo.upsertAll(listOf(item))

        val result = repo.getById("lib1")
        assertEquals("Near Library", result?.name)
    }
}

private class FakeNearbyLibraryDao : NearbyLibraryDao {
    private val items = MutableStateFlow<List<NearbyLibraryEntity>>(emptyList())

    override suspend fun upsertAll(items: List<NearbyLibraryEntity>) {
        val current = this.items.value.associateBy { it.id }.toMutableMap()
        items.forEach { current[it.id] = it }
        this.items.value = current.values.toList()
    }

    override fun observeAll(): Flow<List<NearbyLibraryEntity>> {
        return items
    }

    override fun observeWithinDistance(maxDistanceMeters: Double): Flow<List<NearbyLibraryEntity>> {
        return items.map { list -> list.filter { it.distanceMeters <= maxDistanceMeters } }
    }

    override suspend fun getById(id: String): NearbyLibraryEntity? {
        return items.value.firstOrNull { it.id == id }
    }

    override suspend fun clearAll() {
        items.value = emptyList()
    }
}
