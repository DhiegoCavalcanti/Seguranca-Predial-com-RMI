package br.com.securitysystem;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

/**
 * Interface remota para o Sistema de Segurança Predial.
 * Define todos os métodos que podem ser chamados remotamente pelo Cliente RMI.
 */
public interface SecuritySystem extends Remote {

    /**
     * Ativa ou desativa o alarme principal do sistema.
     * @param status Verdadeiro para ATIVAR, Falso para DESATIVAR.
     * @return Uma mensagem de confirmação do novo status.
     * @throws RemoteException
     */
    String setAlarmStatus(boolean status) throws RemoteException;

    /**
     * Define o status (Aberto/Fechado) de um sensor específico.
     * @param sensorId O identificador do sensor.
     * @param isOpen True para ABERTO, False para FECHADO.
     * @return Uma mensagem de confirmação do novo status.
     * @throws RemoteException
     */
    String setSensorStatus(String sensorId, boolean isOpen) throws RemoteException;

    /**
     * Lê o status atual de um sensor específico (ex: "porta_principal",
     * "janela_sala").
     * @param sensorId O identificador do sensor.
     * @return O status do sensor (ex: "FECHADO", "ABERTO", "ALERTA_TRANCADO").
     * @throws RemoteException
     */
    String getSensorStatus(String sensorId) throws RemoteException;

    /**
     * Lê o status atual de todos os sensores monitorados pelo sistema.
     * @return Um mapa (Map) onde a chave é o ID do sensor (String) e o valor é o
     * status (String).
     * @throws RemoteException
     */
    Map<String, String> getAllSensorStatus() throws RemoteException;

    /**
     * Registra um novo evento no log do sistema.
     * @param event A descrição do evento a ser logado.
     * @throws RemoteException
     */
    void registerEvent(String event) throws RemoteException;

    /**
     * Retorna os N eventos mais recentes do log do sistema.
     * @param limit O número máximo de eventos a serem retornados.
     * @return Uma lista de strings representando os eventos do log.
     * @throws RemoteException
     */
    List<String> getLogEvents(int limit) throws RemoteException;

    /**
     * Reseta manualmente o estado de alerta persistente (trancado) de um sensor.
     * @param sensorId O identificador do sensor cujo alarme deve ser resetado.
     * @return Uma mensagem de confirmação ou erro.
     * @throws RemoteException
     */
    String resetSensorAlarm(String sensorId) throws RemoteException;

    /**
     * Adiciona um novo sensor ao sistema, inicializando-o com status "FECHADO".
     * @param sensorId O identificador único do novo sensor.
     * @return Uma mensagem de confirmação.
     * @throws RemoteException
     */
    String addSensor(String sensorId) throws RemoteException;

    /**
     * Remove um sensor existente do sistema.
     * @param sensorId O identificador do sensor a ser removido.
     * @return Uma mensagem de confirmação.
     * @throws RemoteException
     */
    String removeSensor(String sensorId) throws RemoteException;
}
