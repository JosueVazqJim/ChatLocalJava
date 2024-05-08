package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class Server {
    //conexion a la base de datos
    Conexion conectar = Conexion.getInstancia();
    Connection conexion = conectar.conectar(); //se conecta a la base de datos

    private static final int PORT = 5000;
    private List<GestionCliente> clientes = new ArrayList<>();
    //PreparedStatement consultar;

    public Server(){
        try {
            ServerSocket serverSocket = new ServerSocket(PORT); //levantamos el servidor
            System.out.println("Se abrio el servidor en el puerto: " + PORT);

            while (true) {
                Socket sc = serverSocket.accept(); //aceptamos conexiones, esta a la espera de clientes
                System.out.println("Nuevo cliente conectado: " + sc); //se conecto un cliente
                //le creamos al nuevo cliente un hilo
                GestionCliente cliente = new GestionCliente(sc, this);
                clientes.add(cliente);
                System.out.println("Nuevo cliente conectado. Clientes totales: " + clientes.size());
                Thread hilo = new Thread(cliente);
                hilo.start();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        new Server();
    }

    public boolean verificarInicioSesion(String nombre, String pass) {
        try {
            PreparedStatement consulta = conexion.prepareStatement("SELECT * FROM usuarios WHERE nombre = ? AND pass = ?");
            consulta.setString(1, nombre);
            consulta.setString(2, pass);
            ResultSet resultado = consulta.executeQuery();

            if (resultado.next()) {
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean registrarUsuario(String nombre, String pass) {
        try {
            PreparedStatement consulta = conexion.prepareStatement("SELECT * FROM usuarios WHERE nombre = ? AND pass = ?");
            consulta.setString(1, nombre);
            consulta.setString(2, pass);
            ResultSet resultado = consulta.executeQuery();

            if (resultado.next()) {
                return false;
            } else {
                PreparedStatement insertar = conexion.prepareStatement("INSERT INTO usuarios(nombre, pass) VALUES (?, ?)");
                insertar.setString(1, nombre);
                insertar.setString(2, pass);
                insertar.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void enviarMensajeGlobal(String usuario, String mensaje) {
        System.out.println("Mensaje recibido: " + mensaje);
        try {
            // Consultar el ID del usuario
            PreparedStatement insertar = conexion.prepareStatement("SELECT id FROM usuarios WHERE nombre = ?");
            insertar.setString(1, usuario);
            ResultSet resultado = insertar.executeQuery();

            if (resultado.next()){
                int usuarioId = resultado.getInt("id"); //obetenemos el id del usuario
                insertar = conexion.prepareStatement("INSERT INTO mensajes(usuario_id, mensaje) VALUES (?, ?)");
                insertar.setInt(1, usuarioId);
                insertar.setString(2, mensaje);
                insertar.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        Date fecha = new Date(System.currentTimeMillis());
        SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String fechaFormateada = formatoFecha.format(fecha);
        for (GestionCliente cliente : clientes) {
            cliente.enviarDatos(new Object[]{"mensajeGlobal", usuario + ": " + mensaje + "\t\t\t" + fechaFormateada});
        }
    }

    public Object[] obtenerMensajesPrevios() {
        try {
            PreparedStatement consulta = conexion.prepareStatement("SELECT mensajes.mensaje, mensajes.fecha_envio, usuarios.nombre FROM mensajes INNER JOIN usuarios ON mensajes.usuario_id = usuarios.id ORDER BY mensajes.id");
            ResultSet resultado = consulta.executeQuery();
            List<String> mensajes = new ArrayList<>();
            while (resultado.next()) {
                String nombreUsuario = resultado.getString("nombre");
                String mensaje = resultado.getString("mensaje");
                String fecha = resultado.getString("fecha_envio");
                mensajes.add(nombreUsuario + ": " + mensaje + "\t\t\t" + fecha);
            }
            return mensajes.toArray();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public void removerCliente(GestionCliente cliente) {
        clientes.remove(cliente);

        System.out.println("Cliente desconectado. Clientes totales: " + clientes.size());

    }
}