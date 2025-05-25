package view;

import model.Difficulty;
import model.LeaderboardEntry;
import model.LeaderboardManager;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

public class LeaderboardFrame extends JFrame {
    private LeaderboardManager leaderboardManager;
    private JTabbedPane tabbedPane;
    private JTable[] tables;
    private DefaultTableModel[] tableModels;
    
    // Custom colors and fonts
    private static final Color PRIMARY_COLOR = new Color(27, 84, 84);
    private static final Color SECONDARY_COLOR = new Color(240, 250, 240);
    private static final Color HEADER_COLOR = new Color(57, 76, 83);
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 24);
    private static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font TABLE_FONT = new Font("Segoe UI", Font.PLAIN, 13);

    public LeaderboardFrame() {
        leaderboardManager = new LeaderboardManager();
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Leaderboard");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(600, 500);
        setLocationRelativeTo(null);
        setResizable(false);

        // Create main panel with custom background
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
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Add title
        JLabel titleLabel = new JLabel("Leaderboard", SwingConstants.CENTER);
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(PRIMARY_COLOR);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Create tabbed pane with custom styling
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(HEADER_FONT);
        tabbedPane.setBackground(SECONDARY_COLOR);
        tabbedPane.setForeground(PRIMARY_COLOR);
        
        // Custom tab renderer
        tabbedPane.putClientProperty("JTabbedPane.tabAreaBackground", SECONDARY_COLOR);
        tabbedPane.putClientProperty("JTabbedPane.selected", PRIMARY_COLOR);
        
        tables = new JTable[Difficulty.values().length];
        tableModels = new DefaultTableModel[Difficulty.values().length];

        // Create a tab for each difficulty level
        for (int i = 0; i < Difficulty.values().length; i++) {
            Difficulty difficulty = Difficulty.values()[i];
            JPanel panel = new JPanel(new BorderLayout());
            panel.setBackground(SECONDARY_COLOR);
            
            // Create table model
            String[] columnNames = {"Rank", "Username", "Moves", "Completion Time"};
            tableModels[i] = new DefaultTableModel(columnNames, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            
            // Create table with custom styling
            tables[i] = new JTable(tableModels[i]);
            tables[i].setFont(TABLE_FONT);
            tables[i].setRowHeight(30);
            tables[i].setShowGrid(false);
            tables[i].setIntercellSpacing(new Dimension(0, 0));
            tables[i].setBackground(SECONDARY_COLOR);
            tables[i].setForeground(HEADER_COLOR);
            
            // Custom header renderer
            JTableHeader header = tables[i].getTableHeader();
            header.setFont(HEADER_FONT);
            header.setBackground(PRIMARY_COLOR);
            header.setForeground(Color.WHITE);
            header.setPreferredSize(new Dimension(header.getWidth(), 40));
            
            // Custom cell renderer
            DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value,
                        boolean isSelected, boolean hasFocus, int row, int column) {
                    Component c = super.getTableCellRendererComponent(table, value,
                            isSelected, hasFocus, row, column);
                    
                    // Set background color for even/odd rows
                    if (row % 2 == 0) {
                        c.setBackground(new Color(245, 250, 245));
                    } else {
                        c.setBackground(SECONDARY_COLOR);
                    }
                    
                    // Highlight top 3 positions
                    if (row < 3) {
                        c.setForeground(PRIMARY_COLOR);
                        ((JLabel) c).setFont(TABLE_FONT.deriveFont(Font.BOLD));
                    } else {
                        c.setForeground(HEADER_COLOR);
                        ((JLabel) c).setFont(TABLE_FONT);
                    }
                    
                    // Center align all columns
                    ((JLabel) c).setHorizontalAlignment(SwingConstants.CENTER);
                    
                    return c;
                }
            };
            
            // Apply cell renderer to all columns
            for (int j = 0; j < tables[i].getColumnCount(); j++) {
                tables[i].getColumnModel().getColumn(j).setCellRenderer(cellRenderer);
            }
            
            // Set column widths
            tables[i].getColumnModel().getColumn(0).setPreferredWidth(60);  // Rank
            tables[i].getColumnModel().getColumn(1).setPreferredWidth(150); // Username
            tables[i].getColumnModel().getColumn(2).setPreferredWidth(100); // Moves
            tables[i].getColumnModel().getColumn(3).setPreferredWidth(200); // Time
            
            // Create scroll pane with custom styling
            JScrollPane scrollPane = new JScrollPane(tables[i]);
            scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(10, 0, 0, 0),
                BorderFactory.createLineBorder(PRIMARY_COLOR, 1)
            ));
            scrollPane.getViewport().setBackground(SECONDARY_COLOR);
            
            panel.add(scrollPane, BorderLayout.CENTER);
            
            // Add tab
            tabbedPane.addTab(difficulty.getDisplayName(), panel);
            
            // Load data
            loadLeaderboardData(difficulty);
        }

        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        add(mainPanel);
    }

    private void loadLeaderboardData(Difficulty difficulty) {
        DefaultTableModel model = tableModels[difficulty.ordinal()];
        model.setRowCount(0); // Clear existing data
        
        List<LeaderboardEntry> entries = leaderboardManager.getLeaderboard(difficulty);
        for (int i = 0; i < entries.size(); i++) {
            LeaderboardEntry entry = entries.get(i);
            model.addRow(new Object[]{
                i + 1,
                entry.getUsername(),
                entry.getMoves(),
                new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                    .format(new java.util.Date(entry.getTimestamp()))
            });
        }
    }

    public void refreshLeaderboards() {
        for (Difficulty difficulty : Difficulty.values()) {
            loadLeaderboardData(difficulty);
        }
    }
} 