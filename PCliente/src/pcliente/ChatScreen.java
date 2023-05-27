package pcliente;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import static javax.swing.JOptionPane.showMessageDialog;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class ChatScreen extends JFrame {

    static Map<Integer, String> activeUsers = Collections.synchronizedMap(new HashMap<>());
    static Map<Integer, String> allUsers = Collections.synchronizedMap(new HashMap<>());
    static List<Integer> allClientFriends = Collections.synchronizedList(new ArrayList<>());
    static List<ChatWindow> allChats = Collections.synchronizedList(new ArrayList<>());
    static List<GroupWindow> allGroupsWindows = Collections.synchronizedList(new ArrayList<>());
    static List<String> usersSelect = Collections.synchronizedList(new ArrayList<>());
    static List<Integer> usersIdForAGroup = Collections.synchronizedList(new ArrayList<>());
    static List<Integer> allClientGroups = null;
    static List<String> allClientGroupsNames = null;
    static List<Integer> mensajesGrupoIDGrupo = Collections.synchronizedList(new ArrayList<>());
    static List<String> mensajesGrupoMensaje = Collections.synchronizedList(new ArrayList<>());
    String nuevoAmigo = null;
    Integer amigoSeleccionado = null;
    static Integer ID;
    JPanel principalPanel;
    String selected = "";

    public ChatScreen(Socket s, Integer id) {
        super();
        System.out.println("Iniciando chat Screen");
        ID = id;
        Thread hilo = new RecibirDatos(s, this);
        hilo.start();
        setTitle("Chat");
        setSize(500, 500);
        setVisible(true);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        principalPanel = setScreenContent();
        this.getContentPane().add(principalPanel);
        validate();
    }

    private JPanel setScreenContent() {
        JPanel panel;
        panel = usuariosConectados();
        return panel;
    }

    private JPanel usuariosConectados() {
        selected = "usuariosConectados";
        setTitle("Usuarios conectados");
        JPanel panel;
        Vector<String> vector = new Vector<>();
        for (Map.Entry<Integer, String> entry : activeUsers.entrySet()) {
            vector.add(entry.getValue());
        }
        JList list = new JList(vector);
        list.setSize(new Dimension(500, 500));

        list.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (list.getSelectedValue() != null) {
                    if (list.getSelectedValue().equals(allUsers.get(ID))) {
                        showMessageDialog(null, "No puedes seleccionarte a ti");
                        changePanel(usuariosConectados());
                    } else {
                        nuevoAmigo = (String) list.getSelectedValue();
                    }

                }
            }
        });
        JButton añadir = new JButton();

        añadir.setText("Añadir amigo");
        añadir.addActionListener(
                (ActionEvent e) -> {
                    if (nuevoAmigo != null) {
                        añadirAmigo(nuevoAmigo);
                        showMessageDialog(null, "Invitacion enviada a " + nuevoAmigo);
                    } else {
                        showMessageDialog(null, "Debes seleccionar un usuario");
                    }
                }
        );

        GridBagLayout gridBag = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1;
        constraints.gridy = 0;
        constraints.weighty = 1;
        panel = new JPanel(gridBag);

        gridBag.setConstraints(menu(), constraints);
        constraints.gridy = 1;
        constraints.weighty = 8;

        gridBag.setConstraints(list, constraints);
        constraints.gridy = 2;
        constraints.weighty = 1;

        gridBag.setConstraints(añadir, constraints);

        panel.add(menu());
        panel.add(list);

        panel.add(añadir);
        return panel;
    }

    private JPanel todosLosUsuarios() {
        selected = "todosLosUsuarios";
        JPanel panel;
        Vector<String> vector = new Vector<>();
        for (Map.Entry<Integer, String> entry : allUsers.entrySet()) {
            String aux = "Usuario: " + entry.getValue() + " Estatus: ";
            if (activeUsers.containsValue(entry.getValue())) {
                aux += "Conectado";
            } else {
                aux += "Desconectado";
            }
            vector.add(aux);
        }
        JList list = new JList(vector);
        list.setSize(new Dimension(500, 500));
        list.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (list.getSelectedValue() != null) {
                    if (list.getSelectedValue().equals("Usuario: " + allUsers.get(ID) + " Estatus: Conectado")) {
                        showMessageDialog(null, "No puedes seleccionarte a ti");
                        changePanel(todosLosUsuarios());
                    } else {
                        String valor = (String) list.getSelectedValue();
                        valor = valor.substring(9, valor.indexOf(" Estatus:"));
                        nuevoAmigo = valor;
                    }

                }
            }
        });
        JButton añadir = new JButton();
        añadir.setText("Añadir amigo");
        añadir.addActionListener(
                (ActionEvent e) -> {
                    if (nuevoAmigo != null) {
                        añadirAmigo(nuevoAmigo);
                        showMessageDialog(null, "Invitacion enviada a " + nuevoAmigo);
                    } else {
                        showMessageDialog(null, "Debes seleccionar un usuario");
                    }
                }
        );

        GridBagLayout gridBag = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1;
        constraints.gridy = 0;
        constraints.weighty = 1;
        panel = new JPanel(gridBag);
        gridBag.setConstraints(menu(), constraints);
        constraints.gridy = 1;
        constraints.weighty = 8;
        gridBag.setConstraints(list, constraints);
        constraints.gridy = 2;
        constraints.weighty = 1;
        gridBag.setConstraints(añadir, constraints);
        panel.add(menu());
        panel.add(list);
        panel.add(añadir);
        return panel;
    }

    private JPanel amigosYGrupos() {
        selected = "amigosYGrupos";
        setTitle("Amigos y grupos");
        JPanel panel;
        Vector<String> vector = new Vector<>();
        for (Integer friendId : allClientFriends) {
            String aux = "Usuario: " + allUsers.get(friendId) + " Estatus: ";
            if (activeUsers.containsKey(friendId)) {
                aux += "Conectado";
            } else {
                aux += "Desconectado";
            }
            System.out.println("Amigo " + friendId);
            vector.add(aux);
        }
        JList list = new JList(vector);
        list.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (list.getSelectedValue() != null) {
                    amigoSeleccionado = allClientFriends.get(list.getSelectedIndex());
                }
                if (list.getSelectedValuesList().size() >= 2) {
                    usersSelect = list.getSelectedValuesList();
                } else {
                    usersSelect = null;
                }
            }
        });
        JButton enviarMensaje = new JButton();
        enviarMensaje.setText("Abrir Chat");
        enviarMensaje.addActionListener(
                (ActionEvent e) -> {
                    if (amigoSeleccionado != null) {
                        ChatWindow chat = new ChatWindow();
                        chat.ID = amigoSeleccionado;
                        allChats.add(chat);
                        chat.setTitle("Chat " + allUsers.get(amigoSeleccionado));
                    } else {
                        showMessageDialog(null, "Debes seleccionar un usuario");
                    }
                }
        );

        JButton crearGrupoBtn = new JButton();
        crearGrupoBtn.setText("Crear Grupo");
        crearGrupoBtn.addActionListener((ActionEvent e) -> {
            if (usersSelect != null) {
                String aux = "";
                usersIdForAGroup.clear();
                for (String str : usersSelect) {
                    str = str.substring(9, str.indexOf(" Estatus:"));
                    for (Map.Entry<Integer, String> entry : allUsers.entrySet()) {
                        if (entry.getValue().equals(str)) {
                            usersIdForAGroup.add(entry.getKey());
                        }
                    }
                    aux += " " + str + ",";
                }
                aux = aux.substring(0, aux.length() - 1);
                aux += "?";
                String nombreDelGrupo = JOptionPane.showInputDialog("Ingresa el nombre del grupo");
                if (!nombreDelGrupo.equals("") && !nombreDelGrupo.equals(null)) {
                    System.out.println(nombreDelGrupo);
                    int result = JOptionPane.showConfirmDialog(this, aux, "¿Quieres invitar a un nuevo grupo a:",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE);
                    if (result == JOptionPane.YES_OPTION) {
                        try {
                            Cliente.oos.reset();
                            Cliente.oos.writeObject("creargrupo");
                            Cliente.oos.writeObject(nombreDelGrupo);
                            Cliente.oos.writeObject(usersIdForAGroup);
                            Cliente.oos.flush();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }

            } else {
                showMessageDialog(null, "Debes seleccionar minimo 2 usuarios");
            }
        }
        );

        Vector<String> vector2 = new Vector<>();
        for (String str : allClientGroupsNames) {
            vector2.add("Grupo: " + str);
        }

        JList list2 = new JList(vector2);

        list2.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (list2.getSelectedValue() != null) {
                    boolean control = true;
                    for (GroupWindow g : allGroupsWindows) {
                        if (g.ID == allClientGroups.get(list2.getSelectedIndex())) {
                            control = false;
                        }
                    }
                    if (control) {
                        GroupWindow chat = new GroupWindow();
                        chat.ID = allClientGroups.get(list2.getSelectedIndex());
                        chat.setTitle("Grupo " + list2.getSelectedValue() + " " + chat.ID);
                        allGroupsWindows.add(chat);
                        chat.cargarMensajes();
                    }
                }
            }
        });

        JPanel subPanel = new JPanel(new GridLayout(1, 2));

        subPanel.add(list);

        subPanel.add(list2);

        JPanel subPanel2 = new JPanel(new GridLayout(1, 2));

        subPanel2.add(enviarMensaje);

        subPanel2.add(crearGrupoBtn);

        GridBagLayout gridBag = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weighty = 2;
        panel = new JPanel(gridBag);

        gridBag.setConstraints(menu(), constraints);
        constraints.gridy = 1;
        constraints.weighty = 8;

        gridBag.setConstraints(subPanel, constraints);
        constraints.gridy = 2;
        constraints.weighty = 1;

        gridBag.setConstraints(subPanel2, constraints);

        panel.add(menu());
        panel.add(subPanel);

        panel.add(subPanel2);
        return panel;
    }

    private JPanel menu() {
        JPanel panel;
        JButton btnUsuariosConectados = new JButton();
        btnUsuariosConectados.setText("Usuarios conectados");
        btnUsuariosConectados.addActionListener((ActionEvent e) -> {
            changePanel(usuariosConectados());
        });
        JButton btnTodosLosUsuarios = new JButton();
        btnTodosLosUsuarios.setText("Todos los usuarios");
        btnTodosLosUsuarios.addActionListener((ActionEvent e) -> {
            changePanel(todosLosUsuarios());
        });
        JButton btnAmigosYGrupos = new JButton();
        btnAmigosYGrupos.setText("Amigos y grupos");
        btnAmigosYGrupos.addActionListener((ActionEvent e) -> {
            changePanel(amigosYGrupos());
        });
        panel = new JPanel(new GridLayout(1, 3));

        panel.add(btnUsuariosConectados);
        panel.add(btnTodosLosUsuarios);
        panel.add(btnAmigosYGrupos);
        return panel;
    }

    private void changePanel(JPanel panel) {
        getContentPane().removeAll();
        getContentPane().add(panel);
        validate();
    }

    public void updateActiveUsers() {
        if (selected.equals("usuariosConectados")) {
            changePanel(usuariosConectados());
        }

    }

    void updateUsers() {
        if (selected.equals("todosLosUsuarios")) {
            changePanel(todosLosUsuarios());
        }
    }

    private void añadirAmigo(String nombre) {
        try {
            Cliente.oos.writeObject("añadiramigo");
            Cliente.oos.writeObject(ID);
            Integer idAux = null;
            for (Map.Entry<Integer, String> entry : allUsers.entrySet()) {
                if (entry.getValue().equals(nombre)) {
                    idAux = entry.getKey();
                }
            }
            Cliente.oos.writeObject(idAux);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    void mostrarSolicitud(Integer remitente) {
        int result = JOptionPane.showConfirmDialog(this, "Aceptar solicitud de amistad de: " + allUsers.get(remitente), "Solicitud de amistad",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (result == JOptionPane.YES_OPTION) {
            try {
                Cliente.oos.reset();
                Cliente.oos.writeObject("respuestadesolicitudaceptada");
                Cliente.oos.writeObject(ID);
                Cliente.oos.writeObject(remitente);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (result == JOptionPane.NO_OPTION) {
            try {
                Cliente.oos.reset();
                Cliente.oos.writeObject("respuestadesolicitudrechazada");
                Cliente.oos.writeObject(ID);
                Cliente.oos.writeObject(remitente);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("None selected");
        }
    }

    void updateFriendScreen() {
        if (selected.equals("amigosYGrupos")) {
            changePanel(amigosYGrupos());
        }
    }

    void enviarSolicitudGrupo(String nombreDelGrupo, Integer idSolicitud) {
        int result = JOptionPane.showConfirmDialog(this, "Quieres unirte al grupo: " + nombreDelGrupo, "Invitacion a grupo",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (result == JOptionPane.YES_OPTION) {
            try {
                Cliente.oos.reset();
                Cliente.oos.writeObject("grupoaceptado");
                Cliente.oos.writeObject(idSolicitud);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (result == JOptionPane.NO_OPTION) {
            try {
                Cliente.oos.reset();
                Cliente.oos.writeObject("gruporechazado");
                Cliente.oos.writeObject(idSolicitud);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("None selected");
        }
    }

}
