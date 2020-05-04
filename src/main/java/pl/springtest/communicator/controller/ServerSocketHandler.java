package pl.springtest.communicator.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import pl.springtest.communicator.chat.Message;
import pl.springtest.communicator.info.AppInfo;
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
                // sending info to all clients about shutting down (broadcast)
                Message shutdownMessage = new Message(AppInfo.VERSION_INFO, "Communicator Server", "BROADCAST", "SHUTDOWN", "SERVER IS GOING DOWN");
                synchronized (readWriteMessages) {
                    readWriteMessages.add(shutdownMessage);
                    readWriteMessages.notify(); // wake up sending thread
                }
                // waiting for empty write buffer
                for (int i = 0; i < 5; i++) {
                    if (readWriteMessages.isEmpty())
                        break;
                    try {
                        this.sleep(100);
                    } catch (InterruptedException e) {
                        ServerStatement.Error("InterruptedException error while sleeping in ShutdownHook.", ServerStatement.NO_EXIT);
                    }
                }

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
