package test;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;
import model.MapModel;
import model.AISolver;
import controller.GameController;
import view.game.GamePanel;

public class TestProgressiveDifficulty {
    private MapModel model;
    private GameController controller;
    private AISolver solver;
    private GamePanel mockPanel;
    
    @Before
    public void setUp() {
        model = new MapModel();
        mockPanel = new GamePanel(model);
        controller = new GameController(mockPanel, model);
        solver = new AISolver(model, controller);
    }
    
    @Test
    public void testEasyLayout() {
        // Test easy layout
        model = new MapModel(0); // Easy level
        mockPanel.resetBoard(model.getMatrix()); // Update view's model
        solver = new AISolver(model, controller); // Update solver with new model
        assertTrue("Easy layout should be solvable", solver.findSolution());
        assertTrue("Solution should not be empty", solver.getSolutionLength() > 0);
    }
    
    @Test
    public void testHardLayout() {
        // Test hard layout
        model = new MapModel(1); // Hard level
        mockPanel.resetBoard(model.getMatrix()); // Update view's model
        solver = new AISolver(model, controller); // Update solver with new model
        assertTrue("Hard layout should be solvable", solver.findSolution());
        assertTrue("Solution should not be empty", solver.getSolutionLength() > 0);
    }
    
    @Test
    public void testExpertLayout() {
        // Test expert layout
        model = new MapModel(2); // Expert level
        mockPanel.resetBoard(model.getMatrix()); // Update view's model
        solver = new AISolver(model, controller); // Update solver with new model
        assertTrue("Expert layout should be solvable", solver.findSolution());
        assertTrue("Solution should not be empty", solver.getSolutionLength() > 0);
    }
    
    @Test
    public void testMasterLayout() {
        // Test master layout
        model = new MapModel(3); // Master level
        mockPanel.resetBoard(model.getMatrix()); // Update view's model
        solver = new AISolver(model, controller); // Update solver with new model
        assertTrue("Master layout should be solvable", solver.findSolution());
        assertTrue("Solution should not be empty", solver.getSolutionLength() > 0);
    }
    
    @Test
    public void testSolutionProgression() {
        // Test that harder layouts require more moves
        model = new MapModel(0); // Easy level
        mockPanel.resetBoard(model.getMatrix()); // Update view's model
        solver = new AISolver(model, controller); // Update solver with new model
        assertTrue(solver.findSolution());
        int easyMoves = solver.getSolutionLength();
        
        model = new MapModel(1); // Hard level
        mockPanel.resetBoard(model.getMatrix()); // Update view's model
        solver = new AISolver(model, controller); // Update solver with new model
        assertTrue(solver.findSolution());
        int hardMoves = solver.getSolutionLength();
        
        model = new MapModel(2); // Expert level
        mockPanel.resetBoard(model.getMatrix()); // Update view's model
        solver = new AISolver(model, controller); // Update solver with new model
        assertTrue(solver.findSolution());
        int expertMoves = solver.getSolutionLength();
        
        model = new MapModel(3); // Master level
        mockPanel.resetBoard(model.getMatrix()); // Update view's model
        solver = new AISolver(model, controller); // Update solver with new model
        assertTrue(solver.findSolution());
        int masterMoves = solver.getSolutionLength();
        
        // Verify that harder layouts require more moves
        assertTrue("Hard layout should require more moves than easy", hardMoves > easyMoves);
        assertTrue("Expert layout should require more moves than hard", expertMoves > hardMoves);
        assertTrue("Master layout should require more moves than expert", masterMoves > expertMoves);
    }
} 