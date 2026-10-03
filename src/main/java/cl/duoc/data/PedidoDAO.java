package cl.duoc.data;

import cl.duoc.model.Pedido;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO de pedidos: operaciones CRUD sobre la tabla "pedidos".
 * Usa PreparedStatement y ResultSet, y cierra los recursos con try-with-resources.
 */
public class PedidoDAO {

    /** Inserta un pedido nuevo. Devuelve true si se guardó. */
    public boolean create(Pedido pedido) throws SQLException {
        String sql = "INSERT INTO pedidos (direccion, tipo, estado) VALUES (?, ?, ?)";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, pedido.getDireccion());
            stmt.setString(2, pedido.getTipo());
            stmt.setString(3, pedido.getEstado());
            return stmt.executeUpdate() > 0;
        }
    }

    /** Devuelve todos los pedidos registrados. */
    public List<Pedido> readAll() throws SQLException {
        List<Pedido> lista = new ArrayList<>();
        String sql = "SELECT * FROM pedidos";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                lista.add(new Pedido(
                        rs.getInt("id"),
                        rs.getString("direccion"),
                        rs.getString("tipo"),
                        rs.getString("estado")
                ));
            }
        }
        return lista;
    }

    /**
     * Devuelve los pedidos filtrando opcionalmente por estado y/o tipo.
     *
     * @param estado estado a buscar, o null para no filtrar por estado
     * @param tipo   tipo a buscar, o null para no filtrar por tipo
     */
    public List<Pedido> readFiltrado(String estado, String tipo) throws SQLException {
        List<Pedido> lista = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM pedidos WHERE 1 = 1");
        if (estado != null) {
            sql.append(" AND estado = ?");
        }
        if (tipo != null) {
            sql.append(" AND tipo = ?");
        }
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            int i = 1;
            if (estado != null) {
                stmt.setString(i++, estado);
            }
            if (tipo != null) {
                stmt.setString(i++, tipo);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Pedido(
                            rs.getInt("id"),
                            rs.getString("direccion"),
                            rs.getString("tipo"),
                            rs.getString("estado")
                    ));
                }
            }
        }
        return lista;
    }

    /** Actualiza dirección, tipo y estado del pedido indicado por su id. Devuelve true si se modificó. */
    public boolean update(Pedido pedido) throws SQLException {
        String sql = "UPDATE pedidos SET direccion = ?, tipo = ?, estado = ? WHERE id = ?";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, pedido.getDireccion());
            stmt.setString(2, pedido.getTipo());
            stmt.setString(3, pedido.getEstado());
            stmt.setInt(4, pedido.getId());
            return stmt.executeUpdate() > 0;
        }
    }

    /** Elimina un pedido por id (falla si tiene entregas asociadas). Devuelve true si se eliminó. */
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM pedidos WHERE id = ?";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }
}