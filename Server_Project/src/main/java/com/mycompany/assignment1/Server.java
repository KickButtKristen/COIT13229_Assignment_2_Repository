
package com.mycompany.assignment1;

import java.awt.*;
import java.net.*;
import java.io.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import javax.swing.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.mycompany.assignment1.Connector;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.*;

public class Server extends JFrame implements ActionListener, Runnable {
    
    // If recall has been called
    static boolean recallStatus = false;
    
    // ArrayLists for Drone and Fire Objects
    static ArrayList<DroneDetails> drones = new ArrayList<>();
    static ArrayList<FireDetails> fires = new ArrayList<>();
    static ArrayList<FiretruckDetails> fireTrucks = new ArrayList<>();
    
    // GUI Setup, all elements of GUI declared
    private JLabel titleText = new JLabel("Drone Server");
    private static JTextArea outputText = new JTextArea(25, 25);
    private JLabel headingText = new JLabel("Server Output Log");
    private JLabel mapText = new JLabel("Drone and Fire Map");
    private JLabel buttonText = new JLabel("Admin Controls");
    private JButton deleteButton = new JButton("Delete Fire");
    private JButton recallButton = new JButton("Recall Drones");
    private JButton moveButton = new JButton("Move Drone");
    private JButton sendFireTruckButton = new JButton("Send Firetruck");
    private JButton shutDownButton = new JButton("Shut Down");
    private JScrollPane scrollPane; // Scroll pane for the text area
    private MapPanel mapPanel;
    private Timer timer;
    
    // Hash Maps to store positions of drones that need to be moved
    static HashMap<Integer, Integer> newXPositions = new HashMap<>();
    static HashMap<Integer, Integer> newYPositions = new HashMap<>();
    
    public class MapPanel extends JPanel {

        private ArrayList<DroneDetails> drones;
        private ArrayList<FireDetails> fires;
        private ArrayList<FiretruckDetails> fireTrucks;

        public MapPanel(ArrayList<DroneDetails> drones, ArrayList<FireDetails> fires, ArrayList<FiretruckDetails> fireTrucks) {
            this.drones = drones;
            this.fires = fires;
            this.fireTrucks = new ArrayList<>();

            timer = new Timer(10000, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // Repaint the panel
                    repaint();
                }
            });
            timer.start();

            ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

            // Get the initial list of firetrucks
            this.fireTrucks = getFireTrucksFromDatabase();

            // Set up a task to run every 10 seconds
            executorService.scheduleAtFixedRate(() -> {
                ArrayList<FiretruckDetails> updatedFireTrucks = getFireTrucksFromDatabase();

                // Check if the list of firetrucks has changed
                if (!updatedFireTrucks.equals(this.fireTrucks)) {
                    this.fireTrucks = updatedFireTrucks;

                    // Repaint the panel if the list of firetrucks has changed
                    repaint();
                }
            }, 0, 10, TimeUnit.SECONDS);

