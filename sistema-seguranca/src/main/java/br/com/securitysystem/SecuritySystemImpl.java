package br.com.securitysystem;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class SecuritySystemImpl extends UnicastRemoteObject implements SecuritySystem {

    private boolean alarmIsActive = false;
    private final List<String> logEvents = new ArrayList<>();

    private final Map<String, String> mockSensors = new ConcurrentHashMap<>();
    private final Map<String, Boolean> alarmIsLocked = new ConcurrentHashMap<>();

    private final Random random = new Random();

    private static final String LOG_FILE = "log_eventos.txt";
    private static final String SENSORS_FILE = "sensores_data.txt";

    public SecuritySystemImpl() throws RemoteException {
        loadData();

        if (mockSensors.isEmpty()) {
            mockSensors.put("porta_principal", "FECHADO");
            alarmIsLocked.put("porta_principal", false);
            mockSensors.put("janela_sala", "FECHADO");
            alarmIsLocked.put("janela_sala", false);
            saveSensorState(); 
        }

        System.out.println("\nServiço de Segurança Predial iniciado.");
    }

    // 1. MÉTODOS DE CONTROLE (ALARMES E SENSORES)

    @Override
    public String setAlarmStatus(boolean status) throws RemoteException {
        this.alarmIsActive = status;
        String mensage = status ? "ATIVADO" : "DESATIVADO";
        registerEvent("\nALARME " + mensage + " via requisição remota.");
        saveSensorState();
        return "\nAlarme principal: " + mensage;
    }

    @Override
    public String setSensorStatus(String sensorId, boolean isOpen) throws RemoteException {
        if (!mockSensors.containsKey(sensorId)) {
            return "\nSensor '" + sensorId + "' não encontrado.";
        }

        String newStatus = isOpen ? "ABERTO" : "FECHADO";

        if (!isOpen && alarmIsLocked.getOrDefault(sensorId, false)) {
            alarmIsLocked.put(sensorId, false);
            registerEvent("\nAlarme trancado do sensor '" + sensorId + "' resetado automaticamente ao FECHAR.");
        }

        mockSensors.put(sensorId, newStatus);

        if (isOpen && alarmIsActive) {
            registerEvent("\nATENÇÃO: Sensor '" + sensorId + "' ABERTO enquanto o alarme está ATIVO!");
        } else {
            registerEvent("\nSensor '" + sensorId + "' alterado para " + newStatus + ".");
        }

        saveSensorState();
        return "\nStatus do sensor '" + sensorId + "' alterado para: " + newStatus;
    }

    @Override
    public String resetSensorAlarm(String sensorId) throws RemoteException {
        if (alarmIsLocked.containsKey(sensorId) && alarmIsLocked.get(sensorId)) {
            alarmIsLocked.put(sensorId, false);
            mockSensors.put(sensorId, "FECHADO");
            registerEvent("\nAlarme do sensor '" + sensorId + "' resetado manualmente.");
            saveSensorState();
            return "\nAlarme do sensor " + sensorId + " resetado com sucesso.";
        }
        return "\nNenhum alarme trancado para o sensor " + sensorId + ".";
    }

    // 2. MÉTODOS DE CONSULTA (STATUS E LOG)

    @Override
    public String getSensorStatus(String sensorId) throws RemoteException {
        if (alarmIsActive && alarmIsLocked.getOrDefault(sensorId, false)) {
            return "ALERTA_TRANCADO";
        }
        return mockSensors.getOrDefault(sensorId, "SENSOR_NAO_ENCONTRADO");
    }

    @Override
    public Map<String, String> getAllSensorStatus() throws RemoteException {
        Map<String, String> currentStatus = new HashMap<>(mockSensors);

        for (String id : currentStatus.keySet()) {
            if (alarmIsActive && alarmIsLocked.getOrDefault(id, false)) {
                currentStatus.put(id, "ALERTA_TRANCADO");
            }
        }
        return currentStatus;
    }

    @Override
    public List<String> getLogEvents(int limit) throws RemoteException {
        int endIndex = Math.min(limit, logEvents.size());

        if (endIndex == 0) {
            return new ArrayList<>();
        }

        List<String> sublist = this.logEvents.subList(0, endIndex);
        return new ArrayList<>(sublist); 
    }

    @Override
    public void registerEvent(String event) throws RemoteException {
        String logEntry = LocalDateTime.now().toString() + " - " + event;
        this.logEvents.add(0, logEntry);
        System.out.println("\n[LOG DO SERVIDOR] " + logEntry);
        saveLog();
    }

    // 3. MÉTODOS DE GERENCIAMENTO (CRUD de Sensores)

    @Override
    public String addSensor(String sensorId) throws RemoteException {
        if (mockSensors.containsKey(sensorId)) {
            return "\nSensor '" + sensorId + "' já existe no sistema.";
        }
        mockSensors.put(sensorId, "FECHADO");
        alarmIsLocked.put(sensorId, false);
        registerEvent("\nNOVO SENSOR ADICIONADO: " + sensorId);
        saveSensorState();
        return "\nSensor '" + sensorId + "' adicionado com sucesso com status FECHADO.";
    }

    @Override
    public String removeSensor(String sensorId) throws RemoteException {
        if (!mockSensors.containsKey(sensorId)) {
            return "\nSensor '" + sensorId + "' não encontrado no sistema.";
        }
        mockSensors.remove(sensorId);
        alarmIsLocked.remove(sensorId); 
        registerEvent("\nSENSOR REMOVIDO: " + sensorId);
        saveSensorState();
        return "\nSensor '" + sensorId + "' removido com sucesso.";
    }

    // 4. MÉTODOS DE SIMULAÇÃO (CHAMADO PELO ServerMain)

    public void checkAndTriggerAlarm() throws RemoteException {
        List<String> sensorIds = new ArrayList<>(mockSensors.keySet());

        if (alarmIsActive && !sensorIds.isEmpty() && random.nextInt(10) == 0) {
            
            int randomIndex = random.nextInt(sensorIds.size());
            String sensorId = sensorIds.get(randomIndex);

            if (!alarmIsLocked.getOrDefault(sensorId, false)) {

                mockSensors.put(sensorId, "VIOLADO");
                alarmIsLocked.put(sensorId, true); 

                String event = "\n!!! Sensor '" + sensorId + "' violado. ALARME DISPARADO. !!!";
                registerEvent(event);
                saveSensorState();
            }
        }
    }
    
    // 5. MÉTODOS DE PERSISTÊNCIA

    private void loadData() {
        System.out.println("\n[PERSISTÊNCIA] Tentando carregar dados de sensores e alarme...");
        
        try (BufferedReader reader = new BufferedReader(new FileReader(SENSORS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("alarmIsActive=")) {
                    this.alarmIsActive = Boolean.parseBoolean(line.substring("alarmIsActive=".length()));
                    System.out.println("\n[PERSISTÊNCIA] Status do Alarme Central carregado: " + (alarmIsActive ? "ATIVADO" : "DESATIVADO"));
                } else {
                    String[] parts = line.split(",");
                    if (parts.length == 3) {
                        String id = parts[0].trim();
                        String status = parts[1].trim();
                        boolean isLocked = Boolean.parseBoolean(parts[2].trim());

                        mockSensors.put(id, status);
                        alarmIsLocked.put(id, isLocked);
                        System.out.println("\n[PERSISTÊNCIA] Sensor carregado: " + id + " (" + status + (isLocked ? ", TRANCADO)" : ")"));
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("\n[PERSISTÊNCIA] Arquivo de dados de sensores não encontrado ou vazio. Usando valores padrão.");
        }

        System.out.println("\n[PERSISTÊNCIA] Tentando carregar log de eventos...");
        try (BufferedReader reader = new BufferedReader(new FileReader(LOG_FILE))) {
            String line;
            List<String> tempLog = new ArrayList<>();
            while ((line = reader.readLine()) != null) {
                tempLog.add(line);
            }
            for (int i = tempLog.size() - 1; i >= 0; i--) {
                logEvents.add(tempLog.get(i));
            }
            System.out.println("\n[PERSISTÊNCIA] " + logEvents.size() + " eventos de log carregados.");

        } catch (IOException e) {
            System.out.println("\n[PERSISTÊNCIA] Arquivo de log de eventos não encontrado ou vazio. Iniciando log vazio.");
        }
    }
    
    private void saveSensorState() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(SENSORS_FILE))) {
            writer.write("alarmIsActive=" + this.alarmIsActive);
            writer.newLine();

            for (Map.Entry<String, String> sensorEntry : mockSensors.entrySet()) {
                String id = sensorEntry.getKey();
                String status = sensorEntry.getValue();
                boolean isLocked = alarmIsLocked.getOrDefault(id, false);
                writer.write(String.format("%s,%s,%b", id, status, isLocked));
                writer.newLine();
            }

        } catch (IOException e) {
            System.err.println("\n[PERSISTÊNCIA] Erro ao salvar dados de sensores: " + e.getMessage());
        }
    }
    
    private void saveLog() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_FILE))) {
            for (int i = logEvents.size() - 1; i >= 0; i--) {
                writer.write(logEvents.get(i));
                writer.newLine();
            }

        } catch (IOException e) {
            System.err.println("\n[PERSISTÊNCIA] Erro ao salvar log de eventos: " + e.getMessage());
        }
    }
}
