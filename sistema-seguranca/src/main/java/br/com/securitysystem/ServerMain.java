package br.com.securitysystem;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ServerMain {
    
    private static final int RMI_PORT = 1099;
    // CRÍTICO: Usando o IP local do usuário para estabilidade RMI
    private static final String RMI_HOST = "192.168.56.1"; 

    public static void main(String[] args) {
        try {
            // 1. Força o RMI a se ligar neste IP. Sem isso, a conexão falha.
            System.setProperty("java.rmi.server.hostname", RMI_HOST);
            
            // 2. Cria a implementação do objeto RMI
            SecuritySystem remoteObj = new SecuritySystemImpl();

            // 3. Inicia o registry RMI na porta padrão
            Registry registry = LocateRegistry.createRegistry(RMI_PORT);

            // 4. Liga a implementação do objeto RMI ao registry, dando-lhe um nome.
            registry.bind("SecuritySystemService", remoteObj);

            System.out.println(">>> Servidor RMI SecuritySystem iniciado em " + RMI_HOST + ":" + RMI_PORT);
            System.out.println(">>> Serviço 'SecuritySystemService' pronto.");

        } catch (Exception e) {
            System.err.println("Erro Crítico no Servidor RMI: " + e.toString());
            e.printStackTrace();
        }
    }
}