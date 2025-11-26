import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.net.URL;

public class Register extends JFrame {

    private final int ARC_SIZE = 100;
    private final int TOTAL_WIDTH = 800;
    private final int TOTAL_HEIGHT = 550;

    public Register() {
        setTitle("Tạo Tài Khoản - Chatify");
        setSize(TOTAL_WIDTH, TOTAL_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(TOTAL_WIDTH, TOTAL_HEIGHT));
        setContentPane(layeredPane);

        int leftPanelWidth = (int) (TOTAL_WIDTH * 0.60);
        int rightPanelWidth = (int) (TOTAL_WIDTH * 0.45);

        // Panel trái
        CustomRoundedPanel leftPanel = new CustomRoundedPanel(
                leftPanelWidth, TOTAL_HEIGHT, "resources/login.png", ARC_SIZE, false
        );
        leftPanel.setBounds(0, 0, leftPanelWidth, TOTAL_HEIGHT);
        layeredPane.add(leftPanel, JLayeredPane.DEFAULT_LAYER);

        // Panel phải (Form)
        CustomRoundedPanel rightPanel = new CustomRoundedPanel(
                rightPanelWidth, TOTAL_HEIGHT, null, ARC_SIZE, true
        );

        int gap = 20;
        int rightPanelX = leftPanelWidth - ARC_SIZE + gap;

        rightPanel.setBounds(rightPanelX, 0, rightPanelWidth + ARC_SIZE, TOTAL_HEIGHT);
        layeredPane.add(rightPanel, JLayeredPane.PALETTE_LAYER);

        rightPanel.setLayout(new BorderLayout());

        // Thêm form đăng ký
        rightPanel.add(createRegistrationForm(), BorderLayout.CENTER);

        setVisible(true);
    }

    // ================================
    // PANEL BO GÓC
    // ================================
    private class CustomRoundedPanel extends JPanel {
        private Image backgroundImage;
        private Color panelColor = Color.WHITE;
        private final int arcSize;
        private final boolean roundLeftOnly;

