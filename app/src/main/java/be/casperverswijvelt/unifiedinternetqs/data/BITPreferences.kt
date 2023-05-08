package be.casperverswijvelt.unifiedinternetqs.data

import android.content.Context
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import be.casperverswijvelt.unifiedinternetqs.BuildConfig
import be.casperverswijvelt.unifiedinternetqs.R
import be.casperverswijvelt.unifiedinternetqs.tilebehaviour.TileType
import kotlinx.coroutines.flow.Flow
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

        // All caps because legacy
        private val KEY_INSTALLATION_ID = stringPreferencesKey(
            "INSTALLATION_ID"
        )
        private val KEY_SHELL_METHOD = stringPreferencesKey("shell_method")
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

    // Shell method

    val getShellMethod: kotlinx.coroutines.flow.Flow<ShellMethod> = context
        .dataStore.data.map {
            it[KEY_SHELL_METHOD]?.let {methodString ->
                ShellMethod.getByValue(methodString)
            } ?: ShellMethod.AUTO
        }

    suspend fun setShellMethod(shellMethod: ShellMethod) {
        context.dataStore.edit {
            it[KEY_SHELL_METHOD] = shellMethod.method
        }
    }

    // Require unlock (tile specific)

    private fun getRequireUnlockKey(tileType: TileType): Preferences.Key<String> {
        return stringPreferencesKey("require_unlock/${tileType.value}")
    }

    suspend fun setRequireUnlock(tileType: TileType, setting: RequireUnlockSetting) {
        context.dataStore.edit {
            it[getRequireUnlockKey(tileType)] = setting.value
        }
    }

    fun getRequireUnlock(tileType: TileType): Flow<RequireUnlockSetting> {
        return context.dataStore.data.map {
            it[getRequireUnlockKey(tileType)]?.let { value ->
                RequireUnlockSetting.getByValue(value)
            } ?: RequireUnlockSetting.FOLLOW
        }
    }
}
enum class ShellMethod(val method: String, val nameResource: Int, val descriptionResource: Int? = null) {
    ROOT(
        "root",
        R.string.root,
        R.string.root_description
    ),
    SHIZUKU(
        "shizuku",
        R.string.shizuku,
        R.string.shizuku_description
    ),
    AUTO(
        "auto",
        R.string.auto
    );
    companion object {
        infix fun getByValue(value: String): ShellMethod {
            return ShellMethod.values().firstOrNull { it.method == value } ?: AUTO
        }
    }
}
enum class RequireUnlockSetting(val value: String, val stringResource: Int) {
    FOLLOW("follow", R.string.use_default),
    YES("yes", R.string.yes),
    NO("no", R.string.no);

    companion object {
        infix fun getByValue(value: String): RequireUnlockSetting {
            return RequireUnlockSetting.values().firstOrNull { it.value == value } ?: FOLLOW
        }
    }
}