import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

public class ChatUI extends JFrame {
    // Colors
    private final Color PRIMARY_COLOR = new Color(0, 153, 255);
    private final Color SECONDARY_COLOR = new Color(240, 242, 245);
    private final Color MESSAGE_SENT = new Color(0, 153, 255);
    private final Color MESSAGE_RECEIVED = new Color(229, 229, 229);

    // Components
    private JPanel mainPanel, leftPanel, centerPanel, rightPanel;
    private JTextField searchField;
    private JScrollPane friendsScrollPane, chatScrollPane;
    private JPanel friendsPanel, chatPanel;
    private JTextArea messageArea;
    private JButton sendButton, attachButton, emojiButton, fileButton, voiceButton;

    // Data
    private List<Friend> friends;
    private Friend selectedFriend;
    private ClientSocket clientSocket;
    private String currentUser;
    private Map<String, List<ChatMessage>> chatHistory;

    public ChatUI(String username, ClientSocket clientSocket) {
        this.currentUser = username;
        this.clientSocket = clientSocket;
        this.chatHistory = new HashMap<>();
        this.friends = new ArrayList<>();

        initializeComponents();
        initializeUI(username);
        setupEventListeners();

        // Load online users after UI is ready
        SwingUtilities.invokeLater(this::loadOnlineUsers);
    }

    private void initializeComponents() {
        sendButton = new JButton("Gửi");
        messageArea = new JTextArea(3, 20);
    }

    private void initializeUI(String username) {
        setTitle("Chatify - " + username);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setIconImage(createAppIcon().getImage());

        // Main layout
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        createTopPanel();
        createLeftPanel();
        createCenterPanel();
        createRightPanel();

        // Show center panel by default
        showCenterWelcome();

        add(mainPanel);
    }

    private ImageIcon createAppIcon() {
        BufferedImage image = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(PRIMARY_COLOR);
        g2.fillOval(0, 0, 32, 32);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
        g2.drawString("C", 11, 22);

        g2.dispose();
        return new ImageIcon(image);
    }

    private void createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(new CompoundBorder(
                new LineBorder(SECONDARY_COLOR, 1),
                new EmptyBorder(10, 15, 10, 15)
        ));

        // Title label with custom font
        JLabel titleLabel = new JLabel("Chatify");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(PRIMARY_COLOR);

        // User info
        JLabel userLabel = new JLabel("Xin chào, " + currentUser + "!");
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        userLabel.setForeground(Color.GRAY);

        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        userPanel.setBackground(Color.WHITE);
        userPanel.add(userLabel);

        // Search panel
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBackground(Color.WHITE);
        searchPanel.setBorder(new EmptyBorder(0, 50, 0, 50));

        searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(250, 35));
        searchField.setBorder(new RoundBorder(20, SECONDARY_COLOR));
        searchField.setForeground(Color.GRAY);

        JLabel searchIcon = new JLabel("🔍");
        searchIcon.setBorder(new EmptyBorder(0, 10, 0, 0));

        searchPanel.add(searchIcon, BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);

        topPanel.add(titleLabel, BorderLayout.WEST);
        topPanel.add(searchPanel, BorderLayout.CENTER);
        topPanel.add(userPanel, BorderLayout.EAST);

        mainPanel.add(topPanel, BorderLayout.NORTH);
    }

    private void createLeftPanel() {
        leftPanel = new JPanel(new BorderLayout());
        leftPanel.setPreferredSize(new Dimension(300, 0));
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setBorder(new CompoundBorder(
                new LineBorder(SECONDARY_COLOR, 1),
                new EmptyBorder(10, 0, 10, 10)
        ));

        JLabel friendsLabel = new JLabel("Bạn bè online");
        friendsLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        friendsLabel.setBorder(new EmptyBorder(0, 15, 10, 0));

        friendsPanel = new JPanel();
        friendsPanel.setLayout(new BoxLayout(friendsPanel, BoxLayout.Y_AXIS));
        friendsPanel.setBackground(Color.WHITE);

        friendsScrollPane = new JScrollPane(friendsPanel);
        friendsScrollPane.setBorder(null);
        friendsScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        leftPanel.add(friendsLabel, BorderLayout.NORTH);
        leftPanel.add(friendsScrollPane, BorderLayout.CENTER);

        mainPanel.add(leftPanel, BorderLayout.WEST);
    }

    private void createCenterPanel() {
        centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(Color.WHITE);
        centerPanel.setBorder(new CompoundBorder(
                new LineBorder(SECONDARY_COLOR, 1),
                new EmptyBorder(10, 10, 10, 10)
        ));

        mainPanel.add(centerPanel, BorderLayout.CENTER);
    }

    private void createRightPanel() {
        rightPanel = new JPanel(new BorderLayout());
        rightPanel.setPreferredSize(new Dimension(250, 0));
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(new CompoundBorder(
                new LineBorder(SECONDARY_COLOR, 1),
                new EmptyBorder(10, 10, 10, 0)
        ));

        mainPanel.add(rightPanel, BorderLayout.EAST);
    }

    private void showCenterWelcome() {
        centerPanel.removeAll();

        JPanel welcomePanel = new JPanel(new BorderLayout());
        welcomePanel.setBackground(Color.WHITE);

        // Stylish Chatify text
        JLabel chatifyLabel = new JLabel("Chatify", JLabel.CENTER);
        chatifyLabel.setFont(new Font("Segoe UI", Font.BOLD, 48));
        chatifyLabel.setForeground(PRIMARY_COLOR);

        JLabel subtitleLabel = new JLabel("Kết nối mọi lúc, trò chuyện mọi nơi", JLabel.CENTER);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtitleLabel.setForeground(Color.GRAY);
        subtitleLabel.setBorder(new EmptyBorder(10, 0, 0, 0));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBackground(Color.WHITE);
        textPanel.add(chatifyLabel);
        textPanel.add(subtitleLabel);

        welcomePanel.add(textPanel, BorderLayout.CENTER);
        centerPanel.add(welcomePanel, BorderLayout.CENTER);

        centerPanel.revalidate();
        centerPanel.repaint();

        // Clear right panel
        rightPanel.removeAll();
        rightPanel.revalidate();
        rightPanel.repaint();
    }

    private void showChatInterface(Friend friend) {
        selectedFriend = friend;

        // Setup center panel for chat
        centerPanel.removeAll();
        centerPanel.setLayout(new BorderLayout());

        // Chat header
        JPanel chatHeader = new JPanel(new BorderLayout());
        chatHeader.setBackground(Color.WHITE);
        chatHeader.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, SECONDARY_COLOR),
                new EmptyBorder(10, 15, 10, 15)
        ));

        JLabel friendName = new JLabel(friend.getName() + " 🟢");
        friendName.setFont(new Font("Segoe UI", Font.BOLD, 16));

        chatHeader.add(friendName, BorderLayout.WEST);

        // Chat messages area
        chatPanel = new JPanel();
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
        chatPanel.setBackground(Color.WHITE);

        chatScrollPane = new JScrollPane(chatPanel);
        chatScrollPane.setBorder(null);
        chatScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // Message input area
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBackground(Color.WHITE);
        inputPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        // Toolbar
        JPanel toolbarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolbarPanel.setBackground(Color.WHITE);

        attachButton = createToolbarButton("📎");
        emojiButton = createToolbarButton("😊");
        fileButton = createToolbarButton("📁");
        voiceButton = createToolbarButton("🎤");

        toolbarPanel.add(attachButton);
        toolbarPanel.add(emojiButton);
        toolbarPanel.add(fileButton);
        toolbarPanel.add(voiceButton);

        // Message input
        JPanel messageInputPanel = new JPanel(new BorderLayout());
        messageInputPanel.setBackground(Color.WHITE);

        messageArea = new JTextArea(3, 20);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setBorder(new RoundBorder(15, SECONDARY_COLOR));
        messageArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        sendButton = new JButton("Gửi");
        sendButton.setBackground(PRIMARY_COLOR);
        sendButton.setForeground(Color.WHITE);
        sendButton.setBorder(new RoundBorder(10, PRIMARY_COLOR));
        sendButton.setPreferredSize(new Dimension(80, 40));
        sendButton.setFocusPainted(false);

        // Setup listeners for chat components
        setupChatEventListeners();

        messageInputPanel.add(new JScrollPane(messageArea), BorderLayout.CENTER);
        messageInputPanel.add(sendButton, BorderLayout.EAST);

        inputPanel.add(toolbarPanel, BorderLayout.NORTH);
        inputPanel.add(messageInputPanel, BorderLayout.CENTER);

        centerPanel.add(chatHeader, BorderLayout.NORTH);
        centerPanel.add(chatScrollPane, BorderLayout.CENTER);
        centerPanel.add(inputPanel, BorderLayout.SOUTH);

        // Setup right panel for conversation info
        showConversationInfo(friend);

        centerPanel.revalidate();
        centerPanel.repaint();

        // Load chat history
        loadChatHistory(friend.getName());

        // Focus vào message area
        SwingUtilities.invokeLater(() -> messageArea.requestFocus());
    }

    private void showConversationInfo(Friend friend) {
        rightPanel.removeAll();

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(new EmptyBorder(20, 10, 10, 10));

        JLabel infoLabel = new JLabel("Thông tin hội thoại");
        infoLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        infoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Friend avatar
        JPanel avatarPanel = new JPanel();
        avatarPanel.setBackground(Color.WHITE);
        avatarPanel.setLayout(new BoxLayout(avatarPanel, BoxLayout.Y_AXIS));

        JLabel avatarLabel = new JLabel(createCircularAvatar(friend.getName(), 80));
        JLabel nameLabel = new JLabel(friend.getName(), JLabel.CENTER);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel statusLabel = new JLabel("🟢 Đang hoạt động", JLabel.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(Color.GRAY);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        avatarLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        avatarPanel.add(avatarLabel);
        avatarPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        avatarPanel.add(nameLabel);
        avatarPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        avatarPanel.add(statusLabel);

        // Action buttons
        JPanel actionPanel = new JPanel();
        actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.Y_AXIS));
        actionPanel.setBackground(Color.WHITE);
        actionPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        JButton videoCallBtn = createActionButton("📹 Gọi video");
        JButton voiceCallBtn = createActionButton("📞 Gọi thoại");
        JButton shareFileBtn = createActionButton("📤 Chia sẻ file");

        actionPanel.add(videoCallBtn);
        actionPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        actionPanel.add(voiceCallBtn);
        actionPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        actionPanel.add(shareFileBtn);

        contentPanel.add(infoLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        contentPanel.add(avatarPanel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        contentPanel.add(actionPanel);

        rightPanel.add(contentPanel);

        rightPanel.revalidate();
        rightPanel.repaint();
    }

    private JButton createActionButton(String text) {
        JButton button = new JButton(text);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setBackground(Color.WHITE);
        button.setBorder(new RoundBorder(8, SECONDARY_COLOR));
        button.setPreferredSize(new Dimension(150, 35));
        button.setFocusPainted(false);
        return button;
    }

    private void loadFriendsList() {
        friendsPanel.removeAll();

        if (friends.isEmpty()) {
            JLabel noFriendsLabel = new JLabel("Chưa có bạn bè online", JLabel.CENTER);
            noFriendsLabel.setForeground(Color.GRAY);
            noFriendsLabel.setBorder(new EmptyBorder(20, 0, 0, 0));
            friendsPanel.add(noFriendsLabel);
        } else {
            for (Friend friend : friends) {
                JPanel friendPanel = createFriendPanel(friend);
                friendsPanel.add(friendPanel);
                friendsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            }
        }

        friendsPanel.revalidate();
        friendsPanel.repaint();
    }

    private JPanel createFriendPanel(Friend friend) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyPadding(8, 10, 8, 10));
        panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        panel.setMaximumSize(new Dimension(280, 70));

        // Avatar
        JLabel avatarLabel = new JLabel(createCircularAvatar(friend.getName(), 50));

        // Text info
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBackground(Color.WHITE);

        JLabel nameLabel = new JLabel(friend.getName());
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JLabel lastMsgLabel = new JLabel(friend.getLastMessage());
        lastMsgLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lastMsgLabel.setForeground(Color.GRAY);

        textPanel.add(nameLabel);
        textPanel.add(Box.createRigidArea(new Dimension(0, 2)));
        textPanel.add(lastMsgLabel);

        panel.add(avatarLabel, BorderLayout.WEST);
        panel.add(Box.createRigidArea(new Dimension(10, 0)), BorderLayout.CENTER);
        panel.add(textPanel, BorderLayout.CENTER);

        // Online indicator
        JLabel onlineIndicator = new JLabel("🟢");
        onlineIndicator.setBorder(new EmptyPadding(0, 0, 0, 5));
        panel.add(onlineIndicator, BorderLayout.EAST);

        // Add click listener
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showChatInterface(friend);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                panel.setBackground(SECONDARY_COLOR);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                panel.setBackground(Color.WHITE);
            }
        });

        return panel;
    }

    private ImageIcon createCircularAvatar(String text, int size) {
        // Create a circular avatar with colored background
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();

        // Set rendering hints for quality
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw circular background
        Color[] colors = {PRIMARY_COLOR, new Color(76, 175, 80), new Color(255, 87, 34),
                new Color(156, 39, 176), new Color(255, 193, 7)};
        Color bgColor = colors[Math.abs(text.hashCode()) % colors.length];

        g2.setColor(bgColor);
        g2.fillOval(0, 0, size, size);

        // Draw text (first letter of name)
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Segoe UI", Font.BOLD, size / 2));
        FontMetrics fm = g2.getFontMetrics();
        String firstLetter = text.substring(0, 1).toUpperCase();
        int x = (size - fm.stringWidth(firstLetter)) / 2;
        int y = ((size - fm.getHeight()) / 2) + fm.getAscent();
        g2.drawString(firstLetter, x, y);

        g2.dispose();
        return new ImageIcon(image);
    }

    private JButton createToolbarButton(String text) {
        JButton button = new JButton(text);
        button.setBorder(new RoundBorder(8, SECONDARY_COLOR));
        button.setBackground(Color.WHITE);
        button.setPreferredSize(new Dimension(40, 40));
        button.setFocusPainted(false);
        return button;
    }

    private void loadChatHistory(String friendName) {
        chatPanel.removeAll();

        if (chatHistory.containsKey(friendName)) {
            for (ChatMessage msg : chatHistory.get(friendName)) {
                addMessageToChat(msg.getContent(), msg.isSent(), msg.getTime());
            }
        } else {
            // Add welcome message for new conversation
            addSystemMessageToChat("Bắt đầu cuộc trò chuyện với " + friendName);
        }

        chatPanel.revalidate();
        chatPanel.repaint();

        // Scroll to bottom
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = chatScrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }

    private void addMessageToChat(String text, boolean isSent, String time) {
        JPanel messagePanel = new JPanel(new BorderLayout());
        messagePanel.setBackground(Color.WHITE);
        messagePanel.setBorder(new EmptyPadding(5, 10, 5, 10));

        JPanel bubblePanel = new JPanel(new BorderLayout());
        bubblePanel.setBackground(isSent ? MESSAGE_SENT : MESSAGE_RECEIVED);
        bubblePanel.setBorder(new RoundBorder(15, isSent ? MESSAGE_SENT : MESSAGE_RECEIVED));
        bubblePanel.setMaximumSize(new Dimension(400, Integer.MAX_VALUE));

        JTextPane messageText = new JTextPane();
        messageText.setContentType("text/html");
        messageText.setText("<html><body style='width: 350px; padding: 8px; color: " +
                (isSent ? "white" : "black") + "; font-family: Segoe UI;'>" +
                text + "</body></html>");
        messageText.setEditable(false);
        messageText.setBackground(isSent ? MESSAGE_SENT : MESSAGE_RECEIVED);
        messageText.setBorder(null);

        JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        timePanel.setBackground(isSent ? MESSAGE_SENT : MESSAGE_RECEIVED);
        JLabel timeLabel = new JLabel(time);
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        timeLabel.setForeground(isSent ? new Color(200, 200, 200) : new Color(100, 100, 100));
        timePanel.add(timeLabel);

        bubblePanel.add(messageText, BorderLayout.CENTER);
        bubblePanel.add(timePanel, BorderLayout.SOUTH);

        if (isSent) {
            messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.X_AXIS));
            messagePanel.add(Box.createHorizontalGlue());
            messagePanel.add(bubblePanel);
        } else {
            messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.X_AXIS));
            messagePanel.add(bubblePanel);
            messagePanel.add(Box.createHorizontalGlue());
        }

        chatPanel.add(messagePanel);
        chatPanel.add(Box.createRigidArea(new Dimension(0, 5)));

        // Scroll to bottom
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = chatScrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }

    private void addSystemMessageToChat(String content) {
        JPanel messagePanel = new JPanel(new BorderLayout());
        messagePanel.setBackground(Color.WHITE);
        messagePanel.setBorder(new EmptyPadding(5, 10, 5, 10));

        JLabel systemLabel = new JLabel(content, JLabel.CENTER);
        systemLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        systemLabel.setForeground(Color.GRAY);
        systemLabel.setOpaque(true);
        systemLabel.setBackground(new Color(245, 245, 245));
        systemLabel.setBorder(new RoundBorder(10, new Color(225, 225, 225)));

        messagePanel.add(systemLabel, BorderLayout.CENTER);
        chatPanel.add(messagePanel);
        chatPanel.add(Box.createRigidArea(new Dimension(0, 5)));
    }

    private void setupEventListeners() {
        // Search functionality
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filterFriends(searchField.getText().trim());
            }
        });
    }

    private void setupChatEventListeners() {
        if (sendButton != null) {
            sendButton.addActionListener(e -> sendMessage());
        }

        if (messageArea != null) {
            messageArea.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER && !e.isShiftDown()) {
//                        e.preventDefault();
                        sendMessage();
                    }
                }
            });
        }

        // Toolbar buttons
        if (attachButton != null) {
            attachButton.addActionListener(e -> showAttachmentOptions());
        }

        if (emojiButton != null) {
            emojiButton.addActionListener(e -> showEmojiPicker());
        }
    }

    private void filterFriends(String query) {
        if (query.isEmpty()) {
            loadFriendsList();
            return;
        }

        friendsPanel.removeAll();

        for (Friend friend : friends) {
            if (friend.getName().toLowerCase().contains(query.toLowerCase())) {
                JPanel friendPanel = createFriendPanel(friend);
                friendsPanel.add(friendPanel);
                friendsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            }
        }

        if (friendsPanel.getComponentCount() == 0) {
            JLabel noResultsLabel = new JLabel("Không tìm thấy kết quả", JLabel.CENTER);
            noResultsLabel.setForeground(Color.GRAY);
            noResultsLabel.setBorder(new EmptyPadding(20, 0, 0, 0));
            friendsPanel.add(noResultsLabel);
        }

        friendsPanel.revalidate();
        friendsPanel.repaint();
    }

    // Trong phương thức sendMessage(), thêm phần này:
    private void sendMessage() {
        if (messageArea != null && selectedFriend != null) {
            String message = messageArea.getText().trim();
            if (!message.isEmpty()) {
                // Kiểm tra clientSocket có null không (demo mode)
                if (clientSocket != null) {
                    // Gửi tin nhắn P2P thật
                    clientSocket.sendP2PMessage(selectedFriend.getName(), message);
                } else {
                    // Demo mode: hiển thị tin nhắn và auto-reply
                    System.out.println("Demo mode: Gửi tin nhắn đến " + selectedFriend.getName() + ": " + message);
                }

                // Hiển thị tin nhắn gửi đi ngay lập tức
                addOutgoingMessage(message);
                messageArea.setText("");

                // Auto-reply cho demo
                if (clientSocket == null) {
                    Timer timer = new Timer(1000, e -> {
                        String[] replies = {"Đã nhận tin nhắn của bạn!", "OK bạn", "Hihi", "Cảm ơn!", "Đúng rồi đó"};
                        String reply = replies[(int)(Math.random() * replies.length)];
                        addIncomingMessage(selectedFriend.getName(), reply);
                    });
                    timer.setRepeats(false);
                    timer.start();
                }
            }
        }
    }

    private void loadOnlineUsers() {
        // XÓA: không thêm bạn bè ảo nữa
        // Chỉ hiển thị bạn bè thực từ server

        System.out.println("Đang chờ danh sách bạn bè online từ server...");

        // Nếu là demo mode (clientSocket = null), thêm 1 bạn bè demo
        if (clientSocket == null) {
            SwingUtilities.invokeLater(() -> {
                addOrUpdateOnlineUser("Người dùng demo");
            });
        }
    }

    // === P2P INTEGRATION METHODS ===

    public void addOrUpdateOnlineUser(String username) {
        if (username.equals(currentUser)) return;

        SwingUtilities.invokeLater(() -> {
            boolean exists = false;
            for (Friend friend : friends) {
                if (friend.getName().equals(username)) {
                    exists = true;
                    // Cập nhật trạng thái
                    friend.setLastMessage("🟢 Online");
                    break;
                }
            }

            if (!exists) {
                Friend newFriend = new Friend(username, "🟢 Online");
                friends.add(newFriend);
                loadFriendsList();

                // Show notification for new user
                if (selectedFriend == null) {
                    showNotification(username + " vừa online");
                }
            } else {
                loadFriendsList(); // Refresh để cập nhật trạng thái
            }
        });
    }

    public void removeOnlineUser(String username) {
        SwingUtilities.invokeLater(() -> {
            boolean removed = friends.removeIf(friend -> friend.getName().equals(username));
            if (removed) {
                loadFriendsList();

                // Update chat header if currently chatting with this user
                if (selectedFriend != null && selectedFriend.getName().equals(username)) {
                    addSystemMessageToChat(username + " đã offline");
                    // Quay về màn hình chào
                    showCenterWelcome();
                }
            }
        });
    }

    public void clearAllFriends() {
        SwingUtilities.invokeLater(() -> {
            friends.clear();
            loadFriendsList();
        });
    }

    public void showChatWithUser(String username) {
        SwingUtilities.invokeLater(() -> {
            for (Friend friend : friends) {
                if (friend.getName().equals(username)) {
                    showChatInterface(friend);
                    break;
                }
            }
        });
    }

    public void addIncomingMessage(String fromUser, String content) {
        SwingUtilities.invokeLater(() -> {
            if (selectedFriend != null && selectedFriend.getName().equals(fromUser)) {
                addMessageToChat(content, false, getCurrentTime());
                saveChatMessage(fromUser, content, false);
            } else {
                // Show notification for new message
                showNotification("Tin nhắn mới từ " + fromUser + ": " +
                        (content.length() > 30 ? content.substring(0, 30) + "..." : content));

                // Update last message in friends list
                for (Friend friend : friends) {
                    if (friend.getName().equals(fromUser)) {
                        friend.setLastMessage(content);
                        loadFriendsList();
                        break;
                    }
                }

                saveChatMessage(fromUser, content, false);
            }
        });
    }

    public void addOutgoingMessage(String content) {
        SwingUtilities.invokeLater(() -> {
            if (selectedFriend != null) {
                addMessageToChat(content, true, getCurrentTime());
                saveChatMessage(selectedFriend.getName(), content, true);

                // Update last message in friends list
                for (Friend friend : friends) {
                    if (friend.getName().equals(selectedFriend.getName())) {
                        friend.setLastMessage("Bạn: " + content);
                        loadFriendsList();
                        break;
                    }
                }
            }
        });
    }

    public void addSystemMessage(String content) {
        SwingUtilities.invokeLater(() -> {
            if (selectedFriend != null) {
                addSystemMessageToChat(content);
            }
        });
    }

    private void saveChatMessage(String withUser, String content, boolean isSent) {
        if (!chatHistory.containsKey(withUser)) {
            chatHistory.put(withUser, new ArrayList<>());
        }
        chatHistory.get(withUser).add(new ChatMessage(content, isSent, getCurrentTime()));
    }


    private void showNotification(String message) {
        // Simple notification - you can enhance this with toast notifications
        System.out.println("Notification: " + message);

        if (isVisible() && !isFocused()) {
            // Flash taskbar icon (Windows)
            if (getState() == JFrame.ICONIFIED) {
                setState(JFrame.NORMAL);
            }
            toFront();
            requestFocus();
        }
    }

    private void showAttachmentOptions() {
        JOptionPane.showMessageDialog(this,
                "Tính năng đính kèm file sẽ được cập nhật sau!",
                "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showEmojiPicker() {
        JOptionPane.showMessageDialog(this,
                "Bộ chọn emoji sẽ được cập nhật sau!",
                "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    private String getCurrentTime() {
        return new java.text.SimpleDateFormat("HH:mm").format(new Date());
    }

    @Override
    public void dispose() {
        if (clientSocket != null) {
            clientSocket.disconnect();
        }
        super.dispose();
    }

    // Custom border for rounded corners
    class RoundBorder implements Border {
        private int radius;
        private Color color;

        public RoundBorder(int radius, Color color) {
            this.radius = radius;
            this.color = color;
        }

        public Insets getBorderInsets(Component c) {
            return new Insets(this.radius+1, this.radius+1, this.radius+1, this.radius+1);
        }

        public boolean isBorderOpaque() {
            return true;
        }

        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.draw(new RoundRectangle2D.Float(x, y, width-1, height-1, radius, radius));
        }
    }

    // Custom EmptyPadding class to replace EmptyBorder
    class EmptyPadding extends EmptyBorder {
        public EmptyPadding(int top, int left, int bottom, int right) {
            super(top, left, bottom, right);
        }
    }

    // Friend class
    class Friend {
        private String name;
        private String lastMessage;

        public Friend(String name, String lastMessage) {
            this.name = name;
            this.lastMessage = lastMessage;
        }

        public String getName() { return name; }
        public String getLastMessage() { return lastMessage; }
        public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }
    }

    // Inner class for chat history
    class ChatMessage {
        private String content;
        private boolean sent;
        private String time;

        public ChatMessage(String content, boolean sent, String time) {
            this.content = content;
            this.sent = sent;
            this.time = time;
        }

        public String getContent() { return content; }
        public boolean isSent() { return sent; }
        public String getTime() { return time; }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            // For testing without actual server connection
            ChatUI chatUI = new ChatUI("TestUser", null);
            chatUI.setVisible(true);
        });
    }
}