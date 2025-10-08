package br.com.magnatasoriginal.magnatas.sistemas.titulos;

import br.com.magnatasoriginal.magnatas.Magnatas;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Arrays;

public class TituloCommandAdmin implements CommandExecutor {

    private final Magnatas plugin;
    private final TituloManager tituloManager;

    public TituloCommandAdmin(Magnatas plugin, TituloManager tituloManager) {
        this.plugin = plugin;
        this.tituloManager = tituloManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!sender.hasPermission("magnatas.admin.titulos")) {
            sender.sendMessage(plugin.getMensagens().get("titulos.admin.sem_permissao"));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(plugin.getMensagens().get("titulos.admin.uso_geral"));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "criar":
                if (args.length < 4) {
                    sender.sendMessage(plugin.getMensagens().get("titulos.admin.uso_criar"));
                    return true;
                }

                String nome = args[1];
                String duracaoStr = args[2];
                String descricao = String.join(" ", Arrays.copyOfRange(args, 3, args.length));

                long duracaoMillis = TituloConfigLoader.parseDuracao(duracaoStr);
                Titulo titulo = new Titulo(
                        nome,
                        plugin.colorir(nome),
                        plugin.colorir(descricao),
                        "magnatas.titulos." + nome.toLowerCase(),
                        nome.toLowerCase(),
                        "Manual",
                        duracaoMillis
                );

                tituloManager.registrarTitulo(titulo);
                sender.sendMessage(plugin.getMensagens().get("titulos.admin.criado", titulo.getNomeVisivel()));
                break;

            case "apagar":
                if (args.length < 2) {
                    sender.sendMessage(plugin.getMensagens().get("titulos.admin.uso_apagar"));
                    return true;
                }
                String nomeApagar = args[1].toLowerCase();
                if (tituloManager.getTituloPorNome(nomeApagar) == null) {
                    sender.sendMessage(plugin.getMensagens().get("titulos.titulo_inexistente"));
                    return true;
                }
                tituloManager.apagarTitulo(nomeApagar);
                sender.sendMessage(plugin.getMensagens().get("titulos.admin.apagado", nomeApagar));
                break;

            case "dar":
                if (args.length < 3) {
                    sender.sendMessage(plugin.getMensagens().get("titulos.admin.uso_dar"));
                    return true;
                }

                OfflinePlayer alvo = Bukkit.getOfflinePlayer(args[1]);
                String tituloNome = args[2];

                if (tituloManager.getTituloPorNome(tituloNome) == null) {
                    sender.sendMessage(plugin.getMensagens().get("titulos.titulo_inexistente"));
                    return true;
                }

                tituloManager.darTitulo(alvo.getPlayer(), tituloNome);
                sender.sendMessage(plugin.getMensagens().get("titulos.admin.dado", tituloNome, alvo.getName()));
                break;

            case "remover":
                if (args.length < 3) {
                    sender.sendMessage(plugin.getMensagens().get("titulos.admin.uso_remover"));
                    return true;
                }

                OfflinePlayer alvoRemover = Bukkit.getOfflinePlayer(args[1]);
                String tituloRemover = args[2];

                tituloManager.getTitulosDoJogador(alvoRemover.getPlayer()).remove(tituloRemover.toLowerCase());
                sender.sendMessage(plugin.getMensagens().get("titulos.admin.removido", tituloRemover, alvoRemover.getName()));
                break;

            case "listar":
                if (args.length < 2) {
                    sender.sendMessage(plugin.getMensagens().get("titulos.admin.uso_listar"));
                    return true;
                }

                OfflinePlayer alvoListar = Bukkit.getOfflinePlayer(args[1]);
                sender.sendMessage(plugin.getMensagens().get("titulos.admin.listando", alvoListar.getName()));
                for (String t : tituloManager.getTitulosDoJogador(alvoListar.getPlayer())) {
                    Titulo tObj = tituloManager.getTituloPorNome(t);
                    String nomeFormatado = tObj != null ? tObj.getNomeVisivel() : t;
                    sender.sendMessage("  §7- " + nomeFormatado);
                }
                break;

            default:
                sender.sendMessage(plugin.getMensagens().get("titulos.admin.subcomando_invalido"));
                break;
        }

        return true;
    }
}