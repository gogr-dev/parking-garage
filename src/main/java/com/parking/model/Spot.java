package com.parking.model;

/**
 * Represents a single parking spot on a level.
 * A spot has a fixed size, an optional handicap designation, and tracks
 * the vehicle currently occupying it (if any).
 */
public class Spot {

    private final String spotId;
    private final SpotSize size;
    private final boolean handicapOnly;
    private Vehicle occupant;

    /**
     * Creates a new parking spot.
     *
     * @param spotId       unique identifier within the garage, e.g. "L1-S04"
     * @param size         the physical size of the spot; must be non-null
     * @param handicapOnly true if the spot is reserved for handicap vehicles;
     *                     handicap spots must be MEDIUM or LARGE
     * @throws IllegalArgumentException if arguments are invalid
     */
    public Spot(String spotId, SpotSize size, boolean handicapOnly) {
        if (spotId == null || spotId.trim().isEmpty()) {
            throw new IllegalArgumentException("Spot id must not be null or blank");
        }
        if (size == null) {
            throw new IllegalArgumentException("Spot size must not be null");
        }
        if (handicapOnly && size == SpotSize.SMALL) {
            throw new IllegalArgumentException("Handicap spots must be MEDIUM or LARGE");
        }
        this.spotId = spotId;
        this.size = size;
        this.handicapOnly = handicapOnly;
    }

    /**
     * Returns the unique identifier of this spot.
     *
     * @return the spot id
     */
    public String getSpotId() {
        return spotId;
    }

    /**
     * Returns the physical size of this spot.
     *
     * @return the spot size
     */
    public SpotSize getSize() {
        return size;
    }

    /**
     * Returns whether this spot is reserved for handicap vehicles.
     *
     * @return true if handicap-only
     */
    public boolean isHandicapOnly() {
        return handicapOnly;
    }

    /**
     * Returns whether this spot is currently occupied.
     *
     * @return true if a vehicle is parked here
     */
    public boolean isOccupied() {
        return occupant != null;
    }

    /**
     * Returns the vehicle currently occupying this spot, or null if vacant.
     *
     * @return the occupant, or null
     */
    public Vehicle getOccupant() {
        return occupant;
    }

    /**
     * Parks the given vehicle in this spot.
     *
     * @param vehicle the vehicle to park; must be non-null
     * @throws IllegalStateException    if the spot is already occupied
     * @throws IllegalArgumentException if the vehicle is null
     */
    public void occupy(Vehicle vehicle) {
        if (vehicle == null) {
            throw new IllegalArgumentException("Vehicle must not be null");
        }
        if (isOccupied()) {
            throw new IllegalStateException("Spot " + spotId + " is already occupied");
        }
        this.occupant = vehicle;
    }

    /**
     * Vacates this spot and returns the vehicle that was parked here.
     *
     * @return the vehicle that just left
     * @throws IllegalStateException if the spot is already vacant
     */
    public Vehicle vacate() {
        if (!isOccupied()) {
            throw new IllegalStateException("Spot " + spotId + " is already vacant");
        }
        Vehicle leaving = occupant;
        this.occupant = null;
        return leaving;
    }

    @Override
    public String toString() {
        return String.format("Spot[%s, %s%s, %s]", spotId, size.displayName(),
                handicapOnly ? ", handicap" : "",
                isOccupied() ? "occupied by " + occupant.getLicensePlate() : "vacant");
    }
}
