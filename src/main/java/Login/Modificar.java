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

public class Modificar extends JDialog implements ActionListener {
    private static final long serialVersionUID = 1L;
    private JButton botonModificar;
    private JButton botonCancelar;
    private SistemaFactura sistemaFactura;
    private int filaSeleccionada;
    private JTextField text2; 
    private int idfactura;

    public Modificar(SistemaFactura parent, boolean modal, int filaSeleccionada, int idfactura, int cantidad) {
        super(parent, modal);
        this.sistemaFactura = parent;
        this.filaSeleccionada = filaSeleccionada;
        this.idfactura = idfactura;

        setLayout(null);
        setBounds(440, 10, 410, 250);
        
        JLabel label2 = new JLabel("Cantidad");
        label2.setBounds(50, 60, 200, 30);
        add(label2);
        
        text2 = new JTextField(String.valueOf(cantidad)); 
        text2.setBounds(150, 60, 200, 30);
        add(text2);
        
        botonModificar = new JButton("Modificar");
        botonModificar.setBounds(50, 120, 150, 40);
        add(botonModificar);

        botonCancelar = new JButton("Cancelar");
        botonCancelar.setBounds(200, 120, 150, 40);
        add(botonCancelar);

        botonModificar.addActionListener(this);
        botonCancelar.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
       if (e.getSource() == botonModificar) {
            try 
            {
               int cantidad = Integer.parseInt(text2.getText()); 
               sistemaFactura.modificarCantidadTabla(filaSeleccionada, cantidad, idfactura);
               setVisible(false);
            }
            catch (Exception ex)
            {
                 JOptionPane.showMessageDialog(this, "Numero no existe", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else if (e.getSource() == botonCancelar) {
            setVisible(false);
        }
    }
}
