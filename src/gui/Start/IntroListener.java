package gui.Start;

/**
 * Lets the start and slot screens tell the window what the player did.
 */
public interface IntroListener {

    /** START was pressed: show the slot selection. */
    void onStart();

    /** Back was pressed on the slot selection: show the start screen. */
    void onBack();

    /** A slot (1, 2 or 3) was chosen: open the main menu. */
    void onSlotChosen(int slot);
}