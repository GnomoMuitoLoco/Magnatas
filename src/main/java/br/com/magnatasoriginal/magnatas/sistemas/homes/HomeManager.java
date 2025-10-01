package br.com.magnatasoriginal.magnatas.sistemas.homes;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.*;

public class HomeManager {
    private final Connection connection;

    public HomeManager(Connection connection) {
        this.connection = connection;
        criarTabela();
    }

    private void criarTabela() {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS homes (" +
                    "uuid TEXT NOT NULL, " +
                    "name TEXT NOT NULL, " +
                    "x REAL, y REAL, z REAL, " +
                    "world TEXT, " +
                    "PRIMARY KEY (uuid, name))");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setHome(Player player, String name, Location loc) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT OR REPLACE INTO homes (uuid, name, x, y, z, world) VALUES (?, ?, ?, ?, ?, ?)")) {
            stmt.setString(1, player.getUniqueId().toString());
            stmt.setString(2, name.toLowerCase());
            stmt.setDouble(3, loc.getX());
            stmt.setDouble(4, loc.getY());
            stmt.setDouble(5, loc.getZ());
            stmt.setString(6, loc.getWorld().getName());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Location getHome(Player player, String name) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT x, y, z, world FROM homes WHERE uuid = ? AND name = ?")) {
            stmt.setString(1, player.getUniqueId().toString());
            stmt.setString(2, name.toLowerCase());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                World world = Bukkit.getWorld(rs.getString("world"));
                double x = rs.getDouble("x");
                double y = rs.getDouble("y");
                double z = rs.getDouble("z");
                rs.close();
                return new Location(world, x, y, z);
            }

            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Set<String> listHomes(Player player) {
        Set<String> homes = new HashSet<>();
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT name FROM homes WHERE uuid = ?")) {
            stmt.setString(1, player.getUniqueId().toString());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                homes.add(rs.getString("name"));
            }

            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return homes;
    }

    public boolean deleteHome(Player player, String name) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "DELETE FROM homes WHERE uuid = ? AND name = ?")) {
            stmt.setString(1, player.getUniqueId().toString());
            stmt.setString(2, name.toLowerCase());
            int affected = stmt.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int getHomeCount(Player player) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT COUNT(*) FROM homes WHERE uuid = ?")) {
            stmt.setString(1, player.getUniqueId().toString());
            ResultSet rs = stmt.executeQuery();
            int count = rs.next() ? rs.getInt(1) : 0;
            rs.close();
            return count;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }
}