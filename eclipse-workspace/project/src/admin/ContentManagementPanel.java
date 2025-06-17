package admin;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.FocusAdapter; 
import java.awt.event.FocusEvent;   
import java.awt.event.KeyAdapter;   
import java.awt.event.KeyEvent;     
import java.util.ArrayList;
import java.util.List;
import java.util.Map; 
import java.util.Vector;
import java.util.Date;

import com.toedter.calendar.JDateChooser;

import logear.DataBase;
// Importamos SeleccionDisciplinas solo para referencia, ya no para obtener la info hardcodeada.
import usuarios.SeleccionDisciplinas;

public class ContentManagementPanel {

    private static JList<String> userList; // Lista de todos los usuarios
    private static DefaultListModel<String> userListModel;

    private static JList<String[]> gradesList; // Lista de grados del usuario seleccionado
    private static DefaultListModel<String[]> gradesListModel;

    private static JList<String[]> examList; // Lista de exámenes de la disciplina seleccionada
    private static DefaultListModel<String[]> examListModel;

    private static JComboBox<String> disciplineFilterComboBox; // Para filtrar exámenes por disciplina
    private static String currentExamFilterDiscipline = "Todas";

    // Botones para controlar habilitación/deshabilitación (gestionado internamente)
    private static JButton btnAddExam;
    private static JButton btnDeleteExam;


    public static JPanel createPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(new Color(45, 55, 70));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Gestión de Progreso y Exámenes", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(new Color(255, 215, 0)); // Título principal se mantiene en dorado
        panel.add(title, BorderLayout.NORTH);

        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplitPane.setDividerLocation(200);
        mainSplitPane.setBackground(new Color(45, 55, 70));
        mainSplitPane.setForeground(new Color(80, 90, 105));


