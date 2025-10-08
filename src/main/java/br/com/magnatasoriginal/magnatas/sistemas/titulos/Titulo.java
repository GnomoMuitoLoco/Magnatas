package br.com.magnatasoriginal.magnatas.sistemas.titulos;

import java.time.LocalDateTime;

public class Titulo {

    public enum Tipo {
        PERMANENTE,
        TEMPORARIO,
        SAZONAL,
        RANKING
    }

    private final String nome;
    private final String descricao;
    private final Tipo tipo;
    private final LocalDateTime expiraEm; // null se permanente

    public Titulo(String nome, String descricao, Tipo tipo, LocalDateTime expiraEm) {
        this.nome = nome;
        this.descricao = descricao;
        this.tipo = tipo;
        this.expiraEm = expiraEm;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public LocalDateTime getExpiraEm() {
        return expiraEm;
    }

    public boolean isExpirado() {
        return expiraEm != null && LocalDateTime.now().isAfter(expiraEm);
    }
}