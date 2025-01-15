package model;

import java.io.Serializable;

/**
 * Die Model-Klasse repräsentiert den Zustand des Spielbretts und ermöglicht mit ihren Methoden, das Ausgeben des Spiels
 * in der Konsole oder in der JShell und das Erstellen der Ausgangsstellung eines Damespiels.
 */
public class Model implements IModel, Serializable {
    private int width, height;
    private int rows = 8, cols = 8, boxDiam;
    private int[][] board = new int[cols][rows];

    private Move move = new Move(new Position(0,0),new Position(0,0));

    /**
     {@inheritDoc}
     */
    public int getRows() {
        return rows;
    }

    /**
     {@inheritDoc}
     */
    public int getCols() {
        return cols;
    }

    /**
     {@inheritDoc}
     */
    public int getBoxDiam() {
        return boxDiam;
    }

    /**
     {@inheritDoc}
     */
    public int[][] getBoard() {
        return board;
    }

    /**
     {@inheritDoc}
     */
    public Move getMove() {
        return move;
    }


    /**
     * Dies ist der Konstruktor der Model, beim Aufrufen des Konstruktors wird auch gleichzeitig startNewGame()
     * und gameBoard() aufgerufen ebenso benötigt der Konstruktor eine Breite und eine Höhe für das Spielfenster
     *  width, height.
     */
    public Model(int width, int height) {
        this.width = width;
        this.height = height;
        gameBoard();
        startNewGame();
    }


    /**
     {@inheritDoc}
     */
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("Current Board:\n");

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (board[j][i] == 0) {
                    stringBuilder.append(" - ");
                } else {
                    if (board[j][i] == 1) {
                        stringBuilder.append(" X ");
                    } else if (board[j][i] == -1) {
                        stringBuilder.append(" O ");
                    } else if (board[j][i] == 2) {
                        stringBuilder.append(" Y ");
                    } else if (board[j][i] == -2) {
                        stringBuilder.append(" Q ");
                    }
                }
            }
            stringBuilder.append("\n");
        }

        return stringBuilder.toString();
    }



    private void gameBoard() {
        boxDiam = width / cols;
        for (int i = 0; i < cols; i++) {
            for (int j = 0; j < rows; j++) {
                if ((i + j) % 2 == 0) {
                    board[i][j] = 0;
                } else {
                    board[i][j] = 1;
                }
            }
        }
    }




    /**
     {@inheritDoc}
     */
    @Override
    public void startNewGame() {
        // Erstellt die Ausgangsstellung für beide Spieler
        for (int i = 0; i < cols; i++) {
            for (int j = 0; j < rows; j++) {
                if ((i + j) % 2 == 1) {
                    // In den ersten drei Reihen Spieler 1 setzen
                    if (j < 3) {
                        board[i][j] = -1; // Spieler 2
                    }
                    // In den letzten drei Reihen Spieler 2 setzen
                    else if (j >= rows - 3) {
                        board[i][j] = 1; // Spieler 1
                    } else {
                        board[i][j] = 0; // Leer
                    }
                } else {
                    board[i][j] = 0; // Leer
                }
            }
        }

    }




}
