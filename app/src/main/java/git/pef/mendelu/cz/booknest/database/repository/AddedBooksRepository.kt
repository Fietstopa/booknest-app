package git.pef.mendelu.cz.booknest.database.repository

import git.pef.mendelu.cz.booknest.database.dao.AddedBookDao
import git.pef.mendelu.cz.booknest.database.entities.AddedBookEntity
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

interface IAddedBooksRepository {
    suspend fun upsertAll(items: List<AddedBookEntity>)
    suspend fun upsert(item: AddedBookEntity)
    fun observeAll(): Flow<List<AddedBookEntity>>
    fun observeByLibrary(libraryId: String): Flow<List<AddedBookEntity>>
    suspend fun deleteById(id: String)
    suspend fun clearAll()
}

class AddedBooksRepositoryImpl @Inject constructor(
    private val dao: AddedBookDao
) : IAddedBooksRepository {
    override suspend fun upsertAll(items: List<AddedBookEntity>) {
        dao.upsertAll(items)
    }

    override suspend fun upsert(item: AddedBookEntity) {
        dao.upsert(item)
    }

    override fun observeAll(): Flow<List<AddedBookEntity>> {
        return dao.observeAll()
    }

    override fun observeByLibrary(libraryId: String): Flow<List<AddedBookEntity>> {
        return dao.observeByLibrary(libraryId)
    }

    override suspend fun deleteById(id: String) {
        dao.deleteById(id)
    }

    override suspend fun clearAll() {
        dao.clearAll()
    }
}
