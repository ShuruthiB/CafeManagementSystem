import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class CommonLoginWindow extends JFrame {
    private MenuWindow parent;
    private JTextField emailField;
    private JPasswordField passField;

    public CommonLoginWindow(MenuWindow parent) {
        this.parent = parent;
        setTitle("Login");
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

        try (Connection con = DB.getCon()) {

            // First check admin table
            PreparedStatement psAdmin = con.prepareStatement(
                "SELECT * FROM admin WHERE email=? AND password=?"
            );
            psAdmin.setString(1, email);
            psAdmin.setString(2, pass);
            ResultSet rsAdmin = psAdmin.executeQuery();

            if (rsAdmin.next()) {
                dispose();
                new AdminPanel().setVisible(true); // open admin panel
                return;
            }

            // Then check customer/users table
            PreparedStatement psUser = con.prepareStatement(
                "SELECT id FROM customers  WHERE email=? AND password=?"
            );
            psUser.setString(1, email);
            psUser.setString(2, pass);
            ResultSet rsUser = psUser.executeQuery();

            if (rsUser.next()) {
                int customerId = rsUser.getInt("id");
                Session.customerId = customerId;
                dispose();
                new PaymentPage(parent, customerId).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Invalid credentials or user does not exist.");
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "DB Error: " + ex.getMessage());
        }
    }
}
