package logear;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File; 
import java.util.Arrays;
import java.util.ArrayList; 
import usuarios.EntradaUser;
import usuarios.SeleccionDisciplinas; 
import usuarios.ProgresoExamenes;    

public class UserLogin {

    public static final String USERS_DATA_DIR = "data/users/"; 

    public static void showLoginWindow() {
        JFrame frame = new JFrame("Acceso Alumnos");
        frame.setSize(400, 400); 
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(240, 240, 240));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        Font labelFont = new Font("Arial", Font.BOLD, 14);
        Font buttonFont = new Font("Arial", Font.BOLD, 14);

        JLabel lblTitle = new JLabel("INGRESO ALUMNOS", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitle.setForeground(new Color(70, 70, 70));

        JLabel lblUser = new JLabel("Usuario:");
        lblUser.setFont(labelFont);
        JTextField txtUser = new JTextField(15);
        txtUser.setFont(new Font("Arial", Font.PLAIN, 14));
        
        JLabel lblPass = new JLabel("Contraseña:");
        lblPass.setFont(labelFont);
        JPasswordField txtPass = new JPasswordField(15);
        txtPass.setFont(new Font("Arial", Font.PLAIN, 14));

        JButton btnLogin = new JButton("ENTRAR");
        btnLogin.setBackground(new Color(100, 200, 100));
        btnLogin.setFont(buttonFont);
        btnLogin.setForeground(Color.BLACK);

        JButton btnRegister = new JButton("REGISTRARSE"); 
        btnRegister.setBackground(new Color(50, 150, 200));
        btnRegister.setFont(buttonFont);
        btnRegister.setForeground(Color.BLACK);

        JButton btnBack = new JButton("VOLVER");
        btnBack.setBackground(new Color(255, 150, 150));
        btnBack.setFont(buttonFont);
        btnBack.setForeground(Color.BLACK);

        btnLogin.addActionListener(e -> {
            String username = txtUser.getText().trim();
            String password = new String(txtPass.getPassword()); 
            
            // Autenticar al usuario usando DataBase
            String userType = DataBase.autenticarUsuario(username, password); 
            
            if (userType != null && userType.equals("usuario")) { 
                frame.dispose();
                EntradaUser.mostrarPantalla(username);
            } else {
                JOptionPane.showMessageDialog(frame,
                    "Usuario o contraseña incorrectos",
                    "Error de acceso",
                    JOptionPane.ERROR_MESSAGE);
            }
            Arrays.fill(txtPass.getPassword(), '\0'); 
        });

        btnRegister.addActionListener(e -> {
            frame.dispose();
            UserRegistration.showRegistrationWindow();
        });

        btnBack.addActionListener(e -> {
            frame.dispose();
            login.showMainMenu();
        });

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(lblTitle, gbc);

        gbc.gridwidth = 1; gbc.gridy = 1; panel.add(lblUser, gbc);
        gbc.gridx = 1; panel.add(txtUser, gbc);

        gbc.gridx = 0; gbc.gridy = 2; panel.add(lblPass, gbc);
        gbc.gridx = 1; panel.add(txtPass, gbc);

        JPanel topButtonPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        topButtonPanel.setBackground(new Color(240, 240, 240));
        topButtonPanel.add(btnLogin);
        topButtonPanel.add(btnRegister);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        panel.add(topButtonPanel, gbc);

        gbc.gridy = 4;
        gbc.gridwidth = 2;
        panel.add(btnBack, gbc);

        frame.add(panel);
        frame.setVisible(true);
    }

    /**
     * Verifica si un nombre de usuario ya existe en la base de datos a través de DataBase.
     */
    public static boolean userExists(String username) {
        return DataBase.usuarioExiste(username);
    }

    /**
     * Guarda un nuevo usuario en la base de datos y crea sus archivos de datos iniciales.
     */
    public static void saveNewUser(String username, String password, String document, String dobString) {
        boolean registeredInDB = DataBase.registrarUsuario(username, password, document, dobString, "usuario");
        
        if (registeredInDB) {
            SeleccionDisciplinas.saveUserInscriptions(username, new ArrayList<>());
            ProgresoExamenes.saveGrades(username, new ArrayList<>());
            ProgresoExamenes.saveExams(username, new ArrayList<>());
        }
    }
}