import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.List;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.geom.RoundRectangle2D;
import java.text.SimpleDateFormat;
import javax.swing.table.TableCellRenderer;

class StudentQuestion {
    private int id;
    private String questionText;
    private String option1, option2, option3, option4;
    private String correctAnswer;
    private int marks;
    private String explanation;


    public StudentQuestion(int id, String questionText, String option1, String option2, String option3, String option4, int marks, String explanation) {
        this.id = id;
        this.questionText = questionText;
        this.option1 = option1;
        this.option2 = option2;
        this.option3 = option3;
        this.option4 = option4;
        this.marks = marks;
        this.explanation = explanation;
    }

    public int getId() { return id; }
    public String getQuestionText() { return questionText; }
    public String getOption1() { return option1; }
    public String getOption2() { return option2; }
    public String getOption3() { return option3; }
    public String getOption4() { return option4; }
    public int getMarks() { return marks; }
    public String getExplanation() { return explanation; }
}

public class StudentExamUI extends JFrame {
    // Professional Color Palette
    private List<Clip> activeClips = new ArrayList<>();
    private static final Color PRIMARY_COLOR = new Color(33, 150, 243);    // Vibrant Blue (Headers, Buttons)
    private static final Color SECONDARY_COLOR = new Color(76, 175, 80);   // Fresh Green (Navigation, Success)
    private static final Color ACCENT_COLOR = new Color(255, 193, 7);      // Warm Amber (Submit, Highlights)
    private static final Color BACKGROUND_COLOR = new Color(245, 245, 250); // Soft Gray-Blue (Background)
    private static final Color CARD_COLOR = new Color(255, 255, 255);      // Crisp White (Cards, Panels)
    private static final Color TEXT_COLOR = new Color(33, 33, 33);         // Dark Gray (Text)

    private Connection connection;
    private List<StudentQuestion> questions;
    private Map<Integer, String> studentAnswers;
    private int currentQuestionIndex;
    private javax.swing.Timer timer;
    private int remainingSeconds;
    private JLabel timerLabel;
    private JLabel lblQuestion;
    private JRadioButton radio1, radio2, radio3, radio4;
    private ButtonGroup optionGroup;
    private int studentId;
    private int examId;
    private int totalMarks;
    private String examineeName;
    private CardLayout cardLayout;
    private JPanel cardsPanel;
    private JPanel mainPanel, examPanel, meritListPanel, resultPanel;
    private JTextField txtExamCode;
    private JButton btnStartExam, btnViewMeritList;
    private Clip alarmClip;
    private JPanel contentPanel;
    private JPanel meritTablePanel;

    public StudentExamUI(int studentId, String examineeName) {
        this.studentId = studentId;
        this.examineeName = examineeName;
        setTitle("ExamMaster - Student Portal (MCQ Exams)");
        setSize(900, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Database connection
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/eah22", "root", "MySQL@24");
        } catch (SQLException | ClassNotFoundException e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        // Set up card layout
        cardLayout = new CardLayout();
        cardsPanel = new JPanel(cardLayout);

        // Initialize panels
        createMainPanel();
        examPanel = new JPanel(); // Empty initially, updated in showExamPanel
        createMeritListPanel();
        resultPanel = new JPanel(); // Empty initially, updated in displayResults

        // Add panels to card layout
        cardsPanel.add(mainPanel, "MAIN");
        cardsPanel.add(examPanel, "EXAM");
        cardsPanel.add(meritListPanel, "MERIT_LIST");
        cardsPanel.add(resultPanel, "RESULT");

        add(cardsPanel);
        cardLayout.show(cardsPanel, "MAIN");
        setVisible(true);
    }

    private void createMainPanel() {
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Header with examinee info
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        JLabel lblStudentInfo = new JLabel("Welcome, " + examineeName);
        lblStudentInfo.setForeground(Color.WHITE);
        lblStudentInfo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        headerPanel.add(lblStudentInfo, BorderLayout.WEST);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Exam code entry
        JPanel examCodePanel = new JPanel();
        examCodePanel.setBackground(BACKGROUND_COLOR);
        examCodePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(PRIMARY_COLOR, 2),
                "Enter MCQ Exam Code",
                0, 0, new Font("Segoe UI", Font.BOLD, 16), PRIMARY_COLOR
        ));

