package br.com.magnatasoriginal.magnatas.sistemas.titulos.lojadetitulos;

import br.com.magnatasoriginal.magnatas.Magnatas;
import br.com.magnatasoriginal.magnatas.sistemas.titulos.Titulo;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.SimpleDateFormat;
import java.util.*;

public class LojadeTitulos implements Listener {

    private final Magnatas plugin;

    public LojadeTitulos(Magnatas plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void abrirLoja(Player player) {
        Inventory loja = Bukkit.createInventory(null, 54, ChatColor.GOLD + "Loja de Títulos");

        List<Titulo> titulos = new ArrayList<>(plugin.getTituloManager().getTodosTitulos());

        int[] slots = {
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34,
                37, 38, 39, 40, 41, 42, 43
        };

        int index = 0;
        for (Titulo titulo : titulos) {
            if (!titulo.isLoja()) continue; // só mostra se estiver marcado como loja
            if (index >= slots.length) break;

            ItemStack item = new ItemStack(Material.NAME_TAG);
            ItemMeta meta = item.getItemMeta();
            if (meta == null) continue;

            meta.setDisplayName(titulo.getNomeVisivel());

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + titulo.getDescricao());
            lore.add(ChatColor.AQUA + "Obtenção: " + ChatColor.WHITE + titulo.getObtencao());

            if (titulo.getPreco() <= 0) {
                lore.add(ChatColor.GREEN + "Preço: " + ChatColor.YELLOW + "Grátis");
            } else {
                lore.add(ChatColor.GREEN + "Preço: " + ChatColor.YELLOW + titulo.getPreco() + " tokens");
            }

            // Sempre mostra a duração
            if (titulo.isPermanente()) {
                lore.add(ChatColor.LIGHT_PURPLE + "Duração: Permanente");
            } else {
                lore.add(ChatColor.LIGHT_PURPLE + "Duração: " + Magnatas.formatarDuracao(titulo.getDuracaoMillis()));
            }

            lore.add("");
            lore.add(ChatColor.GREEN + "Clique para comprar");

            meta.setLore(lore);
            item.setItemMeta(meta);

            loja.setItem(slots[index], item);
            index++;
        }

        player.openInventory(loja);
    }

    private void concederTitulo(Player player, Titulo titulo) {
        plugin.getTituloManager().darTitulo(player, titulo.getNome());

        // Se for temporário, salvar no titulos_ativos.yml
        if (!titulo.isPermanente()) {
            long expiraMillis = System.currentTimeMillis() + titulo.getDuracaoMillis();
            String dataFormatada = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date(expiraMillis));

            FileConfiguration config = plugin.getTitulosAtivosConfig();
            String path = "activeTitles." + titulo.getNome().toLowerCase() + "." + player.getUniqueId();

            config.set(path + ".nick", player.getName());
            config.set(path + ".duration", expiraMillis);
            config.set(path + ".expires-on-exact", dataFormatada);
            config.set(path + ".active", true);

            plugin.salvarTitulosAtivos();
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(ChatColor.GOLD + "Loja de Títulos")) return;
        if (event.getClickedInventory() == null || event.getCurrentItem() == null) return;

        event.setCancelled(true);

        ItemStack item = event.getCurrentItem();
        if (item.getType() == Material.AIR || !item.hasItemMeta()) return;

        Player player = (Player) event.getWhoClicked();
        String displayName = ChatColor.stripColor(item.getItemMeta().getDisplayName());

        // Procura o título pelo nome visível
        Titulo titulo = plugin.getTituloManager().getTodosTitulos().stream()
                .filter(t -> ChatColor.stripColor(t.getNomeVisivel()).equalsIgnoreCase(displayName))
                .findFirst()
                .orElse(null);

        if (titulo == null) return;

        int preco = titulo.getPreco();
        String uuid = player.getUniqueId().toString();

        // Verifica se já possui (apenas conquistados, não permissões)
        if (plugin.getTituloManager().possuiTituloConquistado(player.getUniqueId(), titulo.getNome())) {
            player.sendMessage(ChatColor.RED + "Você já possui este título!");
            return;
        }

        // Compra grátis
        if (preco <= 0) {
            concederTitulo(player, titulo);
            player.sendMessage(ChatColor.GREEN + "Você adquiriu o título " + titulo.getNomeVisivel() + " gratuitamente!");
            return;
        }

        // Compra com tokens
        int saldo = plugin.getTokens().getTokenCount(uuid);

        if (saldo < preco) {
            player.sendMessage(ChatColor.RED + "Você não tem tokens suficientes! (" + saldo + "/" + preco + ")");
            return;
        }

        plugin.getTokens().removeTokens(uuid, preco);
        concederTitulo(player, titulo);
        player.sendMessage(ChatColor.GREEN + "Você adquiriu o título " + titulo.getNomeVisivel() +
                ChatColor.GRAY + " por " + preco + " tokens!");
    }
}