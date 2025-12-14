import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.StringJoiner;
import java.awt.print.*;

public class PaymentWindow extends JFrame {

    private MenuWindow parent;
    private JLabel subtotalLabel, taxLabel, totalLabel;
    private int customerId;

    public PaymentWindow(MenuWindow parent, int customerId) {
        this.parent = parent;
        this.customerId = customerId;
        setTitle("Payment");
        setSize(420, 320);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        JPanel info = new JPanel(new GridLayout(0,1,6,6));
        subtotalLabel = new JLabel();
        taxLabel = new JLabel();
        totalLabel = new JLabel();

        computeTotals();

        info.add(new JLabel("Order Summary:"));
        info.add(subtotalLabel);
        info.add(taxLabel);
        info.add(totalLabel);

        add(info, BorderLayout.CENTER);

        JPanel btns = new JPanel();
        JButton makePayment = new JButton("Make Payment");
        JButton addMore = new JButton("Add More Items");

        makePayment.addActionListener(e -> doPayment());
        addMore.addActionListener(e -> this.dispose());

        btns.add(makePayment);
        btns.add(addMore);

        add(btns, BorderLayout.SOUTH);
    }

    private double subtotal, tax, total;

    private void computeTotals() {
        subtotal = 0;
        for (String item : parent.quantityMap.keySet()) {
            int qty = parent.quantityMap.get(item);
            if (qty > 0) {
                subtotal += qty * parent.priceMap.get(item);
            }
        }
        tax = subtotal * 0.05;
        total = subtotal + tax;

        subtotalLabel.setText("Subtotal: Rs " + subtotal);
        taxLabel.setText("Tax (5%): Rs " + tax);
        totalLabel.setText("Total: Rs " + total);
    }

    private void doPayment() {
        if (Session.customerId == 0) {
            JOptionPane.showMessageDialog(this, "You must login first.");
            return;
        }

        StringJoiner sj = new StringJoiner(", ");
        for (String item : parent.quantityMap.keySet()) {
            int qty = parent.quantityMap.get(item);
            if (qty > 0) sj.add(item + " x " + qty);
        }
        final String itemsStr = sj.toString();

        try (Connection con = DB.getCon()) {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO orders (customer_id, items, total_amount) VALUES (?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setInt(1, Session.customerId);
            ps.setString(2, itemsStr);
            ps.setDouble(3, total);
            ps.executeUpdate();

            int orderId = -1;
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) orderId = rs.getInt(1);

            // Show success dialog without OK button
            JTextArea ta = new JTextArea();
            ta.setEditable(false);
            ta.setFont(new Font("Monospaced", Font.PLAIN, 12));
            ta.append("Payment Successful!\n");
            ta.append("Order Number: " + orderId + "\n\n");
            ta.append("----- Bill Summary -----\n");
            ta.append("Items: " + itemsStr + "\n");
            ta.append("Subtotal: Rs " + subtotal + "\n");
            ta.append("Tax (5%): Rs " + tax + "\n");
            ta.append("---------------------------\n");
            ta.append("Total: Rs " + total + "\n");

            // 🔥 Only one button "Print Receipt", no OK
            JOptionPane.showOptionDialog(
                    this,
                    new JScrollPane(ta),
                    "Payment Successful",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.INFORMATION_MESSAGE,
                    null,
                    new Object[]{"Print Receipt"},
                    "Print Receipt"
            );

            fullyResetMenuPage();
            Session.customerId = 0;
            this.dispose();

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Payment error: " + ex.getMessage());
        }
    }

    private void fullyResetMenuPage() {
        // Reset item quantities
        for (String item : parent.quantityMap.keySet()) parent.quantityMap.put(item, 0);

        parent.resetQuantities();
        parent.receiptArea.setText("==== RECEIPT ====\n");

        // Disable buttons
        parent.generateReceiptBtn.setEnabled(false);
        parent.placeOrderBtn.setVisible(false);
        parent.placeOrderBtn.setEnabled(false);

        // Ensure cart-empty state
        parent.updateButtonStates();
    }
}
