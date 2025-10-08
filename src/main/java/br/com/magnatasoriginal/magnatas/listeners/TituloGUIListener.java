package br.com.magnatasoriginal.magnatas.listeners;

import br.com.magnatasoriginal.magnatas.Magnatas;
import br.com.magnatasoriginal.magnatas.sistemas.titulos.Titulo;
import br.com.magnatasoriginal.magnatas.sistemas.titulos.TituloMenu;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class TituloGUIListener implements Listener {

    private final Magnatas plugin;
    private final TituloMenu tituloMenu;

    public TituloGUIListener(Magnatas plugin) {
        this.plugin = plugin;
        this.tituloMenu = new TituloMenu();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().startsWith("Títulos dos Magnatas")) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null) return;

            Player player = (Player) event.getWhoClicked();
            Material itemType = event.getCurrentItem().getType();

            if (itemType == Material.NAME_TAG) {
                String tituloNome = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());
                Titulo titulo = plugin.getTituloManager().getTituloPorNome(tituloNome);

                if (titulo == null) {
                    player.sendMessage(ChatColor.RED + "Esse título não existe mais.");
                    return;
                }

                if (event.isLeftClick()) {
                    plugin.getTituloManager().equiparTitulo(player, titulo.getNome());
                    player.sendMessage(ChatColor.GREEN + "Você equipou o título: " + titulo.getNome());
                } else if (event.isRightClick()) {
                    plugin.getTituloManager().removerTitulo(player);
                    player.sendMessage(ChatColor.YELLOW + "Você removeu seu título atual.");
                }
            } else if (itemType == Material.ARROW) {
                tituloMenu.handleClick(event);
            }
        }
    }
}