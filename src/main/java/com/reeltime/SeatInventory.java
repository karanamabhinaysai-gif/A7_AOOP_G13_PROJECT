package com.reeltime;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

/** One inventory per show. Synchronized operations make multi-seat holds atomic across threads. */
public final class SeatInventory {
    private final Show show;
    private final Map<String, SeatState> states = new LinkedHashMap<>();

    public SeatInventory(Show show) {
        this.show = show;
        for (char row = 'A'; row <= 'G'; row++)
            for (int number = 1; number <= 12; number++) states.put(label(row, number), new SeatState.Free());
        for (String seat : List.of("A1", "A2", "A10", "B4", "B5", "C9", "D2", "D3", "E8", "F6", "F7", "G11"))
            states.put(seat, new SeatState.Sold("sample", "sample"));
    }

    public Show show() { return show; }
    public static String label(char row, int number) { return "" + row + number; }
    public static List<String> allSeatLabels() { return List.copyOf(new SeatInventoryLabels().labels); }

    public synchronized Map<String, SeatState> snapshot() {
        expireDueHolds();
        return Collections.unmodifiableMap(new LinkedHashMap<>(states));
    }

    public synchronized boolean holdSeats(List<String> seatLabels, String holdId, String bookingId, Duration ttl) {
        expireDueHolds();
        if (seatLabels.isEmpty() || seatLabels.size() > 6 || new HashSet<>(seatLabels).size() != seatLabels.size()) return false;
        for (String seat : seatLabels) {
            SeatState state = states.get(seat);
            if (state == null || !(state instanceof SeatState.Free)) return false;
        }
        Instant expiry = Instant.now().plus(ttl);
        seatLabels.forEach(seat -> states.put(seat, new SeatState.Held(holdId, bookingId, expiry)));
        return true;
    }

    public synchronized boolean sellHeldSeats(List<String> seatLabels, String holdId, String bookingId, String ticketId) {
        expireDueHolds();
        for (String seat : seatLabels) {
            SeatState state = states.get(seat);
            if (!(state instanceof SeatState.Held held) || !held.holdId().equals(holdId) || !held.bookingId().equals(bookingId)) return false;
        }
        seatLabels.forEach(seat -> states.put(seat, new SeatState.Sold(bookingId, ticketId)));
        return true;
    }

    public synchronized void releaseHold(String holdId) {
        states.replaceAll((seat, state) -> state instanceof SeatState.Held held && held.holdId().equals(holdId)
            ? new SeatState.Free() : state);
    }

    private void expireDueHolds() {
        Instant now = Instant.now();
        states.replaceAll((seat, state) -> state instanceof SeatState.Held held && !held.expiresAt().isAfter(now)
            ? new SeatState.Free() : state);
    }

    private static final class SeatInventoryLabels {
        final List<String> labels = new ArrayList<>();
        SeatInventoryLabels() { for (char row='A'; row<='G'; row++) for (int n=1; n<=12; n++) labels.add(label(row,n)); }
    }
}
