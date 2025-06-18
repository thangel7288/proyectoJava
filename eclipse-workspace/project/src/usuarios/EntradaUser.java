package usuarios;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener; // Importación necesaria para ActionListener

public class EntradaUser {
    public static void mostrarPantalla(String username) {
        JFrame frame = new JFrame("Dojo Martial Arts");
        frame.setSize(700, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(245, 245, 220)); // Fondo claro para el panel principal
        
        // Header con imagen
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(70, 70, 70)); // Fondo oscuro para el encabezado
        
        JLabel lblWelcome = new JLabel("Bienvenido, " + username + "!", SwingConstants.CENTER);
        lblWelcome.setFont(new Font("Arial", Font.BOLD, 24));
        lblWelcome.setForeground(Color.WHITE); // Texto blanco
        lblWelcome.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        
        headerPanel.add(lblWelcome, BorderLayout.CENTER);
        
        // Panel de opciones
        JPanel optionsPanel = new JPanel(new GridLayout(5, 1, 10, 10)); // Aumentado a 5 filas para el nuevo botón
        optionsPanel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));
        optionsPanel.setBackground(new Color(245, 245, 220)); // Fondo claro
        
        // Botones con estilo marcial
        String[] opciones = {
            "🥋 Mis Clases Programadas",
            "📅 Calendario de Entrenamientos",
            "🎯 Progreso y Exámenes",    
            "📞 Soporte Técnico", // Nuevo botón
            "🚪 Cerrar Sesión"
        };
        
        for (String opcion : opciones) {
            JButton btn = new JButton(opcion);
            btn.setFont(new Font("Arial", Font.PLAIN, 18));
            btn.setBackground(new Color(220, 220, 220)); // Fondo gris claro para los botones
            btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(150, 150, 150)), // Borde gris más oscuro
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
            ));
            
            // Asignar acciones a los botones
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
            } else if (opcion.equals("📞 Soporte Técnico")) { // Lógica para el nuevo botón de soporte
                btn.setBackground(new Color(59, 130, 246)); // Un color azul para el botón de soporte
                btn.setForeground(Color.BLACK); // CAMBIO: Texto a negro para el botón de soporte
                btn.addActionListener(e -> {
                    // Datos de contacto de soporte (inventados)
                    String nombreSoporte = "Equipo de Soporte Fight Club";
                    String emailSoporte = "soporte@fightclub.com";
                    String telefonoSoporte = "+57 310 123 4567";
                    String horarioSoporte = "Lunes a Viernes: 9:00 AM - 6:00 PM (Hora de Colombia)";

                    String mensajeSoporte = "<html><body style='font-family: Arial; font-size: 12px;'>"
                                            + "<h2>📞 Contacto de Soporte Técnico</h2>"
                                            + "<b>Nombre:</b> " + nombreSoporte + "<br>"
                                            + "<b>Email:</b> <a href='mailto:" + emailSoporte + "'>" + emailSoporte + "</a><br>"
                                            + "<b>Teléfono:</b> " + telefonoSoporte + "<br>"
                                            + "<b>Horario:</b> " + horarioSoporte + "<br><br>"
                                            + "<i>Por favor, describa su problema detalladamente para una asistencia más rápida.</i>"
                                            + "</body></html>";
                    
                    JOptionPane.showMessageDialog(frame, mensajeSoporte, "Soporte Técnico", JOptionPane.INFORMATION_MESSAGE);
                });
            }
            
            if (opcion.equals("🚪 Cerrar Sesión")) {
                btn.setBackground(new Color(200, 50, 50)); // Fondo rojo para cerrar sesión
                btn.setForeground(Color.BLACK); // Texto negro para cerrar sesión
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