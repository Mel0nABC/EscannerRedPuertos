package com.mycompany.EscannerRedPuertos;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.EventDispatcher;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class PrimaryController implements Initializable {

    //Dos variables para lo mismo, así es una forma de poder, desde fuera, cambiar el text de lblProceso
    //Su usa el método static setEstatus()
    @FXML
    private Label lblProceso;
    private static Label lblProceso_static;

    @FXML
    private Button btnBuscar, btnBorrar, btnScanPort, btnGuardar;
    private static Button btnBuscar_static, btnBorrar_static, btnScanPort_static;

    @FXML
    private TextField fieldIpInicio, fieldIpFinal, fieldIpEscan, fieldPuertos;
    private static TextField fieldIpInicio_static, fieldIpFinal_static, fieldIpEscan_static, fieldPuertos_static;

    @FXML
    private TableView<Ipss> resulTable;
    private static TableView<Ipss> resulTable_static;
    private ObservableList<Ipss> observableList;
    private static ObservableList<Ipss> observableList_static;

    @FXML
    private CheckBox mostrarTodoScan;
    private static CheckBox mostrarTodoScan_static;

    @FXML
    private TextArea instrucciones;

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
        btnBorrar_static = btnBorrar;
        btnScanPort_static = btnScanPort;

        fieldIpInicio_static = fieldIpInicio;
        fieldIpFinal_static = fieldIpFinal;
        fieldIpEscan_static = fieldIpEscan;
        fieldPuertos_static = fieldPuertos;

        mostrarTodoScan_static = mostrarTodoScan;

        resulTable_static = resulTable;
        observableList_static = observableList;

        ipListaCompleta = new ArrayList<>();
        ipListaCompletaFinal = new ArrayList<>();
        model = new Modelo();

        String textoInformativo = "Seleccione una IP de la lista o escriba una manualmente.\n"
                + "\n"
                + "¿Cómo indicar los puertos a escanear?\n"
                + "\n"
                + "Ejemplo individual: 2000\n"
                + "Ejemplo concretos: 2000,2001,8080 ...\n"
                + "Ejemplo rango: 2000-2010";
        instrucciones.setText(textoInformativo);

        
        //Zona para hacer pruebas rápidas.
//        fieldIpInicio.setText("192.168.1.1");
//        fieldIpFinal.setText("192.168.1.110");
//        fieldPuertos.setText("1-80");
//        fieldIpEscan.setText("192.168.1.1");

//Para seleccionar la ip de la lista de ips escaneadas y colocarla en su tableField, fieldIpEscan
        resulTable.setOnMouseClicked(event -> {
            String ipSeleccionada = resulTable.getSelectionModel().getSelectedItem().getIp();
            if (ipSeleccionada != null) {
                fieldIpEscan.setText(ipSeleccionada);
            }
        });
    }
