package br.com.magnatasoriginal.magnatas.sistemas.homes;

import br.com.magnatasoriginal.magnatas.Magnatas;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DelHomeCommand implements CommandExecutor {

    private final HomeManager manager;

    public DelHomeCommand(Magnatas plugin) {
        this.manager = plugin.getHomeManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Este comando só pode ser usado por jogadores.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage("Use /delhome <nome>");
            return true;
        }

        String nomeHome = args[0].toLowerCase();
        boolean removido = manager.deleteHome(player, nomeHome);

        if (removido) {
            player.sendMessage("Home '" + nomeHome + "' removida com sucesso!");
        } else {
            player.sendMessage("Home '" + nomeHome + "' não encontrada.");
        }

        return true;
    }
}