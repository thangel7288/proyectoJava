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

import logear.DataBase; // Necesitamos DataBase para obtener y reactivar inscripciones

public class EnrollmentReactivationPanel {

    private static JList<Map<String, String>> enrollmentJList;
    private static DefaultListModel<Map<String, String>> enrollmentListModel;
    private static List<Map<String, String>> allEnrollmentsData; // Para el filtrado

    private static JButton btnReactivateEnrollment;
    private static JButton btnViewReason;

    public static JPanel createPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(new Color(45, 55, 70));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Gestión de Inscripciones Inactivas/Rechazadas", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(new Color(255, 215, 0));
        panel.add(title, BorderLayout.NORTH);

        // --- Panel Superior: Búsqueda ---
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBackground(new Color(60, 70, 85));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField searchField = new JTextField("Buscar por usuario o disciplina...");
        searchField.setFont(new Font("Arial", Font.ITALIC, 13));
        searchField.setForeground(Color.GRAY);
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(80, 90, 105)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        searchField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (searchField.getText().equals("Buscar por usuario o disciplina...")) {
                    searchField.setText("");
                    searchField.setForeground(Color.WHITE);
                    searchField.setFont(new Font("Arial", Font.PLAIN, 14));
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (searchField.getText().isEmpty()) {
                    searchField.setText("Buscar por usuario o disciplina...");
                    searchField.setForeground(Color.GRAY);
                    searchField.setFont(new Font("Arial", Font.ITALIC, 13));
                }
            }
        });
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filterEnrollments(searchField.getText());
            }
        });
        topPanel.add(searchField, BorderLayout.CENTER);
        panel.add(topPanel, BorderLayout.CENTER); // Esto irá al centro, pero luego el splitPane lo reemplaza

        // --- Panel Central: Lista de Inscripciones ---
        JPanel listPanel = new JPanel(new BorderLayout(10, 10));
        listPanel.setBackground(new Color(60, 70, 85));
        listPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(80, 90, 105)), "Inscripciones No Activas",
            0, 2, new Font("Arial", Font.BOLD, 14), Color.WHITE));

        enrollmentListModel = new DefaultListModel<>();
        allEnrollmentsData = new ArrayList<>(); // Inicializamos aquí
        loadEnrollments(); // Cargar datos al inicio

        enrollmentJList = new JList<>(enrollmentListModel);
        enrollmentJList.setFont(new Font("Arial", Font.PLAIN, 16));
        enrollmentJList.setBackground(new Color(60, 70, 85));
        enrollmentJList.setForeground(Color.WHITE);
        enrollmentJList.setSelectionBackground(new Color(168, 85, 247)); // Un color para selección
        enrollmentJList.setSelectionForeground(Color.WHITE);
        enrollmentJList.setBorder(BorderFactory.createLineBorder(new Color(80, 90, 105)));
        enrollmentJList.setCellRenderer(new EnrollmentCellRenderer()); // Usar nuestro renderizador personalizado

        JScrollPane scrollPane = new JScrollPane(enrollmentJList);
        scrollPane.setBorder(null);
        listPanel.add(scrollPane, BorderLayout.CENTER);

        // --- Panel Inferior: Botones de Acción ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setBackground(new Color(45, 55, 70));

        btnReactivateEnrollment = createAdminButton("✅ Habilitar Inscripción Seleccionada", new Color(34, 197, 94));
        btnViewReason = createAdminButton("ℹ️ Ver Razón de Inactivación", new Color(59, 130, 246));

        btnReactivateEnrollment.setEnabled(false); // Deshabilitar inicialmente
        btnViewReason.setEnabled(false); // Deshabilitar inicialmente

        buttonPanel.add(btnReactivateEnrollment);
        buttonPanel.add(btnViewReason);

        listPanel.add(buttonPanel, BorderLayout.SOUTH); // Añadir los botones al panel de la lista

        // Ensamblaje final
        panel.add(topPanel, BorderLayout.NORTH); // El panel de búsqueda ahora va arriba
        panel.add(listPanel, BorderLayout.CENTER); // El panel con la lista y botones va al centro

        // Listeners
        enrollmentJList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    boolean somethingSelected = !enrollmentJList.getSelectionModel().isSelectionEmpty();
                    btnReactivateEnrollment.setEnabled(somethingSelected);
                    btnViewReason.setEnabled(somethingSelected);
                }
            }
        });

        btnReactivateEnrollment.addActionListener(e -> reactivateSelectedEnrollment(panel));
        btnViewReason.addActionListener(e -> viewSelectedReason(panel));

        return panel;
    }

    // --- Métodos Auxiliares ---

    private static JButton createAdminButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setFocusPainted(false);
        return button;
    }

    private static void loadEnrollments() {
        enrollmentListModel.clear();
        allEnrollmentsData.clear();
        // Carga todas las inscripciones no activas desde la base de datos
        allEnrollmentsData = DataBase.obtenerInscripcionesInactivasRechazadas();
        for (Map<String, String> enrollment : allEnrollmentsData) {
            enrollmentListModel.addElement(enrollment);
        }
    }

    private static void filterEnrollments(String searchText) {
        enrollmentListModel.clear();
        String lowerCaseSearchText = searchText.toLowerCase().trim();

        if (lowerCaseSearchText.isEmpty()) {
            for (Map<String, String> enrollment : allEnrollmentsData) {
                enrollmentListModel.addElement(enrollment);
            }
        } else {
            for (Map<String, String> enrollment : allEnrollmentsData) {
                String username = enrollment.get("username").toLowerCase();
                String discipline = enrollment.get("disciplina").toLowerCase();
                String status = enrollment.get("estado").toLowerCase();
                String reason = enrollment.get("razon") != null ? enrollment.get("razon").toLowerCase() : "";

                if (username.contains(lowerCaseSearchText) ||
                    discipline.contains(lowerCaseSearchText) ||
                    status.contains(lowerCaseSearchText) ||
                    reason.contains(lowerCaseSearchText)) {
                    enrollmentListModel.addElement(enrollment);
                }
            }
        }
        enrollmentJList.clearSelection();
        btnReactivateEnrollment.setEnabled(false);
        btnViewReason.setEnabled(false);
    }

    private static void reactivateSelectedEnrollment(JPanel parentPanel) {
        Map<String, String> selectedEnrollment = enrollmentJList.getSelectedValue();

        if (selectedEnrollment == null) {
            JOptionPane.showMessageDialog(parentPanel, "Selecciona una inscripción para habilitar.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String username = selectedEnrollment.get("username");
        String discipline = selectedEnrollment.get("disciplina");

        int confirm = JOptionPane.showConfirmDialog(parentPanel,
                "¿Seguro que quieres habilitar la inscripción de '" + username + "' en '" + discipline + "'?",
                "Confirmar Habilitación", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (DataBase.reactivarInscripcionUsuario(username, discipline)) {
                JOptionPane.showMessageDialog(parentPanel, "Inscripción de '" + username + "' en '" + discipline + "' habilitada con éxito.");
                loadEnrollments(); // Recargar la lista para reflejar el cambio
            } else {
                JOptionPane.showMessageDialog(parentPanel, "No se pudo habilitar la inscripción.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static void viewSelectedReason(JPanel parentPanel) {
        Map<String, String> selectedEnrollment = enrollmentJList.getSelectedValue();

        if (selectedEnrollment == null) {
            JOptionPane.showMessageDialog(parentPanel, "Selecciona una inscripción para ver la razón.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String reason = selectedEnrollment.get("razon");
        String username = selectedEnrollment.get("username");
        String discipline = selectedEnrollment.get("disciplina");
        String status = selectedEnrollment.get("estado");

        if (reason == null || reason.trim().isEmpty()) {
            JOptionPane.showMessageDialog(parentPanel,
                    "No se registró una razón de inactivación para la inscripción de " + username + " en " + discipline + " (Estado: " + status + ").",
                    "Razón no Disponible", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JTextArea reasonArea = new JTextArea(reason);
            reasonArea.setWrapStyleWord(true);
            reasonArea.setLineWrap(true);
            reasonArea.setEditable(false);
            reasonArea.setFont(new Font("Arial", Font.PLAIN, 14));
            reasonArea.setForeground(Color.BLACK);
            reasonArea.setBackground(new Color(240, 240, 240));
            
            JScrollPane scrollReason = new JScrollPane(reasonArea);
            scrollReason.setPreferredSize(new Dimension(300, 150)); // Tamaño sugerido para el cuadro de diálogo
            scrollReason.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

            JOptionPane.showMessageDialog(parentPanel,
                    scrollReason,
                    "Razón de Inactivación: " + username + " - " + discipline,
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }


    // --- Custom Cell Renderer para la JList ---
    static class EnrollmentCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value instanceof Map) {
                Map<String, String> enrollment = (Map<String, String>) value;
                String username = enrollment.get("username");
                String discipline = enrollment.get("disciplina");
                String status = enrollment.get("estado");
                String reason = enrollment.get("razon");

                String displayText = String.format("<html><b>Usuario:</b> %s<br><b>Disciplina:</b> %s<br><b>Estado:</b> <span style='color:%s;'>%s</span>",
                        username,
                        discipline,
                        getStatusColorHex(status),
                        getStatusText(status)
                );

                if (reason != null && !reason.trim().isEmpty()) {
                    displayText += "<br><i>Razón: " + reason + "</i>";
                }
                displayText += "</html>";

                setText(displayText);
                setBorder(new EmptyBorder(5, 10, 5, 10)); // Añadir padding interno
            }
            return this;
        }

        private String getStatusText(String status) {
            switch (status) {
                case "rechazado": return "Rechazado";
                case "inactivo": return "Inactivo";
                case "completado": return "Completado";
                default: return status;
            }
        }

        private String getStatusColorHex(String status) {
            switch (status) {
                case "rechazado": return "#FF4444"; // Rojo vibrante
                case "inactivo": return "#FFAA00";  // Naranja
                case "completado": return "#5555FF"; // Azul
                default: return "#FFFFFF"; // Blanco por defecto (para 'activo' si lo mostráramos aquí)
            }
        }
    }
}