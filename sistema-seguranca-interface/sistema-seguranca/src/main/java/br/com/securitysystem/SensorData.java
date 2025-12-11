package br.com.securitysystem;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class SensorData {
    private final StringProperty id;
    private final StringProperty status;

    public SensorData(String id, String status) {
        this.id = new SimpleStringProperty(id);
        this.status = new SimpleStringProperty(status);
    }

    public StringProperty idProperty() {
        return id;
    }

    public StringProperty statusProperty() {
        return status;
    }

    public String getId() {
        return id.get();
    }

    public void setId(String newId) {
        this.id.set(newId);
    }

    public String getStatus() {
        return status.get();
    }

    public void setStatus(String newStatus) {
        this.status.set(newStatus);
    }
}
