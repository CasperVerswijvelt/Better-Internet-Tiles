package be.casperverswijvelt.unifiedinternetqs.data

import android.content.Context
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import be.casperverswijvelt.unifiedinternetqs.BuildConfig
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

class BITPreferences(private val context: Context) {

    companion object {
        private const val PREFERENCES_NAME = "preferences"
        private val Context.dataStore by preferencesDataStore(
            name = PREFERENCES_NAME,
            produceMigrations = {
                listOf(SharedPreferencesMigration(
                    it,
                    "${BuildConfig.APPLICATION_ID}_preferences"
                ))
            }
        )

        private val KEY_REQUIRE_UNLOCK = booleanPreferencesKey("require_unlock")
        private val KEY_LAST_CONNECTED_SSID = stringPreferencesKey(
            "last_connected_wifi"
        )
        // All caps because legacy
        private val KEY_INSTALLATION_ID = stringPreferencesKey(
            "INSTALLATION_ID"
        )
    }

    fun loadPreferences() {
        runBlocking {
            context.dataStore.data.first()
        }
    }

    // Require unlock

    val getRequireUnlock: kotlinx.coroutines.flow.Flow<Boolean> = context
        .dataStore.data.map {
            it[KEY_REQUIRE_UNLOCK] ?: true
        }

    suspend fun setRequireUnlock(requireUnlock: Boolean) {
        context.dataStore.edit {
            it[KEY_REQUIRE_UNLOCK] = requireUnlock
        }
    }

    // Last Connected Wi-Fi SSID

    val getLastConnectedSSID: kotlinx.coroutines.flow.Flow<String?> = context
        .dataStore.data.map {
            it[KEY_LAST_CONNECTED_SSID]
        }

    suspend fun setLastConnectedSSID(ssid: String?) {
        context.dataStore.edit {
            ssid?.let {nonNullSsid ->
                it[KEY_LAST_CONNECTED_SSID] = nonNullSsid
            } ?: run {
                it.remove(KEY_LAST_CONNECTED_SSID)
            }
        }
    }

    // Installation ID

    val getInstallationId: kotlinx.coroutines.flow.Flow<String?> = context
        .dataStore.data.map {
            it[KEY_INSTALLATION_ID]
        }

    suspend fun setInstallationId(id: String) {
        context.dataStore.edit {
            it[KEY_INSTALLATION_ID] = id
        }
    }
}