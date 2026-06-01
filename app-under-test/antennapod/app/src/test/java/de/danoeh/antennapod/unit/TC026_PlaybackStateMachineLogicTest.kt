package de.danoeh.antennapod.unit

import de.danoeh.antennapod.playback.base.PlayerStatus
import org.junit.Assert.*
import org.junit.Test

/**
 * TC-026: Playback State Machine Logic
 *
 * Validates the PlayerStatus enum — state values, ordering semantics
 * (isAtLeast), ordinal positions, and valueOf round-trips.
 *
 * @author Yuanbing Wang
 */
class TC026_PlaybackStateMachineLogicTest {

    @Test
    fun allStatusValues_shouldBeUnique() {
        val values = PlayerStatus.values()
        val statusValues = values.map { it.statusValue }
        assertEquals("All status values should be unique",
            values.size, statusValues.distinct().size)
    }

    @Test
    fun playing_isAtLeast_shouldBeTrueForAllLowerStates() {
        assertTrue(PlayerStatus.PLAYING.isAtLeast(PlayerStatus.STOPPED))
        assertTrue(PlayerStatus.PLAYING.isAtLeast(PlayerStatus.PAUSED))
        assertTrue(PlayerStatus.PLAYING.isAtLeast(PlayerStatus.PREPARED))
        assertTrue(PlayerStatus.PLAYING.isAtLeast(PlayerStatus.INITIALIZED))
        assertTrue(PlayerStatus.PLAYING.isAtLeast(PlayerStatus.INITIALIZING))
        assertTrue(PlayerStatus.PLAYING.isAtLeast(PlayerStatus.SEEKING))
        assertTrue(PlayerStatus.PLAYING.isAtLeast(PlayerStatus.INDETERMINATE))
        assertTrue(PlayerStatus.PLAYING.isAtLeast(PlayerStatus.ERROR))
    }

    @Test
    fun stopped_isAtLeast_shouldBeTrueForStoppedAndBelow() {
        assertTrue(PlayerStatus.STOPPED.isAtLeast(PlayerStatus.STOPPED))
        assertTrue(PlayerStatus.STOPPED.isAtLeast(PlayerStatus.PREPARED))
        assertTrue(PlayerStatus.STOPPED.isAtLeast(PlayerStatus.INITIALIZED))
        assertTrue(PlayerStatus.STOPPED.isAtLeast(PlayerStatus.ERROR))
        assertFalse(PlayerStatus.STOPPED.isAtLeast(PlayerStatus.PLAYING))
    }

    @Test
    fun paused_isAtLeast_shouldBeTrueForPausedAndBelow() {
        assertTrue(PlayerStatus.PAUSED.isAtLeast(PlayerStatus.PAUSED))
        assertTrue(PlayerStatus.PAUSED.isAtLeast(PlayerStatus.PREPARED))
        assertTrue(PlayerStatus.PAUSED.isAtLeast(PlayerStatus.ERROR))
        assertFalse(PlayerStatus.PAUSED.isAtLeast(PlayerStatus.PLAYING))
        assertFalse(PlayerStatus.PAUSED.isAtLeast(PlayerStatus.STOPPED))
    }

    @Test
    fun initialized_isAtLeast_shouldBeAboveErrorAndIndeterminate() {
        assertTrue(PlayerStatus.INITIALIZED.isAtLeast(PlayerStatus.INITIALIZED))
        assertTrue(PlayerStatus.INITIALIZED.isAtLeast(PlayerStatus.INITIALIZING))
        assertTrue(PlayerStatus.INITIALIZED.isAtLeast(PlayerStatus.ERROR))
        assertTrue(PlayerStatus.INITIALIZED.isAtLeast(PlayerStatus.INDETERMINATE))
        assertFalse(PlayerStatus.INITIALIZED.isAtLeast(PlayerStatus.PREPARING))
        assertFalse(PlayerStatus.INITIALIZED.isAtLeast(PlayerStatus.PREPARED))
    }

    @Test
    fun error_isAtLeast_shouldOnlySupersedeIndeterminate() {
        assertTrue(PlayerStatus.ERROR.isAtLeast(PlayerStatus.ERROR))
        assertTrue(PlayerStatus.ERROR.isAtLeast(PlayerStatus.INDETERMINATE))
        assertFalse(PlayerStatus.ERROR.isAtLeast(PlayerStatus.INITIALIZING))
        assertFalse(PlayerStatus.ERROR.isAtLeast(PlayerStatus.PLAYING))
    }

    @Test
    fun isAtLeast_withNull_shouldReturnTrue() {
        assertTrue(PlayerStatus.PLAYING.isAtLeast(null))
        assertTrue(PlayerStatus.ERROR.isAtLeast(null))
        assertTrue(PlayerStatus.STOPPED.isAtLeast(null))
    }

    @Test
    fun valueOf_shouldRoundTrip() {
        for (status in PlayerStatus.values()) {
            assertEquals(status, PlayerStatus.valueOf(status.name))
        }
    }

    @Test
    fun playerStatus_shouldHaveTenStates() {
        assertEquals(10, PlayerStatus.values().size)
    }

    @Test
    fun playing_shouldHaveHighestValue() {
        val maxVal = PlayerStatus.values().maxOf { it.statusValue }
        assertEquals(PlayerStatus.PLAYING.statusValue, maxVal)
    }

    @Test
    fun error_shouldHaveLowestValue() {
        val minVal = PlayerStatus.values().minOf { it.statusValue }
        assertEquals(PlayerStatus.ERROR.statusValue, minVal)
    }
}
