package com.parking.model;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * A receipt issued when a vehicle exits the garage.
 * The receipt pairs the original ticket with the exit timestamp and the
 * final fee charged.
 */
public class Receipt {

    private final Ticket ticket;
    private final LocalDateTime exitTime;
    private final double feeCharged;

    /**
     * Creates a receipt for a completed parking session.
     *
     * @param ticket     the original entry ticket; must be non-null
     * @param exitTime   the exit timestamp; must not precede the entry time
     * @param feeCharged the fee charged in dollars; must be non-negative
     * @throws IllegalArgumentException if any argument is invalid
     */
    public Receipt(Ticket ticket, LocalDateTime exitTime, double feeCharged) {
        if (ticket == null) {
            throw new IllegalArgumentException("Receipt ticket must not be null");
        }
        if (exitTime == null || exitTime.isBefore(ticket.getEntryTime())) {
            throw new IllegalArgumentException("Exit time must not precede entry time");
        }
        if (feeCharged < 0) {
            throw new IllegalArgumentException("Fee must not be negative");
        }
        this.ticket = ticket;
        this.exitTime = exitTime;
        this.feeCharged = feeCharged;
    }

    /**
     * Returns the original entry ticket.
     *
     * @return the ticket
     */
    public Ticket getTicket() {
        return ticket;
    }

    /**
     * Returns the exit timestamp.
     *
     * @return the exit time
     */
    public LocalDateTime getExitTime() {
        return exitTime;
    }

    /**
     * Returns the fee charged for the session, in dollars.
     *
     * @return the fee
     */
    public double getFeeCharged() {
        return feeCharged;
    }

    /**
     * Returns the total time the vehicle was parked.
     *
     * @return the parking duration
     */
    public Duration getParkedDuration() {
        return Duration.between(ticket.getEntryTime(), exitTime);
    }

    @Override
    public String toString() {
        return String.format("Receipt[ticket #%d, %s, parked %d min, charged $%.2f]",
                ticket.getTicketId(), ticket.getVehicle().getLicensePlate(),
                getParkedDuration().toMinutes(), feeCharged);
    }
}