        // --- Panel Izquierdo: Lista de Usuarios ---
        JPanel userSelectionPanel = new JPanel(new BorderLayout(10, 10));
        userSelectionPanel.setBackground(new Color(60, 70, 85));
        userSelectionPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(80, 90, 105)), "Seleccionar Usuario",
            0, 2, new Font("Arial", Font.BOLD, 14), Color.WHITE)); // Título de borde en blanco

        // Campo de búsqueda/filtro para la lista de usuarios
        JTextField searchUserField = new JTextField("Buscar usuario...");
        searchUserField.setFont(new Font("Arial", Font.ITALIC, 13));
        searchUserField.setForeground(Color.GRAY); // Placeholder
        searchUserField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(80, 90, 105)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        searchUserField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (searchUserField.getText().equals("Buscar usuario...")) {
                    searchUserField.setText("");
                    searchUserField.setForeground(Color.BLACK); // CAMBIO: Texto a negro al ganar foco
                    searchUserField.setFont(new Font("Arial", Font.PLAIN, 14));
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (searchUserField.getText().isEmpty()) {
                    searchUserField.setText("Buscar usuario...");
                    searchUserField.setForeground(Color.GRAY); // Placeholder vuelve a gris
                    searchUserField.setFont(new Font("Arial", Font.ITALIC, 13));
                } else {
                    searchUserField.setForeground(Color.BLACK); // CAMBIO: Texto a negro al perder foco si tiene contenido
                }
            }
        });
        searchUserField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filterUsers(searchUserField.getText()); // Mantiene la llamada al método filterUsers
            }
        });
        userSelectionPanel.add(searchUserField, BorderLayout.NORTH); // Añadir el campo de búsqueda

        userListModel = new DefaultListModel<>();
        loadAllUsersForContentManagement();
        userList = new JList<>(userListModel);
        userList.setFont(new Font("Arial", Font.PLAIN, 16));
        userList.setBackground(new Color(60, 70, 85));
        userList.setForeground(Color.WHITE); // Texto de la lista de usuarios
        userList.setSelectionBackground(new Color(59, 130, 246));
        userList.setSelectionForeground(Color.WHITE);
        userList.setBorder(BorderFactory.createLineBorder(new Color(80, 90, 105)));

        JScrollPane userScrollPane = new JScrollPane(userList);
        userScrollPane.setBorder(null);
        userSelectionPanel.add(userScrollPane, BorderLayout.CENTER);
        mainSplitPane.setLeftComponent(userSelectionPanel);


        // --- Panel Derecho: Gestión de Grados y Exámenes ---
        JTabbedPane contentTabs = new JTabbedPane();
        contentTabs.setFont(new Font("Arial", Font.BOLD, 14));
        contentTabs.setBackground(new Color(60, 70, 85));
        contentTabs.setForeground(Color.WHITE); // Se mantiene en blanco, no fue especificado para cambiar a negro.

        // Pestaña de Grados
        contentTabs.addTab("📈 Gestionar Grados", createGradesManagementPanel());
        // Pestaña de Exámenes
        contentTabs.addTab("📅 Gestionar Exámenes", createExamsManagementPanel());

        mainSplitPane.setRightComponent(contentTabs);
        panel.add(mainSplitPane, BorderLayout.CENTER);

        // Listener para la selección de usuario
        userList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    String selectedUsername = userList.getSelectedValue();
                    if (selectedUsername != null) {
                        refreshGradesList(selectedUsername);
                        // Los exámenes se refrescan con el filtro de disciplina, no por usuario seleccionado
                    } else {
                        gradesListModel.clear();
                    }
                }
            }
        });

        return panel;
    }

    // --- Panel de Gestión de Grados ---
    private static JPanel createGradesManagementPanel() {
        JPanel gradesPanel = new JPanel(new BorderLayout(10, 10));
        gradesPanel.setBackground(new Color(75, 85, 100));
        gradesPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        gradesListModel = new DefaultListModel<>();
        gradesList = new JList<>(gradesListModel);
        gradesList.setFont(new Font("Arial", Font.PLAIN, 14));
        gradesList.setBackground(new Color(90, 100, 115));
        gradesList.setForeground(Color.WHITE); // Color de texto de la lista de grados
        gradesList.setSelectionBackground(new Color(16, 185, 129));
        gradesList.setSelectionForeground(Color.WHITE);
        gradesList.setCellRenderer(new GradeListCellRenderer());

        JScrollPane gradesScrollPane = new JScrollPane(gradesList);
        gradesScrollPane.setBorder(BorderFactory.createLineBorder(new Color(100, 110, 125)));
        gradesPanel.add(gradesScrollPane, BorderLayout.CENTER);

        // Formulario para añadir/eliminar grados
        JPanel addDeleteGradePanel = new JPanel(new GridBagLayout());
        addDeleteGradePanel.setBackground(new Color(90, 100, 115));
        addDeleteGradePanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(100, 110, 125)), "Añadir/Eliminar Grado",
            0, 2, new Font("Arial", Font.BOLD, 12), Color.LIGHT_GRAY)); // Título de borde en gris claro

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // !! AHORA LAS DISCIPLINAS SE CARGAN DE LA DB (SOLO ACTIVAS) !!
        Vector<String> disciplineNamesForGrades = new Vector<>();
        for (Map<String, String> discipline : DataBase.obtenerTodasLasDisciplinas(false)) { // Solo disciplinas activas
            disciplineNamesForGrades.add(discipline.get("nombre"));
        }
        JComboBox<String> disciplineComboBox = new JComboBox<>(disciplineNamesForGrades);
        disciplineComboBox.setForeground(Color.BLACK); // CAMBIO: Texto del JComboBox a negro
        // FIN CAMBIO

        JTextField gradeField = new JTextField(15);
        gradeField.setForeground(Color.BLACK); // CAMBIO: Texto del campo de grado a negro
        gradeField.addFocusListener(new FocusAdapter() { // Añadido para gestionar el color del texto
            @Override public void focusGained(FocusEvent e) { gradeField.setForeground(Color.BLACK); }
            @Override public void focusLost(FocusEvent e) { if (gradeField.getText().isEmpty()) gradeField.setForeground(Color.BLACK); }
        });

        JTextField notesField = new JTextField(20);
        notesField.setForeground(Color.BLACK); // CAMBIO: Texto del campo de notas a negro
        notesField.addFocusListener(new FocusAdapter() { // Añadido para gestionar el color del texto
            @Override public void focusGained(FocusEvent e) { notesField.setForeground(Color.BLACK); }
            @Override public void focusLost(FocusEvent e) { if (notesField.getText().isEmpty()) notesField.setForeground(Color.BLACK); }
        });

        JDateChooser dateChooser = new JDateChooser();
        dateChooser.setDateFormatString("dd/MM/yyyy");
        dateChooser.setDate(new Date());
        dateChooser.setForeground(Color.BLACK); // CAMBIO: Texto del JDateChooser a negro (componente interno)

        gbc.gridx = 0; gbc.gridy = 0; addDeleteGradePanel.add(new JLabel("Disciplina:"), gbc);
        gbc.gridx = 1; addDeleteGradePanel.add(disciplineComboBox, gbc);
        gbc.gridx = 0; gbc.gridy = 1; addDeleteGradePanel.add(new JLabel("Grado Obtenido:"), gbc);
        gbc.gridx = 1; addDeleteGradePanel.add(gradeField, gbc);
        gbc.gridx = 0; gbc.gridy = 2; addDeleteGradePanel.add(new JLabel("Fecha de Obtención:"), gbc);
        gbc.gridx = 1; addDeleteGradePanel.add(dateChooser, gbc);
        gbc.gridx = 0; gbc.gridy = 3; addDeleteGradePanel.add(new JLabel("Notas (opcional):"), gbc);
        gbc.gridx = 1; addDeleteGradePanel.add(notesField, gbc);

        // Botones de Grados - utilizan createAdminButton, que ya pone el texto en negro
        JButton btnAddGrade = createAdminButton("Añadir Grado", new Color(34, 197, 94));
        JButton btnDeleteGrade = createAdminButton("Eliminar Grado Seleccionado", new Color(239, 68, 68));

        JPanel gradeButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        gradeButtonsPanel.setBackground(new Color(90, 100, 115));
        gradeButtonsPanel.add(btnAddGrade);
        gradeButtonsPanel.add(btnDeleteGrade);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2; addDeleteGradePanel.add(gradeButtonsPanel, gbc);

        gradesPanel.add(addDeleteGradePanel, BorderLayout.SOUTH);

        // Lógica de los botones de grado
        btnAddGrade.addActionListener(e -> {
            String selectedUsername = userList.getSelectedValue();
            if (selectedUsername == null) {
                JOptionPane.showMessageDialog(gradesPanel, "Por favor, selecciona un usuario.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String discipline = (String) disciplineComboBox.getSelectedItem();
            String grade = gradeField.getText().trim();
            String notes = notesField.getText().trim();
            Date gradeDate = dateChooser.getDate();

            if (discipline == null || grade.isEmpty() || gradeDate == null) {
                JOptionPane.showMessageDialog(gradesPanel, "Disciplina, grado y fecha son obligatorios.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String gradeDateStr = new java.text.SimpleDateFormat("dd/MM/yyyy").format(gradeDate);

            if (DataBase.añadirGrado(selectedUsername, discipline, grade, gradeDateStr, notes)) {
                JOptionPane.showMessageDialog(gradesPanel, "Grado añadido con éxito.");
                gradeField.setText(""); notesField.setText(""); dateChooser.setDate(new Date());
                refreshGradesList(selectedUsername);
            }
        });

        btnDeleteGrade.addActionListener(e -> {
            String selectedUsername = userList.getSelectedValue();
            String[] selectedGradeInfo = gradesList.getSelectedValue();
            if (selectedUsername == null || selectedGradeInfo == null) {
                JOptionPane.showMessageDialog(gradesPanel, "Selecciona un usuario y un grado a eliminar.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(gradesPanel, "¿Seguro que quieres eliminar el grado '" + selectedGradeInfo[1] + "' de " + selectedUsername + "?", "Confirmar Eliminación", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                if (DataBase.eliminarGrado(selectedUsername, selectedGradeInfo[0], selectedGradeInfo[1])) {
                    JOptionPane.showMessageDialog(gradesPanel, "Grado eliminado con éxito.");
                    refreshGradesList(selectedUsername);
                }
            }
        });

        return gradesPanel;
    }

    static class GradeListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof String[]) {
                String[] gradeInfo = (String[]) value;
                // Formato: [Disciplina] Grado - Fecha (Notas)
                String displayText = String.format("[%s] %s - %s%s",
                                                    gradeInfo[0],
                                                    gradeInfo[1],
                                                    gradeInfo[2],
                                                    (gradeInfo[3] != null && !gradeInfo[3].isEmpty() ? " (" + gradeInfo[3] + ")" : ""));
                setText(displayText);
                setForeground(Color.BLACK); // CAMBIO: Texto del renderer a negro
            }
            return this;
        }
    }


    // --- Panel de Gestión de Exámenes ---
    private static JPanel createExamsManagementPanel() {
        JPanel examsPanel = new JPanel(new BorderLayout(10, 10));
        examsPanel.setBackground(new Color(75, 85, 100));
        examsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Filtro por disciplina
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filterPanel.setBackground(new Color(90, 100, 115));
        filterPanel.add(new JLabel("Filtrar por Disciplina:")); // Texto de label

        // !! AHORA LAS DISCIPLINAS SE CARGAN DE LA DB (SOLO ACTIVAS) !!
        Vector<String> disciplinesForFilter = new Vector<>();
        disciplinesForFilter.add("Todas");
        for (Map<String, String> discipline : DataBase.obtenerTodasLasDisciplinas(false)) { // Solo disciplinas activas
            disciplinesForFilter.add(discipline.get("nombre"));
        }
        disciplineFilterComboBox = new JComboBox<>(disciplinesForFilter);
        disciplineFilterComboBox.setForeground(Color.BLACK); // CAMBIO: Texto del JComboBox a negro
        // FIN CAMBIO

        filterPanel.add(disciplineFilterComboBox);

        examsPanel.add(filterPanel, BorderLayout.NORTH);


        examListModel = new DefaultListModel<>();
        examList = new JList<>(examListModel);
        examList.setFont(new Font("Arial", Font.PLAIN, 14));
        examList.setBackground(new Color(90, 100, 115));
        examList.setForeground(Color.WHITE); // Texto de la lista de exámenes
        examList.setSelectionBackground(new Color(59, 130, 246));
        examList.setSelectionForeground(Color.WHITE);
        examList.setCellRenderer(new ExamListCellRenderer());

        JScrollPane examScrollPane = new JScrollPane(examList);
        examScrollPane.setBorder(BorderFactory.createLineBorder(new Color(100, 110, 125)));
        examsPanel.add(examScrollPane, BorderLayout.CENTER);

        // Formulario para añadir/eliminar exámenes
        JPanel addDeleteExamPanel = new JPanel(new GridBagLayout());
        addDeleteExamPanel.setBackground(new Color(90, 100, 115));
        addDeleteExamPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(100, 110, 125)), "Añadir/Eliminar Examen",
            0, 2, new Font("Arial", Font.BOLD, 12), Color.LIGHT_GRAY)); // Título de borde en gris claro

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // !! AHORA LAS DISCIPLINAS SE CARGAN DE LA DB (SOLO ACTIVAS) !!
        Vector<String> examDisciplineNames = new Vector<>();
        for (Map<String, String> discipline : DataBase.obtenerTodasLasDisciplinas(false)) { // Solo disciplinas activas
            examDisciplineNames.add(discipline.get("nombre"));
        }
        JComboBox<String> examDisciplineComboBox = new JComboBox<>(examDisciplineNames);
        examDisciplineComboBox.setForeground(Color.BLACK); // CAMBIO: Texto del JComboBox a negro
        // FIN CAMBIO

        JTextField examTypeField = new JTextField(15);
        examTypeField.setForeground(Color.BLACK); // CAMBIO: Texto del campo de tipo de examen a negro
        examTypeField.addFocusListener(new FocusAdapter() { // Añadido para gestionar el color del texto
            @Override public void focusGained(FocusEvent e) { examTypeField.setForeground(Color.BLACK); }
            @Override public void focusLost(FocusEvent e) { if (examTypeField.getText().isEmpty()) examTypeField.setForeground(Color.BLACK); }
        });

        JDateChooser examDateChooser = new JDateChooser();
        examDateChooser.setDateFormatString("dd/MM/yyyy");
        examDateChooser.setDate(new Date());
        examDateChooser.setForeground(Color.BLACK); // CAMBIO: Texto del JDateChooser a negro (componente interno)

        JTextField examTimeField = new JTextField(10);
        examTimeField.setForeground(Color.BLACK); // CAMBIO: Texto del campo de hora de examen a negro
        examTimeField.addFocusListener(new FocusAdapter() { // Añadido para gestionar el color del texto
            @Override public void focusGained(FocusEvent e) { examTimeField.setForeground(Color.BLACK); }
            @Override public void focusLost(FocusEvent e) { if (examTimeField.getText().isEmpty()) examTimeField.setForeground(Color.BLACK); }
        });

        JTextArea examDescriptionArea = new JTextArea(3, 15);
        examDescriptionArea.setLineWrap(true);
        examDescriptionArea.setWrapStyleWord(true);
        examDescriptionArea.setForeground(Color.BLACK); // CAMBIO: Texto del campo de descripción a negro
        examDescriptionArea.addFocusListener(new FocusAdapter() { // Añadido para gestionar el color del texto
            @Override public void focusGained(FocusEvent e) { examDescriptionArea.setForeground(Color.BLACK); }
            @Override public void focusLost(FocusEvent e) { if (examDescriptionArea.getText().isEmpty()) examDescriptionArea.setForeground(Color.BLACK); }
        });
        JScrollPane examDescriptionScrollPane = new JScrollPane(examDescriptionArea);

        gbc.gridx = 0; gbc.gridy = 0; addDeleteExamPanel.add(new JLabel("Disciplina:"), gbc);
        gbc.gridx = 1; addDeleteExamPanel.add(examDisciplineComboBox, gbc);
        gbc.gridx = 0; gbc.gridy = 1; addDeleteExamPanel.add(new JLabel("Tipo de Examen:"), gbc);
        gbc.gridx = 1; addDeleteExamPanel.add(examTypeField, gbc);
        gbc.gridx = 0; gbc.gridy = 2; addDeleteExamPanel.add(new JLabel("Fecha del Examen:"), gbc);
        gbc.gridx = 1; addDeleteExamPanel.add(examDateChooser, gbc);
        gbc.gridx = 0; gbc.gridy = 3; addDeleteExamPanel.add(new JLabel("Hora (ej. 10:00 AM):"), gbc);
        gbc.gridx = 1; addDeleteExamPanel.add(examTimeField, gbc);
        gbc.gridx = 0; gbc.gridy = 4; addDeleteExamPanel.add(new JLabel("Descripción:"), gbc);
        gbc.gridx = 1; addDeleteExamPanel.add(examDescriptionScrollPane, gbc);

        // Botones de Examen - utilizan createAdminButton, que ya pone el texto en negro
        btnAddExam = createAdminButton("Añadir Examen", new Color(34, 197, 94));
        btnDeleteExam = createAdminButton("Eliminar Examen Seleccionado", new Color(239, 68, 68));

        btnDeleteExam.setEnabled(false);

        JPanel examButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        examButtonsPanel.setBackground(new Color(90, 100, 115));
        examButtonsPanel.add(btnAddExam);
        examButtonsPanel.add(btnDeleteExam);

        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2; addDeleteExamPanel.add(examButtonsPanel, gbc);

        examsPanel.add(addDeleteExamPanel, BorderLayout.SOUTH);

        // Lógica de los botones de examen
        disciplineFilterComboBox.addActionListener(e -> {
            currentExamFilterDiscipline = (String) disciplineFilterComboBox.getSelectedItem();
            filterExamsList(); // CORREGIDO: Llama a filterExamsList
        });

        btnAddExam.addActionListener(e -> {
            String discipline = (String) examDisciplineComboBox.getSelectedItem();
            String examType = examTypeField.getText().trim();
            String examTime = examTimeField.getText().trim();
            String examDescription = examDescriptionArea.getText().trim();
            Date examDate = examDateChooser.getDate();

            if (discipline == null || examType.isEmpty() || examTime.isEmpty() || examDate == null) {
                JOptionPane.showMessageDialog(examsPanel, "Disciplina, tipo, fecha y hora son obligatorios.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String examDateStr = new java.text.SimpleDateFormat("dd/MM/yyyy").format(examDate);

            if (DataBase.añadirExamen(discipline, examDateStr, examTime, examDescription, examType)) {
                JOptionPane.showMessageDialog(examsPanel, "Examen programado con éxito.");
                examTypeField.setText(""); examTimeField.setText(""); examDescriptionArea.setText(""); examDateChooser.setDate(new Date());
                filterExamsList(); // CORREGIDO: Llama a filterExamsList
            }
        });

        btnDeleteExam.addActionListener(e -> {
            String[] selectedExamInfo = examList.getSelectedValue();
            if (selectedExamInfo == null) {
                JOptionPane.showMessageDialog(examsPanel, "Por favor, selecciona un examen a eliminar.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String disciplineToDelete = selectedExamInfo[0];
            String examDateStr = selectedExamInfo[1];
            String examTime = selectedExamInfo[2];
            String examType = selectedExamInfo[4];

            int confirm = JOptionPane.showConfirmDialog(examsPanel,
                    "¿Seguro que quieres eliminar el examen '" + examType + "' de la disciplina " + disciplineToDelete +
                    "\nprogramado para el " + examDateStr + " a las " + examTime + "?",
                    "Confirmar Eliminación de Examen", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                if (DataBase.eliminarExamen(disciplineToDelete, examDateStr, examTime)) {
                    JOptionPane.showMessageDialog(examsPanel, "Examen eliminado con éxito.");
                    filterExamsList(); // CORREGIDO: Llama a filterExamsList
                }
            }
        });

        examList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    boolean somethingSelected = !examList.getSelectionModel().isSelectionEmpty();
                    btnDeleteExam.setEnabled(somethingSelected);
                }
            }
        });

        filterExamsList(); // CORREGIDO: Llama a filterExamsList

        return examsPanel;
    }

    static class ExamListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value instanceof String[]) {
                String[] examInfo = (String[]) value;
                // {nombre_disciplina [0], fecha [1], hora [2], descripcion [3], tipo_examen [4]}
                String discipline = examInfo[0];
                String date = examInfo[1];
                String time = examInfo[2];
                String description = examInfo[3];
                String type = examInfo[4];

                String displayText = String.format("<html><b>[%s] %s</b> - %s a las %s<br><i>%s</i></html>",
                                                    discipline,
                                                    type,
                                                    date,
                                                    time,
                                                    description != null && !description.isEmpty() ? description : "Sin descripción.");
                setText(displayText);
                setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
                setForeground(Color.BLACK); // CAMBIO: Texto del renderer a negro
            }
            return this;
        }
    }

    // --- Métodos de Refresco de Listas ---
    private static void refreshGradesList(String username) {
        gradesListModel.clear();
        List<String[]> currentGrades = DataBase.obtenerGradosUsuario(username);
        for (String[] grade : currentGrades) {
            gradesListModel.addElement(grade);
        }
    }

    // CORREGIDO: Renombrado a filterExamsList para coincidir con las llamadas en el código
    private static void filterExamsList() { 
        examListModel.clear();
        List<String[]> currentExams = new ArrayList<>();
        
        if (currentExamFilterDiscipline.equals("Todas")) {
            // Obtener todas las disciplinas activas para iterar
            List<Map<String, String>> activeDisciplines = DataBase.obtenerTodasLasDisciplinas(false);
            for (Map<String, String> disciplineMap : activeDisciplines) {
                String discName = disciplineMap.get("nombre");
                currentExams.addAll(DataBase.obtenerExamenesDisciplina(discName));
            }
        } else {
            currentExams = DataBase.obtenerExamenesDisciplina(currentExamFilterDiscipline);
        }
        
        for (String[] exam : currentExams) {
            examListModel.addElement(exam);
        }

        btnDeleteExam.setEnabled(false);
    }

    // Carga todos los usuarios para la lista de selección en ContentManagementPanel
    private static void loadAllUsersForContentManagement() {
        userListModel.clear();
        List<String> usernames = DataBase.obtenerTodosUsernames();
        for (String username : usernames) {
            userListModel.addElement(username);
        }
    }
    
    // CORREGIDO: Añadido el método filterUsers aquí, ya que estaba siendo llamado pero no definido
    private static void filterUsers(String searchText) {
        userListModel.clear();
        List<String> allUsernames = DataBase.obtenerTodosUsernames(); // Obtiene todos los nombres de usuario
        String lowerCaseSearchText = searchText.toLowerCase().trim();

        if (lowerCaseSearchText.isEmpty() || lowerCaseSearchText.equals("buscar usuario...")) {
            for (String username : allUsernames) {
                userListModel.addElement(username);
            }
        } else {
            for (String username : allUsernames) {
                if (username.toLowerCase().contains(lowerCaseSearchText)) {
                    userListModel.addElement(username);
                }
            }
        }
        userList.clearSelection(); // Limpia la selección después de filtrar
    }


    // Método auxiliar para crear botones de administración (reutilizado)
    private static JButton createAdminButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(Color.BLACK); // CAMBIO: Texto de botón a negro
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setFocusPainted(false);
        return button;
    } }