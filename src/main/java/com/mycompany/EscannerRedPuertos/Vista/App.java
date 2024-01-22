package com.mycompany.EscannerRedPuertos.Vista;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.stage.*;
import java.io.IOException;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.ProgressBar;

/**
 * JavaFX App
 */
public class App extends Application {

    //Declaramos variables
    private static Scene scene;
    //Variables para el tamaño de pantalla inicial.
    private static double height;
    private static double width;

    static ProgressBar bar;
    static Stage stage2;

    @Override
    public void start(Stage stage) throws IOException {
        //Creamos objeto para capturar propiedades de la pantalla
        Screen screen = Screen.getPrimary();
        //Ajustamos a polígono tipo rectángulo
        Rectangle2D properties = screen.getBounds();

        //Obtenemos los valores de la pantalla
        height = properties.getHeight() * 0.7;
        width = properties.getWidth() * 0.7;

        // System.out.println("Height: "+height);
        // System.out.println("Width: "+width);
        //Declaramos la scene con el archivo fxml y el tamaño inicial
        scene = new Scene(loadFXML("primary"));
        //Seteamos la scena en la pantalla
        stage.setScene(scene);
        //Mostramos la pantalla
        stage.show();

    }

    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }



}
