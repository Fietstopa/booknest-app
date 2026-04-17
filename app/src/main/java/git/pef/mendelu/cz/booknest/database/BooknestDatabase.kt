package git.pef.mendelu.cz.booknest.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import git.pef.mendelu.cz.booknest.database.dao.AddedBookDao
import git.pef.mendelu.cz.booknest.database.dao.HistoryDao
import git.pef.mendelu.cz.booknest.database.dao.NearbyLibraryDao
import git.pef.mendelu.cz.booknest.database.dao.PendingBookDao
import git.pef.mendelu.cz.booknest.database.dao.PendingLibraryDao
import git.pef.mendelu.cz.booknest.database.entities.AddedBookEntity
import git.pef.mendelu.cz.booknest.database.entities.HistoryEntryEntity
import git.pef.mendelu.cz.booknest.database.entities.NearbyLibraryEntity
import git.pef.mendelu.cz.booknest.database.entities.PendingBookEntity
import git.pef.mendelu.cz.booknest.database.entities.PendingLibraryEntity

@Database(
    entities = [
        NearbyLibraryEntity::class,
        AddedBookEntity::class,
        HistoryEntryEntity::class,
        PendingLibraryEntity::class,
        PendingBookEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class BooknestDatabase : RoomDatabase() {
    abstract fun nearbyLibraryDao(): NearbyLibraryDao
    abstract fun addedBookDao(): AddedBookDao
    abstract fun historyDao(): HistoryDao
    abstract fun pendingLibraryDao(): PendingLibraryDao
    abstract fun pendingBookDao(): PendingBookDao

    companion object {
        @Volatile
        private var INSTANCE: BooknestDatabase? = null

        fun getDatabase(context: Context): BooknestDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BooknestDatabase::class.java,
                    "booknest.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
