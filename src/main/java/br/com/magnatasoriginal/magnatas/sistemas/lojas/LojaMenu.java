package br.com.magnatasoriginal.magnatas.sistemas.lojas;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class LojaMenu {

    private final List<Inventory> pages = new ArrayList<>();
    private int currentPage = 0;

    private ItemStack previousPageIcon;
    private ItemStack nextPageIcon;
    private final ItemStack borderItem;

    public LojaMenu() {
        borderItem = createIcon(Material.YELLOW_STAINED_GLASS_PANE, " ");
        setDefaultIcons();
        createPages(1); // Inicializa com 1 página vazia
    }

    public void createPages(int totalPages) {
        pages.clear();
        for (int i = 0; i < totalPages; i++) {
            Inventory inv = Bukkit.createInventory(null, 54, "Lojas dos Magnatas - Página " + (i + 1));
            addBorders(inv);
            addIcons(inv);
            pages.add(inv);
        }
    }

    private void setDefaultIcons() {
        previousPageIcon = createIcon(Material.ARROW, "Voltar Página");
        nextPageIcon = createIcon(Material.ARROW, "Avançar Página");
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

    public void addBorders(Inventory inv) {
        int[] borderSlots = {
                0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 26,
                27, 35, 36, 44, 46, 47, 48, 49, 50, 51, 52
        };
        for (int slot : borderSlots) {
            if (inv.getItem(slot) == null) {
                inv.setItem(slot, borderItem);
            }
        }
    }

    private void addIcons(Inventory inv) {
        if (inv.getItem(45) == null) inv.setItem(45, previousPageIcon);
        if (inv.getItem(46) == null) inv.setItem(46, borderItem);
        if (inv.getItem(53) == null) inv.setItem(53, nextPageIcon);
    }

    public void setPreviousPageIcon(Material material, String name) {
        previousPageIcon = createIcon(material, name);
        for (Inventory page : pages) {
            page.setItem(45, previousPageIcon);
        }
    }

    public void setNextPageIcon(Material material, String name) {
        nextPageIcon = createIcon(material, name);
        for (Inventory page : pages) {
            page.setItem(53, nextPageIcon);
        }
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

    public int getTotalPages() {
        return pages.size();
    }

    public Inventory[] getPages() {
        return pages.toArray(new Inventory[0]);
    }

    public ItemStack getPreviousPageIcon() {
        return previousPageIcon;
    }

    public ItemStack getNextPageIcon() {
        return nextPageIcon;
    }
}