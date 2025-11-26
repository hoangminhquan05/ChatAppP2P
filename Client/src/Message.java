import java.io.Serializable;
import java.util.Date;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Type {
        // Tin nhắn hệ thống
        LOGIN, LOGOUT, GET_ONLINE_USERS, HEARTBEAT,
        // Signaling
        PEER_REQUEST, PEER_INFO, PEER_READY,
        // Tin nhắn chat
        TEXT, FILE,
        // Trạng thái
        USER_ONLINE, USER_OFFLINE, ERROR
    }

    private Type type;
    private String fromUser;
    private String toUser;
    private String content;
    private Date timestamp;
    private byte[] fileData;
    private String fileName;

    public Message() {}

    public Message(Type type, String fromUser, String content) {
        this.type = type;
        this.fromUser = fromUser;
        this.content = content;
        this.timestamp = new Date();
    }

    // Getters and Setters
    public Type getType() { return type; }
    public void setType(Type type) { this.type = type; }

    public String getFromUser() { return fromUser; }
    public void setFromUser(String fromUser) { this.fromUser = fromUser; }

    public String getToUser() { return toUser; }
    public void setToUser(String toUser) { this.toUser = toUser; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }

    public byte[] getFileData() { return fileData; }
    public void setFileData(byte[] fileData) { this.fileData = fileData; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    @Override
    public String toString() {
        return "Message{" + "type=" + type + ", from=" + fromUser +
                ", to=" + toUser + ", content=" + content + '}';
    }
}