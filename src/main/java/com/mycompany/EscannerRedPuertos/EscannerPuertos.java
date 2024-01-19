package com.mycompany.EscannerRedPuertos;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;

/**
 *
 * @author alex
 */
public class EscannerPuertos implements Runnable {

    private ArrayList<Integer> arrayPuertosAbiertos = new ArrayList<>();
    private Socket sock;
    private int puertoUnico;
    private String ip = "";

    public EscannerPuertos() {
    }

    public EscannerPuertos(String ip, int puertoUnico) {
        this.ip = ip;
        this.puertoUnico = puertoUnico;
    }

    public void getPuerto(String host, int puerto) {

        try {
            sock = new Socket();
            sock.connect(new InetSocketAddress(host, puerto), 1000);

            if (sock.isConnected()) {
                System.out.println("Puerto abierto: " + puerto);
                arrayPuertosAbiertos.add(puerto);
                puertoUnico = puerto;
                //Modelo.setPuertoUnico(puertoUnico);
                sock.close();
            }

        } catch (IOException ex) {
            System.out.println("Puerto cerrado: " + puerto);
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
