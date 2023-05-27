package pcliente;

public class PCliente {

    public static void main(String[] args) {
        Cliente cliente = new Cliente("localhost", 1234);
        System.out.println("ventana");
        Ventana ventana = new Ventana(cliente);
    }

}
