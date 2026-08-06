# Answer Key — do not read until you've attempted Part 1 and Part 2

## Planted bug #1 — Car allowed in a SMALL spot

- **File:** `src/main/java/com/parking/model/Level.java`
- **Method:** `canVehicleUseSpot(Vehicle, Spot)`
- **Where:** the `case CAR:` branch of the switch statement.
- **What's wrong:** the branch reads
  `return spot.getSize().isAtLeast(SpotSize.SMALL);`
  Every spot size is "at least SMALL", so the check is always true and a car
  can be assigned a SMALL spot. Because `findBestFit` prefers the smallest
  eligible spot, cars actively grab SMALL spots first.
- **Fix:** compare against `SpotSize.MEDIUM`:
  `return spot.getSize().isAtLeast(SpotSize.MEDIUM);`
- **Exposed by tests:** `carIsNeverAssignedASmallSpot`,
  `carCannotParkInGarageWithOnlySmallSpots`.

## Planted bug #2 — Handicap discount applied twice

- **File:** `src/main/java/com/parking/service/PricingService.java`
- **Method:** `calculateFee(Ticket, LocalDateTime)`
- **What's wrong:** `getEffectiveHourlyRate(vehicle)` already halves the rate
  for handicap vehicles, but `calculateFee` then multiplies the total by
  `HANDICAP_DISCOUNT_MULTIPLIER` again in the `if (vehicle.isHandicap())`
  block. A handicap vehicle ends up paying 25% of the base fee instead of 50%.
- **Fix:** remove the second discount — delete the
  `if (vehicle.isHandicap()) { total *= HANDICAP_DISCOUNT_MULTIPLIER; }`
  block in `calculateFee` (keeping the discount inside
  `getEffectiveHourlyRate`).
- **Exposed by test:** `handicapCarReceivesFiftyPercentDiscount`
  (expects $2.00 for 2 hours; buggy code returns $1.00).

## Broken tests (Part 2 answers)

1. **`truckHourlyRateMatchesDomainRules`** — asserts the truck rate is
   `2.0`; the domain rule says trucks are **$3/hr**. Fix the expected value
   to `3.0`.
2. **`garagePeakOccupancyTracksHighWaterMark`** — two vehicles are parked
   simultaneously before one exits, so the peak is **2**, not 1. Fix the
   expected value to `2`.
