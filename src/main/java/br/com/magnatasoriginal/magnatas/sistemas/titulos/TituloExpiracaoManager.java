package br.com.magnatasoriginal.magnatas.sistemas.titulos;

import br.com.magnatasoriginal.magnatas.Magnatas;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class TituloExpiracaoManager {

    private final Magnatas plugin;
    private final TituloManager tituloManager;

    // Cache de títulos ativos por jogador
    private final Map<UUID, Set<String>> ativos = new ConcurrentHashMap<>();

    private BukkitTask task;

    public TituloExpiracaoManager(Magnatas plugin, TituloManager tituloManager) {
        this.plugin = plugin;
        this.tituloManager = tituloManager;
    }

    /**
     * Inicia o verificador de expiração em modo assíncrono.
     */
    public void iniciar() {
        long intervaloSegundos = plugin.getConfig().getLong("titulos.expiracao.intervalo", 60L);

        if (task != null && !task.isCancelled()) {
            task.cancel();
        }

        task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::verificarExpiracoes,
                intervaloSegundos * 20L, intervaloSegundos * 20L);

        plugin.getLogger().info("⏳ Verificador de expiração de títulos iniciado (intervalo: "
                + intervaloSegundos + "s)");
    }

    /**
     * Para o verificador de expiração.
     */
    public void parar() {
        if (task != null) {
            task.cancel();
            plugin.getLogger().info("⏹ Verificador de expiração de títulos parado.");
        }
    }

    /**
     * Verifica títulos expirados de todos os jogadores.
     */
    private void verificarExpiracoes() {
        try {
            for (UUID uuid : tituloManager.getJogadoresComTitulos()) {
                Set<String> titulos = new HashSet<>(tituloManager.getTitulosDoJogador(uuid));

                for (String nome : titulos) {
                    tituloManager.getTituloPorNome(nome).ifPresent(titulo -> {
                        if (titulo.isExpirado()) {
                            removerTituloExpirado(uuid, titulo);
                        }
                    });
                }
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Erro ao verificar expirações de títulos", e);
        }
    }

    /**
     * Remove título expirado e dispara evento customizado.
     */
    private void removerTituloExpirado(UUID uuid, Titulo titulo) {
        tituloManager.removerTitulo(uuid, titulo.getNome());

        // Se estava equipado, remove também
        tituloManager.getTituloEquipado(uuid).ifPresent(equipado -> {
            if (equipado.equalsIgnoreCase(titulo.getNome())) {
                tituloManager.removerTituloEquipado(uuid);
            }
        });

        // Log
        plugin.getLogger().info("⚠ Título '" + titulo.getNomeVisivel() + "' expirou para jogador " + uuid);

        // Evento customizado
        Bukkit.getScheduler().runTask(plugin, () -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                Bukkit.getPluginManager().callEvent(new TituloExpiradoEvent(player, titulo));
                player.sendMessage("§cSeu título '" + titulo.getNomeVisivel() + "' expirou!");
            }
        });
    }
}