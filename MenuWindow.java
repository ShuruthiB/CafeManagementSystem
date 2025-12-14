import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.HashMap;

public class MenuWindow extends JFrame {

    public HashMap<String, Integer> quantityMap = new HashMap<>();
    public HashMap<String, Double> priceMap = new HashMap<>();
    private HashMap<String, JTextField> qtyBoxMap = new HashMap<>();
    public static MenuWindow instance;


    // NEW: map to keep references to item buttons so we can re-enable/update them later
    private HashMap<String, JButton> itemButtonMap = new HashMap<>();

    // NEW: store default button colors to restore after low-stock styling
    private Color defaultButtonBg = null;
    private Color defaultButtonFg = null;

    public JTextArea receiptArea;
    public JButton placeOrderBtn;
    public JButton generateReceiptBtn;
    private JButton resetBtn;
    private JButton loginBtn;
    private JPanel menuGrid;


    public MenuWindow() {
        setTitle("Cafe Management System");
        setSize(900, 520);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        MenuWindow.instance = this;


        JSplitPane split = new JSplitPane();
        split.setDividerLocation(460);

        JPanel leftContainer = new JPanel(new BorderLayout(8, 8));
        menuGrid = new JPanel(new GridLayout(0, 2, 10, 10));
        menuGrid.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel right = new JPanel(new BorderLayout());
        receiptArea = new JTextArea();
        receiptArea.setEditable(false);
        receiptArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        receiptArea.setText("==== RECEIPT ====\n");

        loadMenu(menuGrid);

        JPanel leftButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        generateReceiptBtn = new JButton("Generate Receipt");
        generateReceiptBtn.setEnabled(false);
        generateReceiptBtn.addActionListener(e -> {
            printReceipt();
            placeOrderBtn.setVisible(true);
            placeOrderBtn.setEnabled(true);
        });

        resetBtn = new JButton("Reset");
        resetBtn.addActionListener(e -> {
            resetQuantities();
            receiptArea.setText("==== RECEIPT ====\n");
        });

        leftButtons.add(generateReceiptBtn);
        leftButtons.add(resetBtn);
        leftContainer.add(new JScrollPane(menuGrid), BorderLayout.CENTER);
        leftContainer.add(leftButtons, BorderLayout.SOUTH);

        JPanel rightBottom = new JPanel(new BorderLayout());
        JPanel placePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel loginPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        placeOrderBtn = new JButton("Place Order");
        placeOrderBtn.setVisible(false);
        placeOrderBtn.setEnabled(false);
        placeOrderBtn.addActionListener(e -> handlePlaceOrderClick());

        loginBtn = new JButton("Login");
        loginBtn.addActionListener(e -> {
            new CommonLoginWindow(this).setVisible(true);
        });


        placePanel.add(placeOrderBtn);
        loginPanel.add(loginBtn);
        rightBottom.add(placePanel, BorderLayout.WEST);
        rightBottom.add(loginPanel, BorderLayout.EAST);

        right.add(new JScrollPane(receiptArea), BorderLayout.CENTER);
        right.add(rightBottom, BorderLayout.SOUTH);

        split.setLeftComponent(leftContainer);
        split.setRightComponent(right);

        add(split);
        setVisible(true);

        updateButtonStates(); // 🔥 ensures Place Order disabled if cart empty
    }

