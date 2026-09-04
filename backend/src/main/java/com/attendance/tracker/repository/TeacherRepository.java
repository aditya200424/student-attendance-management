package com.attendance.tracker.repository;

import com.attendance.tracker.model.Teacher;
import com.attendance.tracker.util.JsonStorageUtil;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.*;

/**
 * DSA: HashMap<String teacherId, Teacher> -> O(1) average lookup/insert/delete.
 * Secondary HashMap<String email, teacherId> for O(1) average auth lookup.
 */
@Repository
public class TeacherRepository {

    @Value("${app.data.teachers}")
    private String filePath;

    private final Map<String, Teacher> teachersById = new HashMap<>();
    private final Map<String, String> teacherIdByEmail = new HashMap<>();

    @PostConstruct
    public void init() {
        List<Teacher> loaded = JsonStorageUtil.readList(filePath, Teacher.class);
        for (Teacher t : loaded) {
            teachersById.put(t.getTeacherId(), t);
            if (t.getEmail() != null) teacherIdByEmail.put(t.getEmail().toLowerCase(), t.getTeacherId());
        }
        System.out.println("[TeacherRepository] Loaded " + teachersById.size() + " teachers into HashMap");
    }

    public Optional<Teacher> findById(String teacherId) {
        return Optional.ofNullable(teachersById.get(teacherId));
    }

    public Optional<Teacher> findByEmail(String email) {
        if (email == null) return Optional.empty();
        String id = teacherIdByEmail.get(email.toLowerCase());
        return id == null ? Optional.empty() : findById(id);
    }

    public List<Teacher> findAll() {
        return new ArrayList<>(teachersById.values());
    }

    public Teacher save(Teacher teacher) {
        teachersById.put(teacher.getTeacherId(), teacher);
        if (teacher.getEmail() != null) teacherIdByEmail.put(teacher.getEmail().toLowerCase(), teacher.getTeacherId());
        persist();
        return teacher;
    }

    public boolean deleteById(String teacherId) {
        Teacher removed = teachersById.remove(teacherId);
        if (removed == null) return false;
        if (removed.getEmail() != null) teacherIdByEmail.remove(removed.getEmail().toLowerCase());
        persist();
        return true;
    }

    private void persist() {
        JsonStorageUtil.writeList(filePath, new ArrayList<>(teachersById.values()));
    }
}
