package com.mycompany.mavenproject1;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class PrimaryController {
    
    @FXML
    private TextField fieldIpInicio;
    
    @FXML
    private TextField fieldIpFinal;

    @FXML
    private Button btnBuscar;
    
    
    private void switchToSecondary() throws IOException {
        App.setRoot("secondary");
    }
    

    public void btnBuscar(){
        System.out.println("IP INICIO: "+fieldIpInicio.getText());
        System.out.println("IP FINAL: "+fieldIpFinal.getText());
    }
}
