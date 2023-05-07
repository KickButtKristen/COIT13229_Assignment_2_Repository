/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.assignment1;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Kristen
 */

class Connector extends Thread {
    // Sets up input and output streams for socket
    ObjectInputStream in;
    ObjectOutputStream out;
    Socket clientSocket;
    
    public Connector (Socket aClientSocket) {
        
        // Assigns streams to the socket and starts the thread run()
        try {
            clientSocket = aClientSocket;
            in = new ObjectInputStream( clientSocket.getInputStream());
            out =new ObjectOutputStream( clientSocket.getOutputStream());
            this.start();
	} catch(IOException e) {System.out.println("Connection:"+e.getMessage());}
    }
    
    @Override
    public void run() {
        try {
            
            String message = "";
            String clientMessage = "";
            
            // Movement variables if drone is outside of boundaries
            // New positions will be set if required
            boolean outOfBounds = false;
            boolean movementRequired = false;
            
            // Gets drone object from client and adds it to tempDrone object
            DroneDetails tempDrone = (DroneDetails)in.readObject();
            
            // Confirm drone object
            message = "confirmed";
            out.writeObject(message);
            
            // Receives how many fires there are and confirms receival
            Integer numFires = (Integer)in.readObject();
            out.writeObject(message);
            
            // Loops for how many fires there are and receives the fire objects
            // Sends fire object to addFire(); for it to be added, sends confirmation message
            if (numFires > 0) {
                for (int i = 0; i < numFires; i++) {
                    FireDetails tempFire = (FireDetails)in.readObject();
                    Server.addFire(tempFire);
                    message = "confirmed";
                    out.writeObject(message);
                }
            }
            
            // Checks if drone is in hashmaps for movements
            // If so sets movementRequired to true, updates drone X and Y positions
            for (Integer i : Server.newXPositions.keySet()) {
                if (i == tempDrone.getId()) {
                    movementRequired = true;
                    tempDrone.setXpos(Server.newXPositions.get(i));
                    Server.newXPositions.remove(i);
                }
            }
            
            for (Integer i : Server.newYPositions.keySet()) {
                if (i == tempDrone.getId()) {
                    movementRequired = true;
                    tempDrone.setYpos(Server.newYPositions.get(i));
                    Server.newYPositions.remove(i);
                }
            }
            
            // Check x positions, set if out of bounds
            if (tempDrone.getXpos() > 100) {
                outOfBounds = true;
                tempDrone.setXpos(80);
            } else if (tempDrone.getXpos() < -100) {
                outOfBounds = true;
                tempDrone.setXpos(-80);
            }
            
            // Check y positions, set if out of bounds
            if (tempDrone.getYpos() > 100) {
                outOfBounds = true;
                tempDrone.setYpos(80);
            } else if (tempDrone.getYpos() < -100) {
                outOfBounds = true;
                tempDrone.setYpos(-80);
            }
            
            // If a Recall is active it will respond to the client saying so
            // Recall is done first since it matters the most, position doesn't matter if it's being recalled to 0,0 regardless
            if (Server.ifRecall()) {
                message = "recall";
                out.writeObject(message);
                clientMessage = (String)in.readObject();
                if (clientMessage.equals("Recall Confirmed")) {
                    // If drone confirms recall, set the drone active to false
                    tempDrone.setActive(false);
                    tempDrone.setXpos(0);
                    tempDrone.setYpos(0);
                }
            } else if (movementRequired || outOfBounds) {
                // Sends move message and receives confirmation between object writes
                message = "move";
                out.writeObject(message);
                clientMessage = (String)in.readObject();
                out.writeObject(tempDrone.getXpos());
                clientMessage = (String)in.readObject();
                out.writeObject(tempDrone.getYpos());
                clientMessage = (String)in.readObject();
                
                // Messages outputed based on if the drone was moved or out of bounds
                // Not an if else because both messages could be required
                if (movementRequired) {
                    Server.outputLog("Drone " + tempDrone.getId() + " successfully moved.");
                }
                
                if (outOfBounds) {
                    Server.outputLog("Drone " + tempDrone.getId() + " outside of boundaries. Moved back.");
                }
            } else {
                // Otherwise just confirms to the client it received the object
                message = "confirmed";
                out.writeObject(message);
            }
            
            // Sends tempDrone to the addDrone function to get it in the ArrayList
            Server.addDrone(tempDrone);
            
            System.out.println(tempDrone);
            
            System.out.println("There are " + numFires + " new fires.");
            System.out.println("There are " + Server.fires.size() + " fires.");
            
        }catch (EOFException e){System.out.println("EOF:"+e.getMessage());
        } catch(IOException e) {System.out.println("readline:"+e.getMessage());
	} catch(ClassNotFoundException ex){ ex.printStackTrace();
	} catch (SQLException ex) {
            Logger.getLogger(Connector.class.getName()).log(Level.SEVERE, null, ex);
        } finally{ try {clientSocket.close();}catch (IOException e){/*close failed*/}}
    }
}