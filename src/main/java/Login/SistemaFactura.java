/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package Login;
import Conexion.conexion;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JDialog;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JTable;
/**
 *
 * @author JOSEP
 */
public class SistemaFactura extends javax.swing.JFrame {

    Timer timer = new Timer();
    private int idFactura;
    conexion conect = new conexion();
    Connection conn = conect.getConnection();
    
    double ActualizaMontos()
    {
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        double monto  = 0;

        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/crudpulperia", "root","");
            String query = "select sum(MontoNeto) monto from facturas where id = " + txtid.getText();
            stmt = conn.prepareStatement(query);
            
            rs = stmt.executeQuery();
            if (rs.next()) {
                monto = rs.getDouble("monto");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return monto;
    }
    
   TimerTask timertask = new TimerTask() {
    public void run() {
        double montoNeto = getMontoNetoTotal(); 
        double impuesto = montoNeto * 0.15; 
        double montoTotal = montoNeto + impuesto; 
        txtmonto.setText(String.valueOf(montoNeto)); 
        txtimpuesto.setText(String.valueOf(impuesto));
        txtmontototal.setText(String.valueOf(montoTotal)); 
    }
};

    
    public void actualizarMontoNeto() {
        double montoNetoTotal = getMontoNetoTotal();
        txtmonto.setText(String.valueOf(montoNetoTotal));
    }
    
     private double getMontoNetoTotal() {
        double montoTotal = 0.0;
        DefaultTableModel model = (DefaultTableModel) tablaFactura.getModel();
        int rowCount = model.getRowCount();
        for (int i = 0; i < rowCount; i++) {
            double montoProducto = (double) model.getValueAt(i, 4);
            montoTotal += montoProducto;
        }
        return montoTotal;
    }
    //new Timer().schedule(new TimerTask() {
    //        @Override
    //        public void run() {
    //            txtmonto.setText("a" + txtmonto.getText());
    //            
    //        }
    //    }, 2 * 60 * 1000, 2 * 60 * 1000);
        
    /**
     * Creates new form SistemaFactura
     */
    public SistemaFactura() {
        initComponents();
        java.util.Date dia = new java.util.Date();
        this.datefactura.setDate(dia); 
        DefaultTableModel model = (DefaultTableModel) tablaFactura.getModel();
        model.setRowCount(0);
        txtmonto.setText("0");
        timer.scheduleAtFixedRate(timertask, 0, 1000);
    }
    
    public JTable getTablaFactura() {
        return tablaFactura;
    }
    
    public SistemaFactura(JTable tablaFactura) {
        this.tablaFactura = tablaFactura;
    }

    public void agregarEntradaTabla(int linea, String cantidad, int idProducto, String nombreProducto, double monto) {
    double cantidadDouble = Double.parseDouble(cantidad);
    double montoTotal = monto * cantidadDouble;
    DefaultTableModel model = (DefaultTableModel) tablaFactura.getModel();
    model.addRow(new Object[] { linea, cantidad, idProducto, nombreProducto, montoTotal });
    guardarDetalleFactura(idProducto, montoTotal);
    }
    
    public void guardarDetalleFactura(int idProducto, double monto) {
    int idFactura = Integer.parseInt(txtid.getText());
    Date fecha = datefactura.getDate();
    String hora = txthorafactura.getText();
    guardarDetalleFacturaDB(idFactura, fecha, hora, idProducto, monto);
}
    
    private void guardarDetalleFacturaDB(int idFactura, Date fecha, String hora, int idProducto, double monto) {
    Connection conn = null;
    PreparedStatement stmt = null;
    try {
        conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/crudpulperia", "root", "");
        String query = "INSERT INTO detalle_facturas (idfactura, fecha, hora, idproducto, monto) VALUES (?, ?, ?, ?, ?)";
        stmt = conn.prepareStatement(query);
        stmt.setInt(1, idFactura);
        stmt.setDate(2, new java.sql.Date(fecha.getTime()));
        stmt.setString(3, hora);
        stmt.setInt(4, idProducto);
        stmt.setDouble(5, monto);
        stmt.executeUpdate();
    } catch (SQLException ex) {
        ex.printStackTrace();
    } finally {
        try {
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}

public void modificarCantidadTabla(int filaSeleccionada, int cantidad, int idfactura) {
    if (filaSeleccionada >= 0 && filaSeleccionada < tablaFactura.getRowCount()) {
        tablaFactura.setValueAt(cantidad, filaSeleccionada, 1);
        int idProducto = Integer.parseInt(tablaFactura.getValueAt(filaSeleccionada, 2).toString());
        double precioUnitario = consultarPrecioProducto(idProducto);
        double nuevoMonto = cantidad * precioUnitario;
        tablaFactura.setValueAt(nuevoMonto, filaSeleccionada, 4);
        actualizarDetalleFactura(idProducto, nuevoMonto, idfactura);
    } else {
        JOptionPane.showMessageDialog(this, "Seleccione una fila válida para modificar.", "Error", JOptionPane.ERROR_MESSAGE);
    }
}

private void eliminarDetalleFactura(int idProducto, int idfactura) {
    Connection conn = null;
    PreparedStatement stmt = null;

    try {
        conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/crudpulperia", "root", "");
        String query = "DELETE FROM detalle_facturas WHERE idproducto = ? and idfactura = ?";
        stmt = conn.prepareStatement(query);
        stmt.setInt(1, idProducto);
        stmt.setInt(2, idfactura);
        stmt.executeUpdate();
    } catch (SQLException ex) {
        ex.printStackTrace();
    } finally {
        try {
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}

private void actualizarDetalleFactura(int idProducto, double nuevoMonto,int idfactura) {
    Connection conn = null;
    PreparedStatement stmt = null;

    try {
        conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/crudpulperia", "root", "");
        String query = "UPDATE detalle_facturas SET monto = ? WHERE idproducto = ? and idfactura = ?";
        stmt = conn.prepareStatement(query);
        stmt.setDouble(1, nuevoMonto);
        stmt.setInt(2, idProducto);
        stmt.setInt(3, idfactura);
        stmt.executeUpdate();
    } catch (SQLException ex) {
        ex.printStackTrace();
    } finally {
        try {
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}

public double consultarPrecioProducto(int idProducto) {
    double precio = 0.0;
    Connection conn = null;
    PreparedStatement stmt = null;
    ResultSet rs = null;

    try {
        conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/crudpulperia", "root","");
        String query = "SELECT precio FROM productos WHERE id = ?";
        stmt = conn.prepareStatement(query);
        stmt.setInt(1, idProducto);
        rs = stmt.executeQuery();
        if (rs.next()) {
            precio = rs.getDouble("precio");
        }
    } catch (SQLException e) {
        e.printStackTrace();
    } finally {
        try {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    return precio;
}

    public void mostrarDatos(String nombre, String apellido1, String apellido2, Date fecha, Date hora) {
        txtnombrefactura.setText(nombre);
        txtapellido1factura.setText(apellido1);
        txtapellido2factura.setText(apellido2);
        datefactura.setDate(fecha);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        String horaFormateada = sdf.format(hora);
        txthorafactura.setText(horaFormateada);
    }
    
    public void setIDFactura(int id) {
    txtid.setText(String.valueOf(id));
}
    
    public void actualizarEstado(String estado) {
    txtestado.setText(estado);
}


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        txtid = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        txtnombrefactura = new javax.swing.JTextField();
        txtapellido1factura = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        txthorafactura = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tablaFactura = new javax.swing.JTable();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jButton5 = new javax.swing.JButton();
        txtmonto = new javax.swing.JTextField();
        txtimpuesto = new javax.swing.JTextField();
        txtmontototal = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        txtestado = new javax.swing.JTextField();
        txtapellido2factura = new javax.swing.JTextField();
        datefactura = new com.toedter.calendar.JDateChooser();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(204, 255, 204));
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Detalle Factura Pulperia La Paz 2", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP, new java.awt.Font("Segoe UI", 1, 24))); // NOI18N

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel1.setText("Numero de factura");

        txtid.setEditable(false);
        txtid.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtidActionPerformed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel2.setText("Nombre del cliente");

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel3.setText("Fecha");

        txtnombrefactura.setEditable(false);
        txtnombrefactura.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtnombrefacturaActionPerformed(evt);
            }
        });

        txtapellido1factura.setEditable(false);
        txtapellido1factura.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtapellido1facturaActionPerformed(evt);
            }
        });

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel4.setText("Hora");

        txthorafactura.setEditable(false);
        txthorafactura.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txthorafacturaActionPerformed(evt);
            }
        });

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel5.setText("Estado");

        tablaFactura.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "Linea", "Cantidad", "id producto", "Nombre producto", "Monto"
            }
        ));
        jScrollPane1.setViewportView(tablaFactura);

        jButton1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jButton1.setIcon(new javax.swing.ImageIcon("C:\\Users\\JOSEP\\OneDrive\\Documentos\\NetBeansProjects\\CrudPulperia\\src\\main\\java\\imagenes\\add.png")); // NOI18N
        jButton1.setText("Agregar");
        jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton1MouseClicked(evt);
            }
        });
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jButton2.setIcon(new javax.swing.ImageIcon("C:\\Users\\JOSEP\\OneDrive\\Documentos\\NetBeansProjects\\CrudPulperia\\src\\main\\java\\imagenes\\delete.png")); // NOI18N
        jButton2.setText("Borrar");
        jButton2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton2MouseClicked(evt);
            }
        });

        jButton3.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jButton3.setIcon(new javax.swing.ImageIcon("C:\\Users\\JOSEP\\OneDrive\\Documentos\\NetBeansProjects\\CrudPulperia\\src\\main\\java\\imagenes\\pencil.png")); // NOI18N
        jButton3.setText("Modificar");
        jButton3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton3MouseClicked(evt);
            }
        });
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jButton4.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jButton4.setIcon(new javax.swing.ImageIcon("C:\\Users\\JOSEP\\OneDrive\\Documentos\\NetBeansProjects\\CrudPulperia\\src\\main\\java\\imagenes\\logout.png")); // NOI18N
        jButton4.setText("Salir");
        jButton4.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton4MouseClicked(evt);
            }
        });
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel6.setText("Monto Neto");

        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel7.setText("Impuestos");

        jLabel8.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel8.setText("Monto Total");

        jButton5.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jButton5.setText("Facturar");
        jButton5.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton5MouseClicked(evt);
            }
        });
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        txtmonto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtmontoActionPerformed(evt);
            }
        });

        txtimpuesto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtimpuestoActionPerformed(evt);
            }
        });

        txtmontototal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtmontototalActionPerformed(evt);
            }
        });

        jLabel9.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel9.setText("Apellido 2");

        jLabel10.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel10.setText("Apellido 1");

        txtestado.setEditable(false);
        txtestado.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtestadoActionPerformed(evt);
            }
        });

        txtapellido2factura.setEditable(false);
        txtapellido2factura.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtapellido2facturaActionPerformed(evt);
            }
        });

        datefactura.setDateFormatString("dd/MM/yyyy");
        datefactura.setEnabled(false);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel1)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(txtid, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel2)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(txtnombrefactura, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel9)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(txtapellido2factura, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(jLabel4)
                                        .addGap(18, 18, 18)
                                        .addComponent(txthorafactura, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel10)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(txtapellido1factura, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(jLabel3)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(datefactura, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(jLabel5)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(txtestado, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE))))
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 865, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(13, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel8)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 148, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 148, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(158, 158, 158)))
                                .addGap(18, 18, 18)
                                .addComponent(txtmontototal, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(34, 34, 34)
                                .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 209, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(14, 14, 14))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 148, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 148, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(73, 73, 73)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel6)
                                    .addComponent(jLabel7))
                                .addGap(18, 18, 18)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(txtimpuesto, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(txtmonto, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(259, 259, 259))))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(37, 37, 37)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(txtid, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(txtapellido1factura, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(txtestado, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(datefactura, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtnombrefactura, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtapellido2factura, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txthorafactura, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(36, 36, 36)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 201, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(52, 52, 52)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(txtmonto, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(85, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                        .addGap(123, 123, 123)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtimpuesto, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtmontototal, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 40, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(15, 15, 15))))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void txtidActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtidActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtidActionPerformed

    private void txtnombrefacturaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtnombrefacturaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtnombrefacturaActionPerformed

    private void txtapellido1facturaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtapellido1facturaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtapellido1facturaActionPerformed

    private void txthorafacturaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txthorafacturaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txthorafacturaActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton4ActionPerformed

    private void txtmontoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtmontoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtmontoActionPerformed

    private void txtimpuestoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtimpuestoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtimpuestoActionPerformed

    private void txtmontototalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtmontototalActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtmontototalActionPerformed

    private void jButton1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton1MouseClicked
       
        /*DefaultTableModel model = (DefaultTableModel) tablaFactura.getModel();
        model.addRow(new Object[]{"1", "2", "3", "4", "5"});*/
        Agregar xxx = new Agregar(this, true, tablaFactura.getRowCount());
        xxx.setVisible(true);
        
    }//GEN-LAST:event_jButton1MouseClicked

    private void jButton2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton2MouseClicked
                                   
    int filaSeleccionada = tablaFactura.getSelectedRow();
    if (filaSeleccionada != -1) { 
        int idProducto = Integer.parseInt(tablaFactura.getValueAt(filaSeleccionada, 2).toString());
        int respuesta = JOptionPane.showConfirmDialog(null, "Desea eliminar el registro?");
        if (respuesta == JOptionPane.YES_OPTION) {
            ((DefaultTableModel) tablaFactura.getModel()).removeRow(filaSeleccionada);
            eliminarDetalleFactura(idProducto, Integer.valueOf(txtid.getText()));
        }
    } else {
        JOptionPane.showMessageDialog(this, "Por favor, selecciona una fila para eliminar.");
    }

    }//GEN-LAST:event_jButton2MouseClicked

    private void jButton4MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton4MouseClicked
       
        int a = 0;
        a = JOptionPane.showConfirmDialog(null, "Desea salir del sistema?");
        if (a == JOptionPane.YES_OPTION) {
            a = JOptionPane.showConfirmDialog(null, "Realmente estas seguro?");
            if (a == JOptionPane.YES_OPTION) {   
                
                Connection conn = null;
                PreparedStatement stmt = null;
                boolean rs;
                try {
        
                    conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/crudpulperia", "root","");
                    String query = "update facturas set MontoNeto = 0, impuestos = 0, MontoTotal = 0, estado = 'A' where id = " + txtid.getText();
                    //Statement st=conect.createStatement();
                    //boolean rs=st.execute(sql);
                    //String query = "SELECT id FROM productos WHERE nombre = ?";
                    stmt = conn.prepareStatement(query);
                    //stmt.setString(1, nombreProducto);
                    rs = stmt.execute();
                    //if (rs.next()) {
                    //    idProducto = rs.getInt("id");
                   // }
                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        //if (rs != null) rs.close();
                        if (stmt != null) stmt.close();
                        if (conn != null) conn.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                System.exit(0);              
            }         
        }
    }//GEN-LAST:event_jButton4MouseClicked

    private void jButton3MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton3MouseClicked
       
         int filaSeleccionada = tablaFactura.getSelectedRow();
         int cantidad = Integer.parseInt(tablaFactura.getValueAt(filaSeleccionada, 1).toString());
    if (filaSeleccionada != -1) { 
        Modificar modificarVentana = new Modificar(this, true, filaSeleccionada, Integer.valueOf(txtid.getText()), cantidad);
        modificarVentana.setVisible(true);
    } else {
        JOptionPane.showMessageDialog(this, "Por favor, selecciona una fila para modificar.");
    }
       /* Login 
        Login nv = new Login();
        
        JDialog jd = new JDialog(nv, «Dialogo modal», Dialog.ModalityType.DOCUMENT_MODAL);
        nv.setVisible(true); */
    }//GEN-LAST:event_jButton3MouseClicked

    private void txtestadoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtestadoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtestadoActionPerformed

    private void txtapellido2facturaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtapellido2facturaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtapellido2facturaActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton5MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton5MouseClicked
        int a = 0;
        a = JOptionPane.showConfirmDialog(null, "Confirmar facturacion?");
        if (a == JOptionPane.YES_OPTION) {
            a = JOptionPane.showConfirmDialog(null, "Realmente estas seguro?");
            if (a == JOptionPane.YES_OPTION) {   
                
                Connection conn = null;
                PreparedStatement stmt = null;
                boolean rs;
                try {
        
                    conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/crudpulperia", "root","");
                    String query = "update facturas set MontoNeto = " + txtmonto.getText() + ", impuestos = " + txtimpuesto.getText() + ", MontoTotal = " + txtmontototal.getText() + ", estado = 'F' where id = " + txtid.getText();
                    //Statement st=conect.createStatement();
                    //boolean rs=st.execute(sql);
                    //String query = "SELECT id FROM productos WHERE nombre = ?";
                    stmt = conn.prepareStatement(query);
                    //stmt.setString(1, nombreProducto);
                    rs = stmt.execute();
                    //if (rs.next()) {
                    //    idProducto = rs.getInt("id");
                   // }
                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        //if (rs != null) rs.close();
                        if (stmt != null) stmt.close();
                        if (conn != null) conn.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                System.exit(0);              
            }         
        }
    }//GEN-LAST:event_jButton5MouseClicked

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton5ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(SistemaFactura.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(SistemaFactura.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(SistemaFactura.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(SistemaFactura.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
               
            }
        });
        
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.toedter.calendar.JDateChooser datefactura;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tablaFactura;
    private javax.swing.JTextField txtapellido1factura;
    private javax.swing.JTextField txtapellido2factura;
    private javax.swing.JTextField txtestado;
    private javax.swing.JTextField txthorafactura;
    private javax.swing.JTextField txtid;
    private javax.swing.JTextField txtimpuesto;
    private javax.swing.JTextField txtmonto;
    private javax.swing.JTextField txtmontototal;
    private javax.swing.JTextField txtnombrefactura;
    // End of variables declaration//GEN-END:variables
}
