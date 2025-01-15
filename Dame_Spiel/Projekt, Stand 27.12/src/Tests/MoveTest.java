/*
package Tests;

import model.IModel;
import model.Move;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import model.Model;

public class MoveTest {
    Model model;

    @BeforeEach
    void setUp(){
        //startNewGame() wird im Konstruktor aufgerufen
        model = new Model(10,10);
    }

    @AfterEach
    void tearDown(){}

    @Test
    void IfMoveCanBeDoneThenTargetFieldIsEmpty(){
        model.getMove().makeMove(model.getBoard(), 1,new IModel.Position(2,5), new IModel.Position(3,4));
        assertEquals(0,model.getBoard()[2][5]);
        assertEquals(1,model.getBoard()[3][4]);

    }

    @Test
    void IfMoveCanNotBeDoneThenTargetFieldIsNotEmpty(){
        model.getMove().makeMove(model.getBoard(), 1,new IModel.Position(2,5), new IModel.Position(3,4));
        model.getMove().makeMove(model.getBoard(), -1,new IModel.Position(5,2), new IModel.Position(4,3));
        model.getMove().makeMove(model.getBoard(), 1,new IModel.Position(3,4), new IModel.Position(4,3));
        assertEquals(1,model.getBoard()[3][4]);
        assertEquals(-1,model.getBoard()[4][3]);

    }

    @Test
    void IfMoveCanBeDoneThenStartAndTargetAreNeighbors(){
        model.getMove().makeMove(model.getBoard(), 1,new IModel.Position(2,5), new IModel.Position(3,4));
        assertEquals(0,model.getBoard()[2][5]);
        assertEquals(1,model.getBoard()[3][4]);
    }

    @Test
    void IfMoveCanNotBeDoneThenStartAndTargetAreNotNeighbors(){
        model.getMove().makeMove(model.getBoard(), 1,new IModel.Position(2,5), new IModel.Position(4,3));
        assertEquals(1,model.getBoard()[2][5]);
        assertEquals(0,model.getBoard()[4][3]);
    }

    @Test
    void IfMoveCanBeDoneThenTargetIsBlack(){
        model.getMove().makeMove(model.getBoard(), 1,new IModel.Position(2,5), new IModel.Position(3,4));
        assertEquals(0,model.getBoard()[2][5]);
        assertEquals(1,model.getBoard()[3][4]);
    }

    @Test
    void IfMoveCanNotBeDoneThenTargetIsNotBlack(){
        model.getMove().makeMove(model.getBoard(), 1,new IModel.Position(2,5), new IModel.Position(2,4));
        assertEquals(1,model.getBoard()[2][5]);
        assertEquals(0,model.getBoard()[2][4]);
    }

    @Test
    void moveCountShouldMatch(){
        //Züge ganz am Anfang
        assertEquals(0,model.getMove().getMoves());

        model.getMove().makeMove(model.getBoard(), 1,new IModel.Position(2,5),new IModel.Position(3,4));
        model.getMove().makeMove(model.getBoard(), -1,new IModel.Position(5,2),new IModel.Position(4,3));
        model.getMove().makeMove(model.getBoard(), 1,new IModel.Position(3,4),new IModel.Position(5,2));
        //Anzahl an Zügen nach 3 Zügen
        assertEquals(3,model.getMove().getMoves());
    }

    @Test
    void IfMoveCanBeDoneThenMoveWasValid(){
        model.getMove().makeMove(model.getBoard(), 1,new IModel.Position(4,5), new IModel.Position(5,4));
       assertEquals(0,model.getBoard()[4][5]);
        assertEquals(1,model.getBoard()[5][4]);
    }

    @Test
    void IfMoveCanNotBeDoneThenMoveWasNotValid(){
        model.getMove().makeMove(model.getBoard(), 1,new IModel.Position(4,5), new IModel.Position(6,3));
        assertEquals(1,model.getBoard()[4][5]);
        assertEquals(0,model.getBoard()[6][3]);
    }


    @Test
    void IfACaptureIsPossibleThenItWillBeDone(){
        model.getMove().makeMove(model.getBoard(), 1,new IModel.Position(2,5),new IModel.Position(3,4));
        model.getMove().makeMove(model.getBoard(),-1,new IModel.Position(5,2), new IModel.Position(4,3));
        //Eigentliches Schlagen
        model.getMove().makeMove(model.getBoard(),1,new IModel.Position(3,4), new IModel.Position(5,2));
        //P1 ist jetzt ein Feld hinter Ursprungspositions von dem Stein der geschlagen wurde
        assertEquals(1,model.getBoard()[5][2]);
        //Ursprungspositions vom schlagenden Stein ist leer
        assertEquals(0,model.getBoard()[3][4]);
        //Ursprungspositions vom geschlagenen Stein ist leer
        assertEquals(0,model.getBoard()[4][3]);
    }

    @Test
    void shouldCalculateTheXCoordinateOfFieldInbetweenCorrectly(){
        IModel.Position start = new IModel.Position(3,4);
        IModel.Position target = new IModel.Position(5,2);

        assertEquals(4,model.getMove().calcBetweenX(start,target));
    }

    @Test
    void shouldCalculateTheYCoordinateOfFieldInbetweenCorrectly(){
        IModel.Position start = new IModel.Position(3,4);
        IModel.Position target = new IModel.Position(5,2);

        assertEquals(3,model.getMove().calcBetweenY(start,target));
    }


    @Test
    void boardHasCorrectValuesAfterAQueenIsInGame(){
        model.getMove().makeMove(model.getBoard(), 1,new IModel.Position(2,5),new IModel.Position(3,4));
        model.getMove().makeMove(model.getBoard(), -1,new IModel.Position(5,2),new IModel.Position(4,3));
        model.getMove().makeMove(model.getBoard(), 1,new IModel.Position(3,4),new IModel.Position(5,2));
        model.getMove().makeMove(model.getBoard(), -1,new IModel.Position(4,1),new IModel.Position(6,3));
        model.getMove().makeMove(model.getBoard(), 1,new IModel.Position(4,5),new IModel.Position(5,4));
        model.getMove().makeMove(model.getBoard(), -1,new IModel.Position(6,3),new IModel.Position(4,5));
        model.getMove().makeMove(model.getBoard(), 1,new IModel.Position(3,6),new IModel.Position(5,4));
        model.getMove().makeMove(model.getBoard(), -1,new IModel.Position(3,2),new IModel.Position(2,3));
        model.getMove().makeMove(model.getBoard(), 1,new IModel.Position(5,4),new IModel.Position(4,3));
        model.getMove().makeMove(model.getBoard(), -1,new IModel.Position(3,0),new IModel.Position(4,1));
        model.getMove().makeMove(model.getBoard(), 1,new IModel.Position(4,3),new IModel.Position(3,2));
        model.getMove().makeMove(model.getBoard(), -1,new IModel.Position(4,1),new IModel.Position(5,2));
        model.getMove().makeMove(model.getBoard(), 1,new IModel.Position(3,2),new IModel.Position(4,1));
        model.getMove().makeMove(model.getBoard(), -1,new IModel.Position(5,2),new IModel.Position(6,3));
        //Zug um Dame zu bekommen
        model.getMove().makeMove(model.getBoard(), 1,new IModel.Position(4,1),new IModel.Position(3,0));
        //erwartet wird 2, weil es sich um ein Stein vom Spieler 1 handelt, ansonsten wäre es -2
        assertEquals(2,model.getBoard()[3][0]);
    }

    @Test
    void IfQueenWantsToMakeAValidMoveItsDone(){
        model.getMove().makeMove(model.getBoard(), 1,new IModel.Position(2,5),new IModel.Position(3,4));
        model.getMove().makeMove(model.getBoard(), -1,new IModel.Position(5,2),new IModel.Position(4,3));
        model.getMove().makeMove(model.getBoard(), 1,new IModel.Position(3,4),new IModel.Position(5,2));
        model.getMove().makeMove(model.getBoard(), -1,new IModel.Position(4,1),new IModel.Position(6,3));
        model.getMove().makeMove(model.getBoard(), 1,new IModel.Position(4,5),new IModel.Position(5,4));
        model.getMove().makeMove(model.getBoard(), -1,new IModel.Position(6,3),new IModel.Position(4,5));
        model.getMove().makeMove(model.getBoard(), 1,new IModel.Position(3,6),new IModel.Position(5,4));
        model.getMove().makeMove(model.getBoard(), -1,new IModel.Position(3,2),new IModel.Position(2,3));
        model.getMove().makeMove(model.getBoard(), 1,new IModel.Position(5,4),new IModel.Position(4,3));
        model.getMove().makeMove(model.getBoard(), -1,new IModel.Position(3,0),new IModel.Position(4,1));
        model.getMove().makeMove(model.getBoard(), 1,new IModel.Position(4,3),new IModel.Position(3,2));
        model.getMove().makeMove(model.getBoard(), -1,new IModel.Position(4,1),new IModel.Position(5,2));
        model.getMove().makeMove(model.getBoard(), 1,new IModel.Position(3,2),new IModel.Position(4,1));
        model.getMove().makeMove(model.getBoard(), -1,new IModel.Position(5,2),new IModel.Position(6,3));
        //Zug um Dame zu bekommen
        model.getMove().makeMove(model.getBoard(), 1,new IModel.Position(4,1),new IModel.Position(3,0));

        model.getMove().makeMove(model.getBoard(), -1,new IModel.Position(2,3), new IModel.Position(1,4));
        //Hier steht für 'player' eine '2', weil wir die Dame des Spieler 1 ausgewählt haben und der interne Wert dieses Feldes 2 ist
        model.getMove().makeMove(model.getBoard(), 2,new IModel.Position(3,0), new IModel.Position(4,1));

        assertEquals(0,model.getBoard()[3][0]);
        assertEquals(2,model.getBoard()[4][1]);
    }



    @Test
    void IfQueenCanCaptureThenSheCaptures(){
        model.getMove().makeMove(model.getBoard(), 1,new IModel.Position(2,5),new IModel.Position(3,4));
        model.getMove().makeMove(model.getBoard(), -1,new IModel.Position(5,2),new IModel.Position(4,3));
        model.getMove().makeMove(model.getBoard(), 1,new IModel.Position(3,4),new IModel.Position(5,2));
        model.getMove().makeMove(model.getBoard(), -1,new IModel.Position(4,1),new IModel.Position(6,3));
        model.getMove().makeMove(model.getBoard(), 1,new IModel.Position(4,5),new IModel.Position(5,4));
        model.getMove().makeMove(model.getBoard(), -1,new IModel.Position(6,3),new IModel.Position(4,5));
        model.getMove().makeMove(model.getBoard(), 1,new IModel.Position(3,6),new IModel.Position(5,4));
        model.getMove().makeMove(model.getBoard(), -1,new IModel.Position(3,2),new IModel.Position(2,3));
        model.getMove().makeMove(model.getBoard(), 1,new IModel.Position(5,4),new IModel.Position(4,3));
        model.getMove().makeMove(model.getBoard(), -1,new IModel.Position(3,0),new IModel.Position(4,1));
        model.getMove().makeMove(model.getBoard(), 1,new IModel.Position(4,3),new IModel.Position(3,2));
        model.getMove().makeMove(model.getBoard(), -1,new IModel.Position(4,1),new IModel.Position(5,2));
        model.getMove().makeMove(model.getBoard(), 1,new IModel.Position(3,2),new IModel.Position(4,1));
        model.getMove().makeMove(model.getBoard(), -1,new IModel.Position(5,2),new IModel.Position(6,3));
        //Zug um Dame zu bekommen
        model.getMove().makeMove(model.getBoard(), 1,new IModel.Position(4,1),new IModel.Position(3,0));
        model.getMove().makeMove(model.getBoard(), -1,new IModel.Position(5,0),new IModel.Position(4,1));

        //Hier steht für 'player' eine '2', weil wir die Dame des Spieler 1 ausgewählt haben und der interne Wert dieses Feldes 2 ist
        model.getMove().makeMove(model.getBoard(),2,new IModel.Position(3,0), new IModel.Position(5,2));

        assertEquals(0,model.getBoard()[3][0]);
        assertEquals(0,model.getBoard()[4][1]);
        assertEquals(2,model.getBoard()[5][2]);

    }

    @Test
    void ifPlayerLandsOnLastOrFirstRowAfterACaptureItBecomesQueen(){
        model.getMove().makeMove(model.getBoard(), 1,new IModel.Position(2,5),new IModel.Position(3,4));
        model.getMove().makeMove(model.getBoard(), -1,new IModel.Position(5,2),new IModel.Position(4,3));
        model.getMove().makeMove(model.getBoard(), 1,new IModel.Position(3,4),new IModel.Position(5,2));
        model.getMove().makeMove(model.getBoard(), -1,new IModel.Position(4,1),new IModel.Position(6,3));
        model.getMove().makeMove(model.getBoard(), 1,new IModel.Position(4,5),new IModel.Position(5,4));
        model.getMove().makeMove(model.getBoard(), -1,new IModel.Position(6,3),new IModel.Position(4,5));
        model.getMove().makeMove(model.getBoard(), 1,new IModel.Position(3,6),new IModel.Position(5,4));
        model.getMove().makeMove(model.getBoard(), -1,new IModel.Position(3,2),new IModel.Position(2,3));
        model.getMove().makeMove(model.getBoard(), 1,new IModel.Position(5,4),new IModel.Position(4,3));
        model.getMove().makeMove(model.getBoard(), -1,new IModel.Position(3,0),new IModel.Position(4,1));

        model.getMove().makeMove(model.getBoard(), 1,new IModel.Position(4,3),new IModel.Position(5,2));
        model.getMove().makeMove(model.getBoard(), -1,new IModel.Position(7,2),new IModel.Position(6,3));
    //Eigentliches Schlagen
        model.getMove().makeMove(model.getBoard(), 1,new IModel.Position(5,2),new IModel.Position(3,0));

        assertEquals(0,model.getBoard()[5][2]);
        assertEquals(0,model.getBoard()[4][1]);
        assertEquals(2,model.getBoard()[3][0]);


    }

    @Test
    void valuesFromConstructorShouldMatch(){
        var start = new IModel.Position(6,5);
        var target = new IModel.Position(7,4);
        Move move = new Move(start, target);

        assertEquals(start,move.getStart());
        assertEquals(target, move.getTarget());
    }

    @Test
    void makeMoveShouldActuallyMakeAMove(){
        model.getMove().makeMove(model.getBoard(),1,new IModel.Position(2,5), new IModel.Position(3,4));

        assertEquals(0, model.getBoard()[2][5]);
        assertEquals(1, model.getBoard()[3][4]);
    }

    @Test
    void trueIfIsGameOver(){
        var gameOver = model.getMove().isGameOver(model.getBoard());
        assertFalse(gameOver);
    }




}

*/
