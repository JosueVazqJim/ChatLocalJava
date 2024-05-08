package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Prueba {public static void main(String[] args) {
    try {
        // Conexión al servidor
        Socket socket = new Socket("localhost", 5000);
        System.out.println("Conectado al servidor");

        // Streams de entrada y salida
        ObjectOutputStream salida = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream entrada = new ObjectInputStream(socket.getInputStream());

        // Envío de mensaje de inicio de sesión
        salida.writeObject(new Object[]{"inicioSesion", "josue", "123120"});
        Object respuesta = entrada.readObject();
        if (respuesta instanceof Object[]) {
            Object[] datosRespuesta = (Object[]) respuesta;
            if (datosRespuesta.length > 0 && datosRespuesta[0].equals("resultadoInicioSesion")) {
                boolean resultado = (boolean) datosRespuesta[1];
                System.out.println("Resultado del inicio de sesión: " + resultado);
            }
        }

        // Cierre de la conexión
        salida.close();
        entrada.close();
        socket.close();
    } catch (ClassNotFoundException | IOException e) {
        e.printStackTrace();
    }
}
}