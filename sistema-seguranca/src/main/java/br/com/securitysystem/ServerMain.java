package br.com.securitysystem;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ServerMain {

    private static final int RMI_PORT = 1099;
    // IP do Servidor RMI
    private static final String RMI_HOST = "10.239.171.7";

    public static void main(String[] args) {
        try {
            System.setProperty("java.rmi.server.hostname", RMI_HOST);

            SecuritySystem remoteObj = new SecuritySystemImpl();

            Registry registry = LocateRegistry.createRegistry(RMI_PORT);

            registry.bind("SecuritySystemService", remoteObj);

            System.out.println("\n[SERVER INIT] Servidor RMI SecuritySystem iniciado em " + RMI_HOST + ":" + RMI_PORT);
            System.out.println("[SERVER INIT] Serviço 'SecuritySystemService' pronto.");

        } catch (Exception e) {
            System.err.println("\nErro Crítico no Servidor RMI: " + e.toString());
            e.printStackTrace();
        }
    }
}
