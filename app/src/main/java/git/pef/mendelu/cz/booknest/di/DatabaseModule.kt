package git.pef.mendelu.cz.booknest.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import git.pef.mendelu.cz.booknest.database.BooknestDatabase
import git.pef.mendelu.cz.booknest.database.dao.AddedBookDao
import git.pef.mendelu.cz.booknest.database.dao.HistoryDao
import git.pef.mendelu.cz.booknest.database.dao.NearbyLibraryDao
import git.pef.mendelu.cz.booknest.database.dao.PendingBookDao
import git.pef.mendelu.cz.booknest.database.dao.PendingLibraryDao
import git.pef.mendelu.cz.booknest.database.repository.AddedBooksRepositoryImpl
import git.pef.mendelu.cz.booknest.database.repository.HistoryRepositoryImpl
import git.pef.mendelu.cz.booknest.database.repository.IAddedBooksRepository
import git.pef.mendelu.cz.booknest.database.repository.IHistoryRepository
import git.pef.mendelu.cz.booknest.database.repository.INearbyLibrariesRepository
import git.pef.mendelu.cz.booknest.database.repository.NearbyLibrariesRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): BooknestDatabase {
        return BooknestDatabase.getDatabase(context)
    }

    @Provides
    fun provideNearbyLibraryDao(database: BooknestDatabase): NearbyLibraryDao {
        return database.nearbyLibraryDao()
    }

    @Provides
    fun provideAddedBookDao(database: BooknestDatabase): AddedBookDao {
        return database.addedBookDao()
    }

    @Provides
    fun provideHistoryDao(database: BooknestDatabase): HistoryDao {
        return database.historyDao()
    }

    @Provides
    fun providePendingLibraryDao(database: BooknestDatabase): PendingLibraryDao {
        return database.pendingLibraryDao()
    }

    @Provides
    fun providePendingBookDao(database: BooknestDatabase): PendingBookDao {
        return database.pendingBookDao()
    }

    @Provides
    fun provideNearbyLibrariesRepository(
        dao: NearbyLibraryDao
    ): INearbyLibrariesRepository {
        return NearbyLibrariesRepositoryImpl(dao)
    }

    @Provides
    fun provideAddedBooksRepository(
        dao: AddedBookDao
    ): IAddedBooksRepository {
        return AddedBooksRepositoryImpl(dao)
    }

    @Provides
    fun provideHistoryRepository(
        dao: HistoryDao
    ): IHistoryRepository {
        return HistoryRepositoryImpl(dao)
    }
}
