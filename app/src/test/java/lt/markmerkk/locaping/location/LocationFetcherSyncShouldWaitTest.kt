package lt.markmerkk.locaping.location

import lt.markmerkk.locaping.AppTimeProviderTest
import org.assertj.core.api.Assertions
import org.junit.Test

class LocationFetcherSyncShouldWaitTest {

    private val timeProvider = AppTimeProviderTest()

    @Test
    fun noTimeout_noObject() {
        // Assemble
        val now = timeProvider.now()

        // Act
        val result = LocationFetcherSync.shouldWaitForObject(
            dtNow = now,
            dtTimeout = now.plusSeconds(5),
            targetObject = null,
        )

        // Assert
        Assertions.assertThat(result).isTrue()
    }

    @Test
    fun timeOutHit() {
        // Assemble
        val now = timeProvider.now()

        // Act
        val result = LocationFetcherSync.shouldWaitForObject(
            dtNow = now.plusSeconds(10),
            dtTimeout = now.plusSeconds(5),
            targetObject = null,
        )

        // Assert
        Assertions.assertThat(result).isFalse()
    }

    @Test
    fun hasObject() {
        // Assemble
        val now = timeProvider.now()

        // Act
        val result = LocationFetcherSync.shouldWaitForObject(
            dtNow = now,
            dtTimeout = now.plusSeconds(5),
            targetObject = "objectRef",
        )

        // Assert
        Assertions.assertThat(result).isFalse()
    }
}
