package br.com.magnatasoriginal.magnatas.sistemas.titulos;

import br.com.magnatasoriginal.magnatas.Magnatas;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.format.DateTimeFormatter;

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
            sender.sendMessage(plugin.getMensagens().get("titulos.apenas_jogadores"));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(plugin.getMensagens().get("titulos.menu_aberto"));
            new TituloGUI(plugin).openMenu(player);
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "info":
                if (args.length < 2) {
                    player.sendMessage(plugin.getMensagens().get("titulos.uso_info"));
                    return true;
                }
                mostrarInfo(player, args[1]);
                break;

            case "equipar":
                if (args.length < 2) {
                    player.sendMessage(plugin.getMensagens().get("titulos.uso_equipar"));
                    return true;
                }
                equiparTitulo(player, args[1]);
                break;

            case "remover":
                tituloManager.removerTitulo(player);
                player.sendMessage(plugin.getMensagens().get("titulos.titulo_removido"));
                break;

            default:
                player.sendMessage(plugin.getMensagens().get("titulos.subcomando_invalido"));
                break;
        }

        return true;
    }

    private void mostrarInfo(Player player, String nomeTitulo) {
        Titulo titulo = tituloManager.getTituloPorNome(nomeTitulo);
        if (titulo == null) {
            player.sendMessage(plugin.getMensagens().get("titulos.titulo_inexistente"));
            return;
        }

        player.sendMessage(plugin.getMensagens().get("titulos.info.cabecalho"));
        player.sendMessage(plugin.getMensagens().get("titulos.info.nome", titulo.getNomeVisivel()));
        player.sendMessage(plugin.getMensagens().get("titulos.info.descricao", titulo.getDescricao()));
        player.sendMessage(plugin.getMensagens().get("titulos.info.obtencao", titulo.getObtencao()));

        if (titulo.isPermanente()) {
            player.sendMessage(plugin.getMensagens().get("titulos.info.duracao_permanente"));
        } else if (titulo.getExpiraEm() != null) {
            String formatado = titulo.getExpiraEm().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            player.sendMessage(plugin.getMensagens().get("titulos.info.expira_em", formatado));
        } else {
            String tempoFormatado = plugin.formatarDuracao(titulo.getDuracaoMillis()); // método utilitário
            player.sendMessage(plugin.getMensagens().get("titulos.info.duracao_formatada", tempoFormatado));
        }
    }

    private void equiparTitulo(Player player, String nomeTitulo) {
        if (!tituloManager.getTitulosDoJogador(player).contains(nomeTitulo.toLowerCase())) {
            player.sendMessage(plugin.getMensagens().get("titulos.titulo_nao_possui"));
            return;
        }

        tituloManager.equiparTitulo(player, nomeTitulo);
        Titulo titulo = tituloManager.getTituloPorNome(nomeTitulo);
        if (titulo != null) {
            player.sendMessage(plugin.getMensagens().get("titulos.titulo_equipado", titulo.getNomeVisivel()));
        }
    }
}