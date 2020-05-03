package pl.springtest.communicator.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import pl.springtest.communicator.chat.Message;
import pl.springtest.communicator.statement.ServerStatement;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Reading message from clients
 */
@Controller
public class MessagesHandler {
    @Autowired
    private List<Socket> clientSockets;

    /**
     * Get message from clients and print it to ServerStatement
     */
    @Bean
    public void readMessages() {
        ServerStatement.Info("Creating readMessage bean");

        class NewMessages extends Thread {
            @Override
            public void run() {
                BufferedReader messageRaw = null;
                Message message = null;
                List<Integer> removeIndex = new ArrayList();

                this.setName("Thread-NewMessages");
                while (true) {
                    synchronized(clientSockets) {
                        removeIndex.clear(); // clear list at the beginning
                        for (Socket client : clientSockets) {
                            try {
                                messageRaw = new BufferedReader(new InputStreamReader(client.getInputStream()));
                                if (!client.isClosed() && messageRaw.ready()) {
                                    String inputString = null;
                                    inputString = messageRaw.readLine();
                                    if (inputString != null) {
                                        message = new Message(inputString);
                                        if (message.getExtraInfo().equals("SHUTDOWN")) // client is shutting down
                                            removeIndex.add(clientSockets.indexOf(client));
                                        else
                                            ServerStatement.Info("Message from client: " + message.printMessageLocally());
                                    }
                                }
                            } catch (IOException e) {
                                ServerStatement.Error("Error while reading message from client.", ServerStatement.NO_EXIT);
                                //ServerStatement.Info("Client " + clientSockets.indexOf(client) + " is being removed");
                                //clientSockets.remove(client);
                            }
                        }
                        for (int index = removeIndex.size() - 1; index >= 0; index--) {
                            ServerStatement.Info("Closing connection (" + (removeIndex.get(index) + 1) + ").");
                            try {
                                clientSockets.get(removeIndex.get(index)).close(); // close connection
                            } catch (IOException e) {
                                ServerStatement.Error("Cannot close connection", ServerStatement.NO_EXIT);
                            }
                            clientSockets.remove(removeIndex.get(index)); // remove from list
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
        NewMessages newMessages = new NewMessages();
        newMessages.start();
    }
}