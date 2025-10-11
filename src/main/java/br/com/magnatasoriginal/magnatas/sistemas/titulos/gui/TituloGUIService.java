package br.com.magnatasoriginal.magnatas.sistemas.titulos.gui;

import br.com.magnatasoriginal.magnatas.Magnatas;
import br.com.magnatasoriginal.magnatas.sistemas.titulos.Titulo;
import br.com.magnatasoriginal.magnatas.sistemas.titulos.TituloService;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class TituloGUIService {

    private final Magnatas plugin;
    private final TituloService tituloService;
    private final TituloMenu menu;

    public TituloGUIService(Magnatas plugin, TituloService tituloService, TituloMenu menu) {
        this.plugin = plugin;
        this.tituloService = tituloService;
        this.menu = menu;
    }

    public void abrirMenu(Player player) {
        abrirMenu(player, 1);
    }

    public void abrirMenu(Player player, int pagina) {
        UUID uuid = player.getUniqueId();

        List<Titulo> titulos = tituloService.listarTitulos(uuid).stream()
                .map(nome -> tituloService.getManager().getTituloPorNome(nome).orElse(null))
                .filter(t -> t != null)
                .collect(Collectors.toList());

        String equipado = tituloService.getTituloEquipado(uuid).orElse(null);
        Inventory inv = menu.criarMenu(player, titulos, equipado, pagina);
        player.openInventory(inv);
    }

    public void processarClique(Player player, int slot) {
        Inventory inv = player.getOpenInventory().getTopInventory();
        String title = player.getOpenInventory().getTitle();

        // Detectar página atual
        int pagina = 1;
        if (title.contains("Página")) {
            try {
                pagina = Integer.parseInt(title.substring(title.lastIndexOf(" ") + 1));
            } catch (NumberFormatException ignored) {}
        }

        // Paginação
        if (slot == 48) {
            abrirMenu(player, Math.max(1, pagina - 1));
            return;
        }
        if (slot == 50) {
            abrirMenu(player, pagina + 1);
            return;
        }

        // Ignorar moldura
        if (inv.getItem(slot) == null || inv.getItem(slot).getType() == Material.YELLOW_STAINED_GLASS_PANE) return;

        ItemStack item = inv.getItem(slot);
        if (item == null || item.getType() != Material.NAME_TAG) return;

        String nomeVisivel = item.getItemMeta().getDisplayName();
        Titulo titulo = tituloService.getManager().getTituloPorNomeVisivel(nomeVisivel).orElse(null);
        if (titulo == null) return;

        UUID uuid = player.getUniqueId();

        // Verifica se o jogador possui e se não está expirado
        if (!tituloService.listarTitulos(uuid).contains(titulo.getNome())) {
            player.sendMessage("§cVocê não possui este título.");
            return;
        }
        if (tituloService.isExpirado(uuid, titulo)) {
            player.sendMessage("§cEste título expirou.");
            return;
        }

        // Equipar ou desequipar
        String equipado = tituloService.getTituloEquipado(uuid).orElse(null);
        if (equipado != null && equipado.equalsIgnoreCase(titulo.getNome())) {
            tituloService.removerTituloEquipado(uuid);
            player.sendMessage("§eVocê desequipou o título.");
        } else {
            boolean sucesso = tituloService.equiparTitulo(player, titulo.getNome());
            if (sucesso) {
                player.sendMessage("§aVocê equipou o título: " + titulo.getNomeVisivel());
            } else {
                player.sendMessage("§cNão foi possível equipar este título.");
            }
        }

        abrirMenu(player, pagina); // Atualiza a mesma página
    }
}