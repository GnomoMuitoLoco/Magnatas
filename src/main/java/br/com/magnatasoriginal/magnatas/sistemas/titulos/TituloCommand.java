package br.com.magnatasoriginal.magnatas.sistemas.titulos;

import br.com.magnatasoriginal.magnatas.Magnatas;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TituloCommand implements CommandExecutor {

    private final Magnatas plugin;
    private final TituloManager tituloManager;

    public TituloCommand(Magnatas plugin, TituloManager tituloManager) {
        this.plugin = plugin;
        this.tituloManager = tituloManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Apenas jogadores podem usar este comando.");
            return true;
        }

        if (args.length == 0) {
            // Abre o menu de títulos
            TituloGUI gui = new TituloGUI(plugin);
            gui.openMenu(player);
            return true;
        }

        // Subcomandos futuros (ex: /titulos info <nome>)
        switch (args[0].toLowerCase()) {
            case "info":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Uso correto: /titulos info <nome>");
                    return true;
                }
                String nomeTitulo = args[1];
                Titulo titulo = tituloManager.getTituloPorNome(nomeTitulo);
                if (titulo == null) {
                    player.sendMessage(ChatColor.RED + "Esse título não existe.");
                    return true;
                }
                player.sendMessage(ChatColor.YELLOW + "=== Informações do Título ===");
                player.sendMessage(ChatColor.GOLD + "Nome: " + titulo.getNome());
                player.sendMessage(ChatColor.GRAY + "Descrição: " + titulo.getDescricao());
                player.sendMessage(ChatColor.AQUA + "Tipo: " + titulo.getTipo().name());
                if (titulo.getExpiraEm() != null) {
                    player.sendMessage(ChatColor.RED + "Expira em: " + titulo.getExpiraEm().toString());
                } else {
                    player.sendMessage(ChatColor.GREEN + "Permanente");
                }
                break;

            default:
                player.sendMessage(ChatColor.RED + "Subcomando inválido. Use /titulos ou /titulos info <nome>.");
                break;
        }

        return true;
    }
}