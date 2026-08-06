package com.parking.service;

import com.parking.model.Ticket;
import com.parking.model.Vehicle;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Calculates parking fees based on parked duration, vehicle type, and
 * handicap registration.
 *
 * <p>Billing rules: time is billed per started hour with a one-hour
 * minimum. Base rates are $1/hr for motorcycles, $2/hr for cars, and
 * $3/hr for trucks. Handicap-registered vehicles receive a 50% discount
 * on the total fee.</p>
 */
public class PricingService {

    /** Multiplier applied to the fee for handicap-registered vehicles. */
    public static final double HANDICAP_DISCOUNT_MULTIPLIER = 0.5;

    /**
     * Computes the number of billable hours between entry and exit.
     * Any started hour is billed in full, with a minimum of one hour.
     *
     * @param entryTime the entry timestamp; must be non-null
     * @param exitTime  the exit timestamp; must not precede entry
     * @return billable hours, always at least 1
     * @throws IllegalArgumentException if timestamps are invalid
     */
    public long computeBillableHours(LocalDateTime entryTime, LocalDateTime exitTime) {
        if (entryTime == null || exitTime == null) {
            throw new IllegalArgumentException("Entry and exit times must not be null");
        }
        if (exitTime.isBefore(entryTime)) {
            throw new IllegalArgumentException("Exit time must not precede entry time");
        }
        long minutes = ChronoUnit.MINUTES.between(entryTime, exitTime);
        long fullHours = minutes / 60;
        long billable = (minutes % 60 == 0) ? fullHours : fullHours + 1;
        return Math.max(1, billable);
    }

    /**
     * Returns the effective hourly rate for a vehicle, taking handicap
     * registration into account.
     *
     * @param vehicle the vehicle; must be non-null
     * @return dollars per hour after any applicable discount
     * @throws IllegalArgumentException if the vehicle is null
     */
    public double getEffectiveHourlyRate(Vehicle vehicle) {
        if (vehicle == null) {
            throw new IllegalArgumentException("Vehicle must not be null");
        }
        double rate = vehicle.getType().getHourlyRate();
        if (vehicle.isHandicap()) {
            rate *= HANDICAP_DISCOUNT_MULTIPLIER;
        }
        return rate;
    }

    /**
     * Calculates the total fee for a completed parking session.
     *
     * @param ticket   the entry ticket; must be non-null
     * @param exitTime the exit timestamp; must not precede the entry time
     * @return the fee in dollars
     * @throws IllegalArgumentException if arguments are invalid
     */
    public double calculateFee(Ticket ticket, LocalDateTime exitTime) {
        if (ticket == null) {
            throw new IllegalArgumentException("Ticket must not be null");
        }
        if (ticket.getVehicle() == null) {
            throw new IllegalArgumentException("Ticket vehicle must not be null");
        }
        Vehicle vehicle = ticket.getVehicle();
        long hours = computeBillableHours(ticket.getEntryTime(), exitTime);
        double total = hours * getEffectiveHourlyRate(vehicle);
        if (vehicle.isHandicap()) {
            total *= HANDICAP_DISCOUNT_MULTIPLIER;
        }
        return total;
    }

    /**
     * Formats a fee as a display string, e.g. "$4.50".
     *
     * @param fee the fee in dollars
     * @return the formatted string
     */
    public String formatFee(double fee) {
        return String.format("$%.2f", fee);
    }
}
