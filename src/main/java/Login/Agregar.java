package Login;

import javax.swing.JLabel;
import javax.swing.JDialog;
import javax.swing.JButton;
import javax.swing.JTextField;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

public class Agregar extends JDialog implements ActionListener {
    private static final long serialVersionUID = 1L;
    private JButton botonAgregar;
    private JButton botonCancelar;
    private JTextField text2;
    private JTextField text3;
    private SistemaFactura sistemaFactura;
    private JTextField text1;
    private int ultimaLinea = 0;

    public Agregar(SistemaFactura parent, boolean modal, int fila) {
        super(parent, modal);
        this.sistemaFactura = parent;

        setLayout(null);
        setBounds(440, 10, 410, 350);

        JLabel label1 = new JLabel("Linea");
        label1.setBounds(50, 60, 200, 30);
        add(label1);
        
        fila = fila + 1;
        text1 = new JTextField(String.valueOf(fila)); 
        text1.setBounds(150, 60, 200, 30);
        text1.setEnabled(false);

        add(text1);
        
        JLabel label2 = new JLabel("Cantidad");
        label2.setBounds(50, 120, 200, 30);
        add(label2);
        
        text2 = new JTextField("");
        text2.setBounds(150, 120, 200, 30);
        add(text2);
        
        JLabel label3 = new JLabel("Producto");
        label3.setBounds(50, 180, 200, 30);
        add(label3);

        text3 = new JTextField("");
        text3.setBounds(150, 180, 200, 30);
        add(text3);
        
        botonAgregar = new JButton("Agregar");
        botonAgregar.setBounds(50, 240, 150, 40);
        add(botonAgregar);

        botonCancelar = new JButton("Cancelar");
        botonCancelar.setBounds(200, 240, 150, 40);
        add(botonCancelar);

        botonAgregar.addActionListener(this);
        botonCancelar.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == botonAgregar) {
            int linea = Integer.parseInt(text1.getText());
            String cantidad = text2.getText();
            String nombreProducto = text3.getText();
            int idProducto = consultarIdProducto(nombreProducto);
            if (idProducto >= 0){
                double monto = consultarMontoProducto(idProducto);
                sistemaFactura.agregarEntradaTabla(linea, cantidad, idProducto, nombreProducto, monto);
                sistemaFactura.actualizarEstado("Pendiente");
                setVisible(false);
            }   
            else 
            {
               JOptionPane.showMessageDialog(this, "El producto no existe", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else if (e.getSource() == botonCancelar) {
            setVisible(false);
        }
    }

    
    private int consultarIdProducto(String nombreProducto) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        int idProducto = -1;

        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/crudpulperia", "root","");
            String sql = "SELECT id FROM productos WHERE nombre = ?";
            statement = connection.prepareStatement(sql);
            statement.setString(1, nombreProducto);
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                idProducto = resultSet.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (resultSet != null) resultSet.close();
                if (statement != null) statement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return idProducto;
    }

    private double consultarMontoProducto(int idProducto) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        double precioProducto = -1;

        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/crudpulperia", "root","");
            String sql = "SELECT precio FROM productos WHERE id = ?";
            statement = connection.prepareStatement(sql);
            statement.setInt(1, idProducto);
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                precioProducto = resultSet.getDouble("precio");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (resultSet != null) resultSet.close();
                if (statement != null) statement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return precioProducto;
    }
}
