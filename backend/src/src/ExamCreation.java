import java.sql.*;
import java.util.Scanner;

public class ExamCreation {
    public static void main(String[] args) {
        try {
            // Step 1: Connect to the database
            Connection conn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/examdb",
                    "root",
                    "MySQL@24"  // Replace with your MySQL password
            );
            Scanner scanner = new Scanner(System.in);

            // Step 2: Get exam details from the user
            System.out.print("Enter exam code: ");
            String examCode = scanner.nextLine();
            System.out.print("Enter timer (in minutes): ");
            int timer = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            // Step 3: Insert exam into the exams table with total_marks = 0
            String sql = "INSERT INTO exams (exam_code, timer, total_marks) VALUES (?, ?, 0)";
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, examCode);
            ps.setInt(2, timer);
            ps.executeUpdate();

            // Retrieve the generated exam_id
            ResultSet rs = ps.getGeneratedKeys();
            int examId = 0;
            if (rs.next()) {
                examId = rs.getInt(1);
            }

            // Step 4: Add questions and options
            boolean addMore = true;
            while (addMore) {
                // Get question details
                System.out.print("Enter question text: ");
                String questionText = scanner.nextLine();
                System.out.print("Enter marks for this question: ");
                int marks = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                // Insert question into the questions table
                sql = "INSERT INTO questions (exam_id, question_text, marks) VALUES (?, ?, ?)";
                ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setInt(1, examId);
                ps.setString(2, questionText);
                ps.setInt(3, marks);
                ps.executeUpdate();

                // Retrieve the generated question_id
                rs = ps.getGeneratedKeys();
                int questionId = 0;
                if (rs.next()) {
                    questionId = rs.getInt(1);
                }

                // Get four options for the question
                System.out.println("Enter 4 options for this question:");
                String[] options = new String[4];
                for (int i = 0; i < 4; i++) {
                    System.out.print("Option " + (i + 1) + ": ");
                    options[i] = scanner.nextLine();
                }

                // Get the correct option number with validation
                int correctOption = 0;
                while (correctOption < 1 || correctOption > 4) {
                    System.out.print("Enter the number of the correct option (1-4): ");
                    correctOption = scanner.nextInt();
                    scanner.nextLine(); // Consume newline
                    if (correctOption < 1 || correctOption > 4) {
                        System.out.println("Invalid option number. Please enter a number between 1 and 4.");
                    }
                }

                // Insert options into the options table
                for (int i = 0; i < 4; i++) {
                    boolean isCorrect = (i + 1 == correctOption);
                    sql = "INSERT INTO options (question_id, option_text, is_correct) VALUES (?, ?, ?)";
                    ps = conn.prepareStatement(sql);
                    ps.setInt(1, questionId);
                    ps.setString(2, options[i]);
                    ps.setBoolean(3, isCorrect);
                    ps.executeUpdate();
                }

                // Ask if the user wants to add another question
                System.out.print("Do you want to add another question? (y/n): ");
                String response = scanner.nextLine();
                if (!response.equalsIgnoreCase("y")) {
                    addMore = false;
                }
            }

            // Step 5: Calculate total marks
            sql = "SELECT SUM(marks) FROM questions WHERE exam_id = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, examId);
            rs = ps.executeQuery();
            int totalMarks = 0;
            if (rs.next()) {
                totalMarks = rs.getInt(1);
            }

            // Step 6: Update the exams table with the total marks
            sql = "UPDATE exams SET total_marks = ? WHERE exam_id = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, totalMarks);
            ps.setInt(2, examId);
            ps.executeUpdate();

            // Step 7: Display success message and close the connection
            System.out.println("Exam created successfully with code: " + examCode + " and total marks: " + totalMarks);
            conn.close();

        } catch (SQLException e) {
            System.err.println("Database error occurred:");
            e.printStackTrace();
        }
    }
}