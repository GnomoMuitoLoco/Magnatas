package br.com.magnatasoriginal.magnatas.sistemas.lojas;

import br.com.magnatasoriginal.magnatas.Magnatas;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class LojaCommand implements CommandExecutor {
    private final Magnatas plugin;
    private final LojaGUI lojaGUI;

    public LojaCommand(Magnatas plugin) {
        this.plugin = plugin;
        this.lojaGUI = new LojaGUI(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Apenas jogadores podem usar este comando.");
            return true;
        }

        Player player = (Player) sender;

        // Atualiza o menu antes de abrir
        lojaGUI.updateMenu();

        // Abre o menu após atualização
        new BukkitRunnable() {
            @Override
            public void run() {
                lojaGUI.lojaMenu.openMenu(player, 0);
            }
        }.runTaskLater(plugin, 10L); // pequeno delay para garantir que updateMenu conclua

        return true;
    }
}