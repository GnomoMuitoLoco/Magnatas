package br.com.magnatasoriginal.magnatas.sistemas.titulos;

import org.bukkit.ChatColor;

import java.time.Duration;
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
    private final boolean loja;
    private final int preco;
    private final String duracao; // string original da config (ex: "1d", "permanente")

    /**
     * Construtor principal usado ao carregar do config (titulos.yml)
     */
    public Titulo(String nome, String nomeVisivel, String descricao, String permissao,
                  String obtencao, String duracao, boolean loja, int preco) {
        this.nome = nome;
        this.nomeVisivel = ChatColor.translateAlternateColorCodes('&', nomeVisivel);
        this.descricao = ChatColor.translateAlternateColorCodes('&', descricao);
        this.permissao = permissao;
        this.nomePermissao = nome; // pode ser ajustado se quiser um campo separado
        this.obtencao = obtencao;
        this.duracao = duracao;
        this.loja = loja;
        this.preco = preco;

        // Calcula duração em millis e expiração
        if (duracao == null || duracao.equalsIgnoreCase("permanente")) {
            this.duracaoMillis = 0;
            this.expiraEm = null;
        } else {
            // Aqui você pode implementar um parser de duração (ex: "1d" -> 86400000)
            long millis = parseDuracao(duracao);
            this.duracaoMillis = millis;
            this.expiraEm = LocalDateTime.now().plus(Duration.ofMillis(millis));
        }
    }

    /**
     * Construtor legado para banco de dados (quando já existe uma expiração exata)
     */
    public Titulo(String nome, String descricao, LocalDateTime expiraEm) {
        this.nome = nome;
        this.nomeVisivel = nome;
        this.descricao = descricao;
        this.permissao = "";
        this.nomePermissao = nome;
        this.obtencao = "Desconhecida";
        this.loja = false;
        this.preco = 0;
        this.duracao = "";
        this.expiraEm = expiraEm;
        this.duracaoMillis = expiraEm != null
                ? Duration.between(LocalDateTime.now(), expiraEm).toMillis()
                : 0;
    }

    /**
     * Exemplo simples de parser de duração (pode expandir para h/m/s)
     */
    private long parseDuracao(String duracao) {
        try {
            if (duracao.endsWith("d")) {
                int dias = Integer.parseInt(duracao.replace("d", ""));
                return Duration.ofDays(dias).toMillis();
            } else if (duracao.endsWith("h")) {
                int horas = Integer.parseInt(duracao.replace("h", ""));
                return Duration.ofHours(horas).toMillis();
            } else if (duracao.endsWith("m")) {
                int minutos = Integer.parseInt(duracao.replace("m", ""));
                return Duration.ofMinutes(minutos).toMillis();
            }
        } catch (NumberFormatException e) {
            return 0;
        }
        return 0;
    }

    public boolean isLoja() {
        return loja;
    }

    public int getPreco() {
        return preco;
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
    public String getDuracao() { return duracao; }
}