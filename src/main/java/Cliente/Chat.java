package Cliente;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class Chat extends JFrame implements MensajeListener{
    private JTextArea txt_mensajesGlobales;
    private JPanel panel1;
    private JTextField txt_mensajeEnviar;
    private JButton btn_enviarMensaje;
    private JButton btn_salir;
    private JButton transferenciaDeArchivosButton;

    private Login login;
    private boolean listening = true;

    Cliente cliente;

    public Chat( Cliente cliente, Login login) {
        this.login = login;
        this.cliente = cliente;

        setContentPane(panel1);
        setTitle("CHAT");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 500);
        setLocationRelativeTo(null);
        setVisible(true);

        solicitoMensajesPrevios();
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
        transferenciaDeArchivosButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                login.mostrarTransferenciaArchivos();
            }
        });
    }

    public void cerrarSesion() throws IOException {
        login.setVisible(true); // Llama al método de Login para cerrar la conexión
        setVisible(false); // Hace invisible el Chat
        listening = false; // Detiene el hilo de escucha
        System.out.println("Sesión cerrada");
        cliente.cerrarConexion();
        this.dispose(); //libera los recursos
    }

    public void solicitoMensajesPrevios() {
        Object mensajesPrevios = cliente.solicitoMensajesPrevios();
        if (mensajesPrevios instanceof Object[]) {
            Object[] datos = (Object[]) mensajesPrevios;
            if (datos.length > 0 && "mensajesPrevios".equals(datos[0])) {
                // Procesar los mensajes previos recibidos
                for (int i = 1; i < datos.length; i++) {
                    String mensajePrevio = (String) datos[i];
                    onMensajeRecibido(mensajePrevio);
                }
            }
        }
    }

    public void enviarMensaje() {
        cliente.enviarMensaje(txt_mensajeEnviar.getText());
    }

    @Override
    public void onMensajeRecibido(String mensaje) { //metodo de la interfaz para mostrar mensajes
        SwingUtilities.invokeLater(() -> txt_mensajesGlobales.append(mensaje + "\n"));
    }
}
