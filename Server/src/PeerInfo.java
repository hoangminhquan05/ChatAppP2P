import java.net.InetAddress;

public class PeerInfo {
    private String username;
    private InetAddress ipAddress;
    private int port;
    private long lastHeartbeat;
    private boolean online;

    public PeerInfo(String username, InetAddress ipAddress, int port) {
        this.username = username;
        this.ipAddress = ipAddress;
        this.port = port;
        this.lastHeartbeat = System.currentTimeMillis();
        this.online = true;
    }

    // Getters and Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public InetAddress getIpAddress() { return ipAddress; }
    public void setIpAddress(InetAddress ipAddress) { this.ipAddress = ipAddress; }

    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }

    public long getLastHeartbeat() { return lastHeartbeat; }
    public void setLastHeartbeat(long lastHeartbeat) { this.lastHeartbeat = lastHeartbeat; }

    public boolean isOnline() { return online; }
    public void setOnline(boolean online) { this.online = online; }

    public void updateHeartbeat() {
        this.lastHeartbeat = System.currentTimeMillis();
        this.online = true;
    }

    public boolean isTimedOut(long timeout) {
        return (System.currentTimeMillis() - lastHeartbeat) > timeout;
    }

    @Override
    public String toString() {
        return username + " [" + ipAddress + ":" + port + "] - " +
                (online ? "ONLINE" : "OFFLINE");
    }
}