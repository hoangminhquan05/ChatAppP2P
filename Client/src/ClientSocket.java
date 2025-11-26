import javax.swing.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class ClientSocket {
    private Socket serverSocket;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private String username;
    private volatile boolean connected;
    private ChatUI chatUI;

    // Map lưu kết nối P2P với các user
    private ConcurrentHashMap<String, PeerConnection> peerConnections;

    public ClientSocket(String host, int port, String username, ChatUI chatUI) throws IOException {
        this.serverSocket = new Socket(host, port);
        this.output = new ObjectOutputStream(serverSocket.getOutputStream());
        this.input = new ObjectInputStream(serverSocket.getInputStream());
        this.username = username;
        this.chatUI = chatUI;
        this.peerConnections = new ConcurrentHashMap<>();
        this.connected = true;

        // Gửi tin nhắn login
        sendLogin();

        // Bắt đầu lắng nghe tin nhắn từ server
        startServerListener();
    }

    private void sendLogin() throws IOException {
        Message loginMsg = new Message(Message.Type.LOGIN, username, "login");
        output.writeObject(loginMsg);
        output.flush();
    }

    private void startServerListener() {
        Thread listenerThread = new Thread(() -> {
            while (connected) {
                try {
                    Message message = (Message) input.readObject();
                    handleServerMessage(message);
                } catch (IOException | ClassNotFoundException e) {
                    if (connected) {
                        System.err.println("Lỗi kết nối server: " + e.getMessage());
                        connected = false;
                    }
                }
            }
        });
        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    private void handleServerMessage(Message message) {
        switch (message.getType()) {
            case USER_ONLINE:
                // Cập nhật danh sách bạn bè online
                SwingUtilities.invokeLater(() -> {
                    if (chatUI != null) {
                        chatUI.addOrUpdateOnlineUser(message.getFromUser());
                    }
                });
                break;

            case USER_OFFLINE:
                // Cập nhật bạn bè offline
                SwingUtilities.invokeLater(() -> {
                    if (chatUI != null) {
                        chatUI.removeOnlineUser(message.getFromUser());
                    }
                });
                break;

            case GET_ONLINE_USERS:
                // Nhận danh sách user online từ server
                String[] onlineUsers = message.getContent().split(",");
                SwingUtilities.invokeLater(() -> {
                    if (chatUI != null) {
                        // Xóa tất cả bạn bè cũ
                        chatUI.clearAllFriends();

                        // Thêm bạn bè mới từ server
                        for (String user : onlineUsers) {
                            if (!user.isEmpty() && !user.equals(username)) {
                                chatUI.addOrUpdateOnlineUser(user);
                            }
                        }
                    }
                });
                break;

            case PEER_INFO:
                // Nhận thông tin peer để thiết lập kết nối P2P
                handlePeerInfo(message);
                break;

            case PEER_REQUEST:
                // Nhận yêu cầu kết nối P2P từ user khác
                handlePeerRequest(message);
                break;

            case ERROR:
                System.err.println("Lỗi từ server: " + message.getContent());
                break;

            default:
                System.out.println("Tin nhắn không xác định: " + message.getType());
        }
    }

    private void handlePeerInfo(Message message) {
        // message.getContent() có dạng "ip:port"
        String[] parts = message.getContent().split(":");
        if (parts.length == 2) {
            try {
                String peerIp = parts[0];
                int peerPort = Integer.parseInt(parts[1]);
                String peerUser = message.getFromUser();

                // Thiết lập kết nối P2P
                establishP2PConnection(peerUser, peerIp, peerPort);
            } catch (Exception e) {
                System.err.println("Lỗi xử lý peer info: " + e.getMessage());
            }
        }
    }

    private void handlePeerRequest(Message message) {
        String fromUser = message.getFromUser();
        int choice = JOptionPane.showConfirmDialog(
                chatUI,
                fromUser + " muốn kết nối chat với bạn. Chấp nhận?",
                "Yêu cầu kết nối",
                JOptionPane.YES_NO_OPTION
        );

        if (choice == JOptionPane.YES_OPTION) {
            try {
                // Gửi thông tin peer của mình cho người yêu cầu
                sendPeerInfo(fromUser);
            } catch (IOException e) {
                System.err.println("Lỗi gửi peer info: " + e.getMessage());
            }
        }
    }

    public void sendPeerRequest(String targetUser) throws IOException {
        Message request = new Message(Message.Type.PEER_REQUEST, username, "peer_request");
        request.setToUser(targetUser);
        output.writeObject(request);
        output.flush();
    }

    public void sendPeerInfo(String targetUser) throws IOException {
        // Lấy port mà client đang lắng nghe kết nối P2P
        int p2pPort = getP2PListenerPort();
        String localIp = InetAddress.getLocalHost().getHostAddress();
        String peerInfo = localIp + ":" + p2pPort;

        Message info = new Message(Message.Type.PEER_INFO, username, peerInfo);
        info.setToUser(targetUser);
        output.writeObject(info);
        output.flush();
    }

    private int getP2PListenerPort() {
        // Trả về port mà client lắng nghe kết nối P2P
        // Trong thực tế, bạn cần quản lý port này
        return 5000 + Math.abs(username.hashCode() % 1000);
    }

    private void establishP2PConnection(String peerUser, String peerIp, int peerPort) {
        try {
            PeerConnection peerConn = new PeerConnection(peerUser, peerIp, peerPort, chatUI);
            peerConnections.put(peerUser, peerConn);

            SwingUtilities.invokeLater(() -> {
                chatUI.showChatWithUser(peerUser);
            });

        } catch (IOException e) {
            System.err.println("Lỗi kết nối P2P với " + peerUser + ": " + e.getMessage());
        }
    }

    // Thêm phương thức này vào ClientSocket class
    public void sendP2PMessage(String targetUser, String content) {
        if (targetUser == null || content == null) {
            System.err.println("Target user or content is null");
            return;
        }

        PeerConnection peerConn = peerConnections.get(targetUser);
        if (peerConn != null && peerConn.isConnected()) {
            peerConn.sendMessage(content);
        } else {
            // Nếu chưa có kết nối P2P, yêu cầu qua server
            try {
                sendPeerRequest(targetUser);
                System.out.println("Đang yêu cầu kết nối P2P với " + targetUser + "...");

                // Trong demo, tạm thời hiển thị tin nhắn local
                SwingUtilities.invokeLater(() -> {
                    if (chatUI != null) {
                        chatUI.addSystemMessage("Đang kết nối với " + targetUser + "...");
                    }
                });

            } catch (IOException e) {
                System.err.println("Lỗi gửi yêu cầu kết nối: " + e.getMessage());
                SwingUtilities.invokeLater(() -> {
                    if (chatUI != null) {
                        chatUI.addSystemMessage("Lỗi kết nối với " + targetUser);
                    }
                });
            }
        }
    }
    public void disconnect() {
        connected = false;
        try {
            // Gửi tin nhắn logout
            Message logoutMsg = new Message(Message.Type.LOGOUT, username, "logout");
            output.writeObject(logoutMsg);
        } catch (IOException e) {
            // Ignore
        }

        // Đóng kết nối
        try { input.close(); } catch (IOException e) {}
        try { output.close(); } catch (IOException e) {}
        try { serverSocket.close(); } catch (IOException e) {}

        // Đóng tất cả kết nối P2P
        for (PeerConnection conn : peerConnections.values()) {
            conn.close();
        }
    }
}