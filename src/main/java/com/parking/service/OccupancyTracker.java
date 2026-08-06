package com.parking.service;

import com.parking.model.Level;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tracks current and peak occupancy, both per level and garage-wide.
 * The tracker is notified on every entry and exit and maintains counters;
 * it never mutates the levels themselves.
 */
public class OccupancyTracker {

    private final List<Level> levels;
    private final Map<Integer, Integer> currentByLevel;
    private final Map<Integer, Integer> peakByLevel;
    private int currentGarageOccupancy;
    private int peakGarageOccupancy;

    /**
     * Creates a tracker over the given levels with all counters at zero.
     *
     * @param levels the levels of the garage; must be non-null
     * @throws IllegalArgumentException if levels is null
     */
    public OccupancyTracker(List<Level> levels) {
        if (levels == null) {
            throw new IllegalArgumentException("Levels must not be null");
        }
        this.levels = levels;
        this.currentByLevel = new HashMap<>();
        this.peakByLevel = new HashMap<>();
        for (Level level : levels) {
            currentByLevel.put(level.getLevelNumber(), 0);
            peakByLevel.put(level.getLevelNumber(), 0);
        }
        this.currentGarageOccupancy = 0;
        this.peakGarageOccupancy = 0;
    }

    /**
     * Records that a vehicle parked on the given level, updating current
     * and peak counters.
     *
     * @param levelNumber the level the vehicle parked on
     * @throws IllegalArgumentException if the level is unknown
     */
    public void recordEntry(int levelNumber) {
        requireKnownLevel(levelNumber);
        int updated = currentByLevel.get(levelNumber) + 1;
        currentByLevel.put(levelNumber, updated);
        if (updated > peakByLevel.get(levelNumber)) {
            peakByLevel.put(levelNumber, updated);
        }
        currentGarageOccupancy++;
        if (currentGarageOccupancy > peakGarageOccupancy) {
            peakGarageOccupancy = currentGarageOccupancy;
        }
    }

    /**
     * Records that a vehicle exited from the given level.
     *
     * @param levelNumber the level the vehicle exited from
     * @throws IllegalArgumentException if the level is unknown
     * @throws IllegalStateException    if the level's counter is already zero
     */
    public void recordExit(int levelNumber) {
        requireKnownLevel(levelNumber);
        int current = currentByLevel.get(levelNumber);
        if (current <= 0) {
            throw new IllegalStateException(
                    "Level " + levelNumber + " occupancy is already zero");
        }
        currentByLevel.put(levelNumber, current - 1);
        currentGarageOccupancy--;
    }

    /**
     * Returns the current number of parked vehicles on a level.
     *
     * @param levelNumber the level to query
     * @return current occupancy for that level
     * @throws IllegalArgumentException if the level is unknown
     */
    public int getCurrentOccupancy(int levelNumber) {
        requireKnownLevel(levelNumber);
        return currentByLevel.get(levelNumber);
    }

    /**
     * Returns the highest occupancy a level has reached.
     *
     * @param levelNumber the level to query
     * @return peak occupancy for that level
     * @throws IllegalArgumentException if the level is unknown
     */
    public int getPeakOccupancy(int levelNumber) {
        requireKnownLevel(levelNumber);
        return peakByLevel.get(levelNumber);
    }

    /**
     * Returns the current number of parked vehicles across the whole garage.
     *
     * @return garage-wide current occupancy
     */
    public int getCurrentGarageOccupancy() {
        return currentGarageOccupancy;
    }

    /**
     * Returns the highest occupancy the whole garage has reached.
     *
     * @return garage-wide peak occupancy
     */
    public int getPeakGarageOccupancy() {
        return peakGarageOccupancy;
    }

    /**
     * Returns an unmodifiable snapshot of current occupancy per level.
     *
     * @return map of level number to current occupancy
     */
    public Map<Integer, Integer> snapshotByLevel() {
        return Collections.unmodifiableMap(new HashMap<>(currentByLevel));
    }

    /**
     * Returns the levels this tracker observes.
     *
     * @return the tracked levels
     */
    public List<Level> getLevels() {
        return Collections.unmodifiableList(levels);
    }

    private void requireKnownLevel(int levelNumber) {
        if (!currentByLevel.containsKey(levelNumber)) {
            throw new IllegalArgumentException("Unknown level: " + levelNumber);
        }
    }
}
