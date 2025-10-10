package br.com.magnatasoriginal.magnatas.sistemas.economia;

import br.com.magnatasoriginal.magnatas.Magnatas;
import br.com.magnatasoriginal.magnatas.sistemas.titulos.gui.TituloGUIService;
import br.com.magnatasoriginal.magnatas.sistemas.titulos.gui.TituloLojaMenu;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class TokenLojaGUI implements Listener {

    private final Magnatas plugin;
    private final TituloGUIService tituloGUIService;

    public TokenLojaGUI(Magnatas plugin, TituloGUIService tituloGUIService) {
        this.plugin = plugin;
        this.tituloGUIService = tituloGUIService;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void openMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.DARK_GREEN + "Loja de Tokens");

        inv.setItem(11, createIcon(Material.NAME_TAG, ChatColor.GOLD + "Títulos",
                "Compre e equipe títulos exclusivos usando tokens."));
        inv.setItem(13, createIcon(Material.DIAMOND_CHESTPLATE, ChatColor.AQUA + "Cosméticos",
                "Itens visuais e estilos para personalizar seu personagem."));
        inv.setItem(15, createIcon(Material.NETHER_STAR, ChatColor.LIGHT_PURPLE + "Recompensas Sazonais",
                "Prêmios especiais disponíveis temporariamente."));

        player.openInventory(inv);
    }

    private ItemStack createIcon(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(Arrays.asList(lore));
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(ChatColor.DARK_GREEN + "Loja de Tokens")) return;

        event.setCancelled(true);

        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() == Material.AIR || !item.hasItemMeta()) return;

        Player player = (Player) event.getWhoClicked();
        Material type = item.getType();

        if (event.getClickedInventory() == null || !event.getClickedInventory().equals(event.getView().getTopInventory())) return;

        if (type == Material.NAME_TAG) {
            player.closeInventory();
            player.sendMessage(ChatColor.YELLOW + "Abrindo loja de Títulos...");
            TituloLojaMenu lojaMenu = new TituloLojaMenu(plugin.getTituloManager());
            player.openInventory(lojaMenu.criarMenu(player, 1)); // ✅ corrigido
        } else if (type == Material.DIAMOND_CHESTPLATE) {
            player.closeInventory();
            player.sendMessage(ChatColor.AQUA + "Abrindo loja de Cosméticos...");
        } else if (type == Material.NETHER_STAR) {
            player.closeInventory();
            player.sendMessage(ChatColor.LIGHT_PURPLE + "Abrindo loja de Recompensas Sazonais...");
        }
    }
}