package com.parking.simulation;

import com.parking.model.Vehicle;

import java.time.LocalDateTime;

/**
 * Represents a single event in a garage simulation: one vehicle either
 * entering or exiting the garage at a specific timestamp.
 */
public class SimulationEvent implements Comparable<SimulationEvent> {

    /** The kind of event: a vehicle arriving or departing. */
    public enum EventType {
        /** Vehicle arrives and requests a spot. */
        ENTER,
        /** Vehicle leaves and pays its fee. */
        EXIT
    }

    private final EventType type;
    private final Vehicle vehicle;
    private final LocalDateTime timestamp;

    /**
     * Creates a simulation event.
     *
     * @param type      the event type; must be non-null
     * @param vehicle   the vehicle involved; must be non-null
     * @param timestamp when the event occurs; must be non-null
     * @throws IllegalArgumentException if any argument is null
     */
    public SimulationEvent(EventType type, Vehicle vehicle, LocalDateTime timestamp) {
        if (type == null || vehicle == null || timestamp == null) {
            throw new IllegalArgumentException("Event fields must not be null");
        }
        this.type = type;
        this.vehicle = vehicle;
        this.timestamp = timestamp;
    }

    /**
     * Returns the type of this event.
     *
     * @return ENTER or EXIT
     */
    public EventType getType() {
        return type;
    }

    /**
     * Returns the vehicle involved in this event.
     *
     * @return the vehicle
     */
    public Vehicle getVehicle() {
        return vehicle;
    }

    /**
     * Returns the timestamp at which this event occurs.
     *
     * @return the timestamp
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    /**
     * Orders events chronologically so a simulation can replay them in time
     * order.
     *
     * @param other the event to compare against
     * @return negative if this event occurs first, positive if later
     */
    @Override
    public int compareTo(SimulationEvent other) {
        return this.timestamp.compareTo(other.timestamp);
    }

    @Override
    public String toString() {
        return String.format("%s %s @ %s", type, vehicle.getLicensePlate(), timestamp);
    }
}
