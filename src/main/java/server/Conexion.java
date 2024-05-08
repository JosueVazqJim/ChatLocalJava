package server;

import javax.swing.*;
import java.sql.*;

public class Conexion {
    private static Connection conexion = null;
    private static Conexion instancia = null;

    private static final String URL = "jdbc:mysql://localhost:3306/chatcomputo";
    private static final String USER = "root";
    private static final String PASSWORD = "Normita1230";

    public Connection conectar() { //se parametriza la nueva instancia creada
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conexion = DriverManager.getConnection(URL, USER, PASSWORD);

            return conexion;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al conectar a la base de datos", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return conexion;
    }

    public void cerrarConexion() {
        try {
            if (conexion != null) {
                conexion.close();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al cerrar la conexión", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    public static Conexion getInstancia() {
        if (instancia == null) {
            instancia = new Conexion(); //se crea una nueva instancia
        }
        return instancia;
    }
}
