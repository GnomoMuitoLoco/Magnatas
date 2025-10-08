package br.com.magnatasoriginal.magnatas.sistemas.economia;

import br.com.magnatasoriginal.magnatas.Magnatas;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TokenCommand implements CommandExecutor {

    private final Magnatas plugin;
    private final Tokens tokens;

    public TokenCommand(Magnatas plugin, Tokens tokens) {
        this.plugin = plugin;
        this.tokens = tokens;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cApenas jogadores podem usar esse comando.");
            return true;
        }

        if (args.length == 0) {
            // Interface visual com botões
            player.sendMessage("§8=============================================");
            player.sendMessage("§fSistema de Tokens:");

            TextComponent line = new TextComponent("§fComandos: ");

            TextComponent receber = new TextComponent("§aReceber");
            receber.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/token receber"));
            line.addExtra(receber);
            line.addExtra(new TextComponent("§f, "));

            TextComponent info = new TextComponent("§bInfo");
            info.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/token info"));
            line.addExtra(info);
            line.addExtra(new TextComponent("§f, "));

            TextComponent loja = new TextComponent("§eLoja");
            loja.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/token loja"));
            line.addExtra(loja);
            line.addExtra(new TextComponent("§f, "));

            TextComponent top = new TextComponent("§6Top");
            top.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/token top"));
            line.addExtra(top);

            player.spigot().sendMessage(line);

            player.sendMessage("§8=============================================");
            player.sendMessage("§7by Magnatas");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "receber":
                tokens.claimDailyToken(player);
                break;
            case "info":
                tokens.getTokenInfo(player);
                break;
            case "loja":
                plugin.getTokenLojaGUI().openMenu(player);
                break;
            case "top":
                tokens.showTokenTop(player);
                break;
            default:
                player.sendMessage("§cSubcomando inválido. Use /token para ver os comandos disponíveis.");
        }

        return true;
    }
}