package view.login;

import model.MapModel;
import model.UserManager;
import view.game.GameFrame;
import view.menu.SelectionMenuFrame;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

class BackgroundPanel extends JPanel {
    private Image backgroundImage;

    public BackgroundPanel() {
        try {
            BufferedImage img = ImageIO.read(getClass().getResourceAsStream("/resource/loginbackground.jpg"));
            backgroundImage = img.getScaledInstance(600, 450, Image.SCALE_SMOOTH);
        } catch (Exception e) {
            e.printStackTrace();
            setBackground(new Color(240, 240, 240));
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(255, 255, 255, 230));
        g2.fillRoundRect((getWidth() - 400) / 2, (getHeight() - 380) / 2, 400, 380, 25, 25);
    }
}

public class LoginFrame extends JFrame {
    private static final Color PRIMARY_COLOR = new Color(116, 199, 203);
    private static final Color TEXT_COLOR = new Color(56, 133, 139);
    private static final Color BORDER_COLOR = new Color(226, 232, 240);
    private static final Color BG_LIGHT = new Color(248, 250, 252);
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 18);
    private static final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font INPUT_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 14);

    private JTextField username;
    private JPasswordField password;
    private JButton submitBtn;
    private JButton resetBtn;
    private JButton guestBtn;
    private JButton registerBtn;
    private GameFrame gameFrame;
    private final UserManager userManager;

    private boolean validateLogin(String username, String password) {
        boolean isValid = userManager.validateUser(username, password);
        System.out.println("Login validation for " + username + ": " + isValid);
        if (!isValid) {
            System.out.println("Stored users: " + userManager.getUsers());
        }
        return isValid;
    }

    private void showRegistrationDialog() {
        JDialog registerDialog = new JDialog(this, "Create Account", true);
        registerDialog.setSize(420, 380);
        registerDialog.setLocationRelativeTo(this);
        registerDialog.setUndecorated(true);
        registerDialog.getRootPane().setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1),
                new EmptyBorder(15, 15, 15, 15)
        ));
        registerDialog.setBackground(BG_LIGHT);

        JPanel dialogPanel = new JPanel(new GridBagLayout());
        dialogPanel.setOpaque(false);
        GridBagConstraints dgbc = new GridBagConstraints();
        dgbc.insets = new Insets(8, 10, 8, 10);
        dgbc.anchor = GridBagConstraints.WEST;
        dgbc.fill = GridBagConstraints.HORIZONTAL;
        dgbc.weightx = 1.0;

        // 标题标签
        JLabel title = new JLabel("Create New Account");
        title.setFont(TITLE_FONT.deriveFont(16f));
        title.setForeground(TEXT_COLOR);
        title.setBorder(new EmptyBorder(0, 0, 15, 0));
        dgbc.gridx = 0;
        dgbc.gridy = 0;
        dgbc.gridwidth = 2;
        dialogPanel.add(title, dgbc);

        JLabel userLabel = new JLabel("Username:");
        userLabel.setFont(LABEL_FONT);
        userLabel.setForeground(TEXT_COLOR);
        dgbc.gridx = 0;
        dgbc.gridy = 1;
        dgbc.gridwidth = 1;
        dgbc.weightx = 0;
        dialogPanel.add(userLabel, dgbc);

        JTextField newUsername = new JTextField();
        newUsername.setFont(INPUT_FONT);
        newUsername.setForeground(TEXT_COLOR);
        newUsername.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
        newUsername.setPreferredSize(new Dimension(260, 36));
        dgbc.gridx = 1;
        dgbc.gridy = 1;
        dgbc.weightx = 1.0;
        dialogPanel.add(newUsername, dgbc);

        // 密码标签
        JLabel pwdLabel = new JLabel("Password:");
        pwdLabel.setFont(LABEL_FONT);
        pwdLabel.setForeground(TEXT_COLOR);
        dgbc.gridx = 0;
        dgbc.gridy = 2;
        dgbc.weightx = 0;
        dialogPanel.add(pwdLabel, dgbc);

        // 密码输入框
        JPasswordField newPassword = new JPasswordField();
        newPassword.setFont(INPUT_FONT);
        newPassword.setForeground(TEXT_COLOR);
        newPassword.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
        newPassword.setPreferredSize(new Dimension(260, 36)); // 增大宽度
        dgbc.gridx = 1;
        dgbc.gridy = 2;
        dgbc.weightx = 1.0;
        dialogPanel.add(newPassword, dgbc);

        // 确认密码标签
        JLabel confirmLabel = new JLabel("Confirm Password:");
        confirmLabel.setFont(LABEL_FONT);
        confirmLabel.setForeground(TEXT_COLOR);
        dgbc.gridx = 0;
        dgbc.gridy = 3;
        dgbc.weightx = 0;
        dialogPanel.add(confirmLabel, dgbc);

        // 确认密码输入框
        JPasswordField confirmPassword = new JPasswordField();
        confirmPassword.setFont(INPUT_FONT);
        confirmPassword.setForeground(TEXT_COLOR);
        confirmPassword.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
        confirmPassword.setPreferredSize(new Dimension(260, 36));
        dgbc.gridx = 1;
        dgbc.gridy = 3;
        dgbc.weightx = 1.0;
        dialogPanel.add(confirmPassword, dgbc);

        // 按钮面板
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.setBorder(new EmptyBorder(15, 0, 0, 0));

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(LABEL_FONT);
        cancelBtn.setForeground(TEXT_COLOR);
        cancelBtn.setBackground(BG_LIGHT);
        cancelBtn.setBorder(new LineBorder(BORDER_COLOR, 1, true));
        cancelBtn.setPreferredSize(new Dimension(100, 36));
        cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelBtn.addActionListener(e -> registerDialog.dispose());
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(cancelBtn);
        buttonPanel.add(Box.createRigidArea(new Dimension(15, 0)));

        JButton confirmBtn = new JButton("Create");
        confirmBtn.setFont(BUTTON_FONT);
        confirmBtn.setForeground(Color.WHITE);
        confirmBtn.setBackground(PRIMARY_COLOR);
        confirmBtn.setBorderPainted(false);
        confirmBtn.setFocusPainted(false);
        confirmBtn.setPreferredSize(new Dimension(100, 36));
        confirmBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        confirmBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                confirmBtn.setBackground(new Color(30, 100, 160));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                confirmBtn.setBackground(PRIMARY_COLOR);
            }
        });
        confirmBtn.addActionListener(e -> {
            String user = newUsername.getText();
            String pwd = new String(newPassword.getPassword());
            String confirmPwd = new String(confirmPassword.getPassword());

            if (user.isEmpty() || pwd.isEmpty()) {
                JOptionPane.showMessageDialog(registerDialog, "Please fill in all fields", "Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!pwd.equals(confirmPwd)) {
                JOptionPane.showMessageDialog(registerDialog, "Passwords do not match", "Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (userManager.registerUser(user, pwd)) {
                JOptionPane.showMessageDialog(registerDialog, "Registration successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                registerDialog.dispose();
            } else {
                JOptionPane.showMessageDialog(registerDialog, "Username already exists", "Error", JOptionPane.WARNING_MESSAGE);
            }
        });
        buttonPanel.add(confirmBtn);
        buttonPanel.add(Box.createHorizontalGlue());

        registerDialog.add(dialogPanel, BorderLayout.CENTER);
        registerDialog.add(buttonPanel, BorderLayout.SOUTH);
        registerDialog.setVisible(true);
    }

    public LoginFrame(int width, int height) {
        this.userManager = new UserManager();
        initUI(width, height);
        initEventListeners();
    }

    private void initUI(int width, int height) {
        setTitle("Klotski Puzzle - Login");
        setSize(width, height);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);

        BackgroundPanel backgroundPanel = new BackgroundPanel();
        backgroundPanel.setLayout(new BorderLayout());

        JPanel formContainer = new JPanel();
        formContainer.setOpaque(false);
        formContainer.setLayout(new BoxLayout(formContainer, BoxLayout.Y_AXIS));
        formContainer.add(Box.createVerticalGlue());
        formContainer.add(Box.createHorizontalStrut(360));
        formContainer.add(Box.createVerticalGlue());
        backgroundPanel.add(formContainer, BorderLayout.CENTER);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        formPanel.setBorder(new EmptyBorder(30, 30, 30, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("Welcome to Klotski_Puzzle");
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets.bottom = 20;
        formPanel.add(titleLabel, gbc);
        gbc.gridwidth = 1;
        gbc.insets.bottom = 8;

        JLabel userLabel = new JLabel("Username:");
        userLabel.setFont(LABEL_FONT);
        userLabel.setForeground(TEXT_COLOR);
        userLabel.setPreferredSize(new Dimension(80, 20));
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(userLabel, gbc);
        username = new JTextField();
        username.setFont(INPUT_FONT);
        username.setForeground(TEXT_COLOR);
        username.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
        username.setPreferredSize(new Dimension(240, 36));
        gbc.gridx = 1;
        gbc.gridy = 1;
        formPanel.add(username, gbc);

        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(LABEL_FONT);
        passLabel.setForeground(TEXT_COLOR);
        passLabel.setPreferredSize(new Dimension(80, 20));
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(passLabel, gbc);
        password = new JPasswordField();
        password.setFont(INPUT_FONT);
        password.setForeground(TEXT_COLOR);
        password.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
        password.setPreferredSize(new Dimension(240, 36));
        gbc.gridx = 1;
        gbc.gridy = 2;
        formPanel.add(password, gbc);

        submitBtn = new JButton("Login");
        submitBtn.setFont(BUTTON_FONT);
        submitBtn.setForeground(Color.WHITE);
        submitBtn.setBackground(PRIMARY_COLOR);
        submitBtn.setBorderPainted(false);
        submitBtn.setFocusPainted(false);
        submitBtn.setPreferredSize(new Dimension(240, 38));
        submitBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        submitBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                submitBtn.setBackground(new Color(30, 100, 160));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                submitBtn.setBackground(PRIMARY_COLOR);
            }
        });
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.insets.top = 15;
        formPanel.add(submitBtn, gbc);

        JPanel helperPanel = new JPanel();
        helperPanel.setOpaque(false);
        helperPanel.setLayout(new BoxLayout(helperPanel, BoxLayout.X_AXIS));
        helperPanel.setBorder(new EmptyBorder(10, 30, 0, 30));
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        formPanel.add(helperPanel, gbc);

        resetBtn = new JButton("Reset");
        resetBtn.setFont(LABEL_FONT);
        resetBtn.setForeground(TEXT_COLOR);
        resetBtn.setBackground(BG_LIGHT);
        resetBtn.setBorder(new LineBorder(BORDER_COLOR, 1, true));
        resetBtn.setPreferredSize(new Dimension(100, 32));
        resetBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        helperPanel.add(Box.createHorizontalGlue());
        helperPanel.add(resetBtn);
        helperPanel.add(Box.createRigidArea(new Dimension(15, 0)));

        guestBtn = new JButton("Play As Guest");
        guestBtn.setFont(LABEL_FONT);
        guestBtn.setForeground(TEXT_COLOR);
        guestBtn.setBackground(BG_LIGHT);
        guestBtn.setBorder(new LineBorder(BORDER_COLOR, 1, true));
        guestBtn.setPreferredSize(new Dimension(100, 32));
        guestBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        helperPanel.add(guestBtn);
        helperPanel.add(Box.createHorizontalGlue());

        registerBtn = new JButton("Register");
        registerBtn.setFont(LABEL_FONT.deriveFont(Font.PLAIN));
        registerBtn.setForeground(PRIMARY_COLOR);
        registerBtn.setBorderPainted(false);
        registerBtn.setContentAreaFilled(false);
        registerBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                registerBtn.setForeground(new Color(30, 100, 160));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                registerBtn.setForeground(PRIMARY_COLOR);
            }
        });
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.insets.top = 12;
        formPanel.add(registerBtn, gbc);

        formContainer.add(formPanel);
        add(backgroundPanel);
    }

    private void initEventListeners() {
        submitBtn.addActionListener(e -> {
            String user = username.getText();
            String pwd = new String(password.getPassword());
            if (validateLogin(user, pwd)) {
                navigateToGame(user, false);
            } else {
                JOptionPane.showMessageDialog(this, "Incorrect username or password", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        resetBtn.addActionListener(e -> {
            username.setText("");
            password.setText("");
        });

        guestBtn.addActionListener(e -> {
            navigateToGame("Guest", true);
        });

        registerBtn.addActionListener(e -> {
            showRegistrationDialog();
        });
    }

    private void navigateToGame(String username, boolean isGuest) {
        if (gameFrame == null) {
            gameFrame = new GameFrame(800, 600, new MapModel());
        }
        gameFrame.getController().setCurrentUser(username);
        gameFrame.setGuestMode(isGuest);
        SelectionMenuFrame menuFrame = new SelectionMenuFrame(600, 600, username);
        menuFrame.setGameFrame(gameFrame);
        gameFrame.setParentFrame(menuFrame);
        menuFrame.setVisible(true);
        this.setVisible(false);
    }

    public void setGameFrame(GameFrame gameFrame) {
        this.gameFrame = gameFrame;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame(600, 450));
    }
}