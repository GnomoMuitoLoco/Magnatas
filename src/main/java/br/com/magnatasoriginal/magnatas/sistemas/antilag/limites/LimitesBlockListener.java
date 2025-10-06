package br.com.magnatasoriginal.magnatas.sistemas.antilag.limites;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.Plugin;

public class LimitesBlockListener implements Listener {

    private final LimitesManager limitesManager;

    public LimitesBlockListener(LimitesManager limitesManager, Plugin plugin) {
        this.limitesManager = limitesManager;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlockPlaced();
        Chunk chunk = block.getChunk();
        Material tipo = block.getType();
        String blocoId = tipo.getKey().toString();

        if (!limitesManager.estaLimitado(blocoId)) return;

        // ✅ Permissão para ignorar limite
        if (player.hasPermission("magnatas.admin.bypasslimites")) return;

        int limite = limitesManager.getLimite(blocoId);
        int quantidadeAtual = contarBlocosNaChunk(chunk, tipo, block);

        if (quantidadeAtual >= limite) {
            event.setCancelled(true);
            player.sendMessage("§cVocê atingiu o limite de §e" + limite + "§c para §b" + blocoId +
                    "§c nesta chunk. Use §e/limites§c para ver os blocos limitados.");
            Bukkit.getLogger().info("[Limites] BLOQUEADO: " + blocoId + " em chunk " + chunk.getX() + "," + chunk.getZ());
        }
    }

    private int contarBlocosNaChunk(Chunk chunk, Material tipo, Block ignorar) {
        int count = 0;
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = chunk.getWorld().getMinHeight(); y < chunk.getWorld().getMaxHeight(); y++) {
                    Block b = chunk.getBlock(x, y, z);
                    if (b.getType() == tipo && !b.getLocation().equals(ignorar.getLocation())) {
                        count++;
                    }
                }
            }
        }
        return count;
    }
}