//############### INICIO ESCANER DE RED ###############

    public void btnBuscar() {
        ipListaCompleta = new ArrayList<>();
        ipListaCompletaFinal = new ArrayList<>();

        if (btnBuscar.getText().equals("SCAN")) {
            btnBuscar.setText("STOP");
            PrimaryController.setDisableEnableBtn();
        } else {
            btnBuscar.setText("SCAN");
            PrimaryController.setDisableEnableBtn();
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
        //Borra el tableview y resetear los arrays.
        ArrayList<Ipss> ipListaVacia = new ArrayList<>();
        insertarTabla(ipListaVacia);
        ipListaCompleta = new ArrayList<>();
        ipListaCompletaFinal = new ArrayList<>();
        model = new Modelo();
    }

    public void btnGuardar() {
        //Guarda la información que muestra el tableview en un archivo .log (de texto).
        model.guardarEscaneoLocal(ipListaCompleta);
    }

    public static void setDisableEnableBtn() {
//Deshabilitamos botones, fieldtext a la hora de escanear ips
        if (btnBorrar_static.isDisabled()) {
            fieldIpInicio_static.setDisable(false);
            fieldIpFinal_static.setDisable(false);
            fieldIpEscan_static.setDisable(false);
            fieldPuertos_static.setDisable(false);
            btnBorrar_static.setDisable(false);
            btnScanPort_static.setDisable(false);
            mostrarTodoScan_static.setDisable(false);
        } else {
            fieldIpInicio_static.setDisable(true);
            fieldIpFinal_static.setDisable(true);
            fieldIpEscan_static.setDisable(true);
            fieldPuertos_static.setDisable(true);
            btnBorrar_static.setDisable(true);
            btnScanPort_static.setDisable(true);
            mostrarTodoScan_static.setDisable(true);
        }
    }

    public static void insertarTabla(ArrayList<Ipss> lista) {
//Recibe un array de Ipss y lo muestra en el tableview.
        Platform.runLater(() -> {
            //Creamos un objeto ObservableList de Ipss, este viene por el método setItemsTable, que nos lo envia la clase Modelo.
            observableList_static = FXCollections.observableArrayList(lista);
            resulTable_static.setItems(observableList_static);
            //Se crean las columnas
            TableColumn<Ipss, String> columna0 = new TableColumn<>("ID");
            TableColumn<Ipss, String> columna1 = new TableColumn<>("IPS");
            TableColumn<Ipss, String> columna2 = new TableColumn<>("¿VIVA?");
            TableColumn<Ipss, Puerto> columna3 = new TableColumn<>("PUERTOS");

            //Se especifica el campo de Ipss que se visualizará en cada columna
            columna0.setCellValueFactory(new PropertyValueFactory("id"));
            columna1.setCellValueFactory(new PropertyValueFactory("ip"));
            columna2.setCellValueFactory(new PropertyValueFactory("viva"));
            columna3.setCellValueFactory(new PropertyValueFactory("puertos"));

            //Se agregan las columnas al cableView.
            resulTable_static.getColumns().setAll(columna0, columna1, columna2, columna3);
        });

        //Validacion cuando se ejecuta btnBorrar, envia una lista vacia para hacer el reset de la taabla.
        if (!lista.isEmpty()) {
            //Por si se pulsa stop scan, para que cuando pare, los botones esten activos.
            PrimaryController.setDisableEnableBtn();
        }

    }

    public void mostrarTodoONoAntesDeScan() {
        ipListaCompletaFinal = new ArrayList<>();
        if (mostrarTodoScan.isSelected()) {
            ipListaCompletaFinal = ipListaCompleta;
        } else {
            for (Ipss ip : ipListaCompleta) {
                if (ip.getViva()) {
                    ipListaCompletaFinal.add(ip);
                }
            }
        }
    }

    public void mostrarTodoONoDespuesDeScan() {
        //Filtra las ips que ya tenemos en la lista, mostrando todas o sólo vivas.

        if (ipListaCompleta != null) {
            ipListaCompletaFinal = new ArrayList<>();
            if (mostrarTodoScan.isSelected()) {
                ipListaCompletaFinal = ipListaCompleta;
            } else {
                for (Ipss ip : ipListaCompleta) {
                    if (ip.getViva()) {
                        ipListaCompletaFinal.add(ip);
                    }
                }
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
        PrimaryController.btnBuscar_static.setDisable(true);
        PrimaryController.setDisableEnableBtn();
        String ipEscan = fieldIpEscan.getText();
        String puertos = fieldPuertos.getText();
        if (ipEscan.equals("")) {
            PrimaryController.setAlarmaError("Debe seleccionar o introducir una ip manualmente.");
        } else if (puertos.equals("")) {
            PrimaryController.setAlarmaError("No ha especificado puertos a escanear.");
        } else {
            tarea = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    //Escaneamos la red con los rangos especificados.
                    model.portEscaner(ipEscan, puertos);
                    return null;
                }
            };
            hiloEscaner = new Thread(tarea);
            hiloEscaner.setDaemon(true);
            hiloEscaner.setName("puertosThread");
            hiloEscaner.start();
        }
    }

    public static void setPuertos(ArrayList<Puerto> listaPuertos) {

        //Modelo nos envía el array cono de objetos Ipss, que nos indica las Ip's que están vivas.
        Platform.runLater(() -> {

            if (ipListaCompleta.isEmpty()) {
                for (Puerto p : listaPuertos) {
                    Ipss tmp = new Ipss(1, p.getIp(), p.getAbierto());
                    tmp.setPuertos(listaPuertos);
                    ipListaCompleta.add(tmp);
                    break;
                }

                for (Ipss e : ipListaCompleta) {
                    System.out.println("IP: " + e.getIp());
                    ArrayList<Puerto> pList = e.getPuertos();
                    for (Puerto p : pList) {
                        System.out.println("      Puerto: " + p.getPuerto());
                    }
                }

            } else {

                for (Ipss ipss : ipListaCompleta) {
                    if (!listaPuertos.isEmpty()) {
                        if (ipss.getIp().equals(listaPuertos.get(0).getIp())) {
                            ArrayList<Puerto> tmp = new ArrayList<>();
                            ipss.setPuertos(tmp);
                            ipss.setPuertos(listaPuertos);
                        }
                    }
                }

            }
            ipListaCompletaFinal = new ArrayList<>();
            for (Ipss ip : ipListaCompleta) {
                if (ip.getViva()) {
                    ipListaCompletaFinal.add(ip);
                }
            }

            PrimaryController.insertarTabla(ipListaCompletaFinal);
            btnBuscar_static.setDisable(false);

        });
    }

//############### FINAL ESCANER DE PUERTOS ###############
}
