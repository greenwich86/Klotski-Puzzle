package view.game;

import controller.GameController;
import model.AISolver;
import model.MapModel;
import model.Prop;
import view.FrameUtil;
import view.menu.SelectionMenuFrame;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GameFrame extends JFrame {
    private Clip victorySound;
    private GameController controller;
    private JButton restartBtn;
    private JButton loadBtn;
    private SelectionMenuFrame parentFrame; // Reference to the menu
    private boolean guestMode = false;
    private boolean timeAttackMode = false;
    private int timeLimit = 0; // Time limit in minutes
    private JLabel stepLabel;
    private JLabel timerLabel;
    private GamePanel gamePanel;
    private Timer countdownTimer;
    public PropPanel propPanel;
    private int currentTimeLeft = 0;  // 添加成员变量来跟踪剩余时间

    private void setButtonStyle(JButton button, Color bgColor, Color borderColor) {
        button.setFont(new Font("微软雅黑", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(bgColor);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor, 2),
                BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        button.setContentAreaFilled(true);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        final Color originalBg = bgColor;
        final Color hoverBg = borderColor.darker();
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(hoverBg);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(originalBg);
            }
        });
    }

    /**
     * Gets the prop panel
     * @return The prop panel
     */
    public PropPanel getPropPanel() {
        return propPanel;
    }

    /**
     * Direct method to handle obstacle removal
     * This method is called by PropPanel when the Obstacle Remover prop is used
     */
    public void handleObstacleRemoval() {
        System.out.println("GameFrame: Starting direct obstacle removal process");

        // First check if prop is available
        if (!controller.isPropAvailable(Prop.PropType.OBSTACLE_REMOVER)) {
            JOptionPane.showMessageDialog(this,
                    "You don't have any obstacle remover props available.",
                    "Obstacle Remover",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Show message to prompt user to select an obstacle
        JOptionPane.showMessageDialog(this,
                "Click on an obstacle (gray block) to remove it temporarily.",
                "Select Obstacle",
                JOptionPane.INFORMATION_MESSAGE);

        // Set game panel into obstacle selection mode
        gamePanel.setObstacleSelectionMode(true);
    }

    public GameFrame(int width, int height, MapModel mapModel) {
        this(width, height, mapModel, null);
    }

    public GameFrame(int width, int height, MapModel mapModel, SelectionMenuFrame parentFrame) {
        this.parentFrame = parentFrame;
        this.setTitle("2025 CS109 Project Demo");
        this.setLayout(new BorderLayout());
        this.setSize(Math.max(width, 900), Math.max(height, 750));
        this.setMinimumSize(new Dimension(900, 750));

        // Main panel with game board
        try {
            gamePanel = new GamePanel(mapModel);
            JScrollPane scrollPane = new JScrollPane(gamePanel);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
            this.add(scrollPane, BorderLayout.CENTER);

            this.controller = new GameController(gamePanel, mapModel);
            this.controller.restartGame();
            gamePanel.requestFocusInWindow();
        } catch (Exception e) {
            System.err.println("Error creating GamePanel:");
            e.printStackTrace();
            throw e;
        }

        // Control panel on the right
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        propPanel = new PropPanel(controller, this);
        propPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        propPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(100, 100, 255), 3),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        propPanel.setBackground(new Color(230, 230, 255));
        propPanel.setOpaque(true);
        propPanel.setVisible(true);
        propPanel.setMinimumSize(new Dimension(200, 180));
        propPanel.setPreferredSize(new Dimension(220, 200));

        updatePropPanelVisibility(controller.getCurrentLevel());

        // Step counter and timer
        JPanel statsPanel = new JPanel();
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
        statsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        this.stepLabel = new JLabel("Step: 0");
        stepLabel.setFont(new Font("serif", Font.ITALIC, 22));
        stepLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        this.timerLabel = new JLabel("Time: 00:00");
        timerLabel.setFont(new Font("serif", Font.BOLD, 22));
        timerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        timerLabel.setForeground(Color.BLACK);
        timerLabel.setVisible(false);  // 默认设置为不可见
        timerLabel.setPreferredSize(new Dimension(200, 30));
        timerLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        statsPanel.add(stepLabel);
        statsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        statsPanel.add(timerLabel);

        controlPanel.add(statsPanel);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        controlPanel.add(propPanel);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        gamePanel.setStepLabel(stepLabel);

        // AI Solve按钮（蓝色系）
        JButton aiButton = new JButton("AI Solve");
        aiButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        setButtonStyle(aiButton, new Color(232, 189, 189), new Color(156, 206, 211));
        aiButton.addActionListener(e -> {
            // 1. 创建带动画的"正在搜索"对话框
            JDialog searchingDialog = new JDialog(this, "AI正在搜索", true);
            searchingDialog.setUndecorated(true);
            JPanel panel = new JPanel(new BorderLayout());
            JLabel label = new JLabel("AI正在搜索，请稍候...", SwingConstants.CENTER);
            label.setFont(new Font("微软雅黑", Font.BOLD, 18));
            label.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            panel.add(label, BorderLayout.NORTH);

            // 加载gif动画，如果找不到则用进度条
            JLabel gifLabel;
            java.net.URL gifUrl = getClass().getResource("/resource/loading.gif");
            if (gifUrl != null) {
                ImageIcon loadingIcon = new ImageIcon(gifUrl);
                gifLabel = new JLabel(loadingIcon, SwingConstants.CENTER);
            } else {
                JProgressBar bar = new JProgressBar();
                bar.setIndeterminate(true);
                gifLabel = new JLabel();
                gifLabel.setLayout(new BorderLayout());
                gifLabel.add(bar, BorderLayout.CENTER);
            }
            panel.add(gifLabel, BorderLayout.CENTER);

            searchingDialog.getContentPane().add(panel);
            searchingDialog.setSize(300, 180);
            searchingDialog.setLocationRelativeTo(this);

            // 2. 用SwingWorker后台执行AI搜索
            SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
                AISolver solver = new AISolver(controller.getModel(), controller);
                @Override
                protected Boolean doInBackground() {
                    return solver.findSolution();
                }
                @Override
                protected void done() {
                    searchingDialog.dispose();
                    try {
                        if (get()) {
                            JOptionPane.showMessageDialog(GameFrame.this,
                                "AI搜索成功，正在自动演示解法！",
                                "AI Solver",
                                JOptionPane.INFORMATION_MESSAGE);
                            solver.executeSolution();
                        } else {
                            JOptionPane.showMessageDialog(GameFrame.this,
                                "未找到当前局面的解法。",
                                "AI Solver",
                                JOptionPane.WARNING_MESSAGE);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(GameFrame.this,
                            "AI搜索过程中发生错误！",
                            "AI Solver",
                            JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            worker.execute();
            searchingDialog.setVisible(true);
        });
        controlPanel.add(aiButton);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // 按钮十字
        JPanel directionPanel = new JPanel(new GridLayout(3, 3, 5, 5));
        directionPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        directionPanel.setOpaque(false);
        directionPanel.setPreferredSize(new Dimension(150, 150));

        Dimension btnSize = new Dimension(45, 45); // 按钮

// 上
        JButton upBtn = new JButton("↑");
        upBtn.addActionListener(e -> {
            gamePanel.doMoveUp();
            gamePanel.requestFocusInWindow();
        });
        setButtonStyle(upBtn, new Color(232, 189, 189), new Color(156, 206, 211));
        upBtn.setPreferredSize(btnSize);
        directionPanel.add(Box.createGlue()); // (0,0) 空白占位
        directionPanel.add(upBtn);          // (0,1) 上按钮
        directionPanel.add(Box.createGlue()); // (0,2) 空白占位

// 左、右
        JButton leftBtn = new JButton("←");
        leftBtn.addActionListener(e -> {
            gamePanel.doMoveLeft();
            gamePanel.requestFocusInWindow();
        });
        setButtonStyle(leftBtn, new Color(232, 189, 189), new Color(156, 206, 211));
        leftBtn.setPreferredSize(btnSize);
        directionPanel.add(leftBtn);        // (1,0) 左按钮
        directionPanel.add(Box.createGlue()); // (1,1) 中间空白
        JButton rightBtn = new JButton("→");
        rightBtn.addActionListener(e -> {
            gamePanel.doMoveRight();
            gamePanel.requestFocusInWindow();
        });
        setButtonStyle(rightBtn, new Color(232, 189, 189), new Color(156, 206, 211));
        rightBtn.setPreferredSize(btnSize);
        directionPanel.add(rightBtn);       // (1,2) 右按钮

// 下
        JButton downBtn = new JButton("↓");
        downBtn.addActionListener(e -> {
            gamePanel.doMoveDown();
            gamePanel.requestFocusInWindow();
        });
        setButtonStyle(downBtn, new Color(232, 189, 189), new Color(156, 206, 211));
        downBtn.setPreferredSize(btnSize);
        directionPanel.add(Box.createGlue()); // (2,0) 空白占位
        directionPanel.add(downBtn);         // (2,1) 下按钮
        directionPanel.add(Box.createGlue()); // (2,2) 空白占位

        controlPanel.add(directionPanel);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Restart按钮
        this.restartBtn = new JButton("Restart");
        restartBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        setButtonStyle(restartBtn, new Color(232, 189, 189), new Color(156, 206, 211));
        restartBtn.addActionListener(e -> {
            controller.restartGame();
            gamePanel.requestFocusInWindow();
        });
        // Undo
        JButton undoBtn = new JButton("Undo");
        undoBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        setButtonStyle(undoBtn, new Color(232, 189, 189), new Color(156, 206, 211));
        undoBtn.addActionListener(e -> {
            controller.undoMove();
            gamePanel.requestFocusInWindow();
        });

        // Load
        this.loadBtn = new JButton("Load");
        loadBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        setButtonStyle(loadBtn, new Color(232, 189, 189), new Color(156, 206, 211));
        loadBtn.addActionListener(e -> {
            if (guestMode) {
                JOptionPane.showMessageDialog(this, "Guest users cannot load games");
            } else {
                controller.loadGame();
                gamePanel.requestFocusInWindow();
            }
        });

        // Save
        JButton saveBtn = new JButton("Save");
        saveBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        setButtonStyle(saveBtn, new Color(232, 189, 189), new Color(156, 206, 211));
        saveBtn.addActionListener(e -> {
            if (guestMode) {
                JOptionPane.showMessageDialog(this, "Guest users cannot save games");
            } else {
                controller.saveGame();
                gamePanel.requestFocusInWindow();
            }
        });

        // 返回菜单
        JButton returnToMenuBtn = new JButton("Return to Menu");
        returnToMenuBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        setButtonStyle(returnToMenuBtn, new Color(156, 206, 211), new Color(232, 189, 189));
        returnToMenuBtn.addActionListener(e -> {
            returnToMenu();
        });

        controlPanel.add(restartBtn);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        controlPanel.add(undoBtn);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        controlPanel.add(loadBtn);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        controlPanel.add(saveBtn);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        controlPanel.add(returnToMenuBtn);

        this.add(controlPanel, BorderLayout.EAST);
        this.setLocationRelativeTo(null);

        this.addWindowListener(new WindowAdapter() {
            public void windowActivated(WindowEvent e) {
                gamePanel.requestFocusInWindow();
            }
        });

        this.requestFocusInWindow();
        gamePanel.requestFocusInWindow();
    }

    /**
     * Returns to the main menu screen
     */
    private void returnToMenu() {
        if (parentFrame != null) {
            resetTimerState();  // 在返回菜单时重置计时器状态
            parentFrame.setVisible(true);
            this.setVisible(false);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Cannot return to menu: Menu reference not available.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public void setGuestMode(boolean guestMode) {
        this.guestMode = guestMode;
        if (guestMode) {
            this.setTitle("2025 CS109 Project Demo (Guest Mode)");
            this.loadBtn.setEnabled(false);
            for (Component c : this.getContentPane().getComponents()) {
                if (c instanceof JButton && ((JButton)c).getText().equals("Save")) {
                    ((JButton)c).setEnabled(false);
                }
            }
        } else {
            this.setTitle("2025 CS109 Project Demo");
            this.loadBtn.setEnabled(true);
            for (Component c : this.getContentPane().getComponents()) {
                if (c instanceof JButton && ((JButton)c).getText().equals("Save")) {
                    ((JButton)c).setEnabled(true);
                }
            }
        }
    }

    public boolean isGuestMode() {
        return guestMode;
    }

    public GameController getController() {
        return controller;
    }

    public void setParentFrame(SelectionMenuFrame parentFrame) {
        this.parentFrame = parentFrame;
    }

    public void updatePropPanelVisibility(int level) {
        if (propPanel != null) {
            boolean propsAllowed = MapModel.LEVEL_PROPS_ALLOWED[level];

            System.out.println("Updating prop panel for level " + level +
                    " (name: " + MapModel.LEVEL_NAMES[level] +
                    ", props allowed: " + propsAllowed + ")");

            propPanel.setVisible(true);

            if (!propsAllowed) {
                if (level == 3) {
                    propPanel.setToolTipText("Props are disabled in Master difficulty");
                    propPanel.setBorder(BorderFactory.createTitledBorder(
                            BorderFactory.createLineBorder(Color.RED, 2),
                            "Props (Disabled in Master)"
                    ));
                } else {
                    propPanel.setToolTipText("Props are disabled in Easy difficulty");
                    propPanel.setBorder(BorderFactory.createTitledBorder(
                            BorderFactory.createLineBorder(Color.GRAY, 2),
                            "Props (Disabled in Easy)"
                    ));
                }
            } else {
                propPanel.setToolTipText("Use props to help solve the puzzle");
                propPanel.setBorder(BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(100, 100, 255), 3),
                        "Available Props"
                ));

                controller.initializeProps(level);

                System.out.println("Level " + level + " prop counts: " +
                        "Hint: " + controller.getPropCount(Prop.PropType.HINT) + ", " +
                        "Time Bonus: " + controller.getPropCount(Prop.PropType.TIME_BONUS) + ", " +
                        "Obstacle Remover: " + controller.getPropCount(Prop.PropType.OBSTACLE_REMOVER));
            }

            propPanel.updatePropAvailability();
            propPanel.revalidate();
            propPanel.repaint();
            this.revalidate();
            this.repaint();
        } else {
            System.out.println("Warning: PropPanel is null when trying to update visibility for level " + level);
        }
    }

    public boolean addTimeToTimer(int secondsToAdd) {
        if (!timeAttackMode || countdownTimer == null || !countdownTimer.isRunning()) {
            return false;
        }

        currentTimeLeft += secondsToAdd;
        updateTimerDisplay(currentTimeLeft);
        highlightTimerUpdate();
        return true;
    }

    private void highlightTimerUpdate() {
        final Color originalFg = timerLabel.getForeground();
        final Color originalBg = timerLabel.getBackground();
        final boolean wasOpaque = timerLabel.isOpaque();

        Timer flashTimer = new Timer(150, new ActionListener() {
            private int count = 0;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (count % 2 == 0) {
                    timerLabel.setForeground(Color.WHITE);
                    timerLabel.setBackground(new Color(0, 150, 0));
                    timerLabel.setOpaque(true);
                } else {
                    timerLabel.setForeground(originalFg);
                    timerLabel.setBackground(originalBg);
                    timerLabel.setOpaque(wasOpaque);
                }

                timerLabel.repaint();
                count++;

                if (count >= 4) {
                    ((Timer)e.getSource()).stop();
                    timerLabel.setForeground(originalFg);
                    timerLabel.setBackground(originalBg);
                    timerLabel.setOpaque(wasOpaque);
                    timerLabel.repaint();
                }
            }
        });

        flashTimer.setRepeats(true);
        flashTimer.start();
    }

    private void resetTimerState() {
        if (countdownTimer != null) {
            countdownTimer.stop();
            countdownTimer = null;
        }
        currentTimeLeft = 0;
        timerLabel.setText("Time: 00:00");
        timerLabel.setVisible(false);  // 重置时设置为不可见
        timerLabel.setForeground(Color.BLACK);
        timerLabel.setBackground(null);
        timerLabel.setOpaque(false);
    }

    public void setTimeAttackMode(boolean enabled, int minutes) {
        setTimeAttackMode(enabled, minutes, minutes * 60);  // 默认使用完整时间
    }

    public void setTimeAttackMode(boolean enabled, int minutes, int remainingSeconds) {
        System.out.println("setTimeAttackMode called with enabled=" + enabled + 
            ", minutes=" + minutes + 
            ", remainingSeconds=" + remainingSeconds);
        
        // 设置成员变量
        this.timeAttackMode = enabled;
        this.timeLimit = minutes;
        
        if (enabled) {
            System.out.println("Enabling time attack mode...");
            // 停止现有的计时器
            if (countdownTimer != null) {
                countdownTimer.stop();
                countdownTimer = null;
            }

            // 设置剩余时间
            currentTimeLeft = remainingSeconds;
            
            // 更新显示
            updateTimerDisplay();
            timerLabel.setVisible(true);
            System.out.println("Timer label visible: " + timerLabel.isVisible());
            
            // 创建新的计时器
            System.out.println("Creating new timer");
            countdownTimer = new Timer(1000, e -> {
                if (currentTimeLeft > 0) {
                    currentTimeLeft--;
                    updateTimerDisplay();
                } else {
                    gameOver();
                }
            });
            
            // 启动计时器
            System.out.println("Starting timer");
            countdownTimer.start();
            System.out.println("Timer started with " + currentTimeLeft + " seconds remaining");
            
            // 确保标签可见
            timerLabel.revalidate();
            timerLabel.repaint();
            System.out.println("Timer label revalidated and repainted");
        } else {
            // 禁用计时模式
            if (countdownTimer != null) {
                countdownTimer.stop();
                countdownTimer = null;
            }
            currentTimeLeft = 0;
            timerLabel.setVisible(false);
        }
    }

    private void updateTimerDisplay(int seconds) {
        int mins = seconds / 60;
        int secs = seconds % 60;
        String timeText = String.format("Time: %02d:%02d", mins, secs);
        timerLabel.setText(timeText);
        System.out.println("Timer display updated: " + timeText + ", Label visible: " + timerLabel.isVisible());
    }

    private void updateTimerDisplay() {
        updateTimerDisplay(currentTimeLeft);
    }

    private void gameOver() {
        Timer flashTimer = new Timer(250, new ActionListener() {
            private int count = 0;
            @Override
            public void actionPerformed(ActionEvent e) {
                if (count % 2 == 0) {
                    timerLabel.setForeground(Color.WHITE);
                    timerLabel.setBackground(Color.RED);
                    timerLabel.setOpaque(true);
                } else {
                    timerLabel.setForeground(Color.RED);
                    timerLabel.setBackground(null);
                    timerLabel.setOpaque(false);
                }
                count++;
                if (count > 10) {
                    ((Timer)e.getSource()).stop();
                    showTimeAttackGameOver();
                }
            }
        });
        flashTimer.start();
    }

    private void playVictorySound() {
        new Thread(() -> {
            try {
                if (victorySound == null || !victorySound.isRunning()) {
                    AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(GameFrame.class.getResourceAsStream("/resource/victory.wav"));
                    victorySound = AudioSystem.getClip();
                    victorySound.open(audioInputStream);
                    victorySound.start();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void showVictoryDialog() {
        playVictorySound();

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(new Color(240, 255, 240));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("🎉 Success!");
        titleLabel.setFont(new Font("Inter", Font.BOLD, 24));
        titleLabel.setForeground(new Color(30, 130, 76));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel msgLabel = new JLabel("You successfully moved Cao Cao to the exit!");
        msgLabel.setFont(new Font("Inter", Font.PLAIN, 18));
        msgLabel.setForeground(new Color(55, 65, 81));
        msgLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        msgLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        contentPanel.add(titleLabel);
        contentPanel.add(msgLabel);

        JOptionPane.showMessageDialog(this, contentPanel, "Victory", JOptionPane.PLAIN_MESSAGE);
    }

    private void showTimeAttackGameOver() {
        JOptionPane.showMessageDialog(this,
                "<html><h2>TIME'S UP!</h2><br>You ran out of time!</html>",
                "Game Over",
                JOptionPane.ERROR_MESSAGE);

        if (parentFrame != null) {
            if (countdownTimer != null && countdownTimer.isRunning()) {
                countdownTimer.stop();
            }
            parentFrame.setVisible(true);
            this.setVisible(false);
        } else {
            controller.restartGame();
            if (timeAttackMode) {
                setTimeAttackMode(true, timeLimit);
            }
        }
    }

    public boolean isTimeAttackMode() {
        return timeAttackMode;
    }

    public int getRemainingTime() {
        return currentTimeLeft;
    }

    public int getTimeLimit() {
        return timeLimit;
    }
}