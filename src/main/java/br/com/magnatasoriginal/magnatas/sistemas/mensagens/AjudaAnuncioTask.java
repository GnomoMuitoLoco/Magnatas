package br.com.magnatasoriginal.magnatas.sistemas.mensagens;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

    public class AjudaAnuncioTask extends BukkitRunnable {

        private final Plugin plugin;
        private final MensagemProvider mensagens;

        public AjudaAnuncioTask(Plugin plugin, MensagemProvider mensagens) {
            this.plugin = plugin;
            this.mensagens = mensagens;
        }

        @Override
        public void run() {
            String texto = mensagens.get("ajuda.texto");
            String hover = mensagens.get("ajuda.hover");
            String comando = mensagens.get("ajuda.comando");

            TextComponent msg = new TextComponent(texto);
            msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, comando));
            msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hover).create()));

            for (Player player : Bukkit.getOnlinePlayers()) {
                player.spigot().sendMessage(msg);
            }
        }
    }