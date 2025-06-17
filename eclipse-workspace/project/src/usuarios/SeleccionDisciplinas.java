package usuarios;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import logear.DataBase;
import logear.login;
import usuarios.EntradaUser;

public class SeleccionDisciplinas {
    // Ya no necesitamos un mapa estático hardcodeado; cargaremos desde la DB.
    // Esta variable se usará para almacenar las disciplinas cargadas de la DB.
    private static List<Map<String, String>> allDisciplinesFromDB = new ArrayList<>();

    // La ruta de los directorios de archivos antiguos se mantiene solo para referencia si aún los usas para algo.
    public static final String INSCRIPTIONS_DIR = "data/inscripciones/";

    // Nueva clase interna para encapsular la información de la disciplina para mostrar en la UI
    // Esta clase se crea a partir de los datos obtenidos de la base de datos.
    public static class DisciplineDisplayInfo {
        String id;
        String nombre;
        String descripcion;
        String horario;
        String instructor;
        String nivel;
        boolean activo;
        String extendedDescription; // Puede ser la misma 'descripcion' o un campo adicional si existe en la DB
        Color color; // Color asociado para la UI

        public DisciplineDisplayInfo(Map<String, String> dbData) {
            this.id = dbData.get("id");
            this.nombre = dbData.get("nombre");
            this.descripcion = dbData.get("descripcion");
            this.horario = dbData.get("horario");
            this.instructor = dbData.get("instructor");
            this.nivel = dbData.get("nivel");
            this.activo = Boolean.parseBoolean(dbData.get("activo"));
            // Usamos la descripción como descripción extendida si no hay un campo separado
            this.extendedDescription = dbData.get("descripcion"); 
            this.color = getColorForDiscipline(this.nombre); // Asignamos color basado en el nombre
        }
    }

    // Método auxiliar para obtener el color de una disciplina (copia del que ya existe)
    private static Color getColorForDiscipline(String disciplineName) {
        switch (disciplineName) {
            case "Karate": return new Color(220, 38, 38);
            case "Judo": return new Color(59, 130, 246);
            case "Taekwondo": return new Color(16, 185, 129);
            case "Boxeo": return new Color(239, 68, 68);
            case "Muay Thai": return new Color(168, 85, 247);
            default: return Color.LIGHT_GRAY;
        }
    }

    // Nuevo método para cargar las disciplinas desde la base de datos
    private static void loadAllDisciplinesFromDB() {
        // Carga TODAS las disciplinas, incluyendo las inactivas, para poder filtrar después
        allDisciplinesFromDB = DataBase.obtenerTodasLasDisciplinas(true);
        System.out.println("DEBUG: SeleccionDisciplinas.loadAllDisciplinesFromDB() cargó " + allDisciplinesFromDB.size() + " disciplinas.");
    }

