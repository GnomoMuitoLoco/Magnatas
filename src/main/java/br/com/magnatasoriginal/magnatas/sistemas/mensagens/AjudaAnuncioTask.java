package br.com.magnatasoriginal.magnatas.sistemas.mensagens;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.configuration.file.FileConfiguration;

public class AjudaAnuncioTask extends BukkitRunnable {

    private final Plugin plugin;
    private final FileConfiguration config;

    public AjudaAnuncioTask(Plugin plugin, FileConfiguration config) {
        this.plugin = plugin;
        this.config = config;
    }

    @Override
    public void run() {
        String texto = config.getString("mensagens.ajuda_convite.texto", "&ePrecisa de ajuda? &a<Clique aqui> &epara chamar um ajudante.");
        String hover = config.getString("mensagens.ajuda_convite.hover", "&7Clique para solicitar ajuda");
        String comando = config.getString("mensagens.ajuda_convite.comando", "/magnatas ajuda solicitar");

        TextComponent msg = new TextComponent(texto.replace("&", "§"));
        msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, comando));
        msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hover.replace("&", "§")).create()));

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.spigot().sendMessage(msg);
        }
    }
}