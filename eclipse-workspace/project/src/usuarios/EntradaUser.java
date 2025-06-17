package usuarios;

import javax.swing.*;
import java.awt.*;

public class EntradaUser {
    public static void mostrarPantalla(String username) {
        JFrame frame = new JFrame("Dojo Martial Arts");
        frame.setSize(700, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(245, 245, 220)); 
        
        // Header con imagen
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(70, 70, 70));
        
        JLabel lblWelcome = new JLabel("Bienvenido, " + username + "!", SwingConstants.CENTER);
        lblWelcome.setFont(new Font("Arial", Font.BOLD, 24));
        lblWelcome.setForeground(Color.WHITE);
        lblWelcome.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        
        headerPanel.add(lblWelcome, BorderLayout.CENTER);
        
        // Panel de opciones
        JPanel optionsPanel = new JPanel(new GridLayout(4, 1, 10, 10));
        optionsPanel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));
        optionsPanel.setBackground(new Color(245, 245, 220));
        
        // Botones con estilo marcial
        String[] opciones = {
            "🥋 Mis Clases Programadas",
            "📅 Calendario de Entrenamientos",
            "🎯 Progreso y Exámenes", 
            "🚪 Cerrar Sesión"
        };
        
        for (String opcion : opciones) {
            JButton btn = new JButton(opcion);
            btn.setFont(new Font("Arial", Font.PLAIN, 18));
            btn.setBackground(new Color(220, 220, 220));
            btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(150, 150, 150)),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
            ));
            
            if (opcion.equals("🥋 Mis Clases Programadas")) {
                btn.addActionListener(e -> {
                    frame.dispose();
                    usuarios.SeleccionDisciplinas.mostrar(username);
                });
            } else if (opcion.equals("📅 Calendario de Entrenamientos")) {
                btn.addActionListener(e -> {
                    frame.dispose(); 
                    usuarios.CalendarioEntrenamientos.mostrar(username); 
                });
            } else if (opcion.equals("🎯 Progreso y Exámenes")) { 
                btn.addActionListener(e -> {
                    frame.dispose();
                    usuarios.ProgresoExamenes.mostrar(username);
                });
            }
            
            if (opcion.equals("🚪 Cerrar Sesión")) {
                btn.setBackground(new Color(200, 50, 50));
                btn.setForeground(Color.BLACK);
                btn.addActionListener(e -> {
                    frame.dispose();
                    logear.login.showMainMenu();
                });
            }
            
            optionsPanel.add(btn);
        }
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(optionsPanel, BorderLayout.CENTER);
        
        frame.add(mainPanel);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}