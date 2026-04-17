package git.pef.mendelu.cz.booknest.di


import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import git.pef.mendelu.cz.booknest.communication.BookRemoteRepositoryImpl
import git.pef.mendelu.cz.booknest.communication.IBooksRemoteRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RemoteRepositoryModule {

    @Provides
    @Singleton
    fun provideBooksRemoteRepository(repository: BookRemoteRepositoryImpl): IBooksRemoteRepository {
        return repository
    }
}
