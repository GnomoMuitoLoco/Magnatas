package br.com.magnatasoriginal.magnatas.sistemas.lojas;

import br.com.magnatasoriginal.magnatas.Magnatas;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public class TeleportLojaCommand implements CommandExecutor {
    private final Magnatas plugin;
    private final Map<Player, BukkitRunnable> pendingTeleports = new HashMap<>();

    public TeleportLojaCommand(Magnatas plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Apenas jogadores podem usar este comando."); // opcional: pode criar chave "geral.apenas_jogadores"
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage(plugin.getMensagens().get("loja.uso_comando"));
            return false;
        }

        String lojaOwner = args[0];
        String normalizedName = plugin.normalize(lojaOwner);

        new BukkitRunnable() {
            @Override
            public void run() {
                Location loc = plugin.getLojaLocation(normalizedName);

                if (loc == null) {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            player.sendMessage(plugin.getMensagens().get("loja.nao_encontrada", lojaOwner));
                        }
                    }.runTask(plugin);
                    return;
                }

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (player.hasPermission("magnatas.bypasscooldown")) {
                            player.teleport(loc);
                            player.sendMessage(plugin.getMensagens().get("loja.teleportado", lojaOwner));
                        } else {
                            if (pendingTeleports.containsKey(player)) {
                                player.sendMessage(plugin.getMensagens().get("loja.teleporte_em_progresso", lojaOwner));
                                return;
                            }

                            player.sendMessage(plugin.getMensagens().get("loja.teleporte_iniciado", lojaOwner));

                            BukkitRunnable task = new BukkitRunnable() {
                                final Location initialLocation = player.getLocation();
                                int seconds = 5;

                                @Override
                                public void run() {
                                    if (!player.isOnline() || player.getLocation().distance(initialLocation) > 0.5) {
                                        player.sendMessage(plugin.getMensagens().get("loja.teleporte_cancelado"));
                                        pendingTeleports.remove(player);
                                        cancel();
                                        return;
                                    }

                                    if (seconds <= 0) {
                                        player.teleport(loc);
                                        player.sendMessage(plugin.getMensagens().get("loja.teleportado", lojaOwner));
                                        pendingTeleports.remove(player);
                                        cancel();
                                        return;
                                    }

                                    player.sendMessage(plugin.getMensagens().get("loja.tempo_restante", String.valueOf(seconds)));
                                    seconds--;
                                }
                            };

                            pendingTeleports.put(player, task);
                            task.runTaskTimer(plugin, 0L, 20L);
                        }
                    }
                }.runTask(plugin);

                plugin.logVisit(player.getName(), lojaOwner);
            }
        }.runTaskAsynchronously(plugin);

        return true;
    }
}