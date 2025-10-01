package br.com.magnatasoriginal.magnatas.sistemas.mensagens;

import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.CommandExecutor;
import br.com.magnatasoriginal.magnatas.ReloadCommand;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public class MagnatasCommandDispatcher implements CommandExecutor {

    private final Map<String, CommandExecutor> subcomandos = new HashMap<>();

    public MagnatasCommandDispatcher(Plugin plugin, BukkitTask tarefaAjuda) {
        subcomandos.put("info", new InfoCommand());
        subcomandos.put("ajuda", new AjudaCommand());
        subcomandos.put("reload", new ReloadCommand(plugin, tarefaAjuda));
        // você pode adicionar outros aqui: wiki, discord, etc.
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§eUse /magnatas [info|ajuda|wiki|discord|site|token|reload]");
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