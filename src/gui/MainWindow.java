
package gui;

// ---------------------------------------------------------
// JAVA AWT IMPORTS
// ---------------------------------------------------------

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

// ---------------------------------------------------------
// FILE HANDLING
// ---------------------------------------------------------

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

// ---------------------------------------------------------
// SWING COMPONENTS
// ---------------------------------------------------------

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

// ---------------------------------------------------------
// SWING LISTENERS
// ---------------------------------------------------------

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.plaf.basic.BasicButtonUI;

// ---------------------------------------------------------
// PROJECT MODEL
// ---------------------------------------------------------

import model.SimulationData;


/**
 * Main window of the Formula One 2026 simulator.
 *
 * Features:
 * - F1 logo and application heading.
 * - Dark motorsport-inspired interface.
 * - Navigation tiles for drivers, teams, race simulation
 *   and championship standings.
 * - Separate screens managed with CardLayout.
 * - Hover and pressed effects for buttons.
 * - Back buttons for returning to the home screen.

 */
public class MainWindow extends JFrame {

    private static final long serialVersionUID = 1L;


    // ---------------------------------------------------------
    // COLOUR CONSTANTS
    // ---------------------------------------------------------

    // Background of the top navigation header.
    private static final Color HEADER_BG =
            new Color(21, 21, 21);

    // Main application background.
    private static final Color HOME_BG =
            new Color(15, 17, 22);

    // Background colour of the home screen tiles.
    private static final Color TILE_BG =
            new Color(27, 30, 37);

    // Formula One-inspired red accent.
    private static final Color F1_RED =
            new Color(225, 6, 0);

    // Subtitle colour.
    private static final Color SUBTITLE_GRAY =
            new Color(200, 200, 200);

    // Main text colour.
    private static final Color TEXT =
            new Color(245, 246, 248);

    // Secondary text colour.
    private static final Color MUTED =
            new Color(165, 170, 180);


    // ---------------------------------------------------------
    // SCREEN IDENTIFIERS
    // ---------------------------------------------------------

    // Name used to identify the home screen in CardLayout.
    private static final String HOME = "home";


    // ---------------------------------------------------------
    // MAIN LAYOUT COMPONENTS
    // ---------------------------------------------------------

    // CardLayout displays one application screen at a time.
    private final CardLayout cards =
            new CardLayout();

    // Container that holds all application screens.
    private final JPanel content =
            new JPanel(cards);


    // ---------------------------------------------------------
    // CONSTRUCTOR
    // ---------------------------------------------------------

    /**
     * Creates the main simulator window.
     */
    public MainWindow(SimulationData data) {

        // Set the title shown in the operating system window.
        super("F1 2026 Simulator");

        // -----------------------------------------------------
        // HOME SCREEN
        // -----------------------------------------------------

        // Build and register the home screen.
        content.add(buildHome(), HOME);

        // -----------------------------------------------------
        // APPLICATION SECTIONS
        // -----------------------------------------------------

        // Teams management screen.
        addSection(
                "teams",
                "Teams",
                new TeamsPanel());

        // Drivers database screen.
        addSection(
                "drivers",
                "Drivers",
                new DriversPanel(data));

        // Race simulation placeholder.
        // Replace this component with the actual race interface
        // when the simulation screen has been implemented.
        addSection(
                "race",
                "Simulate Race",
                placeholder(
                        "Simulate Race: choose circuit, run, see results"));

        // Championship standings placeholder.
        addSection(
                "standings",
                "Standings",
                placeholder(
                        "Standings: driver and team rankings"));

        // -----------------------------------------------------
        // FRAME CONFIGURATION
        // -----------------------------------------------------

        // Add the screen container to the main window.
        add(content);

        // Close the application when the window is closed.
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Set the initial window size.
        setSize(900, 650);

        // Prevent the window from becoming too small.
        setMinimumSize(new Dimension(700, 500));

        // Centre the window on the screen.
        setLocationRelativeTo(null);
    }


    // ---------------------------------------------------------
    // HOME SCREEN
    // ---------------------------------------------------------

