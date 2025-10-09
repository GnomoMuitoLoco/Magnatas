package br.com.magnatasoriginal.magnatas.sistemas.titulos.commands;

import br.com.magnatasoriginal.magnatas.sistemas.titulos.TituloService;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class TituloCommandAdmin implements CommandExecutor {

    private final TituloCommandHandler handler;

    public TituloCommandAdmin(TituloService service) {
        this.handler = new TituloCommandHandler(service);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("magnatas.titulos.admin")) {
            sender.sendMessage("§cVocê não tem permissão para usar este comando.");
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage("§eUso: /tituloadmin <dar|remover> <jogador> <titulo>");
            return true;
        }

        String action = args[0].toLowerCase();
        OfflinePlayer alvo = Bukkit.getOfflinePlayer(args[1]);
        String tituloNome = args[2];

        switch (action) {
            case "dar" -> handler.darTitulo(sender, alvo.getUniqueId(), tituloNome);
            case "remover" -> handler.removerTitulo(sender, alvo.getUniqueId(), tituloNome);
            default -> sender.sendMessage("§cSubcomando inválido.");
        }
        return true;
    }
}