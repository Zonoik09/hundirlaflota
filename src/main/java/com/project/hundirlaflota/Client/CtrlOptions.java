package com.project.hundirlaflota.Client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;


public class CtrlOptions {
    @FXML
    private TextField protocol;
    @FXML
    private TextField serverIP;
    @FXML
    private TextField port;

    @FXML
    private void setConfigProxmox(String txtProtocol,String txtServerip, String txtPort) {
        protocol.setText(txtProtocol);
        serverIP.setText(txtServerip);
        port.setText(txtPort);
        System.out.println("Ha cambiado la config");
    }

    @FXML
    private void getConfigProxmox() {
        String txtProtocol = protocol.getText();
        String txtServerip = serverIP.getText();
        String txtPort = port.getText();
        setConfigProxmox(txtProtocol,txtServerip,txtPort);
    }

    
    public void changeView(ActionEvent actionEvent) {
        getConfigProxmox();
        UtilsViews.setView("ViewPagePrincipal");
    }
}
