package server;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

//seran instruccuines del hilo
public class GestionCliente implements Runnable{
    private Socket sc;
    private Server server;
    private ObjectInputStream entrada;
    private ObjectOutputStream salida;
    private String usuario;

    public GestionCliente(Socket sc, Server server) {
        try {
            this.sc = sc;
            this.server = server;
            entrada = new ObjectInputStream(sc.getInputStream());
            salida = new ObjectOutputStream(sc.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void enviarDatos(Object[] datos) {
        try {
            salida.writeObject(datos);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() { //escucha al cliente
        try {
            while (!Thread.currentThread().isInterrupted()) {
                // Leer mensaje del cliente
                Object mensaje = entrada.readObject(); //recibira objetos, y dependera del tipo de objeto la respuesta
                if (mensaje instanceof Object[]) {
                    Object[] datosToServer = (Object[]) mensaje;
                    String tipoMensaje = (String) datosToServer[0];
                    switch (tipoMensaje) {
                        case "inicioSesion":
                            this.usuario = (String) datosToServer[1];
                            String contrasena = (String) datosToServer[2];
                            boolean resultadoInicioSesion = server.verificarInicioSesion(usuario, contrasena);
                            enviarDatos(new Object[]{"resultadoInicioSesion", resultadoInicioSesion});
                            if (!resultadoInicioSesion){
                                server.removerCliente(this);
                                cerrarConexion();
                            }
                            break;
                        case "mensaje":
                            String mensajeGlobal = (String) datosToServer[1];
                            server.enviarMensajeGlobal(usuario, mensajeGlobal);
                            break;
                        case "registro":
                            String nombre = (String) datosToServer[1];
                            String pass = (String) datosToServer[2];
                            boolean resultadoRegistro = server.registrarUsuario(nombre, pass);
                            enviarDatos(new Object[]{"resultadoRegistro", resultadoRegistro});
                            server.removerCliente(this);
                            cerrarConexion();
                            break;
                        case "solicitoMensajesPrevios":
                            Object[] mensajesPrevios = server.obtenerMensajesPrevios();
                            // Agregar el encabezado "mensajesPrevios" al inicio del array de datos
                            Object[] datosRespuesta = new Object[mensajesPrevios.length + 1];
                            datosRespuesta[0] = "mensajesPrevios";
                            System.arraycopy(mensajesPrevios, 0, datosRespuesta, 1, mensajesPrevios.length);
                            enviarDatos(datosRespuesta);
                            break;
                        case "enviarArchivo":
                            String nombreArchivo = (String) datosToServer[1]; //nombre del archivo
                            byte[] fileBytes = (byte[]) datosToServer[2]; //contenido del archivo
                            // prepara el archivo en el lugar de almacenamiento del servidor
                            FileOutputStream fileOutputStream = new FileOutputStream(server.pathAlmacenamiento() + nombreArchivo);
                            fileOutputStream.write(fileBytes); //escribe el archivo donde se le dijo que preparara
                            System.out.println("Archivo recibido: " + nombreArchivo);
                            fileOutputStream.close();
                            break;
                        case "exit":
                            System.out.println("Cerrando conexión");
                            server.removerCliente(this);
                            cerrarConexion();
                            break;
                        default:
                            // Tipo de mensaje desconocido
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error al leer mensaje: " + e.getMessage());
        } finally {
            cerrarConexion();
        }
    }

    private void cerrarConexion() {
        try {
            entrada.close();
            salida.close();
            sc.close();
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            System.err.println("Error al cerrar la conexión: " + e.getMessage());
        }
    }
}
