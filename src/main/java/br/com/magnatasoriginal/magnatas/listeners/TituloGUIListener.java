package br.com.magnatasoriginal.magnatas.listeners;

import br.com.magnatasoriginal.magnatas.Magnatas;
import br.com.magnatasoriginal.magnatas.sistemas.titulos.Titulo;
import br.com.magnatasoriginal.magnatas.sistemas.titulos.TituloMenu;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class TituloGUIListener implements Listener {

    private final Magnatas plugin;
    private final TituloMenu tituloMenu;
    private final NamespacedKey tituloKey;

    public TituloGUIListener(Magnatas plugin, TituloMenu tituloMenu) {
        this.plugin = plugin;
        this.tituloMenu = tituloMenu;
        this.tituloKey = new NamespacedKey(plugin, "titulo_id");
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().startsWith("Títulos dos Magnatas")) return;
        if (event.getClickedInventory() == null || !event.getClickedInventory().equals(event.getView().getTopInventory())) return;

        event.setCancelled(true);

        if (event.getCurrentItem() == null || !event.getCurrentItem().hasItemMeta()) return;

        Player player = (Player) event.getWhoClicked();
        Material itemType = event.getCurrentItem().getType();

        if (itemType == Material.NAME_TAG) {
            ItemMeta meta = event.getCurrentItem().getItemMeta();
            if (meta == null) return;

            // Recupera o id interno salvo no PersistentDataContainer
            String tituloId = meta.getPersistentDataContainer().get(tituloKey, PersistentDataType.STRING);
            if (tituloId == null) {
                player.sendMessage(ChatColor.RED + "Esse título não existe mais.");
                return;
            }

            Titulo titulo = plugin.getTituloManager().getTituloPorNome(tituloId);
            if (titulo == null) {
                player.sendMessage(ChatColor.RED + "Esse título não existe mais.");
                return;
            }

            // Verifica se está expirado
            if (titulo.isExpirado()) {
                player.sendMessage(ChatColor.RED + "Esse título já expirou.");
                return;
            }

            String equipado = plugin.getTituloManager().getTituloEquipado(player);

            if (event.isLeftClick()) {
                plugin.getTituloManager().equiparTitulo(player, titulo.getNome());
                player.sendMessage(ChatColor.GREEN + "Você equipou o título: " + titulo.getNomeVisivel());
            } else if (event.isRightClick()) {
                if (equipado != null && equipado.equalsIgnoreCase(titulo.getNome())) {
                    plugin.getTituloManager().removerTitulo(player);
                    player.sendMessage(ChatColor.YELLOW + "Você removeu seu título atual.");
                } else {
                    player.sendMessage(ChatColor.RED + "Você não está usando esse título.");
                }
            }
        } else if (itemType == Material.ARROW) {
            tituloMenu.handleClick(event);
        }
    }
}