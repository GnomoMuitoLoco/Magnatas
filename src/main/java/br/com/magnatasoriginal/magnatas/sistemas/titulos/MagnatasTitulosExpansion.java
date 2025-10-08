package br.com.magnatasoriginal.magnatas.sistemas.titulos;

import br.com.magnatasoriginal.magnatas.Magnatas;
import br.com.magnatasoriginal.magnatas.sistemas.economia.Tokens;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import net.milkbowl.vault.economy.Economy;

import java.util.*;
import java.util.stream.Collectors;

public class MagnatasTitulosExpansion extends PlaceholderExpansion {

    private final Magnatas plugin;
    private final TituloManager tituloManager;
    private final Tokens tokens;
    private Economy economy;

    public MagnatasTitulosExpansion(Magnatas plugin, TituloManager tituloManager, Tokens tokens) {
        this.plugin = plugin;
        this.tituloManager = tituloManager;
        this.tokens = tokens;

        // Integração com Vault
        if (plugin.getServer().getPluginManager().getPlugin("Vault") != null) {
            RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp != null) {
                economy = rsp.getProvider();
            }
        }
    }

    @Override
    public String getIdentifier() {
        return "magnatastitulos";
    }

    @Override
    public String getAuthor() {
        return "GnomoMuitoLouco";
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true; // não desregistrar em reload
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer offlinePlayer, String params) {
        Player player = offlinePlayer != null ? offlinePlayer.getPlayer() : null;

        // %magnatastitulos_tag% → mostra a tag atual do jogador
        if (params.equalsIgnoreCase("tag")) {
            if (player == null) return "";
            String equipado = tituloManager.getTituloEquipado(player);
            if (equipado != null) {
                var titulo = tituloManager.getTituloPorNome(equipado);
                return titulo != null ? "[" + titulo.getNome() + "]" : "[" + equipado + "]";
            }
            return "";
        }

        // %magnatastitulos_<tag>% → mostra uma tag específica se o jogador estiver usando
        if (!params.equalsIgnoreCase("duracao") &&
                !params.equalsIgnoreCase("tipo") &&
                !params.equalsIgnoreCase("count") &&
                !params.startsWith("topcoins") &&
                !params.startsWith("toptokens") &&
                !params.startsWith("toptitulos")) {

            if (player == null) return "";
            String equipado = tituloManager.getTituloEquipado(player);
            if (equipado != null && equipado.equalsIgnoreCase(params)) {
                var titulo = tituloManager.getTituloPorNome(params);
                return titulo != null ? "[" + titulo.getNome() + "]" : "[" + params + "]";
            }
            return "";
        }

        // %magnatastitulos_duracao%
        if (params.equalsIgnoreCase("duracao")) {
            if (player == null) return "";
            String equipado = tituloManager.getTituloEquipado(player);
            if (equipado != null) {
                var titulo = tituloManager.getTituloPorNome(equipado);
                if (titulo != null && titulo.getExpiraEm() != null) {
                    return titulo.getExpiraEm().toString();
                }
            }
            return "";
        }

        // %magnatastitulos_tipo%
        if (params.equalsIgnoreCase("tipo")) {
            if (player == null) return "";
            String equipado = tituloManager.getTituloEquipado(player);
            if (equipado != null) {
                var titulo = tituloManager.getTituloPorNome(equipado);
                if (titulo != null) {
                    return titulo.getTipo().name();
                }
            }
            return "";
        }

        // %magnatastitulos_count%
        if (params.equalsIgnoreCase("count")) {
            if (player == null) return "0";
            String equipado = tituloManager.getTituloEquipado(player);
            if (equipado != null) {
                long count = Bukkit.getOnlinePlayers().stream()
                        .filter(p -> equipado.equalsIgnoreCase(tituloManager.getTituloEquipado(p)))
                        .count();
                return String.valueOf(count);
            }
            return "0";
        }

        // %magnatastitulos_topcoins1-10%
        if (params.startsWith("topcoins")) {
            if (economy == null) return "";
            int pos = Integer.parseInt(params.replace("topcoins", ""));
            return getTopVault(pos);
        }

        // %magnatastitulos_toptokens1-10%
        if (params.startsWith("toptokens")) {
            int pos = Integer.parseInt(params.replace("toptokens", ""));
            return getTopTokens(pos);
        }

        // %magnatastitulos_toptitulos1-10%
        if (params.startsWith("toptitulos")) {
            int pos = Integer.parseInt(params.replace("toptitulos", ""));
            return getTopTitulos(pos);
        }

        return null;
    }

    // =========================
    // Rankings
    // =========================
    private String getTopVault(int pos) {
        List<OfflinePlayer> players = Arrays.asList(Bukkit.getOfflinePlayers());
        players.sort((a, b) -> Double.compare(
                economy.getBalance(b),
                economy.getBalance(a)
        ));
        if (pos <= players.size()) {
            OfflinePlayer p = players.get(pos - 1);
            return p.getName() + " - " + economy.getBalance(p);
        }
        return "";
    }

    private String getTopTokens(int pos) {
        Map<UUID, Integer> all = tokens.getAllTokens();
        List<Map.Entry<UUID, Integer>> sorted = all.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .collect(Collectors.toList());
        if (pos <= sorted.size()) {
            UUID uuid = sorted.get(pos - 1).getKey();
            return Bukkit.getOfflinePlayer(uuid).getName() + " - " + sorted.get(pos - 1).getValue();
        }
        return "";
    }

    private String getTopTitulos(int pos) {
        Map<UUID, Integer> counts = new HashMap<>();
        for (UUID uuid : tituloManager.getTitulosPorJogador().keySet()) {
            counts.put(uuid, tituloManager.getTitulosPorJogador().get(uuid).size());
        }
        List<Map.Entry<UUID, Integer>> sorted = counts.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .collect(Collectors.toList());
        if (pos <= sorted.size()) {
            UUID uuid = sorted.get(pos - 1).getKey();
            return Bukkit.getOfflinePlayer(uuid).getName() + " - " + sorted.get(pos - 1).getValue();
        }
        return "";
    }
}