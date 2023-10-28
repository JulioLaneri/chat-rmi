package Cliente;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ICliente extends Remote {
    void receiveMessage(String message) throws RemoteException;
}
