package com.cinemats.ui.admin.movies;

import com.cinemats.ui.admin.AdminDashboard;
import com.cinemats.util.Theme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Movie Catalogue Management Page
 * Handles movie listing, interactive addition of new titles, and deletion.
 */
public class ManageMoviesPage extends JPanel {

    private final AdminDashboard dashboard;
    private DefaultTableModel movieTableModel;
    private JTable movieTable;

    public ManageMoviesPage(AdminDashboard dashboard) {
        this.dashboard = dashboard;
        setLayout(new BorderLayout(0, 16));
        setBackground(Theme.BG_MAIN);
        setBorder(new EmptyBorder(22, 26, 22, 26));

        initUI();
    }

    private void initUI() {
        add(createBanner("🎬 Movie Catalogue Management",
                "Add, edit, and configure movies, ratings, running durations, and base ticket pricing."),
                BorderLayout.NORTH);

        JPanel card = new JPanel(new BorderLayout(0, 14));
        card.setBackground(Theme.CARD_BG);
        card.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(18, 20, 18, 20)
        ));

        // Top Toolbar
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setOpaque(false);

        JLabel title = new JLabel("Currently Running Titles & Upcoming Features");
        title.setFont(Theme.FONT_HEADER);
        title.setForeground(Theme.TEXT_DARK);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setOpaque(false);

        JButton addBtn = Theme.createPrimaryButton("+ Add Movie");
        addBtn.addActionListener(e -> openAddMovieDialog());

        JButton delBtn = Theme.createSecondaryButton("Remove Selected");
        delBtn.setForeground(Theme.ACCENT_RED);
        delBtn.addActionListener(e -> removeSelectedMovie());

        btnRow.add(addBtn);
        btnRow.add(delBtn);

        toolbar.add(title, BorderLayout.WEST);
        toolbar.add(btnRow, BorderLayout.EAST);
        card.add(toolbar, BorderLayout.NORTH);

        // Movie Table
        String[] cols = {"Movie ID", "Title", "Genre", "Duration", "Rating", "Base Price", "Status"};
        movieTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        movieTableModel.addRow(new Object[]{"MOV-101", "Interstellar", "Sci-Fi / Adventure", "169 min", "PG-13", "$12.00", "Now Showing"});
        movieTableModel.addRow(new Object[]{"MOV-102", "Dune: Part Two", "Sci-Fi / Action", "166 min", "PG-13", "$14.00", "Now Showing"});
        movieTableModel.addRow(new Object[]{"MOV-103", "Spider-Man: Across The Spider-Verse", "Animation / Action", "140 min", "PG", "$11.00", "Now Showing"});
        movieTableModel.addRow(new Object[]{"MOV-104", "Oppenheimer", "Biography / Drama", "180 min", "R", "$13.00", "Now Showing"});
        movieTableModel.addRow(new Object[]{"MOV-105", "Avatar: The Way of Water", "Action / Adventure", "192 min", "PG-13", "$12.50", "Now Showing"});
        movieTableModel.addRow(new Object[]{"MOV-106", "Gladiator II", "Action / History", "148 min", "R", "$14.50", "Upcoming"});

        movieTable = new JTable(movieTableModel);
        styleTable(movieTable);
        card.add(new JScrollPane(movieTable), BorderLayout.CENTER);

        add(card, BorderLayout.CENTER);
    }

    private void openAddMovieDialog() {
        JTextField nameField = Theme.createTextField("Title");
        JTextField genreField = Theme.createTextField("Genre");
        JTextField durationField = Theme.createTextField("Duration (e.g. 150 min)");
        JTextField priceField = Theme.createTextField("Base Price (e.g. 12.00)");
        JComboBox<String> ratingCombo = new JComboBox<>(new String[]{"G", "PG", "PG-13", "R"});
        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"Now Showing", "Upcoming"});

        JPanel form = new JPanel(new GridLayout(6, 2, 8, 10));
        form.add(new JLabel("Movie Title:")); form.add(nameField);
        form.add(new JLabel("Genre:")); form.add(genreField);
        form.add(new JLabel("Duration:")); form.add(durationField);
        form.add(new JLabel("Rating:")); form.add(ratingCombo);
        form.add(new JLabel("Base Price ($):")); form.add(priceField);
        form.add(new JLabel("Status:")); form.add(statusCombo);

        int res = JOptionPane.showConfirmDialog(this, form, "Add New Movie Title", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION) {
            String title = nameField.getText().trim();
            String genre = genreField.getText().trim();
            String dur = durationField.getText().trim();
            String price = priceField.getText().trim();
            if (title.isEmpty() || price.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Title and Price are required.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String id = "MOV-" + (100 + movieTableModel.getRowCount() + 1);
            movieTableModel.addRow(new Object[]{
                    id, title, genre, dur, ratingCombo.getSelectedItem(), "$" + price, statusCombo.getSelectedItem()
            });
            JOptionPane.showMessageDialog(this, "Movie '" + title + "' added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void removeSelectedMovie() {
        int row = movieTable.getSelectedRow();
        if (row >= 0) {
            String movieTitle = (String) movieTableModel.getValueAt(row, 1);
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to remove '" + movieTitle + "' from catalogue?",
                    "Confirm Removal",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                movieTableModel.removeRow(row);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a movie to remove.", "Notice", JOptionPane.INFORMATION_MESSAGE);
        }
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
        table.setRowHeight(32);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(241, 245, 249));
        table.getTableHeader().setForeground(Theme.TEXT_DARK);
        table.setSelectionBackground(new Color(237, 233, 254));
        table.setSelectionForeground(Theme.TEXT_DARK);
        table.setShowGrid(true);
        table.setGridColor(Theme.BORDER_COLOR);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            if (i == 0 || i >= table.getColumnCount() - 2) {
                table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }
        }
    }
}
