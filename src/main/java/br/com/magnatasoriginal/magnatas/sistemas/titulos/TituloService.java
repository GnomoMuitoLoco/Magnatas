package br.com.magnatasoriginal.magnatas.sistemas.titulos;

import br.com.magnatasoriginal.magnatas.sistemas.titulos.cache.TituloCache;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Serviço de regras de negócio para manipulação de títulos.
 * Atua como camada intermediária entre o Manager (persistência),
 * o Cache (performance) e a lógica de jogo (comandos, GUI, listeners).
 */
public class TituloService {

    private final TituloManager manager;
    private final TituloCache cache;

    public TituloService(TituloManager manager, TituloCache cache) {
        this.manager = manager;
        this.cache = cache;
    }

    public TituloManager getManager() {
        return manager;
    }

    /** Dá um título ao jogador, se ainda não tiver */
    public boolean concederTitulo(UUID uuid, String tituloNome) {
        String nomeFinal = normalizarNome(tituloNome);
        if (listarTitulos(uuid).contains(nomeFinal)) {
            Optional<Titulo> opt = manager.getTituloPorNome(nomeFinal);
            if (opt.isPresent()) {
                Titulo titulo = opt.get();
                if (!titulo.isPermanente()) {
                    long atual = manager.getDataAquisicao(uuid, nomeFinal);
                    long novo = atual + titulo.getDuracaoMillis();
                    manager.atualizarDataAquisicao(uuid, nomeFinal, novo);
                    cache.invalidate(uuid);
                    return true; // tempo estendido
                }
            }
            return false; // título permanente, não pode comprar de novo
        }

        Optional<Titulo> opt = manager.getTituloPorNome(nomeFinal);
        if (opt.isEmpty()) return false;

        Titulo titulo = opt.get();
        long adquiridoEm = System.currentTimeMillis();

        manager.darTitulo(uuid, nomeFinal, adquiridoEm); // salva com timestamp
        cache.invalidate(uuid); // força recarregar no próximo acesso
        return true;
    }

    /** Remove um título do jogador */
    public boolean removerTitulo(UUID uuid, String tituloNome) {
        String nomeFinal = normalizarNome(tituloNome);
        if (!listarTitulos(uuid).contains(nomeFinal)) {
            return false; // não possui
        }
        manager.removerTitulo(uuid, nomeFinal);
        cache.invalidate(uuid);
        return true;
    }

    private final Map<UUID, Long> cooldownEquipar = new HashMap<>();

    /** Equipa um título, se válido, não expirado e fora do cooldown */
    public boolean equiparTitulo(Player player, String tituloNome) {
        UUID uuid = player.getUniqueId();

        // Cooldown de 1 segundo
        long agora = System.currentTimeMillis();
        long ultimoClique = cooldownEquipar.getOrDefault(uuid, 0L);
        if ((agora - ultimoClique) < 1000) {
            player.sendMessage("§cAguarde um instante antes de trocar o título.");
            return false;
        }
        cooldownEquipar.put(uuid, agora);

        String nomeFinal = normalizarNome(tituloNome);

        if (!listarTitulos(uuid).contains(nomeFinal)) {
            return false; // não possui
        }

        Optional<Titulo> opt = manager.getTituloPorNome(nomeFinal);
        if (opt.isEmpty()) return false;

        Titulo titulo = opt.get();
        if (isExpirado(uuid, titulo)) {
            return false; // expirado
        }

        manager.equiparTitulo(player, nomeFinal);
        cache.invalidate(uuid); // garante que o menu reflita o título equipado
        return true;
    }

    /** Desvincula o título atualmente equipado */
    public void removerTituloEquipado(UUID uuid) {
        manager.removerTituloEquipado(uuid);
        cache.invalidate(uuid); // garante que o menu reflita a remoção
    }

    /** Retorna todos os títulos conquistados de um jogador (com cache) */
    public Set<String> listarTitulos(UUID uuid) {
        Set<Titulo> titulosCache = cache.getTitulos(uuid);
        if (!titulosCache.isEmpty()) {
            return titulosCache.stream().map(Titulo::getNome).collect(Collectors.toSet());
        }

        Set<String> nomes = manager.getTitulosDoJogador(uuid);
        Set<Titulo> objetos = nomes.stream()
                .map(nome -> manager.getTituloPorNome(nome).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        cache.putTitulos(uuid, objetos);
        return nomes;
    }

    /** Retorna o título atualmente equipado */
    public Optional<String> getTituloEquipado(UUID uuid) {
        return manager.getTituloEquipado(uuid);
    }

    /** Retorna a duração restante de um título temporário */
    public long getDuracaoRestante(UUID uuid, String tituloNome) {
        String nomeFinal = normalizarNome(tituloNome);

        Optional<Titulo> opt = manager.getTituloPorNome(nomeFinal);
        if (opt.isEmpty()) return 0L;

        Titulo titulo = opt.get();
        if (titulo.isPermanente()) return -1L;

        long adquiridoEm = manager.getDataAquisicao(uuid, nomeFinal);
        if (adquiridoEm <= 0) return 0L;

        long expiraEm = adquiridoEm + titulo.getDuracaoMillis();
        long restante = expiraEm - System.currentTimeMillis();

        return Math.max(restante, 0L);
    }

    /** Verifica se um título está expirado para um jogador */
    public boolean isExpirado(UUID uuid, Titulo titulo) {
        if (titulo.isPermanente()) return false;

        long adquiridoEm = manager.getDataAquisicao(uuid, titulo.getNome());
        if (adquiridoEm <= 0) return false;

        long expiraEm = adquiridoEm + titulo.getDuracaoMillis();
        return System.currentTimeMillis() > expiraEm;
    }

    // ---------------------------
    // Utilitários
    // ---------------------------

    private String normalizarNome(String nome) {
        return nome == null ? "" : nome.trim().toLowerCase();
    }

    public void invalidateCache(UUID uuid) {
        cache.invalidate(uuid);
    }
}