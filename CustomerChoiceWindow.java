import javax.swing.*;
import java.awt.*;

public class CustomerChoiceWindow extends JFrame {
    private MenuWindow parent;

    public CustomerChoiceWindow(MenuWindow parent) {
        this.parent = parent;
        setTitle("Place Order - Customer");
        setSize(360, 160);
        setLocationRelativeTo(parent);
        setLayout(new GridLayout(4,1,6,6));

        JLabel newUser = new JLabel("New user? Don't have an account? Signup here.", SwingConstants.CENTER);
        JButton signupBtn = new JButton("Signup");
        signupBtn.addActionListener(e -> {
            dispose();
            new SignupWindow(parent).setVisible(true);
        });

        JLabel existing = new JLabel("Already a user? Login here.", SwingConstants.CENTER);
        JButton loginBtn = new JButton("Login");
        loginBtn.addActionListener(e -> {
            dispose();
            new CustomerLoginWindow(parent).setVisible(true);
        });

        add(newUser);
        add(signupBtn);
        add(existing);
        add(loginBtn);

        setVisible(true);
    }
}
