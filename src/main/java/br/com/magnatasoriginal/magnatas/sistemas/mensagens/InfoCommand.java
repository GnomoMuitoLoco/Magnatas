package br.com.magnatasoriginal.magnatas.sistemas.mensagens;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class InfoCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return false;
        Player player = (Player) sender;

        player.sendMessage("§8========== §6Servidor Magnatas §8==========");

        TextComponent wiki = new TextComponent("§e• Acessar a Wiki do Servidor");
        wiki.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL,
                "https://servidormagnatas.notion.site/Servidor-Magnatas-Wiki-26857a7ad0ba80d8a498e7d2654ec6df"));
        wiki.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder("§7Clique para abrir a Wiki").create()));

        TextComponent discord = new TextComponent("\n§b• Acessar o Discord");
        discord.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL,
                "https://discord.gg/VewgTM2rNu"));
        discord.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder("§7Clique para entrar no Discord").create()));

        TextComponent site = new TextComponent("\n§a• Acessar o Site");
        site.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL,
                "https://servidormagnatas.com.br/"));
        site.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder("§7Clique para abrir o site").create()));

        TextComponent ajuda = new TextComponent("\n§d• Pedir ajuda");
        ajuda.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/magnatas ajuda"));
        ajuda.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder("§7Clique para solicitar ajuda de um staff online").create()));

        TextComponent token = new TextComponent("\n§6• Sistema de Tokens");
        token.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/token"));
        token.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder("§7Clique para abrir o sistema de tokens").create()));

        TextComponent limite = new TextComponent("\n§c• Limite de blocos (em construção)");
        limite.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder("§7Em breve...").create()));

        TextComponent todos = new TextComponent();
        todos.addExtra(wiki);
        todos.addExtra(discord);
        todos.addExtra(site);
        todos.addExtra(ajuda);
        todos.addExtra(token);
        todos.addExtra(limite);

        player.spigot().sendMessage(todos);
        player.sendMessage("§8===========================================");
        // Rodapé
        player.sendMessage("§7by Magnatas");
        return true;
    }
}