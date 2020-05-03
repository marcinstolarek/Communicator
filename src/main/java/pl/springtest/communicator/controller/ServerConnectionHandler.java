package pl.springtest.communicator.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import pl.springtest.communicator.socket.SocketClient;
import pl.springtest.communicator.statement.ServerStatement;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

/**
 * Handler of server socket - accepting new clients
 */
@Controller
public class ServerConnectionHandler {
    public static final int MAX_CLIENTS = 10;
    @Autowired
    private ServerSocket serverSocket;

    /**
     * Create list with client connections
     */
    @Bean
    public List<SocketClient> clientSockets() {
        ServerStatement.Info("Creating clientSocket bean");
        List<SocketClient> clientList = new ArrayList<>();

        class NewConnections extends Thread {
            @Override
            public void run() {
                boolean error = false;
                int sizeOfClientList = 0;

                this.setName("Thread-NewConnections");
                while (true) {
                    if (clientList.size() < MAX_CLIENTS && !serverSocket.isClosed()) {
                        try {
                            error = false;
                            SocketClient newClient = new SocketClient(serverSocket.accept());
                            synchronized(clientList) {
                                clientList.add(newClient);
                                sizeOfClientList = clientList.size();
                            }
                        } catch (IOException e) {
                            ServerStatement.Error("Error while waiting for connection.", ServerStatement.NO_EXIT);
                            error = true;
                        } finally {
                            if (!error)
                                ServerStatement.Info("Successfully created new connection (" + sizeOfClientList + ") at port " + serverSocket.getLocalPort() + ".");
                        }
                    }
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        ; // do nothing
                    }
                }
            }
        }
        NewConnections newConnections = new NewConnections();
        newConnections.start();

        return clientList;
    }
}