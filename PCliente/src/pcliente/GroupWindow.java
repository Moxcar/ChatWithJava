package pcliente;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.PopupMenu;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import static javax.swing.WindowConstants.HIDE_ON_CLOSE;

public class GroupWindow extends JFrame {
    
    List<String> mensajes = Collections.synchronizedList(new ArrayList<>());
    JTextField inputText = new JTextField();
    Integer ID;
    
    public GroupWindow() {
        super();
        setSize(500, 500);
        setVisible(true);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        getContentPane().add(vista());
        validate();
    }
    
    private JPanel vista() {
        JPanel panel = new JPanel();
        
        Vector<String> vector = new Vector<>();
        for (String str : mensajes) {
            vector.add(str);
        }
        
        JList list = new JList(vector);
        JButton enviar = new JButton();
        
        enviar.setText("Enviar");
        enviar.addActionListener(
                (ActionEvent e) -> {
                    try {
                        Cliente.oos.reset();
                        Cliente.oos.writeObject("enviarmensajegrupo");
                        Cliente.oos.writeObject(ID);
                        Cliente.oos.writeObject(ChatScreen.allUsers.get(ChatScreen.ID) + ": " + inputText.getText());
                        mensajes.add(ChatScreen.allUsers.get(ChatScreen.ID) + ": " + inputText.getText());
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
    
    public void cargarMensajes() {
        for (int i = 0; i < ChatScreen.mensajesGrupoIDGrupo.size(); i++) {
            System.out.println(ChatScreen.mensajesGrupoIDGrupo.get(i) + " - " + ID);
            if (ChatScreen.mensajesGrupoIDGrupo.get(i).equals(ID)) {
                System.out.println("ENTRO");
                mensajes.add(ChatScreen.mensajesGrupoMensaje.get(i));
            }
        }
        actualizar();
    }
    
    
}
