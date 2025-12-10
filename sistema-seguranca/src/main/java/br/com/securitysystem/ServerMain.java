package br.com.securitysystem;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ServerMain {
    public static void main(String[] args) {

        // O Scheduler será responsável por rodar a simulação em background
        ScheduledExecutorService scheduler = null;

        try {
            // 1. Instancia a implementação remota. 
            //    (O construtor de SecuritySystemImpl fará a carga dos dados persistidos)
            SecuritySystemImpl remoteObj = new SecuritySystemImpl();

            // 2. Cria e exporta o Registry na porta padrão (1099)
            Registry registry = LocateRegistry.createRegistry(1099);
            
            // 3. Faz o binding do objeto remoto no Registry com o nome de serviço
            registry.bind("SecuritySystemService", remoteObj);

            // 4. Configura o Simulador para rodar a cada 5 segundos
            scheduler = Executors.newSingleThreadScheduledExecutor();
            scheduler.scheduleAtFixedRate(() -> {
                try {
                    // Chama o método de simulação no objeto remoto
                    remoteObj.checkAndTriggerAlarm();
                } catch (Exception e) {
                    // Captura exceções da chamada remota (embora improvável, é boa prática)
                    System.err.println("\n[SIMULADOR] Erro ao simular evento: " + e.getMessage());
                }
            }, 0, 5, TimeUnit.SECONDS);

            System.out.println("\nServidor de Segurança Predial pronto e aguardando requisições na porta 1099.");
            
            // Mantém a thread principal do servidor viva
            // Isso é necessário para evitar que a JVM encerre.
            while (true) {
                Thread.sleep(10000); 
            }

        } catch (Exception e) {
            System.err.println("\nErro Crítico no Servidor RMI: " + e.toString());
            e.printStackTrace();
        } finally {
            // Garante que o scheduler de simulação seja desligado se houver um erro
            if (scheduler != null) {
                System.out.println("\nDesligando o simulador de sensores...");
                scheduler.shutdownNow(); 
            }
        }
    }
}
