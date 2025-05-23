package test;

import model.MapModel;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class MapTest {
    private MapModel model;
    
    @Before
    public void setUp() {
        model = new MapModel();
    }
    
    @Test
    public void testEasyLayout() {
        model = new MapModel(0); // Easy level is index 0
        int[][] board = model.getMatrix();
        
        // Check board dimensions
        assertEquals("Easy layout should be 5x4", 5, board.length);
        assertEquals("Easy layout should be 5x4", 4, board[0].length);
        
        // Check Cao Cao position (2x2 block)
        assertEquals("Cao Cao should be at [0,1]", MapModel.CAO_CAO, board[0][1]);
        assertEquals("Cao Cao should be at [0,2]", MapModel.CAO_CAO, board[0][2]);
        assertEquals("Cao Cao should be at [1,1]", MapModel.CAO_CAO, board[1][1]);
        assertEquals("Cao Cao should be at [1,2]", MapModel.CAO_CAO, board[1][2]);
        
        // Check Guan Yu position (2x1 block)
        assertEquals("Guan Yu should be at [2,1]", MapModel.GUAN_YU, board[2][1]);
        assertEquals("Guan Yu should be at [2,2]", MapModel.GUAN_YU, board[2][2]);
        
        // Check General positions (1x2 blocks)
        assertEquals("General should be at [3,0]", MapModel.GENERAL, board[3][0]);
        assertEquals("General should be at [4,0]", MapModel.GENERAL, board[4][0]);
        assertEquals("General should be at [3,3]", MapModel.GENERAL, board[3][3]);
        assertEquals("General should be at [4,3]", MapModel.GENERAL, board[4][3]);
        
        // Check Soldier positions (1x1 blocks)
        assertEquals("Soldier should be at [3,1]", MapModel.SOLDIER, board[3][1]);
        assertEquals("Soldier should be at [3,2]", MapModel.SOLDIER, board[3][2]);
    }
    
    @Test
    public void testHardLayout() {
        model = new MapModel(1); // Hard level is index 1
        int[][] board = model.getMatrix();
        
        // Check board dimensions
        assertEquals("Hard layout should be 6x5", 6, board.length);
        assertEquals("Hard layout should be 6x5", 5, board[0].length);
        
        // Check Cao Cao position (2x2 block)
        assertEquals("Cao Cao should be at [0,1]", MapModel.CAO_CAO, board[0][1]);
        assertEquals("Cao Cao should be at [0,2]", MapModel.CAO_CAO, board[0][2]);
        assertEquals("Cao Cao should be at [1,1]", MapModel.CAO_CAO, board[1][1]);
        assertEquals("Cao Cao should be at [1,2]", MapModel.CAO_CAO, board[1][2]);
    }
    
    @Test
    public void testExpertLayout() {
        model = new MapModel(2); // Expert level is index 2
        int[][] board = model.getMatrix();
        
        // Check board dimensions
        assertEquals("Expert layout should be 7x6", 7, board.length);
        assertEquals("Expert layout should be 7x6", 6, board[0].length);
        
        // Check Cao Cao position (2x2 block)
        assertEquals("Cao Cao should be at [0,1]", MapModel.CAO_CAO, board[0][1]);
        assertEquals("Cao Cao should be at [0,2]", MapModel.CAO_CAO, board[0][2]);
        assertEquals("Cao Cao should be at [1,1]", MapModel.CAO_CAO, board[1][1]);
        assertEquals("Cao Cao should be at [1,2]", MapModel.CAO_CAO, board[1][2]);
    }
    
    @Test
    public void testMasterLayout() {
        model = new MapModel(3); // Master level is index 3
        int[][] board = model.getMatrix();
        
        // Check board dimensions
        assertEquals("Master layout should be 7x6", 7, board.length);
        assertEquals("Master layout should be 7x6", 6, board[0].length);
        
        // Check Cao Cao position (2x2 block)
        assertEquals("Cao Cao should be at [0,1]", MapModel.CAO_CAO, board[0][1]);
        assertEquals("Cao Cao should be at [0,2]", MapModel.CAO_CAO, board[0][2]);
        assertEquals("Cao Cao should be at [1,1]", MapModel.CAO_CAO, board[1][1]);
        assertEquals("Cao Cao should be at [1,2]", MapModel.CAO_CAO, board[1][2]);
    }
    
    @Test
    public void testInvalidLayout() {
        model = new MapModel(-1); // Invalid level should default to 0
        int[][] board = model.getMatrix();
        
        // Should load default layout (easy)
        assertEquals("Invalid layout should default to easy", 5, board.length);
        assertEquals("Invalid layout should default to easy", 4, board[0].length);
    }
} 