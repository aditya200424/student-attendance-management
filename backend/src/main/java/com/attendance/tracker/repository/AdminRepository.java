package com.attendance.tracker.repository;

import com.attendance.tracker.model.Admin;
import com.attendance.tracker.util.JsonStorageUtil;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.*;

/** DSA: HashMap<String adminId, Admin> -> O(1) average lookup, used for auth. */
@Repository
public class AdminRepository {

    @Value("${app.data.admins}")
    private String filePath;

    private final Map<String, Admin> adminsById = new HashMap<>();
    private final Map<String, String> adminIdByEmail = new HashMap<>();

    @PostConstruct
    public void init() {
        List<Admin> loaded = JsonStorageUtil.readList(filePath, Admin.class);
        for (Admin a : loaded) {
            adminsById.put(a.getAdminId(), a);
            if (a.getEmail() != null) adminIdByEmail.put(a.getEmail().toLowerCase(), a.getAdminId());
        }
        System.out.println("[AdminRepository] Loaded " + adminsById.size() + " admins into HashMap");
    }

    public Optional<Admin> findById(String adminId) {
        return Optional.ofNullable(adminsById.get(adminId));
    }

    public Optional<Admin> findByEmail(String email) {
        if (email == null) return Optional.empty();
        String id = adminIdByEmail.get(email.toLowerCase());
        return id == null ? Optional.empty() : findById(id);
    }

    public List<Admin> findAll() {
        return new ArrayList<>(adminsById.values());
    }

    public Admin save(Admin admin) {
        adminsById.put(admin.getAdminId(), admin);
        if (admin.getEmail() != null) adminIdByEmail.put(admin.getEmail().toLowerCase(), admin.getAdminId());
        persist();
        return admin;
    }

    private void persist() {
        JsonStorageUtil.writeList(filePath, new ArrayList<>(adminsById.values()));
    }
}
