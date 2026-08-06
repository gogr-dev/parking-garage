package com.parking;

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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Full test suite for the parking garage system.
 *
 * NOTE: some tests currently FAIL because of bugs planted in the production
 * code (that is part of the exercise). Tests marked TODO must be implemented,
 * and two tests contain incorrect assertions that must be corrected.
 */
public class ParkingSystemTest {

    private static final LocalDateTime T0 = LocalDateTime.of(2026, 8, 5, 9, 0);

    private List<Level> levels;
    private PricingService pricing;
    private OccupancyTracker tracker;
    private ParkingService service;

    /**
     * Builds a fresh two-level garage before each test.
     * Level 1: 2 SMALL, 2 MEDIUM, 1 handicap MEDIUM, 1 LARGE.
     * Level 2: 1 SMALL, 1 MEDIUM, 1 LARGE, 1 handicap LARGE.
     */
    @BeforeEach
    void setUp() {
        List<Spot> l1 = new ArrayList<>();
        l1.add(new Spot("L1-S01", SpotSize.SMALL, false));
        l1.add(new Spot("L1-S02", SpotSize.SMALL, false));
        l1.add(new Spot("L1-S03", SpotSize.MEDIUM, false));
        l1.add(new Spot("L1-S04", SpotSize.MEDIUM, false));
        l1.add(new Spot("L1-S05", SpotSize.MEDIUM, true));
        l1.add(new Spot("L1-S06", SpotSize.LARGE, false));

        List<Spot> l2 = new ArrayList<>();
        l2.add(new Spot("L2-S01", SpotSize.SMALL, false));
        l2.add(new Spot("L2-S02", SpotSize.MEDIUM, false));
        l2.add(new Spot("L2-S03", SpotSize.LARGE, false));
        l2.add(new Spot("L2-S04", SpotSize.LARGE, true));

        levels = new ArrayList<>();
        levels.add(new Level(1, l1));
        levels.add(new Level(2, l2));

        pricing = new PricingService();
        tracker = new OccupancyTracker(levels);
        service = new ParkingService(levels, pricing, tracker);
    }

    // ------------------------------------------------------------------
    // Spot assignment by vehicle type
    // ------------------------------------------------------------------

    @Test
    void motorcycleParksInSmallestAvailableSpot() {
        Vehicle bike = new Vehicle("MOTO-1", VehicleType.MOTORCYCLE, false);
        Optional<Ticket> ticket = service.park(bike, T0);
        assertTrue(ticket.isPresent());
        assertEquals(SpotSize.SMALL, ticket.get().getSpot().getSize());
    }

    @Test
    void carIsNeverAssignedASmallSpot() {
        Vehicle car = new Vehicle("CAR-1", VehicleType.CAR, false);
        Optional<Ticket> ticket = service.park(car, T0);
        assertTrue(ticket.isPresent());
        assertNotEquals(SpotSize.SMALL, ticket.get().getSpot().getSize(),
                "Cars must not park in SMALL spots");
    }

    @Test
    void carCannotParkInGarageWithOnlySmallSpots() {
        List<Spot> onlySmall = new ArrayList<>();
        onlySmall.add(new Spot("X-S01", SpotSize.SMALL, false));
        onlySmall.add(new Spot("X-S02", SpotSize.SMALL, false));
        List<Level> smallLevels = new ArrayList<>();
        smallLevels.add(new Level(1, onlySmall));
        ParkingService smallService = new ParkingService(
                smallLevels, pricing, new OccupancyTracker(smallLevels));

        Vehicle car = new Vehicle("CAR-2", VehicleType.CAR, false);
        Optional<Ticket> ticket = smallService.park(car, T0);
        assertFalse(ticket.isPresent(),
                "A car must be turned away when only SMALL spots exist");
    }

    @Test
    void truckOnlyFitsLargeSpot() {
        Vehicle truck = new Vehicle("TRK-1", VehicleType.TRUCK, false);
        Optional<Ticket> ticket = service.park(truck, T0);
        assertTrue(ticket.isPresent());
        assertEquals(SpotSize.LARGE, ticket.get().getSpot().getSize());
    }

    @Test
    void truckTurnedAwayWhenNoLargeSpotFree() {
        Vehicle truck1 = new Vehicle("TRK-1", VehicleType.TRUCK, false);
        Vehicle truck2 = new Vehicle("TRK-2", VehicleType.TRUCK, false);
        Vehicle truck3 = new Vehicle("TRK-3", VehicleType.TRUCK, false);
        assertTrue(service.park(truck1, T0).isPresent());
        assertTrue(service.park(truck2, T0).isPresent());
        // Only remaining LARGE spot is handicap-only on level 2.
        Optional<Ticket> ticket = service.park(truck3, T0);
        assertFalse(ticket.isPresent());
    }

    @Test
    void overflowVehicleIsPlacedOnSecondLevel() {
        Vehicle truck1 = new Vehicle("TRK-1", VehicleType.TRUCK, false);
        Vehicle truck2 = new Vehicle("TRK-2", VehicleType.TRUCK, false);
        assertEquals(1, service.park(truck1, T0).get().getLevelNumber());
        assertEquals(2, service.park(truck2, T0).get().getLevelNumber());
    }

