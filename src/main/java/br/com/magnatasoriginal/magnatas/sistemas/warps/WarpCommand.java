package br.com.magnatasoriginal.magnatas.sistemas.warps;

import br.com.magnatasoriginal.magnatas.Magnatas;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WarpCommand implements CommandExecutor {

    private final WarpManager manager;

    public WarpCommand(Magnatas plugin) {
        this.manager = plugin.getWarpManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Este comando só pode ser usado por jogadores.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage("§eUso correto: /warp <nome>");
            return true;
        }

        String name = args[0].toLowerCase();
        Location warp = manager.getWarp(name);

        if (warp == null) {
            player.sendMessage("§cWarp '" + name + "' não encontrada.");
            return true;
        }

        player.teleport(warp);
        player.sendMessage("§aTeleportado para warp '" + name + "'.");
        return true;
    }
}