package logear;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;

public class DataBase {

    private static Connection connection;

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/sistema_login",
                "root",
                "7288"
            );
            System.out.println("Conexión exitosa a la base de datos.");
        } catch (ClassNotFoundException e) {
            System.err.println("Error: No se encontró el controlador JDBC de MySQL. " + e.getMessage());
            JOptionPane.showMessageDialog(null, "Error: No se encontró el controlador JDBC de MySQL.", "Error de Conexión", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        } catch (SQLException e) {
            System.err.println("Error al conectar a la base de datos: " + e.getMessage());
            JOptionPane.showMessageDialog(null, "Error al conectar a la base de datos:\n" + e.getMessage() + "\nAsegúrate de que MySQL esté corriendo.", "Error de Conexión", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Registra un nuevo USUARIO REGULAR en la base de datos.
     * El tipo siempre será 'usuario'. Para administradores, usar registrarAdmin().
     * @param username Nombre de usuario.
     * @param password Contraseña (¡Recomendación: Usar hashing para seguridad!).
     * @param numeroDocumento Número de documento único.
     * @param fechaNacimientoStr Fecha de nacimiento en formato "dd/MM/yyyy".
     * @return true si el registro fue exitoso, false en caso contrario.
     */
    public static boolean registrarUsuario(String username, String password, String numeroDocumento, String fechaNacimientoStr) {
        String sql = "INSERT INTO usuarios (username, password, numero_documento, fecha_nacimiento, tipo) VALUES (?, ?, ?, ?, 'usuario')";
        
        java.sql.Date sqlFechaNacimiento = null;
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate localDate = LocalDate.parse(fechaNacimientoStr, formatter);
            sqlFechaNacimiento = java.sql.Date.valueOf(localDate);
        } catch (DateTimeParseException e) {
            System.err.println("Error de formato de fecha al intentar registrar usuario: " + e.getMessage());
            JOptionPane.showMessageDialog(null, "Error interno con el formato de la fecha de nacimiento.", "Error de Registro", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password); // ¡RECOMENDACIÓN: HASHEAR LA CONTRASEÑA ANTES DE ALMACENARLA!
            stmt.setString(3, numeroDocumento);
            stmt.setDate(4, sqlFechaNacimiento);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) { // Código de error para entrada duplicada (MySQL)
                if (e.getMessage().toLowerCase().contains("for key 'username'")) {
                    JOptionPane.showMessageDialog(null, "El nombre de usuario '" + username + "' ya está registrado.", "Error de Registro", JOptionPane.ERROR_MESSAGE);
                } else if (e.getMessage().toLowerCase().contains("for key 'numero_documento'")) {
                    JOptionPane.showMessageDialog(null, "El número de documento '" + numeroDocumento + "' ya está registrado.", "Error de Registro", JOptionPane.ERROR_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(null, "Error al registrar usuario: Dato duplicado. " + e.getMessage(), "Error de Registro", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(null, "Error al registrar usuario: " + e.getMessage(), "Error de Registro", JOptionPane.ERROR_MESSAGE);
            }
            System.err.println("Error SQL al registrar usuario: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Autentica a un usuario (normal o admin) y devuelve su tipo.
     * Primero busca en la tabla 'usuarios' (tipo 'usuario'), luego en 'administradores'.
     * @param username Nombre de usuario.
     * @param password Contraseña.
     * @return El tipo de usuario ('usuario' o 'admin') si la autenticación es exitosa, null en caso contrario.
     */
    public static String autenticarUsuario(String username, String password) {
        // Intentar autenticar como USUARIO normal
        String sqlUsuario = "SELECT 'usuario' AS tipo FROM usuarios WHERE username = ? AND password = ?"; // ¡RECOMENDACIÓN: COMPARAR CON HASH DE CONTRASEÑA!
        try (PreparedStatement stmt = connection.prepareStatement(sqlUsuario)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return "usuario";
            }
        } catch (SQLException e) {
            System.err.println("Error SQL al autenticar usuario (tabla 'usuarios'): " + e.getMessage());
            e.printStackTrace();
        }

        // Si no es un usuario normal, intentar autenticar como ADMINISTRADOR
        String sqlAdmin = "SELECT 'admin' AS tipo FROM administradores WHERE username = ? AND password = ?"; // ¡RECOMENDACIÓN: COMPARAR CON HASH DE CONTRASEÑA!
        try (PreparedStatement stmt = connection.prepareStatement(sqlAdmin)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return "admin";
            }
        } catch (SQLException e) {
            System.err.println("Error SQL al autenticar administrador (tabla 'administradores'): " + e.getMessage());
            e.printStackTrace();
        }
        return null; // No encontrado ni como usuario ni como admin
    }

    /**
     * Verifica si un nombre de usuario normal ya existe en la base de datos (tabla 'usuarios').
     * @param username Nombre de usuario a verificar.
     * @return true si el usuario existe, false en caso contrario.
     */
    public static boolean usuarioExiste(String username) {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE username = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error SQL al verificar existencia de usuario: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Verifica si un nombre de usuario administrador ya existe en la base de datos (tabla 'administradores').
     * @param username Nombre de usuario a verificar.
     * @return true si el admin existe, false en caso contrario.
     */
    public static boolean adminExiste(String username) {
        String sql = "SELECT COUNT(*) FROM administradores WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error SQL al verificar existencia de admin: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Verifica si un número de documento ya existe en la base de datos (solo para usuarios normales).
     * @param numeroDocumento Número de documento a verificar.
     * @return true si el documento existe, false en caso contrario.
     */
    public static boolean documentoExiste(String numeroDocumento) {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE numero_documento = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, numeroDocumento);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error SQL al verificar existencia de documento: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Obtiene una lista de todos los nombres de usuario NORMALES registrados.
     * @return Lista de nombres de usuario.
     */
    public static List<String> obtenerTodosUsernames() {
        List<String> usernames = new ArrayList<>();
        String sql = "SELECT username FROM usuarios ORDER BY username";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                usernames.add(rs.getString("username"));
            }
        } catch (SQLException e) {
            System.err.println("Error SQL al obtener todos los usernames: " + e.getMessage());
            e.printStackTrace();
        }
        return usernames;
    }

    /**
     * Obtiene una lista de todos los nombres de usuario ADMINISTRADORES registrados.
     * @return Lista de nombres de usuario administradores.
     */
    public static List<String> obtenerTodosAdminUsernames() {
        List<String> adminUsernames = new ArrayList<>();
        String sql = "SELECT username FROM administradores ORDER BY username";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                adminUsernames.add(rs.getString("username"));
            }
        } catch (SQLException e) {
            System.err.println("Error SQL al obtener todos los admin usernames: " + e.getMessage());
            e.printStackTrace();
        }
        return adminUsernames;
    }

    /**
     * Elimina un usuario normal de la base de datos.
     * Debido a ON DELETE CASCADE, también eliminará sus inscripciones y grados asociados.
     * @param username Nombre de usuario a eliminar.
     * @return true si el usuario fue eliminado exitosamente, false en caso contrario.
     */
    public static boolean eliminarUsuario(String username) {
        String sql = "DELETE FROM usuarios WHERE username = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
        catch (SQLException e) {
            System.err.println("Error SQL al eliminar usuario: " + e.getMessage());
            JOptionPane.showMessageDialog(null, "Error al eliminar usuario de la DB: " + e.getMessage(), "Error de DB", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Elimina un administrador de la base de datos (tabla 'administradores').
     * @param username Nombre de usuario administrador a eliminar.
     * @return true si el admin fue eliminado exitosamente, false en caso contrario.
     */
    public static boolean eliminarAdmin(String username) {
        String sql = "DELETE FROM administradores WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error SQL al eliminar administrador: " + e.getMessage());
            JOptionPane.showMessageDialog(null, "Error al eliminar administrador de la DB: " + e.getMessage(), "Error de DB", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Registra un nuevo administrador en la base de datos (tabla 'administradores').
     * @param username Nombre de usuario administrador.
     * @param password Contraseña.
     * @return true si el registro fue exitoso, false en caso contrario.
     */
    public static boolean registrarAdmin(String username, String password) {
        String sql = "INSERT INTO administradores (username, password) VALUES (?, ?)"; // ¡RECOMENDACIÓN: HASHEAR LA CONTRASEÑA!
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) { // Duplicado
                JOptionPane.showMessageDialog(null, "El nombre de administrador '" + username + "' ya está registrado.", "Error de Registro Admin", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, "Error al registrar administrador: " + e.getMessage(), "Error de Registro Admin", JOptionPane.ERROR_MESSAGE);
            }
            System.err.println("Error SQL al registrar administrador: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }


    /**
     * Inscribe a un usuario en una disciplina.
     * Realiza validaciones para evitar inscripciones duplicadas activas o en estado 'rechazado'.
     * @param username_usuario Nombre de usuario.
     * @param nombre_disciplina Nombre de la disciplina.
     * @return true si la inscripción fue exitosa, false en caso contrario.
     */
    public static boolean inscribirUsuarioEnDisciplina(String username_usuario, String nombre_disciplina) {
        // Primero, verificar si ya existe una inscripción para este usuario y disciplina,
        // especialmente si está en estado 'activo' o 'rechazado'.
        Map<String, String> inscriptionStatus = obtenerEstadoInscripcionUsuario(username_usuario, nombre_disciplina);
        if (inscriptionStatus != null && inscriptionStatus.containsKey("estado")) {
            String estadoExistente = inscriptionStatus.get("estado");
            String razonExistente = inscriptionStatus.get("razon_inactivacion");

            if ("activo".equals(estadoExistente)) {
                JOptionPane.showMessageDialog(null,
                    "Ya estás inscrito en la disciplina '" + nombre_disciplina + "'.",
                    "Error de Inscripción", JOptionPane.WARNING_MESSAGE);
                return false;
            } else if ("rechazado".equals(estadoExistente)) {
                JOptionPane.showMessageDialog(null,
                    "No puedes inscribirte a la disciplina '" + nombre_disciplina + "'.\nRazón: " + (razonExistente != null && !razonExistente.isEmpty() ? razonExistente : "Motivo no especificado."),
                    "Inscripción Rechazada", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            // Si el estado es 'inactivo' o 'completado', podemos permitir una nueva inscripción.
            // Si el deseo es reactivar, se debe usar reactivarInscripcionUsuario().
        }

        String sql = "INSERT INTO inscripciones (username_usuario, nombre_disciplina, estado) VALUES (?, ?, 'activo')";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username_usuario);
            pstmt.setString(2, nombre_disciplina);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            // Este catch podría seguir capturando la duplicidad si la validación previa no es exhaustiva.
            if (e.getErrorCode() == 1062) { // Duplicado
                JOptionPane.showMessageDialog(null,
                    "Ya existe un registro de inscripción para esta disciplina (puede que no esté activo).",
                    "Error de Inscripción", JOptionPane.WARNING_MESSAGE);
            } else if (e.getErrorCode() == 1452) { // Foreign Key Constraint (usuario no existe)
                JOptionPane.showMessageDialog(null,
                    "Error: El usuario '" + username_usuario + "' no existe para inscribirlo en '" + nombre_disciplina + "'.",
                    "Error de Inscripción", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null,
                    "Error al inscribir a " + username_usuario + " en " + nombre_disciplina + ": " + e.getMessage(),
                    "Error de Inscripción", JOptionPane.ERROR_MESSAGE);
            }
            System.err.println("Error SQL al inscribir usuario en disciplina: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Desinscribe a un usuario de una disciplina, cambiando su estado a 'rechazado' y registrando una razón.
     * @param username_usuario Nombre de usuario.
     * @param nombre_disciplina Nombre de la disciplina.
     * @param razon Razón de la desinscripción.
     * @return true si la desinscripción fue exitosa, false en caso contrario.
     */
    public static boolean desinscribirUsuarioDeDisciplina(String username_usuario, String nombre_disciplina, String razon) {
        String sql = "UPDATE inscripciones SET estado = ?, razon_inactivacion = ? WHERE username_usuario = ? AND nombre_disciplina = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, "rechazado"); // Cambiamos el estado a 'rechazado'
            pstmt.setString(2, razon);       // Guardamos la razón
            pstmt.setString(3, username_usuario);
            pstmt.setString(4, nombre_disciplina);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                "Error al desinscribir a " + username_usuario + " de " + nombre_disciplina + ": " + e.getMessage(),
                "Error de Desinscripción", JOptionPane.ERROR_MESSAGE);
            System.err.println("Error SQL al desinscribir usuario de disciplina: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Obtiene una lista de las disciplinas en las que un usuario está activamente inscrito.
     * @param username_usuario Nombre de usuario.
     * @return Lista de nombres de disciplinas activas.
     */
    public static List<String> obtenerDisciplinasInscritas(String username_usuario) {
        List<String> disciplinas = new ArrayList<>();
        // Solo obtenemos las disciplinas con estado 'activo'
        String sql = "SELECT nombre_disciplina FROM inscripciones WHERE username_usuario = ? AND estado = 'activo'";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username_usuario);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                disciplinas.add(rs.getString("nombre_disciplina"));
            }
        } catch (SQLException e) {
            System.err.println("Error SQL al obtener disciplinas activas inscritas para " + username_usuario + ": " + e.getMessage());
            e.printStackTrace();
        }
        return disciplinas;
    }

    /**
     * Obtiene el estado y la razón de inactivación de una inscripción específica de un usuario.
     * @param username_usuario Nombre de usuario.
     * @param nombre_disciplina Nombre de la disciplina.
     * @return Un mapa con "estado" y "razon_inactivacion" si la inscripción existe, null en caso contrario.
     */
    public static Map<String, String> obtenerEstadoInscripcionUsuario(String username_usuario, String nombre_disciplina) {
        Map<String, String> statusInfo = null;
        String sql = "SELECT estado, razon_inactivacion FROM inscripciones WHERE username_usuario = ? AND nombre_disciplina = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username_usuario);
            pstmt.setString(2, nombre_disciplina);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                statusInfo = new HashMap<>();
                statusInfo.put("estado", rs.getString("estado"));
                statusInfo.put("razon_inactivacion", rs.getString("razon_inactivacion"));
            }
        } catch (SQLException e) {
            System.err.println("Error SQL al obtener estado de inscripción para " + username_usuario + " en " + nombre_disciplina + ": " + e.getMessage());
            e.printStackTrace();
        }
        return statusInfo;
    }

    /**
     * Reactiva una inscripción de un usuario, cambiando su estado a 'activo' y borrando la razón de inactivación.
     * @param username_usuario Nombre de usuario.
     * @param nombre_disciplina Nombre de la disciplina.
     * @return true si la reactivación fue exitosa, false en caso contrario.
     */
    public static boolean reactivarInscripcionUsuario(String username_usuario, String nombre_disciplina) {
        String sql = "UPDATE inscripciones SET estado = 'activo', razon_inactivacion = NULL WHERE username_usuario = ? AND nombre_disciplina = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username_usuario);
            pstmt.setString(2, nombre_disciplina);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
        catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                "Error al reactivar inscripción para " + username_usuario + " en " + nombre_disciplina + ": " + e.getMessage(),
                "Error de Reactivación", JOptionPane.ERROR_MESSAGE);
            System.err.println("Error SQL al reactivar inscripción: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Obtiene todas las inscripciones que no están en estado 'activo'.
     * Utilizado para paneles de administración que gestionan re-habilitación.
     * Devuelve una lista de Maps, donde cada Map representa una inscripción
     * con claves como "username", "disciplina", "estado", y "razon".
     */
    public static List<Map<String, String>> obtenerInscripcionesInactivasRechazadas() {
        List<Map<String, String>> inactiveEnrollments = new ArrayList<>();
        String sql = "SELECT username_usuario, nombre_disciplina, estado, razon_inactivacion FROM inscripciones WHERE estado != 'activo' ORDER BY username_usuario, nombre_disciplina";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Map<String, String> enrollmentInfo = new HashMap<>();
                enrollmentInfo.put("username", rs.getString("username_usuario"));
                enrollmentInfo.put("disciplina", rs.getString("nombre_disciplina"));
                enrollmentInfo.put("estado", rs.getString("estado"));
                enrollmentInfo.put("razon", rs.getString("razon_inactivacion"));
                inactiveEnrollments.add(enrollmentInfo);
            }
        } catch (SQLException e) {
            System.err.println("Error SQL al obtener inscripciones inactivas/rechazadas: " + e.getMessage());
            e.printStackTrace();
        }
        return inactiveEnrollments;
    }


    // --- MÉTODOS PARA GESTIÓN DE DISCIPLINAS ---

    /**
     * Obtiene todas las disciplinas de la base de datos, con opción de incluir inactivas.
     * @param includeInactive Si es true, incluye disciplinas con activo=false. Si es false, solo activo=true.
     * @return Lista de mapas, donde cada mapa representa una disciplina con sus detalles.
     */
    public static List<Map<String, String>> obtenerTodasLasDisciplinas(boolean includeInactive) {
        List<Map<String, String>> disciplinas = new ArrayList<>();
        String sql = "SELECT id, nombre, descripcion, horario, instructor, nivel, activo FROM disciplinas";
        if (!includeInactive) {
            sql += " WHERE activo = TRUE";
        }
        sql += " ORDER BY nombre";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, String> disciplinaInfo = new HashMap<>();
                disciplinaInfo.put("id", String.valueOf(rs.getInt("id")));
                disciplinaInfo.put("nombre", rs.getString("nombre"));
                disciplinaInfo.put("descripcion", rs.getString("descripcion"));
                disciplinaInfo.put("horario", rs.getString("horario"));
                disciplinaInfo.put("instructor", rs.getString("instructor"));
                disciplinaInfo.put("nivel", rs.getString("nivel"));
                disciplinaInfo.put("activo", String.valueOf(rs.getBoolean("activo")));
                disciplinas.add(disciplinaInfo);
            }
        } catch (SQLException e) {
            System.err.println("Error SQL al obtener todas las disciplinas: " + e.getMessage());
            e.printStackTrace();
        }
        return disciplinas;
    }

    /**
     * Sobrecarga para obtener solo disciplinas activas por defecto.
     * @return Lista de mapas de disciplinas activas.
     */
    public static List<Map<String, String>> obtenerTodasLasDisciplinas() {
        return obtenerTodasLasDisciplinas(false); // Por defecto, no incluir inactivas
    }


    /**
     * Añade una nueva disciplina a la base de datos.
     * @param nombre Nombre de la disciplina.
     * @param descripcion Descripción de la disciplina.
     * @param horario Horario de la disciplina.
     * @param instructor Instructor de la disciplina.
     * @param nivel Nivel de la disciplina.
     * @return true si la adición fue exitosa, false en caso contrario.
     */
    public static boolean añadirDisciplina(String nombre, String descripcion, String horario, String instructor, String nivel) {
        String sql = "INSERT INTO disciplinas (nombre, descripcion, horario, instructor, nivel, activo) VALUES (?, ?, ?, ?, ?, TRUE)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, nombre);
            pstmt.setString(2, descripcion);
            pstmt.setString(3, horario);
            pstmt.setString(4, instructor);
            pstmt.setString(5, nivel);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) { // Duplicado
                JOptionPane.showMessageDialog(null, "Ya existe una disciplina con el nombre '" + nombre + "'.", "Error de Disciplina", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, "Error al añadir disciplina: " + e.getMessage(), "Error de Disciplina", JOptionPane.ERROR_MESSAGE);
            }
            System.err.println("Error SQL al añadir disciplina: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Actualiza los detalles de una disciplina existente.
     * @param id ID de la disciplina a actualizar.
     * @param nombre Nuevo nombre de la disciplina.
     * @param descripcion Nueva descripción.
     * @param horario Nuevo horario.
     * @param instructor Nuevo instructor.
     * @param nivel Nuevo nivel.
     * @return true si la actualización fue exitosa, false en caso contrario.
     */
    public static boolean actualizarDisciplina(int id, String nombre, String descripcion, String horario, String instructor, String nivel) {
        String sql = "UPDATE disciplinas SET nombre = ?, descripcion = ?, horario = ?, instructor = ?, nivel = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, nombre);
            pstmt.setString(2, descripcion);
            pstmt.setString(3, horario);
            pstmt.setString(4, instructor);
            pstmt.setString(5, nivel);
            pstmt.setInt(6, id);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) { // Duplicado
                JOptionPane.showMessageDialog(null, "Ya existe otra disciplina con el nombre '" + nombre + "'.", "Error de Disciplina", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, "Error al actualizar disciplina: " + e.getMessage(), "Error de Disciplina", JOptionPane.ERROR_MESSAGE);
            }
            System.err.println("Error SQL al actualizar disciplina: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }


    // --- MÉTODOS PARA GESTIÓN DE GRADOS ---

    /**
     * Añade un grado obtenido por un usuario en una disciplina.
     */
    public static boolean añadirGrado(String username_usuario, String nombre_disciplina, String grado_obtenido, String fecha_obtencion_str, String notas) {
        String sql = "INSERT INTO grados (username_usuario, nombre_disciplina, grado_obtenido, fecha_obtencion, notas) VALUES (?, ?, ?, ?, ?)";
        java.sql.Date sqlFechaObtencion = null;
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate localDate = LocalDate.parse(fecha_obtencion_str, formatter);
            sqlFechaObtencion = java.sql.Date.valueOf(localDate);
        } catch (DateTimeParseException e) {
            System.err.println("Error de formato de fecha al añadir grado (DataBase): " + e.getMessage());
            JOptionPane.showMessageDialog(null, "Error interno con el formato de la fecha de obtención del grado.", "Error de Grado", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username_usuario);
            pstmt.setString(2, nombre_disciplina);
            pstmt.setString(3, grado_obtenido);
            pstmt.setDate(4, sqlFechaObtencion);
            pstmt.setString(5, notas);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                JOptionPane.showMessageDialog(null, "El usuario ya tiene registrado ese grado para esa disciplina.", "Error de Grado", JOptionPane.WARNING_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, "Error al añadir grado: " + e.getMessage(), "Error de Grado", JOptionPane.ERROR_MESSAGE);
            }
            System.err.println("Error SQL al añadir grado (DataBase): " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Obtiene los grados obtenidos por un usuario en todas las disciplinas.
     */
    public static List<String[]> obtenerGradosUsuario(String username_usuario) {
        List<String[]> grados = new ArrayList<>();
        String sql = "SELECT nombre_disciplina, grado_obtenido, fecha_obtencion, notas FROM grados WHERE username_usuario = ? ORDER BY fecha_obtencion DESC";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username_usuario);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                grados.add(new String[]{
                    rs.getString("nombre_disciplina"),
                    rs.getString("grado_obtenido"),
                    rs.getDate("fecha_obtencion").toString(),
                    rs.getString("notas")
                });
            }
        } catch (SQLException e) {
            System.err.println("Error SQL al obtener grados de usuario (DataBase): " + e.getMessage());
            e.printStackTrace();
        }
        return grados;
    }

    /**
     * Elimina un grado específico de un usuario.
     */
    public static boolean eliminarGrado(String username_usuario, String nombre_disciplina, String grado_obtenido) {
        String sql = "DELETE FROM grados WHERE username_usuario = ? AND nombre_disciplina = ? AND grado_obtenido = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username_usuario);
            pstmt.setString(2, nombre_disciplina);
            pstmt.setString(3, grado_obtenido);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al eliminar grado: " + e.getMessage(), "Error de Grado", JOptionPane.ERROR_MESSAGE);
            System.err.println("Error SQL al eliminar grado (DataBase): " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }


    // --- MÉTODOS PARA GESTIÓN DE EXÁMENES ---

    /**
     * Añade un nuevo examen programado para una disciplina.
     */
    public static boolean añadirExamen(String nombre_disciplina, String fecha_examen_str, String hora_examen, String descripcion, String tipo_examen) {
        String sql = "INSERT INTO examenes (nombre_disciplina, fecha_examen, hora_examen, descripcion, tipo_examen) VALUES (?, ?, ?, ?, ?)";
        java.sql.Date sqlFechaExamen = null;
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate localDate = LocalDate.parse(fecha_examen_str, formatter);
            sqlFechaExamen = java.sql.Date.valueOf(localDate);
        } catch (DateTimeParseException e) {
            System.err.println("Error de formato de fecha al añadir examen (DataBase): " + e.getMessage());
            JOptionPane.showMessageDialog(null, "Error interno con el formato de la fecha del examen.", "Error de Examen", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, nombre_disciplina);
            pstmt.setDate(2, sqlFechaExamen);
            pstmt.setString(3, hora_examen);
            pstmt.setString(4, descripcion);
            pstmt.setString(5, tipo_examen);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                JOptionPane.showMessageDialog(null, "Ya existe un examen programado para esa disciplina en la misma fecha y hora.", "Error de Examen", JOptionPane.WARNING_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, "Error al añadir examen: " + e.getMessage(), "Error de Examen", JOptionPane.ERROR_MESSAGE);
            }
            System.err.println("Error SQL al añadir examen (DataBase): " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Obtiene todos los exámenes programados para una disciplina específica.
     * Devuelve un String[] con {nombre_disciplina, fecha_examen, hora_examen, descripcion, tipo_examen}.
     */
    public static List<String[]> obtenerExamenesDisciplina(String nombre_disciplina) {
        List<String[]> examenes = new ArrayList<>();
        // Se selecciona nombre_disciplina también para poder identificar el examen de forma única.
        String sql = "SELECT nombre_disciplina, fecha_examen, hora_examen, descripcion, tipo_examen FROM examenes WHERE nombre_disciplina = ? ORDER BY fecha_examen ASC, hora_examen ASC";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, nombre_disciplina);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                examenes.add(new String[]{
                    rs.getString("nombre_disciplina"), // Elemento [0]
                    rs.getDate("fecha_examen").toString(), // Elemento [1]
                    rs.getString("hora_examen"),         // Elemento [2]
                    rs.getString("descripcion"),         // Elemento [3]
                    rs.getString("tipo_examen")          // Elemento [4]
                });
            }
        } catch (SQLException e) {
            System.err.println("Error SQL al obtener exámenes de disciplina (DataBase): " + e.getMessage());
            e.printStackTrace();
        }
        return examenes;
    }

    /**
     * Elimina un examen específico de la base de datos.
     * Identifica el examen por la disciplina, fecha y hora.
     */
    public static boolean eliminarExamen(String nombre_disciplina, String fecha_examen_str, String hora_examen) {
        String sql = "DELETE FROM examenes WHERE nombre_disciplina = ? AND fecha_examen = ? AND hora_examen = ?";
        
        java.sql.Date sqlFechaExamen = null;
        try {
            sqlFechaExamen = java.sql.Date.valueOf(fecha_examen_str);
        } catch (IllegalArgumentException e) {
            System.err.println("Error de formato de fecha al intentar eliminar examen (DataBase): " + e.getMessage());
            JOptionPane.showMessageDialog(null, "Error interno con el formato de la fecha del examen al intentar eliminar.", "Error de Examen", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, nombre_disciplina);
            pstmt.setDate(2, sqlFechaExamen);
            pstmt.setString(3, hora_examen);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al eliminar examen de la base de datos: " + e.getMessage(), "Error de DB", JOptionPane.ERROR_MESSAGE);
            System.err.println("Error SQL al eliminar examen (DataBase): " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }


    /**
     * Cierra la conexión a la base de datos.
     * ¡Importante: Llamar a este método al cerrar la aplicación para liberar recursos!
     */
    public static void cerrarConexion() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Conexión a la base de datos cerrada.");
            }
        } catch (SQLException e) {
            System.err.println("Error al cerrar conexión a la base de datos: " + e.getMessage());
            e.printStackTrace();
        }
    }

	public static boolean registrarUsuario(String username, String password, String document, String dobString,
			String string) {
		// TODO Auto-generated method stub
		return false;
	}
}