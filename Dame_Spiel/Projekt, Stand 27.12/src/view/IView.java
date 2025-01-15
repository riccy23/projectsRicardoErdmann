package view;

/**
 * Dieses Interface verfügt über Reihe von verschiedenen Methoden, die für das Zeichnen des Spiels bzw. die Darstellung
 * der GUI, essenziell sind.
 */
public interface IView {
    /**
     * Diese Methode ist für das Zeichnen des Schachbretts zuständig. Es iteriert mit 2 for-loops über die
     * Reihen und Spalten des Spielbretts, berechnet und zeichnet dabei helle und dunkle Felder.
     * @param cols
     * @param rows
     * @param boxDiam
     */
    void drawBoard(int cols, int rows, int boxDiam);

    /**
     * Diese Methode stellt den Startbildschirm bzw. das Menü des Spiels dar. Auf diesem befinden sich zwei Buttons, die
     * via ControlP5 gesetzt wurden. Der Button "Local" startet sofort ein Spiel wo man am gleichen Rechner zusammen spielen
     * kann. Der Button "Online" benötigt einen Client, der sich mit dem Server verbindet und so kann man dann theoretisch
     * von zwei verschiedenen Rechnern im selben Netzwert gemeinsam spielen.
     */
    void drawStartScreen();

    /**
     * Diese Methode stellt den Game Over Bildschirm dar. Dieser erscheint immer wenn einer der beiden Spieler keine
     * Spielsteine mehr und dadurch verloren hat.
     * @param activePlayer
     */
    void drawGameOver(int activePlayer);

    /**
     * Diese Methode gibt lediglich die X Koordinate des Maus-Cursors zurück
     * @return
     */
    float getMouseX();

    /**
     * Diese Methode gibt lediglich die Y Koordinate des Maus-Cursors zurück
     * @return
     */
    float getMouseY();

    /**
     * Diese Methode überprüft als allererstes, ob überhaupt ein Feld ausgewählt wurde, wenn keins ausgewählt wurde
     * passiert auch nichts. Dann werden die x und y Koordinaten des ausgewählten Feldes berechnet. Nachdem wird geprüft,
     * ob es einen Client gibt, wenn nicht, dann kann man ganz normal hintereinander Ziehen und die ausgewählten Felder
     * den ersten Spieler sind immer in einem Blauton und die des zweiten Spielers in einem Grünton gefärbt. Wenn es
     * jedoch einen verbundenen Client gibt, dann wird erstmal eine Trennung zwischen Server und Client vorgenommen.
     * Wenn es sich bei der Anwendung um einen Server handelt, dann kann nur Spieler 1 Felder auswählen und seine eigenen
     * Spielsteine bewegen, wenn die Anwendung jedoch ein Client ist, dann kann nur Spieler 2 Felder auswählen und seine
     * eigenen Spielsteine bewegen.
     * @param board
     * @param moves
     * @param boxDiam
     */
    void drawSelectedPos(int[][] board, int moves, int boxDiam);

    /**
     * Diese Methode ist dafür da um die Spielsteine zu zeichnen. Hat das Brett den Wert 1 so wird an dieser Stelle ein
     * normaler Spielstein des ersten Spielers gezeichnet, ist es -1 dann einer des zweiten Spielers. Ist es eine 2 dann
     * eine Dame des ersten Spielers, falls eine -2 dann eine Dame des zweiten Spielers.
     * @param board
     * @param cols
     * @param rows
     * @param boxDiam
     */
    void drawPieces(int[][] board, int cols, int rows, int boxDiam);

    /**
     * Diese Methode zeichnet lediglich eine visuelle Anzeige für den aktiven Spieler
     * @param activePlayer
     */
    void drawActivePlayer(int activePlayer);

    /**
     * Diese Methode ist dafür da um die Buttons von ControlP5 sichtbar oder unsichtbar zu machen, dies ist notwendig, da
     * man sonst das Spiel beginnen würde, aber die Buttons immernoch mitten auf dem Spielbrett zu sehen wären.
     * @param visible
     */
    void setButtonVisibility(boolean visible);

    /**
     * Diese Methode zeichnet nur einen Text, der für den Client als Information dient, dass auf den Spielstart seitens
     * des ersten Spielers gewartet werden soll
     */
    void drawWaitingForServerToStart();
}
