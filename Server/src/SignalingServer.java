import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class SignalingServer {
    private ServerSocket serverSocket;
    private ConcurrentHashMap<String, PeerInfo> onlinePeers;
    private ConcurrentHashMap<String, ClientHandler> clientHandlers;
    private ExecutorService threadPool;
    private volatile boolean running;
    private HeartbeatMonitor heartbeatMonitor;

    public SignalingServer(int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.onlinePeers = new ConcurrentHashMap<>();
        this.clientHandlers = new ConcurrentHashMap<>();
        this.threadPool = Executors.newCachedThreadPool();
        this.running = true;
        this.heartbeatMonitor = new HeartbeatMonitor();

        ServerUtils.info("Signaling Server khởi động trên port " + port);
    }

    public void start() {
        // Bắt đầu heartbeat monitor
        new Thread(heartbeatMonitor).start();

        // Chấp nhận kết nối từ client
        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                handleNewConnection(clientSocket);
            } catch (IOException e) {
                if (running) {
                    ServerUtils.error("Lỗi chấp nhận kết nối: " + e.getMessage());
                }
            }
        }
    }

    private void handleNewConnection(Socket clientSocket) {
        try {
            ClientHandler clientHandler = new ClientHandler(clientSocket, this);
            threadPool.execute(clientHandler);
            ServerUtils.info("Client kết nối: " + clientSocket.getInetAddress());
        } catch (IOException e) {
            ServerUtils.error("Lỗi xử lý kết nối mới: " + e.getMessage());
            try {
                clientSocket.close();
            } catch (IOException ex) {
                // Ignore
            }
        }
    }

    // Thêm peer vào danh sách online
    public synchronized void addOnlinePeer(String username, InetAddress ipAddress, int port, ClientHandler handler) {
        PeerInfo peerInfo = new PeerInfo(username, ipAddress, port);
        onlinePeers.put(username, peerInfo);
        clientHandlers.put(username, handler);

        ServerUtils.info("User online: " + username + " - " + onlinePeers.size() + " users online");

        // Thông báo cho tất cả user khác
        broadcastUserOnline(username);

        // Gửi danh sách user online cho user mới
        sendOnlineUsersToUser(username);
    }

    // Xóa peer khỏi danh sách online
    public synchronized void removeOnlinePeer(String username) {
        onlinePeers.remove(username);
        clientHandlers.remove(username);

        ServerUtils.info("User offline: " + username + " - " + onlinePeers.size() + " users online");

        // Thông báo cho tất cả user khác
        broadcastUserOffline(username);
    }

    // Lấy thông tin peer
    public PeerInfo getPeerInfo(String username) {
        return onlinePeers.get(username);
    }

    // Gửi tin nhắn đến user cụ thể
    public void sendMessageToUser(String targetUser, Object message) {
        ClientHandler handler = clientHandlers.get(targetUser);
        if (handler != null && handler.isConnected()) {
            handler.sendMessage(message);
        }
    }

    // Broadcast user online
    private void broadcastUserOnline(String username) {
        Message onlineMsg = new Message(Message.Type.USER_ONLINE, username, "online");
        broadcastMessage(onlineMsg, username); // Không gửi cho chính user đó
    }

    // Broadcast user offline
    private void broadcastUserOffline(String username) {
        Message offlineMsg = new Message(Message.Type.USER_OFFLINE, username, "offline");
        broadcastMessage(offlineMsg, username);
    }

    // Broadcast tin nhắn đến tất cả user (trừ excludeUser)
    private void broadcastMessage(Message message, String excludeUser) {
        for (String username : clientHandlers.keySet()) {
            if (!username.equals(excludeUser)) {
                sendMessageToUser(username, message);
            }
        }
    }

    // Gửi danh sách user online cho user mới
// Gửi danh sách user online cho user mới
    private void sendOnlineUsersToUser(String targetUser) {
        StringBuilder usersList = new StringBuilder();
        for (String username : onlinePeers.keySet()) {
            if (!username.equals(targetUser)) {
                if (usersList.length() > 0) {
                    usersList.append(",");
                }
                usersList.append(username);
            }
        }

        Message usersMsg = new Message(Message.Type.GET_ONLINE_USERS, "Server", usersList.toString());
        sendMessageToUser(targetUser, usersMsg);

        ServerUtils.info("Gửi danh sách " + onlinePeers.size() + " users đến " + targetUser);
    }

    // Xử lý yêu cầu kết nối P2P
    public void handlePeerRequest(String fromUser, String toUser) {
        PeerInfo targetPeer = onlinePeers.get(toUser);
        if (targetPeer != null) {
            // Gửi yêu cầu kết nối đến target user
            Message requestMsg = new Message(Message.Type.PEER_REQUEST, fromUser, "peer_request");
            sendMessageToUser(toUser, requestMsg);
            ServerUtils.info("Gửi yêu cầu P2P từ " + fromUser + " đến " + toUser);
        } else {
            // User không online
            Message errorMsg = new Message(Message.Type.ERROR, "Server", "User " + toUser + " không online");
            sendMessageToUser(fromUser, errorMsg);
        }
    }

    // Xử lý thông tin peer để thiết lập P2P
    public void handlePeerInfo(String fromUser, String toUser, String peerInfo) {
        // Chuyển thông tin peer đến user đích
        Message peerInfoMsg = new Message(Message.Type.PEER_INFO, fromUser, peerInfo);
        sendMessageToUser(toUser, peerInfoMsg);
        ServerUtils.info("Chuyển thông tin peer từ " + fromUser + " đến " + toUser);
    }

    // Cập nhật heartbeat
    public void updateHeartbeat(String username) {
        PeerInfo peer = onlinePeers.get(username);
        if (peer != null) {
            peer.updateHeartbeat();
        }
    }

    // Dừng server
    public void stop() {
        running = false;
        try {
            serverSocket.close();
        } catch (IOException e) {
            // Ignore
        }

        threadPool.shutdown();
        heartbeatMonitor.stop();

        // Đóng tất cả kết nối client
        for (ClientHandler handler : clientHandlers.values()) {
            handler.disconnect();
        }

        ServerUtils.info("Signaling Server đã dừng");
    }

    // Lớp monitor heartbeat
    private class HeartbeatMonitor implements Runnable {
        private volatile boolean monitoring = true;

        @Override
        public void run() {
            ServerUtils.info("Heartbeat Monitor started");

            while (monitoring) {
                try {
                    Thread.sleep(10000); // Kiểm tra mỗi 10 giây
                    checkTimeouts();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }

            ServerUtils.info("Heartbeat Monitor stopped");
        }

        private void checkTimeouts() {
            long timeout = ServerUtils.getHeartbeatTimeout();
            List<String> timedOutUsers = new ArrayList<>();

            for (Map.Entry<String, PeerInfo> entry : onlinePeers.entrySet()) {
                if (entry.getValue().isTimedOut(timeout)) {
                    timedOutUsers.add(entry.getKey());
                }
            }

            for (String username : timedOutUsers) {
                ServerUtils.warn("User timeout: " + username);
                removeOnlinePeer(username);
            }
        }

        public void stop() {
            monitoring = false;
        }
    }
}