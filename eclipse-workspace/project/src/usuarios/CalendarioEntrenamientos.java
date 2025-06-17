package usuarios;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter; // Importación añadida
import java.awt.event.MouseEvent;   // Importación añadida
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import logear.DataBase;

public class CalendarioEntrenamientos {

    private static LocalDate currentMonthView; // Controla el mes que se está mostrando en el calendario
    private static JPanel calendarPanel; // Panel donde se dibuja el calendario
    private static JLabel monthYearLabel; // Etiqueta para mostrar Mes y Año
    private static String currentUsername; // Para mantener el username al navegar meses

    // Almacenará todas las disciplinas cargadas de la DB para acceder a sus horarios
    private static List<Map<String, String>> allDisciplinesFromDB = new ArrayList<>();

    // Reutilizamos la clase DisciplineDisplayInfo de SeleccionDisciplinas para un manejo estructurado
    public static class DisciplineDisplayInfo {
        String id;
        String nombre;
        String descripcion;
        String horario;
        String instructor;
        String nivel;
        boolean activo;
        String extendedDescription;
        Color color;

        public DisciplineDisplayInfo(Map<String, String> dbData) {
            this.id = dbData.get("id");
            this.nombre = dbData.get("nombre");
            this.descripcion = dbData.get("descripcion");
            this.horario = dbData.get("horario");
            this.instructor = dbData.get("instructor");
            this.nivel = dbData.get("nivel");
            this.activo = Boolean.parseBoolean(dbData.get("activo"));
            this.extendedDescription = dbData.get("descripcion"); // Usamos la descripción como extendida
            this.color = getColorForDiscipline(this.nombre);
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

    // Carga TODAS las disciplinas de la DB
    private static void loadAllDisciplinesFromDB() {
        allDisciplinesFromDB = DataBase.obtenerTodasLasDisciplinas(true);
        System.out.println("DEBUG: CalendarioEntrenamientos.loadAllDisciplinesFromDB() cargó " + allDisciplinesFromDB.size() + " disciplinas.");
    }

    public static void mostrar(String username) {
        currentUsername = username; // Guardamos el username
        currentMonthView = LocalDate.now(); // Empezar en el mes actual

        loadAllDisciplinesFromDB(); // Cargar disciplinas al mostrar el calendario

        JFrame frame = new JFrame("📅 Calendario de Entrenamientos - " + username);
        frame.setSize(1000, 700);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(new Color(25, 30, 40));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(40, 50, 65));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        JLabel titleLabel = new JLabel("📅 CALENDARIO DE ENTRENAMIENTOS", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(new Color(255, 215, 0));
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        JLabel userLabel = new JLabel("Usuario: " + username, SwingConstants.RIGHT);
        userLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        userLabel.setForeground(Color.WHITE);
        userLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20));
        headerPanel.add(userLabel, BorderLayout.EAST);

        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Panel de navegación del calendario (botones mes anterior/siguiente)
        JPanel navPanel = new JPanel(new BorderLayout(10, 0));
        navPanel.setBackground(new Color(45, 55, 70));
        navPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JButton prevMonthBtn = createNavButton("◀️ Mes Anterior");
        prevMonthBtn.addActionListener(e -> navigateMonth(-1));
        navPanel.add(prevMonthBtn, BorderLayout.WEST);

        monthYearLabel = new JLabel("", SwingConstants.CENTER);
        monthYearLabel.setFont(new Font("Arial", Font.BOLD, 20));
        monthYearLabel.setForeground(new Color(255, 215, 0));
        navPanel.add(monthYearLabel, BorderLayout.CENTER);

        JButton nextMonthBtn = createNavButton("Mes Siguiente ▶️");
        nextMonthBtn.addActionListener(e -> navigateMonth(1));
        navPanel.add(nextMonthBtn, BorderLayout.EAST);

        mainPanel.add(navPanel, BorderLayout.CENTER);

        // Panel para el calendario dinámico
        calendarPanel = new JPanel(new GridLayout(0, 7, 5, 5)); // 7 columnas para los días de la semana
        calendarPanel.setBackground(new Color(25, 30, 40));
        calendarPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane calendarScrollPane = new JScrollPane(calendarPanel);
        calendarScrollPane.setBorder(null);
        calendarScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        calendarScrollPane.getVerticalScrollBar().setBackground(new Color(25, 30, 40));
        calendarScrollPane.getVerticalScrollBar().setForeground(new Color(70, 80, 95));

