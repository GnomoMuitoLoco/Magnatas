package br.com.magnatasoriginal.magnatas.sistemas.titulos.util;

import java.util.concurrent.TimeUnit;

public class DuracaoParser {

    /**
     * Converte uma string de duração em milissegundos.
     * Exemplos aceitos:
     * - "permanente" -> -1
     * - "7d" -> 7 dias
     * - "1h" -> 1 hora
     * - "30m" -> 30 minutos
     * - "45s" -> 45 segundos
     */
    public static long parse(String input) {
        if (input == null) return -1;

        input = input.trim().toLowerCase();

        if (input.equals("permanente")) {
            return -1; // usamos -1 para indicar permanente
        }

        try {
            if (input.endsWith("d")) {
                int dias = Integer.parseInt(input.replace("d", ""));
                return TimeUnit.DAYS.toMillis(dias);
            } else if (input.endsWith("h")) {
                int horas = Integer.parseInt(input.replace("h", ""));
                return TimeUnit.HOURS.toMillis(horas);
            } else if (input.endsWith("m")) {
                int minutos = Integer.parseInt(input.replace("m", ""));
                return TimeUnit.MINUTES.toMillis(minutos);
            } else if (input.endsWith("s")) {
                int segundos = Integer.parseInt(input.replace("s", ""));
                return TimeUnit.SECONDS.toMillis(segundos);
            } else {
                // fallback: tenta interpretar como milissegundos direto
                return Long.parseLong(input);
            }
        } catch (NumberFormatException e) {
            return -1; // se não conseguir interpretar, assume permanente
        }
    }

    /**
     * Formata milissegundos em string legível.
     * Exemplo: 90061000 -> "1d 1h 1m 1s"
     */
    public static String formatar(long millis) {
        if (millis < 0) return "Permanente";

        long dias = TimeUnit.MILLISECONDS.toDays(millis);
        millis -= TimeUnit.DAYS.toMillis(dias);

        long horas = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(horas);

        long minutos = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutos);

        long segundos = TimeUnit.MILLISECONDS.toSeconds(millis);

        StringBuilder sb = new StringBuilder();
        if (dias > 0) sb.append(dias).append("d ");
        if (horas > 0) sb.append(horas).append("h ");
        if (minutos > 0) sb.append(minutos).append("m ");
        if (segundos > 0) sb.append(segundos).append("s");

        return sb.toString().trim();
    }
}