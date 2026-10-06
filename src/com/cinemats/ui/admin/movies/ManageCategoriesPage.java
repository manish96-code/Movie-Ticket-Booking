package com.cinemats.ui.admin.movies;

import com.cinemats.dao.CategoryDAO;
import com.cinemats.model.Category;
import com.cinemats.ui.admin.AdminDashboard;
import com.cinemats.util.Theme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

// Movie category management page
public class ManageCategoriesPage extends JPanel {

    private final AdminDashboard dashboard;
    private DefaultTableModel categoryTableModel;
    private JTable categoryTable;
    private JTextField nameField;
    private JTextArea descArea;
    private JTextField searchField;
    private JLabel countBadge;

    // Unified Form state & controls
    private JLabel formTitle;
    private JLabel formSubtitle;
    private JButton submitBtn;
    private JButton cancelEditBtn;
    private Integer editingCategoryId = null;

    public ManageCategoriesPage(AdminDashboard dashboard) {
        this.dashboard = dashboard;
        setLayout(new BorderLayout(0, 16));
        setBackground(Theme.BG_MAIN);
        setBorder(new EmptyBorder(22, 26, 22, 26));

        initUI();
        refreshCategoryTable();
    }

    private void initUI() {
        // Banner Header
        add(createBanner("Movie Category Management",
                "Define movie genres and classifications. Categories must be configured before scheduling new movies."),
                BorderLayout.NORTH);

        // Main Content: Split into Left Form and Right Table
        JPanel mainContent = new JPanel(new BorderLayout(16, 0));
        mainContent.setOpaque(false);

        // Left Panel: Create / Edit Category Form
        JPanel formCard = createFormCard();
        formCard.setPreferredSize(new Dimension(340, 0));
        mainContent.add(formCard, BorderLayout.WEST);

        // Right Panel: Categories Table & Actions
        JPanel tableCard = createTableCard();
        mainContent.add(tableCard, BorderLayout.CENTER);

        add(mainContent, BorderLayout.CENTER);
    }

