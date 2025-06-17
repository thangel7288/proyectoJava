package logear;

import javax.swing.*;
import java.awt.*;
import java.io.File; // Se mantiene por si hay otros usos de archivos, aunque los datos principales van a DB

public class login {
    // =============================================
    // 🌟 ZONA PERSONALIZABLE (Modifica aquí)
    // =============================================
    
    // 1. Configuración de la ventana
    private static final String TITULO_VENTANA = "Mi Sistema de Artes Marciales";
    private static final int ANCHO = 900;
    private static final int ALTO = 500;
    private static final Color COLOR_FONDO = new Color(240, 240, 240);
    
    // 2. Configuración de imagen (Asegúrate de que 'src/imagenes/karate.jpg' exista)
    private static final String RUTA_IMAGEN = "/imagenes/karate.jpg"; // <-- RUTA CORREGIDA
    private static final int ANCHO_IMAGEN = 300;
    
    // 3. Texto principal
    private static final String TITULO = "BIENVENIDO";
    private static final Font FUENTE_TITULO = new Font("Arial", Font.BOLD, 28);
    private static final Color COLOR_TITULO = new Color(50, 50, 50);
    
    // 4. Botones (Texto, Color fondo, Accion)
    private static final BotonConfig[] BOTONES = {
            new BotonConfig("USUARIOS", new Color(46, 125, 50), () -> UserLogin.showLoginWindow()),
            new BotonConfig("ADMINISTRADORES", new Color(25, 118, 210), () -> AdminLogin.showAdminLogin()),
            new BotonConfig("SALIR", new Color(211, 47, 47), () -> System.exit(0))
    };
    
    // Clase auxiliar para configuración de botones
    private static class BotonConfig {
        String texto;
        Color color;
        Runnable accion;
        
        public BotonConfig(String texto, Color color, Runnable accion) {
            this.texto = texto;
            this.color = color;
            this.accion = accion;
        }
    }
    
    // =============================================
    // 🛠 FIN DE ZONA PERSONALIZABLE
    // =============================================

    public static void showMainMenu() {
        JFrame frame = new JFrame(TITULO_VENTANA);
        frame.setSize(ANCHO, ALTO);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        
        // Panel Principal
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(COLOR_FONDO);
        
        // Panel Izquierdo (imagen)
        JPanel imagePanel = new JPanel(new BorderLayout());
        imagePanel.setPreferredSize(new Dimension(ANCHO_IMAGEN, 0));
        try {
            ImageIcon icono = new ImageIcon(login.class.getResource(RUTA_IMAGEN));
            // Validar que la imagen se cargó correctamente (ancho > 0)
            if (icono != null && icono.getIconWidth() > 0) {
                // Escalar la imagen proporcionalmente para que quepa bien
                int originalWidth = icono.getIconWidth();
                int originalHeight = icono.getIconHeight();
                double scale = (double)(ANCHO_IMAGEN - 50) / originalWidth; // Reducir un poco el tamaño
                int scaledWidth = (int) (originalWidth * scale);
                int scaledHeight = (int) (originalHeight * scale);

                JLabel imagen = new JLabel(new ImageIcon(icono.getImage().getScaledInstance(
                    scaledWidth, scaledHeight, Image.SCALE_SMOOTH)));
                imagePanel.add(imagen, BorderLayout.CENTER);
            } else {
                JLabel placeholder = new JLabel("<html><center>IMAGEN NO<br>CARGADA</center></html>", SwingConstants.CENTER);
                placeholder.setFont(new Font("Arial", Font.BOLD, 14));
                placeholder.setForeground(Color.RED);
                imagePanel.add(placeholder, BorderLayout.CENTER);
            }
        } catch (Exception e) {
            // Manejar cualquier excepción durante la carga de la imagen
            JLabel placeholder = new JLabel("<html><center>IMAGEN NO<br>CARGADA</center></html>", SwingConstants.CENTER);
            placeholder.setFont(new Font("Arial", Font.BOLD, 14));
            placeholder.setForeground(Color.RED);
            imagePanel.add(placeholder, BorderLayout.CENTER);
            e.printStackTrace(); // Imprime el error en la consola para depuración
        }
        
        // Panel Derecho (contenido)
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        
        // Título
        JLabel titleLabel = new JLabel(TITULO, SwingConstants.CENTER);
        titleLabel.setFont(FUENTE_TITULO);
        titleLabel.setForeground(COLOR_TITULO);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));
        
        // Panel de botones
        JPanel buttonPanel = new JPanel(new GridLayout(BOTONES.length, 1, 20, 20));
        for (BotonConfig config : BOTONES) {
            JButton boton = crearBoton(config);
            buttonPanel.add(boton);
        }
        
        // Ensamblaje
        contentPanel.add(titleLabel, BorderLayout.NORTH);
        contentPanel.add(buttonPanel, BorderLayout.CENTER);
        
        mainPanel.add(imagePanel, BorderLayout.WEST);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        frame.add(mainPanel);
        frame.setVisible(true);
    }
    
    private static JButton crearBoton(BotonConfig config) {
        JButton boton = new JButton(config.texto);
        boton.setBackground(config.color.brighter());
        boton.setForeground(Color.BLACK);
        boton.setFont(new Font("Arial", Font.BOLD, 20));
        boton.setFocusPainted(false);
        boton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.WHITE, 2),
            BorderFactory.createEmptyBorder(15, 30, 15, 30)
        ));
        
        boton.addActionListener(e -> config.accion.run());
        
        return boton;
    }

    public static void main(String[] args) {
        // Asegurarse de que los directorios de datos existan para cualquier archivo auxiliar.
        // NOTA: La persistencia principal de usuarios, disciplinas, inscripciones, grados y exámenes
        // ahora se maneja completamente en la base de datos MySQL.
        // Estos directorios solo serían necesarios si todavía hay funcionalidades secundarias que usen archivos.
        File usersDataDir = new File(UserLogin.USERS_DATA_DIR);
        if (!usersDataDir.exists()) {
            usersDataDir.mkdirs();
        }
        File inscripcionesDir = new File(usuarios.SeleccionDisciplinas.INSCRIPTIONS_DIR);
        if (!inscripcionesDir.exists()) {
            inscripcionesDir.mkdirs();
        }
        File progresoDir = new File(usuarios.ProgresoExamenes.PROGRESS_DIR);
        if (!progresoDir.exists()) {
            progresoDir.mkdirs();
        }

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            showMainMenu();
        });
    }
}
