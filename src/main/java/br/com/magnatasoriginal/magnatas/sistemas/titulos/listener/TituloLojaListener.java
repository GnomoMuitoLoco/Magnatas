package br.com.magnatasoriginal.magnatas.sistemas.titulos.listener;

import br.com.magnatasoriginal.magnatas.sistemas.economia.Tokens;
import br.com.magnatasoriginal.magnatas.sistemas.titulos.Titulo;
import br.com.magnatasoriginal.magnatas.sistemas.titulos.TituloManager;
import br.com.magnatasoriginal.magnatas.sistemas.titulos.TituloService;
import br.com.magnatasoriginal.magnatas.sistemas.titulos.gui.TituloLojaMenu;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.types.PermissionNode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class TituloLojaListener implements Listener {

    private final TituloService tituloService;
    private final TituloManager tituloManager;
    private final Tokens tokens;
    private final LuckPerms luckPerms;

    public TituloLojaListener(TituloService tituloService,
                              TituloManager tituloManager,
                              Tokens tokens,
                              LuckPerms luckPerms) {
        this.tituloService = tituloService;
        this.tituloManager = tituloManager;
        this.tokens = tokens;
        this.luckPerms = luckPerms;
    }

    @EventHandler
    public void aoClicarNaLoja(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        event.getView().getTitle();
        if (!event.getView().getTitle().startsWith(TituloLojaMenu.getMenuTitle())) return;

        event.setCancelled(true);

        ItemStack item = event.getCurrentItem();
        if (item == null || !item.hasItemMeta()) return;

        String displayName = Objects.requireNonNull(item.getItemMeta()).getDisplayName();
        if (displayName.isEmpty()) return;

        // --- Tratamento das setas de navegação ---
        if (item.getType() == Material.ARROW) {
            int paginaAtual = extrairPagina(event.getView().getTitle());
            if (displayName.contains("anterior")) {
                if (paginaAtual > 1) {
                    player.openInventory(new TituloLojaMenu(tituloManager).criarMenu(player, paginaAtual - 1));
                } else {
                    player.sendMessage("§cVocê já está na primeira página.");
                }
            } else if (displayName.contains("Próxima")) {
                int totalPaginas = (int) Math.ceil((double) tituloManager.getTodosTitulos().stream()
                        .filter(Titulo::isLoja).count() / TituloLojaMenu.ITEMS_PER_PAGE);
                if (paginaAtual < totalPaginas) {
                    player.openInventory(new TituloLojaMenu(tituloManager).criarMenu(player, paginaAtual + 1));
                } else {
                    player.sendMessage("§cVocê já está na última página.");
                }
            }
            return;
        }

        // --- Tratamento da compra de títulos ---
        if (item.getType() != Material.NAME_TAG || !event.isLeftClick()) return;

        Optional<Titulo> optTitulo = tituloManager.getTodosTitulos().stream()
                .filter(t -> t.getNomeVisivel().equalsIgnoreCase(displayName))
                .findFirst();

        if (optTitulo.isEmpty()) {
            player.sendMessage("§cTítulo não encontrado.");
            return;
        }

        Titulo titulo = optTitulo.get();
        UUID uuid = player.getUniqueId();

        if (tituloService.listarTitulos(uuid).contains(titulo.getNome())) {
            player.sendMessage("§cVocê já possui esse título.");
            return;
        }

        int preco = titulo.getPreco();
        int saldo = tokens.getTokenCount(uuid);

        if (saldo < preco) {
            player.sendMessage("§cVocê não tem tokens suficientes.");
            return;
        }

        tokens.removeTokens(uuid, preco);
        tituloService.concederTitulo(uuid, titulo.getNome());

        if (titulo.getPermissao() != null && !titulo.getPermissao().isEmpty()) {
            luckPerms.getUserManager().modifyUser(uuid, (User user) ->
                    user.data().add(PermissionNode.builder(titulo.getPermissao()).build()));
        }

        player.sendMessage("§aVocê comprou o título: " + titulo.getNomeVisivel());
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
    }

    /**
     * Extrai o número da página a partir do título do inventário.
     * Exemplo: "§6Loja de Títulos - Página 2" -> retorna 2
     */
    private int extrairPagina(String tituloInventario) {
        try {
            String[] partes = tituloInventario.split("Página ");
            if (partes.length > 1) {
                return Integer.parseInt(partes[1].trim());
            }
        } catch (NumberFormatException ignored) {}
        return 1;
    }
}