package com.parking.model;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A parking ticket issued when a vehicle enters the garage.
 * The ticket records the vehicle, the assigned spot, the level it is on,
 * and the entry timestamp. Tickets receive sequential unique ids.
 */
public class Ticket {

    private static final AtomicLong NEXT_ID = new AtomicLong(1);

    private final long ticketId;
    private final Vehicle vehicle;
    private final Spot spot;
    private final int levelNumber;
    private final LocalDateTime entryTime;

    /**
     * Creates a ticket for a vehicle parked at a spot.
     *
     * @param vehicle     the parked vehicle; must be non-null
     * @param spot        the assigned spot; must be non-null
     * @param levelNumber the level the spot is on
     * @param entryTime   the entry timestamp; must be non-null
     * @throws IllegalArgumentException if any required argument is null
     */
    public Ticket(Vehicle vehicle, Spot spot, int levelNumber, LocalDateTime entryTime) {
        if (vehicle == null) {
            throw new IllegalArgumentException("Ticket vehicle must not be null");
        }
        if (spot == null) {
            throw new IllegalArgumentException("Ticket spot must not be null");
        }
        if (entryTime == null) {
            throw new IllegalArgumentException("Ticket entry time must not be null");
        }
        this.ticketId = NEXT_ID.getAndIncrement();
        this.vehicle = vehicle;
        this.spot = spot;
        this.levelNumber = levelNumber;
        this.entryTime = entryTime;
    }

    /**
     * Returns the unique id of this ticket.
     *
     * @return the ticket id
     */
    public long getTicketId() {
        return ticketId;
    }

    /**
     * Returns the vehicle this ticket was issued to.
     *
     * @return the vehicle
     */
    public Vehicle getVehicle() {
        return vehicle;
    }

    /**
     * Returns the spot assigned by this ticket.
     *
     * @return the spot
     */
    public Spot getSpot() {
        return spot;
    }

    /**
     * Returns the level number of the assigned spot.
     *
     * @return the level number
     */
    public int getLevelNumber() {
        return levelNumber;
    }

    /**
     * Returns the timestamp at which the vehicle entered.
     *
     * @return the entry time
     */
    public LocalDateTime getEntryTime() {
        return entryTime;
    }

    @Override
    public String toString() {
        return String.format("Ticket #%d: %s at %s (level %d) entered %s",
                ticketId, vehicle, spot.getSpotId(), levelNumber, entryTime);
        
    }
}