    /**
     * Builds the home screen.
     *
     * The header is displayed at the top, and the navigation
     * tiles occupy the remaining space.
     */
    private JPanel buildHome() {

        JPanel home =
                new JPanel(new BorderLayout());

        home.setBackground(HOME_BG);

        // Logo and title appear at the top.
        home.add(
                buildHeader(),
                BorderLayout.NORTH);

        // Navigation tiles appear in the centre.
        home.add(
                buildTiles(),
                BorderLayout.CENTER);

        return home;
    }


    // ---------------------------------------------------------
    // HEADER
    // ---------------------------------------------------------

    /**
     * Creates the header containing the F1 logo,
     * application title and subtitle.
     */
    private JPanel buildHeader() {

        JPanel header = new JPanel();

        // Use BorderLayout to position the logo and heading.
        header.setLayout(
                new BorderLayout(24, 0));

        header.setBackground(HEADER_BG);

        // Add space around the contents of the header.
        header.setBorder(
                BorderFactory.createEmptyBorder(
                        18, 25, 18, 25));


        // -----------------------------------------------------
        // F1 LOGO
        // -----------------------------------------------------

        // Load the logo and scale it to fit the header.
        JLabel logo = new JLabel(
                loadLogoIcon(250, 64));

        logo.setToolTipText("Formula 1");


        // -----------------------------------------------------
        // TITLE AND SUBTITLE
        // -----------------------------------------------------

        JPanel heading = new JPanel();

        // Keep the heading transparent so the header background
        // remains visible behind it.
        heading.setOpaque(false);

        // Stack the title and subtitle vertically.
        heading.setLayout(
                new BoxLayout(
                        heading,
                        BoxLayout.Y_AXIS));


        // Main application title.
        JLabel title =
                new JLabel("2026 SIMULATOR");

        title.setFont(
                new Font(
                        "SansSerif",
                        Font.BOLD,
                        27));

        title.setForeground(TEXT);


        // Subtitle below the title.
        JLabel subtitle =
                new JLabel("Championship Management");

        subtitle.setFont(
                new Font(
                        "SansSerif",
                        Font.PLAIN,
                        17));

        subtitle.setForeground(SUBTITLE_GRAY);


        // Add the title and subtitle to the heading panel.
        heading.add(title);

        heading.add(
                Box.createVerticalStrut(6));

        heading.add(subtitle);


        // Place the logo on the left and heading in the centre.
        header.add(
                logo,
                BorderLayout.WEST);

        header.add(
                heading,
                BorderLayout.CENTER);

        return header;
    }


    // ---------------------------------------------------------
    // LOAD AND SCALE LOGO
    // ---------------------------------------------------------

    /**
     * Loads the F1 logo from the project's assets directory.
     * Expected file:
     * assets/f1-logo.png
     * The image is scaled proportionally to fit within the
     * supplied maximum width and height.
    
     */
    private ImageIcon loadLogoIcon(
            int maxWidth,
            int maxHeight) {

        // Locate the logo relative to the working directory.
        File logoFile =
                new File("assets/f1-logo.png");


        // Stop with a useful error if the logo does not exist.
        if (!logoFile.isFile()) {

            throw new IllegalStateException(
                    "F1 logo image not found: "
                            + logoFile.getAbsolutePath());
        }


        try {

            // Read the original image.
            BufferedImage source =
                    ImageIO.read(logoFile);


            // ImageIO returns null if the image format is unsupported.
            if (source == null) {

                throw new IllegalStateException(
                        "Unsupported F1 logo image: "
                                + logoFile.getAbsolutePath());
            }


            // -------------------------------------------------
            // CALCULATE IMAGE SIZE
            // -------------------------------------------------

            // Calculate a scale factor that fits both dimensions.
            // Using the same scale for width and height preserves
            // the original image proportions.
            double scale = Math.min(
                    (double) maxWidth / source.getWidth(),
                    (double) maxHeight / source.getHeight());


            // Calculate the scaled image dimensions.
            int width = Math.max(
                    1,
                    (int) Math.round(
                            source.getWidth() * scale));

            int height = Math.max(
                    1,
                    (int) Math.round(
                            source.getHeight() * scale));


            // -------------------------------------------------
            // CREATE SCALED IMAGE
            // -------------------------------------------------

            // ARGB supports transparent backgrounds.
            BufferedImage scaled =
                    new BufferedImage(
                            width,
                            height,
                            BufferedImage.TYPE_INT_ARGB);


            Graphics2D graphics =
                    scaled.createGraphics();

            try {

                // Improve image interpolation quality.
                graphics.setRenderingHint(
                        RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_BICUBIC);

                graphics.setRenderingHint(
                        RenderingHints.KEY_RENDERING,
                        RenderingHints.VALUE_RENDER_QUALITY);


                // Draw the source image at the new dimensions.
                graphics.drawImage(
                        source,
                        0,
                        0,
                        width,
                        height,
                        null);

            } finally {

                // Release graphics resources.
                graphics.dispose();
            }


            // Return the scaled image as a Swing icon.
            return new ImageIcon((Image) scaled);

        } catch (IOException exception) {

            // Convert image-reading errors into a clear
            // application initialization error.
            throw new IllegalStateException(
                    "Could not read F1 logo image: "
                            + logoFile.getAbsolutePath(),
                    exception);
        }
    }


