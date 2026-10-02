package com.cinemats.ui.staff;

import com.cinemats.dao.MovieDAO;
import com.cinemats.dao.ShowDAO;
import com.cinemats.dao.ShowSeatDAO;
import com.cinemats.model.*;
import com.cinemats.service.BookingService;
import com.cinemats.service.CustomerService;
import com.cinemats.ui.staff.booking.TicketConfirmationDialog;
import com.cinemats.util.QRCodeRenderer;
import com.cinemats.util.Theme;
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import com.cinemats.ui.admin.AdminDashboard;

// Enterprise Box Office Movie Ticket Booking Page matching physical screen layouts, rich posters, and counter flow
public class OrderBookingPage extends JPanel {

    private final Component parentComponent;

    // --- 2-Step Workflow: Step 1 (Movies & Shows) -> Step 2 (Seats & Booking Summary) ---
    private final CardLayout stepCardLayout = new CardLayout();
    private final JPanel stepCardsPanel = new JPanel(stepCardLayout);
    public static final String STEP_MOVIES_SHOWS = "STEP_MOVIES_SHOWS";
    public static final String STEP_SEATS_SUMMARY = "STEP_SEATS_SUMMARY";

    // Step 2 Header Navigation & Meta Labels
    private final JLabel step2MovieTitle = new JLabel("-");
    private final JLabel step2ShowMeta = new JLabel("-");

    // --- Step 1: Select Movie (Search Bar + Movie Cards) ---
    private final JTextField movieSearchField = Theme.createTextField("🔍 Search movie by title, genre, lang...");
    private final JPanel movieListPanel = new JPanel();
    private List<Movie> allMoviesList = new ArrayList<>();
    private List<Movie> filteredMoviesList = new ArrayList<>();
    private Movie selectedMovie = null;
    private final Map<Integer, BufferedImage> posterImageCache = new HashMap<>();

