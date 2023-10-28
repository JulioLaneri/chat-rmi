package Servidor;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import Cliente.ICliente;

public interface IServer extends Remote {
    void registerClient(String username, ICliente client) throws RemoteException;
    void unregisterClient(String username) throws RemoteException;
    void broadcastMessage(String username, String message) throws RemoteException;
    List<String> getConnectedClients() throws RemoteException;
}
