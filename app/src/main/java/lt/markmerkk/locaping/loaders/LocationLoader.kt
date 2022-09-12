package lt.markmerkk.locaping.loaders

import androidx.lifecycle.LifecycleCoroutineScope
import kotlinx.coroutines.launch
import lt.markmerkk.locaping.Tags
import lt.markmerkk.locaping.db.AppDatabase
import lt.markmerkk.locaping.db.LocationEntry
import lt.markmerkk.locaping.entities.AppLocation
import lt.markmerkk.locaping.entities.LocationSource
import lt.markmerkk.locaping.network.DataResult
import lt.markmerkk.locaping.repositories.HomeRepository
import org.joda.time.DateTime
import timber.log.Timber

class LocationLoader(
    private val appDatabase: AppDatabase,
    private val homeRepository: HomeRepository,
    private val lifecycleScope: LifecycleCoroutineScope,
) {
    fun postPing(
        currentLocation: AppLocation,
        dtCurrent: DateTime,
    ) {
        lifecycleScope.launch {
            val result = homeRepository.postPingDetail(
                coordLat = currentLocation.lat,
                coordLong = currentLocation.long,
                dtCurrent = dtCurrent,
                locationSource = currentLocation.source,
            )
            when (result) {
                is DataResult.Error -> {
                    Timber.tag(Tags.LOCATION)
                        .w(result.throwable, "postPingError(error: %s)", result.throwable.toString())
                }
                is DataResult.Success -> {
                    Timber.tag(Tags.LOCATION)
                        .d("postPingSuccess(content: %s)", result.result)
                }
            }
        }
    }

    fun postPingDeferrable(
        currentLocation: AppLocation,
    ) {
        lifecycleScope.launch {
            appDatabase.locationDao().insert(
                LocationEntry.fromAppLocation(
                    appLocation = currentLocation,
                    locationSource = currentLocation.source,
                ),
            )
        }
    }
}
