package br.com.magnatasoriginal.magnatas.sistemas.warps;

import br.com.magnatasoriginal.magnatas.Magnatas;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DelWarpCommand implements CommandExecutor {

    private final WarpManager manager;

    public DelWarpCommand(Magnatas plugin) {
        this.manager = plugin.getWarpManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Este comando só pode ser usado por jogadores.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("magnatas.admin.delwarp")) {
            player.sendMessage("§cVocê não tem permissão para usar este comando.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage("§eUso correto: /delwarp <nome>");
            return true;
        }

        String nomeWarp = args[0].toLowerCase();
        boolean removido = manager.deleteWarp(nomeWarp);

        if (removido) {
            player.sendMessage("§aWarp '" + nomeWarp + "' removida com sucesso!");
        } else {
            player.sendMessage("§cWarp '" + nomeWarp + "' não encontrada.");
        }

        return true;
    }
}