    // ---------------------------------------------------------
    // HOME SCREEN NAVIGATION TILES
    // ---------------------------------------------------------

    
     // Creates the four main navigation tiles.
  
    private JPanel buildTiles() {

        // Create a grid with two rows and two columns.
        JPanel grid =
                new JPanel(
                        new GridLayout(2, 2, 15, 15));

        grid.setBackground(HOME_BG);

        // Add padding around the grid.
        grid.setBorder(
                BorderFactory.createEmptyBorder(
                        20, 20, 20, 20));


        // -----------------------------------------------------
        // DRIVERS TILE
        // -----------------------------------------------------

        grid.add(
                createTile(
                        "Drivers",
                        "Manage drivers",
                        "drivers",
                        new Color(225, 6, 0)));


        // -----------------------------------------------------
        // TEAMS TILE
        // -----------------------------------------------------

        grid.add(
                createTile(
                        "Teams",
                        "Manage constructors",
                        "teams",
                        new Color(0, 190, 170)));


        // -----------------------------------------------------
        // RACE SIMULATION TILE
        // -----------------------------------------------------

        grid.add(
                createTile(
                        "Simulate Race",
                        "Run a race",
                        "race",
                        new Color(255, 166, 55)));


        // -----------------------------------------------------
        // STANDINGS TILE
        // -----------------------------------------------------

        grid.add(
                createTile(
                        "Standings",
                        "View rankings",
                        "standings",
                        new Color(155, 115, 255)));


        return grid;
    }


    // ---------------------------------------------------------
    // CREATE A SINGLE NAVIGATION TILE
    // ---------------------------------------------------------