    // ------------------------------------------------------------------
    // Handicap spot rules
    // ------------------------------------------------------------------

    @Test
    void nonHandicapVehicleNeverGetsHandicapSpot() {
        // Fill all non-handicap spots a car could use on both levels.
        service.park(new Vehicle("CAR-A", VehicleType.CAR, false), T0);
        service.park(new Vehicle("CAR-B", VehicleType.CAR, false), T0);
        service.park(new Vehicle("CAR-C", VehicleType.CAR, false), T0);
        service.park(new Vehicle("CAR-D", VehicleType.CAR, false), T0);
        service.park(new Vehicle("CAR-E", VehicleType.CAR, false), T0);
        // NOTE: while bug #1 is present, some of the cars above may land in
        // SMALL spots; this test only asserts the handicap rule below.
        Optional<Ticket> ticket =
                service.park(new Vehicle("CAR-F", VehicleType.CAR, false), T0);
        ticket.ifPresent(t -> assertFalse(t.getSpot().isHandicapOnly(),
                "Regular vehicles must never occupy handicap spots"));
    }

    @Test
    void handicapVehicleGetsHandicapSpotFirst() {
        Vehicle car = new Vehicle("HC-1", VehicleType.CAR, true);
        Optional<Ticket> ticket = service.park(car, T0);
        assertTrue(ticket.isPresent());
        assertTrue(ticket.get().getSpot().isHandicapOnly(),
                "Handicap vehicles should be offered handicap spots first");
    }

    @Test
    void handicapVehicleFallsBackToRegularSpotWhenHandicapSpotsFull() {
        // Occupy both handicap spots.
        assertTrue(service.park(new Vehicle("HC-1", VehicleType.CAR, true), T0).isPresent());
        assertTrue(service.park(new Vehicle("HC-2", VehicleType.CAR, true), T0).isPresent());
        Optional<Ticket> ticket =
                service.park(new Vehicle("HC-3", VehicleType.CAR, true), T0);
        assertTrue(ticket.isPresent());
        assertFalse(ticket.get().getSpot().isHandicapOnly(),
                "Handicap vehicle should fall back to a regular spot");
    }

    // ------------------------------------------------------------------
    // Pricing
    // ------------------------------------------------------------------

    @Test
    void motorcycleChargedOneDollarPerHour() {
        Vehicle bike = new Vehicle("MOTO-1", VehicleType.MOTORCYCLE, false);
        Ticket ticket = service.park(bike, T0).get();
        double fee = pricing.calculateFee(ticket, T0.plusHours(2));
        assertEquals(2.0, fee, 0.001);
    }

    @Test
    void partialHourRoundsUpToFullHour() {
        Vehicle car = new Vehicle("CAR-1", VehicleType.CAR, false);
        Ticket ticket = service.park(car, T0).get();
        double fee = pricing.calculateFee(ticket, T0.plusMinutes(90));
        assertEquals(4.0, fee, 0.001, "90 minutes should bill as 2 hours");
    }

    @Test
    void minimumBillingIsOneHour() {
        Vehicle bike = new Vehicle("MOTO-1", VehicleType.MOTORCYCLE, false);
        Ticket ticket = service.park(bike, T0).get();
        double fee = pricing.calculateFee(ticket, T0.plusMinutes(10));
        assertEquals(1.0, fee, 0.001);
    }

    @Test
    void handicapCarReceivesFiftyPercentDiscount() {
        Vehicle car = new Vehicle("HC-1", VehicleType.CAR, true);
        Ticket ticket = service.park(car, T0).get();
        double fee = pricing.calculateFee(ticket, T0.plusHours(2));
        assertEquals(2.0, fee, 0.001,
                "2 hours at $2/hr with 50% discount should be $2.00");
    }

    /**
     * BROKEN? This test encodes an expectation about the truck hourly rate.
     * Verify the assertion against the domain rules.
     */
    @Test
    void truckHourlyRateMatchesDomainRules() {
        assertEquals(2.0, VehicleType.TRUCK.getHourlyRate(), 0.001);
    }

    // ------------------------------------------------------------------
    // Occupancy tracking
    // ------------------------------------------------------------------

    @Test
    void occupancyIncrementsOnEntryAndDecrementsOnExit() {
        Vehicle car = new Vehicle("CAR-1", VehicleType.CAR, false);
        service.park(car, T0);
        assertEquals(1, tracker.getCurrentGarageOccupancy());
        service.exit(car, T0.plusHours(1));
        assertEquals(0, tracker.getCurrentGarageOccupancy());
    }

    @Test
    void peakOccupancyIsRetainedAfterVehiclesLeave() {
        Vehicle a = new Vehicle("CAR-A", VehicleType.CAR, false);
        Vehicle b = new Vehicle("CAR-B", VehicleType.CAR, false);
        service.park(a, T0);
        service.park(b, T0);
        service.exit(a, T0.plusHours(1));
        service.exit(b, T0.plusHours(1));
        assertEquals(0, tracker.getCurrentGarageOccupancy());
        assertEquals(2, tracker.getPeakGarageOccupancy());
    }

