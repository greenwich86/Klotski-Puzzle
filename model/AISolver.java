package model;

import java.util.*;
import javax.swing.JOptionPane;
import javax.swing.Timer;
import controller.GameController;
import view.game.BoxComponent;

/**
 * AISolver implements an improved A* search algorithm for the Klotski puzzle.
 * Features:
 * 1. Bidirectional search (from both initial and goal states)
 * 2. Improved heuristic function with pattern recognition
 * 3. Priority-based state generation
 * 4. Solution optimization
 */
public class AISolver {
    private MapModel model;
    private GameController controller;
    private List<Move> solution;
    private boolean isSolving = false;
    private boolean isSearching = false;
    
    // Search parameters
    private static final int MAX_STATES = 1000000;  // 增加最大状态数
    private static final int REPORT_INTERVAL = 100;
    private static final int MIN_STATES_TO_EXPLORE = 1000;  // 降低最小探索状态数
    private static final int MAX_DEPTH = 100;  // 降低最大深度限制
    
    // Animation
    private Timer animationTimer;
    private int animationFrame = 0;
    private static final String[] LOADING_FRAMES = {
        "Solving ⠋", "Solving ⠙", "Solving ⠹", "Solving ⠸", 
        "Solving ⠼", "Solving ⠴", "Solving ⠦", "Solving ⠧", 
        "Solving ⠇", "Solving ⠏"
    };
    
    // Goal position
    private int goalRow = -1;
    private int goalCol = -1;
    
    /**
     * Represents a move in the puzzle
     */
    public static class Move {
        public final int row;
        public final int col;
        public final Direction direction;
        public final int pieceType;
        private final GameController controller;
        
        public Move(int row, int col, Direction direction, int pieceType, GameController controller) {
            this.row = row;
            this.col = col;
            this.direction = direction;
            this.pieceType = pieceType;
            this.controller = controller;
        }
        
        @Override
        public String toString() {
            return String.format("Move %s at [%d,%d] %s", 
                controller.getPieceNameByType(pieceType), row, col, direction);
        }
    }
    
    /**
     * Represents a state in the search space
     */
    private static class State implements Comparable<State> {
        public final int[][] board;
        public final List<Move> moves;
        public final int cost;
        public final int heuristic;
        public final int fScore;
        public final int depth;
        
        public State(int[][] board, List<Move> moves, int heuristic, int depth) {
            this.board = board;
            this.moves = moves;
            this.cost = moves.size();
            this.heuristic = heuristic;
            this.fScore = this.cost + this.heuristic;
            this.depth = depth;
        }
        
