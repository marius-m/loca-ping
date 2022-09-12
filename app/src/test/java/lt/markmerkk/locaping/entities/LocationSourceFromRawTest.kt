package lt.markmerkk.locaping.entities

import org.assertj.core.api.Assertions
import org.junit.Test

class LocationSourceFromRawTest {
    @Test
    fun foreground() {
        // Assemble
        val input = LocationSource.FOREGROUND_SERVICE.name

        // Act
        val result = LocationSource.fromRaw(rawSource = input)

        // Assert
        Assertions.assertThat(result).isEqualTo(LocationSource.FOREGROUND_SERVICE)
    }

    @Test
    fun foreground_lowercase() {
        // Assemble
        val input = LocationSource.FOREGROUND_SERVICE.name.lowercase()

        // Act
        val result = LocationSource.fromRaw(rawSource = input)

        // Assert
        Assertions.assertThat(result).isEqualTo(LocationSource.FOREGROUND_SERVICE)
    }

    @Test
    fun push() {
        // Assemble
        val input = LocationSource.PUSH_NOTIFICATION_WORKER.name

        // Act
        val result = LocationSource.fromRaw(rawSource = input)

        // Assert
        Assertions.assertThat(result).isEqualTo(LocationSource.PUSH_NOTIFICATION_WORKER)
    }

    @Test
    fun push_lowercase() {
        // Assemble
        val input = LocationSource.PUSH_NOTIFICATION_WORKER.name.lowercase()

        // Act
        val result = LocationSource.fromRaw(rawSource = input)

        // Assert
        Assertions.assertThat(result).isEqualTo(LocationSource.PUSH_NOTIFICATION_WORKER)
    }

    @Test
    fun unknown() {
        // Assemble
        val input = "invalid"

        // Act
        val result = LocationSource.fromRaw(rawSource = input)

        // Assert
        Assertions.assertThat(result).isEqualTo(LocationSource.UNKNOWN)
    }
}