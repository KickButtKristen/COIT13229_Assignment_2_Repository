/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.assignment1;

import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.net.*;
import java.io.*;
import java.util.ArrayList;
/**
 *
 * @author diamo
 */
public class Drone extends Thread {

    static DroneDetails drone; // Drone Object
    static ArrayList<FireDetails> fires = new ArrayList<>(); // ArrayList for storing fires found since last server update
    static boolean recallStatus = false; // If a recall has been initiated
    static int movements = 1; // How many movements have been done since direction change
    
    // Drone cooordinates
    static int x_pos = 0;
    static int y_pos = 0;
    
    public static void main(String[] args) throws InterruptedException {
        Scanner scanner = new Scanner(System.in);
        Random rand = new Random();
        
        // Socket Initialisation
        Socket s = null;
        
        // Host Name of Server
        String hostName = "localhost";
        
        // Messages received and sent to the server.
        String serverMessage = "";
        String message = "";
        
        // Used for movements
        int direction = 0;
        
        // Drone ID
        int id = 0;
        
        // Drone Name
        String name;
        
        // Asks user to input ID, reads input, if the ID can not be parsed into an integer, displays error and allows re-input
        while (true) {
            System.out.println("Enter Drone ID: ");
            // Receives input on next line
            String idInput = scanner.nextLine();
            try {
                id = Integer.parseInt(idInput);
                if (id < 1) {
                    System.out.println("ID must not be zero or negative.");
                } else {
                    // Breaks while statement to allow code continuation
                    break;
                }
            } catch (NumberFormatException e) {
                System.out.println("ID must be numeric only."); // Error message if parseInt fails
            }
        }
        
        // Asks user to input name, reads input and sets it to the name variable
        System.out.println("Enter Drone Name: ");
        name = scanner.nextLine();
        
        // Adds drone details to a new DroneDetails object named drone
        drone = new DroneDetails(id, name, x_pos, y_pos, true);
        
        // Make first connection here
        try {
            int serverPort = 8888;
            
            s = new Socket(hostName, serverPort);
            
            ObjectInputStream in = null;
            ObjectOutputStream out = null;
			
            out = new ObjectOutputStream(s.getOutputStream());
            in = new ObjectInputStream(s.getInputStream());
            
            // Sends drone object to server and confirms
            out.writeObject(drone);
            serverMessage = (String)in.readObject();
            System.out.println("Server: Confirmed Drone Data");
            
            // Writes that there's 0 fires right now to implement and receives confirmation
            out.writeObject(0);
            serverMessage = (String)in.readObject();
            System.out.println("Server: Confirmed Number of Fires Data");
            
            serverMessage = (String)in.readObject();
            // Checks if the message was a recall, acts accordingly
            if (serverMessage.equals("recall")) {
                System.out.println("Recall Initiated");
                // Confirmation Message to Server
                message = "Recall Confirmed";
                out.writeObject(message);
                // Closes connection
                s.close();
                
                // Exits program immediately
                System.exit(0);
            
            } else if (serverMessage.equals("move")) {
                // If the server asks for the drone to move, receive movement locations, send confirmations between
                message = "Move confirmed";
                out.writeObject(message);
                int newX_pos = (Integer)in.readObject();
                out.writeObject(message);
                int newY_pos = (Integer)in.readObject();
                out.writeObject(message);
                
                // Sets new drone coordinates
                x_pos = newX_pos;
                y_pos = newY_pos;
            // If the server confirms the input, just confirms it in commandline
            } else if (serverMessage.equals("confirmed")) {
                System.out.println("Server: Confirmed Everything\n");
            }
            
            
            
        } catch (UnknownHostException e){System.out.println("Socket:"+e.getMessage());
	} catch (EOFException e){System.out.println("EOF:"+e.getMessage());
	} catch (IOException e){System.out.println("readline:"+e.getMessage());
        } catch(ClassNotFoundException ex){ ex.printStackTrace();
	} finally {if(s!=null) try {s.close();}catch (IOException e){System.out.println("close:"+e.getMessage());}}
        
        // Create Thread
        Drone thread = new Drone();
        thread.start();
        
        // Facilitates Drone movements
        while (true) {
            // If drone needs to be recalled, stops movement by breaking loop
            if (recallStatus) {
                break;
            }
            
            // Sleeps thread for 2 seconds
            Thread.sleep(2000);
            
            // The drone will move in the same direction for 10 movements before changing direction
            if (movements == 1) {
                // Chooses a direction to start moving in
                direction = rand.nextInt(4);
            } else if (movements == 10) {
                // Will Reset movements back to 0
                movements = 0;
            }
            
            // Moves drone randomly in direction between 0 and 3 coordinates
            switch (direction) {
                case 0:
                    x_pos += rand.nextInt(4);
                    y_pos += rand.nextInt(4);
                    break;
                case 1:
                    x_pos += rand.nextInt(4);
                    y_pos -= rand.nextInt(4);
                    break;
                case 2: 
                    x_pos -= rand.nextInt(4);
                    y_pos += rand.nextInt(4);
                    break;
                case 3:
                    x_pos -= rand.nextInt(4);
                    y_pos -= rand.nextInt(4);
                    break;
            }
            
            // Increases movements counter
            movements++;
            
            // Sets drone object's positions to new ones
            drone.setX_pos(x_pos);
            drone.setY_pos(y_pos);
            
            // Makes random number up to 100, if the number is 1 reports that there's a fire at the position
            int fireRand = rand.nextInt(100);
            if (fireRand == 1) {
                int fireSeverity = rand.nextInt(9) + 1;
                System.out.println("Fire with Severity " + fireSeverity + " spotted at " + x_pos + ", " + y_pos);
                FireDetails fire = new FireDetails(0, x_pos, y_pos, id, fireSeverity);
                fires.add(fire);
            }
        }
            
    }
    
