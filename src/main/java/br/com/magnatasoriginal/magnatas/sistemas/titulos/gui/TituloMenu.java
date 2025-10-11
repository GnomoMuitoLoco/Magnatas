package br.com.magnatasoriginal.magnatas.sistemas.titulos.gui;

import br.com.magnatasoriginal.magnatas.Magnatas;
import br.com.magnatasoriginal.magnatas.sistemas.titulos.Titulo;
import br.com.magnatasoriginal.magnatas.sistemas.titulos.TituloService;
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
    private static final int ITEMS_PER_PAGE = 28; // slots internos válidos

    private final TituloService tituloService;

    public TituloMenu(TituloService tituloService) {
        this.tituloService = tituloService;
    }

    public Inventory criarMenu(Player player, List<Titulo> titulos, String equipado, int pagina) {
        Inventory inv = Bukkit.createInventory(null, 54, MENU_TITLE + " - Página " + pagina);

        // Moldura de vidro amarelo
        ItemStack vidro = criarVidro(Material.YELLOW_STAINED_GLASS_PANE);
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                inv.setItem(i, vidro);
            }
        }

        // Slots internos válidos (7x4)
        List<Integer> slotsValidos = List.of(
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34,
                37, 38, 39, 40, 41, 42, 43
        );

        int start = (pagina - 1) * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, titulos.size());

        for (int i = start; i < end && i - start < slotsValidos.size(); i++) {
            Titulo titulo = titulos.get(i);
            if (titulo == null) continue;

            ItemStack item = new ItemStack(Material.NAME_TAG);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(titulo.getNomeVisivel());

                List<String> lore = new ArrayList<>();
                lore.add("§7" + titulo.getDescricao());
                lore.add("§7Obtido em: §f" + titulo.getObtencao());

                long restante = tituloService.getDuracaoRestante(player.getUniqueId(), titulo.getNome());
                if (restante < 0) {
                    lore.add("§7Duração: §aPermanente");
                } else if (restante == 0) {
                    lore.add("§7Duração: §cExpirado");
                } else {
                    lore.add("§7Duração: §f" + Magnatas.formatarDuracao(restante));
                }

                if (titulo.getNome().equalsIgnoreCase(equipado)) {
                    lore.add("§a[Equipado]");
                    lore.add("§cClique direito para desequipar");
                } else {
                    lore.add("§eClique esquerdo para equipar");
                }

                meta.setLore(lore);
                item.setItemMeta(meta);
            }

            inv.setItem(slotsValidos.get(i - start), item);
        }

        // Paginação
        inv.setItem(48, criarBotao(Material.ARROW, "§ePágina anterior"));
        inv.setItem(50, criarBotao(Material.ARROW, "§ePróxima página"));

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

    private ItemStack criarVidro(Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            item.setItemMeta(meta);
        }
        return item;
    }

    public static String getMenuTitle() {
        return MENU_TITLE;
    }
}