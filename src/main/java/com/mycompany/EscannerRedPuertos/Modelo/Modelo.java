package com.mycompany.EscannerRedPuertos.Modelo;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
import com.mycompany.EscannerRedPuertos.Controlador.PrimaryController;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;

/**
 *
 * @author alex
 */
public class Modelo {

    //Constante de límite máximo de un rango de IP
    private final int RANGOMAX = 255;

    //Constante para los hilos simultáneos a la hora de escanear ip's
    private final int THREADS_SIZE_IPS = 500;
    //Constante para los hilos simultáneos a la hora de escanear puertos
    private final int THREADS_SIZE_PUERTOS = 500;

    //Declaración e inicialización de variables del aplicativo.
    private String ipEscan = "";
    private String puertos = "";
    private String[] arrayIpEscan;
    private String[] arrayPuertos;
    private int contadorIpScan = 0;
    private ArrayList<Thread> listaThreadsPing;
    private ThreadGroup grupoDeThreads;
    private boolean stopNuevoThread;

    private ArrayList<Thread> listaThreadsPorts;
    private static ArrayList<Integer> arrayPuertosRango;
    private static ArrayList<Ipss> ipListaCompleta;
    private ArrayList<String> ips;
    private ArrayList<Integer> arrayPuertosInt;
    private static boolean permisoEnvioPuertos = true;
    //Contador para asignar id's en threadPing.
    int contadorIds = 1;

    //Objeto FileWriter para guardar el archivo de log mas adelaante.
    private FileWriter fichero;

    public void inicioScannerIp(String[] arrayIpInicioString, String ipFinal) {

        int ipIn_1 = Integer.parseInt(arrayIpInicioString[0]);
        int ipIn_2 = Integer.parseInt(arrayIpInicioString[1]);
        int ipIn_3 = Integer.parseInt(arrayIpInicioString[2]);
        int ipIn_4 = Integer.parseInt(arrayIpInicioString[3]);

        grupoDeThreads = new ThreadGroup("MiGrupoDeHilos");
        stopNuevoThread = true;
        listaThreadsPing = new ArrayList<>();
        listaThreadsPorts = new ArrayList<>();
        arrayPuertosRango = new ArrayList<>();
        ipListaCompleta = new ArrayList<>();
        ips = new ArrayList<>();
        arrayPuertosInt = new ArrayList<>();

        contadorIds = 1;

        String ip;

        do {
            ip = ipIn_1 + "." + ipIn_2 + "." + ipIn_3 + "." + ipIn_4;
            contadorIpScan++;
            if (ips.size() < THREADS_SIZE_IPS) {
                ips.add(ip);
            } else {
                threadPing(ips);
                ips = new ArrayList<>();
                //enviamos ip's añadidas a array ips cuandoo hemos llegado a cumplir que su tamaño es igual a THREADS_SIZE_IPS
                ips.add(ip);

            }

            if (ipIn_4 < RANGOMAX) {
                ipIn_4++;
            } else {

                if (ipIn_3 < RANGOMAX) {
                    ipIn_4 = 1;
                    ipIn_3++;
                } else {

                    if (ipIn_2 < RANGOMAX) {
                        ipIn_4 = 1;
                        ipIn_3 = 1;
                        ipIn_2++;
                    } else {
                        if (ipIn_1 < RANGOMAX) {
                            ipIn_4 = 1;
                            ipIn_3 = 1;
                            ipIn_2 = 1;
                            ipIn_1++;
                        }
                    }

                }

            }
        } while (!ipFinal.equals(ip));
        //enviamos últimas ip's añadidas al array ips.
        threadPing(ips);

        PrimaryController.setItemsTable(ipListaCompleta);
    }

