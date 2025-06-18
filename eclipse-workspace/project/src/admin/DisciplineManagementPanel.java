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

import logear.DataBase; // Para interactuar con la base de datos

public class DisciplineManagementPanel {

    private static JList<Map<String, String>> disciplinesJList;
    private static DefaultListModel<Map<String, String>> disciplinesListModel = new DefaultListModel<>();
    private static List<Map<String, String>> allDisciplinesData = new ArrayList<>(); // Contiene TODAS las disciplinas de la DB

    // Campos del formulario de edición
    private static JTextField disciplineIdField;
    private static JTextField nameField;
    private static JTextArea descriptionArea;
    private static JTextField scheduleField;
    private static JTextField instructorField;
    private static JTextField levelField;

    // Se elimina el botón btnAddDiscipline.
    private static JButton btnEditDiscipline;

    // Botones de acción del formulario
    private static JButton btnSaveDiscipline;
    private static JButton btnCancelEdit;

    private static JTextField searchDisciplineField;

    public static JPanel createPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(new Color(45, 55, 70));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Administración de Disciplinas", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(new Color(255, 215, 0)); // Título principal se mantiene en dorado
        panel.add(title, BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(300);
        splitPane.setBackground(new Color(45, 55, 70));
        splitPane.setForeground(new Color(80, 90, 105));

        // --- Panel Izquierdo: Lista de Disciplinas con Búsqueda ---
        JPanel disciplineListPanel = new JPanel(new BorderLayout(10, 10));
        disciplineListPanel.setBackground(new Color(60, 70, 85));
        disciplineListPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(80, 90, 105)), "Lista de Disciplinas",
            0, 2, new Font("Arial", Font.BOLD, 14), Color.WHITE)); // Título de borde en blanco

        searchDisciplineField = new JTextField("Buscar disciplina...");
        searchDisciplineField.setFont(new Font("Arial", Font.ITALIC, 13));
        searchDisciplineField.setForeground(Color.GRAY); // Placeholder
        searchDisciplineField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(80, 90, 105)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        searchDisciplineField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (searchDisciplineField.getText().equals("Buscar disciplina...")) {
                    searchDisciplineField.setText("");
                    searchDisciplineField.setForeground(Color.BLACK); // Texto a negro al ganar foco
                    searchDisciplineField.setFont(new Font("Arial", Font.PLAIN, 14));
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (searchDisciplineField.getText().isEmpty()) {
                    searchDisciplineField.setText("Buscar disciplina...");
                    searchDisciplineField.setForeground(Color.GRAY); // Placeholder vuelve a gris
                    searchDisciplineField.setFont(new Font("Arial", Font.ITALIC, 13));
                } else {
                    searchDisciplineField.setForeground(Color.BLACK); // Texto a negro al perder foco si tiene contenido
                }
            }
        });
        searchDisciplineField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                applyDisplayFilter(); // Aplicar filtro al escribir
            }
        });
        disciplineListPanel.add(searchDisciplineField, BorderLayout.NORTH); // Añadir directamente

        disciplinesJList = new JList<>(disciplinesListModel);
        disciplinesJList.setFont(new Font("Arial", Font.PLAIN, 16));
        disciplinesJList.setBackground(new Color(60, 70, 85));
        disciplinesJList.setForeground(Color.WHITE); // Texto de la lista de disciplinas
        disciplinesJList.setSelectionBackground(new Color(168, 85, 247));
        disciplinesJList.setSelectionForeground(Color.WHITE);
        disciplinesJList.setBorder(BorderFactory.createLineBorder(new Color(80, 90, 105)));
        disciplinesJList.setCellRenderer(new DisciplineCellRenderer());

        JScrollPane disciplinesScrollPane = new JScrollPane(disciplinesJList);
        disciplinesScrollPane.setBorder(null);
        disciplineListPanel.add(disciplinesScrollPane, BorderLayout.CENTER);
        splitPane.setLeftComponent(disciplineListPanel);

        // --- Panel Derecho: Detalles / Formulario de Edición ---
        JPanel disciplineDetailsPanel = new JPanel(new BorderLayout(10, 10));
        disciplineDetailsPanel.setBackground(new Color(60, 70, 85));
        disciplineDetailsPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(80, 90, 105)), "Detalles / Edición de Disciplina",
            0, 2, new Font("Arial", Font.BOLD, 14), Color.WHITE)); // Título de borde en blanco

        // Formulario de edición
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(new Color(75, 85, 100));
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        disciplineIdField = new JTextField(1); // Campo oculto para el ID
        disciplineIdField.setVisible(false);

        nameField = new JTextField(20);
        nameField.setForeground(Color.BLACK); // Texto del campo de nombre a negro
        nameField.addFocusListener(new FocusAdapter() { // Añadido para gestionar el color del texto
            @Override public void focusGained(FocusEvent e) { nameField.setForeground(Color.BLACK); }
            @Override public void focusLost(FocusEvent e) { if (nameField.getText().isEmpty()) nameField.setForeground(Color.BLACK); }
        });

        descriptionArea = new JTextArea(4, 20);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setForeground(Color.BLACK); // Texto del campo de descripción a negro
        descriptionArea.addFocusListener(new FocusAdapter() { // Añadido para gestionar el color del texto
            @Override public void focusGained(FocusEvent e) { descriptionArea.setForeground(Color.BLACK); }
            @Override public void focusLost(FocusEvent e) { if (descriptionArea.getText().isEmpty()) descriptionArea.setForeground(Color.BLACK); }
        });
        JScrollPane descriptionScrollPane = new JScrollPane(descriptionArea);

        scheduleField = new JTextField(20);
        scheduleField.setForeground(Color.BLACK); // Texto del campo de horario a negro
        scheduleField.addFocusListener(new FocusAdapter() { // Añadido para gestionar el color del texto
            @Override public void focusGained(FocusEvent e) { scheduleField.setForeground(Color.BLACK); }
            @Override public void focusLost(FocusEvent e) { if (scheduleField.getText().isEmpty()) scheduleField.setForeground(Color.BLACK); }
        });

        instructorField = new JTextField(20);
        instructorField.setForeground(Color.BLACK); // Texto del campo de instructor a negro
        instructorField.addFocusListener(new FocusAdapter() { // Añadido para gestionar el color del texto
            @Override public void focusGained(FocusEvent e) { instructorField.setForeground(Color.BLACK); }
            @Override public void focusLost(FocusEvent e) { if (instructorField.getText().isEmpty()) instructorField.setForeground(Color.BLACK); }
        });

        levelField = new JTextField(20);
        levelField.setForeground(Color.BLACK); // Texto del campo de nivel a negro
        levelField.addFocusListener(new FocusAdapter() { // Añadido para gestionar el color del texto
            @Override public void focusGained(FocusEvent e) { levelField.setForeground(Color.BLACK); }
            @Override public void focusLost(FocusEvent e) { if (levelField.getText().isEmpty()) levelField.setForeground(Color.BLACK); }
        });

        // Labels y Fields
        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(new JLabel("Nombre:"), gbc);
        gbc.gridx = 1; formPanel.add(nameField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; formPanel.add(new JLabel("Descripción:"), gbc);
        gbc.gridx = 1; formPanel.add(descriptionScrollPane, gbc);
        gbc.gridx = 0; gbc.gridy = 2; formPanel.add(new JLabel("Horario:"), gbc);
        gbc.gridx = 1; formPanel.add(scheduleField, gbc);
        gbc.gridx = 0; gbc.gridy = 3; formPanel.add(new JLabel("Instructor:"), gbc);
        gbc.gridx = 1; formPanel.add(instructorField, gbc);
        gbc.gridx = 0; gbc.gridy = 4; formPanel.add(new JLabel("Nivel:"), gbc);
        gbc.gridx = 1; formPanel.add(levelField, gbc);

        disciplineDetailsPanel.add(formPanel, BorderLayout.CENTER);

        // Botones de acción del formulario (Guardar/Cancelar)
        JPanel formActionButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        formActionButtonsPanel.setBackground(new Color(75, 85, 100));
        btnSaveDiscipline = createAdminButton("💾 Guardar Cambios", new Color(34, 197, 94));
        btnCancelEdit = createAdminButton("↩️ Cancelar", new Color(239, 68, 68));
        formActionButtonsPanel.add(btnSaveDiscipline);
        formActionButtonsPanel.add(btnCancelEdit);
        disciplineDetailsPanel.add(formActionButtonsPanel, BorderLayout.SOUTH);

        splitPane.setRightComponent(disciplineDetailsPanel);
        panel.add(splitPane, BorderLayout.CENTER);

        // --- Panel Inferior: Botones Generales (Solo Editar) ---
        JPanel generalActionButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        generalActionButtonsPanel.setBackground(new Color(45, 55, 70));
        generalActionButtonsPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        // Se elimina el botón btnAddDiscipline.
        btnEditDiscipline = createAdminButton("✏️ Editar Disciplina Seleccionada", new Color(59, 130, 246));

        generalActionButtonsPanel.add(btnEditDiscipline); // Solo añadir el botón de editar

        panel.add(generalActionButtonsPanel, BorderLayout.SOUTH);

        // --- Listeners ---

        disciplinesJList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    Map<String, String> selectedDiscipline = disciplinesJList.getSelectedValue();
                    if (selectedDiscipline != null) {
                        displayDisciplineDetails(selectedDiscipline);
                        setFormEditable(false);
                        
                        btnEditDiscipline.setEnabled(true); 

                        btnSaveDiscipline.setEnabled(false);
                        btnCancelEdit.setEnabled(false);
                        // El botón de añadir disciplina ya no existe, no se habilita
                    } else {
                        clearForm();
                        setFormEditable(false);
                        btnEditDiscipline.setEnabled(false);
                        btnSaveDiscipline.setEnabled(false);
                        btnCancelEdit.setEnabled(false);
                        // El botón de añadir disciplina ya no existe
                    }
                }
            }
        });

        // Se elimina el action listener de btnAddDiscipline.
        btnEditDiscipline.addActionListener(e -> startEditDiscipline());

        btnSaveDiscipline.addActionListener(e -> saveDiscipline(panel));
        btnCancelEdit.addActionListener(e -> cancelEdit());

        // Carga inicial y filtro inicial
        loadDisciplinesFromDB(); 
        applyDisplayFilter(); 

        // Inicializar estado de los botones y formulario al inicio
        clearForm();
        setFormEditable(false);
        btnEditDiscipline.setEnabled(false);
        btnSaveDiscipline.setEnabled(false);
        btnCancelEdit.setEnabled(false);
        // El botón de añadir disciplina ya no existe
        return panel;
    }

    // --- Métodos Auxiliares ---

    private static JButton createAdminButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(Color.BLACK); // Texto de botón a negro
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setFocusPainted(false);
        return button;
    }

    // Carga TODAS las disciplinas de la DB a allDisciplinesData. NO FILTRA AQUÍ.
    private static void loadDisciplinesFromDB() {
        allDisciplinesData = DataBase.obtenerTodasLasDisciplinas(true); // Siempre cargar TODAS de la DB
        System.out.println("DEBUG: loadDisciplinesFromDB() cargó " + allDisciplinesData.size() + " disciplinas de la DB.");
    }

    // Aplica el filtro (solo búsqueda de texto) a allDisciplinesData y rellena disciplinesListModel
    private static void applyDisplayFilter() {
        disciplinesListModel.clear(); // Limpia la lista visible
        String searchText = searchDisciplineField.getText().trim();
        boolean isPlaceholderText = searchText.toLowerCase().equals("buscar disciplina...");
        String lowerCaseSearchText = searchText.toLowerCase();

        System.out.println("DEBUG: Aplicando filtro. Buscar: '" + searchText + "', Es placeholder: " + isPlaceholderText);
        System.out.println("DEBUG: allDisciplinesData para filtrar: " + allDisciplinesData.size() + " elementos.");

        for (Map<String, String> discipline : allDisciplinesData) {
            // Lógica de filtrado por texto de búsqueda
            if (isPlaceholderText) { 
                // No hacer nada, la disciplina pasa el filtro de texto por defecto
            } else if (!lowerCaseSearchText.isEmpty()) { 
                if (!discipline.get("nombre").toLowerCase().contains(lowerCaseSearchText) &&
                    !discipline.get("instructor").toLowerCase().contains(lowerCaseSearchText) &&
                    !discipline.get("descripcion").toLowerCase().contains(lowerCaseSearchText) &&
                    !discipline.get("horario").toLowerCase().contains(lowerCaseSearchText) &&
                    !discipline.get("nivel").toLowerCase().contains(lowerCaseSearchText)) {
                    continue; // Saltar si no coincide con el texto de búsqueda
                }
            }
            disciplinesListModel.addElement(discipline); // Añadir la disciplina si pasa los filtros
        }
        System.out.println("DEBUG: disciplinesListModel tiene ahora " + disciplinesListModel.size() + " elementos.");

        // Deseleccionar cualquier elemento y ajustar botones después del filtro
        disciplinesJList.clearSelection();
        setFormEditable(false);
        clearForm();
        btnEditDiscipline.setEnabled(false);
        btnSaveDiscipline.setEnabled(false);
        btnCancelEdit.setEnabled(false);
        // El botón de añadir disciplina ya no existe
    }

    private static void displayDisciplineDetails(Map<String, String> discipline) {
        if (discipline != null) {
            disciplineIdField.setText(discipline.get("id"));
            nameField.setText(discipline.get("nombre"));
            descriptionArea.setText(discipline.get("descripcion"));
            scheduleField.setText(discipline.get("horario"));
            instructorField.setText(discipline.get("instructor"));
            levelField.setText(discipline.get("nivel"));
        }
    }

    private static void clearForm() {
        disciplineIdField.setText("");
        nameField.setText("");
        descriptionArea.setText("");
        scheduleField.setText("");
        instructorField.setText("");
        levelField.setText("");
    }

    private static void setFormEditable(boolean editable) {
        nameField.setEditable(editable);
        descriptionArea.setEditable(editable);
        scheduleField.setEditable(editable);
        instructorField.setEditable(editable);
        levelField.setEditable(editable);
    }

    // Se elimina el método startAddDiscipline.
    // private static void startAddDiscipline(JPanel parentPanel) { ... }

    private static void startEditDiscipline() {
        if (disciplinesJList.getSelectedValue() == null) {
            JOptionPane.showMessageDialog(null, "Selecciona una disciplina para editar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        setFormEditable(true);
        // El botón de añadir disciplina ya no existe, no se deshabilita
        btnEditDiscipline.setEnabled(false);
        btnSaveDiscipline.setEnabled(true);
        btnCancelEdit.setEnabled(true);
        nameField.requestFocusInWindow();
    }

    private static void saveDiscipline(JPanel parentPanel) {
        String idStr = disciplineIdField.getText().trim();
        String name = nameField.getText().trim();
        String description = descriptionArea.getText().trim();
        String schedule = scheduleField.getText().trim();
        String instructor = instructorField.getText().trim();
        String level = levelField.getText().trim();

        if (name.isEmpty() || description.isEmpty() || schedule.isEmpty() || instructor.isEmpty() || level.isEmpty()) {
            JOptionPane.showMessageDialog(parentPanel, "Todos los campos son obligatorios.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean success;
        if (idStr.isEmpty()) { // Es una nueva disciplina (ADD)
            // Esta rama del IF ya no debería ser alcanzable si se eliminó el botón "Añadir".
            // Sin embargo, si se deja el método para la posibilidad de añadir desde otro lado,
            // se mantendría la llamada. Dado el contexto, solo se enfoca en la edición.
            // Para asegurar la coherencia con "solo editar", se asume que solo se llamará
            // para edición (idStr no vacío). Si se presiona "Guardar" sin ID, se considera error.
            JOptionPane.showMessageDialog(parentPanel, "Error: No se puede añadir una nueva disciplina sin un ID existente.", "Error de Operación", JOptionPane.ERROR_MESSAGE);
            return;
        } else { // Es una disciplina existente (EDIT)
            int id = Integer.parseInt(idStr);
            success = DataBase.actualizarDisciplina(id, name, description, schedule, instructor, level);
            if (success) {
                JOptionPane.showMessageDialog(parentPanel, "Disciplina '" + name + "' actualizada con éxito.");
            }
        }

        if (success) {
            loadDisciplinesFromDB(); // Recargar los datos de la DB
            applyDisplayFilter(); // Re-aplicar el filtro para actualizar la vista
            clearForm();
            setFormEditable(false);
            // El botón de añadir disciplina ya no existe
            btnEditDiscipline.setEnabled(false);
            btnSaveDiscipline.setEnabled(false);
            btnCancelEdit.setEnabled(false);
            disciplinesJList.clearSelection();
        }
    }

    private static void cancelEdit() {
        if (!disciplineIdField.getText().isEmpty()) {
            Map<String, String> originalDiscipline = null;
            for (Map<String, String> disc : allDisciplinesData) {
                if (disc.get("id").equals(disciplineIdField.getText())) {
                    originalDiscipline = disc;
                    break;
                }
            }
            displayDisciplineDetails(originalDiscipline);
        } else {
            clearForm();
        }
        setFormEditable(false);
        // El botón de añadir disciplina ya no existe
        btnEditDiscipline.setEnabled(false);
        btnSaveDiscipline.setEnabled(false);
        btnCancelEdit.setEnabled(false);
        disciplinesJList.clearSelection();
        applyDisplayFilter(); // Re-aplicar el filtro por si el estado de alguna disciplina cambió
    }

    // --- Custom Cell Renderer para la JList ---
    static class DisciplineCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value instanceof Map) {
                Map<String, String> discipline = (Map<String, String>) value;
                String name = discipline.get("nombre");
                String instructor = discipline.get("instructor");
                String schedule = discipline.get("horario");
                boolean isActive = Boolean.parseBoolean(discipline.get("activo")); // Se mantiene para referencia si es necesario mostrar el estado

                Color disciplineColor = getColorForDiscipline(name);

                String displayText = String.format("<html><b style='color:%s;'>%s</b><br><i>%s</i><br>%s",
                                                    toHex(disciplineColor),
                                                    name,
                                                    instructor,
                                                    schedule);
                
                if (!isActive) {
                    displayText += "<span style='color:gray;'> (INACTIVA)</span>"; // Muestra solo el texto INACTIVA
                }
                displayText += "</html>";

                setText(displayText);
                setBorder(new EmptyBorder(5, 10, 5, 10));
                setForeground(Color.BLACK); // Texto de la lista a negro
            }
            return this;
        }

        private String toHex(Color color) {
            return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
        }

        private Color getColorForDiscipline(String disciplineName) {
            switch (disciplineName) {
                case "Karate": return new Color(220, 38, 38);
                case "Judo": return new Color(59, 130, 246);
                case "Taekwondo": return new Color(16, 185, 129);
                case "Boxeo": return new Color(239, 68, 68);
                case "Muay Thai": return new Color(168, 85, 247);
                default: return Color.LIGHT_GRAY;
            }
        }
    }
}