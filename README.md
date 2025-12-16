📄 Guia de Execução do Projeto: Sistema de Segurança RMI/JavaFX

Este guia rápido fornece os passos necessários para compilar, iniciar o servidor RMI e executar o cliente JavaFX do Sistema de Segurança.

O projeto utiliza Maven para gerenciar as dependências e o ciclo de vida da aplicação.

🚨 1. Pré-requisitos

Certifique-se de que os seguintes requisitos estão instalados e configurados em seu ambiente:

Java Development Kit (JDK): Versão 11 ou superior.

Apache Maven: Versão 3.6 ou superior instalada e configurada.

Terminais: Necessita de dois terminais (ou abas) para rodar o servidor e o cliente simultaneamente.

⚙️ 2. Compilação e Empacotamento

Este passo compila o código-fonte e gera o arquivo JAR executável do servidor.

Acesse o diretório raiz do projeto (sistema-seguranca):

cd sistema-seguranca

Execute o comando Maven para limpar compilações anteriores, compilar o código e gerar o pacote JAR:

mvn clean install

Nota: Este comando deve ser executado apenas uma vez, ou sempre que houver mudanças no código-fonte.

🖥️ 3. Execução do Servidor RMI (Serviço)

O servidor deve ser iniciado primeiro, pois ele publica o serviço que o cliente irá procurar.

Abra o primeiro terminal (Terminal A).

Execute o arquivo JAR gerado, especificando a classe principal do servidor:

java -cp target/sistema-seguranca-1.0-SNAPSHOT.jar br.com.securitysystem.ServerMain

Resultado Esperado (Terminal A): O servidor deve iniciar o RMI Registry e exibir uma mensagem de confirmação:

[SERVER INIT] Servidor RMI SecuritySystem iniciado em <IP_DO_SEU_HOST>:1099
[SERVER INIT] Serviço 'SecuritySystemService' pronto.

💻 4. Execução do Cliente JavaFX (Interface Gráfica)

O cliente irá se conectar ao servidor ativo para buscar o stub RMI e iniciar a interação.

Abra o segundo terminal (Terminal B).

Ainda no diretório raiz do projeto, use o plugin JavaFX do Maven para iniciar a aplicação cliente:

mvn javafx:run

Resultado Esperado (Terminal B):

A janela do Painel de Controle JavaFX será aberta.

O console deve exibir a mensagem: [CLIENTE FX] Conexão RMI estabelecida via Task.

O painel de controle deve exibir o estado inicial do sistema (DESATIVADO).

🛑 5. Parando a Execução

Para encerrar a aplicação:

Feche a janela do Painel de Controle JavaFX.

No Terminal A (onde o servidor está rodando), pressione Ctrl + C para encerrar o processo do servidor RMI.
