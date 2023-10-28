package Client;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import Cliente.ICliente;

public class ClienteImpl extends UnicastRemoteObject implements ICliente{

	private static final long serialVersionUID = 1L;


	protected ClienteImpl() throws RemoteException {
		super();

	}

	@Override
	public void receiveMessage(String message) throws RemoteException {
		System.out.println(message);
	}

}
