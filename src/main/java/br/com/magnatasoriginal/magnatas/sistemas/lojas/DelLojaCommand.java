package br.com.magnatasoriginal.magnatas.sistemas.lojas;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import br.com.magnatasoriginal.magnatas.Magnatas;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DelLojaCommand implements CommandExecutor {
    private final Magnatas plugin;

    public DelLojaCommand(Magnatas plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Apenas jogadores podem usar este comando.");
            return true;
        }

        Player player = (Player) sender;
        String rawTargetName;

        if (args.length > 0) {
            if (player.hasPermission("magnatas.delloja.others")) {
                rawTargetName = args[0];
            } else {
                player.sendMessage("Você não tem permissão para remover a loja de outro jogador.");
                return true;
            }
        } else {
            rawTargetName = player.getName();
        }

        String normalizedName = plugin.normalize(rawTargetName); // <- Normalização aqui

        new BukkitRunnable() {
            @Override
            public void run() {
                try (Connection conn = plugin.getSQLiteManager().openConnection();
                     PreparedStatement stmt = conn.prepareStatement("DELETE FROM lojas WHERE playerName = ?")) {
                    stmt.setString(1, normalizedName);
                    int affected = stmt.executeUpdate();

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (affected > 0) {
                                player.sendMessage(plugin.getMessage("loja_removida"));
                                plugin.getServer().broadcastMessage(plugin.getMessage("broadcast_loja_removida", rawTargetName));
                                plugin.getServer().getPluginManager().callEvent(new LojaAtualizadaEvent());
                            } else {
                                player.sendMessage("Nenhuma loja encontrada para " + rawTargetName + ".");
                            }
                        }
                    }.runTask(plugin);

                } catch (SQLException e) {
                    plugin.getLogger().warning("Erro ao remover loja de " + rawTargetName);
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(plugin);

        return true;
    }
}