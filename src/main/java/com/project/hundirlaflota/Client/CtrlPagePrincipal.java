package com.project.hundirlaflota.Client;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import jdk.jshell.execution.Util;

public class CtrlPagePrincipal {
    public Button Button;

    public void changeView(ActionEvent actionEvent) {
        UtilsViews.setView("ViewOptions");
    }
}
