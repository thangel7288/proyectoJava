package admin;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

// Importa la clase de login para poder volver al menú principal
import logear.login;

public class AdminDashboard {

    public static void showAdminDashboard() {
        JFrame frame = new JFrame("Panel de Administración - Academia MMA");
        frame.setSize(1200, 800);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(25, 30, 40));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(40, 50, 65));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        JLabel titleLabel = new JLabel("⚙️ PANEL DE ADMINISTRACIÓN", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(new Color(255, 215, 0));
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        // Botón Cerrar Sesión del administrador
        JButton btnLogout = new JButton("🚪 CERRAR SESIÓN");
        btnLogout.setFont(new Font("Arial", Font.BOLD, 14));
        btnLogout.setBackground(new Color(200, 50, 50));
        btnLogout.setForeground(Color.RED);
        btnLogout.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btnLogout.addActionListener(e -> {
            frame.dispose();
            login.showMainMenu();
        });

        JPanel logoutPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        logoutPanel.setBackground(new Color(40, 50, 65));
        logoutPanel.add(btnLogout);
        headerPanel.add(logoutPanel, BorderLayout.EAST);

        // Pestañas de gestión
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 16));
        tabbedPane.setBackground(new Color(25, 30, 40));
        tabbedPane.setForeground(Color.BLACK); // CAMBIO: Texto de la pestaña a negro


        // Pestaña 1: Gestión de Usuarios (regulares)
        JPanel userManagementPanel = UserManagementPanel.createPanel();
        tabbedPane.addTab("👤 Gestión de Usuarios", userManagementPanel);

        // Pestaña 2: Gestión de Disciplinas (¡AHORA SÍ, EL PANEL COMPLETO!)
        JPanel disciplineManagementPanel = DisciplineManagementPanel.createPanel(); // <-- ¡ESTA LÍNEA ES CLAVE!
        tabbedPane.addTab("🥋 Gestión de Disciplinas", disciplineManagementPanel); // <-- ¡Y ESTA!

        // Pestaña 3: Gestión de Contenido (Progreso/Exámenes)
        JPanel contentManagementPanel = ContentManagementPanel.createPanel();
        tabbedPane.addTab("📈 Gestión de Contenido", contentManagementPanel);

        // Pestaña 4: Reactivar Inscripciones
        JPanel enrollmentReactivationPanel = EnrollmentReactivationPanel.createPanel();
        tabbedPane.addTab("🔄 Reactivar Inscripciones", enrollmentReactivationPanel);

        // Pestaña 5: Gestión de Cuentas de Administrador
        JPanel adminAccountManagementPanel = AdminAccountManagementPanel.createPanel();
        tabbedPane.addTab("🛡️ Cuentas Admin", adminAccountManagementPanel);


        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        frame.add(mainPanel);
        frame.setVisible(true);
    }
}