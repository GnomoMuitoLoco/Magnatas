package br.com.magnatasoriginal.magnatas.sistemas.mensagens;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class MensagemProvider {
    private final YamlConfiguration mensagens;

    public MensagemProvider(YamlConfiguration mensagens) {
        this.mensagens = mensagens;
    }

    public FileConfiguration getConfig() {
        return mensagens;
    }

    public String get(String key, String... args) {
        String message = mensagens.getString(key, "Mensagem não encontrada: " + key);
        message = ChatColor.translateAlternateColorCodes('&', message);
        for (int i = 0; i < args.length; i++) {
            message = message.replace("%" + (i + 1), args[i]);
        }
        return message;
    }
}