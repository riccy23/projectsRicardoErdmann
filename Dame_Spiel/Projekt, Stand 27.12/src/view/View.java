package view;

import controller.GameState;
import controller.IController;
import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PImage;
import processing.event.MouseEvent;
import controlP5.*;


/**
 * Diese Klasse visualisiert das Spiel mithilfe des Processing-Frameworks. Sie zeichnet das Spielbrett, Spielsteine,
 * die aktuelle Spieler-Anzeige und ein Menü.
 */
public class View extends PApplet implements IView {

    private IController controller;
    private PImage player1, player2, player1Queen, player2Queen, startScreen, gameOver;
    private ControlP5 cp5;
    private Button local, online;


    /**
     * Dies ist der Konstruktor der Klasse, dieser nimmt als Parameter eine Breite width und eine Höhe height
     * entgegen, welche beim Ausführen des Programms die Maße des Fensters darstellen, indem das Spiel abläuft.
     */
    public View(int width, int height) {
        setSize(width, height);
    }

    /**
     * Dies ist der Setter für den Controller, über diese Methode verbindet man die View und den Controller in der Main Methode
     *  mit dem Parameter controller gibt man dan das Objekt des ControllerInterfaces rein.
     */
    public void setController(IController controller) {
        this.controller = controller;
    }

    /**
     {@inheritDoc}
     */
    public float getMouseX() {
        return mouseX;
    }

    /**
     {@inheritDoc}
     */
    public float getMouseY() {
        return mouseY;
    }


    /**
     * In der processing eigenen settings() Methode werden hier alle `PImage` Variablen mit einer Bilddatei initialisiert,
     * die sich im src Ordner des Projekts befinden
     */
    public void settings() {
        player1 = loadImage("player1WHITEalt.png");
        player1.resize(500, 200);
        player2 = loadImage("player2BLACKalt.png");
        player2.resize(500, 200);
        player1Queen = loadImage("player1WHITEqueenalt.png");
        player1Queen.resize(500, 200);
        player2Queen = loadImage("player2Blackqueenalt.png");
        player2Queen.resize(500, 200);
        startScreen = loadImage("start.png");
        startScreen.resize(width, height);
        gameOver = loadImage("gameOver.png");
        gameOver.resize(width, height);
    }

    /**
     * Die draw() Methode wird 60-mal pro Sekunde aufgerufen und führt darin die nextFrame() Methode aus dem Controller aus.
     */
    public void draw() {
        controller.nextFrame();
    }


    /**
     {@inheritDoc}
     */
    @Override
    public void drawBoard(int cols, int rows, int boxDiam) {
        for (int i = 0; i < cols; i++) {
            for (int j = 0; j < rows; j++) {
                if ((i + j) % 2 == 0) {
                    fill(255);
                } else {
                    fill(116,139,151);
                }
                rect(i * boxDiam, j * boxDiam, boxDiam, boxDiam);

            }
        }

    }

    /**
     {@inheritDoc}
     */
    public void drawPieces(int[][] board, int cols, int rows, int boxDiam) {
        for (int i = 0; i < cols; i++) {
            for (int j = 0; j < rows; j++) {
                if (board[i][j] == 1) {
                    image(player1, i * boxDiam, j * boxDiam, boxDiam, boxDiam);
                } else if (board[i][j] == -1) {
                    image(player2, i * boxDiam, j * boxDiam, boxDiam, boxDiam);
                } else if (board[i][j] == 2) {
                    image(player1Queen, i * boxDiam, j * boxDiam, boxDiam, boxDiam);
                } else if (board[i][j] == -2) {
                    image(player2Queen, i * boxDiam, j * boxDiam, boxDiam, boxDiam);
                }
            }
        }
    }

    /**
     {@inheritDoc}
     */
    public void drawActivePlayer(int moves) {
        if (moves % 2 == 0) {
            fill(144, 195, 208);

        } else {
            fill(122,176,122);

        }
        textSize(11);
        text("Current Player", 704,330);
        ellipse(750,360,50,50);
        strokeWeight(2);


    }


    /**
     {@inheritDoc}
     */
    @Override
    public void drawStartScreen() {
        image(startScreen, 0, 0);

        if (cp5 == null) {
            cp5 = new ControlP5(this);

            PFont myFont = createFont("Space Mono", 30);

            local = cp5.addButton("Local")
                    .setLabel("Local")
                    .setPosition(320, 280)
                    .setSize(200, 70)
                    .addListener(event -> {
                        if (!controller.getThread().isConnected()) {
                           // controller.startGame();
                            controller.setState(GameState.PLAYING);
                        }
                    });
            local.setFont(myFont);


            online = cp5.addButton("Online")
                    .setLabel("Online")
                    .setPosition(320, 360)
                    .setSize(200, 70)
                    .addListener(event -> {
                        if (controller.getThread().isConnected()) {
                            //controller.startGame();
                            controller.setState(GameState.PLAYING);
                        } else {
                            System.err.println("Es gibt zurzeit keinen Client !");
                        }
                    });
            online.setFont(myFont);

            local.setColorBackground(color(34, 139, 34))
                    .setColorForeground(color(0, 100, 0))
                    .setColorActive(color(0, 50, 0));

            online.setColorBackground(color(34, 139, 34))
                    .setColorForeground(color(0, 100, 0))
                    .setColorActive(color(0, 50, 0));

        }

        if (controller.getThread().isServer()) {
            setButtonVisibility(true);
        } else {
            setButtonVisibility(false);
            drawWaitingForServerToStart();
        }

    }

