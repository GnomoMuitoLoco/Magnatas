package br.com.magnatasoriginal.magnatas.sistemas.economia;

import br.com.magnatasoriginal.magnatas.Magnatas;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AdminTokenCommand implements CommandExecutor {

    private final Tokens tokens;

    public AdminTokenCommand(Magnatas plugin, Tokens tokens) {
        this.tokens = tokens;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 2 && !command.getName().equalsIgnoreCase("vertoken")) {
            sender.sendMessage("§cUso: /" + label + " <nick> <quantidade>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage("§cJogador não encontrado.");
            return true;
        }

        String uuid = target.getUniqueId().toString();

        switch (command.getName().toLowerCase()) {
            case "addtoken":
                int add = Integer.parseInt(args[1]);
                tokens.addTokens(uuid, add);
                sender.sendMessage("§aAdicionado " + add + " Tokens para " + target.getName());
                break;
            case "removetoken":
                int remove = Integer.parseInt(args[1]);
                tokens.removeTokens(uuid, remove);
                sender.sendMessage("§aRemovido " + remove + " Tokens de " + target.getName());
                break;
            case "settoken":
                int set = Integer.parseInt(args[1]);
                tokens.setTokens(uuid, set);
                sender.sendMessage("§aTokens de " + target.getName() + " definidos para " + set);
                break;
            case "vertoken":
                tokens.getTokenCount(uuid, sender instanceof Player ? (Player) sender : null);
                break;
        }

        return true;
    }
}