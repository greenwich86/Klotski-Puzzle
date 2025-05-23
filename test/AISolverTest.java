package test;

import controller.GameController;
import model.AISolver;
import model.MapModel;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import view.game.GamePanel;

import static org.junit.Assert.assertTrue;

public class AISolverTest {
    private MapModel model;
    private AISolver solver;
    
    @Before
    public void setUp() {
        model = new MapModel();
        solver = new AISolver(model, null);  // We don't need the controller for this test
    }
    
    public static void main(String[] args) {
        // 创建一个简单的地图模型
        MapModel model = new MapModel(0); // 使用简单难度
        
        // 创建一个游戏面板
        GamePanel gamePanel = new GamePanel(model);
        
        // 创建游戏控制器
        GameController controller = new GameController(gamePanel, model);
        
        // 创建AI求解器
        AISolver solver = new AISolver(model, controller);
        
        // 运行简单测试
        testSimpleCaoCaoOnly(model, solver);
    }
    
    private static void testSimpleCaoCaoOnly(MapModel model, AISolver solver) {
        // 创建一个简单的5x4棋盘状态
        int[][] testBoard = new int[5][4];
        
        // 初始化棋盘 - 只放置曹操在顶部中间
        testBoard[0][1] = MapModel.CAO_CAO;
        testBoard[0][2] = MapModel.CAO_CAO;
        testBoard[1][1] = MapModel.CAO_CAO;
        testBoard[1][2] = MapModel.CAO_CAO;
        
        System.out.println("\n[TEST] Initial test board state (only Cao Cao):");
        printBoardState(testBoard);
        
        // 设置模型的状态
        model.setMatrix(testBoard);
        
        // 尝试找到解决方案
        System.out.println("\n[TEST] Attempting to find solution:");
        boolean solutionFound = solver.findSolution();
        System.out.println("[TEST] Solution found: " + solutionFound);
        
        if (solutionFound) {
            System.out.println("[TEST] Solution length: " + solver.getSolutionLength());
            System.out.println("[TEST] First 5 moves of solution:");
            List<AISolver.Move> moves = solver.getSolutionMoves(5);
            for (int i = 0; i < moves.size(); i++) {
                System.out.println("[TEST] Move " + (i + 1) + ": " + moves.get(i));
            }
        }
        
        // 验证初始布局是否可解
        System.out.println("\n[TEST] Checking if layout is solvable:");
        boolean isSolvable = solver.isSolvable();
        System.out.println("[TEST] Layout is solvable: " + isSolvable);
    }
    
    private static void printBoardState(int[][] board) {
        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < board.length; r++) {
            sb.append("  ");
            for (int c = 0; c < board[0].length; c++) {
                sb.append(String.format("%2d ", board[r][c]));
            }
            sb.append("\n");
        }
        System.out.println(sb.toString());
    }

    private void printBoard(int[][] board) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[0].length; j++) {
                sb.append(String.format("%2d ", board[i][j]));
            }
            sb.append("\n");
        }
        System.out.println(sb.toString());
    }

    @Test
    public void testSimpleLayout() {
        // Create the classic "Heng Dao Li Ma" layout
        int[][] board = new int[5][4];
        
        // Place Cao Cao at the top middle
        board[0][1] = MapModel.CAO_CAO;    // 1 = Cao Cao (2x2)
        board[0][2] = MapModel.CAO_CAO;
        board[1][1] = MapModel.CAO_CAO;
        board[1][2] = MapModel.CAO_CAO;
        
        // Place Guan Yu horizontally below Cao Cao
        board[2][1] = MapModel.GUAN_YU;    // 2 = Guan Yu (2x1)
        board[2][2] = MapModel.GUAN_YU;
        
        // Place generals on both sides
        // Left side generals
        board[0][0] = MapModel.GENERAL;    // 3 = General (1x2)
        board[1][0] = MapModel.GENERAL;
        board[2][0] = MapModel.GENERAL;
        board[3][0] = MapModel.GENERAL;
        
        // Right side generals
        board[0][3] = MapModel.GENERAL;
        board[1][3] = MapModel.GENERAL;
        board[2][3] = MapModel.GENERAL;
        board[3][3] = MapModel.GENERAL;
        
        // Place 4 soldiers at the bottom
        board[4][0] = MapModel.SOLDIER;    // 4 = Soldier (1x1)
        board[4][1] = MapModel.SOLDIER;
        board[4][2] = MapModel.SOLDIER;
        board[4][3] = MapModel.SOLDIER;
        
        model.setMatrix(board);
        
        System.out.println("\n[TEST] Initial test board state (Heng Dao Li Ma layout):");
        printBoard(board);
        
        System.out.println("\n[TEST] Attempting to find solution:");
        boolean solutionFound = solver.findSolution();
        System.out.println("[TEST] Solution found: " + solutionFound);
        
        if (solutionFound) {
            System.out.println("[TEST] Solution length: " + solver.getSolutionLength());
            System.out.println("[TEST] First 5 moves of solution:");
            List<AISolver.Move> moves = solver.getSolutionMoves(5);
            for (int i = 0; i < moves.size(); i++) {
                System.out.println("[TEST] Move " + (i + 1) + ": " + moves.get(i));
            }
        }
        
        assertTrue("Should find a solution", solutionFound);
        assertTrue("Solution should have moves", solver.getSolutionLength() > 0);
    }
} 