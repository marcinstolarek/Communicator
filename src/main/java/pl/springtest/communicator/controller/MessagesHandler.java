package pl.springtest.communicator.controller;

import org.apache.catalina.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import pl.springtest.communicator.chat.ExtraInfo;
import pl.springtest.communicator.chat.Message;
import pl.springtest.communicator.socket.SocketClient;
import pl.springtest.communicator.statement.ServerStatement;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Reading message from clients
 */
@Controller
public class MessagesHandler {
    @Autowired
    private List<SocketClient> clientSockets;

    /**
     * Get message from clients and print it to ServerStatement
     * @return message to send queue
     */
    @Bean
    public Queue<Message> readWriteMessages() {
        Queue<Message> messageToSend = new LinkedList<>();
        ServerStatement.Info("Creating readWriteMessages bean");

        /**
         * Read message from clients
         * if SHUTDOWN extra info is inside, then close socket for that client
         * otherwise add message to list to send other clients
         */
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
                        for (SocketClient client : clientSockets) {
                            try {
                                messageRaw = new BufferedReader(new InputStreamReader(client.getSocket().getInputStream()));
                                if (!client.getSocket().isClosed() && messageRaw.ready()) {
                                    String inputString = null;
                                    inputString = messageRaw.readLine();
                                    if (inputString != null) {
                                        message = new Message(inputString);
                                        if (message.getMessage() != null) {
                                            client.setUserName(message.getUserName()); // save userName info
                                            client.setGroupID(message.getGroupID()); // save groupID info
                                            if (message.getExtraInfo().equals(ExtraInfo.SHUTDOWN.toString())) // client is shutting down
                                                removeIndex.add(clientSockets.indexOf(client));
                                            else if (message.getExtraInfo().equals(ExtraInfo.SHUTDOWN.toString())) // new connection - handshake
                                                ServerStatement.Info("New client: name: " + client.getUserName() + ", groupID: " + client.getGroupID() + ".");
                                            else {
                                                ServerStatement.Info("Message from client: " + message.printMessageLocally());
                                                synchronized (messageToSend) {
                                                    messageToSend.add(message); // add to queue to send to other clients
                                                    messageToSend.notify(); // wake up sending thread
                                                }
                                            }
                                        }
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

        /**
         * Send message to clients from other clients (with the same GROUP_ID)
         */
        class SendMessagesToClients extends Thread {
            @Override
            public void run() {
                PrintWriter outMessage = null;
                String groupID = null;
                String userName = null;

                this.setName("Thread-SendMessagesToClients");
                while (true) {
                    synchronized (messageToSend) {
                        try {
                            messageToSend.wait(); // wait for new message - then will be notified
                        } catch (InterruptedException e) {
                            ServerStatement.Error("InterruptedException in thread SendMessagesToClients", ServerStatement.NO_EXIT);
                        }
                        while (!messageToSend.isEmpty()) {
                            Message message = messageToSend.poll();
                            groupID = message.getGroupID();
                            userName = message.getUserName();
                            synchronized (clientSockets) {
                                for (SocketClient client : clientSockets) {
                                    if (client.getGroupID() == null || client.getUserName() == null) // not recognized client - break
                                        continue;
                                    // found addressee of message (same groupID or broadcast, other userName)- send to this client
                                    if ((client.getGroupID().equals(groupID) || groupID == "BROADCAST") && !client.getUserName().equals(userName)) {
                                        try {
                                            outMessage = new PrintWriter(client.getSocket().getOutputStream(), true);
                                            outMessage.write(message.getPreparedMessage());
                                            outMessage.flush();
                                            ServerStatement.Info("Send message: " + message.getPreparedMessage());
                                        } catch (IOException e) {
                                            ServerStatement.Error("IOException occurred while sending message to client", ServerStatement.NO_EXIT);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        NewMessages newMessages = new NewMessages();
        SendMessagesToClients sendMessages = new SendMessagesToClients();
        newMessages.start();
        sendMessages.start();

        return messageToSend;
    }
}