    /**
     * Creates a clickable home screen tile.
     *
     * Each tile includes:
     * - A coloured decorative circle.
     * - A title.
     * - A short description.
     * - A coloured border.
     * - Hover and pressed visual effects.
    
     */
    private JButton createTile(
            String title,
            String description,
            String sectionKey,
            Color accent) {


        // -----------------------------------------------------
        // CONVERT ACCENT COLOUR TO HTML FORMAT
        // -----------------------------------------------------

        // HTML labels allow different colours and font sizes
        // within a single button.
        String accentHex = String.format(
                "#%02X%02X%02X",
                Integer.valueOf(accent.getRed()),
                Integer.valueOf(accent.getGreen()),
                Integer.valueOf(accent.getBlue()));


        // -----------------------------------------------------
        // TILE TEXT
        // -----------------------------------------------------

        String html =
                "<html><div align='center'>"
                + "<font color='" + accentHex + "' size='+1'>"
                + "&#9679;</font><br>"
                + "<font color='" + accentHex + "' size='+2'><b>"
                + title + "</b></font><br>"
                + "<font color='#A5AAB4' size='+1'>"
                + description
                + "</font></div></html>";


        JButton tile =
                new JButton(html);


        // Use the basic UI delegate for a simpler appearance.
        tile.setUI(new BasicButtonUI());

        tile.setFont(
                new Font(
                        "SansSerif",
                        Font.PLAIN,
                        15));

        tile.setBackground(TILE_BG);
        tile.setForeground(TEXT);

        tile.setHorizontalAlignment(
                SwingConstants.CENTER);

        tile.setOpaque(true);
        tile.setBorderPainted(true);
        tile.setContentAreaFilled(true);
        tile.setFocusPainted(false);

        // Display the hand cursor when hovering over the tile.
        tile.setCursor(
                Cursor.getPredefinedCursor(
                        Cursor.HAND_CURSOR));


        // Set the initial border and padding.
        tile.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                blend(TILE_BG, accent, 0.45)),
                        BorderFactory.createEmptyBorder(
                                16, 16, 16, 16)));


        // Enable rollover state tracking.
        tile.setRolloverEnabled(true);


        // -----------------------------------------------------
        // TILE HOVER AND PRESSED EFFECTS
        // -----------------------------------------------------

        // Java 6 does not support lambda expressions.
        // An anonymous ChangeListener handles changes to the
        // button model instead.
        tile.getModel().addChangeListener(
                new ChangeListener() {

            /**
             * Updates the tile appearance when the button
             * is pressed, hovered over or released.
             *
             
             */
            public void stateChanged(ChangeEvent event) {

                ButtonModel model = tile.getModel();


                // Darker accent when the button is pressed.
                if (model.isPressed()) {

                    tile.setBackground(
                            blend(TILE_BG, accent, 0.35));

                    tile.setBorder(
                            BorderFactory.createCompoundBorder(
                                    BorderFactory.createLineBorder(
                                            accent),
                                    BorderFactory.createEmptyBorder(
                                            16, 16, 16, 16)));
                }

                // Lighter accent while the pointer hovers over it.
                else if (model.isRollover()) {

                    tile.setBackground(
                            blend(TILE_BG, accent, 0.2));

                    tile.setBorder(
                            BorderFactory.createCompoundBorder(
                                    BorderFactory.createLineBorder(
                                            blend(
                                                    accent,
                                                    Color.WHITE,
                                                    0.25)),
                                    BorderFactory.createEmptyBorder(
                                            16, 16, 16, 16)));
                }

                // Restore the default appearance.
                else {

                    tile.setBackground(TILE_BG);

                    tile.setBorder(
                            BorderFactory.createCompoundBorder(
                                    BorderFactory.createLineBorder(
                                            blend(
                                                    TILE_BG,
                                                    accent,
                                                    0.45)),
                                    BorderFactory.createEmptyBorder(
                                            16, 16, 16, 16)));
                }
            }
        });


        // -----------------------------------------------------
        // TILE CLICK ACTION
        // -----------------------------------------------------

        // Display the associated screen when the tile is clicked.
        tile.addActionListener(
                new ActionListener() {

            public void actionPerformed(ActionEvent event) {

                cards.show(content, sectionKey);
            }
        });


        return tile;
    }


    // ---------------------------------------------------------
    // BLEND TWO COLOURS
    // ---------------------------------------------------------

    /**
     * Mixes two colours to produce an intermediate colour.
     *
     * An amount of 0.0 returns the base colour.
     * An amount of 1.0 returns the accent colour.
    
     */
    private Color blend(
            Color base,
            Color accent,
            double amount) {

        // Keep the mixing amount within the valid range.
        if (amount < 0.0) {
            amount = 0.0;
        }

        if (amount > 1.0) {
            amount = 1.0;
        }


        // Calculate each RGB component separately.
        int red = (int) (
                base.getRed() * (1.0 - amount)
                + accent.getRed() * amount);

        int green = (int) (
                base.getGreen() * (1.0 - amount)
                + accent.getGreen() * amount);

        int blue = (int) (
                base.getBlue() * (1.0 - amount)
                + accent.getBlue() * amount);


        return new Color(red, green, blue);
    }


    // ---------------------------------------------------------
    // ADD APPLICATION SECTION
    // ---------------------------------------------------------

    /**
     * Creates a screen with a top navigation bar and a content
     * component.
     *
     * The navigation bar contains a Back button and the
     * section title.
    
     */
    private void addSection(
            String key,
            String title,
            JComponent body) {


        // -----------------------------------------------------
        // SECTION CONTAINER
        // -----------------------------------------------------

        JPanel section =
                new JPanel(
                        new BorderLayout());

        section.setBackground(HOME_BG);


        // -----------------------------------------------------
        // TOP NAVIGATION BAR
        // -----------------------------------------------------

        JPanel topBar =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.LEFT,
                                10,
                                8));

        topBar.setBackground(HEADER_BG);


        // -----------------------------------------------------
        // BACK BUTTON
        // -----------------------------------------------------

        JButton back =
                new JButton("\u2190 Back");

        back.setUI(new BasicButtonUI());

        back.setFont(
                new Font(
                        "SansSerif",
                        Font.BOLD,
                        13));

        back.setForeground(Color.WHITE);
        back.setBackground(F1_RED);

        back.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(255, 85, 78)),
                        BorderFactory.createEmptyBorder(
                                7, 13, 7, 13)));

        back.setOpaque(true);
        back.setBorderPainted(false);
        back.setFocusPainted(false);
        back.setContentAreaFilled(true);
        back.setRolloverEnabled(true);

        back.setCursor(
                Cursor.getPredefinedCursor(
                        Cursor.HAND_CURSOR));


        // -----------------------------------------------------
        // BACK BUTTON HOVER AND PRESSED EFFECTS
        // -----------------------------------------------------

        // Anonymous ChangeListener replaces the Java 8 lambda.
        back.getModel().addChangeListener(
                new ChangeListener() {

            
             // Updates the Back button colour.
             
             
            public void stateChanged(ChangeEvent event) {

                ButtonModel model = back.getModel();


                // Dark red while pressed.
                if (model.isPressed()) {

                    back.setBackground(
                            new Color(150, 4, 0));
                }

                // Slightly lighter red on hover.
                else if (model.isRollover()) {

                    back.setBackground(
                            new Color(190, 5, 0));
                }

                // Default red.
                else {

                    back.setBackground(F1_RED);
                }
            }
        });


        // Return to the home screen when Back is clicked.
        back.addActionListener(
                new ActionListener() {

            public void actionPerformed(ActionEvent event) {

                cards.show(content, HOME);
            }
        });


        // -----------------------------------------------------
        // SECTION TITLE
        // -----------------------------------------------------

        JLabel label =
                new JLabel(title);

        label.setFont(
                new Font(
                        "SansSerif",
                        Font.BOLD,
                        18));

        label.setForeground(Color.WHITE);


        // -----------------------------------------------------
        // ASSEMBLE SECTION
        // -----------------------------------------------------

        // Add the Back button and title to the top bar.
        topBar.add(back);
        topBar.add(label);

        // Position the navigation bar at the top.
        section.add(
                topBar,
                BorderLayout.NORTH);

        // Display the requested content below the navigation.
        section.add(
                body,
                BorderLayout.CENTER);


        // Register the section with the main CardLayout.
        content.add(section, key);
    }


    // ---------------------------------------------------------
    // PLACEHOLDER SCREENS
    // ---------------------------------------------------------

    /**
     * Creates a simple placeholder for a screen that has not
     * yet been fully implemented.
     *
     * This allows navigation to work while the actual screen
     * is being developed.
     *
     
     */
    private JComponent placeholder(String text) {

        JLabel label =
                new JLabel(
                        text,
                        SwingConstants.CENTER);

        label.setFont(
                new Font(
                        "SansSerif",
                        Font.PLAIN,
                        16));

        label.setForeground(MUTED);
        label.setOpaque(true);
        label.setBackground(HOME_BG);

        return label;
    }
}
