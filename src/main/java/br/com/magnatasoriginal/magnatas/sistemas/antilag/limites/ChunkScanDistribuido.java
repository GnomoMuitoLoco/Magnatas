package br.com.magnatasoriginal.magnatas.sistemas.antilag.limites;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class ChunkScanDistribuido extends BukkitRunnable {

    private final Plugin plugin;
    private final LimitesManager limitesManager;
    private final Player executor;
    private final List<Chunk> chunks;
    private final int loteTamanho;
    private int index = 0;
    private int loteContador = 0;

    public ChunkScanDistribuido(Plugin plugin, LimitesManager limitesManager, Player executor, List<Chunk> chunks, int loteTamanho) {
        this.plugin = plugin;
        this.limitesManager = limitesManager;
        this.executor = executor;
        this.chunks = chunks;
        this.loteTamanho = loteTamanho;
    }

    @Override
    public void run() {
        int limite = Math.min(index + loteTamanho, chunks.size());
        for (; index < limite; index++) {
            escanearChunk(chunks.get(index));
        }

        loteContador++;
        if ((loteContador % 5 == 0 || index >= chunks.size()) && executor.hasPermission("magnatas.admin.limites.progresso")) {
            int percent = (int) ((index / (double) chunks.size()) * 100);
            Bukkit.getScheduler().runTask(plugin, () ->
                    executor.sendMessage("§7Progresso: §b" + index + "/" + chunks.size() + " §7chunks (" + percent + "%)")
            );
        }

        if (index >= chunks.size()) {
            Bukkit.getScheduler().runTask(plugin, () ->
                    executor.sendMessage("§aVarredura distribuída concluída. Total de chunks escaneados: §b" + chunks.size())
            );
            cancel();
        }
    }

    private void escanearChunk(Chunk chunk) {
        String dimensao = chunk.getWorld().getKey().toString();
        int x = chunk.getX() << 4;
        int z = chunk.getZ() << 4;
        int y = chunk.getWorld().getHighestBlockYAt(x, z);

        for (String blocoId : limitesManager.getBlocosLimitados()) {
            int limite = limitesManager.getLimite(blocoId);
            int atual = contarBlocosNaChunk(chunk, blocoId.toLowerCase());

            if (atual > limite && executor.hasPermission("magnatas.admin.limites.alerta")) {
                String alerta = String.format(
                        "§c⚠ Bloco §e%s §7(%d/%d) §cem §b[%s §7%d, %d, %d]",
                        blocoId, atual, limite, dimensao, x, y, z
                );

                TextComponent mensagem = new TextComponent(alerta + " §7[§bClique para ir§7]");
                mensagem.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                        "/tp " + executor.getName() + " " + x + " " + y + " " + z));
                mensagem.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new ComponentBuilder("§eTeleportar para o chunk").create()));

                Bukkit.getScheduler().runTask(plugin, () -> executor.spigot().sendMessage(mensagem));
            }
        }
    }

    private int contarBlocosNaChunk(Chunk chunk, String blocoIdEsperado) {
        int count = 0;
        int minY = chunk.getWorld().getMinHeight();
        int maxY = chunk.getWorld().getMaxHeight();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = minY; y <= maxY; y++) {
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

    public static List<Chunk> getChunksCarregados() {
        List<Chunk> chunks = new java.util.ArrayList<>();
        for (World world : Bukkit.getWorlds()) {
            chunks.addAll(java.util.Arrays.asList(world.getLoadedChunks()));
        }
        return chunks;
    }
}