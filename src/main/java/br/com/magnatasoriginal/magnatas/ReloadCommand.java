package br.com.magnatasoriginal.magnatas;

import br.com.magnatasoriginal.magnatas.sistemas.mensagens.AjudaAnuncioTask;
import br.com.magnatasoriginal.magnatas.sistemas.mensagens.MensagemProvider;
import br.com.magnatasoriginal.magnatas.sistemas.titulos.TituloManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public class ReloadCommand implements CommandExecutor {

    private final Plugin plugin;
    private BukkitTask tarefaAjuda;
    private final MensagemProvider mensagens;
    private final TituloManager tituloManager;

    public ReloadCommand(Plugin plugin, MensagemProvider mensagens, BukkitTask tarefaAjuda, TituloManager tituloManager) {
        this.plugin = plugin;
        this.mensagens = mensagens;
        this.tarefaAjuda = tarefaAjuda;
        this.tituloManager = tituloManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("magnatas.reload")) {
            sender.sendMessage("§cVocê não tem permissão para executar este comando.");
            return true;
        }

        plugin.reloadConfig();
        tituloManager.recarregarTitulos(); // 🔥 recarrega os títulos

        if (tarefaAjuda != null) {
            tarefaAjuda.cancel();
        }

        int intervalo = plugin.getConfig().getInt("messages.ajuda_convite_intervalo", 300);
        tarefaAjuda = new AjudaAnuncioTask(plugin, mensagens)
                .runTaskTimer(plugin, 20L * intervalo, 20L * intervalo);

        sender.sendMessage("§aPlugin Magnatas reiniciado com sucesso.");
        return true;
    }
}