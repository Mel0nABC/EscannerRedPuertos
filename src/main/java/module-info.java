module com.mycompany.EscannerRedPuertos {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.mycompany.EscannerRedPuertos.Vista to javafx.fxml;
    exports com.mycompany.EscannerRedPuertos.Vista;
    
    opens com.mycompany.EscannerRedPuertos.Modelo to javafx.fxml;
    exports com.mycompany.EscannerRedPuertos.Modelo;
    
    opens com.mycompany.EscannerRedPuertos.Controlador to javafx.fxml;
    exports com.mycompany.EscannerRedPuertos.Controlador;
    requires javafx.base;
}
