package com.mycompany.EscannerRedPuertos.Controlador;

import com.mycompany.EscannerRedPuertos.Modelo.Ipss;
import com.mycompany.EscannerRedPuertos.Modelo.Modelo;
import com.mycompany.EscannerRedPuertos.Modelo.Puerto;
import java.net.URL;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
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
    private static Button btnBuscar_static, btnBorrar_static, btnScanPort_static, btnGuardar_static;
    private static boolean visibleBtnStat = true;

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

    //Ejecución de threads para scanIps y scanpuertos.
    private Task tarea;
    private static Thread hiloEscaner;
    private Modelo model;

    //Array para a trabajar con las validaciones de ip.
    private String[] arrayIpTest;

    //CONSTANTE para establecer el tamaño máximo de rango, se usa 255 obviando el 254 de la red
    //Esto se debe porque se usa para todos los valores de rango, 8/16/24 y 32 bits.
    private final int RANGOMAX = 255;
    private final int RANGOMIN = 0;

    public void initialize(URL location, ResourceBundle arg1) {
        lblProceso_static = lblProceso;

        btnBuscar_static = btnBuscar;
        btnBorrar_static = btnBorrar;
        btnScanPort_static = btnScanPort;
        btnGuardar_static = btnGuardar;

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

        /*
        LISTA DE ERRORES DETECTADOR Y A SOLUCIONAR:

         */
        //Zona para hacer pruebas rápidas.
//        fieldIpInicio.setText("192.168.1.1");
//        fieldIpFinal.setText("192.168.1.10");
//        fieldPuertos.setText("1-80");
//        fieldIpEscan.setText("192.168.1.1");
//Para seleccionar la ip de la lista de ips escaneadas y colocarla en su tableField, fieldIpEscan
        resulTable.setOnMouseClicked(event -> {
            String ipSeleccionada = resulTable.getSelectionModel().getSelectedItem().getIp();
            if (ipSeleccionada != null) {
                fieldIpEscan.setText(ipSeleccionada);
                //Inicio aviso que la ip no dará resultados en puertos abiertos por no estar viva
                boolean localizada = false;
                for (Ipss ip : ipListaCompletaFinal) {
                    if (ip.getIp().equals(ipSeleccionada)) {
                        localizada = true;
                    }
                }
                if (!localizada) {
                    setAlarmaError("La ip " + ipSeleccionada + " no tendrá puertos abiertos por no estar viva.");
                }
                //Final aviso que la ip no dará resultados en puertos abiertos por no estar viva
            }
        });
    }
