package br.com.magnatasoriginal.magnatas.sistemas.titulos.commands;

import br.com.magnatasoriginal.magnatas.sistemas.titulos.TituloService;
import br.com.magnatasoriginal.magnatas.sistemas.titulos.Titulo;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;

public class TituloCommandHandler {

    private final TituloService service;

    public TituloCommandHandler(TituloService service) {
        this.service = service;
    }

    public TituloService getService() {
        return service;
    }

    public void listarTitulos(Player player) {
        var titulos = service.listarTitulos(player.getUniqueId());
        if (titulos.isEmpty()) {
            player.sendMessage("§cVocê não possui títulos.");
            return;
        }
        player.sendMessage("§aSeus títulos:");
        titulos.forEach(nome -> {
            Optional<Titulo> t = service.getManager().getTituloPorNome(nome);
            t.ifPresent(titulo -> player.sendMessage(" §7- " + titulo.getNomeVisivel()));
        });
    }

    public void equiparTitulo(Player player, String tituloNome) {
        boolean equipado = service.equiparTitulo(player, tituloNome);
        if (equipado) {
            player.sendMessage("§aVocê equipou o título: " + tituloNome);
        } else {
            player.sendMessage("§cNão foi possível equipar este título.");
        }
    }

    public void darTitulo(CommandSender sender, UUID alvo, String tituloNome) {
        boolean deu = service.concederTitulo(alvo, tituloNome);
        if (deu) {
            sender.sendMessage("§aTítulo concedido com sucesso.");
        } else {
            sender.sendMessage("§cO jogador já possui este título.");
        }
    }

    public void removerTitulo(CommandSender sender, UUID alvo, String tituloNome) {
        boolean removido = service.removerTitulo(alvo, tituloNome);
        if (removido) {
            sender.sendMessage("§aTítulo removido com sucesso.");
        } else {
            sender.sendMessage("§cO jogador não possui este título.");
        }
    }
}