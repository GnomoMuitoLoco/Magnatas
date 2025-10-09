package br.com.magnatasoriginal.magnatas.sistemas.titulos.lojadetitulos;

import br.com.magnatasoriginal.magnatas.Magnatas;
import br.com.magnatasoriginal.magnatas.sistemas.mensagens.MensagemProvider;
import br.com.magnatasoriginal.magnatas.sistemas.titulos.Titulo;
import br.com.magnatasoriginal.magnatas.sistemas.titulos.TituloService;
import br.com.magnatasoriginal.magnatas.sistemas.economia.Tokens; // seu sistema de Tokens
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;

public class LojadeTitulos {

    private final Magnatas plugin;
    private final TituloService service;
    private final MensagemProvider mensagens;
    private final Tokens tokens; // integração com o sistema de Tokens

    public LojadeTitulos(Magnatas plugin, TituloService service, MensagemProvider mensagens, Tokens tokens) {
        this.plugin = plugin;
        this.service = service;
        this.mensagens = mensagens;
        this.tokens = tokens;
    }

    /**
     * Fluxo de compra de título usando Tokens
     */
    public void comprarTitulo(Player player, String tituloNome) {
        UUID uuid = player.getUniqueId();

        // Já possui?
        if (service.listarTitulos(uuid).contains(tituloNome.toLowerCase())) {
            player.sendMessage(mensagens.get("ja_possui"));
            return;
        }

        // Existe?
        Optional<Titulo> optTitulo = service.getManager().getTituloPorNome(tituloNome);
        if (optTitulo.isEmpty()) {
            player.sendMessage(mensagens.get("titulo_inexistente"));
            return;
        }

        Titulo titulo = optTitulo.get();
        int preco = titulo.getPreco();

        // Verifica saldo de Tokens
        int saldo = tokens.getTokenCount(uuid.toString());
        if (saldo < preco) {
            player.sendMessage(mensagens.get("sem_saldo"));
            return;
        }

        // Transação atômica: debita e concede
        tokens.removeTokens(uuid.toString(), preco);
        service.concederTitulo(uuid, titulo.getNome());

        player.sendMessage(mensagens.get("compra_sucesso", titulo.getNomeVisivel()));
    }
}