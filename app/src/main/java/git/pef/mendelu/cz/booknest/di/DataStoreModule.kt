package git.pef.mendelu.cz.booknest.di

import android.content.Context
import com.mooder.app.datastore.DataStoreRepositoryImpl
import com.mooder.app.datastore.IDataStoreRepository

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun provideDataStoreRepository(@ApplicationContext appContext: Context): IDataStoreRepository
            = DataStoreRepositoryImpl(appContext)
}
