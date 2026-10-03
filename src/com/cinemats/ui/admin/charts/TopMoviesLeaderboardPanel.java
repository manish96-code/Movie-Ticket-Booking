package com.cinemats.ui.admin.charts;

import com.cinemats.dao.AnalyticsDAO;
import com.cinemats.dao.AnalyticsDAO.MovieRanking;
import com.cinemats.util.Theme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.List;

/**
 * Top Performing Box Office Titles Leaderboard Component
 * with rank chips, revenue meters, and ticket admissions.
 */
public class TopMoviesLeaderboardPanel extends JPanel {

    private List<MovieRanking> rankings;
    private final JPanel rowsContainer;

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

        reloadData();
    }

    public void reloadData() {
        this.rankings = AnalyticsDAO.getTopMovies(5);
        rowsContainer.removeAll();

        Color[] rankColors = {
                new Color(217, 119, 6),    // #1 Gold
                new Color(100, 116, 139),  // #2 Silver/Slate
                new Color(180, 83, 9),     // #3 Bronze
                new Color(71, 85, 105),
                new Color(71, 85, 105)
        };

        for (int i = 0; i < rankings.size(); i++) {
            MovieRanking r = rankings.get(i);
            rowsContainer.add(createMovieRow(i + 1, r, rankColors[Math.min(i, rankColors.length - 1)]));
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

        JLabel title = new JLabel("🎬 Top Grossing Screenings");
        title.setFont(Theme.FONT_HEADER);
        title.setForeground(Theme.TEXT_DARK);

        JLabel sub = new JLabel("Ranked by box office revenue & admissions");
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

    private JPanel createMovieRow(int rank, MovieRanking r, Color rankColor) {
        JPanel row = new JPanel(new BorderLayout(10, 4));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));

        // Rank Badge + Movie Title
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);

        JLabel rankBadge = new JLabel(String.valueOf(rank));
        rankBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        rankBadge.setForeground(Color.WHITE);
        rankBadge.setOpaque(true);
        rankBadge.setBackground(rankColor);
        rankBadge.setHorizontalAlignment(SwingConstants.CENTER);
        rankBadge.setPreferredSize(new Dimension(22, 22));
        rankBadge.setBorder(new LineBorder(rankColor.darker(), 1, true));

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);

        JLabel titleLbl = new JLabel(r.title);
        titleLbl.setFont(Theme.FONT_BOLD_SM);
        titleLbl.setForeground(Theme.TEXT_DARK);

        JLabel genreLbl = new JLabel(r.genre + " • " + r.tickets + " tickets");
        genreLbl.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        genreLbl.setForeground(Theme.TEXT_MUTED);

        titleBlock.add(titleLbl);
        titleBlock.add(genreLbl);

        left.add(rankBadge);
        left.add(titleBlock);

        // Right side: Revenue & Progress bar
        JPanel right = new JPanel(new BorderLayout(0, 4));
        right.setOpaque(false);
        right.setPreferredSize(new Dimension(130, 36));

        JLabel revLbl = new JLabel(String.format("₹%,.0f", r.revenue));
        revLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        revLbl.setForeground(Theme.TEXT_DARK);
        revLbl.setHorizontalAlignment(SwingConstants.RIGHT);

        JProgressBar pb = new JProgressBar(0, 100);
        pb.setValue((int) r.sharePercent);
        pb.setPreferredSize(new Dimension(130, 6));
        pb.setForeground(rank == 1 ? new Color(217, 119, 6) : new Color(37, 99, 235));
        pb.setBackground(new Color(241, 245, 249));
        pb.setBorderPainted(false);

        right.add(revLbl, BorderLayout.NORTH);
        right.add(pb, BorderLayout.SOUTH);

        row.add(left, BorderLayout.WEST);
        row.add(right, BorderLayout.EAST);
        return row;
    }
}
