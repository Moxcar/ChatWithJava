package pserver;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Conexion {

    private String url = "jdbc:mysql://localhost:3306/prograavanzada?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
    Connection conn = null;

    public Conexion() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(url, "root", "");
            if (conn != null) {
                Servidor.log.append("Se ha conectado a la BD\n");
            }
        } catch (Exception e) {
            Servidor.log.append("No se ha conectado a la BD\n");
            System.exit(1);
        }
    }

    public boolean registrarUsuario(String nombre, String contraseña) {
        try {
            String query = "insert into Usuarios (Nombre, Contraseña) values (?,?)";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, nombre);
            ps.setString(2, contraseña);
            int resultado = ps.executeUpdate();
            if (resultado == 1) {
                Servidor.log.append("Usuario " + nombre + " registrado\n");
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Servidor.log.append("Error al registrar usuario" + nombre + "\n");
        }
        return false;
    }

    public int iniciarSesion(String nombre, String contraseña) {
        try {
            String query = "select ID from usuarios where Nombre = '" + nombre + "' AND Contraseña = '" + contraseña + "'";
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(query);
            if (rs.next()) {
                int result = rs.getInt("ID");
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public String recuperarContraseña(String nombre) {
        try {
            String query = "select Contraseña from usuarios where Nombre = '" + nombre + "'";
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(query);
            if (rs.next()) {
                String result = rs.getString("Contraseña");
                Servidor.log.append("Contraseña del usuario: " + nombre + "Recuperada: " + result + "\n");
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Servidor.log.append("Error al recuperar contraseña\n");
        }
        return "";
    }

    Map<Integer, String> obtenerUsuarios() {
        Map<Integer, String> result = Collections.synchronizedMap(new HashMap<>());
        try {
            String query = "select ID, Nombre from usuarios";
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(query);
            while (rs.next()) {
                result.put(rs.getInt("ID"), rs.getString("Nombre"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    void enviarSolicitudDeAmistad(Integer id1, Integer id2) {
        try {
            String query = "insert into solicitudes (ID_Remitente, ID_Destinatario) values (?,?)";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setInt(1, id1);
            ps.setInt(2, id2);
            int resultado = ps.executeUpdate();
            if (resultado == 1) {
                Servidor.log.append("Solicitud de amistad registrada\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    Integer crearGrupo(Integer idCreador, String nombre) {
        try {
            String query = "insert into grupo (ID_Creador, Nombre) values (?,?)";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setInt(1, idCreador);
            ps.setString(2, nombre);
            int resultado = ps.executeUpdate();
            if (resultado == 1) {
                Servidor.log.append("Grupo '" + nombre + "' creado");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            String query = "select ID from grupo where ID_Creador=" + idCreador + " AND Nombre='" + nombre + "'";
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(query);
            Integer result;
            while (rs.next()) {
                result = rs.getInt("ID");
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    Integer enviarSolicitudDeGrupo(Integer idGrupo, Integer idDestinatario) {
        try {
            String query = "insert into solicitudesgrupos (ID_Grupo, ID_Destinatario) values (?,?)";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setInt(1, idGrupo);
            ps.setInt(2, idDestinatario);
            int resultado = ps.executeUpdate();
            if (resultado == 1) {
                Servidor.log.append("Solicitud de grupo registrada\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            String query = "select ID from solicitudesgrupo where ID_Grupo=" + idGrupo + " AND ID_Destinatario='" + idDestinatario + "'";
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(query);
            Integer result;
            while (rs.next()) {
                result = rs.getInt("ID");
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    void actualizarSolicitudesDeAmistad() {
        try {
            String query = "select ID_Remitente, ID_Destinatario from solicitudes";
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(query);
            Servidor.solicitudesRemitentes.clear();
            Servidor.solicitudesDestinatarios.clear();
            while (rs.next()) {
                Servidor.solicitudesRemitentes.add(rs.getInt("ID_Remitente"));
                Servidor.solicitudesDestinatarios.add(rs.getInt("ID_Destinatario"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            Servidor.log.append("Error al actualizar solicitudes de amistad\n");
        }
    }

    void actualizarSolicitudesDeGrupos() {
        try {
            String query = "select * from solicitudesgrupos";
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(query);
            Servidor.solicitudesGruposID.clear();
            Servidor.solicitudesGruposIDGrupo.clear();
            Servidor.solicitudesGruposIDDestinatario.clear();
            while (rs.next()) {
                Servidor.solicitudesGruposID.add(rs.getInt("ID"));
                Servidor.solicitudesGruposIDGrupo.add(rs.getInt("ID_Grupo"));
                Servidor.solicitudesGruposIDDestinatario.add(rs.getInt("ID_Destinatario"));
            }
            query = "select * from grupo";
            Statement st2 = conn.createStatement();
            ResultSet rs2 = st2.executeQuery(query);
            Servidor.allGroups.clear();
            Servidor.allGroupCreators.clear();
            while (rs2.next()) {
                Integer IDDelGrupo = rs2.getInt("ID");
                Servidor.allGroupCreators.put(IDDelGrupo, rs2.getInt("ID_Creador"));
                Servidor.allGroups.put(IDDelGrupo, rs2.getString("Nombre"));
            }
            query = "select * from miembrosdegrupo";
            Statement st3 = conn.createStatement();
            ResultSet rs3 = st3.executeQuery(query);
            Servidor.allGroupsMembers1.clear();
            Servidor.allGroupsMembers2.clear();
            while (rs3.next()) {
                Integer idGrupo = rs3.getInt("ID_Grupo");
                Integer idUsuario = rs3.getInt("ID_Usuario");
                Servidor.allGroupsMembers1.add(idGrupo);
                Servidor.allGroupsMembers2.add(idUsuario);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Servidor.log.append("Error al actualizar solicitudes de grupos\n");
        }
    }

    void borrarSolicitudDeAmistad(Integer id1, Integer id2) {
        try {
            PreparedStatement pps = conn.prepareStatement("delete from solicitudes where ID_Remitente=" + id2 + " AND ID_Destinatario=" + id1);
            pps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void crearAmigos(Integer id1, Integer id2) {
        try {
            String query = "insert into amigos (ID_usuario, ID_amigo) values (?,?)";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setInt(1, id1);
            ps.setInt(2, id2);
            int resultado = ps.executeUpdate();
            if (resultado == 1) {
                Servidor.log.append("Amigo registrado");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    List<Integer> obtenerAmigos(Integer id) {
        List<Integer> result = Collections.synchronizedList(new ArrayList<>());
        try {
            String query = "select ID_amigo from amigos where ID_usuario=" + id;
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(query);
            while (rs.next()) {
                result.add(rs.getInt("ID_amigo"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            Servidor.log.append("Error al obtener amigos\n");
        }
        return result;
    }

    void borrarSolicitudDeGrupo(Integer idSolicitud) {
        System.out.println("BORRAR SOLICITUD " + idSolicitud);
        int aux = Servidor.solicitudesGruposID.indexOf(idSolicitud);
        Servidor.solicitudesGruposID.remove(aux);
        Servidor.solicitudesGruposIDGrupo.remove(aux);
        Servidor.solicitudesGruposIDDestinatario.remove(aux);

        try {
            PreparedStatement pps = conn.prepareStatement("delete from solicitudesgrupos where ID=" + idSolicitud);
            pps.executeUpdate();
            Servidor.log.append("Se borro la solicitud de grupo con id " + idSolicitud + "\n");
        } catch (Exception e) {
            e.printStackTrace();
            Servidor.log.append("Error al borrar solicitud de grupo\n");
        }
    }

    void agregarAGrupo(Integer idSolicitud) {
        try {
            String query = "select ID_Grupo, ID_Destinatario from solicitudesgrupos where ID=" + idSolicitud;
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(query);
            Integer idGrupo = null, idDestinatario = null;
            while (rs.next()) {
                idGrupo = rs.getInt("ID_Grupo");
                idDestinatario = rs.getInt("ID_Destinatario");
            }
            query = "insert into miembrosdegrupo (ID_Grupo, ID_Usuario) values (?,?)";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setInt(1, idGrupo);
            ps.setInt(2, idDestinatario);
            int resultado = ps.executeUpdate();
            if (resultado == 1) {
                Servidor.log.append("Añadido a grupo\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void guardarMensajeGrupo(Integer idGrupo, String mensaje) {
        try {
            String query = "insert into mensajesgrupo (ID_Grupo, Mensaje) values (?,?)";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setInt(1, idGrupo);
            ps.setString(2, mensaje);
            int resultado = ps.executeUpdate();
            if (resultado == 1) {
                Servidor.log.append("Mensaje de grupo registrado\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void obtenerMensajes() {
        try {
            String query = "select ID_Grupo, Mensaje from mensajesgrupo";
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(query);
            Servidor.mensajesGrupoIDGrupo.clear();
            Servidor.mensajesGrupoMensaje.clear();
            while (rs.next()) {
                Servidor.mensajesGrupoIDGrupo.add(rs.getInt("ID_Grupo"));
                Servidor.mensajesGrupoMensaje.add(rs.getString("Mensaje"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