    public void threadPing(ArrayList<String> ips) {
        //Generaamos Threads de la lista de ips que entra.
        String ipString = "";
        if (stopNuevoThread) {

            for (String ip : ips) {
                try {
                    ipString = ip;
                    Ping ping = new Ping(ip, contadorIds);
                    Thread t = new Thread(grupoDeThreads, ping);
                    t.setName(ip);
                    t.start();
                    if (!stopNuevoThread) {
                        PrimaryController.setEstatus("ESTATUS: Cancelando....");
                    } else {
                        PrimaryController.setEstatus("Escaneando ip: " + ipString);
                    }

                    listaThreadsPing.add(t);
//                    Se añade pequeño delay, porque si no se perdian ip's escaneadas, 
//                    no llegaban a salir en el array final.
                    Thread.sleep(10);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                contadorIds++;
            }

            boolean estado = true;
            while (estado) {

                estado = false;
                for (Thread t : listaThreadsPing) {
                    if (t.isAlive()) {
                        t.interrupt();
                        estado = true;
                    }
                }
            }
        } else {
            PrimaryController.setEstatus("ESTATUS: Cancelado");
        }
    }

    public void stopThreadsPing() {
        //Método para parar la generación de threadPing
        stopNuevoThread = false;
    }

    public static void setResultado(Ipss ipss) {
        //Método por el cual viene el resultado desde la clase ping, cada ping envía su resultado en un objeto clase Ipss
        //filtramos y obtenemos sólo las vivas.
        //primera opcion filtra resultados de viva, segunda opción no filtra nada.
//        if (ipss.getViva()) {
//            ipListaCompleta.add(ipss);
//        }
        ipListaCompleta.add(ipss);

    }

    public void guardarEscaneoLocal(ArrayList<Ipss> listaGuardar) {

        //Método llamado por el botón GUARDAR para guardar el histórico de la lista completa de 
        //IPS y se los puertos que se hayan detectado abiertos.
        try {

            //Objeto calendar, para la obtención de horas, cara a guardar el log.
            Calendar calendario = Calendar.getInstance();
            int dia = calendario.get(Calendar.DAY_OF_MONTH);
            int mes = calendario.get(Calendar.MONTH);
            int year = calendario.get(Calendar.YEAR);
            int hora = calendario.get(Calendar.HOUR_OF_DAY);
            int minutos = calendario.get(Calendar.MINUTE);
            int segundos = calendario.get(Calendar.SECOND);

            String fecha = hora + "h" + minutos + "m" + segundos + "s_" + dia + "-" + mes + "-" + year;

            //inicialización del objeto fichero, para indicar dónde guardaremos el log y su estructura de nombree
            fichero = new FileWriter(fecha + "_escaner.log");
            //Creamos el fichero .log.
            PrintWriter escribe = new PrintWriter(fichero);
            //Recorremos todas las ip's encontradas y las escribimos en el log con println.
            for (int i = 0; i < listaGuardar.size(); i++) {
                String estado = "";
                String puertosAbiertos = "No se escanearon puertos.";
                estado = "SI";

                if (!listaGuardar.get(i).getViva()) {
                    estado = "NO";
                }

                puertosAbiertos = "";
                ArrayList<Puerto> listaPuertos = listaGuardar.get(i).getPuertos();

                if (listaPuertos != null) {

                    for (int j = 0; j < listaPuertos.size(); j++) {
                        if (j + 1 == listaPuertos.size()) {
                            puertosAbiertos += listaPuertos.get(j).getPuerto() + ".";
                        } else {
                            puertosAbiertos += listaPuertos.get(j).getPuerto() + ", ";
                        }
                    }

                }

                escribe.println("ID: " + (i + 1) + " - IP: " + listaGuardar.get(i).getIp() + ":");
                escribe.println("               IP VIVA: " + estado);
                escribe.println("               PUERTOS ABIERTOS: " + puertosAbiertos);
            }

        } catch (IOException ex) {

        } finally {
            try {
                if (fichero != null) {
                    fichero.close();
                }

            } catch (IOException ex) {
                System.out.println("Fichero NULL");
            }
        }
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
    //############### INICIO ESCANER DE PUERTOS ###############
    //Método para escanear los puertos, opción del menú número 2.
    public void portEscaner(String ipEscanEntrada, String puertosEntrada) {
        arrayPuertosInt = new ArrayList<>();
        arrayPuertosRango = new ArrayList<>();
        listaThreadsPorts = new ArrayList<>();
        this.ipEscan = ipEscanEntrada;
        this.puertos = puertosEntrada;
        boolean seleccionPuertos = true;
        boolean puertoIndividual = true;
        boolean tipoSeparacion = false;

        //Comprobamos que tipo de envío de puertos han hecho, separado por , 
        //o por guiones, si no es ninguno, es un puerto individual
        for (int i = 0; i < puertos.length(); i++) {
            if (puertos.charAt(i) == ',') {
                //Paraa separación por coma
                tipoSeparacion = true;
                puertoIndividual = false;
            } else if (puertos.charAt(i) == '-') {
                tipoSeparacion = false;
                puertoIndividual = false;
            }
        }

        //Si el puerto es individual, comprobamos que sea un números y ejecutamos escanerPuertos()
        if (isNumeric(puertos) && puertoIndividual == true) {
            arrayPuertosInt.add(Integer.parseInt(puertos));
            seleccionPuertos = false;
            escanerPuertos();
            return;
        }

        if (tipoSeparacion) {
            //if para gestión de multipuertos, separados por comas.
            arrayPuertos = puertos.split(",");
            String puertosError = "";
            boolean multiplesPuertosOk = true;
            boolean error = false;
            for (int i = 0; i < arrayPuertos.length; i++) {
                if (isNumeric(arrayPuertos[i])) {
                    arrayPuertosInt.add(Integer.parseInt(arrayPuertos[i]));
                    seleccionPuertos = false;
                } else {
                    puertosError += arrayPuertos[i] + " - ";
                    multiplesPuertosOk = false;
                    seleccionPuertos = true;
                    error = true;
                }
            }
            if (error) {
                PrimaryController.setAlarmaError("El caràcter " + puertosError + "' no corresponde a un número");
                return;
            }
            if (multiplesPuertosOk) {
                escanerPuertos();
            }

        } else {
            //else para gestión de rango de puertos. x-y
            arrayPuertos = puertos.split("-");
            int puertoInicio = 0;
            int puertoFinal = 0;

            if (!isNumeric(arrayPuertos[0])) {
                PrimaryController.setAlarmaError("El carácter " + arrayPuertos[0] + "' no corresponde a un número");
                //setPuertosBlanco, es para que los botones vuelvan a estar habilitados al realizar return en este condicional.
                PrimaryController.setPuertosEnBlanco();
                return;
            }

            if (!isNumeric(arrayPuertos[1])) {
                PrimaryController.setAlarmaError("El carácter " + arrayPuertos[1] + "' no corresponde a un número.");
                //setPuertosBlanco, es para que los botones vuelvan a estar habilitados al realizar return en este condicional.
                PrimaryController.setPuertosEnBlanco();
                return;
            }

            puertoInicio = Integer.parseInt(arrayPuertos[0]);
            puertoFinal = Integer.parseInt(arrayPuertos[1]);
            seleccionPuertos = false;
            escanerPuertosRango(puertoInicio, puertoFinal);
        }
//        }

    }

    public void escanerPuertos() {

        EscannerPuertos escaner = new EscannerPuertos();
        ArrayList<Integer> array = escaner.getArray();
        ArrayList<Puerto> arrayClasePuerto = new ArrayList<>();

        for (int i : arrayPuertosInt) {
            escaner.getPuerto(ipEscan, i);
        }

        if (array.size() == 0) {

            PrimaryController.setPuertos(arrayClasePuerto);
        } else {
            for (int i : array) {
                Puerto puertoTmp = new Puerto(ipEscan, i, true);
                arrayClasePuerto.add(puertoTmp);
            }
            PrimaryController.setPuertos(arrayClasePuerto);
        }
    }

    public void escanerPuertosRango(int puertoInicio, int puertoFinal) {
        ArrayList<Integer> arrayPuertos = new ArrayList<>();
        ArrayList<Puerto> arrayClasePuerto = new ArrayList<>();

        //Montamos arrays con el tamaño THREADS_SIZE_PUERTOS para escanearlos. Se envia a threadPort
        int contador = 0;

        for (int i = puertoInicio; i <= puertoFinal; i++) {
            if (contador == THREADS_SIZE_PUERTOS | i == puertoFinal) {
                threadPort(arrayPuertos);
                contador = 0;
                arrayPuertos = new ArrayList<>();
                arrayPuertos.add(i);
            } else {
                arrayPuertos.add(i);
            }
            if (i == puertoFinal) {
                threadPort(arrayPuertos);
            }
            contador++;
        }

        if (arrayPuertos.size() != 0) {
            do {
                if (!permisoEnvioPuertos) {
                    for (int i : arrayPuertosRango) {
                        Puerto puertoTmp = new Puerto(this.ipEscan, i, true);
                        arrayClasePuerto.add(puertoTmp);
                    }
                }
            } while (permisoEnvioPuertos);
        }

        PrimaryController.setPuertos(arrayClasePuerto);
    }

    public void threadPort(ArrayList<Integer> arrayPuertos) {
        for (int i = 0; i < arrayPuertos.size(); i++) {
            Thread t;
            try {
                EscannerPuertos escaner = new EscannerPuertos(ipEscan, arrayPuertos.get(i));
                PrimaryController.setEstatus("Escaneando puerto: " + arrayPuertos.get(i));
                t = new Thread(escaner);

                t.setName("escaner" + arrayPuertos.get(i));
                t.start();
                listaThreadsPorts.add(t);
                Thread.sleep(10);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }

        while (permisoEnvioPuertos) {

            permisoEnvioPuertos = false;
            for (Thread t : listaThreadsPorts) {
                if (t.isAlive()) {
                    t.interrupt();
                    permisoEnvioPuertos = true;
                }
            }
        }

    }

    public static void setPuertoUnico(int puertoUnico) {
        arrayPuertosRango.add(puertoUnico);
    }

    public boolean isNumeric(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }

//############### FINAL ESCANER DE PUERTOS ###############
    }

}
