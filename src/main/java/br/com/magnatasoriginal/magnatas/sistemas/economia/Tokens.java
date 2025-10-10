package br.com.magnatasoriginal.magnatas.sistemas.economia;

import br.com.magnatasoriginal.magnatas.Magnatas;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.*;
import java.util.*;
import java.util.logging.Level;

public class Tokens {

    private final Magnatas magnatas;

    public Tokens(Magnatas plugin) {
        this.magnatas = plugin;
    }

    // Retorna um mapa com todos os saldos de tokens por UUID
    public Map<UUID, Integer> getAllTokens() {
        Map<UUID, Integer> resultado = new HashMap<>();
        try (Connection conn = magnatas.getSQLiteManager().openConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT uuid, tokenCount FROM tokens")) {

            while (rs.next()) {
                String uuidStr = rs.getString("uuid");
                int count = rs.getInt("tokenCount");
                try {
                    UUID uuid = UUID.fromString(uuidStr);
                    resultado.put(uuid, count);
                } catch (IllegalArgumentException ignored) {
                    // ignora registros com UUID inválido
                }
            }
        } catch (SQLException e) {
            magnatas.getLogger().warning("Erro ao carregar tokens para ranking:");
            e.printStackTrace();
        }
        return resultado;
    }

    public void createTable(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS tokens (" +
                    "uuid TEXT PRIMARY KEY, " +
                    "tokenCount INTEGER DEFAULT 0, " +
                    "lastClaimed TEXT, " +
                    "streak INTEGER DEFAULT 0)");
        }
    }

    public void claimDailyToken(Player player) {
        String uuid = player.getUniqueId().toString();
        boolean isVip = player.hasPermission("magnatas.vip.token");

        new BukkitRunnable() {
            @Override
            public void run() {
                try (Connection conn = magnatas.getSQLiteManager().openConnection()) {
                    PreparedStatement select = conn.prepareStatement("SELECT tokenCount, lastClaimed, streak FROM tokens WHERE uuid = ?");
                    select.setString(1, uuid);
                    ResultSet rs = select.executeQuery();

                    long now = System.currentTimeMillis();
                    int baseTokens = isVip ? 2 : 1;
                    int streak = 0;
                    int bonus = 0;
                    boolean canClaim = true;

                    if (rs.next()) {
                        Timestamp last = Timestamp.valueOf(rs.getString("lastClaimed"));
                        long diff = now - last.getTime();

                        if (diff >= 48 * 60 * 60 * 1000) {
                            streak = 1; // reset
                        } else if (diff >= 20 * 60 * 60 * 1000) {
                            streak = rs.getInt("streak") + 1;
                        } else {
                            canClaim = false;
                        }
                    } else {
                        streak = 1;
                    }

                    if (!canClaim) {
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                send(player, "&cVocê já recebeu seu Token hoje.");
                            }
                        }.runTask(magnatas);
                        return;
                    }

                    // Bônus por streak
                    if (streak >= 30 || streak >= 20 || streak >= 10) bonus = 4;
                    else if (streak >= 7) bonus = 2;
                    else if (streak >= 3) bonus = 1;

                    int total = baseTokens + bonus;

                    int current = rs.next() ? rs.getInt("tokenCount") : 0;
                    int updated = current + total;

                    PreparedStatement upsert = conn.prepareStatement(
                            "INSERT OR REPLACE INTO tokens (uuid, tokenCount, lastClaimed, streak) VALUES (?, ?, ?, ?)");
                    upsert.setString(1, uuid);
                    upsert.setInt(2, updated);
                    upsert.setString(3, new Timestamp(now).toString());
                    upsert.setInt(4, streak);
                    upsert.executeUpdate();

                    final int finalBonus = bonus;
                    final int finalStreak = streak;

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            send(player, "&aVocê recebeu &e" + total + " Token" + (total > 1 ? "s" : "") + " &apelo login diário!");
                            if (finalBonus > 0) {
                                send(player, "&6Bônus de streak: &e+" + finalBonus + " &7(Streak atual: " + finalStreak + " dias)");
                            }
                        }
                    }.runTask(magnatas);

                } catch (SQLException e) {
                    magnatas.getLogger().log(Level.SEVERE,"Erro ao processar Tokens de " + player.getName(), e);
                }
            }
        }.runTaskAsynchronously(magnatas);
    }

    public void getTokenInfo(Player player) {
        String uuid = player.getUniqueId().toString();

        new BukkitRunnable() {
            @Override
            public void run() {
                try (Connection conn = magnatas.getSQLiteManager().openConnection()) {
                    PreparedStatement stmt = conn.prepareStatement("SELECT tokenCount, lastClaimed, streak FROM tokens WHERE uuid = ?");
                    stmt.setString(1, uuid);
                    ResultSet rs = stmt.executeQuery();

                    if (rs.next()) {
                        int count = rs.getInt("tokenCount");
                        int streak = rs.getInt("streak");
                        Timestamp last = Timestamp.valueOf(rs.getString("lastClaimed"));
                        long nextClaim = last.getTime() + 24 * 60 * 60 * 1000;
                        long remaining = nextClaim - System.currentTimeMillis();

                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                player.sendMessage("§8=============================================");
                                send(player, "&eVocê tem &a" + count + " Token" + (count != 1 ? "s" : "") + ".");
                                send(player, "&7Streak atual: &6" + streak + " dias");
                                send(player, "&7Próximo resgate em: &b" + formatTime(remaining));
                                send(player, "&7Próximo bônus: " + getNextStreakBonus(streak));
                                player.sendMessage("§8=============================================");
                                player.sendMessage("§7by Magnatas");
                            }
                        }.runTask(magnatas);
                    } else {
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                send(player, "&eVocê ainda não recebeu Tokens.");
                            }
                        }.runTask(magnatas);
                    }

                } catch (SQLException e) {
                    magnatas.getLogger().log(Level.SEVERE,"Erro ao consultar Tokens de " + player.getName(), e);
                }
            }
        }.runTaskAsynchronously(magnatas);
    }

    public void setTokens(String uuid, int amount) {
        modifyTokens(uuid, amount, true);
    }

    public void addTokens(String uuid, int amount) {
        modifyTokens(uuid, amount, false);
    }

    public void removeTokens(String uuid, int amount) {
        modifyTokens(uuid, -amount, false);
    }
    public int getTokenCount(UUID uuid) {
        return getTokenCount(uuid.toString());
    }

    public void removeTokens(UUID uuid, int amount) {
        removeTokens(uuid.toString(), amount);
    }

    private void modifyTokens(String uuid, int delta, boolean override) {
        new BukkitRunnable() {
            @Override
            public void run() {
                try (Connection conn = magnatas.getSQLiteManager().openConnection()) {
                    int updated = delta;

                    if (!override) {
                        PreparedStatement stmt = conn.prepareStatement("SELECT tokenCount FROM tokens WHERE uuid = ?");
                        stmt.setString(1, uuid);
                        ResultSet rs = stmt.executeQuery();
                        int current = rs.next() ? rs.getInt("tokenCount") : 0;
                        updated = Math.max(0, current + delta);
                    }

                    PreparedStatement update = conn.prepareStatement(
                            "INSERT OR REPLACE INTO tokens (uuid, tokenCount, lastClaimed, streak) VALUES (?, ?, ?, ?)");
                    update.setString(1, uuid);
                    update.setInt(2, updated);
                    update.setString(3, new Timestamp(System.currentTimeMillis()).toString());
                    update.setInt(4, 0); // reset streak on manual change
                    update.executeUpdate();
                } catch (SQLException e) {
                    magnatas.getLogger().log(Level.SEVERE,"Erro ao modificar Tokens de " + uuid, e);;
                }
            }
        }.runTaskAsynchronously(magnatas);
    }

    public void getTokenCount(String uuid, Player viewer) {
        new BukkitRunnable() {
            @Override
            public void run() {
                try (Connection conn = magnatas.getSQLiteManager().openConnection()) {
                    PreparedStatement stmt = conn.prepareStatement("SELECT tokenCount FROM tokens WHERE uuid = ?");
                    stmt.setString(1, uuid);
                    ResultSet rs = stmt.executeQuery();

                    int count = rs.next() ? rs.getInt("tokenCount") : 0;
                    String rawName = Bukkit.getOfflinePlayer(UUID.fromString(uuid)).getName();
                    final String name = (rawName != null) ? rawName : "Desconhecido";

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            send(viewer, "&e" + name + " tem &a" + count + " Token" + (count != 1 ? "s" : "") + ".");
                        }
                    }.runTask(magnatas);
                } catch (SQLException e) {
                    magnatas.getLogger().log(Level.SEVERE,"Erro ao consultar Tokens de " + uuid, e);
                }
            }
        }.runTaskAsynchronously(magnatas);
    }

    public int getTokenCount(String uuid) {
        try (Connection conn = magnatas.getSQLiteManager().openConnection()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT tokenCount FROM tokens WHERE uuid = ?");
            stmt.setString(1, uuid);
            ResultSet rs = stmt.executeQuery();

            return rs.next() ? rs.getInt("tokenCount") : 0;
        } catch (SQLException e) {
            magnatas.getLogger().log(Level.SEVERE, "Erro ao consultar Tokens de " + uuid, e);
            return 0;
        }
    }

    private void send(Player player, String msg) {
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
    }

    private String formatTime(long millis) {
        if (millis <= 0) return "agora";
        long seconds = millis / 1000;
        long h = seconds / 3600;
        long m = (seconds % 3600) / 60;
        long s = seconds % 60;
        return String.format("%02dh %02dm %02ds", h, m, s);
    }

    private String getNextStreakBonus(int streak) {
        if (streak < 3) return "3 dias = +1 Token";
        if (streak < 7) return "7 dias = +2 Tokens";
        if (streak < 10) return "10 dias = +4 Tokens";
        if (streak < 20) return "20 dias = +4 Tokens";
        if (streak < 30) return "30 dias = +4 Tokens";
        return "Você já atingiu o máximo de bônus!";
    }


        //Top Tokens
        public void showTokenTop (Player player){
            new BukkitRunnable() {
                @Override
                public void run() {
                    try (Connection conn = magnatas.getSQLiteManager().openConnection()) {
                        PreparedStatement stmt = conn.prepareStatement(
                                "SELECT uuid, tokenCount FROM tokens ORDER BY tokenCount DESC LIMIT 10"
                        );
                        ResultSet rs = stmt.executeQuery();

                        List<String> lines = new ArrayList<>();
                        int position = 1;

                        while (rs.next()) {
                            String uuid = rs.getString("uuid");
                            int count = rs.getInt("tokenCount");
                            String name = Bukkit.getOfflinePlayer(UUID.fromString(uuid)).getName();
                            if (name == null) name = "Desconhecido";

                            lines.add("§f#" + position + " §b" + name + " §7- §e" + count + " Token" + (count != 1 ? "s" : ""));
                            position++;
                        }

                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                player.sendMessage("§8=============================================");
                                player.sendMessage("§6Ranking de Tokens:");
                                if (lines.isEmpty()) {
                                    player.sendMessage("§cNenhum jogador encontrado.");
                                } else {
                                    lines.forEach(player::sendMessage);
                                }
                                player.sendMessage("§8=============================================");
                                player.sendMessage("§7by Magnatas");
                            }
                        }.runTask(magnatas);

                    } catch (SQLException e) {
                        magnatas.getLogger().log(Level.SEVERE,"Erro ao carregar ranking de Tokens.", e);
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                player.sendMessage("§cErro ao carregar ranking.");
                            }
                        }.runTask(magnatas);
                    }
                }
            }.runTaskAsynchronously(magnatas);
        }
    }
