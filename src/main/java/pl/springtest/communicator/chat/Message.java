package pl.springtest.communicator.chat;

import pl.springtest.communicator.statement.ServerStatement;

import java.util.Objects;

/**
 * Message in one line format (all expect MESSAGE ended by ";" - MESSAGE ends by "\n"):
 * "VERSION_INFO:" + version (eg. "2.1.3;")
 * "CLIENT_NAME:" + name (eq. "Adam;")
 * "GROUP_ID:" + groupId (eq. "Group123;"
 * "EXTRA:" + extra info to server (eg. "SHUTDOWN;" - client is shutting down)
 * "MESSAGE:" + message from parameter
 * EXTRA list:
 * - SHUTDOWN - client is shutting down (since ver. 1.0.0)
 */
public class Message {
    private String version;
    private String clientName;
    private String groupID;
    private String extraInfo;
    private String message;
    public static long index; // index of printed message

    public Message(String version, String clientName, String groupID, String extraInfo, String message){
        this.version = version;
        this.clientName = clientName;
        this.groupID = groupID;
        this.extraInfo = extraInfo;
        this.message = message;
        //ServerStatement.Info("Created new message ("group: " + groupID + ", user: " + userName + ", message: " + printMessage() + ")");
    }

    /**
     * Prepare text message to one line format (all expect MESSAGE ended by ";" - MESSAGE ends by "\n"):
     * "VERSION_INFO:" + version (eg. "2.1.3;")
     * "CLIENT_NAME:" + name (eq. "Adam;")
     * "GROUP_ID:" + groupId (eq. "Group123;"
     * "EXTRA:" + extra info to server (eg. "SHUTDOWN;" - client is shutting down)
     * "MESSAGE:" + message from parameter
     * EXTRA list:
     * - SHUTDOWN - client is shutting down (since ver. 1.0.0)
     * @return prepared string with data
     */
    public String getPreparedMessage() {
        String preparedMessage = "VERSION_INFO:" + version + ";";
        preparedMessage += "CLIENT_NAME:" + clientName + ";";
        preparedMessage += "GROUP_ID:" + groupID + ";";
        preparedMessage += "EXTRA:" + extraInfo + ";";
        preparedMessage += "MESSAGE:" + message + "\n";

        return preparedMessage;
    }

    /**
     * Parse prepared message to object Message
     * @param preparedMessage - message with data from client
     */
    public Message (String preparedMessage) {
        int startIndex;
        int index = 0; // index at preparedMessage String

        ServerStatement.Info("Raw message: " + preparedMessage);

        if (preparedMessage == null) {
            setAllAsNull();
            ServerStatement.Error("Raw message - null message.", ServerStatement.NO_EXIT);
            return;
        }

        // VERSION_INFO
        index = checkKeyWord("VERSION_INFO", preparedMessage, index);
        if (index < 0) {
            this.setAllAsNull();
            ServerStatement.Error("Raw message - error in VERSION_INFO: " + preparedMessage, ServerStatement.NO_EXIT);
            return;
        }
        startIndex = index;
        index = this.checkFieldString(preparedMessage, index);
        if (index < 0) {
            this.setAllAsNull();
            ServerStatement.Error("Raw message - error in VERSION_INFO field: " + preparedMessage, ServerStatement.NO_EXIT);
            return;
        }
        version = preparedMessage.substring(startIndex, index);
        ++index; // drop ';' and go to next character

        // CLIENT_NAME
        index = checkKeyWord("CLIENT_NAME", preparedMessage, index);
        if (index < 0) {
            this.setAllAsNull();
            ServerStatement.Error("Raw message - error in CLIENT_NAME: " + preparedMessage, ServerStatement.NO_EXIT);
            return;
        }
        startIndex = index;
        index = this.checkFieldString(preparedMessage, index);
        if (index < 0) {
            this.setAllAsNull();
            ServerStatement.Error("Raw message - error in CLIENT_NAME field: " + preparedMessage, ServerStatement.NO_EXIT);
            return;
        }
        clientName = preparedMessage.substring(startIndex, index);
        ++index; // drop ';' and go to next character

        // GROUP_ID
        index = checkKeyWord("GROUP_ID", preparedMessage, index);
        if (index < 0) {
            this.setAllAsNull();
            ServerStatement.Error("Raw message - error in GROUP_ID: " + preparedMessage, ServerStatement.NO_EXIT);
            return;
        }
        startIndex = index;
        index = this.checkFieldString(preparedMessage, index);
        if (index < 0) {
            this.setAllAsNull();
            ServerStatement.Error("Raw message - error in GROUP_ID field: " + preparedMessage, ServerStatement.NO_EXIT);
            return;
        }
        groupID = preparedMessage.substring(startIndex, index);
        ++index; // drop ';' and go to next character

        // EXTRA
        index = checkKeyWord("EXTRA", preparedMessage, index);
        if (index < 0) {
            this.setAllAsNull();
            ServerStatement.Error("Raw message - error in EXTRA: " + preparedMessage, ServerStatement.NO_EXIT);
            return;
        }
        startIndex = index;
        index = this.checkFieldString(preparedMessage, index);
        if (index < 0) {
            extraInfo = "";
            index = startIndex;
        }
        else {
            extraInfo = preparedMessage.substring(startIndex, index);
        }
        ++index; // drop ';' and go to next character

        // MESSAGE
        index = checkKeyWord("MESSAGE", preparedMessage, index);
        if (index < 0) {
            this.setAllAsNull();
            ServerStatement.Error("Raw message - error in MESSAGE: " + preparedMessage, ServerStatement.NO_EXIT);
            return;
        }
        message = preparedMessage.substring(index, preparedMessage.length());
    }

