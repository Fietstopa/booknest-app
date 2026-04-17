package git.pef.mendelu.cz.booknest.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val DATASTORE_NAME = "booknest_prefs"

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = DATASTORE_NAME)

object UserPrefs {
    val firstRunKey = booleanPreferencesKey("first_run")

    fun isFirstRun(context: Context): Flow<Boolean> {
        return context.dataStore.data.map { prefs ->
            prefs[firstRunKey] ?: true
        }
    }

    suspend fun setFirstRunComplete(context: Context) {
        context.dataStore.edit { prefs ->
            prefs[firstRunKey] = false
        }
    }
}
