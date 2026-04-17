package git.pef.mendelu.cz.booknest

import android.app.Application
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.HiltAndroidApp
import git.pef.mendelu.cz.booknest.sync.OfflineSyncManager

@HiltAndroidApp
class BooknestApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val syncManager = EntryPointAccessors
            .fromApplication(this, SyncManagerEntryPoint::class.java)
            .syncManager()
        syncManager.start()
    }
}

@dagger.hilt.EntryPoint
@dagger.hilt.InstallIn(dagger.hilt.components.SingletonComponent::class)
interface SyncManagerEntryPoint {
    fun syncManager(): OfflineSyncManager
}
