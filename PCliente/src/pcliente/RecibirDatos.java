package pcliente;

import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import static pcliente.ChatScreen.allChats;
import static pcliente.ChatScreen.allUsers;

public class RecibirDatos extends Thread {

    Socket socket;
    ChatScreen chatScreen;

    public RecibirDatos(Socket s, ChatScreen chat) {
        socket = s;
        chatScreen = chat;
    }

    @Override
    public void run() {
        try {
            while (true) {
                System.out.println("Esperando datos");
                String type = (String) Cliente.ois.readObject();
                System.out.println("recibiendo datos tipo:" + type);
                switch (type) {
                    case "activos": {
                        Map<Integer, String> activeUsers = Collections.synchronizedMap(new HashMap<>());
                        activeUsers = (Map<Integer, String>) Cliente.ois.readObject();
                        ChatScreen.activeUsers = activeUsers;
                        chatScreen.updateActiveUsers();
                        chatScreen.updateUsers();
                        chatScreen.updateFriendScreen();
                        break;
                    }
                    case "todoslosusuarios": {
                        Map<Integer, String> allUsers = (Map<Integer, String>) Cliente.ois.readObject();
                        ChatScreen.allUsers = allUsers;
                        chatScreen.updateUsers();
                        break;
                    }
                    case "solicitud": {
                        Integer aux = (Integer) Cliente.ois.readObject();
                        chatScreen.mostrarSolicitud(aux);
                        break;
                    }
                    case "listaamigos": {
                        List<Integer> aux;
                        aux = (List<Integer>) Cliente.ois.readObject();
                        ChatScreen.allClientFriends = aux;
                        break;
                    }
                    case "recibirmensaje": {
                        Integer id = (Integer) Cliente.ois.readObject();
                        String mensaje = (String) Cliente.ois.readObject();
                        boolean control = false;
                        for (ChatWindow c : ChatScreen.allChats) {
                            if (Objects.equals(c.ID, id)) {
                                c.mensajes.add(mensaje);
                                c.actualizar();
                                control = true;
                            }
                        }
                        if (!control) {
                            ChatWindow chat = new ChatWindow();
                            chat.ID = id;
                            ChatScreen.allChats.add(chat);
                            chat.setTitle("Chat " + ChatScreen.allUsers.get(id));
                            chat.mensajes.add(mensaje);
                            chat.actualizar();
                        }
                        break;
                    }
                    case "recibirmensajegrupo": {
                        Integer id = (Integer) Cliente.ois.readObject();
                        String mensaje = (String) Cliente.ois.readObject();
                        String nombreGrupo = (String) Cliente.ois.readObject();
                        boolean control = false;
                        for (GroupWindow c : ChatScreen.allGroupsWindows) {
                            if (Objects.equals(c.ID, id)) {
                                c.mensajes.add(mensaje);
                                c.actualizar();
                                control = true;
                            }
                        }
                        if (!control) {
                            GroupWindow chat = new GroupWindow();
                            chat.ID = id;
                            ChatScreen.allGroupsWindows.add(chat);
                            chat.setTitle("Grupo " + nombreGrupo + " " + id);
                            chat.mensajes.add(mensaje);
                            chat.actualizar();
                            chat.cargarMensajes();
                        }
                        break;
                    }
                    case "solicitudgrupo": {
                        String nombreDelGrupo = (String) Cliente.ois.readObject();
                        Integer idSolicitud = (Integer) Cliente.ois.readObject();
                        chatScreen.enviarSolicitudGrupo(nombreDelGrupo, idSolicitud);
                        break;
                    }
                    case "listagrupos": {
                        ChatScreen.allClientGroups = (List<Integer>) Cliente.ois.readObject();
                        ChatScreen.allClientGroupsNames = (List<String>) Cliente.ois.readObject();
                        break;
                    }
                    case "mensajes": {
                        ChatScreen.mensajesGrupoIDGrupo = (List<Integer>) Cliente.ois.readObject();
                        ChatScreen.mensajesGrupoMensaje = (List<String>) Cliente.ois.readObject();
                        break;
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
