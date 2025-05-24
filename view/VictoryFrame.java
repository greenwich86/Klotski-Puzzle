package view;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import view.game.GameFrame;
import model.MapModel;
import view.menu.SelectionMenuFrame;

public class VictoryFrame extends JFrame {
    private static final Color PRIMARY_COLOR = new Color(27, 84, 84);
    private static final Color SECONDARY_COLOR = new Color(240, 250, 240);
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 32);
    private static final Font STATS_FONT = new Font("Segoe UI", Font.PLAIN, 18);
    private static final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 16);

    private GameFrame gameFrame;
    private SelectionMenuFrame parentFrame;

    public VictoryFrame(int moves, int remainingTime, boolean isTimeAttack) {
        setTitle("Victory!");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(500, 400);
        setLocationRelativeTo(null);
        setResizable(false);

        // Find the GameFrame and SelectionMenuFrame
        for (Frame frame : Frame.getFrames()) {
            if (frame instanceof GameFrame) {
                gameFrame = (GameFrame) frame;
            } else if (frame instanceof SelectionMenuFrame) {
                parentFrame = (SelectionMenuFrame) frame;
            }
        }

        // Create main panel with gradient background
        JPanel mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                int w = getWidth();
                int h = getHeight();
                Color color1 = new Color(240, 250, 240);
                Color color2 = new Color(255, 255, 255);
                GradientPaint gp = new GradientPaint(0, 0, color1, 0, h, color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, w, h);
            }
        };
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        // Add victory title
        JLabel titleLabel = new JLabel("Victory!", SwingConstants.CENTER);
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(PRIMARY_COLOR);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Create stats panel
        JPanel statsPanel = new JPanel(new GridLayout(0, 1, 0, 20));
        statsPanel.setOpaque(false);

        // Add moves stat
        JLabel movesLabel = new JLabel(String.format("Moves: %d", moves), SwingConstants.CENTER);
        movesLabel.setFont(STATS_FONT);
        movesLabel.setForeground(PRIMARY_COLOR);
        statsPanel.add(movesLabel);

        // Add time stat if in time attack mode
        if (isTimeAttack) {
            int minutes = remainingTime / 60;
            int seconds = remainingTime % 60;
            JLabel timeLabel = new JLabel(
                String.format("Time Remaining: %02d:%02d", minutes, seconds),
                SwingConstants.CENTER
            );
            timeLabel.setFont(STATS_FONT);
            timeLabel.setForeground(PRIMARY_COLOR);
            statsPanel.add(timeLabel);
        }

        mainPanel.add(statsPanel, BorderLayout.CENTER);

        // Create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setOpaque(false);

        // Add "Play Again" button
        JButton playAgainBtn = createButton("Play Again");
        playAgainBtn.addActionListener(e -> {
            if (gameFrame != null) {
                gameFrame.restartGame();
            }
            dispose();
        });

        // Add "Main Menu" button
        JButton mainMenuBtn = createButton("Main Menu");
        mainMenuBtn.addActionListener(e -> {
            if (gameFrame != null) {
                gameFrame.dispose();
            }
            if (parentFrame != null) {
                parentFrame.setVisible(true);
            }
            dispose();
        });

        buttonPanel.add(playAgainBtn);
        buttonPanel.add(mainMenuBtn);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setFont(BUTTON_FONT);
        button.setForeground(Color.WHITE);
        button.setBackground(PRIMARY_COLOR);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(150, 40));

        // Add hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(PRIMARY_COLOR.darker());
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(PRIMARY_COLOR);
            }
        });

        return button;
    }
} 