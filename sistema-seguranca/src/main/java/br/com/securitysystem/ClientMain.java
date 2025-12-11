package br.com.securitysystem;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ClientMain extends Application {

    private static final String RMI_HOST = "192.168.56.1";

    @Override
    public void start(Stage primaryStage) throws Exception {
        System.setProperty("java.rmi.server.hostname", RMI_HOST);

        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Dashboard.fxml"));

            Scene scene = new Scene(root);

            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

            primaryStage.setTitle("Project Arquitetura - Painel de Controle");

            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception e) {
            System.err
                    .println("Erro ao carregar FXML ou iniciar JavaFX. Verifique a estrutura de arquivos e o pom.xml.");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
