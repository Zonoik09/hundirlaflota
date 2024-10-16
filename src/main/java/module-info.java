module com.project.hundirlaflota {
    requires javafx.controls;
    requires javafx.fxml;
    requires Java.WebSocket;
    requires org.jline;
    requires org.json;

    opens com.project.hundirlaflota to javafx.fxml;
    exports com.project.hundirlaflota;
}