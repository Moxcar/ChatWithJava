package pserver;

import java.awt.GridLayout;
import java.awt.PopupMenu;
import java.awt.TextArea;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Vector;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class Servidor extends JFrame {
    
    static List<HiloParaCliente> allClients = Collections.synchronizedList(new ArrayList<>());
    static Map<Integer, String> activeUsers = Collections.synchronizedMap(new HashMap<>());
    static Map<Integer, String> allUsers = Collections.synchronizedMap(new HashMap<>());
    static Map<Integer, String> allGroups = Collections.synchronizedMap(new HashMap<>());
    static List<Integer> allGroupsMembers1 = Collections.synchronizedList(new ArrayList<>());
    static List<Integer> allGroupsMembers2 = Collections.synchronizedList(new ArrayList<>());
    static Map<Integer, Integer> allGroupCreators = Collections.synchronizedMap(new HashMap<>());
    static List<Integer> solicitudesRemitentes = Collections.synchronizedList(new ArrayList<>());
    static List<Integer> solicitudesDestinatarios = Collections.synchronizedList(new ArrayList<>());
    static List<Integer> solicitudesGruposID = Collections.synchronizedList(new ArrayList<>());
    static List<Integer> solicitudesGruposIDGrupo = Collections.synchronizedList(new ArrayList<>());
    static List<Integer> solicitudesGruposIDDestinatario = Collections.synchronizedList(new ArrayList<>());
    static List<Integer> mensajesGrupoIDGrupo = Collections.synchronizedList(new ArrayList<>());
    static List<String> mensajesGrupoMensaje = Collections.synchronizedList(new ArrayList<>());
    
static TextArea log = new TextArea();
    ServerSocket ss;
    Socket cs;
    
    public Servidor(int puerto) {
        try {
            ss = new ServerSocket(puerto);
        } catch (Exception e) {
            log.append(e.getMessage());
        }
        cs = null;
        iniciarVentana();
        iniciarServidor();
        
    }
    
    private void iniciarServidor() {
        log.append("Servidor Iniciado\n");
        try {
            while (true) {
                cs = ss.accept();
                log.append("Cliente  conectado\n");
                HiloParaCliente hiloParaCliente = new HiloParaCliente(cs);
                allClients.add(hiloParaCliente);
                hiloParaCliente.start();
                
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
    
    private void iniciarVentana() {
        setSize(500, 500);
        setVisible(true);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        log.setEditable(false);
        add(new JScrollPane(log));
        validate();
    }
    
}
