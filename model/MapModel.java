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
    public static final int[][][] LEVELS = {
        // Level 0 - Easy (4x5) 经典可解布局 - 横刀立马
        {
            {GENERAL, CAO_CAO, CAO_CAO, GENERAL},
            {GENERAL, CAO_CAO, CAO_CAO, GENERAL},
            {SOLDIER, GUAN_YU, GUAN_YU, SOLDIER},
            {GENERAL, SOLDIER, SOLDIER, GENERAL},
            {GENERAL, 0, 0, GENERAL}
        },
        // Level 1 - Hard (4x5) 兵分三路
        {
            {GENERAL, CAO_CAO, CAO_CAO, GENERAL},
            {GENERAL, CAO_CAO, CAO_CAO, GENERAL},
            {SOLDIER, GUAN_YU, GUAN_YU, SOLDIER},
            {SOLDIER, BLOCKED, BLOCKED, SOLDIER},
            {GENERAL, SOLDIER, SOLDIER, GENERAL}
        },
        // Level 2 - Expert (5x5) 巧过五关
        {
            {GENERAL, CAO_CAO, CAO_CAO, GENERAL, MILITARY_CAMP},
            {GENERAL, CAO_CAO, CAO_CAO, GENERAL, 0},
            {SOLDIER, GUAN_YU, GUAN_YU, SOLDIER, BLOCKED},
            {SOLDIER, ZHOU_YU, ZHOU_YU, ZHOU_YU, SOLDIER},
            {GENERAL, SOLDIER, SOLDIER, GENERAL, MILITARY_CAMP}
        },
        // Level 3 - Master (5x5) 四面楚歌
        {
            {GENERAL, CAO_CAO, CAO_CAO, GENERAL, MILITARY_CAMP},
            {GENERAL, CAO_CAO, CAO_CAO, GENERAL, BLOCKED},
            {SOLDIER, GUAN_YU, GUAN_YU, SOLDIER, BLOCKED},
            {SOLDIER, ZHOU_YU, ZHOU_YU, ZHOU_YU, SOLDIER},
            {GENERAL, SOLDIER, SOLDIER, GENERAL, MILITARY_CAMP}
        }
    };

    public MapModel() {
        this(0); // Default to first level
    }

    public MapModel(int level) {
        if (level < 0 || level >= LEVELS.length) {
            level = 0;
        }
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

    /**
     * Move a piece at the specified position in the given direction
     * @param row The row of the piece to move
     * @param col The column of the piece to move
     * @param direction The direction to move the piece
     * @return true if the move was successful, false otherwise
     */
    public boolean move(int row, int col, Direction direction) {
        // 简化版本，直接返回 true，实际逻辑需要根据游戏规则实现
        return true;
    }
}
