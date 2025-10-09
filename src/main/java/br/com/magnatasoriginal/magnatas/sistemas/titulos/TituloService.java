package br.com.magnatasoriginal.magnatas.sistemas.titulos;

import br.com.magnatasoriginal.magnatas.sistemas.titulos.cache.TituloCache;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
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
            return false; // já possui
        }
        manager.darTitulo(uuid, nomeFinal);
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

    /** Equipa um título, se válido e não expirado */
    public boolean equiparTitulo(Player player, String tituloNome) {
        String nomeFinal = normalizarNome(tituloNome);
        UUID uuid = player.getUniqueId();

        if (!listarTitulos(uuid).contains(nomeFinal)) {
            return false; // não possui
        }

        Optional<Titulo> opt = manager.getTituloPorNome(nomeFinal);
        if (opt.isEmpty() || opt.get().isExpirado()) {
            return false; // inexistente ou expirado
        }

        manager.equiparTitulo(player, nomeFinal);
        return true;
    }

    /** Desvincula o título atualmente equipado */
    public void removerTituloEquipado(UUID uuid) {
        manager.removerTituloEquipado(uuid);
    }

    /** Retorna todos os títulos conquistados de um jogador (com cache) */
    public Set<String> listarTitulos(UUID uuid) {
        // tenta pegar do cache
        Set<Titulo> titulosCache = cache.getTitulos(uuid);
        if (!titulosCache.isEmpty()) {
            return titulosCache.stream().map(Titulo::getNome).collect(Collectors.toSet());
        }

        // se não tiver no cache, busca no manager e salva no cache
        Set<String> nomes = manager.getTitulosDoJogador(uuid);
        Set<Titulo> objetos = nomes.stream()
                .map(nome -> manager.getTituloPorNome(nome).orElse(null))
                .filter(t -> t != null)
                .collect(Collectors.toSet());

        cache.putTitulos(uuid, objetos);
        return nomes;
    }

    /** Retorna o título atualmente equipado */
    public Optional<String> getTituloEquipado(UUID uuid) {
        return manager.getTituloEquipado(uuid);
    }

    // ---------------------------
    // Utilitários
    // ---------------------------

    private String normalizarNome(String nome) {
        return nome == null ? "" : nome.toLowerCase();
    }
}