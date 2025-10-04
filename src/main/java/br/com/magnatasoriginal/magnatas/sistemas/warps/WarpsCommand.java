package br.com.magnatasoriginal.magnatas.sistemas.warps;

import br.com.magnatasoriginal.magnatas.Magnatas;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class WarpsCommand implements CommandExecutor {

    private final WarpManager manager;
    private static final int MAX_LINE_LENGTH = 50;

    public WarpsCommand(Magnatas plugin) {
        this.manager = plugin.getWarpManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Este comando só pode ser usado por jogadores.");
            return true;
        }

        List<String> warps = new ArrayList<>(manager.listWarps());
        if (warps.isEmpty()) {
            player.sendMessage("§cNão há warps disponíveis.");
            return true;
        }

        List<List<String>> pages = new ArrayList<>();
        List<String> currentPage = new ArrayList<>();
        int currentLength = 0;

        for (String warp : warps) {
            int warpLength = warp.length() + 2;
            if (currentLength + warpLength > MAX_LINE_LENGTH && !currentPage.isEmpty()) {
                pages.add(currentPage);
                currentPage = new ArrayList<>();
                currentLength = 0;
            }
            currentPage.add(warp);
            currentLength += warpLength;
        }
        if (!currentPage.isEmpty()) {
            pages.add(currentPage);
        }

        int totalPages = pages.size();
        int page = 1;

        if (args.length >= 1) {
            try {
                page = Integer.parseInt(args[0]);
                if (page < 1 || page > totalPages) page = 1;
            } catch (NumberFormatException ignored) {}
        }

        List<String> pageWarps = pages.get(page - 1);

        player.sendMessage("§8=============================================");

        TextComponent warpsLine = new TextComponent("§fWarps: ");
        for (int i = 0; i < pageWarps.size(); i++) {
            String warpName = pageWarps.get(i);
            TextComponent warpComponent = new TextComponent("§b" + warpName);
            warpComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/warp " + warpName));
            warpsLine.addExtra(warpComponent);

            if (i < pageWarps.size() - 1) {
                warpsLine.addExtra(new TextComponent("§f, "));
            }
        }
        player.spigot().sendMessage(warpsLine);

        TextComponent navLine = new TextComponent("§8==============");

        TextComponent left = new TextComponent((page > 1 ? "§a" : "§c") + "(◀|");
        TextComponent center = new TextComponent("§8=====" + page + "/" + totalPages + "=====");
        TextComponent right = new TextComponent((page < totalPages ? "§a" : "§c") + "|▶)");

        if (page > 1) {
            left.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/warps " + (page - 1)));
        }
        if (page < totalPages) {
            right.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/warps " + (page + 1)));
        }

        TextComponent fullNav = new TextComponent();
        fullNav.addExtra(navLine);
        fullNav.addExtra(left);
        fullNav.addExtra(center);
        fullNav.addExtra(right);
        fullNav.addExtra(navLine);

        player.spigot().sendMessage(fullNav);

        player.sendMessage("§7by Magnatas");

        return true;
    }
}