package view.menu;

import view.game.GameFrame;
import view.login.LoginFrame;
import model.MapModel;
import view.LeaderboardFrame;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class SelectionMenuFrame extends JFrame {
    private static final Color PRIMARY_COLOR = new Color(255, 255, 240);
    private static final Color TEXT_COLOR = new Color(57, 76, 83);
    private static final Color BORDER_COLOR = new Color(226, 232, 240);
    private static final Color BG_LIGHT = new Color(248, 250, 252);
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 24);
    private static final Font SUBTITLE_FONT = new Font("Segoe UI", Font.ITALIC, 16);
    private static final Font BUTTON_FONT = new Font("Segoe UI", Font.PLAIN, 16);
    private static final Image BACKGROUND_IMAGE;
    private static Clip backgroundMusic;

    static {
        ImageIcon icon = new ImageIcon(SelectionMenuFrame.class.getResource("/resource/menuframe.jpg"));
        if (icon.getImageLoadStatus() != MediaTracker.COMPLETE) {
            JOptionPane.showMessageDialog(null, "背景图加载失败，请检查menuframe.jpg是否存在", "错误", JOptionPane.ERROR_MESSAGE);
            BACKGROUND_IMAGE = null;
        } else {
            BACKGROUND_IMAGE = icon.getImage();
        }

        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(SelectionMenuFrame.class.getResourceAsStream("/resource/bgm.wav"));
            backgroundMusic = AudioSystem.getClip();
            backgroundMusic.open(audioInputStream);
            backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "背景音乐加载失败: " + e.getMessage(), "提示", JOptionPane.WARNING_MESSAGE);
        }
    }

    private GameFrame gameFrame;
    private final String currentUser;

    public SelectionMenuFrame(int width, int height, String username) {
        this.currentUser = username;
        initUI(width, height);
    }

    private void initUI(int width, int height) {
        setTitle("Klotski Puzzle - Main Menu");
        setSize(width, height);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (BACKGROUND_IMAGE != null) {
                    g.drawImage(BACKGROUND_IMAGE, 0, 0, getWidth(), getHeight(), this);
                }
            }
        };
        backgroundPanel.setLayout(new BorderLayout());

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        JLabel titleLabel = new JLabel("Klotski Puzzle", SwingConstants.CENTER);
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(PRIMARY_COLOR);
        headerPanel.add(titleLabel, BorderLayout.NORTH);

        JLabel welcomeLabel = new JLabel((currentUser.isEmpty() ? "Guest" : currentUser) + "!", SwingConstants.CENTER);
        welcomeLabel.setFont(SUBTITLE_FONT);
        welcomeLabel.setForeground(PRIMARY_COLOR);
        headerPanel.add(welcomeLabel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));

        JButton startGameBtn = createMenuButton("Start New Game");
        startGameBtn.addActionListener(e -> openGameSettings());

        JButton loadGameBtn = createMenuButton("Load Game");
        loadGameBtn.addActionListener(e -> loadGame());

        JButton rulesBtn = createMenuButton("Game Rules");
        rulesBtn.addActionListener(e -> showGameRules());

        JButton leaderboardBtn = createMenuButton("Leaderboard");
        leaderboardBtn.addActionListener(e -> showLeaderboard());

        JButton logoutBtn = createMenuButton("Logout");
        logoutBtn.addActionListener(e -> logout());

        buttonPanel.add(Box.createVerticalGlue());
        buttonPanel.add(startGameBtn);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 25)));
        buttonPanel.add(loadGameBtn);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 25)));
        buttonPanel.add(rulesBtn);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 25)));
        buttonPanel.add(leaderboardBtn);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 25)));
        buttonPanel.add(logoutBtn);
        buttonPanel.add(Box.createVerticalGlue());
        backgroundPanel.add(headerPanel, BorderLayout.NORTH);
        backgroundPanel.add(buttonPanel, BorderLayout.CENTER);
        add(backgroundPanel);

        setVisible(true);
    }

    private JButton createMenuButton(String text) {
        JButton button = new JButton(text);
        button.setFont(BUTTON_FONT);
        button.setForeground(TEXT_COLOR);
        button.setBackground(new Color(27, 84, 84, 200));
        button.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(10, 20, 10, 20)
        ));
        button.setPreferredSize(new Dimension(220, 45));
        button.setMaximumSize(new Dimension(220, 45));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFocusPainted(false);

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(240, 250, 240, 220));
                button.setForeground(PRIMARY_COLOR);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(27, 84, 84, 200));
                button.setForeground(TEXT_COLOR);
            }
        });

        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        return button;
    }

    private void openGameSettings() {
        if (this.gameFrame == null) {
            MapModel mapModel = new MapModel();
            this.gameFrame = new GameFrame(800, 600, mapModel);
            this.gameFrame.getController().setCurrentUser(currentUser);
            this.gameFrame.setGuestMode(currentUser.isEmpty());
        }
        GameSettingsFrame settingsFrame = new GameSettingsFrame(400, 350, this.gameFrame, this);
        settingsFrame.setVisible(true);
        this.setVisible(false);
    }

    private void loadGame() {
        if (currentUser.isEmpty()) {
            JOptionPane.showMessageDialog(this, "游客模式无法加载存档", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (this.gameFrame == null) {
            MapModel mapModel = new MapModel();
            this.gameFrame = new GameFrame(800, 600, mapModel);
            this.gameFrame.getController().setCurrentUser(currentUser);
            this.gameFrame.setGuestMode(false);
        }
        if (this.gameFrame.getController().loadGame()) {
            this.gameFrame.setVisible(true);
            this.setVisible(false);
        }
    }

    private void showGameRules() {
        JOptionPane.showMessageDialog(this,
                "<html><h2 style='color:#990000;text-align:center'>Klotski Puzzle Rules</h2>" +
                        "<hr style='border:1px solid #cccccc'>" +

                        "<h3 style='color:#333399'>Game Objective</h3>" +
                        "<p>Move the red block (Cao Cao) to the exit at the bottom center of the board. " +
                        "Clear the path by sliding other blocks out of the way.</p>" +

                        "<h3 style='color:#333399'>Basic Controls</h3>" +
                        "<ul>" +
                        "<li><b>Select Block:</b> Click on any block with the mouse</li>" +
                        "<li><b>Move Block:</b> Use arrow keys to move the selected block</li>" +
                        "<li><b>Undo:</b> Click the Undo button to reverse your last move</li>" +
                        "<li><b>Restart:</b> Click the Restart button to reset the puzzle</li>" +
                        "</ul>" +

                        "<h3 style='color:#333399'>Piece Types</h3>" +
                        "<ul>" +
                        "<li><b style='color:red'>Cao Cao (曹操):</b> Red 2×2 block - must be moved to the exit</li>" +
                        "<li><b style='color:#FF8C00'>Guan Yu (关羽):</b> Orange 2×1 horizontal block</li>" +
                        "<li><b style='color:blue'>General (将军):</b> Blue 1×2 vertical block</li>" +
                        "<li><b style='color:green'>Soldier (士兵):</b> Green 1×1 block</li>" +
                        "<li><b style='color:#FF00FF'>Zhou Yu (周瑜):</b> Magenta 1×3 horizontal block</li>" +
                        "<li><b style='color:#696969'>Obstacle (障碍):</b> Gray immovable block</li>" +
                        "<li><b>Military Camp:</b> Special area that soldiers cannot step on (Master difficulty only)</li>" +
                        "</ul>" +

                        "<h3 style='color:#333399'>Difficulty Levels</h3>" +
                        "<ul>" +
                        "<li><b>Easy:</b> Standard 4×5 board with classic layout. No props available.</li>" +
                        "<li><b>Hard:</b> 5×6 board with Cao Cao at top middle and obstacles. Props allowed.</li>" +
                        "<li><b>Expert:</b> 6×7 board with more complex layout and obstacles. Props allowed.</li>" +
                        "<li><b>Master:</b> 6×7 board with military camps that soldiers cannot step on. No props, enforced 5-minute time limit.</li>" +
                        "</ul>" +

                        "<h3 style='color:#333399'>Game Modes</h3>" +
                        "<ul>" +
                        "<li><b>Normal Mode:</b> Solve the puzzle with no time constraints.</li>" +
                        "<li><b>Time Attack Mode:</b> Solve the puzzle before time runs out (3, 5, or 7 minutes).</li>" +
                        "</ul>" +

                        "<h3 style='color:#333399'>Props System</h3>" +
                        "<p>Props are special items available in Hard and Expert difficulty levels:</p>" +
                        "<ul>" +
                        "<li><b>Hint:</b> Highlights a suggested move to help you progress.</li>" +
                        "<li><b>Time Bonus:</b> Adds extra time in Time Attack Mode.</li>" +
                        "<li><b>Obstacle Remover:</b> Temporarily removes an obstacle block.</li>" +
                        "</ul>" +

                        "<h3 style='color:#333399'>AI Solver</h3>" +
                        "<p>The AI Solver can automatically solve the puzzle for you, but it doesn't use props - it finds a pure solution based on moves only.</p>" +

                        "<hr style='border:1px solid #cccccc'>" +
                        "<p style='text-align:center;font-style:italic'>Complete the puzzle in as few moves as possible to master the game!</p>" +
                        "</html>",
                "Game Rules",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void showLeaderboard() {
        LeaderboardFrame leaderboardFrame = new LeaderboardFrame();
        leaderboardFrame.setVisible(true);
    }

    private void logout() {
        LoginFrame loginFrame = new LoginFrame(600, 450);
        loginFrame.setVisible(true);
        if (this.gameFrame != null) {
            loginFrame.setGameFrame(this.gameFrame);
        } else {
            loginFrame.setGameFrame(new GameFrame(800, 600, new MapModel()));
        }
        this.dispose();
    }

    public void setGameFrame(GameFrame gameFrame) {
        this.gameFrame = gameFrame;
    }

    @Override
    public void dispose() {
        if (backgroundMusic != null && backgroundMusic.isRunning()) {
            backgroundMusic.stop();
        }
        super.dispose();
    }
}