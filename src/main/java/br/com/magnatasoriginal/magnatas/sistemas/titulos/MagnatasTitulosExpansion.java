package br.com.magnatasoriginal.magnatas.sistemas.titulos;

import br.com.magnatasoriginal.magnatas.Magnatas;
import br.com.magnatasoriginal.magnatas.sistemas.economia.Tokens;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

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
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer offlinePlayer, String params) {
        Player player = offlinePlayer != null ? offlinePlayer.getPlayer() : null;

        if (params.equalsIgnoreCase("tag")) {
            if (player == null) return "";
            String equipado = tituloManager.getTituloEquipado(player);
            if (equipado != null) {
                Titulo titulo = tituloManager.getTituloPorNome(equipado);
                return titulo != null ? titulo.getNomeVisivel() : "[" + equipado + "]";
            }
            return "";
        }

        if (!params.equalsIgnoreCase("duracao") &&
                !params.equalsIgnoreCase("count") &&
                !params.startsWith("topcoins") &&
                !params.startsWith("toptokens") &&
                !params.startsWith("toptitulos")) {

            if (player == null) return "";
            String equipado = tituloManager.getTituloEquipado(player);
            if (equipado != null && equipado.equalsIgnoreCase(params)) {
                Titulo titulo = tituloManager.getTituloPorNome(params);
                return titulo != null ? titulo.getNomeVisivel() : "[" + params + "]";
            }
            return "";
        }

        if (params.equalsIgnoreCase("duracao")) {
            if (player == null) return "";
            String equipado = tituloManager.getTituloEquipado(player);
            if (equipado != null) {
                Titulo titulo = tituloManager.getTituloPorNome(equipado);
                if (titulo != null) {
                    if (titulo.isPermanente()) return "Permanente";
                    if (titulo.getExpiraEm() != null) {
                        return plugin.formatarDuracao(java.time.Duration.between(java.time.LocalDateTime.now(), titulo.getExpiraEm()).toMillis());
                    }
                    return plugin.formatarDuracao(titulo.getDuracaoMillis());
                }
            }
            return "";
        }

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

        if (params.startsWith("topcoins")) {
            if (economy == null) return "";
            int pos = Integer.parseInt(params.replace("topcoins", ""));
            return getTopVault(pos);
        }

        if (params.startsWith("toptokens")) {
            int pos = Integer.parseInt(params.replace("toptokens", ""));
            return getTopTokens(pos);
        }

        if (params.startsWith("toptitulos")) {
            int pos = Integer.parseInt(params.replace("toptitulos", ""));
            return getTopTitulos(pos);
        }

        return null;
    }

    private String getTopVault(int pos) {
        List<OfflinePlayer> players = Arrays.asList(Bukkit.getOfflinePlayers());
        players.sort(Comparator.comparingDouble((OfflinePlayer p) -> economy.getBalance(p)).reversed());
        if (pos <= players.size()) {
            OfflinePlayer p = players.get(pos - 1);
            return p.getName() + " - " + economy.getBalance(p);
        }
        return "";
    }

    private String getTopTokens(int pos) {
        Map<UUID, Integer> all = tokens.getAllTokens();
        List<Map.Entry<UUID, Integer>> sorted = all.entrySet().stream()
                .sorted(Map.Entry.<UUID, Integer>comparingByValue().reversed())
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
                .sorted(Map.Entry.<UUID, Integer>comparingByValue().reversed())
                .collect(Collectors.toList());
        if (pos <= sorted.size()) {
            UUID uuid = sorted.get(pos - 1).getKey();
            return Bukkit.getOfflinePlayer(uuid).getName() + " - " + sorted.get(pos - 1).getValue();
        }
        return "";
    }
}