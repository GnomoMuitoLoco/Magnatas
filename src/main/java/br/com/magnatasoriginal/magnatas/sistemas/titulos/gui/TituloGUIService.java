package br.com.magnatasoriginal.magnatas.sistemas.titulos.gui;

import br.com.magnatasoriginal.magnatas.Magnatas;
import br.com.magnatasoriginal.magnatas.sistemas.titulos.Titulo;
import br.com.magnatasoriginal.magnatas.sistemas.titulos.TituloManager;
import br.com.magnatasoriginal.magnatas.sistemas.titulos.TituloService;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

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
        UUID uuid = player.getUniqueId();
        List<Titulo> titulos = tituloService.listarTitulos(uuid).stream()
                .map(nome -> tituloService.getManager().getTituloPorNome(nome).orElse(null))
                .filter(t -> t != null && !t.isExpirado())
                .collect(Collectors.toList());

        String equipado = tituloService.getTituloEquipado(uuid).orElse(null);
        Inventory inv = menu.criarMenu(player, titulos, equipado, 0); // abre sempre na página 0
        player.openInventory(inv);
    }

    public void processarClique(Player player, int slot) {
        UUID uuid = player.getUniqueId();
        List<String> titulos = tituloService.listarTitulos(uuid).stream().toList();

        if (slot >= 0 && slot < titulos.size()) {
            String tituloNome = titulos.get(slot);
            boolean equipado = tituloService.equiparTitulo(player, tituloNome);
            if (equipado) {
                player.sendMessage("§aVocê equipou o título: " + tituloNome);
            } else {
                player.sendMessage("§cNão foi possível equipar este título.");
            }
            abrirMenu(player); // reabrir para atualizar estado
        }
    }

}