    private void loadMenu(JPanel panel) {
        try (Connection con = DB.getCon()) {

            // Reset quantities if last_reset < today
            PreparedStatement resetStmt = con.prepareStatement(
                "UPDATE menu SET remaining_quantity = daily_quantity, last_reset = CURRENT_DATE WHERE last_reset < CURRENT_DATE"
            );
            resetStmt.executeUpdate();

            PreparedStatement ps = con.prepareStatement("SELECT id, name, price, remaining_quantity FROM menu");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String item = rs.getString("name");
                double price = rs.getDouble("price");
                int remainingQty = rs.getInt("remaining_quantity");

                priceMap.put(item, price);
                quantityMap.put(item, 0);

                // Build label: show "Only X left" only when <=2 and >=0
                String label = item + " - ₹ " + price;
                if (remainingQty <= 2 && remainingQty >= 0) {
                    label += " (Only " + remainingQty + " left)";
                }

                JButton itemBtn = new JButton(label);

                // Capture default button colors once (first button)
                if (defaultButtonBg == null) defaultButtonBg = itemBtn.getBackground();
                if (defaultButtonFg == null) defaultButtonFg = itemBtn.getForeground();

                // Low-stock styling + animation
                if (remainingQty <= 2 && remainingQty > 0) {
                    itemBtn.setBackground(new Color(65, 105, 225)); 
                    itemBtn.setForeground(Color.WHITE);
                    itemBtn.setOpaque(true);
                    itemBtn.setBorderPainted(false);

                    animateLowStock(itemBtn); // 🔥 blink animation
                }


                JTextField qtyField = new JTextField("0");
                qtyField.setEditable(false);
                qtyField.setHorizontalAlignment(SwingConstants.CENTER);

                qtyBoxMap.put(item, qtyField);

                // store button reference so we can update it later from resetQuantities()
                itemButtonMap.put(item, itemBtn);

                if (remainingQty == 0) {
                    itemBtn.setText(item + " - ₹ " + price + " (OUT OF STOCK)");
                    itemBtn.setEnabled(false);
                    itemBtn.setBackground(Color.GRAY);
                    itemBtn.setForeground(Color.WHITE);
                } else {
                    itemBtn.setEnabled(true);
                }


                itemBtn.addActionListener(e -> {
                    int currentSelected = quantityMap.getOrDefault(item, 0);

                    // if trying to select more than available, show message
                    if (currentSelected + 1 > remainingQty) {
                        JOptionPane.showMessageDialog(this, "No more stock for this item today!");
                        return;
                    }

                    currentSelected++;
                    quantityMap.put(item, currentSelected);
                    qtyField.setText(String.valueOf(currentSelected));

                    // If selection reaches available stock, disable the button for further clicks
                    if (currentSelected == remainingQty) {
                        itemBtn.setEnabled(false);
                    }

                    updateButtonStates();
                });

                panel.add(itemBtn);
                panel.add(qtyField);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading menu: " + ex.getMessage());
        }
    }


    public void printReceipt() {
        double subtotal = 0;
        receiptArea.setText("==== RECEIPT ====\n\n");

        for (String item : quantityMap.keySet()) {
            int qty = quantityMap.get(item);
            if (qty > 0) {
                double amount = qty * priceMap.get(item);
                subtotal += amount;
                receiptArea.append(item + " x " + qty + " = Rs " + amount + "\n");
            }
        }
        double tax = subtotal * 0.05;
        double total = subtotal + tax;

        receiptArea.append("\nSubtotal: Rs " + subtotal);
        receiptArea.append("\nTax (5%): Rs " + tax);
        receiptArea.append("\n---------------------------");
        receiptArea.append("\nTotal: Rs " + total);

        placeOrderBtn.setVisible(true);
        placeOrderBtn.setEnabled(subtotal > 0); // 🔥 disable if cart empty
    }

