package com.mycompany.mavenproject1;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.stage.*;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;


/**
 * JavaFX App
 */
public class App extends Application {

    //Declaramos variables
    private static Scene scene;
    //Variables para el tamaño de pantalla inicial.
    private static double height;
    private static double width;

    
    @Override
    public void start(Stage stage) throws IOException {

        //Creamos objeto para capturar propiedades de la pantalla
        Screen screen = Screen.getPrimary();
        //Ajustamos a polígono tipo rectángulo
        Rectangle2D properties = screen.getBounds();
        
        //Obtenemos los valores de la pantalla
        height = properties.getHeight()*0.7;
        width = properties.getWidth()*0.7;
        
        System.out.println("Height: "+height);
        System.out.println("Width: "+width);
        
        //Declaramos la scene con el archivo fxml y el tamaño inicial
        scene = new Scene(loadFXML("primary"),width,height);
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