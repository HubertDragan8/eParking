package com.example.eparking.model

/**
 * Represents the current status of a parking spot in the system.
 */
enum class ParkingStatus {
    /** The parking spot is currently available for use */
    AVAILABLE,
    
    /** The parking spot is currently occupied by a vehicle */
    OCCUPIED,
    
    /** The parking spot's status cannot be determined (e.g., sensor malfunction) */
    UNKNOWN
}

/**
 * Data class representing a single parking spot in the parking system.
 * This model is used for displaying and managing individual parking spaces
 * in the mobile app's UI.
 *
 * @property id Unique identifier for the parking spot
 * @property label Human-readable identifier (e.g., "P1", "A3")
 * @property status Current status of the parking spot
 */
data class ParkingSpot(
    val id: String,
    val label: String,
    var status: ParkingStatus = ParkingStatus.UNKNOWN
) {
    /**
     * Returns true if the parking spot is available for use.
     */
    val isAvailable: Boolean
        get() = status == ParkingStatus.AVAILABLE

    /**
     * Returns true if the parking spot is currently occupied.
     */
    val isOccupied: Boolean
        get() = status == ParkingStatus.OCCUPIED

    /**
     * Returns a color resource ID based on the parking spot's status.
     * This is useful for UI display purposes.
     */
    fun getStatusColor(): Int {
        return when (status) {
            ParkingStatus.AVAILABLE -> android.graphics.Color.GREEN
            ParkingStatus.OCCUPIED -> android.graphics.Color.RED
            ParkingStatus.UNKNOWN -> android.graphics.Color.GRAY
        }
    }
} 