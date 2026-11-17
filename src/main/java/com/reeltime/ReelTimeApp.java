package com.reeltime;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.*;
import java.util.List;

public final class ReelTimeApp {
    private static final Color BG = new Color(13, 16, 25), PANEL = new Color(21, 26, 38), RED = new Color(240, 77, 96), MINT = new Color(99, 214, 177);
    private final CatalogService catalog = new CatalogService();
    private final BookingService bookings = new BookingService(new SimulatedPaymentGateway(), new DemandPricingStrategy());
    private final JComboBox<Movie> moviePicker = new JComboBox<>();
    private final JComboBox<Show> showPicker = new JComboBox<>();
    private final JPanel seatsPanel = new JPanel(new GridLayout(7, 12, 6, 6));
    private final JPanel historyPanel = new JPanel();
    private final JLabel totalLabel = new JLabel("Total: ₹0");
    private final Set<String> selectedSeats = new LinkedHashSet<>();
    private final Map<String, JButton> seatButtons = new HashMap<>();
    private final Map<String, JCheckBox> comboBoxes = new LinkedHashMap<>();

    public static void main(String[] args) { SwingUtilities.invokeLater(() -> new ReelTimeApp().show()); }

    private void show() {
        JFrame frame = new JFrame("ReelTime | Movie tickets");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(1100, 820); frame.setMinimumSize(new Dimension(900, 700)); frame.setLocationRelativeTo(null);
        JPanel root = new JPanel(new BorderLayout(18, 18)); root.setBackground(BG); root.setBorder(new EmptyBorder(22, 26, 22, 26));
        JLabel brand = new JLabel("reelTIME   ·   HYDERABAD"); brand.setForeground(Color.WHITE); brand.setFont(new Font("SansSerif", Font.BOLD, 24));
        root.add(brand, BorderLayout.NORTH);
        JPanel content = new JPanel(new GridLayout(1, 2, 18, 0)); content.setOpaque(false);
        content.add(buildBookingPanel()); content.add(buildCheckoutPanel()); root.add(content, BorderLayout.CENTER);
        frame.setContentPane(root); refreshMovies(); frame.setVisible(true);
    }

    private JPanel buildBookingPanel() {
        JPanel panel = panel(); panel.setLayout(new BorderLayout(12, 12));
        JPanel header = new JPanel(new GridLayout(0, 1, 0, 8)); header.setOpaque(false);
        JLabel heading = label("Pick your movie & seats", 20, Color.WHITE);
        moviePicker.setBackground(new Color(31, 38, 54)); moviePicker.setForeground(Color.WHITE);
        showPicker.setBackground(new Color(31, 38, 54)); showPicker.setForeground(Color.WHITE);
        moviePicker.addActionListener(e -> refreshShows()); showPicker.addActionListener(e -> { selectedSeats.clear(); refreshSeatGrid(); updateTotal(); });
        header.add(heading); header.add(moviePicker); header.add(showPicker); panel.add(header, BorderLayout.NORTH);
        seatsPanel.setOpaque(false); seatsPanel.setBorder(new EmptyBorder(22, 10, 18, 10)); panel.add(seatsPanel, BorderLayout.CENTER);
        JPanel foot = new JPanel(new FlowLayout(FlowLayout.CENTER, 18, 2)); foot.setOpaque(false);
        foot.add(label("● Available", 11, new Color(170, 181, 200))); foot.add(label("● Selected", 11, MINT)); foot.add(label("● Sold", 11, new Color(100, 108, 126)));
        panel.add(foot, BorderLayout.SOUTH); return panel;
    }

