package pl.springtest.communicator.chat;

import pl.springtest.communicator.statement.ServerStatement;

import java.util.Objects;

/**
 * Message in communicator
 */
public class Message {
    private String userName;
    private String message;
    public static long index; // index of printed message

    public Message(String userName, String message){
        this.userName = userName;
        this.message = message;
        //ServerStatement.Info("Created new message (user: " + userName + ", message: " + printMessage() + ")");
    }

    /**
     * Print typed message
     * @return format "user: message"
     */
    public String printMessage(){
        Message.index++;
        return index + ". " + userName + ": " + message;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message1 = (Message) o;
        return Objects.equals(userName, message1.userName) &&
                Objects.equals(message, message1.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userName, message);
    }
}
