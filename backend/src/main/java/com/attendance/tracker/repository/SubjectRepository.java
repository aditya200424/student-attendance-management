package com.attendance.tracker.repository;

import com.attendance.tracker.model.Subject;
import com.attendance.tracker.util.JsonStorageUtil;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.*;

/** DSA: HashMap<String subjectId, Subject> -> O(1) average lookup/insert/delete. */
@Repository
public class SubjectRepository {

    @Value("${app.data.subjects}")
    private String filePath;

    private final Map<String, Subject> subjectsById = new HashMap<>();

    @PostConstruct
    public void init() {
        List<Subject> loaded = JsonStorageUtil.readList(filePath, Subject.class);
        for (Subject s : loaded) {
            subjectsById.put(s.getSubjectId(), s);
        }
        System.out.println("[SubjectRepository] Loaded " + subjectsById.size() + " subjects into HashMap");
    }

    public Optional<Subject> findById(String subjectId) {
        return Optional.ofNullable(subjectsById.get(subjectId));
    }

    public List<Subject> findAll() {
        return new ArrayList<>(subjectsById.values());
    }

    public Subject save(Subject subject) {
        subjectsById.put(subject.getSubjectId(), subject);
        persist();
        return subject;
    }

    public boolean deleteById(String subjectId) {
        boolean removed = subjectsById.remove(subjectId) != null;
        if (removed) persist();
        return removed;
    }

    private void persist() {
        JsonStorageUtil.writeList(filePath, new ArrayList<>(subjectsById.values()));
    }
}
