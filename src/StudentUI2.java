import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.net.URI;
import java.net.URLEncoder;
import java.sql.*;
import java.util.List;
import java.util.Timer;
import java.awt.Color;
import java.util.*;
import java.util.concurrent.TimeUnit;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.text.SimpleDateFormat;
import org.apache.commons.text.similarity.CosineSimilarity;
import org.apache.commons.text.similarity.JaccardSimilarity;
import org.apache.commons.text.similarity.LevenshteinDistance;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;


public class StudentUI2 extends JFrame {
    private static final Color SECONDARY_COLOR = new Color(76, 175, 80);   // Fresh Green (Navigation, Success)
    private static final Color ACCENT_COLOR = new Color(255, 193, 7);      // Warm Amber (Submit, Highlights)
    private static final Color BACKGROUND_COLOR = new Color(245, 245, 250); // Soft Gray-Blue (Background)
    private static final Color CARD_COLOR = new Color(255, 255, 255);      // Crisp White (Cards, Panels)
    private static final Color TEXT_COLOR = new Color(33, 33, 33);
    private static final Color PRIMARY_COLOR = new Color(33, 150, 243);
    // Database connection
    private Connection connection;

    // UI Components
    private JPanel mainPanel, examPanel, resultPanel, meritListPanel;
    private JTextField txtExamCode;
    private JButton btnStartExam, btnSubmitExam, btnViewMeritList;
    private JLabel lblTimer, lblStudentInfo;
    private CardLayout cardLayout;
    private JPanel cardsPanel;

    // Exam data
    private int examId;
    private int studentId;
    private String studentName;
    private int timerMinutes;
    private List<WrittenQuestion> questions;
    private Map<Integer, JTextArea> answerTextAreas;
    private Timer examTimer;
    private int secondsRemaining;
    private boolean examSubmitted = false;
    private Clip currentClip;
    private List<Clip> activeClips = new ArrayList<>();

    // For navigation between questions
    private JPanel questionNavPanel;
    private JButton prevButton, nextButton;
    private int currentQuestionIndex = 0;
    private JLabel questionCountLabel;
    private JPanel[] questionPanels;

    // Scheduling service for color changing timer
    private ScheduledExecutorService scheduler;

    public StudentUI2(int studentId, String studentName) {
        this.studentId = studentId;
        this.studentName = studentName;
        try {
            // Set cross-platform Look and Feel for consistency
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        setTitle("Online Examination System - Written Exam");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Initialize database connection
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/eah22", "root", "MySQL@24");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database connection failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            System.exit(1);
        }

        // Initialize UI
        questions = new ArrayList<>();
        answerTextAreas = new HashMap<>();

        // Set up card layout for switching between different views
        cardLayout = new CardLayout();
        cardsPanel = new JPanel(cardLayout);

        // Create panels
        createMainPanel();
        createExamPanel();
        createResultPanel();
        createMeritListPanel();

        // Add panels to card layout
        cardsPanel.add(mainPanel, "MAIN");
        cardsPanel.add(examPanel, "EXAM");
        cardsPanel.add(resultPanel, "RESULT");
        cardsPanel.add(meritListPanel, "MERIT_LIST");

        add(cardsPanel);

        // Start with main panel
        cardLayout.show(cardsPanel, "MAIN");
        setVisible(true);
    }

    private void createMainPanel() {
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(new Color(240, 248, 255));

        // Header panel with student info
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(70, 130, 180));
        headerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        lblStudentInfo = new JLabel("Student: " + studentName + " (ID: " + studentId + ")");
        lblStudentInfo.setForeground(Color.WHITE);
        lblStudentInfo.setFont(new Font("Arial", Font.BOLD, 16));
        headerPanel.add(lblStudentInfo, BorderLayout.WEST);

