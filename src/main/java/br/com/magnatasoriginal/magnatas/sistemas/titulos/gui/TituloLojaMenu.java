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
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TituloLojaMenu {

    private static final String MENU_TITLE = "§6Loja de Títulos";
    private final TituloManager tituloManager;

    public TituloLojaMenu(TituloManager tituloManager) {
        this.tituloManager = tituloManager;
    }

    public Inventory criarMenu(Player player) {
        Collection<Titulo> titulos = tituloManager.getTodosTitulos();// carregados do titulos.yml
        Inventory inv = Bukkit.createInventory(null, 54, MENU_TITLE);

        int slot = 0;
        for (Titulo titulo : titulos) {
            // só mostra se estiver disponível na loja
            if (!titulo.isLoja()) continue;

            ItemStack item = new ItemStack(Material.NAME_TAG);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(titulo.getNomeVisivel()); // já vem colorido do config

                List<String> lore = new ArrayList<>();
                lore.add("§7" + titulo.getDescricao());
                lore.add("§7Método de obtenção: §f" + titulo.getObtencao());
                lore.add("§7Duração: §f" + formatarDuracao(titulo));
                lore.add("§aPreço: " + titulo.getPreco() + " tokens");
                lore.add("§eClique para comprar");

                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            inv.setItem(slot++, item);
        }

        return inv;
    }

    private String formatarDuracao(Titulo titulo) {
        if (titulo.isPermanente()) {
            return "Permanente";
        }
        long millis = titulo.getDuracaoMillis();
        if (millis <= 0) return "Desconhecida";

        long horas = TimeUnit.MILLISECONDS.toHours(millis);
        long dias = TimeUnit.MILLISECONDS.toDays(millis);

        if (dias > 0) {
            return dias + " dia" + (dias > 1 ? "s" : "");
        } else if (horas > 0) {
            return horas + " hora" + (horas > 1 ? "s" : "");
        } else {
            long minutos = TimeUnit.MILLISECONDS.toMinutes(millis);
            return minutos + " minuto" + (minutos > 1 ? "s" : "");
        }
    }

    public static String getMenuTitle() {
        return MENU_TITLE;
    }
}