    public void resetQuantities() {
        // reset selection quantities in memory and UI fields
        for (String item : quantityMap.keySet()) {
            quantityMap.put(item, 0);
            JTextField f = qtyBoxMap.get(item);
            if (f != null) f.setText("0");
        }
        receiptArea.setText("==== RECEIPT ====\n");
        generateReceiptBtn.setEnabled(false);

        // Now: refresh buttons from DB so any button disabled due to selection is restored (if stock exists)
        try (Connection con = DB.getCon();
             PreparedStatement ps = con.prepareStatement("SELECT name, remaining_quantity FROM menu");
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String name = rs.getString("name");
                int rem = rs.getInt("remaining_quantity");

                JButton btn = itemButtonMap.get(name);
                JTextField qf = qtyBoxMap.get(name);

                if (btn != null) {
                    // OUT OF STOCK
                    if (rem == 0) {
                        btn.setText(name + " - ₹ " + priceMap.getOrDefault(name, 0.0) + " (OUT OF STOCK)");
                        btn.setEnabled(false);
                        btn.setBackground(Color.GRAY);
                        btn.setForeground(Color.WHITE);
                        continue;
                    }

                    // Normal stock label
                    String baseLabel = name + " - ₹ " + priceMap.getOrDefault(name, 0.0);
                    if (rem <= 2 && rem >= 0) {
                        btn.setText(baseLabel + " (Only " + rem + " left)");
                    } else {
                        btn.setText(baseLabel);
                    }

                    // Low stock styling + animation
                    if (rem <= 2 && rem > 0) {
                        btn.setBackground(new Color(65, 105, 225));
                        btn.setForeground(Color.WHITE);
                        btn.setOpaque(true);
                        btn.setBorderPainted(false);

                        animateLowStock(btn); // 🔥 blink animation
                    } else {
                        // restore default style
                        if (defaultButtonBg != null) btn.setBackground(defaultButtonBg);
                        if (defaultButtonFg != null) btn.setForeground(defaultButtonFg);
                        btn.setOpaque(true);
                        btn.setBorderPainted(true);

                        // stop animation
                        Timer t = (Timer) btn.getClientProperty("blinkTimer");
                        if (t != null) t.stop();
                    }


                    // Set style for low stock
                    if (rem <= 2 && rem > 0) {
                        btn.setBackground(new Color(65, 105, 225));
                        btn.setForeground(Color.WHITE);
                        btn.setOpaque(true);
                        btn.setBorderPainted(false);
                    } else {
                        // restore default style
                        if (defaultButtonBg != null) btn.setBackground(defaultButtonBg);
                        if (defaultButtonFg != null) btn.setForeground(defaultButtonFg);
                        btn.setOpaque(true);
                        btn.setBorderPainted(true);
                    }

                    // enable/disable based on DB stock
                    btn.setEnabled(rem > 0);

                    // reset associated qty field
                    if (qf != null) qf.setText("0");
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            // still continue UI reset if DB fails
        }

        // hide and disable place order button as before
        placeOrderBtn.setVisible(false);
        placeOrderBtn.setEnabled(false);

        // ensure updateButtonStates enforces cart-empty state
        updateButtonStates();
    }

    public void updateButtonStates() {
        boolean cartHasItems = false;
        for (String item : quantityMap.keySet()) {
            if (quantityMap.get(item) > 0) {
                cartHasItems = true;
                break;
            }
        }

        generateReceiptBtn.setEnabled(cartHasItems);

        if (cartHasItems) {
            placeOrderBtn.setEnabled(true);
        } else {
            placeOrderBtn.setVisible(false);
            placeOrderBtn.setEnabled(false);
        }
    }

    public boolean isCartEmpty() {
        for (String item : quantityMap.keySet()) {
            if (quantityMap.get(item) > 0) return false;
        }
        return true;
    }

    private void handlePlaceOrderClick() {
        if (Session.customerId == 0) {
            CustomerChoiceWindow choice = new CustomerChoiceWindow(this);
            choice.setVisible(true);
            if (Session.customerId != 0) {
                openPaymentPage(Session.customerId);
            }
        } else {
            openPaymentPage(Session.customerId);
        }
    }

    private void openLoginWindow() {
        new CustomerLoginWindow(this);
    }

    private void openPaymentPage(int customerId) {
        PaymentPage page = new PaymentPage(this, customerId);
        page.setVisible(true);
    }

    // 🔥 ANIMATION: blink effect for last 1–2 stock items
    private void animateLowStock(JButton btn) {
        Timer t = new Timer(400, e -> {
            Color bg = btn.getBackground();
            if (bg.equals(new Color(65,105,225))) {
                btn.setBackground(new Color(30, 144, 255));
            } else {
                btn.setBackground(new Color(65,105,225));
            }
        });
        t.setRepeats(true);
        t.start();

        // store timer so it doesn’t get garbage collected
        btn.putClientProperty("blinkTimer", t);
    }

    public void refreshMenu() {
        menuGrid.removeAll();
        quantityMap.clear();
        priceMap.clear();
        qtyBoxMap.clear();
        itemButtonMap.clear();

        loadMenu(menuGrid);

        menuGrid.revalidate();
        menuGrid.repaint();
    }


}