        // Exam code entry panel
        JPanel examCodePanel = new JPanel();
        examCodePanel.setBackground(new Color(240, 248, 255));
        examCodePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(70, 130, 180), 2),
                "Enter Written Exam Code",
                0, 0, new Font("Arial", Font.BOLD, 16), new Color(70, 130, 180)
        ));

        examCodePanel.add(new JLabel("Written Exam Code:"));
        txtExamCode = new JTextField(15);
        txtExamCode.setFont(new Font("Arial", Font.PLAIN, 14));
        examCodePanel.add(txtExamCode);

        btnStartExam = createButton("Start Written Exam", e -> validateAndStartExam(), new Color(46, 139, 87), Color.WHITE);
        examCodePanel.add(btnStartExam);

        // View Merit List button
        btnViewMeritList = createButton("View Merit List", e -> showMeritList(), new Color(75, 0, 130), Color.WHITE);
        examCodePanel.add(btnViewMeritList);

        // Put together main panel
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(examCodePanel, BorderLayout.CENTER);

        // Instructions panel
        JPanel instructionsPanel = new JPanel(new BorderLayout());
        instructionsPanel.setBackground(new Color(240, 248, 255));
        instructionsPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(70, 130, 180), 2),
                "Written Exam Instructions",
                0, 0, new Font("Arial", Font.BOLD, 16), new Color(70, 130, 180)
        ));

        JTextArea instructionsText = new JTextArea(
                "1. Enter the exam code provided by your examiner.\n" +
                        "2. Once you start the exam, a timer will begin counting down.\n" +
                        "3. Answer all written questions in the provided text areas.\n" +
                        "4. Your answers will be automatically evaluated based on similarity to the expected answers.\n" +
                        "5. You can navigate between questions using the Previous and Next buttons.\n" +
                        "6. Submit your exam before the timer expires.\n" +
                        "7. After submission, you can view your results and check the merit list."
        );
        instructionsText.setEditable(false);
        instructionsText.setBackground(new Color(240, 248, 255));
        instructionsText.setFont(new Font("Arial", Font.PLAIN, 14));
        instructionsText.setLineWrap(true);
        instructionsText.setWrapStyleWord(true);
        instructionsText.setBorder(new EmptyBorder(10, 10, 10, 10));

        instructionsPanel.add(instructionsText, BorderLayout.CENTER);
        mainPanel.add(instructionsPanel, BorderLayout.SOUTH);
    }



    private void createExamPanel() {
        examPanel = new JPanel(new BorderLayout());
        examPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        examPanel.setBackground(new Color(245, 245, 245));

        // Timer panel
        JPanel timerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        timerPanel.setBackground(new Color(70, 130, 180));
        timerPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

        lblTimer = new JLabel("Time Remaining: 00:00:00");
        lblTimer.setForeground(Color.WHITE);
        lblTimer.setFont(new Font("Arial", Font.BOLD, 18));
        timerPanel.add(lblTimer);

        examPanel.add(timerPanel, BorderLayout.NORTH);

        // Question navigation panel
        questionNavPanel = new JPanel(new BorderLayout());
        questionNavPanel.setBackground(new Color(245, 245, 245));

        JPanel navButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        navButtonsPanel.setBackground(new Color(245, 245, 245));

        prevButton = createButton("Previous", e -> navigateQuestions(-1), new Color(70, 130, 180), Color.WHITE);
        nextButton = createButton("Next", e -> navigateQuestions(1), new Color(70, 130, 180), Color.WHITE);
        questionCountLabel = new JLabel("Question 1 of 1");
        questionCountLabel.setFont(new Font("Arial", Font.BOLD, 14));

        navButtonsPanel.add(prevButton);
        navButtonsPanel.add(questionCountLabel);
        navButtonsPanel.add(nextButton);

        questionNavPanel.add(navButtonsPanel, BorderLayout.SOUTH);

        // Submit button
        JPanel submitPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        submitPanel.setBackground(new Color(245, 245, 245));

        btnSubmitExam = createButton("Submit Exam", e -> submitExam(), new Color(178, 34, 34), Color.WHITE);
        submitPanel.add(btnSubmitExam);

        examPanel.add(questionNavPanel, BorderLayout.CENTER);
        examPanel.add(submitPanel, BorderLayout.SOUTH);
    }

    private void createResultPanel() {
        resultPanel = new JPanel(new BorderLayout());
        resultPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        resultPanel.setBackground(new Color(240, 248, 255));

        // Will be populated when showing results
    }

    private void createMeritListPanel() {
        meritListPanel = new JPanel(new BorderLayout());
        meritListPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        meritListPanel.setBackground(new Color(240, 248, 255));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(70, 130, 180));
        headerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel lblHeader = new JLabel("Written Exam Merit List");
        lblHeader.setForeground(Color.WHITE);
        lblHeader.setFont(new Font("Arial", Font.BOLD, 18));
        lblHeader.setHorizontalAlignment(JLabel.CENTER);
        headerPanel.add(lblHeader, BorderLayout.CENTER);

        JButton backButton = createButton("Back to Main", e -> cardLayout.show(cardsPanel, "MAIN"), new Color(70, 130, 180), Color.WHITE);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(new Color(70, 130, 180));
        buttonPanel.add(backButton);
        headerPanel.add(buttonPanel, BorderLayout.EAST);

        meritListPanel.add(headerPanel, BorderLayout.NORTH);

        // Content will be populated when showing merit list
    }

    private JButton createButton(String text, ActionListener action, Color bgColor, Color fgColor) {
        JButton button = new JButton(text);
        button.addActionListener(action);
        button.setBackground(bgColor);
        button.setForeground(fgColor);
        button.setFocusPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        return button;
    }




    private void validateAndStartExam() {
        String examCode = txtExamCode.getText().trim().toUpperCase(); // Standardize input
        if (examCode.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter an exam code.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            String query = "SELECT * FROM written_exams WHERE exam_code = ?";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setString(1, examCode);

            System.out.println("Executing query: SELECT * FROM written_exams WHERE exam_code = '" + examCode + "'");
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                examId = rs.getInt("id");
                timerMinutes = rs.getInt("timer_minutes");

                // Check if student has already taken this exam
                String checkAttemptQuery = "SELECT * FROM written_results WHERE exam_id = ? AND student_id = ?";
                PreparedStatement checkStmt = connection.prepareStatement(checkAttemptQuery);
                checkStmt.setInt(1, examId);
                checkStmt.setInt(2, studentId);
                ResultSet attemptRs = checkStmt.executeQuery();

                if (attemptRs.next()) {
                    JOptionPane.showMessageDialog(this, "You have already taken this exam.", "Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Load questions and start exam
                loadQuestions();
                startExam();
                cardLayout.show(cardsPanel, "EXAM");
            } else {
                JOptionPane.showMessageDialog(this, "Invalid exam code. Please check and try again.", "Error", JOptionPane.WARNING_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error accessing exam: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void loadQuestions() throws SQLException {
        questions.clear();
        answerTextAreas.clear();

        // Include 'id' in the query
        String query = "SELECT id, exam_id, question_text, possible_answers, marks, explanation, photo_path, explanation_photo_path FROM written_questions WHERE exam_id= ?";
        PreparedStatement pstmt = connection.prepareStatement(query);
        pstmt.setInt(1, examId);
        ResultSet rs = pstmt.executeQuery();

        while (rs.next()) {
            WrittenQuestion question = new WrittenQuestion(
                    rs.getInt("id"), // Use the question's ID here
                    rs.getInt("exam_id"),
                    rs.getString("question_text"),
                    rs.getString("possible_answers"),
                    rs.getInt("marks"),
                    rs.getString("explanation"),
                    rs.getString("photo_path"),
                    rs.getString("explanation_photo_path")
            );
            questions.add(question);
        }

        // Create question panels
        createQuestionPanels();
    }

    private void createQuestionPanels() {
        // Remove existing components
        questionNavPanel.removeAll();

        // Create panel to hold all question panels
        JPanel questionsContainer = new JPanel(new CardLayout());
        questionsContainer.setBackground(new Color(245, 245, 245));

        questionPanels = new JPanel[questions.size()];

        for (int i = 0; i < questions.size(); i++) {
            WrittenQuestion question = questions.get(i);

            JPanel questionPanel = new JPanel(new BorderLayout());
            questionPanel.setBackground(Color.WHITE);
            questionPanel.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));

            // Create header with question number and marks
            JPanel headerPanel = new JPanel(new BorderLayout());
            headerPanel.setBackground(new Color(240, 240, 240));
            headerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

            JLabel questionNumLabel = new JLabel("Question " + (i + 1));
            questionNumLabel.setFont(new Font("Arial", Font.BOLD, 16));
            headerPanel.add(questionNumLabel, BorderLayout.WEST);

            JLabel marksLabel = new JLabel("Marks: " + question.getMarks());
            marksLabel.setFont(new Font("Arial", Font.BOLD, 16));
            headerPanel.add(marksLabel, BorderLayout.EAST);

            // Create question text area
            JTextArea questionTextArea = new JTextArea(question.getQuestionText());
            questionTextArea.setEditable(false);
            questionTextArea.setLineWrap(true);
            questionTextArea.setWrapStyleWord(true);
            questionTextArea.setFont(new Font("Arial", Font.PLAIN, 14));
            questionTextArea.setBorder(new EmptyBorder(10, 10, 10, 10));
            questionTextArea.setBackground(Color.WHITE);

            // Create student answer area
            JTextArea answerArea = new JTextArea(10, 30);
            answerArea.setLineWrap(true);
            answerArea.setWrapStyleWord(true);
            answerArea.setFont(new Font("Arial", Font.PLAIN, 14));
            answerArea.setBorder(BorderFactory.createTitledBorder("Your Answer"));

            // Store answer area for retrieval later
            answerTextAreas.put(question.getId(), answerArea);



            // If there's a photo, display it
            JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
            contentPanel.setBackground(Color.WHITE);
            contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

            if (question.getPhotoPath() != null && !question.getPhotoPath().isEmpty()) {
                try {
                    ImageIcon icon = new ImageIcon(question.getPhotoPath());
                    // Scale the image if it's too large
                    Image img = icon.getImage();
                    Image scaledImg = img.getScaledInstance(300, -1, Image.SCALE_SMOOTH);
                    ImageIcon scaledIcon = new ImageIcon(scaledImg);

                    JLabel photoLabel = new JLabel(scaledIcon);
                    photoLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
                    photoLabel.setHorizontalAlignment(JLabel.CENTER);

                    contentPanel.add(photoLabel, BorderLayout.EAST);
                } catch (Exception e) {
                    System.err.println("Error loading image: " + e.getMessage());
                }
            }

            // Add text components to content panel
            JPanel textPanel = new JPanel(new BorderLayout(5, 10));
            textPanel.setBackground(Color.WHITE);
            textPanel.add(questionTextArea, BorderLayout.NORTH);
            textPanel.add(new JScrollPane(answerArea), BorderLayout.CENTER);


            contentPanel.add(textPanel, BorderLayout.CENTER);

            // Assemble the panel
            questionPanel.add(headerPanel, BorderLayout.NORTH);
            questionPanel.add(contentPanel, BorderLayout.CENTER);

            questionPanels[i] = questionPanel;
            questionsContainer.add(questionPanel, String.valueOf(i));
        }

        // Add container to the navigation panel
        questionNavPanel.add(questionsContainer, BorderLayout.CENTER);

        // Navigation buttons panel
        JPanel navButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        navButtonsPanel.setBackground(new Color(245, 245, 245));

        prevButton = createButton("Previous", e -> navigateQuestions(-1), new Color(70, 130, 180), Color.WHITE);
        nextButton = createButton("Next", e -> navigateQuestions(1), new Color(70, 130, 180), Color.WHITE);
        questionCountLabel = new JLabel("Question 1 of " + questions.size());
        questionCountLabel.setFont(new Font("Arial", Font.BOLD, 14));

        navButtonsPanel.add(prevButton);
        navButtonsPanel.add(questionCountLabel);
        navButtonsPanel.add(nextButton);

        questionNavPanel.add(navButtonsPanel, BorderLayout.SOUTH);

        // Set up card layout for questions
        ((CardLayout) questionsContainer.getLayout()).show(questionsContainer, "0");
        currentQuestionIndex = 0;

        // Update navigation button states
        updateNavigationButtons();

        // Revalidate and repaint
        questionNavPanel.revalidate();
        questionNavPanel.repaint();
    }

    private void navigateQuestions(int direction) {
        int newIndex = currentQuestionIndex + direction;

        if (newIndex >= 0 && newIndex < questions.size()) {
            currentQuestionIndex = newIndex;
            ((CardLayout) ((JPanel) questionNavPanel.getComponent(0)).getLayout()).show(
                    (JPanel) questionNavPanel.getComponent(0), String.valueOf(currentQuestionIndex));

            questionCountLabel.setText("Question " + (currentQuestionIndex + 1) + " of " + questions.size());
            updateNavigationButtons();
        }
    }

    private void updateNavigationButtons() {
        prevButton.setEnabled(currentQuestionIndex > 0);
        nextButton.setEnabled(currentQuestionIndex < questions.size() - 1);
    }
    private void stopAllClips() {
        // Create a copy of the activeClips list
        List<Clip> clipsToStop = new ArrayList<>(activeClips);
        for (Clip clip : clipsToStop) {
            if (clip != null) {
                if (clip.isRunning()) {
                    clip.stop(); // Safe to call, as we're iterating over a copy
                }
                clip.close(); // Free audio resources
            }
        }
        activeClips.clear(); // Clear the original list after stopping all clips
    }

    private void startExam() {
        secondsRemaining = timerMinutes * 60;
        updateTimerDisplay();

        // Set up color-changing timer
        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            if (secondsRemaining > 0 && !examSubmitted) {
                secondsRemaining--;
                SwingUtilities.invokeLater(() -> {
                    updateTimerDisplay();
                    updateTimerColor();
                    if(secondsRemaining<10) {
                        try {
                            AudioInputStream audioIn = AudioSystem.getAudioInputStream(new File("D:\\Project22\\ExamaHead22\\src\\tic-tac-27828.wav"));
                            Clip clip = AudioSystem.getClip();
                            clip.open(audioIn);
                            // Add listener to remove clip when it stops naturally
                            clip.addLineListener(event -> {
                                if (event.getType() == LineEvent.Type.STOP) {
                                    activeClips.remove(clip);
                                }
                            });
                            clip.start();
                            activeClips.add(clip);
                        }


                       catch (Exception e) {
                        e.printStackTrace();
                    }
                    }
                });
            } else if (!examSubmitted) {
                stopAllClips();
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "Time's up! Exam is being submitted automatically.", "Time Up", JOptionPane.INFORMATION_MESSAGE);
                    submitExam();
                });
                scheduler.shutdown();
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    private void updateTimerDisplay() {
        int hours = secondsRemaining / 3600;
        int mins = (secondsRemaining % 3600) / 60;
        int secs = secondsRemaining % 60;

        String timeString = String.format("Time Remaining: %02d:%02d:%02d", hours, mins, secs);
        lblTimer.setText(timeString);
    }

    private void updateTimerColor() {
        // Change color based on remaining time
        float percentRemaining = (float) secondsRemaining / (timerMinutes * 60);

        if (percentRemaining > 0.5) {
            // Green when more than 50% time remains
            lblTimer.setForeground(new Color(0, 255, 0));
        } else if (percentRemaining > 0.25) {
            // Yellow when between 25% and 50% time remains
            lblTimer.setForeground(new Color(255, 255, 0));
        } else if (percentRemaining > 0.1) {
            // Orange when between 10% and 25% time remains
            lblTimer.setForeground(new Color(255, 165, 0));
        } else {
            // Red when less than 10% time remains
            lblTimer.setForeground(new Color(255, 0, 0));
        }
    }

    private void submitExam() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to submit your exam? You cannot change your answers after submission.",
                "Confirm Submission", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            examSubmitted = true;

            if (scheduler != null) {
                scheduler.shutdown();
            }

            // Stop all active clips
            stopAllClips();


            // Grade the exam using NLP similarity algorithms
            Map<Integer, WrittenAnswer> answers = new HashMap<>();
            for (WrittenQuestion question : questions) {
                JTextArea answerArea = answerTextAreas.get(question.getId());
                String studentAnswer = answerArea.getText().trim();

                // Calculate similarity score (between 0 and 1)
                double similarityScore = calculateSimilarityScore(studentAnswer, question.getPossibleAnswers());

                // Calculate awarded marks (based on similarity percentage)
                int awardedMarks = (int) Math.round(similarityScore * question.getMarks());

                WrittenAnswer answer = new WrittenAnswer(
                        question.getId(), studentAnswer, similarityScore, awardedMarks, question.getMarks()
                );
                answers.put(question.getId(), answer);
            }

            // Save results to database
            try {
                saveExamResults(answers);
                showResults(answers);
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error saving exam results: " + e.getMessage(),
                        "Database Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    private double calculateSimilarityScore(String studentAnswer, String correctAnswer) {
        if (studentAnswer.isEmpty()) {
            return 0.0;
        }

        // We'll use a combination of several similarity metrics for better accuracy

        // 1. Jaccard Similarity (based on word overlap)
        JaccardSimilarity jaccardSimilarity = new JaccardSimilarity();
        double jaccardScore = jaccardSimilarity.apply(studentAnswer.toLowerCase(), correctAnswer.toLowerCase());

        // 2. Cosine Similarity (based on term frequency)
        CosineSimilarity cosineSimilarity = new CosineSimilarity();
        Map<CharSequence, Integer> studentVector = getTermFrequencyMap(studentAnswer.toLowerCase());
        Map<CharSequence, Integer> correctVector = getTermFrequencyMap(correctAnswer.toLowerCase());
        double cosineScore = cosineSimilarity.cosineSimilarity(studentVector, correctVector);

        // 3. Inverse Levenshtein Distance (for character-level similarity)
        LevenshteinDistance levenshteinDistance = new LevenshteinDistance();
        int distance = levenshteinDistance.apply(studentAnswer.toLowerCase(), correctAnswer.toLowerCase());
        int maxLength = Math.max(studentAnswer.length(), correctAnswer.length());
        double levenshteinScore = 1.0 - ((double) distance / maxLength);

        // Weighted average of different similarity metrics
        double weightedScore = (jaccardScore * 0.4) + (cosineScore * 0.4) + (levenshteinScore * 0.2);

        // Ensure score is between 0 and 1
        return Math.max(0.0, Math.min(1.0, weightedScore));
    }

    private Map<CharSequence, Integer> getTermFrequencyMap(String text) {
        Map<CharSequence, Integer> termFrequencyMap = new HashMap<>();
        String[] words = text.split("\\s+");

        for (String word : words) {
            if (word.length() > 0) {
                termFrequencyMap.put(word, termFrequencyMap.getOrDefault(word, 0) + 1);
            }
        }

        return termFrequencyMap;
    }

    private void saveExamResults(Map<Integer, WrittenAnswer> answers) throws SQLException {
        // Calculate total score
        int totalScore = 0;
        int totalPossible = 0;

        for (WrittenAnswer answer : answers.values()) {
            totalScore += answer.getAwardedMarks();
            totalPossible += answer.getMaxMarks();
        }

        double percentageScore = (double) totalScore / totalPossible * 100;

        // Begin transaction
        connection.setAutoCommit(false);

        try {
            // 1. Insert into WrittenResults
            String resultQuery = "INSERT INTO written_results (exam_id, student_id, score, total_marks, submission_time) VALUES (?, ?, ?, ?, NOW())";
            PreparedStatement resultStmt = connection.prepareStatement(resultQuery, Statement.RETURN_GENERATED_KEYS);
            resultStmt.setInt(1, examId);
            resultStmt.setInt(2, studentId);
            resultStmt.setInt(3, totalScore);
            resultStmt.setInt(4, totalPossible);
            resultStmt.executeUpdate();

            ResultSet resultKeys = resultStmt.getGeneratedKeys();



            int resultId = -1;
            if (resultKeys.next()) {
                resultId = resultKeys.getInt(1);
            } else {
                throw new SQLException("Failed to get result ID after insertion");
            }

            String answerQuery = "INSERT INTO written_answers (result_id, question_id, student_answer, similarity_score, awarded_marks) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement answerStmt = connection.prepareStatement(answerQuery);

            for (WrittenAnswer answer : answers.values()) {
                answerStmt.setInt(1, resultId);
                answerStmt.setInt(2, answer.getQuestionId());
                answerStmt.setString(3, answer.getStudentAnswer());
                answerStmt.setDouble(4, answer.getSimilarityScore());
                answerStmt.setInt(5, answer.getAwardedMarks());
                answerStmt.addBatch();
            }


            answerStmt.executeBatch();

            // Commit transaction
            connection.commit();
        } catch (SQLException e) {
            // Rollback on error
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    private void showResults(Map<Integer, WrittenAnswer> answers) {
        // Clear previous content
        resultPanel.removeAll();

        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(70, 130, 180));
        headerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel resultHeader = new JLabel("Written Exam Results");
        resultHeader.setForeground(Color.WHITE);
        resultHeader.setFont(new Font("Arial", Font.BOLD, 18));
        resultHeader.setHorizontalAlignment(JLabel.CENTER);
        headerPanel.add(resultHeader, BorderLayout.CENTER);

        // Calculate total score
        int totalScore = 0;
        int totalPossible = 0;

        for (WrittenAnswer answer : answers.values()) {
            totalScore += answer.getAwardedMarks();
            totalPossible += answer.getMaxMarks();
        }

        double percentageScore = (double) totalScore / totalPossible * 100;

        // Summary panel
        JPanel summaryPanel = new JPanel(new GridLayout(3, 1));
        summaryPanel.setBackground(new Color(240, 248, 255));
        summaryPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(70, 130, 180), 2),
                "Exam Summary",
                0, 0, new Font("Arial", Font.BOLD, 16), new Color(70, 130, 180)
        ));

        JLabel scoreLabel = new JLabel("Your Score: " + totalScore + " out of " + totalPossible);
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 16));

        JLabel percentageLabel = new JLabel(String.format("Percentage: %.1f%%", percentageScore));
        percentageLabel.setFont(new Font("Arial", Font.BOLD, 16));

        String grade = getGrade(percentageScore);
        JLabel gradeLabel = new JLabel("Grade: " + grade);
        gradeLabel.setFont(new Font("Arial", Font.BOLD, 16));

        summaryPanel.add(scoreLabel);
        summaryPanel.add(percentageLabel);
        summaryPanel.add(gradeLabel);

        // Results table with explanation and photo
        DefaultTableModel tableModel = new DefaultTableModel(
                new Object[]{"Question", "Your Answer", "Correct Answer", "Explanation", "Explanation Photo", "Max Marks", "Similarity Score", "Awarded Marks"}, 0
        );

        for (WrittenQuestion question : questions) {
            WrittenAnswer answer = answers.get(question.getId());
            String explanation = question.getExplanation() != null ? question.getExplanation() : "No explanation provided";
            ImageIcon explanationPhoto = null;

            // Load explanation photo if available
            if (question.getExplanationPhotoPath() != null && !question.getExplanationPhotoPath().isEmpty()) {
                try {
                    explanationPhoto = new ImageIcon(question.getExplanationPhotoPath());
                    Image img = explanationPhoto.getImage();
                    Image scaledImg = img.getScaledInstance(100, 100, Image.SCALE_SMOOTH); // Scale image to fit table
                    explanationPhoto = new ImageIcon(scaledImg);
                } catch (Exception e) {
                    System.err.println("Error loading explanation photo: " + e.getMessage());
                    explanationPhoto = null;
                }
            }

            tableModel.addRow(new Object[]{
                    question.getQuestionText().length() > 50 ? question.getQuestionText().substring(0, 50) + "..." : question.getQuestionText(),
                    answer.getStudentAnswer().length() > 50 ? answer.getStudentAnswer().substring(0, 50) + "..." : answer.getStudentAnswer(),
                    question.getPossibleAnswers().length() > 50 ? question.getPossibleAnswers().substring(0, 50) + "..." : question.getPossibleAnswers(),
                    explanation.length() > 50 ? explanation.substring(0, 50) + "..." : explanation,
                    explanationPhoto, // ImageIcon for the photo
                    answer.getMaxMarks(),
                    String.format("%.1f%%", answer.getSimilarityScore() * 100),
                    answer.getAwardedMarks()
            });
        }

        JTable resultsTable = new JTable(tableModel);
        resultsTable.setFillsViewportHeight(true);
        resultsTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        resultsTable.setRowHeight(120); // Increase row height to accommodate images

        // Custom renderer for the Explanation Photo column
        resultsTable.getColumnModel().getColumn(4).setCellRenderer(new ImageRenderer());

        JScrollPane tableScrollPane = new JScrollPane(resultsTable);
        tableScrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(70, 130, 180), 2),
                "Detailed Results",
                0, 0, new Font("Arial", Font.BOLD, 16), new Color(70, 130, 180)
        ));

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(new Color(240, 248, 255));

        JButton viewMeritListButton = createButton("View Merit List", e -> showMeritList(),
                new Color(75, 0, 130), Color.WHITE);
        JButton mainMenuButton = createButton("Back to Main Menu", e -> cardLayout.show(cardsPanel, "MAIN"),
                new Color(70, 130, 180), Color.WHITE);

        buttonPanel.add(viewMeritListButton);
        buttonPanel.add(mainMenuButton);

        // Add panels to result panel
        resultPanel.add(headerPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(new Color(240, 248, 255));
        centerPanel.add(summaryPanel, BorderLayout.NORTH);
        centerPanel.add(tableScrollPane, BorderLayout.CENTER);

        resultPanel.add(centerPanel, BorderLayout.CENTER);
        resultPanel.add(buttonPanel, BorderLayout.SOUTH);

        resultPanel.revalidate();
        resultPanel.repaint();

        // Show result panel
        cardLayout.show(cardsPanel, "RESULT");
    }
    class ImageRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            if (value instanceof ImageIcon) {
                JLabel label = new JLabel((ImageIcon) value);
                label.setHorizontalAlignment(JLabel.CENTER);
                label.setVerticalAlignment(JLabel.CENTER);
                return label;
            } else {
                JLabel label = new JLabel("No Image");
                label.setHorizontalAlignment(JLabel.CENTER);
                label.setVerticalAlignment(JLabel.CENTER);
                return label;
            }
        }
    }

    private String getGrade(double percentage) {
        if (percentage >= 90) return "A+ (Excellent)";
        if (percentage >= 80) return "A (Very Good)";
        if (percentage >= 70) return "B+ (Good)";
        if (percentage >= 60) return "B (Above Average)";
        if (percentage >= 50) return "C (Average)";
        if (percentage >= 40) return "D (Below Average)";
        return "F (Fail)";
    }

    private void showMeritList() {
        // Clear previous content
        SwingUtilities.invokeLater(() -> {


        meritListPanel.removeAll();


        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(70, 130, 180));
        headerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel lblHeader = new JLabel("Written Exam Merit List");
        lblHeader.setForeground(Color.WHITE);
        lblHeader.setFont(new Font("Arial", Font.BOLD, 18));
        lblHeader.setHorizontalAlignment(JLabel.CENTER);
        headerPanel.add(lblHeader, BorderLayout.CENTER);

        JButton backButton = createButton("Back to Main", e -> cardLayout.show(cardsPanel, "MAIN"),
                new Color(70, 130, 180), Color.WHITE);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(new Color(70, 130, 180));
        buttonPanel.add(backButton);
        headerPanel.add(buttonPanel, BorderLayout.EAST);

        meritListPanel.add(headerPanel, BorderLayout.NORTH);

        // Exam selector panel
        JPanel examSelectorPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        examSelectorPanel.setBackground(new Color(240, 248, 255));
        examSelectorPanel.add(new JLabel("Select Exam: "));

        JComboBox<String> examComboBox = new JComboBox<>();
        Map<String, Integer> examMap = new HashMap<>();

        try {
            // Get all exams that have results
            String query = "SELECT DISTINCT we.id, we.exam_code FROM written_exams we " +
                    "JOIN written_results wr ON we.id = wr.exam_id";
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

                // Load initial merit list data
                String firstExam = (String) examComboBox.getItemAt(0);
                loadMeritListData(examMap.get(firstExam));
            } else {
                JLabel noExamsLabel = new JLabel("No exam results available");
                noExamsLabel.setFont(new Font("Arial", Font.BOLD, 16));
                noExamsLabel.setHorizontalAlignment(JLabel.CENTER);
                meritListPanel.add(noExamsLabel, BorderLayout.CENTER);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading exams: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }

        examSelectorPanel.add(examComboBox);
        meritListPanel.add(examSelectorPanel, BorderLayout.NORTH, 1); // Add below header

        // Show the merit list panel
        cardLayout.show(cardsPanel, "MERIT_LIST");

    });};

    private void loadMeritListData(int selectedExamId) {
        try {
            // Remove any existing table scroll pane
            Component[] components = meritListPanel.getComponents();
            for (Component comp : components) {
                if (comp instanceof JScrollPane) {
                    meritListPanel.remove(comp);
                }
            }

            // Use the writtenMeritList view directly
            String query = "SELECT * FROM writtenMeritList WHERE ExamID = ? ORDER BY Rnk";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setInt(1, selectedExamId);
            ResultSet rs = pstmt.executeQuery();

            DefaultTableModel tableModel = new DefaultTableModel(
                    new Object[]{"Rank", "Student ID", "Registration No.", "Student Name", "Score", "Percentage", "Submission Time"}, 0);

            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                int rank = rs.getInt("Rnk");
                int studentId = rs.getInt("StudentID");
                String regNo = rs.getString("RegistrationNumber");
                String studentName = rs.getString("StudentName");
                int score = rs.getInt("Score");
                int totalMarks = rs.getInt("TotalMarks");
                double percentage = rs.getDouble("Percentage");
                Timestamp submissionTime = rs.getTimestamp("SubmissionTime");

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                tableModel.addRow(new Object[]{
                        rank,
                        studentId,
                        regNo,
                        studentName,
                        score + " / " + totalMarks,
                        String.format("%.1f%%", percentage),
                        dateFormat.format(submissionTime)
                });
            }

            if (!hasData) {
                JLabel noDataLabel = new JLabel("No results available for this exam.");
                noDataLabel.setFont(new Font("Arial", Font.BOLD, 16));
                noDataLabel.setHorizontalAlignment(JLabel.CENTER);
                meritListPanel.add(noDataLabel, BorderLayout.CENTER);
            } else {
                JTable meritTable = new JTable(tableModel);
                meritTable.setFillsViewportHeight(true);
                meritTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
                meritTable.setFont(new Font("Arial", Font.PLAIN, 14));
                meritTable.setRowHeight(25);
                meritTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

                // Highlight current student's row
                meritTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
                    @Override
                    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                                   boolean hasFocus, int row, int column) {
                        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                        int studentIdCol = 1; // Column index for student ID
                        Object studentIdObj = table.getValueAt(row, studentIdCol);
                        if (studentIdObj != null && Integer.parseInt(studentIdObj.toString()) == studentId) {
                            c.setBackground(new Color(173, 216, 230)); // Light blue background for current student
                        } else {
                            c.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
                        }
                        return c;
                    }
                });

                JScrollPane tableScrollPane = new JScrollPane(meritTable);
                tableScrollPane.setBorder(BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(70, 130, 180), 2),
                        "Merit List",
                        0, 0, new Font("Arial", Font.BOLD, 16), new Color(70, 130, 180)
                ));
                meritListPanel.add(tableScrollPane, BorderLayout.CENTER);
            }

            meritListPanel.revalidate();
            meritListPanel.repaint();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error loading merit list data: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // For testing purposes
        SwingUtilities.invokeLater(() -> new StudentUI2(1, "Test Student"));
    }


    class WrittenQuestion {
        private int id;
        private int exam_id;
        private String questionText;
        private String possibleAnswers;
        private int marks;
        private String explanation;
        private String photoPath;
        private String explanationPhotoPath;

        public WrittenQuestion(int id,int exam_id, String questionText, String possibleAnswers, int marks, String explanation, String photoPath,String explanationPhotoPath) {
            this.id = id;
            this.questionText = questionText;
            this.possibleAnswers = possibleAnswers;
            this.marks = marks;
            this.explanation = explanation;
            this.photoPath = photoPath;
            this.explanationPhotoPath = explanationPhotoPath;
        }

        public String getExplanationPhotoPath() { return explanationPhotoPath; }

        public int getId() {
            return id;
        }

        public String getQuestionText() {
            return questionText;
        }

        public String getPossibleAnswers() {
            return possibleAnswers;
        }

        public int getMarks() {
            return marks;
        }

        public String getExplanation() {
            return explanation;
        }

        public String getPhotoPath() {
            return photoPath;
        }

    }


    class WrittenAnswer {
        private int questionId;
        private String studentAnswer;
        private double similarityScore;
        private int awardedMarks;
        private int maxMarks;

        public WrittenAnswer(int questionId, String studentAnswer, double similarityScore, int awardedMarks, int maxMarks) {
            this.questionId = questionId;
            this.studentAnswer = studentAnswer;
            this.similarityScore = similarityScore;
            this.awardedMarks = awardedMarks;
            this.maxMarks = maxMarks;
        }

        public int getQuestionId() {
            return questionId;
        }

        public String getStudentAnswer() {
            return studentAnswer;
        }

        public double getSimilarityScore() {
            return similarityScore;
        }

        public int getAwardedMarks() {
            return awardedMarks;
        }

        public int getMaxMarks() {
            return maxMarks;
        }
    }
}