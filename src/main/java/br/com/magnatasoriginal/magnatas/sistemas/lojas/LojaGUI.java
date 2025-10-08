package br.com.magnatasoriginal.magnatas.sistemas.lojas;

import br.com.magnatasoriginal.magnatas.Magnatas;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class LojaGUI implements Listener {

    private final Magnatas plugin;
    public LojaMenu lojaMenu;
    private final Map<Player, String> pendingDeletions = new HashMap<>();
    private final Map<Player, TeleportTask> pendingTeleports = new HashMap<>();

    public LojaGUI(Magnatas plugin) {
        this.plugin = plugin;
        this.lojaMenu = new LojaMenu();
        Bukkit.getPluginManager().registerEvents(this, plugin);
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onLojaAtualizada(LojaAtualizadaEvent event) {
                updateMenu();
            }
        }, plugin);
    }

    public void updateMenu() {
        new BukkitRunnable() {
            @Override
            public void run() {
                Map<String, Location> lojas = plugin.getAllLojas(); // nomes reais como chave
                int itemsPerPage = 28;
                int totalPages = Math.max(1, (int) Math.ceil((double) lojas.size() / itemsPerPage));

                lojaMenu.createPages(totalPages);

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        List<Map.Entry<String, Location>> lista = new ArrayList<>(lojas.entrySet());

                        for (int i = 0; i < lista.size(); i++) {
                            Map.Entry<String, Location> entry = lista.get(i);
                            int pageIndex = i / itemsPerPage;
                            if (pageIndex >= lojaMenu.getTotalPages()) break;

                            Inventory page = lojaMenu.getPages()[pageIndex];
                            createLojaItemAsync(entry.getKey(), page);
                        }
                    }
                }.runTask(plugin);
            }
        }.runTaskAsynchronously(plugin);
    }

    private void createLojaItemAsync(String displayName, Inventory page) {
        new BukkitRunnable() {
            @Override
            public void run() {
                int visitCount = plugin.getVisitCount(plugin.normalize(displayName));

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        ItemStack item = new ItemStack(Material.PLAYER_HEAD, 1);
                        SkullMeta meta = (SkullMeta) item.getItemMeta();
                        if (meta == null) return;

                        meta.setOwner(displayName);
                        meta.setDisplayName(ChatColor.YELLOW + displayName + " - Loja");

                        List<String> lore = new ArrayList<>();
                        lore.add(ChatColor.GRAY + "Visitas: " + visitCount);
                        lore.add("");
                        lore.add(ChatColor.WHITE + "Clique esquerdo para visitar");
                        lore.add(ChatColor.WHITE + "Clique direito para remover (apenas dono)");

                        meta.setLore(lore);
                        item.setItemMeta(meta);

                        int slot = getNextAvailableSlot(page);
                        if (slot != -1) {
                            page.setItem(slot, item);
                        }
                    }
                }.runTask(plugin);
            }
        }.runTaskAsynchronously(plugin);
    }

    private int getNextAvailableSlot(Inventory inventory) {
        int[] validSlots = {
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34,
                37, 38, 39, 40, 41, 42, 43
        };
        for (int slot : validSlots) {
            if (inventory.getItem(slot) == null) {
                return slot;
            }
        }
        return -1;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().startsWith("Lojas dos Magnatas")) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null) return;

            Material itemType = event.getCurrentItem().getType();
            Player player = (Player) event.getWhoClicked();

            if (itemType == Material.PLAYER_HEAD) {
                String displayName = Objects.requireNonNull(event.getCurrentItem().getItemMeta()).getDisplayName();
                String playerName = ChatColor.stripColor(displayName).split(" - ")[0];
                String normalizedName = plugin.normalize(playerName);

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        Location loc = plugin.getLojaLocation(normalizedName);

                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (event.isRightClick()) {
                                    if (player.hasPermission("magnatas.delloja") || playerName.equals(player.getName())) {
                                        pendingDeletions.put(player, playerName);
                                        player.sendMessage(plugin.getMensagens().get("confirmar_remocao", playerName));
                                    } else {
                                        player.sendMessage("Você não tem permissão para excluir a loja de outros jogadores.");
                                    }
                                } else {
                                    if (loc != null) {
                                        teleportPlayer(player, loc, playerName);
                                    } else {
                                        player.sendMessage(plugin.getMensagens().get("loja.nao_encontrada", playerName));
                                    }
                                }
                            }
                        }.runTask(plugin);
                    }
                }.runTaskAsynchronously(plugin);
            } else if (itemType == Material.ARROW) {
                lojaMenu.handleClick(event);
            }
        }
    }

    public void teleportPlayer(Player player, Location loc, String playerName) {
        if (player.hasPermission("magnatas.bypasscooldown")) {
            player.teleport(loc);
            plugin.logVisit(player.getName(), playerName);
            player.sendMessage(plugin.getMensagens().get("loja.teleportado", playerName));
        } else {
            if (pendingTeleports.containsKey(player)) {
                player.sendMessage(plugin.getMensagens().get("loja.teleporte_em_progresso"));
                return;
            }

            TeleportTask task = new TeleportTask(player, loc, playerName, 5);
            pendingTeleports.put(player, task);
            task.start();
        }
    }

    private class TeleportTask {
        private final Player player;
        private final Location targetLocation;
        private final String playerName;
        private int secondsRemaining;
        private boolean cancelled = false;

        public TeleportTask(Player player, Location loc, String playerName, int seconds) {
            this.player = player;
            this.targetLocation = loc;
            this.playerName = playerName;
            this.secondsRemaining = seconds;
        }

        public void start() {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (cancelled) {
                        cancel();
                        return;
                    }

                    if (secondsRemaining <= 0) {
                        completeTeleport();
                        cancel();
                        return;
                    }

                    player.sendMessage(plugin.getMensagens().get("loja.tempo_restante", String.valueOf(secondsRemaining)));
                    secondsRemaining--;
                }
            }.runTaskTimer(plugin, 0L, 20L);
        }

        public void cancel() {
            cancelled = true;
            pendingTeleports.remove(player);
            player.sendMessage(plugin.getMensagens().get("loja.teleporte_cancelado"));
        }

        private void completeTeleport() {
            if (!cancelled && pendingTeleports.containsKey(player)) {
                player.teleport(targetLocation);
                plugin.logVisit(player.getName(), playerName);
                player.sendMessage(plugin.getMensagens().get("loja.teleportado", playerName));
                pendingTeleports.remove(player);
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (pendingTeleports.containsKey(player)) {
            Location from = event.getFrom();
            Location to = event.getTo();
            if (to != null && (from.getBlockX() != to.getBlockX() || from.getBlockY() != to.getBlockY() || from.getBlockZ() != to.getBlockZ())) {
                pendingTeleports.get(player).cancel();
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getView().getTitle().equals("Confirmação de Remoção")) {
            Player player = (Player) event.getPlayer();
            String lojaOwner = pendingDeletions.remove(player);
            if (lojaOwner != null) {
                player.performCommand("delloja " + lojaOwner);
            }
        }
    }
}