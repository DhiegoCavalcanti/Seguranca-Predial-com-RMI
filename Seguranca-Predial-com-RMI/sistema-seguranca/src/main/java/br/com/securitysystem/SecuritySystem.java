package br.com.securitysystem;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

public interface SecuritySystem extends Remote {

    String setAlarmStatus(boolean status) throws RemoteException;

    boolean isAlarmeAtivado() throws RemoteException; // NOVO MÉTODO PARA O DASHBOARD

    String setSensorStatus(String sensorId, boolean isOpen) throws RemoteException;

    String getSensorStatus(String sensorId) throws RemoteException;

    Map<String, String> getAllSensorStatus() throws RemoteException;

    void registerEvent(String event) throws RemoteException;

    List<String> getLogEvents(int limit) throws RemoteException;

    String resetSensorAlarm(String sensorId) throws RemoteException;

    String addSensor(String sensorId) throws RemoteException;

    String removeSensor(String sensorId) throws RemoteException;
}