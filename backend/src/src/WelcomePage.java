import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class WelcomePage extends JFrame {
    private Connection connection;

    // Refined Color Palette for a Professional Look
    private static final Color PRIMARY_COLOR = new Color(33, 47, 61);      // Deep Charcoal
    private static final Color PRIMARY_GRADIENT = new Color(66, 94, 122);  // Slate Blue
    private static final Color SECONDARY_COLOR = new Color(0, 77, 64);     // Deep Teal
    private static final Color SECONDARY_GRADIENT = new Color(0, 121, 107); // Lighter Teal
    private static final Color BACKGROUND_START = new Color(245, 247, 250); // Soft Gray-White
    private static final Color BACKGROUND_END = new Color(230, 238, 245);   // Light Blue-Gray
    private static final Color ACCENT_COLOR = new Color(103, 128, 159);     // Subtle Blue-Gray
    private static final Color HIGHLIGHT_COLOR = new Color(255, 241, 227);  // Warm Cream Accent
    private static final Color TEXT_COLOR = new Color(0, 0, 0);

    // Elegant Font Definitions
    private static final Font TITLE_FONT = new Font("Georgia", Font.BOLD, 36);
    private static final Font SUBTITLE_FONT = new Font("Georgia", Font.ITALIC, 18);
    private static final Font BUTTON_FONT = new Font("Roboto", Font.BOLD, 16);
    private static final Font LABEL_FONT = new Font("Roboto", Font.PLAIN, 14);

    public WelcomePage() {
        initializeDatabase();

        setTitle("ExamaHead Portal");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel contentPane = new JPanel(new BorderLayout(20, 20)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gp = new GradientPaint(0, 0, BACKGROUND_START, 0, getHeight(), BACKGROUND_END);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        contentPane.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        setContentPane(contentPane);

        JPanel homepagePanel = createHomepagePanel();
        contentPane.add(homepagePanel, BorderLayout.CENTER);
        setVisible(true);
    }

    private void initializeDatabase() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/eah22", "root", "MySQL@24");
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database connection failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private JPanel createHomepagePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(20, 20, 20, 20),
                BorderFactory.createLineBorder(ACCENT_COLOR, 1, true)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;

        JLabel logo = new JLabel("ExamaHead", JLabel.CENTER);
        logo.setFont(TITLE_FONT.deriveFont(Font.BOLD, 40));
        logo.setForeground(PRIMARY_COLOR);
        logo.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(logo, gbc);

        JLabel tagline = new JLabel("Empowering Education with Excellence");
        tagline.setFont(SUBTITLE_FONT);
        tagline.setForeground(ACCENT_COLOR);
        tagline.setHorizontalAlignment(JLabel.CENTER);
        gbc.gridy = 1;
        panel.add(tagline, gbc);

        JButton btnSignUp = createStyledButton("Create Account", SECONDARY_COLOR, SECONDARY_GRADIENT);
        btnSignUp.addActionListener(e -> showSignUpPanel());
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        panel.add(btnSignUp, gbc);

        JButton btnSignIn = createStyledButton("Access Account", PRIMARY_COLOR, PRIMARY_GRADIENT);
        btnSignIn.addActionListener(e -> showSignInPanel());
        gbc.gridx = 1;
        panel.add(btnSignIn, gbc);

        return panel;
    }

    private JButton createStyledButton(String text, Color startColor, Color endColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, startColor, 0, getHeight(), endColor);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.setColor(new Color(0, 0, 0, 20));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        button.setFont(BUTTON_FONT);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorder(BorderFactory.createEmptyBorder(12, 24, 12, 24));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent evt) {
                button.setForeground(HIGHLIGHT_COLOR);
            }

            @Override
            public void mouseExited(MouseEvent evt) {
                button.setForeground(Color.WHITE);
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

    private void showSignUpPanel() {
        JFrame signUpFrame = new JFrame("Create Account");
        signUpFrame.setSize(500, 450);
        signUpFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        signUpFrame.setLocationRelativeTo(this);

        JPanel signUpContent = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(0, 0, BACKGROUND_START, 0, getHeight(), BACKGROUND_END);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        signUpContent.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        signUpFrame.setContentPane(signUpContent);

        JPanel signUpPanel = new JPanel(new GridBagLayout());
        signUpPanel.setOpaque(false);
        signUpPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(20, 20, 20, 20),
                BorderFactory.createLineBorder(ACCENT_COLOR, 1, true)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;

        JLabel title = new JLabel("Create Your Account");
        title.setFont(TITLE_FONT.deriveFont(Font.BOLD, 28));
        title.setForeground(PRIMARY_COLOR);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        signUpPanel.add(title, gbc);

        JButton btnExaminee = createStyledButton("As Examinee", SECONDARY_COLOR, SECONDARY_GRADIENT);
        btnExaminee.addActionListener(e -> showExamineeSignUp());
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        signUpPanel.add(btnExaminee, gbc);

        JButton btnExaminer = createStyledButton("As Examiner", PRIMARY_COLOR, PRIMARY_GRADIENT);
        btnExaminer.addActionListener(e -> showExaminerSignUp());
        gbc.gridx = 1;
        signUpPanel.add(btnExaminer, gbc);

        signUpContent.add(signUpPanel, BorderLayout.CENTER);
        signUpFrame.setVisible(true);
    }

    private void showExamineeSignUp() {
        JFrame examineeSignUpFrame = new JFrame("Examinee Registration");
        examineeSignUpFrame.setSize(450, 450);
        examineeSignUpFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        examineeSignUpFrame.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(0, 0, BACKGROUND_START, 0, getHeight(), BACKGROUND_END);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Examinee Registration");
        title.setFont(TITLE_FONT.deriveFont(Font.BOLD, 24));
        title.setForeground(PRIMARY_COLOR);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(title, gbc);

        JLabel lblName = new JLabel("Full Name:");
        lblName.setFont(LABEL_FONT);
        lblName.setForeground(TEXT_COLOR);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        panel.add(lblName, gbc);

        JTextField txtName = new JTextField(20);
        txtName.setFont(LABEL_FONT);
        txtName.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT_COLOR, 1, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        gbc.gridx = 1;
        panel.add(txtName, gbc);

        // Added Registration Number Field
        JLabel lblRegistrationNumber = new JLabel("Registration Number:");
        lblRegistrationNumber.setFont(LABEL_FONT);
        lblRegistrationNumber.setForeground(TEXT_COLOR);
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(lblRegistrationNumber, gbc);

        JTextField txtRegistrationNumber = new JTextField(20);
        txtRegistrationNumber.setFont(LABEL_FONT);
        txtRegistrationNumber.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT_COLOR, 1, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        gbc.gridx = 1;
        panel.add(txtRegistrationNumber, gbc);

        JLabel lblEmail = new JLabel("Email Address:");
        lblEmail.setFont(LABEL_FONT);
        lblEmail.setForeground(TEXT_COLOR);
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(lblEmail, gbc);

        JTextField txtEmail = new JTextField(20);
        txtEmail.setFont(LABEL_FONT);
        txtEmail.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT_COLOR, 1, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        gbc.gridx = 1;
        panel.add(txtEmail, gbc);

        JLabel lblPassword = new JLabel("Password:");
        lblPassword.setFont(LABEL_FONT);
        lblPassword.setForeground(TEXT_COLOR);
        gbc.gridx = 0;
        gbc.gridy = 4;
        panel.add(lblPassword, gbc);

        JPasswordField txtPassword = new JPasswordField(20);
        txtPassword.setFont(LABEL_FONT);
        txtPassword.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT_COLOR, 1, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        gbc.gridx = 1;
        panel.add(txtPassword, gbc);

        JButton btnRegister = createStyledButton("Register Now", SECONDARY_COLOR, SECONDARY_GRADIENT);
        btnRegister.addActionListener(e -> {
            String name = txtName.getText().trim();
            String registrationNumber = txtRegistrationNumber.getText().trim();
            String email = txtEmail.getText().trim();
            String password = new String(txtPassword.getPassword()).trim();

            if (name.isEmpty() || registrationNumber.isEmpty() || email.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            registerExaminee(name, registrationNumber, email, password);
            examineeSignUpFrame.dispose();
        });
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        panel.add(btnRegister, gbc);

        examineeSignUpFrame.add(panel);
        examineeSignUpFrame.setVisible(true);
    }

    private void registerExaminee(String name, String registrationNumber, String email, String password) {
        try {
            connection.setAutoCommit(false);

            // Insert into examinees
            String examineeQuery = "INSERT INTO examinees (name, email, password) VALUES (?, ?, ?)";
            PreparedStatement examineeStmt = connection.prepareStatement(examineeQuery, Statement.RETURN_GENERATED_KEYS);
            examineeStmt.setString(1, name);
            examineeStmt.setString(2, email);
            examineeStmt.setString(3, password);
            examineeStmt.executeUpdate();

            ResultSet examineeKeys = examineeStmt.getGeneratedKeys();
            int examineeId = -1;
            if (examineeKeys.next()) {
                examineeId = examineeKeys.getInt(1);
            } else {
                throw new SQLException("Failed to get examinee ID");
            }

            // Insert into students with name and registration_number
            String studentQuery = "INSERT INTO students (name, registration_number) VALUES (?, ?)";
            PreparedStatement studentStmt = connection.prepareStatement(studentQuery, Statement.RETURN_GENERATED_KEYS);
            studentStmt.setString(1, name);
            studentStmt.setString(2, registrationNumber);
            studentStmt.executeUpdate();

            ResultSet studentKeys = studentStmt.getGeneratedKeys();
            int studentId = -1;
            if (studentKeys.next()) {
                studentId = studentKeys.getInt(1);
            } else {
                throw new SQLException("Failed to get student ID");
            }

            // Insert into examinee_student_mapping
            String mappingQuery = "INSERT INTO examinee_student_mapping (examinee_id, student_id) VALUES (?, ?)";
            PreparedStatement mappingStmt = connection.prepareStatement(mappingQuery);
            mappingStmt.setInt(1, examineeId);
            mappingStmt.setInt(2, studentId);
            mappingStmt.executeUpdate();

            connection.commit();
            JOptionPane.showMessageDialog(this, "Examinee registered successfully!");
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error registering examinee: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void showExaminerSignUp() {
        JFrame examinerSignUpFrame = new JFrame("Examiner Registration");
        examinerSignUpFrame.setSize(450, 400);
        examinerSignUpFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        examinerSignUpFrame.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(0, 0, BACKGROUND_START, 0, getHeight(), BACKGROUND_END);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Examiner Registration");
        title.setFont(TITLE_FONT.deriveFont(Font.BOLD, 24));
        title.setForeground(PRIMARY_COLOR);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(title, gbc);

        JLabel lblName = new JLabel("Full Name:");
        lblName.setFont(LABEL_FONT);
        lblName.setForeground(TEXT_COLOR);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        panel.add(lblName, gbc);

        JTextField txtName = new JTextField(20);
        txtName.setFont(LABEL_FONT);
        txtName.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT_COLOR, 1, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        gbc.gridx = 1;
        panel.add(txtName, gbc);

        JLabel lblEmail = new JLabel("Email Address:");
        lblEmail.setFont(LABEL_FONT);
        lblEmail.setForeground(TEXT_COLOR);
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(lblEmail, gbc);

        JTextField txtEmail = new JTextField(20);
        txtEmail.setFont(LABEL_FONT);
        txtEmail.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT_COLOR, 1, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        gbc.gridx = 1;
        panel.add(txtEmail, gbc);

        JLabel lblPassword = new JLabel("Password:");
        lblPassword.setFont(LABEL_FONT);
        lblPassword.setForeground(TEXT_COLOR);
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(lblPassword, gbc);

        JPasswordField txtPassword = new JPasswordField(20);
        txtPassword.setFont(LABEL_FONT);
        txtPassword.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT_COLOR, 1, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        gbc.gridx = 1;
        panel.add(txtPassword, gbc);

        JButton btnRegister = createStyledButton("Register Now", PRIMARY_COLOR, PRIMARY_GRADIENT);
        btnRegister.addActionListener(e -> {
            String name = txtName.getText().trim();
            String email = txtEmail.getText().trim();
            String password = new String(txtPassword.getPassword()).trim();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            registerExaminer(name, email, password);
            examinerSignUpFrame.dispose();
        });
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        panel.add(btnRegister, gbc);

        examinerSignUpFrame.add(panel);
        examinerSignUpFrame.setVisible(true);
    }

    private void registerExaminer(String name, String email, String password) {
        try {
            String query = "INSERT INTO examiners (name, email, password) VALUES (?, ?, ?)";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, name);
            stmt.setString(2, email);
            stmt.setString(3, password);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Examiner registered successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error registering examiner: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Update showSignInPanel to close the frame after successful login
    private void showSignInPanel() {
        JFrame signInFrame = new JFrame("Access Account");
        signInFrame.setSize(450, 350);
        signInFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        signInFrame.setLocationRelativeTo(this);

        JPanel signInPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(0, 0, BACKGROUND_START, 0, getHeight(), BACKGROUND_END);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        signInPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;

        JLabel title = new JLabel("Access Your Account");
        title.setFont(TITLE_FONT.deriveFont(Font.BOLD, 24));
        title.setForeground(PRIMARY_COLOR);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        signInPanel.add(title, gbc);

        JLabel lblEmail = new JLabel("Email Address:");
        lblEmail.setFont(LABEL_FONT);
        lblEmail.setForeground(TEXT_COLOR);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        signInPanel.add(lblEmail, gbc);

        JTextField txtEmail = new JTextField(20);
        txtEmail.setFont(LABEL_FONT);
        txtEmail.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT_COLOR, 1, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        gbc.gridx = 1;
        signInPanel.add(txtEmail, gbc);

        JLabel lblPassword = new JLabel("Password:");
        lblPassword.setFont(LABEL_FONT);
        lblPassword.setForeground(TEXT_COLOR);
        gbc.gridx = 0;
        gbc.gridy = 2;
        signInPanel.add(lblPassword, gbc);

        JPasswordField txtPassword = new JPasswordField(20);
        txtPassword.setFont(LABEL_FONT);
        txtPassword.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT_COLOR, 1, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        gbc.gridx = 1;
        signInPanel.add(txtPassword, gbc);

        JButton btnLogin = createStyledButton("Sign In", PRIMARY_COLOR, PRIMARY_GRADIENT);
        btnLogin.addActionListener(e -> {
            String email = txtEmail.getText().trim();
            String password = new String(txtPassword.getPassword()).trim();
            if (loginExaminer(email, password)) {
                signInFrame.dispose();
                showExamCreationPanel();
            } else if (loginExaminee(email, password)) {
                signInFrame.dispose(); // Close sign-in frame after successful examinee login
            } else {
                JOptionPane.showMessageDialog(this, "Invalid email or password", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        signInPanel.add(btnLogin, gbc);

        signInFrame.add(signInPanel);
        signInFrame.setVisible(true);
    }

    private boolean loginExaminee(String email, String password) {
        try {
            String query = "SELECT e.id AS examinee_id, e.name AS examinee_name, s.id AS student_id " +
                    "FROM examinees e " +
                    "INNER JOIN examinee_student_mapping esm ON e.id = esm.examinee_id " +
                    "INNER JOIN students s ON esm.student_id = s.id " +
                    "WHERE e.email = ? AND e.password = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, email);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int studentId = rs.getInt("student_id");
                String examineeName = rs.getString("examinee_name"); // Name from examinees table
                showExamineeExamSelectionPanel(studentId, examineeName);
                return true;
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error logging in: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    private void showExamineeExamSelectionPanel(int studentId, String examineeName) {
        JFrame examSelectionFrame = new JFrame("Select Exam Type");
        examSelectionFrame.setSize(450, 300);
        examSelectionFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        examSelectionFrame.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(0, 0, BACKGROUND_START, 0, getHeight(), BACKGROUND_END);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;

        JLabel title = new JLabel("Welcome, " + examineeName);
        title.setFont(TITLE_FONT.deriveFont(Font.BOLD, 24));
        title.setForeground(PRIMARY_COLOR);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(title, gbc);

        JLabel subTitle = new JLabel("Choose Exam Type");
        subTitle.setFont(SUBTITLE_FONT);
        subTitle.setForeground(ACCENT_COLOR);
        gbc.gridy = 1;
        panel.add(subTitle, gbc);

        JButton btnMCQ = createStyledButton("Participate in MCQ Exam", SECONDARY_COLOR, SECONDARY_GRADIENT);
        btnMCQ.addActionListener(e -> {
            new StudentExamUI(studentId,examineeName); // Assuming StudentExamUI exists and takes studentId and name
            examSelectionFrame.dispose();
        });
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        panel.add(btnMCQ, gbc);

        JButton btnWritten = createStyledButton("Participate in Written Exam", PRIMARY_COLOR, PRIMARY_GRADIENT);
        btnWritten.addActionListener(e -> {
            new StudentUI2((studentId), examineeName); // Convert studentId to String as per requirement
            examSelectionFrame.dispose();
        });
        gbc.gridx = 1;
        panel.add(btnWritten, gbc);

        examSelectionFrame.add(panel);
        examSelectionFrame.setVisible(true);
    }


    private boolean loginExaminer(String email, String password) {
        try {
            String query = "SELECT * FROM examiners WHERE email = ? AND password = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, email);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error logging in: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void showExamCreationPanel() {
        JFrame examCreationFrame = new JFrame("Exam Creation Suite");
        examCreationFrame.setSize(500, 400);
        examCreationFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        examCreationFrame.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(0, 0, BACKGROUND_START, 0, getHeight(), BACKGROUND_END);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;

        JLabel title = new JLabel("Create Your Exam");
        title.setFont(TITLE_FONT.deriveFont(Font.BOLD, 24));
        title.setForeground(PRIMARY_COLOR);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(title, gbc);

        JButton btnMCQ = createStyledButton("MCQ Exam", SECONDARY_COLOR, SECONDARY_GRADIENT);
        btnMCQ.addActionListener(e -> {
             new ExaminerProfile2(); // Uncomment if ExaminerProfile2 exists
            examCreationFrame.dispose();
        });
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        panel.add(btnMCQ, gbc);

        JButton btnWritten = createStyledButton("Written Exam", PRIMARY_COLOR, PRIMARY_GRADIENT);
        btnWritten.addActionListener(e -> {
             new ExaminerProfile1(); // Uncomment if ExaminerProfile1 exists
            examCreationFrame.dispose();
        });
        gbc.gridx = 1;
        panel.add(btnWritten, gbc);

        examCreationFrame.add(panel);
        examCreationFrame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new WelcomePage();
        }); 
    }
}