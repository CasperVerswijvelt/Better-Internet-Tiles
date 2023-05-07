package be.casperverswijvelt.unifiedinternetqs.tilebehaviour

enum class TileType(val value: Int) {
    WiFi(1),
    MobileData(2),
    Internet(3),
    AirplaneMode(4),
    NFC(5),
    Bluetooth(6)
}