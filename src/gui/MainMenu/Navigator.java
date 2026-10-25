package gui.MainMenu;

/**
 * Lets the sidebar and the home cards ask the window to change screen,
 * without knowing anything about the window itself.
 */
interface Navigator {

    /** Shows the screen of this section (MainWindow implements it). */
    void showSection(Section section);
}