package cl.duoc.data;

import cl.duoc.model.Entrega;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO de entregas: operaciones CRUD sobre la tabla "entregas".
 * Usa PreparedStatement y ResultSet, y cierra los recursos con try-with-resources.
 */
public class EntregaDAO {

    /** Inserta una entrega nueva (pedido + repartidor + fecha + hora). Devuelve true si se guardó. */
    public boolean create(Entrega entrega) throws SQLException {
        String sql = "INSERT INTO entregas (id_pedido, id_repartidor, fecha, hora) VALUES (?, ?, ?, ?)";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, entrega.getIdPedido());
            stmt.setInt(2, entrega.getIdRepartidor());
            stmt.setDate(3, entrega.getFecha());
            stmt.setTime(4, entrega.getHora());
            return stmt.executeUpdate() > 0;
        }
    }

    /** Devuelve todas las entregas registradas. */
    public List<Entrega> readAll() throws SQLException {
        List<Entrega> lista = new ArrayList<>();
        String sql = "SELECT * FROM entregas";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                lista.add(new Entrega(
                        rs.getInt("id"),
                        rs.getInt("id_pedido"),
                        rs.getInt("id_repartidor"),
                        rs.getDate("fecha"),
                        rs.getTime("hora")
                ));
            }
        }
        return lista;
    }

    /**
     * Devuelve las entregas filtrando opcionalmente por pedido y/o repartidor.
     *
     * @param idPedido     id del pedido, o null para no filtrar por pedido
     * @param idRepartidor id del repartidor, o null para no filtrar por repartidor
     */
    public List<Entrega> readFiltrado(Integer idPedido, Integer idRepartidor) throws SQLException {
        List<Entrega> lista = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM entregas WHERE 1 = 1");
        if (idPedido != null) {
            sql.append(" AND id_pedido = ?");
        }
        if (idRepartidor != null) {
            sql.append(" AND id_repartidor = ?");
        }
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            int i = 1;
            if (idPedido != null) {
                stmt.setInt(i++, idPedido);
            }
            if (idRepartidor != null) {
                stmt.setInt(i++, idRepartidor);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Entrega(
                            rs.getInt("id"),
                            rs.getInt("id_pedido"),
                            rs.getInt("id_repartidor"),
                            rs.getDate("fecha"),
                            rs.getTime("hora")
                    ));
                }
            }
        }
        return lista;
    }

    /**
     * Indica si el repartidor ya tiene otra entrega en curso, es decir, asociada a un pedido
     * que todavía no está ENTREGADO. Sirve para no asignarle dos pedidos en simultáneo.
     *
     * @param idRepartidor     id del repartidor a consultar
     * @param idEntregaExcluir id de una entrega que no se cuenta (la que se está editando); 0 si no aplica
     */
    public boolean repartidorOcupado(int idRepartidor, int idEntregaExcluir) throws SQLException {
        String sql = "SELECT COUNT(*) FROM entregas e JOIN pedidos p ON e.id_pedido = p.id "
                + "WHERE e.id_repartidor = ? AND e.id <> ? AND COALESCE(p.estado, '') <> 'ENTREGADO'";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idRepartidor);
            stmt.setInt(2, idEntregaExcluir);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /** Actualiza pedido, repartidor, fecha y hora de la entrega indicada por su id. Devuelve true si se modificó. */
    public boolean update(Entrega entrega) throws SQLException {
        String sql = "UPDATE entregas SET id_pedido = ?, id_repartidor = ?, fecha = ?, hora = ? WHERE id = ?";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, entrega.getIdPedido());
            stmt.setInt(2, entrega.getIdRepartidor());
            stmt.setDate(3, entrega.getFecha());
            stmt.setTime(4, entrega.getHora());
            stmt.setInt(5, entrega.getId());
            return stmt.executeUpdate() > 0;
        }
    }

    /** Elimina una entrega por id. Devuelve true si se eliminó. */
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM entregas WHERE id = ?";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }
}