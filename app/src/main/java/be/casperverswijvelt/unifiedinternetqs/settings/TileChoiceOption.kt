package be.casperverswijvelt.unifiedinternetqs.settings

interface TileChoiceOption {
    val value: String
    val stringResource: Int

    companion object {
        inline fun <reified T> getByValue(value: String): T where T : Enum<T>, T : TileChoiceOption {
            return enumValues<T>().firstOrNull { it.value == value } ?: enumValues<T>().first()
        }
    }
}