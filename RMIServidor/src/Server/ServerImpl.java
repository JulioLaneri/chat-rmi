package Server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import Cliente.ICliente;
import Servidor.IServer;

public class ServerImpl extends UnicastRemoteObject implements IServer {

	private static final long serialVersionUID = 1L;
	private Map<String, ICliente> connectedClients;


	protected ServerImpl() throws RemoteException {
		connectedClients = new ConcurrentHashMap<>();

	}

	@Override
	public void broadcastMessage(String username, String message) throws RemoteException {
        // Recorrer la lista de clientes y enviar el mensaje a cada uno de ellos
        for (Map.Entry<String, ICliente> entry : connectedClients.entrySet()) {
            String clientUsername = entry.getKey();
            ICliente client = entry.getValue();

            try {
                // Llamar al método receiveMessage del cliente para enviar el mensaje
                client.receiveMessage(username + ": " + message);
            } catch (RemoteException e) {
                // Manejar excepciones de clientes desconectados si es necesario
                unregisterClient(clientUsername);
            }
        }
		
	}

	@Override
	public List<String> getConnectedClients() throws RemoteException {

		return new ArrayList<>(connectedClients.keySet());
	}


	@Override
	public void registerClient(String username, ICliente client) throws RemoteException {
	    // Verificar si el nombre de usuario ya está en uso
	    if (connectedClients.containsKey(username)) {
	        throw new RemoteException("El nombre de usuario ya está en uso.");
	    }

	    // Registrar al cliente
	    connectedClients.put(username, client);
	    // Almacena una referencia al cliente actual (objeto ICliente) en el servidor
	    connectedClients.put(username, client);
	    // Notificar a todos los clientes conectados sobre el nuevo cliente
	    broadcastMessage("Servidor", username + " se ha unido al chat.");
	}


	@Override
	public void unregisterClient(String username) throws RemoteException {
		// Eliminar al cliente de la lista de clientes conectados
        connectedClients.remove(username);
        // Notificar a todos los clientes conectados sobre la desconexión del cliente
        broadcastMessage("Servidor", username + " se ha desconectado del chat.");
		
	}

}
