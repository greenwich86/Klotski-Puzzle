package test;

import model.AISolver;
import model.MapModel;
import model.Direction;
import controller.GameController;
import view.game.GamePanel;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.List;

public class AISolverTest {
    private MapModel model;
    private AISolver solver;
    private GameController controller;
    private GamePanel gamePanel;
    
    @Before
    public void setUp() {
        model = new MapModel(0); // Use Easy level (index 0)
        gamePanel = new GamePanel(model);
        controller = new GameController(gamePanel, model);
        solver = new AISolver(model, controller);
    }
    
    @Test
    public void testEasyLevelSolution() {
        // The board is already initialized with Easy level layout from MapModel
        assertTrue("AI should find a solution for Easy level", solver.findSolution());
        
        // Wait for solution to be found (since it runs in a separate thread)
        try {
            Thread.sleep(1000); // Give some time for the solution to be found
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Verify solution exists and has reasonable length
        assertTrue("Solution should not be empty", solver.getSolutionLength() > 0);
        assertTrue("Solution should not be too long", solver.getSolutionLength() < 100);
        
        // Get first few moves and verify they are valid
        List<AISolver.Move> moves = solver.getSolutionMoves(5);
        assertNotNull("Should be able to get moves", moves);
        assertTrue("Should have some moves", moves.size() > 0);
        
        // Verify each move has valid coordinates and direction
        for (AISolver.Move move : moves) {
            assertTrue("Row should be valid", move.row >= 0 && move.row < model.getHeight());
            assertTrue("Column should be valid", move.col >= 0 && move.col < model.getWidth());
            assertNotNull("Direction should not be null", move.direction);
        }
    }
    
    @Test
    public void testSolutionExecution() {
        assertTrue("AI should find a solution", solver.findSolution());
        
        // Wait for solution to be found
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Execute the solution
        solver.executeSolution();
        
        // Wait for execution to complete
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Verify the solution was executed
        assertTrue("Solution should have been executed", solver.getSolutionLength() > 0);
    }
    
    @Test
    public void testInvalidMoves() {
        // Create an invalid move
        AISolver.Move invalidMove = new AISolver.Move(-1, -1, Direction.UP, MapModel.SOLDIER, controller);
        
        // Verify the move is invalid
        assertFalse("Row should be valid", invalidMove.row >= 0 && invalidMove.row < model.getHeight());
        assertFalse("Column should be valid", invalidMove.col >= 0 && invalidMove.col < model.getWidth());
    }
    
    @Test
    public void testHardLevelSolution() {
        // Set up hard level board using MapModel
        model = new MapModel(1); // Hard level is index 1
        gamePanel = new GamePanel(model);
        controller = new GameController(gamePanel, model);
        solver = new AISolver(model, controller);
        
        // Find solution
        assertTrue("AI should find a solution for Hard level", solver.findSolution());
        
        // Wait for solution to be found
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Verify solution exists and has reasonable length
        assertTrue("Solution should not be empty", solver.getSolutionLength() > 0);
        assertTrue("Solution should not be too long", solver.getSolutionLength() < 100);
    }
    
    @Test
    public void testExpertLevelSolution() {
        // Set up expert level board using MapModel
        model = new MapModel(2); // Expert level is index 2
        gamePanel = new GamePanel(model);
        controller = new GameController(gamePanel, model);
        solver = new AISolver(model, controller);
        
        // Find solution
        assertTrue("AI should find a solution for Expert level", solver.findSolution());
        
        // Wait for solution to be found
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Verify solution exists and has reasonable length
        assertTrue("Solution should not be empty", solver.getSolutionLength() > 0);
        assertTrue("Solution should not be too long", solver.getSolutionLength() < 100);
    }
    
    @Test
    public void testMasterLevelSolution() {
        // Set up master level board using MapModel
        model = new MapModel(3); // Master level is index 3
        gamePanel = new GamePanel(model);
        controller = new GameController(gamePanel, model);
        solver = new AISolver(model, controller);
        
        // Find solution
        assertTrue("AI should find a solution for Master level", solver.findSolution());
        
        // Wait for solution to be found
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Verify solution exists and has reasonable length
        assertTrue("Solution should not be empty", solver.getSolutionLength() > 0);
        assertTrue("Solution should not be too long", solver.getSolutionLength() < 100);
    }
    
    @Test
    public void testInvalidBoard() {
        // Set up invalid board (no Cao Cao)
        int[][] invalidBoard = {
            {0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0}
        };
        model.setMatrix(invalidBoard);
        
        // Find solution
        boolean found = solver.findSolution();
        assertFalse("Should not find solution for invalid board", found);
    }
    
    @Test
    public void testMoveConstructor() {
        // Test with different piece types and directions
        AISolver.Move move1 = new AISolver.Move(0, 0, Direction.UP, MapModel.CAO_CAO, controller);
        assertEquals(0, move1.row);
        assertEquals(0, move1.col);
        assertEquals(Direction.UP, move1.direction);
        assertEquals(MapModel.CAO_CAO, move1.pieceType);
        
        AISolver.Move move2 = new AISolver.Move(2, 3, Direction.RIGHT, MapModel.SOLDIER, controller);
        assertEquals(2, move2.row);
        assertEquals(3, move2.col);
        assertEquals(Direction.RIGHT, move2.direction);
        assertEquals(MapModel.SOLDIER, move2.pieceType);
    }
    
    @Test
    public void testMoveToString() {
        AISolver.Move move = new AISolver.Move(1, 1, Direction.DOWN, MapModel.GENERAL, controller);
        String expected = "Move General (1x2 vertical blue) at [1,1] downward";
        assertEquals(expected, move.toString());
    }
} 