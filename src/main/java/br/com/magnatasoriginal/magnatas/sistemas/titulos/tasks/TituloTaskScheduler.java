package br.com.magnatasoriginal.magnatas.sistemas.titulos.tasks;

import br.com.magnatasoriginal.magnatas.Magnatas;
import br.com.magnatasoriginal.magnatas.sistemas.titulos.TituloService;
import br.com.magnatasoriginal.magnatas.sistemas.titulos.cache.TituloCache;
import org.bukkit.scheduler.BukkitTask;

import java.util.UUID;

/**
 * Responsável por agendar e executar tarefas recorrentes relacionadas a títulos:
 * - Verificação de expiração
 * - Atualização/invalidação de cache
 */
public class TituloTaskScheduler {

    private final Magnatas plugin;
    private final TituloService service;
    private final TituloCache cache;
    private BukkitTask expiracaoTask;

    public TituloTaskScheduler(Magnatas plugin, TituloService service, TituloCache cache) {
        this.plugin = plugin;
        this.service = service;
        this.cache = cache;
    }

    /**
     * Inicia a tarefa de verificação de expiração de títulos.
     * @param intervaloTicks intervalo em ticks (20 ticks = 1 segundo)
     */
    public void iniciarVerificacaoExpiracao(long intervaloTicks) {
        if (expiracaoTask != null) {
            expiracaoTask.cancel();
        }

        expiracaoTask = plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            for (UUID uuid : service.getManager().getJogadoresComTitulos()) {
                // Verifica expirações no Manager/Service
                service.getManager().verificarExpiracoes(uuid);

                // Invalida cache para forçar recarregamento atualizado
                cache.invalidate(uuid);
            }
        }, intervaloTicks, intervaloTicks);
    }

    /** Para a tarefa de expiração */
    public void parar() {
        if (expiracaoTask != null) {
            expiracaoTask.cancel();
            expiracaoTask = null;
        }
    }
}