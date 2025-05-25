package model;

import java.io.*;
import java.util.*;
import org.json.JSONObject;
import org.json.JSONArray;
import java.nio.file.Files;

public class LeaderboardManager {
    private static final String LEADERBOARD_DIR = "leaderboards";
    private static final int MAX_ENTRIES = 10;
    private Map<Difficulty, List<LeaderboardEntry>> leaderboards;

    public LeaderboardManager() {
        leaderboards = new EnumMap<>(Difficulty.class);
        for (Difficulty difficulty : Difficulty.values()) {
            leaderboards.put(difficulty, new ArrayList<>());
        }
        
        // Load existing leaderboards first
        loadLeaderboards();
        
        // Only add test data if no leaderboard files exist
        boolean hasExistingData = false;
        for (Difficulty difficulty : Difficulty.values()) {
            if (!leaderboards.get(difficulty).isEmpty()) {
                hasExistingData = true;
                break;
            }
        }
        
        if (!hasExistingData) {
            // Add test data for Easy difficulty only if no data exists
            addEntry(Difficulty.EASY, "Player1", 45);
            addEntry(Difficulty.EASY, "Player2", 52);
            addEntry(Difficulty.EASY, "Player3", 48);
        }
    }

    public void addEntry(Difficulty difficulty, String username, int moves) {
        System.out.println("\nAdding leaderboard entry:");
        System.out.println("Difficulty: " + difficulty);
        System.out.println("Username: " + username);
        System.out.println("Moves: " + moves);
        
        List<LeaderboardEntry> entries = leaderboards.get(difficulty);
        System.out.println("Current entries before update: " + entries.size());
        
        // Remove existing entry for this username if exists
        entries.removeIf(entry -> entry.getUsername().equals(username));
        System.out.println("Entries after removing existing: " + entries.size());
        
        // Add new entry
        entries.add(new LeaderboardEntry(username, moves));
        System.out.println("Entries after adding new: " + entries.size());
        
        // Sort and limit to MAX_ENTRIES
        Collections.sort(entries);
        if (entries.size() > MAX_ENTRIES) {
            entries = entries.subList(0, MAX_ENTRIES);
        }
        System.out.println("Final entries count: " + entries.size());
        
        leaderboards.put(difficulty, entries);
        saveLeaderboards();
        System.out.println("Leaderboard saved successfully");
    }

    public List<LeaderboardEntry> getLeaderboard(Difficulty difficulty) {
        return new ArrayList<>(leaderboards.get(difficulty));
    }

    private void saveLeaderboards() {
        File dir = new File(LEADERBOARD_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        for (Map.Entry<Difficulty, List<LeaderboardEntry>> entry : leaderboards.entrySet()) {
            String filename = LEADERBOARD_DIR + "/" + entry.getKey().name().toLowerCase() + ".json";
            try {
                JSONArray entriesArray = new JSONArray();
                for (LeaderboardEntry leaderboardEntry : entry.getValue()) {
                    JSONObject entryJson = new JSONObject();
                    entryJson.put("username", leaderboardEntry.getUsername());
                    entryJson.put("moves", leaderboardEntry.getMoves());
                    entryJson.put("timestamp", leaderboardEntry.getTimestamp());
                    entriesArray.put(entryJson);
                }

                // Write to file with pretty printing
                try (FileWriter writer = new FileWriter(filename)) {
                    writer.write(entriesArray.toString(2)); // Pretty print with 2 spaces indentation
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadLeaderboards() {
        for (Difficulty difficulty : Difficulty.values()) {
            String filename = LEADERBOARD_DIR + "/" + difficulty.name().toLowerCase() + ".json";
            File file = new File(filename);
            if (file.exists()) {
                try {
                    String content = new String(Files.readAllBytes(file.toPath()));
                    JSONArray entriesArray = new JSONArray(content);
                    List<LeaderboardEntry> entries = new ArrayList<>();
                    
                    for (int i = 0; i < entriesArray.length(); i++) {
                        JSONObject entryJson = entriesArray.getJSONObject(i);
                        String username = entryJson.getString("username");
                        int moves = entryJson.getInt("moves");
                        long timestamp = entryJson.getLong("timestamp");
                        
                        LeaderboardEntry entry = new LeaderboardEntry(username, moves);
                        entry.setTimestamp(timestamp); // Set the loaded timestamp
                        entries.add(entry);
                    }
                    
                    leaderboards.put(difficulty, entries);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
} 