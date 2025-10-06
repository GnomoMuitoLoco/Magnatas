package br.com.magnatasoriginal.magnatas.sistemas.antilag.limites;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class LimitesManager {

    private final Map<String, Integer> blocosLimitados = new HashMap<>();
    private final LimitesStorage storage;

    public LimitesManager(LimitesStorage storage) {
        this.storage = storage;
        this.blocosLimitados.putAll(storage.carregarTodos());
    }

    public void adicionarLimite(String blocoId, int quantidade) {
        blocosLimitados.put(blocoId, quantidade);
        storage.salvarLimite(blocoId, quantidade);
    }

    public void removerLimite(String blocoId) {
        blocosLimitados.remove(blocoId);
        storage.removerLimite(blocoId); // agora remove do banco
    }

    public boolean estaLimitado(String blocoId) {
        return blocosLimitados.containsKey(blocoId);
    }

    public int getLimite(String blocoId) {
        return blocosLimitados.getOrDefault(blocoId, -1);
    }

    public Map<String, Integer> getTodosLimites() {
        return new HashMap<>(blocosLimitados);
    }

    public Set<String> getBlocosLimitados() {
        return blocosLimitados.keySet();
    }
}