package br.com.magnatasoriginal.magnatas.sistemas.warps;

import br.com.magnatasoriginal.magnatas.db.SQLiteManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.sql.*;
import java.util.*;

public class WarpManager {
    private final SQLiteManager sqliteManager;

    public WarpManager(SQLiteManager sqliteManager) {
        this.sqliteManager = sqliteManager;
        criarTabela();
    }

    private void criarTabela() {
        try (Connection conn = sqliteManager.openConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS warps (" +
                    "name TEXT PRIMARY KEY, " +
                    "x REAL, y REAL, z REAL, " +
                    "world TEXT)");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setWarp(String name, Location loc) {
        try (Connection conn = sqliteManager.openConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT OR REPLACE INTO warps (name, x, y, z, world) VALUES (?, ?, ?, ?, ?)")) {
            stmt.setString(1, name.toLowerCase());
            stmt.setDouble(2, loc.getX());
            stmt.setDouble(3, loc.getY());
            stmt.setDouble(4, loc.getZ());
            stmt.setString(5, loc.getWorld().getName());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Location getWarp(String name) {
        try (Connection conn = sqliteManager.openConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT x, y, z, world FROM warps WHERE name = ?")) {
            stmt.setString(1, name.toLowerCase());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    World world = Bukkit.getWorld(rs.getString("world"));
                    double x = rs.getDouble("x");
                    double y = rs.getDouble("y");
                    double z = rs.getDouble("z");
                    return new Location(world, x, y, z);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Set<String> listWarps() {
        Set<String> warps = new HashSet<>();
        try (Connection conn = sqliteManager.openConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT name FROM warps");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                warps.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return warps;
    }

    public boolean deleteWarp(String name) {
        try (Connection conn = sqliteManager.openConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "DELETE FROM warps WHERE name = ?")) {
            stmt.setString(1, name.toLowerCase());
            int affected = stmt.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}