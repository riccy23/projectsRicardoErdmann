/**
 * Dieses package bietet eine Klasse und ein Interface zur GUI-Darstellung. Es ermöglicht das Zeichnen des Spielbretts, Start- und
 * Game-Over-Bildschirmen, Hervorheben ausgewählter Positionen, visuelles Anzeigen des aktiven Spielers. Zudem unterstützt es
 * Mauskoordinatenabfragen und die Steuerung der Button-Sichtbarkeit.
 * <p>
 * Die Klassen innerhalb dieses Pakets sind die einzigen, die auf die Processing Bibliothek zugreifen dürfen.
 * <p>
 * Ist für den Start des Spiels in der Main Klasse via der runSketch() zuständig. Die View ist beliebig austauschbar,
 * man kann hier auch seine eigene View schreiben und trotzdem noch denselben Controller und Model verwenden.
 * <p>
 * Die View Klasse entscheidet WIE etwas gezeichnet werden soll, ebenso darf die View unter keinen Umständen das Model
 * kennen bzw. ein Modelobjekt besitzen, dies würde ansonsten das MVC-Prinzip verletzen.
 *  <p>
 * @since 1.0
 * @author Ricardo Erdmann
 * @version 1.0
 */
package view;