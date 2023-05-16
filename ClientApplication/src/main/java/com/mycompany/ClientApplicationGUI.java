/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
         * This button INSERTs a new firetruck into the database by assigning it to an existing fire ID
         * 
         */
        JButton btnSendRequest = new JButton("Send Fire Truck");
        btnSendRequest.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Send request to server
                //TODO add some logic to send a firetruck to fires that have isActive as 1
                
   
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
                // Get fire report from server
                try {
                    FireRestClient fireRestClient = new FireRestClient();
                    Fire[] fires = fireRestClient.findAll_JSON(Fire[].class);
                    StringBuilder reportBuilder = new StringBuilder();
                    for (Fire fire : fires) {
                        reportBuilder.append("Fire ID: ").append(fire.getId()).append("\n")
                                     .append("Active: ").append(fire.getIsActive() == 1 ? "Yes" : "No").append("\n")
                                     .append("Intensity: ").append(fire.getIntensity()).append("\n")
                                     .append("Burning Area Radius: ").append(fire.getBurningAreaRadius()).append("\n")
                                     .append("Position: (").append(fire.getXpos()).append(", ").append(fire.getYpos()).append(")\n\n");
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
         * This button requests fire reports from inactive fires that are stored on the server
         * 
         */
        JButton btnGetPreviousReports = new JButton("Get Previous Fire Reports");
        btnGetPreviousReports.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Get previous fire reports from server
                //TODO add some logic to get reports of fires with isActive as 0
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