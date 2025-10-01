package br.com.magnatasoriginal.magnatas.sistemas.lojas;

import br.com.magnatasoriginal.magnatas.Magnatas;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class SetLojaCommand implements CommandExecutor {
    private final Magnatas plugin;

    public SetLojaCommand(Magnatas plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Apenas jogadores podem usar este comando.");
            return true;
        }

        Player player = (Player) sender;
        String playerName = player.getName();
        String normalizedName = plugin.normalize(playerName);
        Location loc = player.getLocation();

        new BukkitRunnable() {
            @Override
            public void run() {
                Location existingLoja = plugin.getLojaLocation(normalizedName);

                if (existingLoja != null) {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            player.sendMessage(plugin.getMessage("loja_existe"));
                        }
                    }.runTask(plugin);
                    return;
                }

                plugin.saveLoja(playerName, loc, () -> {
                    player.sendMessage(plugin.getMessage("loja_setada"));
                    plugin.getServer().broadcastMessage(plugin.getMessage("broadcast_loja_criada", playerName));
                    plugin.getServer().getPluginManager().callEvent(new LojaAtualizadaEvent());
                });
            }
        }.runTaskAsynchronously(plugin);

        return true;
    }
}