    /**
     * BROKEN? This test encodes an expectation about garage-wide peak
     * occupancy. Trace the scenario by hand and verify the assertion.
     */
    @Test
    void garagePeakOccupancyTracksHighWaterMark() {
        Vehicle a = new Vehicle("MOTO-A", VehicleType.MOTORCYCLE, false);
        Vehicle b = new Vehicle("MOTO-B", VehicleType.MOTORCYCLE, false);
        service.park(a, T0);
        service.park(b, T0.plusMinutes(5));
        service.exit(a, T0.plusMinutes(10));
        assertEquals(1, tracker.getPeakGarageOccupancy());
    }

    @Test
    void exitAddsFeeToTotalRevenue() {
        Vehicle car = new Vehicle("CAR-1", VehicleType.CAR, false);
        service.park(car, T0);
        Receipt receipt = service.exit(car, T0.plusHours(3));
        assertEquals(receipt.getFeeCharged(), service.getTotalRevenue(), 0.001);
        assertTrue(service.getTotalRevenue() > 0);
    }

    // ------------------------------------------------------------------
    // Edge cases
    // ------------------------------------------------------------------

    @Test
    void fullGarageTurnsAwayNewVehicles() {
        // 10 spots total; motorcycles fit everywhere except handicap spots.
        for (int i = 0; i < 10; i++) {
            boolean handicap = i >= 8; // last two take the handicap spots
            assertTrue(service.park(
                    new Vehicle("M-" + i, VehicleType.MOTORCYCLE, handicap), T0)
                    .isPresent(), "vehicle " + i + " should fit");
        }
        Optional<Ticket> overflow = service.park(
                new Vehicle("M-99", VehicleType.MOTORCYCLE, false), T0);
        assertFalse(overflow.isPresent());
    }

    @Test
    void exitingAVehicleThatNeverParkedThrows() {
        Vehicle ghost = new Vehicle("GHOST-1", VehicleType.CAR, false);
        assertThrows(IllegalStateException.class,
                () -> service.exit(ghost, T0.plusHours(1)));
    }

    @Test
    void parkingNullVehicleThrows() {
        assertThrows(IllegalArgumentException.class, () -> service.park(null, T0));
    }

    @Test
    void creatingVehicleWithBlankPlateThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new Vehicle("   ", VehicleType.CAR, false));
    }

    @Test
    void handicapSpotCannotBeSmall() {
        assertThrows(IllegalArgumentException.class,
                () -> new Spot("BAD-1", SpotSize.SMALL, true));
    }

    // ------------------------------------------------------------------
    // TODO tests — implement these yourself (Part 2 of the README)
    // ------------------------------------------------------------------

    /**
     * TODO: Verify that a motorcycle parks in a LARGE spot when only LARGE
     * spots are available (build a single-level garage with one LARGE spot).
     */
    @Test
    void todoMotorcycleUsesLargeSpotWhenNothingSmallerExists() {
        fail("TODO: implement this test");
    }

    /**
     * TODO: Verify pricing for a handicap TRUCK parked exactly 4 hours.
     * Work out the expected fee from the domain rules first.
     */
    @Test
    void todoHandicapTruckPricingForFourHours() {
        fail("TODO: implement this test");
    }

    /**
     * TODO: Verify that parking the same vehicle twice (without exiting)
     * throws IllegalStateException.
     */
    @Test
    void todoDoubleParkingSameVehicleThrows() {
        fail("TODO: implement this test");
    }

    /**
     * TODO: Verify that a session of exactly 120 minutes bills as exactly
     * 2 hours (not 3) — exercise the hour-boundary logic in
     * PricingService.computeBillableHours.
     */
    @Test
    void todoExactHourBoundaryDoesNotRoundUp() {
        fail("TODO: implement this test");
    }

    /**
     * TODO: Verify that revenue accumulates correctly across three separate
     * exits (park three vehicles, exit all three, assert the sum).
     */
    @Test
    void todoRevenueAccumulatesAcrossMultipleExits() {
        fail("TODO: implement this test");
    }

    /**
     * TODO: Verify OccupancyTracker.snapshotByLevel() reflects per-level
     * counts after parking vehicles on both levels.
     */
    @Test
    void todoPerLevelOccupancySnapshot() {
        fail("TODO: implement this test");
    }

    /**
     * TODO: Verify that exiting with a null exit time throws
     * IllegalArgumentException (vehicle must be parked first).
     */
    @Test
    void todoNullExitTimeThrows() {
        fail("TODO: implement this test");
    }

    /**
     * TODO: Verify that after a vehicle exits, its spot can immediately be
     * reused by another vehicle of the same type.
     */
    @Test
    void todoFreedSpotIsImmediatelyReusable() {
        fail("TODO: implement this test");
    }
}
