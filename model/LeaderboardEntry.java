package model;

import java.io.Serializable;

public class LeaderboardEntry implements Serializable, Comparable<LeaderboardEntry> {
    private String username;
    private int moves;
    private long timestamp;

    public LeaderboardEntry(String username, int moves) {
        this.username = username;
        this.moves = moves;
        this.timestamp = System.currentTimeMillis();
    }

    public String getUsername() {
        return username;
    }

    public int getMoves() {
        return moves;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public int compareTo(LeaderboardEntry other) {
        // First compare by moves
        int movesCompare = Integer.compare(this.moves, other.moves);
        if (movesCompare != 0) {
            return movesCompare;
        }
        // If moves are equal, compare by timestamp (earlier is better)
        return Long.compare(this.timestamp, other.timestamp);
    }
} 