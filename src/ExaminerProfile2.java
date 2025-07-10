import javax.mail.*;
import javax.mail.internet.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;
import java.util.List;
import java.util.ArrayList;



public class ExaminerProfile2 extends JFrame {

    private JTextField txtQuestion, txtOption1, txtOption2, txtOption3, txtOption4, txtCorrectAnswer, txtMarks, txtExamCode;
    private JTextArea txtQuestionDatabase;
    private JComboBox<String> cboTimer;
    private JButton btnAddQuestion, btnViewQuestions, btnSetTimer, btnCreateExam, btnInviteStudents;
    private JLabel lblQuestionCount, lblRemainingQuestions;
    private JSpinner spinnerQuestionCount;
    private JPanel mainPanel, formPanel, timerPanel, examCodePanel, invitePanel;

    private Connection connection;
    private List<Question1> questionList;
    private int totalMarks;
    private int remainingQuestions;
    private int currentExamId = -1; // Track the current exam ID
    private String currentExamCode; // Track the current exam code

    public ExaminerProfile2() {
        setTitle("MCQ Exam Creator");
        setSize(1000, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Database connection
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection rootConnection = DriverManager.getConnection("jdbc:mysql://localhost:3306/eah22", "root", "MySQL@24");
            Statement stmt = rootConnection.createStatement();
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS eah22");
            stmt.close();
            rootConnection.close();

            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/eah22", "root", "MySQL@24");
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database connection failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            System.exit(1);
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(this, "JDBC Driver not found: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            System.exit(1);
        }

        // Main panel with gradient background
        mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                Color color1 = new Color(30, 136, 229);
                Color color2 = new Color(255, 193, 7);
                GradientPaint gradient = new GradientPaint(0, 0, color1, getWidth(), getHeight(), color2);
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Headline
        JLabel lblHeadline = new JLabel("MCQ Exam Creator", JLabel.CENTER);
        lblHeadline.setFont(new Font("Arial", Font.BOLD, 28));
        lblHeadline.setForeground(Color.WHITE);
        lblHeadline.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(lblHeadline);

        // Form panel for question input
        formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(new Color(255, 255, 255, 180));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        lblQuestionCount = new JLabel("Total Questions:");
        lblQuestionCount.setFont(new Font("Arial", Font.BOLD, 16));
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(lblQuestionCount, gbc);

        spinnerQuestionCount = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        spinnerQuestionCount.setFont(new Font("Arial", Font.PLAIN, 16));
        gbc.gridx = 1;
        gbc.gridy = 0;
        formPanel.add(spinnerQuestionCount, gbc);

        lblRemainingQuestions = new JLabel("Remaining Questions: 0");
        lblRemainingQuestions.setFont(new Font("Arial", Font.BOLD, 16));
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        formPanel.add(lblRemainingQuestions, gbc);
        gbc.gridwidth = 1;

        addLabelAndField("Question:", txtQuestion = new JTextField(60), 2, formPanel, gbc);
        addLabelAndField("Option 1:", txtOption1 = new JTextField(60), 3, formPanel, gbc);
        addLabelAndField("Option 2:", txtOption2 = new JTextField(60), 4, formPanel, gbc);
        addLabelAndField("Option 3:", txtOption3 = new JTextField(60), 5, formPanel, gbc);
        addLabelAndField("Option 4:", txtOption4 = new JTextField(60), 6, formPanel, gbc);
        addLabelAndField("Correct Answer:", txtCorrectAnswer = new JTextField(60), 7, formPanel, gbc);
        addLabelAndField("Marks:", txtMarks = new JTextField(60), 8, formPanel, gbc);

        btnAddQuestion = createButton("Add Question", new Color(0, 150, 136));
        btnAddQuestion.addActionListener(e -> addQuestion());
        gbc.gridx = 0;
        gbc.gridy = 9;
        gbc.gridwidth = 2;
        formPanel.add(btnAddQuestion, gbc);
        gbc.gridwidth = 1;

        btnViewQuestions = createButton("View Questions", new Color(255, 193, 7));
        btnViewQuestions.addActionListener(e -> viewQuestions());
        gbc.gridx = 0;
        gbc.gridy = 10;
        gbc.gridwidth = 2;
        formPanel.add(btnViewQuestions, gbc);
        gbc.gridwidth = 1;

        mainPanel.add(formPanel);

        // Timer panel
        timerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        timerPanel.setBackground(new Color(255, 255, 255, 180));
        timerPanel.setVisible(false);

        JLabel lblTimer = new JLabel("Set Timer (minutes):");
        lblTimer.setFont(new Font("Arial", Font.BOLD, 16));
        cboTimer = new JComboBox<>(new String[]{"1","5", "10", "20", "30", "40", "50", "60", "70", "80", "90", "100", "110", "120"});
        cboTimer.setFont(new Font("Arial", Font.PLAIN, 16));
        btnSetTimer = createButton("Set Timer", new Color(3, 169, 244));
        btnSetTimer.addActionListener(e -> setTimer());

        timerPanel.add(lblTimer);
        timerPanel.add(cboTimer);
        timerPanel.add(btnSetTimer);
        mainPanel.add(timerPanel);

        // Exam code panel
        examCodePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        examCodePanel.setBackground(new Color(255, 255, 255, 180));
        examCodePanel.setVisible(false);

        JLabel lblExamCode = new JLabel("Exam Code:");
        lblExamCode.setFont(new Font("Arial", Font.BOLD, 16));
        txtExamCode = new JTextField(30);
        txtExamCode.setFont(new Font("Arial", Font.PLAIN, 16));
        txtExamCode.setEditable(false);

        examCodePanel.add(lblExamCode);
        examCodePanel.add(txtExamCode);
        mainPanel.add(examCodePanel);

        // Invite panel
        invitePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        invitePanel.setBackground(new Color(255, 255, 255, 180));
        invitePanel.setVisible(false);

        btnInviteStudents = createButton("Invite Students", new Color(76, 175, 80));
        btnInviteStudents.addActionListener(e -> showInviteDialog());
        invitePanel.add(btnInviteStudents);
        mainPanel.add(invitePanel);

        // Create exam button
        btnCreateExam = createButton("Create Exam", new Color(3, 169, 244));
        btnCreateExam.addActionListener(e -> createExam());
        btnCreateExam.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(btnCreateExam);

        // Question database display
        txtQuestionDatabase = new JTextArea(30, 80);
        txtQuestionDatabase.setEditable(false);
        txtQuestionDatabase.setBackground(new Color(230, 230, 230));
        txtQuestionDatabase.setFont(new Font("Arial", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(txtQuestionDatabase);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Question Database"));
        mainPanel.add(scrollPane);

        add(mainPanel);

        questionList = new ArrayList<>();
        totalMarks = 0;
        remainingQuestions = 0;

        // Initialize exam creation
        initializeExam();

        setVisible(true);
    }

    //### Initialize Exam
    private void initializeExam() {
        try {
            currentExamCode = generateExamCode();
            String initExamQuery = "INSERT INTO exams (title, duration_minutes, total_marks, exam_code, examiner_id) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement initExamStmt = connection.prepareStatement(initExamQuery, Statement.RETURN_GENERATED_KEYS);
            initExamStmt.setString(1, "Temporary Exam Title");
            initExamStmt.setInt(2, 60); // Default duration
            initExamStmt.setInt(3, 0);  // Initial total marks
            initExamStmt.setString(4, currentExamCode);
            initExamStmt.setInt(5, 1); // Hardcoded examiner_id for now
            initExamStmt.executeUpdate();

            ResultSet rs = initExamStmt.getGeneratedKeys();
            if (rs.next()) {
                currentExamId = rs.getInt(1);
            }

            connection.commit();
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to initialize exam: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addLabelAndField(String labelText, JTextField textField, int row, JPanel panel, GridBagConstraints gbc) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.BOLD, 16));
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        panel.add(label, gbc);

        textField.setFont(new Font("Arial", Font.PLAIN, 16));
        textField.setPreferredSize(new Dimension(300, 30));
        gbc.gridx = 1;
        gbc.gridy = row;
        panel.add(textField, gbc);
    }

    private JButton createButton(String text, Color backgroundColor) {
        JButton button = new JButton(text);
        button.setBackground(backgroundColor);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(150, 40));
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setUI(new StyledButtonUI());
        return button;
    }

  //  ### Add Question
    private void addQuestion() {
        String question = txtQuestion.getText();
        String option1 = txtOption1.getText();
        String option2 = txtOption2.getText();
        String option3 = txtOption3.getText();
        String option4 = txtOption4.getText();
        String correctAnswer = txtCorrectAnswer.getText();
        String marksText = txtMarks.getText();

        if (question.isEmpty() || option1.isEmpty() || option2.isEmpty() || option3.isEmpty() ||
                option4.isEmpty() || correctAnswer.isEmpty() || marksText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required!", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int marks = Integer.parseInt(marksText);
            Question1 q = new Question1(question, option1, option2, option3, option4, correctAnswer, marks, connection);
            boolean saved = q.saveQuestionToDatabase();

            if (saved) {
                if (currentExamId != -1) {
                    linkQuestionToExam(q.getId(), currentExamId);
                }
                questionList.add(q);
                totalMarks += marks;
                remainingQuestions = (int) spinnerQuestionCount.getValue() - questionList.size();
                lblRemainingQuestions.setText("Remaining Questions: " + remainingQuestions);
                JOptionPane.showMessageDialog(this, "Question added successfully!");
                clearFields();

                if (remainingQuestions == 0) {
                    timerPanel.setVisible(true);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Failed to save question to database!", "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Marks must be a valid number!", "Input Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void linkQuestionToExam(int questionId, int examId) {
        try {
            String query = "INSERT INTO exam_questions (exam_id, question_id) VALUES (?, ?)";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, examId);
            stmt.setInt(2, questionId);
            stmt.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        }
    }

   // ### View Questions
    private void viewQuestions() {
        try {
            String query = "SELECT q.* FROM questions q JOIN exam_questions eq ON q.id = eq.question_id WHERE eq.exam_id = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, currentExamId);
            ResultSet rs = stmt.executeQuery();
            StringBuilder questionsDisplay = new StringBuilder();
            while (rs.next()) {
                questionsDisplay.append("Question: ").append(rs.getString("question_text")).append("\n")
                        .append("Options: ").append(rs.getString("option1")).append(", ").append(rs.getString("option2")).append(", ")
                        .append(rs.getString("option3")).append(", ").append(rs.getString("option4")).append("\n")
                        .append("Correct Answer: ").append(rs.getString("correct_answer")).append("\n")
                        .append("Marks: ").append(rs.getInt("marks")).append("\n\n");
            }
            txtQuestionDatabase.setText(questionsDisplay.toString());
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching questions: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Set Timer
    private void setTimer() {
        String timerValue = (String) cboTimer.getSelectedItem();
        if (timerValue != null) {
            try {
                String updateQuery = "UPDATE exams SET duration_minutes = ? WHERE id = ?";
                PreparedStatement updateStmt = connection.prepareStatement(updateQuery);
                updateStmt.setInt(1, Integer.parseInt(timerValue));
                updateStmt.setInt(2, currentExamId);
                updateStmt.executeUpdate();
                connection.commit();

                JOptionPane.showMessageDialog(this, "Timer set to " + timerValue + " minutes.");
                txtExamCode.setText(currentExamCode);
                examCodePanel.setVisible(true);
                invitePanel.setVisible(true);
            } catch (SQLException e) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error updating timer: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    //### Create Exam
    private void createExam() {
        int questionCount = (int) spinnerQuestionCount.getValue();
        if (questionList.size() < questionCount) {
            JOptionPane.showMessageDialog(this, "You need to add at least " + questionCount + " questions!", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String timerValue = (String) cboTimer.getSelectedItem();
        if (timerValue == null) {
            JOptionPane.showMessageDialog(this, "Please set the timer!", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            String updateQuery = "UPDATE exams SET title = ?, total_marks = ? WHERE id = ?";
            PreparedStatement updateStmt = connection.prepareStatement(updateQuery);
            updateStmt.setString(1, "My MCQ Exam");
            updateStmt.setInt(2, totalMarks);
            updateStmt.setInt(3, currentExamId);
            updateStmt.executeUpdate();
            connection.commit();

            JOptionPane.showMessageDialog(this, "Exam created successfully!\nExam Code: " + currentExamCode);
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error finalizing exam: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearFields() {
        txtQuestion.setText("");
        txtOption1.setText("");
        txtOption2.setText("");
        txtOption3.setText("");
        txtOption4.setText("");
        txtCorrectAnswer.setText("");
        txtMarks.setText("");
    }

    private String generateExamCode() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

   // ### Invite Students
    private void showInviteDialog() {
        JDialog inviteDialog = new JDialog(this, "Invite Students", true);
        inviteDialog.setSize(400, 250);
        inviteDialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblInfo = new JLabel("Enter Examinee Email to invite:");
        lblInfo.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        inviteDialog.add(lblInfo, gbc);
        gbc.gridwidth = 1;

        JLabel lblEmail = new JLabel("Email:");
        lblEmail.setFont(new Font("Arial", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 1;
        inviteDialog.add(lblEmail, gbc);
        JTextField txtEmail = new JTextField();
        txtEmail.setFont(new Font("Arial", Font.PLAIN, 14));
        gbc.gridx = 1;
        gbc.gridy = 1;
        inviteDialog.add(txtEmail, gbc);

        JButton btnSendInvite = createButton("Send Invite", new Color(0, 150, 136));
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        inviteDialog.add(btnSendInvite, gbc);

        btnSendInvite.addActionListener(e -> {
            String email = txtEmail.getText().trim();
            if (email.isEmpty()) {
                JOptionPane.showMessageDialog(inviteDialog, "Please enter an email", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                saveInvitation(email);
                sendEmailInvitation(email, currentExamCode);
                JOptionPane.showMessageDialog(inviteDialog, "Invitation sent successfully!");
                txtEmail.setText("");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(inviteDialog, "Error sending invitation: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        inviteDialog.setLocationRelativeTo(this);
        inviteDialog.setVisible(true);
    }

    private int getExamineeIdByEmail(String email) {
        try {
            String query = "SELECT id FROM examinees WHERE email = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            } else {
                return -1; // Not found
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private void saveInvitation(String email) {
        int examineeId = getExamineeIdByEmail(email);
        if (examineeId == -1) {
            JOptionPane.showMessageDialog(this, "Examinee not found with email: " + email, "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            String checkQuery = "SELECT COUNT(*) FROM exam_participation WHERE examinee_id = ? AND exam_id = ?";
            PreparedStatement checkStmt = connection.prepareStatement(checkQuery);
            checkStmt.setInt(1, examineeId);
            checkStmt.setInt(2, currentExamId);
            ResultSet rs = checkStmt.executeQuery();
            rs.next();
            if (rs.getInt(1) > 0) {
                JOptionPane.showMessageDialog(this, "Examinee already invited to this exam.", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            String query = "INSERT INTO exam_participation (examinee_id, exam_id) VALUES (?, ?)";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, examineeId);
            stmt.setInt(2, currentExamId);
            stmt.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            throw new RuntimeException("Error saving invitation: " + e.getMessage());
        }
    }

    private void sendEmailInvitation(String toEmail, String examCode) {
        String examLink = "http://localhost:8080/exam?code=" + examCode;
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        final String username = "zfahmid54@gmail.com"; // Replace with your Gmail email
        final String password = "okjy tyzo uzui sjkg"; // Replace with your Gmail App Password

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Exam Invitation");
            message.setText("Dear Examinee,\n\nYou have been invited to participate in an MCQ exam.\n"
                    + "Please use the following link to access the exam:\n" + examLink
                    + "\n\nBest Regards,\nExam Management Team");
            Transport.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
            throw new RuntimeException("Error sending email: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ExaminerProfile2());
    }
}

class StyledButtonUI extends javax.swing.plaf.basic.BasicButtonUI {
    @Override
    public void paint(Graphics g, JComponent c) {
        AbstractButton button = (AbstractButton) c;
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(button.getBackground());
        g2d.fillRoundRect(0, 0, button.getWidth(), button.getHeight(), 20, 20);
        super.paint(g, c);
    }
}

class Question1 {
    private int id;
    private String questionText;
    private String option1, option2, option3, option4;
    private String correctAnswer;
    private int marks;
    private Connection connection;

    public Question1(String questionText, String option1, String option2, String option3,
                     String option4, String correctAnswer, int marks, Connection connection) {
        this.questionText = questionText;
        this.option1 = option1;
        this.option2 = option2;
        this.option3 = option3;
        this.option4 = option4;
        this.correctAnswer = correctAnswer;
        this.marks = marks;
        this.connection = connection;
    }

    public boolean saveQuestionToDatabase() {
        String query = "INSERT INTO questions (question_text, option1, option2, option3, option4, correct_answer, marks) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, questionText);
            stmt.setString(2, option1);
            stmt.setString(3, option2);
            stmt.setString(4, option3);
            stmt.setString(5, option4);
            stmt.setString(6, correctAnswer);
            stmt.setInt(7, marks);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating question failed, no rows affected.");
            }

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                this.id = rs.getInt(1);
                connection.commit();
                return true;
            } else {
                throw new SQLException("Creating question failed, no ID obtained.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to save question: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            try {
                connection.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            return false;
        }
    }

    public int getId() { return id; }
    public String getQuestionText() { return questionText; }
    public String getOption1() { return option1; }
    public String getOption2() { return option2; }
    public String getOption3() { return option3; }
    public String getOption4() { return option4; }
    public String getCorrectAnswer() { return correctAnswer; }
    public int getMarks() { return marks; }
}