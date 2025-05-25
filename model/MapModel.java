package model;

/**
 * This class represents the Klotski game board with different block types:
 * 0 = Empty
 * 1 = Cao Cao (2x2)
 * 2 = Guan Yu (2x1) 
 * 3 = General (1x2)
 * 4 = Soldier (1x1)
 * 5 = Zhou Yu (1x3)
 * 9 = Blocked (immovable)
 * 10 = Military Camp (soldiers cannot step on)
 */
public class MapModel {
    public static final int CAO_CAO = 1;    // 2x2 size, 1 piece
    public static final int GUAN_YU = 2;    // 2x1 size, 1 piece
    public static final int GENERAL = 3;    // 1x2 size, 4 pieces
    public static final int SOLDIER = 4;    // 1x1 size, 4 pieces
    public static final int ZHOU_YU = 5;    // 1x3 horizontal block
    public static final int BLOCKED = 9;    // Immovable obstacle
    public static final int MILITARY_CAMP = 10; // Military camp - only soldiers can step on
    
    // Difficulty level names
    public static final String[] LEVEL_NAMES = {
        "Easy", "Hard", "Expert", "Master"
    };
    
    // Props availability per level
    public static final boolean[] LEVEL_PROPS_ALLOWED = {
        false, // Easy - no props
        true,  // Hard - props allowed
        true,  // Expert - props allowed
        false  // Master - no props
    };
    
    // Time attack enforced settings
    public static final boolean[] LEVEL_TIME_ATTACK_ENFORCED = {
        false, // Easy - optional time attack
        false, // Hard - optional time attack
        false, // Expert - optional time attack
        true   // Master - enforced 5 min time attack
    };
    
    // Default time for enforced time attack levels (in minutes)
    public static final int DEFAULT_MASTER_TIME_LIMIT = 5;
    
    int[][] matrix;
    private int currentLevel = 0;  // Default to first level
    
    public static final int[][][] LEVELS = {
        // Level 0 - Easy (4x5) 经典可解布局 - 横刀立马
        {
            {GENERAL, CAO_CAO, CAO_CAO, GENERAL},
            {GENERAL, CAO_CAO, CAO_CAO, GENERAL},
            {SOLDIER, GUAN_YU, GUAN_YU, SOLDIER},
            {GENERAL, SOLDIER, SOLDIER, GENERAL},
            {GENERAL, 0, 0, GENERAL}
        },
        // Level 1 - Hard (6x4) 兵分三路
        {
//            {GENERAL, CAO_CAO, CAO_CAO, GENERAL},
//            {GENERAL, CAO_CAO, CAO_CAO, GENERAL},
//            {0, GUAN_YU, GUAN_YU, SOLDIER},
//            {0, 0, 0, 0},
//            {0, 0, SOLDIER, GENERAL},
//            {0, 0, 0, GENERAL}

                {GENERAL, CAO_CAO, CAO_CAO, GENERAL},
                {GENERAL, CAO_CAO, CAO_CAO, GENERAL},
                {SOLDIER, 0, 0, 0},
                {0, 0, SOLDIER, 0},
                {GUAN_YU, GUAN_YU, 0, 0},
                {0, 0, 0, 0}
        },
        // Level 2 - Expert (6x5) 巧过五关
        {
                {GENERAL, SOLDIER, CAO_CAO, CAO_CAO, SOLDIER, BLOCKED},
                {GENERAL, SOLDIER, CAO_CAO, CAO_CAO, BLOCKED, 0},
                {SOLDIER, GUAN_YU, GUAN_YU, SOLDIER, BLOCKED, 0},
                {SOLDIER, ZHOU_YU, ZHOU_YU, ZHOU_YU, SOLDIER, 0},
                {GENERAL, SOLDIER, SOLDIER, GENERAL, MILITARY_CAMP, 0},
                {GENERAL, 0, 0, GENERAL, 0, 0}


        },
        // Level 3 - Master (6x5) 四面楚歌
        {
            {GENERAL, SOLDIER, CAO_CAO, CAO_CAO, SOLDIER, 0},
            {GENERAL, SOLDIER, CAO_CAO, CAO_CAO, BLOCKED, 0},
            {SOLDIER, GUAN_YU, GUAN_YU, SOLDIER, SOLDIER, 0},
            {0, ZHOU_YU, ZHOU_YU, ZHOU_YU, 0, 0},
            {SOLDIER, SOLDIER, SOLDIER, SOLDIER, 0, 0},
            {0, 0, 0, 0, 0, 0}
        }
    };

    public MapModel() {
        this(0); // Default to first level
    }

    public MapModel(int level) {
        if (level < 0 || level >= LEVELS.length) {
            level = 0;
        }
        this.currentLevel = level;
        int rows = LEVELS[level].length;
        int cols = LEVELS[level][0].length;
        this.matrix = new int[rows][cols];
        for (int i = 0; i < rows; i++) {
            System.arraycopy(LEVELS[level][i], 0, this.matrix[i], 0, cols);
        }
    }

    public MapModel(int[][] matrix) {
        this.matrix = matrix;
    }

    public int getWidth() {
        return this.matrix[0].length;
    }

    public int getHeight() {
        return this.matrix.length;
    }

    public int getId(int row, int col) {
        return matrix[row][col];
    }

    public int[][] getMatrix() {
        return matrix;
    }

    /**
     * Set the board matrix to a custom layout
     * @param matrix The new board layout
     */
    public void setMatrix(int[][] matrix) {
        if (matrix == null || matrix.length == 0 || matrix[0].length == 0) {
            throw new IllegalArgumentException("Invalid matrix dimensions");
        }
        this.matrix = new int[matrix.length][matrix[0].length];
        for (int i = 0; i < matrix.length; i++) {
            System.arraycopy(matrix[i], 0, this.matrix[i], 0, matrix[i].length);
        }
    }

    public boolean checkInWidthSize(int col) {
        return col >= 0 && col < matrix[0].length;
    }

    public boolean checkInHeightSize(int row) {
        return row >= 0 && row < matrix.length;
    }

    public int[][] copyMatrix() {
        int[][] copy = new int[matrix.length][matrix[0].length];
        for (int i = 0; i < matrix.length; i++) {
            System.arraycopy(matrix[i], 0, copy[i], 0, matrix[i].length);
        }
        return copy;
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    public boolean isWin() {
        // Check if Cao Cao is at the bottom center of the board
        int boardHeight = getHeight();
        int boardWidth = getWidth();
        int goalRow = boardHeight - 2;
        int goalCol = (boardWidth - 2) / 2;

        // Check if Cao Cao is at the goal position
        for (int r = goalRow; r < goalRow + 2; r++) {
            for (int c = goalCol; c < goalCol + 2; c++) {
                if (r >= boardHeight || c >= boardWidth || matrix[r][c] != CAO_CAO) {
                    return false;
                }
            }
        }
        return true;
    }

    public void reset() {
        // Reset the map to its initial state
        int rows = LEVELS[currentLevel].length;
        int cols = LEVELS[currentLevel][0].length;
        this.matrix = new int[rows][cols];
        for (int i = 0; i < rows; i++) {
            System.arraycopy(LEVELS[currentLevel][i], 0, this.matrix[i], 0, cols);
        }
    }
}
