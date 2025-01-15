package controller;

import model.IModel;
import view.IView;


public class Controller implements IController {
   private int width, height;

    private IModel model;

    private IView view;

    // Diese Position ist für die eigentlich Logik zu 100% irrelevant. Sie dient lediglich dazu um die, auf der GUI,
    // ausgewählte Position des Benutzers auszumachen.
    private IModel.Position selectedPos;
    private GameState state;
    private ClientServerThread thread;

    /**
     * Dies ist der Konstruktor vom Controller, welcher zwei Parameter width und height entgegennimmt, diese
     * zwei Werte sind dieselben wie bei der Model und View Klasse, also einfach nur die Maße des Fensters wo das Spiel
     * später stattfindet. Ebenso wird im Konstruktor der Spielstand gleich auf START gesetzt.
     * @param width
     * @param height
     */
    public Controller(int width, int height) {
        state = GameState.START;
        this.width = width;
        this.height = height;
    }

    /**
     * Diese Methode dient dazu um beim Starten der Anwendung(en) zu entscheiden, ob die zu öffnende Anwendung als Server
     * oder als Client geöffnet wird, diese Differenzierung ist wichtig, damit das Zusammenspielen wie vorgesehen funktioniert.
     * @param ip
     * @param port
     * @param model
     * @return
     */
    public static Controller ServerOrClient(String ip, int port, IModel model) {
        var controller = new Controller(800,800);
        controller.thread = ClientServerThread.newServerOrClient(ip,port,controller,model);
        controller.thread.start();
        return controller;

    }

    /**
     {@inheritDoc}
     */
    public int getRows() {
        return model.getRows();
    }

    /**
     {@inheritDoc}
     */
    public int getCols() {
        return model.getCols();
    }

    /**
     {@inheritDoc}
     */
    public int getBoxDiam() {
        return model.getBoxDiam();
    }

    /**
     * Dies ist der Setter für das Model, also quasi das Bindeglied zwischen dem Model und dem Controller
     * mit @param model nimmt der Setter ein Objekt von IModel entgegen
     * @param model
     */
    public void setModel(IModel model) {
        this.model = model;
    }

    /**
     * Dies ist der Setter für die View, also quasi das Bindeglied zwischen der View und dem Controller
     * mit @param view nimmt der Setter ein Objekt der IView entgegen
     * @param view
     */
    public void setView(IView view) {
        this.view = view;
    }


    /**
     {@inheritDoc}
     */
    public void setState(GameState state) {
        this.state = state;
    }

    /**
     {@inheritDoc}
     */
    public ClientServerThread getThread() {
        return thread;
    }

    /**
     {@inheritDoc}
     */
    public void setSelectedPos(IModel.Position selectedPos) {
        this.selectedPos = selectedPos;
    }

    /**
     {@inheritDoc}
     */
    @Override
    public void nextFrame() {


            switch (state) {

                case START -> {view.drawStartScreen();}

                case PLAYING -> {
                    if(thread != null){
                        thread.send("startGame");
                    }

                    view.setButtonVisibility(false);
                    checkGameOver();
                    view.drawBoard(model.getCols(), model.getRows(), model.getBoxDiam());
                    view.drawSelectedPos(model.getBoard(), model.getMove().getMoves(),getBoxDiam() );
                    view.drawPieces(model.getBoard(), model.getCols(), model.getRows(), model.getBoxDiam());
                    view.drawActivePlayer(model.getMove().getMoves());

                }

                case GAME_OVER -> {view.drawGameOver(model.getMove().getMoves());}
            }
    }

    private void makeMove(int player, IModel.Position start, IModel.Position target){
        model.getMove().makeMove(model.getBoard(), player,start,target);
        if(thread != null){
            thread.send(model);
        }

    }

    private void handleMouseInput(IModel.Position choosenPos) {
        if (selectedPos == null) {
            selectedPos = choosenPos;
        } else {
            int player = model.getBoard()[selectedPos.x()][selectedPos.y()];
            makeMove(player, selectedPos, choosenPos);
            selectedPos = null;
        }
    }

    /**
     {@inheritDoc}
     */
    public IModel.Position getSelectedPos(){
        int col = (int) view.getMouseX() / getBoxDiam();
        int row = (int) view.getMouseY() /getBoxDiam();
        if(col >= 0 && col < getCols() && row >= 0 && row < getRows()){
            IModel.Position selectedPos = new IModel.Position(col, row);
            handleMouseInput(selectedPos);
        }

        return selectedPos;
    }

    /**
     {@inheritDoc}
     */
    public void checkGameOver(){
        if(model.getMove().isGameOver(model.getBoard())){
           state =  GameState.GAME_OVER;
           if(thread != null){
               thread.send("gameOver");
           }

        }
    }

    /**
     {@inheritDoc}
     */
    public IModel.Position getSelectedPosition() {
        return selectedPos;
    }




}
