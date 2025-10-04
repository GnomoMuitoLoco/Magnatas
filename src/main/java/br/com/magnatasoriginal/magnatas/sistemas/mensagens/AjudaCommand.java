package br.com.magnatasoriginal.magnatas.sistemas.mensagens;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class AjudaCommand implements CommandExecutor {

    private final Map<UUID, UUID> chamadosPendentes = new HashMap<>();
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final long cooldownSegundos = 300;
    private final MensagemProvider mensagens;

    public AjudaCommand(MensagemProvider mensagens) {
        this.mensagens = mensagens;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return false;
        Player player = (Player) sender;

        if (args.length == 0) {
            enviarMensagemInterativa(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("solicitar")) {
            return solicitarAjuda(player);
        }

        if (args[0].equalsIgnoreCase("ajudar") && args.length == 2) {
            return atenderAjuda(player, args[1]);
        }

        return false;
    }

    private void enviarMensagemInterativa(Player player) {
        TextComponent msg = new TextComponent(mensagens.get("ajuda.msg_interativa_texto"));
        msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, mensagens.get("ajuda.msg_interativa_comando")));
        msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder(mensagens.get("ajuda.msg_interativa_hover")).create()));
        player.spigot().sendMessage(msg);
    }

    private boolean solicitarAjuda(Player jogador) {
        UUID id = jogador.getUniqueId();
        long agora = System.currentTimeMillis() / 1000;

        if (cooldowns.containsKey(id) && agora - cooldowns.get(id) < cooldownSegundos) {
            jogador.sendMessage(mensagens.get("ajuda.cooldown"));
            return true;
        }

        List<Player> ajudantes = Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.hasPermission("magnatas.staff.ajuda"))
                .collect(Collectors.toList());

        if (ajudantes.isEmpty()) {
            jogador.sendMessage(mensagens.get("ajuda.sem_ajudantes"));
            return true;
        }

        jogador.sendMessage(mensagens.get("ajuda.solicitada"));
        chamadosPendentes.put(id, null);
        cooldowns.put(id, agora);

        for (Player ajudante : ajudantes) {
            TextComponent msg = new TextComponent(mensagens.get("ajuda.msg_para_ajudante"));
            msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/magnatas ajuda ajudar " + jogador.getName()));
            msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new ComponentBuilder(mensagens.get("ajuda.msg_hover_ajudante", jogador.getName())).create()));
            ajudante.spigot().sendMessage(msg);
        }

        return true;
    }

    private boolean atenderAjuda(Player ajudante, String nomeJogador) {
        Player jogador = Bukkit.getPlayer(nomeJogador);
        if (jogador == null) {
            ajudante.sendMessage(mensagens.get("ajuda.jogador_offline"));
            return true;
        }

        UUID id = jogador.getUniqueId();
        if (!chamadosPendentes.containsKey(id) || chamadosPendentes.get(id) != null) {
            ajudante.sendMessage(mensagens.get("ajuda.chamado_respondido"));
            return true;
        }

        ajudante.teleport(jogador.getLocation());
        ajudante.sendMessage(mensagens.get("ajuda.ajudando", jogador.getName()));
        jogador.sendMessage(mensagens.get("ajuda.ajudado"));
        chamadosPendentes.put(id, ajudante.getUniqueId());
        return true;
    }
}