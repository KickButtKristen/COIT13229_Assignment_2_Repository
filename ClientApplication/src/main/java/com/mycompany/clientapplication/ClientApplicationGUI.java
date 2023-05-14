/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.clientapplication;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ClientApplicationGUI {
    private JFrame frame;
    private JTextArea reportArea;
    private JTextArea messageArea;

    public ClientApplicationGUI() {
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

        JButton btnSendRequest = new JButton("Send Fire Truck");
        btnSendRequest.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Send request to server
                //TODO add some logic to send a firetruck to fires that have isActive as 1
            }
        });
        buttonPanel.add(btnSendRequest);

        JButton btnGetReport = new JButton("Get Fire Report");
        btnGetReport.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Get fire report from server
                //TODO add some logic to get reports of fires with isActive as 1
            }
        });
        buttonPanel.add(btnGetReport);

        JButton btnGetPreviousReports = new JButton("Get Previous Fire Reports");
        btnGetPreviousReports.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Get previous fire reports from server
                //TODO add some logic to get reports of fires with isActive as 0
            }
        });
        buttonPanel.add(btnGetPreviousReports);

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