package logear;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId; 
import java.time.format.DateTimeFormatter; 
import java.time.format.DateTimeParseException; 
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Arrays;

import com.toedter.calendar.JDateChooser; 

public class UserRegistration {

    public static void showRegistrationWindow() {
        JFrame frame = new JFrame("Registro de Alumnos");
        frame.setSize(450, 550);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(240, 240, 240));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        Font labelFont = new Font("Arial", Font.BOLD, 14);
        Font fieldFont = new Font("Arial", Font.PLAIN, 14);
        Font buttonFont = new Font("Arial", Font.BOLD, 14);

        JLabel lblTitle = new JLabel("REGISTRO DE ALUMNOS", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 20));
        lblTitle.setForeground(Color.BLACK); // Cambiado a Color.BLACK

        JLabel lblUser = new JLabel("Usuario:");
        lblUser.setFont(labelFont);
        JTextField txtUser = new JTextField(20);
        txtUser.setFont(fieldFont);

        JLabel lblPass = new JLabel("Contraseña:");
        lblPass.setFont(labelFont);
        JPasswordField txtPass = new JPasswordField(20);
        txtPass.setFont(fieldFont);

        JLabel lblConfirmPass = new JLabel("Confirmar Contraseña:");
        lblConfirmPass.setFont(labelFont);
        JPasswordField txtConfirmPass = new JPasswordField(20);
        txtConfirmPass.setFont(fieldFont);

        JLabel lblDocument = new JLabel("No. Documento:");
        lblDocument.setFont(labelFont);
        JTextField txtDocument = new JTextField(20);
        txtDocument.setFont(fieldFont);

        JLabel lblDOB = new JLabel("Fecha Nacimiento:");
        lblDOB.setFont(labelFont);
        
        JDateChooser dateChooserDOB = new JDateChooser();
        dateChooserDOB.setDateFormatString("dd/MM/yyyy");
        dateChooserDOB.setFont(fieldFont);
        dateChooserDOB.setBackground(Color.WHITE);
        dateChooserDOB.setPreferredSize(new Dimension(200, 28));
        
        // Deshabilitar fechas futuras en el calendario (Solo fechas hasta hoy)
        dateChooserDOB.setMaxSelectableDate(new Date()); 


        JButton btnRegister = new JButton("REGISTRAR");
        btnRegister.setBackground(new Color(34, 197, 94));
        btnRegister.setFont(buttonFont);
        btnRegister.setForeground(Color.BLACK); // <-- Cambiado a Color.BLACK

        JButton btnBack = new JButton("VOLVER");
        btnBack.setBackground(new Color(255, 100, 100));
        btnBack.setFont(buttonFont);
        btnBack.setForeground(Color.BLACK);

        btnRegister.addActionListener(e -> {
            String username = txtUser.getText().trim();
            char[] password = txtPass.getPassword();
            char[] confirmPassword = txtConfirmPass.getPassword();
            String document = txtDocument.getText().trim();
            Date dobDate = dateChooserDOB.getDate(); 

            if (validateRegistration(frame, username, password, confirmPassword, document, dobDate)) {
                String dobString = new java.text.SimpleDateFormat("dd/MM/yyyy").format(dobDate);

                UserLogin.saveNewUser(username, new String(password), document, dobString);
                
                JOptionPane.showMessageDialog(frame, "¡Registro exitoso!\nAhora puedes iniciar sesión.", "Registro Completado", JOptionPane.INFORMATION_MESSAGE);
                frame.dispose();
                UserLogin.showLoginWindow(); 
            }
            Arrays.fill(password, '\0');
            Arrays.fill(confirmPassword, '\0');
        });

        btnBack.addActionListener(e -> {
            frame.dispose();
            UserLogin.showLoginWindow();
        });

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8); 
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(lblTitle, gbc);

        gbc.gridwidth = 1; gbc.gridy = 1; panel.add(lblUser, gbc);
        gbc.gridx = 1; panel.add(txtUser, gbc);

        gbc.gridx = 0; gbc.gridy = 2; panel.add(lblPass, gbc);
        gbc.gridx = 1; panel.add(txtPass, gbc);

        gbc.gridx = 0; gbc.gridy = 3; panel.add(lblConfirmPass, gbc);
        gbc.gridx = 1; panel.add(txtConfirmPass, gbc);

        gbc.gridx = 0; gbc.gridy = 4; panel.add(lblDocument, gbc);
        gbc.gridx = 1; panel.add(txtDocument, gbc);

        gbc.gridx = 0; gbc.gridy = 5; panel.add(lblDOB, gbc);
        gbc.gridx = 1; panel.add(dateChooserDOB, gbc);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        buttonPanel.setBackground(new Color(240, 240, 240));
        buttonPanel.add(btnRegister);
        buttonPanel.add(btnBack);

        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);

        frame.add(panel);
        frame.setVisible(true);
    }

    private static boolean validateRegistration(JFrame parentFrame, String username, char[] password, char[] confirmPassword, String document, Date dobDate) {
        // Validación de Usuario
        if (username.length() < 5) {
            JOptionPane.showMessageDialog(parentFrame, "El usuario debe tener al menos 5 caracteres.", "Error de Validación", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (DataBase.usuarioExiste(username)) { 
            JOptionPane.showMessageDialog(parentFrame, "El usuario '" + username + "' ya existe. Por favor, elige otro.", "Error de Validación", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        // Validación de Contraseña
        String pass = new String(password);
        String confPass = new String(confirmPassword);
        if (pass.length() < 6) { 
            JOptionPane.showMessageDialog(parentFrame, "La contraseña debe tener al menos 6 caracteres.", "Error de Validación", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        Pattern passwordPattern = Pattern.compile("^(?=.*[A-Z])(?=.*\\d).*$"); 
        Matcher matcher = passwordPattern.matcher(pass);
        if (!matcher.matches()) {
            JOptionPane.showMessageDialog(parentFrame, "La contraseña debe contener al menos una letra mayúscula y un número.", "Error de Validación", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (!pass.equals(confPass)) {
            JOptionPane.showMessageDialog(parentFrame, "Las contraseñas no coinciden.", "Error de Validación", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        // Validación de Número de Documento
        if (!document.matches("\\d{9,10}")) { 
            JOptionPane.showMessageDialog(parentFrame, "El número de documento debe contener entre 9 y 10 dígitos numéricos.", "Error de Validación", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (DataBase.documentoExiste(document)) { 
             JOptionPane.showMessageDialog(parentFrame, "El número de documento '" + document + "' ya está registrado.", "Error de Validación", JOptionPane.WARNING_MESSAGE);
             return false;
        }

        // Validación de Fecha de Nacimiento
        if (dobDate == null) {
            JOptionPane.showMessageDialog(parentFrame, "Por favor, selecciona una fecha de nacimiento.", "Error de Validación", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        LocalDate dob = dobDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(); 
        LocalDate today = LocalDate.now();

        if (dob.isAfter(today)) { 
            JOptionPane.showMessageDialog(parentFrame, "La fecha de nacimiento no puede ser una fecha futura.", "Error de Validación", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        Period age = Period.between(dob, today);
        if (age.getYears() < 8) { 
            JOptionPane.showMessageDialog(parentFrame, "Debes tener al menos 8 años para registrarte.", "Error de Validación", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        return true; 
    }
}