package Server;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Properties;

import Servidor.IServer;

import utils.ConfigLoader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServerChat {
    private static final Logger logger = LogManager.getLogger(ServerChat.class);

	public static void main(String[] args) {
        Properties properties = ConfigLoader.loadConfig("config.properties");

        int puerto = Integer.parseInt(properties.getProperty("server.port"));
        try {
            IServer server = new ServerImpl();
            Registry registry = LocateRegistry.createRegistry(puerto);
            registry.rebind("ChatServer", server);
            System.out.println("Servidor.....");
        } catch (Exception e) {
            System.err.println("ChatServer exception:");
            e.printStackTrace();
            logger.error(e);
        }

	}

}