    public static void mostrar(String username) {
        // Primero, carga las disciplinas desde la base de datos cada vez que se muestra la pantalla
        loadAllDisciplinesFromDB();

        // Carga SOLO las inscripciones activas para la pestaña "Mis Disciplinas"
        List<String> inscripcionesUsuarioActivas = DataBase.obtenerDisciplinasInscritas(username);

        JFrame frame = new JFrame("🥋 Academia MMA - " + username);
        frame.setSize(1200, 800);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLocationRelativeTo(null); // Centra la ventana

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(25, 30, 40));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(40, 50, 65));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        JLabel titleLabel = new JLabel("🥋 ACADEMIA DE ARTES MARCIALES", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(new Color(255, 215, 0));
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        JLabel userLabel = new JLabel("Usuario: " + username, SwingConstants.RIGHT);
        userLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        userLabel.setForeground(Color.WHITE);
        userLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20));
        headerPanel.add(userLabel, BorderLayout.EAST);

        // Pestañas
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 14));
        tabbedPane.setBackground(new Color(25, 30, 40));
        tabbedPane.setForeground(Color.BLACK); // CAMBIO: Texto de la pestaña a negro


        // Panel Mis Disciplinas
        JPanel enrolledPanel = new JPanel();
        enrolledPanel.setLayout(new BoxLayout(enrolledPanel, BoxLayout.Y_AXIS));
        enrolledPanel.setBackground(new Color(25, 30, 40));

        // Reconstruye las tarjetas de disciplinas inscritas usando los datos cargados
        if (inscripcionesUsuarioActivas.isEmpty()) {
            JLabel emptyLabel = new JLabel("🥋 No tienes disciplinas inscritas activamente.", SwingConstants.CENTER);
            emptyLabel.setFont(new Font("Arial", Font.BOLD, 18));
            emptyLabel.setForeground(Color.GRAY);
            emptyLabel.setBorder(BorderFactory.createEmptyBorder(50, 0, 0, 0));
            enrolledPanel.add(emptyLabel);
        } else {
            boolean hasActiveDisciplinesToShow = false;
            for (Map<String, String> dbDiscipline : allDisciplinesFromDB) {
                // Si la disciplina es una de las que el usuario tiene inscrita (por nombre)
                if (inscripcionesUsuarioActivas.contains(dbDiscipline.get("nombre"))) {
                    // Y si la disciplina está ACTIVA en la DB
                    if (Boolean.parseBoolean(dbDiscipline.get("activo"))) {
                        enrolledPanel.add(createDisciplineCard(new DisciplineDisplayInfo(dbDiscipline), true, username, frame));
                        enrolledPanel.add(Box.createVerticalStrut(15));
                        hasActiveDisciplinesToShow = true;
                    }
                }
            }
            if (!hasActiveDisciplinesToShow) { // Si después de filtrar no hay activas
                 JLabel emptyLabel = new JLabel("🥋 No tienes disciplinas inscritas activamente.", SwingConstants.CENTER);
                 emptyLabel.setFont(new Font("Arial", Font.BOLD, 18));
                 emptyLabel.setForeground(Color.GRAY);
                 emptyLabel.setBorder(BorderFactory.createEmptyBorder(50, 0, 0, 0));
                 enrolledPanel.add(emptyLabel);
            }
        }

        JScrollPane enrolledScroll = new JScrollPane(enrolledPanel);
        enrolledScroll.setBorder(null);
        tabbedPane.addTab("🎯 Mis Disciplinas", enrolledScroll);

        // Panel Disponibles
        JPanel availablePanel = new JPanel();
        availablePanel.setLayout(new BoxLayout(availablePanel, BoxLayout.Y_AXIS));
        availablePanel.setBackground(new Color(25, 30, 40));

        for (Map<String, String> dbDiscipline : allDisciplinesFromDB) {
            String disciplineName = dbDiscipline.get("nombre");
            boolean isActiveDB = Boolean.parseBoolean(dbDiscipline.get("activo"));

            if (!isActiveDB) { // Si la disciplina está inactiva en la DB, no la mostramos como disponible para inscripción
                continue;
            }

            Map<String, String> statusInfo = DataBase.obtenerEstadoInscripcionUsuario(username, disciplineName);
            String estadoGeneral = (statusInfo != null && statusInfo.containsKey("estado")) ? statusInfo.get("estado") : null;
            String razonRechazo = (statusInfo != null && statusInfo.containsKey("razon_inactivacion")) ? statusInfo.get("razon_inactivacion") : null;

            // Si el usuario no está inscrito activamente, o su inscripción anterior fue inactiva/completada
            if (estadoGeneral == null || "inactivo".equals(estadoGeneral) || "completado".equals(estadoGeneral)) {
                availablePanel.add(createDisciplineCard(new DisciplineDisplayInfo(dbDiscipline), false, username, frame));
                availablePanel.add(Box.createVerticalStrut(15));
            } else if ("rechazado".equals(estadoGeneral)) {
                // Si está rechazada, creamos la tarjeta con el estado de rechazo
                availablePanel.add(createDisciplineCard(new DisciplineDisplayInfo(dbDiscipline), false, username, frame, true, razonRechazo));
                availablePanel.add(Box.createVerticalStrut(15));
            }
            // Si el estadoGeneral es "activo", no la agregamos aquí porque ya está en "Mis Disciplinas"
        }

        JScrollPane availableScroll = new JScrollPane(availablePanel);
        availableScroll.setBorder(null);
        tabbedPane.addTab("⚔️ Disponibles", availableScroll);

        // Botón volver
        JButton btnVolver = new JButton("🏠 VOLVER");
        btnVolver.setFont(new Font("Arial", Font.BOLD, 16));
        btnVolver.setBackground(new Color(70, 80, 95));
        btnVolver.setForeground(Color.BLACK);
        btnVolver.setBorder(BorderFactory.createEmptyBorder(12, 25, 12, 25));
        btnVolver.addActionListener(e -> {
            frame.dispose();
            EntradaUser.mostrarPantalla(username); // Vuelve al dashboard del usuario
        });

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        mainPanel.add(btnVolver, BorderLayout.SOUTH);

        frame.add(mainPanel);
        frame.setVisible(true);
    }

    // Nuevo método createDisciplineCard que acepta un objeto DisciplineDisplayInfo
    private static JPanel createDisciplineCard(DisciplineDisplayInfo info, boolean enrolled, String username, JFrame frame) {
        return createDisciplineCard(info, enrolled, username, frame, false, null); // Llama al método completo por defecto
    }

    private static JPanel createDisciplineCard(DisciplineDisplayInfo info, boolean enrolled, String username, JFrame frame, boolean isRejected, String rejectionReason) {
        JPanel card = new JPanel(new BorderLayout(15, 15));
        card.setBackground(new Color(45, 55, 70));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(enrolled ? info.color : (isRejected ? new Color(239, 68, 68) : new Color(80, 90, 105)), 2),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        // ESPACIO PARA IMAGEN (Izquierda)
        JPanel imagePanel = new JPanel(new BorderLayout());
        imagePanel.setBackground(new Color(60, 70, 85));
        imagePanel.setPreferredSize(new Dimension(150, 150));
        imagePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(info.color, 2),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        try {
            // Intentamos cargar la imagen de la disciplina
            // Ahora solo convierte a minúsculas, sin reemplazar espacios.
            // Esto asume que los nombres de los archivos .jpg coinciden exactamente con los nombres de las disciplinas en minúsculas,
            // incluyendo espacios si los tienen. Por ejemplo, "muay thai.jpg".
            ImageIcon imageIcon = new ImageIcon(SeleccionDisciplinas.class.getResource("/imagenes/" + info.nombre.toLowerCase() + ".jpg"));
            // Si la imagen no se carga correctamente (ancho <= 0), usamos un placeholder
            if (imageIcon.getIconWidth() > 0) {
                Image image = imageIcon.getImage();
                Image newimg = image.getScaledInstance(130, 130, Image.SCALE_SMOOTH);
                imageIcon = new ImageIcon(newimg);

                JLabel imageLabel = new JLabel(imageIcon);
                imagePanel.add(imageLabel, BorderLayout.CENTER);
            } else {
                JLabel imagePlaceholder = new JLabel("<html><center>🖼️<br><br>IMAGEN<br>" + info.nombre + "</center></html>", SwingConstants.CENTER);
                imagePlaceholder.setFont(new Font("Arial", Font.BOLD, 12));
                imagePlaceholder.setForeground(info.color);
                imagePanel.add(imagePlaceholder, BorderLayout.CENTER);
            }
        } catch (Exception e) {
            // En caso de cualquier error al cargar la imagen (ej. archivo no encontrado)
            JLabel imagePlaceholder = new JLabel("<html><center>🖼️<br><br>IMAGEN<br>" + info.nombre + "</center></html>", SwingConstants.CENTER);
            imagePlaceholder.setFont(new Font("Arial", Font.BOLD, 12));
            imagePlaceholder.setForeground(info.color);
            imagePanel.add(imagePlaceholder, BorderLayout.CENTER);
            System.err.println("Error al cargar imagen para " + info.nombre + ": " + e.getMessage());
        }

        // Panel de información (Centro)
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(new Color(45, 55, 70));

        // Nombre
        JLabel nameLabel = new JLabel(info.nombre);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 24));
        nameLabel.setForeground(info.color);
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Estado
        JLabel statusLabel;
        if (enrolled) {
            statusLabel = new JLabel("✅ INSCRITO");
            statusLabel.setForeground(new Color(34, 197, 94));
        } else if (isRejected) {
            statusLabel = new JLabel("❌ INSCRIPCIÓN RECHAZADA");
            statusLabel.setForeground(new Color(239, 68, 68)); // Rojo para rechazado
        } else {
            statusLabel = new JLabel("⭐ DISPONIBLE");
            statusLabel.setForeground(new Color(255, 215, 0));
        }
        statusLabel.setFont(new Font("Arial", Font.BOLD, 12));
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 15, 0));

        // Razón de rechazo (si aplica)
        JTextArea rejectionReasonArea = null;
        if (isRejected && rejectionReason != null && !rejectionReason.isEmpty()) {
            rejectionReasonArea = new JTextArea("Razón: " + rejectionReason);
            rejectionReasonArea.setFont(new Font("Arial", Font.ITALIC, 12));
            rejectionReasonArea.setForeground(Color.LIGHT_GRAY);
            rejectionReasonArea.setBackground(new Color(45, 55, 70));
            rejectionReasonArea.setEditable(false);
            rejectionReasonArea.setWrapStyleWord(true);
            rejectionReasonArea.setLineWrap(true);
            rejectionReasonArea.setAlignmentX(Component.LEFT_ALIGNMENT);
            rejectionReasonArea.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        }

        // Descripción (descripción corta)
        JTextArea description = new JTextArea(info.descripcion);
        description.setFont(new Font("Arial", Font.PLAIN, 14));
        description.setForeground(new Color(200, 200, 200));
        description.setBackground(new Color(45, 55, 70));
        description.setEditable(false);
        description.setWrapStyleWord(true);
        description.setLineWrap(true);
        description.setAlignmentX(Component.LEFT_ALIGNMENT);
        description.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        // Detalles
        JPanel detailsPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        detailsPanel.setBackground(new Color(45, 55, 70));
        detailsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        addDetailRow(detailsPanel, "🕐 Horario:", info.horario);
        addDetailRow(detailsPanel, "👨‍🏫 Instructor:", info.instructor);
        addDetailRow(detailsPanel, "📈 Nivel:", info.nivel);

        infoPanel.add(nameLabel);
        infoPanel.add(statusLabel);
        if (rejectionReasonArea != null) { // Añade la razón si existe
            infoPanel.add(rejectionReasonArea);
        }
        infoPanel.add(description);
        infoPanel.add(detailsPanel);

        // Panel de botones (Derecha)
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setBackground(new Color(45, 55, 70));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        if (enrolled) {
            JButton btnProgress = createButton("📊 Progreso", new Color(10, 10, 10), Color.GREEN);
            btnProgress.addActionListener(e -> showProgress(frame, info.nombre, username, info));

            JButton btnCancel = createButton("❌ Cancelar", new Color(200, 0, 0), Color.RED);
            btnCancel.addActionListener(e -> confirmUnenroll(info.nombre, username, frame));

            buttonPanel.add(btnProgress);
            buttonPanel.add(Box.createVerticalStrut(10));
            buttonPanel.add(btnCancel);
        } else if (isRejected) { // Si está rechazada, solo botón de info, no se puede inscribir
            JButton btnInfo = createButton("ℹ️ Más Info", new Color(70, 70, 70), Color.PINK);
            btnInfo.addActionListener(e -> showMoreInfo(frame, info.nombre, info));
            
            // Botón Inscribirse deshabilitado y con texto indicativo
            JButton btnEnrollRejected = createButton("🚫 No Disponible", new Color(100, 100, 100), Color.LIGHT_GRAY);
            btnEnrollRejected.setEnabled(false); // Deshabilitar
            
            buttonPanel.add(btnInfo);
            buttonPanel.add(Box.createVerticalStrut(10));
            buttonPanel.add(btnEnrollRejected);

        } else { // Disponible para inscribirse
            JButton btnInfo = createButton("ℹ️ Más Info", new Color(70, 70, 70), Color.PINK);
            btnInfo.addActionListener(e -> showMoreInfo(frame, info.nombre, info));

            JButton btnEnroll = createButton("✅ Inscribirse", new Color(0, 150, 0), Color.GREEN);
            btnEnroll.addActionListener(e -> enroll(info.nombre, username, frame));

            buttonPanel.add(btnInfo);
            buttonPanel.add(Box.createVerticalStrut(10));
            buttonPanel.add(btnEnroll);
        }

        card.add(imagePanel, BorderLayout.WEST);
        card.add(infoPanel, BorderLayout.CENTER);
        card.add(buttonPanel, BorderLayout.EAST);

        return card;
    }

    private static void addDetailRow(JPanel parent, String label, String value) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        row.setBackground(new Color(45, 55, 70));

        JLabel lblLabel = new JLabel(label);
        lblLabel.setFont(new Font("Arial", Font.BOLD, 12));
        lblLabel.setForeground(new Color(150, 150, 150));

        JLabel lblValue = new JLabel(" " + value);
        lblValue.setFont(new Font("Arial", Font.PLAIN, 12));
        lblValue.setForeground(Color.WHITE);

        row.add(lblLabel);
        row.add(lblValue);
        parent.add(row);
    }

    private static JButton createButton(String text, Color bgColor, Color fgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setBackground(bgColor);
        button.setForeground(fgColor);
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setFocusPainted(false);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        return button;
    }

    private static void showProgress(JFrame parentFrame, String disciplina, String username, DisciplineDisplayInfo info) {
        // Esta función sigue mostrando un mensaje estático.
        // Para que sea dinámica, necesitarías integrar los datos de 'grados' y 'examenes'
        // del usuario desde la base de datos (usando DataBase.obtenerGradosUsuario y obtenerExamenesDisciplina)
        // y generar un resumen más real.
        String progressMessage;
        switch (disciplina) {
            case "Karate":
                progressMessage = "¡Excelente progreso en Karate, " + username + "!\n" +
                                  "Has demostrado gran dedicación. Tu próximo cinturón es el Naranja.\n" +
                                  "Asistencia: 85% | Última Evaluación: 9/10";
                break;
            case "Judo":
                progressMessage = "Tu desempeño en Judo, " + username + ", es notable.\n" +
                                  "Sigue practicando tus lanzamientos. Objetivo: Cinturón Verde.\n" +
                                  "Asistencia: 90% | Última Evaluación: 8.5/10";
                break;
            case "Taekwondo":
                progressMessage = "¡Felicidades en Taekwondo, " + username + "!\n" +
                                  "Tus patadas son cada vez más precisas. ¡Prepárate para tu examen de Cinturón Azul!\n" +
                                  "Asistencia: 88% | Última Evaluación: 9.2/10";
                break;
            default:
                progressMessage = "Progreso de " + disciplina + " para " + username + ":\n" +
                                  "Actualmente, tu nivel es '" + info.nivel + "'.\n" +
                                  "¡Sigue trabajando duro para alcanzar tus metas!";
                break;
        }

        JOptionPane.showMessageDialog(
            parentFrame,
            progressMessage,
            "📊 Progreso en " + disciplina,
            JOptionPane.INFORMATION_MESSAGE,
            null
        );
    }

    private static void showMoreInfo(JFrame parentFrame, String disciplina, DisciplineDisplayInfo info) {
        String fullInfoMessage = "<html>" +
                                 "<b>" + disciplina.toUpperCase() + "</b><br><br>" +
                                 "<b>Descripción:</b> " + info.extendedDescription + "<br><br>" +
                                 "<b>Horario:</b> " + info.horario + "<br>" +
                                 "<b>Instructor:</b> " + info.instructor + "<br>" +
                                 "<b>Nivel:</b> " + info.nivel + "<br><br>" +
                                 "<span style='color:green;'>¡Inscríbete hoy y comienza tu entrenamiento!</span>" +
                                 "</html>";

        JOptionPane.showMessageDialog(
            parentFrame,
            fullInfoMessage,
            "ℹ️ Más Información sobre " + disciplina,
            JOptionPane.INFORMATION_MESSAGE,
            null
        );
    }

    private static void enroll(String disciplina, String username, JFrame frame) {
        // Esta validación ya la hace DataBase.inscribirUsuarioEnDisciplina.
        // Si el estado es "rechazado", DataBase.inscribirUsuarioEnDisciplina
        // mostrará un JOptionPane y devolverá false.
        
        // Buscamos la información de la disciplina cargada de la DB
        DisciplineDisplayInfo info = null;
        for (Map<String, String> dbDiscipline : allDisciplinesFromDB) {
            if (dbDiscipline.get("nombre").equals(disciplina)) {
                info = new DisciplineDisplayInfo(dbDiscipline);
                break;
            }
        }

        String firstClassTime = "Horario no disponible";
        if (info != null && info.horario != null && !info.horario.isEmpty()) {
            String[] parts = info.horario.split(" ");
            if (parts.length >= 3) {
                firstClassTime = parts[0] + " a las " + parts[parts.length - 1];
            } else {
                firstClassTime = info.horario;
            }
        }

        int confirm = JOptionPane.showConfirmDialog(
            frame,
            "¿Confirmas tu inscripción en " + disciplina + "?\n\n" +
            "Horario: " + (info != null ? info.horario : "No disponible") + "\n" +
            "Instructor: " + (info != null ? info.instructor : "No disponible"),
            "Confirmar Inscripción",
            JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            if (logear.DataBase.inscribirUsuarioEnDisciplina(username, disciplina)) {
                JOptionPane.showMessageDialog(
                    frame,
                    "¡Inscripción exitosa en " + disciplina + "!\n\n" +
                    "¡Te esperamos el " + firstClassTime + " en tu primera clase!",
                    "¡Bienvenido!",
                    JOptionPane.INFORMATION_MESSAGE
                );
                // Si la inscripción fue exitosa en la DB, recarga la pantalla
                frame.dispose();
                mostrar(username);
            } else {
                // DataBase.inscribirUsuarioEnDisciplina ya maneja los JOptionPane de error (ej. ya inscrito o rechazado)
            }
        }
    }

    private static void confirmUnenroll(String disciplina, String username, JFrame frame) {
        int confirm = JOptionPane.showConfirmDialog(
            frame,
            "¿Seguro que quieres cancelar " + disciplina + "?", // Mensaje simplificado
            "Confirmar Cancelación",
            JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            // Ya no se pide la razón al usuario
            String reason = ""; // La razón se deja vacía o con un valor predeterminado interno.
            
            if (logear.DataBase.desinscribirUsuarioDeDisciplina(username, disciplina, reason.trim())) {
                JOptionPane.showMessageDialog(frame, "Inscripción cancelada en " + disciplina + ".");
                // Si la desinscripción fue exitosa en la DB, recarga la pantalla
                frame.dispose();
                mostrar(username);
            } else {
                // DataBase.desinscribirUsuarioDeDisciplina ya maneja los JOptionPane de error
            }
        }
    }

    public static List<String> loadUserInscriptions(String username) {
        return logear.DataBase.obtenerDisciplinasInscritas(username);
    }

    public static void saveUserInscriptions(String username, List<String> inscriptions) {
        System.out.println("ADVERTENCIA: saveUserInscriptions llamado, pero las inscripciones ahora se gestionan en la base de datos.");
    }
}