    // --- Step 1: Date & Showtime Selection ---
    private final JLabel movieBannerTitle = new JLabel("Select a Movie");
    private final JLabel movieBannerMeta = new JLabel("Choose a movie from the left to view available screening dates and showtimes");
    private final JPanel datesBarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
    private final JPanel showsContainerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));

    // --- Step 2: Exact Seating Matrix ---
    private final JPanel seatHeaderPanel = new JPanel(new BorderLayout());
    private final JLabel seatHeaderTitle = new JLabel("Seating Arrangement");
    private final JPanel seatPanel = new JPanel();
    private JScrollPane seatsScrollPane;
    private final MouseAdapter seatPanner = new MouseAdapter() {
        private Point mousePressPoint;

        @Override
        public void mousePressed(MouseEvent e) {
            mousePressPoint = e.getLocationOnScreen();
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (mousePressPoint == null || seatsScrollPane == null) {
                return;
            }
            Point current = e.getLocationOnScreen();
            int dx = mousePressPoint.x - current.x;
            int dy = mousePressPoint.y - current.y;

            JViewport vp = seatsScrollPane.getViewport();
            Point viewPos = vp.getViewPosition();
            Dimension viewSize = vp.getView().getSize();
            Dimension extentSize = vp.getExtentSize();

            int maxX = Math.max(0, viewSize.width - extentSize.width);
            int maxY = Math.max(0, viewSize.height - extentSize.height);

            int targetX = Math.max(0, Math.min(maxX, viewPos.x + dx));
            int targetY = Math.max(0, Math.min(maxY, viewPos.y + dy));

            vp.setViewPosition(new Point(targetX, targetY));
            mousePressPoint = current;
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            mousePressPoint = null;
        }
    };
    private List<Show> movieShows = new ArrayList<>();
    private String selectedDate = null;
    private Show selectedShow = null;
    private final Map<Integer, ShowSeat> selectedSeats = new LinkedHashMap<>();

    // --- Column 3: Booking Summary, Price, Customer & Payment ---
    private final JLabel summaryMovie = new JLabel("-");
    private final JLabel summaryDate = new JLabel("-");
    private final JLabel summaryTime = new JLabel("-");
    private final JLabel summaryScreen = new JLabel("-");
    private final JLabel summarySeats = new JLabel("No seats selected");
    private final JPanel summaryItemsPanel = new JPanel();
    private final JLabel summaryAmount = new JLabel("₹0.00");
    private final JLabel summarySeatCountLbl = new JLabel("0 seats selected");

    private JTextField customerPhoneField;
    private JTextField customerNameField;
    private JLabel customerBadge;
    private Customer activeCustomer = null;

    private JRadioButton cashRadio;
    private JRadioButton upiRadio;
    private JRadioButton cardRadio;
    private JPanel cashCalcPanel;
    private JTextField cashReceivedField;
    private JLabel changeReturnedLbl;
    private JTextField refField;

    // UPI Dynamic QR Components
    public static final String UPI_VPA = "manish966152@ybl";
    public static final String UPI_PAYEE_NAME = "Cinema Express";
    private JPanel upiQrPanel;
    private JLabel upiQrImageLabel;
    private JLabel upiAmountLabel;
    private JLabel upiStatusBadge;
    private String currentUpiTrxId = "";
    private boolean upiPaymentReceived = false;

    private JButton confirmBookingBtn;
    private JButton resetBtn;

    // --- Bottom: Current Bookings (Today) ---
    private JTable todayBookingsTable;
    private DefaultTableModel bookingsTableModel;
    private List<Booking> recentBookingsList = new ArrayList<>();

    public OrderBookingPage() {
        this((Component) null);
    }

    public OrderBookingPage(StaffDashboard dashboard) {
        this((Component) dashboard);
    }

    public OrderBookingPage(AdminDashboard dashboard) {
        this((Component) dashboard);
    }

    public OrderBookingPage(Component parent) {
        this.parentComponent = parent;
        setLayout(new BorderLayout());
        setBackground(Theme.BG_MAIN);
        setBorder(new EmptyBorder(10, 12, 10, 12));

        initUI();
        loadMovies();
        refreshRecentBookings();
    }

    public StaffDashboard getDashboard() {
        return (parentComponent instanceof StaffDashboard) ? (StaffDashboard) parentComponent : null;
    }

    public Component getParentComponent() {
        return parentComponent;
    }

    public void refreshData() {
        loadMovies();
        refreshRecentBookings();
    }

    private void initUI() {
        stepCardsPanel.setOpaque(false);
        stepCardsPanel.add(buildStep1MoviesAndShowsView(), STEP_MOVIES_SHOWS);
        stepCardsPanel.add(buildStep2SeatsAndSummaryView(), STEP_SEATS_SUMMARY);

        add(stepCardsPanel, BorderLayout.CENTER);
    }

    // STEP 1 VIEW: BROWSE MOVIES & SELECT SHOWTIME
    private JPanel buildStep1MoviesAndShowsView() {
        JPanel view = new JPanel(new BorderLayout(0, 10));
        view.setOpaque(false);

        // Header Banner for Step 1
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        JLabel title = new JLabel("🎬 Book Movie Tickets");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Theme.TEXT_DARK);

        JLabel sub = new JLabel("Step 1 of 2: Select a movie and screening showtime to proceed to seat booking");
        sub.setFont(Theme.FONT_REGULAR);
        sub.setForeground(Theme.TEXT_MUTED);

        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(2));
        titlePanel.add(sub);
        header.add(titlePanel, BorderLayout.WEST);

        view.add(header, BorderLayout.NORTH);

        // Center Split Workspace: Left = Movies, Right = Showtimes
        JPanel splitWorkspace = new JPanel(new GridBagLayout());
        splitWorkspace.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        // Column 1 (46%): Movie Search & Card Grid
        gbc.gridx = 0;
        gbc.weightx = 0.46;
        gbc.insets = new Insets(0, 0, 0, 10);
        JPanel col1 = buildMoviePanel();
        col1.setMinimumSize(new Dimension(360, 0));
        splitWorkspace.add(col1, gbc);

        // Column 2 (54%): Selected Movie Details, Date Pills & Available Shows
        gbc.gridx = 1;
        gbc.weightx = 0.54;
        gbc.insets = new Insets(0, 0, 0, 0);
        JPanel col2 = buildStep1ShowtimesPanel();
        col2.setMinimumSize(new Dimension(420, 0));
        splitWorkspace.add(col2, gbc);

        view.add(splitWorkspace, BorderLayout.CENTER);
        return view;
    }

    private JPanel buildStep1ShowtimesPanel() {
        JPanel panel = createSection("Available Screenings & Showtimes");
        panel.setLayout(new BorderLayout(0, 10));

        // Top: Selected Movie Header Banner
        JPanel movieBanner = new JPanel(new BorderLayout(10, 2));
        movieBanner.setBackground(new Color(248, 250, 252));
        movieBanner.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(10, 14, 10, 14)
        ));

        movieBannerTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        movieBannerTitle.setForeground(Theme.TEXT_DARK);

        movieBannerMeta.setFont(Theme.FONT_REGULAR);
        movieBannerMeta.setForeground(Theme.TEXT_MUTED);

        JPanel bannerText = new JPanel();
        bannerText.setLayout(new BoxLayout(bannerText, BoxLayout.Y_AXIS));
        bannerText.setOpaque(false);
        bannerText.add(movieBannerTitle);
        bannerText.add(Box.createVerticalStrut(3));
        bannerText.add(movieBannerMeta);

        movieBanner.add(bannerText, BorderLayout.CENTER);

        // Center Container: Dates & Shows
        JPanel centerContainer = new JPanel(new BorderLayout(0, 10));
        centerContainer.setOpaque(false);

        // 1. Date Selection (Pills)
        JPanel dateSection = new JPanel(new BorderLayout(4, 4));
        dateSection.setOpaque(false);
        JLabel dateTitle = new JLabel("📅 Select Screening Date");
        dateTitle.setFont(Theme.FONT_BOLD_SM);
        dateTitle.setForeground(new Color(51, 65, 85));
        dateSection.add(dateTitle, BorderLayout.NORTH);

        datesBarPanel.setOpaque(false);
        dateSection.add(datesBarPanel, BorderLayout.CENTER);
        centerContainer.add(dateSection, BorderLayout.NORTH);

        // 2. Available Shows & Screens
        JPanel showSection = new JPanel(new BorderLayout(4, 6));
        showSection.setOpaque(false);

        JPanel showHeader = new JPanel(new BorderLayout());
        showHeader.setOpaque(false);
        JLabel showTitle = new JLabel("🎬 Available Shows & Screens");
        showTitle.setFont(Theme.FONT_BOLD_SM);
        showTitle.setForeground(new Color(51, 65, 85));

        JLabel hint = new JLabel("Click any showtime to select seats →");
        hint.setFont(new Font("Segoe UI", Font.BOLD, 11));
        hint.setForeground(new Color(37, 99, 235));

        showHeader.add(showTitle, BorderLayout.WEST);
        showHeader.add(hint, BorderLayout.EAST);
        showSection.add(showHeader, BorderLayout.NORTH);

        showsContainerPanel.setBackground(Color.WHITE);
        JScrollPane showsScroll = new JScrollPane(showsContainerPanel);
        showsScroll.setBorder(new LineBorder(Theme.BORDER_COLOR, 1, true));
        showsScroll.getViewport().setBackground(Color.WHITE);
        com.cinemats.util.ModernScrollBarUI.apply(showsScroll, 8);

        showSection.add(showsScroll, BorderLayout.CENTER);
        centerContainer.add(showSection, BorderLayout.CENTER);

        panel.add(movieBanner, BorderLayout.NORTH);
        panel.add(centerContainer, BorderLayout.CENTER);
        return panel;
    }

    // ==========================================================
    // STEP 2 VIEW: SEATS & BOOKING SUMMARY
    // ==========================================================
    private JPanel buildStep2SeatsAndSummaryView() {
        JPanel view = new JPanel(new BorderLayout(0, 8));
        view.setOpaque(false);

        // Top Navigation & Movie/Show Banner
        view.add(buildStep2Header(), BorderLayout.NORTH);

        // 2-Column POS Workspace
        JPanel workspace = new JPanel(new GridBagLayout());
        workspace.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        // Column 1 (68%): Seating Arrangement & Layout
        gbc.gridx = 0;
        gbc.weightx = 0.68;
        gbc.insets = new Insets(0, 0, 0, 8);
        JPanel col1 = buildShowAndSeatsPanel();
        col1.setMinimumSize(new Dimension(540, 0));
        workspace.add(col1, gbc);

        // Column 2 (32%): Booking Summary & Payment
        gbc.gridx = 1;
        gbc.weightx = 0.32;
        gbc.insets = new Insets(0, 0, 0, 0);
        JPanel col2 = buildSummaryAndPaymentPanel();
        col2.setMinimumSize(new Dimension(300, 0));
        workspace.add(col2, gbc);

        view.add(workspace, BorderLayout.CENTER);
        return view;
    }

    private JPanel buildStep2Header() {
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setBackground(Color.WHITE);
        header.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(8, 12, 8, 14)
        ));

        // Left: Back button
        JButton backBtn = new JButton("← Back to Movies & Shows") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) {
                    g2.setColor(new Color(241, 245, 249));
                } else {
                    g2.setColor(Color.WHITE);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(new Color(203, 213, 225));
                g2.setStroke(new BasicStroke(1.0f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.setColor(new Color(30, 41, 59));
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int tx = (getWidth() - fm.stringWidth(getText())) / 2;
                int ty = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), tx, ty);
                g2.dispose();
            }
        };
        backBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        backBtn.setPreferredSize(new Dimension(205, 34));
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.setFocusPainted(false);
        backBtn.setBorder(new EmptyBorder(0, 0, 0, 0));
        backBtn.setContentAreaFilled(false);
        backBtn.addActionListener(e -> showStep1());

        // Center: Movie & Show Info
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        step2MovieTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        step2MovieTitle.setForeground(Theme.TEXT_DARK);

        step2ShowMeta.setFont(Theme.FONT_SMALL);
        step2ShowMeta.setForeground(Theme.TEXT_MUTED);

        infoPanel.add(step2MovieTitle);
        infoPanel.add(Box.createVerticalStrut(2));
        infoPanel.add(step2ShowMeta);

        // Right: Step badge
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightPanel.setOpaque(false);

        JLabel stepBadge = new JLabel("Step 2 of 2: Seat Selection & POS");
        stepBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        stepBadge.setForeground(new Color(29, 78, 216));
        stepBadge.setBackground(new Color(239, 246, 255));
        stepBadge.setOpaque(true);
        stepBadge.setBorder(new CompoundBorder(
                new LineBorder(new Color(191, 219, 254), 1, true),
                new EmptyBorder(4, 8, 4, 8)
        ));

        rightPanel.add(stepBadge);

        header.add(backBtn, BorderLayout.WEST);
        header.add(infoPanel, BorderLayout.CENTER);
        header.add(rightPanel, BorderLayout.EAST);

        return header;
    }

    public void showStep1() {
        stepCardLayout.show(stepCardsPanel, STEP_MOVIES_SHOWS);
    }

    public void showStep2() {
        if (selectedMovie != null && selectedShow != null) {
            step2MovieTitle.setText("🎬 " + capitalizeTitle(selectedMovie.getTitle()) + "  •  " + selectedShow.getScreenName() + " (" + selectedShow.getScreenType() + ")");
            step2ShowMeta.setText("📅 " + formatDatePill(selectedShow.getShowDate()) + "  •  ⏰ " + selectedShow.getStartTime() + "  •  " + selectedMovie.getLanguage() + " • " + selectedMovie.getCertificate() + " • " + selectedMovie.getShortDuration());
        }
        stepCardLayout.show(stepCardsPanel, STEP_SEATS_SUMMARY);
    }

    // ==========================================================
    // COLUMN 1: SEARCH BAR + MOVIE LIST
    // ==========================================================
    private JPanel buildMoviePanel() {
        JPanel panel = createSection("Movies");
        panel.setLayout(new BorderLayout(0, 8));

        // Search Bar at Top of Movie Column
        JPanel searchBox = new JPanel(new BorderLayout(4, 0));
        searchBox.setOpaque(false);
        movieSearchField.setPreferredSize(new Dimension(0, 32));
        movieSearchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                filterMovies();
            }

            public void removeUpdate(DocumentEvent e) {
                filterMovies();
            }

            public void changedUpdate(DocumentEvent e) {
                filterMovies();
            }
        });
        searchBox.add(movieSearchField, BorderLayout.CENTER);
        panel.add(searchBox, BorderLayout.NORTH);

        // Scrollable Movie Cards List (Responsive Wrap Grid)
        movieListPanel.setLayout(new com.cinemats.util.WrapLayout(FlowLayout.LEFT, 10, 10));
        movieListPanel.setBackground(new Color(248, 250, 252));
        movieListPanel.setBorder(new EmptyBorder(8, 8, 8, 8));

        JScrollPane cardsScroll = new JScrollPane(movieListPanel);
        cardsScroll.setBorder(new LineBorder(Theme.BORDER_COLOR, 1));
        cardsScroll.getViewport().setBackground(new Color(248, 250, 252));
        cardsScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        cardsScroll.getVerticalScrollBar().setUnitIncrement(16);
        com.cinemats.util.ModernScrollBarUI.apply(cardsScroll, 6);

        panel.add(cardsScroll, BorderLayout.CENTER);
        return panel;
    }

    private void filterMovies() {
        String query = movieSearchField.getText().trim().toLowerCase();
        filteredMoviesList.clear();
        for (Movie m : allMoviesList) {
            boolean match = query.isEmpty()
                    || m.getTitle().toLowerCase().contains(query)
                    || m.getGenre().toLowerCase().contains(query)
                    || m.getLanguage().toLowerCase().contains(query);
            if (match) {
                filteredMoviesList.add(m);
            }
        }
        renderMovieList();
    }

    private void renderMovieList() {
        movieListPanel.removeAll();
        if (filteredMoviesList.isEmpty()) {
            JPanel empty = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 40));
            empty.setOpaque(false);
            JLabel msg = new JLabel("No matching movies found.");
            msg.setFont(Theme.FONT_REGULAR);
            msg.setForeground(Theme.TEXT_MUTED);
            empty.add(msg);
            movieListPanel.add(empty);
        } else {
            for (Movie movie : filteredMoviesList) {
                movieListPanel.add(createMovieGridCard(movie));
            }
        }
        movieListPanel.revalidate();
        movieListPanel.repaint();
    }

    private JPanel createMovieGridCard(Movie movie) {
        final boolean isSelected = (selectedMovie != null && selectedMovie.getId() == movie.getId());
        final String capitalizedTitle = capitalizeTitle(movie.getTitle());
        final BufferedImage posterImg = getOrLoadPosterImage(movie);

        final int CARD_W = 160;
        final int CARD_H = 248;
        final int POSTER_H = 164;
        final int ARC = 12;

        JPanel card = new JPanel() {
            private boolean isHovered = false;

            {
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        isHovered = true;
                        repaint();
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        isHovered = false;
                        repaint();
                    }

                    @Override
                    public void mouseClicked(MouseEvent e) {
                        selectMovie(movie);
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

                int w = getWidth();
                int h = getHeight();

                // 1. Draw Card Shape & Background
                RoundRectangle2D cardShape = new RoundRectangle2D.Float(1, 1, w - 2, h - 2, ARC, ARC);
                g2.setColor(isSelected ? new Color(241, 246, 255) : Color.WHITE);
                g2.fill(cardShape);

                // 2. Poster section (top portion clipped to rounded card corners)
                Shape oldClip = g2.getClip();
                Area posterClip = new Area(cardShape);
                posterClip.intersect(new Area(new Rectangle(0, 0, w, POSTER_H)));
                g2.setClip(posterClip);

                if (posterImg != null) {
                    int iw = posterImg.getWidth();
                    int ih = posterImg.getHeight();
                    double scale = Math.max((double) w / iw, (double) POSTER_H / ih);
                    int sw = (int) (iw * scale);
                    int sh = (int) (ih * scale);
                    int sx = (w - sw) / 2;
                    int sy = (POSTER_H - sh) / 2;
                    g2.drawImage(posterImg, sx, sy, sw, sh, null);

                    // Gradient fade at bottom of poster
                    GradientPaint fade = new GradientPaint(0, POSTER_H - 35, new Color(0, 0, 0, 0), 0, POSTER_H, new Color(0, 0, 0, 110));
                    g2.setPaint(fade);
                    g2.fillRect(0, POSTER_H - 35, w, 35);
                } else {
                    // Fallback cinematic gradient poster
                    int hash = movie.getTitle().hashCode();
                    Color c1 = new Color(28 + Math.abs(hash % 25), 38 + Math.abs(hash % 25), 62 + Math.abs(hash % 35));
                    Color c2 = new Color(15 + Math.abs(hash % 20), 20 + Math.abs(hash % 20), 32 + Math.abs(hash % 20));
                    g2.setPaint(new GradientPaint(0, 0, c1, 0, POSTER_H, c2));
                    g2.fillRect(0, 0, w, POSTER_H);

                    // Film Clapper Icon
                    g2.setColor(new Color(255, 255, 255, 140));
                    g2.drawRoundRect(w / 2 - 16, POSTER_H / 2 - 20, 32, 24, 4, 4);
                    g2.fillRoundRect(w / 2 - 8, POSTER_H / 2 - 14, 16, 12, 2, 2);

                    // Initial letter
                    String init = capitalizedTitle.isEmpty() ? "M" : capitalizedTitle.substring(0, 1);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
                    g2.setColor(new Color(255, 255, 255, 220));
                    FontMetrics ifm = g2.getFontMetrics();
                    g2.drawString(init, (w - ifm.stringWidth(init)) / 2, POSTER_H / 2 + 20);
                }
                g2.setClip(oldClip);

                // Subtle border line between poster and info
                g2.setColor(new Color(226, 232, 240, 120));
                g2.drawLine(1, POSTER_H, w - 2, POSTER_H);

                // 3. Overlaid Badges on Poster
                // Top-Left: Language Badge
                String lang = movie.getLanguage() != null && !movie.getLanguage().isEmpty() ? movie.getLanguage() : "Hindi";
                g2.setFont(new Font("Segoe UI", Font.BOLD, 9));
                FontMetrics fmBadge = g2.getFontMetrics();
                int lWidth = fmBadge.stringWidth(lang) + 10;
                g2.setColor(new Color(15, 23, 42, 210));
                g2.fillRoundRect(6, 6, lWidth, 18, 8, 8);
                g2.setColor(new Color(241, 245, 249));
                g2.drawString(lang, 11, 6 + 13);

                // Top-Right: Rating / Certificate Badge
                String cert = movie.getCertificate() != null && !movie.getCertificate().isEmpty() ? movie.getCertificate() : "UA";
                int cWidth = fmBadge.stringWidth(cert) + 10;
                Color certBg = "A".equalsIgnoreCase(cert) ? new Color(225, 29, 72, 220)
                        : "U".equalsIgnoreCase(cert) ? new Color(16, 185, 129, 220)
                        : new Color(217, 119, 6, 220); // Amber for UA / UA 13+
                g2.setColor(certBg);
                g2.fillRoundRect(w - cWidth - 6, 6, cWidth, 18, 8, 8);
                g2.setColor(Color.WHITE);
                g2.drawString(cert, w - cWidth - 1, 6 + 13);

                // Selected Checkmark Badge (bottom-right of poster)
                if (isSelected) {
                    int bx = w - 26;
                    int by = POSTER_H - 26;
                    g2.setColor(new Color(37, 99, 235));
                    g2.fillOval(bx, by, 20, 20);
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    g2.drawString("✓", bx + 5, by + 15);
                }

                // 4. Text / Details Section (Bottom: POSTER_H to h)
                int textY = POSTER_H + 16;

                // Title
                g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                FontMetrics titleFm = g2.getFontMetrics();
                g2.setColor(isSelected ? new Color(29, 78, 216) : new Color(15, 23, 42));
                String displayTitle = capitalizedTitle;
                if (titleFm.stringWidth(displayTitle) > w - 16) {
                    while (displayTitle.length() > 3 && titleFm.stringWidth(displayTitle + "…") > w - 16) {
                        displayTitle = displayTitle.substring(0, displayTitle.length() - 1);
                    }
                    displayTitle = displayTitle + "…";
                }
                g2.drawString(displayTitle, 8, textY);

                // Genre
                textY += 15;
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                g2.setColor(new Color(100, 116, 139));
                FontMetrics genreFm = g2.getFontMetrics();
                String genre = movie.getGenre();
                if (genre != null && genreFm.stringWidth(genre) > w - 16) {
                    while (genre.length() > 3 && genreFm.stringWidth(genre + "…") > w - 16) {
                        genre = genre.substring(0, genre.length() - 1);
                    }
                    genre = genre + "…";
                }
                g2.drawString(genre != null ? genre : "", 8, textY);

                // Duration & Status Pill
                textY += 15;
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                g2.setColor(new Color(71, 85, 105));
                g2.drawString("⏱ " + movie.getShortDuration(), 8, textY);

                // If Selected: Bottom Accent Indicator Bar
                if (isSelected) {
                    g2.setColor(new Color(37, 99, 235));
                    g2.fillRoundRect(8, h - 5, w - 16, 3, 2, 2);
                }

                // 5. Border & Focus Ring
                if (isSelected) {
                    g2.setColor(new Color(37, 99, 235));
                    g2.setStroke(new BasicStroke(2.0f));
                    g2.drawRoundRect(1, 1, w - 3, h - 3, ARC, ARC);
                } else if (isHovered) {
                    g2.setColor(new Color(148, 163, 184));
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawRoundRect(1, 1, w - 2, h - 2, ARC, ARC);
                } else {
                    g2.setColor(new Color(226, 232, 240));
                    g2.setStroke(new BasicStroke(1.0f));
                    g2.drawRoundRect(1, 1, w - 2, h - 2, ARC, ARC);
                }

                g2.dispose();
            }
        };

        card.setPreferredSize(new Dimension(CARD_W, CARD_H));
        card.setMinimumSize(new Dimension(CARD_W, CARD_H));
        card.setMaximumSize(new Dimension(CARD_W, CARD_H));
        card.setOpaque(false);
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        card.setToolTipText(capitalizedTitle + " (" + movie.getLanguage() + " • " + movie.getCertificate() + " • " + movie.getShortDuration() + ")");

        return card;
    }

    private BufferedImage getOrLoadPosterImage(Movie movie) {
        if (posterImageCache.containsKey(movie.getId())) {
            return posterImageCache.get(movie.getId());
        }
        BufferedImage img = loadPosterImage(movie);
        posterImageCache.put(movie.getId(), img);
        return img;
    }

    private BufferedImage loadPosterImage(Movie movie) {
        String imgPath = movie.getImagePath();
        if (imgPath == null || imgPath.trim().isEmpty()) {
            return null;
        }
        File f = new File(imgPath.trim());
        if (!f.isAbsolute()) {
            f = new File(System.getProperty("user.dir"), imgPath.trim());
        }
        if (!f.exists() || !f.isFile()) {
            f = new File(new File(System.getProperty("user.dir"), "assets/posters"), new File(imgPath.trim()).getName());
        }
        if (f.exists() && f.isFile()) {
            try {
                BufferedImage img = ImageIO.read(f);
                if (img != null) {
                    return img;
                }
            } catch (Exception ignored) {
            }
            try {
                ImageIcon ic = new ImageIcon(f.getAbsolutePath());
                Image raw = ic.getImage();
                if (raw != null && raw.getWidth(null) > 0 && raw.getHeight(null) > 0) {
                    BufferedImage img = new BufferedImage(raw.getWidth(null), raw.getHeight(null), BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g2d = img.createGraphics();
                    g2d.drawImage(raw, 0, 0, null);
                    g2d.dispose();
                    return img;
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    // ==========================================================
    // STEP 2: EXACT SCREEN SEAT MATRIX PANEL
    // ==========================================================
    private JPanel buildShowAndSeatsPanel() {
        JPanel panel = createSection("Show & Seat Selection");
        panel.setLayout(new BorderLayout(0, 8));

        // Seat Layout Panel
        JPanel seatWrapper = new JPanel(new BorderLayout(0, 6));
        seatWrapper.setBackground(Color.WHITE);
        seatWrapper.setBorder(new LineBorder(Theme.BORDER_COLOR, 1, true));

        // Header with Screen Title + Legend
        seatHeaderPanel.setOpaque(false);
        seatHeaderPanel.setBorder(new EmptyBorder(8, 12, 4, 12));
        seatHeaderTitle.setFont(Theme.FONT_BOLD_SM);
        seatHeaderTitle.setForeground(Theme.TEXT_DARK);
        seatHeaderPanel.add(seatHeaderTitle, BorderLayout.WEST);
        seatHeaderPanel.add(buildLegend(), BorderLayout.EAST);
        seatWrapper.add(seatHeaderPanel, BorderLayout.NORTH);

        // Seat Matrix Container
        seatPanel.setLayout(new BoxLayout(seatPanel, BoxLayout.Y_AXIS));
        seatPanel.setBackground(Color.WHITE);
        seatPanel.setBorder(new EmptyBorder(6, 8, 10, 8));
        seatPanel.addMouseListener(seatPanner);
        seatPanel.addMouseMotionListener(seatPanner);

        seatsScrollPane = new JScrollPane(seatPanel);
        seatsScrollPane.setBorder(null);
        seatsScrollPane.getViewport().setBackground(Color.WHITE);
        seatsScrollPane.getViewport().addMouseListener(seatPanner);
        seatsScrollPane.getViewport().addMouseMotionListener(seatPanner);
        com.cinemats.util.ModernScrollBarUI.apply(seatsScrollPane, 8);

        // Smart Mouse Wheel: Shift+Wheel or wheel rotation when horizontally scrollable
        seatsScrollPane.addMouseWheelListener(e -> {
            JScrollBar hBar = seatsScrollPane.getHorizontalScrollBar();
            JScrollBar vBar = seatsScrollPane.getVerticalScrollBar();
            boolean hScrollable = hBar != null && hBar.isVisible() && hBar.getMaximum() > hBar.getVisibleAmount();
            boolean vScrollable = vBar != null && vBar.isVisible() && vBar.getMaximum() > vBar.getVisibleAmount();

            if (e.isShiftDown() || (hScrollable && !vScrollable)) {
                if (hBar != null) {
                    int step = e.getWheelRotation() * 32;
                    hBar.setValue(Math.max(0, Math.min(hBar.getMaximum() - hBar.getVisibleAmount(), hBar.getValue() + step)));
                }
            } else if (hScrollable && vScrollable) {
                int rot = e.getWheelRotation();
                if ((rot > 0 && vBar.getValue() >= vBar.getMaximum() - vBar.getVisibleAmount())
                        || (rot < 0 && vBar.getValue() <= 0)) {
                    int step = rot * 32;
                    hBar.setValue(Math.max(0, Math.min(hBar.getMaximum() - hBar.getVisibleAmount(), hBar.getValue() + step)));
                } else {
                    int step = rot * vBar.getUnitIncrement() * 2;
                    vBar.setValue(vBar.getValue() + step);
                }
            } else if (vBar != null) {
                int step = e.getWheelRotation() * vBar.getUnitIncrement() * 2;
                vBar.setValue(vBar.getValue() + step);
            }
        });

        seatWrapper.add(seatsScrollPane, BorderLayout.CENTER);

        panel.add(seatWrapper, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildLegend() {
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        legend.setOpaque(false);
        legend.add(createLegendDot("Available", new Color(71, 85, 105)));
        legend.add(createLegendDot("Selected", new Color(37, 99, 235)));
        legend.add(createLegendDot("Booked", new Color(225, 29, 72)));
        legend.add(createLegendDot("Blocked (✕)", new Color(148, 163, 184)));
        return legend;
    }

    private JPanel createLegendDot(String text, Color color) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 0));
        p.setOpaque(false);
        JLabel dot = new JLabel("■");
        dot.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        dot.setForeground(color);
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lbl.setForeground(Theme.TEXT_MUTED);
        p.add(dot);
        p.add(lbl);
        return p;
    }

    // ==========================================================
    // COLUMN 3: SUMMARY, PRICE, CUSTOMER DETAILS & PAYMENT
    // ==========================================================
    private JPanel buildSummaryAndPaymentPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new CompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(0, 0, 0, 0)
        ));

        // 1. Header Bar
        JPanel cardHeader = new JPanel(new BorderLayout(8, 0));
        cardHeader.setBackground(new Color(248, 250, 252));
        cardHeader.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, new Color(226, 232, 240)),
                new EmptyBorder(12, 16, 12, 16)
        ));

        JLabel headerTitle = new JLabel("🧾 Booking Summary");
        headerTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        headerTitle.setForeground(new Color(15, 23, 42));

        JLabel counterBadge = new JLabel("Counter POS");
        counterBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        counterBadge.setForeground(new Color(79, 70, 229));
        counterBadge.setBackground(new Color(238, 242, 255));
        counterBadge.setOpaque(true);
        counterBadge.setBorder(new EmptyBorder(3, 8, 3, 8));

        cardHeader.add(headerTitle, BorderLayout.WEST);
        cardHeader.add(counterBadge, BorderLayout.EAST);
        panel.add(cardHeader, BorderLayout.NORTH);

        // 2. Center Content with generous internal padding
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(16, 16, 16, 16));

        // Ticket Overview Slate Box
        JPanel overviewBox = new JPanel();
        overviewBox.setLayout(new BoxLayout(overviewBox, BoxLayout.Y_AXIS));
        overviewBox.setBackground(new Color(248, 250, 252));
        overviewBox.setBorder(new CompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(12, 14, 12, 14)
        ));
        overviewBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel movieRow = new JPanel(new BorderLayout(6, 0));
        movieRow.setOpaque(false);
        movieRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel movieIcon = new JLabel("🎬");
        summaryMovie.setFont(new Font("Segoe UI", Font.BOLD, 13));
        summaryMovie.setForeground(new Color(15, 23, 42));
        movieRow.add(movieIcon, BorderLayout.WEST);
        movieRow.add(summaryMovie, BorderLayout.CENTER);
        overviewBox.add(movieRow);
        overviewBox.add(Box.createVerticalStrut(8));

        JSeparator div1 = new JSeparator();
        div1.setForeground(new Color(226, 232, 240));
        div1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        div1.setAlignmentX(Component.LEFT_ALIGNMENT);
        overviewBox.add(div1);
        overviewBox.add(Box.createVerticalStrut(8));

        overviewBox.add(createMetaRow("📅 Date", summaryDate));
        overviewBox.add(Box.createVerticalStrut(4));
        overviewBox.add(createMetaRow("⏰ Show Time", summaryTime));
        overviewBox.add(Box.createVerticalStrut(4));
        overviewBox.add(createMetaRow("📺 Screen", summaryScreen));
        overviewBox.add(Box.createVerticalStrut(4));
        overviewBox.add(createMetaRow("💺 Seats", summarySeats));

        content.add(overviewBox);
        content.add(Box.createVerticalStrut(12));

        // Itemized Seats Breakdown
        summaryItemsPanel.setLayout(new BoxLayout(summaryItemsPanel, BoxLayout.Y_AXIS));
        summaryItemsPanel.setOpaque(false);
        summaryItemsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(summaryItemsPanel);
        content.add(Box.createVerticalStrut(10));

        // Highlighted Total Amount Card
        JPanel totalCard = new JPanel(new BorderLayout(10, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(240, 253, 244));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(new Color(187, 247, 208));
                g2.setStroke(new BasicStroke(1.0f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
            }
        };
        totalCard.setOpaque(false);
        totalCard.setBorder(new EmptyBorder(12, 14, 12, 14));
        totalCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        totalCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));

        JPanel totalLeft = new JPanel();
        totalLeft.setLayout(new BoxLayout(totalLeft, BoxLayout.Y_AXIS));
        totalLeft.setOpaque(false);

        JLabel totalTitle = new JLabel("Total Amount");
        totalTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        totalTitle.setForeground(new Color(22, 101, 52));

        summarySeatCountLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        summarySeatCountLbl.setForeground(new Color(21, 128, 61));

        totalLeft.add(totalTitle);
        totalLeft.add(Box.createVerticalStrut(2));
        totalLeft.add(summarySeatCountLbl);

        summaryAmount.setFont(new Font("Segoe UI", Font.BOLD, 22));
        summaryAmount.setForeground(new Color(22, 101, 52));

        totalCard.add(totalLeft, BorderLayout.WEST);
        totalCard.add(summaryAmount, BorderLayout.EAST);

        content.add(totalCard);
        content.add(Box.createVerticalStrut(16));

        // 3. Customer Details Section
        JLabel custHeader = new JLabel("CUSTOMER DETAILS");
        custHeader.setFont(new Font("Segoe UI", Font.BOLD, 11));
        custHeader.setForeground(new Color(100, 116, 139));
        custHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(custHeader);
        content.add(Box.createVerticalStrut(6));

        JLabel nameLbl = new JLabel("Customer Name *");
        nameLbl.setFont(Theme.FONT_BOLD_SM);
        nameLbl.setForeground(Theme.TEXT_DARK);
        nameLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(nameLbl);
        content.add(Box.createVerticalStrut(4));

        customerNameField = createStyledInputField("Enter customer name");
        customerNameField.setAlignmentX(Component.LEFT_ALIGNMENT);
        customerNameField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                checkNameInput();
            }

            public void removeUpdate(DocumentEvent e) {
                checkNameInput();
            }

            public void changedUpdate(DocumentEvent e) {
                checkNameInput();
            }
        });
        customerNameField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                String val = customerNameField.getText().trim();
                if (!val.isEmpty()) {
                    customerNameField.setText(capitalizeTitle(val));
                }
            }
        });
        content.add(customerNameField);
        content.add(Box.createVerticalStrut(10));

        JLabel phoneLbl = new JLabel("Mobile Number (10 digits) *");
        phoneLbl.setFont(Theme.FONT_BOLD_SM);
        phoneLbl.setForeground(Theme.TEXT_DARK);
        phoneLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(phoneLbl);
        content.add(Box.createVerticalStrut(4));

        customerPhoneField = createStyledInputField("Enter 10-digit mobile number");
        customerPhoneField.setAlignmentX(Component.LEFT_ALIGNMENT);
        customerPhoneField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                handlePhoneLookup();
            }

            public void removeUpdate(DocumentEvent e) {
                handlePhoneLookup();
            }

            public void changedUpdate(DocumentEvent e) {
                handlePhoneLookup();
            }
        });
        content.add(customerPhoneField);
        content.add(Box.createVerticalStrut(4));

        customerBadge = new JLabel("● Enter 10-digit mobile number");
        customerBadge.setFont(Theme.FONT_SMALL);
        customerBadge.setForeground(Theme.TEXT_MUTED);
        customerBadge.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(customerBadge);
        content.add(Box.createVerticalStrut(16));

        // 4. Payment Method Section
        JLabel payHeader = new JLabel("PAYMENT METHOD");
        payHeader.setFont(new Font("Segoe UI", Font.BOLD, 11));
        payHeader.setForeground(new Color(100, 116, 139));
        payHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(payHeader);
        content.add(Box.createVerticalStrut(6));

        cashRadio = new JRadioButton("Cash", true);
        upiRadio = new JRadioButton("UPI");
        cardRadio = new JRadioButton("Card");

        ButtonGroup bg = new ButtonGroup();
        bg.add(cashRadio);
        bg.add(upiRadio);
        bg.add(cardRadio);

        cashRadio.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        upiRadio.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cardRadio.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cashRadio.setOpaque(false);
        upiRadio.setOpaque(false);
        cardRadio.setOpaque(false);

        cashRadio.addActionListener(e -> updatePaymentModeUI());
        upiRadio.addActionListener(e -> updatePaymentModeUI());
        cardRadio.addActionListener(e -> updatePaymentModeUI());

        JPanel payModes = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        payModes.setOpaque(false);
        payModes.setAlignmentX(Component.LEFT_ALIGNMENT);
        payModes.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        payModes.add(cashRadio);
        payModes.add(upiRadio);
        payModes.add(cardRadio);
        content.add(payModes);
        content.add(Box.createVerticalStrut(8));

        // Cash Calculator Panel
        cashCalcPanel = new JPanel(new BorderLayout(8, 0));
        cashCalcPanel.setOpaque(false);
        cashCalcPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        cashCalcPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        cashReceivedField = createStyledInputField("Cash Received (₹)");
        cashReceivedField.setPreferredSize(new Dimension(130, 34));
        cashReceivedField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                updateCashChange();
            }

            public void removeUpdate(DocumentEvent e) {
                updateCashChange();
            }

            public void changedUpdate(DocumentEvent e) {
                updateCashChange();
            }
        });

        changeReturnedLbl = new JLabel("Change: ₹0.00", SwingConstants.CENTER);
        changeReturnedLbl.setFont(Theme.FONT_BOLD_SM);
        changeReturnedLbl.setForeground(new Color(37, 99, 235));
        changeReturnedLbl.setBackground(new Color(239, 246, 255));
        changeReturnedLbl.setOpaque(true);
        changeReturnedLbl.setBorder(new CompoundBorder(
                new LineBorder(new Color(191, 219, 254), 1, true),
                new EmptyBorder(6, 10, 6, 10)
        ));

        cashCalcPanel.add(cashReceivedField, BorderLayout.WEST);
        cashCalcPanel.add(changeReturnedLbl, BorderLayout.CENTER);
        content.add(cashCalcPanel);

        // UPI Dynamic QR Panel
        upiQrPanel = new JPanel();
        upiQrPanel.setLayout(new BoxLayout(upiQrPanel, BoxLayout.Y_AXIS));
        upiQrPanel.setBackground(new Color(248, 250, 252));
        upiQrPanel.setBorder(new CompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(10, 10, 10, 10)
        ));
        upiQrPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        upiQrPanel.setVisible(false);

        JPanel upiHeader = new JPanel(new BorderLayout());
        upiHeader.setOpaque(false);
        upiHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel upiTitle = new JLabel("📱 Dynamic UPI QR");
        upiTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        upiTitle.setForeground(new Color(67, 56, 202));
        JLabel upiApps = new JLabel("GPay • PhonePe • Paytm");
        upiApps.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        upiApps.setForeground(Theme.TEXT_MUTED);
        upiHeader.add(upiTitle, BorderLayout.WEST);
        upiHeader.add(upiApps, BorderLayout.EAST);
        upiQrPanel.add(upiHeader);
        upiQrPanel.add(Box.createVerticalStrut(6));

        upiAmountLabel = new JLabel("Payable: ₹0.00", SwingConstants.CENTER);
        upiAmountLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        upiAmountLabel.setForeground(new Color(22, 101, 52));
        upiAmountLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        upiQrPanel.add(upiAmountLabel);
        upiQrPanel.add(Box.createVerticalStrut(6));

        JPanel qrFrame = new JPanel(new GridBagLayout());
        qrFrame.setBackground(Color.WHITE);
        qrFrame.setBorder(new CompoundBorder(
                new LineBorder(new Color(203, 213, 225), 1, true),
                new EmptyBorder(4, 4, 4, 4)
        ));
        qrFrame.setAlignmentX(Component.CENTER_ALIGNMENT);
        qrFrame.setPreferredSize(new Dimension(148, 148));
        qrFrame.setMaximumSize(new Dimension(148, 148));

        upiQrImageLabel = new JLabel("", SwingConstants.CENTER);
        qrFrame.add(upiQrImageLabel);
        upiQrPanel.add(qrFrame);
        upiQrPanel.add(Box.createVerticalStrut(6));

        upiStatusBadge = new JLabel("● Scan & Pay with any UPI app", SwingConstants.CENTER);
        upiStatusBadge.setFont(Theme.FONT_SMALL);
        upiStatusBadge.setForeground(new Color(79, 70, 229));
        upiStatusBadge.setAlignmentX(Component.CENTER_ALIGNMENT);
        upiQrPanel.add(upiStatusBadge);
        upiQrPanel.add(Box.createVerticalStrut(8));

        JPanel upiActions = new JPanel(new GridLayout(1, 2, 6, 0));
        upiActions.setOpaque(false);
        upiActions.setAlignmentX(Component.CENTER_ALIGNMENT);
        upiActions.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

        JButton customerDisplayBtn = new JButton("🔍 Expand QR");
        customerDisplayBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        customerDisplayBtn.setFocusPainted(false);
        customerDisplayBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        customerDisplayBtn.setBackground(Color.WHITE);
        customerDisplayBtn.setBorder(new CompoundBorder(
                new LineBorder(new Color(203, 213, 225), 1, true),
                new EmptyBorder(4, 6, 4, 6)
        ));
        customerDisplayBtn.addActionListener(e -> showCustomerQrDialog());

        JButton markPaidBtn = new JButton("✓ Paid");
        markPaidBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        markPaidBtn.setFocusPainted(false);
        markPaidBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        markPaidBtn.setBackground(new Color(240, 253, 244));
        markPaidBtn.setForeground(new Color(22, 101, 52));
        markPaidBtn.setBorder(new CompoundBorder(
                new LineBorder(new Color(187, 247, 208), 1, true),
                new EmptyBorder(4, 6, 4, 6)
        ));
        markPaidBtn.addActionListener(e -> {
            upiPaymentReceived = true;
            refreshUpiQrCode();
        });

        upiActions.add(customerDisplayBtn);
        upiActions.add(markPaidBtn);
        upiQrPanel.add(upiActions);
        content.add(upiQrPanel);

        // Ref field for UPI / Card
        refField = createStyledInputField("Card Slip / Auth Reference");
        refField.setAlignmentX(Component.LEFT_ALIGNMENT);
        refField.setVisible(false);
        content.add(refField);

        content.add(Box.createVerticalGlue());

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        com.cinemats.util.ModernScrollBarUI.apply(scroll, 6);
        panel.add(scroll, BorderLayout.CENTER);

        // 5. Pinned Action Buttons at Bottom
        JPanel bottomActions = new JPanel(new GridLayout(2, 1, 0, 8));
        bottomActions.setBackground(Color.WHITE);
        bottomActions.setBorder(new CompoundBorder(
                new MatteBorder(1, 0, 0, 0, new Color(226, 232, 240)),
                new EmptyBorder(12, 16, 14, 16)
        ));

        confirmBookingBtn = new JButton("Confirm Booking") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (!isEnabled()) {
                    g2.setColor(new Color(226, 232, 240));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    g2.setColor(new Color(148, 163, 184));
                } else if (getModel().isPressed()) {
                    g2.setColor(new Color(29, 78, 216));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    g2.setColor(Color.WHITE);
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(37, 99, 235));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    g2.setColor(Color.WHITE);
                } else {
                    g2.setColor(new Color(30, 64, 175));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    g2.setColor(Color.WHITE);
                }
                FontMetrics fm = g2.getFontMetrics(getFont());
                int tx = (getWidth() - fm.stringWidth(getText())) / 2;
                int ty = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), tx, ty);
                g2.dispose();
            }
        };
        confirmBookingBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        confirmBookingBtn.setPreferredSize(new Dimension(0, 40));
        confirmBookingBtn.setBorder(new EmptyBorder(0, 0, 0, 0));
        confirmBookingBtn.setContentAreaFilled(false);
        confirmBookingBtn.setFocusPainted(false);
        confirmBookingBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        confirmBookingBtn.setEnabled(false);
        confirmBookingBtn.addActionListener(e -> executeBooking());

        resetBtn = new JButton("Reset Form") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? new Color(241, 245, 249) : Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(new Color(203, 213, 225));
                g2.setStroke(new BasicStroke(1.0f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.setColor(new Color(71, 85, 105));
                FontMetrics fm = g2.getFontMetrics(getFont());
                int tx = (getWidth() - fm.stringWidth(getText())) / 2;
                int ty = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), tx, ty);
                g2.dispose();
            }
        };
        resetBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        resetBtn.setPreferredSize(new Dimension(0, 32));
        resetBtn.setBorder(new EmptyBorder(0, 0, 0, 0));
        resetBtn.setContentAreaFilled(false);
        resetBtn.setFocusPainted(false);
        resetBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        resetBtn.addActionListener(e -> resetBookingWorkspace());

        bottomActions.add(confirmBookingBtn);
        bottomActions.add(resetBtn);
        panel.add(bottomActions, BorderLayout.SOUTH);

        return panel;
    }

    private JTextField createStyledInputField(String placeholder) {
        JTextField tf = new JTextField();
        tf.setBackground(Color.WHITE);
        tf.setForeground(Theme.TEXT_DARK);
        tf.setCaretColor(Theme.TEXT_DARK);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tf.setBorder(new CompoundBorder(
                new LineBorder(new Color(203, 213, 225), 1, true),
                new EmptyBorder(7, 10, 7, 10)
        ));
        tf.setPreferredSize(new Dimension(100, 36));
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        return tf;
    }

    private JPanel createMetaRow(String labelText, JLabel valueLabel) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lbl = new JLabel(labelText);
        lbl.setFont(Theme.FONT_SMALL);
        lbl.setForeground(new Color(100, 116, 139));

        valueLabel.setFont(Theme.FONT_BOLD_SM);
        valueLabel.setForeground(new Color(30, 41, 59));

        row.add(lbl, BorderLayout.WEST);
        row.add(valueLabel, BorderLayout.EAST);
        return row;
    }

    private void updatePaymentModeUI() {
        if (cashRadio.isSelected()) {
            cashCalcPanel.setVisible(true);
            if (upiQrPanel != null) {
                upiQrPanel.setVisible(false);
            }
            refField.setVisible(false);
        } else if (upiRadio.isSelected()) {
            cashCalcPanel.setVisible(false);
            if (upiQrPanel != null) {
                upiQrPanel.setVisible(true);
                refreshUpiQrCode();
            }
            refField.setVisible(false);
        } else {
            // Card
            cashCalcPanel.setVisible(false);
            if (upiQrPanel != null) {
                upiQrPanel.setVisible(false);
            }
            refField.setVisible(true);
            refField.setToolTipText("Card Auth / Slip Ref");
        }
        revalidate();
        repaint();
    }

    private void refreshUpiQrCode() {
        if (upiAmountLabel == null || upiQrImageLabel == null) {
            return;
        }
        BigDecimal total = calculateTotal();
        if (total.compareTo(BigDecimal.ZERO) <= 0) {
            upiAmountLabel.setText("Payable: ₹0.00");
            upiQrImageLabel.setIcon(null);
            upiQrImageLabel.setText("<html><center><font color='#64748B' size='2'>Select seats to generate<br>dynamic UPI QR code</font></center></html>");
            upiStatusBadge.setText("● Waiting for seat selection");
            upiStatusBadge.setForeground(Theme.TEXT_MUTED);
            currentUpiTrxId = "";
            return;
        }

        if (currentUpiTrxId == null || currentUpiTrxId.isEmpty()) {
            currentUpiTrxId = "UPI" + (System.currentTimeMillis() % 100000000L);
        }

        String amountStr = String.format(java.util.Locale.US, "%.2f", total.doubleValue());
        upiAmountLabel.setText("Payable: ₹" + amountStr);

        String movieTitle = (selectedMovie != null) ? selectedMovie.getTitle().replaceAll("[^a-zA-Z0-9 ]", "") : "Ticket";
        String note = "Tickets-" + movieTitle.replace(" ", "-");
        if (note.length() > 30) {
            note = note.substring(0, 30);
        }

        String upiPayload;
        try {
            upiPayload = "upi://pay?pa=" + UPI_VPA
                    + "&pn=" + java.net.URLEncoder.encode(UPI_PAYEE_NAME, "UTF-8").replace("+", "%20")
                    + "&am=" + amountStr
                    + "&cu=INR"
                    + "&tn=" + java.net.URLEncoder.encode(note, "UTF-8").replace("+", "%20")
                    + "&tr=" + currentUpiTrxId;
        } catch (Exception ex) {
            upiPayload = "upi://pay?pa=" + UPI_VPA + "&pn=Cinema%20Express&am=" + amountStr + "&cu=INR&tn=Tickets&tr=" + currentUpiTrxId;
        }

        BufferedImage qrImg = QRCodeRenderer.renderQRCode(upiPayload, 140, 140);
        upiQrImageLabel.setText("");
        upiQrImageLabel.setIcon(new ImageIcon(qrImg));

        if (upiPaymentReceived) {
            upiStatusBadge.setText("✓ Payment Verified (Ref: " + currentUpiTrxId + ")");
            upiStatusBadge.setForeground(new Color(22, 163, 74));
        } else {
            upiStatusBadge.setText("● Scan & Pay with any UPI app");
            upiStatusBadge.setForeground(new Color(79, 70, 229));
        }
    }

    private void showCustomerQrDialog() {
        BigDecimal total = calculateTotal();
        if (total.compareTo(BigDecimal.ZERO) <= 0) {
            JOptionPane.showMessageDialog(this, "Please select at least one seat to generate UPI QR code.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        if (currentUpiTrxId == null || currentUpiTrxId.isEmpty()) {
            currentUpiTrxId = "UPI" + (System.currentTimeMillis() % 100000000L);
        }

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Customer UPI QR Payment - Cinema Express", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(390, 530);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(Color.WHITE);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(79, 70, 229));
        header.setBorder(new EmptyBorder(14, 18, 14, 18));
        JLabel title = new JLabel("📱 Cinema Express UPI Payment");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(Color.WHITE);

        header.add(title, BorderLayout.WEST);
        dialog.add(header, BorderLayout.NORTH);

        // Body
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel movieLbl = new JLabel(selectedMovie != null ? capitalizeTitle(selectedMovie.getTitle()) : "Movie Tickets");
        movieLbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        movieLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel showLbl = new JLabel(selectedShow != null ? (selectedShow.getScreenName() + " • " + selectedShow.getStartTime()) : "");
        showLbl.setFont(Theme.FONT_SMALL);
        showLbl.setForeground(Theme.TEXT_MUTED);
        showLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        String amountStr = String.format(java.util.Locale.US, "%.2f", total.doubleValue());
        JLabel amtLbl = new JLabel("₹" + amountStr);
        amtLbl.setFont(new Font("Segoe UI", Font.BOLD, 28));
        amtLbl.setForeground(new Color(22, 163, 74));
        amtLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        String movieTitle = (selectedMovie != null) ? selectedMovie.getTitle().replaceAll("[^a-zA-Z0-9 ]", "") : "Ticket";
        String note = "Tickets-" + movieTitle.replace(" ", "-");
        if (note.length() > 30) {
            note = note.substring(0, 30);
        }

        String upiPayload;
        try {
            upiPayload = "upi://pay?pa=" + UPI_VPA
                    + "&pn=" + java.net.URLEncoder.encode(UPI_PAYEE_NAME, "UTF-8").replace("+", "%20")
                    + "&am=" + amountStr
                    + "&cu=INR"
                    + "&tn=" + java.net.URLEncoder.encode(note, "UTF-8").replace("+", "%20")
                    + "&tr=" + currentUpiTrxId;
        } catch (Exception ex) {
            upiPayload = "upi://pay?pa=" + UPI_VPA + "&pn=Cinema%20Express&am=" + amountStr + "&cu=INR&tn=Tickets&tr=" + currentUpiTrxId;
        }

        BufferedImage largeQr = QRCodeRenderer.renderQRCode(upiPayload, 210, 210);
        JLabel qrLbl = new JLabel(new ImageIcon(largeQr));
        qrLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        qrLbl.setBorder(new CompoundBorder(
                new LineBorder(new Color(203, 213, 225), 1, true),
                new EmptyBorder(8, 8, 8, 8)
        ));

        JLabel scanPrompt = new JLabel("Scan with Google Pay, PhonePe, Paytm, BHIM");
        scanPrompt.setFont(new Font("Segoe UI", Font.BOLD, 11));
        scanPrompt.setForeground(new Color(100, 116, 139));
        scanPrompt.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel refPrompt = new JLabel("Ref: " + currentUpiTrxId);
        refPrompt.setFont(Theme.FONT_SMALL);
        refPrompt.setForeground(Theme.TEXT_MUTED);
        refPrompt.setAlignmentX(Component.CENTER_ALIGNMENT);

        body.add(movieLbl);
        body.add(Box.createVerticalStrut(2));
        body.add(showLbl);
        body.add(Box.createVerticalStrut(8));
        body.add(amtLbl);
        body.add(Box.createVerticalStrut(10));
        body.add(qrLbl);
        body.add(Box.createVerticalStrut(8));
        body.add(scanPrompt);
        body.add(Box.createVerticalStrut(2));
        body.add(refPrompt);

        dialog.add(body, BorderLayout.CENTER);

        // Footer
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        footer.setBackground(new Color(248, 250, 252));
        footer.setBorder(new MatteBorder(1, 0, 0, 0, new Color(226, 232, 240)));

        JButton confirmPaidBtn = Theme.createSuccessButton("✓ Payment Received (Confirm Booking)");
        confirmPaidBtn.addActionListener(e -> {
            upiPaymentReceived = true;
            refreshUpiQrCode();
            dialog.dispose();
            executeBooking();
        });

        JButton closeBtn = Theme.createSecondaryButton("Close");
        closeBtn.addActionListener(e -> dialog.dispose());

        footer.add(confirmPaidBtn);
        footer.add(closeBtn);
        dialog.add(footer, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private void updateCashChange() {
        try {
            String text = cashReceivedField.getText().replaceAll("[^0-9.]", "");
            BigDecimal received = text.isEmpty() ? BigDecimal.ZERO : new BigDecimal(text);
            BigDecimal total = calculateTotal();
            BigDecimal change = received.subtract(total);
            if (change.compareTo(BigDecimal.ZERO) >= 0) {
                changeReturnedLbl.setText(String.format("Change: ₹%.2f", change.doubleValue()));
                changeReturnedLbl.setForeground(new Color(22, 163, 74));
                changeReturnedLbl.setBackground(new Color(240, 253, 244));
            } else {
                changeReturnedLbl.setText("Short: ₹" + total.subtract(received).setScale(2, java.math.RoundingMode.HALF_UP));
                changeReturnedLbl.setForeground(Theme.ACCENT_RED);
                changeReturnedLbl.setBackground(new Color(254, 242, 242));
            }
        } catch (Exception ignored) {
            changeReturnedLbl.setText("Change: ₹0.00");
            changeReturnedLbl.setForeground(new Color(37, 99, 235));
            changeReturnedLbl.setBackground(new Color(239, 246, 255));
        }
    }

    private void checkNameInput() {
        String nameVal = customerNameField.getText().trim();
        String phoneVal = customerPhoneField.getText().trim();
        if (CustomerService.isValidIndianMobile(nameVal) && !CustomerService.isValidIndianMobile(phoneVal)) {
            handlePhoneLookup();
        }
    }

    private void handlePhoneLookup() {
        String phone = customerPhoneField.getText().trim();
        // If phone field does not have a valid mobile, but name field does, use name field
        if (!CustomerService.isValidIndianMobile(phone) && CustomerService.isValidIndianMobile(customerNameField.getText().trim())) {
            phone = customerNameField.getText().trim();
        }

        String normalized = CustomerService.normalizePhone(phone);

        if (CustomerService.isValidIndianMobile(normalized)) {
            Customer existing = CustomerService.findCustomerByPhone(normalized);
            if (existing != null) {
                activeCustomer = existing;
                String capName = capitalizeTitle(existing.getName());
                customerNameField.setText(capName);
                customerBadge.setText("✓ Existing Patron: " + capName);
                customerBadge.setForeground(new Color(22, 163, 74));
            } else {
                activeCustomer = null;
                customerBadge.setText("● New Customer (Will be saved on confirm)");
                customerBadge.setForeground(new Color(37, 99, 235));
            }
        } else if (normalized.length() == 10) {
            customerBadge.setText("⚠ Must start with 6, 7, 8, or 9");
            customerBadge.setForeground(Theme.ACCENT_RED);
        } else if (normalized.length() > 0) {
            activeCustomer = null;
            customerBadge.setText("● Mobile number (" + normalized.length() + "/10 digits)");
            customerBadge.setForeground(Theme.TEXT_MUTED);
        } else {
            activeCustomer = null;
            customerBadge.setText("● Enter 10-digit mobile number");
            customerBadge.setForeground(Theme.TEXT_MUTED);
        }
    }

    // ==========================================================
    // BOTTOM: TODAY'S CURRENT BOOKINGS AUDIT TABLE
    // ==========================================================
    private JPanel buildTodayBookingsCard() {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(0, 130));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(new LineBorder(Theme.BORDER_COLOR), "Current Bookings (Today)"),
                new EmptyBorder(2, 4, 4, 4)));

        String[] columns = {"Booking #", "Customer", "Phone", "Movie", "Date", "Show Time", "Screen", "Seats", "Amount", "Payment", "Status"};
        bookingsTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        todayBookingsTable = new JTable(bookingsTableModel);
        todayBookingsTable.setRowHeight(24);
        todayBookingsTable.setFont(Theme.FONT_REGULAR);
        todayBookingsTable.setShowGrid(false);
        todayBookingsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        todayBookingsTable.getTableHeader().setFont(Theme.FONT_BOLD_SM);
        todayBookingsTable.getTableHeader().setPreferredSize(new Dimension(0, 24));
        todayBookingsTable.getTableHeader().setBackground(new Color(248, 250, 252));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        todayBookingsTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        todayBookingsTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        todayBookingsTable.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
        todayBookingsTable.getColumnModel().getColumn(8).setCellRenderer(centerRenderer);

        todayBookingsTable.getColumnModel().getColumn(10).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                String val = (value == null) ? "" : value.toString();
                l.setFont(new Font("Segoe UI", Font.BOLD, 10));
                if ("CONFIRMED".equalsIgnoreCase(val)) {
                    l.setForeground(new Color(22, 163, 74));
                    l.setText("● CONFIRMED");
                } else {
                    l.setForeground(new Color(225, 29, 72));
                    l.setText("● CANCELLED");
                }
                return l;
            }
        });

        JScrollPane scroll = new JScrollPane(todayBookingsTable);
        scroll.setBorder(new LineBorder(Theme.BORDER_COLOR, 1));
        com.cinemats.util.ModernScrollBarUI.apply(scroll, 8);
        card.add(scroll, BorderLayout.CENTER);

        return card;
    }

    // ==========================================================
    // LOGIC: LOADING MOVIES & INTERACTIVE WORKFLOW
    // ==========================================================
    public void loadMovies() {
        posterImageCache.clear();
        allMoviesList = MovieDAO.getAllMovies();
        filteredMoviesList = new ArrayList<>(allMoviesList);
        renderMovieList();

        if (!allMoviesList.isEmpty()) {
            selectMovie(allMoviesList.get(0));
        } else {
            showSeatMessage("No movies are available in the database.");
        }
    }

    public static String capitalizeTitle(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }
        String[] words = text.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (!w.isEmpty()) {
                sb.append(Character.toUpperCase(w.charAt(0)));
                if (w.length() > 1) {
                    sb.append(w.substring(1).toLowerCase());
                }
                sb.append(" ");
            }
        }
        return sb.toString().trim();
    }

    private void selectMovie(Movie movie) {
        if (movie == null) {
            return;
        }
        selectedMovie = movie;

        // Update Movie Banner in Column 2
        movieBannerTitle.setText(capitalizeTitle(movie.getTitle()));
        movieBannerMeta.setText(movie.getLanguage() + " • " + movie.getCertificate() + " • " + movie.getGenre() + " • " + movie.getShortDuration());

        // Refresh Movie List Highlight
        renderMovieList();

        selectedSeats.clear();
        movieShows = ShowDAO.getShowsByMovie(movie.getId());
        movieShows.removeIf(show -> !show.isBookable());

        renderDatesAndShows();
        updateSummary();
    }

    private void renderDatesAndShows() {
        datesBarPanel.removeAll();
        showsContainerPanel.removeAll();

        if (movieShows.isEmpty()) {
            JLabel noShows = new JLabel("No upcoming screenings available for booking.");
            noShows.setFont(Theme.FONT_REGULAR);
            noShows.setForeground(Theme.TEXT_MUTED);
            datesBarPanel.add(noShows);

            selectedDate = null;
            selectedShow = null;
            loadSeatsForSelectedShow();
            datesBarPanel.revalidate();
            datesBarPanel.repaint();
            showsContainerPanel.revalidate();
            showsContainerPanel.repaint();
            return;
        }

        // Collect unique dates
        TreeSet<String> dates = new TreeSet<>();
        for (Show s : movieShows) {
            if (s.getShowDate() != null && !s.getShowDate().isEmpty()) {
                dates.add(s.getShowDate());
            }
        }

        if (selectedDate == null || !dates.contains(selectedDate)) {
            selectedDate = dates.first();
        }

        for (String d : dates) {
            JButton datePill = createDatePill(d);
            datesBarPanel.add(datePill);
        }

        renderShowsForSelectedDate();
        datesBarPanel.revalidate();
        datesBarPanel.repaint();
    }

    private JButton createDatePill(String dateStr) {
        boolean isSelected = dateStr.equals(selectedDate);
        String labelText = formatDatePill(dateStr);

        JButton btn = new JButton(labelText);
        btn.setFont(Theme.FONT_BOLD_SM);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        if (isSelected) {
            btn.setBackground(new Color(37, 99, 235));
            btn.setForeground(Color.WHITE);
            btn.setBorder(new CompoundBorder(
                    new LineBorder(new Color(29, 78, 216), 1, true),
                    new EmptyBorder(5, 12, 5, 12)
            ));
        } else {
            btn.setBackground(Color.WHITE);
            btn.setForeground(Theme.TEXT_DARK);
            btn.setBorder(new CompoundBorder(
                    new LineBorder(new Color(203, 213, 225), 1, true),
                    new EmptyBorder(5, 12, 5, 12)
            ));
        }

        btn.addActionListener(e -> {
            selectedDate = dateStr;
            renderDatesAndShows();
        });

        return btn;
    }

    public static String formatDatePill(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return "";
        }
        try {
            LocalDate d = LocalDate.parse(dateStr.trim());
            LocalDate today = LocalDate.now();
            String formatted = d.format(DateTimeFormatter.ofPattern("dd MMM"));
            if (d.equals(today)) {
                return "Today, " + formatted;
            } else if (d.equals(today.plusDays(1))) {
                return "Tomorrow, " + formatted;
            } else {
                return d.format(DateTimeFormatter.ofPattern("EEE, dd MMM"));
            }
        } catch (Exception e) {
            return dateStr;
        }
    }

    private void renderShowsForSelectedDate() {
        showsContainerPanel.removeAll();

        List<Show> matchingShows = new ArrayList<>();
        for (Show s : movieShows) {
            if (selectedDate != null && selectedDate.equals(s.getShowDate())) {
                matchingShows.add(s);
            }
        }

        if (matchingShows.isEmpty()) {
            JLabel noShows = new JLabel("No screenings scheduled on " + selectedDate);
            noShows.setFont(Theme.FONT_REGULAR);
            noShows.setForeground(Theme.TEXT_MUTED);
            showsContainerPanel.add(noShows);

            selectedShow = null;
            loadSeatsForSelectedShow();
            showsContainerPanel.revalidate();
            showsContainerPanel.repaint();
            return;
        }

        // Auto select first show if current is invalid
        if (selectedShow == null || !matchingShows.contains(selectedShow)) {
            selectedShow = matchingShows.get(0);
        }

        for (Show s : matchingShows) {
            showsContainerPanel.add(createShowTile(s));
        }

        loadSeatsForSelectedShow();
        showsContainerPanel.revalidate();
        showsContainerPanel.repaint();
    }

    private JPanel createShowTile(Show show) {
        boolean isSelected = (selectedShow != null && selectedShow.getId() == show.getId());

        JPanel tile = new JPanel(new BorderLayout(6, 4)) {
            private boolean isHovered = false;

            {
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        isHovered = true;
                        repaint();
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        isHovered = false;
                        repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();

                if (isSelected || isHovered) {
                    g2.setColor(new Color(239, 246, 255));
                    g2.fillRoundRect(2, 2, w - 4, h - 4, 10, 10);
                    g2.setColor(new Color(37, 99, 235));
                    g2.setStroke(new BasicStroke(1.8f));
                    g2.drawRoundRect(2, 2, w - 4, h - 4, 10, 10);
                } else {
                    g2.setColor(Color.WHITE);
                    g2.fillRoundRect(2, 2, w - 4, h - 4, 10, 10);
                    g2.setColor(new Color(226, 232, 240));
                    g2.setStroke(new BasicStroke(1.0f));
                    g2.drawRoundRect(2, 2, w - 4, h - 4, 10, 10);
                }
                g2.dispose();
            }
        };

        tile.setPreferredSize(new Dimension(210, 72));
        tile.setCursor(new Cursor(Cursor.HAND_CURSOR));
        tile.setBorder(new EmptyBorder(8, 12, 8, 12));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        // Screen Name & Type
        JPanel screenRow = new JPanel(new BorderLayout(4, 0));
        screenRow.setOpaque(false);
        JLabel screenLbl = new JLabel(show.getScreenName());
        screenLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        screenLbl.setForeground(new Color(30, 41, 59));

        JLabel typePill = new JLabel(" " + show.getScreenType() + " ");
        typePill.setFont(new Font("Segoe UI", Font.BOLD, 10));
        typePill.setForeground(new Color(79, 70, 229));
        typePill.setBackground(new Color(238, 242, 255));
        typePill.setOpaque(true);
        typePill.setBorder(new LineBorder(new Color(199, 210, 254), 1, true));

        screenRow.add(screenLbl, BorderLayout.WEST);
        screenRow.add(typePill, BorderLayout.EAST);

        // Time
        JLabel timeLbl = new JLabel("⏰ " + show.getStartTime());
        timeLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        timeLbl.setForeground(new Color(37, 99, 235));

        // Seats & Action
        JPanel botRow = new JPanel(new BorderLayout(4, 0));
        botRow.setOpaque(false);

        JLabel seatsLbl;
        if (show.isStarted()) {
            int minsLeft = show.getMinutesUntilCutoff();
            seatsLbl = new JLabel("● " + show.getAvailableSeats() + " left • " + minsLeft + "m");
            seatsLbl.setFont(Theme.FONT_SMALL);
            seatsLbl.setForeground(new Color(217, 119, 6)); // Amber warning
            seatsLbl.setToolTipText("Show started! Booking window closes in " + minsLeft + " minutes.");
        } else {
            seatsLbl = new JLabel("● " + show.getAvailableSeats() + " seats left");
            seatsLbl.setFont(Theme.FONT_SMALL);
            seatsLbl.setForeground(new Color(22, 163, 74));
        }

        JLabel actionLbl = new JLabel("Book Seats →");
        actionLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        actionLbl.setForeground(new Color(37, 99, 235));

        botRow.add(seatsLbl, BorderLayout.WEST);
        botRow.add(actionLbl, BorderLayout.EAST);

        content.add(screenRow);
        content.add(Box.createVerticalStrut(2));
        content.add(timeLbl);
        content.add(Box.createVerticalStrut(2));
        content.add(botRow);

        tile.add(content, BorderLayout.CENTER);

        tile.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectShow(show);
            }
        });

        return tile;
    }

    private void selectShow(Show show) {
        if (show == null) {
            return;
        }
        selectedShow = show;
        renderShowsForSelectedDate();
        loadSeatsForSelectedShow();
        updateSummary();
        showStep2();
    }

    // ==========================================================
    // EXACT SCREEN SEAT MATRIX (MATCHING SeatLayoutPage)
    // ==========================================================
    private void loadSeatsForSelectedShow() {
        selectedSeats.clear();
        seatPanel.removeAll();

        if (selectedShow == null) {
            seatHeaderTitle.setText("Seating Arrangement");
            showSeatMessage("Please select an available showtime above to view the seating layout.");
            updateSummary();
            return;
        }

        seatHeaderTitle.setText(selectedShow.getScreenName() + " (" + selectedShow.getScreenType() + ") • " + selectedShow.getStartTime() + " Seating");

        List<ShowSeat> showSeats = ShowSeatDAO.getShowSeatsByShowId(selectedShow.getId());

        // If seats not yet generated for this show, generate them automatically from physical screen seats
        if (showSeats.isEmpty()) {
            try (java.sql.Connection conn = com.cinemats.config.DBConnection.getConnection()) {
                List<ShowPrice> prices = com.cinemats.dao.ShowPriceDAO.getPricesByShowId(selectedShow.getId());
                ShowSeatDAO.generateShowSeats(selectedShow.getId(), selectedShow.getScreenId(), prices, conn);
                showSeats = ShowSeatDAO.getShowSeatsByShowId(selectedShow.getId());
            } catch (Exception ex) {
                System.err.println("[OrderBookingPage] Auto-generating seats error: " + ex.getMessage());
            }
        }

        if (showSeats.isEmpty()) {
            showSeatMessage("This show has no seat inventory configured.");
            updateSummary();
            return;
        }

        // Group seats by row (A, B, C...)
        Map<String, List<ShowSeat>> seatsByRow = new LinkedHashMap<>();
        for (ShowSeat ss : showSeats) {
            String label = ss.getSeatLabel();
            String row = label.replaceAll("[0-9]", "");
            if (row.isEmpty()) {
                row = "Seats";
            }
            seatsByRow.computeIfAbsent(row, k -> new ArrayList<>()).add(ss);
        }

        // Build row position map (0 = farthest from screen e.g. Row A; highest = closest to screen e.g. Row J)
        List<String> rowOrderList = new ArrayList<>(seatsByRow.keySet());
        Map<String, Integer> rowIndexMap = new HashMap<>();
        for (int i = 0; i < rowOrderList.size(); i++) {
            rowIndexMap.put(rowOrderList.get(i), i);
        }

        // Auto-select first available seat starting from REGULAR -> PREMIUM -> RECLINER,
        // and within each tier starting from the front row closest to the screen (e.g. J1, J2...)
        ShowSeat autoSelectSeat = null;
        List<ShowSeat> availableCandidates = new ArrayList<>();
        for (ShowSeat ss : showSeats) {
            if ("AVAILABLE".equalsIgnoreCase(ss.getStatus())) {
                availableCandidates.add(ss);
            }
        }

        if (!availableCandidates.isEmpty()) {
            availableCandidates.sort((s1, s2) -> {
                // 1. Tier Priority: REGULAR (1) -> PREMIUM (2) -> RECLINER (3)
                int p1 = getTierPriority(s1.getSeatType());
                int p2 = getTierPriority(s2.getSeatType());
                if (p1 != p2) {
                    return Integer.compare(p1, p2);
                }

                // 2. Row Position: Closest to screen first (higher row index in hall, e.g. Row J before Row I)
                String r1 = extractRow(s1.getSeatLabel());
                String r2 = extractRow(s2.getSeatLabel());
                int idx1 = rowIndexMap.getOrDefault(r1, 0);
                int idx2 = rowIndexMap.getOrDefault(r2, 0);
                if (idx1 != idx2) {
                    return Integer.compare(idx2, idx1); // descending
                }

                // 3. Seat Number: Starting from seat 1 (1, 2, 3...)
                int n1 = parseSeatNum(s1.getSeatLabel());
                int n2 = parseSeatNum(s2.getSeatLabel());
                return Integer.compare(n1, n2);
            });
            autoSelectSeat = availableCandidates.get(0);
        }

        if (autoSelectSeat != null) {
            selectedSeats.put(autoSelectSeat.getId(), autoSelectSeat);
        }

        String currentTier = null;

        for (Map.Entry<String, List<ShowSeat>> entry : seatsByRow.entrySet()) {
            String rowName = entry.getKey();
            List<ShowSeat> seatsInRow = entry.getValue();
            seatsInRow.sort(Comparator.comparingInt(s -> parseSeatNum(s.getSeatLabel())));

            String rowTier = seatsInRow.isEmpty() ? "REGULAR" : seatsInRow.get(0).getSeatType().toUpperCase();
            String rowPrice = (!seatsInRow.isEmpty() && seatsInRow.get(0).getPrice() != null)
                    ? ("₹" + seatsInRow.get(0).getPrice().toPlainString()) : "₹150.00";

            // Tier Header (SILVER, GOLD, RECLINER)
            if (!rowTier.equalsIgnoreCase(currentTier)) {
                currentTier = rowTier;
                seatPanel.add(Box.createVerticalStrut(12));
                seatPanel.add(createTierHeader(currentTier, rowPrice));
                seatPanel.add(Box.createVerticalStrut(8));
            }

            JPanel seatRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 2));
            seatRow.setOpaque(false);
            seatRow.addMouseListener(seatPanner);
            seatRow.addMouseMotionListener(seatPanner);

            // Left Row Badge
            seatRow.add(createRowBadge(rowName));
            seatRow.add(Box.createHorizontalStrut(6));

            int totalInRow = seatsInRow.size();
            int leftCount, rightCount;
            if (totalInRow >= 12) {
                leftCount = 4;
                rightCount = 4;
            } else if (totalInRow >= 9) {
                leftCount = 3;
                rightCount = 3;
            } else if (totalInRow >= 6) {
                leftCount = 2;
                rightCount = 2;
            } else {
                leftCount = 0;
                rightCount = 0;
            }
            int centerEnd = totalInRow - rightCount;

            for (int i = 0; i < totalInRow; i++) {
                ShowSeat ss = seatsInRow.get(i);
                JToggleButton btn = createSeatButton(ss);
                if (autoSelectSeat != null && ss.getId() == autoSelectSeat.getId()) {
                    btn.setSelected(true);
                }
                seatRow.add(btn);

                // Left Aisle Walkway (32px gap)
                if (leftCount > 0 && i == leftCount - 1) {
                    seatRow.add(Box.createHorizontalStrut(32));
                }
                // Right Aisle Walkway (32px gap)
                if (rightCount > 0 && i == centerEnd - 1) {
                    seatRow.add(Box.createHorizontalStrut(32));
                }
            }

            // Right Row Badge
            seatRow.add(Box.createHorizontalStrut(6));
            seatRow.add(createRowBadge(rowName));

            seatPanel.add(seatRow);
        }

        // Curved 3D Projection Screen Graphic at bottom
        seatPanel.add(Box.createVerticalStrut(16));
        seatPanel.add(createScreenGraphic());
        seatPanel.add(Box.createVerticalStrut(6));

        seatPanel.revalidate();
        seatPanel.repaint();
        updateSummary();
    }

    private JToggleButton createSeatButton(ShowSeat ss) {
        boolean isBooked = "BOOKED".equalsIgnoreCase(ss.getStatus());
        boolean isBlocked = "BLOCKED".equalsIgnoreCase(ss.getStatus());
        String type = ss.getSeatType();
        String displayNum = ss.getSeatLabel().replaceAll("[^0-9]", "");
        BigDecimal price = ss.getPrice() != null ? ss.getPrice() : BigDecimal.valueOf(150.0);

        JToggleButton btn = new JToggleButton(displayNum) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                Color bg;
                Color border;
                Color text;
                float strokeWidth = 1.3f;
                boolean drawCross = false;

                if (isSelected()) {
                    bg = new Color(37, 99, 235);     // Royal Blue
                    border = new Color(29, 78, 216);
                    text = Color.WHITE;
                    strokeWidth = 2.0f;
                } else if (isBooked) {
                    bg = new Color(254, 226, 226);   // Soft Red Muted
                    border = new Color(248, 113, 113);
                    text = new Color(225, 29, 72);
                    drawCross = true;
                } else if (isBlocked) {
                    bg = new Color(248, 250, 252);   // Slate blocked
                    border = new Color(203, 213, 225);
                    text = new Color(148, 163, 184);
                    drawCross = true;
                } else if ("RECLINER".equalsIgnoreCase(type)) {
                    bg = new Color(254, 243, 199);   // Amber glow
                    border = new Color(217, 119, 6);
                    text = new Color(120, 53, 15);
                    strokeWidth = 1.5f;
                } else if ("PREMIUM".equalsIgnoreCase(type)) {
                    bg = new Color(240, 249, 255);   // Ice-blue
                    border = new Color(56, 189, 248);
                    text = new Color(15, 23, 42);
                    strokeWidth = 1.5f;
                } else {
                    bg = Color.WHITE;                // Regular white
                    border = new Color(71, 85, 105);
                    text = new Color(15, 23, 42);
                }

                // Rounded seat
                int arc = 8;
                g2.setColor(bg);
                g2.fillRoundRect(2, 2, w - 4, h - 4, arc, arc);

                g2.setColor(border);
                g2.setStroke(new BasicStroke(strokeWidth));
                g2.drawRoundRect(2, 2, w - 4, h - 4, arc, arc);

                if (drawCross) {
                    g2.setColor(text);
                    g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    int pad = 10;
                    g2.drawLine(pad, pad, w - pad, h - pad);
                    g2.drawLine(w - pad, pad, pad, h - pad);
                } else {
                    g2.setColor(text);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
                    FontMetrics fm = g2.getFontMetrics();
                    String t = getText();
                    int tx = (w - fm.stringWidth(t)) / 2;
                    int ty = (h - fm.getHeight()) / 2 + fm.getAscent();
                    g2.drawString(t, tx, ty);
                }

                g2.dispose();
            }
        };

        boolean isRecliner = "RECLINER".equalsIgnoreCase(type);
        Dimension size = isRecliner ? new Dimension(42, 34) : new Dimension(34, 34);
        btn.setPreferredSize(size);
        btn.setMinimumSize(size);
        btn.setMaximumSize(size);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);

        if (isBooked || isBlocked) {
            btn.setEnabled(false);
            btn.setToolTipText("Seat " + ss.getSeatLabel() + " (" + ss.getStatus() + ")");
        } else {
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.setToolTipText(ss.getSeatLabel() + " • " + type + " • ₹" + price.toPlainString());
            btn.addActionListener(e -> {
                if (btn.isSelected()) {
                    selectedSeats.put(ss.getId(), ss);
                } else {
                    selectedSeats.remove(ss.getId());
                }
                updateSummary();
            });
        }

        return btn;
    }

    private JLabel createRowBadge(String rowName) {
        JLabel lbl = new JLabel(rowName, SwingConstants.CENTER);
        lbl.setFont(Theme.FONT_BOLD_SM);
        lbl.setForeground(Theme.TEXT_MUTED);
        lbl.setPreferredSize(new Dimension(28, 34));
        lbl.setBackground(Theme.CARD_HOVER);
        lbl.setOpaque(true);
        lbl.setBorder(new LineBorder(Theme.BORDER_COLOR, 1, true));
        return lbl;
    }

    private JPanel createTierHeader(String tierName, String price) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        p.setOpaque(false);

        String title = "REGULAR".equalsIgnoreCase(tierName) ? "SILVER / REGULAR"
                : ("PREMIUM".equalsIgnoreCase(tierName) ? "GOLD / PREMIUM"
                : ("RECLINER".equalsIgnoreCase(tierName) ? "PLATINUM RECLINER" : tierName));

        Color badgeColor = "PREMIUM".equalsIgnoreCase(tierName) ? new Color(59, 130, 246)
                : ("RECLINER".equalsIgnoreCase(tierName) ? new Color(217, 119, 6)
                : new Color(71, 85, 105));

        JLabel lbl = new JLabel("━━━  " + title + " : " + price + "  ━━━");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(badgeColor);
        p.add(lbl);

        p.addMouseListener(seatPanner);
        p.addMouseMotionListener(seatPanner);

        return p;
    }

    private JPanel createScreenGraphic() {
        JPanel p = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int screenW = Math.min(Math.max(w - 140, 280), 440);
                int startX = (w - screenW) / 2;
                int endX = startX + screenW;

                int insetX = 24;
                int topY = 6;
                int botY = 24;

                java.awt.geom.Path2D.Float path = new java.awt.geom.Path2D.Float();
                path.moveTo(startX + insetX, topY);
                path.curveTo(w / 2f, topY - 4, w / 2f, topY - 4, endX - insetX, topY);
                path.lineTo(endX, botY);
                path.curveTo(w / 2f, botY - 3, w / 2f, botY - 3, startX, botY);
                path.closePath();

                GradientPaint gp = new GradientPaint(w / 2f, topY, new Color(221, 214, 254), w / 2f, botY, new Color(196, 181, 253));
                g2.setPaint(gp);
                g2.fill(path);

                g2.setColor(new Color(167, 139, 250));
                g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.draw(path);

                g2.setColor(new Color(109, 40, 217));
                g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                FontMetrics fm = g2.getFontMetrics();
                String text = "SCREEN THIS WAY";
                int tx = (w - fm.stringWidth(text)) / 2;
                g2.drawString(text, tx, botY + 14);

                g2.dispose();
            }
        };
        p.setPreferredSize(new Dimension(0, 44));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        p.setOpaque(false);
        p.addMouseListener(seatPanner);
        p.addMouseMotionListener(seatPanner);
        return p;
    }

    private void showSeatMessage(String msg) {
        seatPanel.removeAll();
        JLabel l = new JLabel(msg, SwingConstants.CENTER);
        l.setFont(Theme.FONT_REGULAR);
        l.setForeground(Theme.TEXT_MUTED);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        seatPanel.add(Box.createVerticalGlue());
        seatPanel.add(l);
        seatPanel.add(Box.createVerticalGlue());
        seatPanel.revalidate();
        seatPanel.repaint();
    }

    private int parseSeatNum(String label) {
        try {
            return Integer.parseInt(label.replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            return 0;
        }
    }

    private int getTierPriority(String seatType) {
        if (seatType == null) return 99;
        String t = seatType.trim().toUpperCase();
        if (t.contains("REGULAR") || t.contains("SILVER") || t.contains("STANDARD")) {
            return 1;
        } else if (t.contains("PREMIUM") || t.contains("GOLD") || t.contains("PRIME")) {
            return 2;
        } else if (t.contains("RECLINER") || t.contains("PLATINUM") || t.contains("VIP") || t.contains("LOUNGE")) {
            return 3;
        }
        return 4;
    }

    private String extractRow(String label) {
        if (label == null) return "";
        String row = label.replaceAll("[0-9]", "").trim().toUpperCase();
        return row.isEmpty() ? "SEATS" : row;
    }

    // ==========================================================
    // SUMMARY, PRICE RECALCULATION & BOOKING EXECUTION
    // ==========================================================
    private void updateSummary() {
        summaryMovie.setText(selectedMovie == null ? "-" : capitalizeTitle(selectedMovie.getTitle()));
        summaryDate.setText(selectedShow == null ? "-" : selectedShow.getShowDate());
        summaryTime.setText(selectedShow == null ? "-" : selectedShow.getStartTime());
        summaryScreen.setText(selectedShow == null ? "-" : (selectedShow.getScreenName() + " (" + selectedShow.getScreenType() + ")"));

        if (selectedSeats.isEmpty()) {
            summarySeats.setText("No seats selected");
            summarySeats.setForeground(new Color(148, 163, 184));
            summarySeatCountLbl.setText("0 seats selected");
        } else {
            List<String> labels = new ArrayList<>();
            for (ShowSeat ss : selectedSeats.values()) {
                labels.add(ss.getSeatLabel());
            }
            summarySeats.setText(String.join(", ", labels));
            summarySeats.setForeground(new Color(37, 99, 235));
            int count = selectedSeats.size();
            summarySeatCountLbl.setText(count + (count == 1 ? " seat selected" : " seats selected"));
        }

        // Itemized breakdown
        summaryItemsPanel.removeAll();
        BigDecimal total = BigDecimal.ZERO;

        if (!selectedSeats.isEmpty()) {
            JLabel breakdownHeader = new JLabel("SEATS BREAKDOWN");
            breakdownHeader.setFont(new Font("Segoe UI", Font.BOLD, 10));
            breakdownHeader.setForeground(new Color(100, 116, 139));
            breakdownHeader.setBorder(new EmptyBorder(4, 0, 4, 0));
            breakdownHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
            summaryItemsPanel.add(breakdownHeader);
        }

        for (ShowSeat ss : selectedSeats.values()) {
            BigDecimal p = ss.getPrice() != null ? ss.getPrice() : BigDecimal.valueOf(150.0);
            total = total.add(p);

            JPanel itemRow = new JPanel(new BorderLayout());
            itemRow.setOpaque(false);
            itemRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
            itemRow.setBorder(new EmptyBorder(2, 0, 2, 0));
            itemRow.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel nameLbl = new JLabel("💺 " + ss.getSeatLabel() + " (" + ss.getSeatType() + ")");
            nameLbl.setFont(Theme.FONT_SMALL);
            nameLbl.setForeground(Theme.TEXT_DARK);

            JLabel priceLbl = new JLabel("₹" + p.toPlainString());
            priceLbl.setFont(Theme.FONT_BOLD_SM);
            priceLbl.setForeground(Theme.TEXT_DARK);

            itemRow.add(nameLbl, BorderLayout.WEST);
            itemRow.add(priceLbl, BorderLayout.EAST);
            summaryItemsPanel.add(itemRow);
        }

        summaryAmount.setText(String.format("₹%.2f", total.doubleValue()));
        confirmBookingBtn.setEnabled(!selectedSeats.isEmpty());
        confirmBookingBtn.repaint();

        updateCashChange();
        if (upiRadio != null && upiRadio.isSelected()) {
            refreshUpiQrCode();
        }
        summaryItemsPanel.revalidate();
        summaryItemsPanel.repaint();
    }

    private BigDecimal calculateTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (ShowSeat ss : selectedSeats.values()) {
            BigDecimal p = ss.getPrice() != null ? ss.getPrice() : BigDecimal.valueOf(150.0);
            total = total.add(p);
        }
        return total;
    }

    private void executeBooking() {
        if (selectedShow == null || selectedSeats.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select an active show and at least one seat.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!selectedShow.isBookable()) {
            JOptionPane.showMessageDialog(this,
                    "Ticket booking for this show is closed.\n\n"
                    + "• Show Date: " + selectedShow.getShowDate() + "\n"
                    + "• Show Time: " + selectedShow.getStartTime() + "\n\n"
                    + "Bookings are only permitted for upcoming shows or within 30 minutes after the show starts.",
                    "Booking Cutoff Reached", JOptionPane.WARNING_MESSAGE);
            if (selectedMovie != null) {
                selectMovie(selectedMovie);
            }
            return;
        }

        String phone = customerPhoneField.getText().trim();
        String name = capitalizeTitle(customerNameField.getText().trim());

        // Smart-swap if user typed mobile in name field and name in phone field
        if (!CustomerService.isValidIndianMobile(phone) && CustomerService.isValidIndianMobile(name)) {
            String temp = phone;
            phone = name;
            name = capitalizeTitle(temp);
            customerPhoneField.setText(phone);
        }
        customerNameField.setText(name);

        String normalizedPhone = CustomerService.normalizePhone(phone);

        if (!CustomerService.isValidIndianMobile(normalizedPhone)) {
            JOptionPane.showMessageDialog(this, "Please enter a valid 10-digit Indian mobile number (e.g. 9876543210).", "Validation Error", JOptionPane.WARNING_MESSAGE);
            customerPhoneField.requestFocus();
            return;
        }

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter the customer's name.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            customerNameField.requestFocus();
            return;
        }

        BigDecimal total = calculateTotal();
        String method = cashRadio.isSelected() ? "CASH" : (upiRadio.isSelected() ? "UPI" : "CARD");

        BigDecimal amountReceived = total;
        BigDecimal change = BigDecimal.ZERO;

        if ("CASH".equalsIgnoreCase(method)) {
            try {
                String cleanRecv = cashReceivedField.getText().replaceAll("[^0-9.]", "");
                amountReceived = cleanRecv.isEmpty() ? BigDecimal.ZERO : new BigDecimal(cleanRecv);
            } catch (Exception ex) {
                amountReceived = BigDecimal.ZERO;
            }

            if (amountReceived.compareTo(total) < 0) {
                JOptionPane.showMessageDialog(this,
                        "Cash received (₹" + amountReceived + ") is less than total amount (₹" + total + ").",
                        "Insufficient Payment", JOptionPane.WARNING_MESSAGE);
                cashReceivedField.requestFocus();
                return;
            }
            change = amountReceived.subtract(total);
        } else if ("UPI".equalsIgnoreCase(method)) {
            amountReceived = total;
            change = BigDecimal.ZERO;
            if (!upiPaymentReceived) {
                int choice = JOptionPane.showConfirmDialog(this,
                        "Confirm UPI Payment Received?\n\n"
                        + "• Amount: ₹" + String.format(java.util.Locale.US, "%.2f", total.doubleValue()) + "\n"
                        + "• Reference: " + ((currentUpiTrxId != null && !currentUpiTrxId.isEmpty()) ? currentUpiTrxId : "Auto-generated") + "\n\n"
                        + "Has the customer scanned the QR code and paid successfully?",
                        "Confirm UPI Payment", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (choice != JOptionPane.YES_OPTION) {
                    return;
                }
                upiPaymentReceived = true;
                refreshUpiQrCode();
            }
        }

        String ref = refField.getText().trim();
        if ("UPI".equalsIgnoreCase(method)) {
            if (ref.isEmpty() || ref.toLowerCase().contains("slip") || ref.toLowerCase().contains("card")) {
                ref = (currentUpiTrxId != null && !currentUpiTrxId.isEmpty())
                        ? currentUpiTrxId
                        : ("UPI" + (System.currentTimeMillis() % 100000000L));
            }
        }

        Payment payment = new Payment(method, total, amountReceived, change, ref);
        Customer customer = new Customer(name, normalizedPhone);

        try {
            confirmBookingBtn.setEnabled(false);
            confirmBookingBtn.setText("Processing...");

            Booking booking = BookingService.processBooking(
                    customer,
                    selectedShow,
                    new ArrayList<>(selectedSeats.values()),
                    "Rahul Sharma (Counter #02)",
                    BigDecimal.ZERO,
                    payment
            );

            // Open Confirmation Dialog with Vector QR Code
            TicketConfirmationDialog dialog = new TicketConfirmationDialog(
                    SwingUtilities.getWindowAncestor(this),
                    booking,
                    this::resetBookingWorkspace
            );
            dialog.setVisible(true);

            refreshRecentBookings();
            loadSeatsForSelectedShow();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Booking Failed: " + ex.getMessage(),
                    "Transaction Error", JOptionPane.ERROR_MESSAGE);
            loadSeatsForSelectedShow();
        } finally {
            confirmBookingBtn.setEnabled(!selectedSeats.isEmpty());
            confirmBookingBtn.setText("Confirm Booking");
            confirmBookingBtn.repaint();
        }
    }

    public void resetBookingWorkspace() {
        selectedSeats.clear();
        customerPhoneField.setText("");
        customerNameField.setText("");
        customerBadge.setText("● Enter 10-digit mobile number");
        customerBadge.setForeground(Theme.TEXT_MUTED);
        cashReceivedField.setText("");
        changeReturnedLbl.setText("Change: ₹0.00");
        changeReturnedLbl.setForeground(new Color(37, 99, 235));
        refField.setText("");
        currentUpiTrxId = "";
        upiPaymentReceived = false;
        cashRadio.setSelected(true);
        updatePaymentModeUI();

        if (selectedShow != null) {
            loadSeatsForSelectedShow();
        }
        updateSummary();
    }

    public void refreshRecentBookings() {
        recentBookingsList = BookingService.getRecentBookings(25);
        if (bookingsTableModel != null) {
            bookingsTableModel.setRowCount(0);
            for (Booking b : recentBookingsList) {
                String pay = (b.getPayment() != null) ? b.getPayment().getPaymentMethod() : "PAID";
                bookingsTableModel.addRow(new Object[]{
                    b.getBookingNumber(),
                    capitalizeTitle(b.getCustomerName()),
                    b.getCustomerPhone(),
                    capitalizeTitle(b.getMovieTitle()),
                    b.getShowDate(),
                    b.getStartTime(),
                    b.getScreenName(),
                    b.getFormattedSeats(),
                    "₹" + b.getTotalAmount().toPlainString(),
                    pay,
                    b.getStatus()
                });
            }
        }
    }

    // Public method for external navigation (e.g. from TodayShowsPage or MoviesListPage)
    public void selectMovieFromExternal(int movieId) {
        for (Movie m : allMoviesList) {
            if (m.getId() == movieId) {
                selectMovie(m);
                showStep1();
                break;
            }
        }
    }

    public void selectShowFromExternal(Show show) {
        if (show == null) {
            return;
        }
        if (!show.isBookable()) {
            JOptionPane.showMessageDialog(this,
                    "This screening cannot be booked because it is in the past or its 30-minute booking window has expired.",
                    "Show Unavailable", JOptionPane.WARNING_MESSAGE);
            return;
        }
        selectMovieFromExternal(show.getMovieId());
        if (show.getShowDate() != null) {
            selectedDate = show.getShowDate();
            renderDatesAndShows();
        }
        selectShow(show);
    }

    private void addSummaryRow(JPanel panel, String label, JLabel value) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(1, 0, 1, 0));

        JLabel l = new JLabel(label);
        l.setFont(Theme.FONT_SMALL);
        l.setForeground(Theme.TEXT_MUTED);

        value.setFont(Theme.FONT_BOLD_SM);
        value.setForeground(Theme.TEXT_DARK);

        row.add(l, BorderLayout.WEST);
        row.add(value, BorderLayout.EAST);
        panel.add(row);
    }

    private JPanel createSection(String title) {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(new LineBorder(Theme.BORDER_COLOR), title),
                new EmptyBorder(8, 10, 10, 10)));
        return panel;
    }
}
