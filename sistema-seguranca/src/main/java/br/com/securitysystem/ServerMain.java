package br.com.securitysystem;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ServerMain {
    public static void main(String[] args) {

        ScheduledExecutorService scheduler = null;

        try {
            SecuritySystemImpl remoteObj = new SecuritySystemImpl();

            Registry registry = LocateRegistry.createRegistry(1099);
            
            registry.bind("SecuritySystemService", remoteObj);

            scheduler = Executors.newSingleThreadScheduledExecutor();
            scheduler.scheduleAtFixedRate(() -> {
                try {
                    remoteObj.checkAndTriggerAlarm();
                } catch (Exception e) {
                    System.err.println("\n[SIMULADOR] Erro ao simular evento: " + e.getMessage());
                }
            }, 0, 5, TimeUnit.SECONDS);

            System.out.println("\nServidor de Segurança Predial pronto e aguardando requisições na porta 1099.");
            
            while (true) {
                Thread.sleep(10000); 
            }

        } catch (Exception e) {
            System.err.println("\nErro Crítico no Servidor RMI: " + e.toString());
            e.printStackTrace();
        } finally {
            if (scheduler != null) {
                System.out.println("\nDesligando o simulador de sensores...");
                scheduler.shutdownNow(); 
            }
        }
    }
}
