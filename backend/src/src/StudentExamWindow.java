import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StudentExamWindow extends JFrame {
    private JLabel lblQuestion, lblTimer;
    private JRadioButton[] options;
    private ButtonGroup optionGroup;
    private JButton btnNext, btnPrevious, btnSubmit;
    private int currentQuestionIndex = 0;
    private List<Question1> questions;
    private int[] selectedAnswers;
    private Timer timer;
    private int timeRemaining;

    public class Question1 {
        private String questionText;
        private String[] options;
        private int correctOption;
        private int marks;

        public Question1(String questionText, String option1, String option2, String option3, String option4, int correctOption, int marks) {
            this.questionText = questionText;
            this.options = new String[]{option1, option2, option3, option4};
            this.correctOption = correctOption;
            this.marks = marks;
        }

        public String getQuestionText() {
            return questionText;
        }

        public String[] getOptions() {  // **FIX: Added this method**
            return options;
        }

        public int getCorrectOption() {
            return correctOption;
        }

        public int getMarks() {
            return marks;
        }
    }

    public StudentExamWindow(String examCode) {
        setTitle("Student Exam Window");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        questions = fetchQuestionsFromDB(examCode);
        if (questions.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No questions found for this exam.", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        selectedAnswers = new int[questions.size()];
        for (int i = 0; i < selectedAnswers.length; i++) selectedAnswers[i] = -1;

        JPanel mainPanel = new JPanel(new BorderLayout());
        lblTimer = new JLabel("Time Left: ", JLabel.CENTER);
        lblTimer.setFont(new Font("Arial", Font.BOLD, 16));
        mainPanel.add(lblTimer, BorderLayout.NORTH);

        JPanel questionPanel = new JPanel(new GridLayout(6, 1));
        lblQuestion = new JLabel("", JLabel.LEFT);
        questionPanel.add(lblQuestion);

        options = new JRadioButton[4];
        optionGroup = new ButtonGroup();
        for (int i = 0; i < 4; i++) {
            options[i] = new JRadioButton();
            optionGroup.add(options[i]);
            questionPanel.add(options[i]);
        }

        mainPanel.add(questionPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        btnPrevious = new JButton("Previous");
        btnNext = new JButton("Next");
        btnSubmit = new JButton("Submit");

        btnPrevious.addActionListener(e -> moveQuestion(-1));
        btnNext.addActionListener(e -> moveQuestion(1));
        btnSubmit.addActionListener(e -> submitExam());

        buttonPanel.add(btnPrevious);
        buttonPanel.add(btnNext);
        buttonPanel.add(btnSubmit);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
        loadQuestion();
        startTimer(getExamTimeFromDB(examCode));
    }

    private void loadQuestion() {
        Question1 q = questions.get(currentQuestionIndex);
        lblQuestion.setText((currentQuestionIndex + 1) + ". " + q.getQuestionText());
        String[] opts = q.getOptions();  // **No more error**
        for (int i = 0; i < 4; i++) {
            options[i].setText(opts[i]);
            options[i].setSelected(selectedAnswers[currentQuestionIndex] == i);
        }
    }

    private void moveQuestion(int step) {
        for (int i = 0; i < 4; i++) {
            if (options[i].isSelected()) {
                selectedAnswers[currentQuestionIndex] = i;
                break;
            }
        }
        currentQuestionIndex += step;
        if (currentQuestionIndex < 0) currentQuestionIndex = 0;
        if (currentQuestionIndex >= questions.size()) currentQuestionIndex = questions.size() - 1;
        loadQuestion();
    }

    private void submitExam() {
        timer.stop();
        JOptionPane.showMessageDialog(this, "Exam submitted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        System.exit(0);
    }

    private void startTimer(int duration) {
        timeRemaining = duration * 60;
        timer = new Timer(1000, e -> {
            timeRemaining--;
            lblTimer.setText("Time Left: " + (timeRemaining / 60) + "m " + (timeRemaining % 60) + "s");
            if (timeRemaining <= 0) {
                timer.stop();
                submitExam();
            }
        });
        timer.start();
    }

    private List<Question1> fetchQuestionsFromDB(String examCode) {
        List<Question1> questionsList = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/examdb", "root", "password");
             PreparedStatement stmt = conn.prepareStatement("SELECT question, option1, option2, option3, option4, correct_option, marks FROM questions WHERE exam_code = ?")) {
            stmt.setString(1, examCode);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                questionsList.add(new Question1(
                        rs.getString("question"),
                        rs.getString("option1"),
                        rs.getString("option2"),
                        rs.getString("option3"),
                        rs.getString("option4"),
                        rs.getInt("correct_option"),
                        rs.getInt("marks")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return questionsList;
    }

    private int getExamTimeFromDB(String examCode) {
        int examTime = 30; // Default to 30 minutes if not found
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/examdb", "root", "password");
             PreparedStatement stmt = conn.prepareStatement("SELECT duration FROM exams WHERE exam_code = ?")) {
            stmt.setString(1, examCode);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                examTime = rs.getInt("duration");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return examTime;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new StudentExamWindow("EXAM123").setVisible(true));
    }
}
