package br.com.magnatasoriginal.magnatas.sistemas.titulos.listener;

import br.com.magnatasoriginal.magnatas.sistemas.economia.Tokens;
import br.com.magnatasoriginal.magnatas.sistemas.titulos.Titulo;
import br.com.magnatasoriginal.magnatas.sistemas.titulos.TituloManager;
import br.com.magnatasoriginal.magnatas.sistemas.titulos.TituloService;
import br.com.magnatasoriginal.magnatas.sistemas.titulos.gui.TituloLojaMenu;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public class TituloLojaListener implements Listener {

    private final TituloService tituloService;
    private final Tokens tokens;
    private final TituloManager tituloManager;

    public TituloLojaListener(TituloService tituloService, Tokens tokens, TituloManager tituloManager) {
        this.tituloService = tituloService;
        this.tokens = tokens;
        this.tituloManager = tituloManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(TituloLojaMenu.getMenuTitle())) return;
        event.setCancelled(true);

        ItemStack item = event.getCurrentItem();
        if (item == null || !item.hasItemMeta()) return;

        Player player = (Player) event.getWhoClicked();
        String tituloNome = ChatColor.stripColor(item.getItemMeta().getDisplayName());

        // Recupera o título e o preço do TituloManager
        Optional<Titulo> optTitulo = tituloManager.getTituloPorNome(tituloNome);
        if (optTitulo.isEmpty()) {
            player.sendMessage("§cTítulo não encontrado.");
            return;
        }

        Titulo titulo = optTitulo.get();
        int preco = titulo.getPreco();

        // Verifica saldo
        int saldo = tokens.getTokenCount(player.getUniqueId().toString());
        if (saldo >= preco) {
            tokens.removeTokens(player.getUniqueId().toString(), preco);

            // ⚠️ Você precisa implementar esse método no TituloService
            boolean sucesso = tituloService.concederTitulo(player.getUniqueId(), tituloNome);
            if (sucesso) {
                player.sendMessage("§aVocê comprou o título: " + titulo.getNomeVisivel());
            } else {
                player.sendMessage("§cVocê já possui esse título!");
            }

            player.sendMessage("§aVocê comprou o título: " + titulo.getNomeVisivel());
        } else {
            player.sendMessage("§cVocê não tem tokens suficientes! Precisa de " + preco + " tokens.");
        }
    }
}