    private JPanel createFormCard() {
        JPanel card = new JPanel(new BorderLayout(0, 14));
        card.setBackground(Theme.CARD_BG);
        card.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));

        // Form Title
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        formTitle = new JLabel("Create New Category");
        formTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        formTitle.setForeground(Theme.TEXT_DARK);

        formSubtitle = new JLabel("Add a genre required for film cataloguing");
        formSubtitle.setFont(Theme.FONT_SMALL);
        formSubtitle.setForeground(Theme.TEXT_MUTED);

        titlePanel.add(formTitle);
        titlePanel.add(Box.createVerticalStrut(4));
        titlePanel.add(formSubtitle);
        card.add(titlePanel, BorderLayout.NORTH);

        // Input Fields Panel
        JPanel fieldsPanel = new JPanel();
        fieldsPanel.setLayout(new BoxLayout(fieldsPanel, BoxLayout.Y_AXIS));
        fieldsPanel.setOpaque(false);

        JLabel nameLbl = new JLabel("Category / Genre Name *");
        nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        nameLbl.setForeground(Theme.TEXT_DARK);

        nameField = new JTextField();
        nameField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        nameField.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));
        nameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        JLabel descLbl = new JLabel("Description (Optional)");
        descLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        descLbl.setForeground(Theme.TEXT_DARK);

        descArea = new JTextArea(4, 20);
        descArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setBorder(new EmptyBorder(6, 8, 6, 8));

        JScrollPane descScroll = new JScrollPane(descArea);
        descScroll.setBorder(new LineBorder(Theme.BORDER_COLOR, 1, true));
        descScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        // Suggested Pills
        JLabel suggestLbl = new JLabel("Quick Suggested Categories:");
        suggestLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        suggestLbl.setForeground(Theme.TEXT_MUTED);

        JPanel pillsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        pillsPanel.setOpaque(false);
        String[] quickSuggestions = {"Sci-Fi", "Action", "Thriller", "Comedy", "Drama", "Animation", "Romance", "Horror"};
        for (String sug : quickSuggestions) {
            JButton pill = createPillButton(sug);
            pill.addActionListener(e -> nameField.setText(sug));
            pillsPanel.add(pill);
        }

        fieldsPanel.add(nameLbl);
        fieldsPanel.add(Box.createVerticalStrut(6));
        fieldsPanel.add(nameField);
        fieldsPanel.add(Box.createVerticalStrut(14));
        fieldsPanel.add(descLbl);
        fieldsPanel.add(Box.createVerticalStrut(6));
        fieldsPanel.add(descScroll);
        fieldsPanel.add(Box.createVerticalStrut(16));
        fieldsPanel.add(suggestLbl);
        fieldsPanel.add(Box.createVerticalStrut(6));
        fieldsPanel.add(pillsPanel);
        fieldsPanel.add(Box.createVerticalGlue());

        card.add(fieldsPanel, BorderLayout.CENTER);

        // Action Buttons: Save / Update + Cancel Edit
        JPanel bottomAction = new JPanel();
        bottomAction.setLayout(new BoxLayout(bottomAction, BoxLayout.Y_AXIS));
        bottomAction.setOpaque(false);

        submitBtn = Theme.createPrimaryButton("+ Save Category");
        submitBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        submitBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        submitBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        submitBtn.addActionListener(e -> handleSaveCategory());

        cancelEditBtn = Theme.createSecondaryButton("Cancel Edit");
        cancelEditBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cancelEditBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        cancelEditBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        cancelEditBtn.setVisible(false);
        cancelEditBtn.addActionListener(e -> resetToCreateMode());

        bottomAction.add(submitBtn);
        bottomAction.add(Box.createVerticalStrut(8));
        bottomAction.add(cancelEditBtn);

        card.add(bottomAction, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createTableCard() {
        JPanel card = new JPanel(new BorderLayout(0, 14));
        card.setBackground(Theme.CARD_BG);
        card.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(18, 20, 18, 20)
        ));

        // Toolbar: Search and Action Buttons
        JPanel toolbar = new JPanel(new BorderLayout(10, 0));
        toolbar.setOpaque(false);

        JPanel leftToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftToolbar.setOpaque(false);

        JLabel tableTitle = new JLabel("Category Catalogue");
        tableTitle.setFont(Theme.FONT_HEADER);
        tableTitle.setForeground(Theme.TEXT_DARK);

        countBadge = new JLabel("0 Categories");
        countBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        countBadge.setForeground(new Color(109, 40, 217));
        countBadge.setBackground(new Color(243, 232, 255));
        countBadge.setOpaque(true);
        countBadge.setBorder(new EmptyBorder(3, 8, 3, 8));

        searchField = new JTextField(14);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        searchField.putClientProperty("JTextField.placeholderText", "Search category...");
        searchField.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(4, 8, 4, 8)
        ));
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
        });

        leftToolbar.add(tableTitle);
        leftToolbar.add(countBadge);
        leftToolbar.add(searchField);

        JPanel rightToolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightToolbar.setOpaque(false);

        JButton refreshBtn = Theme.createSecondaryButton("Refresh");
        refreshBtn.addActionListener(e -> refreshCategoryTable());

        JButton editBtn = Theme.createSecondaryButton("✏️ Edit Category");
        editBtn.addActionListener(e -> handleEditCategory());

        JButton deleteBtn = Theme.createSecondaryButton("Remove Selected");
        deleteBtn.setForeground(Theme.ACCENT_RED);
        deleteBtn.addActionListener(e -> handleDeleteCategory());

        JButton goToMoviesBtn = Theme.createPrimaryButton("Manage Movies");
        goToMoviesBtn.addActionListener(e -> {
            if (dashboard != null) {
                dashboard.switchToPage("PAGE_MOVIES");
            }
        });

        rightToolbar.add(refreshBtn);
        rightToolbar.add(editBtn);
        rightToolbar.add(deleteBtn);
        rightToolbar.add(goToMoviesBtn);

        toolbar.add(leftToolbar, BorderLayout.WEST);
        toolbar.add(rightToolbar, BorderLayout.EAST);
        card.add(toolbar, BorderLayout.NORTH);

        // Category Table
        String[] cols = {"ID", "Category Name", "Description", "Movies Using", "Created Date"};
        categoryTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        categoryTable = new JTable(categoryTableModel);
        styleTable(categoryTable);

        categoryTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        categoryTable.getColumnModel().getColumn(1).setPreferredWidth(140);
        categoryTable.getColumnModel().getColumn(2).setPreferredWidth(260);
        categoryTable.getColumnModel().getColumn(3).setPreferredWidth(95);
        categoryTable.getColumnModel().getColumn(4).setPreferredWidth(130);

        // Double-click row listener to edit
        categoryTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int r = categoryTable.rowAtPoint(e.getPoint());
                    if (r >= 0 && r < categoryTable.getRowCount()) {
                        categoryTable.setRowSelectionInterval(r, r);
                    }
                }
            }

            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && categoryTable.getSelectedRow() >= 0) {
                    handleEditCategory();
                }
            }
        });

        // Right-click context popup menu
        JPopupMenu contextMenu = new JPopupMenu();
        JMenuItem editItem = new JMenuItem("✏️ Edit Category Details");
        editItem.setFont(Theme.FONT_REGULAR);
        editItem.addActionListener(e -> handleEditCategory());

        JMenuItem deleteItem = new JMenuItem("🗑️ Remove Category");
        deleteItem.setFont(Theme.FONT_REGULAR);
        deleteItem.setForeground(Theme.ACCENT_RED);
        deleteItem.addActionListener(e -> handleDeleteCategory());

        contextMenu.add(editItem);
        contextMenu.addSeparator();
        contextMenu.add(deleteItem);
        categoryTable.setComponentPopupMenu(contextMenu);

        JScrollPane catScroll = new JScrollPane(categoryTable);
        com.cinemats.util.Theme.applyModernScrollBars(catScroll);
        card.add(catScroll, BorderLayout.CENTER);
        return card;
    }

    private void handleSaveCategory() {
        String name = nameField.getText().trim();
        String desc = descArea.getText().trim();

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Category Name is required. Please specify a name.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            nameField.requestFocus();
            return;
        }

        if (editingCategoryId == null) {
            // --- CREATE MODE ---
            if (CategoryDAO.categoryExists(name)) {
                JOptionPane.showMessageDialog(this,
                        "A category with name '" + name + "' already exists.",
                        "Duplicate Category",
                        JOptionPane.WARNING_MESSAGE);
                nameField.requestFocus();
                return;
            }

            boolean success = CategoryDAO.addCategory(name, desc);
            if (success) {
                JOptionPane.showMessageDialog(this,
                        "Category '" + name + "' added successfully!",
                        "Category Created",
                        JOptionPane.INFORMATION_MESSAGE);
                resetToCreateMode();
                refreshCategoryTable();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to save category. Please try again.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } else {
            // --- EDIT MODE ---
            if (CategoryDAO.categoryExists(name, editingCategoryId)) {
                JOptionPane.showMessageDialog(this,
                        "Another category with name '" + name + "' already exists.",
                        "Duplicate Category",
                        JOptionPane.WARNING_MESSAGE);
                nameField.requestFocus();
                return;
            }

            boolean success = CategoryDAO.updateCategory(editingCategoryId, name, desc);
            if (success) {
                JOptionPane.showMessageDialog(this,
                        "Category '" + name + "' updated successfully!",
                        "Category Updated",
                        JOptionPane.INFORMATION_MESSAGE);
                resetToCreateMode();
                refreshCategoryTable();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to update category. Please verify your database connection.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleEditCategory() {
        int selectedRow = categoryTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select a category row from the table to edit.",
                    "No Category Selected",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int catId = Integer.parseInt(categoryTableModel.getValueAt(selectedRow, 0).toString());
        Category category = CategoryDAO.getCategoryById(catId);
        if (category == null) {
            String catName = (String) categoryTableModel.getValueAt(selectedRow, 1);
            String desc = (String) categoryTableModel.getValueAt(selectedRow, 2);
            category = new Category(catId, catName, "—".equals(desc) ? "" : desc, "");
        }

        setEditMode(category);
    }

    public void setEditMode(Category category) {
        if (category == null) return;
        this.editingCategoryId = category.getId();
        formTitle.setText("Edit Category #" + category.getId());
        formSubtitle.setText("Modifying: " + category.getName());
        nameField.setText(category.getName());
        descArea.setText(category.getDescription() != null ? category.getDescription() : "");
        submitBtn.setText("💾 Update Category");
        cancelEditBtn.setVisible(true);
        nameField.requestFocus();
        revalidate();
        repaint();
    }

    public void resetToCreateMode() {
        this.editingCategoryId = null;
        formTitle.setText("Create New Category");
        formSubtitle.setText("Add a genre required for film cataloguing");
        nameField.setText("");
        descArea.setText("");
        submitBtn.setText("+ Save Category");
        cancelEditBtn.setVisible(false);
        categoryTable.clearSelection();
        revalidate();
        repaint();
    }

    private void handleDeleteCategory() {
        int selectedRow = categoryTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select a category row to delete.",
                    "Selection Required",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int catId = (int) categoryTableModel.getValueAt(selectedRow, 0);
        String catName = (String) categoryTableModel.getValueAt(selectedRow, 1);
        int moviesCount = (int) categoryTableModel.getValueAt(selectedRow, 3);

        String warningText = "Are you sure you want to delete category '" + catName + "'?";
        if (moviesCount > 0) {
            warningText += "\n\nWarning: There are currently " + moviesCount
                    + " movie(s) classified under this category!";
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                warningText,
                "Confirm Category Deletion",
                JOptionPane.YES_NO_OPTION,
                moviesCount > 0 ? JOptionPane.WARNING_MESSAGE : JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean deleted = CategoryDAO.deleteCategory(catId);
            if (deleted) {
                JOptionPane.showMessageDialog(this,
                        "Category '" + catName + "' was deleted.",
                        "Deleted",
                        JOptionPane.INFORMATION_MESSAGE);
                if (editingCategoryId != null && editingCategoryId == catId) {
                    resetToCreateMode();
                }
                refreshCategoryTable();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to delete category.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void refreshCategoryTable() {
        categoryTableModel.setRowCount(0);
        List<Category> categories = CategoryDAO.getAllCategories();
        for (Category cat : categories) {
            int movieCount = CategoryDAO.getMovieCountForCategory(cat.getName());
            categoryTableModel.addRow(new Object[]{
                    cat.getId(),
                    cat.getName(),
                    cat.getDescription().isEmpty() ? "—" : cat.getDescription(),
                    movieCount,
                    cat.getCreatedAt().isEmpty() ? "Default" : cat.getCreatedAt()
            });
        }
        countBadge.setText(categories.size() + " Categories");
    }

    private void filterTable() {
        String query = searchField.getText().trim().toLowerCase();
        categoryTableModel.setRowCount(0);
        List<Category> categories = CategoryDAO.getAllCategories();
        int matched = 0;
        for (Category cat : categories) {
            if (query.isEmpty() || cat.getName().toLowerCase().contains(query)
                    || cat.getDescription().toLowerCase().contains(query)) {
                int movieCount = CategoryDAO.getMovieCountForCategory(cat.getName());
                categoryTableModel.addRow(new Object[]{
                        cat.getId(),
                        cat.getName(),
                        cat.getDescription().isEmpty() ? "—" : cat.getDescription(),
                        movieCount,
                        cat.getCreatedAt().isEmpty() ? "Default" : cat.getCreatedAt()
                });
                matched++;
            }
        }
        countBadge.setText(matched + " Categories");
    }

    private JButton createPillButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btn.setForeground(new Color(79, 70, 229));
        btn.setBackground(new Color(238, 242, 255));
        btn.setFocusPainted(false);
        btn.setBorder(new CompoundBorder(
                new LineBorder(new Color(199, 210, 254), 1, true),
                new EmptyBorder(3, 8, 3, 8)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JPanel createBanner(String titleText, String descText) {
        JPanel banner = new JPanel(new BorderLayout());
        banner.setBackground(Theme.CARD_BG);
        banner.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(18, 22, 18, 22)
        ));

        JLabel title = new JLabel(titleText);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Theme.TEXT_DARK);

        JLabel desc = new JLabel(descText);
        desc.setFont(Theme.FONT_REGULAR);
        desc.setForeground(Theme.TEXT_MUTED);

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);
        titleBlock.add(title);
        titleBlock.add(Box.createVerticalStrut(4));
        titleBlock.add(desc);

        banner.add(titleBlock, BorderLayout.WEST);
        return banner;
    }

    private void styleTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setRowHeight(34);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(241, 245, 249));
        table.getTableHeader().setForeground(Theme.TEXT_DARK);
        table.setSelectionBackground(new Color(237, 233, 254));
        table.setSelectionForeground(Theme.TEXT_DARK);
        table.setShowGrid(true);
        table.setGridColor(Theme.BORDER_COLOR);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);

        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
    }
}
