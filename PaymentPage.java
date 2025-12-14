// PaymentPage.java
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.StringJoiner;
import java.awt.print.*;

public class PaymentPage extends JFrame {

    private MenuWindow parent;
    private int customerId;

    private JTable table;
    private DefaultTableModel model;
    private JLabel subtotalLabel, taxLabel, totalLabel;
    private ButtonGroup paymentGroup;

    private double subtotal = 0;
    private double tax = 0;
    private double total = 0;

    public PaymentPage(MenuWindow parent, int customerId) {
        this.parent = parent;
        this.customerId = customerId;

        setTitle("Payment Page");
        setSize(640, 520);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(8,8));

        JLabel title = new JLabel("Payment Page", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        add(title, BorderLayout.NORTH);

        String[] cols = {"Item", "Qty", "Price/unit", "Line Subtotal"};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        populateTable();
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel rightPanel = new JPanel(new BorderLayout(8,8));

        // Payment mode section
        JPanel pmodes = new JPanel(new GridLayout(0,1,6,6));
        pmodes.setBorder(BorderFactory.createTitledBorder("Payment Mode"));

        JRadioButton gpay = new JRadioButton("GPay");
        JRadioButton phonepe = new JRadioButton("PhonePe");
        JRadioButton razor = new JRadioButton("Razorpay");
        JRadioButton paytm = new JRadioButton("Paytm");

        paymentGroup = new ButtonGroup();
        paymentGroup.add(gpay); paymentGroup.add(phonepe);
        paymentGroup.add(razor); paymentGroup.add(paytm);
        gpay.setSelected(true);

        pmodes.add(gpay); pmodes.add(phonepe);
        pmodes.add(razor); pmodes.add(paytm);

        rightPanel.add(pmodes, BorderLayout.NORTH);

        // Summary
        JPanel totals = new JPanel(new GridLayout(0,1,6,6));
        totals.setBorder(BorderFactory.createTitledBorder("Summary"));

        subtotalLabel = new JLabel();
        taxLabel = new JLabel();
        totalLabel = new JLabel();
        computeTotals();

        totals.add(subtotalLabel);
        totals.add(taxLabel);
        totals.add(new JSeparator());
        totals.add(totalLabel);
        rightPanel.add(totals, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 12));
        JButton makePayment = new JButton("Make Payment");
        JButton cancel = new JButton("Cancel");

        makePayment.addActionListener(e -> makePaymentAction());
        cancel.addActionListener(e -> this.dispose());

        bottom.add(makePayment);
        bottom.add(cancel);
        rightPanel.add(bottom, BorderLayout.SOUTH);

        add(rightPanel, BorderLayout.EAST);
    }

    private void populateTable() {
        model.setRowCount(0);
        subtotal = 0;
        for (String item : parent.quantityMap.keySet()) {
            int qty = parent.quantityMap.get(item);
            if (qty > 0) {
                double price = parent.priceMap.get(item);
                double line = qty * price;
                model.addRow(new Object[]{item, qty, price, line});
                subtotal += line;
            }
        }
        computeTotals();
    }

    private void computeTotals() {
        // tax = subtotal * 0.05;
        // total = subtotal + tax;
        // subtotalLabel.setText("Subtotal: Rs " + subtotal);
        // taxLabel.setText("Tax (5%): Rs " + tax);
        // totalLabel.setText("Total: Rs " + total);
        subtotal = 0;

        for (String item : parent.quantityMap.keySet()) {
            int qty = parent.quantityMap.get(item);
            if (qty > 0) {
                subtotal += qty * parent.priceMap.get(item);
            }
        }

        tax = subtotal * 0.05;
        total = subtotal + tax;

        // update the EXISTING labels (do NOT replace them)
        if (subtotalLabel != null) subtotalLabel.setText("Subtotal: Rs " + subtotal);
        if (taxLabel != null) taxLabel.setText("Tax (5%): Rs " + tax);
        if (totalLabel != null) totalLabel.setText("Total: Rs " + total);


    }

    // ===========================
    //     MAKE PAYMENT ACTION
    // ===========================
    private void makePaymentAction() {
        if (parent.isCartEmpty()) return;

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

            showSuccessDialog(orderId, itemsStr);

            // --- NEW: Update stock prevent negative ---
            String sql = "UPDATE menu m " +
                        "JOIN order_items oi ON m.id = oi.menu_id " +
                        "SET m.remaining_quantity = GREATEST(m.remaining_quantity - oi.quantity, 0) " +
                        "WHERE oi.order_id = ?";

            PreparedStatement ps2 = con.prepareStatement(sql);
            ps2.setInt(1, orderId);
            ps2.executeUpdate();


            // inside makePaymentAction() after inserting order
            for (String item : parent.quantityMap.keySet()) {
                int qty = parent.quantityMap.get(item);
                if (qty > 0) {
                    PreparedStatement psQty = con.prepareStatement(
                        "UPDATE menu SET remaining_quantity = remaining_quantity - ? WHERE name = ?"
                    );
                    psQty.setInt(1, qty);
                    psQty.setString(2, item);
                    psQty.executeUpdate();
                }
            }


            // reset parent window & logout
            for (String item : parent.quantityMap.keySet())
                parent.quantityMap.put(item, 0);

            parent.resetQuantities();
            parent.printReceipt();
            Session.customerId = 0;

            this.dispose();
            Window menuWindow = SwingUtilities.getWindowAncestor(parent);
            if (menuWindow != null) menuWindow.dispose();

            SwingUtilities.invokeLater(() -> new CafeSystem());

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Payment Error: " + ex.getMessage());
        }
    }

    // ===========================
    //    PAYMENT SUCCESS POPUP
    // ===========================
    private void showSuccessDialog(int orderId, String itemsStr) {

        JTextArea ta = new JTextArea();
        ta.setFont(new Font("Monospaced", Font.PLAIN, 12));
        ta.setEditable(false);

        ta.append("Payment Successful!\n");
        ta.append("Order Number: " + orderId + "\n\n");
        ta.append("----- Bill Summary -----\n");
        ta.append("Items: " + itemsStr + "\n");
        ta.append("Subtotal: Rs " + subtotal + "\n");
        ta.append("Tax (5%): Rs " + tax + "\n");
        ta.append("---------------------------\n");
        ta.append("Total: Rs " + total + "\n");

        // Replace in showSuccessDialog or PaymentWindow success dialog
        Object[] options = {"Print Receipt"};  // only one button now
        JOptionPane.showOptionDialog(
            this,
            new JScrollPane(ta),
            "Payment Successful",
            JOptionPane.DEFAULT_OPTION,         // use default option type
            JOptionPane.INFORMATION_MESSAGE,
            null,
            options,
            options[0]                         // default selection
        );


        //if (choice == 0) printReceipt(ta.getText());
    }

    // ===========================
    //      PRINT RECEIPT
    // ===========================
    private void printReceipt(String text) {
        JTextArea area = new JTextArea(text);
        area.setFont(new Font("Monospaced", Font.PLAIN, 12));

        try {
            area.print();
        } catch (PrinterException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Print Failed: " + e.getMessage());
        }
    }
}
