package com.mycompany.EscannerRedPuertos;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 *
 * @author alex
 */
public class Ping implements Runnable {

    private String ip;
    private static boolean resultado;
    private boolean ping;
    private final int TIMEOUTPING = 1500;

    public Ping(String ip) {
        this.ip = ip;
    }

    public void ping() {
        String hostName = "";
        try {
            InetAddress address = InetAddress.getByName(ip);
            try {
                resultado = address.isReachable(TIMEOUTPING);
                
                //Para asignar el host de una ip viva.
                if (resultado) {
                    hostName = address.getHostName();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }

        } catch (UnknownHostException ex) {
            ex.printStackTrace();
        }
        Ipss ipsTmp = new Ipss(ip, resultado);
        Modelo.setResultado(ipsTmp);
    }
    
    
    public boolean getResultado(){
        return resultado;
    }

    @Override
    public void run() {
        ping();
    }

}
