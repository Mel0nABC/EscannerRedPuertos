module com.mycompany.mavenproject1 {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.mycompany.EscannerRedPuertos to javafx.fxml;
    exports com.mycompany.EscannerRedPuertos;
}
