package model;

import java.io.Serializable;

/**
 * Die Move-Klasse dient als Verwaltung von Spielzügen und ermöglicht das
 * Validieren und Ausführen von Zügen sowohl für normale Spielsteine als auch für
 * Damen. Zusätzlich bietet sie eine Funktion zur Überprüfung des Spielendes via der isGameOver Methode.
 */
public class Move implements Serializable {
    private IModel.Position start, target;
    //Variable für die Überprüfung ob das Spiel bereits vorbei ist
    private boolean isGameOver = false;
    // Das ist ein counter für die Züge, ist die Anzahl der Züge mod 2 = 0 dann steht dies für Spieler 1, wenn % 2 = 1 dann für Spieler 2
    private int moves = 0;

    /**
     * Diese Methode gibt die Startposition eines Zuges zurück
     * @return
     */
    public IModel.Position getStart() {
        return start;
    }

    /**
     * Diese Methode gibt die Zielposition eines Zuges zurück
     * @return
     */
    public IModel.Position getTarget() {
        return target;
    }

    //Ein Zug besteht immer aus einem Start und einem Ziel

    /**
     * Dies ist der Konstruktor der Move Klasse. Ein Move (Zug) besteht hier immer aus einem Start (Feld wo man sich
     * gerade befindet) und einem Ziel (Feld wo man hin möchte).
     * @param start
     * @param target
     */
    public Move(IModel.Position start, IModel.Position target) {
        this.start = start;
        this.target = target;
    }

    /**
     * Diese Methode gibt die aktuelle Anzahl an bereits gemachten Zügen zurück
     * @return
     */
    public int getMoves() {
        return moves;
    }


    //Methode, die für die eigentliche Ausführung eines Zuges zuständig ist

    /**
     * Diese Methode ist der Dreh- und Angelpunkt des Spiels, denn hier kommen alle Methoden, die einen Zug validieren zusammen
     * und sorgen für die eigentliche Ausführung eines Zuges.
     * <p>
     * Diese Methode überprüft zuerst, ob um welchen Spieler es sich handelt. ist der Wert des Spielers welcher reinkommt
     * eine 2 oder -2, so wird dieser dann entweder in eine 1 oder -1 umgewandelt. Nach der Umwandlung geht es weiter mit
     * der Prüfung of ein valider Dame Zug gemacht werden kann, innerhalb von dieser Überprüfung wird dann nochmal
     * überprüft, ob es sich um eine Dame des ersten oder zweiten Spielers handelt und dann wird der Zug ausgeführt.
     * Kurz darunter findet man die Überprüfung, ob die Dame jemanden schlagen kann, falls ja so wird der Schlag aus-
     * geführt, ansonsten passiert nichts
     * <p>
     * Wenn es sich bei eingehendem Spielerwert weder um eine 2 noch eine -2 handelt, dann weiß man, dass es sich um
     * normale Spielsteine handelt. Dort wird ebenfalls wieder geprüft, ob der gewünschte Zug valide ist. Innerhalb
     * von dieser Überprüfung wird dann wiederrum geprüft, ob es sich um Spieler 1 oder Spieler 2 (-1) handelt. Dann
     * wird der Zug ausgeführt, nach jedem Zug eines normalen Spielsteins wird auch immer mitgeprüft, ob dieser Spielstein
     * sich nun zu einer Dame verwandeln kann.
     * Kurz darunter findet man auch wieder eine Überprüfung, ob der Spielstein die Möglichkeit hat einen anderen zu schlagen.
     * falls ja wird die Methode für das Schlagen der normalen Spielsteine ausgeführt.
     * <p>
     * Egal ob normaler Spielstein oder Dame, nach jedem Zug wird die Anzahl an Zügen hochgezählt.
     * @param board
     * @param player
     * @param start
     * @param target
     */
    public void makeMove(int[][] board, int player, IModel.Position start, IModel.Position target) {

        //Überprüfung, ob es sich beim zu bewegenden Spielstein um eine Dame oder einen normalen Spielstein handelt
        if(player ==2 || player == -2){

            //Falls es eine Dame (2 Mega Schiggy) ist, soll sie auf den Spieler 1 zurückzuführen sein
            if(player == 2){
                player = 1;
                //Falls es eine Dame (-2 Mega Bisasam) ist, soll sie auf den Spieler 2 (-1) zurückzuführen sein
            } else if(player == -2){
                player = -1;
            }

            //Überprüfung, ob der gewünschte Dame-Zug valide ist
            if (isValidQueenMove(board, player, start, target)) {
                //Überprüfung, ob es sich um Spieler 1 handelt und ob das Brett an der gedrückten Stelle eine Dame des ersten Spielers ist
                if (moves % 2 == 0 && board[start.x()][start.y()] == 2) {
                    board[target.x()][target.y()] = 2;
                    board[start.x()][start.y()] = 0;
                    //Nach dem Zug wird dieser inkrementiert
                    increaseMoves();
                }
                //Überprüfung, ob es sich um Spieler 2 handelt und ob das Brett an der gedrückten Stelle eine Dame des zweiten Spielers ist
                if(moves % 2 == 1 && board[start.x()][start.y()] == -2){
                    board[target.x()][target.y()] = -2;
                    board[start.x()][start.y()] = 0;
                    //Nach dem Zug wird dieser inkrementiert
                    increaseMoves();
                }
            }
            //Überprüfung, ob die Dame die Möglichkeit einen gegnerischen Spielstein zu schlagen
            if(canQueenCapture(board,player,start,target)){
                queenCapture(board,start,target);
            }

        }else{

            //Überprüfung, ob der gewünschte Zug valide ist
            if (isValidMove(board, player, start, target)) {
                //Überprüfung, ob es sich um Spieler 1 und einen normalen Spielstein dieses Spielers handelt
                if (moves % 2 == 0 && board[start.x()][start.y()] == 1) {
                    board[target.x()][target.y()] = 1;
                    board[start.x()][start.y()] = 0;
                    //Überprüfung, ob ein normaler Spielstein zu einer Dame werden kann
                    promoteToQueen(board);
                    increaseMoves();
                }
                //Überprüfung, ob es sich um Spieler 2 und einen normalen Spielstein dieses Spielers handelt
                if(moves % 2 == 1 && board[start.x()][start.y()] == -1){
                    board[target.x()][target.y()] = -1;
                    board[start.x()][start.y()] = 0;
                    //Überprüfung, ob ein normaler Spielstein zu einer Dame werden kann
                    promoteToQueen(board);
                    increaseMoves();
                }
            }

            //Überprüfung, ob ein normaler Spielstein die Möglichkeit zum Schlagen hat
            if (canCapture(board,start, target)) {
                capture(board,start, target);
            }
        }
    }


