package br.com.magnatasoriginal.magnatas.sistemas.homes;

import br.com.magnatasoriginal.magnatas.Magnatas;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

public class SetHomeCommand implements CommandExecutor {

    private final HomeManager manager;

    public SetHomeCommand(Magnatas plugin) {
        this.manager = plugin.getHomeManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Este comando só pode ser usado por jogadores.");
            return true;
        }

        Player player = (Player) sender;
        String name = args.length > 0 ? args[0].toLowerCase() : "home";

        int maxHomes = 1;

        if (player.hasPermission("magnatas.homes.*")) {
            maxHomes = Integer.MAX_VALUE;
        } else {
            for (PermissionAttachmentInfo permInfo : player.getEffectivePermissions()) {
                String perm = permInfo.getPermission();
                if (perm.startsWith("magnatas.homes.")) {
                    try {
                        int value = Integer.parseInt(perm.substring("magnatas.homes.".length()));
                        if (value > maxHomes) {
                            maxHomes = value;
                        }
                    } catch (NumberFormatException ignored) {
                        // ignora permissões como magnatas.homes.admin
                    }
                }
            }
        }

        boolean homeExists = manager.getHome(player, name) != null;
        int currentCount = manager.getHomeCount(player);

        if (currentCount >= maxHomes && !homeExists) {
            player.sendMessage("Você atingiu o limite de homes permitido.");
            return true;
        }

        if (homeExists) {
            player.sendMessage("Home '" + name + "' já existe. Use outro nome ou remova com /delhome " + name + ".");
            return true;
        }
        Location location = player.getLocation();
        manager.setHome(player, name, location);
        player.sendMessage("Home '" + name + "' salva com sucesso!");
        return true;
    }
}