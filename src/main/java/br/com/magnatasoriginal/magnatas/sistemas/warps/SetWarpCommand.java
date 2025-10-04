package br.com.magnatasoriginal.magnatas.sistemas.warps;

import br.com.magnatasoriginal.magnatas.Magnatas;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetWarpCommand implements CommandExecutor {

    private final WarpManager manager;

    public SetWarpCommand(Magnatas plugin) {
        this.manager = plugin.getWarpManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Este comando só pode ser usado por jogadores.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("magnatas.admin.setwarp")) {
            player.sendMessage("§cVocê não tem permissão para usar este comando.");
            return true;
        }

        if (args.length != 1) {
            player.sendMessage("§eUso correto: /setwarp <nome>");
            return true;
        }

        String name = args[0].toLowerCase();

        if (manager.getWarp(name) != null) {
            player.sendMessage("§cA warp '" + name + "' já existe. Use outro nome ou remova com /delwarp " + name + ".");
            return true;
        }

        Location location = player.getLocation();
        manager.setWarp(name, location);
        player.sendMessage("§aWarp '" + name + "' salva com sucesso!");
        return true;
    }
}