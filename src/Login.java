import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

public class Login extends JFrame {

    public Login() {
        setTitle("Modern Login");
        setSize(420, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);
        setUndecorated(true); // bỏ khung mặc định để làm UI đẹp hơn

        // Nền tổng thể
        JPanel background = new JPanel();
        background.setBackground(new Color(227, 237, 247));
        background.setBounds(0, 0, 420, 550);
        background.setLayout(null);
        add(background);

        // Panel Form Login
        JPanel loginPanel = new JPanel();
        loginPanel.setBounds(60, 100, 300, 350);
        loginPanel.setLayout(null);
        loginPanel.setBackground(Color.WHITE);
        loginPanel.setBorder(new RoundBorder(30));
        background.add(loginPanel);

        // Tiêu đề Login
        JLabel title = new JLabel("Welcome Back!");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(new Color(50, 50, 50));
        title.setBounds(70, 20, 200, 40);
        loginPanel.add(title);

        // Username field
        JTextField username = new HintTextField("Username");
        username.setBounds(35, 90, 230, 45);
        loginPanel.add(username);

        // Password field
        JPasswordField password = new HintPasswordField("Password");
        password.setBounds(35, 150, 230, 45);
        loginPanel.add(password);

        // Button login (Gradient)
        JButton loginBtn = new JButton("LOGIN") {
            protected void paintComponent(Graphics g) {
                final Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gp = new GradientPaint(0, 0, new Color(52, 143, 235),
                        getWidth(), getHeight(), new Color(86, 180, 211));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                g2.dispose();
                super.paintComponent(g);
            }
        };
        loginBtn.setBounds(35, 220, 230, 45);
        loginBtn.setFocusPainted(false);
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        loginBtn.setOpaque(false);
        loginBtn.setContentAreaFilled(false);
        loginBtn.setBorder(new RoundBorder(20));
        loginPanel.add(loginBtn);

        // Close button
        JButton close = new JButton("X");
        close.setBounds(380, 10, 30, 30);
        close.setFocusPainted(false);
        close.setForeground(Color.DARK_GRAY);
        close.setContentAreaFilled(false);
        close.setBorder(null);
        close.addActionListener(e -> System.exit(0));
        background.add(close);

        setVisible(true);
    }

    // Border bo góc
    class RoundBorder extends AbstractBorder {
        private int radius;
        public RoundBorder(int radius) {
            this.radius = radius;
        }
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            g.setColor(new Color(200, 200, 200));
            g.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
        }
    }

    // TextField có placeholder
    class HintTextField extends JTextField {
        private String hint;
        public HintTextField(String hint) {
            this.hint = hint;
            setFont(new Font("Segoe UI", Font.PLAIN, 14));
            setBorder(new RoundBorder(20));
            setOpaque(false);
            setMargin(new Insets(5, 10, 5, 10));
        }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getText().isEmpty()) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(Color.GRAY);
                g2.setFont(getFont());
                g2.drawString(hint, 12, getHeight() / 2 + 5);
                g2.dispose();
            }
        }
    }

    // Password có placeholder
    class HintPasswordField extends JPasswordField {
        private String hint;
        public HintPasswordField(String hint) {
            this.hint = hint;
            setFont(new Font("Segoe UI", Font.PLAIN, 14));
            setBorder(new RoundBorder(20));
            setOpaque(false);
            setMargin(new Insets(5, 10, 5, 10));
        }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (String.valueOf(getPassword()).isEmpty()) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(Color.GRAY);
                g2.setFont(getFont());
                g2.drawString(hint, 12, getHeight() / 2 + 5);
                g2.dispose();
            }
        }
    }

    public static void main(String[] args) {
        new Login();
    }
}
