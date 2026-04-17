package git.pef.mendelu.cz.booknest.database.repository

import git.pef.mendelu.cz.booknest.database.dao.AddedBookDao
import git.pef.mendelu.cz.booknest.database.entities.AddedBookEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class AddedBooksRepositoryTest {
    @Test
    fun upsertAndObserveAll_returnsLatestItems() = runBlocking {
        val dao = FakeAddedBookDao()
        val repo = AddedBooksRepositoryImpl(dao)
        val item = AddedBookEntity(
            id = "book1",
            title = "Clean Code",
            authors = listOf("Robert C. Martin"),
            thumbnail = null,
            libraryId = "lib1",
            addedByUid = "user1",
            createdAtMillis = 10L
        )

        repo.upsert(item)

        val result = repo.observeAll().first()
        assertEquals(1, result.size)
        assertEquals("book1", result.first().id)
    }

    @Test
    fun observeByLibrary_filtersItems() = runBlocking {
        val dao = FakeAddedBookDao()
        val repo = AddedBooksRepositoryImpl(dao)
        repo.upsertAll(
            listOf(
                AddedBookEntity(
                    id = "book1",
                    title = "Clean Code",
                    authors = emptyList(),
                    thumbnail = null,
                    libraryId = "lib1",
                    addedByUid = "user1",
                    createdAtMillis = 1L
                ),
                AddedBookEntity(
                    id = "book2",
                    title = "Refactoring",
                    authors = emptyList(),
                    thumbnail = null,
                    libraryId = "lib2",
                    addedByUid = "user1",
                    createdAtMillis = 2L
                )
            )
        )

        val result = repo.observeByLibrary("lib1").first()
        assertEquals(1, result.size)
        assertEquals("book1", result.first().id)
    }

    @Test
    fun clearAll_removesItems() = runBlocking {
        val dao = FakeAddedBookDao()
        val repo = AddedBooksRepositoryImpl(dao)
        repo.upsert(
            AddedBookEntity(
                id = "book1",
                title = "Clean Code",
                authors = emptyList(),
                thumbnail = null,
                libraryId = "lib1",
                addedByUid = "user1",
                createdAtMillis = 1L
            )
        )

        repo.clearAll()

        val result = repo.observeAll().first()
        assertEquals(0, result.size)
    }
}

private class FakeAddedBookDao : AddedBookDao {
    private val items = MutableStateFlow<List<AddedBookEntity>>(emptyList())

    override suspend fun upsertAll(items: List<AddedBookEntity>) {
        val current = this.items.value.associateBy { it.id }.toMutableMap()
        items.forEach { current[it.id] = it }
        this.items.value = current.values.toList()
    }

    override suspend fun upsert(item: AddedBookEntity) {
        upsertAll(listOf(item))
    }

    override fun observeAll(): Flow<List<AddedBookEntity>> {
        return items
    }

    override fun observeByLibrary(libraryId: String): Flow<List<AddedBookEntity>> {
        return items.map { list -> list.filter { it.libraryId == libraryId } }
    }

    override suspend fun deleteById(id: String) {
        items.value = items.value.filterNot { it.id == id }
    }

    override suspend fun clearAll() {
        items.value = emptyList()
    }
}
