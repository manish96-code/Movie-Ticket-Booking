package com.cinemats.ui.staff;

import com.cinemats.dao.MovieDAO;
import com.cinemats.dao.ScreenDAO;
import com.cinemats.dao.ShowDAO;
import com.cinemats.dao.ShowSeatDAO;
import com.cinemats.dao.BookingDAO;
import com.cinemats.model.*;
import com.cinemats.service.BookingService;
import com.cinemats.service.CustomerService;
import com.cinemats.ui.staff.booking.TicketConfirmationDialog;
import com.cinemats.util.Theme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.math.BigDecimal;
import java.util.*;
import java.util.List;
import javax.imageio.ImageIO;

// Enterprise Box Office Movie Ticket Booking Page matching physical screen layouts, rich posters, and counter flow
public class OrderBookingPage extends JPanel {

    private final StaffDashboard dashboard;

    // --- Column 1: Select Movie ---
    private final JComboBox<MovieWrapper> movieDropdown = new JComboBox<>();
    private final JPanel movieGridPanel = new JPanel(new GridLayout(0, 2, 8, 8));
    private List<Movie> allMoviesList = new ArrayList<>();
    private Movie selectedMovie = null;
    private final Map<Integer, JPanel> movieCardMap = new HashMap<>();

    // --- Column 2: Select Show & Seats ---
    private final JComboBox<String> dateDropdown = new JComboBox<>();
    private final JComboBox<ScreenWrapper> screenDropdown = new JComboBox<>();
    private final JComboBox<ShowWrapper> timeDropdown = new JComboBox<>();
    private final JPanel seatPanel = new JPanel();
    private List<Show> movieShows = new ArrayList<>();
    private Show selectedShow = null;
    private final Map<Integer, ShowSeat> selectedSeats = new LinkedHashMap<>();
    private boolean seatInventoryAvailable = false;

    // --- Column 3: Booking Summary, Price, Customer & Payment ---
    private final JLabel summaryMovie = new JLabel("-");
    private final JLabel summaryDate = new JLabel("-");
    private final JLabel summaryTime = new JLabel("-");
    private final JLabel summaryScreen = new JLabel("-");
    private final JLabel summarySeats = new JLabel("-");
    private final JPanel summaryItemsPanel = new JPanel();
    private final JLabel summaryAmount = new JLabel("₹0.00");

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

    private JButton confirmBookingBtn;
    private JButton resetBtn;

    // --- Bottom: Current Bookings (Today) ---
    private JTable todayBookingsTable;
    private DefaultTableModel bookingsTableModel;
    private JTextField searchBookingField;
    private List<Booking> recentBookingsList = new ArrayList<>();

    // Wrappers for Combo Display
    private static class MovieWrapper {
        final Movie movie;
        MovieWrapper(Movie m) { this.movie = m; }
        @Override
        public String toString() {
            if (movie == null) return "-- Select Movie --";
            return capitalizeTitle(movie.getTitle()) + " (" + movie.getLanguage() + " • " + movie.getCertificate() + ")";
        }
    }

    private static class ScreenWrapper {
        final Screen screen;
        ScreenWrapper(Screen s) { this.screen = s; }
        @Override
        public String toString() {
            if (screen == null) return "-- All Screens --";
            return screen.getName() + " (" + screen.getScreenType() + ")";
        }
    }

    private static class ShowWrapper {
        final Show show;
        ShowWrapper(Show s) { this.show = s; }
        @Override
        public String toString() {
            if (show == null) return "-- Select Showtime --";
            return show.getStartTime() + (show.getEndTime().isEmpty() ? "" : " - " + show.getEndTime());
        }
    }

    public OrderBookingPage(StaffDashboard dashboard) {
        this.dashboard = dashboard;
        setLayout(new BorderLayout(10, 10));
        setBackground(Theme.BG_MAIN);
        setBorder(new EmptyBorder(12, 14, 12, 14));

        initUI();
        loadMovies();
        refreshRecentBookings();
    }

