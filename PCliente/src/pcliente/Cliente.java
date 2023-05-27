package pcliente;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Cliente {

    Socket cs;
    static ObjectOutputStream oos;
    static ObjectInputStream ois;
    BufferedReader in;
    PrintWriter out;
    final Scanner sc = new Scanner(System.in);

    public Cliente(String host, int puerto) {
        try {
            cs = new Socket(host, puerto);
            System.out.println("Conectado al server");
            oos = new ObjectOutputStream(cs.getOutputStream());
            ois = new ObjectInputStream(cs.getInputStream());
            System.out.println("Conectado al server");
        } catch (Exception e) {
            System.out.println("No se encontro el servidor");
            System.exit(1);
        }
    }
}
