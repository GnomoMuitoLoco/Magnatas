# 🧠 Sobre o Plugin Magnatas
**Magnatas** é um plugin completo e original desenvolvido para oferecer modularidade ao Servidor Magnatas, conta com uma variedade de sistemas essenciais para a jogabilidade e administração. Criado por **GnomoMuitoLouco**, ele integra funcionalidades como:
- 📢 Sistema de mensagens e ajuda
- 🧱 Limite de blocos por chunk
- 🏠 Sistema de homes
- 🧭 Sistema de warps
- 🏪 Sistema de lojas de jogadores
- 💰 Sistema de tokens com comandos para jogadores e administradores

Com comandos intuitivos e permissões bem definidas, o plugin proporciona controle, organização e praticidade para servidores que buscam uma experiência personalizada e profissional
[Site oficial do servidor](https://servidormagnatas.com.br/)

## Comandos e Permissões
**Sistema de limites**
/limite - `magnatas.limites` - gerenciar limites de blocos
/limites - `default` - listar blocos limitados
(bônus) - `magnatas.admin.limites.bypass` - ignorar limites por chunk
(bônus) - `magnatas.admin.limites.alerta` - receber alertas de escaneamento
(bônus) - `magnatas.admin.limites.progresso` - ver progresso da varredura

**Sistema de homes**
- /sethome - `magnatas.homes.1` - definir uma home
- /home - `magnatas.homes.1` - teleportar para uma home
- /homes - `magnatas.homes.1` - listar homes
- /delhome - `magnatas.homes.1` - remover uma home
- (bônus) - `magnatas.homes.*` - permitir homes ilimitadas

**Sistema de warps**
- /setwarp - `magnatas.admin.setwarp` - definir warp pública
- /delwarp - `magnatas.admin.delwarp` - remover warp pública
- /warp - `magnatas.warp` - teleportar para warp
- /warps - `magnatas.warp` - listar warps públicas

**Sistema de lojas**
- /setloja - `magnatas.setloja` - salvar loja
- /delloja - `magnatas.setloja` - remover loja própria
- /lojas - `default` - mostrar lojas
- /tploja - `default` - teleportar para loja de outro jogador
- (bônus) - `magnatas.delloja.others` - remover loja de outro jogador
- (bônus) - `magnatas.admin` - administrar lojas

**Sistema de tokens**
- /token - `magnatas.token` - comandos de tokens para jogadores
- /addtoken - `magnatas.admin.token.add` - adicionar tokens
- /removetoken - `magnatas.admin.token.remove` - remover tokens
- /settoken - `magnatas.admin.token.set` - definir tokens
- /vertoken - `magnatas.admin.token.ver` - ver tokens de outro jogador
- (bônus) - `magnatas.vip.token` - receber 2 tokens diários

**Sistema de mensagens e ajuda**
- /magnatas - `default` - info, ajuda e reload do plugin
- (bônus) - `magnatas.staff.ajuda` - receber pedidos de ajuda
- (bônus) - `magnatas.reload` - recarregar o plugin
- (bônus) - `magnatas.bypasscooldown` - ignorar tempo de espera para teleportar
