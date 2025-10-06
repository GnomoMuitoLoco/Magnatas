package br.com.magnatasoriginal.magnatas.sistemas.antilag.limites;

import br.com.magnatasoriginal.magnatas.db.SQLiteManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class LimitesStorage {

    private final SQLiteManager db;

    public LimitesStorage(SQLiteManager db) {
        this.db = db;
    }

    public void salvarLimite(String blocoId, int quantidade) {
        try (Connection conn = db.openConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "REPLACE INTO limites_blocos (bloco_id, quantidade) VALUES (?, ?)");
            stmt.setString(1, blocoId);
            stmt.setInt(2, quantidade);
            stmt.executeUpdate();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Map<String, Integer> carregarTodos() {
        Map<String, Integer> limites = new HashMap<>();
        try (Connection conn = db.openConnection()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT bloco_id, quantidade FROM limites_blocos");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int quantidade = rs.getInt("quantidade");
                if (quantidade > 0) {
                    limites.put(rs.getString("bloco_id"), quantidade);
                }
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return limites;
    }

    public void removerLimite(String blocoId) {
        try (Connection conn = db.openConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "DELETE FROM limites_blocos WHERE bloco_id = ?"
            );
            stmt.setString(1, blocoId);
            stmt.executeUpdate();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
