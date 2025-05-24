package controller;

// Resolve ambiguous imports by using explicit imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Toolkit;
import model.Direction;
import model.MapModel;
import model.Prop;
import view.game.AnimationHandler;
import view.game.BoxComponent;
import view.game.GamePanel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import model.AutoSaveManager;
import model.SaveFileValidator;
import java.awt.Frame;
import org.json.JSONObject;
import org.json.JSONArray;
import java.io.FileWriter;
import java.nio.file.Files;
import model.LeaderboardManager;
import model.Difficulty;

/**
 * It is a bridge to combine GamePanel(view) and MapMatrix(model) in one game.
 * You can design several methods about the game logic in this class.
 */
public class GameController {
    private final GamePanel view;
    private MapModel model;
    private Stack<int[][]> moveHistory;
    private Stack<int[][]> redoHistory;
    private String currentUser;
    private int currentLevel;
    private int moveCount;
    private boolean isAISolving;
    private LeaderboardManager leaderboardManager;

    // Props management
    private Map<Prop.PropType, Prop> availableProps = new HashMap<>();
    private ArrayList<int[]> removedObstacles = new ArrayList<>(); // [row, col, stepsRemaining]

    private AutoSaveManager autoSaveManager;

    public GameController(GamePanel view, MapModel model) {
        this.moveHistory = new Stack<>();
        this.redoHistory = new Stack<>();
        this.view = view;
        this.model = model;
        this.currentLevel = 0; // Default to first level
        this.moveCount = 0;
        this.isAISolving = false;
        this.leaderboardManager = new LeaderboardManager();
        view.setController(this);
    }

    public int getMoveCount() {
        return moveCount;
    }

    public void setCurrentUser(String username) {
        this.currentUser = username;
        if (username != null && !username.isEmpty()) {
            this.autoSaveManager = new AutoSaveManager(username, this);
            this.autoSaveManager.startAutoSave();
        }
    }

    public void setLevel(int level) {
        // Reset game with specified level
        this.currentLevel = level;
        this.model = new MapModel(level);
        this.moveCount = 0;
        this.moveHistory.clear();
        this.redoHistory.clear();
        moveHistory.push(model.copyMatrix());
        view.resetBoard(model.getMatrix());
        view.updateMoveCount(0);

        // Add debug logging to help trace prop initialization
        System.out.println("GameController: Setting level to " + level +
                " (" + MapModel.LEVEL_NAMES[level] + ")");
        System.out.println("Props allowed for this level: " + MapModel.LEVEL_PROPS_ALLOWED[level]);

        // Always initialize props, even if they're not allowed for this level
        // This ensures consistent behavior across all levels
        initializeProps(level);

        // Find the GameFrame and update the prop panel
        view.game.GameFrame gameFrame = null;
        if (view.getParent() != null && view.getParent().getParent() instanceof view.game.GameFrame) {
            gameFrame = (view.game.GameFrame) view.getParent().getParent();

            // Force update of prop panel visibility and contents
            gameFrame.updatePropPanelVisibility(level);

            // Log prop availability after initialization
            System.out.println("After initialization - Prop counts: " +
                    "Hint: " + getPropCount(Prop.PropType.HINT) + ", " +
                    "Time Bonus: " + getPropCount(Prop.PropType.TIME_BONUS) + ", " +
                    "Obstacle Remover: " + getPropCount(Prop.PropType.OBSTACLE_REMOVER));
        } else {
            System.out.println("Warning: Could not find GameFrame to update prop panel");
        }

        view.requestFocusInWindow();
    }

    /**
     * Initialize props based on the difficulty level
     *
     * @param level The difficulty level (0-3)
     */
    public void initializeProps(int level) {
        availableProps.clear();
        removedObstacles.clear();

        // Check if props are allowed for this level
        if (!MapModel.LEVEL_PROPS_ALLOWED[level]) {
            return; // No props for Easy and Master levels
        }

        // Initialize props based on difficulty
        if (level == 1) { // Hard level
            availableProps.put(Prop.PropType.HINT, new Prop(Prop.PropType.HINT, 2));
            availableProps.put(Prop.PropType.TIME_BONUS, new Prop(Prop.PropType.TIME_BONUS, 3));
            availableProps.put(Prop.PropType.OBSTACLE_REMOVER, new Prop(Prop.PropType.OBSTACLE_REMOVER, 1));
        } else if (level == 2) { // Expert level
            availableProps.put(Prop.PropType.HINT, new Prop(Prop.PropType.HINT, 1));
            availableProps.put(Prop.PropType.TIME_BONUS, new Prop(Prop.PropType.TIME_BONUS, 2));
            availableProps.put(Prop.PropType.OBSTACLE_REMOVER, new Prop(Prop.PropType.OBSTACLE_REMOVER, 2));
        }
    }

    /**
     * Check if a prop is available for use
     *
     * @param type The prop type to check
     * @return true if the prop is available
     */
    public boolean isPropAvailable(Prop.PropType type) {
        if (!MapModel.LEVEL_PROPS_ALLOWED[currentLevel]) {
            return false;
        }

        Prop prop = availableProps.get(type);
        return prop != null && prop.isAvailable();
    }

    /**
     * Get the count of a specific prop
     *
     * @param type The prop type to get
     * @return The number of props available, or 0 if none
     */
    public int getPropCount(Prop.PropType type) {
        Prop prop = availableProps.get(type);
        return prop != null ? prop.getCount() : 0;
    }

    public void restartGame() {
        restartGame(currentLevel);
    }

    public void restartGame(int level) {
        // Reset to specified level's initial board state
        this.currentLevel = level;
        this.model = new MapModel(level);
        this.moveCount = 0;
        this.moveHistory.clear();
        this.redoHistory.clear();
        // Save initial state to allow undo back to start
        moveHistory.push(model.copyMatrix());
        view.resetBoard(model.getMatrix());
        view.updateMoveCount(0);

        // Always initialize props for the level, regardless of whether props are allowed
        // This ensures prop counts are properly set for all difficulty levels
        initializeProps(level);

        // Ensure prop panel is visible and properly initialized in GameFrame
        if (view.getParent() != null && view.getParent().getParent() instanceof view.game.GameFrame) {
            view.game.GameFrame gameFrame = (view.game.GameFrame) view.getParent().getParent();

            // Force update of prop panel visibility and contents
            gameFrame.updatePropPanelVisibility(level);

            // Log prop panel status for debugging
            System.out.println("GameController: Updated prop panel for level " + level +
                    " (" + MapModel.LEVEL_NAMES[level] + ")");
            System.out.println("Prop availability: " +
                    "Hint: " + getPropCount(Prop.PropType.HINT) + ", " +
                    "Time Bonus: " + getPropCount(Prop.PropType.TIME_BONUS) + ", " +
                    "Obstacle Remover: " + getPropCount(Prop.PropType.OBSTACLE_REMOVER));
        }

        view.requestFocusInWindow();
    }


