import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.net.URI;
import java.net.URLEncoder;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.swing.border.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.geom.RoundRectangle2D;

public class ExaminerProfile1 extends JFrame {
    private JTextField txtQuestion, txtAnswers, txtMarks, txtExamCode, txtPhotoPath;
    private JTextArea txtQuestionDatabase, txtExplanation;
    private JComboBox<String> cboTimer;
    private JButton btnAddQuestion, btnViewQuestions, btnSetTimer, btnCreateExam, btnUploadPhoto,btnInvite;
    private JLabel lblRemainingQuestions;
    private JSpinner spinnerQuestionCount;
    private JPanel mainPanel, questionEditorPanel, timerPanel, examCodePanel, databasePanel;
    private JTextField txtExplanationPhotoPath;
    private JButton btnUploadExplanationPhoto;

    private Connection connection;
    private List<Question> questionList;
    private int totalMarks;
    private int remainingQuestions;

    // Enhanced Color Palette for better visibility
    private static final Color PRIMARY_COLOR = new Color(0, 102, 204);      // Rich Blue
    private static final Color PRIMARY_GRADIENT = new Color(51, 153, 255);  // Lighter Blue
    private static final Color SECONDARY_COLOR = new Color(51, 153, 0);     // Green
    private static final Color SECONDARY_GRADIENT = new Color(102, 204, 0); // Light Green
    private static final Color BACKGROUND_START = new Color(240, 240, 245); // Very Light Blue-Gray
    private static final Color BACKGROUND_END = new Color(220, 220, 235);   // Slightly Darker Blue-Gray
    private static final Color PANEL_BACKGROUND = new Color(255, 255, 255); // White Panels
    private static final Color TEXT_COLOR = new Color(50, 50, 50);          // Dark Text for readability
    private static final Color BUTTON_TEXT = new Color(255, 255, 255);      // White Text
    private static final Color ACCENT_COLOR = new Color(204, 0, 0);         // Red Accent
    private static final Color FIELD_BACKGROUND = new Color(248, 248, 248); // Light field background

