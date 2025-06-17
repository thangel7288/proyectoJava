package logear;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

import admin.AdminDashboard; 

public class AdminLogin {

    public static void showAdminLogin() {
        JFrame frame = new JFrame("Acceso Administrador");
        frame.setSize(400, 350); 
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(240, 240, 240)); 
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        Font labelFont = new Font("Arial", Font.BOLD, 14);
        
        JLabel lblUser = new JLabel("Usuario:");
        lblUser.setFont(labelFont);
        JTextField txtUser = new JTextField(15);
        
        JLabel lblPass = new JLabel("Contraseña:");
        lblPass.setFont(labelFont);
        JPasswordField txtPass = new JPasswordField(15);
        
        JButton btnLogin = new JButton("INGRESAR");
        btnLogin.setBackground(new Color(100, 200, 100));
        btnLogin.setForeground(Color.BLACK);
        btnLogin.setFont(new Font("Arial", Font.BOLD, 14));
        btnLogin.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        
        JButton btnBack = new JButton("VOLVER");
        btnBack.setBackground(new Color(255, 100, 100));
        btnBack.setForeground(Color.BLACK);
        btnBack.setFont(new Font("Arial", Font.BOLD, 14));
        btnBack.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        
        btnLogin.addActionListener(e -> {
            String user = txtUser.getText().trim();
            String password = new String(txtPass.getPassword());
            
            // Autenticar al administrador usando DataBase
            String userType = DataBase.autenticarUsuario(user, password);
            
            if(userType != null && userType.equals("admin")) { 
                frame.dispose();
                AdminDashboard.showAdminDashboard(); 
            } else {
                JOptionPane.showMessageDialog(frame, 
                    "Credenciales incorrectas", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
            Arrays.fill(txtPass.getPassword(), '\0');
        });
        
        btnBack.addActionListener(e -> {
            frame.dispose();
            login.showMainMenu();
        });
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JLabel title = new JLabel("ACCESO ADMINISTRADOR", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 18));
        title.setForeground(new Color(50, 50, 50)); 
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(title, gbc);
        
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        panel.add(lblUser, gbc);
        
        gbc.gridx = 1;
        panel.add(txtUser, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(lblPass, gbc);
        gbc.gridx = 1;
        panel.add(txtPass, gbc);
        
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        buttonPanel.setBackground(new Color(240, 240, 240));
        buttonPanel.add(btnLogin);
        buttonPanel.add(btnBack);
        
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);
        
        frame.add(panel);
        frame.setVisible(true);
    }
}