    public int getLevelCount() {
        return MapModel.LEVELS.length;
    }

    public boolean undoMove() {
        if (moveHistory.isEmpty() || moveCount <= 0) {
            return false;
        }

        int[][] previousState = moveHistory.pop();
        this.model = new MapModel(previousState);
        this.moveCount--;
        view.resetBoard(previousState);
        view.updateMoveCount(moveCount);
        return true;
    }

    private boolean canMove(int row, int col, int width, int height, Direction direction) {
        // System.err.println("Checking move from ["+row+"]["+col+"] size "+width+"x"+height+" dir "+direction);

        // Get the block type being moved
        int blockType = model.getId(row, col);

        // Check boundaries first
        if (direction == Direction.UP && row == 0) return false;
        if (direction == Direction.DOWN && row + height >= model.getHeight()) return false;
        if (direction == Direction.LEFT && col == 0) return false;
        if (direction == Direction.RIGHT && col + width >= model.getWidth()) return false;

        // Debug info for military camps
        System.out.println("canMove check: blockType=" + blockType + ", direction=" + direction);

        // Check all cells in movement direction
        if (direction == Direction.UP) {
            for (int c = col; c < col + width; c++) {
                int targetCell = model.getId(row - 1, c);
                System.out.println("  Checking UP at [" + (row-1) + "," + c + "] = " + targetCell);

                // Only block movement if target cell is occupied by another piece
                if (targetCell > 0 && targetCell < MapModel.BLOCKED) return false;

                // Only block movement for BLOCKED cells (always immovable)
                if (targetCell == MapModel.BLOCKED) return false;

                // Military camps: allow soldiers to move onto them, block others
                if (targetCell == MapModel.MILITARY_CAMP) {
                    if (blockType == MapModel.SOLDIER) {
                        System.out.println("  Allowing soldier to move onto military camp");
                    } else {
                        System.out.println("  Blocking non-soldier from moving onto military camp");
                        return false;
                    }
                }
            }
        }
        else if (direction == Direction.DOWN) {
            for (int c = col; c < col + width; c++) {
                int targetCell = model.getId(row + height, c);
                System.out.println("  Checking DOWN at [" + (row+height) + "," + c + "] = " + targetCell);

                // Only block movement if target cell is occupied by another piece
                if (targetCell > 0 && targetCell < MapModel.BLOCKED) return false;

                // Only block movement for BLOCKED cells (always immovable)
                if (targetCell == MapModel.BLOCKED) return false;

                // Military camps: allow soldiers to move onto them, block others
                if (targetCell == MapModel.MILITARY_CAMP) {
                    if (blockType == MapModel.SOLDIER) {
                        System.out.println("  Allowing soldier to move onto military camp");
                    } else {
                        System.out.println("  Blocking non-soldier from moving onto military camp");
                        return false;
                    }
                }
            }
        }
        else if (direction == Direction.LEFT) {
            for (int r = row; r < row + height; r++) {
                int targetCell = model.getId(r, col - 1);
                System.out.println("  Checking LEFT at [" + r + "," + (col-1) + "] = " + targetCell);

                // Only block movement if target cell is occupied by another piece
                if (targetCell > 0 && targetCell < MapModel.BLOCKED) return false;

                // Only block movement for BLOCKED cells (always immovable)
                if (targetCell == MapModel.BLOCKED) return false;

                // Military camps: allow soldiers to move onto them, block others
                if (targetCell == MapModel.MILITARY_CAMP) {
                    if (blockType == MapModel.SOLDIER) {
                        System.out.println("  Allowing soldier to move onto military camp");
                    } else {
                        System.out.println("  Blocking non-soldier from moving onto military camp");
                        return false;
                    }
                }
            }
        }
        else if (direction == Direction.RIGHT) {
            for (int r = row; r < row + height; r++) {
                int targetCell = model.getId(r, col + width);
                System.out.println("  Checking RIGHT at [" + r + "," + (col+width) + "] = " + targetCell);

                // Only block movement if target cell is occupied by another piece
                if (targetCell > 0 && targetCell < MapModel.BLOCKED) return false;

                // Only block movement for BLOCKED cells (always immovable)
                if (targetCell == MapModel.BLOCKED) return false;

                // Military camps: allow soldiers to move onto them, block others
                if (targetCell == MapModel.MILITARY_CAMP) {
                    if (blockType == MapModel.SOLDIER) {
                        System.out.println("  Allowing soldier to move onto military camp");
                    } else {
                        System.out.println("  Blocking non-soldier from moving onto military camp");
                        return false;
                    }
                }
            }
        }

        // System.out.println("Move valid");
        return true;
    }