    private JPanel buildCheckoutPanel() {
        JPanel panel = panel(); panel.setLayout(new BorderLayout(10, 12));
        JPanel upper = new JPanel(); upper.setOpaque(false); upper.setLayout(new BoxLayout(upper, BoxLayout.Y_AXIS));
        upper.add(label("Your evening", 20, Color.WHITE)); upper.add(Box.createVerticalStrut(15)); upper.add(label("FOOD COMBOS", 11, MINT));
        addCombo(upper, "popcorn", "🍿  Classic popcorn + 2 colas", 320);
        addCombo(upper, "nachos", "🥨  Nachos + cheese + 2 drinks", 390);
        addCombo(upper, "sweet", "🍫  Caramel popcorn + chocolate", 260);
        upper.add(Box.createVerticalStrut(15)); totalLabel.setForeground(Color.WHITE); totalLabel.setFont(new Font("SansSerif", Font.BOLD, 20)); upper.add(totalLabel);
        JButton bookButton = new JButton("HOLD SEATS & PAY"); bookButton.setBackground(RED); bookButton.setForeground(Color.WHITE); bookButton.setFocusPainted(false); bookButton.setFont(new Font("SansSerif", Font.BOLD, 13)); bookButton.setBorder(new EmptyBorder(13, 14, 13, 14));
        bookButton.addActionListener(e -> confirmBooking()); upper.add(Box.createVerticalStrut(12)); upper.add(bookButton);
        panel.add(upper, BorderLayout.NORTH);
        historyPanel.setOpaque(false); historyPanel.setLayout(new BoxLayout(historyPanel, BoxLayout.Y_AXIS));
        JScrollPane scroll = new JScrollPane(historyPanel); scroll.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(45, 55, 74)), "YOUR BOOKINGS", 0, 0, new Font("SansSerif", Font.BOLD, 11), MINT)); scroll.getViewport().setBackground(PANEL);
        panel.add(scroll, BorderLayout.CENTER); return panel;
    }

    private void addCombo(JPanel parent, String id, String text, int price) {
        JCheckBox box = new JCheckBox(text + "  ·  ₹" + price); box.setOpaque(false); box.setForeground(new Color(226, 230, 238)); box.setFont(new Font("SansSerif", Font.PLAIN, 12)); box.addActionListener(e -> updateTotal());
        comboBoxes.put(id, box); parent.add(box);
    }

    private void refreshMovies() {
        moviePicker.removeAllItems(); catalog.movies().forEach(moviePicker::addItem); refreshShows();
    }

    private void refreshShows() {
        showPicker.removeAllItems(); Movie movie = (Movie) moviePicker.getSelectedItem();
        if (movie != null) catalog.showsFor(movie).forEach(showPicker::addItem);
        selectedSeats.clear(); refreshSeatGrid(); updateTotal();
    }

    private void refreshSeatGrid() {
        seatsPanel.removeAll(); seatButtons.clear(); Show show = (Show) showPicker.getSelectedItem();
        if (show == null) { seatsPanel.revalidate(); seatsPanel.repaint(); return; }
        Map<String, SeatState> state = bookings.inventoryFor(show).snapshot();
        for (char row='A'; row<='G'; row++) for (int number=1; number<=12; number++) {
            String id = SeatInventory.label(row, number); SeatState seatState = state.get(id);
            JButton button = new JButton(Integer.toString(number)); button.setMargin(new Insets(2, 2, 2, 2)); button.setFont(new Font("SansSerif", Font.PLAIN, 10));
            boolean unavailable = seatState instanceof SeatState.Sold || seatState instanceof SeatState.Held;
            button.setEnabled(!unavailable); button.setBackground(unavailable ? new Color(45, 50, 64) : selectedSeats.contains(id) ? MINT : new Color(31, 38, 54));
            button.setForeground(selectedSeats.contains(id) ? BG : unavailable ? new Color(105, 112, 127) : Color.WHITE);
            button.setToolTipText(id + " · " + SeatCategory.forRow(row).label());
            button.addActionListener(e -> { if (selectedSeats.contains(id)) selectedSeats.remove(id); else if (selectedSeats.size() < 6) selectedSeats.add(id); else { JOptionPane.showMessageDialog(null, "You can choose up to 6 seats."); return; } refreshSeatGrid(); updateTotal(); });
            seatButtons.put(id, button); seatsPanel.add(button);
        }
        seatsPanel.revalidate(); seatsPanel.repaint();
    }

    private void updateTotal() {
        Show show = (Show) showPicker.getSelectedItem(); if (show == null) { totalLabel.setText("Total: ₹0"); return; }
        DynamicPricingStrategy pricing = new DemandPricingStrategy(); Money amount = Money.zero();
        for (String seat : selectedSeats) amount = amount.plus(pricing.price(show, SeatCategory.forRow(seat.charAt(0))));
        for (Map.Entry<String, JCheckBox> entry : comboBoxes.entrySet()) if (entry.getValue().isSelected()) {
            int price = switch (entry.getKey()) { case "popcorn" -> 320; case "nachos" -> 390; default -> 260; }; amount = amount.plus(new Money(price));
        }
        if (!selectedSeats.isEmpty()) amount = amount.plus(new Money(Math.round(amount.rupees() * 0.035)));
        totalLabel.setText("Total: " + amount.formatted());
    }

    private void confirmBooking() {
        Show show = (Show) showPicker.getSelectedItem();
        if (show == null || selectedSeats.isEmpty()) { JOptionPane.showMessageDialog(null, "Choose a show and at least one available seat."); return; }
        Set<String> selectedCombos = new HashSet<>(); comboBoxes.forEach((id, box) -> { if (box.isSelected()) selectedCombos.add(id); });
        try {
            Booking booking = bookings.book(show, new ArrayList<>(selectedSeats), selectedCombos);
            JPanel card = panel(); card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS)); card.setBorder(new EmptyBorder(12, 12, 12, 12));
            card.add(label("✓  " + booking.show().movie().title(), 15, MINT));
            card.add(label(booking.show().time() + " · " + booking.show().cinema(), 11, Color.WHITE));
            card.add(label("Seats " + String.join(", ", booking.seats()), 11, Color.WHITE));
            card.add(label(booking.orderDescription(), 10, new Color(170, 181, 200)));
            card.add(label(booking.total().formatted() + " · " + booking.ticketId(), 11, Color.WHITE));
            card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140)); historyPanel.add(card); historyPanel.add(Box.createVerticalStrut(8));
            selectedSeats.clear(); comboBoxes.values().forEach(box -> box.setSelected(false)); refreshSeatGrid(); updateTotal(); historyPanel.revalidate(); historyPanel.repaint();
        } catch (IllegalStateException error) { JOptionPane.showMessageDialog(null, error.getMessage(), "Booking not completed", JOptionPane.WARNING_MESSAGE); refreshSeatGrid(); }
    }

    private static JPanel panel() { JPanel panel = new JPanel(); panel.setBackground(PANEL); panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(38, 46, 63)), new EmptyBorder(18, 18, 18, 18))); return panel; }
    private static JLabel label(String text, int size, Color color) { JLabel label = new JLabel(text); label.setFont(new Font("SansSerif", Font.PLAIN, size)); label.setForeground(color); return label; }
}
