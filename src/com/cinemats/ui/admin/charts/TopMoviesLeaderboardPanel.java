package com.cinemats.ui.admin.charts;

import com.cinemats.dao.AnalyticsDAO;
import com.cinemats.dao.AnalyticsDAO.MovieRanking;
import com.cinemats.util.Theme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Top Performing Box Office Titles Leaderboard Component
 * with clean subtle rank chips, refined slate progress meters, and admissions info.
 */
public class TopMoviesLeaderboardPanel extends JPanel {

    private List<MovieRanking> rankings;
    private final JPanel rowsContainer;
    private LocalDate activeDate = LocalDate.now();

    public TopMoviesLeaderboardPanel() {
        setLayout(new BorderLayout(0, 10));
        setBackground(Theme.CARD_BG);
        setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(16, 20, 16, 20)
        ));

        add(buildHeader(), BorderLayout.NORTH);

        rowsContainer = new JPanel();
        rowsContainer.setLayout(new BoxLayout(rowsContainer, BoxLayout.Y_AXIS));
        rowsContainer.setOpaque(false);

        add(rowsContainer, BorderLayout.CENTER);

        reloadData(LocalDate.now());
    }

    public void reloadData() {
        reloadData(this.activeDate);
    }

    public void reloadData(LocalDate date) {
        this.activeDate = (date != null) ? date : LocalDate.now();
        this.rankings = AnalyticsDAO.getTopMovies(5, this.activeDate);
        rowsContainer.removeAll();

        Color[] rankBgColors = {
                new Color(15, 23, 42),     // #1 Deep Slate Navy
                new Color(51, 65, 85),     // #2 Slate 700
                new Color(71, 85, 105),    // #3 Slate 600
                new Color(148, 163, 184),  // #4 Slate 400
                new Color(203, 213, 225)   // #5 Slate 300
        };

        Color[] rankFgColors = {
                Color.WHITE,
                Color.WHITE,
                Color.WHITE,
                Color.WHITE,
                new Color(15, 23, 42)
        };

        for (int i = 0; i < rankings.size(); i++) {
            MovieRanking r = rankings.get(i);
            Color bg = rankBgColors[Math.min(i, rankBgColors.length - 1)];
            Color fg = rankFgColors[Math.min(i, rankFgColors.length - 1)];
            rowsContainer.add(createMovieRow(i + 1, r, bg, fg));
            if (i < rankings.size() - 1) {
                rowsContainer.add(Box.createVerticalStrut(10));
            }
        }

        rowsContainer.revalidate();
        rowsContainer.repaint();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 8, 0));

        JLabel title = new JLabel("Top Grossing Screenings");
        title.setFont(Theme.FONT_HEADER);
        title.setForeground(Theme.TEXT_DARK);

        JLabel sub = new JLabel("Ranked by box office revenue & tickets sold");
        sub.setFont(Theme.FONT_SMALL);
        sub.setForeground(Theme.TEXT_MUTED);

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);
        left.add(title);
        left.add(Box.createVerticalStrut(2));
        left.add(sub);

        header.add(left, BorderLayout.WEST);
        return header;
    }

    private JPanel createMovieRow(int rank, MovieRanking r, Color rankBg, Color rankFg) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        // Rank Badge
        JLabel rankLbl = new JLabel(String.valueOf(rank), SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(rankBg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                super.paintComponent(g);
            }
        };
        rankLbl.setPreferredSize(new Dimension(22, 22));
        rankLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        rankLbl.setForeground(rankFg);

        // Center: Title + Subtitle
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);

        JLabel titleLbl = new JLabel(r.title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titleLbl.setForeground(Theme.TEXT_DARK);

        JLabel subLbl = new JLabel(r.genre + " • " + r.tickets + " tickets");
        subLbl.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        subLbl.setForeground(Theme.TEXT_MUTED);

        center.add(titleLbl);
        center.add(Box.createVerticalStrut(2));
        center.add(subLbl);

        // Right: Revenue + Progress bar
        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setOpaque(false);
        right.setPreferredSize(new Dimension(100, 36));

        JLabel revLbl = new JLabel(String.format("₹%,.0f", r.revenue), SwingConstants.RIGHT);
        revLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        revLbl.setForeground(Theme.TEXT_DARK);
        revLbl.setAlignmentX(Component.RIGHT_ALIGNMENT);

        JProgressBar bar = new JProgressBar(0, 100);
        bar.setValue((int) r.sharePercent);
        bar.setPreferredSize(new Dimension(100, 4));
        bar.setMaximumSize(new Dimension(100, 4));
        bar.setForeground(new Color(15, 23, 42)); // Deep Slate Navy
        bar.setBackground(new Color(241, 245, 249));
        bar.setBorderPainted(false);
        bar.setAlignmentX(Component.RIGHT_ALIGNMENT);

        right.add(revLbl);
        right.add(Box.createVerticalStrut(4));
        right.add(bar);

        row.add(rankLbl, BorderLayout.WEST);
        row.add(center, BorderLayout.CENTER);
        row.add(right, BorderLayout.EAST);

        return row;
    }
}
