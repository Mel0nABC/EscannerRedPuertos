package com.mycompany.EscannerRedPuertos;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
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
    private String ipInicio = "";
    private String ipFinal = "";
    private String ipEscan = "";
    private String puertos = "";
    private String[] arrayIpInicio;
    private String[] arrayIpFinal;
    private String[] arrayIpEscan;
    private String[] arrayPuertos;
    private int contadorIpScan = 0;
    private ArrayList<Thread> listaThreadsPing;
    private ArrayList<Thread> listaThreadsPorts;
    private ArrayList<Integer> arrayPuertosRango;
    private static ArrayList<Ipss> ipListaCompleta;
    private ArrayList<String> ips;
    private ArrayList<Integer> arrayPuertosInt;
    private int[] arrayIntInicio;
    private int[] arrayIntFinal;

    //Variables del control de finalizar o continuar aplicación.
    private boolean asignaIpFinal;

    //Objeto FileWriter para guardar el archivo de log mas adelaante.
    private FileWriter fichero;

//############### INICIO ESCANER DE RED ###############
    public void escanearRed(String ipInicioString, String ipFinalString) {
        
        listaThreadsPing = new ArrayList<>();
        listaThreadsPorts = new ArrayList<>();
        arrayPuertosRango = new ArrayList<>();
        ipListaCompleta = new ArrayList<>();
        ips = new ArrayList<>();
        arrayPuertosInt = new ArrayList<>();

        boolean asignaIpInicio = true;

        ipInicio = ipInicioString;

        arrayIpInicio = ipInicio.split("\\.");

        if (arrayIpInicio.length == 4) {
            asignaIpInicio = true;
        } else {
            asignaIpInicio = false;
            PrimaryController.setAlarmaError("Algo ocurre con la ip de inicio asignada, vuelva a intentarlo.");
        }

        if (asignaIpInicio) {

            asignaIpFinal = true;

            ipFinal = ipFinalString;

            arrayIpFinal = ipFinal.split("\\.");

            boolean testRangos = true;

            if (arrayIpFinal.length == 4) {

                if (ipInicio.compareTo(ipFinal) > 0) {
                    PrimaryController.setAlarmaError("La ip inicial es de rango superior a la ip final.");
                    asignaIpFinal = false;
                    testRangos = false;
                }
                if (!testRangos) {
                    System.out.println("La ip de inicio es mayor que la ip final, reviselo, por favor..");
                    System.out.println("IP Inicio: " + ipInicio + " - IP Final: " + ipFinal);
                    System.out.println("Vuelva a intentearlo.");
                    asignaIpFinal = false;
                } else {
                    asignaIpFinal = true;
                    System.out.println("Comienza el escaneo de "+ipInicioString+ " hasta "+ipFinalString);
                }
            } else {
                PrimaryController.setAlarmaError("Algo ocurre con la ip final asignada, vuelva a intentarlo.");

            }

        } else {

        }

        if (asignaIpFinal) {
            recorrerRangoIp();

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

                System.out.println("#########################################################################################");
                System.out.println("#################### IP INICIO: " + ipInicio + " -- IP FINAL: " + ipFinal + " ####################");
                System.out.println("Finalizó el escaneo de la red y se detectaron " + ipListaCompleta.size() + " ip's conectadas en " + contadorIpScan + " escaneadas.");
                System.out.println("#########################################################################################");
                System.out.println("IP's registradas en la red:");

                //Recorremos todas las ip's encontradas y las escribimos en el log con println.
                for (int i = 0; i < ipListaCompleta.size(); i++) {
                    ipListaCompleta.get(i).setId(i + 1);
                    System.out.println("ID: " + ipListaCompleta.get(i).getId() + " con IP: " + ipListaCompleta.get(i).getIp());

                    escribe.println(ipListaCompleta.get(i).getIp());
                }
                System.out.println("#################################################################################1#########");
                PrimaryController.setItemsTable(ipListaCompleta);
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

    }

    //Método por el cual viene el resultado desde la clase ping, cada ping envía su resultado en un objeto clase Ipss
    public static void setResultado(Ipss ipss) {
        if (ipss.getViva()) {
            ipListaCompleta.add(ipss);
        }
    }

    public void recorrerRangoIp() {

        //Arrays de enteros para pasar los arrays de string obtenidos de los strings de ipInicio e ipFinal.
        arrayIntInicio = new int[4];
        arrayIntFinal = new int[4];

        //Pasamos el array de Strings a entero.
        for (int i = 0; i < arrayIpInicio.length; i++) {
            arrayIntInicio[i] = Integer.parseInt(arrayIpInicio[i]);
            arrayIntFinal[i] = Integer.parseInt(arrayIpFinal[i]);
        }

        int ipIn_1 = arrayIntInicio[0];
        int ipIn_2 = arrayIntInicio[1];
        int ipIn_3 = arrayIntInicio[2];
        int ipIn_4 = arrayIntInicio[3];

        ips = new ArrayList<>();

        String ip;

        do {

            ip = ipIn_1 + "." + ipIn_2 + "." + ipIn_3 + "." + ipIn_4;
            contadorIpScan++;

            if (ips.size() < THREADS_SIZE_IPS) {
                ips.add(ip);
            } else {
                threadPing(ips);
                ips = new ArrayList<>();
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
            //System.out.println(ip);
        } while (!ipFinal.equals(ip));

        if (ips.size() != THREADS_SIZE_IPS) {
            threadPing(ips);
        }

    }

    public void threadPing(ArrayList<String> ips) {

        for (String ip : ips) {
            Ping ping = new Ping(ip);
            Thread t = new Thread(ping);
            t.setName(ip);
            t.start();
            listaThreadsPing.add(t);
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
    }

    //############### FINAL ESCANER DE RED ###############
    //############### INICIO ESCANER DE PUERTOS ###############
    //Método para escanear los puertos, opción del menú número 2.
    public void portEscaner(String ipEscan, String puertos) {
        System.out.println("########################");
        System.out.println("## ESCANER DE PUERTOS ##");
        System.out.println("########################");

        boolean asignaIpEscanear = true;

        do {
            System.out.println("Por favor, indique ip a escanear:");
            ipEscan = ipEscan;

            arrayIpEscan = ipEscan.split("\\.");

            if (arrayIpEscan.length == 4) {
                asignaIpEscanear = false;
            } else {
                System.out.println("Algo ocurre con la ip asignada, vuelva a intentarlo.");
            }
        } while (asignaIpEscanear);

        boolean seleccionPuertos = true;
        boolean puertoIndividual = true;
        do {

            System.out.println("Por favor, indique los puertos. Individual, concretos, rango");
            System.out.println("Ejemplo individual: 2000");
            System.out.println("Ejemplo concretos: 2000,2001,8080 ...");
            System.out.println("Ejemplo rango: 2000-2010");
            puertos = puertos;

            boolean tipoSeparacion = false;

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

            if (isNumeric(puertos) && puertoIndividual == true) {
                arrayPuertosInt.add(Integer.parseInt(puertos));
                seleccionPuertos = false;
                escanerPuertos();
            }

            if (!puertoIndividual) {
                //if para gestión de puertos individuales
                if (tipoSeparacion) {
                    System.out.println("numeric");
                    arrayPuertos = puertos.split(",");

                    boolean multiplesPuertosOk = true;
                    for (int i = 0; i < arrayPuertos.length; i++) {
                        if (isNumeric(arrayPuertos[i])) {
                            arrayPuertosInt.add(Integer.parseInt(arrayPuertos[i]));
                            seleccionPuertos = false;
                        } else {
                            System.out.println("El caràcter '" + arrayPuertos[i] + "' no corresponde a un número, se reinicia la aplicación.");
                            multiplesPuertosOk = false;
                            seleccionPuertos = true;
                        }

                    }

                    if (multiplesPuertosOk) {
                        escanerPuertos();
                    }

                } else {//else para gestión de rango de puertos.
                    arrayPuertos = puertos.split("-");
                    int puertoInicio = 0;
                    int puertoFinal = 0;
                    if (isNumeric(arrayPuertos[0])) {
                        puertoInicio = Integer.parseInt(arrayPuertos[0]);
                    } else {
                        System.out.println("El carácter '" + arrayPuertos[0] + "' no corresponde a un número, se reinicia la aplicación.");
                    }

                    if (isNumeric(arrayPuertos[1])) {
                        puertoFinal = Integer.parseInt(arrayPuertos[1]);
                    } else {
                        System.out.println("El carácter '" + arrayPuertos[1] + "' no corresponde a un número, se reinicia la aplicación.");
                    }
                    if (isNumeric(arrayPuertos[0]) && isNumeric(arrayPuertos[1])) {
                        seleccionPuertos = false;
                        escanerPuertosRango(puertoInicio, puertoFinal);
                    }

                }
            }
        } while (seleccionPuertos);

    }

    public void escanerPuertos() {

        EscannerPuertos escaner = new EscannerPuertos();

        for (int i : arrayPuertosInt) {
            escaner.getPuerto(ipEscan, i);
        }
        ArrayList<Integer> array = escaner.getArray();

        if (array.size() == 0) {
            System.out.println("No se han localizado ningún puerto abierto.");
        } else {
            System.out.println("###############################################");
            System.out.println("### Finalizó el escaneo de puertos abiertos ###");
            System.out.println("###############################################");
            for (int i : array) {
                System.out.println(i + " - open");
            }
            System.out.println("###############################################");
        }

    }

    public void threadPort(ArrayList<Integer> arrayPuertos) {

        for (int i = 0; i < arrayPuertos.size(); i++) {

            EscannerPuertos escaner = new EscannerPuertos(ipEscan, arrayPuertos.get(i));
            Thread t = new Thread(escaner);
            t.setName("escaner" + arrayPuertos.get(i));
            t.start();
            listaThreadsPorts.add(t);

        }

        boolean estado = true;
        while (estado) {

            estado = false;
            for (Thread t : listaThreadsPorts) {
                if (t.isAlive()) {
                    t.interrupt();
                    estado = true;
                }
            }
        }

    }

    public void escanerPuertosRango(int puertoInicio, int puertoFinal) {

        ArrayList<Integer> arrayPuertos = new ArrayList<>();

        for (int i = puertoInicio; i <= puertoFinal; i++) {

            if (arrayPuertos.size() < THREADS_SIZE_PUERTOS) {
                arrayPuertos.add(i);
            } else {
                threadPort(arrayPuertos);
                arrayPuertos = new ArrayList<>();
                arrayPuertos.add(i);
            }
        }

        if (arrayPuertosRango.isEmpty()) {
            System.out.println("No se han localizado ningún puerto abierto.");
        } else {

            System.out.println("###############################################");
            System.out.println("### Finalizó el escaneo de puertos abiertos ###");
            System.out.println("###############################################");
            System.out.println("Puertos del " + puertoInicio + " al " + puertoFinal);
            for (int i : arrayPuertosRango) {
                System.out.println(i + " - open");
            }
            System.out.println("###############################################");

        }

    }

    public void setPuertoUnico(int puertoUnico) {
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
