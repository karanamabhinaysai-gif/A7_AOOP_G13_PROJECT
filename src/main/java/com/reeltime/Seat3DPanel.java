package com.reeltime;

import javax.swing.JPanel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/** Clickable perspective seat map rendered with Java2D (no graphics dependencies). */
public final class Seat3DPanel extends JPanel {
    private static final Color MINT = new Color(99, 214, 177);
    private Map<String, SeatState> inventory = Map.of();
    private Set<String> selected = Set.of();
    private final Map<String, Rectangle> hitAreas = new HashMap<>();
    private final Consumer<String> seatSelected;
    private boolean perspective = true;

    public Seat3DPanel(Consumer<String> seatSelected) {
        this.seatSelected = seatSelected;
        setOpaque(false);
        setPreferredSize(new Dimension(530, 470));
        setToolTipText("");
        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent event) {
                for (Map.Entry<String, Rectangle> entry : hitAreas.entrySet()) {
                    if (entry.getValue().contains(event.getPoint()) && inventory.get(entry.getKey()) instanceof SeatState.Free) {
                        Seat3DPanel.this.seatSelected.accept(entry.getKey());
                        return;
                    }
                }
            }
        });
        addMouseMotionListener(new MouseAdapter() {
            @Override public void mouseMoved(MouseEvent event) {
                for (Map.Entry<String, Rectangle> entry : hitAreas.entrySet()) if (entry.getValue().contains(event.getPoint())) {
                    char row = entry.getKey().charAt(0);
                    setToolTipText(entry.getKey() + " · " + SeatCategory.forRow(row).label());
                    return;
                }
                setToolTipText("Click an available seat to select it");
            }
        });
    }

    public void setSeatData(Map<String, SeatState> inventory, Set<String> selected) {
        this.inventory = Map.copyOf(inventory);
        this.selected = Set.copyOf(selected);
        repaint();
    }

    public void setPerspective(boolean perspective) {
        this.perspective = perspective;
        repaint();
    }

    public boolean isPerspective() { return perspective; }

    @Override protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        hitAreas.clear();
        int width = getWidth(), height = getHeight();
        g.setPaint(new GradientPaint(0, 0, new Color(29, 39, 58), 0, height, new Color(16, 20, 30)));
        g.fillRoundRect(2, 2, width - 4, height - 4, 20, 20);
        drawScreen(g, width);
        for (int row = 0; row < 7; row++) drawRow(g, width, row);
        g.dispose();
    }

    private void drawScreen(Graphics2D g, int width) {
        int screenWidth = Math.min(300, width - 120), left = (width - screenWidth) / 2;
        Path2D screen = new Path2D.Double();
        screen.moveTo(left + 16, 28); screen.lineTo(left + screenWidth - 16, 28);
        screen.lineTo(left + screenWidth, 53); screen.lineTo(left, 53); screen.closePath();
        g.setPaint(new GradientPaint(left, 25, new Color(169, 186, 255), left, 55, new Color(83, 102, 171)));
        g.fill(screen); g.setColor(new Color(188, 201, 255, 150)); g.draw(screen);
        g.setFont(new Font("SansSerif", Font.BOLD, 9)); g.setColor(new Color(21, 26, 42));
        FontMetrics metrics = g.getFontMetrics(); String title = "SCREEN";
        g.drawString(title, (width - metrics.stringWidth(title)) / 2, 44);
    }

    private void drawRow(Graphics2D g, int width, int row) {
        double scale = perspective ? 0.72 + row * 0.065 : 0.88;
        int seatW = (int) (22 * scale), seatH = (int) (30 * scale), step = (int) (31 * scale);
        int aisle = (int) (24 * scale), rowGap = perspective ? 44 : 39;
        int fullWidth = step * 11 + seatW + aisle;
        int left = (width - fullWidth) / 2;
        int y = 76 + row * rowGap + (perspective ? row * 2 : 0);
        g.setFont(new Font("SansSerif", Font.BOLD, 9)); g.setColor(new Color(132, 144, 166));
        g.drawString(String.valueOf((char) ('A' + row)), Math.max(6, left - 19), y + seatH - 4);
        for (int col = 1; col <= 12; col++) {
            int split = col > 6 ? aisle : 0;
            int x = left + (col - 1) * step + split;
            String id = "" + (char) ('A' + row) + col;
            boolean isSelected = selected.contains(id);
            boolean unavailable = inventory.get(id) instanceof SeatState.Sold || inventory.get(id) instanceof SeatState.Held;
            Rectangle hit = new Rectangle(x - 2, y - 2, seatW + 5, seatH + 6);
            hitAreas.put(id, hit);
            drawSeat(g, id, x, y, seatW, seatH, isSelected, unavailable, row);
        }
    }

    private void drawSeat(Graphics2D g, String id, int x, int y, int w, int h, boolean selected, boolean unavailable, int row) {
        Color base = selected ? MINT : unavailable ? new Color(43, 48, 64)
            : row < 2 ? new Color(160, 126, 78) : row < 4 ? new Color(87, 104, 167) : new Color(70, 88, 112);
        Color shade = selected ? new Color(39, 143, 112) : unavailable ? new Color(39, 43, 56) : base.darker();
        int backH = (int) (h * 0.60);
        g.setColor(shade); g.fillRoundRect(x + 2, y + 2, w - 4, backH, 8, 8);
        g.setPaint(new GradientPaint(x, y, base.brighter(), x, y + backH, base));
        g.fillRoundRect(x + 2, y, w - 4, backH, 8, 8);
        Path2D cushion = new Path2D.Double();
        cushion.moveTo(x, y + backH - 1); cushion.lineTo(x + w, y + backH - 1);
        cushion.lineTo(x + w - 2, y + h - 3); cushion.lineTo(x + 2, y + h - 3); cushion.closePath();
        g.setColor(shade); g.fill(cushion);
        g.setPaint(new GradientPaint(x, y + backH, base.brighter(), x, y + h, base)); g.fill(cushion);
        g.setColor(unavailable ? new Color(99, 106, 124) : selected ? new Color(11, 47, 36) : Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, Math.max(7, (int) (8 * (w / 22.0)))));
        FontMetrics fm = g.getFontMetrics(); g.drawString(id.substring(1), x + (w - fm.stringWidth(id.substring(1))) / 2, y + h - 5);
        if (!unavailable) { g.setColor(new Color(255, 255, 255, 55)); g.drawLine(x + 5, y + 4, x + w - 5, y + 4); }
    }
}

