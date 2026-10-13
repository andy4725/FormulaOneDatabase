
package gui;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;

import model.Driver;
import model.SimulationData;
import model.Team;

/**
 * Displays the Formula 1 driver database.
 *
 * The panel contains:
 * - A list of available drivers.
 * - A portrait card for the selected driver.
 * - Driver statistics, including team, number, experience,
 *   racecraft, pace and awareness.
 *
 * Uses Java 6-compatible syntax and APIs.
 */
public class DriversPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    // ---------------------------------------------------------
    // COLOURS
    // ---------------------------------------------------------

    // Main background colour of the interface.
    private static final Color BACKGROUND =
            new Color(15, 17, 22);

    // Background used for cards and the driver list.
    private static final Color CARD_COLOR =
            new Color(27, 30, 37);

    // Formula 1-inspired red accent.
    private static final Color RED =
            new Color(225, 6, 0);

    // Main text colour.
    private static final Color TEXT =
            new Color(245, 246, 248);

    // Secondary text colour used for labels.
    private static final Color MUTED =
            new Color(165, 170, 180);


    // ---------------------------------------------------------
    // DRIVER LIST AND INFORMATION LABELS
    // ---------------------------------------------------------

    // Stores the drivers displayed in the JList.
    private final DefaultListModel<Driver> driverModel =
            new DefaultListModel<Driver>();

    // Displays the available drivers.
    private final JList<Driver> driverList =
            new JList<Driver>(driverModel);

    // Custom component that paints the driver's portrait card.
    private final PortraitCard portraitCard =
            new PortraitCard();

    // Labels updated whenever the selected driver changes.
    private final JLabel nameLabel =
            new JLabel("Select a driver");

    private final JLabel numberLabel =
            new JLabel("—");

    private final JLabel teamLabel =
            new JLabel("—");

    private final JLabel experienceLabel =
            new JLabel("—");

    private final JLabel racecraftLabel =
            new JLabel("—");

    private final JLabel paceLabel =
            new JLabel("—");

    private final JLabel awarenessLabel =
            new JLabel("—");


    // ---------------------------------------------------------
    // FILES AND TEAM INFORMATION
    // ---------------------------------------------------------

    // Root directory of the project.
    private final File projectRoot;

    // Directory containing driver portrait images.
    private final File portraitDirectory;

    // Connects a team ID to its display name.
    private final Map<Integer, String> teamNames =
            new HashMap<Integer, String>();


    /**
     * Creates the driver database panel.
     *
     * @param data simulation data containing teams and drivers
     */
    public DriversPanel(SimulationData data) {

        // Find the project directory so image paths can be resolved.
        projectRoot = findProjectRoot();

        // Portraits are expected in the project's assets folder.
        portraitDirectory = new File(
                projectRoot, "assets/driver-portraits");

        // Store team names for quick lookup when displaying a driver.
        for (Team team : data.getTeams()) {
            teamNames.put(
                    Integer.valueOf(team.getTeamId()),
                    team.getTeamName());
        }

        // Set up the main layout and spacing.
        setLayout(new BorderLayout(18, 18));
        setBackground(BACKGROUND);
        setBorder(new EmptyBorder(16, 16, 16, 16));

        // Build the visual interface before loading the drivers.
        buildInterface();

        // Populate the list and select the first driver, if available.
        loadDrivers(data);
    }


    // ---------------------------------------------------------
    // MAIN INTERFACE
    // ---------------------------------------------------------

    /**
     * Creates the heading, driver list and details area.
     */
    private void buildInterface() {

        // Main page heading.
        JLabel heading =
                new JLabel("FORMULA 1  /  DRIVERS");

        heading.setForeground(TEXT);
        heading.setFont(
                new Font("SansSerif", Font.BOLD, 25));

        // Smaller subtitle below the heading.
        JLabel subtitle =
                new JLabel("DRIVER DATABASE");

        subtitle.setForeground(MUTED);
        subtitle.setFont(
                new Font("SansSerif", Font.PLAIN, 11));

        // Stack the heading and subtitle vertically.
        JPanel headingPanel = new JPanel();

        headingPanel.setOpaque(false);
        headingPanel.setLayout(
                new BoxLayout(headingPanel, BoxLayout.Y_AXIS));

        headingPanel.add(heading);
        headingPanel.add(Box.createVerticalStrut(5));
        headingPanel.add(subtitle);

        add(headingPanel, BorderLayout.NORTH);


        // -----------------------------------------------------
        // DRIVER LIST APPEARANCE
        // -----------------------------------------------------

        driverList.setBackground(CARD_COLOR);
        driverList.setForeground(TEXT);

        driverList.setSelectionBackground(
                new Color(65, 27, 32));

        driverList.setSelectionForeground(Color.WHITE);

        driverList.setFont(
                new Font("SansSerif", Font.PLAIN, 14));

        // Give every driver entry a consistent height.
        driverList.setFixedCellHeight(48);

        driverList.setBorder(
                new EmptyBorder(6, 6, 6, 6));


        // Customize how each driver appears in the list.
        // An anonymous class is used instead of a lambda
        // to maintain Java 6 compatibility.
        driverList.setCellRenderer(
                new DefaultListCellRenderer() {

                    private static final long serialVersionUID = 1L;

                    public Component getListCellRendererComponent(
                            JList list,
                            Object value,
                            int index,
                            boolean selected,
                            boolean focus) {

                        // Let Swing configure the basic list-cell state.
                        JLabel label = (JLabel)
                                super.getListCellRendererComponent(
                                        list,
                                        value,
                                        index,
                                        selected,
                                        focus);

                        Driver driver = (Driver) value;

                        // Show the driver's name instead of the object's
                        // default string representation.
                        label.setText(driver.getName());

                        label.setOpaque(true);
                        label.setBorder(
                                new EmptyBorder(10, 12, 10, 10));

                        // Highlight the selected driver in bold.
                        label.setFont(new Font(
                                "SansSerif",
                                selected ? Font.BOLD : Font.PLAIN,
                                13));

                        label.setForeground(
                                selected ? Color.WHITE : TEXT);

                        // Use a darker red background for selection.
                        label.setBackground(
                                selected
                                        ? new Color(75, 31, 36)
                                        : CARD_COLOR);

                        return label;
                    }
                });


        // A scroll pane allows the user to browse long driver lists.
        JScrollPane listScroll =
                new JScrollPane(driverList);

        listScroll.setBorder(
                BorderFactory.createLineBorder(
                        new Color(48, 51, 60)));

        listScroll.getViewport().setBackground(CARD_COLOR);

        listScroll.setPreferredSize(
                new Dimension(225, 450));

        JScrollBar verticalScrollBar =
                listScroll.getVerticalScrollBar();
        verticalScrollBar.setUnitIncrement(16);
        verticalScrollBar.setBorder(null);
        verticalScrollBar.setOpaque(false);
        verticalScrollBar.setUI(new BasicScrollBarUI() {

            @Override
            protected JButton createDecreaseButton(int orientation) {
                return createScrollBarButton();
            }

            @Override
            protected JButton createIncreaseButton(int orientation) {
                return createScrollBarButton();
            }

            @Override
            protected void configureScrollBarColors() {
                thumbColor = new Color(82, 86, 96);
                trackColor = CARD_COLOR;
            }

            @Override
            protected void paintTrack(
                    Graphics graphics,
                    JComponent component,
                    Rectangle bounds) {
                graphics.setColor(CARD_COLOR);
                graphics.fillRect(
                        bounds.x, bounds.y,
                        bounds.width, bounds.height);
            }

            @Override
            protected void paintThumb(
                    Graphics graphics,
                    JComponent component,
                    Rectangle bounds) {
                if (bounds.isEmpty() || !scrollbar.isEnabled()) {
                    return;
                }

                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(isDragging
                        ? RED
                        : isThumbRollover()
                                ? new Color(190, 20, 16)
                                : thumbColor);
                g.fillRoundRect(
                        bounds.x + 2,
                        bounds.y + 2,
                        bounds.width - 4,
                        bounds.height - 4,
                        8,
                        8);
                g.dispose();
            }

            private JButton createScrollBarButton() {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(0, 0));
                button.setMinimumSize(new Dimension(0, 0));
                button.setMaximumSize(new Dimension(0, 0));
                button.setFocusable(false);
                return button;
            }
        });
        verticalScrollBar.setPreferredSize(new Dimension(12, 0));


        // -----------------------------------------------------
        // DRIVER DETAILS AREA
        // -----------------------------------------------------

        JPanel detailsPanel =
                new JPanel(new BorderLayout(12, 12));

        detailsPanel.setBackground(BACKGROUND);

        // Preferred size guides the initial layout.
        // Minimum size allows the portrait card to shrink when needed.
        portraitCard.setPreferredSize(
                new Dimension(430, 540));

        portraitCard.setMinimumSize(
                new Dimension(250, 300));

        // Create the statistics displayed beneath the portrait.
        JPanel informationPanel =
                createInformationPanel();

        detailsPanel.add(
                portraitCard, BorderLayout.CENTER);

        detailsPanel.add(
                informationPanel, BorderLayout.SOUTH);


        // -----------------------------------------------------
        // RESIZABLE SPLIT PANE
        // -----------------------------------------------------

        // The divider lets the user adjust the width of the
        // driver list and the driver details area.
        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                listScroll,
                detailsPanel);

        splitPane.setBorder(null);
        splitPane.setDividerSize(6);

        // Initially give approximately 25% of the space to the list.
        splitPane.setResizeWeight(0.25);

        // Update the layout continuously while the divider moves.
        splitPane.setContinuousLayout(true);
        splitPane.setBackground(BACKGROUND);

        add(splitPane, BorderLayout.CENTER);


        // -----------------------------------------------------
        // DRIVER SELECTION
        // -----------------------------------------------------

        // Update the details whenever the selected list entry changes.
        driverList.addListSelectionListener(
                new javax.swing.event.ListSelectionListener() {

                    public void valueChanged(
                            javax.swing.event.ListSelectionEvent event) {

                        // Swing may fire multiple events during one
                        // selection change. Process only the final event.
                        if (!event.getValueIsAdjusting()) {

                            Driver selected =
                                    driverList.getSelectedValue();

                            if (selected != null) {
                                displayDriver(selected);
                            }
                        }
                    }
                });
    }


    // ---------------------------------------------------------
    // DRIVER STATISTICS PANEL
    // ---------------------------------------------------------

    /**
     * Creates the panel containing driver statistics.
     *
     * @return panel containing the driver's information
     */
    private JPanel createInformationPanel() {

        // Two columns arrange the statistics in a compact grid.
        JPanel panel =
                new JPanel(new GridLayout(0, 2, 12, 10));

        panel.setBackground(CARD_COLOR);

        // Add a subtle border and internal spacing.
        panel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(49, 52, 61)),
                        new EmptyBorder(14, 16, 14, 16)));

        // Apply consistent typography to all values.
        styleValue(nameLabel, 18);
        styleValue(numberLabel, 14);
        styleValue(teamLabel, 14);
        styleValue(experienceLabel, 14);
        styleValue(racecraftLabel, 14);
        styleValue(paceLabel, 14);
        styleValue(awarenessLabel, 14);

        // Add each statistic as a labelled item.
        addInfo(panel, "DRIVER", nameLabel);
        addInfo(panel, "CAR NUMBER", numberLabel);
        addInfo(panel, "TEAM", teamLabel);
        addInfo(panel, "EXPERIENCE", experienceLabel);
        addInfo(panel, "RACECRAFT", racecraftLabel);
        addInfo(panel, "PACE", paceLabel);
        addInfo(panel, "AWARENESS", awarenessLabel);

        return panel;
    }


    /**
     * Adds a title and value as one statistics item.
     *
     * @param panel panel that will contain the item
     * @param title descriptive title for the statistic
     * @param value label displaying the statistic value
     */
    private void addInfo(
            JPanel panel, String title, JLabel value) {

        JLabel label = new JLabel(title);

        label.setForeground(MUTED);
        label.setFont(
                new Font("SansSerif", Font.BOLD, 10));

        // Stack the title above its value.
        JPanel item = new JPanel();

        item.setOpaque(false);
        item.setLayout(
                new BoxLayout(item, BoxLayout.Y_AXIS));

        item.add(label);
        item.add(Box.createVerticalStrut(5));
        item.add(value);

        panel.add(item);
    }


    /**
     * Applies the standard style to a statistic value.
     *
     * @param label label to style
     * @param size font size
     */
    private void styleValue(JLabel label, int size) {

        label.setForeground(TEXT);

        label.setFont(
                new Font("SansSerif", Font.BOLD, size));
    }


    // ---------------------------------------------------------
    // DISPLAY SELECTED DRIVER
    // ---------------------------------------------------------

    /**
     * Updates the statistics and portrait for the selected driver.
     *
     * @param driver driver whose information should be displayed
     */
    private void displayDriver(Driver driver) {

        nameLabel.setText(driver.getName());

        numberLabel.setText(
                Integer.toString(driver.getCarNumber()));

        // Convert the driver's team ID into a readable team name.
        // If no name is available, display a dash.
        teamLabel.setText(
                emptyFallback(
                        teamNames.get(
                                Integer.valueOf(driver.getTeamId()))));

        experienceLabel.setText(
                Integer.toString(driver.getExperience()));

        racecraftLabel.setText(
                Integer.toString(driver.getRacecraft()));

        paceLabel.setText(
                Integer.toString(driver.getPace()));

        awarenessLabel.setText(
                Integer.toString(driver.getAwareness()));

        // Load the portrait and tell the custom component to repaint.
        portraitCard.setPortrait(
                loadPhoto(
                        driver.getName(),
                        driver.getPortraitPath()),
                driver.getName());
    }


    /**
     * Replaces null or blank text with a dash.
     *
     * @param value text to check
     * @return original text or a fallback dash
     */
    private String emptyFallback(String value) {

        return value == null || value.trim().length() == 0
                ? "—" : value;
    }


    // ---------------------------------------------------------
    // PORTRAIT LOADING
    // ---------------------------------------------------------

    /**
     * Loads a driver portrait.
     *
     * Search order:
     * 1. The path stored in the driver data.
     * 2. The same filename inside the portrait directory.
     * 3. A file whose normalized name matches the driver.
     *
     * JPG and PNG images are supported by standard ImageIO.
     * JPEG is also accepted.
     *
     * @param driverName driver's name
     * @param portraitPath path stored in the driver data
     * @return loaded image, or null if no usable image is found
     */
    private BufferedImage loadPhoto(
            String driverName, String portraitPath) {

        // -----------------------------------------------------
        // FIRST ATTEMPT: USE THE STORED PORTRAIT PATH
        // -----------------------------------------------------

        if (portraitPath != null
                && portraitPath.trim().length() > 0) {

            File storedFile = new File(portraitPath);

            // Resolve relative paths against the project root.
            // Absolute paths are used as they are.
            File projectFile;

            if (storedFile.isAbsolute()) {
                projectFile = storedFile;
            } else {
                projectFile = new File(
                        projectRoot, portraitPath);
            }

            // Also try the stored filename inside the portrait folder.
            File filenameFile = new File(
                    portraitDirectory,
                    storedFile.getName());

            // Avoid attempting to load the same file twice.
            File[] candidates;

            if (projectFile.equals(filenameFile)) {
                candidates = new File[] { projectFile };
            } else {
                candidates = new File[] {
                        projectFile,
                        filenameFile
                };
            }

            // Try each candidate until a readable image is found.
            for (int i = 0; i < candidates.length; i++) {

                File candidate = candidates[i];

                if (!candidate.isFile()) {
                    continue;
                }

                try {
                    BufferedImage image =
                            ImageIO.read(candidate);

                    // ImageIO returns null for unsupported image formats.
                    if (image != null) {
                        return image;
                    }

                } catch (IOException exception) {

                    // Continue to the next candidate if this file
                    // cannot be read.
                    System.err.println(
                            "Failed to read driver portrait: "
                                    + candidate);
                }
            }
        }


        // -----------------------------------------------------
        // SECOND ATTEMPT: SEARCH BY DRIVER NAME
        // -----------------------------------------------------

        // The stored path might be missing or outdated.
        // Normalize the name to allow matching despite spaces,
        // punctuation, capitalization and accented characters.
        String wanted = normalize(driverName);

        // The fallback search requires an existing directory.
        if (!portraitDirectory.isDirectory()) {
            System.err.println(
                    "Portrait directory not found: "
                            + portraitDirectory);

            return null;
        }

        File[] files = portraitDirectory.listFiles();

        if (files == null) {
            System.err.println(
                    "Cannot list portrait directory: "
                            + portraitDirectory);

            return null;
        }

        // If several matching images exist, retain the largest
        // image by pixel count as a simple quality preference.
        BufferedImage bestImage = null;
        long bestPixels = 0L;

        for (int i = 0; i < files.length; i++) {

            File file = files[i];

            if (!file.isFile()) {
                continue;
            }

            String filename = file.getName();

            // Separate the filename from its extension.
            int dot = filename.lastIndexOf('.');

            String base = dot < 0
                    ? filename
                    : filename.substring(0, dot);

            String ext = extensionOf(filename);

            // Standard Java 6 ImageIO supports common formats
            // such as PNG and JPEG. WebP requires an extra plugin.
            boolean supported =
                    ext.equals(".png")
                    || ext.equals(".jpg")
                    || ext.equals(".jpeg");

            // Ignore unsupported files and names that do not match.
            if (!supported
                    || !normalize(base).equals(wanted)) {
                continue;
            }

            try {
                BufferedImage image = ImageIO.read(file);

                if (image == null) {
                    continue;
                }

                // Use pixel count to compare matching image sizes.
                long pixels = (long) image.getWidth()
                        * (long) image.getHeight();

                if (pixels > bestPixels) {
                    bestPixels = pixels;
                    bestImage = image;
                }

            } catch (IOException exception) {

                // Ignore this candidate and continue searching.
                System.err.println(
                        "Failed to read portrait: " + file);
            }
        }

        // Return null if no matching image could be loaded.
        // PortraitCard will display its missing-image message.
        if (bestImage == null) {
            System.err.println(
                    "No readable portrait for " + driverName);
        }

        return bestImage;
    }


    /**
     * Extracts a lowercase file extension, including the dot.
     *
     * Example: "driver.JPG" returns ".jpg".
     *
     * @param filename filename to inspect
     * @return lowercase extension, or an empty string
     */
    private String extensionOf(String filename) {

        int dot = filename.lastIndexOf('.');

        return dot < 0
                ? ""
                : filename.substring(dot).toLowerCase(
                        Locale.ROOT);
    }


    /**
     * Converts a name into a consistent form for file matching.
     *
     * Examples:
     * "Max Verstappen" becomes "maxverstappen".
     * Accented letters are converted to their basic equivalents.
     *
     * @param text name to normalize
     * @return normalized text containing lowercase letters and digits
     */
    private String normalize(String text) {

        if (text == null) {
            return "";
        }

        // Separate accented letters from their accent marks.
        String normalized = Normalizer.normalize(
                text, Normalizer.Form.NFD);

        // Remove accent marks, convert to lowercase, and remove
        // spaces and punctuation.
        return normalized
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]", "");
    }


    // ---------------------------------------------------------
    // LOAD DRIVERS
    // ---------------------------------------------------------

    /**
     * Adds all drivers to the list and selects the first one.
     *
     * @param data simulation data containing the drivers
     */
    private void loadDrivers(SimulationData data) {

        for (Driver driver : data.getDrivers()) {
            driverModel.addElement(driver);
        }

        // Selecting the first entry triggers the selection listener,
        // which displays its statistics and portrait.
        if (!driverModel.isEmpty()) {
            driverList.setSelectedIndex(0);
        }
    }


    // ---------------------------------------------------------
    // PROJECT DIRECTORY
    // ---------------------------------------------------------

    /**
     * Searches the current directory and its parents for the project root.
     *
     * A directory is considered the root when it contains both
     * "src" and "assets" folders.
     *
     * @return detected project root, or the current directory
     */
    private File findProjectRoot() {

        File current = new File("").getAbsoluteFile();

        File file = current;

        while (file != null) {

            File sourceDirectory = new File(file, "src");
            File assetsDirectory = new File(file, "assets");

            if (sourceDirectory.isDirectory()
                    && assetsDirectory.isDirectory()) {
                return file;
            }

            // Move to the parent directory and try again.
            file = file.getParentFile();
        }

        // Fall back to the current directory if the project root
        // could not be detected.
        return current;
    }


    // ---------------------------------------------------------
    // CUSTOM PORTRAIT CARD
    // ---------------------------------------------------------

    /**
     * Custom Swing component that draws the selected driver's portrait.
     *
     * The card includes:
     * - Rounded corners.
     * - A dark background and soft shadow.
     * - A red accent line.
     * - A centered portrait.
     * - The driver's name below the image.
     *
     * Painting directly gives more control over the appearance than
     * using a standard JLabel with an ImageIcon.
     */
    private static class PortraitCard extends JPanel {

        private static final long serialVersionUID = 1L;

        // Image currently displayed by the card.
        private BufferedImage portrait;

        // Text displayed beneath the image.
        private String driverName = "DRIVER PROFILE";

        // Layout constants make the card easier to adjust.
        private static final int CARD_INSET = 12;
        private static final int IMAGE_PADDING = 20;
        private static final int NAME_AREA_HEIGHT = 55;


        /**
         * Creates an empty portrait card.
         */
        PortraitCard() {

            // The background and all card elements are painted manually.
            setOpaque(false);

            setPreferredSize(new Dimension(430, 540));
            setMinimumSize(new Dimension(250, 300));
        }


        /**
         * Sets the portrait and name, then requests a repaint.
         *
         * @param image driver's image, or null if unavailable
         * @param name driver's name
         */
        void setPortrait(BufferedImage image, String name) {

            portrait = image;

            driverName = name == null
                    ? "DRIVER PROFILE" : name;

            // Swing will call paintComponent when the component
            // needs to be redrawn.
            repaint();
        }


        /**
         * Resizes an image to the requested dimensions.
         *
         * The caller calculates dimensions that preserve the image's
         * aspect ratio. This method therefore does not distort the
         * portrait by independently stretching its width and height.
        
         */
        private BufferedImage resizeHighQuality(
                BufferedImage source,
                int targetWidth,
                int targetHeight) {

            if (source == null
                    || targetWidth < 1
                    || targetHeight < 1) {
                return source;
            }

            // Avoid allocating a new image when the dimensions
            // already match.
            if (source.getWidth() == targetWidth
                    && source.getHeight() == targetHeight) {
                return source;
            }

            // ARGB preserves transparency if the source contains it.
            BufferedImage resized = new BufferedImage(
                    targetWidth,
                    targetHeight,
                    BufferedImage.TYPE_INT_ARGB);

            Graphics2D g = resized.createGraphics();

            try {
                // Improve the quality of image scaling.
                g.setRenderingHint(
                        RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_BICUBIC);

                g.setRenderingHint(
                        RenderingHints.KEY_RENDERING,
                        RenderingHints.VALUE_RENDER_QUALITY);

                g.setRenderingHint(
                        RenderingHints.KEY_ALPHA_INTERPOLATION,
                        RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);

                // Draw the original image at the new dimensions.
                g.drawImage(
                        source,
                        0, 0,
                        targetWidth,
                        targetHeight,
                        null);

            } finally {
                // Always release graphics resources.
                g.dispose();
            }

            return resized;
        }


        /**
         * Paints the card background, portrait and driver name.
         */
        protected void paintComponent(Graphics graphics) {

            super.paintComponent(graphics);

            // Use a separate graphics context so our rendering changes
            // do not affect the original context supplied by Swing.
            Graphics2D g = (Graphics2D) graphics.create();

            try {
                g.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                int x = CARD_INSET;
                int y = CARD_INSET;

                int cardWidth = w - CARD_INSET * 2;
                int cardHeight = h - CARD_INSET * 2;

                // Do not attempt to draw if the component is too small.
                if (cardWidth <= 0 || cardHeight <= 0) {
                    return;
                }


                // -------------------------------------------------
                // CARD SHADOW
                // -------------------------------------------------

                // Several faint shapes create a simple layered shadow.
                for (int i = 5; i >= 1; i--) {

                    g.setColor(new Color(0, 0, 0, 12));

                    g.fillRoundRect(
                            x + i,
                            y + i * 2,
                            cardWidth,
                            cardHeight,
                            28, 28);
                }


                // -------------------------------------------------
                // CARD BACKGROUND AND RED ACCENT
                // -------------------------------------------------

                g.setColor(CARD_COLOR);

                g.fillRoundRect(
                        x, y,
                        cardWidth, cardHeight,
                        28, 28);

                // Red vertical line along the left side of the card.
                g.setColor(RED);

                g.fillRoundRect(
                        x,
                        y + 25,
                        5,
                        Math.max(0, cardHeight - 50),
                        5, 5);


                // -------------------------------------------------
                // PORTRAIT AREA
                // -------------------------------------------------

                int imageAreaX = x + IMAGE_PADDING;
                int imageAreaY = y + IMAGE_PADDING;

                int imageAreaWidth =
                        cardWidth - IMAGE_PADDING * 2;

                int imageAreaHeight =
                        cardHeight - IMAGE_PADDING * 2
                                - NAME_AREA_HEIGHT;

                if (portrait != null
                        && portrait.getWidth() > 0
                        && portrait.getHeight() > 0
                        && imageAreaWidth > 0
                        && imageAreaHeight > 0) {

                    // Calculate the scale that fits the entire image
                    // inside the available area without cropping it.
                    double scale = Math.min(
                            (double) imageAreaWidth
                                    / portrait.getWidth(),
                            (double) imageAreaHeight
                                    / portrait.getHeight());

                    int imageWidth = Math.max(
                            1,
                            (int) Math.round(
                                    portrait.getWidth() * scale));

                    int imageHeight = Math.max(
                            1,
                            (int) Math.round(
                                    portrait.getHeight() * scale));

                    // Center the portrait horizontally and vertically.
                    int imageX = imageAreaX
                            + (imageAreaWidth - imageWidth) / 2;

                    int imageY = imageAreaY
                            + (imageAreaHeight - imageHeight) / 2;

                    // Create a resized image using high-quality rendering.
                    BufferedImage displayImage =
                            resizeHighQuality(
                                    portrait,
                                    imageWidth,
                                    imageHeight);


                    // Draw a small shadow behind the portrait.
                    g.setColor(new Color(0, 0, 0, 90));

                    g.fillRoundRect(
                            imageX + 3,
                            imageY + 3,
                            imageWidth,
                            imageHeight,
                            14, 14);


                    // Save the existing clipping area so it can be restored.
                    Shape oldClip = g.getClip();

                    // Restrict drawing to a rounded rectangle so the
                    // portrait corners appear rounded.
                    g.setClip(new RoundRectangle2D.Float(
                            imageX,
                            imageY,
                            imageWidth,
                            imageHeight,
                            14, 14));

                    g.setColor(CARD_COLOR);

                    g.fillRect(
                            imageX,
                            imageY,
                            imageWidth,
                            imageHeight);

                    // Draw the resized portrait.
                    g.setRenderingHint(
                            RenderingHints.KEY_INTERPOLATION,
                            RenderingHints.VALUE_INTERPOLATION_BICUBIC);

                    g.setRenderingHint(
                            RenderingHints.KEY_RENDERING,
                            RenderingHints.VALUE_RENDER_QUALITY);

                    g.drawImage(
                            displayImage,
                            imageX,
                            imageY,
                            null);

                    // Restore the previous clipping area to avoid
                    // affecting the text and other card elements.
                    g.setClip(oldClip);


                    // Thin border around the portrait.
                    g.setColor(new Color(255, 255, 255, 70));

                    g.drawRoundRect(
                            imageX,
                            imageY,
                            imageWidth - 1,
                            imageHeight - 1,
                            14, 14);

                } else {

                    // Show a placeholder if no portrait could be loaded.
                    g.setColor(MUTED);

                    g.setFont(
                            new Font("SansSerif", Font.BOLD, 13));

                    String message = "NO PORTRAIT FOUND";

                    FontMetrics fm = g.getFontMetrics();

                    int messageX = x + (cardWidth
                            - fm.stringWidth(message)) / 2;

                    int messageY = y + cardHeight / 2;

                    g.drawString(
                            message,
                            messageX,
                            messageY);
                }


                // -------------------------------------------------
                // DRIVER NAME
                // -------------------------------------------------

                g.setColor(Color.WHITE);

                g.setFont(
                        new Font("SansSerif", Font.BOLD, 15));

                // Uppercase text matches the style of the card.
                String originalName =
                        driverName.toUpperCase(Locale.ROOT);

                String displayName = originalName;

                FontMetrics fm = g.getFontMetrics();

                int maxTextWidth = cardWidth - 36;

                // Shorten long names until the name and ellipsis fit.
                while (fm.stringWidth(displayName + "...")
                        > maxTextWidth
                        && displayName.length() > 3) {

                    displayName = displayName.substring(
                            0, displayName.length() - 1);
                }

                // Add an ellipsis only when the original name was shortened.
                if (!displayName.equals(originalName)) {
                    displayName = displayName.trim() + "...";
                }

                // Center the name horizontally beneath the portrait.
                int textX = x + (cardWidth
                        - fm.stringWidth(displayName)) / 2;

                int textY = y + cardHeight - 20;

                g.drawString(displayName, textX, textY);

            } finally {
                // Release the copied graphics context.
                g.dispose();
            }
        }
    }
}
