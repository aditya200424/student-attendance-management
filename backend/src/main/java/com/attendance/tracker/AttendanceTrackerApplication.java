package com.attendance.tracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Attendance Tracker System.
 *
 * DSA NOTE: This project intentionally avoids a relational/NoSQL database.
 * All persistent entities (Student, Teacher, Admin, Attendance, Subject) are
 * held in memory using java.util.HashMap for O(1) average time lookups,
 * inserts, updates and deletes. On startup, JSON files under /data are
 * deserialized into HashMaps (see repository package). On every mutation,
 * the in-memory HashMap is flushed back to the JSON file so state survives
 * restarts, without ever using a database engine.
 */
@SpringBootApplication
public class AttendanceTrackerApplication {
    public static void main(String[] args) {
        SpringApplication.run(AttendanceTrackerApplication.class, args);
    }
}
