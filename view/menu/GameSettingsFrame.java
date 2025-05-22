package view.menu;

import view.game.GameFrame;
import model.MapModel;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.RoundRectangle2D;
import java.net.URL;

public class GameSettingsFrame extends JFrame {
    private final GameFrame gameFrame;
    private final SelectionMenuFrame parentFrame;

    private JRadioButton[] levelButtons;
    private JRadioButton normalModeButton;
    private JRadioButton timeAttackButton;
    private int selectedTimeLimit = 3;
    private JPanel buttonPanel;
    private JPanel gameModePanel;

    private static final Color PRIMARY_COLOR = new Color(139, 69, 19);    // 主色调
    private static final Color ACCENT_COLOR = new Color(244, 164, 96);    // 强调色
    private static final Color BACKGROUND_COLOR = new Color(245, 245, 220, 240); // 背景
    private static final Color TEXT_COLOR = new Color(51, 51, 51);         // 文本
    private static final Color DISABLED_COLOR = new Color(200, 200, 200);  //
    private static final Color HOVER_COLOR = new Color(184, 134, 11, 200); // 悬停颜色
    private static final Color SELECTED_COLOR = new Color(139, 69, 19);    // 选中颜色

    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 32);
    private static final Font SUBTITLE_FONT = new Font("Segoe UI", Font.BOLD, 18);
    private static final Font BODY_FONT = new Font("Segoe UI", Font.PLAIN, 16);
    private static final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 18);

    private Image backgroundImage;

    public GameSettingsFrame(int width, int height, GameFrame gameFrame, SelectionMenuFrame parentFrame) {
        this.gameFrame = gameFrame;
        this.parentFrame = parentFrame;

        URL bgImageUrl = getClass().getResource("/resource/Settingsframe.jpg");
        if (bgImageUrl != null) {
            this.backgroundImage = new ImageIcon(bgImageUrl).getImage();
        }
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        this.setTitle("Game Settings");
        this.setContentPane(new BackgroundPanel());
        this.setLayout(new BorderLayout());
        this.setSize(Math.max(width, 600), Math.max(height, 700));
        this.setPreferredSize(new Dimension(950, 850));
        this.setUndecorated(true);

        JPanel titleBar = createTitleBar();

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);

        // 标题区
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titlePanel.setOpaque(false);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 30, 0));

        JLabel titleLabel = new JLabel("Game Settings");
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(PRIMARY_COLOR);
        titlePanel.add(titleLabel);

        // 主设置面板
        JPanel settingsPanel = new JPanel(new GridBagLayout());
        settingsPanel.setOpaque(false);
        settingsPanel.setBorder(BorderFactory.createEmptyBorder(0, 60, 30, 60));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 30, 0);
        gbc.weightx = 1.0;

        JPanel difficultyPanel = createDifficultyPanel();
        difficultyPanel.setBorder(createRoundedBorder("Difficulty Level", PRIMARY_COLOR));
        difficultyPanel.setBackground(BACKGROUND_COLOR);
        settingsPanel.add(difficultyPanel, gbc);

        gbc.gridy = 1;
        gameModePanel = createGameModePanel();
        gameModePanel.setBorder(createRoundedBorder("Game Mode", PRIMARY_COLOR));
        gameModePanel.setBackground(BACKGROUND_COLOR);
        settingsPanel.add(gameModePanel, gbc);

        JScrollPane scrollPane = new JScrollPane(settingsPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUI(new CustomScrollBarUI());

        JPanel controlButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 20));
        controlButtonPanel.setOpaque(false);
        controlButtonPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 30, 0));

        JButton startButton = createStyledButton("Start Game", PRIMARY_COLOR);
        startButton.addActionListener(e -> startGame());

        JButton backButton = createStyledButton("Back", DISABLED_COLOR);
        backButton.addActionListener(e -> goBack());

        controlButtonPanel.add(backButton);
        controlButtonPanel.add(startButton);

        contentPanel.add(titlePanel, BorderLayout.NORTH);
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        contentPanel.add(controlButtonPanel, BorderLayout.SOUTH);

        this.add(titleBar, BorderLayout.NORTH);
        this.add(contentPanel, BorderLayout.CENTER);

        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.pack();

        setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 20, 20));

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                parentFrame.setVisible(true);
            }
        });

        addWindowDragFunctionality();
    }

    // 标题栏
    private JPanel createTitleBar() {
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setBackground(new Color(139, 69, 19, 220));
        titleBar.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, ACCENT_COLOR));
        titleBar.setPreferredSize(new Dimension(0, 40));

        JLabel titleLabel = new JLabel("  Klotski Puzzle - Game Settings");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);

        JButton closeButton = new JButton("×");
        closeButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        closeButton.setForeground(Color.WHITE);
        closeButton.setBackground(new Color(139, 69, 19));
        closeButton.setBorder(null);
        closeButton.setFocusPainted(false);
        closeButton.setContentAreaFilled(false);

        closeButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                closeButton.setForeground(Color.RED);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                closeButton.setForeground(Color.WHITE);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                parentFrame.setVisible(true);
                dispose();
            }
        });

        titleBar.add(titleLabel, BorderLayout.WEST);
        titleBar.add(closeButton, BorderLayout.EAST);

        return titleBar;
    }

    // 添加窗口拖动功能
    private void addWindowDragFunctionality() {
        final Point[] initialClick = {new Point()};

        Component titleBar = getContentPane().getComponent(0);
        titleBar.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                initialClick[0] = e.getPoint();
            }
        });

        titleBar.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                // 获取当前窗口位置
                int thisX = getLocation().x;
                int thisY = getLocation().y;

                // 计算移动距离
                int xMoved = e.getX() - initialClick[0].x;
                int yMoved = e.getY() - initialClick[0].y;

                // 移动窗口
                setLocation(thisX + xMoved, thisY + yMoved);
            }
        });
    }

    // 创建带圆角的边框
    private Border createRoundedBorder(String title, Color color) {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        null,
                        title,
                        TitledBorder.DEFAULT_JUSTIFICATION,
                        TitledBorder.DEFAULT_POSITION,
                        SUBTITLE_FONT,
                        color
                ),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)
        );
    }

    // 自定义滚动条UI
    private static class CustomScrollBarUI extends javax.swing.plaf.basic.BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            this.thumbColor = new Color(139, 69, 19, 150);
            this.trackColor = new Color(245, 245, 220, 100);
            this.thumbHighlightColor = new Color(184, 134, 11);
            this.thumbLightShadowColor = new Color(139, 69, 19);
            this.thumbDarkShadowColor = new Color(101, 67, 33);
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return createZeroButton();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return createZeroButton();
        }

        private JButton createZeroButton() {
            JButton button = new JButton();
            button.setPreferredSize(new Dimension(0, 0));
            button.setMinimumSize(new Dimension(0, 0));
            button.setMaximumSize(new Dimension(0, 0));
            return button;
        }
    }

    private JButton createStyledButton(String text, Color baseColor) {
        JButton button = new JButton(text);
        button.setFont(BUTTON_FONT);
        button.setForeground(Color.WHITE);
        button.setBackground(baseColor);
        button.setPreferredSize(new Dimension(160, 50));
        button.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(baseColor.darker(), 2, true),
                new EmptyBorder(8, 16, 8, 16)
        ));
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(true);

        // 按钮悬停
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(baseColor.darker());
                button.setBorder(BorderFactory.createLineBorder(baseColor.darker().darker(), 2, true));
                button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(baseColor);
                button.setBorder(BorderFactory.createLineBorder(baseColor.darker(), 2, true));
            }
        });

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                button.setBorder(BorderFactory.createLineBorder(baseColor.darker().darker(), 3, true));
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                button.setBorder(BorderFactory.createLineBorder(baseColor.darker(), 2, true));
            }
        });

        return button;
    }

    private JPanel createDifficultyPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BACKGROUND_COLOR);

        levelButtons = new JRadioButton[MapModel.LEVELS.length];
        ButtonGroup levelGroup = new ButtonGroup();

        for (int i = 0; i < MapModel.LEVELS.length; i++) {
            levelButtons[i] = new JRadioButton(MapModel.LEVEL_NAMES[i]);
            levelButtons[i].setAlignmentX(Component.LEFT_ALIGNMENT);
            levelButtons[i].setFont(BODY_FONT);
            levelButtons[i].setBackground(BACKGROUND_COLOR);
            levelButtons[i].setForeground(TEXT_COLOR);
            levelButtons[i].setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

            if (i == MapModel.LEVELS.length - 1) {
                levelButtons[i].setForeground(new Color(165, 42, 42));
                levelButtons[i].setFont(new Font("Segoe UI", Font.BOLD, 17));
            } else if (i == 0) {
                levelButtons[i].setForeground(PRIMARY_COLOR);
            }

            levelButtons[i].setToolTipText(switch (i) {
                case 0 -> "Classic level - No props available";
                case 1 -> "Harder puzzles - Props allowed";
                case 2 -> "Expert difficulty - Props allowed";
                case 3 -> "Master difficulty - 5 minute time limit, No props, Military camps restrict soldier movement";
                default -> "";
            });

            levelButtons[i].addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    JRadioButton btn = (JRadioButton) e.getSource();
                    if (!btn.isSelected()) {
                        btn.setBackground(new Color(230, 230, 210, 240));
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    JRadioButton btn = (JRadioButton) e.getSource();
                    if (!btn.isSelected()) {
                        btn.setBackground(BACKGROUND_COLOR);
                    }
                }
            });

            final int level = i;
            levelButtons[i].addActionListener(e -> {
                for (JRadioButton btn : levelButtons) {
                    btn.setBackground(BACKGROUND_COLOR);
                }

                levelButtons[level].setBackground(new Color(220, 220, 200, 240));

                if (level == MapModel.LEVELS.length - 1) {
                    timeAttackButton.setSelected(true);
                    timeAttackButton.setEnabled(false);
                    normalModeButton.setEnabled(false);
                    selectedTimeLimit = MapModel.DEFAULT_MASTER_TIME_LIMIT;

                    for (Component comp : buttonPanel.getComponents()) {
                        JButton btn = (JButton) comp;
                        if (btn.getText().equals("5 MINUTES")) {
                            btn.setBackground(PRIMARY_COLOR);
                            btn.setForeground(Color.WHITE);
                            btn.setEnabled(true);
                        } else {
                            btn.setBackground(DISABLED_COLOR);
                            btn.setForeground(Color.GRAY);
                            btn.setEnabled(false);
                        }
                    }

                    JOptionPane.showMessageDialog(
                            this,
                            "Master difficulty enforces a 5-minute time limit and contains military camps\n" +
                                    "that soldiers cannot step on. No props are available in this mode.",
                            "Master Difficulty",
                            JOptionPane.WARNING_MESSAGE
                    );
                } else {
                    timeAttackButton.setEnabled(true);
                    normalModeButton.setEnabled(true);
                    for (Component comp : buttonPanel.getComponents()) {
                        if (comp instanceof JButton) comp.setEnabled(true);
                    }
                }
            });

            levelGroup.add(levelButtons[i]);
            panel.add(levelButtons[i]);
            panel.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        return panel;
    }

    private JPanel createGameModePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BACKGROUND_COLOR);
        JPanel modeSelectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        modeSelectionPanel.setOpaque(false);

        normalModeButton = new JRadioButton("Normal Mode");
        normalModeButton.setFont(SUBTITLE_FONT);
        normalModeButton.setBackground(BACKGROUND_COLOR);
        normalModeButton.setSelected(true);
        normalModeButton.setForeground(TEXT_COLOR);
        normalModeButton.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        timeAttackButton = new JRadioButton("⏱️ TIME ATTACK MODE");
        timeAttackButton.setFont(SUBTITLE_FONT);
        timeAttackButton.setForeground(PRIMARY_COLOR);
        timeAttackButton.setBackground(BACKGROUND_COLOR);
        timeAttackButton.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));


        normalModeButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!normalModeButton.isSelected()) {
                    normalModeButton.setBackground(new Color(230, 230, 210, 240));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!normalModeButton.isSelected()) {
                    normalModeButton.setBackground(BACKGROUND_COLOR);
                }
            }
        });

        timeAttackButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!timeAttackButton.isSelected()) {
                    timeAttackButton.setBackground(new Color(230, 230, 210, 240));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!timeAttackButton.isSelected()) {
                    timeAttackButton.setBackground(BACKGROUND_COLOR);
                }
            }
        });

        ButtonGroup modeGroup = new ButtonGroup();
        modeGroup.add(normalModeButton);
        modeGroup.add(timeAttackButton);

        modeSelectionPanel.add(normalModeButton);
        modeSelectionPanel.add(Box.createRigidArea(new Dimension(30, 0)));
        modeSelectionPanel.add(timeAttackButton);

        panel.add(modeSelectionPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        JPanel timerOptionsPanel = new JPanel(new BorderLayout());
        timerOptionsPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(PRIMARY_COLOR, 2, true),
                new EmptyBorder(10, 10, 10, 10)
        ));
        timerOptionsPanel.setBackground(Color.WHITE);
        timerOptionsPanel.setPreferredSize(new Dimension(320, 200));

        JLabel timeLabel = new JLabel("⏱️ SELECT TIME LIMIT");
        timeLabel.setFont(SUBTITLE_FONT);
        timeLabel.setForeground(PRIMARY_COLOR);
        timeLabel.setHorizontalAlignment(JLabel.CENTER);
        timeLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        buttonPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton btn3Min = createTimeButton("3 MINUTES", 3);
        JButton btn5Min = createTimeButton("5 MINUTES", 5);
        JButton btn7Min = createTimeButton("7 MINUTES", 7);
        buttonPanel.add(btn3Min);
        buttonPanel.add(btn5Min);
        buttonPanel.add(btn7Min);

        timerOptionsPanel.add(timeLabel, BorderLayout.NORTH);
        timerOptionsPanel.add(buttonPanel, BorderLayout.CENTER);

        panel.add(timerOptionsPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        JLabel timerInfoLabel = new JLabel("<html>In Time Attack Mode, you must solve<br>the puzzle before time runs out!</html>");
        timerInfoLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        timerInfoLabel.setForeground(new Color(100, 100, 100));
        panel.add(timerInfoLabel);
        timeAttackButton.addActionListener(e -> {
            timerOptionsPanel.setBorder(BorderFactory.createLineBorder(PRIMARY_COLOR, 2, true));
            timerOptionsPanel.setBackground(Color.WHITE);
            for (Component comp : buttonPanel.getComponents()) {
                if (comp instanceof JButton) {
                    JButton btn = (JButton) comp;
                    if (btn.isEnabled()) {
                        btn.setBackground(btn.getText().equals("3 MINUTES") ?
                                (selectedTimeLimit == 3 ? PRIMARY_COLOR : new Color(240, 240, 220)) :
                                (btn.getText().equals("5 MINUTES") ?
                                        (selectedTimeLimit == 5 ? PRIMARY_COLOR : new Color(240, 240, 220)) :
                                        (selectedTimeLimit == 7 ? PRIMARY_COLOR : new Color(240, 240, 220))));
                        btn.setForeground(btn.getText().equals("3 MINUTES") ?
                                (selectedTimeLimit == 3 ? Color.WHITE : PRIMARY_COLOR) :
                                (btn.getText().equals("5 MINUTES") ?
                                        (selectedTimeLimit == 5 ? Color.WHITE : PRIMARY_COLOR) :
                                        (selectedTimeLimit == 7 ? Color.WHITE : PRIMARY_COLOR)));
                    }
                }
            }
        });

        normalModeButton.addActionListener(e -> {
            timerOptionsPanel.setBorder(BorderFactory.createLineBorder(DISABLED_COLOR, 2, true));
            timerOptionsPanel.setBackground(new Color(240, 240, 220));
            for (Component comp : buttonPanel.getComponents()) {
                JButton btn = (JButton) comp;
                btn.setBackground(DISABLED_COLOR);
                btn.setForeground(Color.GRAY);
            }
        });

        return panel;
    }

    private JButton createTimeButton(String text, int time) {
        JButton button = new JButton(text);
        button.setFont(BUTTON_FONT);
        button.setPreferredSize(new Dimension(240, 50));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(PRIMARY_COLOR, 2, true),
                new EmptyBorder(8, 16, 8, 16)
        ));
        button.setContentAreaFilled(false);
        button.setOpaque(true);

        if (time == selectedTimeLimit) {
            button.setBackground(PRIMARY_COLOR);
            button.setForeground(Color.WHITE);
        } else {
            button.setBackground(new Color(240, 240, 220));
            button.setForeground(PRIMARY_COLOR);
        }

        button.addActionListener(e -> {
            selectedTimeLimit = time;
            for (Component comp : buttonPanel.getComponents()) {
                JButton btn = (JButton) comp;
                if (btn == button) {
                    btn.setBackground(PRIMARY_COLOR);
                    btn.setForeground(Color.WHITE);
                } else {
                    btn.setBackground(new Color(240, 240, 220));
                    btn.setForeground(PRIMARY_COLOR);
                }
            }
        });

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                JButton btn = (JButton) e.getSource();
                if (btn.isEnabled() && !btn.getBackground().equals(PRIMARY_COLOR)) {
                    btn.setBackground(new Color(230, 230, 210));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                JButton btn = (JButton) e.getSource();
                if (btn.isEnabled() && !btn.getBackground().equals(PRIMARY_COLOR)) {
                    btn.setBackground(new Color(240, 240, 220));
                }
            }
        });

        return button;
    }
    private void startGame() {
        int selectedLevel = 0;
        for (int i = 0; i < levelButtons.length; i++) {
            if (levelButtons[i].isSelected()) {
                selectedLevel = i;
                break;
            }
        }

        boolean timeAttackMode = timeAttackButton.isSelected();
        int timeLimit = timeAttackMode ? selectedTimeLimit : 3;

        gameFrame.getController().setLevel(selectedLevel);
        gameFrame.setTimeAttackMode(timeAttackMode, timeLimit);
        gameFrame.setParentFrame(parentFrame);
        gameFrame.setVisible(true);
        this.dispose();
    }

    private void goBack() {
        parentFrame.setVisible(true);
        this.dispose();
    }

    private class BackgroundPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (backgroundImage != null) {
                int width = getWidth();
                int height = getHeight();
                g.drawImage(backgroundImage, 0, 0, width, height, this);

                g.setColor(new Color(255, 255, 255, 180));
                g.fillRect(0, 0, width, height);
            }
        }
    }
}