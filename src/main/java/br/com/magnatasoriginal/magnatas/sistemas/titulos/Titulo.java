package br.com.magnatasoriginal.magnatas.sistemas.titulos;

import java.time.LocalDateTime;

public class Titulo {

    private final String nome;
    private final String nomeVisivel;
    private final String descricao;
    private final String permissao;
    private final String nomePermissao;
    private final String obtencao;
    private final long duracaoMillis; // 0 = permanente
    private final LocalDateTime expiraEm; // opcional, usado se quiser data exata

    public Titulo(String nome, String nomeVisivel, String descricao, String permissao, String nomePermissao, String obtencao, long duracaoMillis) {
        this.nome = nome;
        this.nomeVisivel = nomeVisivel;
        this.descricao = descricao;
        this.permissao = permissao;
        this.nomePermissao = nomePermissao;
        this.obtencao = obtencao;
        this.duracaoMillis = duracaoMillis;
        this.expiraEm = duracaoMillis > 0 ? LocalDateTime.now().plusSeconds(duracaoMillis / 1000) : null;
    }

    // Construtor legado para banco de dados
    public Titulo(String nome, String descricao, LocalDateTime expiraEm) {
        this.nome = nome;
        this.nomeVisivel = nome;
        this.descricao = descricao;
        this.permissao = "";
        this.nomePermissao = nome;
        this.obtencao = "Desconhecida";
        this.expiraEm = expiraEm;
        this.duracaoMillis = expiraEm != null ? java.time.Duration.between(LocalDateTime.now(), expiraEm).toMillis() : 0;
    }

    public boolean isPermanente() {
        return duracaoMillis <= 0;
    }

    public boolean isExpirado() {
        return expiraEm != null && LocalDateTime.now().isAfter(expiraEm);
    }

    // Getters
    public String getNome() { return nome; }
    public String getNomeVisivel() { return nomeVisivel; }
    public String getDescricao() { return descricao; }
    public String getPermissao() { return permissao; }
    public String getNomePermissao() { return nomePermissao; }
    public String getObtencao() { return obtencao; }
    public long getDuracaoMillis() { return duracaoMillis; }
    public LocalDateTime getExpiraEm() { return expiraEm; }
}