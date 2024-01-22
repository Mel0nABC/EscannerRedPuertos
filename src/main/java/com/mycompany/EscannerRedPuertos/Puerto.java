/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.EscannerRedPuertos;

/**
 *
 * @author alex
 */
public class Puerto {
    
    private String ip;
    private int puerto;
    private boolean abierto;

    public Puerto(String ip, int puerto, boolean abierto) {
        this.ip = ip;
        this.puerto = puerto;
        this.abierto = abierto;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
    

    public int getPuerto() {
        return puerto;
    }

    public void setPuerto(int puerto) {
        this.puerto = puerto;
    }

    public boolean getAbierto() {
        return abierto;
    }

    public void setAbierto(boolean abierto) {
        this.abierto = abierto;
    }
    
    
    
}
