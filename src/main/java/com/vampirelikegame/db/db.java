package com.vampirelikegame.db;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Managing connection to PostgresSQL database and saving gaming session results
 */
public class db {
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/vampire_like";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "25042007A@d";

    private Connection connection;

    public db() {
        try {
            Class.forName("org.postgresql.Driver");
            connect();
            createTablesIfNotExist();
        } catch (ClassNotFoundException e) {
            System.err.println("PostgreSQL JDBC Driver not found!");
            e.printStackTrace();
        }
    }

    /**
     * Connecting to DB
     */
    private void connect() {
        try {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            System.out.println("Connected to PostgreSQL database!");
        } catch (SQLException e) {
            System.err.println("Connection failed!");
            e.printStackTrace();
        }
    }

    /**
     * Creating tables at first start
     */
    private void createTablesIfNotExist() {
        String createGameResultsTable =
                "CREATE TABLE IF NOT EXISTS game_results (" +
                        "id SERIAL PRIMARY KEY," +
                        "player_level INTEGER NOT NULL," +
                        "survival_time INTEGER NOT NULL," +
                        "enemies_killed INTEGER NOT NULL," +
                        "play_date TIMESTAMP NOT NULL," +
                        "score INTEGER NOT NULL" +
                        ")";

        String createPlayerStatsTable =
                "CREATE TABLE IF NOT EXISTS player_stats (" +
                        "id SERIAL PRIMARY KEY," +
                        "total_games INTEGER DEFAULT 0," +
                        "total_play_time INTEGER DEFAULT 0," +
                        "total_enemies_killed INTEGER DEFAULT 0," +
                        "highest_level INTEGER DEFAULT 0," +
                        "highest_score INTEGER DEFAULT 0," +
                        "last_updated TIMESTAMP NOT NULL" +
                        ")";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createGameResultsTable);
            stmt.execute(createPlayerStatsTable);

            // Initializing statistics if they do not exist
            String checkStats = "SELECT COUNT(*) FROM player_stats";
            ResultSet rs = stmt.executeQuery(checkStats);
            if (rs.next() && rs.getInt(1) == 0) {
                String initStats = "INSERT INTO player_stats (total_games, last_updated) VALUES (0, NOW())";
                stmt.execute(initStats);
            }

            System.out.println("Database tables ready!");
        } catch (SQLException e) {
            System.err.println("Error creating tables!");
            e.printStackTrace();
        }
    }

    /**
     * Saving game session result
     */
    public void saveGameResult(int level, int survivalTime, int enemiesKilled) {
        int score = calculateScore(level, survivalTime, enemiesKilled);

        String insertQuery =
                "INSERT INTO game_results (player_level, survival_time, enemies_killed, play_date, score) " +
                        "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(insertQuery)) {
            pstmt.setInt(1, level);
            pstmt.setInt(2, survivalTime);
            pstmt.setInt(3, enemiesKilled);
            pstmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setInt(5, score);

            pstmt.executeUpdate();
            System.out.println("Game result saved!");

            updatePlayerStats(level, survivalTime, enemiesKilled, score);
        } catch (SQLException e) {
            System.err.println("Error saving game result!");
            e.printStackTrace();
        }
    }

    /**
     * Updating overall player statistics
     */
    private void updatePlayerStats(int level, int survivalTime, int enemiesKilled, int score) {
        String updateQuery =
                "UPDATE player_stats SET " +
                        "total_games = total_games + 1, " +
                        "total_play_time = total_play_time + ?, " +
                        "total_enemies_killed = total_enemies_killed + ?, " +
                        "highest_level = GREATEST(highest_level, ?), " +
                        "highest_score = GREATEST(highest_score, ?), " +
                        "last_updated = NOW() " +
                        "WHERE id = 1";

        try (PreparedStatement pstmt = connection.prepareStatement(updateQuery)) {
            pstmt.setInt(1, survivalTime);
            pstmt.setInt(2, enemiesKilled);
            pstmt.setInt(3, level);
            pstmt.setInt(4, score);

            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating player stats!");
            e.printStackTrace();
        }
    }

    /**
     * Getting top 10 results
     */
    public List<Map<String, Object>> getTopScores(int limit) {
        List<Map<String, Object>> results = new ArrayList<>();

        String query =
                "SELECT player_level, survival_time, enemies_killed, play_date, score " +
                        "FROM game_results " +
                        "ORDER BY score DESC " +
                        "LIMIT ?";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> result = new HashMap<>();
                result.put("level", rs.getInt("player_level"));
                result.put("time", rs.getInt("survival_time"));
                result.put("enemies", rs.getInt("enemies_killed"));
                result.put("date", rs.getTimestamp("play_date"));
                result.put("score", rs.getInt("score"));
                results.add(result);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving top scores!");
            e.printStackTrace();
        }

        return results;
    }

    /**
     * Getting overall player statistics
     */
    public Map<String, Object> getPlayerStats() {
        Map<String, Object> stats = new HashMap<>();

        String query = "SELECT * FROM player_stats WHERE id = 1";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                stats.put("totalGames", rs.getInt("total_games"));
                stats.put("totalPlayTime", rs.getInt("total_play_time"));
                stats.put("totalEnemiesKilled", rs.getInt("total_enemies_killed"));
                stats.put("highestLevel", rs.getInt("highest_level"));
                stats.put("highestScore", rs.getInt("highest_score"));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving player stats!");
            e.printStackTrace();
        }

        return stats;
    }

    /**
     * Score calculation
     */
    private int calculateScore(int level, int survivalTime, int enemiesKilled) {
        return (level * 100) + (survivalTime * 10) + (enemiesKilled * 5);
    }

    /**
     * Close connection
     */
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed!");
            }
        } catch (SQLException e) {
            System.err.println("Error closing connection!");
            e.printStackTrace();
        }
    }
}
