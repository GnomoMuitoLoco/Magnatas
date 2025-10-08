package br.com.magnatasoriginal.magnatas.sistemas.titulos;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class TituloMenu {

    private final List<Inventory> pages = new ArrayList<>();
    private int currentPage = 0;

    private final ItemStack previousPageIcon;
    private final ItemStack nextPageIcon;
    private final ItemStack borderItem;

    public TituloMenu() {
        borderItem = createIcon(Material.GRAY_STAINED_GLASS_PANE, " ");
        previousPageIcon = createIcon(Material.ARROW, "Voltar Página");
        nextPageIcon = createIcon(Material.ARROW, "Avançar Página");
        createPages(1);
    }

    public void createPages(int totalPages) {
        pages.clear();
        for (int i = 0; i < totalPages; i++) {
            Inventory inv = Bukkit.createInventory(null, 54, "Títulos dos Magnatas - Página " + (i + 1));
            addBorders(inv);
            addIcons(inv);
            pages.add(inv);
        }
    }

    private ItemStack createIcon(Material material, String name) {
        ItemStack icon = new ItemStack(material);
        ItemMeta meta = icon.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            icon.setItemMeta(meta);
        }
        return icon;
    }

    private void addBorders(Inventory inv) {
        int[] borderSlots = {
                0,1,2,3,4,5,6,7,8,9,17,18,26,
                27,35,36,44,46,47,48,49,50,51,52
        };
        for (int slot : borderSlots) {
            inv.setItem(slot, borderItem);
        }
    }

    private void addIcons(Inventory inv) {
        inv.setItem(45, previousPageIcon);
        inv.setItem(53, nextPageIcon);
    }

    public void openMenu(Player player, int page) {
        if (page >= 0 && page < pages.size()) {
            currentPage = page;
            player.openInventory(pages.get(page));
        }
    }

    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();

        if (slot == 45) {
            int previousPage = (currentPage - 1 + pages.size()) % pages.size();
            openMenu(player, previousPage);
        } else if (slot == 53) {
            int nextPage = (currentPage + 1) % pages.size();
            openMenu(player, nextPage);
        }
    }

    public Inventory[] getPages() {
        return pages.toArray(new Inventory[0]);
    }

    public int getTotalPages() {
        return pages.size();
    }
}