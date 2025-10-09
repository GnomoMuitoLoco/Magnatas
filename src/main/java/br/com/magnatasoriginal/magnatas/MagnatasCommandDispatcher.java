package br.com.magnatasoriginal.magnatas;

import br.com.magnatasoriginal.magnatas.sistemas.mensagens.AjudaCommand;
import br.com.magnatasoriginal.magnatas.sistemas.mensagens.InfoCommand;
import br.com.magnatasoriginal.magnatas.sistemas.mensagens.MensagemProvider;
import br.com.magnatasoriginal.magnatas.sistemas.titulos.TituloManager;
import br.com.magnatasoriginal.magnatas.sistemas.titulos.TituloService;
import br.com.magnatasoriginal.magnatas.sistemas.titulos.commands.TituloCommand;
import br.com.magnatasoriginal.magnatas.sistemas.titulos.commands.TituloCommandAdmin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MagnatasCommandDispatcher implements CommandExecutor {

    private final Map<String, CommandExecutor> subcomandos = new HashMap<>();

    public MagnatasCommandDispatcher(
            Plugin plugin,
            MensagemProvider mensagens,
            BukkitTask tarefaAjuda,
            TituloManager tituloManager,
            TituloService tituloService
    ) {
        // Subcomandos principais
        subcomandos.put("info", new InfoCommand());
        subcomandos.put("ajuda", new AjudaCommand(mensagens));
        subcomandos.put("reload", new ReloadCommand(plugin, mensagens, tarefaAjuda, tituloManager));
        // Aqui você pode adicionar outros atalhos úteis:
        // subcomandos.put("wiki", new WikiCommand());
        // subcomandos.put("discord", new DiscordCommand());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§eUse /magnatas info.");
            return true;
        }

        CommandExecutor executor = subcomandos.get(args[0].toLowerCase());
        if (executor != null) {
            return executor.onCommand(sender, cmd, label, Arrays.copyOfRange(args, 1, args.length));
        }

        sender.sendMessage("§cSubcomando desconhecido. Use /magnatas info.");
        return true;
    }
}