            // New task to check if firetruck has been assigned to a fire for more than 5 seconds
            executorService.scheduleAtFixedRate(() -> {
                long currentTime = System.currentTimeMillis();
                List<FiretruckDetails> trucksToRemove = new ArrayList<>();
                for (FiretruckDetails truck : fireTrucks) {
                    for (FireDetails fire : fires) {
                        if (fire.isActive() && truck.getDesignatedFireId() == fire.getId() && currentTime - truck.getActivationTime() > 5000) {
                            fire.setActive(false);
                            updateFireIsActive(fire.getId(), false);
                            // Add firetruck to the list of trucks to be removed
                            trucksToRemove.add(truck);

                            // Output a message saying the fire was extinguished
                            System.out.println("Fire " + fire.getId() + " was extinguished by firetruck " + truck.getId() + ", this fire is now classified as a historical fire.");
                        }
                    }
                }

                // Remove firetrucks that have extinguished their fires
                for (FiretruckDetails truck : trucksToRemove) {
                    fireTrucks.remove(truck);
                    removeFireTruckFromDatabase(truck);
                }
                if (!trucksToRemove.isEmpty()) {
                    repaint();
                }
            }, 0, 1, TimeUnit.SECONDS);
        }
        
        

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            // Set background color of map panel
            setBackground(Color.WHITE);

            // Fetch drones, fires and fireTrucks from the database
            ArrayList<DroneDetails> drones = getDronesFromDatabase();
            ArrayList<FireDetails> fires = getFiresFromDatabase();
            ArrayList<FiretruckDetails> fireTrucks = getFireTrucksFromDatabase();

            // Draw drones as blue circles with drone id
            for (DroneDetails p : drones) {
                
                    // Converts coordinates for use on 400 by 400 grid
                    int x = (100 - p.getXpos()) * 2;
                    int y = (100 - p.getYpos()) * 2;
                    int size = 10;
                    g.setColor(Color.BLUE);
                    g.fillOval(x - size/2, y - size/2, size, size);
                    g.setColor(Color.BLACK);
                    g.drawString("Drone " + p.getId(), x - 30, y - 5);
                
            }

            // Draw fires as red circles with fire id and severity
            for (FireDetails p : fires) {
                if (p.isActive()) {
                    // Converts coordinates for use on 400 by 400 grid
                    int x = (100 - p.getXpos()) * 2;
                    int y = (100 - p.getYpos()) * 2;
                    int intensity = p.getIntensity();
                    int size = (int) (p.getBurningAreaRadius() * 3); 
                    g.setColor(Color.RED);
                    g.fillOval(x - size/2, y - size/2, size, size);
                    g.setColor(Color.BLACK);
                    g.drawString("Fire " + p.getId() + " (" + intensity + ")", x - 30, y - 5);
                }
            }
            
           

            // Draw firetrucks as green circles at the fire location
            for (FiretruckDetails truck : fireTrucks) {
                for (FireDetails fire : fires) {
                    if (fire.getId() == truck.getDesignatedFireId()) {
                        // Converts coordinates for use on 400 by 400 grid
                        int x = (100 - fire.getXpos()) * 2;
                        int y = (100 - fire.getYpos()) * 2;
                        int size = 10;
                        g.setColor(Color.GREEN);
                        
                        // Calculate new position for the fire truck, adding the radius of the fire to it
                        int truckX = x + (int)fire.getBurningAreaRadius();
                        int truckY = y + (int)fire.getBurningAreaRadius();

                        g.fillOval(truckX - size/2, truckY - size/2, size, size);
                        g.setColor(Color.BLACK);
                        g.drawString("Truck " + truck.getId(), truckX - 30, truckY - 5);
                    }
                }
            }
            
            
        }
        
    
    
    }
    
    Server() {
        
        
        // Sets settings for java swing GUI Frame
        super("Server GUI");
        
        // Sets font for title
        titleText.setFont(new Font("Arial", Font.PLAIN, 30));
        
        // Sets X button to do nothing, shut down should be used to exit
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        
        // Other GUI settings
        setSize(750, 600);
        this.setLayout(new FlowLayout());
        this.setResizable(false);
        
        // Heading Panel
        JPanel headingPanel = new JPanel();
        headingPanel.setPreferredSize(new Dimension(750, 40));
        headingPanel.add(titleText);
        
        // Set Text Area Wrapping and read-only
        outputText.setEditable(false);
        outputText.setLineWrap(true);
        outputText.setWrapStyleWord(true);
        
        // Button Panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setPreferredSize(new Dimension(750, 40));
        buttonPanel.add(deleteButton);
        buttonPanel.add(recallButton);
        buttonPanel.add(moveButton);
        buttonPanel.add(sendFireTruckButton);
        buttonPanel.add(shutDownButton);
        
        
        // Bottom panel
        JPanel bottomPanel = new JPanel();
        
        // Output Panel
        JPanel outputPanel = new JPanel();
        outputPanel.setPreferredSize(new Dimension(300, 500));
        outputPanel.add(headingText);
        outputPanel.add(outputText);
        
        // Text Area Vertical ScrollBar
        scrollPane = new JScrollPane(outputText);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        outputPanel.add(scrollPane);
        
         // Map Panel
        mapPanel = new MapPanel(drones, fires, fireTrucks);
        mapPanel.setPreferredSize(new Dimension(400, 400));
        
        // Outer Map Panel with text
        JPanel outerMapPanel = new JPanel();
        outerMapPanel.setPreferredSize(new Dimension(400, 500));
        
        // Add panels and text to GUI
        add(headingPanel);
        add(buttonText);
        add(buttonPanel);
        
        outerMapPanel.add(mapText);
        outerMapPanel.add(mapPanel);
        bottomPanel.add(outputPanel);
        bottomPanel.add(outerMapPanel);
        
        add(bottomPanel);
        
        // Makes the GUI visible
        this.setVisible(true);
        
        // Action Listeners for Buttons
        deleteButton.addActionListener(this);
        recallButton.addActionListener(this);
        moveButton.addActionListener(this);
        shutDownButton.addActionListener(this);
        
        sendFireTruckButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendFireTruck();
            }
        });
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        // This runs when an object action is clicked
        // Gets the name of the object clicked and finds the case
        // Runs the corresponding method and breaks the switch
        String actionString=e.getActionCommand();
        switch(actionString) {
            case "Delete Fire":
                deleteFire();
                break;
                
            case "Recall Drones":
                recallDrones();
                break;
                
            case "Move Drone":
                moveDrone();
                break;
                
            case "Shut Down":
                shutDown();
                break;
        }
    }
    
    
    
    public static void main(String[] args) {
        // Calls function to read data from files
        loadDroneData();
        loadFireData();
        
        // Starts thread to update map and GUI because that's how it works apparently
        Server obj = new Server();
        Thread thread = new Thread(obj);
        thread.start();
        
        // Sets up connection listener with port 8888
        try {
            int serverPort = 8888;
            ServerSocket listenSocket = new ServerSocket(serverPort);
            
            // Constantly on loop, checks for connections and sends connections to new thread
            while(true) {
                Socket clientSocket = listenSocket.accept();
                Connector c = new Connector(clientSocket);
            }
            
        }   catch(IOException e) {System.out.println("Listen Socket : " + e.getMessage());}
    }
    
    private static Connection connectToDatabase() {
        Connection conn = null;
        String url = "jdbc:mysql://localhost:3306/ibdms_server?zeroDateTimeBehavior=CONVERT_TO_NULL";
        String username = "test";
        String password = "test";

        try {
            conn = DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            System.out.println("Error connecting to the database: " + e.getMessage());
        }

        return conn;
    }
    
    static boolean ifRecall() {
        // Returns if the recall status is true
        return recallStatus;
    }
    
    //Method to Add a new drone into the database if it is not already existing (compared by  Id of the drone)
    static void addDrone(DroneDetails tempDrone) {
        // Add your code to connect to the database and insert or update drone information
        String insertDrone = "INSERT INTO drone (id, name, xpos, ypos) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE name = ?, xpos = ?, ypos = ?";
        try (Connection conn = connectToDatabase();
             PreparedStatement pstmt = conn.prepareStatement(insertDrone)) {
            pstmt.setInt(1, tempDrone.getId());
            pstmt.setString(2, tempDrone.getName());
            pstmt.setInt(3, tempDrone.getXpos());
            pstmt.setInt(4, tempDrone.getYpos());
            pstmt.setString(5, tempDrone.getName());
            pstmt.setInt(6, tempDrone.getXpos());
            pstmt.setInt(7, tempDrone.getYpos());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    //Method to Add a new fire report into the fire table if it is not already existing (compared by Id of the fire)
    static void addFire(FireDetails tempFire) {
        String insertFire = "INSERT IGNORE INTO fire (id, isActive, intensity, burningAreaRadius, xpos, ypos) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = connectToDatabase();
             PreparedStatement pstmt = conn.prepareStatement(insertFire)) {

            int id = tempFire.getId();

            // Check if a fire with the same ID already exists in the database
            if (fireExists(id)) {
                id = generateUniqueFireId();  // Generate a new unique ID for the fire
            }

            pstmt.setInt(1, id);
            pstmt.setBoolean(2, tempFire.isActive());
            pstmt.setInt(3, tempFire.getIntensity());
            pstmt.setDouble(4, tempFire.getBurningAreaRadius());
            pstmt.setInt(5, tempFire.getXpos());
            pstmt.setInt(6, tempFire.getYpos());

            pstmt.executeUpdate();

            tempFire.setId(id);
            pstmt.setBoolean(2, true);
            loadFireData();

        } catch (SQLIntegrityConstraintViolationException e) {
            outputLog("Fire " + tempFire.getId() + " already exists in the database.");
        } catch (SQLException e) {
            outputLog("Error adding fire to the database: " + e.getMessage());
        }
    }
    
    //Method to add a new fire truck response to the firetrucks table in the database (Compared by firetruck Id)
    static void addFireTruck(FiretruckDetails fireTruck) {
        String insertFireTruck = "INSERT INTO firetrucks (id, name, designatedFireId) VALUES (?, ?, ?)";
        try (Connection conn = connectToDatabase();
            PreparedStatement pstmt = conn.prepareStatement(insertFireTruck)) {

            pstmt.setInt(1, fireTruck.getId());
            pstmt.setString(2, fireTruck.getName());
            pstmt.setInt(3, fireTruck.getDesignatedFireId());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            outputLog("Error adding fire truck to the database: " + e.getMessage());
        }
    }
    
    static void removeFireTruckFromDatabase(FiretruckDetails fireTruck) {
        String deleteFireTruck = "DELETE FROM firetrucks WHERE id = ?";
        try (Connection conn = connectToDatabase();
             PreparedStatement pstmt = conn.prepareStatement(deleteFireTruck)) {

            pstmt.setInt(1, fireTruck.getId());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            outputLog("Error removing fire truck from the database: " + e.getMessage());
        }
    }
    
    
    //Method to check if the fire report already exists in the database 
    private static boolean fireExists(int id) {
        String query = "SELECT * FROM fire WHERE id = ?";
        try (Connection conn = connectToDatabase();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            outputLog("Error checking if fire exists in the database: " + e.getMessage());
        }
        return false;
    }

    //Method to give the fire reports their own unique fire report Id so that there is no overlap when trying to identify that report
    private static int generateUniqueFireId() {
        int id = 1;
        while (fireExists(id)) {
            id++;
        }
        return id;
    }
    
    
    
    //ArrayList to store the DroneDetails that are received from the database. 
    private ArrayList<DroneDetails> getDronesFromDatabase() {
        ArrayList<DroneDetails> drones = new ArrayList<>();

        // Connect to the database using the existing connectToDatabase() method
        try (Connection con = connectToDatabase()) {
            if (con != null) {
                // Execute the query
                Statement stmt = con.createStatement();
                String query = "SELECT * FROM drone";
                ResultSet rs = stmt.executeQuery(query);

                // Iterate through the result set and add the drone data to the drones list
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String name = rs.getString("name");
                    int xpos = rs.getInt("xpos");
                    int ypos = rs.getInt("ypos");

                    DroneDetails drone = new DroneDetails(id, name, xpos, ypos);
                    drones.add(drone);
                }

                // Close the resources
                rs.close();
                stmt.close();
            }
        } catch (SQLException e) {
            System.out.println("Error fetching drone data: " + e.getMessage());
        }

        return drones;
    }

    private ArrayList<FireDetails> getFiresFromDatabase() {
        ArrayList<FireDetails> fires = new ArrayList<>();

        // Connect to the database using the existing connectToDatabase() method
        try (Connection con = connectToDatabase()) {
            if (con != null) {
                // Execute the query
                Statement stmt = con.createStatement();
                String query = "SELECT * FROM fire";
                ResultSet rs = stmt.executeQuery(query);

                // Iterate through the result set and add the fire data to the fires list
                while (rs.next()) {
                    int id = rs.getInt("id");
                    boolean isActive = rs.getBoolean("isActive");
                    int intensity = rs.getInt("intensity");
                    double burningAreaRadius = rs.getDouble("burningAreaRadius");
                    int xpos = rs.getInt("xpos");
                    int ypos = rs.getInt("ypos");

                    FireDetails fire = new FireDetails(id, isActive, intensity, burningAreaRadius, xpos, ypos);
                    fires.add(fire);
                }

                // Close the resources
                rs.close();
                stmt.close();
            }
        } catch (SQLException e) {
            System.out.println("Error fetching fire data: " + e.getMessage());
        }

        return fires;
    }
    
    private ArrayList<FiretruckDetails> getFireTrucksFromDatabase() {
        ArrayList<FiretruckDetails> fireTrucks = new ArrayList<>();

        try (Connection con = connectToDatabase()) {
            if (con != null) {
                Statement stmt = con.createStatement();
                String query = "SELECT id, name, designatedFireId FROM firetrucks";
                ResultSet rs = stmt.executeQuery(query);

                while (rs.next()) {
                    int id = rs.getInt("id");
                    String name = rs.getString("name");
                    int designatedFireId = rs.getInt("designatedFireId");

                    FiretruckDetails fireTruck = new FiretruckDetails(id, name, designatedFireId);
                    fireTrucks.add(fireTruck);
                }

                rs.close();
                stmt.close();
            }
        } catch (SQLException e) {
            System.out.println("Error fetching fire truck data: " + e.getMessage());
        }

        return fireTrucks;
    }


    
    static void loadDroneData() {
        String query = "SELECT * FROM drone";

        try (Connection connection = connectToDatabase();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                int xpos = resultSet.getInt("xpos");
                int ypos = resultSet.getInt("ypos");

                DroneDetails drone = new DroneDetails(id, name, xpos, ypos);
                drones.add(drone);
            }

            outputLog(drones.size() + " drones loaded.");

        } catch (SQLException e) {
            System.out.println("Error loading drone data: " + e.getMessage());
        }
    }
    
    static void loadFireData() {
        String query = "SELECT * FROM fire";

        try (Connection connection = connectToDatabase();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                boolean isActive = resultSet.getBoolean("isActive");
                int intensity = resultSet.getInt("intensity");
                double burningAreaRadius = resultSet.getDouble("burningAreaRadius");
                int xpos = resultSet.getInt("xpos");
                int ypos = resultSet.getInt("ypos");

                FireDetails fire = new FireDetails(id, isActive, intensity, burningAreaRadius, xpos, ypos);
                fires.add(fire);
            }

            outputLog(fires.size() + " fires loaded.");

        } catch (SQLException e) {
            System.out.println("Error loading fire data: " + e.getMessage());
        }
    }
    
    static void initialDronePosition(ObjectOutputStream out, DroneDetails currentDrone) {
        String query = "SELECT * FROM drone WHERE id = ?";
        boolean droneFound = false;

        try (Connection connection = connectToDatabase();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, currentDrone.getId());
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                int xpos = resultSet.getInt("xpos");
                int ypos = resultSet.getInt("ypos");

                out.writeObject(xpos);
                out.writeObject(ypos);

                droneFound = true;
            }

            if (!droneFound) {
                out.writeObject(0);
                out.writeObject(0);
            }

        } catch (SQLException e) {
            System.out.println("Error getting drone initial position: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Error sending drone initial position: " + e.getMessage());
        }
    }
    
    private void updateFireIsActive(int fireId, boolean isActive) {
        try (Connection con = connectToDatabase()) {
            if (con != null) {
                String query = "UPDATE fire SET isActive = ? WHERE id = ?";
                PreparedStatement pstmt = con.prepareStatement(query);
                pstmt.setBoolean(1, isActive);
                pstmt.setInt(2, fireId);
                pstmt.executeUpdate();
                pstmt.close();
            }
        } catch (SQLException e) {
            System.out.println("Error updating fire active status: " + e.getMessage());
        }
    }
    
    public void deleteFire() {
        // Triggered by Delete Fire Button
        // intId is the id that'll be entered
        int intId = -1;

        /*
        Opens Option Pane prompting for a Fire ID
        If cancel is pressed, null will be returned causing the loop to break
        otherwise it'll attempt to parse the ID to int, if this fails the user will be reprompted after an error message
        */
        while (true) {
            String enteredId = JOptionPane.showInputDialog(null, "Enter a Fire ID");
            if (enteredId == null) {
                return;
            }
            try {
                intId = Integer.parseInt(enteredId);
                break;
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "ID must be numerical.");
            }
        }

        try {
            // Create a new database connection
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/ibdms_server?useSSL=false", "test", "test");

            // Create a statement to delete the fire with the entered ID from the fire table
            String query = "DELETE FROM fire WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, intId);
            int numRowsAffected = stmt.executeUpdate();

            if (numRowsAffected > 0) {
                outputLog("Fire " + intId + " removed.");

                // Remove fire trucks assigned to this fire
                String queryTruck = "DELETE FROM firetrucks WHERE designatedFireId = ?";
                PreparedStatement stmtTruck = conn.prepareStatement(queryTruck);
                stmtTruck.setInt(1, intId);
                int trucksAffected = stmtTruck.executeUpdate();

                if (trucksAffected > 0) {
                    outputLog("Firetrucks assigned to Fire " + intId + " removed.");
                }

            } else {
                outputLog("Fire " + intId + " not found.");
            }

            // Close the database connection
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "An error occurred while deleting the fire.");
        }
    }
    
    
    public void sendFireTruck() {
        // Triggered by Send Fire Truck Button
        // intId is the id that'll be entered
        int intId = -1;

        /*
        Opens Option Pane prompting for a Fire ID
        If cancel is pressed, null will be returned causing the loop to break
        otherwise it'll attempt to parse the ID to int, if this fails the user will be reprompted after an error message
        */
        while (true) {
            String enteredId = JOptionPane.showInputDialog(null, "Enter the Fire ID to Extinguish");
            if (enteredId == null) {
                return;
            }
            try {
                intId = Integer.parseInt(enteredId);
                break;
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "ID must be numerical.");
            }
        }

        // Find the fire with the given ID
        for (FireDetails fire : fires) {
            if (fire.getId() == intId) {
                // Create a new FireTruckDetails object and add it to the fireTrucks ArrayList
                FiretruckDetails newTruck = new FiretruckDetails(fireTrucks.size() + 1, "FireTruck " + (fireTrucks.size() + 1), intId);

                // Set the activation time to the current time
                newTruck.setActivationTime(System.currentTimeMillis());

                fireTrucks.add(newTruck);
                addFireTruck(newTruck);
                // Repaint the panel to show the new fire truck
                repaint();
                return;
            }
        }

        // If we get here, the fire with the given ID was not found
        JOptionPane.showMessageDialog(null, "Fire with ID " + intId + " not found.");
    }
    
    
    static boolean droneByIdCheck(int droneId) {
        String query = "SELECT id FROM drone WHERE id = ?";

        try (Connection connection = connectToDatabase();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, droneId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                // the drone id exists in the database
                return true;
            }

        } catch (SQLException e) {
            System.out.println("Error checking drone existence: " + e.getMessage());
        }

        // the drone id was not found in the database
        return false;
    }
    
    
    
    
    public void recallDrones() {
        // Checks if a recall is initiated
        if (recallStatus) {
            recallStatus = false;
            outputLog("Recall uninitiated.");
        } else {
            recallStatus = true;
            outputLog("Recall initiated.");
        }
    }
    
    public void moveDrone() {
        // Initialization of variables
        int intId = -1;
        int newX = -0;
        int newY = -0;
        boolean droneCheck;

        // Get drone id
            String strId = JOptionPane.showInputDialog("Enter the drone ID:");
            try {
                intId = Integer.parseInt(strId);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Invalid drone ID. Please enter a valid integer.");
                return;
            }

        // Check if the drone with the given ID exists
        droneCheck = droneByIdCheck(intId);

        // Get new X position from the user
        String strNewX = JOptionPane.showInputDialog("Enter the new X position:");
        try {
            newX = Integer.parseInt(strNewX);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Invalid X position. Please enter a valid integer.");
            return;
        }

        // Get new Y position from the user
        String strNewY = JOptionPane.showInputDialog("Enter the new Y position:");
        try {
            newY = Integer.parseInt(strNewY);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Invalid Y position. Please enter a valid integer.");
            return;
        }

        if (droneCheck) {
            try {
                // Create a new database connection
                Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/ibdms_server?useSSL=false", "test", "test");

                // Get new X and Y positions
                // ...

                // Update the drone's position in the database
                String updateQuery = "UPDATE drone SET xpos = ?, ypos = ? WHERE id = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                updateStmt.setInt(1, newX);
                updateStmt.setInt(2, newY);
                updateStmt.setInt(3, intId);
                updateStmt.executeUpdate();

                // Outputs message to confirm move
                outputLog("Drone " + intId + " will be moved to " + newX + ", " + newY + ".");

                // Close the resources
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "An error occurred while moving the drone.");
            }
        } else {
            System.out.println("Drone with id " + intId + " does not exist.");
        }
    }
    
    public void shutDown() {
        /*
        Sets recall status to true
        drones active is set to false before each loop
        Checks each object of the ArrayList to see if a drone is still active
        If one is, dronesActive is set to true
        
        If dronesActive is false that means there's no drones active
        The program saves that data (saveData()) and exits
        
        If there is a drone still active it will loop until no drones are active
        */
        recallStatus = true;
        boolean dronesActive;
        
        outputLog("Recall Intiated.");
        
        while (true) {
            dronesActive = false;
            for (DroneDetails p : drones) {
                if (p.getActive()) {
                    dronesActive = true;
                }
            }
            
            if (!dronesActive) {
                outputLog("Shut Down Commencing.");
                //saveData();
                System.exit(0);
            }
        }
    }
    
    public static void outputLog(String message) {
        // Outputs message given through the output text area along with a newline
        outputText.append(message + "\n");
        // Moves scrollbar straight to bottom to make textarea act as a log
        outputText.setCaretPosition(outputText.getDocument().getLength());
    }

    @Override
    public void run() {
        // Runs constantly
        while (true) {
            
            // Repaints mapPanel
            mapPanel.repaint();
        }
    }
}

