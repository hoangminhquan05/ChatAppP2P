import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.net.URL;

public class Login extends JFrame {

    private final int ARC_SIZE = 100;
    private final int TOTAL_WIDTH = 800;
    private final int TOTAL_HEIGHT = 550;
    private JTextField emailField;
    private JPasswordField passwordField;

    public Login() {
        setTitle("Đăng Nhập - Chatify");
        setSize(TOTAL_WIDTH, TOTAL_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(TOTAL_WIDTH, TOTAL_HEIGHT));
        setContentPane(layeredPane);

        int leftPanelWidth = (int) (TOTAL_WIDTH * 0.60);
        int rightPanelWidth = (int) (TOTAL_WIDTH * 0.45);

        CustomRoundedPanel leftPanel = new CustomRoundedPanel(leftPanelWidth, TOTAL_HEIGHT, "resources/login.png", ARC_SIZE, false);
        leftPanel.setBounds(0, 0, leftPanelWidth, TOTAL_HEIGHT);
        layeredPane.add(leftPanel, JLayeredPane.DEFAULT_LAYER);

        CustomRoundedPanel rightPanel = new CustomRoundedPanel(rightPanelWidth, TOTAL_HEIGHT, null, ARC_SIZE, true);

        int gap = 20;
        int rightPanelX = leftPanelWidth - ARC_SIZE + gap;
        rightPanel.setBounds(rightPanelX, 0, rightPanelWidth + ARC_SIZE, TOTAL_HEIGHT);
        layeredPane.add(rightPanel, JLayeredPane.PALETTE_LAYER);

        JPanel formPanel = createLoginForm();

        rightPanel.setLayout(new BorderLayout());
        rightPanel.add(formPanel, BorderLayout.CENTER);

        setVisible(true);
    }

    private class CustomRoundedPanel extends JPanel {
        private Image backgroundImage;
        private Color panelColor = Color.WHITE;
        private final int arcSize;
        private final boolean roundLeftOnly;

