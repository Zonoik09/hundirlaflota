package com.project.hundirlaflota.Client;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import org.java_websocket.client.WebSocketClient;

import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;

import static com.project.hundirlaflota.Client.CtrlWaiting.webSocketClient;

public class CtrlPagePrincipal {
    @FXML
    private TextField username;
    @FXML
    private Button search;

    public void changeView(ActionEvent actionEvent) {
        UtilsViews.setView("ViewOptions");
    }

    @FXML
    public void searchAction(ActionEvent event) {
        String name = username.getText();
        if (!name.isEmpty()) {
            // Enviar el nombre de usuario al servidor
            JSONObject message = new JSONObject();
            message.put("type", "setName");
            message.put("name", name);
            webSocketClient.send(message.toString());

            // Cambiar la vista a la pantalla de espera
            UtilsViews.setView("ViewWait");

            // Actualizar la pantalla de espera con el nombre del jugador
            Main.ctrlWait.updatePlayer1(name);
        }
    }
}
