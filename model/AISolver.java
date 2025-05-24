package model;

import java.util.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;
import javax.swing.Timer;
import controller.GameController;
import view.game.BoxComponent;

/**
 * AISolver implements an A* search algorithm to find an optimal solution for the Klotski puzzle.
 * The solver uses Manhattan distance as the heuristic function to guide the search towards the goal state.
 */
public class AISolver {
    private MapModel model;
    private GameController controller;
    private List<Move> solution;
    private boolean isSolving = false;
    private boolean isSearching = false;
    private int statesExplored = 0;

    // Search parameters
    private static final int MAX_STATES = 1000000;  // 增加最大状态数
    private static final int REPORT_INTERVAL = 100;
    private static final int MIN_STATES_TO_EXPLORE = 1000;  // 降低最小探索状态数
    private static final int MAX_DEPTH = 100;  // 降低最大深度限制

    // Constants for A* search
    private int getMaxStates() {
        int boardSize = model.getHeight() * model.getWidth();
        return Math.min(MAX_STATES, boardSize * 2000000);
    }

    private int getCacheSize() {
        return Math.min(50000, getMaxStates() / 100);
    }

    // Cache for heuristic values
    private Map<String, Integer> stateCache;

    // Cache for goal position
    private int goalRow = -1;
    private int goalCol = -1;

    /**
     * Represents a move in the puzzle
     */
    public static class Move {
        public final int row;
        public final int col;
        public final Direction direction;

        public Move(int row, int col, Direction direction) {
            this.row = row;
            this.col = col;
            this.direction = direction;
        }

        @Override
        public String toString() {
            return String.format("Move piece at [%d,%d] %s", row, col, direction);
        }
    }

    /**
     * Represents a state in the A* search
     */
    private static class State implements Comparable<State> {
        int[][] board;
        State parent;
        int g;  // Cost from start to current state
        int h;  // Heuristic value (Manhattan distance)
        int f;  // f = g + h
        Move lastMove;

        State(int[][] board, State parent, int g, int h, Move lastMove) {
            this.board = deepCopyBoard(board);
            this.parent = parent;
            this.g = g;
            this.h = h;
            this.f = g + h;
            this.lastMove = lastMove;
        }