    public boolean doMove(int row, int col, Direction direction) {
        // Get the block type at the current position
        int blockType = model.getId(row, col);

        // Determine block dimensions based on type
        int width = 1;
        int height = 1;
        final boolean isGeneral = blockType == MapModel.GENERAL; // Make final for lambda usage

        if (blockType == MapModel.CAO_CAO) { // 2x2 block
            width = 2;
            height = 2;
        } else if (blockType == MapModel.GUAN_YU) { // 2x1 block
            width = 2;
            height = 1;
        } else if (blockType == MapModel.GENERAL) { // 1x2 block
            width = 1;
            height = 2;
        } else if (blockType == MapModel.ZHOU_YU) { // 1x3 block
            width = 3;
            height = 1;
        } else if (blockType == MapModel.BLOCKED || blockType == MapModel.MILITARY_CAMP) { // Immovable pieces
            return false;
        }

        boolean canMove = canMove(row, col, width, height, direction);

        // Find and select the box component for this position
        BoxComponent box = findBoxAtPosition(row, col);
        if (box == null) {
            System.out.println("Warning: No box component found for move at [" + row + "," + col + "]");
            return false;
        }

        // Select the box before attempting to move
        view.setSelectedBox(box);
        final BoxComponent selectedBox = view.getSelectedBox();

        if (canMove) {
            // Calculate new top-left position
            final int nextRow = row + direction.getRowOffset();
            final int nextCol = col + direction.getColOffset();

            // Save current matrix state before modifying
            final int[][] originalMatrix = model.copyMatrix();

            // Store original position for animations
            final int originalX = selectedBox.getX();
            final int originalY = selectedBox.getY();

            // Update the component's logical position
            selectedBox.setRow(nextRow);
            selectedBox.setCol(nextCol);

            // Clear the old positions in the model
            clearOldPositions(row, col, width, height);

            // Set the new positions in the model
            setNewPositions(nextRow, nextCol, width, height, blockType);

            // Fixed offset values for consistent positioning
            int xOffset = view.getWidth() / 2 - (model.getWidth() * view.getGRID_SIZE()) / 2;
            int yOffset = 100; // Fixed vertical padding value

            // Ensure minimum offset values
            if (xOffset < 10) xOffset = 10;
            if (yOffset < 10) yOffset = 10;

            // Calculate precise target position - use EXACT grid size value
            int GRID_SIZE = view.getGRID_SIZE();
            System.out.println("GRID_SIZE = " + GRID_SIZE);

            int targetX = xOffset + nextCol * GRID_SIZE;
            int targetY = yOffset + nextRow * GRID_SIZE;

            // Debug positioning calculation
            System.out.println("Target position: (" + targetX + "," + targetY + ")");
            System.out.println("Current position: (" + selectedBox.getX() + "," + selectedBox.getY() + ")");
            System.out.println("Movement delta: (" + (targetX - selectedBox.getX()) + "," + (targetY - selectedBox.getY()) + ")");

            // Force minimum movement delta based on direction
            // This ensures animation always has a meaningful distance to travel
            int minDelta = GRID_SIZE / 2; // Minimum 35px movement

            // Set direction-specific deltas
            if (direction == Direction.LEFT && targetX >= selectedBox.getX()) {
                targetX = selectedBox.getX() - GRID_SIZE;
                System.out.println("Forcing LEFT movement, new targetX: " + targetX);
            }
            else if (direction == Direction.RIGHT && targetX <= selectedBox.getX()) {
                targetX = selectedBox.getX() + GRID_SIZE;
                System.out.println("Forcing RIGHT movement, new targetX: " + targetX);
            }
            else if (direction == Direction.UP && targetY >= selectedBox.getY()) {
                targetY = selectedBox.getY() - GRID_SIZE;
                System.out.println("Forcing UP movement, new targetY: " + targetY);
            }
            else if (direction == Direction.DOWN && targetY <= selectedBox.getY()) {
                targetY = selectedBox.getY() + GRID_SIZE;
                System.out.println("Forcing DOWN movement, new targetY: " + targetY);
            }

            // Debug new delta
            System.out.println("Adjusted movement delta: (" +
                    (targetX - selectedBox.getX()) + "," + (targetY - selectedBox.getY()) + ")");

            // Safety check bounds
            if (targetX < 0) targetX = 0;
            if (targetY < 0) targetY = 0;
            if (targetX > view.getWidth() - selectedBox.getWidth()) {
                targetX = view.getWidth() - selectedBox.getWidth();
            }
            if (targetY > view.getHeight() - selectedBox.getHeight()) {
                targetY = view.getHeight() - selectedBox.getHeight();
            }

            // Mark as animating
            selectedBox.setAnimating(true);

            // Use a fixed animation duration for all pieces - longer to ensure smooth movement
            int animationDuration = 350; // Increased duration for even smoother animation

            // Debug print
            System.out.println("Animating piece " + blockType +
                    " dir=" + direction +
                    " from [" + row + "," + col + "] to [" + nextRow + "," + nextCol + "]");

            // Create animation handler with improved version
            final AnimationHandler animation = new AnimationHandler(
                    selectedBox,
                    targetX,
                    targetY,
                    animationDuration,
                    () -> {
                        // Animation complete callback
                        selectedBox.setAnimating(false);

                        // Special handling for General pieces, but in a unified way
                        if (isGeneral) {
                            // The General is a vertical piece (1 wide, 2 tall)
                            // Update both cells in the matrix to ensure consistency
                            model.getMatrix()[nextRow][nextCol] = blockType;

                            // Set second cell (below first cell)
                            if (nextRow + 1 < model.getHeight()) {
                                model.getMatrix()[nextRow + 1][nextCol] = blockType;
                            }

                            // Ensure repaint happens regardless
                            selectedBox.repaint();
                        } else {
                            // For other pieces, just repaint
                            selectedBox.repaint();
                        }

                        // Save game state - shared logic for all pieces
                        moveHistory.push(model.copyMatrix());
                        moveCount++;
                        view.updateMoveCount(moveCount);

                        // Check for obstacle restoration
                        checkObstacleRestoration();

                        // Check for victory
                        checkVictoryCondition(blockType, nextRow, nextCol);
                    }
            );

            // Set block type and start the animation
            animation.setBlockType(blockType);
            animation.start();

            return true;
        } else {
            // Enhanced collision feedback with small shake animation
            if (selectedBox != null) {
                // Store original position for the shake animation
                final int originalX = selectedBox.getX();
                final int originalY = selectedBox.getY();

                // Calculate shake direction based on attempted move
                final int shakeDistance = 5; // Subtle shake distance
                final int dirX = direction == Direction.LEFT ? -1 : (direction == Direction.RIGHT ? 1 : 0);
                final int dirY = direction == Direction.UP ? -1 : (direction == Direction.DOWN ? 1 : 0);

                // Highlight the piece
                selectedBox.setSelected(true);

                // Store original color
                final Color originalBackground = selectedBox.getBackground();

                // Set collision background
                selectedBox.setBackground(new Color(255, 100, 100, 150));

                // Create a shake animation sequence with 6 steps
                Timer shakeTimer = new Timer(40, new ActionListener() {
                    private int step = 0;
                    private final int[] shakePattern = {1, 2, 1, 0, -1, 0}; // Subtle shake pattern

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (step < shakePattern.length) {
                            // Calculate new position for this shake step
                            int offset = shakePattern[step] * shakeDistance;
                            selectedBox.setLocation(originalX + (dirX * offset), originalY + (dirY * offset));

                            // Update alpha for fading effect
                            int alpha = Math.max(50, 150 - (step * 20));
                            selectedBox.setBackground(new Color(255, 100, 100, alpha));

                            step++;
                        } else {
                            // Animation complete, restore original state
                            selectedBox.setLocation(originalX, originalY);
                            selectedBox.setSelected(false);
                            selectedBox.setBackground(originalBackground);
                            ((javax.swing.Timer)e.getSource()).stop();
                        }

                        // Force repaint at each step
                        selectedBox.repaint();
                    }
                });

                shakeTimer.setRepeats(true);
                shakeTimer.start();

                // Play collision sound effect (system beep as fallback)
                Toolkit.getDefaultToolkit().beep();
            }
            return false;
        }
    }

    // Track military camp positions to preserve them
    private List<int[]> militaryCampPositions = new ArrayList<>();

    /**
     * Clear positions for the block being moved
     */
    private void clearOldPositions(int row, int col, int width, int height) {
        // Clear military camp positions from previous move
        militaryCampPositions.clear();

        for (int r = row; r < row + height; r++) {
            for (int c = col; c < col + width; c++) {
                if (r < model.getHeight() && c < model.getWidth()) {
                    // Check if this position originally had a military camp
                    // This is crucial for Master level where military camps should remain
                    if (currentLevel == 3) { // Master level
                        // Check level 3 model if this position should be a military camp
                        int originalCell = MapModel.LEVELS[3][r][c];
                        if (originalCell == MapModel.MILITARY_CAMP) {
                            System.out.println("Preserving military camp at [" + r + "," + c + "]");
                            militaryCampPositions.add(new int[]{r, c});
                        }
                    }

                    // Clear the cell
                    model.getMatrix()[r][c] = 0;
                }
            }
        }
    }

    /**
     * Set new positions for the block after movement
     */
    private void setNewPositions(int nextRow, int nextCol, int width, int height, int blockType) {
        // Extra logging for debugging military camp movement
        if (blockType == MapModel.SOLDIER) {
            System.out.println("GameController: Moving a soldier to [" + nextRow + "," + nextCol + "]");
        }

        // Store military camp positions that will be covered by the moved piece
        List<int[]> coveredMilitaryCamps = new ArrayList<>();

        // First check if we're moving onto any military camps
        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                int targetRow = nextRow + r;
                int targetCol = nextCol + c;

                if (targetRow < model.getHeight() && targetCol < model.getWidth()) {
                    // Check if target position is a military camp
                    if (model.getMatrix()[targetRow][targetCol] == MapModel.MILITARY_CAMP) {
                        // Only soldiers can step on military camps
                        if (blockType == MapModel.SOLDIER) {
                            System.out.println("GameController: Soldier will cover military camp at [" +
                                    targetRow + "," + targetCol + "]");
                            coveredMilitaryCamps.add(new int[]{targetRow, targetCol});
                        }
                    }
                }
            }
        }

        // Now update positions with the moved piece
        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                int targetRow = nextRow + r;
                int targetCol = nextCol + c;

                if (targetRow < model.getHeight() && targetCol < model.getWidth()) {
                    // Special handling for soldiers moving onto military camps
                    boolean isMilitaryCampPosition = false;

                    // Check if this position is a covered military camp
                    for (int[] campPos : coveredMilitaryCamps) {
                        if (campPos[0] == targetRow && campPos[1] == targetCol) {
                            isMilitaryCampPosition = true;
                            break;
                        }
                    }

                    // For soldiers stepping on military camps, we still want to
                    // update the model to show the soldier (not the camp)
                    model.getMatrix()[targetRow][targetCol] = blockType;
                }
            }
        }

        // Restore military camps that were saved during clearOldPositions
        // This ensures military camps are preserved when pieces move off them
        for (int[] campPos : militaryCampPositions) {
            int row = campPos[0];
            int col = campPos[1];

            // Only restore if the position is now empty (not occupied by the moved piece)
            boolean isOccupiedByMovedPiece = false;
            for (int r = 0; r < height; r++) {
                for (int c = 0; c < width; c++) {
                    if (nextRow + r == row && nextCol + c == col) {
                        isOccupiedByMovedPiece = true;
                        break;
                    }
                }
                if (isOccupiedByMovedPiece) break;
            }

            if (!isOccupiedByMovedPiece) {
                System.out.println("GameController: Restoring military camp at [" + row + "," + col + "]");
                model.getMatrix()[row][col] = MapModel.MILITARY_CAMP;
            }
        }
    }

    /**
     * Use the hint prop to show the next best 3 moves
     */
    public boolean useHintProp() {
        if (!isPropAvailable(Prop.PropType.HINT)) {
            return false;
        }

        // Use AI solver to find the best move
        model.AISolver solver = new model.AISolver(this.model, this);
        if (solver.findSolution()) {
            // Get moves from the solution if available
            if (solver.getSolutionLength() > 0) {
                // Get up to 3 steps from the solution
                int stepsToShow = Math.min(3, solver.getSolutionLength());
                StringBuilder hintMessage = new StringBuilder();
                hintMessage.append("<html><b>Hint:</b> Here are the next ").append(stepsToShow).append(" moves:<br><br>");

                // Get the solution moves
                List<model.AISolver.Move> solutionMoves = solver.getSolutionMoves(stepsToShow);

                // Build step-by-step hints
                for (int i = 0; i < solutionMoves.size(); i++) {
                    model.AISolver.Move move = solutionMoves.get(i);
                    int pieceType = model.getId(move.row, move.col);
                    String pieceName = getPieceNameByType(pieceType);

                    hintMessage.append("<b>Step ").append(i+1).append(":</b> Move the ");
                    hintMessage.append(pieceName).append(" piece at position [").append(move.row + 1).append(", ").append(move.col + 1);
                    hintMessage.append("] ").append(getDirectionText(move.direction)).append("<br>");
                }

                hintMessage.append("</html>");

                // Show the hint dialog with the multi-step suggestions
                JOptionPane.showMessageDialog(view,
                        hintMessage.toString(),
                        "Hint - Next " + stepsToShow + " Moves",
                        JOptionPane.INFORMATION_MESSAGE);

                // Flash the piece that should be moved next to highlight it
                // Find the box component for the first move
                BoxComponent pieceToMove = findBoxAtPosition(solutionMoves.get(0).row, solutionMoves.get(0).col);
                if (pieceToMove != null) {
                    highlightNextMove(pieceToMove);
                }

                // Consume the prop
                Prop hintProp = availableProps.get(Prop.PropType.HINT);
                hintProp.use();

                return true;
            }

            // Fallback message if solution found but no moves available (shouldn't happen)
            JOptionPane.showMessageDialog(view,
                    "Hint: Try moving a piece toward the exit!",
                    "Hint",
                    JOptionPane.INFORMATION_MESSAGE);

            // Consume the prop
            Prop hintProp = availableProps.get(Prop.PropType.HINT);
            hintProp.use();

            return true;
        } else {
            JOptionPane.showMessageDialog(view,
                    "Sorry, no solution found from the current position.",
                    "Hint",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    /**
     * Find a box component at a specific position
     */
    private BoxComponent findBoxAtPosition(int row, int col) {
        // Add debug information
        System.out.println("Finding box at position [" + row + "," + col + "]");
        System.out.println("Current piece type: " + model.getId(row, col));
        System.out.println("Total boxes in view: " + view.getBoxes().size());

        // First, check if there's a box component exactly at this position
        for (BoxComponent box : view.getBoxes()) {
            if (box.getRow() == row && box.getCol() == col) {
                System.out.println("Found exact match at [" + row + "," + col + "]");
                return box;
            }
        }

        // If no exact match, try to find the origin of a multi-cell piece
        int pieceType = model.getId(row, col);
        System.out.println("No exact match, checking for multi-cell piece of type " + pieceType);

        for (BoxComponent box : view.getBoxes()) {
            // Debug each box's position and type
            System.out.println("Checking box at [" + box.getRow() + "," + box.getCol() + 
                             "] with type " + model.getId(box.getRow(), box.getCol()));
            
            // For multi-cell pieces, we need to find the top-left corner
            int originRow = row;
            int originCol = col;

            // Check for multi-cell pieces
            if (pieceType == MapModel.CAO_CAO) { // 2x2
                if (row > 0 && col > 0 && model.getId(row-1, col-1) == pieceType) {
                    originRow = row-1;
                    originCol = col-1;
                } else if (row > 0 && model.getId(row-1, col) == pieceType) {
                    originRow = row-1;
                } else if (col > 0 && model.getId(row, col-1) == pieceType) {
                    originCol = col-1;
                }
            } else if (pieceType == MapModel.GUAN_YU) { // 2x1
                if (col > 0 && model.getId(row, col-1) == pieceType) {
                    originCol = col-1;
                }
            } else if (pieceType == MapModel.GENERAL) { // 1x2
                System.out.println("Checking GENERAL piece at [" + row + "," + col + "]");
                
                // For GENERAL pieces, check both above and below
                boolean foundAbove = (row > 0 && model.getId(row-1, col) == pieceType);
                boolean foundBelow = (row < model.getHeight()-1 && model.getId(row+1, col) == pieceType);
                
                System.out.println("Found above: " + foundAbove + ", Found below: " + foundBelow);
                
                // If we're in the middle of two GENERAL pieces, prefer the one that's already selected
                BoxComponent selectedBox = view.getSelectedBox();
                if (selectedBox != null && selectedBox.getRow() == row-1 && selectedBox.getCol() == col) {
                    // If the box above is already selected, use that as origin
                    originRow = row-1;
                    System.out.println("Using already selected box above as origin: [" + originRow + "," + originCol + "]");
                } else if (selectedBox != null && selectedBox.getRow() == row && selectedBox.getCol() == col) {
                    // If current box is already selected, keep it as origin
                    System.out.println("Using already selected current box as origin: [" + originRow + "," + originCol + "]");
                } else if (foundAbove) {
                    // Otherwise, prefer the box above if it exists
                    originRow = row-1;
                    System.out.println("Using position above as origin: [" + originRow + "," + originCol + "]");
                } else if (foundBelow) {
                    // If no box above, use current position as origin
                    System.out.println("Using current position as origin: [" + originRow + "," + originCol + "]");
                }
            } else if (pieceType == MapModel.ZHOU_YU) { // 1x3
                if (col > 0 && model.getId(row, col-1) == pieceType) {
                    originCol = col-1;
                    if (col > 1 && model.getId(row, col-2) == pieceType) {
                        originCol = col-2;
                    }
                }
            }

            // Debug the origin position we're looking for
            System.out.println("Looking for box at origin [" + originRow + "," + originCol + "]");
            
            if (box.getRow() == originRow && box.getCol() == originCol) {
                System.out.println("Found matching box at origin!");
                return box;
            }
        }
        
        // If we get here, we didn't find the box
        System.out.println("No box found at position [" + row + "," + col + "]");
        return null;
    }

    /**
     * Get a human-readable name for a piece type
     */
    private String getPieceNameByType(int pieceType) {
        switch (pieceType) {
            case MapModel.CAO_CAO:
                return "Cao Cao (2x2 red)";
            case MapModel.GUAN_YU:
                return "Guan Yu (2x1 horizontal orange)";
            case MapModel.GENERAL:
                return "General (1x2 vertical blue)";
            case MapModel.SOLDIER:
                return "Soldier (1x1 green)";
            case MapModel.ZHOU_YU:
                return "Zhou Yu (1x3 horizontal purple)";
            case MapModel.BLOCKED:
                return "Obstacle (gray)";
            default:
                return "Unknown";
        }
    }

    /**
     * Get a human-readable description of a direction
     */
    private String getDirectionText(Direction dir) {
        switch (dir) {
            case UP:
                return "upward";
            case DOWN:
                return "downward";
            case LEFT:
                return "to the left";
            case RIGHT:
                return "to the right";
            default:
                return "in some direction";
        }
    }

    /**
     * Highlights the next piece to move with a flashing effect
     *
     * @param piece The box component to highlight
     */
    private void highlightNextMove(BoxComponent piece) {
        if (piece == null) return;

        // Store original colors
        final Color originalBackground = piece.getBackground();
        // Work around the isSelected() method issue by using a direct check of the border
        final boolean wasSelected = piece.getBorder() != null &&
                piece.getBorder().equals(BorderFactory.createLineBorder(Color.red, 3));

        // Create a flash animation
        Timer flashTimer = new Timer(200, new ActionListener() {
            private int count = 0;
            private final Color highlightColor = Color.YELLOW;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (count % 2 == 0) {
                    // Highlight phase
                    piece.setBackground(highlightColor);
                    piece.setSelected(true);
                } else {
                    // Normal phase
                    piece.setBackground(originalBackground);
                    piece.setSelected(wasSelected);
                }

                piece.repaint();
                count++;

                // Stop after 6 flashes (3 cycles)
                if (count >= 6) {
                    ((Timer)e.getSource()).stop();

                    // Restore original state
                    piece.setBackground(originalBackground);
                    piece.setSelected(wasSelected);
                    piece.repaint();
                }
            }
        });

        flashTimer.setRepeats(true);
        flashTimer.start();
    }

    /**
     * Use the time bonus prop to add time
     *
     * @return true if successful, false if the prop is not available or time attack mode is not active
     */
    public boolean useTimeBonusProp(view.game.GameFrame gameFrame) {
        if (!isPropAvailable(Prop.PropType.TIME_BONUS)) {
            return false;
        }

        // Try to add time to the game frame
        boolean success = gameFrame.addTimeToTimer(30); // Add 30 seconds

        if (success) {
            // Consume the prop
            Prop timeProp = availableProps.get(Prop.PropType.TIME_BONUS);
            timeProp.use();
        } else {
            JOptionPane.showMessageDialog(view,
                    "Time Bonus can only be used in Time Attack mode!",
                    "Time Bonus",
                    JOptionPane.WARNING_MESSAGE);
        }

        return success;
    }

    /**
     * Use the obstacle remover prop to temporarily remove an obstacle
     *
     * @param row Row position of the obstacle
     * @param col Column position of the obstacle
     * @return true if successful, false if the prop is not available or the target is not a removable obstacle
     */
    public boolean useObstacleRemoverProp(int row, int col) {
        System.out.println("GameController: Attempting to remove obstacle at [" + row + "," + col + "]");

        // Check if the prop is available
        if (!isPropAvailable(Prop.PropType.OBSTACLE_REMOVER)) {
            System.out.println("GameController: Obstacle remover prop not available");
            JOptionPane.showMessageDialog(view,
                    "You don't have any obstacle remover props available.",
                    "Obstacle Remover",
                    JOptionPane.WARNING_MESSAGE);
            return false;
        }

        // Get the cell type directly from the model
        int cellType = model.getId(row, col);
        System.out.println("GameController: Cell type at [" + row + "," + col + "]: " + cellType);

        // Check if the target is a removable obstacle
        if (cellType != MapModel.BLOCKED) {
            System.out.println("GameController: Target is not a removable obstacle (type=" + cellType +
                    ", expected=" + MapModel.BLOCKED + ")");
            JOptionPane.showMessageDialog(view,
                    "This prop can only be used on obstacles (gray blocks).",
                    "Obstacle Remover",
                    JOptionPane.WARNING_MESSAGE);
            return false;
        }

        // Debug helper
        System.out.println("GameController: Confirmed obstacle at [" + row + "," + col + "]");

        // Determine how many steps the obstacle will be removed for
        int stepsRemoved = 3; // Always 3 steps for consistency

        // Store the obstacle information for restoration
        removedObstacles.add(new int[] {row, col, stepsRemoved});

        // Mark the obstacle as temporarily removed using negative value
        model.getMatrix()[row][col] = -MapModel.BLOCKED;

        // Consume the prop
        Prop obstacleProp = availableProps.get(Prop.PropType.OBSTACLE_REMOVER);
        obstacleProp.use();

        System.out.println("GameController: Obstacle successfully removed for " + stepsRemoved + " steps");
        System.out.println("GameController: removedObstacles list now has " + removedObstacles.size() + " items");

        return true;
    }

    /**
     * Check if any removed obstacles need to be restored after a move
     */
    private void checkObstacleRestoration() {
        if (removedObstacles.isEmpty()) {
            return;
        }

        ArrayList<int[]> obstaclesRestored = new ArrayList<>();
        boolean viewNeedsUpdate = false;

        // Decrement step counters and restore obstacles that have reached zero
        for (int[] obstacle : removedObstacles) {
            int row = obstacle[0];
            int col = obstacle[1];
            int stepsLeft = obstacle[2] - 1;

            // Special case: stepsLeft is negative, which means this obstacle is waiting for its position to become clear
            boolean waitingToRestore = stepsLeft < 0;

            if (waitingToRestore) {
                // This obstacle is just waiting for its position to be clear, don't decrement further
                stepsLeft = obstacle[2]; // Keep the negative value
            }

            System.out.println("Checking obstacle at [" + row + "," + col + "] - Steps left: " + stepsLeft +
                    (waitingToRestore ? " (waiting for position to clear)" : ""));

            if (stepsLeft <= 0) {
                // Check if the position is currently occupied by a piece
                int cellValue = model.getId(row, col);
                System.out.println("  Position content: " + cellValue);

                if (cellValue == 0) {
                    // Position is empty, restore the obstacle
                    model.getMatrix()[row][col] = MapModel.BLOCKED;
                    obstaclesRestored.add(obstacle);
                    viewNeedsUpdate = true;

                    // Notify the user
                    JOptionPane.showMessageDialog(view,
                            "An obstacle has reappeared at position [" + (row+1) + "," + (col+1) + "]!",
                            "Obstacle Restored",
                            JOptionPane.INFORMATION_MESSAGE);

                    System.out.println("  Obstacle at [" + row + "," + col + "] has been restored");
                } else {
                    // Position is occupied, keep the obstacle in the list but mark it as waiting to restore
                    System.out.println("  Cannot restore obstacle at [" + row + "," + col + "] because position is occupied");
                    System.out.println("  Will restore when position becomes clear");

                    // Set stepsLeft to a special negative value to indicate waiting status
                    if (stepsLeft == 0) {
                        obstacle[2] = -1; // -1 means waiting for position to clear
                    }

                    // Show a warning message to the player if this is the first time we're waiting
                    if (!waitingToRestore) {
                        JOptionPane.showMessageDialog(view,
                                "An obstacle is trying to reappear at position [" + (row+1) + "," + (col+1) + "],\n" +
                                        "but the position is currently occupied.\n" +
                                        "The obstacle will reappear when the position becomes clear.",
                                "Obstacle Waiting",
                                JOptionPane.WARNING_MESSAGE);
                    }
                }
            } else {
                // Update the steps counter
                obstacle[2] = stepsLeft;
                System.out.println("  Obstacle at [" + row + "," + col + "] will reappear in " + stepsLeft + " moves");
            }
        }

        // Remove restored obstacles from the list
        removedObstacles.removeAll(obstaclesRestored);

        // Update the view if any obstacles were restored
        if (viewNeedsUpdate) {
            view.resetBoard(model.getMatrix());
        }
    }

    private void checkVictoryCondition(int blockType, int nextRow, int nextCol) {
        // Debug output for movement
        System.out.printf("Moving block %d to [%d][%d] (model size %dx%d)\n",
                blockType, nextRow, nextCol, model.getWidth(), model.getHeight());

        // Check victory condition (Cao Cao covering exit position)
        // Debug victory condition check
        System.out.printf("Checking victory for block %d at [%d][%d] (model size %dx%d)\n",
                blockType, nextRow, nextCol, model.getWidth(), model.getHeight());

        // Check victory condition when CaoCao moves to exit position
        if (blockType == MapModel.CAO_CAO) {
            // Verify all 4 positions of CaoCao block (2x2)
            boolean validPosition = true;
            for (int r = nextRow; r < nextRow + 2; r++) {
                for (int c = nextCol; c < nextCol + 2; c++) {
                    if (r >= model.getHeight() || c >= model.getWidth() ||
                            model.getId(r, c) != MapModel.CAO_CAO) {
                        validPosition = false;
                        System.out.printf("  [%d][%d]: %s (expected CAO_CAO)\n",
                                r, c,
                                r >= model.getHeight() || c >= model.getWidth() ?
                                        "OUT_OF_BOUNDS" : model.getId(r, c));
                    } else {
                        System.out.printf("  [%d][%d]: OK\n", r, c);
                    }
                }
            }

            // Get the goal position based on board dimensions
            int goalRow = model.getHeight() - 2;
            int goalCol = (model.getWidth() / 2) - 1;

            // Victory occurs when CaoCao covers exit position at the bottom center
            boolean coversExitPosition = (nextRow == goalRow && nextCol == goalCol);

            // Victory condition - CaoCao must cover exit position at the bottom center
            if (blockType == MapModel.CAO_CAO && nextRow == goalRow && nextCol == goalCol) {
                // Additional check that all 4 CaoCao positions are valid
                boolean allPositionsValid = true;
                for (int r = nextRow; r < nextRow + 2; r++) {
                    for (int c = nextCol; c < nextCol + 2; c++) {
                        if (model.getId(r, c) != MapModel.CAO_CAO) {
                            allPositionsValid = false;
                            break;
                        }
                    }
                    if (!allPositionsValid) break;
                }

                if (allPositionsValid) {
                    System.out.println("***** VICTORY! CaoCao at exit position with DOWN press *****");
                    System.out.println("CaoCao covers:");
                    System.out.println("  [3][1] - [3][2]");
                    System.out.println("  [4][1] - [4][2]");

                    // Call checkWin to update leaderboard and show victory message
                    checkWin();
                    showVictory();
                }
            }

            for (int r = 0; r < model.getHeight(); r++) {
                for (int c = 0; c < model.getWidth(); c++) {
                    System.out.printf("%2d ", model.getId(r, c));
                }
                System.out.println();
            }
        }
    }

    private void showVictory() {
        // More prominent victory feedback
        for (int i = 0; i < 5; i++) {
            // Flash exit and highlight CaoCao
            view.highlightExit(true);
            view.highlightCaoCao(true);
            try { Thread.sleep(200); } catch (InterruptedException e) {}

            // Flash entire panel background
            view.setBackground(Color.YELLOW);
            view.repaint();
            try { Thread.sleep(200); } catch (InterruptedException e) {}

            view.highlightExit(false);
            view.highlightCaoCao(false);
            view.setBackground(Color.LIGHT_GRAY);
            view.repaint();
            try { Thread.sleep(200); } catch (InterruptedException e) {}
        }

        // More prominent victory message
        JOptionPane.showMessageDialog(view,
                String.format("<html><h1>VICTORY!</h1><br>You won in %d moves!</html>", moveCount),
                "Klotski Puzzle Solved!",
                JOptionPane.INFORMATION_MESSAGE);

        restartGame();
    }

    public void saveGame() {
        if (currentUser == null || currentUser.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Cannot save in guest mode", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            File savesDir = new File("saves");
            if (!savesDir.exists()) {
                savesDir.mkdir();
            }

            // Create JSON object for game state
            JSONObject gameState = new JSONObject();
            gameState.put("username", currentUser);
            gameState.put("moveCount", moveCount);
            gameState.put("currentLevel", currentLevel);
            gameState.put("height", model.getHeight());
            gameState.put("width", model.getWidth());

            // Save board state as 2D array
            JSONArray board = new JSONArray();
            for (int i = 0; i < model.getHeight(); i++) {
                JSONArray row = new JSONArray();
                for (int j = 0; j < model.getWidth(); j++) {
                    row.put(model.getMatrix()[i][j]);
                }
                board.put(row);
            }
            gameState.put("board", board);

            // Save props state
            JSONObject props = new JSONObject();
            for (Prop.PropType type : Prop.PropType.values()) {
                props.put(type.name(), getPropCount(type));
            }
            gameState.put("props", props);

            // Save removed obstacles
            JSONArray removedObstaclesArray = new JSONArray();
            for (int[] obstacle : removedObstacles) {
                JSONArray obstacleData = new JSONArray();
                obstacleData.put(obstacle[0]); // row
                obstacleData.put(obstacle[1]); // col
                obstacleData.put(obstacle[2]); // steps remaining
                removedObstaclesArray.put(obstacleData);
            }
            gameState.put("removedObstacles", removedObstaclesArray);

            // Save time attack mode state
            view.game.GameFrame gameFrame = null;
            if (view.getParent() != null && view.getParent().getParent() instanceof view.game.GameFrame) {
                gameFrame = (view.game.GameFrame) view.getParent().getParent();
            } else {
                for (Frame frame : Frame.getFrames()) {
                    if (frame instanceof view.game.GameFrame) {
                        gameFrame = (view.game.GameFrame) frame;
                        break;
                    }
                }
            }

            if (gameFrame != null) {
                boolean timeAttackMode = gameFrame.isTimeAttackMode();
                int timeLimit = gameFrame.getTimeLimit();
                int remainingTime = gameFrame.getRemainingTime(); // 获取剩余时间
                System.out.println("Saving time attack mode: " + timeAttackMode + 
                    ", time limit: " + timeLimit + 
                    ", remaining time: " + remainingTime);
                gameState.put("timeAttackMode", timeAttackMode);
                gameState.put("timeLimit", timeLimit);
                gameState.put("remainingTime", remainingTime); // 保存剩余时间
            } else {
                System.out.println("Warning: Could not find GameFrame to save time attack mode");
                gameState.put("timeAttackMode", false);
                gameState.put("timeLimit", 0);
                gameState.put("remainingTime", 0);
            }

            // Write to file
            File saveFile = new File("saves/" + currentUser + ".sav");
            try (FileWriter writer = new FileWriter(saveFile)) {
                writer.write(gameState.toString(2)); // Pretty print with 2 spaces indentation
            }

            JOptionPane.showMessageDialog(view, "Game saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(view, "Failed to save game: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    /**
     * Gets the current MapModel
     * Used by the AI solver to analyze the current game state
     *
     * @return The current MapModel instance
     */
    public MapModel getModel() {
        return this.model;
    }

    /**
     * Gets the GamePanel view
     * Used by the AI solver to update the UI
     *
     * @return The GamePanel view instance
     */
    public GamePanel getView() {
        return this.view;
    }

    /**
     * Selects the box at the specified board position
     * Used by the AI solver to move specific pieces
     *
     * @param row Row position on the board
     * @param col Column position on the board
     * @return The selected BoxComponent, or null if no box at position
     */
    public BoxComponent selectBoxAt(int row, int col) {
        if (row < 0 || row >= model.getHeight() || col < 0 || col >= model.getWidth()) {
            return null;
        }
        
        int pieceType = model.getId(row, col);
        if (pieceType == 0) {
            return null;
        }
        
        // Find and return the box component at this position
        return findBoxAtPosition(row, col);
    }

    public boolean loadGame() {
        if (currentUser == null || currentUser.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Cannot load in guest mode", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        File saveFile = new File("saves/" + currentUser + ".sav");
        boolean manualSaveFailed = false;

        if (saveFile.exists()) {
            try {
                // Read JSON from file
                String jsonContent = new String(Files.readAllBytes(saveFile.toPath()));
                JSONObject gameState = new JSONObject(jsonContent);

                // Verify username
                String username = gameState.getString("username");
                if (!username.equals(currentUser)) {
                    throw new Exception("Save file does not belong to current user");
                }

                // Load basic game state
                moveCount = gameState.getInt("moveCount");
                currentLevel = gameState.getInt("currentLevel");
                int height = gameState.getInt("height");
                int width = gameState.getInt("width");

                // Load board state
                JSONArray board = gameState.getJSONArray("board");
                int[][] loadedMatrix = new int[height][width];
                for (int i = 0; i < height; i++) {
                    JSONArray row = board.getJSONArray(i);
                    for (int j = 0; j < width; j++) {
                        loadedMatrix[i][j] = row.getInt(j);
                    }
                }

                // Load props
                JSONObject props = gameState.getJSONObject("props");
                for (Prop.PropType type : Prop.PropType.values()) {
                    if (props.has(type.name())) {
                        int count = props.getInt(type.name());
                        if (count > 0) {
                            availableProps.put(type, new Prop(type, count));
                        }
                    }
                }

                // Load removed obstacles
                removedObstacles.clear();
                JSONArray removedObstaclesArray = gameState.getJSONArray("removedObstacles");
                for (int i = 0; i < removedObstaclesArray.length(); i++) {
                    JSONArray obstacleData = removedObstaclesArray.getJSONArray(i);
                    removedObstacles.add(new int[]{
                        obstacleData.getInt(0), // row
                        obstacleData.getInt(1), // col
                        obstacleData.getInt(2)  // steps remaining
                    });
                }

                // Update model and view
                this.model = new MapModel(loadedMatrix);
                this.moveHistory.clear();
                this.moveHistory.push(model.copyMatrix());
                view.resetBoard(loadedMatrix);
                view.updateMoveCount(moveCount);
                view.requestFocusInWindow();

                // Restore time attack mode
                view.game.GameFrame gameFrame = null;
                if (view.getParent() != null && view.getParent().getParent() instanceof view.game.GameFrame) {
                    gameFrame = (view.game.GameFrame) view.getParent().getParent();
                } else {
                    for (Frame frame : Frame.getFrames()) {
                        if (frame instanceof view.game.GameFrame) {
                            gameFrame = (view.game.GameFrame) frame;
                            break;
                        }
                    }
                }

                if (gameFrame != null) {
                    boolean timeAttackMode = gameState.getBoolean("timeAttackMode");
                    int timeLimit = gameState.getInt("timeLimit");
                    int remainingTime = gameState.getInt("remainingTime");
                    System.out.println("Loading time attack mode: " + timeAttackMode + 
                        ", time limit: " + timeLimit + 
                        ", remaining time: " + remainingTime);
                    
                    if (remainingTime > 0) {
                        System.out.println("Attempting to restore time attack mode...");
                        gameFrame.setTimeAttackMode(true, timeLimit, remainingTime);
                        System.out.println("Time attack mode restored");
                    } else {
                        System.out.println("Not in time attack mode, skipping timer restoration");
                    }
                } else {
                    System.out.println("Warning: Could not find GameFrame to restore time attack mode");
                }

                return true;
            } catch (Exception e) {
                manualSaveFailed = true;
                System.err.println("Failed to load manual save: " + e.getMessage());
                e.printStackTrace();
            }
        }

        // 如果手动存档不存在或加载失败，尝试加载自动存档
        if (!saveFile.exists() || manualSaveFailed) {
            if (autoSaveManager != null) {
                List<AutoSaveManager.AutoSaveInfo> autoSaves = autoSaveManager.getAvailableAutoSaves();
                if (!autoSaves.isEmpty()) {
                    int choice = JOptionPane.showConfirmDialog(view,
                        manualSaveFailed ? 
                            "手动存档加载失败，是否从自动存档恢复？" :
                            "未找到主存档，是否从自动存档恢复？",
                        "恢复自动存档",
                        JOptionPane.YES_NO_OPTION);
                        
                    if (choice == JOptionPane.YES_OPTION) {
                        return recoverFromAutoSave();
                    }
                } else {
                    JOptionPane.showMessageDialog(view, 
                        manualSaveFailed ? 
                            "手动存档加载失败，且没有可用的自动存档" :
                            "未找到主存档，且没有可用的自动存档", 
                        "加载失败", 
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        }

        return false;
    }

    /**
     * 从自动存档恢复
     */
    private boolean recoverFromAutoSave() {
        if (autoSaveManager == null) {
            return false;
        }

        List<AutoSaveManager.AutoSaveInfo> autoSaves = autoSaveManager.getAvailableAutoSaves();
        if (autoSaves.isEmpty()) {
            JOptionPane.showMessageDialog(view, "没有可用的自动存档", "恢复失败", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // 创建自动存档选择对话框
        String[] options = autoSaves.stream()
            .map(AutoSaveManager.AutoSaveInfo::toString)
            .toArray(String[]::new);

        int choice = JOptionPane.showOptionDialog(view,
            "请选择要恢复的自动存档：",
            "选择自动存档",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]);

        if (choice >= 0 && choice < autoSaves.size()) {
            return autoSaveManager.recoverFromAutoSave(autoSaves.get(choice).file);
        }

        return false;
    }

    /**
     * Gets the list of removed obstacles
     * @return The list of removed obstacles
     */
    public ArrayList<int[]> getRemovedObstacles() {
        return removedObstacles;
    }

    /**
     * Gets the map of available props
     * @return The map of available props
     */
    public Map<Prop.PropType, Prop> getAvailableProps() {
        return availableProps;
    }

    public void setAISolving(boolean isAISolving) {
        this.isAISolving = isAISolving;
    }

    public void checkWin() {
        System.out.println("Checking win condition...");
        System.out.println("isWin: " + model.isWin());
        System.out.println("isAISolving: " + isAISolving);
        System.out.println("currentUser: " + currentUser);
        System.out.println("moveCount: " + moveCount);
        
        if (model.isWin() && !isAISolving) {
            // Only update leaderboard if not using AI solver
            Difficulty difficulty = getCurrentDifficulty();
            System.out.println("Current difficulty: " + difficulty);
            
            if (difficulty != null) {
                System.out.println("Adding entry to leaderboard: " + currentUser + " with " + moveCount + " moves");
                leaderboardManager.addEntry(difficulty, currentUser, moveCount);
            } else {
                System.out.println("Difficulty is null, cannot add to leaderboard");
            }
            
            JOptionPane.showMessageDialog(view,
                    "Congratulations! You solved the puzzle in " + moveCount + " moves!",
                    "Victory!",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            System.out.println("Not updating leaderboard because:");
            if (!model.isWin()) System.out.println("- Game is not won");
            if (isAISolving) System.out.println("- AI is solving");
        }
    }

    private Difficulty getCurrentDifficulty() {
        int level = currentLevel;  // Use currentLevel instead of model.getCurrentLevel()
        System.out.println("Getting difficulty for level: " + level);
        
        switch (level) {
            case 0:
            case 1:
            case 2:
            case 3:
                return Difficulty.EASY;
            case 4:
            case 5:
            case 6:
                return Difficulty.MEDIUM;
            case 7:
            case 8:
            case 9:
                return Difficulty.HARD;
            case 10:
            case 11:
            case 12:
                return Difficulty.EXPERT;
            default:
                System.out.println("Unknown level: " + level);
                return null;
        }
    }
}
