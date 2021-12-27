package be.casperverswijvelt.unifiedinternetqs.util

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ExecutorServiceSingleton {
    companion object {
        private var executorService: ExecutorService? = null
        fun getInstance(): ExecutorService {
            return (executorService ?: run {
                executorService = Executors.newCachedThreadPool()
                executorService!!
            })
        }
    }
}