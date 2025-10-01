package br.com.magnatasoriginal.magnatas.sistemas.lojas;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class LojaAtualizadaEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    public LojaAtualizadaEvent() {
        super();
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
