package br.com.magnatasoriginal.magnatas.sistemas.homes;

import br.com.magnatasoriginal.magnatas.Magnatas;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class HomesCommand implements CommandExecutor {

    private final HomeManager manager;
    private static final int MAX_LINE_LENGTH = 50; // Máximo de caracteres por linha de homes

    public HomesCommand(Magnatas plugin) {
        this.manager = plugin.getHomeManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Este comando só pode ser usado por jogadores.");
            return true;
        }

        List<String> homes = new ArrayList<>(manager.listHomes(player));
        if (homes.isEmpty()) {
            player.sendMessage("§cVocê ainda não tem nenhuma home salva.");
            return true;
        }

        // Paginação baseada em comprimento da linha
        List<List<String>> pages = new ArrayList<>();
        List<String> currentPage = new ArrayList<>();
        int currentLength = 0;

        for (String home : homes) {
            int homeLength = home.length() + 2; // +2 por ", "
            if (currentLength + homeLength > MAX_LINE_LENGTH && !currentPage.isEmpty()) {
                pages.add(currentPage);
                currentPage = new ArrayList<>();
                currentLength = 0;
            }
            currentPage.add(home);
            currentLength += homeLength;
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

        List<String> pageHomes = pages.get(page - 1);

        // Cabeçalho
        player.sendMessage("§8=============================================");

        // Linha de homes clicáveis
        TextComponent homesLine = new TextComponent("§fHomes: ");
        for (int i = 0; i < pageHomes.size(); i++) {
            String homeName = pageHomes.get(i);
            TextComponent homeComponent = new TextComponent("§b" + homeName);
            homeComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/home " + homeName));
            homesLine.addExtra(homeComponent);

            if (i < pageHomes.size() - 1) {
                homesLine.addExtra(new TextComponent("§f, "));
            }
        }
        player.spigot().sendMessage(homesLine);

        // Navegação
        TextComponent navLine = new TextComponent("§8==============");

        TextComponent left = new TextComponent((page > 1 ? "§a" : "§c") + "(◀|");
        TextComponent center = new TextComponent("§8=====" + page + "/" + totalPages + "=====");
        TextComponent right = new TextComponent((page < totalPages ? "§a" : "§c") + "|▶)");

        if (page > 1) {
            left.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/homes " + (page - 1)));
        }
        if (page < totalPages) {
            right.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/homes " + (page + 1)));
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