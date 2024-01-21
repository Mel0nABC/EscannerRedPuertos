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
import javafx.scene.control.CheckBox;
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
    private Button btnBuscar, btnBorrar, btnScanPort;
    private static Button btnBuscar_static;

    @FXML
    private TextField fieldIpInicio, fieldIpFinal, fieldIpEscan, fieldPuertos;
    @FXML
    private TableView<Ipss> resulTable;

    @FXML
    private CheckBox mostrarTodoScan;

    //Variable que recibimos por método static desde modelo, es el array de ips vivas.
    private static ArrayList<Ipss> ipListaCompleta;
    //Esta variable es para usarla de filtro si el checkbox está o no marcado.
    private static ArrayList<Ipss> ipListaCompletaFinal;

    private Task tarea;
    private static Thread hiloEscaner;
    private Modelo model;

    public void initialize(URL location, ResourceBundle arg1) {
        lblProceso_static = lblProceso;
        btnBuscar_static = btnBuscar;
        model = new Modelo();
        fieldIpInicio.setText("192.168.1.1");
        fieldIpFinal.setText("192.168.1.255");

//Para seleccionar la ip de la lista de ips escaneadas y colocarla en su tableField, fieldIpEscan
        resulTable.setOnMouseClicked(event -> {
            String ipSeleccionada = resulTable.getSelectionModel().getSelectedItem().getIp();
            fieldIpEscan.setText(ipSeleccionada);
        });

    }
//############### INICIO ESCANER DE RED ###############

    public void btnBuscar() {

        if (btnBuscar.getText().equals("SCAN")) {
            btnBuscar.setText("STOP");
            setDisableEnableBtn();
        } else if (btnBuscar.getText().equals("STOP")) {
            btnBuscar.setText("SCAN");
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
                    model.escanearRed(fieldIpInicio.getText(), fieldIpFinal.getText());
                    if (ipListaCompleta != null) {

                        //Filtramos array de Ipss para ver si usamos todas las ips escaneadas o sólo las vivas antes de iniciar el scan.
                        mostrarTodoONoAntesDeScan();
                        insertarTabla(ipListaCompletaFinal);
                        setDisableEnableBtn();
                    }
                    return null;
                }
            };
            hiloEscaner = new Thread(tarea);
            hiloEscaner.setDaemon(true);
            hiloEscaner.setName("escanerThread");
            hiloEscaner.start();
        }

    }

    public void btnBorrar() {
        ArrayList<Ipss> ipListaVacia = new ArrayList<>();
        insertarTabla(ipListaVacia);
    }

    public void setDisableEnableBtn() {
        if (btnBorrar.isDisable()) {
            mostrarTodoScan.setDisable(false);
            btnBorrar.setDisable(false);
        } else {
            mostrarTodoScan.setDisable(true);
            btnBorrar.setDisable(true);
        }
    }

    public void insertarTabla(ArrayList<Ipss> lista) {
        //Creamos un objeto ObservableList de Ipss, este viene por el método setItemsTable, que nos lo envia la clase Modelo.
        ObservableList<Ipss> observableList = FXCollections.observableArrayList(lista);
        resulTable.setItems(observableList);
        //Se crean las columnas
        TableColumn<Ipss, String> columna0 = new TableColumn<>("ID");
        TableColumn<Ipss, String> columna1 = new TableColumn<>("IPS");
        TableColumn<Ipss, String> columna2 = new TableColumn<>("¿VIVA?");

        //Se especifica el campo de Ipss que se visualizará en cada columna
        columna0.setCellValueFactory(new PropertyValueFactory("id"));
        columna1.setCellValueFactory(new PropertyValueFactory("ip"));
        columna2.setCellValueFactory(new PropertyValueFactory("viva"));
        Platform.runLater(() -> {
            //Se agregan las columnas al cableView.
            resulTable.getColumns().setAll(columna0, columna1, columna2);
        });
    }

    public void mostrarTodoONoAntesDeScan() {
        ipListaCompletaFinal = new ArrayList<>();
        if (mostrarTodoScan.isSelected()) {
            System.out.println("SELECCIONADO");
            ipListaCompletaFinal = ipListaCompleta;
        } else {
            for (Ipss ip : ipListaCompleta) {
                if (ip.getViva()) {
                    ipListaCompletaFinal.add(ip);
                }
            }

            System.out.println("NO SELECCIONADO");
        }
    }

    public void mostrarTodoONoDespuesDeScan() {
        //Filtra las ips que ya tenemos en la lista, mostrando todas o sólo vivas.

        if (ipListaCompleta != null) {
            ipListaCompletaFinal = new ArrayList<>();
            if (mostrarTodoScan.isSelected()) {
                System.out.println("SELECCIONADO");
                ipListaCompletaFinal = ipListaCompleta;
            } else {
                for (Ipss ip : ipListaCompleta) {
                    if (ip.getViva()) {
                        ipListaCompletaFinal.add(ip);
                    }
                }

                System.out.println("NO SELECCIONADO");
            }
            insertarTabla(ipListaCompletaFinal);
        }
    }

    public static void setItemsTable(ArrayList<Ipss> ipListaCompleta) {
        //Modelo nos envía el array cono de objetos Ipss, que nos indica las Ip's que están vivas.
        Platform.runLater(() -> {
            PrimaryController.ipListaCompleta = ipListaCompleta;
            btnBuscar_static.setText("SCAN");
        });
    }

    public static void setAlarmaError(String msg) {
        Platform.runLater(() -> {
            btnBuscar_static.setText("SCAN");
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

//############### FINAL ESCANER DE RED ###############
//############### INICIO ESCANER DE PUERTOS ###############
    public void portScan() {

        String ipEscan = fieldIpEscan.getText();
        String puertos = fieldPuertos.getText();
        
        if (ipEscan.equals("")) {
            PrimaryController.setAlarmaError("Debe seleccionar o introducir una ip manualmente.");
        } else if (puertos.equals("")) {
            PrimaryController.setAlarmaError("No ha especificado puertos a escanear.");
        } else {

            model.portEscaner(ipEscan, puertos);

        }
    }

//############### FINAL ESCANER DE PUERTOS ###############
}