        @Override
        public int compareTo(State other) {
            return Integer.compare(this.f, other.f);
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof State)) return false;
            State other = (State) obj;
            boolean equal = Arrays.deepEquals(this.board, other.board);
            // if (equal) {
            //     System.out.println("Found equal states:");
            //     System.out.println("State 1:");
            //     printBoard(this.board);
            //     System.out.println("State 2:");
            //     printBoard(other.board);
            // }
            return equal;
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
        this.stateCache = new LinkedHashMap<String, Integer>(getCacheSize(), 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, Integer> eldest) {
                return size() > getCacheSize();
            }
        };

        calculateGoalPosition();
    }

    /**
     * Calculate the goal position for Cao Cao based on board dimensions
     */
    private void calculateGoalPosition() {
        int boardHeight = model.getHeight();
        int boardWidth = model.getWidth();

        if (boardHeight == 5 && boardWidth == 4) {
            goalRow = 4;
            goalCol = 1;
        } else if (boardHeight == 6 && boardWidth == 4) {
            goalRow = 4;
            goalCol = 1;
        } else if (boardHeight == 6 && boardWidth == 5) {
            goalRow = 3;
            goalCol = 1;
        } else {
            goalRow = boardHeight - 2;
            goalCol = (boardWidth - 2) / 2;
        }
    }

    /**
     * Find an optimal solution using A* search algorithm
     */
    public boolean findSolution() {
        if (isSearching) {
            return false;
        }
        isSearching = true;
        solution.clear();

        // 使用当前棋盘状态而不是原始布局
        int[][] currentBoard = model.copyMatrix();
        System.out.println("\n=== Starting A* Search ===");
        System.out.println("Current board state:");
        printBoard(currentBoard);

        boolean result = performAStarSearch();

        isSearching = false;
        return result;
    }

    /**
     * Perform A* search to find the optimal solution
     */
    private boolean performAStarSearch() {
        int[][] initialBoard = model.copyMatrix();  // 使用当前棋盘状态

        PriorityQueue<State> openSet = new PriorityQueue<>();
        Set<String> closedSet = new HashSet<>();
        Map<String, State> stateMap = new HashMap<>();

        State initialState = new State(initialBoard, null, 0, calculateHeuristic(initialBoard), null);
        openSet.add(initialState);
        String initialStateStr = boardToString(initialBoard);
        stateMap.put(initialStateStr, initialState);

        int statesExplored = 0;
        long startTime = System.currentTimeMillis();
        State lastExploredState = null;
        State bestState = null;
        int bestHeuristic = Integer.MAX_VALUE;
        int lastBestHeuristic = Integer.MAX_VALUE;

        while (!openSet.isEmpty() && statesExplored < getMaxStates()) {
            State current = openSet.poll();
            lastExploredState = current;
            statesExplored++;

            // Update best state if current state has better heuristic
            if (current.h < bestHeuristic) {
                bestState = current;
                lastBestHeuristic = bestHeuristic;
                bestHeuristic = current.h;
                System.out.println("\n=== Found Better Solution ===");
                System.out.println("Step: " + statesExplored);
                System.out.println("Previous best heuristic: " + lastBestHeuristic);
                System.out.println("New best heuristic: " + bestHeuristic);
                System.out.println("Improvement: " + (lastBestHeuristic - bestHeuristic));
                if (current.lastMove != null) {
                    System.out.println("Move: " + current.lastMove);
                }
                printBoard(current.board);
            }

            if (statesExplored % REPORT_INTERVAL == 0 || statesExplored == 1) {
                // System.out.println("\nStep " + statesExplored + ":");
                // System.out.println("Current heuristic: " + current.h);
                // System.out.println("Best heuristic so far: " + bestHeuristic);
                // if (current.lastMove != null) {
                //     System.out.println("Move: " + current.lastMove);
                // }
                // printBoard(current.board);
            }

            if (isGoalState(current.board)) {
                System.out.println("\n=== Goal State Found! ===");
                System.out.println("Total steps: " + statesExplored);
                System.out.println("Final heuristic: " + current.h);
                System.out.println("Final board state:");
                printBoard(current.board);
                solution = reconstructPath(current);
                return true;
            }

            String currentStateStr = boardToString(current.board);
            if (closedSet.contains(currentStateStr)) {
                continue;
            }
            closedSet.add(currentStateStr);

            List<State> nextStates = generateNextStates(current);

            for (State next : nextStates) {
                String nextStateStr = boardToString(next.board);

                if (closedSet.contains(nextStateStr)) {
                    continue;
                }

                State existingState = stateMap.get(nextStateStr);
                if (existingState != null) {
                    if (next.g < existingState.g) {
                        openSet.remove(existingState);
                        openSet.add(next);
                        stateMap.put(nextStateStr, next);
                    }
                } else {
                    openSet.add(next);
                    stateMap.put(nextStateStr, next);
                }
            }

            if (System.currentTimeMillis() - startTime > 30000) {
                System.out.println("\nSearch timeout after " + statesExplored + " states");
                if (bestState != null) {
                    System.out.println("\n=== Best Solution Found ===");
                    System.out.println("Total steps: " + statesExplored);
                    System.out.println("Best heuristic: " + bestHeuristic);
                    if (bestState.lastMove != null) {
                        System.out.println("Last move: " + bestState.lastMove);
                    }
                    printBoard(bestState.board);
                }
                return false;
            }
        }

        System.out.println("\n=== Search Failed ===");
        System.out.println("Search exceeded state limit of " + getMaxStates());
        System.out.println("States explored: " + statesExplored);

        // Print the best state found
        if (bestState != null) {
            System.out.println("\n=== Best Solution Found ===");
            System.out.println("Total steps: " + statesExplored);
            System.out.println("Best heuristic: " + bestHeuristic);
            if (bestState.lastMove != null) {
                System.out.println("Last move: " + bestState.lastMove);
            }
            printBoard(bestState.board);
        }

        return false;
    }

    private static void printBoard(int[][] board) {
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[0].length; j++) {
                System.out.print(String.format("%2d ", board[i][j]));
            }
            System.out.println();
        }
        System.out.println();
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
     * Generate next possible states from current state
     */
    private List<State> generateNextStates(State current) {
        List<State> nextStates = new ArrayList<>();
        int[][] board = current.board;

        // Try to move all pieces
        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[0].length; col++) {
                int pieceType = board[row][col];
                if (pieceType == 0) continue;  // Skip empty cells

                // For soldiers, we need to try each one individually
                if (pieceType == MapModel.SOLDIER) {
                    // Try moving in all directions
                    for (Direction direction : Direction.values()) {
                        if (canMove(board, row, col, 1, 1, direction)) {
                            // Create a new board for this move
                            int[][] newBoard = deepCopyBoard(board);
                            Move move = new Move(row, col, direction);

                            // Calculate new position
                            int newRow = row;
                            int newCol = col;
                            switch (direction) {
                                case UP: newRow--; break;
                                case DOWN: newRow++; break;
                                case LEFT: newCol--; break;
                                case RIGHT: newCol++; break;
                            }

                            // Clear old position
                            newBoard[row][col] = 0;

                            // Set new position
                            newBoard[newRow][newCol] = pieceType;

                            nextStates.add(new State(newBoard, current, current.g + 1,
                                    calculateHeuristic(newBoard),
                                    move));
                        }
                    }
                    continue;
                }

                // For generals, we need to try each one individually
                if (pieceType == MapModel.GENERAL) {
                    // Check if this is a general piece
                    if (row + 1 < board.length && board[row + 1][col] == MapModel.GENERAL) {
                        // Try moving in all directions
                        for (Direction direction : Direction.values()) {
                            if (canMove(board, row, col, 1, 2, direction)) {
                                // Create a new board for this move
                                int[][] newBoard = deepCopyBoard(board);
                                Move move = new Move(row, col, direction);

                                // Calculate new position
                                int newRow = row;
                                int newCol = col;
                                switch (direction) {
                                    case UP: newRow--; break;
                                    case DOWN: newRow++; break;
                                    case LEFT: newCol--; break;
                                    case RIGHT: newCol++; break;
                                }

                                // Clear old positions
                                newBoard[row][col] = 0;
                                newBoard[row+1][col] = 0;

                                // Set new positions
                                newBoard[newRow][newCol] = pieceType;
                                newBoard[newRow+1][newCol] = pieceType;

                                nextStates.add(new State(newBoard, current, current.g + 1,
                                        calculateHeuristic(newBoard),
                                        move));
                            }
                        }
                    }
                    continue;
                }

                // For other pieces, skip if this is not the top-left corner
                if (row > 0 && board[row-1][col] == pieceType) {
                    continue;
                }
                if (col > 0 && board[row][col-1] == pieceType) {
                    continue;
                }

                // Get piece dimensions
                int width = 1;
                int height = 1;

                // Determine piece dimensions
                if (pieceType == MapModel.CAO_CAO) {
                    width = 2;
                    height = 2;
                } else if (pieceType == MapModel.GUAN_YU) {
                    width = 2;
                    height = 1;
                } else if (pieceType == MapModel.ZHOU_YU) {
                    width = 3;
                    height = 1;
                }

                // Try moving in all directions
                for (Direction direction : Direction.values()) {
                    if (canMove(board, row, col, width, height, direction)) {
                        // Create a new board for this move
                        int[][] newBoard = deepCopyBoard(board);
                        Move move = new Move(row, col, direction);

                        // Calculate new position
                        int newRow = row;
                        int newCol = col;
                        switch (direction) {
                            case UP: newRow--; break;
                            case DOWN: newRow++; break;
                            case LEFT: newCol--; break;
                            case RIGHT: newCol++; break;
                        }

                        // Clear old positions
                        for (int r = 0; r < height; r++) {
                            for (int c = 0; c < width; c++) {
                                newBoard[row + r][col + c] = 0;
                            }
                        }

                        // Set new positions
                        for (int r = 0; r < height; r++) {
                            for (int c = 0; c < width; c++) {
                                newBoard[newRow + r][newCol + c] = pieceType;
                            }
                        }

                        nextStates.add(new State(newBoard, current, current.g + 1,
                                calculateHeuristic(newBoard),
                                move));
                    }
                }
            }
        }

        return nextStates;
    }

    /**
     * Reconstruct path from goal state to initial state
     */
    private List<Move> reconstructPath(State goalState) {
        List<Move> path = new ArrayList<>();
        State current = goalState;

        while (current.parent != null) {
            path.add(0, current.lastMove);
            current = current.parent;
        }

        return path;
    }

    /**
     * Check if the current state is a goal state
     */
    private boolean isGoalState(int[][] board) {
        int[] caoCaoPos = findCaoCao(board);
        if (caoCaoPos == null) {
            return false;
        }

        // Check if Cao Cao is at the bottom of the board
        // For a 2x2 piece, the bottom position should be at board.length - 2
        if (caoCaoPos[0] == board.length - 2) {
            // Calculate the middle position for the board width
            int middleCol = (board[0].length - 2) / 2;

            // Check if Cao Cao is in the middle position
            if (caoCaoPos[1] == middleCol) {
                // Verify that Cao Cao completely covers the bottom middle area
                for (int r = board.length - 2; r < board.length; r++) {
                    for (int c = middleCol; c < middleCol + 2; c++) {
                        if (board[r][c] != MapModel.CAO_CAO) {
                            return false;
                        }
                    }
                }
                return true;
            }
        }

        return false;
    }

    /**
     * Find Cao Cao's position in the board
     */
    private int[] findCaoCao(int[][] board) {
        for (int i = 0; i < board.length - 1; i++) {
            for (int j = 0; j < board[0].length - 1; j++) {
                if (board[i][j] == MapModel.CAO_CAO &&
                        board[i][j+1] == MapModel.CAO_CAO &&
                        board[i+1][j] == MapModel.CAO_CAO &&
                        board[i+1][j+1] == MapModel.CAO_CAO) {
                    return new int[]{i, j};
                }
            }
        }
        return null;
    }

    /**
     * Convert board state to string for hashing
     */
    private String boardToString(int[][] board) {
        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < board.length; r++) {
            for (int c = 0; c < board[0].length; c++) {
                sb.append(board[r][c]).append(",");
            }
        }
        return sb.toString();
    }

    /**
     * Deep copy a board state
     */
    private static int[][] deepCopyBoard(int[][] board) {
        int[][] copy = new int[board.length][board[0].length];
        for (int i = 0; i < board.length; i++) {
            System.arraycopy(board[i], 0, copy[i], 0, board[i].length);
        }
        return copy;
    }

    /**
     * Execute the solution move by move with animation
     */
    public void executeSolution() {
        if (solution.isEmpty() || isSolving) {
            return;
        }

        isSolving = true;
        final int[] moveIndex = {0};
        final long[] lastMoveTime = {System.currentTimeMillis()};

        Timer timer = new Timer(1000, e -> {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastMoveTime[0] < 800) {
                return;
            }

            if (moveIndex[0] >= solution.size()) {
                ((Timer)e.getSource()).stop();
                isSolving = false;

                JOptionPane.showMessageDialog(
                        null,
                        "<html><h2>Solution Complete!</h2>" +
                                "The AI has successfully solved the puzzle in " + solution.size() + " moves.</html>",
                        "AI Solution Complete",
                        JOptionPane.INFORMATION_MESSAGE
                );

                return;
            }

            Move move = solution.get(moveIndex[0]);
            
            // 获取当前棋盘状态
            int[][] currentBoard = model.copyMatrix();
            
            // 打印解决方案中的这一步
            System.out.println("\n=== Step " + moveIndex[0] + " in Solution ===");
            System.out.println("Move: " + move);
            System.out.println("Expected board state:");
            printBoard(currentBoard);
            
            // 打印实际棋盘状态
            System.out.println("\nActual board state:");
            printBoard(model.copyMatrix());
            
            // 验证当前位置的方块
            BoxComponent selectedBox = controller.selectBoxAt(move.row, move.col);
            if (selectedBox == null) {
                System.out.println("\nError: Could not select box at [" + move.row + "," + move.col + "]");
                if (findSolution()) {
                    System.out.println("Recalculated solution with " + solution.size() + " moves");
                    moveIndex[0] = 0;
                    return;
                } else {
                    System.out.println("Error: Could not find new solution after failed move");
                    ((Timer)e.getSource()).stop();
                    isSolving = false;
                    JOptionPane.showMessageDialog(
                            null,
                            "Failed to execute solution. The puzzle state has changed.",
                            "AI Solution Failed",
                            JOptionPane.ERROR_MESSAGE
                    );
                    return;
                }
            }

            boolean moveSuccess = controller.doMove(move.row, move.col, move.direction);
            if (moveSuccess) {
                lastMoveTime[0] = System.currentTimeMillis();
                moveIndex[0]++;
            } else {
                System.out.println("\nWarning: Move failed at step " + moveIndex[0]);
                if (findSolution()) {
                    System.out.println("Recalculated solution with " + solution.size() + " moves");
                    moveIndex[0] = 0;
                } else {
                    System.out.println("Error: Could not find new solution after failed move");
                    ((Timer)e.getSource()).stop();
                    isSolving = false;
                    JOptionPane.showMessageDialog(
                            null,
                            "Failed to execute solution. The puzzle state has changed.",
                            "AI Solution Failed",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        });

        timer.start();
    }

    /**
     * Get the length of the current solution
     */
    public int getSolutionLength() {
        return solution.size();
    }

    /**
     * Get a specific number of moves from the solution
     */
    public List<Move> getSolutionMoves(int count) {
        int movesToReturn = Math.min(count, solution.size());
        return solution.subList(0, movesToReturn);
    }

    private int getPieceWidth(int pieceType) {
        switch (pieceType) {
            case MapModel.CAO_CAO: return 2;  // 2x2
            case MapModel.GUAN_YU: return 2;  // 2x1
            case MapModel.GENERAL: return 1;  // 1x2
            case MapModel.SOLDIER: return 1;  // 1x1
            case MapModel.ZHOU_YU: return 3;  // 3x1
            default: return 1;
        }
    }

    private int getPieceHeight(int pieceType) {
        switch (pieceType) {
            case MapModel.CAO_CAO: return 2;  // 2x2
            case MapModel.GUAN_YU: return 1;  // 2x1
            case MapModel.GENERAL: return 2;  // 1x2
            case MapModel.SOLDIER: return 1;  // 1x1
            case MapModel.ZHOU_YU: return 1;  // 3x1
            default: return 1;
        }
    }

    private boolean canMove(int[][] board, int row, int col, int width, int height, Direction direction) {
        // Check if position is out of bounds
        if (row < 0 || col < 0 || row >= board.length || col >= board[0].length) {
            return false;
        }

        // Check if there is a piece at the position
        int pieceType = board[row][col];
        if (pieceType == 0 || pieceType == MapModel.BLOCKED || pieceType == MapModel.MILITARY_CAMP) {
            return false;
        }

        // Verify piece dimensions
        if (row + height > board.length || col + width > board[0].length) {
            return false;
        }

        // Verify piece integrity
        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                if (board[row + r][col + c] != pieceType) {
                    return false;
                }
            }
        }

        // Calculate new position
        int newRow = row;
        int newCol = col;
        switch (direction) {
            case UP: newRow--; break;
            case DOWN: newRow++; break;
            case LEFT: newCol--; break;
            case RIGHT: newCol++; break;
        }

        // Check if new position is within bounds
        if (newRow < 0 || newRow + height > board.length ||
                newCol < 0 || newCol + width > board[0].length) {
            return false;
        }

        // Check if target area is empty and valid
        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                // Skip checking the original piece's position
                if (newRow + r >= row && newRow + r < row + height &&
                        newCol + c >= col && newCol + c < col + width) {
                    continue;
                }

                int targetCell = board[newRow + r][newCol + c];

                // Check for blocked cells and military camps - cannot move onto or through them
                if (targetCell == MapModel.BLOCKED || targetCell == MapModel.MILITARY_CAMP) {
                    // Only soldiers can step on military camps
                    if (targetCell == MapModel.MILITARY_CAMP && pieceType == MapModel.SOLDIER) {
                        continue;
                    }
                    return false;
                }

                // For generals, we need to check both positions
                if (pieceType == MapModel.GENERAL) {
                    if (direction == Direction.LEFT || direction == Direction.RIGHT) {
                        // For horizontal movement, check both rows
                        if (targetCell != 0) {
                            return false;
                        }
                    } else {
                        // For vertical movement, check both columns
                        if (targetCell != 0) {
                            return false;
                        }
                    }
                }
                // For Zhou Yu, we need to check all three positions
                else if (pieceType == MapModel.ZHOU_YU) {
                    if (direction == Direction.UP || direction == Direction.DOWN) {
                        // For vertical movement, check all three columns
                        if (targetCell != 0) {
                            return false;
                        }
                    } else {
                        // For horizontal movement, check all three positions
                        if (targetCell != 0) {
                            return false;
                        }
                    }
                }
                // For other pieces
                else {
                    if (targetCell != 0) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private void applyMove(int[][] board, Move move) {
        int row = move.row;
        int col = move.col;
        Direction direction = move.direction;

        // Get the block type and dimensions
        int blockType = board[row][col];
        int width = 1;
        int height = 1;

        // Determine piece dimensions
        if (blockType == MapModel.CAO_CAO) {
            width = 2;
            height = 2;
        } else if (blockType == MapModel.GUAN_YU) {
            width = 2;
            height = 1;
        } else if (blockType == MapModel.GENERAL) {
            width = 1;
            height = 2;
        } else if (blockType == MapModel.ZHOU_YU) {
            width = 3;
            height = 1;
        }

        // Calculate new position
        int nextRow = row;
        int nextCol = col;
        switch (direction) {
            case UP:
                nextRow = row - 1;
                break;
            case DOWN:
                nextRow = row + 1;
                break;
            case LEFT:
                nextCol = col - 1;
                break;
            case RIGHT:
                nextCol = col + 1;
                break;
        }

        // Debug output before move
        System.out.println("\nBefore move:");
        System.out.println("Moving piece type " + blockType + " (" + width + "x" + height + ")");
        System.out.println("From [" + row + "," + col + "] to [" + nextRow + "," + nextCol + "]");
        printBoard(board);

        // Create a temporary board to store the new state
        int[][] tempBoard = deepCopyBoard(board);

        // Clear old positions
        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                if (row + r < board.length && col + c < board[0].length) {
                    tempBoard[row + r][col + c] = 0;
                }
            }
        }

        // Set new positions
        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                if (nextRow + r < board.length && nextCol + c < board[0].length) {
                    tempBoard[nextRow + r][nextCol + c] = blockType;
                }
            }
        }

        // Verify piece integrity in new position
        boolean isValid = true;
        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                if (tempBoard[nextRow + r][nextCol + c] != blockType) {
                    isValid = false;
                    System.out.println("ERROR: Piece integrity check failed at [" + (nextRow + r) + "," + (nextCol + c) + "]");
                    break;
                }
            }
            if (!isValid) break;
        }

        if (isValid) {
            // Copy the temporary board back to the original board
            for (int i = 0; i < board.length; i++) {
                System.arraycopy(tempBoard[i], 0, board[i], 0, board[i].length);
            }
            System.out.println("Move successful - piece integrity maintained");
        } else {
            System.out.println("ERROR: Piece integrity check failed after move!");
            System.out.println("Keeping original board state");
        }

        // Debug output after move
        System.out.println("\nAfter move:");
        printBoard(board);
    }

    /**
     * Check if the initial layout is solvable
     * @return true if the layout is solvable, false otherwise
     */
    public boolean isSolvable() {
        int[][] board = model.copyMatrix();

        // First check if Cao Cao exists on the board
        int[] caoCaoPos = findCaoCao(board);
        if (caoCaoPos == null) {
            System.out.println("Cao Cao not found on the board");
            return false;
        }

        // Then check if Cao Cao can move in any direction
        boolean canMove = false;
        for (Direction dir : Direction.values()) {
            if (canMove(board, caoCaoPos[0], caoCaoPos[1], 2, 2, dir)) {
                canMove = true;
                System.out.println("Cao Cao can move " + dir);
                break;
            }
        }

        if (!canMove) {
            System.out.println("Cao Cao cannot move in any direction");
            return false;
        }

        // Check if there's a potential path to the goal
        // We don't need to check for a completely clear path,
        // as other pieces can be moved to make way for Cao Cao
        int minRow = Math.min(caoCaoPos[0], goalRow);
        int maxRow = Math.max(caoCaoPos[0], goalRow);
        int minCol = Math.min(caoCaoPos[1], goalCol);
        int maxCol = Math.max(caoCaoPos[1], goalCol);

        // Check if there are any immovable obstacles in the way
        for (int i = minRow; i <= maxRow; i++) {
            for (int j = minCol; j <= maxCol; j++) {
                if (board[i][j] == MapModel.BLOCKED) {
                    System.out.println("Found immovable obstacle at [" + i + "," + j + "]");
                    return false;
                }
            }
        }

        // Check if there's enough space for Cao Cao to move
        int emptySpaces = 0;
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[0].length; j++) {
                if (board[i][j] == 0) {
                    emptySpaces++;
                }
            }
        }

        // We need at least 4 empty spaces for Cao Cao to move
        if (emptySpaces < 4) {
            System.out.println("Not enough empty spaces for Cao Cao to move");
            return false;
        }

        System.out.println("Layout is solvable");
        return true;
    }

    /**
     * Find Cao Cao's position in the board
     */
    private int[] findCaoCaoPosition(int[][] board) {
        for (int r = 0; r < board.length; r++) {
            for (int c = 0; c < board[0].length; c++) {
                if (board[r][c] == 1) {
                    return new int[]{r, c};
                }
            }
        }
        return null;
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

        // Check horizontal conflicts
        for (int c = 0; c < board[0].length; c++) {
            if (c != caoCaoCol && board[caoCaoRow][c] != 0) {
                conflicts++;
            }
        }

        // Check vertical conflicts
        for (int r = 0; r < board.length; r++) {
            if (r != caoCaoRow && board[r][caoCaoCol] != 0) {
                conflicts++;
            }
        }

        return conflicts;
    }

    /**
     * Calculate pattern score
     */
    private int calculatePatternScore(int[][] board, int caoCaoRow, int caoCaoCol) {
        int score = 0;

        // Check if Cao Cao is in a corner
        if ((caoCaoRow == 0 || caoCaoRow == board.length - 2) &&
                (caoCaoCol == 0 || caoCaoCol == board[0].length - 2)) {
            score += 5;
        }

        // Check if Cao Cao is aligned with goal
        if (caoCaoRow == goalRow) {
            score += 3;
        }
        if (caoCaoCol == goalCol) {
            score += 3;
        }

        return score;
    }

    /**
     * Calculate path score
     */
    private int calculatePathScore(int[][] board, int caoCaoRow, int caoCaoCol) {
        int score = 0;

        // Check if there's a clear path to the goal
        boolean hasClearPath = true;

        // Check horizontal path
        int startCol = Math.min(caoCaoCol, goalCol);
        int endCol = Math.max(caoCaoCol, goalCol);
        for (int c = startCol; c <= endCol; c++) {
            if (board[goalRow][c] != 0) {
                hasClearPath = false;
                break;
            }
        }

        if (hasClearPath) {
            score += 10;
        }

        // Check vertical path
        hasClearPath = true;
        int startRow = Math.min(caoCaoRow, goalRow);
        int endRow = Math.max(caoCaoRow, goalRow);
        for (int r = startRow; r <= endRow; r++) {
            if (board[r][goalCol] != 0) {
                hasClearPath = false;
                break;
            }
        }

        if (hasClearPath) {
            score += 10;
        }

        return score;
    }
}