
package com.mycompany.assignment1;

import java.awt.*;
import java.net.*;
import java.io.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
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

public class Server extends JFrame implements ActionListener, Runnable {
    
    // If recall has been called
    static boolean recallStatus = false;
    
    // ArrayLists for Drone and Fire Objects
    static ArrayList<DroneDetails> drones = new ArrayList<>();
    static ArrayList<FireDetails> fires = new ArrayList<>();
    
    // GUI Setup, all elements of GUI declared
    private JLabel titleText = new JLabel("Drone Server");
    private static JTextArea outputText = new JTextArea(25, 25);
    private JLabel headingText = new JLabel("Server Output Log");
    private JLabel mapText = new JLabel("Drone and Fire Map");
    private JLabel buttonText = new JLabel("Admin Controls");
    private JButton deleteButton = new JButton("Delete Fire");
    private JButton recallButton = new JButton("Recall Drones");
    private JButton moveButton = new JButton("Move Drone");
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

        public MapPanel(ArrayList<DroneDetails> drones, ArrayList<FireDetails> fires) {
            this.drones = drones;
            this.fires = fires;
            
            timer = new Timer(10000, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // Repaint the panel
                    repaint();
                }
            });
            timer.start();
            
        }
        
        

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            // Set background color of map panel
            setBackground(Color.WHITE);

            // Fetch drones and fires from the database
            ArrayList<DroneDetails> drones = getDronesFromDatabase();
            ArrayList<FireDetails> fires = getFiresFromDatabase();

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
        mapPanel = new MapPanel(drones, fires);
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
        Connection connection = null;
        String url = "jdbc:mysql://localhost:3306/ibdms_server?zeroDateTimeBehavior=CONVERT_TO_NULL";
        String username = "test";
        String password = "test";

        try {
            connection = DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            System.out.println("Error connecting to the database: " + e.getMessage());
        }

        return connection;
    }
    
    static boolean ifRecall() {
        // Returns if the recall status is true
        return recallStatus;
    }
    
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
    
    static void addFire(FireDetails tempFire) throws SQLException {
        // Add your code to connect to the database and insert fire information
        String insertFire = "INSERT IGNORE INTO fire (id, isActive, intensity, burningAreaRadius, xpos, ypos) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = connectToDatabase();
             PreparedStatement pstmt = conn.prepareStatement(insertFire, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, tempFire.getId());
            pstmt.setBoolean(2, tempFire.isActive());
            pstmt.setInt(3, tempFire.getIntensity());
            pstmt.setDouble(4, tempFire.getBurningAreaRadius());
            pstmt.setInt(5, tempFire.getXpos());
            pstmt.setInt(6, tempFire.getYpos());

            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    tempFire.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating fire failed, no ID obtained.");
                }
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            outputLog("Fire " + tempFire.getId() + " already exists in the database.");
        }
    }
    
    private ArrayList getDronesFromDatabase() {
        ArrayList drones = new ArrayList();

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
        // Triggered by move drone button
        // Initialisation of variables, -0 does not exist as a coordinate
        int intId = -1;
        int newX = -0;
        int newY = -0;
        boolean droneExists = false;
        
        /*
        Opens Option Pane prompting for a Drone ID
        If cancel is pressed, null will be returned causing the loop to break
        otherwise it'll attempt to parse the ID to int, if this fails the user will be reprompted after an error message
        */
        while (true) {
            String enteredId = JOptionPane.showInputDialog(null, "Enter ID of drone to be moved.");
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
        
        // Searches for ArrayList to check if a drone with the ID entered exists
        // If drone is active then it is good to be moved and droneExists is changed to true
        for (DroneDetails p : drones) {
            if (p.getId() == intId) {
                if (p.getActive()) {
                    droneExists = true;
                }
            }
        }
        
        // If no drone exists that is active then droneExists will be false and the user will be given an error message
        if (!droneExists) {
            JOptionPane.showMessageDialog(null, "Drone with ID " + intId + " does not exist or is not active.");
            return;
        }
        
        // Opens option pane prompting user to enter an X position for the drone to be moved to
        while (true) {
            String enteredX = JOptionPane.showInputDialog(null, "Enter new X position for drone " + intId + ".");
            if (enteredX == null) {
                return;
            }
            try {
                newX = Integer.parseInt(enteredX);
                break;
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "ID must be numerical.");
            }
        }
        
        // Opens option pane prompting user to enter an X position for the drone to be moved to
        while (true) {
            String enteredY = JOptionPane.showInputDialog(null, "Enter new Y position for drone " + intId + ".");
            if (enteredY == null) {
                return;
            }
            try {
                newY = Integer.parseInt(enteredY);
                break;
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "ID must be numerical.");
            }
        }
        
        // Removes drone id from hash map in case it's there
        newXPositions.remove(intId);
        newYPositions.remove(intId);
        
        // When all information has been inputted, ids and new positions are added to hash maps
        newXPositions.put(intId, newX);
        newYPositions.put(intId, newY);
        
        // Outputs message to confirm move
        outputLog("Drone " + intId + " will be moved to " + newX + ", " + newY + ".");
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

