package br.com.magnatasoriginal.magnatas.sistemas.titulos;

import br.com.magnatasoriginal.magnatas.Magnatas;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.time.LocalDateTime;

public class TituloCommandAdmin implements CommandExecutor {

    private final Magnatas plugin;
    private final TituloManager tituloManager;

    public TituloCommandAdmin(Magnatas plugin, TituloManager tituloManager) {
        this.plugin = plugin;
        this.tituloManager = tituloManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!sender.hasPermission("titulos.admin")) {
            sender.sendMessage(ChatColor.RED + "Você não tem permissão para usar este comando.");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(ChatColor.YELLOW + "Uso: /titulosadmin <criar|dar|remover|listar>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "criar":
                if (args.length < 4) {
                    sender.sendMessage(ChatColor.RED + "Uso: /titulosadmin criar <nome> <tipo> <descricao>");
                    return true;
                }
                String nome = args[1];
                String tipoStr = args[2].toUpperCase();
                String descricao = String.join(" ", java.util.Arrays.copyOfRange(args, 3, args.length));

                try {
                    Titulo.Tipo tipo = Titulo.Tipo.valueOf(tipoStr);
                    Titulo titulo = new Titulo(nome, descricao, tipo, null);
                    tituloManager.registrarTitulo(titulo);
                    sender.sendMessage(ChatColor.GREEN + "Título criado: " + nome + " (" + tipo + ")");
                } catch (IllegalArgumentException e) {
                    sender.sendMessage(ChatColor.RED + "Tipo inválido. Use: PERMANENTE, TEMPORARIO, SAZONAL, RANKING");
                }
                break;

            case "dar":
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Uso: /titulosadmin dar <jogador> <titulo>");
                    return true;
                }
                OfflinePlayer alvo = Bukkit.getOfflinePlayer(args[1]);
                String tituloNome = args[2];
                if (tituloManager.getTituloPorNome(tituloNome) == null) {
                    sender.sendMessage(ChatColor.RED + "Esse título não existe.");
                    return true;
                }
                tituloManager.darTitulo(alvo.getPlayer(), tituloNome);
                sender.sendMessage(ChatColor.GREEN + "Título " + tituloNome + " dado a " + alvo.getName());
                break;

            case "remover":
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Uso: /titulosadmin remover <jogador> <titulo>");
                    return true;
                }
                OfflinePlayer alvoRemover = Bukkit.getOfflinePlayer(args[1]);
                String tituloRemover = args[2];
                tituloManager.getTitulosDoJogador(alvoRemover.getPlayer()).remove(tituloRemover.toLowerCase());
                sender.sendMessage(ChatColor.YELLOW + "Título " + tituloRemover + " removido de " + alvoRemover.getName());
                break;

            case "listar":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Uso: /titulosadmin listar <jogador>");
                    return true;
                }
                OfflinePlayer alvoListar = Bukkit.getOfflinePlayer(args[1]);
                sender.sendMessage(ChatColor.YELLOW + "Títulos de " + alvoListar.getName() + ":");
                for (String t : tituloManager.getTitulosDoJogador(alvoListar.getPlayer())) {
                    sender.sendMessage(ChatColor.GRAY + "- " + t);
                }
                break;

            default:
                sender.sendMessage(ChatColor.RED + "Subcomando inválido. Use: criar, dar, remover, listar.");
                break;
        }

        return true;
    }
}