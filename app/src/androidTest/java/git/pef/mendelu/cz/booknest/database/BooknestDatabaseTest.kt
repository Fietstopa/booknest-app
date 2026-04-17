package git.pef.mendelu.cz.booknest.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import git.pef.mendelu.cz.booknest.database.entities.AddedBookEntity
import git.pef.mendelu.cz.booknest.database.entities.HistoryEntryEntity
import git.pef.mendelu.cz.booknest.database.entities.NearbyLibraryEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class BooknestDatabaseTest {
    private lateinit var db: BooknestDatabase

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            BooknestDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun addedBookDao_upsertAndObserveAll() = runBlocking {
        val dao = db.addedBookDao()
        val item = AddedBookEntity(
            id = "book1",
            title = "Clean Code",
            authors = listOf("Robert C. Martin"),
            thumbnail = null,
            libraryId = "lib1",
            addedByUid = "user1",
            createdAtMillis = 123L
        )
        dao.upsert(item)

        val result = dao.observeAll().first()
        assertEquals(1, result.size)
        assertEquals("Clean Code", result.first().title)
    }

    @Test
    fun historyDao_upsertAndObserveByType() = runBlocking {
        val dao = db.historyDao()
        val book = HistoryEntryEntity(
            id = "book_1",
            type = "book_added",
            bookId = "book1",
            bookTitle = "Clean Code",
            libraryId = "lib1",
            libraryName = "Main Library",
            createdAtMillis = 123L
        )
        val visited = HistoryEntryEntity(
            id = "visited_lib1",
            type = "visited",
            bookId = null,
            bookTitle = null,
            libraryId = "lib1",
            libraryName = "Main Library",
            createdAtMillis = 456L
        )
        dao.upsert(book)
        dao.upsert(visited)

        val books = dao.observeByType("book_added").first()
        val visits = dao.observeByType("visited").first()
        assertEquals(1, books.size)
        assertEquals(1, visits.size)
    }

    @Test
    fun nearbyLibraryDao_observeWithinDistance() = runBlocking {
        val dao = db.nearbyLibraryDao()
        val inRange = NearbyLibraryEntity(
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
        val outRange = NearbyLibraryEntity(
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
        dao.upsertAll(listOf(inRange, outRange))

        val within = dao.observeWithinDistance(10000.0).first()
        assertEquals(1, within.size)
        assertEquals("lib1", within.first().id)
    }
}
