# RFC (Request for Comments) - Chat Server

## 1. Introdução

Este documento descreve o protocolo de comunicação e os comandos disponíveis para um servidor de chat. O servidor permite que os clientes se conectem, enviem mensagens públicas e privadas, executem ações especiais, mudem configurações e muito mais.

## 2. Convenções de Nomenclatura

- **\<name>**: Nome de usuário do cliente.
- **\<text>**: Texto da mensagem.
- **\<user>**: Nome de usuário de destino.
- **\<emoji>**: Nome ou código do emoji.

## 3. Sintaxe e Semântica

### 3.1 Comandos Básicos

- **JOIN \<name>**: Para ingressar no chat com um nome de usuário.
- **LEAVE**: Para sair do chat.
- **USERS**: Para listar os usuários presentes no chat.
- **MESSAGE \<text>**: Para enviar uma mensagem pública para todos os usuários.
- **PRIVATE \<user> \<text>**: Para enviar uma mensagem privada para um usuário específico.
- **HELP**: Para exibir a lista de comandos disponíveis.
- **BLOCK \<user>**: Para bloquear mensagens de um usuário específico.
- **UNBLOCK \<user>**: Para desbloquear mensagens de um usuário bloqueado.

### 3.2 Comandos Especiais

- **YODA**: Exibe um desenho do Yoda.
- **COFFEE \<user>**: Envia uma mensagem especial de café para um usuário.
- **IMPORTANT \<text>**: Envia uma mensagem importante com uma moldura retangular.
- **MUTE \<user>**: Muta um usuário específico.
- **UNMUTE \<user>**: Remove a mutação de um usuário.
- **CHANGE_NAME \<newName>**: Altera o nome de usuário.
- **CLEAR**: Limpa o console.
- **SET_STATUS \<status>**: Define o status do usuário.
- **STATUS \<user>**: Exibe o status de um usuário.
- **EMOJI \<emoji>**: Envia um emoji.

**Observação**: Alguns comandos requerem apenas o TOKEN, como "LEAVE," "USERS," "YODA," "HELP," "CLEAR," "UNBLOCK," "EMOJI," enquanto outros exigem conteúdo adicional, como "MESSAGE," "PRIVATE," "BLOCK," "COFFEE," "IMPORTANT," "MUTE," "UNMUTE," "CHANGE_NAME," "SET_STATUS," "STATUS," e "EMOJI." Certifique-se de seguir a sintaxe adequada para cada tipo de comando.

## 4. Porta de Comunicação

O servidor utiliza a porta 1337 para comunicação. Certifique-se de configurar seus clientes para se conectarem a esta porta ao usar o serviço de chat.

## 5. Limitações

- O servidor não pode controlar a cor da fonte nos terminais dos clientes, pois isso depende das configurações individuais do cliente, por isso optei por retirar a função.
- A reprodução de música é limitada a uma mensagem simples no cliente.

## 6. Considerações Finais

Este RFC descreve a sintaxe e a semântica dos comandos disponíveis para o servidor de chat. O protocolo permite uma variedade de interações e ações para os usuários do chat. Certifique-se de seguir as convenções de nomenclatura e as instruções específicas de cada comando ao utilizar o sistema. Certifique-se também de configurar os clientes para se conectarem à porta 1337 para se comunicarem com o servidor.