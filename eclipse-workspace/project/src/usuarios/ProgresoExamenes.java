package usuarios;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.time.format.DateTimeParseException;

// Importa la clase DataBase
import logear.DataBase;
import logear.login; // Para volver al menú principal
import usuarios.EntradaUser; // Importa EntradaUser

public class ProgresoExamenes {

    public static final String PROGRESS_DIR = "data/progreso/"; // Se mantiene si aún se usa para borrar archivos antiguos

    // --- MÉTODOS DE PERSISTENCIA (AHORA USAN LA BASE DE DATOS) ---
    // Estos métodos en esta clase son ahora adaptadores/envoltorios para DataBase.java
    // Considera limpiar los archivos físicos en data/progreso manualmente si ya no se usarán.

    public static List<String> loadGrades(String username) {
        List<String> formattedGrades = new ArrayList<>();
        try {
            List<String[]> dbGrades = DataBase.obtenerGradosUsuario(username);
            for(String[] gradeInfo : dbGrades) {
                // gradeInfo: {nombre_disciplina, grado_obtenido, fecha_obtencion(String), notas}
                // Formato compatible con el antiguo: nombre_disciplina: Grado (Año)
                formattedGrades.add(gradeInfo[0] + ": " + gradeInfo[1] + " (" + gradeInfo[2].substring(0,4) + ")");
            }
        } catch (Exception e) { // Captura cualquier error de la DB o formato
            System.err.println("Error al cargar grados para " + username + " desde la DB: " + e.getMessage());
            JOptionPane.showMessageDialog(null, "Error al cargar tu progreso. Por favor, intenta de nuevo más tarde.", "Error de Carga", JOptionPane.ERROR_MESSAGE);
            // Si el error es crítico, podrías devolver una lista vacía para no bloquear la UI
            formattedGrades.clear();
        }
        return formattedGrades;
    }

    public static void saveGrades(String username, List<String> grades) {
        System.out.println("ADVERTENCIA: ProgresoExamenes.saveGrades llamado. Los grados ahora se gestionan desde el panel de administración en la base de datos, no aquí.");
        // Esta función ahora no hace nada porque los grados se añaden/modifican desde el admin.
        // Si necesitas limpiar archivos antiguos, puedes hacerlo aquí.
        // new File(PROGRESS_DIR + username + "_grados.txt").delete();
    }

    public static List<String[]> loadExams(String username) {
        List<String[]> relevantExams = new ArrayList<>();
        try {
            // Primero, obtener las disciplinas en las que el usuario está inscrito (desde la DB)
            List<String> userDisciplines = DataBase.obtenerDisciplinasInscritas(username);
            
            // Luego, obtener los exámenes programados para esas disciplinas (desde la DB)
            for (String discipline : userDisciplines) {
                relevantExams.addAll(DataBase.obtenerExamenesDisciplina(discipline));
            }
        } catch (Exception e) { // Captura cualquier error de la DB o formato
            System.err.println("Error al cargar exámenes para " + username + " desde la DB: " + e.getMessage());
            JOptionPane.showMessageDialog(null, "Error al cargar los exámenes programados. Por favor, intenta de nuevo más tarde.", "Error de Carga", JOptionPane.ERROR_MESSAGE);
            relevantExams.clear();
        }
        return relevantExams;
    }

    public static void saveExams(String username, List<String[]> exams) {
        System.out.println("ADVERTENCIA: ProgresoExamenes.saveExams llamado. Los exámenes ahora se gestionan desde el panel de administración en la base de datos, no aquí.");
        // Esta función ahora no hace nada porque los exámenes se añaden/modifican desde el admin.
        // Si necesitas limpiar archivos antiguos, puedes hacerlo aquí.
        // new File(PROGRESS_DIR + username + "_examenes.txt").delete();
    }
    // --- FIN MÉTODOS DE PERSISTENCIA ---


