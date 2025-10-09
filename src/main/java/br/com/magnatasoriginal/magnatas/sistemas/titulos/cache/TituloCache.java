package br.com.magnatasoriginal.magnatas.sistemas.titulos.cache;

import br.com.magnatasoriginal.magnatas.sistemas.titulos.Titulo;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TituloCache {

    // Mapa: UUID do jogador -> conjunto de títulos
    private final Map<UUID, Set<Titulo>> cache = new ConcurrentHashMap<>();

    public Set<Titulo> getTitulos(UUID uuid) {
        return cache.getOrDefault(uuid, Collections.emptySet());
    }

    public void putTitulos(UUID uuid, Set<Titulo> titulos) {
        cache.put(uuid, new HashSet<>(titulos));
    }

    public void invalidate(UUID uuid) {
        cache.remove(uuid);
    }

    public void clear() {
        cache.clear();
    }

    public void addTitulo(UUID uuid, Titulo titulo) {
        cache.computeIfAbsent(uuid, k -> new HashSet<>()).add(titulo);
    }

    public void removeTitulo(UUID uuid, Titulo titulo) {
        Set<Titulo> titulos = cache.get(uuid);
        if (titulos != null) {
            titulos.remove(titulo);
            if (titulos.isEmpty()) {
                cache.remove(uuid);
            }
        }
    }
}