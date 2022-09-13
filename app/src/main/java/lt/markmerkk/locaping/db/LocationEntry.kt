package lt.markmerkk.locaping.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import lt.markmerkk.locaping.AppDateTimeUtils
import lt.markmerkk.locaping.entities.AppLocation
import lt.markmerkk.locaping.entities.LocationSource
import org.joda.time.DateTime

@Entity(tableName = "location")
open class LocationEntry(
    @PrimaryKey(autoGenerate = true) var id: Long? = null,
    @ColumnInfo(name = "latitude") var latitude: Double,
    @ColumnInfo(name = "longitude") var longitude: Double,
    @ColumnInfo(name = "dt_current_millis") var dtCurrentMillis: Long,
    @ColumnInfo(name = "location_source") var locationSource: String,
    @ColumnInfo(name = "extra") var extras: String,
) {

    constructor(): this(
        id = null,
        latitude = 0.0,
        longitude = 0.0,
        dtCurrentMillis = AppDateTimeUtils.defaultDateTime.millis,
        locationSource = LocationSource.UNKNOWN.name,
        extras = "",
    )

    companion object {
        fun fromAppLocation(
            appLocation: AppLocation,
            locationSource: LocationSource = LocationSource.UNKNOWN,
            extras: String = "",
        ): LocationEntry {
            return LocationEntry(
                latitude = appLocation.lat,
                longitude = appLocation.long,
                dtCurrentMillis = appLocation.dtCurrent.millis,
                locationSource = locationSource.name,
                extras = extras,
            )
        }

        fun LocationEntry.toAppLocation(): AppLocation {
            return AppLocation(
                lat = this.latitude,
                long = this.longitude,
                dtCurrent = DateTime(this.dtCurrentMillis),
                source = LocationSource.fromRaw(this.locationSource),
            )
        }
    }
}