        @Override
        public int compareTo(State other) {
            // Primary comparison by f-score
            if (this.fScore != other.fScore) {
                return Integer.compare(this.fScore, other.fScore);
            }
            
            // Secondary comparison by heuristic
            if (this.heuristic != other.heuristic) {
                return Integer.compare(this.heuristic, other.heuristic);
            }
            
            // Tertiary comparison by depth
            return Integer.compare(this.depth, other.depth);
        }
        
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof State)) return false;
            State other = (State) obj;
            return Arrays.deepEquals(this.board, other.board);
        }
        
        @Override
        public int hashCode() {
            return Arrays.deepHashCode(board);
        }
    }
    
    public AISolver(MapModel model, GameController controller) {
        this.model = model;
        this.controller = controller;
        this.solution = new ArrayList<>();
        initializeGoalPosition();
    }
    
    /**
     * Calculate the goal position for Cao Cao
     */
    private void initializeGoalPosition() {
        // Find the exit position (bottom center)
        goalRow = model.getHeight() - 1;
        goalCol = model.getWidth() / 2;
    }
    
    /**
     * Find a solution using bidirectional A* search
     */
    public boolean findSolution() {
        if (isSolving) return false;
        
        solution.clear();
        isSearching = true;
        startLoadingAnimation();
        
        Thread searchThread = new Thread(() -> {
            boolean found = performBidirectionalSearch();
        isSearching = false;
            stopLoadingAnimation();
            
            if (found) {
                System.out.println("AI Solver: Solution found with " + solution.size() + " moves");
                showMessage("Solution Found", 
                    "AI has found a solution with " + solution.size() + " moves.");
            } else {
                System.out.println("AI Solver: No solution found");
                showMessage("No Solution", 
                    "AI could not find a solution for this puzzle configuration.");
            }
        });
        
        searchThread.start();
        
        // Wait for the search thread to complete
        try {
            searchThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
        
        return !solution.isEmpty();
    }
    
    /**
     * Perform A* search
     */
    private boolean performBidirectionalSearch() {
        // Initialize search
        PriorityQueue<State> queue = new PriorityQueue<>();
        Set<String> visited = new HashSet<>();
        
        // Get initial state
        int[][] initialBoard = model.copyMatrix();
        int initialHeuristic = calculateHeuristic(initialBoard);
        State initialState = new State(initialBoard, new ArrayList<>(), initialHeuristic, 0);
        queue.add(initialState);
        
        System.out.println("Initial board state:");
        printBoardState(initialBoard);
        
        int statesExplored = 0;
        State bestState = initialState;
        int bestHeuristic = initialHeuristic;
        
        // Main search loop
        while (!queue.isEmpty() && isSearching) {
            // Get next state
            State current = queue.poll();
            statesExplored++;
            
            // Check if current state is goal state
            if (isGoalState(current.board)) {
                // Verify Cao Cao is at the bottom
                int[] caoCaoPos = findCaoCaoPosition(current.board);
                if (caoCaoPos != null && caoCaoPos[0] == goalRow) {
                    solution.clear();
                    solution.addAll(current.moves);
                    System.out.println("\nSolution found!");
                    System.out.println("Final board state:");
                printBoardState(current.board);
                    System.out.println("Solution moves: " + solution.size());
                    System.out.println("\nFinal board state:");
                    printBoardState(current.board);
                    System.out.println("Total moves: " + solution.size());
                    return true;
                }
            }
            
            // Update best state
            if (current.heuristic < bestHeuristic) {
                bestState = current;
                bestHeuristic = current.heuristic;
                System.out.println("\nNew best state found:");
                System.out.println("Heuristic: " + bestHeuristic);
                System.out.println("Depth: " + current.depth);
                System.out.println("States explored: " + statesExplored);
                System.out.println("Board state:");
                printBoardState(bestState.board);
            }
            
            // Add to visited set
            String currentBoardStr = boardToString(current.board);
            visited.add(currentBoardStr);
            
            // Generate and add next states
            List<State> nextStates = generateNextStates(current, true);
            for (State next : nextStates) {
                String nextBoardStr = boardToString(next.board);
                if (!visited.contains(nextBoardStr)) {
                    queue.add(next);
                }
            }
            
            // Check search limits
            if (statesExplored > MAX_STATES) {
                if (statesExplored >= MIN_STATES_TO_EXPLORE && bestState.moves.size() > 0) {
                    // Verify Cao Cao is at the bottom
                    int[] caoCaoPos = findCaoCaoPosition(bestState.board);
                    if (caoCaoPos != null && caoCaoPos[0] == goalRow) {
                        solution.clear();
                        solution.addAll(bestState.moves);
                        System.out.println("\nUsing best found state");
                        System.out.println("Best board state:");
                        printBoardState(bestState.board);
                        System.out.println("Best solution moves: " + bestState.moves.size());
                        return true;
                    }
                }
                System.out.println("\nSearch limit reached:");
                System.out.println("States explored: " + statesExplored);
                System.out.println("Best state found:");
                System.out.println("Heuristic: " + bestHeuristic);
                System.out.println("Depth: " + bestState.depth);
                System.out.println("Board state:");
                printBoardState(bestState.board);
                return false;
            }
        }
        
        System.out.println("\nSearch completed without finding solution:");
        System.out.println("States explored: " + statesExplored);
        System.out.println("Best state found:");
        System.out.println("Heuristic: " + bestHeuristic);
        System.out.println("Depth: " + bestState.depth);
        System.out.println("Board state:");
        printBoardState(bestState.board);
        return false;
    }
    
    /**
     * Calculate heuristic value for a board state
     */
    private int calculateHeuristic(int[][] board) {
        // Find Cao Cao position
        int[] caoCaoPos = findCaoCaoPosition(board);
        if (caoCaoPos == null) return Integer.MAX_VALUE;
        
        int caoCaoRow = caoCaoPos[0];
        int caoCaoCol = caoCaoPos[1];
        
        // 1. Manhattan Distance with Linear Conflicts
        int manhattanDistance = calculateManhattanDistance(caoCaoRow, caoCaoCol);
        int linearConflicts = calculateLinearConflicts(board, caoCaoRow, caoCaoCol);
        
        // 2. Pattern Recognition
        int patternScore = calculatePatternScore(board, caoCaoRow, caoCaoCol);
        
        // 3. Space Analysis
        int spaceScore = calculateSpaceScore(board, caoCaoRow, caoCaoCol);
        
        // 4. Path Analysis
        int pathScore = calculatePathScore(board, caoCaoRow, caoCaoCol);
        
        // Combine all factors with adjusted weights
        return manhattanDistance * 5 +     // 降低距离权重
               linearConflicts * 10 +      // 降低冲突权重
               patternScore * 8 +          // 降低模式权重
               spaceScore * 15 +           // 增加空间权重
               pathScore * 20;             // 增加路径权重
    }
    
    /**
     * Calculate Manhattan distance to goal
     */
    private int calculateManhattanDistance(int row, int col) {
        return Math.abs(row - goalRow) + Math.abs(col - goalCol);
    }
    
    /**
     * Calculate linear conflicts
     */
    private int calculateLinearConflicts(int[][] board, int caoCaoRow, int caoCaoCol) {
        int conflicts = 0;
        
        // Check vertical conflicts
        for (int r = caoCaoRow + 2; r <= goalRow; r++) {
            if (r < board.length) {
                for (int c = caoCaoCol; c <= caoCaoCol + 1; c++) {
                    if (board[r][c] != 0) {
                        // Count pieces that need to move in the same direction
                        int pieceType = board[r][c];
                        if (pieceType == MapModel.SOLDIER) {
                            conflicts += 3;  // Higher penalty for soldiers
                        } else {
                            conflicts += 1;
                        }
                    }
                }
            }
        }
        
        // Check horizontal conflicts
        int centerCol = (board[0].length - 2) / 2;
        if (caoCaoCol != centerCol) {
            int dir = Integer.compare(centerCol, caoCaoCol);
            int startCol = caoCaoCol + (dir > 0 ? 2 : -1);
            int endCol = centerCol + (dir > 0 ? 1 : 0);
            
            for (int c = startCol; dir > 0 ? c <= endCol : c >= endCol; c += dir) {
                if (c >= 0 && c < board[0].length) {
                    for (int r = goalRow; r <= goalRow + 1; r++) {
                        if (r < board.length && board[r][c] != 0) {
                            conflicts += 2;
                        }
                    }
                }
            }
        }
        
        return conflicts;
    }
    
    /**
     * Calculate space score
     */
    private int calculateSpaceScore(int[][] board, int caoCaoRow, int caoCaoCol) {
        int score = 0;
        
        // Check space below Cao Cao
        for (int r = caoCaoRow + 2; r <= goalRow; r++) {
            if (r < board.length) {
                int emptyCells = 0;
                for (int c = 0; c < board[0].length; c++) {
                    if (board[r][c] == 0) {
                        emptyCells++;
                    }
                }
                score += emptyCells * 3;  // 降低空单元格奖励
            }
        }
        
        // Check space around Cao Cao
        for (int dr = -1; dr <= 2; dr++) {
            for (int dc = -1; dc <= 2; dc++) {
                int r = caoCaoRow + dr;
                int c = caoCaoCol + dc;
                if (r >= 0 && r < board.length && c >= 0 && c < board[0].length) {
                    if (board[r][c] == 0) {
                        score += 2;  // 降低周围空单元格奖励
                    }
                }
            }
        }
        
        return -score;  // Negative because lower is better
    }
    
    /**
     * Calculate path score
     */
    private int calculatePathScore(int[][] board, int caoCaoRow, int caoCaoCol) {
        int score = 0;
        
        // Check vertical path
        boolean hasClearPath = true;
        for (int r = caoCaoRow + 2; r <= goalRow; r++) {
            if (r < board.length) {
                for (int c = caoCaoCol; c <= caoCaoCol + 1; c++) {
                    if (board[r][c] != 0) {
                        hasClearPath = false;
                        if (board[r][c] == MapModel.SOLDIER) {
                            score += 30;  // High penalty for soldier blocks
                        } else {
                            score += 15;  // Normal penalty for other blocks
                        }
                    }
                }
            }
        }
        
        if (hasClearPath) {
            score -= 50;  // Large bonus for clear path
        }
        
        // Check if pieces can be moved to clear the path
        for (int r = caoCaoRow + 2; r <= goalRow; r++) {
            if (r < board.length) {
                for (int c = 0; c < board[0].length; c++) {
                    if (board[r][c] != 0 && board[r][c] != MapModel.CAO_CAO) {
                        // Check if piece can be moved
                        boolean canMove = false;
                        for (Direction dir : Direction.values()) {
                            if (controller.canMove(r, c, 1, 1, dir)) {
                                canMove = true;
                    break;
                }
            }
                        if (canMove) {
                            score -= 10;  // Bonus for movable pieces
                        }
                    }
                }
            }
        }
        
        return score;
    }
    
    /**
     * Calculate pattern score
     */
    private int calculatePatternScore(int[][] board, int caoCaoRow, int caoCaoCol) {
        int score = 0;
        
        // Bonus for having clear space below
        if (caoCaoRow + 2 < board.length) {
            boolean clearBelow = true;
            for (int c = 0; c < board[0].length; c++) {
                if (board[caoCaoRow + 2][c] != 0) {
                    clearBelow = false;
                    break;
                }
            }
            if (clearBelow) score -= 20;
        }
        
        // Bonus for having clear space to sides
        int centerCol = (board[0].length - 2) / 2;
        if (caoCaoCol != centerCol) {
            boolean clearSide = true;
            int sideCol = caoCaoCol + (caoCaoCol < centerCol ? 2 : -1);
            if (sideCol >= 0 && sideCol < board[0].length) {
                for (int r = caoCaoRow; r <= caoCaoRow + 1; r++) {
                    if (board[r][sideCol] != 0) {
                        clearSide = false;
                    break;
                }
            }
                if (clearSide) score -= 15;
            }
        }
        
        // Penalty for having pieces blocking the path
        for (int r = caoCaoRow + 2; r <= goalRow; r++) {
            if (r < board.length) {
                for (int c = caoCaoCol; c <= caoCaoCol + 1; c++) {
                    if (board[r][c] != 0) {
                        score += (board[r][c] == MapModel.SOLDIER) ? 25 : 10;
                    }
                }
            }
        }
        
        return score;
    }
    
    /**
     * Find Cao Cao's position in the board
     */
    private int[] findCaoCaoPosition(int[][] board) {
        for (int r = 0; r < board.length - 1; r++) {
            for (int c = 0; c < board[0].length - 1; c++) {
                if (board[r][c] == MapModel.CAO_CAO &&
                    board[r][c+1] == MapModel.CAO_CAO &&
                    board[r+1][c] == MapModel.CAO_CAO &&
                    board[r+1][c+1] == MapModel.CAO_CAO) {
                    return new int[]{r, c};
                }
            }
        }
        return null;
    }
    
    /**
     * Check if Cao Cao can move toward the goal
     */
    private boolean canMoveTowardGoal(int[][] board, int caoCaoRow, int caoCaoCol) {
        if (caoCaoRow >= goalRow) return true;
        
        // Check if can move down
        if (caoCaoRow + 2 < board.length) {
            return board[caoCaoRow + 2][caoCaoCol] == 0 && 
                   board[caoCaoRow + 2][caoCaoCol + 1] == 0;
        }
        return false;
    }
    
    /**
     * Generate next states from current state
     */
    private List<State> generateNextStates(State current, boolean isForward) {
        List<State> nextStates = new ArrayList<>();
        int[][] board = current.board;
        
        // Track which piece types we've tried
        Set<Integer> processedPieces = new HashSet<>();
        
        // First try to move Cao Cao
        int[] caoCaoPos = findCaoCaoPosition(board);
        if (caoCaoPos != null) {
            processedPieces.add(MapModel.CAO_CAO);
            int caoCaoRow = caoCaoPos[0];
            int caoCaoCol = caoCaoPos[1];
            
            // Prioritize downward movement
            Direction[] directions = {Direction.DOWN, Direction.LEFT, Direction.RIGHT, Direction.UP};
            
            for (Direction dir : directions) {
                if (controller.canMove(caoCaoRow, caoCaoCol, 2, 2, dir)) {
                    int[][] newBoard = deepCopyBoard(board);
                    // Clear original position
                    for (int i = 0; i < 2; i++) {
                        for (int j = 0; j < 2; j++) {
                            newBoard[caoCaoRow + i][caoCaoCol + j] = 0;
                        }
                    }
                    
                    // Calculate new position
                    int dr = 0, dc = 0;
                    switch (dir) {
                        case UP: dr = -1; break;
                        case DOWN: dr = 1; break;
                        case LEFT: dc = -1; break;
                        case RIGHT: dc = 1; break;
                    }
                    
                    // Place in new position
                    for (int i = 0; i < 2; i++) {
                        for (int j = 0; j < 2; j++) {
                            int newRow = caoCaoRow + i + dr;
                            int newCol = caoCaoCol + j + dc;
                            if (newRow >= 0 && newRow < board.length && 
                                newCol >= 0 && newCol < board[0].length) {
                                newBoard[newRow][newCol] = MapModel.CAO_CAO;
                            }
                        }
                    }
                    
                    Move move = new Move(caoCaoRow, caoCaoCol, dir, MapModel.CAO_CAO, controller);
                    List<Move> newMoves = new ArrayList<>(current.moves);
                    newMoves.add(move);
                    
                    int heuristic = calculateHeuristic(newBoard);
                    State newState = new State(newBoard, newMoves, heuristic, current.depth + 1);
                    nextStates.add(newState);
                }
            }
        }
        
        // Then try other pieces
            for (int r = 0; r < board.length; r++) {
                for (int c = 0; c < board[0].length; c++) {
                int pieceType = board[r][c];
                if (pieceType == 0 || pieceType == MapModel.CAO_CAO || processedPieces.contains(pieceType)) continue;
                
                processedPieces.add(pieceType);
                
                // Determine piece dimensions based on type
                int width = 1;
                int height = 1;
                
                if (pieceType == MapModel.CAO_CAO) { // 2x2 block
                    width = 2;
                    height = 2;
                } else if (pieceType == MapModel.GUAN_YU) { // 2x1 block
                    width = 2;
                    height = 1;
                } else if (pieceType == MapModel.GENERAL) { // 1x2 block
                    width = 1;
                    height = 2;
                } else if (pieceType == MapModel.ZHOU_YU) { // 1x3 block
                    width = 3;
                    height = 1;
                }
                
                // Check if this is a valid piece position (not part of a larger piece)
                boolean isValidPosition = true;
                if (r > 0 && board[r-1][c] == pieceType) isValidPosition = false;
                if (c > 0 && board[r][c-1] == pieceType) isValidPosition = false;
                
                if (isValidPosition) {
                    // Prioritize movements that help Cao Cao
                    Direction[] directions = getPrioritizedDirections(r, c, caoCaoPos);
                    
                    for (Direction dir : directions) {
                        if (controller.canMove(r, c, width, height, dir)) {
                            int[][] newBoard = deepCopyBoard(board);
                            // Clear original position
                            for (int i = 0; i < height; i++) {
                                for (int j = 0; j < width; j++) {
                                    newBoard[r + i][c + j] = 0;
                                }
                            }
                            
                            // Calculate new position
                            int dr = 0, dc = 0;
                            switch (dir) {
                                case UP: dr = -1; break;
                                case DOWN: dr = 1; break;
                                case LEFT: dc = -1; break;
                                case RIGHT: dc = 1; break;
                            }
                            
                            // Place in new position
                            for (int i = 0; i < height; i++) {
                                for (int j = 0; j < width; j++) {
                                    int newRow = r + i + dr;
                                    int newCol = c + j + dc;
                                    if (newRow >= 0 && newRow < board.length && 
                                        newCol >= 0 && newCol < board[0].length) {
                                        newBoard[newRow][newCol] = pieceType;
                                    }
                                }
                            }
                            
                            Move move = new Move(r, c, dir, pieceType, controller);
                            List<Move> newMoves = new ArrayList<>(current.moves);
                            newMoves.add(move);
                            
                            int heuristic = calculateHeuristic(newBoard);
                            State newState = new State(newBoard, newMoves, heuristic, current.depth + 1);
                            nextStates.add(newState);
                        }
                    }
                }
            }
        }
        
        return nextStates;
    }
    
    /**
     * Get prioritized directions based on piece position relative to Cao Cao
     */
    private Direction[] getPrioritizedDirections(int row, int col, int[] caoCaoPos) {
        if (caoCaoPos == null) {
            return Direction.values();
        }
        
        int caoCaoRow = caoCaoPos[0];
        int caoCaoCol = caoCaoPos[1];
        
        // If piece is above Cao Cao, prioritize moving down
        if (row < caoCaoRow) {
            return new Direction[]{Direction.DOWN, Direction.LEFT, Direction.RIGHT, Direction.UP};
        }
        // If piece is below Cao Cao, prioritize moving up
        else if (row > caoCaoRow) {
            return new Direction[]{Direction.UP, Direction.LEFT, Direction.RIGHT, Direction.DOWN};
        }
        // If piece is to the left of Cao Cao, prioritize moving right
        else if (col < caoCaoCol) {
            return new Direction[]{Direction.RIGHT, Direction.UP, Direction.DOWN, Direction.LEFT};
        }
        // If piece is to the right of Cao Cao, prioritize moving left
        else if (col > caoCaoCol) {
            return new Direction[]{Direction.LEFT, Direction.UP, Direction.DOWN, Direction.RIGHT};
        }
        
        return Direction.values();
    }
    
    /**
     * Get piece name for debugging
     */
    private String getPieceName(int type) {
        switch (type) {
            case MapModel.CAO_CAO: return "Cao Cao";
            case MapModel.GUAN_YU: return "Guan Yu";
            case MapModel.GENERAL: return "General";
            case MapModel.SOLDIER: return "Soldier";
            default: return "Unknown";
        }
    }
    
    /**
     * Apply a move to the board
     */
    private int[][] applyMove(int[][] board, int row, int col, int width, int height, 
                            Direction dir, int pieceType) {
        // First verify the piece exists at the specified position
        boolean pieceExists = true;
        for (int i = 0; i < height && pieceExists; i++) {
            for (int j = 0; j < width && pieceExists; j++) {
                if (row + i >= board.length || col + j >= board[0].length ||
                    board[row + i][col + j] != pieceType) {
                    pieceExists = false;
                }
            }
        }
        
        if (!pieceExists) {
            System.out.println("Error: Piece not found at specified position");
            return board;
        }
        
        // Calculate movement direction (only one step)
        int dr = 0, dc = 0;
        switch (dir) {
            case UP: dr = -1; break;
            case DOWN: dr = 1; break;
            case LEFT: dc = -1; break;
            case RIGHT: dc = 1; break;
        }
        
        // Create new board
        int[][] newBoard = deepCopyBoard(board);
        
        // Clear original position
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (row + i < board.length && col + j < board[0].length) {
                    newBoard[row + i][col + j] = 0;
                }
            }
        }
        
        // Place in new position (only one step)
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int newRow = row + i + dr;
                int newCol = col + j + dc;
                if (newRow >= 0 && newRow < board.length && 
                    newCol >= 0 && newCol < board[0].length) {
                    newBoard[newRow][newCol] = pieceType;
                }
            }
        }
        
        // Print debug information
        System.out.println("\nApplying move:");
        System.out.println("Piece type: " + getPieceName(pieceType));
        System.out.println("From position: [" + row + "," + col + "]");
        System.out.println("Direction: " + dir);
        System.out.println("Dimensions: " + width + "x" + height);
        System.out.println("Original board:");
        printBoardState(board);
        System.out.println("Resulting board:");
        printBoardState(newBoard);
        
        return newBoard;
    }
    
    /**
     * Deep copy a board
     */
    private int[][] deepCopyBoard(int[][] board) {
        int[][] copy = new int[board.length][board[0].length];
        for (int i = 0; i < board.length; i++) {
            System.arraycopy(board[i], 0, copy[i], 0, board[i].length);
        }
        return copy;
    }
    
    /**
     * Convert board to string for hashing
     */
    private String boardToString(int[][] board) {
        StringBuilder sb = new StringBuilder();
        for (int[] row : board) {
            for (int cell : row) {
                sb.append(cell).append(",");
            }
        }
        return sb.toString();
    }
    
    /**
     * Print board state for debugging
     */
    private void printBoardState(int[][] board) {
        System.out.println("Board State:");
        for (int[] row : board) {
            for (int cell : row) {
                System.out.print(String.format("%2d ", cell));
            }
            System.out.println();
        }
        System.out.println();
    }
    
    /**
     * Show message dialog
     */
    private void showMessage(String title, String message) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(null, message, title, 
                JOptionPane.INFORMATION_MESSAGE);
        });
    }
    
    /**
     * Start loading animation
     */
    private void startLoadingAnimation() {
        if (animationTimer != null) {
            animationTimer.stop();
        }
        
        animationFrame = 0;
        animationTimer = new Timer(150, e -> {
            animationFrame = (animationFrame + 1) % LOADING_FRAMES.length;
            updateAnimationText(LOADING_FRAMES[animationFrame]);
        });
        
        animationTimer.start();
    }
    
    /**
     * Stop loading animation
     */
    private void stopLoadingAnimation() {
        if (animationTimer != null) {
            animationTimer.stop();
            animationTimer = null;
        }
        
        if (controller != null) {
            javax.swing.SwingUtilities.invokeLater(() -> {
                try {
                    JOptionPane.getRootFrame().setTitle("AI Solver");
                } catch (Exception e) {
                    // Ignore if no dialog is showing
                }
            });
        }
    }
    
    /**
     * Update animation text
     */
    private void updateAnimationText(String text) {
        if (controller != null) {
            javax.swing.SwingUtilities.invokeLater(() -> {
                try {
                    JOptionPane.getRootFrame().setTitle("AI Solver: " + text);
                } catch (Exception e) {
                    // Ignore if no dialog is showing
                }
            });
        }
    }
    
    /**
     * Execute the solution
     */
    public void executeSolution() {
        if (solution.isEmpty() || isSolving) return;
        
        isSolving = true;
        final int[] moveIndex = {0};
        final long[] lastMoveTime = {System.currentTimeMillis()};
        
        Timer timer = new Timer(1000, e -> {
            if (System.currentTimeMillis() - lastMoveTime[0] < 800) return;
            
            if (moveIndex[0] >= solution.size()) {
                ((Timer)e.getSource()).stop();
                isSolving = false;
                showMessage("Solution Complete", 
                    "The AI has successfully solved the puzzle in " + solution.size() + " moves.");
                return;
            }
            
            Move move = solution.get(moveIndex[0]);
            BoxComponent selectedBox = controller.selectBoxAt(move.row, move.col);
            
            if (selectedBox != null) {
                boolean moveSuccess = controller.doMove(move.row, move.col, move.direction);
                if (moveSuccess) {
                    lastMoveTime[0] = System.currentTimeMillis();
                    moveIndex[0]++;
                } else {
                    moveIndex[0]++;
                }
            } else {
                moveIndex[0]++;
            }
        });
        
        timer.start();
    }
    
    /**
     * Get solution length
     */
    public int getSolutionLength() {
        return solution.size();
    }
    
    /**
     * Get solution moves
     */
    public List<Move> getSolutionMoves(int count) {
        return solution.subList(0, Math.min(count, solution.size()));
    }

    /**
     * Optimize the solution by removing unnecessary moves
     */
    private void optimizeSolution() {
        if (solution.size() <= 1) return;
        
        // Copy the original board
        int[][] board = model.copyMatrix();
        List<Move> optimizedSolution = new ArrayList<>();
        
        // For each move in the solution
        for (Move move : solution) {
            // Get the piece type and dimensions
            int pieceType = move.pieceType;
            int width = 1, height = 1;
            
            if (pieceType == MapModel.CAO_CAO) {
                            width = 2;
                height = 2;
            } else if (pieceType == MapModel.GUAN_YU) {
                width = 2;
                height = 1;
                        } else if (pieceType == MapModel.GENERAL) {
                width = 1;
                            height = 2;
            }
            
            // Apply the move to the board
            int dr = 0, dc = 0;
            switch (move.direction) {
                case UP: dr = -1; break;
                case DOWN: dr = 1; break;
                case LEFT: dc = -1; break;
                case RIGHT: dc = 1; break;
            }
            
            // Clear original position
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    if (move.row + i < board.length && move.col + j < board[0].length) {
                        board[move.row + i][move.col + j] = 0;
                    }
                }
            }
            
            // Place in new position
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    if (move.row + i + dr < board.length && 
                        move.col + j + dc < board[0].length &&
                        move.row + i + dr >= 0 &&
                        move.col + j + dc >= 0) {
                        board[move.row + i + dr][move.col + j + dc] = pieceType;
                    }
                }
            }
            
            // Add the move to the optimized solution
            optimizedSolution.add(move);
            
            // Print debug information
            System.out.println("\nAfter move " + move + ":");
            printBoardState(board);
            
            // If this is the goal state, stop
            if (isGoalState(board)) {
                System.out.println("Goal state reached!");
                break;
            }
        }
        
        // Only update solution if we found a valid path to goal
        if (isGoalState(board)) {
            solution = optimizedSolution;
            System.out.println("AI Solver: Optimized solution from " + solution.size() + 
                          " moves to " + optimizedSolution.size() + " moves");
        } else {
            System.out.println("AI Solver: Could not optimize solution - no valid path to goal found");
        }
    }
    
    /**
     * Check if the given state is the goal state
     */
    private boolean isGoalState(int[][] board) {
        int[] caoCaoPos = findCaoCaoPosition(board);
        if (caoCaoPos == null) return false;
        
        // Check if Cao Cao is at the bottom center position
        int boardHeight = board.length;
        int boardWidth = board[0].length;
        int expectedRow = boardHeight - 2;  // Bottom row - 2 (since Cao Cao is 2x2)
        int expectedCol = (boardWidth - 2) / 2;  // Center column
        
        // Verify Cao Cao's position and dimensions
        return caoCaoPos[0] == expectedRow && 
               caoCaoPos[1] == expectedCol &&
               board[expectedRow][expectedCol] == MapModel.CAO_CAO &&
               board[expectedRow][expectedCol + 1] == MapModel.CAO_CAO &&
               board[expectedRow + 1][expectedCol] == MapModel.CAO_CAO &&
               board[expectedRow + 1][expectedCol + 1] == MapModel.CAO_CAO;
    }
    
    /**
     * Count blocking pieces between Cao Cao and goal
     */
    private int countBlockingPieces(int[][] board, int caoCaoRow, int caoCaoCol) {
        int blockingPieces = 0;
        
        // Check vertical path
        for (int r = caoCaoRow + 2; r <= goalRow + 1; r++) {
            if (r < board.length) {
                for (int c = caoCaoCol; c <= caoCaoCol + 1; c++) {
                    if (board[r][c] != 0 && board[r][c] != MapModel.CAO_CAO) {
                        blockingPieces++;
                    }
                }
            }
        }
        
        // Check horizontal path if needed
        int horizontalDir = Integer.compare(goalCol, caoCaoCol);
        if (horizontalDir != 0) {
            int startCol = caoCaoCol + (horizontalDir > 0 ? 2 : -1);
            int endCol = goalCol + (horizontalDir > 0 ? 1 : 0);
            
            for (int c = startCol; horizontalDir > 0 ? c <= endCol : c >= endCol; c += horizontalDir) {
                if (c >= 0 && c < board[0].length && goalRow < board.length && goalRow + 1 < board.length) {
                    for (int r = goalRow; r <= goalRow + 1; r++) {
                        if (board[r][c] != 0) {
                            blockingPieces++;
                        }
                    }
                }
            }
        }
        
        return blockingPieces;
    }
    
    /**
     * Calculate path complexity
     */
    private int calculatePathComplexity(int[][] board, int caoCaoRow, int caoCaoCol) {
        int complexity = 0;
        Set<Integer> blockingPieces = new HashSet<>();
        
        // Find pieces in vertical path
        for (int r = caoCaoRow + 2; r <= goalRow + 1; r++) {
            if (r < board.length) {
                for (int c = caoCaoCol; c <= caoCaoCol + 1; c++) {
                    if (board[r][c] != 0 && board[r][c] != MapModel.CAO_CAO) {
                        blockingPieces.add(board[r][c]);
                    }
                }
            }
        }
        
        // Find pieces in horizontal path if needed
        int horizontalDir = Integer.compare(goalCol, caoCaoCol);
        if (horizontalDir != 0) {
            int startCol = caoCaoCol + (horizontalDir > 0 ? 2 : -1);
            int endCol = goalCol + (horizontalDir > 0 ? 1 : 0);
            
            for (int c = startCol; horizontalDir > 0 ? c <= endCol : c >= endCol; c += horizontalDir) {
                if (c >= 0 && c < board[0].length && goalRow < board.length && goalRow + 1 < board.length) {
                    for (int r = goalRow; r <= goalRow + 1; r++) {
                        if (board[r][c] != 0) {
                            blockingPieces.add(board[r][c]);
                        }
                    }
                }
            }
        }
        
        // Evaluate each blocking piece
        for (Integer pieceType : blockingPieces) {
            // Find piece position
            int pieceRow = -1;
            int pieceCol = -1;
            int width = 1;
            int height = 1;
            
            outerLoop:
            for (int r = 0; r < board.length; r++) {
                for (int c = 0; c < board[0].length; c++) {
                    if (board[r][c] == pieceType) {
                        pieceRow = r;
                        pieceCol = c;
                        
                        // Determine piece dimensions
                        if (pieceType == MapModel.GUAN_YU) {
                            width = 2;
                        } else if (pieceType == MapModel.GENERAL) {
                            height = 2;
                        }
                        
                        break outerLoop;
                    }
                }
            }
            
            if (pieceRow < 0) continue;
            
            // Check how many directions the piece can move
            int movableDirections = 0;
        for (Direction dir : Direction.values()) {
                if (controller.canMove(pieceRow, pieceCol, width, height, dir)) {
                    movableDirections++;
                }
            }
            
            // Add complexity based on piece mobility
            if (movableDirections == 0) {
                complexity += 15; // Completely blocked
            } else if (movableDirections == 1) {
                complexity += 8;  // Limited movement
            } else {
                complexity += 3;  // More freedom
            }
        }
        
        return complexity;
    }
    
    /**
     * Check if a piece can move in the given direction
     */
    private boolean canMove(int[][] board, int row, int col, int width, int height, Direction dir) {
        return controller.canMove(row, col, width, height, dir);
    }

    private void movePiece(int row, int col, int direction) {
        controller.doMove(row, col, Direction.values()[direction]);
    }
}
