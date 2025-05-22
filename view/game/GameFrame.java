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

        this.timerLabel = new JLabel("");
        timerLabel.setFont(new Font("serif", Font.BOLD, 22));
        timerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        timerLabel.setForeground(Color.RED);
        timerLabel.setVisible(false);

        statsPanel.add(stepLabel);
        statsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        statsPanel.add(timerLabel);

        controlPanel.add(statsPanel);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        controlPanel.add(propPanel);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        gamePanel.setStepLabel(stepLabel);

        JButton aiButton = new JButton("AI Solve");
        aiButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        setButtonStyle(aiButton, new Color(52, 152, 219), new Color(41, 128, 185)); // 蓝
        controlPanel.add(aiButton);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        this.restartBtn = new JButton("Restart");
        restartBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        setButtonStyle(restartBtn, new Color(52, 152, 219), new Color(41, 128, 185));
        restartBtn.addActionListener(e -> {
            controller.restartGame();
            gamePanel.requestFocusInWindow();
        });

        JButton undoBtn = new JButton("Undo");
        undoBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        setButtonStyle(undoBtn, new Color(52, 152, 219), new Color(41, 128, 185));
        undoBtn.addActionListener(e -> {
            controller.undoMove();
            gamePanel.requestFocusInWindow();
        });

        this.loadBtn = new JButton("Load");
        loadBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        setButtonStyle(loadBtn, new Color(52, 152, 219), new Color(41, 128, 185));
        loadBtn.addActionListener(e -> {
            if (guestMode) {
                JOptionPane.showMessageDialog(this, "Guest users cannot load games");
            } else {
                controller.loadGame();
                gamePanel.requestFocusInWindow();
            }
        });

        JButton saveBtn = new JButton("Save");
        saveBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        setButtonStyle(saveBtn, new Color(52, 152, 219), new Color(41, 128, 185));
        saveBtn.addActionListener(e -> {
            if (guestMode) {
                JOptionPane.showMessageDialog(this, "Guest users cannot save games");
            } else {
                controller.saveGame();
                gamePanel.requestFocusInWindow();
            }
        });

        JButton returnToMenuBtn = new JButton("Return to Menu");
        returnToMenuBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        setButtonStyle(returnToMenuBtn, new Color(46, 204, 113), new Color(39, 174, 96)); // 绿
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
            if (countdownTimer != null && countdownTimer.isRunning()) {
                countdownTimer.stop();
            }
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

    private int currentTimeLeft = 0;

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

    public void setTimeAttackMode(boolean enabled, int minutes) {
        this.timeAttackMode = enabled;
        this.timeLimit = minutes;

        if (countdownTimer != null && countdownTimer.isRunning()) {
            countdownTimer.stop();
        }

        if (enabled) {
            timerLabel.setVisible(true);
            currentTimeLeft = minutes * 60;
            updateTimerDisplay(currentTimeLeft);

            countdownTimer = new Timer(1000, e -> {
                currentTimeLeft--;
                updateTimerDisplay(currentTimeLeft);

                if (currentTimeLeft <= 60) {
                    timerLabel.setForeground(Color.RED);
                } else {
                    timerLabel.setForeground(Color.BLACK);
                }

                if (currentTimeLeft <= 0) {
                    ((Timer)e.getSource()).stop();
                    timeAttackGameOver();
                }
            });

            countdownTimer.start();
        } else {
            timerLabel.setVisible(false);
        }
    }

    private void updateTimerDisplay(int seconds) {
        int mins = seconds / 60;
        int secs = seconds % 60;
        timerLabel.setText(String.format("Time: %02d:%02d", mins, secs));
    }

    private void timeAttackGameOver() {
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
}