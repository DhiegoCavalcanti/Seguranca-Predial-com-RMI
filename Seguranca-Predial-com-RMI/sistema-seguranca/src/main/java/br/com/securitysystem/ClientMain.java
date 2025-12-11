package br.com.securitysystem;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class ClientMain {
    public static void main(String[] args) {
        String host = (args.length < 1) ? "localhost" : args[0];
        int port = 1099;

        try {
            // 1. Conexão e lookup do objeto remoto
            Registry registry = LocateRegistry.getRegistry(host, port);
            SecuritySystem stub = (SecuritySystem) registry.lookup("SecuritySystemService");

            System.out.println("\nCliente RMI Conectado ao Servidor " + host + ":" + port + "\n");

            Scanner scanner = new Scanner(System.in);
            boolean isRunning = true;

            while (isRunning) {
                displayMenu();

                if (!scanner.hasNextInt()) {
                    System.out.println("\nEntrada inválida. Por favor, digite um número.");
                    scanner.nextLine();
                    continue;
                }

                int option = scanner.nextInt();
                scanner.nextLine();

                try {
                    switch (option) {
                        case 1: // Ativar/Desativar Alarme
                            toggleAlarm(stub, scanner);
                            break;
                        case 2: // Consultar Status de Sensor Específico
                            consultSpecificStatus(stub, scanner);
                            break;
                        case 3: // Consultar Status de Todos os Sensores
                            consultStatusAll(stub);
                            break;
                        case 4: // Consultar Log de Eventos
                            consultLog(stub, scanner);
                            break;
                        case 5: // Resetar Alarme de Sensor Trancado
                            resetAlarmSensor(stub, scanner);
                            break;
                        case 6: // Adicionar Sensor
                            addSensor(stub, scanner);
                            break;
                        case 7: // Remover Sensor
                            removeSensor(stub, scanner);
                            break;
                        case 8: // NOVO: Mudar Status de Sensor (ABRIR/FECHAR)
                            changeStatusSensor(stub, scanner);
                            break;
                        case 9: // Encerrar
                            System.out.println("\nEncerrando aplicação cliente.");
                            isRunning = false;
                            break;
                        default:
                            System.out.println("\nOpção inválida. Tente novamente.");
                    }
                } catch (RemoteException re) {
                    System.err.println("\nErro na comunicação remota: " + re.getMessage());
                    System.err.println("Verifique se o servidor RMI ainda está ativo.");
                }
            }
            scanner.close();

        } catch (Exception e) {
            System.err.println("\nErro Crítico na conexão RMI. Verifique se o servidor está isRunning.");
            System.err.println("\nDetalhe: " + e.toString());
        }
    }

    // --- MÉTODOS DE INTERAÇÃO E CHAMADAS REMOTAS ---

    private static void displayMenu() {
        System.out.println("\n--- MENU DE CONTROLE DA SEGURANÇA PREDIAL ---");
        System.out.println("\n1. Ativar/Desativar Alarme");
        System.out.println("2. Consultar Status de um Sensor Específico");
        System.out.println("3. Consultar Status de Todos os Sensores (Simulação ATIVA)");
        System.out.println("4. Consultar Log de Eventos");
        System.out.println("5. Resetar Alarme de Sensor Trancado");
        System.out.println("6. Adicionar Sensor");
        System.out.println("7. Remover Sensor");
        System.out.println("8. Mudar Status de Sensor (ABRIR/FECHAR)"); // NOVO
        System.out.println("9. Encerrar Aplicação");
        System.out.print("\nEscolha uma opção (1-9): ");
    }

    private static void toggleAlarm(SecuritySystem stub, Scanner scanner) throws RemoteException {
        System.out.print("\nAtivar (S/s) ou Desativar (N/n) o alarme? ");
        String choice = scanner.nextLine().trim().toLowerCase();
        boolean status = choice.equals("s");

        String resposta = stub.setAlarmStatus(status);
        System.out.println("\n>> Resposta do Servidor: " + resposta);
    }

    private static void consultSpecificStatus(SecuritySystem stub, Scanner scanner) throws RemoteException {
        System.out.print("\nInforme o ID do sensor (ex: porta_principal): ");
        String sensorId = scanner.nextLine().trim();

        String status = stub.getSensorStatus(sensorId);
        System.out.println("\n>> Status do sensor '" + sensorId + "': " + status);
    }

    private static void consultStatusAll(SecuritySystem stub) throws RemoteException {
        System.out.println("\n>> Consultando Status de Todos os Sensores:");
        Map<String, String> allStatus = stub.getAllSensorStatus();

        if (allStatus.isEmpty()) {
            System.out.println("\nNenhum sensor registrado.");
            return;
        }

        for (Map.Entry<String, String> entry : allStatus.entrySet()) {
            // Usa um formato para alinhamento da saída
            System.out.printf("   - %-20s: %s\n", entry.getKey(), entry.getValue());
        }
    }

    private static void consultLog(SecuritySystem stub, Scanner scanner) throws RemoteException {
        System.out.print("\nQuantos eventos recentes deseja consultar? ");

        if (!scanner.hasNextInt()) {
            System.out.println("\nValor inválido. Retornando ao menu.");
            scanner.nextLine();
            return;
        }

        int limit = scanner.nextInt();
        scanner.nextLine();

        List<String> log = stub.getLogEvents(limit);
        System.out.println("\n>> Últimos " + log.size() + " Eventos do Log (Mais recentes primeiro):");

        if (log.isEmpty()) {
            System.out.println("   (Log de eventos vazio)");
            return;
        }

        for (String item : log) {
            System.out.println("   " + item);
        }
    }

    private static void resetAlarmSensor(SecuritySystem stub, Scanner scanner) throws RemoteException {
        System.out.print("\nInforme o ID do sensor para reset (ex: porta_principal): ");
        String sensorId = scanner.nextLine().trim();
        String resposta = stub.resetSensorAlarm(sensorId);
        System.out.println("\n>> Resposta do Servidor: " + resposta);
    }

    private static void addSensor(SecuritySystem stub, Scanner scanner) throws RemoteException {
        System.out.print("\nInforme o ID do novo sensor (ex: portao_garagem): ");
        String sensorId = scanner.nextLine().trim();
        String resposta = stub.addSensor(sensorId);
        System.out.println("\n>> Resposta do Servidor: " + resposta);
    }

    private static void removeSensor(SecuritySystem stub, Scanner scanner) throws RemoteException {
        System.out.print("\nInforme o ID do sensor a ser removido: ");
        String sensorId = scanner.nextLine().trim();
        String resposta = stub.removeSensor(sensorId);
        System.out.println("\n>> Resposta do Servidor: " + resposta);
    }
    
    /**
     * NOVO MÉTODO: Chama o setSensorStatus remoto para alterar o estado do sensor.
     */
    private static void changeStatusSensor(SecuritySystem stub, Scanner scanner) throws RemoteException {
        System.out.print("\nInforme o ID do sensor para alterar o status: ");
        String sensorId = scanner.nextLine().trim();

        System.out.print("Abrir (S/s) ou Fechar (N/n)? ");
        String choice = scanner.nextLine().trim().toLowerCase();
        boolean status = choice.equals("s"); // True se 's', False se 'n'

        String resposta = stub.setSensorStatus(sensorId, status);
        System.out.println("\n>> Resposta do Servidor: " + resposta);
    }
}
