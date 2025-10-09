package br.com.magnatasoriginal.magnatas.sistemas.titulos;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

public final class Titulo {

    private final String nome;            // identificador interno
    private final String nomeVisivel;     // nome colorido
    private final String descricao;
    private final String permissao;
    private final String obtencao;
    private final boolean permanente;
    private final long duracaoMillis;     // -1 se permanente
    private final boolean loja;
    private final int preco;
    private final Instant expiraEm;       // null se não expira

    public Titulo(String nome,
                  String nomeVisivel,
                  String descricao,
                  String permissao,
                  String obtencao,
                  boolean permanente,
                  long duracaoMillis,
                  boolean loja,
                  int preco,
                  Instant expiraEm) {

        this.nome = Objects.requireNonNull(nome).toLowerCase();
        this.nomeVisivel = Objects.requireNonNullElse(nomeVisivel, nome);
        this.descricao = Objects.requireNonNullElse(descricao, "");
        this.permissao = Objects.requireNonNullElse(permissao, "");
        this.obtencao = Objects.requireNonNullElse(obtencao, "");
        this.permanente = permanente;
        this.duracaoMillis = permanente ? -1 : duracaoMillis;
        this.loja = loja;
        this.preco = preco;
        this.expiraEm = expiraEm;
    }

    // ---------------------------
    // Getters imutáveis
    // ---------------------------
    public String getNome() { return nome; }
    public String getNomeVisivel() { return nomeVisivel; }
    public String getDescricao() { return descricao; }
    public String getPermissao() { return permissao; }
    public String getObtencao() { return obtencao; }
    public boolean isPermanente() { return permanente; }
    public long getDuracaoMillis() { return duracaoMillis; }
    public boolean isLoja() { return loja; }
    public int getPreco() { return preco; }
    public Optional<Instant> getExpiraEm() { return Optional.ofNullable(expiraEm); }

    // ---------------------------
    // Lógica utilitária
    // ---------------------------
    public boolean isExpirado() {
        return expiraEm != null && Instant.now().isAfter(expiraEm);
    }

    public long getDuracaoRestanteMillis() {
        if (permanente) return -1;
        if (expiraEm != null) {
            return Math.max(0, expiraEm.toEpochMilli() - Instant.now().toEpochMilli());
        }
        return duracaoMillis;
    }
}