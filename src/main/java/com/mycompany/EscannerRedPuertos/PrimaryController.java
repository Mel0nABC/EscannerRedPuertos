package com.mycompany.EscannerRedPuertos;


import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;


public class PrimaryController implements Initializable {

    @FXML
    private Label lblProceso;

    public static Label lblProceso_static;

    @FXML
    private TextField fieldIpInicio, fieldIpFinal;
    @FXML
    private TableView<Ipss> resulTable;

    private static ArrayList<Ipss> ipListaCompleta;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        lblProceso_static = lblProceso;
    }

    public void btnBuscar(){
        //Escaneamos la red con los rangos especificados.
        Modelo model = new Modelo();
        model.escanearRed(fieldIpInicio.getText(), fieldIpFinal.getText());

        if (!ipListaCompleta.isEmpty() | ipListaCompleta != null) {

            //Creamos un objeto ObservableList de Ipss, este viene por el método setItemsTable, que nos lo envia la clase Modelo.
            ObservableList<Ipss> observableList = FXCollections.observableArrayList(ipListaCompleta);
            resulTable.setItems(observableList);
            //Se crean las columnas
            TableColumn<Ipss, String> columna0 = new TableColumn<>("ID");
            TableColumn<Ipss, String> columna1 = new TableColumn<>("IPS");
            TableColumn<Ipss, String> columna2 = new TableColumn<>("¿VIVA?");

            //Se especifica el campo de Ipss que se visualizará en cada columna
            columna0.setCellValueFactory(new PropertyValueFactory("id"));
            columna1.setCellValueFactory(new PropertyValueFactory("ip"));
            columna2.setCellValueFactory(new PropertyValueFactory("viva"));

            //Se agregan las columnas al cableView.
            resulTable.getColumns().setAll(columna0, columna1, columna2);

        }

    }

    public static void setAlarmaError(String msg) {
        Alert alerta = new Alert(Alert.AlertType.WARNING);
        alerta.setContentText(msg);
        alerta.showAndWait();
    }

    public static void setItemsTable(ArrayList<Ipss> ipListaCompleta) {
        //Modelo nos envía el array cono de objetos Ipss, que nos indica las Ip's que están vivas.
        PrimaryController.ipListaCompleta = ipListaCompleta;
    }




}