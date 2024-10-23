package com.project.hundirlaflota.Client;


import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    public static CtrlPagePrincipal ctrlPagePrincp;
    public static CtrlOptions ctrlOptions;
    public static CtrlPlay ctrlPlay;
    public static CtrlWaiting ctrlWait;

    @Override
    public void start(Stage stage) throws Exception {


        // Esto sirve para hacer mas facil el cambio de escena.
        UtilsViews.parentContainer.setStyle("-fx-font: 14 arial;");
        UtilsViews.addView(getClass(), "ViewPagePrincipal", "/PaginaPrincipal.fxml");
        UtilsViews.addView(getClass(), "ViewWait", "/viewWait.fxml");
        UtilsViews.addView(getClass(), "ViewPlay", "/viewPlay.fxml");
        UtilsViews.addView(getClass(), "ViewOptions","/viewOptions.fxml");


        ctrlPagePrincp = (CtrlPagePrincipal) UtilsViews.getController("ViewPagePrincipal");
        ctrlWait = (CtrlWaiting) UtilsViews.getController("ViewWait");
        ctrlPlay = (CtrlPlay) UtilsViews.getController("ViewPlay");
        ctrlOptions = (CtrlOptions) UtilsViews.getController("ViewOptions");

        Scene scene = new Scene(UtilsViews.parentContainer);

        stage.setTitle("Hundir la Flota");
        stage.setWidth(1280);
        stage.setHeight(720);
        stage.setResizable(false);
        stage.setScene(scene);

        stage.show();



    }

    public static void main(String[] args) {
        launch(args);

    }

}