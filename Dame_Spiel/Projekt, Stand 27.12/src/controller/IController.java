package controller;

import model.IModel;

/**
 * Dieses Interface stellt eine Reihe von Methoden zur Verfügung, um die Wechselwirkung zwischen dem Model,
 * der GUI und der Spiellogik zu lenken und zu synchronisieren.
 */
public interface IController {
    /**
     * Diese Methode steuert die Aktualisierung der GUI abhängig vom derzeitigen Spielstand.
     * Je nachdem, ob es START, PLAYING oder GAME_OVER ist, wird der view immer was andres zum Zeichnen
     * vorgegeben. Ebenso wird ein String "startGame" an den Thread gesendet, wenn der Spielstand auf PLAYING ist,
     * dies ist das Signal für beide Anwendungen, dass das Spiel begonnen hat.
     */
    void nextFrame();

    /**
     * Diese Methode holt sich die Anzahl an Reihen des Bretts aus der Model
     * @return
     */
    int getRows();

    /**
     * Diese Methode holt sich die Anzahl an Spalten des Bretts aus der Model
     * @return
     */
    int getCols();

    /**
     * Diese Methode holt sich die Größe eines Feldes aus der Model
     * @return
     */
    int getBoxDiam();

    /**
     * Diese Methode dient dazu, das vom Benutzer ausgewählte Feld zu erfassen. Dieses Feld wird dann
     * in der handleMouseInput Methode verarbeitet.
     * @return
     */
    IModel.Position getSelectedPos();

    /**
     * Diese Methode holt sich den boolean Wert der isGameOver Methode aus der Model und je nachdem, ob dieser
     * wahr ist, wird der Spielzustand (GameState) auf GAME_OVER geändert. Wenn dieser Zustand eingetroffen ist
     * diese Information auch hier dem Thread gesendet, sodass in beiden Anwendung dann GameOver geschaltet wird.
     */
    void checkGameOver();

    /**
     * Diese Methode gibt lediglich die selektierte Position des Benutzers zurück, diese Methode ist wichtig um zu
     * entscheiden wie man gewissen Sachen später zeichnet.
     * @return
     */
    IModel.Position getSelectedPosition();

    /**
     * Diese Methode ist dazu da um den Spielstand auf den gewünschten Stand zu setzten, je nachdem was man gerade machen
     * möchte. In meinem Fall verwende ich diesen Setter um mir die Arbeit in der Client/Server Implementierung zu erleichtern.
     * @param state
     */
    void setState(GameState state);

    /**
     * Diese Methode dient dazu um in der View differenzieren zu können, wann zum Beispiel ein Client verbunden ist oder
     * ob eine Anwendung ein Server oder ein Client ist.
     * @return
     */
    ClientServerThread getThread();

    /**
     * Diese Methode wird dazu verwendet, um die vom Benutzer selektierte Position auf dem Spielbrett zurückzusetzen.
     * Dies ist deshalb nützlich, da man so zum Beispeil verhindern kann, dass man die Spielsteine seines Gegners auswählen
     * kann.
     * @param selectedPos
     */
    void setSelectedPos(IModel.Position selectedPos);
}
