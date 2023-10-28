package Client;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.swing.*;
import javax.swing.border.LineBorder;

import Cliente.ICliente;
import Servidor.IServer;

import utils.ConfigLoader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ChatClienteGUI2 {
    private static ICliente cliente;
    private static IServer servidor;
    private static List<String> mensajes = new ArrayList<>();
    private static JTextArea areaMensajes;
    private static JTextArea areaUsuarios;
    private static String nombreUsuario;
    private static File mensajesFile; 
    private static boolean conectado = false; 
    
    private static final Logger logger = LogManager.getLogger(ChatClienteGUI.class);

    public static void main(String[] args) {
        Properties properties = ConfigLoader.loadConfig("config.properties");
        int puerto = Integer.parseInt(properties.getProperty("server.port"));
        String serverIP = properties.getProperty("server.address");
        JFrame frame = new JFrame("Chat Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 600);

        // Crear el panel principal con un GridBagLayout
        JPanel panelPrincipal = new JPanel(new GridBagLayout());

        // Crear dos paneles para las columnas izquierda y derecha
        JPanel panelIzquierda = new JPanel(new GridBagLayout());

        JPanel panelDerecha = new JPanel(new BorderLayout()); // Usar BorderLayout en el panel derecho

        // Crear un tercer panel para la parte inferior
        JPanel panelInferior = new JPanel();

        // Colores que usarás
        Color blanco = Color.decode("#F0F0F5");
        Color negro = Color.decode("#17171F");

        // Configurar los colores de fondo para distinguir los paneles
        panelIzquierda.setBackground(blanco);
        panelIzquierda.setBorder(new LineBorder(negro));
        panelDerecha.setBackground(blanco);
        panelDerecha.setBorder(new LineBorder(negro));
        panelInferior.setBackground(blanco);
        panelInferior.setBorder(new LineBorder(negro));

        // Crear un campo de texto
        JLabel lblMsg = new JLabel("Mensaje: ");
        JTextField campoTexto = new JTextField(20); // 20 caracteres de ancho
        panelDerecha.add(campoTexto, BorderLayout.NORTH); // Agregar al norte

        JTextField campoMensaje = new JTextField(20); // 20 caracteres de ancho
        panelInferior.add(campoMensaje, BorderLayout.NORTH); // Agregar al norte

        // Crear un área de texto para mostrar la lista de usuarios
        JLabel titleConect = new JLabel("Lista de Conectados");
        areaUsuarios = new JTextArea(10, 20); // 10 filas y 20 columnas
        areaUsuarios.setEditable(false); // No editable por los usuarios
        JScrollPane scrollUsuarios = new JScrollPane(areaUsuarios);

        // Crear un área de texto para mostrar los mensajes
        JLabel titleMsg = new JLabel("Lista de Mensajes");
        areaMensajes = new JTextArea(10, 30); // 10 filas y 30 columnas
        areaMensajes.setEditable(false); // No editable por los usuarios
        JScrollPane scrollMensajes = new JScrollPane(areaMensajes);

        // Crear el archivo para almacenar mensajes
        mensajesFile = new File("mensajes.txt");

        // Cargar mensajes anteriores desde el archivo
        cargarMensajes();

        // Agregar un botón "Conectarse"
        JButton botonAgregar = new JButton("Conectarse");

        botonAgregar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!conectado) {
                    // Si no está conectado, realizar la conexión
                    nombreUsuario = campoTexto.getText(); // Obtener el nombre de usuario

                    if (!nombreUsuario.isEmpty()) {
                        try {
                            // Crear una instancia del cliente
                            cliente = new ClienteImpl();

                            // Obtener el registro RMI del servidor
                            Registry registry = LocateRegistry.getRegistry(serverIP, puerto);

                            // Buscar el servidor RMI por su nombre (debe coincidir con el nombre registrado en el servidor)
                            servidor = (IServer) registry.lookup("ChatServer");

                            // Verificar si el nombre de usuario ya está en uso
                            if (servidor.getConnectedClients().contains(nombreUsuario)) {
                                // Nombre de usuario en uso, muestra un mensaje de error o toma otras acciones según tus necesidades.
                                JOptionPane.showMessageDialog(frame, "El nombre de usuario ya está en uso.", "Error",
                                        JOptionPane.ERROR_MESSAGE);
                            } else {
                                // Llamar al método del servidor para registrar al cliente
                                servidor.registerClient(nombreUsuario, cliente);

                                // Cambiar el texto del botón a "Desconectarse"
                                botonAgregar.setText("Desconectarse");
                                conectado = true; // Establecer el estado de conexión a verdadero

                                // Inhabilitar el campo de texto
                                campoTexto.setEnabled(false);

                                // Mostrar los mensajes previamente almacenados
                                mostrarMensajes();
                                // Mostrar los clientes conectados
                                mostrarUsuariosConectados();
                            }

                        } catch (Exception ex) {
                        	logger.error(ex);
                            ex.printStackTrace();
                        }
                    } else {
                        // Manejar el caso en el que el nombre de usuario esté vacío
                        // Puedes mostrar un mensaje de error o tomar otras acciones según tus necesidades.
                    }
                } else {
                    // Si ya está conectado, realizar la desconexión
                    try {
                        // Llamar al método del servidor para desconectar al cliente
                        servidor.unregisterClient(nombreUsuario);

                        // Guardar mensajes en el archivo antes de desconectar
                        guardarMensajes();

                        // Cambiar el texto del botón a "Conectarse"
                        botonAgregar.setText("Conectarse");
                        conectado = false; // Establecer el estado de conexión a falso

                        // Habilitar el campo de texto nuevamente
                        campoTexto.setEnabled(true);

                        // Limpiar el área de mensajes
                        areaMensajes.setText("");

                        // Limpiar el nombre de usuario
                        nombreUsuario = "";

                        // Mostrar los clientes conectados
                        mostrarUsuariosConectados();

                        // Aquí puedes realizar otras acciones relacionadas con la desconexión si es necesario.

                    } catch (Exception ex) {
                    	logger.error(ex);
                        ex.printStackTrace();
                    }
                }
            }
        });

        JButton botonEnviar = new JButton("Send");
        botonEnviar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Obtener el mensaje ingresado
                String mensaje = campoMensaje.getText();
                campoMensaje.setText(""); // Limpiar el campo de mensaje

                // Enviar el mensaje al servidor
                if (conectado && cliente != null && !mensaje.isEmpty()) {
                    try {
                        Date fechaHoraActual = new Date();
                        SimpleDateFormat formatoFechaHora = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                        String fechaHoraFormateada = formatoFechaHora.format(fechaHoraActual);
                        String mensajeConFechaHora = "["+fechaHoraFormateada+"]" + " " + nombreUsuario + ": " + mensaje;

                        servidor.broadcastMessage(nombreUsuario, mensajeConFechaHora);

                        // Agregar el mensaje localmente y mostrarlo en el área de mensajes
                        agregarMensajeLocal(mensajeConFechaHora);
                    } catch (RemoteException ex) {
                    	logger.error(ex);
                        ex.printStackTrace();
                    }
                } else {
                    // Mostrar un mensaje de error si el cliente intenta enviar un mensaje sin estar conectado
                    JOptionPane.showMessageDialog(frame, "Debe estar conectado para enviar mensajes.", "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Crear panel interno para los botones
        JPanel panelBoton = new JPanel();
        panelBoton.add(botonAgregar);

        JPanel panelEnviar = new JPanel();
        panelEnviar.add(botonEnviar);

        // Agregar el panel interno con el botón y el área de texto al centro de panelDerecha
        panelDerecha.add(panelBoton, BorderLayout.CENTER);
        panelDerecha.add(scrollUsuarios, BorderLayout.EAST); // Lista de usuarios con desplazamiento
        panelInferior.add(panelEnviar, BorderLayout.NORTH);

        GridBagConstraints gbcScrollMensajes = new GridBagConstraints();
        gbcScrollMensajes.gridx = 0;
        gbcScrollMensajes.gridy = 0;
        gbcScrollMensajes.weightx = 1.0;
        gbcScrollMensajes.weighty = 1.0;
        gbcScrollMensajes.fill = GridBagConstraints.BOTH;

        panelIzquierda.add(scrollMensajes, gbcScrollMensajes);

        // Crear restricciones para el panel superior (paneles izquierdo y derecho)
        GridBagConstraints gbcSuperior = new GridBagConstraints();
        gbcSuperior.gridx = 0;
        gbcSuperior.gridy = 0;
        gbcSuperior.weightx = 1.0;
        gbcSuperior.weighty = 0.7;
        gbcSuperior.fill = GridBagConstraints.BOTH;
        panelPrincipal.add(panelIzquierda, gbcSuperior);

        gbcSuperior.gridx = 1;
        panelPrincipal.add(panelDerecha, gbcSuperior);

        // Crear restricciones para el panel inferior
        GridBagConstraints gbcInferior = new GridBagConstraints();
        gbcInferior.gridx = 0;
        gbcInferior.gridy = 1;
        gbcInferior.gridwidth = 2;
        gbcInferior.weightx = 1.0;
        gbcInferior.weighty = 0.3;
        gbcInferior.fill = GridBagConstraints.BOTH;
        panelPrincipal.add(panelInferior, gbcInferior);

        // Agregar el panel principal al centro de la ventana
        frame.add(panelPrincipal, BorderLayout.CENTER);

        frame.setVisible(true);
    }



    private static void cargarMensajes() {
        try (BufferedReader reader = new BufferedReader(new FileReader(mensajesFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                mensajes.add(line);
            }
        } catch (IOException e) {
        	logger.error(e);
        }
    }

    // Método para guardar mensajes en el archivo
    private static void guardarMensajes() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(mensajesFile))) {
            for (String mensaje : mensajes) {
                writer.write(mensaje);
                writer.newLine();
            }
        } catch (IOException e) {
        	logger.error(e);
        }
    }

    // Método para mostrar los mensajes en el área de mensajes
    private static void mostrarMensajes() {
        areaMensajes.setText(""); // Limpiar el área de mensajes
        for (String msg : mensajes) {
            areaMensajes.append(msg + "\n");
        }
    }

    // Método para mostrar los usuarios conectados
    private static void mostrarUsuariosConectados() {
        try {
            areaUsuarios.setText(""); // Limpiar el área de texto

            for (String usuario : servidor.getConnectedClients()) {
                areaUsuarios.append(usuario + "\n");
            }
        } catch (RemoteException ex) {
        	logger.error(ex);
            ex.printStackTrace();
        }
    }

    // Método para agregar mensajes al área de mensajes
    private static void agregarMensajeLocal(String mensaje) {
        mensajes.add(mensaje);

        // Mostrar el mensaje en el área de mensajes
        mostrarMensajes();

        // Actualizar el archivo de mensajes
        guardarMensajes();
    }
    
    
    
}



