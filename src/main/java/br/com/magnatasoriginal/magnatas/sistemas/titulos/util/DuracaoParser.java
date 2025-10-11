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
        if (millis == 0) return "Expirado";

        long segundos = millis / 1000;
        long minutos = segundos / 60;
        long horas = minutos / 60;
        long dias = horas / 24;

        horas = horas % 24;
        minutos = minutos % 60;
        segundos = segundos % 60;

        StringBuilder sb = new StringBuilder();
        if (dias > 0) sb.append(dias).append(" dia").append(dias > 1 ? "s" : "");
        if (horas > 0) {
            if (sb.length() > 0) sb.append(" e ");
            sb.append(horas).append("h");
        }
        if (dias == 0 && minutos > 0) {
            if (sb.length() > 0) sb.append(" e ");
            sb.append(minutos).append("min");
        }
        if (dias == 0 && horas == 0 && minutos == 0 && segundos > 0) {
            sb.append(segundos).append("s");
        }

        return sb.toString();
    }
}