package model;

import java.io.*;
import java.util.*;
import javax.swing.JOptionPane;

public class SaveFileValidator {
    // 存档文件的基本要求
    private static final int MIN_BOARD_WIDTH = 4;
    private static final int MIN_BOARD_HEIGHT = 5;
    private static final int MAX_BOARD_WIDTH = 10;
    private static final int MAX_BOARD_HEIGHT = 10;
    
    // 棋子类型的有效值
    private static final Set<Integer> VALID_PIECE_TYPES = new HashSet<>(Arrays.asList(
        0,  // 空
        MapModel.CAO_CAO,      // 曹操 (2x2)
        MapModel.GUAN_YU,      // 关羽 (2x1)
        MapModel.GENERAL,      // 将军 (1x2)
        MapModel.SOLDIER,      // 士兵 (1x1)
        MapModel.ZHOU_YU,      // 周瑜 (3x1)
        MapModel.BLOCKED,      // 障碍
        MapModel.MILITARY_CAMP // 军事营地
    ));

    public static class ValidationResult {
        public final boolean isValid;
        public final String message;
        public final int[][] fixedBoard;
        public final Map<String, Object> fixedData;

        public ValidationResult(boolean isValid, String message, int[][] fixedBoard, Map<String, Object> fixedData) {
            this.isValid = isValid;
            this.message = message;
            this.fixedBoard = fixedBoard;
            this.fixedData = fixedData;
        }
    }

    /**
     * 验证存档文件的完整性
     */
    public static ValidationResult validateSaveFile(String username) {
        File saveFile = new File("saves/" + username + ".sav");
        if (!saveFile.exists()) {
            return new ValidationResult(false, "存档文件不存在", null, null);
        }

        try (FileInputStream fis = new FileInputStream(saveFile);
             DataInputStream dis = new DataInputStream(fis)) {
            
            // 读取基本信息
            String savedUsername = dis.readUTF();
            int moveCount = dis.readInt();
            int height = dis.readInt();
            int width = dis.readInt();

            // 验证基本信息
            if (!savedUsername.equals(username)) {
                return new ValidationResult(false, "存档用户名不匹配", null, null);
            }

            if (moveCount < 0) {
                return new ValidationResult(false, "移动次数无效", null, null);
            }

            if (height < MIN_BOARD_HEIGHT || height > MAX_BOARD_HEIGHT ||
                width < MIN_BOARD_WIDTH || width > MAX_BOARD_WIDTH) {
                return new ValidationResult(false, "棋盘尺寸无效", null, null);
            }

            // 读取并验证棋盘状态
            int[][] board = new int[height][width];
            boolean hasCaoCao = false;
            int pieceCount = 0;

            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    int pieceType = dis.readInt();
                    
                    // 验证棋子类型
                    if (!VALID_PIECE_TYPES.contains(pieceType)) {
                        // 尝试修复无效的棋子类型
                        pieceType = 0;
                    }
                    
                    board[i][j] = pieceType;
                    
                    if (pieceType == 1) { // 曹操
                        hasCaoCao = true;
                    }
                    if (pieceType != 0) {
                        pieceCount++;
                    }
                }
            }

            // 验证基本游戏规则
            if (!hasCaoCao) {
                return new ValidationResult(false, "存档缺少曹操棋子", null, null);
            }

            if (pieceCount < 5) { // 至少需要曹操和几个其他棋子
                return new ValidationResult(false, "存档棋子数量异常", null, null);
            }

            // 验证曹操的位置和大小
            boolean caoCaoValid = validateCaoCao(board);
            if (!caoCaoValid) {
                // 尝试修复曹操的位置
                int[][] fixedBoard = fixCaoCaoPosition(board);
                if (fixedBoard != null) {
                    Map<String, Object> fixedData = new HashMap<>();
                    fixedData.put("moveCount", moveCount);
                    fixedData.put("height", height);
                    fixedData.put("width", width);
                    return new ValidationResult(true, "已修复曹操位置", fixedBoard, fixedData);
                }
                return new ValidationResult(false, "曹操位置无效且无法修复", null, null);
            }

            // 所有检查通过
            Map<String, Object> data = new HashMap<>();
            data.put("moveCount", moveCount);
            data.put("height", height);
            data.put("width", width);
            return new ValidationResult(true, "存档验证通过", board, data);

        } catch (Exception e) {
            return new ValidationResult(false, "存档文件损坏: " + e.getMessage(), null, null);
        }
    }

    /**
     * 验证曹操的位置和大小是否正确
     */
    private static boolean validateCaoCao(int[][] board) {
        for (int i = 0; i < board.length - 1; i++) {
            for (int j = 0; j < board[0].length - 1; j++) {
                if (board[i][j] == 1) {
                    // 检查2x2区域是否都是曹操
                    if (board[i][j+1] == 1 && 
                        board[i+1][j] == 1 && 
                        board[i+1][j+1] == 1) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 尝试修复曹操的位置
     */
    private static int[][] fixCaoCaoPosition(int[][] board) {
        int[][] fixedBoard = new int[board.length][board[0].length];
        
        // 复制原始棋盘
        for (int i = 0; i < board.length; i++) {
            System.arraycopy(board[i], 0, fixedBoard[i], 0, board[i].length);
        }

        // 寻找曹操的碎片
        List<int[]> caoCaoPieces = new ArrayList<>();
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[0].length; j++) {
                if (board[i][j] == 1) {
                    caoCaoPieces.add(new int[]{i, j});
                }
            }
        }

        // 如果找到4个曹操碎片，尝试重新组合
        if (caoCaoPieces.size() == 4) {
            // 清除所有曹操碎片
            for (int[] pos : caoCaoPieces) {
                fixedBoard[pos[0]][pos[1]] = 0;
            }

            // 在合适的位置重新放置曹操
            int startRow = Math.min(caoCaoPieces.get(0)[0], caoCaoPieces.get(1)[0]);
            int startCol = Math.min(caoCaoPieces.get(0)[1], caoCaoPieces.get(1)[1]);

            // 确保位置有效
            if (startRow >= 0 && startRow < board.length - 1 &&
                startCol >= 0 && startCol < board[0].length - 1) {
                
                // 放置曹操
                fixedBoard[startRow][startCol] = 1;
                fixedBoard[startRow][startCol + 1] = 1;
                fixedBoard[startRow + 1][startCol] = 1;
                fixedBoard[startRow + 1][startCol + 1] = 1;
                
                return fixedBoard;
            }
        }

        return null;
    }
} 