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
import be.casperverswijvelt.unifiedinternetqs.tile_options.FollowSetting
import be.casperverswijvelt.unifiedinternetqs.tile_options.WifiSSIDVisibilityOption
import be.casperverswijvelt.unifiedinternetqs.tilebehaviour.TileType
import be.casperverswijvelt.unifiedinternetqs.util.AlertDialogData
import be.casperverswijvelt.unifiedinternetqs.util.ShizukuUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import com.topjohnwu.superuser.Shell

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

        // All caps because legacy
        private val KEY_INSTALLATION_ID = stringPreferencesKey(
            "INSTALLATION_ID"
        )

        private val KEY_REQUIRE_UNLOCK = booleanPreferencesKey("require_unlock")
        private val KEY_SHELL_METHOD = stringPreferencesKey("shell_method")

        private val KEY_SSID_VISIBILITY = stringPreferencesKey("SSID_VISIBILITY")
    }

    fun loadPreferences() {
        runBlocking {
            context.dataStore.data.first()
        }
    }

    // Require unlock

    val getRequireUnlock: Flow<Boolean> = context
        .dataStore.data.map {
            it[KEY_REQUIRE_UNLOCK] ?: true
        }

    suspend fun setRequireUnlock(requireUnlock: Boolean) {
        context.dataStore.edit {
            it[KEY_REQUIRE_UNLOCK] = requireUnlock
        }
    }

    // Installation ID

    val getInstallationId: Flow<String?> = context
        .dataStore.data.map {
            it[KEY_INSTALLATION_ID]
        }

    suspend fun setInstallationId(id: String) {
        context.dataStore.edit {
            it[KEY_INSTALLATION_ID] = id
        }
    }

    // Shell method

    val getShellMethod: Flow<ShellMethod> = context
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

    // SSID method

    val getSSIDVisibility: Flow<WifiSSIDVisibilityOption> = context
        .dataStore.data.map {
            it[KEY_SSID_VISIBILITY]?.let { option ->
                TileChoiceOption.getByValue(option)
            } ?: WifiSSIDVisibilityOption.HIDDEN_DURING_RECORDING
        }

    suspend fun setSSIDVisibility(option: WifiSSIDVisibilityOption) {
        context.dataStore.edit {
            it[KEY_SSID_VISIBILITY] = option.value
        }
    }

    // Require unlock (tile specific)

    private fun getRequireUnlockKey(tileType: TileType): Preferences.Key<String> {
        return stringPreferencesKey("require_unlock/${tileType.value}")
    }

    suspend fun setRequireUnlock(tileType: TileType, setting: TileChoiceOption) {
        context.dataStore.edit {
            it[getRequireUnlockKey(tileType)] = setting.value
        }
    }

    fun getRequireUnlock(tileType: TileType): Flow<FollowSetting> {
        return context.dataStore.data.map {
            it[getRequireUnlockKey(tileType)]?.let { value ->
                TileChoiceOption.getByValue(value)
            } ?: FollowSetting.FOLLOW
        }
    }
}
enum class ShellMethod(
    val method: String,
    val nameResource: Int,
    val descriptionResource: Int? = null,
    val isGranted: () -> Boolean = { false },
    val alertDialog: AlertDialogData? = null
) {
    ROOT(
        method = "root",
        nameResource = R.string.root,
        descriptionResource = R.string.root_description,
        isGranted = { Shell.isAppGrantedRoot() == true },
        alertDialog = AlertDialogData(
            iconResource = R.drawable.baseline_block_24,
            titleResource = R.string.allow_root_access,
            messageResource = R.string.allow_root_access_description,
            positiveButtonResource = R.string.ok
        )
    ),
    SHIZUKU(
        method = "shizuku",
        nameResource = R.string.shizuku,
        descriptionResource = R.string.shizuku_description,
        isGranted = { ShizukuUtil.hasShizukuPermission() },
        alertDialog = AlertDialogData(
            iconResource = R.drawable.baseline_block_24,
            titleResource = R.string.allow_shizuku_access,
            messageResource = R.string.allow_shizuku_access_description,
            positiveButtonResource = R.string.ok
        )
    ),
    AUTO(
        method = "auto",
        nameResource = R.string.auto,
        isGranted = {
            Shell.isAppGrantedRoot() == true ||
            ShizukuUtil.hasShizukuPermission()
        }
    );
    companion object {
        infix fun getByValue(value: String): ShellMethod {
            return ShellMethod.values().firstOrNull { it.method == value } ?: AUTO
        }
    }
}

interface TileChoiceOption {
    val value: String
    val stringResource: Int

    companion object {
        inline fun <reified T> getByValue(value: String): T where T : Enum<T>, T : TileChoiceOption {
            return enumValues<T>().firstOrNull { it.value == value } ?: enumValues<T>().first()
        }
    }
}
