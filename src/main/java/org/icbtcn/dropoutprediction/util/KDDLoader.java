package org.icbtcn.dropoutprediction.util;
import org.icbtcn.dropoutprediction.model.StudentFeatures;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KDDLoader {
    private static final Logger logger = LoggerFactory.getLogger(KDDLoader.class);
    private static List<String[]> loadCSV(String path, boolean hasHeader) throws IOException {
        List<String[]> data = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(path));
        String line;
        if (hasHeader) br.readLine(); // skip header
        while ((line = br.readLine()) != null) {
            data.add(line.split(","));
        }
        br.close();
        return data;
    }

    // Main method to load all CSVs and merge features
    public static List<StudentFeatures> loadAndPreprocess(
            String enrollmentPath,
            String logPath,
            String truthPath) throws IOException {

        Map<String, StudentFeatures> featuresMap = new HashMap<>();

        // 1️ Load log_train.csv
        List<String[]> logData = loadCSV(logPath, true);
        for (String[] row : logData) {
            String enrollmentId = row[0];
            String event = row[3]; // event column
            StudentFeatures feat = featuresMap.getOrDefault(enrollmentId, new StudentFeatures(enrollmentId));
            feat.addEvent(event);
            featuresMap.put(enrollmentId, feat);
        }

        // 2️ Load enrollment_train.csv
        List<String[]> enrollData = loadCSV(enrollmentPath, true);
        for (String[] row : enrollData) {
            String enrollmentId = row[0];
            StudentFeatures feat = featuresMap.getOrDefault(enrollmentId, new StudentFeatures(enrollmentId));
            feat.setUsername(row[1]);
            feat.setCourseId(row[2]);
            featuresMap.put(enrollmentId, feat);
        }

        // 3️ Load truth_train.csv
        List<String[]> truthData = loadCSV(truthPath, false);
        for (String[] row : truthData) {
            String enrollmentId = row[0];
            int label = Integer.parseInt(row[1]);
            if (featuresMap.containsKey(enrollmentId)) {
                featuresMap.get(enrollmentId).setLabel(label);
            }
        }

        return new ArrayList<>(featuresMap.values());
    }

    // Simple test main
    public static void main(String[] args) {
        try {
            List<StudentFeatures> students = loadAndPreprocess(
                    "D:/mini project/dataset/train/train/enrollment_train.csv",
                    "D:/mini project/dataset/train/train/log_train.csv",
                    "D:/mini project/dataset/train/train/truth_train.csv"
            );
            // Print first 5 rows
            students.stream().limit(5).forEach(System.out::println);
        } catch (IOException e) {
            logger.error("Error reading CSV file", e);
        }
    }
}
