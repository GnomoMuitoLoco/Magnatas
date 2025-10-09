package br.com.magnatasoriginal.magnatas.sistemas.titulos;

import br.com.magnatasoriginal.magnatas.Magnatas;
import br.com.magnatasoriginal.magnatas.sistemas.titulos.Titulo;
import br.com.magnatasoriginal.magnatas.sistemas.titulos.TituloService;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Integração com PlaceholderAPI com foco em:
 * - Segurança contra NPE e entradas inválidas
 * - Redução de chamadas repetidas
 * - Cache leve com TTL para placeholders dinâmicas
 * - Compatibilidade com futuras versões da API
 *
 * Placeholders (prefixo: magnatas):
 * - %magnatas_titulo_equipado%                 → id do título equipado
 * - %magnatas_titulo_equipado_nome%            → nome visível do título equipado
 * - %magnatas_titulo_equipado_descricao%       → descrição do título equipado
 * - %magnatas_titulos_count%                   → quantidade de títulos do jogador
 *
 * Por título (id = nome interno em lowercase):
 * - %magnatas_titulo_<id>_possui%
 * - %magnatas_titulo_<id>_expira_em%           → ms restantes (ou -1 se permanente/sem expiração)
 * - %magnatas_titulo_<id>_expira_em_formatado% → texto amigável (ex.: 2d 3h 10m)
 */
public class MagnatasTitulosExpansion extends PlaceholderExpansion {

    private final Magnatas plugin;
    private final TituloService tituloService;

    // Cache leve por jogador com TTL curto
    private final Map<UUID, CacheEntry> cachePorJogador = new ConcurrentHashMap<>();

    // TTL configurável em ms (padrão: 1000ms)
    private final long ttlMillis;

    public MagnatasTitulosExpansion(Magnatas plugin, TituloService tituloService) {
        this.plugin = plugin;
        this.tituloService = tituloService;
        this.ttlMillis = plugin.getConfig().getLong("placeholders.titulos.ttlMillis", 1000L);
    }

    @Override
    public String getIdentifier() {
        return "magnatas";
    }

    @Override
    public String getAuthor() {
        return "MagnatasOriginal";
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true; // mantém registrada entre reloads
    }

    @Override
    public boolean canRegister() {
        return true; // compatibilidade futura
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        // Validações defensivas
        if (player == null || player.getUniqueId() == null || params == null || params.isEmpty()) {
            return "";
        }

        final UUID uuid = player.getUniqueId();
        final String key = params.toLowerCase(Locale.ROOT).trim();

        // Cache leve com TTL
        CacheEntry entry = cachePorJogador.compute(uuid, (u, old) -> {
            if (old == null || old.expirou(ttlMillis)) {
                return CacheEntry.carregar(uuid, tituloService);
            }
            return old;
        });

        switch (key) {
            case "titulo_equipado":
                return entry.equipadoId != null ? entry.equipadoId : "";
            case "titulo_equipado_nome":
                return entry.equipadoVisivel != null ? entry.equipadoVisivel : "";
            case "titulo_equipado_descricao":
                return entry.equipadoDescricao != null ? entry.equipadoDescricao : "";
            case "titulos_count":
                return String.valueOf(entry.possuídos.size());
            default:
                if (key.startsWith("titulo_")) {
                    return handlePorTitulo(entry, key);
                }
                return "";
        }
    }

    private String handlePorTitulo(CacheEntry entry, String key) {
        // Esperado: titulo_<id>_<campo>
        int lastUnderscore = key.lastIndexOf('_');
        if (lastUnderscore <= "titulo_".length()) return "";

        String idNorm = key.substring("titulo_".length(), lastUnderscore).toLowerCase(Locale.ROOT);
        String campo = key.substring(lastUnderscore + 1);

        switch (campo) {
            case "possui":
                return String.valueOf(entry.possuídos.contains(idNorm));

            case "expira_em": {
                Titulo t = entry.metadados.get(idNorm);
                if (t == null || t.isPermanente() || t.getExpiraEm().isEmpty()) return "-1";
                long msRestante = Duration.between(Instant.now(), t.getExpiraEm().get()).toMillis();
                return String.valueOf(Math.max(msRestante, 0));
            }

            case "expira_em_formatado": {
                Titulo t = entry.metadados.get(idNorm);
                if (t == null || t.isPermanente() || t.getExpiraEm().isEmpty()) return "";
                long ms = Duration.between(Instant.now(), t.getExpiraEm().get()).toMillis();
                return formatarDuracao(Math.max(ms, 0));
            }

            default:
                return "";
        }
    }

    // Utilitário de formatação: ms → "Xd Yh Zm"
    private static String formatarDuracao(long ms) {
        long totalSeconds = ms / 1000;
        long days = totalSeconds / 86400;
        long hours = (totalSeconds % 86400) / 3600;
        long minutes = (totalSeconds % 3600) / 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("d ");
        if (hours > 0 || days > 0) sb.append(hours).append("h ");
        sb.append(minutes).append("m");
        return sb.toString().trim();
    }

    /**
     * Entrada de cache por jogador
     * - Evita NPEs armazenando dados normalizados
     * - TTL curto para placeholders dinâmicas
     */
    private static class CacheEntry {
        final long carregaEm;
        final Set<String> possuídos;          // ids em lowercase
        final Map<String, Titulo> metadados;  // id → Titulo
        final String equipadoId;
        final String equipadoVisivel;
        final String equipadoDescricao;

        CacheEntry(long carregaEm,
                   Set<String> possuídos,
                   Map<String, Titulo> metadados,
                   String equipadoId,
                   String equipadoVisivel,
                   String equipadoDescricao) {
            this.carregaEm = carregaEm;
            this.possuídos = possuídos;
            this.metadados = metadados;
            this.equipadoId = equipadoId;
            this.equipadoVisivel = equipadoVisivel;
            this.equipadoDescricao = equipadoDescricao;
        }

        boolean expirou(long ttlMillis) {
            return (System.currentTimeMillis() - carregaEm) >= ttlMillis; // se necessário, ajuste abaixo
        }

        static CacheEntry carregar(UUID uuid, TituloService service) {
            long agora = System.currentTimeMillis();

            Set<String> poss = new HashSet<>(service.listarTitulos(uuid));
            Map<String, Titulo> meta = new HashMap<>();
            for (String id : poss) {
                service.getManager().getTituloPorNome(id).ifPresent(t -> meta.put(id.toLowerCase(Locale.ROOT), t));
            }

            String equipId = null;
            String equipNome = null;
            String equipDesc = null;

            Optional<String> equipOpt = service.getTituloEquipado(uuid);
            if (equipOpt.isPresent()) {
                equipId = equipOpt.get();
                Titulo t = meta.get(equipId.toLowerCase(Locale.ROOT));
                if (t != null) {
                    equipNome = t.getNomeVisivel();
                    equipDesc = t.getDescricao();
                }
            }

            return new CacheEntry(agora, poss, meta, equipId, equipNome, equipDesc);
        }
    }
}