        // Un JPanel para contener el calendarScrollPane con BorderLayout.CENTER
        // y así el navPanel y los botones de volver no interfieran con su BorderLayout
        JPanel centerContentPanel = new JPanel(new BorderLayout());
        centerContentPanel.setBackground(new Color(25, 30, 40));
        centerContentPanel.add(navPanel, BorderLayout.NORTH);
        centerContentPanel.add(calendarScrollPane, BorderLayout.CENTER);


        mainPanel.add(centerContentPanel, BorderLayout.CENTER); // Añadir el panel central al mainPanel

        // Botón volver
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
        updateCalendarDisplay(); // Cargar y mostrar el calendario inicial
        frame.setVisible(true);
    }

    private static JButton createNavButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(new Color(59, 130, 246));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        return button;
    }

    private static void navigateMonth(int monthsToAdd) {
        currentMonthView = currentMonthView.plusMonths(monthsToAdd);
        updateCalendarDisplay(); // Re-dibujar el calendario
    }

    private static void updateCalendarDisplay() {
        calendarPanel.removeAll(); // Limpiar el panel del calendario
        
        // Formato para mostrar Mes y Año (ej. "Junio 2025")
        // Se corrige el patrón para que sea válido y funcione en todas las versiones de Java.
        // `MMMM` para el nombre completo del mes, `yyyy` para el año.
        monthYearLabel.setText(currentMonthView.format(
            DateTimeFormatter.ofPattern("MMMM yyyy", new Locale("es", "ES")) 
        ).toUpperCase());

        // Días de la semana
        String[] dayNames = {"LUNES", "MARTES", "MIÉRCOLES", "JUEVES", "VIERNES", "SÁBADO", "DOMINGO"};
        for (String dayName : dayNames) {
            JLabel label = new JLabel(dayName, SwingConstants.CENTER);
            label.setFont(new Font("Arial", Font.BOLD, 14));
            label.setForeground(new Color(255, 215, 0)); // Color dorado
            calendarPanel.add(label);
        }

        YearMonth yearMonth = YearMonth.from(currentMonthView);
        LocalDate firstOfMonth = currentMonthView.withDayOfMonth(1);
        int daysInMonth = yearMonth.lengthOfMonth();

        // Calcular el desplazamiento del primer día del mes (Lunes = 1, Domingo = 7)
        int firstDayOfWeek = firstOfMonth.getDayOfWeek().getValue(); // 1 para LUNES, 7 para DOMINGO
        int offset = (firstDayOfWeek == 7) ? 6 : (firstDayOfWeek - 1); // Ajustar para que Lunes sea el primer día de la semana en la UI

        // Rellenar días vacíos al principio del mes
        for (int i = 0; i < offset; i++) {
            JPanel emptyDay = new JPanel();
            emptyDay.setBackground(new Color(35, 40, 50)); // Fondo oscuro para días vacíos
            calendarPanel.add(emptyDay);
        }

        // Obtener las disciplinas en las que el usuario está inscrito activamente
        List<String> inscripcionesUsuarioActivas = DataBase.obtenerDisciplinasInscritas(currentUsername);

        // Crear una lista de DisciplineDisplayInfo para las disciplinas del usuario
        List<DisciplineDisplayInfo> userEnrolledDisciplinesInfo = new ArrayList<>();
        for (Map<String, String> dbDiscipline : allDisciplinesFromDB) {
            if (inscripcionesUsuarioActivas.contains(dbDiscipline.get("nombre")) &&
                Boolean.parseBoolean(dbDiscipline.get("activo"))) { // Solo disciplinas activas
                userEnrolledDisciplinesInfo.add(new DisciplineDisplayInfo(dbDiscipline));
            }
        }

        // Añadir días del mes
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = currentMonthView.withDayOfMonth(day);
            calendarPanel.add(createDayCard(date, userEnrolledDisciplinesInfo));
        }

        calendarPanel.revalidate();
        calendarPanel.repaint();
    }

    private static JPanel createDayCard(LocalDate date, List<DisciplineDisplayInfo> userEnrolledDisciplinesInfo) {
        JPanel dayCard = new JPanel(new BorderLayout());
        dayCard.setBackground(new Color(60, 70, 85)); // Fondo por defecto para los días
        dayCard.setBorder(BorderFactory.createLineBorder(new Color(80, 90, 105), 1)); // Borde gris claro

        JLabel dayLabel = new JLabel(String.valueOf(date.getDayOfMonth()), SwingConstants.CENTER);
        dayLabel.setFont(new Font("Arial", Font.BOLD, 16));
        dayLabel.setForeground(Color.WHITE);
        dayCard.add(dayLabel, BorderLayout.NORTH);

        // Panel para listar las clases del día
        JPanel classesPanel = new JPanel();
        classesPanel.setLayout(new BoxLayout(classesPanel, BoxLayout.Y_AXIS));
        classesPanel.setBackground(new Color(60, 70, 85)); // Mismo color de fondo
        classesPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Se define una lista final para ser capturada por la lambda del MouseAdapter
        final List<DisciplineDisplayInfo> classesTodayInfo = new ArrayList<>(); 

        // Identificar si hay clases para este día de la semana
        DayOfWeek currentDayOfWeek = date.getDayOfWeek();
        // Obtener el nombre del día de la semana en español para comparar con el horario
        // Usamos DayOfWeek.getDisplayName para obtener el nombre completo en minúsculas.
        String dayOfWeekSpanish = currentDayOfWeek.getDisplayName(TextStyle.FULL, new Locale("es", "ES")).toLowerCase();

        for (DisciplineDisplayInfo discipline : userEnrolledDisciplinesInfo) {
            // Suponemos que el horario contiene el día de la semana en español (ej. "Lunes y Miércoles 18:00-20:00")
            // Comparamos si el horario de la disciplina contiene el nombre del día actual.
            if (discipline.horario != null && discipline.horario.toLowerCase().contains(dayOfWeekSpanish)) {
                classesTodayInfo.add(discipline); // Agregamos el objeto completo de la disciplina
            }
        }
        
        // Ahora, 'hasClassesForDay' se deriva de si la lista de clases del día tiene elementos.
        // Esto la hace efectivamente final y elimina el error.
        final boolean hasClassesForDay = !classesTodayInfo.isEmpty();

        if (hasClassesForDay) { 
            // Resaltar el día con clases
            dayCard.setBackground(new Color(34, 139, 34)); // Un verde oscuro para días con clases
            dayCard.setBorder(BorderFactory.createLineBorder(new Color(50, 205, 50), 2)); // Borde verde más fuerte
            dayLabel.setForeground(Color.YELLOW); // Color de número de día para resaltar

            // Mostrar un indicador o listar las clases directamente
            for(DisciplineDisplayInfo discipline : classesTodayInfo) { // Iteramos sobre classesTodayInfo
                // Solo mostramos el nombre de la disciplina
                JLabel classIndicator = new JLabel("• " + discipline.nombre, SwingConstants.LEFT);
                classIndicator.setFont(new Font("Arial", Font.PLAIN, 10));
                classIndicator.setForeground(Color.WHITE);
                classIndicator.setToolTipText("Clase: " + discipline.nombre + " (" + discipline.horario + ")"); // Tooltip para más detalles
                classesPanel.add(classIndicator);
            }
        }

        dayCard.add(classesPanel, BorderLayout.CENTER);

        // Añadir un listener para mostrar detalles de las clases al hacer clic en el día
        dayCard.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (hasClassesForDay) { // Usamos la variable efectivamente final
                    StringBuilder details = new StringBuilder("Clases para el " + date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + ":\n");
                    for (DisciplineDisplayInfo discipline : classesTodayInfo) { // Usamos la lista final
                        details.append("\n- ").append(discipline.nombre)
                               .append("\n  Horario: ").append(discipline.horario)
                               .append("\n  Instructor: ").append(discipline.instructor)
                               .append("\n  Nivel: ").append(discipline.nivel);
                    }
                    JOptionPane.showMessageDialog(dayCard, details.toString(), "Detalles de Clases", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(dayCard, "No hay clases programadas para el " + date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), "Sin Clases", JOptionPane.INFORMATION_MESSAGE);
                }
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                dayCard.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                dayCard.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });

        return dayCard;
    }
}