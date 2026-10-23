package gui.DriversMenu;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.Locale;
import javax.swing.JPanel;

/**
 * Large profile card: the portrait fills the card and fades into the
 * dark background, with the car number at the top and the name and team
 * at the bottom.
 */
class Portraitcard extends JPanel {

    private static final long serialVersionUID = 1L;

    private BufferedImage portrait;
    private String name = "SELECT A DRIVER";
    private String team = "";
    private int number = -1;
    private Color accent = DriverStyle.RED;

    Portraitcard() {
        setOpaque(false);
        setMinimumSize(new Dimension(250, 300));
        setPreferredSize(new Dimension(430, 540));
    }

    void setDriver(BufferedImage image, String name, String team,
            int number, Color accent) {
        this.portrait = image;
        this.name = name == null ? "" : name;
        this.team = team == null ? "" : team;
        this.number = number;
        this.accent = accent;
        repaint();
    }

    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        try {
            DriverStyle.enableQuality(g);
            int w = getWidth() - 1;
            int h = getHeight() - 1;
            if (w <= 0 || h <= 0) {
                return;
            }

            // Everything inside the card is clipped to rounded corners.
            g.setClip(new RoundRectangle2D.Float(0, 0, w, h, 24, 24));

            g.setColor(DriverStyle.CARD);
            g.fillRect(0, 0, w + 1, h + 1);

            paintPicture(g, w, h);
            paintFade(g, w, h);
            paintNumber(g, w);

            // Team-coloured stripe on the left.
            g.setColor(accent);
            g.fillRect(0, 0, 5, h + 1);

            g.setClip(null);

            // Border.
            g.setColor(DriverStyle.LINE);
            g.drawRoundRect(0, 0, w, h, 24, 24);

            paintNames(g, w, h);
        } finally {
            g.dispose();
        }
    }

    /** Portrait filling the card (top-aligned so faces stay visible). */
    private void paintPicture(Graphics2D g, int w, int h) {
        if (portrait != null) {
            double scale = Math.max((double) (w + 1) / portrait.getWidth(),
                    (double) (h + 1) / portrait.getHeight());
            int dw = (int) Math.round(portrait.getWidth() * scale);
            int dh = (int) Math.round(portrait.getHeight() * scale);
            g.drawImage(portrait, (w + 1 - dw) / 2, 0, dw, dh, null);
        } else {
            g.setPaint(new GradientPaint(0, 0,
                    DriverStyle.blend(DriverStyle.CARD, accent, 0.40),
                    0, h, DriverStyle.CARD));
            g.fillRect(0, 0, w + 1, h + 1);

            g.setFont(DriverStyle.font(Font.BOLD, 13));
            g.setColor(DriverStyle.MUTED);
            String message = "NO PORTRAIT FOUND";
            FontMetrics fm = g.getFontMetrics();
            g.drawString(message, (w - fm.stringWidth(message)) / 2, h / 3);
        }
    }

    /** Fades the bottom of the picture into the card colour. */
    private void paintFade(Graphics2D g, int w, int h) {
        Color card = DriverStyle.CARD;
        g.setPaint(new GradientPaint(
                0, h * 0.40f,
                new Color(card.getRed(), card.getGreen(), card.getBlue(), 0),
                0, h * 0.82f,
                card));
        g.fillRect(0, 0, w + 1, h + 1);
    }

    /** Big car number in the top-right corner. */
    private void paintNumber(Graphics2D g, int w) {
        if (number < 0) {
            return;
        }
        String text = Integer.toString(number);
        g.setFont(DriverStyle.font(Font.BOLD | Font.ITALIC, 80));
        g.setColor(new Color(accent.getRed(), accent.getGreen(),
                accent.getBlue(), 210));
        FontMetrics fm = g.getFontMetrics();
        g.drawString(text, w - 28 - fm.stringWidth(text), 100);
    }

    /** First name, big last name and team, from the bottom up. */
    private void paintNames(Graphics2D g, int w, int h) {
        int space = name.lastIndexOf(' ');
        String first = space < 0 ? "" : name.substring(0, space);
        String last = (space < 0 ? name : name.substring(space + 1))
                .toUpperCase(Locale.ROOT);

        int x = 28;

        g.setFont(DriverStyle.font(Font.PLAIN, 16));
        g.setColor(DriverStyle.MUTED);
        g.drawString(first, x, h - 96);

        // Shrink the last name until it fits the card.
        int size = 36;
        FontMetrics fm;
        do {
            g.setFont(DriverStyle.font(Font.BOLD, size));
            fm = g.getFontMetrics();
            size -= 2;
        } while (fm.stringWidth(last) > w - 60 && size > 16);
        g.setColor(Color.WHITE);
        g.drawString(last, x, h - 56);

        // Team with a coloured dot.
        if (team.length() > 0) {
            g.setColor(accent);
            g.fillOval(x, h - 36, 10, 10);
            g.setFont(DriverStyle.font(Font.PLAIN, 14));
            g.setColor(DriverStyle.MUTED);
            g.drawString(team, x + 18, h - 26);
        }
    }
}
