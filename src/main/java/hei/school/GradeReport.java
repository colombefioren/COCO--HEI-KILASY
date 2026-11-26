package hei.school;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Grade transcript of a student.
 */
@Component
public class GradeReport {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Grade out of 20, with the late penalty: -10% per started day after the deadline day, capped at -50%.
     * Used by the grading endpoint, the transcript and the CSV export so that they all show the same grade.
     */
    static BigDecimal gradeOutOf20(BigDecimal rawPoints, BigDecimal maxPoints, LocalDate deadline,
            LocalDateTime submittedAt) {
        long daysLate = deadline == null ? 0 // no deadline, no penalty
                : Math.max(0, ChronoUnit.DAYS.between(deadline, submittedAt.toLocalDate()));
        long penaltyPercent = Math.min(daysLate * 10, 50);
        return rawPoints.multiply(BigDecimal.valueOf(20 * (100 - penaltyPercent)))
                .divide(maxPoints.multiply(BigDecimal.valueOf(100)), 1, RoundingMode.HALF_UP);
    }

    public Map<String, Object> build(long studentId) {
        List<Map<String, Object>> students = jdbcTemplate.queryForList("SELECT * FROM student WHERE id = ?", studentId);
        if (students.isEmpty()) {
            return null;
        }
        Map<String, Object> student = students.get(0);

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT e.title, e.deadline, e.max_points, e.duration_minutes, es.started_at, "
                        + "s.id AS submission_id, s.submitted_at, s.raw_points, s.feedback "
                        + "FROM exercise e LEFT JOIN submission s ON s.exercise_id = e.id AND s.student_id = ? "
                        + "LEFT JOIN exercise_start es ON es.exercise_id = e.id AND es.student_id = ? "
                        + "ORDER BY e.deadline",
                studentId, studentId);

        List<Map<String, Object>> lines = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        int graded = 0;
        for (Map<String, Object> row : rows) {
            Map<String, Object> line = new LinkedHashMap<>();
            Date deadlineColumn = (Date) row.get("deadline");
            LocalDate deadline = deadlineColumn == null ? null : deadlineColumn.toLocalDate();
            line.put("exercise", row.get("title"));
            line.put("deadline", deadline);

            Integer duration = (Integer) row.get("duration_minutes");
            Timestamp startedAt = (Timestamp) row.get("started_at");
            if (duration != null) {
                Long timeUsed = null;
                if (startedAt != null && row.get("submission_id") != null) {
                    // whole minutes, rounded up
                    timeUsed = (Duration.between(startedAt.toLocalDateTime(),
                            ((Timestamp) row.get("submitted_at")).toLocalDateTime()).toMillis() + 59_999) / 60_000;
                }
                line.put("durationMinutes", duration);
                line.put("timeUsedMinutes", timeUsed);
            }

            if (row.get("submission_id") == null) {
                boolean timeIsUp = duration != null && startedAt != null
                        && LocalDateTime.now().isAfter(startedAt.toLocalDateTime().plusMinutes(duration));
                if (timeIsUp || (deadline != null && deadline.isBefore(LocalDate.now()))) {
                    line.put("status", "NON RENDU");
                } else {
                    line.put("status", "À RENDRE");
                }
                line.put("grade", null);
            } else {
                LocalDateTime submittedAt = ((Timestamp) row.get("submitted_at")).toLocalDateTime();
                line.put("submittedAt", submittedAt);
                line.put("late", deadline != null && submittedAt.toLocalDate().isAfter(deadline));
                if (row.get("raw_points") == null) {
                    line.put("status", "EN ATTENTE DE CORRECTION");
                    line.put("grade", null);
                } else {
                    BigDecimal raw = (BigDecimal) row.get("raw_points");
                    BigDecimal max = (BigDecimal) row.get("max_points");
                    BigDecimal grade = gradeOutOf20(raw, max, deadline, submittedAt);
                    line.put("status", "CORRIGÉ");
                    line.put("grade", grade);
                    line.put("feedback", row.get("feedback"));
                    total = total.add(grade);
                    graded++;
                }
            }
            lines.add(line);
        }

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("reference", student.get("reference"));
        report.put("student", student.get("full_name"));
        report.put("email", student.get("email"));
        report.put("cohort", student.get("cohort"));
        report.put("exercises", lines);
        report.put("average", graded == 0 ? null : total.divide(new BigDecimal(graded), 2, RoundingMode.HALF_UP));
        return report;
    }
}
