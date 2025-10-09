package br.com.magnatasoriginal.magnatas.sistemas.titulos;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class TituloExpiradoEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final Titulo titulo;

    public TituloExpiradoEvent(Player player, Titulo titulo) {
        this.player = player;
        this.titulo = titulo;
    }

    public Player getPlayer() {
        return player;
    }

    public Titulo getTitulo() {
        return titulo;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}