package br.com.magnatasoriginal.magnatas.sistemas.titulos.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class TituloGUIListener implements Listener {

    private final TituloGUIService service;

    public TituloGUIListener(TituloGUIService service) {
        this.service = service;
    }

    @EventHandler
    public void aoClicar(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        if (e.getView().getTitle().startsWith(TituloMenu.getMenuTitle())) {
            e.setCancelled(true);
            if (e.getCurrentItem() != null) {
                service.processarClique(player, e.getSlot());
            }
        }
    }

    @EventHandler
    public void aoFechar(InventoryCloseEvent e) {
        if (e.getView().getTitle().startsWith(TituloMenu.getMenuTitle())) {
            e.getInventory().clear();
        }
    }
}