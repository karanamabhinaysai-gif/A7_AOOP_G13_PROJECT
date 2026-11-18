# ReelTime — Java OOP movie booking app

A Java 17 Swing desktop app demonstrating movie/show browsing, a clickable perspective 3D seat checker, temporary seat holds, food combos, dynamic ticket pricing, simulated payment, and repeat bookings. The website version includes the same 3D/2D seat view switch with rotate and zoom controls.

## Run

Install JDK 17 or newer and Maven, then run from this folder:

```sh
mvn package
java -jar target/movie-ticket-booking-1.0.0.jar
```

No external runtime libraries are used. Movie titles, showtimes, prices, and cinema details are sample data. Poster artwork can be added when supplied.

## OOP design

- **Catalog:** `CatalogService`, `Movie`, and `Show` own the sample catalog.
- **Show inventory:** `SeatInventory` owns the state for each show. `SeatCategory` and `SeatState` are sealed types. Multi-seat holds are all-or-nothing and synchronized per show; expired holds are reclaimed on the next inventory operation. `Seat3DPanel` paints a perspective seat view and maps mouse clicks back to seat IDs.
- **Booking:** `BookingService` coordinates a hold, payment, seat sale, and ticket record. It supports multiple bookings in one app session.
- **Payments:** `PaymentGateway` is an interface; `SimulatedPaymentGateway` stands in for a real provider.
- **Patterns:** `DynamicPricingStrategy` is the pricing Strategy. `ComboDecorator` wraps a ticket order with food add-ons. `SeatState` models the Free → Held → Sold state transitions. Booking orchestration represents a small hold/pay/confirm saga with hold release on payment failure.

Data is in memory for this learning demo; bookings and inventory reset when the app closes. Replace the simulated payment gateway and in-memory stores before using this for real purchases.