    /**
     * Set all objects variables as null
     */
    private void setAllAsNull() {
        version = null;
        clientName = null;
        groupID = null;
        extraInfo = null;
        message = null;
    }

    /**
     * Check if keyWord is correct
     * @param keyWord - keyWord to find
     * @param preparedMessage - message to search
     * @param index - index of character at preparedMessages to start
     * @return -1 if keyWord is not correct, index of character at preparedMessage if keyWord is correct
     */
    private int checkKeyWord (String keyWord, String preparedMessage, int index) {
        String readKeyWord = new String("");

        while (index <= preparedMessage.length() && preparedMessage.charAt(index) != ':') {
            readKeyWord += Character.toString(preparedMessage.charAt(index));
            ++index;
        }
        if (index < preparedMessage.length() && readKeyWord.equals(keyWord))
            return ++index; // go to next character after ';'
        else
            return -1;
    }

    /**
     * read preparedMessage (to one of keyWord) to get field String
     * @param preparedMessage - message to search
     * @param startIndex - index of character at preparedMessages to start
     * @return end index of character at preparedMessages of field String
     */
    private int checkFieldString (String preparedMessage, int startIndex) {
        int endIndex = startIndex;

        if (preparedMessage.charAt(startIndex) == ';')
            return -1;
        while (endIndex <= preparedMessage.length() && preparedMessage.charAt(endIndex) != ';')
            ++endIndex;
        return endIndex;
    }

    /**
     * Print message locally
     * @return format "user (groupID): message"
     */
    public String printMessageLocally(){
        Message.index++;
        return clientName + " (" + groupID + "): " + message;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getUserName() {
        return clientName;
    }

    public void setUserName(String userName) {
        this.clientName = userName;
    }

    public String getGroupID() {
        return groupID;
    }

    public void setGroupID(String groupID) {
        this.groupID = groupID;
    }

    public String getExtraInfo() {
        return extraInfo;
    }

    public void setExtraInfo(String extraInfo) {
        this.extraInfo = extraInfo;
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
        return Objects.equals(version, message1.version) &&
                Objects.equals(clientName, message1.clientName) &&
                Objects.equals(groupID, message1.groupID) &&
                Objects.equals(extraInfo, message1.extraInfo) &&
                Objects.equals(message, message1.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(version, clientName, groupID, extraInfo, message);
    }
}
