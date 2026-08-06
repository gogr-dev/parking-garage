package com.parking.service;

import com.parking.model.Level;
import com.parking.model.Receipt;
import com.parking.model.Spot;
import com.parking.model.Ticket;
import com.parking.model.Vehicle;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Core garage operations: parking a vehicle, exiting a vehicle, and
 * querying garage state. Coordinates spot assignment across levels,
 * fee calculation via {@link PricingService}, and occupancy accounting
 * via {@link OccupancyTracker}. Also accumulates total revenue.
 */
public class ParkingService {

    private final List<Level> levels;
    private final PricingService pricingService;
    private final OccupancyTracker occupancyTracker;
    private final Map<String, Ticket> activeTicketsByPlate;
    private double totalRevenue;

    /**
     * Creates a parking service over the given levels.
     *
     * @param levels           the levels of the garage; must be non-null and non-empty
     * @param pricingService   the fee calculator; must be non-null
     * @param occupancyTracker the occupancy tracker; must be non-null
     * @throws IllegalArgumentException if any argument is invalid
     */
    public ParkingService(List<Level> levels, PricingService pricingService,
                          OccupancyTracker occupancyTracker) {
        if (levels == null || levels.isEmpty()) {
            throw new IllegalArgumentException("Garage must have at least one level");
        }
        if (pricingService == null || occupancyTracker == null) {
            throw new IllegalArgumentException("Services must not be null");
        }
        this.levels = levels;
        this.pricingService = pricingService;
        this.occupancyTracker = occupancyTracker;
        this.activeTicketsByPlate = new HashMap<>();
        this.totalRevenue = 0.0;
    }

    /**
     * Parks a vehicle at the given entry time. Levels are searched in order
     * and the first suitable vacant spot is assigned.
     *
     * @param vehicle   the vehicle entering; must be non-null
     * @param entryTime the entry timestamp; must be non-null
     * @return a ticket if a spot was assigned, or empty if the garage cannot
     *         accommodate the vehicle
     * @throws IllegalArgumentException if arguments are null
     * @throws IllegalStateException    if the vehicle is already parked
     */
    public Optional<Ticket> park(Vehicle vehicle, LocalDateTime entryTime) {
        if (vehicle == null) {
            throw new IllegalArgumentException("Vehicle must not be null");
        }
        if (entryTime == null) {
            throw new IllegalArgumentException("Entry time must not be null");
        }
        if (activeTicketsByPlate.containsKey(vehicle.getLicensePlate())) {
            throw new IllegalStateException(
                    "Vehicle " + vehicle.getLicensePlate() + " is already parked");
        }
        for (Level level : levels) {
            Optional<Spot> spot = level.findAvailableSpot(vehicle);
            if (spot.isPresent()) {
                Spot assigned = spot.get();
                assigned.occupy(vehicle);
                vehicle.setEntryTime(entryTime);
                Ticket ticket = new Ticket(vehicle, assigned,
                        level.getLevelNumber(), entryTime);
                activeTicketsByPlate.put(vehicle.getLicensePlate(), ticket);
                occupancyTracker.recordEntry(level.getLevelNumber());
                return Optional.of(ticket);
            }
        }
        return Optional.empty();
    }

    /**
     * Exits a vehicle at the given time, freeing its spot, charging the fee,
     * and adding the fee to total revenue.
     *
     * @param vehicle  the vehicle exiting; must be non-null
     * @param exitTime the exit timestamp; must be non-null
     * @return the receipt for the completed session
     * @throws IllegalArgumentException if arguments are null
     * @throws IllegalStateException    if the vehicle is not currently parked
     */
    public Receipt exit(Vehicle vehicle, LocalDateTime exitTime) {
        if (vehicle == null) {
            throw new IllegalArgumentException("Vehicle must not be null");
        }
        if (exitTime == null) {
            throw new IllegalArgumentException("Exit time must not be null");
        }
        Ticket ticket = activeTicketsByPlate.get(vehicle.getLicensePlate());
        if (ticket == null) {
            throw new IllegalStateException(
                    "Vehicle " + vehicle.getLicensePlate() + " is not currently parked");
        }
        double fee = pricingService.calculateFee(ticket, exitTime);
        ticket.getSpot().vacate();
        activeTicketsByPlate.remove(vehicle.getLicensePlate());
        occupancyTracker.recordExit(ticket.getLevelNumber());
        totalRevenue += fee;
        return new Receipt(ticket, exitTime, fee);
    }

    /**
     * Returns whether the given vehicle is currently parked in the garage.
     *
     * @param vehicle the vehicle to check
     * @return true if parked
     */
    public boolean isParked(Vehicle vehicle) {
        return vehicle != null
                && activeTicketsByPlate.containsKey(vehicle.getLicensePlate());
    }

    /**
     * Returns the active ticket for a license plate, if the vehicle is parked.
     *
     * @param licensePlate the plate to look up
     * @return the active ticket, or empty
     */
    public Optional<Ticket> findActiveTicket(String licensePlate) {
        if (licensePlate == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(
                activeTicketsByPlate.get(licensePlate.trim().toUpperCase()));
    }

    /**
     * Returns the total revenue collected from all completed sessions.
     *
     * @return total revenue in dollars
     */
    public double getTotalRevenue() {
        return totalRevenue;
    }

    /**
     * Returns the number of vehicles currently parked in the garage.
     *
     * @return active vehicle count
     */
    public int getActiveVehicleCount() {
        return activeTicketsByPlate.size();
    }

    /**
     * Returns the levels managed by this service.
     *
     * @return an unmodifiable view of the levels
     */
    public List<Level> getLevels() {
        return Collections.unmodifiableList(levels);
    }
}
