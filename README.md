# Parking Garage Management System — Practice Project

A mid-level Java practice codebase in the style of a Karat multi-file technical
interview. The system models a multi-level parking garage with sized spots,
handicap rules, time-based pricing, occupancy tracking, and an event-driven
simulation.

## Domain rules

- A `ParkingGarage` consists of multiple `Level`s, each with a fixed set of spots.
- Spot sizes: `SMALL`, `MEDIUM`, `LARGE`.
- Vehicle fit rules:
  - **Motorcycle** — fits SMALL, MEDIUM, LARGE
  - **Car** — fits MEDIUM, LARGE only
  - **Truck** — fits LARGE only
- Handicap spots are MEDIUM or LARGE spots reserved for handicap-registered
  vehicles. Handicap vehicles are offered handicap spots first, then fall back
  to any spot they fit in. Regular vehicles never get handicap spots.
- Pricing: Motorcycle $1/hr, Car $2/hr, Truck $3/hr. Billing is per started
  hour with a one-hour minimum. Handicap vehicles get a **50% discount** on
  the total fee.
- The garage tracks total revenue, current occupancy per level, and peak
  occupancy (per level and garage-wide).

## Project layout

```
src/main/java/com/parking/
  model/       Vehicle, VehicleType, Spot, SpotSize, Level, Ticket, Receipt
  service/     ParkingService, PricingService, OccupancyTracker
  simulation/  GarageSimulation (has a main method), SimulationEvent
src/test/java/com/parking/
  ParkingSystemTest.java
```

## Running it

```bash
mvn test                                                    # run the test suite
mvn compile exec:java -Dexec.mainClass=com.parking.simulation.GarageSimulation
# or without the exec plugin:
mvn compile && java -cp target/classes com.parking.simulation.GarageSimulation
```

Expect some test failures out of the box — that is the exercise.

---

## Practice tasks

### Part 1 — Bug hunt (15 min)
Two bugs are planted in the production code (they are **not** marked with
comments). Several tests in `ParkingSystemTest` fail because of them.

1. A spot-assignment bug that lets a **Car park in a SMALL spot**.
2. A pricing bug that applies the **handicap discount twice**.

Find both, fix them, and get the related tests passing. Check your answers
against `BUGS.md` when done (no peeking first).

### Part 2 — Test suite (20 min)
- Implement the **8 TODO tests** at the bottom of `ParkingSystemTest.java`
  (they currently call `fail("TODO: ...")`).
- Two tests contain **wrong assertions** (their Javadoc says "BROKEN?").
  Trace the expected behavior by hand and correct the assertions.

### Part 3 — Waitlist feature (20 min)
Implement a waitlist: when `ParkingService.park(...)` finds no spot, the
vehicle joins a FIFO queue instead of being turned away. When a spot frees up
on exit, the first waiting vehicle that fits that spot is parked automatically
and "notified" (a callback interface or a log entry is fine). Suggested shape:

- New class `service/WaitlistService.java` with `enqueue`, `onSpotFreed`,
  `getWaitingCount`.
- Wire it into `ParkingService.exit(...)`.
- Add at least 3 tests: queued vehicle is parked on release, FIFO order is
  respected, a freed SMALL spot does not dequeue a waiting truck.

### Part 4 — Utilization report (15 min)
Add to `OccupancyTracker`:

```java
public Map<SpotSize, Double> getUtilizationBySize()
```

Returns, for each spot size across the whole garage, `occupied / total` as a
value between 0.0 and 1.0. Sizes with zero spots should map to 0.0 (no
division-by-zero). Add tests for an empty garage, a partially full garage,
and a size with no spots.

### Part 5 — Edge-case hardening (10 min)
Make sure the following are handled gracefully (exception with a clear
message, or a documented no-op) and covered by tests:

- A vehicle tries to exit that was never parked.
- A `Ticket` constructed with a null vehicle.
- A `Level` created with zero spots (empty list) — parking attempts should
  return empty, and occupancy math should not break.

---

## Suggested time budget

| Part | Task | Time |
|------|------|------|
| 1 | Find and fix both bugs | 15 min |
| 2 | Complete TODO tests, fix broken tests | 20 min |
| 3 | Waitlist feature | 20 min |
| 4 | Utilization-by-size report | 15 min |
| 5 | Edge cases | 10 min |