    private void increaseMoves(){
        moves++;
    }

    /**
     * Diese Methode prüft, ob das Spiel vorbei ist, indem sie mit zwei for-loops über das Spielbrett iteriert und jedes
     * Mal, wenn ein Spielstein (normal oder Dame) gefunden wird einen Counter hochzählt. Am Ende wird geprüft, ob einer
     * der beider Counter gleich 0 ist, ist dies der Fall bedeutet es, dass einer der beiden Spieler keine Steine mehr
     * hat und somit sein Gegner gewinnt.
     * @param board
     * @return
     */
    public boolean isGameOver(int[][] board) {
        int cntP1 = 0;
        int cntP2 = 0;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if(board[i][j] == 1 || board[i][j] == 2){
                    cntP1++;
                }else if(board[i][j] == -1 || board[i][j] == -2){
                    cntP2++;
                }
            }
        }
        if(cntP1 == 0 || cntP2 == 0){
            isGameOver = true;
        }

        return isGameOver;
    }


    //Methode zur Überprüfung, ob ein Feld leer ist
    private boolean fieldIsEmpty(int[][] board, IModel.Position target) {
        return board[target.x()][target.y()] == 0;
    }


    //Methode zur Überprüfung, ob Startfeld und ein Zielfeld benachbart sind
    private boolean fieldIsNeighbor(IModel.Position start, IModel.Position target) {
        //Math.abs nimmt sich den Betrag einer Rechnung, sodass nicht mit negativen Werten gearbeitet wird, weil sonst beim Schlagen Spielsteine einfach übersprungen werden.
        int differenceX = Math.abs(start.x() - target.x());
        int differenceY = Math.abs(start.y() - target.y());

        // Überprüfung, ob Startfeld und Zielfeld eine maximale Positionsdifferenz von 1, bzgl. x und y Koordinaten, zueinander haben
        if (differenceX <= 1 && differenceY <= 1) {
            //Überprüfung, ob Start- und Zielfeld beide die schwarzen Felder sind
            if (fieldIsBlack(start) && fieldIsBlack(target)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }


    }

    //Methode zur Überprüfung, ob ein Feld ein dunkles ist
    private boolean fieldIsBlack(IModel.Position target) {
        return (target.x() + target.y()) % 2 == 1;
    }

    //Methode zur Validierung eines Zugs
    private boolean isValidMove(int[][] board, int player, IModel.Position start, IModel.Position target) {
        if (fieldIsEmpty(board, target) && fieldIsNeighbor(start, target)) {
            if (player == 1 && target.y() < start.y()) {
                return true; // Spieler 1 kann sich nur das Brett hoch bewegen
            } else if (player == -1 && target.y() > start.y()) {
                return true; // Spieler -1 kann sich nur das Brett runterbewegen
            }
        }
        return false;
    }

    //Methode, welche entscheidet WANN geschlagen werden darf
    private boolean canCapture(int[][] board, IModel.Position start, IModel.Position target) {
        //Ist das Zielfeld schwarz?
        if (fieldIsBlack(target)) {
            //Ist das Zielfeld leer?
            if (fieldIsEmpty(board,target)) {
                //Berechnung des Feldes zwischen Start- und Zielfeld
                int betweenX = calcBetweenX(start, target);
                int betweenY = calcBetweenY(start, target);
                //Ist das kalkulierte Feld zwischen Startfeld und Zielfeld auch wirklich ein diagonaler Nachbar von Start? (Differenzberechnung)
                if (Math.abs(betweenX - start.x()) == 1 && Math.abs(betweenY - start.y()) == 1) {
                    //Überprüfung, ob die Reihe des Zielfeldes auch wirklich kleiner ist als der Start (P1 darf immer nur hoch)
                    if (moves % 2 == 0 && target.y() < start.y()) {
                        //Liegt auf dem Zwischenfeld auch ein Gegner (Dame oder normaler Spielstein) ? (notwendig zum Schlagen)
                        if (board[betweenX][betweenY] == -1 || board[betweenX][betweenY] == -2) {
                            return true;
                        }
                        //Überprüfung, ob die Reihe des Zielfeldes auch wirklich größer ist als der Start (P2 darf immer nur runter)
                    } else if (moves % 2 == 1 && target.y() > start.y()) {
                        //Liegt auf dem Zwischenfeld auch ein Gegner (Dame oder normaler Spielstein) ? (notwendig zum Schlagen)
                        if (board[betweenX][betweenY] == 1 || board[betweenX][betweenY] == 2) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }



    //Methode, welche entscheidet WIE geschlagen wird
    private void capture(int[][] board,IModel.Position start, IModel.Position target) {
        //Überprüfung, ob Spieler 1 gerade schlagen will
        if (moves % 2 == 0) {
            //Eigentliches Schlagen
            board[start.x()][start.y()] = 0;
            board[target.x()][target.y()] = 1;
            //Überprüfung, ob auf dem Feld zwischen Start- und Zielfeld ein gegnerischer Spielstein liegt
            if (board[calcBetweenX(start, target)][calcBetweenY(start, target)] == -1 || board[calcBetweenX(start, target)][calcBetweenY(start, target)] == -2) {
                //Wenn ja, wird er geschlagen
                board[calcBetweenX(start, target)][calcBetweenY(start, target)] = 0;
                //Überprüfung, ob man durch die Zielposition nach dem Schlagen eine Dame bekommt
                promoteToQueen(board);
            }
            //Überprüfung, ob Spieler 2 gerade schlagen will
        } else if (moves % 2 == 1) {
            //Eigentliches Schlagen
            board[start.x()][start.y()] = 0;
            board[target.x()][target.y()] = -1;
            //Überprüfung, ob auf dem Feld zwischen Start- und Zielfeld ein gegnerischer Spielstein liegt
            if (board[calcBetweenX(start, target)][calcBetweenY(start, target)] == 1 || board[calcBetweenX(start, target)][calcBetweenY(start, target)] == 2) {
                //Wenn ja, wird er geschlagen
                board[calcBetweenX(start, target)][calcBetweenY(start, target)] = 0;
                //Überprüfung, ob man durch die Zielposition nach dem Schlagen eine Dame bekommt
                promoteToQueen(board);
            }
        }
        //Auch ein Schlagen ist ein Zug, deshalb wird auch hier der counter für die Züge erhöht
        increaseMoves();
    }



    /**
     * Diese Methode bekommt eine Start- und Zielposition und berechnet mithilfe der x Koordinaten dieser Positionen, die
     * x Koordinate des Feldes welches zwischen diesen beiden eingegebenen Positionen liegt.
     * @param start
     * @param target
     * @return
     */
    public int calcBetweenX(IModel.Position start, IModel.Position target) {
        //Mit Modulo wird sichergestellt, dass die Rechnung glatt aufgeht
        if ((start.x() + target.x()) % 2 == 0) {
            return (start.x() + target.x()) / 2;
        } else {
           //Falls die Berechnung, nicht glatt aufgeht
            return -1;
        }
    }

    /**
     * Diese Methode bekommt eine Start- und Zielposition und berechnet mithilfe der y Koordinaten dieser Positionen, die
     * y Koordinate des Feldes welches zwischen diesen beiden eingegebenen Positionen liegt.
     * @param start
     * @param target
     * @return
     */
    public int calcBetweenY(IModel.Position start, IModel.Position target) {
        //Mit Modulo wird sichergestellt, dass die Rechnung glatt aufgeht
        if ((start.y() + target.y()) % 2 == 0) {
            return (start.y() + target.y()) / 2;
        } else {
            //Falls die Berechnung, nicht glatt aufgeht
            return -1;
        }
    }

    //Methode zur Promotion eines normalen Spielsteins zur Dame
    private void promoteToQueen(int[][] board) {
        //Iteration über das gesamte Spielbrett
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                //Überprüfung, ob sich in der ersten Reihe des Bretts ein Spielstein des ersten Spielers befindet
                if (j < 1 && board[i][j] == 1) {
                    //Wenn ja, wandle ihn zu einer Dame um
                    board[i][j] = 2;
                }
                //Überprüfung, ob sich in der letzten Reihe des Bretts ein Spielstein des zweiten Spielers befindet
                if (j >= 8 - 1 && board[i][j] == -1) {
                    //Wenn ja, wandle ihn zu einer Dame um
                    board[i][j] = -2;
                }
            }
        }
    }

    //Methode zur Überprüfung, ob der Damen-Zug valide ist
    private boolean isValidQueenMove(int[][] board, int player, IModel.Position start, IModel.Position target) {
        //Überprüfung, ob das Zielfeld leer ist und ob Start- und Zielfeld benachbart sind
        if (fieldIsEmpty(board, target) && fieldIsNeighbor(start, target)) {
            if (player == 1) {
                return true;
            } else if (player == -1) {
                return true;
            }
        }
        return false;
    }



    //Methode, welche entscheidet WANN eine Dame andere Spielsteine schlagen darf
    private boolean canQueenCapture(int[][] board, int player, IModel.Position start, IModel.Position target) {
        //Überprüfung, ob das Zielfeld ein dunkles ist
        if (fieldIsBlack(target)) {
            //Überprüfung, ob das Zielfeld leer ist
            if (fieldIsEmpty(board,target)) {
                //Berechnung des Feldes zwischen Start- und Zielfeld
                int betweenX = calcBetweenX(start, target);
                int betweenY = calcBetweenY(start, target);
                //Ist das kalkulierte Feld zwischen Startfeld und Zielfeld auch wirklich ein diagonaler Nachbar von Start? (Differenzberechnung)
                if (Math.abs(betweenX - start.x()) == 1 && Math.abs(betweenY - start.y()) == 1) {
                    //Überprüfung, ob die Reihe des Zielfeldes auch wirklich kleiner ist als der Start (P1 darf immer nur hoch)
                    if (player == 1) {
                        //Liegt auf dem Zwischenfeld auch ein Gegner (Dame oder normaler Spielstein) ? (notwendig zum Schlagen)
                        if (board[betweenX][betweenY] == -1 || board[betweenX][betweenY] == -2 ) {
                            return true;
                        }
                        //Überprüfung, ob die Reihe des Zielfeldes auch wirklich größer ist als der Start (P2 darf immer nur runter)
                    } else if (player == -1) {
                        //Liegt auf dem Zwischenfeld auch ein Gegner (Dame oder normaler Spielstein) ? (notwendig zum Schlagen)
                        if (board[betweenX][betweenY] == 1 || board[betweenX][betweenY] == 2) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    //Methode, welche entscheidet WIE eine Dame schlägt
    private void queenCapture(int[][] board,IModel.Position start, IModel.Position target){
        if (moves % 2 == 0) {
            //Eigentliches Schlagen
            board[start.x()][start.y()] = 0;
            board[target.x()][target.y()] = 2;
            //Überprüfung, ob auf dem Feld zwischen Start- und Zielfeld ein gegnerischer Spielstein liegt
            if (board[calcBetweenX(start, target)][calcBetweenY(start, target)] == -1 || board[calcBetweenX(start, target)][calcBetweenY(start, target)] == -2) {
                //Wenn ja, wird er geschlagen
                board[calcBetweenX(start, target)][calcBetweenY(start, target)] = 0;
            }
        } else if (moves % 2 == 1) {
            //Eigentliches Schlagen
            board[start.x()][start.y()] = 0;
            board[target.x()][target.y()] = -2;
            //Überprüfung, ob auf dem Feld zwischen Start- und Zielfeld ein gegnerischer Spielstein liegt
            if (board[calcBetweenX(start, target)][calcBetweenY(start, target)] == 1 || board[calcBetweenX(start, target)][calcBetweenY(start, target)] == 2) {
                //Wenn ja, wird er geschlagen
                board[calcBetweenX(start, target)][calcBetweenY(start, target)] = 0;
            }
        }
        //Auch ein Schlagen ist ein Zug, deshalb wird auch hier der counter für die Züge erhöht
        increaseMoves();
    }








}