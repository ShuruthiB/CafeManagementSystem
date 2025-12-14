import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class AdminPanel extends JFrame {

    public AdminPanel() {
        setTitle("Admin Panel - Cafe System");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Top label
        JLabel title = new JLabel("Welcome Admin", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        add(title, BorderLayout.NORTH);

        // Left panel with buttons
        JPanel leftPanel = new JPanel(new GridLayout(7, 1, 8, 8));
        leftPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        JButton addMenuBtn = new JButton("Add Menu");
        JButton deleteMenuBtn = new JButton("Delete Menu");
        JButton updateMenuBtn = new JButton("Update Menu");
        JButton showOrdersBtn = new JButton("Show Orders");
        JButton showCustomersBtn = new JButton("Show Customers");
        JButton showMenuBtn = new JButton("Show Menu");
        JButton showPaymentsBtn = new JButton("Show Payments");

        leftPanel.add(addMenuBtn);
        leftPanel.add(deleteMenuBtn);
        leftPanel.add(updateMenuBtn);
        leftPanel.add(showOrdersBtn);
        leftPanel.add(showCustomersBtn);
        leftPanel.add(showMenuBtn);
        leftPanel.add(showPaymentsBtn);

        add(leftPanel, BorderLayout.WEST);

        // Right panel: to display results
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        add(rightPanel, BorderLayout.CENTER);

        JTextArea displayArea = new JTextArea();
        displayArea.setEditable(false);
        displayArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane scroll = new JScrollPane(displayArea);
        rightPanel.add(scroll, BorderLayout.CENTER);

        JTable table = new JTable();
        JScrollPane tableScroll = new JScrollPane(table);

        // ================= ACTIONS ==================
        addMenuBtn.addActionListener(e -> openAddMenu());
        deleteMenuBtn.addActionListener(e -> openDeleteMenu());
        updateMenuBtn.addActionListener(e -> openUpdateMenu());
        showOrdersBtn.addActionListener(e -> displayOrders(table, rightPanel));
        showCustomersBtn.addActionListener(e -> displayCustomers(table, rightPanel));
        showMenuBtn.addActionListener(e -> displayMenu(table, rightPanel));
        showPaymentsBtn.addActionListener(e -> displayPayments(table, rightPanel));

        setVisible(true);
    }

    // ---------- Add Menu ----------
    private void openAddMenu() {
        JFrame f = new JFrame("Add Menu Item");
        f.setSize(350, 250);
        f.setLayout(new GridLayout(5,2,6,6));
        f.setLocationRelativeTo(null);

        JTextField nameField = new JTextField();
        JTextField priceField = new JTextField();
        JTextField qtyField = new JTextField();

        JButton addBtn = new JButton("Add Item");

        f.add(new JLabel("Item Name:"));
        f.add(nameField);
        f.add(new JLabel("Price:"));
        f.add(priceField);
        f.add(new JLabel("Quantity:"));
        f.add(qtyField);
        f.add(new JLabel(""));
        f.add(addBtn);

        addBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String priceStr = priceField.getText().trim();
            if(name.isEmpty() || priceStr.isEmpty()) {
                JOptionPane.showMessageDialog(f, "Fill all fields");
                return;
            }
            try (Connection con = DB.getCon()) {
                PreparedStatement ps = con.prepareStatement("INSERT INTO menu (name, price, daily_quantity) VALUES (?, ?, ?)");
                ps.setString(1, name);
                ps.setDouble(2, Double.parseDouble(priceStr));
                ps.setInt(3, Integer.parseInt(qtyField.getText().trim()));

                ps.executeUpdate();
                JOptionPane.showMessageDialog(f, "Menu item added successfully!");
                f.dispose();
            } catch(Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(f, "Error: " + ex.getMessage());
            }
        });

        f.setVisible(true);
        MenuWindow.instance.refreshMenu();

    }

    // ---------- Delete Menu ----------
    private void openDeleteMenu() {
        JFrame f = new JFrame("Delete Menu Item");
        f.setSize(400, 400);
        f.setLocationRelativeTo(null);

        JTable table = new JTable();
        JScrollPane scroll = new JScrollPane(table);
        f.add(scroll, BorderLayout.CENTER);

        try (Connection con = DB.getCon()) {
            DefaultTableModel model = new DefaultTableModel(new Object[]{"ID","Item","Price"}, 0);
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM menu");
            while(rs.next()) {
                model.addRow(new Object[]{rs.getInt("id"), rs.getString("name"), rs.getDouble("price")});
            }
            table.setModel(model);
        } catch(Exception ex) {
            ex.printStackTrace();
        }

        JButton deleteBtn = new JButton("Delete Selected");
        f.add(deleteBtn, BorderLayout.SOUTH);

        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if(row == -1) return;
            int id = (int)table.getValueAt(row,0);
            try (Connection con = DB.getCon()) {
                PreparedStatement ps = con.prepareStatement("DELETE FROM menu WHERE id=?");
                ps.setInt(1, id);
                ps.executeUpdate();
                ((DefaultTableModel)table.getModel()).removeRow(row);
                JOptionPane.showMessageDialog(f, "Menu item deleted");
                // 🔥 Refresh customer menu instantly
                if (MenuWindow.instance != null) {
                    MenuWindow.instance.refreshMenu();
                }
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        });

        f.setVisible(true);
        MenuWindow.instance.refreshMenu();

    }

    // ---------- Update Menu ----------
    private void openUpdateMenu() {
        JFrame f = new JFrame("Update Menu Item");
        f.setSize(400, 400);
        f.setLocationRelativeTo(null);

        JTable table = new JTable();
        JScrollPane scroll = new JScrollPane(table);
        f.add(scroll, BorderLayout.CENTER);

        try (Connection con = DB.getCon()) {
            DefaultTableModel model = new DefaultTableModel(new Object[]{"ID","Item","Price","Daily Stock"}, 0);
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT id, name, price, daily_quantity FROM menu");
            while(rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getDouble("price"),
                    rs.getInt("daily_quantity")
                });
            }
            table.setModel(model);
        } 
        catch(Exception ex) {
            ex.printStackTrace();
        }

        JButton updateBtn = new JButton("Update Selected");
        f.add(updateBtn, BorderLayout.SOUTH);

        updateBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if(row == -1) return;
            int id = (int)table.getValueAt(row,0);
            String name = (String)table.getValueAt(row,1);
            double price = (double)table.getValueAt(row,2);
            int qty = (int)table.getValueAt(row,3);

            JTextField nameField = new JTextField(name);
            JTextField priceField = new JTextField(String.valueOf(price));
            JTextField dailyStockField = new JTextField(String.valueOf(qty));

            Object[] fields = {
                "Item Name", nameField,
                "Price", priceField,
                "Daily Stock", dailyStockField
            };

            int result = JOptionPane.showConfirmDialog(f, fields, "Update Item", JOptionPane.OK_CANCEL_OPTION);
            if(result == JOptionPane.OK_OPTION) {
                try (Connection con = DB.getCon()) {
                    PreparedStatement ps = con.prepareStatement(
                        "UPDATE menu SET name=?, price=?, daily_quantity=?, remaining_quantity=? WHERE id=?"
                    );

                    int dailyNew = Integer.parseInt(dailyStockField.getText().trim());

                    // Updating daily stock also resets remaining stock
                    ps.setString(1, nameField.getText());
                    ps.setDouble(2, Double.parseDouble(priceField.getText()));
                    ps.setInt(3, dailyNew);
                    ps.setInt(4, dailyNew);  // Reset remaining_quantity to daily stock
                    ps.setInt(5, id);

                    ps.executeUpdate();

                    // 🔥 Refresh customer menu instantly
                    if (MenuWindow.instance != null) {
                        MenuWindow.instance.refreshMenu();
                    }
                    table.setValueAt(nameField.getText(), row, 1);
                    table.setValueAt(Double.parseDouble(priceField.getText()), row, 2);
                    table.setValueAt(dailyNew,row, 3);
                    JOptionPane.showMessageDialog(f, "Item updated successfully");
                } catch(Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        f.setVisible(true);
        MenuWindow.instance.refreshMenu();

    }

    // ---------- Display Orders ----------
    private void displayOrders(JTable table, JPanel panel) {
        panel.removeAll();
        try (Connection con = DB.getCon()) {
            DefaultTableModel model = new DefaultTableModel(new Object[]{"Order ID","Customer ID","Items","Total"},0);
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM orders");
            while(rs.next()) {
                model.addRow(new Object[]{rs.getInt("id"), rs.getInt("customer_id"), rs.getString("items"), rs.getDouble("total_amount")});
            }
            table.setModel(model);
            panel.add(new JScrollPane(table), BorderLayout.CENTER);
            panel.revalidate();
            panel.repaint();
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        MenuWindow.instance.refreshMenu();

    }

    // ---------- Display Customers ----------
    private void displayCustomers(JTable table, JPanel panel) {
        panel.removeAll();
        try (Connection con = DB.getCon()) {
            DefaultTableModel model = new DefaultTableModel(new Object[]{"ID","Name","Email","Phone"},0);
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT id,name,email,phone FROM customers");
            while(rs.next()) {
                model.addRow(new Object[]{rs.getInt("id"), rs.getString("name"), rs.getString("email"), rs.getString("phone")});
            }
            table.setModel(model);
            panel.add(new JScrollPane(table), BorderLayout.CENTER);
            panel.revalidate();
            panel.repaint();
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        MenuWindow.instance.refreshMenu();

    }

    // ---------- Display Menu ----------
    private void displayMenu(JTable table, JPanel panel) {
        panel.removeAll();
        try (Connection con = DB.getCon()) {
            DefaultTableModel model = new DefaultTableModel(new Object[]{"ID","Item","Price","Quantity"},0);
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM menu");
            while(rs.next()) {
                model.addRow(new Object[]{rs.getInt("id"), rs.getString("name"), rs.getDouble("price"), rs.getInt("quantity")});
            }
            table.setModel(model);
            panel.add(new JScrollPane(table), BorderLayout.CENTER);
            panel.revalidate();
            panel.repaint();
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        MenuWindow.instance.refreshMenu();

    }

    // ---------- Display Payments ----------
    private void displayPayments(JTable table, JPanel panel) {
        panel.removeAll();
        try (Connection con = DB.getCon()) {
            DefaultTableModel model = new DefaultTableModel(new Object[]{"Order ID","Customer ID","Total"},0);
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT id,customer_id,total_amount FROM orders");
            while(rs.next()) {
                model.addRow(new Object[]{rs.getInt("id"), rs.getInt("customer_id"), rs.getDouble("total_amount")});
            }
            table.setModel(model);
            panel.add(new JScrollPane(table), BorderLayout.CENTER);
            panel.revalidate();
            panel.repaint();
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        MenuWindow.instance.refreshMenu();

    }
}
