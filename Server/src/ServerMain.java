import java.io.IOException;

public class ServerMain {
    public static void main(String[] args) {
        try {
            int port = ServerUtils.getServerPort();
            SignalingServer server = new SignalingServer(port);

            // Thêm shutdown hook để dừng server gracefully
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\nĐang dừng server...");
                server.stop();
            }));

            // Start server
            server.start();

        } catch (IOException e) {
            ServerUtils.error("Lỗi khởi động server: " + e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            ServerUtils.error("Lỗi không xác định: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}