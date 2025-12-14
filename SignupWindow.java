import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class SignupWindow extends JFrame {
    private MenuWindow parent;
    private JTextField nameField, emailField, phoneField;
    private JPasswordField passField;

    public SignupWindow(MenuWindow parent) {
        this.parent = parent;
        setTitle("Signup");
        setSize(380, 260);
        setLocationRelativeTo(parent);
        setLayout(new GridLayout(6,2,6,6));

        add(new JLabel("Name:"));
        nameField = new JTextField();
        add(nameField);

        add(new JLabel("Email:"));
        emailField = new JTextField();
        add(emailField);

        add(new JLabel("Phone:"));
        phoneField = new JTextField();
        add(phoneField);

        add(new JLabel("Password:"));
        passField = new JPasswordField();
        add(passField);

        // Buttons row: Login (go to login) and Create Account
        JButton loginBtn = new JButton("Login");
        loginBtn.addActionListener(e -> {
            dispose();
            new CustomerLoginWindow(parent).setVisible(true);
        });

        JButton createBtn = new JButton("Create Account");
        createBtn.addActionListener(e -> doCreateAccount());

        add(loginBtn);
        add(createBtn);

        setVisible(true);
    }

    private void doCreateAccount() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String pass = String.valueOf(passField.getPassword()).trim();

        // ============================
        //   NEW VALIDATION LAYERS
        // ============================

        // 1. Empty fields check
        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields.");
            return;
        }

        // 2. Name validation – only letters & spaces, and cannot be email
        if (!name.matches("^[A-Za-z ]+$")) {
            JOptionPane.showMessageDialog(this, "Name must contain only letters and spaces.");
            return;
        }
        if (name.contains("@") || name.contains(".com")) {
            JOptionPane.showMessageDialog(this, "Name cannot contain email-like text.");
            return;
        }

        // 3. Email validation
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            JOptionPane.showMessageDialog(this, "Invalid email format.");
            return;
        }

        // 4. Phone number validation – exactly 10 digits
        if (!phone.matches("\\d{10}")) {
            JOptionPane.showMessageDialog(this, "Phone number must be exactly 10 digits.");
            return;
        }

        // 5. Password validation – min 4 chars (you can change)
        if (pass.length() < 4) {
            JOptionPane.showMessageDialog(this, "Password must be at least 4 characters.");
            return;
        }

        // ============================
        //   ORIGINAL CODE (UNCHANGED)
        // ============================


        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields.");
            return;
        }

        try (Connection con = DB.getCon();
             PreparedStatement ps = con.prepareStatement(
                     "INSERT INTO customers (name, email, phone, password) VALUES (?, ?, ?, ?)",
                     Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, phone);
            ps.setString(4, pass); // plain password (per requested)

            int updated = ps.executeUpdate();
            if (updated == 0) {
                throw new SQLException("Creating customer failed.");
            }

            int custId = -1;
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) custId = keys.getInt(1);
            }

            // Do NOT show customer id per your request
            JOptionPane.showMessageDialog(this, "Account created successfully. Please login.");

            dispose();
            new CustomerLoginWindow(parent).setVisible(true);

        } catch (SQLIntegrityConstraintViolationException dup) {
            JOptionPane.showMessageDialog(this, "Email already exists. Please login or use another email.");
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Signup error: " + ex.getMessage());
        }
    }
}
