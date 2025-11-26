import javax.swing.*;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class PeerConnection {
    private Socket peerSocket;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private String peerUser;
    private ChatUI chatUI;
    private volatile boolean connected;

    public PeerConnection(String peerUser, String peerIp, int peerPort, ChatUI chatUI) throws IOException {
        this.peerUser = peerUser;
        this.chatUI = chatUI;

        // Kết nối đến peer
        this.peerSocket = new Socket(peerIp, peerPort);
        this.output = new ObjectOutputStream(peerSocket.getOutputStream());
        this.input = new ObjectInputStream(peerSocket.getInputStream());
        this.connected = true;

        // Bắt đầu lắng nghe tin nhắn từ peer
        startPeerListener();
    }

    // Constructor cho phía lắng nghe
    public PeerConnection(Socket peerSocket, String peerUser, ChatUI chatUI) throws IOException {
        this.peerSocket = peerSocket;
        this.peerUser = peerUser;
        this.chatUI = chatUI;
        this.output = new ObjectOutputStream(peerSocket.getOutputStream());
        this.input = new ObjectInputStream(peerSocket.getInputStream());
        this.connected = true;

        startPeerListener();
    }

    private void startPeerListener() {
        Thread listenerThread = new Thread(() -> {
            while (connected) {
                try {
                    Message message = (Message) input.readObject();
                    handlePeerMessage(message);
                } catch (IOException | ClassNotFoundException e) {
                    if (connected) {
                        System.err.println("Lỗi kết nối P2P với " + peerUser + ": " + e.getMessage());
                        connected = false;

                        SwingUtilities.invokeLater(() -> {
                            chatUI.addSystemMessage(peerUser + " đã ngắt kết nối");
                        });
                    }
                }
            }
        });
        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    private void handlePeerMessage(Message message) {
        if (message.getType() == Message.Type.TEXT) {
            SwingUtilities.invokeLater(() -> {
                chatUI.addIncomingMessage(peerUser, message.getContent());
            });
        }
    }

    public void sendMessage(String content) {
        try {
            Message message = new Message(Message.Type.TEXT, "", content);
            output.writeObject(message);
            output.flush();

            SwingUtilities.invokeLater(() -> {
                chatUI.addOutgoingMessage(content);
            });

        } catch (IOException e) {
            System.err.println("Lỗi gửi tin nhắn P2P: " + e.getMessage());
            connected = false;
        }
    }

    public boolean isConnected() {
        return connected && peerSocket != null && !peerSocket.isClosed();
    }

    public void close() {
        connected = false;
        try { input.close(); } catch (IOException e) {}
        try { output.close(); } catch (IOException e) {}
        try { peerSocket.close(); } catch (IOException e) {}
    }
}