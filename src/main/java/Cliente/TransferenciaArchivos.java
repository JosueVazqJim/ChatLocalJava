package Cliente;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

public class TransferenciaArchivos extends JFrame{
    private JPanel panel1;
    private JButton btn_selArchivo;
    private JTextArea txt_ubicaiconArchivo;
    private JButton btn_enviarArchivo;
    private JButton btn_salir;
    private JButton btn_chat;
    private JList listaArchivos;
    private DefaultListModel<String> archivosModel;

    private boolean listening = true;
    private Login login;
    private Cliente cliente;

    public TransferenciaArchivos( Cliente cliente, Login login) {
        this.cliente = cliente;
        this.login = login;

        setContentPane(panel1);
        setTitle("TRANSFERENCIA DE ARCHIVOS");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 500);
        setLocationRelativeTo(null);
        setVisible(false);

        cliente.solicitarListaArchivos();

        archivosModel = new DefaultListModel<>();
        listaArchivos.setModel(archivosModel);

        btn_selArchivo.addActionListener(new ActionListener() { // Seleccionar archivo
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
                         UnsupportedLookAndFeelException ex) {
                    throw new RuntimeException(ex);
                }
                mostrarSeleccionArchivo();
            }
        });
        btn_enviarArchivo.addActionListener(new ActionListener() { // Enviar archivo
            @Override
            public void actionPerformed(ActionEvent e) {
                if (txt_ubicaiconArchivo.getText().isEmpty() || txt_ubicaiconArchivo.getText().equals("/ubicacion/archivo/")){
                    JOptionPane.showMessageDialog(null, "Selecciona un archivo", "Error", JOptionPane.ERROR_MESSAGE);
                } else{
                    cliente.enviarArchivo(txt_ubicaiconArchivo.getText());
                }

            }
        });
        btn_chat.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                login.mostrarChat();
            }
        });
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
        listaArchivos.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) { //con un clic
                    int index = listaArchivos.locationToIndex(e.getPoint());
                    if (index >= 0) {
                        String fileName = (String) listaArchivos.getModel().getElementAt(index);
                        descargarArchivo(fileName);
                    }
                }
            }
        });
    }

    public void mostrarSeleccionArchivo(){
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) != JFileChooser.CANCEL_OPTION){
            txt_ubicaiconArchivo.setText(fileChooser.getSelectedFile().getAbsolutePath());
        }
    }

    public void cerrarSesion() throws IOException {
        login.setVisible(true); // Llama al método de Login para cerrar la conexión
        setVisible(false); // Hace invisible el Chat
        System.out.println("Sesión cerrada");
        cliente.cerrarConexion();
        this.dispose(); //libera los recursos
    }

    public void onArchivoDisponible(String archivo) { //metodo de la interfaz para mostrar mensajes
        SwingUtilities.invokeLater(() -> archivosModel.addElement(archivo));
    }

    private void descargarArchivo(String fileName) {
        cliente.descargarArchivo(fileName);
        JOptionPane.showMessageDialog(null, "Archivo descargado", "Descarga", JOptionPane.INFORMATION_MESSAGE);
    }
}
