package com.parking.model;

/**
 * Enumerates the types of vehicles the garage supports.
 * Each type carries its base hourly parking rate in dollars.
 */
public enum VehicleType {

    /** Two-wheeled vehicle; fits in any spot size. */
    MOTORCYCLE(1.0),

    /** Standard passenger car; fits in MEDIUM or LARGE spots. */
    CAR(2.0),

    /** Large vehicle; fits in LARGE spots only. */
    TRUCK(3.0);

    private final double hourlyRate;

    VehicleType(double hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    /**
     * Returns the base hourly rate for this vehicle type, before discounts.
     *
     * @return dollars per hour
     */
    public double getHourlyRate() {
        return hourlyRate;
    }

    /**
     * Returns a human-readable label for this vehicle type.
     *
     * @return capitalized display name, e.g. "Car"
     */
    public String displayName() {
        String lower = name().toLowerCase();
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }

    /**
     * Parses a vehicle type from a case-insensitive string.
     *
     * @param raw the string to parse, e.g. "car"
     * @return the matching VehicleType
     * @throws IllegalArgumentException if the string matches no type
     */
    public static VehicleType fromString(String raw) {
        if (raw == null) {
            throw new IllegalArgumentException("Vehicle type string must not be null");
        }
        return VehicleType.valueOf(raw.trim().toUpperCase());
    }
}
