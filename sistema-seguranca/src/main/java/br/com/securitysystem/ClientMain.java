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
            Registry registry = LocateRegistry.getRegistry(host, port);
            SecuritySystem stub = (SecuritySystem) registry.lookup("SecuritySystemService");

            System.out.println("Cliente RMI Conectado ao Servidor " + host + ":" + port);

            Scanner scanner = new Scanner(System.in);
            boolean isRunning = true;

            while (isRunning) {
                displayMenu();

                if (!scanner.hasNextInt()) {
                    System.out.println("Entrada inválida. Por favor, digite um número.");
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
                        case 8: // Mudar Status de Sensor (ABRIR/FECHAR)
                            changeStatusSensor(stub, scanner);
                            break;
                        case 9: // Encerrar
                            System.out.println("Encerrando aplicação cliente.");
                            isRunning = false;
                            break;
                        default:
                            System.out.println("Opção inválida. Tente novamente.");
                    }
                } catch (RemoteException re) {
                    System.err.println("Erro na comunicação remota: " + re.getMessage());
                    System.err.println("Verifique se o servidor RMI ainda está ativo.");
                }
            }
            scanner.close();

        } catch (Exception e) {
            System.err.println("Erro Crítico na conexão RMI. Verifique se o servidor está isRunning.");
            System.err.println("Detalhe: " + e.toString());
        }
    }

    // MÉTODOS DE INTERAÇÃO E CHAMADAS REMOTAS

    private static void displayMenu() {
        System.out.println("\n--- MENU DE CONTROLE DA SEGURANÇA PREDIAL ---");
        System.out.println("\n1. Ativar/Desativar Alarme");
        System.out.println("2. Consultar Status de um Sensor Específico");
        System.out.println("3. Consultar Status de Todos os Sensores (Simulação ATIVA)");
        System.out.println("4. Consultar Log de Eventos");
        System.out.println("5. Resetar Alarme de Sensor Trancado");
        System.out.println("6. Adicionar Sensor");
        System.out.println("7. Remover Sensor");
        System.out.println("8. Mudar Status de Sensor (ABRIR/FECHAR)");
        System.out.println("9. Encerrar Aplicação");
        System.out.print("\nEscolha uma opção (1-9): ");
    }

    private static void toggleAlarm(SecuritySystem stub, Scanner scanner) throws RemoteException {
        System.out.print("Ativar (S/s) ou Desativar (N/n) o alarme? ");
        String choice = scanner.nextLine().trim().toLowerCase();
        boolean status = choice.equals("s");

        String response = stub.setAlarmStatus(status);
        System.out.println(" > Resposta do Servidor: " + response);
    }

    private static void consultSpecificStatus(SecuritySystem stub, Scanner scanner) throws RemoteException {
        System.out.print("Informe o ID do sensor (ex: porta_principal): ");
        String sensorId = scanner.nextLine().trim();

        String status = stub.getSensorStatus(sensorId);
        System.out.println(" > Status do sensor '" + sensorId + "': " + status);
    }

    private static void consultStatusAll(SecuritySystem stub) throws RemoteException {
        System.out.println(" > Consultando Status de Todos os Sensores:");
        Map<String, String> allStatus = stub.getAllSensorStatus();

        if (allStatus.isEmpty()) {
            System.out.println("Nenhum sensor registrado.");
            return;
        }

        for (Map.Entry<String, String> entry : allStatus.entrySet()) {
            System.out.printf("   - %-20s: %s", entry.getKey(), entry.getValue());
        }
    }

    private static void consultLog(SecuritySystem stub, Scanner scanner) throws RemoteException {
        System.out.print("Quantos eventos recentes deseja consultar? ");

        if (!scanner.hasNextInt()) {
            System.out.println("Valor inválido. Retornando ao menu.");
            scanner.nextLine();
            return;
        }

        int limit = scanner.nextInt();
        scanner.nextLine();

        List<String> log = stub.getLogEvents(limit);
        System.out.println(" > Últimos " + log.size() + " Eventos do Log (Mais recentes primeiro):");

        if (log.isEmpty()) {
            System.out.println("   (Log de eventos vazio)");
            return;
        }

        for (String item : log) {
            System.out.println("   " + item);
        }
    }

    private static void resetAlarmSensor(SecuritySystem stub, Scanner scanner) throws RemoteException {
        System.out.print("Informe o ID do sensor para reset (ex: porta_principal): ");
        String sensorId = scanner.nextLine().trim();
        String response = stub.resetSensorAlarm(sensorId);
        System.out.println(" > Resposta do Servidor: " + response);
    }

    private static void addSensor(SecuritySystem stub, Scanner scanner) throws RemoteException {
        System.out.print("Informe o ID do novo sensor (ex: portao_garagem): ");
        String sensorId = scanner.nextLine().trim();
        String response = stub.addSensor(sensorId);
        System.out.println(" > Resposta do Servidor: " + response);
    }

    private static void removeSensor(SecuritySystem stub, Scanner scanner) throws RemoteException {
        System.out.print("Informe o ID do sensor a ser removido: ");
        String sensorId = scanner.nextLine().trim();
        String response = stub.removeSensor(sensorId);
        System.out.println(" > Resposta do Servidor: " + response);
    }

    private static void changeStatusSensor(SecuritySystem stub, Scanner scanner) throws RemoteException {
        System.out.print("Informe o ID do sensor para alterar o status: ");
        String sensorId = scanner.nextLine().trim();

        System.out.print("Abrir (S/s) ou Fechar (N/n)? ");
        String choice = scanner.nextLine().trim().toLowerCase();
        boolean status = choice.equals("s"); // True se 's', False se 'n'

        String response = stub.setSensorStatus(sensorId, status);
        System.out.println(" > Resposta do Servidor: " + response);
    }
}
