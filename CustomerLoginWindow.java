import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class CustomerLoginWindow extends JFrame {
    private JTextField emailField;
    private JPasswordField passField;
    private MenuWindow parent;

    public CustomerLoginWindow(MenuWindow parent) {
        this.parent = parent;
        setTitle("Customer Login");
        setSize(380, 200);
        setLocationRelativeTo(parent);
        setLayout(new GridLayout(4,2,6,6));

        add(new JLabel("Email:"));
        emailField = new JTextField();
        add(emailField);

        add(new JLabel("Password:"));
        passField = new JPasswordField();
        add(passField);

        JButton signupBtn = new JButton("Signup");
        signupBtn.addActionListener(e -> {
            dispose();
            new SignupWindow(parent).setVisible(true);
        });

        JButton loginBtn = new JButton("Login");
        loginBtn.addActionListener(e -> doLogin());

        add(signupBtn);
        add(loginBtn);

        setVisible(true);
    }

    private void doLogin() {
        String email = emailField.getText().trim();
        String pass = String.valueOf(passField.getPassword()).trim();

        if (email.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter email and password");
            return;
        }

        try (Connection con = DB.getCon();
             PreparedStatement ps = con.prepareStatement("SELECT id FROM customers WHERE email=? AND password=?")) {
            ps.setString(1, email);
            ps.setString(2, pass); // plain check
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int customerId = rs.getInt("id");
                Session.customerId = customerId; // set session
                dispose();
                // open PaymentPage now
                new PaymentPage(parent, customerId).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Invalid credentials. Please signup or try again.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "DB Error: " + ex.getMessage());
        }
    }
}
