package br.com.magnatasoriginal.magnatas.sistemas.titulos.gui;

import br.com.magnatasoriginal.magnatas.sistemas.titulos.Titulo;
import br.com.magnatasoriginal.magnatas.sistemas.titulos.TituloManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TituloLojaMenu {

    private static final String MENU_TITLE = "§6Loja de Títulos";
    public static final int ITEMS_PER_PAGE = 28; // slots internos válidos
    private final TituloManager tituloManager;

    public TituloLojaMenu(TituloManager tituloManager) {
        this.tituloManager = tituloManager;
    }

    public Inventory criarMenu(Player player, int pagina) {
        List<Titulo> todos = tituloManager.getTodosTitulos().stream()
                .filter(Titulo::isLoja)
                .toList();

        int start = (pagina - 1) * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, todos.size());

        Inventory inv = Bukkit.createInventory(null, 54, MENU_TITLE + " - Página " + pagina);

        // Moldura completa
        ItemStack vidro = criarVidroDecorativo();
        for (int i = 0; i < 9; i++) inv.setItem(i, vidro);
        for (int i = 45; i < 54; i++) inv.setItem(i, vidro);
        for (int i = 9; i < 45; i += 9) {
            inv.setItem(i, vidro);
            inv.setItem(i + 8, vidro);
        }

        // Slots internos válidos (7x4)
        List<Integer> slotsValidos = List.of(
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34,
                37, 38, 39, 40, 41, 42, 43
        );

        for (int i = start; i < end && i - start < slotsValidos.size(); i++) {
            Titulo titulo = todos.get(i);
            ItemStack item = new ItemStack(Material.NAME_TAG);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(titulo.getNomeVisivel());
                List<String> lore = new ArrayList<>();
                lore.add("§7" + titulo.getDescricao());
                lore.add("§7Método de obtenção: §f" + titulo.getObtencao());
                lore.add("§7Duração: §f" + formatarDuracao(titulo));
                lore.add("§aPreço: " + titulo.getPreco() + " tokens");
                lore.add("§eClique com botão esquerdo para comprar");
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            inv.setItem(slotsValidos.get(i - start), item);
        }

        // Botões de navegação
        inv.setItem(48, criarBotao(Material.ARROW, "§ePágina anterior"));
        inv.setItem(50, criarBotao(Material.ARROW, "§ePróxima página"));

        return inv;
    }

    private ItemStack criarVidroDecorativo() {
        ItemStack item = new ItemStack(Material.YELLOW_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            item.setItemMeta(meta);
        }
        return item;
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

    private String formatarDuracao(Titulo titulo) {
        if (titulo.isPermanente()) return "Permanente";
        long millis = titulo.getDuracaoMillis();
        if (millis <= 0) return "Desconhecida";

        long dias = TimeUnit.MILLISECONDS.toDays(millis);
        if (dias > 0) return dias + " dia" + (dias > 1 ? "s" : "");

        long horas = TimeUnit.MILLISECONDS.toHours(millis);
        if (horas > 0) return horas + " hora" + (horas > 1 ? "s" : "");

        long minutos = TimeUnit.MILLISECONDS.toMinutes(millis);
        return minutos + " minuto" + (minutos > 1 ? "s" : "");
    }

    public static String getMenuTitle() {
        return MENU_TITLE;
    }
}