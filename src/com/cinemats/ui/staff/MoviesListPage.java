package com.cinemats.ui.staff;
import com.cinemats.dao.MovieDAO;
import com.cinemats.model.Movie;
import com.cinemats.util.Theme;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class MoviesListPage extends JPanel {
    private final StaffDashboard dashboard;
    private JPanel moviesContainer;
    public MoviesListPage() {
        this(null);
    }

    public MoviesListPage(StaffDashboard dashboard) {
        this.dashboard = dashboard;
        setLayout(new BorderLayout());
        setBackground(Theme.BG_MAIN);
        setBorder(new EmptyBorder(24, 28, 24, 28));
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Movies List");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(Theme.TEXT_DARK);
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadMovies());
        header.add(title, BorderLayout.WEST);
        header.add(refreshButton, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);
        moviesContainer = new JPanel(new BorderLayout());
        moviesContainer.setOpaque(false);
        add(moviesContainer, BorderLayout.CENTER);
        loadMovies();
    }

    private void loadMovies() {
        moviesContainer.removeAll();
        MovieDAO movieDAO = new MovieDAO();
        List<Movie> movies = movieDAO.getAllMovies();

        String[] columns = {
                             "ID","Poster","Title","Genre","Duration","Price","Rating","Status"
                           };

        DefaultTableModel model = new DefaultTableModel(columns, 0) {

            @Override
            public boolean isCellEditable(int row,int column ) {
                return false;
            }
        };

        for (Movie movie : movies) {
                model.addRow(new Object[]{
                movie.getId(),
                movie.getPosterLabel(),
                movie.getTitle(),
                movie.getGenre(),
                movie.getDurationMins() + " mins",
                "₹" + movie.getPrice(),
                movie.getRating(),
                movie.getStatus()
            });
        }

        JTable table = new JTable(model);
        table.setRowHeight(35);
        table.setFont(new Font("Segoe UI",Font.PLAIN,14 ) );
        table.getTableHeader().setFont(new Font("Segoe UI",Font.BOLD,14) );

        table.getTableHeader().setReorderingAllowed(false);
        table.setGridColor(new Color(220, 220, 220) );
        table.setSelectionBackground(new Color(230, 240, 255));
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER );

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(center);
        }

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder() );
        moviesContainer.add(scrollPane,BorderLayout.CENTER);
        moviesContainer.revalidate();
        moviesContainer.repaint();
    }

    public StaffDashboard getDashboard() {
        return dashboard;
    }
}
