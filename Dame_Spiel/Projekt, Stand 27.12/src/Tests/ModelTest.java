/*
package Tests;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import model.Model;


public class ModelTest {
    Model model;

    @BeforeEach
    void setUp(){
        //startNewGame() wird im Konstruktor aufgerufen
        model = new Model(10,10);
    }

    @AfterEach
    void tearDown(){}


    @Test
    void startNewGameShouldIntializeStartPositions(){
        //Prüfungen ob tatsächlich auch eine Ausgangsstellung vorliegt
        //Ausgangsstellung für Spieler 1
        assertEquals(1,model.getBoard()[0][5]);
        assertEquals(1,model.getBoard()[2][5]);
        assertEquals(1,model.getBoard()[4][5]);
        assertEquals(1,model.getBoard()[6][5]);
        assertEquals(1,model.getBoard()[1][6]);
        assertEquals(1,model.getBoard()[3][6]);
        assertEquals(1,model.getBoard()[5][6]);
        assertEquals(1,model.getBoard()[7][6]);
        assertEquals(1,model.getBoard()[0][7]);
        assertEquals(1,model.getBoard()[2][7]);
        assertEquals(1,model.getBoard()[4][7]);
        assertEquals(1,model.getBoard()[6][7]);

        //Ausgangsstellung für Spieler 2
        assertEquals(-1,model.getBoard()[1][0]);
        assertEquals(-1,model.getBoard()[3][0]);
        assertEquals(-1,model.getBoard()[5][0]);
        assertEquals(-1,model.getBoard()[7][0]);
        assertEquals(-1,model.getBoard()[0][1]);
        assertEquals(-1,model.getBoard()[2][1]);
        assertEquals(-1,model.getBoard()[4][1]);
        assertEquals(-1,model.getBoard()[6][1]);
        assertEquals(-1,model.getBoard()[1][2]);
        assertEquals(-1,model.getBoard()[3][2]);
        assertEquals(-1,model.getBoard()[5][2]);
        assertEquals(-1,model.getBoard()[7][2]);
    }

    @Test
    void toStringShouldMatch(){
        String expected = "Current Board:\n" +
                " -  O  -  O  -  O  -  O \n" +
                " O  -  O  -  O  -  O  - \n" +
                " -  O  -  O  -  O  -  O \n" +
                " -  -  -  -  -  -  -  - \n" +
                " -  -  -  -  -  -  -  - \n" +
                " X  -  X  -  X  -  X  - \n" +
                " -  X  -  X  -  X  -  X \n" +
                " X  -  X  -  X  -  X  - \n";

        assertEquals(expected,model.toString());
    }

}


 */
