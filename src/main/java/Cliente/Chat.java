package Cliente;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Chat extends JFrame implements MensajeListener{
    private JTextArea txt_mensajesGlobales;
    private JPanel panel1;
    private JTextField txt_mensajeEnviar;
    private JButton btn_enviarMensaje;
    private JButton btn_salir;

    private Socket socket; // Atributo para almacenar el socket
    private Login login;
    private ObjectOutputStream salida;
    private ObjectInputStream entrada;
    private boolean listening = true;

    public Chat( Socket socket, Login login, ObjectOutputStream salida, ObjectInputStream entrada ) {
        this.socket = socket;
        this.login = login;
        this.salida = salida;
        this.entrada = entrada;

        setContentPane(panel1);
        setTitle("Chat");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 500);
        setLocationRelativeTo(null);
        setVisible(true);

        solicitoMensajesPrevios();
        startListening();
        btn_salir.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    cerrarSesion();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        btn_enviarMensaje.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                enviarMensaje();
            }
        });
    }

    public void cerrarSesion() throws IOException {
        login.setVisible(true); // Llama al método de Login para cerrar la conexión
        setVisible(false); // Hace invisible el Chat
        login.cerrarConexion(socket); // Cierra la conexión
        listening = false; // Detiene el hilo de escucha
        System.out.println("Sesión cerrada");
        dispose(); //libera los recursos
    }

    public void solicitoMensajesPrevios() {
        try {
            salida.writeObject(new Object[]{"solicitoMensajesPrevios"});
            Object respuesta = entrada.readObject(); // Espera la respuesta del servidor
            if (respuesta instanceof Object[]) {
                Object[] datos = (Object[]) respuesta;
                if (datos.length > 0 && "mensajesPrevios".equals(datos[0])) {
                    // Procesar los mensajes previos recibidos
                    for (int i = 1; i < datos.length; i++) {
                        String mensajePrevio = (String) datos[i];
                        onMensajeRecibido(mensajePrevio);
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void enviarMensaje() {
        try {
            String mensaje = txt_mensajeEnviar.getText();
            salida.writeObject(new Object[]{"mensaje", mensaje});
            txt_mensajeEnviar.setText("");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void startListening() { //hilo para escuchar mensajes
        new Thread(() -> {
            try {
                while (listening) {
                    Object mensajeObj = entrada.readObject(); //esta a la espera de un mensaje
                    if (mensajeObj instanceof Object[]) {
                        Object[] datos = (Object[]) mensajeObj;
                        if (datos.length > 0 && "mensajeGlobal".equals(datos[0])) {
                            String mensajeGlobal = (String) datos[1];
                            onMensajeRecibido(mensajeGlobal); //llama al metodo onMensajeRecibido de la interfaz
                        }
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    public void onMensajeRecibido(String mensaje) { //metodo de la interfaz para mostrar mensajes
        SwingUtilities.invokeLater(() -> txt_mensajesGlobales.append(mensaje + "\n"));
    }
}
