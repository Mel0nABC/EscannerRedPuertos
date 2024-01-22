/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.EscannerRedPuertos;

import java.util.ArrayList;

/**
 *
 * @author alex
 */
public class Ipss {

    private int id;
    private String ip;
    private boolean viva;
    private ArrayList<Puerto> puertos;

    public Ipss(String ip) {
        this.ip = ip;
        this.viva = viva;
    }

    public Ipss(int id, String ip, boolean viva) {
        this.id = id;
        this.ip = ip;
        this.viva = viva;
    }
    
    public Ipss(String ip, boolean viva) {
        this.ip = ip;
        this.viva = viva;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public boolean getViva() {
        return viva;
    }

    public void setViva(boolean viva) {
        this.viva = viva;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ArrayList<Puerto> getPuertos() {
        return puertos;
    }

    public void setPuertos(ArrayList<Puerto> puertos) {
        this.puertos = puertos;
    }

}