    private void initUI() {
        add(buildHeader(), BorderLayout.NORTH);

        // Center 3-Column POS Workspace
        JPanel workspace = new JPanel(new GridBagLayout());
        workspace.setOpaque(false);
        GridBagConstraints column = new GridBagConstraints();
        column.gridy = 0;
        column.fill = GridBagConstraints.BOTH;
        column.weighty = 1;

        // Column 1 (28%): Select Movie (Dropdown + 2-Column Poster Cards)
        column.gridx = 0;
        column.weightx = 0.28;
        column.insets = new Insets(0, 0, 0, 8);
        workspace.add(buildMoviePanel(), column);

        // Column 2 (47%): Select Show & Exact Screen Seats Layout
        column.gridx = 1;
        column.weightx = 0.47;
        column.insets = new Insets(0, 0, 0, 8);
        workspace.add(buildShowAndSeatsPanel(), column);

        // Column 3 (25%): Booking Summary, Price, Customer & Payment
        column.gridx = 2;
        column.weightx = 0.25;
        column.insets = new Insets(0, 0, 0, 0);
        workspace.add(buildSummaryAndPaymentPanel(), column);

        JPanel mainCenter = new JPanel(new BorderLayout(0, 10));
        mainCenter.setOpaque(false);
        mainCenter.add(workspace, BorderLayout.CENTER);
        mainCenter.add(buildTodayBookingsCard(), BorderLayout.SOUTH);

        add(mainCenter, BorderLayout.CENTER);

        wireSelectionControls();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Movie Booking");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Theme.TEXT_DARK);

        header.add(title, BorderLayout.WEST);
        return header;
    }

    // ==========================================================
    // COLUMN 1: SELECT MOVIE (POSTER CARDS + TITLE CAPITALIZATION)
    // ==========================================================
    private JPanel buildMoviePanel() {
        JPanel panel = createSection("Select Movie");
        panel.setLayout(new BorderLayout(0, 8));

        movieDropdown.setFont(Theme.FONT_REGULAR);
        movieDropdown.setBackground(Color.WHITE);
        panel.add(movieDropdown, BorderLayout.NORTH);

        movieGridPanel.setBackground(Color.WHITE);
        movieGridPanel.setBorder(new EmptyBorder(4, 4, 4, 4));

        JScrollPane cardsScroll = new JScrollPane(movieGridPanel);
        cardsScroll.setBorder(new LineBorder(Theme.BORDER_COLOR, 1));
        cardsScroll.getVerticalScrollBar().setUnitIncrement(16);
        cardsScroll.getViewport().setBackground(Color.WHITE);

        panel.add(cardsScroll, BorderLayout.CENTER);
        return panel;
    }

    // ==========================================================
    // COLUMN 2: SELECT SHOW & SCREEN SEATS
    // ==========================================================
    private JPanel buildShowAndSeatsPanel() {
        JPanel panel = createSection("Select Show & Seats");
        panel.setLayout(new BorderLayout(0, 8));

        // Selectors at top: Date, Screen, Show Time, Legend
        JPanel selectors = new JPanel(new GridLayout(2, 2, 8, 6));
        selectors.setOpaque(false);
        selectors.add(labeledControl("Date", dateDropdown));
        selectors.add(labeledControl("Screen", screenDropdown));
        selectors.add(labeledControl("Show Time", timeDropdown));
        selectors.add(buildLegend());
        panel.add(selectors, BorderLayout.NORTH);

        // Seat Container
        seatPanel.setLayout(new BoxLayout(seatPanel, BoxLayout.Y_AXIS));
        seatPanel.setBackground(Color.WHITE);
        seatPanel.setBorder(new EmptyBorder(10, 8, 10, 8));

        JScrollPane seatsScroll = new JScrollPane(seatPanel);
        seatsScroll.setBorder(new LineBorder(Theme.BORDER_COLOR));
        seatsScroll.getVerticalScrollBar().setUnitIncrement(16);
        seatsScroll.getViewport().setBackground(Color.WHITE);
        panel.add(seatsScroll, BorderLayout.CENTER);

        return panel;
    }

    private JPanel buildLegend() {
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        legend.setOpaque(false);
        legend.add(createLegendDot("Available", new Color(71, 85, 105)));
        legend.add(createLegendDot("Selected", new Color(37, 99, 235)));
        legend.add(createLegendDot("Booked", new Color(225, 29, 72)));
        legend.add(createLegendDot("Blocked (✕)", new Color(148, 163, 184)));
        return legend;
    }

