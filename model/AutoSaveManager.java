package model;

import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.Timer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import controller.GameController;
import org.json.JSONObject;
import org.json.JSONArray;
import java.nio.file.Files;

public class AutoSaveManager {
    private static final String AUTO_SAVE_DIR = "saves/autosave";
    private static final int MAX_AUTO_SAVES = 5; // 保留最近的5个自动存档
    private static final long AUTO_SAVE_INTERVAL = 30 * 1000; // 30秒自动保存一次
    
    private final String username;
    private Timer autoSaveTimer;
    private final GameController controller;
    
    public AutoSaveManager(String username, GameController controller) {
        this.username = username;
        this.controller = controller;
        createAutoSaveDirectory();
    }
    
    /**
     * 创建自动存档目录
     */
    private void createAutoSaveDirectory() {
        File dir = new File(AUTO_SAVE_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }
    
    /**
     * 开始自动保存
     */
    public void startAutoSave() {
        if (autoSaveTimer != null) {
            autoSaveTimer.stop();
        }
        
        autoSaveTimer = new Timer((int)AUTO_SAVE_INTERVAL, e -> {
            if (controller != null) {
                createAutoSave();
            }
        });
        autoSaveTimer.start();
    }
    
    /**
     * 停止自动保存
     */
    public void stopAutoSave() {
        if (autoSaveTimer != null) {
            autoSaveTimer.stop();
        }
    }
    
    /**
     * 创建自动存档
     */
    private void createAutoSave() {
        try {
            // 生成时间戳
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = String.format("%s/%s_%s.sav", AUTO_SAVE_DIR, username, timestamp);
            
            // 创建JSON对象
            JSONObject gameState = new JSONObject();
            gameState.put("username", username);
            gameState.put("moveCount", controller.getMoveCount());
            gameState.put("currentLevel", controller.getCurrentLevel());
            
            // 保存棋盘状态
            int[][] board = controller.getModel().getMatrix();
            gameState.put("height", board.length);
            gameState.put("width", board[0].length);
            
            JSONArray boardArray = new JSONArray();
            for (int[] row : board) {
                JSONArray rowArray = new JSONArray();
                for (int cell : row) {
                    rowArray.put(cell);
                }
                boardArray.put(rowArray);
            }
            gameState.put("board", boardArray);
            
            // 保存道具状态
            JSONObject props = new JSONObject();
            for (Prop.PropType type : Prop.PropType.values()) {
                props.put(type.name(), controller.getPropCount(type));
            }
            gameState.put("props", props);
            
            // 保存已移除的障碍物
            JSONArray removedObstaclesArray = new JSONArray();
            for (int[] obstacle : controller.getRemovedObstacles()) {
                JSONArray obstacleData = new JSONArray();
                obstacleData.put(obstacle[0]); // row
                obstacleData.put(obstacle[1]); // col
                obstacleData.put(obstacle[2]); // steps remaining
                removedObstaclesArray.put(obstacleData);
            }
            gameState.put("removedObstacles", removedObstaclesArray);
            
            // 保存计时模式状态
            view.game.GameFrame gameFrame = null;
            if (controller.getView().getParent() != null && 
                controller.getView().getParent().getParent() instanceof view.game.GameFrame) {
                gameFrame = (view.game.GameFrame) controller.getView().getParent().getParent();
            } else {
                for (java.awt.Frame frame : java.awt.Frame.getFrames()) {
                    if (frame instanceof view.game.GameFrame) {
                        gameFrame = (view.game.GameFrame) frame;
                        break;
                    }
                }
            }
            
            if (gameFrame != null) {
                boolean timeAttackMode = gameFrame.isTimeAttackMode();
                int timeLimit = gameFrame.getTimeLimit();
                int remainingTime = gameFrame.getRemainingTime();
                gameState.put("timeAttackMode", timeAttackMode);
                gameState.put("timeLimit", timeLimit);
                gameState.put("remainingTime", remainingTime);
            } else {
                gameState.put("timeAttackMode", false);
                gameState.put("timeLimit", 0);
                gameState.put("remainingTime", 0);
            }
            
            // 写入文件
            try (FileWriter writer = new FileWriter(fileName)) {
                writer.write(gameState.toString(2)); // Pretty print with 2 spaces indentation
            }
            
            // 清理旧的自动存档
            cleanupOldAutoSaves();
            
        } catch (Exception e) {
            System.err.println("自动保存失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 清理旧的自动存档
     */
    private void cleanupOldAutoSaves() {
        File dir = new File(AUTO_SAVE_DIR);
        File[] files = dir.listFiles((d, name) -> name.startsWith(username + "_"));
        
        if (files != null && files.length > MAX_AUTO_SAVES) {
            // 按修改时间排序
            Arrays.sort(files, Comparator.comparingLong(File::lastModified));
            
            // 删除最旧的存档
            for (int i = 0; i < files.length - MAX_AUTO_SAVES; i++) {
                files[i].delete();
            }
        }
    }
    
    /**
     * 获取可用的自动存档列表
     */
    public List<AutoSaveInfo> getAvailableAutoSaves() {
        List<AutoSaveInfo> saves = new ArrayList<>();
        File dir = new File(AUTO_SAVE_DIR);
        File[] files = dir.listFiles((d, name) -> name.startsWith(username + "_"));
        
        if (files != null) {
            for (File file : files) {
                try {
                    String timestamp = file.getName().substring(username.length() + 1, username.length() + 16);
                    LocalDateTime dateTime = LocalDateTime.parse(timestamp, 
                        DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                    
                    saves.add(new AutoSaveInfo(file, dateTime));
                } catch (Exception e) {
                    System.err.println("解析自动存档信息失败: " + e.getMessage());
                }
            }
        }
        
        // 按时间倒序排序
        saves.sort((a, b) -> b.dateTime.compareTo(a.dateTime));
        return saves;
    }
    
    /**
     * 从自动存档恢复
     */
    public boolean recoverFromAutoSave(File autoSaveFile) {
        try {
            // 读取JSON文件
            String jsonContent = new String(Files.readAllBytes(autoSaveFile.toPath()));
            JSONObject gameState = new JSONObject(jsonContent);
            
            // 验证用户名
            String savedUsername = gameState.getString("username");
            if (!savedUsername.equals(username)) {
                JOptionPane.showMessageDialog(null, 
                    "自动存档用户名不匹配", 
                    "恢复失败", 
                    JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
            // 读取基本信息
            int moveCount = gameState.getInt("moveCount");
            int currentLevel = gameState.getInt("currentLevel");
            int height = gameState.getInt("height");
            int width = gameState.getInt("width");
            
            // 读取棋盘状态
            JSONArray board = gameState.getJSONArray("board");
            int[][] loadedMatrix = new int[height][width];
            for (int i = 0; i < height; i++) {
                JSONArray row = board.getJSONArray(i);
                for (int j = 0; j < width; j++) {
                    loadedMatrix[i][j] = row.getInt(j);
                }
            }
            
            // 读取道具状态
            JSONObject props = gameState.getJSONObject("props");
            for (Prop.PropType type : Prop.PropType.values()) {
                if (props.has(type.name())) {
                    int count = props.getInt(type.name());
                    if (count > 0) {
                        controller.getAvailableProps().put(type, new Prop(type, count));
                    }
                }
            }
            
            // 读取已移除的障碍物
            controller.getRemovedObstacles().clear();
            JSONArray removedObstaclesArray = gameState.getJSONArray("removedObstacles");
            for (int i = 0; i < removedObstaclesArray.length(); i++) {
                JSONArray obstacleData = removedObstaclesArray.getJSONArray(i);
                controller.getRemovedObstacles().add(new int[]{
                    obstacleData.getInt(0), // row
                    obstacleData.getInt(1), // col
                    obstacleData.getInt(2)  // steps remaining
                });
            }
            
            // 恢复游戏状态
            controller.getModel().setMatrix(loadedMatrix);
            controller.getView().resetBoard(loadedMatrix);
            controller.getView().updateMoveCount(moveCount);
            
            // 恢复计时模式状态
            view.game.GameFrame gameFrame = null;
            if (controller.getView().getParent() != null && 
                controller.getView().getParent().getParent() instanceof view.game.GameFrame) {
                gameFrame = (view.game.GameFrame) controller.getView().getParent().getParent();
            } else {
                for (java.awt.Frame frame : java.awt.Frame.getFrames()) {
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
                
                if (remainingTime > 0) {
                    gameFrame.setTimeAttackMode(true, timeLimit, remainingTime);
                }
            }
            
            return true;
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                "恢复自动存档失败: " + e.getMessage(),
                "恢复失败",
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 自动存档信息类
     */
    public static class AutoSaveInfo {
        public final File file;
        public final LocalDateTime dateTime;
        
        public AutoSaveInfo(File file, LocalDateTime dateTime) {
            this.file = file;
            this.dateTime = dateTime;
        }
        
        @Override
        public String toString() {
            return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
    }
} 