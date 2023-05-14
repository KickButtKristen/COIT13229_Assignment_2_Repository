/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.assignment1;

import java.io.Serializable;

/**
 *
 * @author Kristen
 */
public class FiretruckDetails implements Serializable {
    
    private static final long serialVersionUID = 6529685098267757700L;
    
    private int id;
    private String name;
    private int designatedFireId;
    
    public FiretruckDetails(int id, String name, int designatedFireId) {
        this.id = id;
        this.name = name;
        this.designatedFireId = designatedFireId;
    }
    
    // Getters
    public int getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public int getDesignatedFireId() {
        return designatedFireId;
    }
    
    // Setters
    public void setId(int id) {
        this.id = id;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setDesignatedFireId(int designatedFireId) {
        this.designatedFireId = designatedFireId;
    }

    public String toCSV() {
        return id + "," +
               name + "," +
               designatedFireId;
    }
    
    @Override
    public String toString() {
        return "Fire Truck ID: " + id + "\n" +
               "Name: " + name + "\n" +
               "Designated Fire ID: " + designatedFireId + "\n";
    }
}