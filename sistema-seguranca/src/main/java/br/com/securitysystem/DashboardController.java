package br.com.securitysystem;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.Alert; 
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import java.net.URL;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.RemoteException;
import java.util.ResourceBundle;
import java.util.Map;
import java.util.List;
import javafx.application.Platform; 
import javafx.concurrent.Task; // Import necessário

public class DashboardController implements Initializable {

    // Cores Hexadecimais
    private static final String PRIMARY_LIGHT = "#34495e"; 
    private static final String STATUS_ACTIVE = "#27ae60"; 
    private static final String STATUS_INACTIVE = "#e74c3c"; 

    // Referências FXML
    @FXML private Label statusGeralLabel;
    @FXML private Label totalSensoresLabel;
    @FXML private Label eventosRecentesLabel; 
    @FXML private Button toggleButton; 
    
    @FXML private TableView<SensorData> sensorTableView;
    @FXML private TableColumn<SensorData, String> idColumn;
    @FXML private TableColumn<SensorData, String> statusColumn;

    @FXML private Button openSensorButton;
    @FXML private Button closeSensorButton;
    
    private final ObservableList<SensorData> sensorDataList = FXCollections.observableArrayList();

    private SecuritySystem stub = null; 
    private boolean isAlertActive = false;
    private String selectedSensorId = null; 
    
    // IP do Servidor RMI (Seu IP local) - CRÍTICO
    private static final String RMI_HOST = "192.168.56.1"; 

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // 0. Desabilitar botões
        if (openSensorButton != null) openSensorButton.setDisable(true);
        if (closeSensorButton != null) closeSensorButton.setDisable(true);
        
