package com.mycompany.assignment1;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.Serializable;

/**
 *
 * @author diamo
 */
public class DroneDetails  implements Serializable {
    
    // Set serialVersion so classes are the same
    static final long serialVersionUID = -687991492884005033L;
    
    // Values in this object
    private int id;
    private String name;
    private int x_pos;
    private int y_pos;
    private boolean active;
    
    // Constructor
    
    public DroneDetails (int id, String name, int x_pos, int y_pos, boolean active) {
        this.id = id;
        this.name = name;
        this.x_pos = x_pos;
        this.y_pos = y_pos;
        this.active = active;
    }
    
    // Accessors / Getters
    
    public int getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public int getX_pos() {
        return x_pos;
    }
    
    public int getY_pos() {
        return y_pos;
    }
    
    public boolean getActive() {
        return active;
    }
    
    // Mutators / Setters
    
    public void setId(int id) {
        this.id = id;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setX_pos(int x_pos) {
        this.x_pos = x_pos;
    }
    
    public void setY_pos(int y_pos) {
        this.y_pos = y_pos;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    // toString() Method
    @Override
    public String toString() {
        return 
               "ID: " + id + "\n" +
               "Name: " + name + "\n" +
               "X Position: " + x_pos + "\n" +
               "Y Position: " + y_pos + "\n";
    }
}
