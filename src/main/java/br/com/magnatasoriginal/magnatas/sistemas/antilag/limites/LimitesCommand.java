package br.com.magnatasoriginal.magnatas.sistemas.antilag.limites;

import br.com.magnatasoriginal.magnatas.Magnatas;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class LimitesCommand implements CommandExecutor {

    private final LimitesManager limitesManager;
    private static final int MAX_ITEMS_PER_PAGE = 5;

    public LimitesCommand(LimitesManager limitesManager, Plugin plugin) {
        this.limitesManager = limitesManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Este comando só pode ser usado por jogadores.");
            return true;
        }

        Map<String, Integer> blocos = limitesManager.getTodosLimites();
        if (blocos.isEmpty()) {
            player.sendMessage("§cNenhum bloco está limitado no momento.");
            return true;
        }

        List<String> blocosFormatados = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : blocos.entrySet()) {
            blocosFormatados.add("§7- §b" + entry.getKey() + " §f(" + entry.getValue() + " por chunk)");
        }

        int totalPages = (int) Math.ceil((double) blocosFormatados.size() / MAX_ITEMS_PER_PAGE);
        int page = 1;

        if (args.length >= 1) {
            try {
                page = Integer.parseInt(args[0]);
                if (page < 1 || page > totalPages) page = 1;
            } catch (NumberFormatException ignored) {}
        }

        int start = (page - 1) * MAX_ITEMS_PER_PAGE;
        int end = Math.min(start + MAX_ITEMS_PER_PAGE, blocosFormatados.size());
        List<String> pageBlocos = blocosFormatados.subList(start, end);

        // Cabeçalho
        player.sendMessage("§8========== §fBlocos Limitados §8==========");

        // Lista vertical
        for (String linha : pageBlocos) {
            player.sendMessage(linha);
        }

        // Navegação
        TextComponent navLine = new TextComponent("§8==============");

        TextComponent left = new TextComponent((page > 1 ? "§a" : "§c") + "(◀|");
        TextComponent center = new TextComponent("§8=====" + page + "/" + totalPages + "=====");
        TextComponent right = new TextComponent((page < totalPages ? "§a" : "§c") + "|▶)");

        if (page > 1) {
            left.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/limites " + (page - 1)));
        }
        if (page < totalPages) {
            right.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/limites " + (page + 1)));
        }

        TextComponent fullNav = new TextComponent();
        fullNav.addExtra(navLine);
        fullNav.addExtra(left);
        fullNav.addExtra(center);
        fullNav.addExtra(right);
        fullNav.addExtra(navLine);

        player.spigot().sendMessage(fullNav);

        // Rodapé
        player.sendMessage("§7by Magnatas");

        return true;
    }
}