    public static void mostrar(String username) {
        // Carga los grados y exámenes del usuario desde la base de datos
        List<String> gradosAlcanzadosUsuario = loadGrades(username);
        List<String[]> proximosExamenesUsuario = loadExams(username);


        JFrame frame = new JFrame("🎯 Progreso y Exámenes - " + username);
        frame.setSize(1000, 700);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(25, 30, 40));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(40, 50, 65));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        JLabel titleLabel = new JLabel("🎯 PROGRESO Y EXÁMENES", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(new Color(255, 215, 0));
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        JLabel userLabel = new JLabel("Usuario: " + username, SwingConstants.RIGHT);
        userLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        userLabel.setForeground(Color.WHITE);
        userLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20));
        headerPanel.add(userLabel, BorderLayout.EAST);

        // Panel de Contenido Central
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(new Color(25, 30, 40));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));

        // ----------------------------------------
        // Sección de Grados Alcanzados
        // ----------------------------------------
        JLabel gradesTitle = new JLabel("🥋 GRADOS ALCANZADOS", SwingConstants.CENTER);
        gradesTitle.setFont(new Font("Arial", Font.BOLD, 22));
        gradesTitle.setForeground(new Color(16, 185, 129));
        gradesTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        gradesTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        contentPanel.add(gradesTitle);

        if (gradosAlcanzadosUsuario.isEmpty()) {
            JLabel noGradesLabel = new JLabel("Aún no tienes grados registrados. ¡Sigue entrenando!", SwingConstants.CENTER);
            noGradesLabel.setFont(new Font("Arial", Font.ITALIC, 14));
            noGradesLabel.setForeground(Color.LIGHT_GRAY);
            noGradesLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            contentPanel.add(noGradesLabel);
        } else {
            for (String grado : gradosAlcanzadosUsuario) {
                JLabel gradeLabel = new JLabel("• " + grado, SwingConstants.LEFT);
                gradeLabel.setFont(new Font("Arial", Font.PLAIN, 16));
                gradeLabel.setForeground(Color.WHITE);
                gradeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                gradeLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
                contentPanel.add(gradeLabel);
            }
        }

        contentPanel.add(Box.createVerticalStrut(40));

        // ----------------------------------------
        // Sección de Próximos Exámenes
        // ----------------------------------------
        JLabel examsTitle = new JLabel("🗓️ PRÓXIMOS EXÁMENES", SwingConstants.CENTER);
        examsTitle.setFont(new Font("Arial", Font.BOLD, 22));
        examsTitle.setForeground(new Color(59, 130, 246));
        examsTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        examsTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        contentPanel.add(examsTitle);

        if (proximosExamenesUsuario.isEmpty()) {
            JLabel noExamsLabel = new JLabel("No hay exámenes programados próximamente. ¡Mantente atento!", SwingConstants.CENTER);
            noExamsLabel.setFont(new Font("Arial", Font.ITALIC, 14));
            noExamsLabel.setForeground(Color.LIGHT_GRAY);
            noExamsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            contentPanel.add(noExamsLabel);
        } else {
            for (String[] examen : proximosExamenesUsuario) {
                // Asumiendo que examen es String[] {nombre_disciplina, fecha, hora, descripcion, tipo_examen}
                // Asegúrate de que el índice 0 es nombre_disciplina, no fecha
                JPanel examCard = createExamCard(examen[0], examen[1], examen[2], examen[3], examen[4]); // Pasar todos los datos necesarios
                examCard.setAlignmentX(Component.CENTER_ALIGNMENT);
                contentPanel.add(examCard);
                contentPanel.add(Box.createVerticalStrut(10));
            }
        }
        
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getVerticalScrollBar().setBackground(new Color(25, 30, 40));
        scrollPane.getVerticalScrollBar().setForeground(new Color(70, 80, 95));

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JButton btnVolver = new JButton("🏠 VOLVER");
        btnVolver.setFont(new Font("Arial", Font.BOLD, 16));
        btnVolver.setBackground(new Color(70, 80, 95));
        btnVolver.setForeground(Color.BLACK);
        btnVolver.setBorder(BorderFactory.createEmptyBorder(12, 25, 12, 25));
        btnVolver.addActionListener(e -> {
            frame.dispose();
            usuarios.EntradaUser.mostrarPantalla(username); // Vuelve al dashboard del usuario
        });
        mainPanel.add(btnVolver, BorderLayout.SOUTH);

        frame.add(mainPanel);
        frame.setVisible(true);
    }

    // Se modificó la firma para que incluya nombre_disciplina y tipo_examen
    private static JPanel createExamCard(String nombre_disciplina, String fecha, String hora, String descripcion, String tipo_examen) {
        JPanel card = new JPanel(new BorderLayout(10, 5));
        card.setBackground(new Color(45, 55, 70));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 215, 0), 2),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        card.setMaximumSize(new Dimension(800, 120));

        JLabel discLabel = new JLabel("📝 Examen de " + nombre_disciplina + " (" + tipo_examen + ")", SwingConstants.LEFT);
        discLabel.setFont(new Font("Arial", Font.BOLD, 18));
        discLabel.setForeground(new Color(255, 215, 0));

        JLabel detailsLabel = new JLabel("<html>Fecha: <b>" + fecha + "</b><br>Hora: <b>" + hora + "</b><br>Descripción: <i>" + descripcion + "</i></html>", SwingConstants.LEFT);
        detailsLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        detailsLabel.setForeground(Color.WHITE);

        JButton viewDetailsBtn = new JButton("Ver Detalles");
        viewDetailsBtn.setFont(new Font("Arial", Font.BOLD, 12));
        viewDetailsBtn.setBackground(new Color(59, 130, 246));
        viewDetailsBtn.setForeground(Color.WHITE);
        viewDetailsBtn.setFocusPainted(false);
        viewDetailsBtn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        viewDetailsBtn.addActionListener(e -> JOptionPane.showMessageDialog(card,
                                                        "Detalles del examen:\n" +
                                                        "Disciplina: " + nombre_disciplina + "\n" +
                                                        "Tipo: " + tipo_examen + "\n" +
                                                        "Fecha: " + fecha + " a las " + hora + "\n" +
                                                        "Descripción: " + descripcion));

        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setBackground(new Color(45, 55, 70));
        textPanel.add(discLabel);
        textPanel.add(detailsLabel);

        card.add(textPanel, BorderLayout.CENTER);
        card.add(viewDetailsBtn, BorderLayout.EAST);

        return card;
    }
}