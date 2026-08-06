package com.parking.simulation;

import com.parking.model.Level;
import com.parking.model.Receipt;
import com.parking.model.Spot;
import com.parking.model.SpotSize;
import com.parking.model.Ticket;
import com.parking.model.Vehicle;
import com.parking.model.VehicleType;
import com.parking.service.OccupancyTracker;
import com.parking.service.ParkingService;
import com.parking.service.PricingService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Runs a full end-to-end simulation of the garage: builds a two-level
 * garage, replays a chronological list of enter/exit events through the
 * {@link ParkingService}, and prints a summary of revenue and occupancy.
 */
public class GarageSimulation {

    private final ParkingService parkingService;
    private final OccupancyTracker occupancyTracker;
    private final List<String> eventLog;

    /**
     * Creates a simulation around an existing parking service and tracker.
     *
     * @param parkingService   the service to drive; must be non-null
     * @param occupancyTracker the tracker to report from; must be non-null
     * @throws IllegalArgumentException if arguments are null
     */
    public GarageSimulation(ParkingService parkingService,
                            OccupancyTracker occupancyTracker) {
        if (parkingService == null || occupancyTracker == null) {
            throw new IllegalArgumentException("Simulation dependencies must not be null");
        }
        this.parkingService = parkingService;
        this.occupancyTracker = occupancyTracker;
        this.eventLog = new ArrayList<>();
    }

    /**
     * Builds a standard demo garage: two levels, each with a mix of small,
     * medium, and large spots, including one handicap spot per level.
     *
     * @return the levels of the demo garage
     */
    public static List<Level> buildDemoGarage() {
        List<Level> levels = new ArrayList<>();
        for (int levelNum = 1; levelNum <= 2; levelNum++) {
            List<Spot> spots = new ArrayList<>();
            int seq = 1;
            for (int i = 0; i < 3; i++) {
                spots.add(new Spot(spotId(levelNum, seq++), SpotSize.SMALL, false));
            }
            for (int i = 0; i < 3; i++) {
                spots.add(new Spot(spotId(levelNum, seq++), SpotSize.MEDIUM, false));
            }
            spots.add(new Spot(spotId(levelNum, seq++), SpotSize.MEDIUM, true));
            for (int i = 0; i < 2; i++) {
                spots.add(new Spot(spotId(levelNum, seq++), SpotSize.LARGE, false));
            }
            levels.add(new Level(levelNum, spots));
        }
        return levels;
    }

    private static String spotId(int levelNum, int seq) {
        return String.format("L%d-S%02d", levelNum, seq);
    }

    /**
     * Replays the given events in chronological order. ENTER events attempt
     * to park; EXIT events attempt to exit and collect a fee. Failures
     * (garage full, unknown vehicle) are logged rather than thrown so a
     * simulation always runs to completion.
     *
     * @param events the events to replay; must be non-null
     * @throws IllegalArgumentException if events is null
     */
    public void run(List<SimulationEvent> events) {
        if (events == null) {
            throw new IllegalArgumentException("Events must not be null");
        }
        List<SimulationEvent> ordered = new ArrayList<>(events);
        Collections.sort(ordered);
        for (SimulationEvent event : ordered) {
            if (event.getType() == SimulationEvent.EventType.ENTER) {
                handleEnter(event);
            } else {
                handleExit(event);
            }
        }
    }

    private void handleEnter(SimulationEvent event) {
        try {
            Optional<Ticket> ticket =
                    parkingService.park(event.getVehicle(), event.getTimestamp());
            if (ticket.isPresent()) {
                log(String.format("%s parked at %s", event.getVehicle(),
                        ticket.get().getSpot().getSpotId()));
            } else {
                log(String.format("%s turned away: no suitable spot", event.getVehicle()));
            }
        } catch (IllegalStateException e) {
            log(String.format("%s rejected: %s", event.getVehicle(), e.getMessage()));
        }
    }

    private void handleExit(SimulationEvent event) {
        try {
            Receipt receipt = parkingService.exit(event.getVehicle(), event.getTimestamp());
            log(String.format("%s exited, charged $%.2f", event.getVehicle(),
                    receipt.getFeeCharged()));
        } catch (IllegalStateException e) {
            log(String.format("%s exit failed: %s", event.getVehicle(), e.getMessage()));
        }
    }

    private void log(String message) {
        eventLog.add(message);
        System.out.println("[sim] " + message);
    }

    /**
     * Returns the log of everything that happened during the simulation.
     *
     * @return an unmodifiable list of log lines
     */
    public List<String> getEventLog() {
        return Collections.unmodifiableList(eventLog);
    }

    /**
     * Prints a summary of the run: revenue, current and peak occupancy.
     */
    public void printSummary() {
        System.out.println("=== Simulation summary ===");
        System.out.printf("Total revenue: $%.2f%n", parkingService.getTotalRevenue());
        System.out.printf("Vehicles still parked: %d%n",
                parkingService.getActiveVehicleCount());
        System.out.printf("Garage peak occupancy: %d%n",
                occupancyTracker.getPeakGarageOccupancy());
        for (Level level : parkingService.getLevels()) {
            System.out.printf("Level %d: current %d, peak %d%n",
                    level.getLevelNumber(),
                    occupancyTracker.getCurrentOccupancy(level.getLevelNumber()),
                    occupancyTracker.getPeakOccupancy(level.getLevelNumber()));
        }
    }

    /**
     * Entry point: builds the demo garage, replays a small scripted day of
     * traffic, and prints the summary.
     *
     * @param args unused
     */
    public static void main(String[] args) {
        List<Level> levels = buildDemoGarage();
        PricingService pricing = new PricingService();
        OccupancyTracker tracker = new OccupancyTracker(levels);
        ParkingService service = new ParkingService(levels, pricing, tracker);
        GarageSimulation simulation = new GarageSimulation(service, tracker);

        LocalDateTime day = LocalDateTime.of(2026, 8, 5, 8, 0);
        Vehicle bike = new Vehicle("MOTO-1", VehicleType.MOTORCYCLE, false);
        Vehicle car1 = new Vehicle("CAR-100", VehicleType.CAR, false);
        Vehicle car2 = new Vehicle("CAR-200", VehicleType.CAR, true);
        Vehicle truck = new Vehicle("TRUCK-9", VehicleType.TRUCK, false);

        List<SimulationEvent> events = new ArrayList<>();
        events.add(new SimulationEvent(SimulationEvent.EventType.ENTER, bike, day));
        events.add(new SimulationEvent(SimulationEvent.EventType.ENTER, car1,
                day.plusMinutes(15)));
        events.add(new SimulationEvent(SimulationEvent.EventType.ENTER, car2,
                day.plusMinutes(30)));
        events.add(new SimulationEvent(SimulationEvent.EventType.ENTER, truck,
                day.plusMinutes(45)));
        events.add(new SimulationEvent(SimulationEvent.EventType.EXIT, car1,
                day.plusHours(2)));
        events.add(new SimulationEvent(SimulationEvent.EventType.EXIT, bike,
                day.plusHours(3)));
        events.add(new SimulationEvent(SimulationEvent.EventType.EXIT, car2,
                day.plusHours(4)));
        events.add(new SimulationEvent(SimulationEvent.EventType.EXIT, truck,
                day.plusHours(5).plusMinutes(30)));

        simulation.run(events);
        simulation.printSummary();
    }
}