        examCodePanel.add(new JLabel("Exam Code:"));
        txtExamCode = new JTextField(15);
        txtExamCode.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        examCodePanel.add(txtExamCode);

        btnStartExam = createButton("Start Exam", PRIMARY_COLOR);
        btnStartExam.addActionListener(e -> validateAndStartExam());
        examCodePanel.add(btnStartExam);

        btnViewMeritList = createButton("View Merit List", SECONDARY_COLOR);
        btnViewMeritList.addActionListener(e -> cardLayout.show(cardsPanel, "MERIT_LIST"));
        examCodePanel.add(btnViewMeritList);

        mainPanel.add(examCodePanel, BorderLayout.CENTER);
    }

    private void validateAndStartExam() {
        String examCode = txtExamCode.getText().trim();
        if (examCode.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter an exam code.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            String query = "SELECT id, duration_minutes FROM exams WHERE exam_code = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, examCode);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                examId = rs.getInt("id");
                int durationMinutes = rs.getInt("duration_minutes");

                // Check if student has already taken this exam
                String checkQuery = "SELECT * FROM student_scores WHERE student_id = ? AND exam_id = ?";
                PreparedStatement checkStmt = connection.prepareStatement(checkQuery);
                checkStmt.setInt(1, studentId);
                checkStmt.setInt(2, examId);
                ResultSet checkRs = checkStmt.executeQuery();
                if (checkRs.next()) {
                    JOptionPane.showMessageDialog(this, "You have already taken this exam.", "Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Fetch questions and start exam
                questions = fetchQuestions(examId);
                if (questions.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "No questions found for this exam.", "Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                showExamPanel(questions, durationMinutes);
                cardLayout.show(cardsPanel, "EXAM");
            } else {
                JOptionPane.showMessageDialog(this, "Invalid exam code.", "Error", JOptionPane.WARNING_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private List<StudentQuestion> fetchQuestions(int examId) throws SQLException {
        List<StudentQuestion> questions = new ArrayList<>();
        String query = "SELECT q.id, q.question_text, q.option1, q.option2, q.option3, q.option4, q.marks, q.correct_answer " +
                "FROM questions q JOIN exam_questions eq ON q.id = eq.question_id WHERE eq.exam_id = ?";
        PreparedStatement stmt = connection.prepareStatement(query);
        stmt.setInt(1, examId);
        ResultSet rs = stmt.executeQuery();
        totalMarks = 0; // Reset totalMarks
        while (rs.next()) {
            int id = rs.getInt("id");
            String questionText = rs.getString("question_text");
            String option1 = rs.getString("option1");
            String option2 = rs.getString("option2");
            String option3 = rs.getString("option3");
            String option4 = rs.getString("option4");
            int marks = rs.getInt("marks");
            String correctAnswer = rs.getString("correct_answer");

            StudentQuestion q = new StudentQuestion(id, questionText, option1, option2, option3, option4, marks, null);
            questions.add(q);
            totalMarks += marks;
        }
        return questions;
    }

    private void showExamPanel(List<StudentQuestion> questions, int durationMinutes) {
        examPanel.removeAll();
        examPanel.setLayout(new BorderLayout(20, 20));
        examPanel.setBackground(BACKGROUND_COLOR);
        examPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Header with Timer
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(CARD_COLOR);
        topPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                new EmptyBorder(15, 15, 15, 15)));
        JLabel examTitle = new JLabel("Exam In Progress");
        examTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        examTitle.setForeground(PRIMARY_COLOR);
        timerLabel = new JLabel("Time Remaining: 00:00");
        timerLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        timerLabel.setForeground(SECONDARY_COLOR);
        topPanel.add(examTitle, BorderLayout.WEST);
        topPanel.add(timerLabel, BorderLayout.EAST);
        examPanel.add(topPanel, BorderLayout.NORTH);

        // Question Card
        JPanel questionCard = new JPanel();
        questionCard.setLayout(new BoxLayout(questionCard, BoxLayout.Y_AXIS));
        questionCard.setBackground(CARD_COLOR);
        questionCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                new EmptyBorder(20, 20, 20, 20)));
        lblQuestion = new JLabel();
        lblQuestion.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblQuestion.setForeground(TEXT_COLOR);
        questionCard.add(lblQuestion);
        questionCard.add(Box.createVerticalStrut(20));

        JPanel optionsPanel = new JPanel(new GridLayout(4, 1, 0, 15));
        optionsPanel.setBackground(CARD_COLOR);
        optionGroup = new ButtonGroup();
        radio1 = createRadioButton();
        radio2 = createRadioButton();
        radio3 = createRadioButton();
        radio4 = createRadioButton();
        optionGroup.add(radio1);
        optionGroup.add(radio2);
        optionGroup.add(radio3);
        optionGroup.add(radio4);
        optionsPanel.add(radio1);
        optionsPanel.add(radio2);
        optionsPanel.add(radio3);
        optionsPanel.add(radio4);
        questionCard.add(optionsPanel);
        examPanel.add(questionCard, BorderLayout.CENTER);

        // Navigation Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setBackground(BACKGROUND_COLOR);
        JButton btnPrevious = createButton("Previous", SECONDARY_COLOR);
        JButton btnNext = createButton("Next", SECONDARY_COLOR);
        JButton btnSubmit = createButton("Submit", ACCENT_COLOR);
        buttonPanel.add(btnPrevious);
        buttonPanel.add(btnNext);
        buttonPanel.add(btnSubmit);
        examPanel.add(buttonPanel, BorderLayout.SOUTH);

        this.questions = questions;
        studentAnswers = new HashMap<>();
        currentQuestionIndex = 0;
        showQuestion(currentQuestionIndex);
        startTimer(durationMinutes);

        btnPrevious.addActionListener(e -> {
            saveCurrentAnswer();
            if (currentQuestionIndex > 0) {
                currentQuestionIndex--;
                showQuestion(currentQuestionIndex);
            }
        });

        btnNext.addActionListener(e -> {
            saveCurrentAnswer();
            if (currentQuestionIndex < questions.size() - 1) {
                currentQuestionIndex++;
                showQuestion(currentQuestionIndex);
            }
        });

        btnSubmit.addActionListener(e -> {
            int option = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to submit your exam?",
                    "Confirm Submission",
                    JOptionPane.YES_NO_OPTION);
            if (option == JOptionPane.YES_OPTION) {
                saveCurrentAnswer();
                submitExam();
            }
        });

        examPanel.revalidate();
        examPanel.repaint();
    }

    private JRadioButton createRadioButton() {
        JRadioButton radio = new JRadioButton();
        radio.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        radio.setBackground(CARD_COLOR);
        radio.setForeground(TEXT_COLOR);
        radio.setFocusPainted(false);
        return radio;
    }

    private void showQuestion(int index) {
        StudentQuestion q = questions.get(index);
        lblQuestion.setText("<html><div style='width: 600px;'>" + (index + 1) + ". " + q.getQuestionText() + " (" + q.getMarks() + " marks)</div></html>");
        radio1.setText(q.getOption1());
        radio2.setText(q.getOption2());
        radio3.setText(q.getOption3());
        radio4.setText(q.getOption4());
        optionGroup.clearSelection();
        if (studentAnswers.containsKey(q.getId())) {
            String answer = studentAnswers.get(q.getId());
            if (answer.equals(q.getOption1())) radio1.setSelected(true);
            else if (answer.equals(q.getOption2())) radio2.setSelected(true);
            else if (answer.equals(q.getOption3())) radio3.setSelected(true);
            else if (answer.equals(q.getOption4())) radio4.setSelected(true);
        }
    }

    private void saveCurrentAnswer() {
        if (currentQuestionIndex >= 0 && currentQuestionIndex < questions.size()) {
            StudentQuestion q = questions.get(currentQuestionIndex);
            String selectedAnswer = null;
            if (radio1.isSelected()) selectedAnswer = radio1.getText();
            else if (radio2.isSelected()) selectedAnswer = radio2.getText();
            else if (radio3.isSelected()) selectedAnswer = radio3.getText();
            else if (radio4.isSelected()) selectedAnswer = radio4.getText();
            if (selectedAnswer != null) {
                studentAnswers.put(q.getId(), selectedAnswer);
            }
        }
    }

    private void playAlarmSound() {
        try {
            File soundFile = new File("D:\\Project22\\ExamaHead22\\src\\tic-tac-27828.wav");
            if (!soundFile.exists()) {
                System.err.println("Sound file not found: " + soundFile.getAbsolutePath());
                return;
            }
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    activeClips.remove(clip);
                }
            });
            clip.start();
            activeClips.add(clip);
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("Error playing sound: " + e.getMessage());
        }
    }
    private void stopAlarmSound() {
        List<Clip> clipsToStop = new ArrayList<>(activeClips);
        for (Clip clip : clipsToStop) {
            if (clip != null) {
                if (clip.isRunning()) {
                    clip.stop();
                }
                clip.close();
            }
        }
        activeClips.clear();
    }

    private void startTimer(int durationMinutes) {
        remainingSeconds = durationMinutes * 60;
        int totalSeconds = remainingSeconds;

        timer = new javax.swing.Timer(1000, e -> {
            remainingSeconds--;
            int minutes = remainingSeconds / 60;
            int seconds = remainingSeconds % 60;
            timerLabel.setText(String.format("Time Remaining: %02d:%02d", minutes, seconds));

            double percentageRemaining = (double) remainingSeconds / totalSeconds;
            if (percentageRemaining <= 0.05) {
                timerLabel.setForeground(Color.RED);
            } else if (percentageRemaining <= 0.30) {
                timerLabel.setForeground(ACCENT_COLOR);
            } else {
                timerLabel.setForeground(SECONDARY_COLOR);
            }

            if (remainingSeconds <= 10 && remainingSeconds > 0) {
                playAlarmSound();
            }

            if (remainingSeconds <= 0) {
                timer.stop();
                stopAlarmSound();
                JOptionPane.showMessageDialog(this, "Time's up! Your exam will be submitted now.");
                submitExam();
            }
        });
        timer.start();
    }




    private void submitExam() {
        if (timer != null) {
            timer.stop();
        }
        stopAlarmSound();
        if (questions.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No questions to submit", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            StringBuilder idList = new StringBuilder();
            for (StudentQuestion q : questions) {
                if (idList.length() > 0) idList.append(",");
                idList.append(q.getId());
            }
            String query = "SELECT id, correct_answer FROM questions WHERE id IN (" + idList.toString() + ")";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            Map<Integer, String> correctAnswers = new HashMap<>();
            while (rs.next()) {
                correctAnswers.put(rs.getInt("id"), rs.getString("correct_answer"));
            }
            int score = 0;

            for (StudentQuestion q : questions) {
                String studentAnswer = studentAnswers.get(q.getId());
                if (studentAnswer != null) {
                    String insertAnswerQuery = "INSERT INTO student_answers (student_id, question_id, selected_answer) VALUES (?, ?, ?)";
                    PreparedStatement answerStmt = connection.prepareStatement(insertAnswerQuery);
                    answerStmt.setInt(1, studentId);
                    answerStmt.setInt(2, q.getId());
                    answerStmt.setString(3, studentAnswer);
                    answerStmt.executeUpdate();

                    if (studentAnswer.equals(correctAnswers.get(q.getId()))) {
                        score += q.getMarks();
                    }
                }
            }

            double percentageScore = (double) score / totalMarks * 100;
            String grade = calculateGrade(percentageScore);
            String insertScoreQuery = "INSERT INTO student_scores (student_id, exam_id, score, total_marks, grade) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement scoreStmt = connection.prepareStatement(insertScoreQuery);
            scoreStmt.setInt(1, studentId);
            scoreStmt.setInt(2, examId);
            scoreStmt.setInt(3, score);
            scoreStmt.setInt(4, totalMarks);
            scoreStmt.setString(5, grade);
            scoreStmt.executeUpdate();

            JOptionPane.showMessageDialog(this,
                    String.format("Exam submitted. Your score: %d out of %d (%.1f%%)\nGrade: %s",
                            score, totalMarks, percentageScore, grade),
                    "Exam Results",
                    JOptionPane.INFORMATION_MESSAGE);
            displayResults(correctAnswers, score, totalMarks, percentageScore);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error submitting exam: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private String calculateGrade(double percentageScore) {
        if (percentageScore >= 90) return "A";
        if (percentageScore >= 80) return "B";
        if (percentageScore >= 70) return "C";
        if (percentageScore >= 60) return "D";
        return "F";
    }

    private void displayResults(Map<Integer, String> correctAnswers, int score, int totalMarks, double percentageScore) {
        resultPanel.removeAll();
        resultPanel.setLayout(new BorderLayout(20, 20));
        resultPanel.setBackground(BACKGROUND_COLOR);
        resultPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(CARD_COLOR);
        headerPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        JLabel title = new JLabel("Your Exam Results");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(PRIMARY_COLOR);
        headerPanel.add(title, BorderLayout.CENTER);
        resultPanel.add(headerPanel, BorderLayout.NORTH);

        // Results Panel
        JPanel resultsPanel = new JPanel();
        resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS));
        resultsPanel.setBackground(BACKGROUND_COLOR);

        int questionNumber = 1;
        for (StudentQuestion q : questions) {
            String correctAnswer = correctAnswers.get(q.getId());
            String studentAnswer = studentAnswers.getOrDefault(q.getId(), "Not Answered");
            boolean isCorrect = studentAnswer.equals(correctAnswer);

            JPanel card = new JPanel();
            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
            card.setBackground(CARD_COLOR);
            card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(200, 200, 200)),
                    new EmptyBorder(15, 15, 15, 15)));
            JLabel qLabel = new JLabel(questionNumber + ". " + q.getQuestionText());
            qLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
            qLabel.setForeground(TEXT_COLOR);
            card.add(qLabel);

            JLabel yourAnswer = new JLabel("Your Answer: " + studentAnswer);
            yourAnswer.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            yourAnswer.setForeground(isCorrect ? SECONDARY_COLOR : Color.RED);
            card.add(Box.createVerticalStrut(10));
            card.add(yourAnswer);

            JLabel correctLabel = new JLabel("Correct Answer: " + correctAnswer);
            correctLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            correctLabel.setForeground(PRIMARY_COLOR);
            card.add(correctLabel);

            JLabel marks = new JLabel("Marks: " + (isCorrect ? q.getMarks() : 0));
            marks.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            marks.setForeground(TEXT_COLOR);
            card.add(marks);

            resultsPanel.add(card);
            resultsPanel.add(Box.createVerticalStrut(15));
            questionNumber++;
        }

        JScrollPane scrollPane = new JScrollPane(resultsPanel);
        scrollPane.setBorder(null);
        resultPanel.add(scrollPane, BorderLayout.CENTER);

        // Footer
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBackground(CARD_COLOR);
        footerPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        String grade = calculateGrade(percentageScore);
        JLabel gradeLabel = new JLabel(String.format("Score: %d/%d (%.1f%%) - Grade: %s", score, totalMarks, percentageScore, grade));
        gradeLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        gradeLabel.setForeground(getGradeColor(grade));
        footerPanel.add(gradeLabel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(CARD_COLOR);
        JButton backButton = createButton("Back to Main", SECONDARY_COLOR);
        backButton.addActionListener(e -> cardLayout.show(cardsPanel, "MAIN"));
        JButton meritButton = createButton("View Merit List", PRIMARY_COLOR);
        meritButton.addActionListener(e -> cardLayout.show(cardsPanel, "MERIT_LIST"));
        buttonPanel.add(backButton);
        buttonPanel.add(meritButton);
        footerPanel.add(buttonPanel, BorderLayout.SOUTH);
        resultPanel.add(footerPanel, BorderLayout.SOUTH);

        resultPanel.revalidate();
        resultPanel.repaint();
        cardLayout.show(cardsPanel, "RESULT");
    }

    private void createMeritListPanel() {
        meritListPanel = new JPanel(new BorderLayout());
        meritListPanel.setBackground(BACKGROUND_COLOR);
        meritListPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        JLabel lblHeader = new JLabel("MCQ Exam Merit List");
        lblHeader.setForeground(Color.WHITE);
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblHeader.setHorizontalAlignment(JLabel.CENTER);
        headerPanel.add(lblHeader, BorderLayout.CENTER);

        JButton backButton = createButton("Back to Main", SECONDARY_COLOR);
        backButton.addActionListener(e -> cardLayout.show(cardsPanel, "MAIN"));
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(PRIMARY_COLOR);
        buttonPanel.add(backButton);
        headerPanel.add(buttonPanel, BorderLayout.EAST);
        meritListPanel.add(headerPanel, BorderLayout.NORTH);

        // Content panel for exam selector and merit list
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(BACKGROUND_COLOR);
        meritListPanel.add(contentPanel, BorderLayout.CENTER);

        // Exam selector
        JPanel examSelectorPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        examSelectorPanel.setBackground(BACKGROUND_COLOR);
        examSelectorPanel.add(new JLabel("Select Exam: "));
        JComboBox<String> examComboBox = new JComboBox<>();
        Map<String, Integer> examMap = new HashMap<>();

        try {
            String query = "SELECT DISTINCT e.id, e.exam_code FROM exams e JOIN student_scores ss ON e.id = ss.exam_id";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                String examCode = rs.getString("exam_code");
                int examId = rs.getInt("id");
                examComboBox.addItem(examCode);
                examMap.put(examCode, examId);
            }

            if (examComboBox.getItemCount() > 0) {
                examComboBox.addActionListener(e -> {
                    String selectedExamCode = (String) examComboBox.getSelectedItem();
                    if (selectedExamCode != null) {
                        loadMeritListData(examMap.get(selectedExamCode));
                    }
                });
                String firstExam = (String) examComboBox.getItemAt(0);
                // Initialize meritTablePanel and load initial data
                meritTablePanel = new JPanel(new BorderLayout());
                contentPanel.add(meritTablePanel, BorderLayout.CENTER);
                loadMeritListData(examMap.get(firstExam));
            } else {
                JLabel noDataLabel = new JLabel("No exam results available.");
                noDataLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
                contentPanel.add(noDataLabel, BorderLayout.CENTER);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading exams: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        examSelectorPanel.add(examComboBox);
        contentPanel.add(examSelectorPanel, BorderLayout.NORTH);
    }


    private void loadMeritListData(int selectedExamId) {
        try {
            // Clear existing content in meritTablePanel
            meritTablePanel.removeAll();

            String query = "SELECT * FROM mcqMeritList WHERE ExamID = ? ORDER BY Rnk";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setInt(1, selectedExamId);
            ResultSet rs = pstmt.executeQuery();

            // Create table model with custom rendering
            DefaultTableModel tableModel = new DefaultTableModel(
                    new Object[]{"🏆 Rank", "🆔 Student ID", "📝 Reg. No.", "👤 Student Name", "📊 Score", "📈 Percentage", "⏰ Submission Time"}, 0
            ) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false; // Make table non-editable
                }
            };

            while (rs.next()) {
                int rank = rs.getInt("Rnk");
                int studentId = rs.getInt("StudentID");
                String regNo = rs.getString("RegistrationNumber");
                String examineeName = rs.getString("StudentName");
                int score = rs.getInt("Score");
                int totalMarks = rs.getInt("TotalMarks");
                double percentage = rs.getDouble("Percentage");
                Timestamp submissionTime = rs.getTimestamp("SubmissionTime");

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                tableModel.addRow(new Object[]{
                        rank, studentId, regNo, examineeName,
                        score + " / " + totalMarks,
                        String.format("%.1f%%", percentage),
                        dateFormat.format(submissionTime)
                });
            }

            if (tableModel.getRowCount() == 0) {
                JLabel noDataLabel = new JLabel("No results available for this exam.");
                noDataLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
                noDataLabel.setHorizontalAlignment(JLabel.CENTER);
                noDataLabel.setForeground(PRIMARY_COLOR);
                meritTablePanel.add(noDataLabel, BorderLayout.CENTER);
            } else {
                // Create table with custom styling
                JTable meritTable = new JTable(tableModel) {
                    @Override
                    public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                        Component comp = super.prepareRenderer(renderer, row, column);
                        if (!isRowSelected(row)) {
                            comp.setBackground(row % 2 == 0 ? CARD_COLOR : new Color(240, 240, 255));
                        }
                        if (getValueAt(row, 1).equals(studentId)) {
                            comp.setBackground(new Color(173, 216, 230, 150));
                        }
                        return comp;
                    }
                };

                // Table styling
                meritTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                meritTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 16));
                meritTable.getTableHeader().setBackground(PRIMARY_COLOR);
                meritTable.getTableHeader().setForeground(Color.WHITE);
                meritTable.setRowHeight(30);
                meritTable.setShowGrid(false);
                meritTable.setIntercellSpacing(new Dimension(0, 0));
                meritTable.setSelectionBackground(new Color(173, 216, 230, 200));

                // Custom column alignment and width
                DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
                centerRenderer.setHorizontalAlignment(JLabel.CENTER);
                for (int i = 0; i < meritTable.getColumnCount(); i++) {
                    meritTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
                }

                meritTable.getColumnModel().getColumn(0).setPreferredWidth(50);
                meritTable.getColumnModel().getColumn(1).setPreferredWidth(80);
                meritTable.getColumnModel().getColumn(2).setPreferredWidth(100);
                meritTable.getColumnModel().getColumn(3).setPreferredWidth(200);
                meritTable.getColumnModel().getColumn(4).setPreferredWidth(100);
                meritTable.getColumnModel().getColumn(5).setPreferredWidth(100);
                meritTable.getColumnModel().getColumn(6).setPreferredWidth(150);

                // Add scrollpane with styled border
                JScrollPane tableScrollPane = new JScrollPane(meritTable);
                tableScrollPane.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createEmptyBorder(10, 10, 10, 10),
                        BorderFactory.createLineBorder(PRIMARY_COLOR, 2)
                ));

                meritTablePanel.add(tableScrollPane, BorderLayout.CENTER);
            }

            meritTablePanel.revalidate();
            meritTablePanel.repaint();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error loading merit list data: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private JPanel createStatsPanel(DefaultTableModel tableModel) {
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        statsPanel.setBackground(BACKGROUND_COLOR);
        statsPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, PRIMARY_COLOR));

        double totalPercentage = 0;
        int rows = tableModel.getRowCount();

        for (int i = 0; i < rows; i++) {
            String percentageStr = tableModel.getValueAt(i, 5).toString().replace("%", "");
            totalPercentage += Double.parseDouble(percentageStr);
        }

        double averagePercentage = rows > 0 ? totalPercentage / rows : 0;

        JLabel statsLabel = new JLabel(String.format(
                "Total Students: %d | Average Score: %.1f%%",
                rows, averagePercentage
        ));
        statsLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        statsLabel.setForeground(PRIMARY_COLOR);

        statsPanel.add(statsLabel);

        return statsPanel;
    }

    private JButton createButton(String text, Color bgColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 15, 15));
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 16));
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(12, 25, 12, 25));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.brighter());
            }
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });
        return button;
    }

    private Color getGradeColor(String grade) {
        switch (grade) {
            case "A": return SECONDARY_COLOR;
            case "B": return new Color(0, 128, 0);
            case "C": return ACCENT_COLOR;
            case "D": return new Color(255, 165, 0);
            case "F": return Color.RED;
            default: return TEXT_COLOR;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new StudentExamUI(1, "n"));

    }
}