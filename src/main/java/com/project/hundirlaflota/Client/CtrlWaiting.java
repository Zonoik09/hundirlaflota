package com.project.hundirlaflota.Client;

import javafx.application.Platform; // Importar Platform
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;

public class CtrlWaiting {
    @FXML
    private Label countdown;
    @FXML
    private Label player1;
    @FXML
    private Label player2;
    static WebSocketClient webSocketClient;

    public void initialize() {
        try {
            // Conectarse al servidor de WebSocket
            webSocketClient = new WebSocketClient(new URI("wss://jruedaneiro.ieti.site")) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    System.out.println("Conexión establecida con el servidor.");
                }

                @Override
                public void onMessage(String message) {
                    JSONObject jsonMessage = new JSONObject(message);

                    if (jsonMessage.has("type")) {
                        String type = jsonMessage.getString("type");

                        switch (type) {
                            case "error":
                                // Mostrar mensaje de error al usuario
                                String errorMessage = jsonMessage.getString("message");
                                System.out.println(errorMessage);
                                // Aquí podrías mostrar el mensaje en una alerta o en un componente UI
                                break;

                            case "clients":
                                // Procesar la lista de clientes
                                System.out.println("Recibida lista de clientes: " + jsonMessage.getJSONArray("list"));
                                break;

                            case "countdown":
                                // Recibir la cuenta regresiva
                                int countdownValue = jsonMessage.getInt("value");
                                // Ejecutar la actualización en el hilo de la aplicación JavaFX
                                Platform.runLater(() -> Main.ctrlWait.updateCountdown(countdownValue));
                                break;

                            case "gameStart":
                                // Manejar el inicio del juego
                                System.out.println(jsonMessage.getString("message"));
                                break;
                        }
                    }
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    System.out.println("Conexión cerrada: " + reason);
                }

                @Override
                public void onError(Exception ex) {
                    ex.printStackTrace();
                }
            };

            webSocketClient.connect();

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    // Para actualizar el nombre del jugador 1
    public void updatePlayer1(String name) {
        Platform.runLater(() -> player1.setText(name)); // Asegurarse de que se ejecute en el hilo de la aplicación
    }

    // Para actualizar el nombre del jugador 2 cuando se conecte
    public void updatePlayer2(String name) {
        Platform.runLater(() -> player2.setText(name)); // Asegurarse de que se ejecute en el hilo de la aplicación
    }

    // Para actualizar el countdown
    public void updateCountdown(int value) {
        Platform.runLater(() -> countdown.setText(String.valueOf(value))); // Asegurarse de que se ejecute en el hilo de la aplicación
    }
}
