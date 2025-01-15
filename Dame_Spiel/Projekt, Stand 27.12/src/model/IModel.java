package model;

import java.io.Serializable;

/**
 * Dieses Interface definiert eine Reihe an Methoden, die für die Verwaltung des Spiel- bretts und zustandes verwendet werden.
 * @author Ricardo Erdmann
 */
public interface IModel {
    /**
     * Diese Methode wird dazu verwendet um den Spielstand im Terminal oder in der JShell auszugeben, dadurch kann man
     * bereits nur mit der Logik des Spiels spielen, ohne irgendeine GUI zu benötigen oder sonstiges.
     * Als Rückgabe wird das Brett samt Spielern als String geliefert.
     * @return
     */
    String toString();

    /**
     * Diese Methode gibt die Anzahl der Zeilen auf dem Schachbrett zurück, hier ist diese Anzahl auf 8 festgelegt.
     * @return
     */
    int getRows();

    /**
     * Diese Methode gibt die Anzahl an Spalten auf dem Schachbrett zurück, welche ebenfalls auf 8 festgelegt ist
     * @return
     */
    int getCols();

    /**
     * Diese Methode gibt die Größe eines Spielfeldes zurück, welches berechnet wird, indem man die Breite oder die Höhe
     * durch die Anzahl an Reihen oder Spalten teilt
     * @return
     */
    int getBoxDiam();

    /**
     * Diese Methode gibt das 2D-Array zurück welches in diesem Fall das Schachbrett repräsentiert.
     * eine 0 in diesem Array bedeutet, dass das Feld leer ist.
     * eine 1 in diesem Array bedeutet, dass Spieler 1 dort ein normales Steinchen hat.
     * eine -1 in diesem Array bedeutet, dass Spieler 2 dort ein normales Steinchen hat.
     * eine 2 in diesem Array bedeutet, dass Spieler 1 dort eine Dame hat.
     * eine -2 in diesem Array bedeutet, dass Spieler 2 dort eine Dame hat.
     * @return
     */
    int[][] getBoard();

    /**
     * Diese Methode gibt ein Objekt der Klasse Move zurück, womit man über der 'model' Instanz immer auf die Methoden
     * dieser Move Klasse Zugriff hat
     * @return
     */
    Move getMove();

    /**
     * Diese Methode erstellt auf dem Spielbrett die Ausgangsstellung für beide Spieler, sprich 12 Steinchen des ersten
     * Spielers auf die dunklen Felder der letzten drei Reihen und 12 Steinchen des zweiten Spielers auf die dunklen
     * Felder der ersten drei Reihen. Alle anderen Felder werden auf 0 (leer) gesetzt.
     */
    void startNewGame();

    /**
     * Dieser record, ist lediglich dazu da um die Arbeit auf dem Spielbrett zu erleichtern, indem Koordinaten bzw.
     * Positionen als Tupel auf der x- und y-Achse ausgegeben werden.
     * @param x
     * @param y
     */
    record Position(int x, int y) implements Serializable {}
}
