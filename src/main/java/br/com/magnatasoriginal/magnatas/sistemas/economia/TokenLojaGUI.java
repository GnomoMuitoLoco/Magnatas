package br.com.magnatasoriginal.magnatas.sistemas.economia;

import br.com.magnatasoriginal.magnatas.Magnatas;
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

    public TokenLojaGUI(Magnatas plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void openMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.DARK_GREEN + "Loja de Tokens");

        // Slots centrais da linha do meio: 11, 13, 15
        inv.setItem(11, createIcon(Material.NAME_TAG, ChatColor.GOLD + "Títulos",
                "Compre e equipe títulos exclusivos usando tokens."));
        inv.setItem(13, createIcon(Material.DIAMOND_CHESTPLATE, ChatColor.AQUA + "Cosméticos",
                "Itens visuais e estilos para personalizar seu personagem."));
        inv.setItem(15, createIcon(Material.NETHER_STAR, ChatColor.LIGHT_PURPLE + "Recompensas Sazonais",
                "Prêmios especiais disponíveis apenas nesta temporada."));

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
        if (event.getView().getTitle().equals(ChatColor.DARK_GREEN + "Loja de Tokens")) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null) return;

            Player player = (Player) event.getWhoClicked();
            Material type = event.getCurrentItem().getType();

            if (type == Material.NAME_TAG) {
                player.closeInventory();
                player.sendMessage(ChatColor.YELLOW + "Abrindo loja de Títulos...");
                // Aqui você chama o GUI de títulos
                // plugin.getTituloGUI().openMenu(player);
            } else if (type == Material.DIAMOND_CHESTPLATE) {
                player.closeInventory();
                player.sendMessage(ChatColor.AQUA + "Abrindo loja de Cosméticos...");
                // Aqui você chama o GUI de cosméticos
            } else if (type == Material.NETHER_STAR) {
                player.closeInventory();
                player.sendMessage(ChatColor.LIGHT_PURPLE + "Abrindo loja de Recompensas Sazonais...");
                // Aqui você chama o GUI de recompensas sazonais
            }
        }
    }
}