package org.icbtcn.dropoutprediction.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class KDDTo3DJSON {
    private static final String ENROLLMENT_CSV = "D:/mini project/dataset/train/train/enrollment_train.csv";
    private static final String LOG_CSV = "D:/mini project/dataset/train/train/log_train.csv";
    private static final String TRUTH_CSV = "D:/mini project/dataset/train/train/truth_train.csv";
    private static final String OUTPUT_FOLDER = "D:/mini project/dataset/train/train/output_json/";

    public static void main(String[] args) {
        preprocessAndGenerate3DMatrix();
    }

    public static void preprocessAndGenerate3DMatrix() {
        try {
            // 1️ Load CSVs
            Map<String, Enrollment> enrollments = loadEnrollments(ENROLLMENT_CSV);
            List<LogEntry> logs = loadLogs(LOG_CSV);
            Map<String, Integer> truth = loadTruth(TRUTH_CSV);

            // 2️ Organize logs per student
            Map<String, List<LogEntry>> studentLogs = new HashMap<>();
            for (LogEntry log : logs) {
                studentLogs.computeIfAbsent(log.enrollmentId, k -> new ArrayList<>()).add(log);
            }

            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);

            // 3️ Generate 3D matrices per student
            for (String studentId : enrollments.keySet()) {
                int T = 5; // weeks
                int E = 7; // events
                double[][][] matrix3D = new double[3][T][E]; // 3 channels: MRatio, SRatio, Rank

                // Count weekly events
                List<LogEntry> sLogs = studentLogs.getOrDefault(studentId, new ArrayList<>());
                for (LogEntry log : sLogs) {
                    int week = log.time / 7; // "time" is already days since first log
                    int eventIdx = Math.abs(log.event.hashCode() % E); // map event safely to 0-6
                    if (week >= 0 && week < T) {
                        matrix3D[0][week][eventIdx] += 1; // MRatio
                        matrix3D[1][week][eventIdx] += 1; // SRatio
                        matrix3D[2][week][eventIdx] += 1; // Rank (simplified)
                    }
                }

                // Normalize values to 0-255
                normalize3DMatrix(matrix3D);

                // Add label
                int label = truth.getOrDefault(studentId, -1);

                // Create JSON object
                Map<String, Object> jsonObj = new HashMap<>();
                jsonObj.put("enrollmentId", studentId);
                jsonObj.put("username", enrollments.get(studentId).username);
                jsonObj.put("courseId", enrollments.get(studentId).courseId);
                jsonObj.put("label", label);
                jsonObj.put("matrix3D", matrix3D);

                // Save JSON to file
                File folder = new File(OUTPUT_FOLDER);
                if (!folder.exists()) folder.mkdirs();
                File outFile = new File(folder, studentId + ".json");
                mapper.writeValue(outFile, jsonObj);
            }

            System.out.println("3D matrices generated and saved as JSON successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Normalize each matrix channel to 0-255
    private static void normalize3DMatrix(double[][][] mat) {
        for (int c = 0; c < mat.length; c++) {
            double max = Arrays.stream(mat[c]).flatMapToDouble(Arrays::stream).max().orElse(1);
            for (int i = 0; i < mat[c].length; i++) {
                for (int j = 0; j < mat[c][0].length; j++) {
                    mat[c][i][j] = (mat[c][i][j] / max) * 255;
                }
            }
        }
    }

    // CSV loaders
    private static Map<String, Enrollment> loadEnrollments(String path) throws IOException {
        Map<String, Enrollment> map = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            br.readLine(); // skip header
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                map.put(parts[0], new Enrollment(parts[0], parts[1], parts[2]));
            }
        }
        return map;
    }

    private static List<LogEntry> loadLogs(String path) throws IOException {
        List<LogEntry> list = new ArrayList<>();

        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME; // e.g. "2014-06-14T09:38:29"
        LocalDateTime startDate = null;

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            br.readLine(); // skip header
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");

                // Parse timestamp as LocalDateTime
                LocalDateTime timestamp = LocalDateTime.parse(parts[1], formatter);

                if (startDate == null) {
                    startDate = timestamp;
                }

                // Convert to "days since first log"
                int time = (int) ChronoUnit.DAYS.between(startDate, timestamp);

                list.add(new LogEntry(parts[0], time, parts[2], parts[3], parts[4]));
            }
        }
        return list;
    }

    private static Map<String, Integer> loadTruth(String path) throws IOException {
        Map<String, Integer> map = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                map.put(parts[0], Integer.parseInt(parts[1]));
            }
        }
        return map;
    }

    // Helper classes
    static class Enrollment {
        String enrollmentId, username, courseId;
        Enrollment(String e, String u, String c) {
            enrollmentId = e;
            username = u;
            courseId = c;
        }
    }

    static class LogEntry {
        String enrollmentId, source, event, object;
        int time; // days since start
        LogEntry(String e, int t, String s, String ev, String o) {
            enrollmentId = e;
            time = t;
            source = s;
            event = ev;
            object = o;
        }
    }
}


