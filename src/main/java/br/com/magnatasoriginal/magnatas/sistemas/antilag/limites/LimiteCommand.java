package br.com.magnatasoriginal.magnatas.sistemas.antilag.limites;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.List;

public class LimiteCommand implements CommandExecutor {

    private final LimitesManager limitesManager;
    private final Plugin plugin;

    public LimiteCommand(LimitesManager limitesManager, Plugin plugin) {
        this.limitesManager = limitesManager;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Este comando só pode ser usado por jogadores.");
            return true;
        }

        if (!player.hasPermission("magnatas.limite")) {
            player.sendMessage("§cVocê não tem permissão para usar este comando.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage("§eUso: §a/limite add <quantidade> §e| §a/limite remover §e| §a/limite scan [raio] §e| §a/limite scanall [mundo]");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "add" -> {
                Material itemNaMao = player.getInventory().getItemInMainHand().getType();
                if (itemNaMao == Material.AIR) {
                    player.sendMessage("§cVocê precisa estar segurando um bloco na mão.");
                    return true;
                }

                if (args.length != 2) {
                    player.sendMessage("§cUso correto: §e/limite add <quantidade>");
                    return true;
                }

                int quantidade;
                try {
                    quantidade = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    player.sendMessage("§cQuantidade inválida.");
                    return true;
                }

                String blocoId = itemNaMao.getKey().toString();
                limitesManager.adicionarLimite(blocoId, quantidade);
                player.sendMessage("§aLimite adicionado: §b" + blocoId + " §7→ §e" + quantidade + " §7por chunk.");
            }

            case "remover" -> {
                Material itemNaMao = player.getInventory().getItemInMainHand().getType();
                if (itemNaMao == Material.AIR) {
                    player.sendMessage("§cVocê precisa estar segurando um bloco na mão.");
                    return true;
                }

                String blocoId = itemNaMao.getKey().toString();
                if (!limitesManager.estaLimitado(blocoId)) {
                    player.sendMessage("§cEste bloco não está limitado.");
                    return true;
                }

                limitesManager.removerLimite(blocoId);
                player.sendMessage("§aLimite removido para §b" + blocoId);
            }

            case "scan" -> {
                int raio = 0;
                if (args.length > 1) {
                    try {
                        raio = Integer.parseInt(args[1]);
                        if (raio < 0 || raio > 5) {
                            player.sendMessage("§cRaio inválido. Use um valor entre 0 e 5.");
                            return true;
                        }
                    } catch (NumberFormatException e) {
                        player.sendMessage("§cRaio inválido. Use um número.");
                        return true;
                    }
                }

                Chunk origem = player.getLocation().getChunk();
                String dimensao = origem.getWorld().getKey().toString();
                player.sendMessage("§7Escaneando chunks no raio §b" + raio + " §7em §f" + dimensao + "§7...");

                for (int dx = -raio; dx <= raio; dx++) {
                    for (int dz = -raio; dz <= raio; dz++) {
                        Chunk chunk = origem.getWorld().getChunkAt(origem.getX() + dx, origem.getZ() + dz);
                        escanearChunk(player, chunk);
                    }
                }
            }

            case "scanall" -> {
                int delayTicks = 10;
                int loteTamanho = 10;

                World mundoAlvo;
                if (args.length > 1) {
                    String nomeMundo = args[1];
                    mundoAlvo = Bukkit.getWorld(nomeMundo);
                    if (mundoAlvo == null) {
                        player.sendMessage("§cMundo §e" + nomeMundo + " §cnão encontrado.");
                        return true;
                    }
                } else {
                    mundoAlvo = player.getWorld();
                }

                List<Chunk> chunks = Arrays.asList(mundoAlvo.getLoadedChunks());
                player.sendMessage("§7Iniciando varredura no mundo §b" + mundoAlvo.getName() + " §7com §b" + loteTamanho + " chunks por lote §7e §b" + delayTicks + " ticks de intervalo...");

                new ChunkScanDistribuido(plugin, limitesManager, player, chunks, loteTamanho).runTaskTimer(plugin, 0L, delayTicks);
            }

            default -> player.sendMessage("§cSubcomando desconhecido. Use §e/limite add <quantidade> §e| §e/limite remover §e| §e/limite scan [raio] §e| §e/limite scanall [mundo]");
        }

        return true;
    }

    private void escanearChunk(Player player, Chunk chunk) {
        String dimensao = chunk.getWorld().getKey().toString();
        int x = chunk.getX() << 4;
        int z = chunk.getZ() << 4;
        int y = chunk.getWorld().getHighestBlockYAt(x, z);

        for (String blocoId : limitesManager.getBlocosLimitados()) {
            int limite = limitesManager.getLimite(blocoId);
            int atual = contarBlocosNaChunk(chunk, blocoId.toLowerCase());

            if (atual > limite) {
                String alerta = String.format(
                        "§c⚠ Bloco §e%s §7(%d/%d) §cem §b[%s §7%d, %d, %d]",
                        blocoId, atual, limite, dimensao, x, y, z
                );
                Bukkit.getScheduler().runTask(plugin, () -> player.sendMessage(alerta));
            }
        }
    }

    private int contarBlocosNaChunk(Chunk chunk, String blocoIdEsperado) {
        int count = 0;
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = 0; y <= 128; y++) {
                    Block block = chunk.getBlock(x, y, z);
                    String blocoIdAtual = block.getType().getKey().toString().toLowerCase();
                    if (blocoIdAtual.equals(blocoIdEsperado)) {
                        count++;
                    }
                }
            }
        }
        return count;
    }
}