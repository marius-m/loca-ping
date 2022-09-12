package lt.markmerkk.locaping.db

import androidx.room.Dao
import lt.markmerkk.locaping.db.LocationEntry
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface LocationEntryDao {
    @Query("SELECT * FROM location")
    suspend fun findAll(): List<LocationEntry>

    @Query("SELECT * FROM location WHERE dt_current_millis > :lastFetch")
    suspend fun findAllAfterLastFetch(lastFetch: Long): List<LocationEntry>

    @Insert
    suspend fun insert(vararg locations: LocationEntry?)

    @Update
    suspend fun update(vararg locations: LocationEntry?)

    @Delete
    suspend fun delete(vararg locations: LocationEntry?)
}