package com.attendance.tracker.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Generic helper that loads a JSON array file into a List<T> and writes a
 * List<T> back out to disk. Repositories convert these Lists into HashMaps
 * for O(1) lookups at runtime; the JSON file is purely for persistence
 * across restarts (no database engine is used anywhere).
 */
public class JsonStorageUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JsonStorageUtil() {}

    public static <T> List<T> readList(String path, Class<T> clazz) {
        try {
            File file = new File(path);
            if (!file.exists() || file.length() == 0) {
                return new ArrayList<>();
            }
            CollectionType listType = MAPPER.getTypeFactory().constructCollectionType(List.class, clazz);
            return MAPPER.readValue(file, listType);
        } catch (IOException e) {
            System.err.println("Failed to read JSON file " + path + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public static <T> void writeList(String path, List<T> data) {
        try {
            File file = new File(path);
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            MAPPER.writerWithDefaultPrettyPrinter().writeValue(file, data);
        } catch (IOException e) {
            System.err.println("Failed to write JSON file " + path + ": " + e.getMessage());
        }
    }
}
