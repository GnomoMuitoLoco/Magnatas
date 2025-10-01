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
        TextComponent msg = new TextComponent("§ePrecisa de ajuda? §a<Clique aqui> §epara chamar um ajudante.");
        msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/magnatas ajuda solicitar"));
        msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder("§7Clique para solicitar ajuda").create()));
        player.spigot().sendMessage(msg);
    }

    private boolean solicitarAjuda(Player jogador) {
        UUID id = jogador.getUniqueId();
        long agora = System.currentTimeMillis() / 1000;

        if (cooldowns.containsKey(id) && agora - cooldowns.get(id) < cooldownSegundos) {
            jogador.sendMessage("§cVocê já solicitou ajuda recentemente. Aguarde um pouco.");
            return true;
        }

        List<Player> ajudantes = Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.hasPermission("magnatas.staff.ajuda"))
                .collect(Collectors.toList());

        if (ajudantes.isEmpty()) {
            jogador.sendMessage("§cNão há nenhum ajudante online no momento.");
            return true;
        }

        jogador.sendMessage("§aVocê solicitou ajuda, aguarde, um ajudante irá até você...");
        chamadosPendentes.put(id, null);
        cooldowns.put(id, agora);

        for (Player ajudante : ajudantes) {
            TextComponent msg = new TextComponent("§eUm jogador está solicitando ajuda. §a<Clique aqui> §epara ajudá-lo.");
            msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/magnatas ajuda ajudar " + jogador.getName()));
            msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new ComponentBuilder("§7Clique para teleportar até " + jogador.getName()).create()));
            ajudante.spigot().sendMessage(msg);
        }

        return true;
    }

    private boolean atenderAjuda(Player ajudante, String nomeJogador) {
        Player jogador = Bukkit.getPlayer(nomeJogador);
        if (jogador == null) {
            ajudante.sendMessage("§cJogador não encontrado ou offline.");
            return true;
        }

        UUID id = jogador.getUniqueId();
        if (!chamadosPendentes.containsKey(id)) {
            ajudante.sendMessage("§cOutro ajudante já respondeu a este chamado.");
            return true;
        }

        if (chamadosPendentes.get(id) != null) {
            ajudante.sendMessage("§cOutro ajudante já respondeu a este chamado.");
            return true;
        }

        ajudante.teleport(jogador.getLocation());
        ajudante.sendMessage("§aVocê está ajudando " + jogador.getName() + ".");
        jogador.sendMessage("§aUm ajudante está vindo te ajudar!");
        chamadosPendentes.put(id, ajudante.getUniqueId());
        return true;
    }
}