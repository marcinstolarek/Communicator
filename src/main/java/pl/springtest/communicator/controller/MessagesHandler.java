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

                this.setName("Thread-NewMessages");
                while (true) {
                    synchronized(clientSockets) {
                        for (Socket client : clientSockets) {
                            try {
                                messageRaw = new BufferedReader(new InputStreamReader(client.getInputStream()));
                                if (!client.isClosed() && messageRaw.ready()) {
                                    String inputString = null;
                                    inputString = messageRaw.readLine();
                                    if (inputString != null) {
                                        message = new Message("Client", inputString);
                                        ServerStatement.Info("Message from client: " + message.printMessage());
                                    }
                                }
                            } catch (IOException e) {
                                ServerStatement.Error("Error while reading message from client.", ServerStatement.NO_EXIT);
                                ServerStatement.Info("Client " + clientSockets.indexOf(client) + " is being removed");
                                clientSockets.remove(client);
                            }
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