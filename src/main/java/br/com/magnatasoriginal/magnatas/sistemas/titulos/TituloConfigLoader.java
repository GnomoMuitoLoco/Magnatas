package br.com.magnatasoriginal.magnatas.sistemas.titulos;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TituloConfigLoader {

    private final Map<String, Titulo> titulos = new HashMap<>();

    public TituloConfigLoader(File file) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        carregar(config);
    }

    private void carregar(YamlConfiguration config) {
        for (String id : config.getKeys(false)) {
            String nomeVisivel = config.getString(id + ".nome_visivel", "&f" + id);
            String descricao = config.getString(id + ".descricao", "&7Sem descrição.");
            String obtencao = config.getString(id + ".obtencao", "Desconhecida");
            String permissao = config.getString(id + ".permissao", "");
            String nomePermissao = config.getString(id + ".nome_permissao", id);

            // Novos campos
            boolean loja = config.getBoolean(id + ".loja", true);
            int preco = config.getInt(id + ".preco", 0);

            // Duração (string original da config)
            String duracaoStr = config.getString(id + ".duracao", "permanente");

            // Cria o título com o novo construtor
            Titulo titulo = new Titulo(
                    id,
                    ChatColor.translateAlternateColorCodes('&', nomeVisivel),
                    ChatColor.translateAlternateColorCodes('&', descricao),
                    permissao,
                    obtencao,
                    duracaoStr,
                    loja,
                    preco,
                    nomePermissao
            );

            titulos.put(id.toLowerCase(), titulo);
        }
    }

    public Collection<Titulo> getTitulos() {
        return titulos.values();
    }

    public Titulo getTitulo(String id) {
        return titulos.get(id.toLowerCase());
    }

    public static long parseDuracao(String input) {
        if (input.equalsIgnoreCase("permanente")) return 0;

        long totalMillis = 0;
        Matcher matcher = Pattern.compile("(\\d+)([dhms])").matcher(input.toLowerCase());
        while (matcher.find()) {
            int valor = Integer.parseInt(matcher.group(1));
            switch (matcher.group(2)) {
                case "d": totalMillis += valor * 86400000L; break;
                case "h": totalMillis += valor * 3600000L; break;
                case "m": totalMillis += valor * 60000L; break;
                case "s": totalMillis += valor * 1000L; break;
            }
        }
        return totalMillis;
    }
}