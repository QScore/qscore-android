package com.berd.qscore.features.geofence

import android.content.Context
import androidx.work.*
import com.berd.qscore.utils.location.LocationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.concurrent.TimeUnit

class UpdateLocationWorker(context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                Timber.d(">>Fetching location at time: " + System.currentTimeMillis())
                LocationHelper.fetchCurrentLocation()
                Result.success()
            } catch (e: Exception) {
                Timber.d("Unable to get location  ${e.message}")
                Result.failure()
            }
        }
    }

    companion object {
        private const val TAG = "UpdateLocationWorker"
        private const val DEFAULT_MIN_INTERVAL = 15L

        fun schedule(context: Context) {
            val worker = PeriodicWorkRequestBuilder<UpdateLocationWorker>(DEFAULT_MIN_INTERVAL, TimeUnit.MINUTES).addTag(TAG).build()
            val workManager = WorkManager.getInstance(context.applicationContext)
            workManager.enqueueUniquePeriodicWork(TAG, ExistingPeriodicWorkPolicy.REPLACE, worker)
            workManager.getWorkInfosByTagLiveData(TAG).observeForever {
                Timber.d(">>WORK STAUS changed: " + it)
            }
        }
    }
}
