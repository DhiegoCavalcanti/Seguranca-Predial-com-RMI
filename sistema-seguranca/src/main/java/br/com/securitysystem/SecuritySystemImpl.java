package br.com.securitysystem;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SecuritySystemImpl extends UnicastRemoteObject implements SecuritySystem {

    private final Map<String, String> sensores;
    private final List<String> logEventos;
    private boolean alarmeAtivado;
    private boolean alarmeTrancado;

    public SecuritySystemImpl() throws RemoteException {
        sensores = new ConcurrentHashMap<>();
        logEventos = new LinkedList<>();
        alarmeAtivado = false;
        alarmeTrancado = false;

        sensores.put("porta_apt1", "FECHADO");
        sensores.put("porta_fundos", "FECHADO");
        sensores.put("porta_principal", "FECHADO");
        logEventos.add(0, "Sistema iniciado com 4 sensores.");
    }

    private void logEvent(String event) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String logEntry = "[" + timestamp + "] " + event;
        logEventos.add(0, logEntry);
        System.out.println("[SERVER LOG] " + logEntry);
    }

    // MÉTODOS RMI

    @Override
    public boolean isAlarmActive() throws RemoteException {
        return alarmeAtivado || alarmeTrancado;
    }

    @Override
    public String setAlarmStatus(boolean status) throws RemoteException {
        if (status) {
            if (alarmeTrancado) {
                logEvent("Tentativa de ativar: Alarme está trancado por violação. Reset necessário.");
                return "Alarme está trancado. Use a função Resetar Alarme (Sensor).";
            }
            if (!alarmeAtivado) {
                alarmeAtivado = true;
                logEvent("Alarme ATIVADO.");
                return "Alarme ATIVADO com sucesso.";
            } else {
                return "Alarme já estava ATIVADO.";
            }
        } else {
            if (alarmeTrancado) {
                logEvent("Tentativa de desativar: Alarme está trancado por violação. Reset necessário.");
                return "Alarme está trancado. Use a função Resetar Alarme (Sensor).";
            }
            if (alarmeAtivado) {
                alarmeAtivado = false;
                logEvent("Alarme DESATIVADO.");
                return "Alarme DESATIVADO com sucesso.";
            } else {
                return "Alarme já estava DESATIVADO.";
            }
        }
    }

    @Override
    public String setSensorStatus(String sensorId, boolean open) throws RemoteException {
        if (!sensores.containsKey(sensorId)) {
            return "Sensor ID " + sensorId + " não encontrado.";
        }

        String currentStatus = sensores.get(sensorId);

        if (open) {
            if (alarmeAtivado) {
                if (!currentStatus.equals("VIOLADO") && !currentStatus.equals("ALERTA_TRANCADO")) {
                    sensores.put(sensorId, "ALERTA_TRANCADO");
                    alarmeTrancado = true;
                    logEvent("!!! VIOLAÇÃO CRÍTICA DETECTADA !!! Sensor: " + sensorId + " - ALARME DISPARADO!");
                    return "ALARME DISPARADO! Sensor " + sensorId + " violado e sistema trancado.";
                } else {
                    return "Sensor já estava em ALERTA_TRANCADO.";
                }
            } else {
                sensores.put(sensorId, "ABERTO");
                logEvent("Sensor " + sensorId + " aberto com alarme desativado.");
                return "Sensor " + sensorId + " aberto (alarme desativado).";
            }
        } else {
            if (currentStatus.equals("ALERTA_TRANCADO")) {
                logEvent("Tentativa de fechar sensor ALERTA_TRANCADO: " + sensorId + ". Necessário Reset.");
                return "Sensor " + sensorId + " trancado. Use 'Resetar Alarme (Sensor)'.";
            } else {
                sensores.put(sensorId, "FECHADO");
                logEvent("Sensor " + sensorId + " fechado/protegido.");
                return "Sensor " + sensorId + " fechado/protegido.";
            }
        }
    }

    @Override
    public String resetSensorAlarm(String sensorId) throws RemoteException {
        if (!sensores.containsKey(sensorId)) {
            return "Sensor ID " + sensorId + " não encontrado.";
        }

        String currentStatus = sensores.get(sensorId);
        if (currentStatus.equals("ALERTA_TRANCADO") || currentStatus.equals("VIOLADO")) {
            sensores.put(sensorId, "FECHADO");

            boolean anotherViolation = sensores.containsValue("ALERTA_TRANCADO");
            if (!anotherViolation) {
                alarmeTrancado = false;
                logEvent("Todos os sensores em alerta foram resetados. Sistema geral destrancado.");
            }

            logEvent("Alarme do sensor " + sensorId + " resetado para FECHADO.");
            return "Alarme do sensor " + sensorId
                    + " resetado para FECHADO. Sistema destrancado se for o último alerta.";
        } else {
            return "O sensor " + sensorId + " não estava em estado de alerta trancado.";
        }
    }

    @Override
    public void registerEvent(String event) throws RemoteException {
        logEvent("Evento externo registrado: " + event);
    }

    @Override
    public String getSensorStatus(String sensorId) throws RemoteException {
        return sensores.getOrDefault(sensorId, "Sensor não encontrado");
    }

    @Override
    public Map<String, String> getAllSensorStatus() throws RemoteException {
        return new HashMap<>(sensores);
    }

    @Override
    public String addSensor(String sensorId) throws RemoteException {
        if (sensores.containsKey(sensorId)) {
            return "Erro: Sensor ID " + sensorId + " já existe.";
        }
        sensores.put(sensorId, "FECHADO");
        logEvent("Sensor " + sensorId + " adicionado ao sistema.");
        return "Sensor " + sensorId + " adicionado com sucesso.";
    }

    @Override
    public String removeSensor(String sensorId) throws RemoteException {
        if (!sensores.containsKey(sensorId)) {
            return "Erro: Sensor ID " + sensorId + " não encontrado.";
        }
        sensores.remove(sensorId);
        logEvent("Sensor " + sensorId + " removido do sistema.");
        return "Sensor " + sensorId + " removido com sucesso.";
    }

    @Override
    public List<String> getLogEvents(int maxEntries) throws RemoteException {
        int count = Math.min(maxEntries, logEventos.size());
        return new LinkedList<>(logEventos.subList(0, count));
    }
}