    DroneDetails returnDrone() {
        return drone;
    }
    
    public void moveDrone(int newX_pos, int newY_pos) {
        // This function is called if the drone is moved
        // Resets movement so a new direction will be chosen and updates coordinates based on move
        movements = 1;
        x_pos = newX_pos;
        y_pos = newY_pos;
    }
    
    @Override
    public void run() {
        // Connect to server every 10 seconds
        
        Socket s = null;
        String hostName = "localhost";
        String serverMessage = "";
        String message = "";
        DroneDetails drone;
        
        while (true) {
            try {
                // Sleeps thread for 10 seconds before executing further code
                Thread.sleep(10000);
            } catch (InterruptedException ex) {
                Logger.getLogger(Drone.class.getName()).log(Level.SEVERE, null, ex);
            }
            // Connects to Server Here
            try {
            int serverPort = 8888;
            
            s = new Socket(hostName, serverPort);
            
            ObjectInputStream in = null;
            ObjectOutputStream out = null;
			
            out = new ObjectOutputStream(s.getOutputStream());
            in = new ObjectInputStream(s.getInputStream());
            
            // Gets drone object from returnDrone function then writes it to server and confirms
            drone = returnDrone();
            out.writeObject(drone);
            serverMessage = (String)in.readObject();
            System.out.println("Server: Confirmed Drone Data");
            
            // Sends number of fires
            Integer numFires = Drone.fires.size();
            out.writeObject(numFires);
            serverMessage = (String)in.readObject();
            System.out.println("Server: Confirmed Number of Fires Data");
            
            // If there's fires it'll loop sending the fire objects one at a time
            // Waits for confirmation message to send next fire
            if (numFires > 0) {
                for (FireDetails p : Drone.fires) {
                    out.writeObject(p);
                    serverMessage = (String)in.readObject();
                }
                // Clears arraylist so fires aren't resent
                Drone.fires.clear();
            }
            
            // Reads server String response, says if recall or confirmed
            serverMessage = (String)in.readObject();
            
            // Checks if the message was a recall, acts accordingly
            if (serverMessage.equals("recall")) {
                System.out.println("Server: Recall Initiated");
                // Sends recall confirmation to server
                message = "Recall Confirmed";
                out.writeObject(message);
                Drone.recallStatus = true;
                
                // Closes connection
                s.close();
                break;
                
            } else if (serverMessage.equals("move")) {
                // If the server asks for the drone to move, receive movement locations, send confirmations between
                message = "Move confirmed";
                out.writeObject(message);
                int newX_pos = (Integer)in.readObject();
                out.writeObject(message);
                int newY_pos = (Integer)in.readObject();
                out.writeObject(message);
                // Calls function to move drone to new location
                moveDrone(newX_pos, newY_pos);
            } else if (serverMessage.equals("confirmed")) {
                // If the server confirms the input, just confirms it in commandline
                System.out.println("Server: Confirmed Everything\n");
            }
            
            // Closes socket
            s.close();
            
            } catch (UnknownHostException e){System.out.println("Socket:"+e.getMessage());
            } catch (EOFException e){System.out.println("EOF:"+e.getMessage());
            } catch (IOException e){System.out.println("readline:"+e.getMessage());
            } catch(ClassNotFoundException ex){ ex.printStackTrace();
            } finally {if(s!=null) try {s.close();}catch (IOException e){System.out.println("close:"+e.getMessage());}}
        }
    } 
}