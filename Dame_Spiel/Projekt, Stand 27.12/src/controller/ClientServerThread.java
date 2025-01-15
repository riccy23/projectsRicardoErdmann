package controller;

import model.IModel;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;


public class ClientServerThread extends Thread{
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private Controller controller;
    private IModel model;
    private ObjectOutputStream oos;
    private boolean isServer;

    private ClientServerThread(IModel model, Controller controller){
       this.model = model;
       this.controller = controller;
    }


    /**
     * Diese Methode dient dazu um beim Starten der Anwendung(en) zu entscheiden, ob die zu öffnende Anwendung als Server
     * oder als Client geöffnet wird, diese Differenzierung ist wichtig, damit das Zusammenspielen wie vorgesehen funktioniert.
     * @param ip
     * @param port
     * @param controller
     * @param model
     * @return
     */
    public static ClientServerThread newServerOrClient(String ip, int port, Controller controller, IModel model){
        var cst = new ClientServerThread(model, controller);

        try {
            //Wenn es keinen Server gibt, wird die Anwendung selbst ein Server
            cst.serverSocket = new ServerSocket(port);
            cst.isServer = true;
            return cst;
        }catch (IOException e){
            System.err.println("Es gibt bereits einen Server: " + e.getMessage());
            System.err.println("Client wird erstellt !");

        }


        try {
            //Es wird versucht sich als Client mit einem Server zu verbinden
            cst.clientSocket = new Socket(ip, port);
            // cst.clientSocket.connect(new InetSocketAddress(ip,port), 1000);
            cst.isServer = false;
            cst.oos = new ObjectOutputStream(cst.clientSocket.getOutputStream());
            return cst;
        }catch (IOException e){
            System.err.println("Es gibt keinen Server: " + e.getMessage());
        }
        return cst;


    }


    /**
     * Diese Methode ist dazu da um verschiedene Objekte an den Thread zu senden, abhängig von diesen Objekten wird in
     * der run Methode des Threads verschiedene Aktionen ausgeführt.
     * @param obj
     */
    public void send(Object obj) {
        try {
            if(oos != null) {
                oos.reset();
                oos.writeObject(obj);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Diese Methode gibt true zurück, wenn ein Client mit dem Server verbunden ist und false wenn noch kein Client da ist.
     * @return
     */
    public boolean isConnected(){
        return clientSocket != null && clientSocket.isConnected();
    }

    /**
     * Diese Methode gibt true zurück, wenn es sich bei der Anwendung um einen Server handelt und false sonst.
     * @return
     */
    public boolean isServer() {
        return isServer;
    }


    /**
     * Diese Methode akzeptiert Client-Verbindungen, verarbeitet Daten (Spielzustände oder Befehle) und aktualisiert
     * den Controller, um das Spiel zu steuern. Der Thread bleibt aktiv und reagiert auf die Kommunikation zwischen
     * Server und Client.
     */
    @Override
    public void run(){
        try {

            //Wenn es noch keinen Client gibt
            if(clientSocket == null){
                System.out.println("Waiting for Client to connect...");
                //Server lauscht auf einen Client
                clientSocket = serverSocket.accept();
                System.out.println("Client connected !");
                oos = new ObjectOutputStream(clientSocket.getOutputStream());
            }
            var ois = new ObjectInputStream(clientSocket.getInputStream());

            while(true){
                Object obj = ois.readObject();
                if(obj instanceof IModel){
                    controller.setModel((IModel) obj);
                }else if(obj instanceof String){
                    switch ((String) obj){

                        case "startGame":
                            controller.setState(GameState.PLAYING);
                            break;

                        case "gameOver":
                            controller.setState(GameState.GAME_OVER);
                            break;

                    }
                }

            }
        }catch (IOException | ClassNotFoundException e){
           e.printStackTrace();
        }
    }
}
