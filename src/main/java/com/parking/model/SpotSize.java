package com.parking.model;

/**
 * Enumerates the physical sizes of parking spots in the garage.
 * Ordinal order is significant: SMALL &lt; MEDIUM &lt; LARGE.
 */
public enum SpotSize {

    /** Fits motorcycles only. */
    SMALL,

    /** Fits motorcycles and cars. */
    MEDIUM,

    /** Fits all vehicle types. */
    LARGE;

    /**
     * Returns true if this size is at least as large as the given size.
     *
     * @param other the size to compare against
     * @return true if this size can hold anything the other size can
     */
    public boolean isAtLeast(SpotSize other) {
        return this.ordinal() >= other.ordinal();
    }

    /**
     * Returns a human-readable label for this spot size.
     *
     * @return capitalized display name, e.g. "Medium"
     */
    public String displayName() {
        String lower = name().toLowerCase();
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }
}
