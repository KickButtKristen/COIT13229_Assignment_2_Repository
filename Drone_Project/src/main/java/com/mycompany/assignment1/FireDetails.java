package com.mycompany.assignment1;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.Serializable;

public class FireDetails implements Serializable {
    
    private static final long serialVersionUID = 6529685098267757690L;
    
    private int id;
    private int xpos;
    private int ypos;
    private boolean isActive;
    private int intensity;
    private double burningAreaRadius;
    
    public FireDetails(int id, boolean isActive, int intensity, double burningAreaRadius, int xpos, int ypos) {
        this.id = id;
        this.xpos = xpos;
        this.ypos = ypos;
        this.isActive = isActive;
        this.intensity = intensity;
        this.burningAreaRadius = burningAreaRadius;
    }
    
    // Getters
    public int getId() {
        return id;
    }
    
    public int getXpos() {
        return xpos;
    }
    
    public int getYpos() {
        return ypos;
    }

    public boolean isActive() {
        return isActive;
    }

    public int getIntensity() {
        return intensity;
    }

    public double getBurningAreaRadius() {
        return burningAreaRadius;
    }
    
    // Setters
    public void setId(int id) {
        this.id = id;
    }
    
    public void setXpos(int xpos) {
        this.xpos = xpos;
    }
    
    public void setYpos(int ypos) {
        this.ypos = ypos;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    public void setIntensity(int intensity) {
        this.intensity = intensity;
    }

    public void setBurningAreaRadius(double burningAreaRadius) {
        this.burningAreaRadius = burningAreaRadius;
    }
    
    public String toCSV() {
        return id + "," +
               xpos + "," +
               ypos + "," +
               isActive + "," +
               intensity + "," +
               burningAreaRadius;
    }
    
    @Override
    public String toString() {
        return "Fire ID: " + id + "\n" +
               "X Position: " + xpos + "\n" +
               "Y Position: " + ypos + "\n" +
               "Active: " + isActive + "\n" +
               "Intensity: " + intensity + "\n" +
               "Burning Area Radius: " + burningAreaRadius + "\n";
    }
}