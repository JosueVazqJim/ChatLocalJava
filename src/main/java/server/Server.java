package server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Server {
    //conexion a la base de datos
    private Conexion conectar = Conexion.getInstancia();
    private Connection conexion = conectar.conectar(); //se conecta a la base de datos
    private static final int PORT = 5000; //abre el puerto 5000
    private List<GestionCliente> clientes = new ArrayList<>(); //en esta lista se guardan los clientes
    private ServerSocket serverSocket;


    public Server(){
        try {
            serverSocket = new ServerSocket(PORT); //levantamos el servidor
            System.out.println("Se abrio el servidor en el puerto: " + PORT);
            acceptConnections();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Server();
    }

    public void acceptConnections() throws IOException { //Metodo para aceptar conexiones
        while (true) { //mientras este encendido el servidor
            Socket sc = serverSocket.accept(); //aceptamos conexiones, esta a la espera de clientes
            System.out.println("Nuevo cliente conectado: " + sc); //se conecto un cliente
            GestionCliente cliente = new GestionCliente(sc, this);
            clientes.add(cliente);
            System.out.println("Nuevo cliente conectado. Clientes totales: " + clientes.size());
            Thread hilo = new Thread(cliente); //el hilo se crea para que este escuhando al cliente
            hilo.start();
        }
    }

    public boolean verificarInicioSesion(String nombre, String pass) {
        try {
            PreparedStatement consulta = conexion.prepareStatement("SELECT * FROM usuarios WHERE nombre = ? AND pass = ?");
            consulta.setString(1, nombre);
            consulta.setString(2, pass);
            ResultSet resultado = consulta.executeQuery();

            if (resultado.next()) { //si hay un resultado
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
            // Verificar si el usuario ya existe
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
                System.out.println("Usuario registrado: " + nombre + " " + pass);
                return true;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void enviarMensajeGlobal(String usuario, String mensaje) {
        System.out.println("Mensaje recibido: " + usuario + ": " + mensaje);
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
            //lista para guardar los mensajes. Cada linea es un mensaje con el nombre del usuario, el mensaje y la fecha
            List<String> mensajes = new ArrayList<>();
            while (resultado.next()) { //va recorriendo uno por uno los mensajes
                String nombreUsuario = resultado.getString("nombre");
                String mensaje = resultado.getString("mensaje");
                String fecha = resultado.getString("fecha_envio");
                mensajes.add(nombreUsuario + ": " + mensaje + "\t\t\t" + fecha); //le damos el formato
            }
            return mensajes.toArray();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public String pathAlmacenamiento() {
        return "src/main/java/server/almacenamiento/";
    }

    public void removerCliente(GestionCliente cliente) {
        clientes.remove(cliente);
        System.out.println("Cliente desconectado. Clientes totales: " + clientes.size());

    }
    public Object[] obtenerArchivosDisponibles() {
        try {
            File carpetaArchivos = new File("src/main/java/server/almacenamiento");
            File[] archivos = carpetaArchivos.listFiles();
            List<String> nombresArchivos = new ArrayList<>();
            if (archivos != null) {
                for (File archivo : archivos) {
                    if (archivo.isFile()) {
                        System.out.println("Archivo encontrado: " + archivo.getName());
                        nombresArchivos.add(archivo.getName());
                    }
                }
            }
            return nombresArchivos.toArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] obtenerArchivo(String archivoDescargar) {
        try {
            File archivo = new File("src/main/java/server/almacenamiento/" + archivoDescargar);
            byte[] fileBytes = new byte[(int) archivo.length()]; //se crea un arreglo de bytes con el tamaño del archivo para guardar el contenido
            FileInputStream fileInputStream = new FileInputStream(archivo); //se crea un flujo de entrada para leer el archivo
            fileInputStream.read(fileBytes); //se lee el archivo y se guarda en el arreglo de bytes
            return fileBytes;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
