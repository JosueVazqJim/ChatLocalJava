package Cliente;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Login extends JFrame{
    private JLabel label_titulo;
    private JTextField txt_usuario;
    private JLabel label_usuario;
    private JPasswordField txt_pass;
    private JButton btn_inicioSesion;
    private JButton registrarButton;
    private JPanel panel1;

    private Socket socket;
    private ObjectOutputStream salida;
    private ObjectInputStream entrada;

    public Login() throws IOException {
        setContentPane(panel1);
        setTitle("CHAT GLOBAL Y FILE STORAGE");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 500);
        setLocationRelativeTo(null);
        setVisible(true);

        btn_inicioSesion.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                iniciarSesion();
            }
        });
        registrarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                registrar();
            }
        });
    }

    public static void main(String[] args) throws IOException {
        new Login();
    }

    public void iniciarSesion(){
        try {
            socket = new Socket("localhost", 5000);
            System.out.println("Conectado al servidor");
            // Streams de entrada y salida
            salida = new ObjectOutputStream(socket.getOutputStream());
            entrada = new ObjectInputStream(socket.getInputStream());

            String nombre = txt_usuario.getText();
            String pass = String.valueOf(txt_pass.getPassword());

            // Envío de mensaje de inicio de sesión
            salida.writeObject(new Object[]{"inicioSesion", nombre, pass});
            Object respuesta = entrada.readObject();
            if (respuesta instanceof Object[]) {
                Object[] datosRespuesta = (Object[]) respuesta;
                if (datosRespuesta.length > 0 && datosRespuesta[0].equals("resultadoInicioSesion")) {
                    if ((boolean) datosRespuesta[1] == true){
                        JOptionPane.showMessageDialog(null, "Inicio de sesión exitoso", "Inicio de sesión", JOptionPane.INFORMATION_MESSAGE);
                        Chat chat = new Chat(socket, this, salida, entrada);
                        setVisible(false);
                        chat.setVisible(true);
                    } else {
                        JOptionPane.showMessageDialog(null, "Inicio de sesión fallido", "Inicio de sesión", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        } catch (IOException er) {
            throw new RuntimeException(er);
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void registrar(){
        try {
            socket = new Socket("localhost", 5000);
            System.out.println("Conectado al servidor");
            // Streams de entrada y salida
            salida = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream entrada = new ObjectInputStream(socket.getInputStream());

            String nombre = txt_usuario.getText();
            String pass = String.valueOf(txt_pass.getPassword());

            // Envío de mensaje de inicio de sesión
            salida.writeObject(new Object[]{"registro", nombre, pass});
            Object respuesta = entrada.readObject();
            if (respuesta instanceof Object[]) {
                Object[] datosRespuesta = (Object[]) respuesta;
                if (datosRespuesta.length > 0 && datosRespuesta[0].equals("resultadoRegistro")) {
                    if ((boolean) datosRespuesta[1] == true){
                        JOptionPane.showMessageDialog(null, "Registro exitoso", "Registro", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(null, "Registro fallido", "Registro", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }

        } catch (IOException | ClassNotFoundException er) {
            throw new RuntimeException(er);
        }
    }
    public void cerrarConexion(Socket socket) throws IOException {
        System.out.println("Cerrando conexión");
        salida.writeObject(new Object[]{"exit"});
        socket.close();
        salida.close();
        entrada.close();
        System.out.println("Conexión cerrada");
    }

    public void prueba() throws IOException {
        salida.writeObject(new Object[]{"mensaje", "hola"});
    }
}
