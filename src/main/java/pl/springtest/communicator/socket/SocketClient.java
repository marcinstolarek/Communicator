package pl.springtest.communicator.socket;

import java.net.Socket;
import java.util.Objects;

/**
 * Socket class with extra fields as userName and groupID - need to identify client
 */
public class SocketClient extends Socket {
    private Socket socket;
    private String userName;
    private String groupID;

    public SocketClient (Socket s) {
        this.socket = s;
        userName = null;
        groupID = null;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getGroupID() {
        return groupID;
    }

    public void setGroupID(String groupID) {
        this.groupID = groupID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SocketClient that = (SocketClient) o;
        return Objects.equals(userName, that.userName) &&
                Objects.equals(groupID, that.groupID) && super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode() * Objects.hash(userName, groupID);
    }
}