    /**
     {@inheritDoc}
     */
    public void setButtonVisibility(boolean visible) {
        if (visible) {
            online.setVisible(true);
            local.setVisible(true);
        } else {
            local.setVisible(false);
            online.setVisible(false);
        }
    }

    /**
     {@inheritDoc}
     */
    public void drawGameOver(int moves) {
        image(gameOver, 0, 0);
        if (moves % 2 == 0) {
            fill(0, 0, 0, 150);
            rect(230, 350, 400, 70);
            fill(122,176,122);
            textSize(50);
            text("BISASAM WON !", 240, 400);
        } else if (moves % 2 == 1) {
            fill(0, 0, 0, 150);
            rect(230, 350, 400, 70);
            fill(144, 195, 208);
            textSize(50);
            text("SCHIGGY WON !", 240, 400);
        }

    }

    /**
     * Diese Methode wird immer dann aufgerufen, wenn die Maus oder das Trackpad gedrückt wird. In jedem Aufruf wird
     * die getSelectedPos Methode aus dem Controller aufgerufen.
     */
    @Override
    public void mousePressed(MouseEvent event) {
        controller.getSelectedPos();
    }


    /**
     {@inheritDoc}
     */
    @Override
    public void drawSelectedPos(int[][] board, int moves, int boxDiam) {
        //Überprüfung, ob überhaupt ein Feld ausgewählt wurde
        if (controller.getSelectedPosition() != null) {
            //Erstellen der jeweiligen Koordinaten des ausgewählten Feldes
            int x = controller.getSelectedPosition().x() * boxDiam;
            int y = controller.getSelectedPosition().y() * boxDiam;


            if (controller.getThread().isConnected()) {
                if (controller.getThread().isServer()) {
                    if ((x + y) % 2 == 0 && board[controller.getSelectedPosition().x()][controller.getSelectedPosition().y()] != 0 && (board[controller.getSelectedPosition().x()][controller.getSelectedPosition().y()] == 1 || board[controller.getSelectedPosition().x()][controller.getSelectedPosition().y()]== 2)) {
                        if (moves % 2 == 0) {
                            fill(0, 161, 255, 180);
                            rect(x, y, boxDiam, boxDiam);
                        }
                    }else if(board[controller.getSelectedPosition().x()][controller.getSelectedPosition().y()] == -1 || board[controller.getSelectedPosition().x()][controller.getSelectedPosition().y()] == -2){
                        controller.setSelectedPos(null);
                    }
                }else if(moves % 2 == 1 && board[controller.getSelectedPosition().x()][controller.getSelectedPosition().y()] != 0 && (board[controller.getSelectedPosition().x()][controller.getSelectedPosition().y()] == -1 || board[controller.getSelectedPosition().x()][controller.getSelectedPosition().y()]== -2)){
                    fill(0, 100, 0, 180);
                    rect(x, y, boxDiam, boxDiam);
                }else if(board[controller.getSelectedPosition().x()][controller.getSelectedPosition().y()] == 1 || board[controller.getSelectedPosition().x()][controller.getSelectedPosition().y()] == 2){
                    controller.setSelectedPos(null);
                }

            }else{


                //Überprüfung stellt sicher, dass das ausgewählte Feld das dunkle ist und dass das Brett an der ausgewählten stelle nicht leer ist
                if ((x + y) % 2 == 0 && board[controller.getSelectedPosition().x()][controller.getSelectedPosition().y()] != 0) {
                    if (moves % 2 == 0) {
                        fill(0, 161, 255, 180);
                        rect(x, y, boxDiam, boxDiam);
                    } else if (moves % 2 == 1) {
                        fill(0, 130, 0, 180);
                        rect(x, y, boxDiam, boxDiam);
                    }
                }
            }
        }
    }

    /**
     {@inheritDoc}
     */
    public void drawWaitingForServerToStart() {
        background(startScreen);

        textSize(47);
        fill(0);
        String text = "Waiting for Player 1 to start the game....";
        float xPosition = 15;
        float yPosition = 400;
        fill(0, 100, 0);
        rect(10,361,785,56);
        fill(255);
        text(text, xPosition, yPosition);
    }

}







