package pcliente;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import static javax.swing.JOptionPane.showMessageDialog;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import static pcliente.ChatScreen.activeUsers;
import static pcliente.ChatScreen.allUsers;

public class ChatWindow extends JFrame {

    public Integer ID;
    List<String> mensajes = Collections.synchronizedList(new ArrayList<>());
    JTextField inputText = new JTextField();

    public ChatWindow() {
        super();
        setSize(500, 500);
        setVisible(true);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        getContentPane().add(vista());
        validate();
    }

    public void cambiarTitulo(String s) {
        setTitle(s);
    }

    private JPanel vista() {

        JPanel panel = new JPanel();

        Vector<String> vector = new Vector<>();
        for (String str : mensajes) {
            vector.add(str);
        }

        JList list = new JList(vector);
        JButton enviar = new JButton();

        enviar.setText("Enviar Mensaje");
        enviar.addActionListener(
                (ActionEvent e) -> {
                    try {
                        Cliente.oos.reset();
                        Cliente.oos.writeObject("enviarmensaje");
                        Cliente.oos.writeObject(ID);
                        Cliente.oos.writeObject(ChatScreen.allUsers.get(ChatScreen.ID) + ": " + inputText.getText());
                        mensajes.add("Tu: " + inputText.getText());
                        actualizar();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    inputText.setText("");
                }
        );

        GridBagLayout gridBag = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1;
        constraints.gridy = 0;
        constraints.weighty = 9;
        panel = new JPanel(gridBag);
        JPanel subPanel = new JPanel(new GridLayout(1, 2));
        subPanel.add(inputText);
        subPanel.add(enviar);
        gridBag.setConstraints(list, constraints);
        constraints.gridy = 1;
        constraints.weighty = 1;

        gridBag.setConstraints(subPanel, constraints);

        panel.add(list);
        panel.add(subPanel);

        return panel;
    }

    public void actualizar() {
        getContentPane().removeAll();
        getContentPane().add(vista());
        validate();
    }
}
