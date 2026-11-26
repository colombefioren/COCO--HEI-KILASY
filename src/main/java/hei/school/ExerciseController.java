package hei.school;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ExerciseController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/exercises")
    public List<Exercise> list() {
        return jdbcTemplate.query("SELECT * FROM exercise ORDER BY deadline", (rs, i) -> {
            Exercise e = new Exercise();
            e.setId(rs.getLong("id"));
            e.setTeacherId(rs.getLong("teacher_id"));
            e.setTitle(rs.getString("title"));
            // the statement of a timed exercise is only given by /start, when the countdown begins
            e.setDescription(rs.getObject("duration_minutes") == null ? rs.getString("description") : null);
            e.setMaxPoints(rs.getDouble("max_points"));
            java.sql.Date deadline = rs.getDate("deadline");
            e.setDeadline(deadline == null ? null : deadline.toLocalDate());
            e.setDurationMinutes(rs.getObject("duration_minutes", Integer.class));
            return e;
        });
    }

    @GetMapping("/exercises/{id}")
    public ResponseEntity<?> get(@PathVariable long id) {
        List<Exercise> found = jdbcTemplate.query("SELECT * FROM exercise WHERE id = ?", (rs, i) -> {
            Exercise e = new Exercise();
            e.setId(rs.getLong("id"));
            e.setTeacherId(rs.getLong("teacher_id"));
            e.setTitle(rs.getString("title"));
            // the statement of a timed exercise is only given by /start, when the countdown begins
            e.setDescription(rs.getObject("duration_minutes") == null ? rs.getString("description") : null);
            e.setMaxPoints(rs.getDouble("max_points"));
            java.sql.Date deadline = rs.getDate("deadline");
            e.setDeadline(deadline == null ? null : deadline.toLocalDate());
            e.setDurationMinutes(rs.getObject("duration_minutes", Integer.class));
            return e;
        }, id);
        if (found.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Exercice introuvable"));
        }
        return ResponseEntity.ok(found.get(0));
    }

    // starts the countdown of a student on a timed exercise; calling it again keeps the first start
    @PostMapping("/exercises/{id}/start")
    public ResponseEntity<?> start(@PathVariable long id, @RequestBody Map<String, Object> body) {
        if (body.get("studentId") == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "studentId est obligatoire"));
        }
        long studentId = ((Number) body.get("studentId")).longValue();
        List<Map<String, Object>> exercises = jdbcTemplate.queryForList("SELECT * FROM exercise WHERE id = ?", id);
        if (exercises.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Exercice introuvable"));
        }
        List<Map<String, Object>> students = jdbcTemplate.queryForList("SELECT * FROM student WHERE id = ?", studentId);
        if (students.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Étudiant introuvable"));
        }
        Integer duration = (Integer) exercises.get(0).get("duration_minutes");
        if (duration == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Cet exercice n'est pas minuté"));
        }

        int inserted = jdbcTemplate.update(
                "INSERT INTO exercise_start (exercise_id, student_id, started_at) VALUES (?, ?, ?) ON CONFLICT DO NOTHING",
                id, studentId, Timestamp.valueOf(LocalDateTime.now()));
        LocalDateTime startedAt = jdbcTemplate.queryForObject(
                "SELECT started_at FROM exercise_start WHERE exercise_id = ? AND student_id = ?", Timestamp.class, id,
                studentId).toLocalDateTime();

        Map<String, Object> result = new HashMap<>();
        result.put("exerciseId", id);
        result.put("studentId", studentId);
        result.put("description", exercises.get(0).get("description"));
        result.put("startedAt", startedAt);
        result.put("durationMinutes", duration);
        result.put("endsAt", startedAt.plusMinutes(duration));
        return ResponseEntity.status(inserted == 1 ? 201 : 200).body(result);
    }

    @PostMapping("/exercises")
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        String title = (String) body.get("title");
        if (title == null || title.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Le titre est obligatoire"));
        }
        Object duration = body.get("durationMinutes");
        if (duration != null && (!(duration instanceof Integer) || (Integer) duration <= 0)) {
            return ResponseEntity.badRequest().body(Map.of("error", "La durée doit être un nombre entier de minutes positif"));
        }
        // a timed exercise may have no deadline
        if (body.get("deadline") == null && duration == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "L'échéance est obligatoire (format AAAA-MM-JJ)"));
        }
        LocalDate deadline = null;
        try {
            if (body.get("deadline") != null) {
                deadline = LocalDate.parse((String) body.get("deadline"));
            }
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("error", "L'échéance est obligatoire (format AAAA-MM-JJ)"));
        }
        if (deadline != null && deadline.isBefore(LocalDate.now())) {
            return ResponseEntity.badRequest().body(Map.of("error", "L'échéance doit être dans le futur"));
        }
        double maxPoints = 20;
        if (body.get("maxPoints") != null) {
            maxPoints = ((Number) body.get("maxPoints")).doubleValue();
        }
        if (maxPoints <= 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "Le barème doit être positif"));
        }
        // TODO: get the logged-in teacher once authentication exists
        long teacherId = 1;
        if (body.get("teacherId") != null) {
            teacherId = ((Number) body.get("teacherId")).longValue();
        }

        jdbcTemplate.update(
                "INSERT INTO exercise (teacher_id, title, description, max_points, deadline, duration_minutes) VALUES (?, ?, ?, ?, ?, ?)",
                teacherId, title, body.get("description"), maxPoints,
                deadline == null ? null : java.sql.Date.valueOf(deadline), duration);
        Long id = jdbcTemplate.queryForObject("SELECT max(id) FROM exercise", Long.class);

        Map<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("title", title);
        result.put("maxPoints", maxPoints);
        result.put("deadline", deadline);
        if (duration != null) {
            result.put("durationMinutes", duration);
        }
        System.out.println("Nouvel exercice créé : " + title + " (id " + id + ")");
        return ResponseEntity.status(201).body(result);
    }
}
