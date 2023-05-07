import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;

public class ClientApplication extends JFrame implements ActionListener {

    private JPanel controlPanel;
    private JLabel titleLabel;
    private JLabel serverUrlLabel;
    private JLabel serverPortLabel;
    private JLabel fireIdLabel;
    private JLabel truckIdLabel;
    private JTextField serverUrlTextField;
    private JTextField serverPortTextField;
    private JTextField fireIdTextField;
    private JTextField truckIdTextField;
    private JButton sendReportButton;
    private JButton sendTruckButton;
    private MapPanel mapPanel;
    
    private static Connection conn;

    public ClientApplication() {
        // Set up GUI components
        titleLabel = new JLabel("NEMA Client Application");
        serverUrlLabel = new JLabel("Server URL:");
        serverPortLabel = new JLabel("Server Port:");
        fireIdLabel = new JLabel("Fire ID:");
        truckIdLabel = new JLabel("Truck ID:");
        serverUrlTextField = new JTextField(20);
        serverPortTextField = new JTextField(5);
        fireIdTextField = new JTextField(5);
        truckIdTextField = new JTextField(5);
        sendReportButton = new JButton("Send Report");
        sendTruckButton = new JButton("Send Truck");
        
        //mapPanel = new MapPanel();

        // Set up control panel
        controlPanel = new JPanel();
        controlPanel.setLayout(new GridLayout(5, 2));
        controlPanel.add(serverUrlLabel);
        controlPanel.add(serverUrlTextField);
        controlPanel.add(serverPortLabel);
        controlPanel.add(serverPortTextField);
        controlPanel.add(fireIdLabel);
        controlPanel.add(fireIdTextField);
        controlPanel.add(truckIdLabel);
        controlPanel.add(truckIdTextField);
        controlPanel.add(sendReportButton);
        controlPanel.add(sendTruckButton);

        // Set up main panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        mainPanel.add(controlPanel, BorderLayout.WEST);
        mainPanel.add(mapPanel, BorderLayout.CENTER);

        // Add action listeners
        sendReportButton.addActionListener(this);
        sendTruckButton.addActionListener(this);

        // Set up JFrame
        setTitle("NEMA Client Application");
        setSize(800, 600);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        add(mainPanel);
        setVisible(true);
        
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String url = "jdbc:mysql://localhost:3306/nema";
            String username = "test";
            String password = "test";
            conn = DriverManager.getConnection(url, username, password);
            System.out.println("Connected to database!");
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        
    }


    public static void main(String[] args) {
        new ClientApplication();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Handle button clicks
        if (e.getSource() == sendReportButton) {
            String serverUrl = serverUrlTextField.getText();
            int serverPort = Integer.parseInt(serverPortTextField.getText());
            int fireId = Integer.parseInt(fireIdTextField.getText());
            // TODO: send report to server
        } else if (e.getSource() == sendTruckButton) {
            String serverUrl = serverUrlTextField.getText();
            int serverPort = Integer.parseInt(serverPortTextField.getText());
            int truckId = Integer.parseInt(truckIdTextField.getText());
            int fireId = Integer.parseInt(fireIdTextField.getText());
            // TODO: send truck to server
        }
    }
    
    private class MapPanel extends JPanel {

        private ArrayList<DroneDetails> drones;
        private ArrayList<FireDetails> fires;

        public MapPanel(ArrayList<DroneDetails> drones, ArrayList<FireDetails> fires) {
            this.drones = drones;
            this.fires = fires;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            // Set background color of map panel
            setBackground(Color.WHITE);

            // Draw drones as blue circles with drone id
            for (DroneDetails p : drones) {
                if (p.getActive()) {
                    // Converts coordinates for use on 400 by 400 grid
                    int x = (100 - p.getX_pos()) * 2;
                    int y = (100 - p.getY_pos()) * 2;
                    int size = 10;
                    g.setColor(Color.BLUE);
                    g.fillOval(x - size/2, y - size/2, size, size);
                    g.setColor(Color.BLACK);
                    g.drawString("Drone " + p.getId(), x - 30, y - 5);
                }
            }

            // Draw fires as red circles with fire id and severity
            for (FireDetails p : fires) {
                // Converts coordinates for use on 400 by 400 grid
                int x = (100 - p.getX_pos()) * 2;
                int y = (100 - p.getY_pos()) * 2;
                int severity = p.getSeverity();
                int size = 10;
                g.setColor(Color.RED);
                g.fillOval(x - size/2, y - size/2, size, size);
                g.setColor(Color.BLACK);
                g.drawString("Fire " + p.getId() + " (" + severity + ")", x - 30, y - 5);
            }
        }
    }
}