        public CustomRoundedPanel(int width, int height, String imagePath, int arcSize, boolean roundLeftOnly) {
            this.arcSize = arcSize;
            this.roundLeftOnly = roundLeftOnly;
            setPreferredSize(new Dimension(width, height));
            setOpaque(false);

            if (imagePath != null) {
                try {
                    URL imageURL = getClass().getResource(imagePath);
                    if (imageURL != null) {
                        this.backgroundImage = new ImageIcon(imageURL).getImage();
                        this.panelColor = new Color(230, 220, 240);
                    } else {
                        this.backgroundImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
                    }
                } catch (Exception e) {
                    this.backgroundImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
                }
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            g2.setColor(panelColor);

            if (roundLeftOnly) {
                g2.fillRoundRect(0, 0, w, h, arcSize, arcSize);
                g2.fillRect(arcSize, 0, w - arcSize, h);
            } else {
                g2.fillRoundRect(0, 0, w, h, arcSize, arcSize);
                Shape clip = g2.getClip();
                g2.setClip(new RoundRectangle2D.Double(0, 0, w, h, arcSize, arcSize));

                if (backgroundImage != null)
                    g2.drawImage(backgroundImage, 0, 0, w, h, this);

                g2.setClip(clip);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private JPanel createLoginForm() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(30, 50, 30, 60));
        panel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 0, 12, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.gridx = 0;

        panel.setBorder(BorderFactory.createEmptyBorder(60, 40, 60, 120));

        int row = 0;

        // HEADER
        JLabel header = new JLabel("ĐĂNG NHẬP", SwingConstants.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 28));
        header.setForeground(Color.BLACK);
        gbc.gridy = row++;

        JPanel headerWrapper = new JPanel();
        headerWrapper.setOpaque(false);
        headerWrapper.add(header);
        panel.add(headerWrapper, gbc);

        row++;

        // EMAIL FIELD
        gbc.gridy = row++;
        JPanel emailPanel = createInputField("Email", "Email", "✉");
        panel.add(emailPanel, gbc);
        row++;

        // PASSWORD FIELD
        gbc.gridy = row++;
        JPanel passwordPanel = createPasswordField("Mật khẩu", "Mật khẩu", "🔒");
        panel.add(passwordPanel, gbc);
        row++;

        // LOGIN BUTTON
        JButton loginButton = createStyledButton("Đăng nhập");
        gbc.gridy = row++;
        panel.add(wrapButton(loginButton), gbc);

        // Thêm action listener cho nút đăng nhập
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleLogin();
            }
        });

        // Thêm action listener cho Enter key
        emailField.addActionListener(e -> handleLogin());
        passwordField.addActionListener(e -> handleLogin());

        // REGISTER LABEL
        JLabel registerLabel = new JLabel("Chưa có tài khoản? Đăng ký ngay");
        registerLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        registerLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerLabel.setHorizontalAlignment(SwingConstants.CENTER);

        registerLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                JOptionPane.showMessageDialog(Login.this,
                        "Chức năng đăng ký sẽ được cập nhật sau!");
            }
        });

        gbc.gridy = row++;
        panel.add(wrapLabel(registerLabel), gbc);

        return panel;
    }

    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        // Kiểm tra tài khoản cố định
        if ((email.equals("quan") && password.equals("quan")) ||
                (email.equals("thao") && password.equals("thao"))) {

            // Đăng nhập thành công
            String username = email;

            // Hiển thị thông báo đang kết nối
            JOptionPane.showMessageDialog(this,
                    "Đang kết nối đến server...",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);

            try {
                // Kết nối đến server
                ClientSocket clientSocket = new ClientSocket("localhost", 8888, username, null);

                // Tạo và hiển thị giao diện chat
                ChatUI chatUI = new ChatUI(username, clientSocket);
                chatUI.setVisible(true);

                // Đóng cửa sổ login
                this.dispose();

            } catch (Exception ex) {
                System.err.println("Lỗi kết nối server: " + ex.getMessage());

                JOptionPane.showMessageDialog(this,
                        "Không thể kết nối đến server!\nChạy chế độ demo...",
                        "Lỗi kết nối", JOptionPane.WARNING_MESSAGE);

                // Chạy chế độ demo nếu không kết nối được server
                ChatUI chatUI = new ChatUI(username, null);
                chatUI.setVisible(true);
                this.dispose();
            }

        } else {
            JOptionPane.showMessageDialog(this,
                    "Tài khoản hoặc mật khẩu không đúng!\n\nTài khoản demo:\n- quan/quan\n- thao/thao",
                    "Lỗi đăng nhập", JOptionPane.ERROR_MESSAGE);
        }
    }
    private JPanel createInputField(String label, String placeholder, String icon) {
        JPanel field = new JPanel(new BorderLayout(10, 0));
        field.setOpaque(false);
        field.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("SansSerif", Font.PLAIN, 18));

        emailField = new JTextField(placeholder);
        emailField.setBorder(BorderFactory.createEmptyBorder());

        field.add(iconLabel, BorderLayout.WEST);
        field.add(emailField, BorderLayout.CENTER);
        return field;
    }

    private JPanel createPasswordField(String label, String placeholder, String icon) {
        JPanel field = new JPanel(new BorderLayout(10, 0));
        field.setOpaque(false);
        field.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("SansSerif", Font.PLAIN, 18));

        passwordField = new JPasswordField(placeholder);
        passwordField.setBorder(BorderFactory.createEmptyBorder());
        passwordField.setEchoChar((char) 0);

        // Hiển thị/ẩn mật khẩu khi focus
        passwordField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (new String(passwordField.getPassword()).equals("Mật khẩu")) {
                    passwordField.setText("");
                }
                passwordField.setEchoChar('•');
            }

            public void focusLost(java.awt.event.FocusEvent evt) {
                if (passwordField.getPassword().length == 0) {
                    passwordField.setEchoChar((char) 0);
                    passwordField.setText("Mật khẩu");
                }
            }
        });

        field.add(iconLabel, BorderLayout.WEST);
        field.add(passwordField, BorderLayout.CENTER);
        return field;
    }

    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 18));
        btn.setBackground(new Color(138, 43, 226, 200));
        btn.setForeground(Color.WHITE);
        btn.setBorder(BorderFactory.createEmptyBorder());
        btn.setPreferredSize(new Dimension(220, 40));
        return btn;
    }

    private JPanel wrapButton(JButton btn) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER));
        p.setOpaque(false);
        p.add(btn);
        return p;
    }

    private JPanel wrapLabel(JLabel lb) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER));
        p.setOpaque(false);
        p.add(lb);
        return p;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Login::new);
    }
}