/**
 * Das controller-Paket dient als Schnittstelle zwischen Interaktionen des Benutzers,
 * Spiellogik und Netzwerkkommunikation via Client/Server Implementierung. Es ist ein wichtiger Baustein für die
 * problemlose Funktion der Anwendung und übernimmt die zentrale Rolle bei der Steuerung und Verwaltung
 *  des gesamten Anwendungsablaufs.
 * <p>
 * Der Controller holt sich Daten aus dem Model und gibt sie an die View weiter. So lässt sich also sagen, dass der
 * Controller entscheidet WAS für den Endnutzer zu zeichnen ist.
 * <p>
 * Der Controller kennt sowohl die View als auch das Model und wird auch von der View gekannt, deshalb hat die
 * Controller-Klasse auch ein Objekt von IModel und IView.
 * <p>
 * So kann beispielsweise das Paket view ausgetauscht werden, das neue Paket müsste dann nur noch das View-Interface
 * implementieren und könnte aber trotzdem noch denselben Controller verwenden.
 *  <p>
 * @since 1.0
 * @author Ricardo Erdmann
 * @version 1.0
 */
package controller;