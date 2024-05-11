package Cliente;

import java.io.IOException;

public class PruebaEnvioArchivo {

    public static void main(String[] args) throws IOException {
        Cliente cliente = new Cliente();
        cliente.enviarArchivo("D:\\DOCUMENTOS\\CursoJava\\socketClienteServidorComputo\\src\\main\\java\\Cliente\\hola.txt");
    }
}