        // 1. Inicializa a tabela
        idColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty());
        statusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
        sensorTableView.setItems(sensorDataList);

        // Listener (Preservação de seleção)
        sensorTableView.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    selectedSensorId = newSelection.getId();
                    if (openSensorButton != null) openSensorButton.setDisable(false);
                    if (closeSensorButton != null) closeSensorButton.setDisable(false);
                } else {
                    selectedSensorId = null;
                    if (openSensorButton != null) openSensorButton.setDisable(true);
                    if (closeSensorButton != null) closeSensorButton.setDisable(true);
                }
            }
        );
        
        // 2. Conexão RMI Assíncrona via Task (Resolve o congelamento e o erro inicial)
        setupRMIConnectionTask();
        
        // 3. Inicia o monitoramento periódico (Timeline)
        startAutoUpdate(); 
    }
    
    /**
     * Usa Task para conectar o RMI em uma thread separada, liberando a UI.
     */
    private void setupRMIConnectionTask() {
        Task<SecuritySystem> connectionTask = new Task<>() {
            @Override
            protected SecuritySystem call() throws Exception {
                Registry registry = LocateRegistry.getRegistry(RMI_HOST, 1099);
                return (SecuritySystem) registry.lookup("SecuritySystemService");
            }
        };

        connectionTask.setOnSucceeded(event -> {
            stub = connectionTask.getValue();
            System.out.println("[CLIENTE FX] Conexão RMI estabelecida via Task.");
            
            Platform.runLater(() -> {
                updateDashboardStatus();
                updateTotalAndLogCount();
                updateSensorTable(); 
            });
        });

        connectionTask.setOnFailed(event -> {
            Platform.runLater(() -> {
                statusGeralLabel.setText("ERRO DE CONEXÃO");
                totalSensoresLabel.setText("?");
                eventosRecentesLabel.setText("?");
                showAlert(Alert.AlertType.ERROR, "Erro Crítico", "Falha ao conectar ao Servidor RMI (" + RMI_HOST + "). Verifique se o ServerMain está ativo.");
            });
            System.err.println("[CLIENTE FX] Falha na conexão RMI inicial via Task: " + event.getSource().getException().getMessage());
        });

        new Thread(connectionTask).start();
    }


    /**
     * Inicia um Timeline para atualizar o Dashboard a cada 2 segundos.
     */
    private void startAutoUpdate() {
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.seconds(2), event -> {
                if (stub != null) { 
                    Platform.runLater(() -> {
                        updateDashboardStatus();
                        updateTotalAndLogCount();
                        updateSensorTable(); 
                    });
                }
            })
        );
        timeline.setCycleCount(Timeline.INDEFINITE); 
        timeline.play();
    }
    
    // --- MÉTODOS DE ATUALIZAÇÃO E UTILITÁRIOS ---

    private void updateSensorTable() {
        if (stub == null) return;
        try {
            Map<String, String> allStatus = stub.getAllSensorStatus();
            String idToRestore = selectedSensorId; 
            ObservableList<SensorData> updatedList = FXCollections.observableArrayList();
            SensorData itemToSelect = null; 

            for (Map.Entry<String, String> entry : allStatus.entrySet()) {
                SensorData currentSensorData = new SensorData(entry.getKey(), entry.getValue());
                updatedList.add(currentSensorData);

                if (idToRestore != null && idToRestore.equals(entry.getKey())) {
                    itemToSelect = currentSensorData;
                }
            }
            sensorDataList.clear();
            sensorDataList.addAll(updatedList);
            
            if (itemToSelect != null) {
                sensorTableView.getSelectionModel().select(itemToSelect);
            } else {
                selectedSensorId = null;
                if (openSensorButton != null) openSensorButton.setDisable(true);
                if (closeSensorButton != null) closeSensorButton.setDisable(true);
            }
            sensorTableView.refresh(); 
        } catch (RemoteException e) {
            statusGeralLabel.setText("ERRO DE RMI");
            totalSensoresLabel.setText("?");
            eventosRecentesLabel.setText("?");
        }
    }


    private void updateDashboardStatus() {
         if (stub == null) return;
         try {
             Map<String, String> allStatus = stub.getAllSensorStatus(); 
             boolean isActive = stub.isAlarmeAtivado(); 
             
             boolean isViolation = false;
             for (String status : allStatus.values()) {
                 if (status.equals("VIOLADO") || status.equals("ALERTA_TRANCADO")) {
                     isViolation = true;
                     break; 
                 }
             }
             
             statusGeralLabel.getStyleClass().removeAll("status-active", "status-inactive");
             toggleButton.getStyleClass().removeAll("toggle-active", "toggle-inactive"); 

             if (isViolation) { 
                 statusGeralLabel.setText("!!! VIOLAÇÃO DETECTADA !!!");
                 statusGeralLabel.getStyleClass().add("status-inactive");
                 toggleButton.setText("Desativar"); 
                 toggleButton.setStyle("-fx-background-color: " + PRIMARY_LIGHT + ";");
                 
                 if (!isAlertActive) {
                     showAlert(Alert.AlertType.ERROR, "ALARME DISPARADO", "Um ou mais sensores foram violados! O alarme foi trancado. Use a função 'Resetar Alarme (Sensor)' para limpar o estado.");
                     isAlertActive = true;
                 }
                 
             } else if (isActive) {
                 statusGeralLabel.setText("ATIVO");
                 statusGeralLabel.getStyleClass().add("status-active");
                 toggleButton.setText("Desativar"); 
                 toggleButton.setStyle("-fx-background-color: " + STATUS_INACTIVE + ";");
                 isAlertActive = false;
                 
             } else {
                 statusGeralLabel.setText("DESATIVADO");
                 statusGeralLabel.getStyleClass().add("status-inactive");
                 toggleButton.setText("Ativar"); 
                 toggleButton.setStyle("-fx-background-color: " + STATUS_ACTIVE + ";");
                 isAlertActive = false;
             }
             
         } catch (RemoteException e) {
             statusGeralLabel.setText("ERRO RMI");
             toggleButton.setText("Erro");
         }
    }

    private void updateTotalAndLogCount() {
         if (stub == null) return;
         try {
             int totalSensores = stub.getAllSensorStatus().size();
             totalSensoresLabel.setText(String.valueOf(totalSensores));
             
             List<String> logs = stub.getLogEvents(9999); 
             eventosRecentesLabel.setText(String.valueOf(logs.size()));

         } catch (RemoteException e) {
             totalSensoresLabel.setText("?");
             eventosRecentesLabel.setText("?");
         }
    }
    
    // --- MÉTODOS DE AÇÃO (onAction) (Lógica de Botões) ---

    @FXML
    private void handleToggleAlarm() {
        if (stub == null) return;
        try {
            boolean isCurrentlyActive = stub.isAlarmeAtivado(); 
            String result = stub.setAlarmStatus(!isCurrentlyActive);
            showAlert(Alert.AlertType.INFORMATION, "Status do Alarme", result.trim());
            updateDashboardStatus();
            updateSensorTable(); 
        } catch (RemoteException e) {
            showAlert(Alert.AlertType.ERROR, "Erro RMI", "Falha na comunicação: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleViewEvents() {
        if (stub == null) return;
        try {
            List<String> logs = stub.getLogEvents(20); 
            StringBuilder logText = new StringBuilder("--- Últimos 20 Eventos ---\n");
            if (logs.isEmpty()) {
                logText.append("Log de eventos vazio.");
            } else {
                for (String item : logs) {
                    logText.append(item.trim()).append("\n");
                }
            }
            showAlert(Alert.AlertType.INFORMATION, "Log de Eventos", logText.toString());
        } catch (RemoteException e) {
            showAlert(Alert.AlertType.ERROR, "Erro RMI", "Falha ao carregar logs: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleConsultSpecificStatus() {
        if (stub == null) return;
        String sensorId = selectedSensorId;
        if (sensorId == null || sensorId.isEmpty()) {
             sensorId = showInputDialog("Consulta de Status", "ID do Sensor:", "Nenhum sensor selecionado. Digite o ID manualmente:");
        }
        if (sensorId == null || sensorId.isEmpty()) return;
        try {
            String status = stub.getSensorStatus(sensorId.trim());
            showAlert(Alert.AlertType.INFORMATION, "Status do Sensor", "Sensor: " + sensorId + "\nStatus Atual: " + status);
        } catch (RemoteException e) {
            showAlert(Alert.AlertType.ERROR, "Erro RMI", "Falha ao consultar sensor: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleAddSensor() {
        if (stub == null) return;
        String sensorId = showInputDialog("Adicionar Sensor", "Novo ID do Sensor:", "Informe um ID único (ex: porta_fundos):");
        if (sensorId == null || sensorId.isEmpty()) return;
        try {
            String result = stub.addSensor(sensorId.trim());
            showAlert(Alert.AlertType.INFORMATION, "Adição de Sensor", result.trim());
            updateTotalAndLogCount(); 
            updateSensorTable(); 
        } catch (RemoteException e) {
            showAlert(Alert.AlertType.ERROR, "Erro RMI", "Falha ao adicionar sensor: " + e.getMessage());
        }
    }

    @FXML
    private void handleRemoveSensor() {
        if (stub == null) return;
        String sensorId = selectedSensorId;
        if (sensorId == null || sensorId.isEmpty()) {
            sensorId = showInputDialog("Remover Sensor", "ID do Sensor a Remover:", "Nenhum sensor selecionado. Digite o ID manualmente:");
        }
        if (sensorId == null || sensorId.isEmpty()) return;
        try {
            String result = stub.removeSensor(sensorId.trim());
            showAlert(Alert.AlertType.INFORMATION, "Remoção de Sensor", result.trim());
            updateTotalAndLogCount(); 
            updateSensorTable(); 
        } catch (RemoteException e) {
            showAlert(Alert.AlertType.ERROR, "Erro RMI", "Falha ao remover sensor: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleResetAlarm() {
        if (stub == null) return;
        String sensorId = selectedSensorId;
        if (sensorId == null || sensorId.isEmpty()) {
             sensorId = showInputDialog("Resetar Alarme", "ID do Sensor:", "Nenhum sensor selecionado. Digite o ID manualmente:");
        }
        if (sensorId == null || sensorId.isEmpty()) return;
        try {
            String result = stub.resetSensorAlarm(sensorId.trim());
            showAlert(Alert.AlertType.INFORMATION, "Reset de Alarme", result.trim());
            updateSensorTable(); 
            updateDashboardStatus();
        } catch (RemoteException e) {
            showAlert(Alert.AlertType.ERROR, "Erro RMI", "Falha ao resetar alarme: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleOpenSelectedSensor() {
        if (stub == null) return;
        if (selectedSensorId == null || selectedSensorId.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Atenção", "Por favor, selecione um sensor na tabela primeiro.");
            return;
        }
        try {
            String result = stub.setSensorStatus(selectedSensorId.trim(), true); 
            showAlert(Alert.AlertType.INFORMATION, "Status Alterado", result.trim());
            updateSensorTable(); 
            updateDashboardStatus(); 
        } catch (RemoteException e) {
            showAlert(Alert.AlertType.ERROR, "Erro RMI", "Falha ao mudar status: " + e.getMessage());
        }
    }

    @FXML
    private void handleCloseSelectedSensor() {
        if (stub == null) return;
        if (selectedSensorId == null || selectedSensorId.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Atenção", "Por favor, selecione um sensor na tabela primeiro.");
            return;
        }
        try {
            String result = stub.setSensorStatus(selectedSensorId.trim(), false); 
            showAlert(Alert.AlertType.INFORMATION, "Status Alterado", result.trim());
            updateSensorTable();
            updateDashboardStatus();
        } catch (RemoteException e) {
            showAlert(Alert.AlertType.ERROR, "Erro RMI", "Falha ao mudar status: " + e.getMessage());
        }
    }
    
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    private String showInputDialog(String title, String header, String content) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.setContentText(content);
        return dialog.showAndWait().orElse(null);
    }
}