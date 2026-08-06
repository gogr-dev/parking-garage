package com.parking.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a vehicle that may enter the garage.
 * A vehicle is identified by its license plate and carries a type,
 * a handicap-registration flag, and the timestamp at which it entered.
 */
public class Vehicle {

    private final String licensePlate;
    private final VehicleType type;
    private final boolean handicap;
    private LocalDateTime entryTime;

    /**
     * Creates a new vehicle.
     *
     * @param licensePlate unique plate identifier; must be non-null and non-blank
     * @param type         the vehicle type; must be non-null
     * @param handicap     true if the vehicle is registered as a handicap vehicle
     * @throws IllegalArgumentException if the plate or type is invalid
     */
    public Vehicle(String licensePlate, VehicleType type, boolean handicap) {
        if (licensePlate == null || licensePlate.trim().isEmpty()) {
            throw new IllegalArgumentException("License plate must not be null or blank");
        }
        if (type == null) {
            throw new IllegalArgumentException("Vehicle type must not be null");
        }
        this.licensePlate = licensePlate.trim().toUpperCase();
        this.type = type;
        this.handicap = handicap;
    }

    /**
     * Returns the normalized (upper-cased, trimmed) license plate.
     *
     * @return the license plate
     */
    public String getLicensePlate() {
        return licensePlate;
    }

    /**
     * Returns the type of this vehicle.
     *
     * @return the vehicle type
     */
    public VehicleType getType() {
        return type;
    }

    /**
     * Returns whether this vehicle is registered as a handicap vehicle.
     *
     * @return true if handicap-registered
     */
    public boolean isHandicap() {
        return handicap;
    }

    /**
     * Returns the timestamp at which this vehicle entered the garage,
     * or null if it has not entered.
     *
     * @return the entry time, or null
     */
    public LocalDateTime getEntryTime() {
        return entryTime;
    }

    /**
     * Records the time this vehicle entered the garage.
     *
     * @param entryTime the entry timestamp; must be non-null
     */
    public void setEntryTime(LocalDateTime entryTime) {
        if (entryTime == null) {
            throw new IllegalArgumentException("Entry time must not be null");
        }
        this.entryTime = entryTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Vehicle)) {
            return false;
        }
        Vehicle other = (Vehicle) o;
        return licensePlate.equals(other.licensePlate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(licensePlate);
    }

    @Override
    public String toString() {
        return String.format("%s[%s%s]", type.displayName(), licensePlate,
                handicap ? ", handicap" : "");
    }
}
