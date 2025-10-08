package br.com.magnatasoriginal.magnatas.sistemas.mensagens;

import br.com.magnatasoriginal.magnatas.Magnatas;
import br.com.magnatasoriginal.magnatas.ReloadCommand;
import br.com.magnatasoriginal.magnatas.sistemas.titulos.TituloCommand;
import br.com.magnatasoriginal.magnatas.sistemas.titulos.TituloCommandAdmin;
import br.com.magnatasoriginal.magnatas.sistemas.titulos.TituloManager;
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

    public MagnatasCommandDispatcher(Plugin plugin, MensagemProvider mensagens, BukkitTask tarefaAjuda, TituloManager tituloManager) {
        subcomandos.put("info", new InfoCommand());
        subcomandos.put("ajuda", new AjudaCommand(mensagens));
        subcomandos.put("reload", new ReloadCommand(plugin, mensagens, tarefaAjuda));
        subcomandos.put("títulosadmin", new TituloCommandAdmin((Magnatas) plugin, tituloManager));
        subcomandos.put("titulosadmin", new TituloCommandAdmin((Magnatas) plugin, tituloManager));
        subcomandos.put("titulos", new TituloCommand((Magnatas) plugin, tituloManager));      // sem acento
        subcomandos.put("títulos", new TituloCommand((Magnatas) plugin, tituloManager));     // com acento
        // você pode adicionar outros aqui: wiki, discord, etc.
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§eUse o comando /magnatas info.");
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