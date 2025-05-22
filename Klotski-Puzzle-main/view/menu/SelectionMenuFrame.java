package view.menu;

import view.FrameUtil;
import view.game.GameFrame;
import view.login.LoginFrame;
import controller.GameController;
import model.MapModel;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

/**
 * 主菜单界面，提供登录后的核心功能入口：
 * - 开始新游戏（打开设置界面）
 * - 加载存档
 * - 查看游戏规则
 * - 退出登录
 */
public class SelectionMenuFrame extends JFrame {
    // 颜色常量
    private static final Color PRIMARY_COLOR = new Color(255, 255, 240);  // 主色（米白）
    private static final Color TEXT_COLOR = new Color(57, 76, 83);       // 文字色（深灰蓝）
    private static final Color BORDER_COLOR = new Color(226, 232, 240);  // 边框色（浅灰）
    private static final Color BG_LIGHT = new Color(248, 250, 252);      // 浅背景色
    // 字体常量
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 24);    // 标题字体
    private static final Font SUBTITLE_FONT = new Font("Segoe UI", Font.ITALIC, 16); // 副标题字体
    private static final Font BUTTON_FONT = new Font("Segoe UI", Font.PLAIN, 16);   // 按钮字体
    // 背景资源
    private static final Image BACKGROUND_IMAGE;  // 背景图
    private static Clip backgroundMusic;         // 背景音乐

    // 静态初始化：加载背景图和音乐
    static {
        // 加载背景图（路径需与resources目录一致）
        ImageIcon icon = new ImageIcon(SelectionMenuFrame.class.getResource("/resource/menuframe.jpg"));
        if (icon.getImageLoadStatus() != MediaTracker.COMPLETE) {
            JOptionPane.showMessageDialog(null, "背景图加载失败，请检查menuframe.jpg是否存在", "错误", JOptionPane.ERROR_MESSAGE);
            BACKGROUND_IMAGE = null;
        } else {
            BACKGROUND_IMAGE = icon.getImage();
        }

        // 加载并播放背景音乐（循环）
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(SelectionMenuFrame.class.getResourceAsStream("/resource/bgm.wav"));
            backgroundMusic = AudioSystem.getClip();
            backgroundMusic.open(audioInputStream);
            backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY); // 无限循环
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "背景音乐加载失败: " + e.getMessage(), "提示", JOptionPane.WARNING_MESSAGE);
        }
    }

    private GameFrame gameFrame;
    private final String currentUser;

    public SelectionMenuFrame(int width, int height, String username) {
        this.currentUser = username;
        initUI(width, height); // 调用美化后的UI初始化
    }

    /**
     * 初始化美化后的UI界面
     */
    private void initUI(int width, int height) {
        setTitle("Klotski Puzzle - Main Menu");
        setSize(width, height);
        setLocationRelativeTo(null); // 窗口居中
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setResizable(false); // 禁止调整窗口大小

        // 背景面板（带背景图）
        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (BACKGROUND_IMAGE != null) {
                    g.drawImage(BACKGROUND_IMAGE, 0, 0, getWidth(), getHeight(), this); // 绘制背景图
                }
            }
        };
        backgroundPanel.setLayout(new BorderLayout());

        // 标题与欢迎信息面板
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false); // 透明背景
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0)); // 上下内边距20

        // 主标题
        JLabel titleLabel = new JLabel("Klotski Puzzle", SwingConstants.CENTER);
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(PRIMARY_COLOR);
        headerPanel.add(titleLabel, BorderLayout.NORTH);

        // 欢迎信息（用户名/游客）
        JLabel welcomeLabel = new JLabel((currentUser.isEmpty() ? "Guest" : currentUser) + "!", SwingConstants.CENTER);
        welcomeLabel.setFont(SUBTITLE_FONT);
        welcomeLabel.setForeground(PRIMARY_COLOR);
        headerPanel.add(welcomeLabel, BorderLayout.CENTER);

        // 按钮面板（垂直布局）
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false); // 透明背景
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50)); // 四周内边距

        // 创建按钮（使用美化方法）
        JButton startGameBtn = createMenuButton("Start New Game");
        startGameBtn.addActionListener(e -> openGameSettings());

        JButton loadGameBtn = createMenuButton("Load Game");
        loadGameBtn.addActionListener(e -> loadGame());

        JButton rulesBtn = createMenuButton("Game Rules");
        rulesBtn.addActionListener(e -> showGameRules());

        JButton logoutBtn = createMenuButton("Logout");
        logoutBtn.addActionListener(e -> logout());

        // 按钮间距设置
        buttonPanel.add(Box.createVerticalGlue()); // 顶部填充
        buttonPanel.add(startGameBtn);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 25))); // 按钮间距25
        buttonPanel.add(loadGameBtn);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 25)));
        buttonPanel.add(rulesBtn);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 25)));
        buttonPanel.add(logoutBtn);
        buttonPanel.add(Box.createVerticalGlue()); // 底部填充

        // 组合界面元素
        backgroundPanel.add(headerPanel, BorderLayout.NORTH);
        backgroundPanel.add(buttonPanel, BorderLayout.CENTER);
        add(backgroundPanel);

        setVisible(true); // 显示窗口
    }

    /**
     * 创建美化的菜单按钮（带悬停效果）
     * @param text 按钮文字
     * @return 美化后的按钮
     */
    private JButton createMenuButton(String text) {
        JButton button = new JButton(text);
        button.setFont(BUTTON_FONT);
        button.setForeground(TEXT_COLOR);
        // 半透明深绿背景（RGBA：27,84,84,200）
        button.setBackground(new Color(27, 84, 84, 200));
        // 边框：浅色细边 + 内边距
        button.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(10, 20, 10, 20)
        ));
        button.setPreferredSize(new Dimension(220, 45)); // 固定尺寸
        button.setMaximumSize(new Dimension(220, 45));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR)); // 手型光标
        button.setFocusPainted(false); // 取消焦点框

        // 悬停效果：背景变浅，文字变色
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(240, 250, 240, 220)); // 浅绿背景
                button.setForeground(PRIMARY_COLOR); // 文字变主色
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(27, 84, 84, 200)); // 恢复原背景
                button.setForeground(TEXT_COLOR); // 恢复原文字色
            }
        });

        button.setAlignmentX(Component.CENTER_ALIGNMENT); // 水平居中
        return button;
    }

    /**
     * 打开游戏设置界面（功能与原代码一致）
     */
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

    /**
     * 加载存档（功能与原代码一致）
     */
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

    /**
     * 显示游戏规则（保留原代码的详细HTML内容）
     */
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

    /**
     * 退出登录（功能与原代码一致）
     */
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

    /**
     * 窗口关闭时停止背景音乐
     */
    @Override
    public void dispose() {
        if (backgroundMusic != null && backgroundMusic.isRunning()) {
            backgroundMusic.stop(); // 停止音乐
        }
        super.dispose();
    }
}