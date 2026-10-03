package cl.duoc.ui;

import cl.duoc.data.EntregaDAO;
import cl.duoc.data.PedidoDAO;
import cl.duoc.data.RepartidorDAO;
import cl.duoc.model.Entrega;
import cl.duoc.model.Pedido;
import cl.duoc.model.Repartidor;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class MainFrame extends JFrame {

    private final RepartidorDAO repartidorDAO = new RepartidorDAO();
    private final PedidoDAO pedidoDAO = new PedidoDAO();
    private final EntregaDAO entregaDAO = new EntregaDAO();

    // Colores Bonitos y Simples (Tema Modern Light)
    private static final Color COLOR_PRIMARY = new Color(33, 150, 243);    // Azul Limpio
    private static final Color COLOR_BG = new Color(245, 247, 250);         // Fondo Claro
    private static final Color COLOR_CARD = Color.WHITE;                    // Tarjetas Blancas
    private static final Color COLOR_SUCCESS = new Color(76, 175, 80);      // Verde
    private static final Color COLOR_DANGER = new Color(244, 67, 54);       // Rojo
    private static final Color COLOR_ACCENT = new Color(255, 152, 0);       // Naranja / Warning

    // Componentes Repartidores
    private JTextField txtNombreRepartidor;
    private JTable tablaRepartidores;
    private DefaultTableModel modelRepartidores;

    // Componentes Pedidos
    private JTextField txtDireccionPedido;
    private JComboBox<String> cbTipoPedido;
    private JComboBox<String> cbEstadoPedido;
    private JTable tablaPedidos;
    private DefaultTableModel modelPedidos;

    // Componentes Entregas
    private JComboBox<Pedido> cbEntregaPedido;
    private JComboBox<Repartidor> cbEntregaRepartidor;
    private JTable tablaEntregas;
    private DefaultTableModel modelEntregas;

    // Validaciones
    private static final int MAX_TEXTO = 100;
    private boolean cargando = false;

    public MainFrame() {
        aplicarEstiloGeneral();

        setTitle("SpeedFast - Gestión Unificada");
        setSize(1200, 850);

        // 1. Confirmación de Cierre con la 'X' de la Ventana
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmarSalida();
            }
        });

        setLocationRelativeTo(null);
        getContentPane().setBackground(COLOR_BG);
        setLayout(new BorderLayout());

        // Header Superior
        add(createHeaderPanel(), BorderLayout.NORTH);

        // Contenedor Unificado (3 Secciones en una sola vista con scroll)
        JPanel mainContent = new JPanel();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setBackground(COLOR_BG);
        mainContent.setBorder(new EmptyBorder(15, 15, 15, 15));

        mainContent.add(createPanelRepartidores());
        mainContent.add(Box.createVerticalStrut(15));
        mainContent.add(createPanelPedidos());
        mainContent.add(Box.createVerticalStrut(15));
        mainContent.add(createPanelEntregas());

        JScrollPane scrollMain = new JScrollPane(mainContent);
        scrollMain.getVerticalScrollBar().setUnitIncrement(16);
        scrollMain.setBorder(null);

        add(scrollMain, BorderLayout.CENTER);

        cargarTodo();
    }

    private void confirmarSalida() {
        int opcion = JOptionPane.showConfirmDialog(
                this,
                "¿Está seguro de que desea salir del sistema SpeedFast?",
                "Confirmar Salida",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (opcion == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }

    private void aplicarEstiloGeneral() {
        try {
            UIManager.setLookAndFeel("com.formdev.flatlaf.FlatLightLaf");
        } catch (Exception ignored) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(COLOR_PRIMARY);
        header.setPreferredSize(new Dimension(getWidth(), 55));
        header.setBorder(new EmptyBorder(10, 20, 10, 20));

        JLabel title = new JLabel("SPEEDFAST - Gestion de Entregas");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(Color.WHITE);

        header.add(title, BorderLayout.WEST);
        return header;
    }

    // --- SECCIÓN 1: REPARTIDORES ---
    private JPanel createPanelRepartidores() {
        JPanel card = createCardPanel("Seccion Repartidores");
        card.setLayout(new BorderLayout(10, 10));

        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        form.setOpaque(false);

        txtNombreRepartidor = createStyledTextField(18);
        JButton btnGuardar = createStyledButton("Crear", COLOR_SUCCESS);
        JButton btnActualizar = createStyledButton("Actualizar", COLOR_ACCENT);
        JButton btnEliminar = createStyledButton("Eliminar", COLOR_DANGER);

        form.add(new JLabel("Nombre:"));
        form.add(txtNombreRepartidor);
        form.add(btnGuardar);
        form.add(btnActualizar);
        form.add(btnEliminar);

        modelRepartidores = new DefaultTableModel(new String[]{"ID Repartidor", "Nombre"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaRepartidores = new JTable(modelRepartidores);
        estilarTabla(tablaRepartidores);

        JScrollPane scroll = new JScrollPane(tablaRepartidores);
        scroll.setPreferredSize(new Dimension(400, 120));

        btnGuardar.addActionListener(e -> guardarRepartidor());
        btnActualizar.addActionListener(e -> actualizarRepartidor());
        btnEliminar.addActionListener(e -> eliminarRepartidor());

        tablaRepartidores.getSelectionModel().addListSelectionListener(e -> {
            int row = tablaRepartidores.getSelectedRow();
            if (!e.getValueIsAdjusting() && row != -1) {
                txtNombreRepartidor.setText((String) modelRepartidores.getValueAt(row, 1));
            }
        });

        card.add(form, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    private void guardarRepartidor() {
        String nombre = txtNombreRepartidor.getText().trim();
        if (nombre.isEmpty() || !formatoNombreValido(nombre)) return;
        try {
            if (repartidorDAO.create(new Repartidor(nombre))) {
                txtNombreRepartidor.setText("");
                cargarTodo();
            }
        } catch (SQLException ex) { mostrarErrorSQL(ex); }
    }

    private void actualizarRepartidor() {
        int row = tablaRepartidores.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un repartidor", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String nombre = txtNombreRepartidor.getText().trim();
        if (nombre.isEmpty() || !formatoNombreValido(nombre)) return;
        int id = (int) modelRepartidores.getValueAt(row, 0);
        try {
            if (repartidorDAO.update(new Repartidor(id, nombre))) {
                txtNombreRepartidor.setText("");
                cargarTodo();
            }
        } catch (SQLException ex) { mostrarErrorSQL(ex); }
    }

    private void eliminarRepartidor() {
        int row = tablaRepartidores.getSelectedRow();
        if (row == -1) return;
        int id = (int) modelRepartidores.getValueAt(row, 0);
        if (JOptionPane.showConfirmDialog(this, "¿Eliminar repartidor?", "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try {
                if (repartidorDAO.delete(id)) cargarTodo();
            } catch (SQLException ex) { mostrarErrorSQL(ex); }
        }
    }

    // --- SECCIÓN 2: PEDIDOS ---
    private JPanel createPanelPedidos() {
        JPanel card = createCardPanel("Seccion Pedidos");
        card.setLayout(new BorderLayout(10, 10));

        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        form.setOpaque(false);

        txtDireccionPedido = createStyledTextField(16);
        cbTipoPedido = new JComboBox<>(new String[]{"COMIDA", "ENCOMIENDA", "EXPRESS"});
        cbEstadoPedido = new JComboBox<>(new String[]{"PENDIENTE", "EN_REPARTO", "ENTREGADO"});
        estilarComboBox(cbTipoPedido);
        estilarComboBox(cbEstadoPedido);

        JButton btnGuardar = createStyledButton("Crear", COLOR_SUCCESS);
        JButton btnActualizar = createStyledButton("️Actualizar", COLOR_ACCENT);
        JButton btnEliminar = createStyledButton("Eliminar", COLOR_DANGER);

        form.add(new JLabel("Dirección:"));
        form.add(txtDireccionPedido);
        form.add(new JLabel("Tipo:"));
        form.add(cbTipoPedido);
        form.add(new JLabel("Estado:"));
        form.add(cbEstadoPedido);
        form.add(btnGuardar);
        form.add(btnActualizar);
        form.add(btnEliminar);

        modelPedidos = new DefaultTableModel(new String[]{"ID Pedido", "Dirección", "Tipo", "Estado"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaPedidos = new JTable(modelPedidos);
        estilarTabla(tablaPedidos);

        JScrollPane scroll = new JScrollPane(tablaPedidos);
        scroll.setPreferredSize(new Dimension(400, 130));

        btnGuardar.addActionListener(e -> guardarPedido());
        btnActualizar.addActionListener(e -> actualizarPedido());
        btnEliminar.addActionListener(e -> eliminarPedido());

        tablaPedidos.getSelectionModel().addListSelectionListener(e -> {
            int row = tablaPedidos.getSelectedRow();
            if (!e.getValueIsAdjusting() && row != -1) {
                txtDireccionPedido.setText((String) modelPedidos.getValueAt(row, 1));
                cbTipoPedido.setSelectedItem(modelPedidos.getValueAt(row, 2));
                cbEstadoPedido.setSelectedItem(modelPedidos.getValueAt(row, 3));
            }
        });

        card.add(form, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    private void guardarPedido() {
        String dir = txtDireccionPedido.getText().trim();
        if (dir.isEmpty() || !direccionValida(dir)) return;
        String tipo = (String) cbTipoPedido.getSelectedItem();
        String estado = (String) cbEstadoPedido.getSelectedItem();

        try {
            if (pedidoDAO.create(new Pedido(dir, tipo, estado))) {
                txtDireccionPedido.setText("");
                cargarTodo();
            }
        } catch (SQLException ex) { mostrarErrorSQL(ex); }
    }

    private void actualizarPedido() {
        int row = tablaPedidos.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un pedido", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String dir = txtDireccionPedido.getText().trim();
        if (dir.isEmpty() || !direccionValida(dir)) return;

        int id = (int) modelPedidos.getValueAt(row, 0);
        String tipo = (String) cbTipoPedido.getSelectedItem();
        String estado = (String) cbEstadoPedido.getSelectedItem();

        try {
            // Al cambiar el pedido a 'ENTREGADO', libera automáticamente al repartidor
            if (pedidoDAO.update(new Pedido(id, dir, tipo, estado))) {
                txtDireccionPedido.setText("");
                cargarTodo(); // Refresca listas y libera al repartidor ocupado
            }
        } catch (SQLException ex) { mostrarErrorSQL(ex); }
    }

    private void eliminarPedido() {
        int row = tablaPedidos.getSelectedRow();
        if (row == -1) return;
        int id = (int) modelPedidos.getValueAt(row, 0);
        if (JOptionPane.showConfirmDialog(this, "¿Eliminar pedido?", "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try {
                if (pedidoDAO.delete(id)) cargarTodo();
            } catch (SQLException ex) { mostrarErrorSQL(ex); }
        }
    }

    // --- SECCIÓN 3: ENTREGAS Y ASIGNACIÓN ---
    private JPanel createPanelEntregas() {
        JPanel card = createCardPanel(" Seccion Asignacion de Entregas");
        card.setLayout(new BorderLayout(10, 10));

        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        form.setOpaque(false);

        cbEntregaPedido = new JComboBox<>();
        cbEntregaRepartidor = new JComboBox<>();
        estilarComboBox(cbEntregaPedido);
        estilarComboBox(cbEntregaRepartidor);

        cbEntregaPedido.setPreferredSize(new Dimension(280, 30));
        cbEntregaRepartidor.setPreferredSize(new Dimension(200, 30));

        JButton btnAsignar = createStyledButton("Asignar Pedido", COLOR_PRIMARY);
        JButton btnEliminar = createStyledButton("Eliminar Entrega", COLOR_DANGER);

        form.add(new JLabel("Pedido:"));
        form.add(cbEntregaPedido);
        form.add(new JLabel("Repartidor Libre:"));
        form.add(cbEntregaRepartidor);
        form.add(btnAsignar);
        form.add(btnEliminar);

        modelEntregas = new DefaultTableModel(new String[]{"ID Entrega", "ID Pedido", "ID Repartidor", "Fecha", "Hora"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaEntregas = new JTable(modelEntregas);
        estilarTabla(tablaEntregas);

        JScrollPane scroll = new JScrollPane(tablaEntregas);
        scroll.setPreferredSize(new Dimension(400, 130));

        btnAsignar.addActionListener(e -> registrarEntrega());
        btnEliminar.addActionListener(e -> eliminarEntrega());

        card.add(form, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    private void registrarEntrega() {
        Pedido p = (Pedido) cbEntregaPedido.getSelectedItem();
        Repartidor r = (Repartidor) cbEntregaRepartidor.getSelectedItem();

        if (p == null || r == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un pedido y repartidor válidos.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Date fechaActual = Date.valueOf(LocalDate.now());
        Time horaActual = Time.valueOf(LocalTime.now());

        try {
            // Verifica que el repartidor no tenga otro pedido pendiente/en reparto en curso
            if (!"ENTREGADO".equals(p.getEstado()) && entregaDAO.repartidorOcupado(r.getId(), 0)) {
                JOptionPane.showMessageDialog(this, "El repartidor " + r.getNombre() + " ya tiene un pedido asignado en curso.\nMarque el pedido como ENTREGADO para asignarle uno nuevo.", "Repartidor Ocupado", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (entregaDAO.create(new Entrega(p.getId(), r.getId(), fechaActual, horaActual))) {
                // Actualiza estado del pedido asignado a EN_REPARTO si estaba PENDIENTE
                if ("PENDIENTE".equals(p.getEstado())) {
                    p.setEstado("EN_REPARTO");
                    pedidoDAO.update(p);
                }
                JOptionPane.showMessageDialog(this, "Entrega asignada exitosamente.");
                cargarTodo();
            }
        } catch (SQLException ex) { mostrarErrorSQL(ex); }
    }

    private void eliminarEntrega() {
        int row = tablaEntregas.getSelectedRow();
        if (row == -1) return;
        int id = (int) modelEntregas.getValueAt(row, 0);
        if (JOptionPane.showConfirmDialog(this, "¿Eliminar registro de entrega?", "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try {
                if (entregaDAO.delete(id)) cargarTodo();
            } catch (SQLException ex) { mostrarErrorSQL(ex); }
        }
    }

    // --- CARGAR DATOS Y REFRESCAR TODO EN PANTALLA ---
    private void cargarTodo() {
        cargando = true;
        try {
            // Cargar Repartidores
            modelRepartidores.setRowCount(0);
            cbEntregaRepartidor.removeAllItems();
            List<Repartidor> repartidores = repartidorDAO.readAll();
            for (Repartidor rep : repartidores) {
                modelRepartidores.addRow(new Object[]{rep.getId(), rep.getNombre()});
                cbEntregaRepartidor.addItem(rep);
            }

            // Cargar Pedidos
            modelPedidos.setRowCount(0);
            cbEntregaPedido.removeAllItems();
            List<Pedido> pedidos = pedidoDAO.readAll();
            for (Pedido ped : pedidos) {
                modelPedidos.addRow(new Object[]{ped.getId(), ped.getDireccion(), ped.getTipo(), ped.getEstado()});
                cbEntregaPedido.addItem(ped);
            }

            // Cargar Entregas
            modelEntregas.setRowCount(0);
            List<Entrega> entregas = entregaDAO.readAll();
            for (Entrega ent : entregas) {
                modelEntregas.addRow(new Object[]{ent.getId(), ent.getIdPedido(), ent.getIdRepartidor(), ent.getFecha(), ent.getHora()});
            }

        } catch (SQLException ex) {
            mostrarErrorSQL(ex);
        } finally {
            cargando = false;
        }
    }

    // --- MÉTODOS DE VALIDACIÓN Y UTILS ---
    private boolean formatoNombreValido(String nombre) {
        if (nombre.length() < 2 || nombre.length() > MAX_TEXTO) {
            JOptionPane.showMessageDialog(this, "El nombre debe tener entre 2 y " + MAX_TEXTO + " caracteres.", "Validación", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private boolean direccionValida(String dir) {
        if (dir.length() < 5 || dir.length() > MAX_TEXTO) {
            JOptionPane.showMessageDialog(this, "La dirección debe tener entre 5 y " + MAX_TEXTO + " caracteres.", "Validación", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private void mostrarErrorSQL(SQLException ex) {
        JOptionPane.showMessageDialog(this, "Error en Base de Datos: " + ex.getMessage(), "Error SQL", JOptionPane.ERROR_MESSAGE);
    }

    private JPanel createCardPanel(String title) {
        JPanel panel = new JPanel();
        panel.setBackground(COLOR_CARD);
        TitledBorder border = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(210, 215, 220), 1, true),
                title
        );
        border.setTitleFont(new Font("Segoe UI", Font.BOLD, 13));
        border.setTitleColor(Color.DARK_GRAY);
        panel.setBorder(BorderFactory.createCompoundBorder(border, new EmptyBorder(8, 8, 8, 8)));
        return panel;
    }

    private JTextField createStyledTextField(int columns) {
        JTextField field = new JTextField(columns);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 205, 210), 1, true),
                new EmptyBorder(4, 6, 4, 6)
        ));
        return field;
    }

    private <T> void estilarComboBox(JComboBox<T> box) {
        box.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        box.setBackground(Color.WHITE);
    }

    private JButton createStyledButton(String text, Color bg) {
        JButton button = new JButton(text);
        button.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 11));
        button.setBackground(bg);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(6, 12, 6, 12));
        return button;
    }

    private void estilarTabla(JTable table) {
        table.setRowHeight(26);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setSelectionBackground(new Color(220, 235, 252));
        table.setSelectionForeground(Color.BLACK);
        table.setGridColor(new Color(230, 230, 230));

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(240, 242, 245));
        header.setForeground(Color.DARK_GRAY);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 249, 250));
                }
                return c;
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}