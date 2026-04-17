package git.pef.mendelu.cz.booknest.database.repository

import git.pef.mendelu.cz.booknest.database.dao.HistoryDao
import git.pef.mendelu.cz.booknest.database.entities.HistoryEntryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class HistoryRepositoryTest {
    @Test
    fun upsertAndObserveByType_filtersCorrectly() = runBlocking {
        val dao = FakeHistoryDao()
        val repo = HistoryRepositoryImpl(dao)

        repo.upsertAll(
            listOf(
                HistoryEntryEntity(
                    id = "book_1",
                    type = "book_added",
                    bookId = "b1",
                    bookTitle = "Clean Code",
                    libraryId = "lib1",
                    libraryName = "Main Library",
                    createdAtMillis = 1L
                ),
                HistoryEntryEntity(
                    id = "visited_lib1",
                    type = "visited",
                    bookId = null,
                    bookTitle = null,
                    libraryId = "lib1",
                    libraryName = "Main Library",
                    createdAtMillis = 2L
                )
            )
        )

        val books = repo.observeByType("book_added").first()
        val visits = repo.observeByType("visited").first()
        assertEquals(1, books.size)
        assertEquals(1, visits.size)
    }

    @Test
    fun clearAll_removesEntries() = runBlocking {
        val dao = FakeHistoryDao()
        val repo = HistoryRepositoryImpl(dao)
        repo.upsert(
            HistoryEntryEntity(
                id = "book_1",
                type = "book_added",
                bookId = "b1",
                bookTitle = "Clean Code",
                libraryId = "lib1",
                libraryName = "Main Library",
                createdAtMillis = 1L
            )
        )

        repo.clearAll()

        val all = repo.observeAll().first()
        assertEquals(0, all.size)
    }
}

private class FakeHistoryDao : HistoryDao {
    private val items = MutableStateFlow<List<HistoryEntryEntity>>(emptyList())

    override suspend fun upsertAll(items: List<HistoryEntryEntity>) {
        val current = this.items.value.associateBy { it.id }.toMutableMap()
        items.forEach { current[it.id] = it }
        this.items.value = current.values.toList()
    }

    override suspend fun upsert(item: HistoryEntryEntity) {
        upsertAll(listOf(item))
    }

    override fun observeAll(): Flow<List<HistoryEntryEntity>> {
        return items
    }

    override fun observeByType(type: String): Flow<List<HistoryEntryEntity>> {
        return items.map { list -> list.filter { it.type == type } }
    }

    override suspend fun clearAll() {
        items.value = emptyList()
    }
}
