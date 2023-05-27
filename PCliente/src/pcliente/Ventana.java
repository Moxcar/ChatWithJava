package pcliente;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import static javax.swing.JOptionPane.showMessageDialog;

public class Ventana extends JFrame {

    JPanel panelLogin, panelButtons, principalPanel;
    Socket cs;
    int intentosDeLogin = 0;

    public Ventana(Cliente cliente) {
        super();
        cs = cliente.cs;
        setTitle("Ventana Cliente");
        setSize(500, 500);
        setVisible(true);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        principalPanel = setAuthenticationScreen();
        this.getContentPane().add(principalPanel);
        validate();
    }

    private JPanel setAuthenticationScreen() {
        JPanel panel;
        panel = login();
        return panel;
    }

    private JPanel login() {
        JPanel panel;

        JTextField userTextField = new JTextField();
        userTextField.setText("Introduzca el usuario");

        JTextField passTextField = new JTextField();
        passTextField.setText("Introduzca la contraseña");

        JButton loginButton = new JButton();
        loginButton.setText("Aceptar");
        loginButton.addActionListener((ActionEvent e) -> {
            checkLogin(userTextField.getText(), passTextField.getText());
        });

        panel = new JPanel(new GridLayout(4, 1));
        panel.add(buttons(), BorderLayout.CENTER);
        panel.add(userTextField);
        panel.add(passTextField);
        panel.add(loginButton);
        return panel;
    }

    private JPanel register() {
        JPanel panel;

        JTextField userTextField = new JTextField();
        userTextField.setText("Introduzca el usuario");

        JTextField passTextField = new JTextField();
        passTextField.setText("Introduzca la contraseña");

        JButton registerButton = new JButton();
        registerButton.setText("Aceptar");
        registerButton.addActionListener((ActionEvent e) -> {
            if (registerUser(userTextField.getText(), passTextField.getText())) {
                checkLogin(userTextField.getText(), passTextField.getText());
            }
        });

        JButton recoverPass = new JButton();
        recoverPass.setText("Recuperar contraseña");
        recoverPass.addActionListener((ActionEvent e) -> {
            changePanel(recoverPassPanel());
        });
        panel = new JPanel(new GridLayout(5, 1));
        panel.add(buttons(), BorderLayout.CENTER);
        panel.add(userTextField);
        panel.add(passTextField);
        panel.add(registerButton);
        panel.add(recoverPass);
        return panel;
    }

    private JPanel recoverPassPanel() {
        JPanel panel;
        JTextField userTextField = new JTextField();
        userTextField.setText("Introduzca el usuario");
        JButton recoverPass = new JButton();
        recoverPass.setText("Aceptar");
        recoverPass.addActionListener((ActionEvent e) -> {
            getPass(userTextField.getText());
        });
        panel = new JPanel(new GridLayout(3, 1));
        panel.add(buttons(), BorderLayout.CENTER);
        panel.add(userTextField);
        panel.add(recoverPass);
        return panel;
    }

    private JPanel buttons() {
        JPanel panel;
        JButton loginButton = new JButton();
        loginButton.setText("login");
        loginButton.addActionListener((ActionEvent e) -> {
            changePanel(login());
        });
        JButton registerButton = new JButton();
        registerButton.setText("register");
        registerButton.addActionListener((ActionEvent e) -> {
            changePanel(register());
        });

        panel = new JPanel(new GridLayout(1, 3));

        panel.add(loginButton);

        panel.add(registerButton);
        return panel;
    }

    private void checkLogin(String nombre, String contraseña) {
        try {
            List<String> data = Collections.synchronizedList(new ArrayList<>());
            data.add(nombre);
            data.add(contraseña);
            System.out.println("Enviando login");
            Cliente.oos.writeObject("login");
            Cliente.oos.writeObject(data);
            Cliente.oos.flush();
            System.out.println("recibiendo login");
            Integer id = (Integer) Cliente.ois.readObject();
            System.out.println("ID de usuario: " + id);
            if (id != -1) {
                new ChatScreen(cs, id);
                setVisible(false);
                return;
            }
            showMessageDialog(null, "Datos incorrectos");
            intentosDeLogin++;
            if (intentosDeLogin == 3) {
                changePanel(register());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void changePanel(JPanel panel) {
        getContentPane().removeAll();
        getContentPane().add(panel);
        validate();
    }

    private boolean registerUser(String nombre, String contraseña) {
        try {
            List<String> data = Collections.synchronizedList(new ArrayList<>());
            data.add(nombre);
            data.add(contraseña);
            System.out.println("Enviando registro");
            Cliente.oos.writeObject("registro");
            Cliente.oos.writeObject(data);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void getPass(String nombre) {
        try {
            List<String> data = Collections.synchronizedList(new ArrayList<>());
            data.add(nombre);
            Cliente.oos.writeObject("recuperar");
            Cliente.oos.writeObject(data);
            Cliente.oos.flush();
            String pass = (String) Cliente.ois.readObject();
            showMessageDialog(null, "La contraseña del usuario: " + nombre + " es: " + pass);
        } catch (Exception e) {
        }
    }
}
