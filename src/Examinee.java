import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Examinee extends JFrame {
    private static final Color PRIMARY_COLOR = new Color(33, 150, 243);      // Bright Blue
    private static final Color PRIMARY_GRADIENT = new Color(100, 181, 246);  // Lighter Blue
    private static final Color PRIMARY_HOVER = new Color(129, 212, 250);     // Hover Light Blue
    private static final Color SECONDARY_COLOR = new Color(0, 150, 136);     // Vibrant Teal
    private static final Color SECONDARY_GRADIENT = new Color(38, 166, 154); // Lighter Teal
    private static final Color SECONDARY_HOVER = new Color(77, 208, 225);    // Hover Light Teal
    private static final Color BACKGROUND_START = new Color(245, 247, 255);  // Soft Blue-White
    private static final Color BACKGROUND_END = new Color(255, 243, 245);    // Soft Pink-White
    private static final Color TEXT_COLOR = new Color(33, 33, 33);           // Darker Gray
    private static final Color ACCENT_COLOR = new Color(120, 144, 156);      // Slate Gray
    private static int nextStudentId = 1; // Starting ID

    public Examinee() {
        setTitle("Examinee Dashboard");
        setSize(600, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel contentPane = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(0, 0, BACKGROUND_START, getWidth(), getHeight(), BACKGROUND_END);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        contentPane.setLayout(new BorderLayout());
        setContentPane(contentPane);

        JPanel dashboardPanel = createDashboardPanel();
        contentPane.add(dashboardPanel, BorderLayout.CENTER);
        setVisible(true);
    }

    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(25, 25, 25, 25);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel welcomeLabel = new JLabel("Welcome, Examinee!");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        welcomeLabel.setForeground(PRIMARY_COLOR);
        welcomeLabel.setHorizontalAlignment(JLabel.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(welcomeLabel, gbc);

        JLabel instructionLabel = new JLabel("Select Your Exam Type:");
        instructionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        instructionLabel.setForeground(ACCENT_COLOR);
        instructionLabel.setHorizontalAlignment(JLabel.CENTER);
        gbc.gridy = 1;
        panel.add(instructionLabel, gbc);

        JButton btnMCQ = createStyledButton("MCQ Exam", PRIMARY_COLOR, PRIMARY_GRADIENT, PRIMARY_HOVER);
        btnMCQ.addActionListener(e -> {
            new StudentExamUI(1,"");
           dispose();
        });
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        panel.add(btnMCQ, gbc);

        JButton btnWritten = createStyledButton("Written Exam", SECONDARY_COLOR, SECONDARY_GRADIENT, SECONDARY_HOVER);
        btnWritten.addActionListener(e -> {
            new StudentUI2(1,"");
            dispose();
        });
        gbc.gridx = 1;
        panel.add(btnWritten, gbc);

        return panel;
    }

    private JButton createStyledButton(String text, Color startColor, Color endColor, Color hoverColor) {
        // Anonymous class with instance field
        JButton button = new JButton(text) {
            private boolean isHovered = false; // Moved to instance field

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = isHovered ?
                        new GradientPaint(0, 0, hoverColor, 0, getHeight(), endColor.brighter()) :
                        new GradientPaint(0, 0, startColor, 0, getHeight(), endColor);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
                g2.setColor(new Color(0, 0, 0, 50)); // Shadow
                g2.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 25, 25);
                super.paintComponent(g2);
                //g2.dispose();
            }
        };
        button.setFont(new Font("Segoe UI", Font.BOLD, 20));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorder(BorderFactory.createEmptyBorder(15, 30, 15, 30));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent evt) {
                // Access the instance field via the button instance
                ((JButton) evt.getSource()).getClass().getDeclaredFields()[0].setAccessible(true);
                try {
                    ((JButton) evt.getSource()).getClass().getDeclaredFields()[0].setBoolean(button, true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                button.setForeground(new Color(245, 245, 245));
                button.setBorder(BorderFactory.createLineBorder(ACCENT_COLOR.darker(), 2, true));
                button.repaint();
            }

            @Override
            public void mouseExited(MouseEvent evt) {
                // Access the instance field via the button instance
                ((JButton) evt.getSource()).getClass().getDeclaredFields()[0].setAccessible(true);
                try {
                    ((JButton) evt.getSource()).getClass().getDeclaredFields()[0].setBoolean(button, false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                button.setForeground(Color.WHITE);
                button.setBorder(BorderFactory.createEmptyBorder(15, 30, 15, 30));
                button.repaint();
            }

            @Override
            public void mousePressed(MouseEvent evt) {
                button.setForeground(new Color(200, 200, 200));
            }

            @Override
            public void mouseReleased(MouseEvent evt) {
                button.setForeground(Color.WHITE);
            }
        });

        return button;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new Examinee();
        });
    }
}