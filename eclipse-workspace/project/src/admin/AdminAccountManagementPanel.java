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
import java.util.Arrays; // Para limpiar password field

import logear.DataBase; // Necesitamos DataBase para gestionar admins

public class AdminAccountManagementPanel {

    private static JList<String> adminList;
    private static DefaultListModel<String> adminListModel;
    private static List<String> allAdminData; // Para el filtrado

    private static JButton btnAddAdmin;
    private static JButton btnDeleteAdmin;

    public static JPanel createPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(new Color(45, 55, 70));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Gestión de Cuentas de Administrador", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(new Color(255, 215, 0)); // Título principal se mantiene en dorado
        panel.add(title, BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(250);
        splitPane.setBackground(new Color(45, 55, 70));
        splitPane.setForeground(new Color(80, 90, 105));

        // --- Panel Izquierdo: Lista de Administradores ---
        JPanel adminListPanel = new JPanel(new BorderLayout(10, 10));
        adminListPanel.setBackground(new Color(60, 70, 85));
        adminListPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(80, 90, 105)), "Lista de Administradores",
            0, 2, new Font("Arial", Font.BOLD, 14), Color.WHITE));

        JTextField searchAdminField = new JTextField("Buscar administrador...");
        searchAdminField.setFont(new Font("Arial", Font.ITALIC, 13));
        searchAdminField.setForeground(Color.GRAY); // Color del placeholder
        searchAdminField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(80, 90, 105)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        searchAdminField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (searchAdminField.getText().equals("Buscar administrador...")) {
                    searchAdminField.setText("");
                    searchAdminField.setForeground(Color.BLACK); // CAMBIO: Texto a negro al ganar foco
                    searchAdminField.setFont(new Font("Arial", Font.PLAIN, 14));
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (searchAdminField.getText().isEmpty()) {
                    searchAdminField.setText("Buscar administrador...");
                    searchAdminField.setForeground(Color.GRAY); // Placeholder vuelve a gris
                    searchAdminField.setFont(new Font("Arial", Font.ITALIC, 13));
                } else {
                    searchAdminField.setForeground(Color.BLACK); // CAMBIO: Texto a negro al perder foco si tiene contenido
                }
            }
        });
        searchAdminField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filterAdmins(searchAdminField.getText());
            }
        });
        adminListPanel.add(searchAdminField, BorderLayout.NORTH);


        adminListModel = new DefaultListModel<>();
        allAdminData = new ArrayList<>(); // Inicializamos aquí
        loadAdmins(); // Cargar administradores al inicio

        adminList = new JList<>(adminListModel);
        adminList.setFont(new Font("Arial", Font.PLAIN, 16));
        adminList.setBackground(new Color(60, 70, 85));
        adminList.setForeground(Color.WHITE);
        adminList.setSelectionBackground(new Color(168, 85, 247)); // Color de selección
        adminList.setSelectionForeground(Color.WHITE);
        adminList.setBorder(BorderFactory.createLineBorder(new Color(80, 90, 105)));

        JScrollPane adminScrollPane = new JScrollPane(adminList);
        adminScrollPane.setBorder(null);
        adminListPanel.add(adminScrollPane, BorderLayout.CENTER);
        splitPane.setLeftComponent(adminListPanel);

        // --- Panel Derecho: Formulario para Añadir/Eliminar ---
        JPanel adminDetailsPanel = new JPanel(new GridBagLayout()); // Usar GridBagLayout para el formulario
        adminDetailsPanel.setBackground(new Color(60, 70, 85));
        adminDetailsPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(80, 90, 105)), "Detalles del Administrador",
            0, 2, new Font("Arial", Font.BOLD, 14), Color.WHITE));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0; gbc.gridy = 0;
        adminDetailsPanel.add(new JLabel("Usuario:"), gbc);
        JTextField usernameField = new JTextField(15);
        gbc.gridx = 1; adminDetailsPanel.add(usernameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        adminDetailsPanel.add(new JLabel("Contraseña:"), gbc);
        JPasswordField passwordField = new JPasswordField(15);
        gbc.gridx = 1; adminDetailsPanel.add(passwordField, gbc);

        // Los botones ya usan createAdminButton, que establece el foreground en BLACK
        btnAddAdmin = createAdminButton("➕ Crear Nuevo Administrador", new Color(34, 197, 94));
        btnDeleteAdmin = createAdminButton("🗑️ Eliminar Administrador Seleccionado", new Color(239, 68, 68));

        JPanel formButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        formButtonsPanel.setBackground(new Color(60, 70, 85));
        formButtonsPanel.add(btnAddAdmin);
        formButtonsPanel.add(btnDeleteAdmin);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        adminDetailsPanel.add(formButtonsPanel, gbc);

        splitPane.setRightComponent(adminDetailsPanel);
        panel.add(splitPane, BorderLayout.CENTER);

        // --- Listeners ---
        adminList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    boolean somethingSelected = !adminList.getSelectionModel().isSelectionEmpty();
                    btnDeleteAdmin.setEnabled(somethingSelected);
                    // Opcional: llenar campos de texto si se selecciona para "editar"
                    // Por ahora, los campos son solo para "añadir".
                }
            }
        });

        btnAddAdmin.addActionListener(e -> addNewAdmin(panel, usernameField, passwordField));
        btnDeleteAdmin.addActionListener(e -> deleteSelectedAdmin(panel));
        
        // Deshabilitar botón de eliminar al inicio
        btnDeleteAdmin.setEnabled(false);

        return panel;
    }

    // --- Métodos Auxiliares ---

    private static JButton createAdminButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(Color.BLACK); // CAMBIO: Texto de botón a negro
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setFocusPainted(false);
        return button;
    }

    private static void loadAdmins() {
        adminListModel.clear();
        allAdminData.clear();
        allAdminData = DataBase.obtenerTodosAdminUsernames(); // Asegúrate de obtener solo los admins de la tabla de administradores
        for (String adminUsername : allAdminData) {
            adminListModel.addElement(adminUsername);
        }
    }

    private static void filterAdmins(String searchText) {
        adminListModel.clear();
        String lowerCaseSearchText = searchText.toLowerCase().trim();

        if (lowerCaseSearchText.isEmpty()) {
            for (String adminUsername : allAdminData) {
                adminListModel.addElement(adminUsername);
            }
        } else {
            for (String adminUsername : allAdminData) {
                if (adminUsername.toLowerCase().contains(lowerCaseSearchText)) {
                    adminListModel.addElement(adminUsername);
                }
            }
        }
        adminList.clearSelection();
        btnDeleteAdmin.setEnabled(false);
    }

    private static void addNewAdmin(JPanel parentPanel, JTextField usernameField, JPasswordField passwordField) {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(parentPanel, "Usuario y contraseña son obligatorios.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (DataBase.adminExiste(username)) {
            JOptionPane.showMessageDialog(parentPanel, "El administrador '" + username + "' ya existe.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (DataBase.registrarAdmin(username, password)) {
            JOptionPane.showMessageDialog(parentPanel, "Administrador '" + username + "' creado con éxito.");
            usernameField.setText("");
            passwordField.setText("");
            Arrays.fill(passwordField.getPassword(), '\0'); // Limpiar array de contraseña
            loadAdmins(); // Recargar la lista de administradores
        }
    }

    private static void deleteSelectedAdmin(JPanel parentPanel) {
        String selectedAdmin = adminList.getSelectedValue();

        if (selectedAdmin == null) {
            JOptionPane.showMessageDialog(parentPanel, "Selecciona un administrador para eliminar.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Validación para evitar que un admin se elimine a sí mismo
        // Esto requeriría saber qué admin está logueado. Por ahora, una simple validación.
        // Si el admin que intenta borrar es 'admin', no lo dejamos.
        // Una forma más robusta sería pasar el username del admin logueado a este panel.
        if (selectedAdmin.equals("admin")) { // Asumiendo que 'admin' es el superusuario.
            JOptionPane.showMessageDialog(parentPanel, "No se puede eliminar el administrador principal 'admin' desde aquí.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Verificar que no sea el último administrador
        if (adminListModel.getSize() <= 1) { // Si solo queda 1 en la lista visible
            // Para ser más precisos, deberíamos obtener el conteo directamente de la DB.
            // Si DataBase.obtenerTodosAdminUsernames().size() == 1
            // Se corrige para usar DataBase.obtenerTodosAdminUsernames().size() en lugar de obtenerTodosUsernames()
            if (DataBase.obtenerTodosAdminUsernames().size() <= 1) {
                JOptionPane.showMessageDialog(parentPanel, "No se puede eliminar el último administrador del sistema. Debe haber al menos un administrador.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }


        int confirm = JOptionPane.showConfirmDialog(parentPanel,
                "ADVERTENCIA: ¿Estás seguro de que quieres eliminar al administrador '" + selectedAdmin + "'?\nEsta acción no se puede deshacer.",
                "Confirmar Eliminación de Administrador", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            if (DataBase.eliminarAdmin(selectedAdmin)) {
                JOptionPane.showMessageDialog(parentPanel, "Administrador '" + selectedAdmin + "' eliminado con éxito.");
                loadAdmins(); // Recargar la lista
            } else {
                JOptionPane.showMessageDialog(parentPanel, "No se pudo eliminar el administrador de la base de datos.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}