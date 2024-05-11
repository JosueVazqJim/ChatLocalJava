package Cliente;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Login extends JFrame{
    private JLabel label_titulo;
    private JTextField txt_usuario;
    private JLabel label_usuario;
    private JPasswordField txt_pass;
    private JButton btn_inicioSesion;
    private JButton registrarButton;
    private JPanel panel1;

    private Cliente cliente;
    private Chat chat;
    private TransferenciaArchivos transferenciaArchivos;

    public Login() {
        setContentPane(panel1);
        setTitle("CHAT GLOBAL Y FILE STORAGE");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 500);
        setLocationRelativeTo(null);
        setVisible(true);
        cliente = new Cliente();

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

    public static void main(String[] args) {
        new Login();
    }

    public void iniciarSesion(){
        boolean permiso = cliente.iniciarSesion(txt_usuario.getText(), String.valueOf(txt_pass.getPassword()));

        if (permiso){
            chat = new Chat(cliente, this);
            transferenciaArchivos = new TransferenciaArchivos(cliente, this);
            setVisible(false);
            chat.setVisible(true);
            transferenciaArchivos.setVisible(false);
        } else {
            JOptionPane.showMessageDialog(null, "Inicio de sesión fallido", "Inicio de sesión", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void registrar(){
        boolean registro = cliente.registrar(txt_usuario.getText(), String.valueOf(txt_pass.getPassword()));
        if (registro){
            JOptionPane.showMessageDialog(null, "Registro exitoso", "Registro", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(null, "Registro fallido", "Registro", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void mostrarChat(){
        chat.setVisible(true);
    }

    public void mostrarTransferenciaArchivos(){
        transferenciaArchivos.setVisible(true);
    }
}
