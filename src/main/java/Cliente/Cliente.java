package Cliente;

import java.io.*;
import java.net.Socket;

public class Cliente {
    private Socket socket;
    private ObjectOutputStream salida;
    private ObjectInputStream entrada;

    public boolean iniciarSesion( String nombre, String pass) {
        try {
            socket = new Socket("localhost", 5000);
            System.out.println("Conectado al servidor");
            // Streams de entrada y salida
            salida = new ObjectOutputStream(socket.getOutputStream());
            entrada = new ObjectInputStream(socket.getInputStream());

            // Envío de mensaje de inicio de sesión
            salida.writeObject(new Object[]{"inicioSesion", nombre, pass});
            Object respuesta = entrada.readObject();
            if (respuesta instanceof Object[]) {
                Object[] datosRespuesta = (Object[]) respuesta;
                if (datosRespuesta.length > 0 && datosRespuesta[0].equals("resultadoInicioSesion")) {
                    if ((boolean) datosRespuesta[1] == true){
                        return true;
                    } else {
                        cerrarConexion();
                        return false;
                    }
                }
            }
        } catch (IOException er) {
            throw new RuntimeException(er);
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }
        return false;
    }

    public boolean registrar( String nombre, String pass){
        try {
            socket = new Socket("localhost", 5000);
            System.out.println("Conectado al servidor");
            // Streams de entrada y salida
            salida = new ObjectOutputStream(socket.getOutputStream());
            entrada = new ObjectInputStream(socket.getInputStream());

            // Envío de mensaje de inicio de sesión
            salida.writeObject(new Object[]{"registro", nombre, pass});
            Object respuesta = entrada.readObject();
            if (respuesta instanceof Object[]) {
                Object[] datosRespuesta = (Object[]) respuesta;
                if (datosRespuesta.length > 0 && datosRespuesta[0].equals("resultadoRegistro")) {
                    if ((boolean) datosRespuesta[1] == true){
                        cerrarConexion();
                        return true;
                    } else {
                        cerrarConexion();
                        return false;
                    }
                }
            }
        } catch (IOException | ClassNotFoundException er) {
            throw new RuntimeException(er);
        }
        return false;
    }

    public Object solicitoMensajesPrevios() {
        try {
            salida.writeObject(new Object[]{"solicitoMensajesPrevios"});
            return entrada.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public Object mensajesGlobales() {
        try {
            return entrada.readObject();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

    public void enviarMensaje(String mensaje) {
        try {
            salida.writeObject(new Object[]{"mensaje", mensaje});
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void enviarArchivo(String path) {
        try {
            // Abre el archivo
            File file = new File(path); //se selecciona el archivo
            FileInputStream fileInputStream = new FileInputStream(file); //se crea un flujo de entrada para leer el archivo
            byte[] fileBytes = new byte[(int) file.length()]; //se crea un arreglo de bytes con el tamaño del archivo
            fileInputStream.read(fileBytes); //se lee el archivo y se guarda en el arreglo de bytes

            // Envia el nombre del archivo y su contenido al servidor
            salida.writeObject(new Object[]{"enviarArchivo", file.getName(), fileBytes});
            System.out.println("Archivo enviado");
            // Cierra el archivo
            fileInputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void cerrarConexion() throws IOException {
        System.out.println("Cerrando conexión");
        salida.writeObject(new Object[]{"exit"});
        salida.close();
        entrada.close();
        socket.close();
        System.out.println("Conexión cerrada");
    }

}