//############### INICIO ESCANER DE RED ###############

    public void btnBuscar() {

        if (btnBuscar.getText().equals("SCAN")) {
            btnBuscar.setText("STOP");
            inicioEscannerIp();
        } else {
            btnBuscar.setText("SCAN");
            tarea.cancel();
            hiloEscaner.interrupt();
            model.stopThreadsPing();
        }

    }

    public void inicioEscannerIp() {

        String ipInicio = fieldIpInicio.getText();
        String ipFinal = fieldIpFinal.getText();

        //Validaciones para comprobar que las ip's sean correctas, tanto en tipo de dato como en estructura ipv4.
        if (!testIp(ipInicio, "inicio")) {
            return;
        }

        if (!testIp(ipFinal, "final")) {
            return;
        }

        if (!testeaRangosIp(ipInicio, "inicio")) {
            return;
        }

        if (!testeaRangosIp(ipFinal, "final")) {
            return;
        }

        ipListaCompleta = new ArrayList<>();
        ipListaCompletaFinal = new ArrayList<>();

        tarea = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                String[] arrayIpInicioString = ipInicio.split("\\.");
                //Escaneamos la red con los rangos especificados.
                model.inicioScannerIp(arrayIpInicioString, ipFinal);
                return null;
            }
        };
        hiloEscaner = new Thread(tarea);
        hiloEscaner.setDaemon(true);
        hiloEscaner.setName("escanerThread");
        hiloEscaner.start();
        setDisableEnableBtn();
        btnBuscar_static.setDisable(false);

    }

    public boolean testIp(String ipString, String tipoIp) {

        boolean respuesta = true;
        arrayIpTest = ipString.split("\\.");

        if (ipString.equals("") | ipString.isEmpty() | arrayIpTest.length != 4) {
            setAlarmaError("Algo ocurre con la ip de " + tipoIp + " asignada, vuelva a intentarlo.");
            return respuesta = false;
        }

        boolean stopoFor = true;
        for (int i = 0; i < arrayIpTest.length; i++) {
            if (!isNumeric(arrayIpTest[i]) | arrayIpTest[i].length() > 3 | arrayIpTest[i].length() < 1) {
                setAlarmaError("En la ip " + tipoIp + ", ha introducido un valor que no es un número o el número es erróneo.");
                stopoFor = false;
                break;
            }
        }

        if (!stopoFor) {
            respuesta = false;
        }

        return respuesta;
    }

    public boolean isNumeric(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public boolean testeaRangosIp(String ipString, String tipoIp) {
        //Variables temporales para generar ipIn_X e ipFi_X
        String[] arrayIp = ipString.split("\\.");
        int[] arrayInt = new int[4];
        boolean respuesta = true;
        String msgAlarma = "Algún rango de ip de " + tipoIp + " es mayor a " + RANGOMAX + " o menor a " + RANGOMIN + ".";
        if (tipoIp.equals("final")) {
            msgAlarma = "Algún rango de ip de " + tipoIp + " es mayor a " + RANGOMAX + " o menor a " + RANGOMIN + ".";
        }

        //Pasamos el array de Strings a enteros de la ipInicial e ipFinal.
        for (int i = 0; i < arrayIp.length; i++) {
            int rangoTest = Integer.parseInt(arrayIp[i]);
            if (rangoTest > RANGOMAX | rangoTest < RANGOMIN) {
                setAlarmaError(msgAlarma);
                respuesta = false;
                break;
            }
        }
        return respuesta;
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

        if (ipListaCompleta.isEmpty() || ipListaCompleta == null) {
            setAlarmaError("Antes de guardar, debe escanear ip's o puertos.");
        } else {
            model.guardarEscaneoLocal(ipListaCompleta);
            setAlarmaError("Guardado satisfactoriamente.");
        }

    }

    public static void setDisableEnableBtn() {
//Deshabilitamos botones, fieldtext a la hora de escanear ips
        if (!visibleBtnStat) {
            btnBuscar_static.setDisable(false);
            fieldIpInicio_static.setDisable(false);
            fieldIpFinal_static.setDisable(false);
            fieldIpEscan_static.setDisable(false);
            fieldPuertos_static.setDisable(false);
            btnBorrar_static.setDisable(false);
            btnScanPort_static.setDisable(false);
            btnGuardar_static.setDisable(false);
            mostrarTodoScan_static.setDisable(false);

            visibleBtnStat = true;
        } else {
            btnBuscar_static.setDisable(true);
            fieldIpInicio_static.setDisable(true);
            fieldIpFinal_static.setDisable(true);
            fieldIpEscan_static.setDisable(true);
            fieldPuertos_static.setDisable(true);
            btnBorrar_static.setDisable(true);
            btnScanPort_static.setDisable(true);
            btnGuardar_static.setDisable(true);
            mostrarTodoScan_static.setDisable(true);
            visibleBtnStat = false;
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
            TableColumn<Ipss, String> columna3 = new TableColumn<>("PUERTOS");

            //Se especifica el campo de Ipss que se visualizará en cada columna
            columna0.setCellValueFactory(new PropertyValueFactory("id"));
            columna1.setCellValueFactory(new PropertyValueFactory("ip"));
            columna2.setCellValueFactory(new PropertyValueFactory("viva"));
//            columna3.setCellValueFactory(new PropertyValueFactory("puertos"));

            columna3.setCellValueFactory(cellData -> {
                List<Puerto> puertosLista = cellData.getValue().getPuertos();
                String puertosString = "";
                if (puertosLista != null) {
                    for (int i = 0; i < puertosLista.size(); i++) {
                        if (i + 1 == puertosLista.size()) {
                            puertosString += puertosLista.get(i).getPuerto();
                        } else {
                            puertosString += puertosLista.get(i).getPuerto() + " - ";
                        }

                    }
                } else {
                    puertosString = "Puertos no disponibles.";
                }

                return new SimpleStringProperty(puertosString);
            });

            //Se agregan las columnas al cableView.
            resulTable_static.getColumns().setAll(columna0, columna1, columna2, columna3);
        });

    }

    public void mostrarTodoONoDespuesDeScan() {
        //Metodo que nos mostrará ipListaCompleta u ipListaCompletaFinal, dependiendo si el checkbox está o no seleccionado.
        if (!ipListaCompleta.isEmpty()) {
            //aquí dentro tenemos puertos escaneados en iplistacompleta e iplistacompletafinal
            //cuando ipListaCompleta tiene contenido.
            if (mostrarTodoScan_static.isSelected()) {
                //Segunda opción: Aquí al seleccionar "MOSTRAR TODAS LAS IP" nos mostrará todo.
                insertarTabla(ipListaCompleta);
            } else {
                //Tercera opcion: Desmarcamos "MOSTRAR TODAS LAS IP" y sólo nos muestra ips vivas.
                insertarTabla(ipListaCompletaFinal);
            }
        }
    }

    public static void setItemsTable(ArrayList<Ipss> ipListaCompletaEntrada) {
//Recibimos una lista de ips escaneadas completa y filtramos para crear la lista ipListaCompletaFinal, que sólo son ip's vivas.
        //Modelo nos envía el array cono de objetos Ipss, que nos indica las Ip's que están vivas.
        Platform.runLater(() -> {
            ipListaCompleta = ipListaCompletaEntrada;
            for (Ipss ip : ipListaCompleta) {
                if (ip.getViva()) {
                    ipListaCompletaFinal.add(ip);
                }
            }
            btnBuscar_static.setText("SCAN");

            if (mostrarTodoScan_static.isSelected()) {
                insertarTabla(ipListaCompleta);
            } else {
                insertarTabla(ipListaCompletaFinal);
            }
            setDisableEnableBtn();
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
    //####################################################
    //####################################################
    //####################################################
    //####################################################
    //####################################################
    //####################################################
    //####################################################
    //####################################################
    //####################################################
    //####################################################
    //####################################################
    //############### INICIO ESCANER DE PUERTOS ###############
    public void portScan() {

        String ipEscan = fieldIpEscan.getText();
        String puertos = fieldPuertos.getText();

        if (!testIp(ipEscan, "puertos")) {
            return;
        }

        if (!testeaRangosIp(ipEscan, "puertos")) {
            return;
        }

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

        setDisableEnableBtn();

    }
    
    
    public static void setPuertosEnBlanco(){
        //Este método se ejecuta en Modelo, se han introducido clausulas guarda y, debo reactivar
        //los botones de operación de la aplicación. Ejemplo de la línea 
        setDisableEnableBtn();
    }

    public static void setPuertos(ArrayList<Puerto> listaPuertos) {

        //Modelo nos envía el array de objetos tipo Puerto, aquí buscamos si la ip de Puerto coincide con lo que tenemos en ipListaCompleta e actualizamos
        //el objeto Ipss de iplistacompleta añadiéndole el objeto Puerto.
        Platform.runLater(() -> {
            setDisableEnableBtn();
            if (ipListaCompleta.isEmpty()) {
                for (Puerto p : listaPuertos) {
                    Ipss tmp = new Ipss(1, p.getIp(), p.getAbierto());
                    tmp.setPuertos(listaPuertos);
                    ipListaCompleta.add(tmp);
                    System.out.println("INICIOO: " + listaPuertos.size());
                    break;
                }

            } else {

                //condicionar para gestión de listaPuertos == null. Evitamos excepciones.
                if (!listaPuertos.isEmpty()) {
                    for (Ipss ipss : ipListaCompleta) {
                        if (ipss.getIp().equals(listaPuertos.get(0).getIp())) {
                            ArrayList<Puerto> tmp = new ArrayList<>();
                            ipss.setPuertos(tmp);
                            System.out.println("TMP: " + ipss.getPuertos().size());
                            ipss.setPuertos(listaPuertos);
                            System.out.println("FINAL: " + ipss.getPuertos().size());
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
            if (listaPuertos.isEmpty()) {
                setAlarmaError("No se han localizado ningún puerto abierto.");
            }
            setEstatus("Puertos detectados abiertos: " + listaPuertos.size());

            mostrarTodoScan_static.setSelected(false);
            PrimaryController.insertarTabla(ipListaCompletaFinal);
        });
        //Habilitamos botones al finalizar scan de puertos.
    }

//############### FINAL ESCANER DE PUERTOS ###############
}
