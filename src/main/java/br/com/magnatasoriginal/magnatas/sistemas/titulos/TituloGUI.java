package br.com.magnatasoriginal.magnatas.sistemas.titulos;

import br.com.magnatasoriginal.magnatas.Magnatas;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.time.format.DateTimeFormatter;
import java.util.*;

public class TituloGUI implements Listener {

    private final Magnatas plugin;
    private final TituloMenu tituloMenu;

    public TituloGUI(Magnatas plugin) {
        this.plugin = plugin;
        this.tituloMenu = new TituloMenu();
    }

    public void openMenu(Player player) {
        Set<String> titulosDoJogador = plugin.getTituloManager().getTitulosDoJogador(player);
        int itemsPerPage = 28;
        int totalPages = Math.max(1, (int) Math.ceil((double) titulosDoJogador.size() / itemsPerPage));

        tituloMenu.createPages(totalPages);

        List<String> lista = new ArrayList<>(titulosDoJogador);
        for (int i = 0; i < lista.size(); i++) {
            String nomeTitulo = lista.get(i);
            Titulo titulo = plugin.getTituloManager().getTituloPorNome(nomeTitulo);
            if (titulo == null) continue;

            int pageIndex = i / itemsPerPage;
            if (pageIndex >= tituloMenu.getTotalPages()) break;

            Inventory page = tituloMenu.getPages()[pageIndex];
            createTituloItem(titulo, page, player);
        }

        player.openInventory(tituloMenu.getPages()[0]);
    }

    private void createTituloItem(Titulo titulo, Inventory page, Player player) {
        ItemStack item = new ItemStack(Material.NAME_TAG, 1);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        meta.setDisplayName(titulo.getNomeVisivel());

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + titulo.getDescricao());
        lore.add(ChatColor.AQUA + "Obtenção: " + ChatColor.WHITE + titulo.getObtencao());

        if (titulo.isPermanente()) {
            lore.add(ChatColor.GREEN + "Duração: Permanente");
        } else if (titulo.getExpiraEm() != null) {
            String formatado = titulo.getExpiraEm().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            lore.add(ChatColor.RED + "Expira em: " + formatado);
        } else {
            String tempoFormatado = plugin.formatarDuracao(titulo.getDuracaoMillis());
            lore.add(ChatColor.RED + "Duração: " + tempoFormatado);
        }

        lore.add("");
        lore.add(ChatColor.GREEN + "Clique esquerdo para equipar");
        lore.add(ChatColor.RED + "Clique direito para remover");

        meta.setLore(lore);
        item.setItemMeta(meta);

        int slot = getNextAvailableSlot(page);
        if (slot != -1) {
            page.setItem(slot, item);
        }
    }

    private int getNextAvailableSlot(Inventory inventory) {
        int[] validSlots = {
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34,
                37, 38, 39, 40, 41, 42, 43
        };
        for (int slot : validSlots) {
            if (inventory.getItem(slot) == null) {
                return slot;
            }
        }
        return -1;
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
                    player.sendMessage(ChatColor.GREEN + "Você equipou o título: " + titulo.getNomeVisivel());
                } else if (event.isRightClick()) {
                    String equipado = plugin.getTituloManager().getTituloEquipado(player);
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
}