    private JPanel createLegendDot(String text, Color color) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        p.setOpaque(false);
        JLabel dot = new JLabel("■");
        dot.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dot.setForeground(color);
        JLabel lbl = new JLabel(text);
        lbl.setFont(Theme.FONT_SMALL);
        lbl.setForeground(Theme.TEXT_MUTED);
        p.add(dot);
        p.add(lbl);
        return p;
    }

    // ==========================================================
    // COLUMN 3: SUMMARY, PRICE, CUSTOMER DETAILS & PAYMENT
    // ==========================================================
    private JPanel buildSummaryAndPaymentPanel() {
        JPanel panel = createSection("Booking Summary");
        panel.setLayout(new BorderLayout(0, 8));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        // 1. Movie / Show Overview Info
        JPanel infoBlock = new JPanel();
        infoBlock.setLayout(new BoxLayout(infoBlock, BoxLayout.Y_AXIS));
        infoBlock.setOpaque(false);
        infoBlock.setBorder(new EmptyBorder(0, 0, 6, 0));

        addSummaryRow(infoBlock, "Movie", summaryMovie);
        addSummaryRow(infoBlock, "Date", summaryDate);
        addSummaryRow(infoBlock, "Show Time", summaryTime);
        addSummaryRow(infoBlock, "Screen", summaryScreen);
        addSummaryRow(infoBlock, "Seats", summarySeats);

        content.add(infoBlock);
        content.add(new JSeparator());
        content.add(Box.createVerticalStrut(6));

        // 2. Itemized Seats Price List
        summaryItemsPanel.setLayout(new BoxLayout(summaryItemsPanel, BoxLayout.Y_AXIS));
        summaryItemsPanel.setOpaque(false);
        content.add(summaryItemsPanel);
        content.add(Box.createVerticalStrut(6));

        // 3. Total Amount Highlight
        JPanel totalBlock = new JPanel(new BorderLayout());
        totalBlock.setOpaque(false);
        totalBlock.setBorder(new EmptyBorder(4, 0, 8, 0));

        JLabel totalTitle = new JLabel("Total Amount");
        totalTitle.setFont(Theme.FONT_HEADER);
        totalTitle.setForeground(Theme.TEXT_DARK);

        summaryAmount.setFont(new Font("Segoe UI", Font.BOLD, 22));
        summaryAmount.setForeground(new Color(22, 163, 74));

        totalBlock.add(totalTitle, BorderLayout.WEST);
        totalBlock.add(summaryAmount, BorderLayout.EAST);
        content.add(totalBlock);
        content.add(new JSeparator());
        content.add(Box.createVerticalStrut(8));

        // 4. Customer Details (Name & Mobile)
        JLabel custHeader = new JLabel("CUSTOMER DETAILS");
        custHeader.setFont(Theme.FONT_BOLD_SM);
        custHeader.setForeground(new Color(100, 116, 139));
        custHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(custHeader);
        content.add(Box.createVerticalStrut(4));

        customerPhoneField = Theme.createTextField("Mobile Number (10 digits)");
        customerPhoneField.setPreferredSize(new Dimension(0, 32));
        customerPhoneField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        customerPhoneField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { handlePhoneLookup(); }
            public void removeUpdate(DocumentEvent e) { handlePhoneLookup(); }
            public void changedUpdate(DocumentEvent e) { handlePhoneLookup(); }
        });

        customerBadge = new JLabel("● Enter 10-digit mobile number");
        customerBadge.setFont(Theme.FONT_SMALL);
        customerBadge.setForeground(Theme.TEXT_MUTED);

        customerNameField = Theme.createTextField("Customer Name");
        customerNameField.setPreferredSize(new Dimension(0, 32));
        customerNameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));

        content.add(customerPhoneField);
        content.add(customerBadge);
        content.add(Box.createVerticalStrut(4));
        content.add(customerNameField);
        content.add(Box.createVerticalStrut(10));

        // 5. Payment Options (Cash, UPI, Card)
        JLabel payHeader = new JLabel("PAYMENT METHOD");
        payHeader.setFont(Theme.FONT_BOLD_SM);
        payHeader.setForeground(new Color(100, 116, 139));
        payHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(payHeader);
        content.add(Box.createVerticalStrut(4));

        cashRadio = new JRadioButton("Cash", true);
        upiRadio = new JRadioButton("UPI");
        cardRadio = new JRadioButton("Card");

        ButtonGroup bg = new ButtonGroup();
        bg.add(cashRadio);
        bg.add(upiRadio);
        bg.add(cardRadio);

        cashRadio.setFont(Theme.FONT_SMALL);
        upiRadio.setFont(Theme.FONT_SMALL);
        cardRadio.setFont(Theme.FONT_SMALL);

        cashRadio.addActionListener(e -> updatePaymentModeUI());
        upiRadio.addActionListener(e -> updatePaymentModeUI());
        cardRadio.addActionListener(e -> updatePaymentModeUI());

        JPanel payModes = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        payModes.setOpaque(false);
        payModes.add(cashRadio);
        payModes.add(upiRadio);
        payModes.add(cardRadio);
        content.add(payModes);

        // Cash Change Calculator
        cashCalcPanel = new JPanel(new GridLayout(1, 2, 6, 0));
        cashCalcPanel.setOpaque(false);
        cashCalcPanel.setBorder(new EmptyBorder(4, 0, 4, 0));

        cashReceivedField = Theme.createTextField("Cash Received (₹)");
        cashReceivedField.setPreferredSize(new Dimension(0, 30));
        cashReceivedField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateCashChange(); }
            public void removeUpdate(DocumentEvent e) { updateCashChange(); }
            public void changedUpdate(DocumentEvent e) { updateCashChange(); }
        });

        changeReturnedLbl = new JLabel("Change: ₹0.00");
        changeReturnedLbl.setFont(Theme.FONT_BOLD_SM);
        changeReturnedLbl.setForeground(new Color(37, 99, 235));

        cashCalcPanel.add(cashReceivedField);
        cashCalcPanel.add(changeReturnedLbl);
        content.add(cashCalcPanel);

        // Ref field for UPI / Card
        refField = Theme.createTextField("Optional UTR / Card Slip Ref");
        refField.setPreferredSize(new Dimension(0, 30));
        refField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        refField.setVisible(false);
        content.add(refField);

        content.add(Box.createVerticalGlue());

        // Confirm & Reset Buttons
        JPanel actionBlock = new JPanel(new GridLayout(2, 1, 0, 6));
        actionBlock.setOpaque(false);
        actionBlock.setBorder(new EmptyBorder(8, 0, 0, 0));

        confirmBookingBtn = Theme.createPrimaryButton("Confirm Booking");
        confirmBookingBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        confirmBookingBtn.setEnabled(false);
        confirmBookingBtn.addActionListener(e -> executeBooking());

        resetBtn = Theme.createSecondaryButton("Reset");
        resetBtn.addActionListener(e -> resetBookingWorkspace());

        actionBlock.add(confirmBookingBtn);
        actionBlock.add(resetBtn);
        content.add(actionBlock);

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private void updatePaymentModeUI() {
        if (cashRadio.isSelected()) {
            cashCalcPanel.setVisible(true);
            refField.setVisible(false);
        } else {
            cashCalcPanel.setVisible(false);
            refField.setVisible(true);
            refField.setToolTipText(upiRadio.isSelected() ? "UPI Ref ID / UTR" : "Card Auth / Slip Ref");
        }
        revalidate();
        repaint();
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
            } else {
                changeReturnedLbl.setText("Short: ₹" + total.subtract(received).setScale(2, java.math.RoundingMode.HALF_UP));
                changeReturnedLbl.setForeground(Theme.ACCENT_RED);
            }
        } catch (Exception ignored) {
            changeReturnedLbl.setText("Change: ₹0.00");
        }
    }

    private void handlePhoneLookup() {
        String phone = customerPhoneField.getText().trim();
        String normalized = CustomerService.normalizePhone(phone);

        if (CustomerService.isValidIndianMobile(normalized)) {
            Customer existing = CustomerService.findCustomerByPhone(normalized);
            if (existing != null) {
                activeCustomer = existing;
                customerNameField.setText(existing.getName());
                customerBadge.setText("✓ Existing Patron: " + existing.getName());
                customerBadge.setForeground(new Color(22, 163, 74));
            } else {
                activeCustomer = null;
                customerBadge.setText("● New Customer (Will be saved on confirm)");
                customerBadge.setForeground(new Color(37, 99, 235));
            }
        } else if (normalized.length() == 10) {
            customerBadge.setText("⚠ Must start with 6, 7, 8, or 9");
            customerBadge.setForeground(Theme.ACCENT_RED);
        } else {
            activeCustomer = null;
            customerBadge.setText("● Enter 10-digit mobile number (" + normalized.length() + "/10)");
            customerBadge.setForeground(Theme.TEXT_MUTED);
        }
    }

    // ==========================================================
    // BOTTOM: TODAY'S CURRENT BOOKINGS AUDIT TABLE
    // ==========================================================
    private JPanel buildTodayBookingsCard() {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(0, 160));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(new LineBorder(Theme.BORDER_COLOR), "Current Bookings (Today)"),
                new EmptyBorder(2, 4, 4, 4)));

        String[] columns = {"Booking #", "Customer", "Phone", "Movie", "Date", "Show Time", "Screen", "Seats", "Amount", "Payment", "Status"};
        bookingsTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        todayBookingsTable = new JTable(bookingsTableModel);
        todayBookingsTable.setRowHeight(26);
        todayBookingsTable.setFont(Theme.FONT_REGULAR);
        todayBookingsTable.setShowGrid(false);
        todayBookingsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        todayBookingsTable.getTableHeader().setFont(Theme.FONT_BOLD_SM);
        todayBookingsTable.getTableHeader().setPreferredSize(new Dimension(0, 26));
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
        card.add(scroll, BorderLayout.CENTER);

        return card;
    }

    // ==========================================================
    // LOGIC: LOADING MOVIES WITH RICH POSTERS & CAPITALIZATION
    // ==========================================================
    private void loadMovies() {
        allMoviesList = MovieDAO.getAllMovies();
        movieGridPanel.removeAll();
        movieDropdown.removeAllItems();
        movieCardMap.clear();

        movieDropdown.addItem(new MovieWrapper(null));

        for (Movie movie : allMoviesList) {
            movieDropdown.addItem(new MovieWrapper(movie));

            // Create rich poster card
            JPanel card = createMoviePosterCard(movie);
            movieCardMap.put(movie.getId(), card);
            movieGridPanel.add(card);
        }

        movieGridPanel.revalidate();
        movieGridPanel.repaint();

        if (!allMoviesList.isEmpty()) {
            selectMovie(allMoviesList.get(0));
        } else {
            showSeatMessage("No movies are available in the database.");
        }
    }

    // Creates rich poster card with image thumbnail, capitalized title, and hover/selection highlight
    private JPanel createMoviePosterCard(Movie movie) {
        String capitalizedTitle = capitalizeTitle(movie.getTitle());

        JPanel card = new JPanel(new BorderLayout(0, 4)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                boolean isSelected = (selectedMovie != null && selectedMovie.getId() == movie.getId());

                if (isSelected) {
                    g2.setColor(new Color(239, 246, 255));
                    g2.fillRoundRect(2, 2, w - 4, h - 4, 8, 8);
                    g2.setColor(new Color(37, 99, 235));
                    g2.setStroke(new BasicStroke(2.5f));
                    g2.drawRoundRect(2, 2, w - 4, h - 4, 8, 8);
                } else {
                    g2.setColor(Color.WHITE);
                    g2.fillRoundRect(2, 2, w - 4, h - 4, 8, 8);
                    g2.setColor(Theme.BORDER_COLOR);
                    g2.setStroke(new BasicStroke(1.0f));
                    g2.drawRoundRect(2, 2, w - 4, h - 4, 8, 8);
                }
                g2.dispose();
            }
        };

        card.setPreferredSize(new Dimension(140, 205));
        card.setMinimumSize(new Dimension(130, 195));
        card.setOpaque(false);
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        card.setBorder(new EmptyBorder(6, 6, 6, 6));

        // Poster Area (Top)
        JPanel posterArea = createPosterThumbnail(movie);
        card.add(posterArea, BorderLayout.CENTER);

        // Text Area (Bottom)
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        textPanel.setBorder(new EmptyBorder(4, 2, 0, 2));

        JLabel titleLbl = new JLabel(capitalizedTitle);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titleLbl.setForeground(Theme.TEXT_DARK);
        titleLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel metaLbl = new JLabel(movie.getLanguage() + " • " + movie.getCertificate());
        metaLbl.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        metaLbl.setForeground(Theme.TEXT_MUTED);
        metaLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        textPanel.add(titleLbl);
        textPanel.add(Box.createVerticalStrut(1));
        textPanel.add(metaLbl);
        card.add(textPanel, BorderLayout.SOUTH);

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectMovie(movie);
            }
        });

        return card;
    }

    private JPanel createPosterThumbnail(Movie movie) {
        String imgPath = movie.getImagePath();
        BufferedImage img = null;

        if (imgPath != null && !imgPath.trim().isEmpty()) {
            File f = new File(imgPath.trim());
            if (!f.isAbsolute()) {
                f = new File(System.getProperty("user.dir"), imgPath.trim());
            }
            if (!f.exists() || !f.isFile()) {
                f = new File(new File(System.getProperty("user.dir"), "assets/posters"), new File(imgPath.trim()).getName());
            }
            if (f.exists() && f.isFile()) {
                try {
                    img = ImageIO.read(f);
                } catch (Exception ignored) {}
                if (img == null) {
                    try {
                        ImageIcon ic = new ImageIcon(f.getAbsolutePath());
                        Image raw = ic.getImage();
                        if (raw.getWidth(null) > 0 && raw.getHeight(null) > 0) {
                            img = new BufferedImage(raw.getWidth(null), raw.getHeight(null), BufferedImage.TYPE_INT_ARGB);
                            Graphics2D g2d = img.createGraphics();
                            g2d.drawImage(raw, 0, 0, null);
                            g2d.dispose();
                        }
                    } catch (Exception ignored) {}
                }
            }
        }

        final BufferedImage posterImg = img;
        final String capitalizedTitle = capitalizeTitle(movie.getTitle());
        final String genre = movie.getGenre();
        final String cert = movie.getCertificate();

        JPanel p = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                int w = getWidth();
                int h = getHeight();

                if (posterImg != null) {
                    // Clip to rounded rectangle
                    Shape oldClip = g2.getClip();
                    g2.clip(new java.awt.geom.RoundRectangle2D.Float(2, 2, w - 4, h - 4, 8, 8));

                    int iw = posterImg.getWidth();
                    int ih = posterImg.getHeight();
                    double scale = Math.max((double)(w - 4) / iw, (double)(h - 4) / ih);
                    int sw = (int) (iw * scale);
                    int sh = (int) (ih * scale);
                    int sx = 2 + (w - 4 - sw) / 2;
                    int sy = 2 + (h - 4 - sh) / 2;

                    g2.drawImage(posterImg, sx, sy, sw, sh, null);
                    g2.setClip(oldClip);

                    // Subtle border overlay
                    g2.setColor(new Color(0, 0, 0, 30));
                    g2.drawRoundRect(2, 2, w - 4, h - 4, 8, 8);
                } else {
                    // Generate rich stylized cinema poster graphic
                    int hash = movie.getTitle().hashCode();
                    Color c1 = new Color(24 + Math.abs(hash % 35), 28 + Math.abs(hash % 25), 48 + Math.abs(hash % 45));
                    Color c2 = new Color(15 + Math.abs(hash % 20), 18 + Math.abs(hash % 20), 30 + Math.abs(hash % 25));
                    GradientPaint gp = new GradientPaint(0, 0, c1, 0, h, c2);
                    g2.setPaint(gp);
                    g2.fillRoundRect(2, 2, w - 4, h - 4, 8, 8);

                    // Top Cinema Ribbon
                    g2.setColor(new Color(255, 255, 255, 30));
                    g2.fillRect(2, 2, w - 4, 18);
                    g2.setColor(new Color(251, 191, 36, 220));
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 8));
                    g2.drawString("CINEMA EXPRESS", 8, 14);

                    // Clapperboard graphic
                    g2.setColor(new Color(255, 255, 255, 170));
                    g2.setStroke(new BasicStroke(1.4f));
                    int cx = w / 2;
                    int cy = h / 2 - 14;
                    g2.drawRoundRect(cx - 16, cy - 12, 32, 24, 4, 4);

                    // Play triangle
                    Polygon tri = new Polygon(
                            new int[]{cx - 4, cx + 6, cx - 4},
                            new int[]{cy - 6, cy, cy + 6},
                            3
                    );
                    g2.fillPolygon(tri);

                    // Movie title on poster
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                    FontMetrics fm = g2.getFontMetrics();
                    String displayTitle = capitalizedTitle;
                    if (fm.stringWidth(displayTitle) > w - 16) {
                        displayTitle = displayTitle.substring(0, Math.min(10, displayTitle.length())) + "...";
                    }
                    int tx = (w - fm.stringWidth(displayTitle)) / 2;
                    g2.drawString(displayTitle, tx, cy + 28);

                    // Genre & Certificate Badge at bottom
                    g2.setColor(new Color(255, 255, 255, 25));
                    g2.fillRoundRect(8, h - 22, w - 16, 16, 6, 6);
                    g2.setColor(new Color(226, 232, 240));
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 9));
                    FontMetrics bfm = g2.getFontMetrics();
                    String badge = cert + " • " + genre;
                    if (bfm.stringWidth(badge) > w - 20) {
                        badge = genre;
                    }
                    int bx = (w - bfm.stringWidth(badge)) / 2;
                    g2.drawString(badge, bx, h - 10);

                    // Gold accent border
                    g2.setColor(new Color(251, 191, 36, 120));
                    g2.setStroke(new BasicStroke(1.0f));
                    g2.drawRoundRect(2, 2, w - 4, h - 4, 8, 8);
                }
                g2.dispose();
            }
        };
        p.setPreferredSize(new Dimension(126, 148));
        p.setMinimumSize(new Dimension(120, 140));
        p.setOpaque(false);
        return p;
    }

    public static String capitalizeTitle(String text) {
        if (text == null || text.trim().isEmpty()) return "";
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

    private void wireSelectionControls() {
        movieDropdown.addActionListener(e -> {
            MovieWrapper wrapper = (MovieWrapper) movieDropdown.getSelectedItem();
            if (wrapper != null && wrapper.movie != null && (selectedMovie == null || selectedMovie.getId() != wrapper.movie.getId())) {
                selectMovie(wrapper.movie);
            }
        });

        dateDropdown.addActionListener(e -> updateTimeOptions());
        screenDropdown.addActionListener(e -> updateTimeOptions());
        timeDropdown.addActionListener(e -> {
            ShowWrapper sw = (ShowWrapper) timeDropdown.getSelectedItem();
            selectedShow = (sw == null) ? null : sw.show;
            loadSeatsForSelectedShow();
            updateSummary();
        });
    }

    private void selectMovie(Movie movie) {
        if (movie == null) return;
        selectedMovie = movie;

        // Sync dropdown without triggering duplicate events
        for (int i = 0; i < movieDropdown.getItemCount(); i++) {
            MovieWrapper mw = movieDropdown.getItemAt(i);
            if (mw != null && mw.movie != null && mw.movie.getId() == movie.getId()) {
                movieDropdown.setSelectedIndex(i);
                break;
            }
        }

        // Repaint movie cards for selection ring
        movieGridPanel.repaint();

        selectedSeats.clear();
        movieShows = ShowDAO.getShowsByMovie(movie.getId());
        movieShows.removeIf(show -> "CANCELLED".equalsIgnoreCase(show.getStatus()));

        updateDateOptions();
        updateSummary();
    }

    private void updateDateOptions() {
        String previousDate = (String) dateDropdown.getSelectedItem();
        TreeSet<String> dates = new TreeSet<>();
        for (Show show : movieShows) {
            if (show.getShowDate() != null && !show.getShowDate().isEmpty()) {
                dates.add(show.getShowDate());
            }
        }

        dateDropdown.removeAllItems();
        for (String date : dates) {
            dateDropdown.addItem(date);
        }
        if (previousDate != null && dates.contains(previousDate)) {
            dateDropdown.setSelectedItem(previousDate);
        }

        // Populate Screens
        screenDropdown.removeAllItems();
        screenDropdown.addItem(new ScreenWrapper(null));
        for (Screen screen : ScreenDAO.getAllScreens()) {
            screenDropdown.addItem(new ScreenWrapper(screen));
        }

        updateTimeOptions();
    }

    private void updateTimeOptions() {
        String date = (String) dateDropdown.getSelectedItem();
        ScreenWrapper screenWrap = (ScreenWrapper) screenDropdown.getSelectedItem();
        Integer screenId = (screenWrap == null || screenWrap.screen == null) ? null : screenWrap.screen.getId();

        timeDropdown.removeAllItems();
        for (Show show : movieShows) {
            boolean dateMatch = (date == null) || date.equals(show.getShowDate());
            boolean screenMatch = (screenId == null) || (show.getScreenId() == screenId);
            if (dateMatch && screenMatch) {
                timeDropdown.addItem(new ShowWrapper(show));
            }
        }

        if (timeDropdown.getItemCount() > 0) {
            timeDropdown.setSelectedIndex(0);
            ShowWrapper sw = (ShowWrapper) timeDropdown.getSelectedItem();
            selectedShow = (sw == null) ? null : sw.show;
        } else {
            selectedShow = null;
        }

        loadSeatsForSelectedShow();
        updateSummary();
    }

    // ==========================================================
    // EXACT SCREEN SEAT MATRIX (MATCHING SeatLayoutPage)
    // ==========================================================
    private void loadSeatsForSelectedShow() {
        selectedSeats.clear();
        seatInventoryAvailable = false;
        seatPanel.removeAll();

        if (selectedShow == null) {
            showSeatMessage(movieShows.isEmpty()
                    ? "No screenings currently scheduled for this movie."
                    : "Select a date and showtime to load seating layout.");
            updateSummary();
            return;
        }

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

        seatInventoryAvailable = true;

        // Group seats by row (A, B, C...)
        Map<String, List<ShowSeat>> seatsByRow = new LinkedHashMap<>();
        for (ShowSeat ss : showSeats) {
            String label = ss.getSeatLabel();
            String row = label.replaceAll("[0-9]", "");
            if (row.isEmpty()) row = "Seats";
            seatsByRow.computeIfAbsent(row, k -> new ArrayList<>()).add(ss);
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
                seatPanel.add(Box.createVerticalStrut(14));
                seatPanel.add(createTierHeader(currentTier, rowPrice));
                seatPanel.add(Box.createVerticalStrut(10));
            }

            JPanel seatRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 2));
            seatRow.setOpaque(false);

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
        seatPanel.add(Box.createVerticalStrut(20));
        seatPanel.add(createScreenGraphic());
        seatPanel.add(Box.createVerticalStrut(6));

        seatPanel.revalidate();
        seatPanel.repaint();
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
        p.setPreferredSize(new Dimension(0, 46));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        p.setOpaque(false);
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

    // ==========================================================
    // SUMMARY, PRICE RECALCULATION & BOOKING EXECUTION
    // ==========================================================
    private void updateSummary() {
        summaryMovie.setText(selectedMovie == null ? "-" : capitalizeTitle(selectedMovie.getTitle()));
        summaryDate.setText(selectedShow == null ? "-" : selectedShow.getShowDate());
        summaryTime.setText(selectedShow == null ? "-" : selectedShow.getStartTime());
        summaryScreen.setText(selectedShow == null ? "-" : selectedShow.getScreenName());

        if (selectedSeats.isEmpty()) {
            summarySeats.setText("-");
        } else {
            List<String> labels = new ArrayList<>();
            for (ShowSeat ss : selectedSeats.values()) {
                labels.add(ss.getSeatLabel());
            }
            summarySeats.setText(String.join(", ", labels));
        }

        // Itemized breakdown
        summaryItemsPanel.removeAll();
        BigDecimal total = BigDecimal.ZERO;

        for (ShowSeat ss : selectedSeats.values()) {
            BigDecimal p = ss.getPrice() != null ? ss.getPrice() : BigDecimal.valueOf(150.0);
            total = total.add(p);

            JPanel itemRow = new JPanel(new BorderLayout());
            itemRow.setOpaque(false);
            itemRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 18));

            JLabel nameLbl = new JLabel(ss.getSeatLabel() + " (" + ss.getSeatType() + ")");
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

        updateCashChange();
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

        String phone = customerPhoneField.getText().trim();
        String name = customerNameField.getText().trim();

        if (!CustomerService.isValidIndianMobile(phone)) {
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
        }

        String ref = refField.getText().trim();
        Payment payment = new Payment(method, total, amountReceived, change, ref);
        Customer customer = new Customer(name, phone);

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
            confirmBookingBtn.setEnabled(true);
            confirmBookingBtn.setText("Confirm Booking");
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
        refField.setText("");
        cashRadio.setSelected(true);
        updatePaymentModeUI();

        if (selectedShow != null) {
            loadSeatsForSelectedShow();
        }
        updateSummary();
    }

    private void refreshRecentBookings() {
        recentBookingsList = BookingService.getRecentBookings(25);
        bookingsTableModel.setRowCount(0);

        for (Booking b : recentBookingsList) {
            String pay = (b.getPayment() != null) ? b.getPayment().getPaymentMethod() : "PAID";
            bookingsTableModel.addRow(new Object[]{
                    b.getBookingNumber(),
                    b.getCustomerName(),
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

    // Public method for external navigation (e.g. from TodayShowsPage or MoviesListPage)
    public void selectMovieFromExternal(int movieId) {
        for (Movie m : allMoviesList) {
            if (m.getId() == movieId) {
                selectMovie(m);
                break;
            }
        }
    }

    public void selectShowFromExternal(Show show) {
        if (show == null) return;
        selectMovieFromExternal(show.getMovieId());
        if (show.getShowDate() != null) {
            dateDropdown.setSelectedItem(show.getShowDate());
        }
        for (int i = 0; i < timeDropdown.getItemCount(); i++) {
            ShowWrapper sw = timeDropdown.getItemAt(i);
            if (sw != null && sw.show != null && sw.show.getId() == show.getId()) {
                timeDropdown.setSelectedIndex(i);
                break;
            }
        }
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

    private JPanel labeledControl(String title, JComponent control) {
        JPanel panel = new JPanel(new BorderLayout(0, 3));
        panel.setOpaque(false);
        JLabel label = new JLabel(title);
        label.setFont(Theme.FONT_BOLD_SM);
        label.setForeground(Theme.TEXT_MUTED);
        control.setFont(Theme.FONT_REGULAR);
        panel.add(label, BorderLayout.NORTH);
        panel.add(control, BorderLayout.CENTER);
        return panel;
    }
}
