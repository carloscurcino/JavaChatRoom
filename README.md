# RFC (Request for Comments) - Mini Chat Application

## 1. Introdução

Este documento descreve o funcionamento e os comandos disponíveis no serviço de chat desenvolvido em Java, que permite que os clientes se comuniquem entre si em um ambiente de chat.

## 2. Palavras-Chave

- **JOIN**: Usado para ingressar no chat com um nome de usuário.
- **LEAVE**: Permite sair do chat.
- **USERS**: Lista os usuários presentes no chat.
- **MESSAGE <text>**: Envia uma mensagem pública para todos os usuários.
- **PRIVATE <user> <text>**: Envia uma mensagem privada para um usuário específico.
- **HELP**: Exibe a lista de comandos disponíveis.
- **BLOCK <user>**: Bloqueia as mensagens de um usuário específico.
- **UNBLOCK <user>**: Desbloqueia as mensagens de um usuário bloqueado.
- **YODA**: Exibe um desenho do Yoda.
- **COFFEE <user>**: Envia uma mensagem especial de café para um usuário.
- **IMPORTANT <text>**: Envia uma mensagem importante com uma moldura retangular.
- **MUTE <user>**: Muta um usuário específico.
- **UNMUTE <user>**: Remove a mutação de um usuário.
- **CHANGE_NAME <newName>**: Altera o nome de usuário.
- **CLEAR**: Limpa o console.
- **SET_STATUS <status>**: Define o status do usuário.
- **STATUS <user>**: Exibe o status de um usuário.
- **EMOJI <emoji>**: Envia um emoji.
- **EMOJI_LIST**: Exibe a lista de emojis disponíveis.
- **PLAY_MUSIC**: Toca uma mensagem de música.

## 3. Sintaxe e Semântica

### 3.1 Comandos Básicos

- **JOIN <name>**: Para ingressar no chat com um nome de usuário.
- **LEAVE**: Para sair do chat.
- **USERS**: Para listar os usuários presentes no chat.
- **MESSAGE <text>**: Para enviar uma mensagem pública para todos os usuários.
- **PRIVATE <user> <text>**: Para enviar uma mensagem privada para um usuário específico.
- **HELP**: Para exibir a lista de comandos disponíveis.
- **BLOCK <user>**: Para bloquear mensagens de um usuário específico.
- **UNBLOCK <user>**: Para desbloquear mensagens de um usuário bloqueado.

### 3.2 Comandos Especiais

- **YODA**: Exibe um desenho do Yoda.
- **COFFEE <user>**: Envia uma mensagem especial de café para um usuário.
- **IMPORTANT <text>**: Envia uma mensagem importante com uma moldura retangular.
- **MUTE <user>**: Muta um usuário específico.
- **UNMUTE <user>**: Remove a mutação de um usuário.
- **CHANGE_NAME <newName>**: Altera o nome de usuário.
- **CLEAR**: Limpa o console.
- **SET_STATUS <status>**: Define o status do usuário.
- **STATUS <user>**: Exibe o status de um usuário.
- **EMOJI <emoji>**: Envia um emoji.
- **EMOJI_LIST**: Exibe a lista de emojis disponíveis.
- **PLAY_MUSIC**: Toca uma mensagem de música.

## 4. Limitações

- O servidor não pode controlar a cor da fonte nos terminais dos clientes, pois isso depende das configurações individuais do cliente.
- A reprodução de música é limitada a uma mensagem simples no cliente.

## 5. Considerações Finais

Este serviço de chat Java oferece uma variedade de recursos e comandos para permitir a comunicação entre os usuários. Os comandos são enviados pelo cliente e interpretados pelo servidor para executar ações específicas no ambiente de chat. Os usuários são incentivados a explorar e usar esses comandos para interagir e personalizar sua experiência no chat.