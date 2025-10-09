package br.com.magnatasoriginal.magnatas.sistemas.titulos.commands;

import br.com.magnatasoriginal.magnatas.sistemas.titulos.TituloService;
import br.com.magnatasoriginal.magnatas.sistemas.titulos.gui.TituloGUIService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TituloCommand implements CommandExecutor {

    private final TituloCommandHandler handler;
    private final TituloGUIService guiService;

    public TituloCommand(TituloService service, TituloGUIService guiService) {
        this.handler = new TituloCommandHandler(service);
        this.guiService = guiService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cApenas jogadores podem usar este comando.");
            return true;
        }

        // Se não passar argumentos, abre o menu GUI
        if (args.length == 0) {
            guiService.abrirMenu(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "listar" -> handler.listarTitulos(player);
            case "equipar" -> {
                if (args.length < 2) {
                    player.sendMessage("§cUso correto: /titulo equipar <nome>");
                    return true;
                }
                handler.equiparTitulo(player, args[1]);
            }
            default -> player.sendMessage("§cSubcomando inválido.");
        }
        return true;
    }
}