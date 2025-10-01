package br.com.magnatasoriginal.magnatas.sistemas.homes;

import br.com.magnatasoriginal.magnatas.Magnatas;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HomeCommand implements CommandExecutor {

    private final HomeManager manager;

    public HomeCommand(Magnatas plugin) {
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

        Location home = manager.getHome(player, name);

        if (home == null) {
            player.sendMessage("Home '" + name + "' não encontrada.");
            return true;
        }

        player.teleport(home);
        player.sendMessage("Teleportado para '" + name + "'.");
        return true;
    }
}