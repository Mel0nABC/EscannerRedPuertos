package com.mycompany.EscannerRedPuertos.Modelo;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

import com.mycompany.EscannerRedPuertos.Controlador.PrimaryController;
import com.mycompany.EscannerRedPuertos.Modelo.Modelo;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;

/**
 *
 * @author alex
 */
public class EscannerPuertos implements Runnable {

    private static ArrayList<Integer> arrayPuertosAbiertos = new ArrayList<>();
    private static Socket sock;
    private static int puertoUnico;
    private static String ip = "";

    public EscannerPuertos() {
        arrayPuertosAbiertos = new ArrayList<>();
    }

    public EscannerPuertos(String ip, int puertoUnico) {
        this.ip = ip;
        this.puertoUnico = puertoUnico;
        arrayPuertosAbiertos = new ArrayList<>();
    }

    public static void getPuerto(String host, int puerto) {
        
        try {
            sock = new Socket();
            sock.connect(new InetSocketAddress(host, puerto), 1000);

            if (sock.isConnected()) {
//                System.out.println("Puerto abierto: " + puerto);
                arrayPuertosAbiertos.add(puerto);
                puertoUnico = puerto;
                Modelo.setPuertoUnico(puertoUnico);
                sock.close();
            }

        } catch (IOException ex) {
//            System.out.println("Puerto cerrado: " + puerto);
        }

    }

    public ArrayList<Integer> getArray() {
        return arrayPuertosAbiertos;
    }

    public int getPuertoUnico() {
        return puertoUnico;
    }

    @Override
    public void run() {
        getPuerto(ip, puertoUnico);
    }
}
    