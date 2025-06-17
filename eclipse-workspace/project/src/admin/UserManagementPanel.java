package admin;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import logear.DataBase;
import usuarios.ProgresoExamenes;
import usuarios.SeleccionDisciplinas;

public class UserManagementPanel {

    private static JList<String> usersJList;
    private static DefaultListModel<String> usersListModel;
    private static JList<String> enrolledDisciplinesJList;
    private static DefaultListModel<String> enrolledDisciplinesListModel;

    public static JPanel createPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(new Color(45, 55, 70));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Administración de Usuarios", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(Color.YELLOW); // CAMBIO: Título a negro
        panel.add(title, BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(250);
        splitPane.setBackground(new Color(45, 55, 70));
        splitPane.setForeground(new Color(80, 90, 105));

        // --- Panel Izquierdo: Lista de Usuarios ---
        JPanel userListPanel = new JPanel(new BorderLayout(10, 10));
        userListPanel.setBackground(new Color(60, 70, 85));
        userListPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(80, 90, 105)), "Lista de Usuarios",
            0, 2, new Font("Arial", Font.BOLD, 14), Color.WHITE));

        JTextField searchUserField = new JTextField("Buscar usuario...");
        searchUserField.setFont(new Font("Arial", Font.ITALIC, 13));
        searchUserField.setForeground(Color.GRAY);
        searchUserField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(80, 90, 105)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        searchUserField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (searchUserField.getText().equals("Buscar usuario...")) {
                    searchUserField.setText("");
                    searchUserField.setForeground(Color.BLACK); // CAMBIO: Texto de búsqueda a negro al ganar foco
                    searchUserField.setFont(new Font("Arial", Font.PLAIN, 14));
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (searchUserField.getText().isEmpty()) {
                    searchUserField.setText("Buscar usuario...");
                    searchUserField.setForeground(Color.GRAY);
                    searchUserField.setFont(new Font("Arial", Font.ITALIC, 13));
                }
            }
        });
        searchUserField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filterUsers(searchUserField.getText());
            }
        });
        userListPanel.add(searchUserField, BorderLayout.NORTH);


        usersListModel = new DefaultListModel<>();
        loadAllUsers(); // Carga todos los usuarios inicialmente desde la DB
        usersJList = new JList<>(usersListModel);
        usersJList.setFont(new Font("Arial", Font.PLAIN, 16));
        usersJList.setBackground(new Color(60, 70, 85));
        usersJList.setForeground(Color.WHITE);
        usersJList.setSelectionBackground(new Color(59, 130, 246));
        usersJList.setSelectionForeground(Color.WHITE);
        usersJList.setBorder(BorderFactory.createLineBorder(new Color(80, 90, 105)));

        JScrollPane usersScrollPane = new JScrollPane(usersJList);
        usersScrollPane.setBorder(null);
        userListPanel.add(usersScrollPane, BorderLayout.CENTER);
        splitPane.setLeftComponent(userListPanel);

        // --- Panel Derecho: Detalles del Usuario / Inscripciones ---
        JPanel userDetailsPanel = new JPanel(new BorderLayout(10, 10));
        userDetailsPanel.setBackground(new Color(60, 70, 85));
        userDetailsPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(80, 90, 105)), "Disciplinas Inscritas",
            0, 2, new Font("Arial", Font.BOLD, 14), Color.WHITE));

        enrolledDisciplinesListModel = new DefaultListModel<>();
        enrolledDisciplinesJList = new JList<>(enrolledDisciplinesListModel);
        enrolledDisciplinesJList.setFont(new Font("Arial", Font.PLAIN, 16));
        enrolledDisciplinesJList.setBackground(new Color(75, 85, 100));
        enrolledDisciplinesJList.setForeground(Color.WHITE);
        enrolledDisciplinesJList.setSelectionBackground(new Color(220, 38, 38));
        enrolledDisciplinesJList.setSelectionForeground(Color.WHITE);
        enrolledDisciplinesJList.setBorder(BorderFactory.createLineBorder(new Color(80, 90, 105)));

        JScrollPane disciplinesScrollPane = new JScrollPane(enrolledDisciplinesJList);
        disciplinesScrollPane.setBorder(null);
        userDetailsPanel.add(disciplinesScrollPane, BorderLayout.CENTER);

        usersJList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    String selectedUser = usersJList.getSelectedValue();
                    if (selectedUser != null) {
                        loadUserDisciplines(selectedUser);
                    } else {
                        enrolledDisciplinesListModel.clear();
                    }
                }
            }
        });

        splitPane.setRightComponent(userDetailsPanel);
        panel.add(splitPane, BorderLayout.CENTER);

        // --- Panel Inferior: Botones de Acción ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setBackground(new Color(45, 55, 70));

        JButton btnAddUser = createAdminButton("➕ Añadir Nuevo Usuario", new Color(34, 197, 94));
        JButton btnRemoveEnrollment = createAdminButton("❌ Eliminar Inscripción Seleccionada", new Color(239, 68, 68));
        JButton btnDeleteUser = createAdminButton("🗑️ Eliminar Usuario Completo", new Color(200, 50, 50));

        btnAddUser.addActionListener(e -> addNewUser(panel));
        btnRemoveEnrollment.addActionListener(e -> removeSelectedEnrollment(panel));
        btnDeleteUser.addActionListener(e -> deleteSelectedUser(panel));

        buttonPanel.add(btnAddUser);
        buttonPanel.add(btnRemoveEnrollment);
        buttonPanel.add(btnDeleteUser);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    // --- MÉTODOS AUXILIARES ---

    private static JButton createAdminButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(Color.BLACK); // CAMBIO: Texto de botón a negro
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setFocusPainted(false);
        return button;
    }

    // Carga todos los usuarios desde la base de datos (SOLO USUARIOS REGULARES)
    private static void loadAllUsers() {
        usersListModel.clear();
        List<String> usernames = DataBase.obtenerTodosUsernames(); // Obtiene SOLO usuarios de la tabla 'usuarios'
        for (String username : usernames) {
            usersListModel.addElement(username);
        }
    }

    private static void loadUserDisciplines(String username) {
        enrolledDisciplinesListModel.clear();
        // Carga solo las inscripciones activas para esta vista
        List<String> userInscriptions = DataBase.obtenerDisciplinasInscritas(username);
        for (String discipline : userInscriptions) {
            enrolledDisciplinesListModel.addElement(discipline);
        }
    }

    // --- LÓGICA DE AÑADIR/ELIMINAR ---

    private static void addNewUser(JPanel parentPanel) {
        try { // Agregado para capturar posibles excepciones internas
            JTextField usernameField = new JTextField(15);
            JPasswordField passwordField = new JPasswordField(15);
            JTextField documentField = new JTextField(15);
            
            JPanel inputPanel = new JPanel(new GridLayout(0, 2, 5, 5));
            inputPanel.add(new JLabel("Usuario:"));
            inputPanel.add(usernameField);
            inputPanel.add(new JLabel("Contraseña Temporal:"));
            inputPanel.add(passwordField);
            inputPanel.add(new JLabel("No. Documento:"));
            inputPanel.add(documentField);

            int result = JOptionPane.showConfirmDialog(parentPanel, inputPanel, 
                    "Añadir Nuevo Usuario", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (result == JOptionPane.OK_OPTION) {
                String username = usernameField.getText().trim();
                String password = new String(passwordField.getPassword());
                String document = documentField.getText().trim();

                if (username.isEmpty() || password.isEmpty() || document.isEmpty()) {
                    JOptionPane.showMessageDialog(parentPanel, "Todos los campos son obligatorios.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (DataBase.usuarioExiste(username)) { // Verifica si el usuario normal ya existe
                    JOptionPane.showMessageDialog(parentPanel, "El usuario '" + username + "' ya existe.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                // ¡IMPORTANTE!: También debemos verificar que no sea un nombre de administrador existente
                if (DataBase.adminExiste(username)) {
                    JOptionPane.showMessageDialog(parentPanel, "El nombre de usuario '" + username + "' ya está registrado como administrador.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (!document.matches("\\d{9,10}")) {
                    JOptionPane.showMessageDialog(parentPanel, "El número de documento debe contener entre 9 y 10 dígitos numéricos.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (DataBase.documentoExiste(document)) {
                    JOptionPane.showMessageDialog(parentPanel, "El número de documento '" + document + "' ya está registrado.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                String tempDob = LocalDate.now().minusYears(18).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

                // !! LLAMADA ACTUALIZADA a DataBase.registrarUsuario (4 parámetros) !!
                // El tipo 'usuario' se fija dentro de DataBase.java
                if (DataBase.registrarUsuario(username, password, document, tempDob)) {
                    usersListModel.addElement(username);
                    JOptionPane.showMessageDialog(parentPanel, "Usuario '" + username + "' añadido con éxito.\nContraseña temporal: " + password);
                    usersJList.setSelectedValue(username, true);
                }
            }
        } catch (Exception ex) { // Captura cualquier excepción que pueda ocurrir en este método
            ex.printStackTrace(); // Imprime el stack trace a la consola (útil para depuración)
            JOptionPane.showMessageDialog(parentPanel, "Ocurrió un error inesperado al añadir usuario: " + ex.getMessage(), "Error Crítico", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void removeSelectedEnrollment(JPanel parentPanel) {
        String selectedUser = usersJList.getSelectedValue();
        String selectedDiscipline = enrolledDisciplinesJList.getSelectedValue();

        if (selectedUser == null) {
            JOptionPane.showMessageDialog(parentPanel, "Selecciona un usuario primero.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (selectedDiscipline == null) {
            JOptionPane.showMessageDialog(parentPanel, "Selecciona una disciplina inscrita para eliminar.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(parentPanel,
                "¿Seguro que quieres desinscribir a '" + selectedUser + "' de '" + selectedDiscipline + "'?",
                "Confirmar Desinscripción", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            String reason = JOptionPane.showInputDialog(parentPanel,
                    "Ingresa la razón para desinscribir a " + selectedUser + " de " + selectedDiscipline + ":",
                    "Razón de Desinscripción", JOptionPane.PLAIN_MESSAGE);

            if (reason == null || reason.trim().isEmpty()) {
                JOptionPane.showMessageDialog(parentPanel, "La razón de desinscripción es obligatoria.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (DataBase.desinscribirUsuarioDeDisciplina(selectedUser, selectedDiscipline, reason.trim())) {
                enrolledDisciplinesListModel.removeElement(selectedDiscipline);
                JOptionPane.showMessageDialog(parentPanel, "Inscripción de '" + selectedDiscipline + "' eliminada y registrada para '" + selectedUser + "'.");
            } else {
                // El error ya lo muestra DataBase.desinscribirUsuarioDeDisciplina
            }
        }
    }

    private static void deleteSelectedUser(JPanel parentPanel) {
        String selectedUser = usersJList.getSelectedValue();

        if (selectedUser == null) {
            JOptionPane.showMessageDialog(parentPanel, "Selecciona un usuario para eliminar.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(parentPanel,
                "ADVERTENCIA: ¿Estás seguro de que quieres eliminar al usuario '" + selectedUser + "' y TODOS sus datos (de la base de datos)?\nEsta acción no se puede deshacer.",
                "Confirmar Eliminación de Usuario", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            if (DataBase.eliminarUsuario(selectedUser)) {
                // Eliminar ARCHIVOS de progreso asociados (si aún existen y no se manejan solo por DB)
                File gradesFile = new File(ProgresoExamenes.PROGRESS_DIR + selectedUser + "_grados.txt");
                if (gradesFile.exists()) gradesFile.delete();
                File examsFile = new File(ProgresoExamenes.PROGRESS_DIR + selectedUser + "_examenes.txt");
                if (examsFile.exists()) examsFile.delete();
                File inscriptionsFile = new File(SeleccionDisciplinas.INSCRIPTIONS_DIR + selectedUser + "_inscripciones.txt");
                if (inscriptionsFile.exists()) inscriptionsFile.delete();


                usersListModel.removeElement(selectedUser);
                enrolledDisciplinesListModel.clear();
                JOptionPane.showMessageDialog(parentPanel, "Usuario '" + selectedUser + "' y sus datos eliminados con éxito.");
            } else {
                JOptionPane.showMessageDialog(parentPanel, "No se pudo eliminar el usuario de la base de datos.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static void filterUsers(String searchText) {
        usersListModel.clear();
        // Obtiene SOLO usuarios normales de la DB
        List<String> allUsernames = DataBase.obtenerTodosUsernames();

        String lowerCaseSearchText = searchText.toLowerCase().trim();

        if (lowerCaseSearchText.isEmpty()) {
            for (String user : allUsernames) {
                usersListModel.addElement(user);
            }
        } else {
            for (String user : allUsernames) {
                if (user.toLowerCase().startsWith(lowerCaseSearchText)) {
                    usersListModel.addElement(user);
                }
            }
        }
        usersJList.clearSelection();
        enrolledDisciplinesListModel.clear();
    }
}