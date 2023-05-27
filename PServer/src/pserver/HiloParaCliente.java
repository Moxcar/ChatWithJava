package pserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

public class HiloParaCliente extends Thread {

    static List<Integer> allClientFriends = Collections.synchronizedList(new ArrayList<>());
    Socket cs;
    BufferedReader in;
    PrintWriter out;
    Scanner scanner = new Scanner(System.in);
    Conexion bd;
    ObjectOutputStream oos;
    ObjectInputStream ois;
    Integer ID;

    public HiloParaCliente(Socket s) {
        bd = new Conexion();
        cs = s;
        try {
            ois = new ObjectInputStream(cs.getInputStream());
            oos = new ObjectOutputStream(cs.getOutputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void run() {
        try {
            while (true) {
                String type = (String) ois.readObject();
                List<String> data = (List<String>) ois.readObject();
                switch (type) {
                    case "registro":
                        bd.registrarUsuario(data.get(0), data.get(1));
                        break;
                    case "login": {
                        Integer id = bd.iniciarSesion(data.get(0), data.get(1));
                        oos.writeObject(id);
                        oos.flush();
                        if (id != -1) {
                            ID = id;
                            Servidor.activeUsers.put(id, data.get(0));
                            chatCliente();
                            return;
                        }
                        Servidor.log.append("Error con el login\n");
                        break;
                    }
                    case "recuperar": {
                        String pass = bd.recuperarContraseña(data.get(0));
                        oos.writeObject(pass);
                        oos.flush();
                        break;
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void chatCliente() {
        Servidor.log.append("Sesion iniciada del cliente: " + ID + "\n");
        actualizarUsuarios();
        actualizarUsuariosActivos();
        enviarSolicitudesAUsusariosConectados();
        enviarSolicitudesDeGrupoAUsuariosConectados();
        actualizarAmigos();
        actualizarGrupos();
        actualizarMensajes();
        try {
            while (true) {
                String type = (String) ois.readObject();
                switch (type) {
                    case "añadiramigo": {
                        Integer id1 = (Integer) ois.readObject();
                        Integer id2 = (Integer) ois.readObject();
                        bd.enviarSolicitudDeAmistad(id1, id2);
                        enviarSolicitudesAUsusariosConectados();
                        break;
                    }
                    case "respuestadesolicitudrechazada": {
                        Integer id1 = (Integer) ois.readObject();
                        Integer id2 = (Integer) ois.readObject();
                        bd.borrarSolicitudDeAmistad(id1, id2);
                        break;
                    }
                    case "respuestadesolicitudaceptada": {
                        Integer id1 = (Integer) ois.readObject();
                        Integer id2 = (Integer) ois.readObject();
                        bd.crearAmigos(id1, id2);
                        bd.borrarSolicitudDeAmistad(id1, id2);
                        break;
                    }
                    case "enviarmensaje": {
                        Integer id = (Integer) ois.readObject();
                        String mensaje = (String) ois.readObject();
                        for (HiloParaCliente cliente : Servidor.allClients) {
                            if (Objects.equals(cliente.ID, id)) {
                                cliente.oos.reset();
                                cliente.oos.writeObject("recibirmensaje");
                                cliente.oos.writeObject(ID);
                                cliente.oos.writeObject(mensaje);
                                cliente.oos.flush();
                            }
                        }
                        break;
                    }
                    case "enviarmensajegrupo": {
                        Integer idGrupo = (Integer) ois.readObject();
                        String mensaje = (String) ois.readObject();
                        bd.guardarMensajeGrupo(idGrupo, mensaje);
                        for (HiloParaCliente cliente : Servidor.allClients) {
                            if (clientePerteneceAGrupo(idGrupo, cliente.ID) && !Objects.equals(cliente.ID, this.ID)) {
                                cliente.oos.reset();
                                cliente.oos.writeObject("recibirmensajegrupo");
                                cliente.oos.writeObject(idGrupo);
                                cliente.oos.writeObject(mensaje);
                                cliente.oos.writeObject(Servidor.allGroups.get(idGrupo));
                                cliente.oos.flush();
                            }
                        }
                        break;
                    }
                    case "creargrupo": {
                        String nombreDelGrupo = (String) ois.readObject();
                        List<Integer> usersIdForAGroup = (List<Integer>) ois.readObject();
                        Integer idDelGrupo = bd.crearGrupo(ID, nombreDelGrupo);
                        for (Integer idDelDestinatario : usersIdForAGroup) {
                            Integer idSolicitud = bd.enviarSolicitudDeGrupo(idDelGrupo, idDelDestinatario);
                            enviarSolicitudParaUnirseAGrupo(nombreDelGrupo, idDelDestinatario, idSolicitud);
                        }
                        break;
                    }
                    case "grupoaceptado": {
                        Integer idSolicitud = (Integer) ois.readObject();
                        bd.agregarAGrupo(idSolicitud);
                        bd.borrarSolicitudDeGrupo(idSolicitud);
                        actualizarGrupos();
                        break;
                    }
                    case "gruporechazado": {
                        Integer idSolicitud = (Integer) ois.readObject();
                        bd.borrarSolicitudDeGrupo(idSolicitud);
                        break;
                    }
                }

            }
        } catch (SocketException e) {
            Servidor.activeUsers.remove(ID);
            Servidor.allClients.remove(this);
            actualizarUsuariosActivos();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void enviarSolicitudParaUnirseAGrupo(String nombreDelGrupo, Integer idDelDestinatario, Integer idSolicitud) {
        for (HiloParaCliente cliente : Servidor.allClients) {
            try {
                if (cliente.ID.equals(idDelDestinatario) && cliente.cs.isConnected()) {
                    Servidor.log.append("Enviando solicitud para unirse a un grupo\n");
                    cliente.oos.reset();
                    cliente.oos.writeObject("solicitudgrupo");
                    cliente.oos.writeObject(nombreDelGrupo);
                    cliente.oos.writeObject(idSolicitud);
                    cliente.oos.flush();
                }
            } catch (Exception e) {
            }
        }
    }

    public void enviarSolicitudesAUsusariosConectados() {
        bd.actualizarSolicitudesDeAmistad();
        for (HiloParaCliente cliente : Servidor.allClients) {
            try {
                if (cliente.cs.isConnected() && Servidor.solicitudesDestinatarios.indexOf(cliente.ID) != -1) {
                    for (int i = 0; i < Servidor.solicitudesDestinatarios.size(); i++) {
                        if (Servidor.solicitudesDestinatarios.get(i).equals(cliente.ID)) {
                            Servidor.log.append("Enviando solicitud de amistad\n");
                            cliente.oos.reset();
                            String type = "solicitud";
                            cliente.oos.writeObject(type);
                            cliente.oos.writeObject(Servidor.solicitudesRemitentes.get(i));
                            cliente.oos.flush();
                            Servidor.solicitudesDestinatarios.remove(i);
                            Servidor.solicitudesRemitentes.remove(i);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void enviarSolicitudesDeGrupoAUsuariosConectados() {
        bd.actualizarSolicitudesDeGrupos();
        for (HiloParaCliente cliente : Servidor.allClients) {
            try {
                if (cliente.cs.isConnected() && Servidor.solicitudesGruposIDDestinatario.indexOf(cliente.ID) != -1) {
                    for (int i = 0; i < Servidor.solicitudesGruposIDDestinatario.size(); i++) {
                        if (Servidor.solicitudesGruposIDDestinatario.get(i).equals(cliente.ID)) {
                            enviarSolicitudParaUnirseAGrupo(Servidor.allGroups.get(Servidor.solicitudesGruposIDGrupo.get(i)), Servidor.solicitudesGruposIDDestinatario.get(i), Servidor.solicitudesGruposID.get(i));
                            Servidor.solicitudesGruposID.remove(i);
                            Servidor.solicitudesGruposIDGrupo.remove(i);
                            Servidor.solicitudesGruposIDDestinatario.remove(i);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void actualizarUsuariosActivos() {
        for (HiloParaCliente cliente : Servidor.allClients) {
            try {
                if (cliente.cs.isConnected()) {
                    cliente.oos.reset();
                    String type = "activos";
                    cliente.oos.writeObject(type);
                    cliente.oos.writeObject(Servidor.activeUsers);
                    cliente.oos.flush();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void actualizarUsuarios() {
        Servidor.allUsers = bd.obtenerUsuarios();
        for (HiloParaCliente cliente : Servidor.allClients) {
            try {
                if (cliente.cs.isConnected()) {
                    cliente.oos.reset();
                    String type = "todoslosusuarios";
                    cliente.oos.writeObject(type);
                    cliente.oos.writeObject(Servidor.allUsers);
                    cliente.oos.flush();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void actualizarAmigos() {
        allClientFriends = bd.obtenerAmigos(ID);
        try {
            oos.reset();
            String type = "listaamigos";
            oos.writeObject(type);
            oos.writeObject(allClientFriends);
            oos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void actualizarGrupos() {
        try {
            List<Integer> allClientGroups = Collections.synchronizedList(new ArrayList<>());
            List<String> allClientGroupsNames = Collections.synchronizedList(new ArrayList<>());
            for (Map.Entry<Integer, Integer> entry : Servidor.allGroupCreators.entrySet()) {
                if (entry.getValue().equals(ID)) {
                    allClientGroups.add(entry.getKey());
                    allClientGroupsNames.add(Servidor.allGroups.get(entry.getKey()));
                }
            }
            for (int i = 0; i < Servidor.allGroupsMembers2.size(); i++) {
                if (Servidor.allGroupsMembers2.get(i).equals(ID)) {
                    allClientGroups.add(Servidor.allGroupsMembers1.get(i));
                    allClientGroupsNames.add(Servidor.allGroups.get(Servidor.allGroupsMembers1.get(i)));
                }
            }
            oos.reset();
            oos.writeObject("listagrupos");
            oos.writeObject(allClientGroups);
            oos.writeObject(allClientGroupsNames);
            oos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean clientePerteneceAGrupo(Integer idGrupo, Integer ID) {
        try {
            for (Map.Entry<Integer, Integer> entry : Servidor.allGroupCreators.entrySet()) {
                if (entry.getValue().equals(ID)) {
                    if (entry.getKey().equals(idGrupo)) {
                        return true;
                    }
                }
            }
            for (int i = 0; i < Servidor.allGroupsMembers2.size(); i++) {
                if (Servidor.allGroupsMembers2.get(i).equals(ID)) {
                    if (Servidor.allGroupsMembers1.get(i).equals(idGrupo)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void actualizarMensajes() {
        bd.obtenerMensajes();
        try {
            oos.reset();
            oos.writeObject("mensajes");
            oos.writeObject(Servidor.mensajesGrupoIDGrupo);
            oos.writeObject(Servidor.mensajesGrupoMensaje);
            oos.flush();
        } catch (Exception e) {
        }
    }
}
