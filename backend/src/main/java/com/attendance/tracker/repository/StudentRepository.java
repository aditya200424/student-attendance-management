package com.attendance.tracker.repository;

import com.attendance.tracker.model.Student;
import com.attendance.tracker.util.JsonStorageUtil;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.*;

/**
 * ============================================================
 * DSA CORE: HashMap-based Student storage.
 * ============================================================
 * Backing structure: HashMap<String, Student>
 *   key   = studentId
 *   value = Student object
 *
 * Time complexity (average case, amortized):
 *   - findById(id):      O(1)  -> direct hash bucket lookup
 *   - save/update(id):   O(1)  -> put() into hash bucket
 *   - delete(id):        O(1)  -> remove() from hash bucket
 *   - findAll():         O(n)  -> must traverse all n entries
 *
 * Secondary index: HashMap<String rollNumber, String studentId> gives O(1)
 * average lookup by roll number without scanning the whole table.
 *
 * Every mutation calls persist(), which flushes the current HashMap values
 * to students.json so data survives an application restart, without ever
 * touching a database engine.
 */
@Repository
public class StudentRepository {

    @Value("${app.data.students}")
    private String filePath;

    private final Map<String, Student> studentsById = new HashMap<>();
    private final Map<String, String> studentIdByRoll = new HashMap<>();
    private final Map<String, String> studentIdByEmail = new HashMap<>();

    @PostConstruct
    public void init() {
        List<Student> loaded = JsonStorageUtil.readList(filePath, Student.class);
        for (Student s : loaded) {
            studentsById.put(s.getStudentId(), s);
            if (s.getRollNumber() != null) studentIdByRoll.put(s.getRollNumber(), s.getStudentId());
            if (s.getEmail() != null) studentIdByEmail.put(s.getEmail().toLowerCase(), s.getStudentId());
        }
        System.out.println("[StudentRepository] Loaded " + studentsById.size() + " students into HashMap");
    }

    /** O(1) average lookup by primary key. */
    public Optional<Student> findById(String studentId) {
        return Optional.ofNullable(studentsById.get(studentId));
    }

    /** O(1) average lookup via secondary hash index on rollNumber. */
    public Optional<Student> findByRollNumber(String rollNumber) {
        String id = studentIdByRoll.get(rollNumber);
        return id == null ? Optional.empty() : findById(id);
    }

    /** O(1) average lookup via secondary hash index on email (used for auth). */
    public Optional<Student> findByEmail(String email) {
        if (email == null) return Optional.empty();
        String id = studentIdByEmail.get(email.toLowerCase());
        return id == null ? Optional.empty() : findById(id);
    }

    /** O(n) - must inspect every value in the map. */
    public List<Student> findAll() {
        return new ArrayList<>(studentsById.values());
    }

    /** O(1) average insert/update into the hash table, then persist to disk. */
    public Student save(Student student) {
        studentsById.put(student.getStudentId(), student);
        if (student.getRollNumber() != null) studentIdByRoll.put(student.getRollNumber(), student.getStudentId());
        if (student.getEmail() != null) studentIdByEmail.put(student.getEmail().toLowerCase(), student.getStudentId());
        persist();
        return student;
    }

    /** O(1) average delete from hash table, then persist to disk. */
    public boolean deleteById(String studentId) {
        Student removed = studentsById.remove(studentId);
        if (removed == null) return false;
        if (removed.getRollNumber() != null) studentIdByRoll.remove(removed.getRollNumber());
        if (removed.getEmail() != null) studentIdByEmail.remove(removed.getEmail().toLowerCase());
        persist();
        return true;
    }

    public boolean existsById(String studentId) {
        return studentsById.containsKey(studentId);
    }

    private void persist() {
        JsonStorageUtil.writeList(filePath, new ArrayList<>(studentsById.values()));
    }
}