    // Improved Fonts for better readability
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 36);
    private static final Font SUBTITLE_FONT = new Font("Segoe UI", Font.ITALIC, 22);
    private static final Font HEADING_FONT = new Font("Segoe UI", Font.BOLD, 24);
    private static final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font LABEL_FONT = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font FIELD_FONT = new Font("Segoe UI", Font.PLAIN, 16);
    private static final Font TEXT_AREA_FONT = new Font("Segoe UI", Font.PLAIN, 16);

    public ExaminerProfile1() {
        setTitle("Written Exam Setter");
        setSize(1000, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1000, 800));

        // Initialize Database Connection
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/eah22", "root", "MySQL@24");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database connection failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            System.exit(1);
        }

        // Initialize Components
        questionList = new ArrayList<>();
        totalMarks = 0;
        remainingQuestions = 0;

        // Main Panel with Professional Gradient
        mainPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gradient = new GradientPaint(0, 0, BACKGROUND_START, getWidth(), getHeight(), BACKGROUND_END);
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15); // Increased spacing between components
        gbc.fill = GridBagConstraints.BOTH;

        // Enhanced Header Section
        JPanel headerPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gradient = new GradientPaint(
                        0, 0, new Color(0, 153, 255),
                        getWidth(), getHeight(), new Color(255, 102, 204)
                );
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            }
        };
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        headerPanel.setPreferredSize(new Dimension(950, 80)); // Fixed height to avoid overlap

        JLabel lblHeadline = new JLabel("Written Exam Creator", JLabel.CENTER);
        lblHeadline.setFont(TITLE_FONT.deriveFont(32f)); // Slightly smaller font to fit better
        lblHeadline.setForeground(Color.WHITE);
        lblHeadline.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        // Add hover effect
        lblHeadline.setOpaque(false);
        lblHeadline.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                lblHeadline.setForeground(new Color(255, 255, 153));
                lblHeadline.setFont(TITLE_FONT.deriveFont(Font.BOLD | Font.ITALIC, 34f));
                headerPanel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(255, 255, 255, 100), 2, true),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)
                ));
                headerPanel.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                lblHeadline.setForeground(Color.WHITE);
                lblHeadline.setFont(TITLE_FONT.deriveFont(32f));
                headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                headerPanel.repaint();
            }
        });

        headerPanel.add(lblHeadline, BorderLayout.CENTER);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 0.1; // Reduced weight to prevent stretching
        mainPanel.add(headerPanel, gbc);

        // Question Editor Panel
        questionEditorPanel = createQuestionEditorPanel();
        gbc.gridy = 1;
        gbc.weighty = 0.5; // Adjusted weight for balance
        mainPanel.add(questionEditorPanel, gbc);

        // Timer Panel
        timerPanel = createTimerPanel();
        gbc.gridy = 2;
        gbc.weighty = 0.1;
        mainPanel.add(timerPanel, gbc);

        // Exam Code Panel
        examCodePanel = createExamCodePanel();
        gbc.gridy = 3;
        gbc.weighty = 0.1;
        mainPanel.add(examCodePanel, gbc);

        // Create Exam Button Panel
        JPanel createExamPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        createExamPanel.setOpaque(false);
        btnCreateExam = createStyledButton("Create Exam", ACCENT_COLOR, new Color(255, 51, 51), true);
        btnCreateExam.setFont(new Font("Segoe UI", Font.BOLD, 18)); // Slightly smaller font
        btnCreateExam.setPreferredSize(new Dimension(200, 40)); // Adjusted size
        btnCreateExam.addActionListener(e -> createExam());
        btnCreateExam.setEnabled(false);
        createExamPanel.add(btnCreateExam);
        gbc.gridy = 4;
        gbc.weighty = 0.1;
        mainPanel.add(createExamPanel, gbc);

        // Question Database Panel
        databasePanel = createDatabasePanel();
        gbc.gridy = 5;
        gbc.weighty = 0.4; // Slightly less weight to balance with question editor
        mainPanel.add(databasePanel, gbc);

        add(mainPanel);
        updateRemainingQuestions();
        setVisible(true);
    }


    private JPanel createQuestionEditorPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(PANEL_BACKGROUND);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // Consistent spacing
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // FIRST ROW: Make Total Questions and Remaining Questions appear side-by-side
        JPanel questionCountPanel = new JPanel(new GridBagLayout());
        questionCountPanel.setOpaque(false);
        GridBagConstraints innerGbc = new GridBagConstraints();
        innerGbc.insets = new Insets(5, 5, 5, 15); // Add more space between the two components

        // Total Questions (smaller box)
        JLabel lblTotalQuestions = new JLabel("Total Questions:");
        lblTotalQuestions.setFont(LABEL_FONT);
        lblTotalQuestions.setForeground(TEXT_COLOR);
        spinnerQuestionCount = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        spinnerQuestionCount.setFont(FIELD_FONT);
        spinnerQuestionCount.setPreferredSize(new Dimension(60, 30)); // Smaller fixed size
        JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) spinnerQuestionCount.getEditor();
        editor.getTextField().setFont(FIELD_FONT);
        spinnerQuestionCount.addChangeListener(e -> updateRemainingQuestions());

        innerGbc.gridx = 0;
        innerGbc.gridy = 0;
        innerGbc.weightx = 0.4;
        questionCountPanel.add(lblTotalQuestions, innerGbc);

        innerGbc.gridx = 1;
        innerGbc.gridy = 0;
        innerGbc.weightx = 0.1;
        questionCountPanel.add(spinnerQuestionCount, innerGbc);

        // Remaining Questions (smaller box)
        JLabel lblRemaining = new JLabel("Remaining:");
        lblRemaining.setFont(LABEL_FONT);
        lblRemaining.setForeground(TEXT_COLOR);
        lblRemainingQuestions = new JLabel("0");
        lblRemainingQuestions.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblRemainingQuestions.setForeground(PRIMARY_COLOR);
        lblRemainingQuestions.setPreferredSize(new Dimension(40, 30)); // Smaller fixed size
        lblRemainingQuestions.setHorizontalAlignment(JLabel.CENTER);
        lblRemainingQuestions.setBorder(createFieldBorder());

        innerGbc.gridx = 2;
        innerGbc.gridy = 0;
        innerGbc.weightx = 0.3;
        questionCountPanel.add(lblRemaining, innerGbc);

        innerGbc.gridx = 3;
        innerGbc.gridy = 0;
        innerGbc.weightx = 0.1;
        questionCountPanel.add(lblRemainingQuestions, innerGbc);

        // Add the panel with both question count components
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        panel.add(questionCountPanel, gbc);

        // SECOND ROW: Question
        JLabel lblQuestion = new JLabel("Question:");
        lblQuestion.setFont(LABEL_FONT);
        lblQuestion.setForeground(TEXT_COLOR);
        txtQuestion = createStyledTextField(30);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0.25;
        panel.add(lblQuestion, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.75;
        panel.add(txtQuestion, gbc);

        // THIRD ROW: Possible Answers
        JLabel lblAnswers = new JLabel("Possible Answers:");
        lblAnswers.setFont(LABEL_FONT);
        lblAnswers.setForeground(TEXT_COLOR);
        txtAnswers = createStyledTextField(30);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.25;
        panel.add(lblAnswers, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.75;
        panel.add(txtAnswers, gbc);

        // FOURTH ROW: Marks
        JLabel lblMarks = new JLabel("Marks:");
        lblMarks.setFont(LABEL_FONT);
        lblMarks.setForeground(TEXT_COLOR);
        txtMarks = createStyledTextField(5); // Still a smaller field for marks
        txtMarks.setPreferredSize(new Dimension(60, 35)); // Fixed smaller size

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.25;
        panel.add(lblMarks, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.75;
        panel.add(txtMarks, gbc);

        // FIFTH ROW: Explanation (larger box)
        JLabel lblExplanation = new JLabel("Explanation:");
        lblExplanation.setFont(LABEL_FONT);
        lblExplanation.setForeground(TEXT_COLOR);
        txtExplanation = new JTextArea(6, 30); // Increased rows for larger size
        txtExplanation.setFont(TEXT_AREA_FONT);
        txtExplanation.setLineWrap(true);
        txtExplanation.setWrapStyleWord(true);
        txtExplanation.setBorder(createFieldBorder());
        txtExplanation.setBackground(FIELD_BACKGROUND);
        JScrollPane scrollExplanation = new JScrollPane(txtExplanation);
        scrollExplanation.setBorder(BorderFactory.createEmptyBorder());

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0.25;
        gbc.fill = GridBagConstraints.VERTICAL; // Allow vertical fill
        panel.add(lblExplanation, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.75;
        gbc.fill = GridBagConstraints.BOTH; // Allow both horizontal and vertical fill
        gbc.weighty = 0.5; // Give vertical weight to this component
        panel.add(scrollExplanation, gbc);
        gbc.weighty = 0; // Reset weighty
        gbc.fill = GridBagConstraints.HORIZONTAL; // Reset fill mode

        // SIXTH ROW: Photo
        JLabel lblPhoto = new JLabel("Visualization:");
        lblPhoto.setFont(LABEL_FONT);
        lblPhoto.setForeground(TEXT_COLOR);
        JPanel photoPanel = new JPanel(new BorderLayout(5, 0));
        photoPanel.setOpaque(false);
        txtPhotoPath = createStyledTextField(20);
        txtPhotoPath.setEditable(false);
        btnUploadPhoto = createStyledButton("Upload", SECONDARY_COLOR, SECONDARY_GRADIENT, false);
        btnUploadPhoto.setPreferredSize(new Dimension(100, 30));
        btnUploadPhoto.addActionListener(e -> uploadPhoto());
        photoPanel.add(txtPhotoPath, BorderLayout.CENTER);
        photoPanel.add(btnUploadPhoto, BorderLayout.EAST);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.weightx = 0.25;
        panel.add(lblPhoto, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.75;
        panel.add(photoPanel, gbc);

        // SEVENTH ROW: Explanation Photo
        JLabel lblExplanationPhoto = new JLabel("Explanation Photo:");
        lblExplanationPhoto.setFont(LABEL_FONT);
        lblExplanationPhoto.setForeground(TEXT_COLOR);
        JPanel explanationPhotoPanel = new JPanel(new BorderLayout(5, 0));
        explanationPhotoPanel.setOpaque(false);
        txtExplanationPhotoPath = createStyledTextField(20);
        txtExplanationPhotoPath.setEditable(false);
        btnUploadExplanationPhoto = createStyledButton("Upload", SECONDARY_COLOR, SECONDARY_GRADIENT, false);
        btnUploadExplanationPhoto.setPreferredSize(new Dimension(100, 30));
        btnUploadExplanationPhoto.addActionListener(e -> uploadExplanationPhoto());
        explanationPhotoPanel.add(txtExplanationPhotoPath, BorderLayout.CENTER);
        explanationPhotoPanel.add(btnUploadExplanationPhoto, BorderLayout.EAST);

        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.weightx = 0.25;
        panel.add(lblExplanationPhoto, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.75;
        panel.add(explanationPhotoPanel, gbc);

        // EIGHTH ROW: Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setOpaque(false);
        btnAddQuestion = createStyledButton("Add Question", PRIMARY_COLOR, PRIMARY_GRADIENT, false);
        btnAddQuestion.setPreferredSize(new Dimension(150, 40));
        btnAddQuestion.addActionListener(e -> addQuestion());
        btnViewQuestions = createStyledButton("View Questions", SECONDARY_COLOR, SECONDARY_GRADIENT, false);
        btnViewQuestions.setPreferredSize(new Dimension(150, 40));
        btnViewQuestions.addActionListener(e -> viewQuestions());
        buttonPanel.add(btnAddQuestion);
        buttonPanel.add(btnViewQuestions);

        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(15, 10, 10, 10);
        panel.add(buttonPanel, gbc);

        return panel;
    }

    private void uploadExplanationPhoto() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Image files", "jpg", "png", "jpeg"));
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            txtExplanationPhotoPath.setText(selectedFile.getAbsolutePath());
        }
    }
    private JPanel createTimerPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        panel.setBackground(PANEL_BACKGROUND);
        panel.setBorder(createStyledBorder("Timer Settings"));
        panel.setVisible(false);

        JLabel lblTimer = new JLabel("Set Timer (minutes):");
        lblTimer.setFont(LABEL_FONT);
        lblTimer.setForeground(TEXT_COLOR);
        cboTimer = new JComboBox<>(new String[]{"1", "10", "20", "30", "40", "50", "60", "90", "120"});
        cboTimer.setFont(FIELD_FONT);
        cboTimer.setPreferredSize(new Dimension(120, 45));

        btnSetTimer = createStyledButton("Set Timer", SECONDARY_COLOR, SECONDARY_GRADIENT, false);
        btnSetTimer.setPreferredSize(new Dimension(160, 45));
        btnSetTimer.addActionListener(e -> setTimer());

        btnInvite = createStyledButton("Invite via WhatsApp", SECONDARY_COLOR, SECONDARY_GRADIENT, false);
        btnInvite.setPreferredSize(new Dimension(200, 45));
        btnInvite.addActionListener(e -> inviteViaWhatsApp());
        btnInvite.setEnabled(false); // Initially disabled until exam code is generated

        panel.add(lblTimer);
        panel.add(cboTimer);
        panel.add(btnSetTimer);
        panel.add(btnInvite);

        // Improved visibility with shadow effect
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY_COLOR, 2),
                BorderFactory.createTitledBorder(
                        BorderFactory.createEmptyBorder(10, 10, 10, 10),
                        "Timer Settings",
                        TitledBorder.CENTER,
                        TitledBorder.TOP,
                        HEADING_FONT,
                        PRIMARY_COLOR
                )
        ));

        return panel;
    }

    private JPanel createExamCodePanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        panel.setBackground(PANEL_BACKGROUND);
        panel.setBorder(createStyledBorder("Exam Code"));
        panel.setVisible(false);

        JLabel lblExamCode = new JLabel("Exam Code:");
        lblExamCode.setFont(LABEL_FONT);
        lblExamCode.setForeground(TEXT_COLOR);
        txtExamCode = new JTextField(15);
        txtExamCode.setEditable(false);
        txtExamCode.setFont(new Font("Segoe UI", Font.BOLD, 24));
        txtExamCode.setHorizontalAlignment(JTextField.CENTER);
        txtExamCode.setBackground(FIELD_BACKGROUND);
        txtExamCode.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY_COLOR, 2, true),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        txtExamCode.setForeground(ACCENT_COLOR);

        panel.add(lblExamCode);
        panel.add(txtExamCode);

        // Improved visibility with shadow effect
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY_COLOR, 2),
                BorderFactory.createTitledBorder(
                        BorderFactory.createEmptyBorder(10, 10, 10, 10),
                        "Exam Code",
                        TitledBorder.CENTER,
                        TitledBorder.TOP,
                        HEADING_FONT,
                        PRIMARY_COLOR
                )
        ));

        return panel;
    }
    private JPanel InvitePanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        panel.setBackground(PANEL_BACKGROUND);
        panel.setBorder(createStyledBorder("Invite Examinee"));
        panel.setVisible(false);
        cboTimer.setPreferredSize(new Dimension(125, 50));
        btnSetTimer = createStyledButton("Set Timer", SECONDARY_COLOR, SECONDARY_GRADIENT, false);
        btnSetTimer.setPreferredSize(new Dimension(165, 50));
        btnSetTimer.addActionListener(e -> setTimer());


        panel.add(cboTimer);
        panel.add(btnSetTimer);

        // Improved visibility with shadow effect
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY_COLOR, 2),
                BorderFactory.createTitledBorder(
                        BorderFactory.createEmptyBorder(10, 10, 10, 10),
                        "Invite",
                        TitledBorder.CENTER,
                        TitledBorder.TOP,
                        HEADING_FONT,
                        PRIMARY_COLOR
                )
        ));

        return panel;
    }
    private JPanel createDatabasePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(PANEL_BACKGROUND);
        panel.setBorder(createStyledBorder("Question Database"));

        txtQuestionDatabase = new JTextArea(12, 50);
        txtQuestionDatabase.setEditable(false);
        txtQuestionDatabase.setFont(TEXT_AREA_FONT);
        txtQuestionDatabase.setMargin(new Insets(15, 15, 15, 15));
        txtQuestionDatabase.setBackground(FIELD_BACKGROUND);
        txtQuestionDatabase.setForeground(TEXT_COLOR);
        JScrollPane scrollPane = new JScrollPane(txtQuestionDatabase);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, JLabel label, JComponent field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.weightx = 0.3;
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0.7;
        panel.add(field, gbc);
    }

    private Border createStyledBorder(String title) {
        return BorderFactory.createTitledBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(PRIMARY_COLOR, 2, true),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)
                ),
                title,
                TitledBorder.CENTER,
                TitledBorder.TOP,
                HEADING_FONT,
                PRIMARY_COLOR
        );
    }

    private Border createFieldBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(SECONDARY_COLOR, 1, true),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        );
    }

    private JTextField createStyledTextField(int columns) {
        JTextField textField = new JTextField(columns);
        textField.setFont(FIELD_FONT);
        textField.setBorder(createFieldBorder());
        textField.setBackground(FIELD_BACKGROUND);
        return textField;
    }

    private JButton createStyledButton(String text, Color startColor, Color endColor, boolean glow) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, startColor, 0, getHeight(), endColor);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                if (glow && isEnabled()) {
                    g2.setColor(new Color(255, 255, 255, 70));
                    g2.fillRoundRect(3, 3, getWidth() - 6, getHeight() - 6, 18, 18);
                }
                g2.dispose();

                super.paintComponent(g);
            }

            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(startColor.darker());
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                g2.dispose();
            }
        };
        button.setFont(BUTTON_FONT);
        button.setForeground(BUTTON_TEXT);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(12, 24, 12, 24));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                button.setForeground(new Color(255, 255, 255));
            }
            public void mouseExited(MouseEvent evt) {
                button.setForeground(BUTTON_TEXT);
            }
            public void mousePressed(MouseEvent evt) {
                button.setForeground(new Color(220, 220, 220));
            }
            public void mouseReleased(MouseEvent evt) {
                button.setForeground(BUTTON_TEXT);
            }
        });
        return button;
    }
    private void inviteViaWhatsApp() {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            String examCode = txtExamCode.getText().trim().toUpperCase();
            if (examCode.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter an exam code.", "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String message = "Join the exam with code: " + examCode;
            try {
                String encodedMessage = URLEncoder.encode(message, "UTF-8");
                String url = "https://api.whatsapp.com/send?text=" + encodedMessage;
                Desktop.getDesktop().browse(new URI(url));
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error opening WhatsApp: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Desktop browsing is not supported on this system.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateRemainingQuestions() {
        int totalQuestions = (int) spinnerQuestionCount.getValue();
        remainingQuestions = totalQuestions - questionList.size();
        lblRemainingQuestions.setText(String.valueOf(remainingQuestions));
        timerPanel.setVisible(remainingQuestions == 0);
        if (remainingQuestions == 0) {
            timerPanel.repaint();
            examCodePanel.repaint();
        }
    }

    private void uploadPhoto() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Image files", "jpg", "png", "jpeg"));
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            txtPhotoPath.setText(selectedFile.getAbsolutePath());
        }
    }

    private void addQuestion() {
        String question = txtQuestion.getText().trim();
        String answers = txtAnswers.getText().trim();
        String marksStr = txtMarks.getText().trim();
        String explanation = txtExplanation.getText().trim();
        String photoPath = txtPhotoPath.getText().trim();
        String explanationPhotoPath = txtExplanationPhotoPath.getText().trim();

        if (question.isEmpty() || answers.isEmpty() || marksStr.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please fill in all required fields.",
                    "Input Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int marks = Integer.parseInt(marksStr);
            Question q = new Question(question, answers, marks, explanation,
                    photoPath.isEmpty() ? null : photoPath,
                    explanationPhotoPath.isEmpty() ? null : explanationPhotoPath);
            questionList.add(q);
            totalMarks += marks;

            updateRemainingQuestions();

            txtQuestion.setText("");
            txtAnswers.setText("");
            txtMarks.setText("");
            txtExplanation.setText("");
            txtPhotoPath.setText("");
            txtExplanationPhotoPath.setText("");

            // Enhanced confirmation dialog
            JOptionPane.showMessageDialog(this,
                    "Question added successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);

            // Focus back to question field for next entry
            txtQuestion.requestFocus();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Marks must be a valid number.",
                    "Input Error",
                    JOptionPane.WARNING_MESSAGE);
        }
    }


        private void viewQuestions() {
            StringBuilder questionsDisplay = new StringBuilder();

            // Create a more structured display format
            questionsDisplay.append("EXAM QUESTIONS SUMMARY\n");
            questionsDisplay.append("======================\n\n");
            questionsDisplay.append("Total Questions: ").append(questionList.size()).append("\n");
            questionsDisplay.append("Total Marks: ").append(totalMarks).append("\n\n");

            int count = 1;
            for (Question q : questionList) {
                questionsDisplay.append("QUESTION ").append(count++).append(" (").append(q.getMarks()).append(" marks)\n");
                questionsDisplay.append("------------------------------------------------------\n");
                questionsDisplay.append("Question: ").append(q.getQuestionText()).append("\n\n");
                questionsDisplay.append("Possible Answers: ").append(q.getPossibleAnswers()).append("\n\n");
                questionsDisplay.append("Photo: ").append(q.getPhotoPath() != null ? q.getPhotoPath() : "None").append("\n\n");
                if (!q.getExplanation().isEmpty()) {
                    questionsDisplay.append("Explanation: ").append(q.getExplanation()).append("\n\n");
                }
                questionsDisplay.append("Explanation Photo: ").append(q.getExplanationPhotoPath() != null ? q.getExplanationPhotoPath() : "None").append("\n\n");
            }

            txtQuestionDatabase.setText(questionsDisplay.toString());
        }
    private void setTimer() {
        String timerValue = (String) cboTimer.getSelectedItem();
        if (timerValue != null) {
            JOptionPane.showMessageDialog(this,
                    "Timer set to " + timerValue + " minutes.\n\nYour exam code has been generated.",
                    "Timer Set",
                    JOptionPane.INFORMATION_MESSAGE);

            examCodePanel.setVisible(true);
            txtExamCode.setText(generateExamCode());
            btnCreateExam.setEnabled(true);
            btnInvite.setEnabled(true); // Enable the Invite button once exam code is generated
        }
    }

    private void createExam() {
        if (questionList.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No questions available to create an exam.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        String examCode = txtExamCode.getText().trim();
        String timerValue = (String) cboTimer.getSelectedItem();

        // Input validation
        if (examCode.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter an exam code.",
                    "Input Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (timerValue == null) {
            JOptionPane.showMessageDialog(this,
                    "Please set a timer before creating the exam.",
                    "Input Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // Check if exam code already exists
            String checkQuery = "SELECT id FROM written_exams WHERE exam_code = ?";
            try (PreparedStatement checkStmt = connection.prepareStatement(checkQuery)) {
                checkStmt.setString(1, examCode);
                ResultSet checkRs = checkStmt.executeQuery();

                if (checkRs.next()) {
                    JOptionPane.showMessageDialog(this,
                            "Exam code already exists. Please use a different code.",
                            "Duplicate Code",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }

            // Begin transaction
            connection.setAutoCommit(false);

            // Insert the exam with examiner_id
            String insertExamSQL = "INSERT INTO written_exams (exam_code, timer_minutes, total_marks, examiner_id) VALUES (?, ?, ?, ?)";
            int examId;

            try (PreparedStatement psExam = connection.prepareStatement(insertExamSQL, Statement.RETURN_GENERATED_KEYS)) {
                psExam.setString(1, examCode);
                psExam.setInt(2, Integer.parseInt(timerValue));
                psExam.setInt(3, totalMarks);
                psExam.setInt(4, 1); // Set the examiner_id field
                psExam.executeUpdate();

                ResultSet rs = psExam.getGeneratedKeys();
                if (rs.next()) {
                    examId = rs.getInt(1);
                } else {
                    throw new SQLException("Failed to get generated exam ID");
                }
            }

            // Insert questions
            String insertQuestionSQL = "INSERT INTO written_questions (exam_id, question_text, possible_answers, marks, explanation, photo_path, explanation_photo_path) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement psQuestion = connection.prepareStatement(insertQuestionSQL)) {
                for (Question q : questionList) {
                    psQuestion.setInt(1, examId);
                    psQuestion.setString(2, q.getQuestionText());
                    psQuestion.setString(3, q.getPossibleAnswers());
                    psQuestion.setInt(4, q.getMarks());
                    psQuestion.setString(5, q.getExplanation());
                    psQuestion.setString(6, q.getPhotoPath());
                    psQuestion.setString(7, q.getExplanationPhotoPath());
                    psQuestion.addBatch();
                }
                psQuestion.executeBatch();
            }

            // Commit transaction
            connection.commit();
            connection.setAutoCommit(true);

            // Success message
            JOptionPane.showMessageDialog(this,
                    "Exam created successfully!\n\nExam Code: " + examCode +
                            "\nTotal Questions: " + questionList.size() +
                            "\nTotal Marks: " + totalMarks +
                            "\nTimer: " + timerValue + " minutes",
                    "Exam Created",
                    JOptionPane.INFORMATION_MESSAGE);

            // Clear form
            resetForm();

        } catch (SQLException e) {
            try {
                // Rollback transaction on error
                connection.rollback();
                connection.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }

            JOptionPane.showMessageDialog(this,
                    "Error saving exam: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void resetForm() {
        // Reset all fields for a new exam
        questionList.clear();
        totalMarks = 0;
        spinnerQuestionCount.setValue(1);
        txtQuestion.setText("");
        txtAnswers.setText("");
        txtMarks.setText("");
        txtExplanation.setText("");
        txtPhotoPath.setText("");
        txtExamCode.setText("");
        txtQuestionDatabase.setText("");
        timerPanel.setVisible(false);
        examCodePanel.setVisible(false);
        btnCreateExam.setEnabled(false);
        btnInvite.setEnabled(false);
        updateRemainingQuestions();
    }

    private String generateExamCode() {
        return "EX" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    public static void main(String[] args) {
        // Set look and feel to improve appearance
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(ExaminerProfile1::new);
    }
}

class Question {
    private static int idCounter = 1;
    private int id;
    private String questionText;
    private String possibleAnswers;
    private int marks;
    private String explanation;
    private String photoPath;
    private String explanationPhotoPath;

    // Update the constructor
    public Question(String questionText, String possibleAnswers, int marks, String explanation,
                    String photoPath, String explanationPhotoPath) {
        this.id = idCounter++;
        this.questionText = questionText;
        this.possibleAnswers = possibleAnswers;
        this.marks = marks;
        this.explanation = explanation;
        this.photoPath = photoPath;
        this.explanationPhotoPath = explanationPhotoPath;
    }

    // Add getter
    public String getExplanationPhotoPath() { return explanationPhotoPath; }



    public String getQuestionText() { return questionText; }
    public String getPossibleAnswers() { return possibleAnswers; }
    public int getMarks() { return marks; }
    public String getExplanation() { return explanation; }
    public String getPhotoPath() { return photoPath; }
}