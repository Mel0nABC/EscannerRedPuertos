package com.mycompany.EscannerRedPuertos;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class PrimaryController implements Initializable {

    //Dos variables para lo mismo, así es una forma de poder, desde fuera, cambiar el text de lblProceso
    //Su usa el método static setEstatus()
    @FXML
    private Label lblProceso;
    private static Label lblProceso_static;

    @FXML
    private Button btnBuscar;

    @FXML
    private TextField fieldIpInicio, fieldIpFinal;
    @FXML
    private TableView<Ipss> resulTable;

    //Variable que recibimos por método static desde modelo, es el array de ips vivas.
    private static ArrayList<Ipss> ipListaCompleta;

    private Task tarea;
    private Thread hiloEscaner;
    private Modelo model;

    public void initialize(URL location, ResourceBundle arg1) {
        lblProceso_static = lblProceso;
        fieldIpInicio.setText("192.168.1.1");
        fieldIpFinal.setText("193.168.1.1");
    }

    public void btnBuscar() {
        
        
        
        
        if(btnBuscar.getText().equals("BUSCAR")){
            btnBuscar.setText("STOP");
        }else{
            btnBuscar.setText("BUSCAR");
            
        }
        
        

        if (tarea != null && tarea.isRunning()) {
            tarea.cancel();
            hiloEscaner.interrupt();
            model.stopThreadsPing();
        } else {
            tarea = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    //Escaneamos la red con los rangos especificados.
                    model = new Modelo();
                    model.escanearRed(fieldIpInicio.getText(), fieldIpFinal.getText());
                    if (ipListaCompleta == null) {
//                    lblProceso.setText("ESTATUS:");
                    } else {

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
                    return null;
                }
            };
            hiloEscaner = new Thread(tarea);
            hiloEscaner.setDaemon(true);
            hiloEscaner.setName("escaner");
            hiloEscaner.start();


        }

    }

    public static void setItemsTable(ArrayList<Ipss> ipListaCompleta) {
        //Modelo nos envía el array cono de objetos Ipss, que nos indica las Ip's que están vivas.
        Platform.runLater(() -> {
            PrimaryController.ipListaCompleta = ipListaCompleta;
        });
    }

    public static void setAlarmaError(String msg) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("¡AVISO!");
            alert.setHeaderText(null);
            alert.setContentText(msg);
            alert.showAndWait();
        });
    }

    public static void setEstatus(String estatus) {
        Platform.runLater(() -> {
            lblProceso_static.setText(estatus);
        });
    }

}
