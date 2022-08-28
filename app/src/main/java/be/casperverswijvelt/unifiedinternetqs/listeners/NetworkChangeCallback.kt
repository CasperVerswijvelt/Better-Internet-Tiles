package be.casperverswijvelt.unifiedinternetqs.listeners

interface NetworkChangeCallback {
    fun handleChange(type: NetworkChangeType?)
}