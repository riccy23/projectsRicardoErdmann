import model.Model;
import processing.core.PApplet;
import controller.Controller;
import view.View;

public class MainMain {
    public static void main(String[] args) {
        final int GAME_WIDTH = 800;
        final int GAME_HEIGHT = 800;
        var model = new Model(GAME_WIDTH, GAME_HEIGHT);
        var controller = Controller.ServerOrClient("localhost",8080,model);
        var view = new View(GAME_WIDTH, GAME_HEIGHT);


        controller.setModel(model);
        controller.setView(view);
        view.setController(controller);


        PApplet.runSketch(new String[]{"Poke Checkers"}, view);
    }
}
