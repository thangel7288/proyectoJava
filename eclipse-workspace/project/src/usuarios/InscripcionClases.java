package usuarios;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class InscripcionClases {
    
    // Clase interna para manejar cada disciplina
    private static class Disciplina {
        String nombre;
        String horario;
        String instructor;
        Color color;
        
        public Disciplina(String nombre, String horario, String instructor, Color color) {
            this.nombre = nombre;
            this.horario = horario;
            this.instructor = instructor;
            this.color = color;
        }
    }
    
    public static void mostrar(String username) {
        JFrame frame = new JFrame("Inscripción a Clases");
        frame.setSize(1000, 700);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(240, 240, 240));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Título
        JLabel titleLabel = new JLabel("ELIGE TU DISCIPLINA", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(new Color(50, 50, 50));
        
        // Panel de disciplinas
        JPanel disciplinesPanel = new JPanel(new GridLayout(0, 2, 30, 30)); // 2 columnas
        disciplinesPanel.setBackground(new Color(240, 240, 240));
        
        // Datos de las disciplinas
        Disciplina[] disciplinas = {
            new Disciplina("Karate", "Lunes y Miércoles 18:00-20:00", "Sensei Tanaka", new Color(200, 50, 50)),
            new Disciplina("Judo", "Martes y Jueves 17:00-19:00", "Sensei Yamada", new Color(30, 80, 180)),
            new Disciplina("Taekwondo", "Lunes y Viernes 19:00-21:00", "Master Kim", new Color(0, 120, 80)),
            new Disciplina("Kung Fu", "Miércoles y Sábados 10:00-12:00", "Sifu Chen", new Color(160, 80, 0)),
            new Disciplina("Boxeo", "Martes y Viernes 18:00-20:00", "Entrenador Pérez", new Color(150, 30, 30)),
            new Disciplina("Capoeira", "Jueves y Sábados 16:00-18:00", "Mestre Silva", new Color(0, 100, 100))
        };
        
        // Crear tarjetas para cada disciplina
        for (Disciplina disciplina : disciplinas) {
            JPanel card = createDisciplineCard(disciplina, frame, username);
            disciplinesPanel.add(card);
        }
        
        // Botón volver
        JButton btnVolver = new JButton("VOLVER");
        btnVolver.setFont(new Font("Arial", Font.BOLD, 16));
        btnVolver.setBackground(new Color(180, 180, 180));
        btnVolver.addActionListener(e -> {
            frame.dispose();
            EntradaUser.mostrarPantalla(username);
        });
        
        // Ensamblaje
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        mainPanel.add(new JScrollPane(disciplinesPanel), BorderLayout.CENTER);
        mainPanel.add(btnVolver, BorderLayout.SOUTH);
        
        frame.add(mainPanel);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    
    private static JPanel createDisciplineCard(Disciplina disciplina, JFrame parent, String username) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(disciplina.color, 3),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        // Nombre de la disciplina
        JLabel nameLabel = new JLabel(disciplina.nombre);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 22));
        nameLabel.setForeground(disciplina.color);
        
        // Detalles
        JTextArea detailsArea = new JTextArea(
            "Horario: " + disciplina.horario + "\n" +
            "Instructor: " + disciplina.instructor + "\n" +
            "Costo mensual: $120.000 COP"
        );
        detailsArea.setEditable(false);
        detailsArea.setFont(new Font("Arial", Font.PLAIN, 16));
        detailsArea.setBackground(Color.WHITE);
        
        // Botón de inscripción
        JButton btnInscribir = new JButton("INSCRIBIRME");
        btnInscribir.setBackground(disciplina.color);
        btnInscribir.setForeground(Color.WHITE);
        btnInscribir.setFont(new Font("Arial", Font.BOLD, 16));
        btnInscribir.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(parent,
                "¿Confirmas tu inscripción en " + disciplina.nombre + "?\n\n" +
                "Horario: " + disciplina.horario + "\n" +
                "Instructor: " + disciplina.instructor,
                "Confirmar Inscripción",
                JOptionPane.YES_NO_OPTION);
                
            if (confirm == JOptionPane.YES_OPTION) {
                JOptionPane.showMessageDialog(parent,
                    "¡Felicidades " + username + "!\n" +
                    "Ahora estás inscrito en " + disciplina.nombre + ".\n\n" +
                    "Tu primera clase es el próximo " + disciplina.horario.split(" ")[0],
                    "Inscripción Exitosa",
                    JOptionPane.INFORMATION_MESSAGE);
                
                // Aquí iría el código para guardar en la base de datos
                // Database.inscribirEnClase(username, disciplina.nombre);
            }
        });
        
        // Añadir componentes a la tarjeta
        card.add(nameLabel, BorderLayout.NORTH);
        card.add(detailsArea, BorderLayout.CENTER);
        card.add(btnInscribir, BorderLayout.SOUTH);
        
        return card;
    }
}