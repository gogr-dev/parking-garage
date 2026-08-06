package com.parking.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Represents one level (floor) of the parking garage.
 * A level owns a fixed collection of spots and implements the logic for
 * finding an appropriate vacant spot for a given vehicle.
 */
public class Level {

    private final int levelNumber;
    private final List<Spot> spots;

    /**
     * Creates a level with the given spots.
     *
     * @param levelNumber the floor number, starting at 1
     * @param spots       the spots belonging to this level; must be non-null
     * @throws IllegalArgumentException if spots is null
     */
    public Level(int levelNumber, List<Spot> spots) {
        if (spots == null) {
            throw new IllegalArgumentException("Spot list must not be null");
        }
        this.levelNumber = levelNumber;
        this.spots = new ArrayList<>(spots);
    }

    /**
     * Returns the floor number of this level.
     *
     * @return the level number
     */
    public int getLevelNumber() {
        return levelNumber;
    }

    /**
     * Returns an unmodifiable view of all spots on this level.
     *
     * @return the spots
     */
    public List<Spot> getSpots() {
        return Collections.unmodifiableList(spots);
    }

    /**
     * Returns the total number of spots on this level.
     *
     * @return spot count
     */
    public int getTotalSpots() {
        return spots.size();
    }

    /**
     * Returns the number of currently occupied spots on this level.
     *
     * @return occupied spot count
     */
    public int getOccupiedCount() {
        int occupied = 0;
        for (Spot spot : spots) {
            if (spot.isOccupied()) {
                occupied++;
            }
        }
        return occupied;
    }

    /**
     * Determines whether the given vehicle is physically and legally allowed
     * to use the given spot. Handicap-only spots may only be used by
     * handicap-registered vehicles.
     *
     * @param vehicle the vehicle attempting to park
     * @param spot    the candidate spot
     * @return true if the vehicle may park in the spot
     */
    public boolean canVehicleUseSpot(Vehicle vehicle, Spot spot) {
        if (vehicle == null || spot == null) {
            return false;
        }
        if (spot.isHandicapOnly() && !vehicle.isHandicap()) {
            return false;
        }
        switch (vehicle.getType()) {
            case MOTORCYCLE:
                return true;
            case CAR:
                return spot.getSize().isAtLeast(SpotSize.SMALL);
            case TRUCK:
                return spot.getSize() == SpotSize.LARGE;
            default:
                return false;
        }
    }

    /**
     * Finds a vacant spot on this level suitable for the given vehicle.
     * Handicap vehicles are offered handicap spots first; if none are free,
     * they fall back to any spot they physically fit in. Non-handicap
     * vehicles never receive handicap spots. Among eligible spots, the
     * smallest suitable size is preferred to leave larger spots free.
     *
     * @param vehicle the vehicle to place
     * @return the chosen spot, or empty if no suitable spot is vacant
     */
    public Optional<Spot> findAvailableSpot(Vehicle vehicle) {
        if (vehicle == null) {
            return Optional.empty();
        }
        if (vehicle.isHandicap()) {
            Optional<Spot> handicapSpot = findBestFit(vehicle, true);
            if (handicapSpot.isPresent()) {
                return handicapSpot;
            }
        }
        return findBestFit(vehicle, false);
    }

    /**
     * Scans vacant spots and returns the smallest eligible one, optionally
     * restricting the search to handicap-only spots.
     *
     * @param vehicle          the vehicle to place
     * @param handicapSpotOnly if true, only handicap-designated spots are considered
     * @return the best-fit spot, or empty
     */
    private Optional<Spot> findBestFit(Vehicle vehicle, boolean handicapSpotOnly) {
        Spot best = null;
        for (Spot spot : spots) {
            if (spot.isOccupied()) {
                continue;
            }
            if (handicapSpotOnly && !spot.isHandicapOnly()) {
                continue;
            }
            if (!canVehicleUseSpot(vehicle, spot)) {
                continue;
            }
            if (best == null || spot.getSize().ordinal() < best.getSize().ordinal()) {
                best = spot;
            }
        }
        return Optional.ofNullable(best);
    }

    /**
     * Finds the spot on this level where the given vehicle is parked.
     *
     * @param vehicle the vehicle to look up
     * @return the occupied spot, or empty if the vehicle is not on this level
     */
    public Optional<Spot> findSpotOf(Vehicle vehicle) {
        if (vehicle == null) {
            return Optional.empty();
        }
        for (Spot spot : spots) {
            if (spot.isOccupied() && spot.getOccupant().equals(vehicle)) {
                return Optional.of(spot);
            }
        }
        return Optional.empty();
    }

    @Override
    public String toString() {
        return String.format("Level %d (%d/%d occupied)",
                levelNumber, getOccupiedCount(), getTotalSpots());
    }
}
