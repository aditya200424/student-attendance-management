package com.attendance.tracker.repository;

import com.attendance.tracker.model.Attendance;
import com.attendance.tracker.model.AttendanceStatus;
import com.attendance.tracker.util.JsonStorageUtil;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.*;

/**
 * ============================================================
 * DSA CORE: Attendance storage using multiple data structures.
 * ============================================================
 *
 * 1) HashMap<String, Attendance> attendanceById
 *    key = studentId + "_" + subjectId + "_" + date (composite key)
 *    -> O(1) average lookup of "was this student marked for this subject
 *       on this date", O(1) average insert/update/delete.
 *
 * 2) HashMap<String, ArrayList<Attendance>> attendanceByStudent
 *    key = studentId, value = ArrayList of all attendance records for that
 *    student -> O(1) average to fetch the bucket, then O(k) to scan k
 *    records for that student (much faster than O(n) full-table scan).
 *
 * 3) HashMap<String, HashSet<String>> presentDatesByStudent
 *    key = studentId, value = HashSet of dates the student was marked
 *    PRESENT -> O(1) average "was date X a present day" checks and
 *    guarantees uniqueness of dates (a HashSet cannot store duplicates).
 *
 * 4) Deque<Attendance> undoStack (used as a Stack)
 *    Push every new/updated record; pop() to instantly undo the most
 *    recent attendance action -> O(1) push/pop.
 *
 * 5) Queue<Attendance> pendingRequestsQueue
 *    FIFO queue of attendance-correction requests waiting for teacher
 *    approval -> O(1) offer()/poll().
 *
 * 6) PriorityQueue used on demand in AttendanceService to rank students by
 *    attendance percentage for the leaderboard/top-attendance feature
 *    (O(log n) insert, O(n log n) to drain in sorted order).
 */
@Repository
public class AttendanceRepository {

    @Value("${app.data.attendance}")
    private String filePath;

    private final Map<String, Attendance> attendanceById = new HashMap<>();
    private final Map<String, ArrayList<Attendance>> attendanceByStudent = new HashMap<>();
    private final Map<String, HashSet<String>> presentDatesByStudent = new HashMap<>();

    // Stack (via Deque) for undo-last-attendance-action feature. O(1) push/pop.
    private final Deque<Attendance> undoStack = new ArrayDeque<>();

    // Queue for pending attendance correction/regularization requests. O(1) offer/poll.
    private final Queue<Attendance> pendingRequestsQueue = new LinkedList<>();

    @PostConstruct
    public void init() {
        List<Attendance> loaded = JsonStorageUtil.readList(filePath, Attendance.class);
        for (Attendance a : loaded) {
            indexRecord(a);
        }
        System.out.println("[AttendanceRepository] Loaded " + attendanceById.size() + " attendance records into HashMap");
    }

    private void indexRecord(Attendance a) {
        attendanceById.put(a.getAttendanceId(), a);
        attendanceByStudent.computeIfAbsent(a.getStudentId(), k -> new ArrayList<>()).add(a);
        if (a.getStatus() == AttendanceStatus.PRESENT || a.getStatus() == AttendanceStatus.LATE) {
            presentDatesByStudent.computeIfAbsent(a.getStudentId(), k -> new HashSet<>()).add(a.getDate());
        }
    }

    public static String buildKey(String studentId, String subjectId, String date) {
        return studentId + "_" + subjectId + "_" + date;
    }

    /** O(1) average lookup by composite key. */
    public Optional<Attendance> findById(String attendanceId) {
        return Optional.ofNullable(attendanceById.get(attendanceId));
    }

    /** O(1) average bucket fetch + O(k) copy, k = number of that student's records. */
    public List<Attendance> findByStudent(String studentId) {
        return new ArrayList<>(attendanceByStudent.getOrDefault(studentId, new ArrayList<>()));
    }

    /** O(1) average HashSet membership check (unique-dates guarantee). */
    public boolean wasPresentOn(String studentId, String date) {
        HashSet<String> dates = presentDatesByStudent.get(studentId);
        return dates != null && dates.contains(date);
    }

    public List<Attendance> findAll() {
        return new ArrayList<>(attendanceById.values());
    }

    /** O(1) average insert/update into hash table + O(1) push onto undo stack. */
    public Attendance save(Attendance attendance) {
        // If it already existed, remove stale entries from the secondary indices first.
        Attendance existing = attendanceById.get(attendance.getAttendanceId());
        if (existing != null) {
            attendanceByStudent.getOrDefault(existing.getStudentId(), new ArrayList<>()).remove(existing);
            HashSet<String> dates = presentDatesByStudent.get(existing.getStudentId());
            if (dates != null) dates.remove(existing.getDate());
        }
        indexRecord(attendance);
        undoStack.push(attendance); // O(1) Stack push for undo feature
        persist();
        return attendance;
    }

    /** O(1) Stack pop to undo the most recently saved attendance action. */
    public Optional<Attendance> undoLast() {
        if (undoStack.isEmpty()) return Optional.empty();
        Attendance last = undoStack.pop();
        deleteById(last.getAttendanceId());
        return Optional.of(last);
    }

    public boolean deleteById(String attendanceId) {
        Attendance removed = attendanceById.remove(attendanceId);
        if (removed == null) return false;
        attendanceByStudent.getOrDefault(removed.getStudentId(), new ArrayList<>()).remove(removed);
        HashSet<String> dates = presentDatesByStudent.get(removed.getStudentId());
        if (dates != null) dates.remove(removed.getDate());
        persist();
        return true;
    }

    /** O(1) Queue enqueue for a pending correction request. */
    public void enqueuePendingRequest(Attendance attendance) {
        pendingRequestsQueue.offer(attendance);
    }

    /** O(1) Queue dequeue - process the oldest pending request first (FIFO). */
    public Optional<Attendance> pollPendingRequest() {
        return Optional.ofNullable(pendingRequestsQueue.poll());
    }

    public List<Attendance> peekAllPending() {
        return new ArrayList<>(pendingRequestsQueue);
    }

    private void persist() {
        JsonStorageUtil.writeList(filePath, new ArrayList<>(attendanceById.values()));
    }
}
