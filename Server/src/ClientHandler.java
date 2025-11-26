import java.io.*;
import java.net.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private SignalingServer server;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private String username;
    private AtomicBoolean connected;

    public ClientHandler(Socket socket, SignalingServer server) throws IOException {
        this.clientSocket = socket;
        this.server = server;
        this.output = new ObjectOutputStream(socket.getOutputStream());
        this.input = new ObjectInputStream(socket.getInputStream());
        this.connected = new AtomicBoolean(true);
    }

    @Override
    public void run() {
        try {
            while (connected.get()) {
                Object obj = input.readObject();
                if (obj instanceof Message) {
                    handleMessage((Message) obj);
                }
            }
        } catch (EOFException e) {
            // Client disconnect bình thường
        } catch (IOException | ClassNotFoundException e) {
            if (connected.get()) {
                ServerUtils.error("Lỗi xử lý client " + username + ": " + e.getMessage());
            }
        } finally {
            disconnect();
        }
    }

    private void handleMessage(Message message) {
        switch (message.getType()) {
            case LOGIN:
                handleLogin(message);
                break;

            case LOGOUT:
                handleLogout();
                break;

            case HEARTBEAT:
                handleHeartbeat();
                break;

            case PEER_REQUEST:
                handlePeerRequest(message);
                break;

            case PEER_INFO:
                handlePeerInfo(message);
                break;

            case GET_ONLINE_USERS:
                handleGetOnlineUsers();
                break;

            default:
                ServerUtils.warn("Loại tin nhắn không xác định: " + message.getType());
        }
    }

    private void handleLogin(Message message) {
        this.username = message.getFromUser();

        // Lấy port mà client sẽ lắng nghe kết nối P2P
        int p2pPort = extractP2PPort(message.getContent());
        if (p2pPort == -1) {
            p2pPort = 5000 + Math.abs(username.hashCode() % 1000); // Port mặc định
        }

        // Thêm vào danh sách online
        server.addOnlinePeer(username, clientSocket.getInetAddress(), p2pPort, this);

        // Gửi xác nhận login
        Message response = new Message(Message.Type.LOGIN, "Server", "login_success");
        sendMessage(response);

        ServerUtils.info("User " + username + " đã đăng nhập");
    }

    private int extractP2PPort(String content) {
        try {
            // content có thể chứa port, ví dụ: "login:5001"
            if (content.contains(":")) {
                String[] parts = content.split(":");
                if (parts.length > 1) {
                    return Integer.parseInt(parts[1]);
                }
            }
        } catch (NumberFormatException e) {
            // Ignore
        }
        return -1;
    }

    private void handleLogout() {
        ServerUtils.info("User " + username + " đăng xuất");
        disconnect();
    }

    private void handleHeartbeat() {
        server.updateHeartbeat(username);
        // Có thể gửi response heartbeat nếu cần
    }

    private void handlePeerRequest(Message message) {
        String targetUser = message.getToUser();
        if (targetUser != null && !targetUser.isEmpty()) {
            server.handlePeerRequest(username, targetUser);
        } else {
            Message errorMsg = new Message(Message.Type.ERROR, "Server", "Target user không hợp lệ");
            sendMessage(errorMsg);
        }
    }

    private void handlePeerInfo(Message message) {
        String targetUser = message.getToUser();
        String peerInfo = message.getContent();

        if (targetUser != null && !targetUser.isEmpty() && peerInfo != null && !peerInfo.isEmpty()) {
            server.handlePeerInfo(username, targetUser, peerInfo);
        } else {
            Message errorMsg = new Message(Message.Type.ERROR, "Server", "Thông tin peer không hợp lệ");
            sendMessage(errorMsg);
        }
    }

    private void handleGetOnlineUsers() {
        // Server tự động gửi danh sách online users khi login
        // Có thể implement thêm nếu cần
    }

    public void sendMessage(Object message) {
        if (connected.get()) {
            try {
                output.writeObject(message);
                output.flush();
            } catch (IOException e) {
                ServerUtils.error("Lỗi gửi tin nhắn đến " + username + ": " + e.getMessage());
                disconnect();
            }
        }
    }

    public void disconnect() {
        if (connected.compareAndSet(true, false)) {
            try {
                if (username != null) {
                    server.removeOnlinePeer(username);
                }

                input.close();
                output.close();
                clientSocket.close();

                ServerUtils.info("Client disconnected: " + username);
            } catch (IOException e) {
                // Ignore khi đang disconnect
            }
        }
    }

    public boolean isConnected() {
        return connected.get() && !clientSocket.isClosed();
    }

    public String getUsername() {
        return username;
    }
}