        public CustomRoundedPanel(int width, int height, String imagePath, int arcSize, boolean roundLeftOnly) {
            this.arcSize = arcSize;
            this.roundLeftOnly = roundLeftOnly;
            setOpaque(false);
            setPreferredSize(new Dimension(width, height));

            if (imagePath != null) {
                try {
                    URL imageURL = getClass().getResource(imagePath);
                    if (imageURL != null) {
                        backgroundImage = new ImageIcon(imageURL).getImage();
                        panelColor = new Color(230, 220, 240);
                    }
                } catch (Exception ignored) {}
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            g2.setColor(panelColor);

            if (roundLeftOnly) {
                g2.fillRoundRect(0, 0, w, h, arcSize, arcSize);
                g2.fillRect(arcSize, 0, w - arcSize, h);
            } else {
                g2.fillRoundRect(0, 0, w, h, arcSize, arcSize);

                Shape clip = g2.getClip();
                g2.setClip(new RoundRectangle2D.Double(0, 0, w, h, arcSize, arcSize));

                if (backgroundImage != null) {
                    int imgW = backgroundImage.getWidth(this);
                    int imgH = backgroundImage.getHeight(this);
                    double scale = Math.max((double) w / imgW, (double) h / imgH);

                    int drawW = (int) (imgW * scale);
                    int drawH = (int) (imgH * scale);
                    int x = (w - drawW) / 2;
                    int y = (h - drawH) / 2;

                    g2.drawImage(backgroundImage, x, y, drawW, drawH, this);
                }
                g2.setClip(clip);
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ================================
    // FORM REGISTER
    // ================================
    private JPanel createRegistrationForm() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        // thêm dòng này sau khi tạo panel
        panel.setFocusable(true);
        panel.requestFocusInWindow(); // đặt focus vào panel thay vì ô email

        GridBagConstraints gbc = createGBC();
        panel.setBorder(BorderFactory.createEmptyBorder(40, 20, 40, 100));

        int row = 0;

        JLabel header = new JLabel("TẠO TÀI KHOẢN", SwingConstants.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 28));
        header.setForeground(Color.BLACK);
        gbc.gridy = row++;
        panel.add(header, gbc);
        row += 2;

        gbc.gridy = row++;
        panel.add(createInputField("Email", "Email", new Color(100, 100, 100), "✉"), gbc);
        row += 2;

        gbc.gridy = row++;
        panel.add(createPasswordField("Mật khẩu", "Mật khẩu", new Color(100, 100, 100), "🔒"), gbc);
        row += 2;

        JButton registerButton = createStyledButton("Đăng ký");
        registerButton.addActionListener(e -> {
            // ------------------------
            // Tạo panel thông báo
            // ------------------------
            JPanel messagePanel = new JPanel(new BorderLayout(10, 10));
            messagePanel.setBackground(Color.WHITE);
            messagePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

            // Icon ✓
            JLabel iconLabel = new JLabel();
            iconLabel.setPreferredSize(new Dimension(60, 60));
            iconLabel.setOpaque(true);
            iconLabel.setBackground(new Color(0, 153, 51));
            iconLabel.setForeground(Color.WHITE);
            iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
            iconLabel.setFont(new Font("Arial", Font.BOLD, 36));
            iconLabel.setText("\u2713"); // ✓
            iconLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            iconLabel.setHorizontalTextPosition(SwingConstants.CENTER);
            iconLabel.setVerticalTextPosition(SwingConstants.CENTER);


            // Text thông báo
            JLabel messageLabel = new JLabel("Đăng ký thành công!");
            messageLabel.setFont(new Font("Arial", Font.PLAIN, 18));
            messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
            messageLabel.setForeground(new Color(50, 50, 50));
            messagePanel.add(messageLabel, BorderLayout.CENTER);

            // ------------------------
            // Tạo nút màu riêng
            // ------------------------
            JButton btnLogin = new JButton("Đăng nhập ngay");
            btnLogin.setBackground(new Color(0, 153, 51)); // xanh
            btnLogin.setForeground(Color.WHITE);
            btnLogin.setFocusPainted(false);
            btnLogin.setFont(new Font("Arial", Font.BOLD, 14));
            btnLogin.addActionListener(ev -> {
                new Login().setVisible(true); // Mở form Login
                SwingUtilities.getWindowAncestor(messagePanel).dispose(); // Đóng popup
                SwingUtilities.getWindowAncestor(panel).dispose(); // Đóng Register
            });

            JButton btnCancel = new JButton("Hủy");
            btnCancel.setBackground(new Color(200, 50, 50)); // đỏ
            btnCancel.setForeground(Color.WHITE);
            btnCancel.setFocusPainted(false);
            btnCancel.setFont(new Font("Arial", Font.BOLD, 14));
            btnCancel.addActionListener(ev -> {
                SwingUtilities.getWindowAncestor(messagePanel).dispose(); // Đóng popup
            });

            // Nút vào panel ngang
            JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
            buttonsPanel.setBackground(Color.WHITE);
            buttonsPanel.add(btnLogin);
            buttonsPanel.add(btnCancel);

            messagePanel.add(buttonsPanel, BorderLayout.SOUTH);

            // ------------------------
            // Hiển thị JOptionPane custom
            // ------------------------
            JOptionPane.showOptionDialog(
                    panel,
                    messagePanel,
                    "",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    new Object[]{}, // bỏ qua options cũ
                    null
            );
        });




        gbc.gridy = row++;
        panel.add(wrapButton(registerButton), gbc);

        return panel;
    }

    // ================================
    // TIỆN ÍCH
    // ================================
    private GridBagConstraints createGBC() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 2;
        gbc.gridx = 0;
        return gbc;
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 18));
        button.setBackground(new Color(138, 43, 226, 200));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(220, 40));
        button.setBorder(BorderFactory.createEmptyBorder());
        return button;
    }

    private JPanel wrapButton(JButton button) {
        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        wrapper.add(button);
        wrapper.setOpaque(false);
        return wrapper;
    }

    private JPanel createInputField(String labelText, String placeholder, Color iconColor, String iconChar) {
        JPanel field = new JPanel(new BorderLayout(10, 0));
        field.setOpaque(false);
        field.setPreferredSize(new Dimension(300, 45));
        field.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(200, 200, 200)));

        JLabel icon = new JLabel(iconChar);
        icon.setFont(new Font("SansSerif", Font.PLAIN, 18));
        icon.setForeground(iconColor);

        JTextField textField = new JTextField(placeholder);
        textField.setBorder(BorderFactory.createEmptyBorder());
        textField.setForeground(Color.GRAY);

        textField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                if (textField.getText().equals(placeholder)) {
                    textField.setText("");
                    textField.setForeground(Color.BLACK);
                }
            }
            public void focusLost(java.awt.event.FocusEvent e) {
                if (textField.getText().isEmpty()) {
                    textField.setText(placeholder);
                    textField.setForeground(Color.GRAY);
                }
            }
        });

        field.add(icon, BorderLayout.WEST);
        field.add(textField, BorderLayout.CENTER);
        return field;
    }

    private JPanel createPasswordField(String labelText, String placeholder, Color iconColor, String iconChar) {
        JPanel field = new JPanel(new BorderLayout(10, 0));
        field.setOpaque(false);
        field.setPreferredSize(new Dimension(300, 45));
        field.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(200, 200, 200)));

        JLabel icon = new JLabel(iconChar);
        icon.setFont(new Font("SansSerif", Font.PLAIN, 18));
        icon.setForeground(iconColor);

        JPasswordField pwd = new JPasswordField(placeholder);
        pwd.setBorder(BorderFactory.createEmptyBorder());
        pwd.setForeground(Color.GRAY);
        pwd.setEchoChar((char) 0);

        pwd.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                if (String.valueOf(pwd.getPassword()).equals(placeholder)) {
                    pwd.setText("");
                    pwd.setForeground(Color.BLACK);
                    pwd.setEchoChar('•');
                }
            }
            public void focusLost(java.awt.event.FocusEvent e) {
                if (String.valueOf(pwd.getPassword()).isEmpty()) {
                    pwd.setText(placeholder);
                    pwd.setForeground(Color.GRAY);
                    pwd.setEchoChar((char) 0);
                }
            }
        });

        field.add(icon, BorderLayout.WEST);
        field.add(pwd, BorderLayout.CENTER);
        return field;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Register::new);
    }
}
