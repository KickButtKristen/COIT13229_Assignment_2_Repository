/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany;

import javax.swing.*;
import java.util.List;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import javax.ws.rs.ClientErrorException;

public class ClientApplicationGUI {

    private JFrame frame;
    private JTextArea reportArea;
    private JTextArea messageArea;
    private WebClient webClient;
    private FireRestClient fireRestClient;

    public ClientApplicationGUI() {
        webClient = new WebClient();
        fireRestClient = new FireRestClient();
        initialize();
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    ClientApplicationGUI window = new ClientApplicationGUI();
                    window.frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void initialize() {
        frame = new JFrame("NEMA Client Application");
        frame.setBounds(100, 100, 1200, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();

        JPanel reportPanel = new JPanel();
        reportPanel.setLayout(new BorderLayout());
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 0.7;
        constraints.weighty = 1.0;
        constraints.fill = GridBagConstraints.BOTH;
        frame.getContentPane().add(reportPanel, constraints);

        JLabel lblReport = new JLabel("Requested Report Field", SwingConstants.CENTER);
        lblReport.setFont(new Font("Tahoma", Font.BOLD, 14));
        reportPanel.add(lblReport, BorderLayout.NORTH);

        JScrollPane reportScrollPane = new JScrollPane();
        reportArea = new JTextArea();
        reportScrollPane.setViewportView(reportArea);
        reportPanel.add(reportScrollPane, BorderLayout.CENTER);

        JPanel messagePanel = new JPanel();
        messagePanel.setLayout(new BorderLayout());
        constraints.gridx = 1;
        constraints.weightx = 0.3;
        frame.getContentPane().add(messagePanel, constraints);

        JLabel lblMessage = new JLabel("Message Area", SwingConstants.CENTER);
        lblMessage.setFont(new Font("Tahoma", Font.BOLD, 14));
        messagePanel.add(lblMessage, BorderLayout.NORTH);

        JScrollPane messageScrollPane = new JScrollPane();
        messageArea = new JTextArea();
        messageScrollPane.setViewportView(messageArea);
        messagePanel.add(messageScrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 2;
        constraints.weighty = 0.0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        frame.getContentPane().add(buttonPanel, constraints);

        /**
         * This button INSERTs a new firetruck into the database by assigning it
         * to an existing fire ID
         *
         */
        JButton btnSendRequest = new JButton("Send Fire Truck");
        btnSendRequest.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Send request to server

                // Show JOption pane to input ID
                String firetruckId = JOptionPane.showInputDialog(null, "Enter new firetruck ID: ");
                int ftId = -1;

                if (firetruckId != null && !firetruckId.isEmpty()) {
                    try {
                        ftId = Integer.parseInt(firetruckId);

                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(null, "Invalid ID - please enter a number");
                        return;
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Firetruck ID is empty, please enter an ID.");
                    return;
                }

                // Show JOptionPane input dialog for new firetruck name
                String firetruckName = JOptionPane.showInputDialog(null, "Enter new firetruck name: ");

                if (firetruckName != null && !firetruckName.isEmpty()) {
                    boolean isValid = false;

                    try {
                        FiretrucksRestClient firetrucksRestClient = new FiretrucksRestClient();
                        FireRestClient fireRestClient = new FireRestClient();

                        // Check if there are any existing firetrucks in the database
                        Firetrucks[] existingFiretrucks = firetrucksRestClient.findAll_JSON(Firetrucks[].class);
                        boolean hasExistingFiretrucks = existingFiretrucks != null && existingFiretrucks.length > 0;
                        
                        
                        // check if ID exists against existing firetrucks if they are populated
                        boolean idExists = false;
                        if (hasExistingFiretrucks) {
                            for (Firetrucks truck : existingFiretrucks) {
                                if (truck.getId() == ftId) {
                                    idExists = true;
                                    break;
                                }
                            }
                        }

                        // handle dupe id
                        if (idExists) {
                            JOptionPane.showMessageDialog(null, "Firetruck ID already exists, please enter a different ID");
                            return;
                        }

                        // add all fires to array then loop to add only active fires to array
                        Fire[] fires = fireRestClient.findAll_JSON(Fire[].class);
                        List<Fire> activeFiresList = new ArrayList<>();
                        for (Fire f : fires) {
                            if (f.getIsActive() == 1) {
                                activeFiresList.add(f);
                            }
                        }
                        
                        // Handle no active fires
                        if (activeFiresList.isEmpty()) {
                            JOptionPane.showMessageDialog(null, "There are currently no active fires!");
                            return;
                        }
                        
                        Fire[] activeFires = activeFiresList.toArray(new Fire[activeFiresList.size()]);

                        // Sort activeFires by intensity
                        Arrays.sort(activeFires, new Comparator<Fire>() {
                            @Override
                            public int compare(Fire f1, Fire f2) {
                                return Integer.compare(f2.getIntensity(), f1.getIntensity());
                            }
                        });

                        // Create string array for display to user of fire options
                        String[] fireOptions = new String[activeFires.length];
                        for (int i = 0; i < activeFires.length; i++) {
                            fireOptions[i] = "ID: " + activeFires[i].getId() + " : "
                                    + "INT-" + activeFires[i].getIntensity() + " : "
                                    + "BR-" + activeFires[i].getBurningAreaRadius() + " : "
                                    + "POS[" + activeFires[i].getXpos() + ", " + activeFires[i].getYpos() + "]";

                        }

                        // Display JOptionPane for fire options and get the fire ID of selection
                        String selectedFireOption = (String) JOptionPane.showInputDialog(
                                null,
                                "Select Fire: ",
                                "Select Fire",
                                JOptionPane.QUESTION_MESSAGE,
                                null,
                                fireOptions,
                                fireOptions[0]
                        );

                        if (selectedFireOption != null) {
                            isValid = true;
                            // Get the fire ID from selected fire
                            int selectedFireId = Integer.parseInt(selectedFireOption.split(":")[1].trim());

                            // Create new firetrucks object from input
                            Firetrucks newFiretruck = new Firetrucks();
                            newFiretruck.setId(ftId);
                            newFiretruck.setName(firetruckName);
                            newFiretruck.setDesignatedFireId(selectedFireId);

                            // send firetruck to server to insert to DB
                            firetrucksRestClient.create_JSON(newFiretruck);

                            // success msg
                            messageArea.setText("Firetruck successfully inserted to DB and assigned to fire id " + selectedFireId + "\n");

                            // close
                            firetrucksRestClient.close();
                            fireRestClient.close();
                        }
                    } catch (ClientErrorException ex) {
                        ex.printStackTrace();
                        messageArea.setText("Error inserting firetruck: " + ex.getMessage() + "\n");
                    }

                    if (!isValid) {
                        messageArea.setText("Fire selection cancelled or no fire was selected\n"); // handle no fire selection
                    }
                } else {
                    // If fire truck name is empty or null...
                    messageArea.setText("Firetruck name cannot be empty!\n");
                }
            }
        });
        buttonPanel.add(btnSendRequest);

        /**
         * This button requests a report of all existing firetrucks
         *
         */
        JButton btnGetFiretrucksReport = new JButton("Get Firetrucks Report");

        btnGetFiretrucksReport.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                try {
                    FiretrucksRestClient firetrucksRestClient = new FiretrucksRestClient();
                    Firetrucks[] firetrucks = firetrucksRestClient.findAll_JSON(Firetrucks[].class);
                    StringBuilder reportBuilder = new StringBuilder();

                    for (Firetrucks ft : firetrucks) {
                        reportBuilder.append("Firetruck ID: ").append(ft.getId()).append("\n")
                                .append("Firetruck name: ").append(ft.getName()).append("\n")
                                .append("Designated to fire ID: ").append(ft.getDesignatedFireId()).append("\n\n");
                    }
                    reportArea.setText(reportBuilder.toString());
                    firetrucksRestClient.close();
                } catch (ClientErrorException ex) {
                    ex.printStackTrace();
                    messageArea.append("Error getting firetrucks report: " + ex.getMessage() + "\n");
                }
            }
        });
        buttonPanel.add(btnGetFiretrucksReport);

        /**
         * This button requests a report of all existing fires
         *
         */
        JButton btnGetReport = new JButton("Get Fire Report");
        btnGetReport.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    FireRestClient fireRestClient = new FireRestClient();
                    Fire[] fires = fireRestClient.findAll_JSON(Fire[].class);
                    StringBuilder reportBuilder = new StringBuilder();
                    for (Fire fire : fires) {
                        if (fire.getIsActive() == 1) {
                            reportBuilder.append("Fire ID: ").append(fire.getId()).append("\n")
                                    .append("Active: Yes\n")
                                    .append("Intensity: ").append(fire.getIntensity()).append("\n")
                                    .append("Burning Area Radius: ").append(fire.getBurningAreaRadius()).append("\n")
                                    .append("Position: (").append(fire.getXpos()).append(", ").append(fire.getYpos()).append(")\n\n");
                        }
                    }
                    reportArea.setText(reportBuilder.toString());
                    fireRestClient.close();
                } catch (ClientErrorException exception) {
                    exception.printStackTrace();
                    messageArea.append("Error getting fire report: " + exception.getMessage() + "\n");
                }
            }
        });
        buttonPanel.add(btnGetReport);

        /**
         * This button requests fire reports from inactive fires that are stored
         * on the server
         *
         */
        JButton btnGetPreviousReports = new JButton("Get Previous Fire Reports");
        btnGetPreviousReports.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    FireRestClient fireRestClient = new FireRestClient();
                    Fire[] fires = fireRestClient.findAll_JSON(Fire[].class);
                    StringBuilder reportBuilder = new StringBuilder();
                    for (Fire fire : fires) {
                        if (fire.getIsActive() == 0) {
                            reportBuilder.append("Fire ID: ").append(fire.getId()).append("\n")
                                    .append("Active: No\n")
                                    .append("Intensity: ").append(fire.getIntensity()).append("\n")
                                    .append("Burning Area Radius: ").append(fire.getBurningAreaRadius()).append("\n")
                                    .append("Position: (").append(fire.getXpos()).append(", ").append(fire.getYpos()).append(")\n\n");
                        }
                    }
                    reportArea.setText(reportBuilder.toString());
                    fireRestClient.close();
                } catch (ClientErrorException exception) {
                    exception.printStackTrace();
                    messageArea.append("Error getting previous fire report: " + exception.getMessage() + "\n");
                }
            }
        });
        buttonPanel.add(btnGetPreviousReports);

        /**
         * This button closes the application
         *
         */
        JButton btnShutdown = new JButton("Shutdown");
        btnShutdown.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Shutdown the application
                System.exit(0);
            }
        });
        buttonPanel.add(btnShutdown);
    }
}
