package br.com.magnatasoriginal.magnatas.sistemas.titulos.gui;

import br.com.magnatasoriginal.magnatas.sistemas.titulos.Titulo;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class TituloMenu {

    private static final String MENU_TITLE = "§6Seus Títulos";
    private static final int ITEMS_PER_PAGE = 45; // 54 slots - 9 reservados para navegação

    public Inventory criarMenu(Player player, List<Titulo> titulos, String equipado, int pagina) {
        Inventory inv = Bukkit.createInventory(null, 54, MENU_TITLE + " - Página " + (pagina + 1));

        int start = pagina * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, titulos.size());

        // Preenche os títulos da página
        for (int i = start; i < end; i++) {
            Titulo titulo = titulos.get(i);
            if (titulo == null) continue;

            ItemStack item = new ItemStack(Material.NAME_TAG);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(titulo.getNomeVisivel());

                List<String> lore = new ArrayList<>();
                lore.add("§7" + titulo.getDescricao());
                if (titulo.getNome().equalsIgnoreCase(equipado)) {
                    lore.add("§a[Equipado]");
                } else {
                    lore.add("§eClique para equipar");
                }

                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            inv.setItem(i - start, item);
        }

        // Botão de página anterior
        if (pagina > 0) {
            inv.setItem(45, criarBotao(Material.ARROW, "§ePágina anterior"));
        }

        // Botão de próxima página
        if (end < titulos.size()) {
            inv.setItem(53, criarBotao(Material.ARROW, "§ePróxima página"));
        }

        return inv;
    }

    private ItemStack criarBotao(Material material, String nome) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(nome);
            item.setItemMeta(meta);
        }
        return item;
    }

    public static String getMenuTitle() {
        return MENU_TITLE;
    }
}