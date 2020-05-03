package pl.springtest.communicator.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import pl.springtest.communicator.chat.Message;
import pl.springtest.communicator.statement.ServerStatement;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Queue;

/**
 * Socket server handler - create and close server socket
 */
@Controller
public class ServerSocketHandler {
    @Autowired
    private Queue<Message> readWriteMessages;

    /**
     * Create socket for server at specified port (1234)
     * Close socket when application is being shutdown
     * @return new socket
     */
    @Bean
    public ServerSocket serverSocket() {
        int port = 1234;
        ServerSocket newServerSocket = null;

        ServerStatement.Info("Creating socketServer bean");
        Message.index = 0; // initialize index of messages

        try {
            newServerSocket = new ServerSocket(port);
        } catch(IOException e) {
            ServerStatement.Error("Cannot create server socket at port " + port + ".", ServerStatement.DO_EXIT);
        } finally {
            ServerStatement.Info("Successfully created server socket at port " + newServerSocket.getLocalPort() + ".");
        }

        // closing socket when application is being shutdown
        ServerSocket finalNewServerSocket = newServerSocket;
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                ServerStatement.Info("CTRL+C");
                // TODO - send info to clients to shutdown them
                try {
                    finalNewServerSocket.close();
                } catch (IOException e) {
                    ServerStatement.Error("IOException when closing socket", ServerStatement.NO_EXIT);
                } finally {
                    ServerStatement.Info("Socket is closed: " + finalNewServerSocket.isClosed());
                }
            }
        });

        return newServerSocket;
    }
}
