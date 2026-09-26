import java.awt.*;
import javax.swing.*;

public class HomeAditya extends JFrame {

    Color background = new Color(245, 245, 245);
    Color dark = new Color(25, 25, 25);
    Color cardColor = Color.WHITE;
    Color red = new Color(210, 40, 40);
    Color gray = new Color(100, 100, 100);

    HomeAditya() {

        setTitle("Ticket - Booking");
        setSize(1500, 850);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        
        // NAVIGATION PANEL
        

        JPanel navPanel = new JPanel(new BorderLayout());
        navPanel.setBackground(dark);
        navPanel.setPreferredSize(new Dimension(0, 80));

        JLabel logo = new JLabel("CINEMA");
        logo.setFont(new Font("Arial", Font.BOLD, 30));
        logo.setForeground(Color.WHITE);
        logo.setBorder(BorderFactory.createEmptyBorder(0, 30, 0, 0));

        navPanel.add(logo, BorderLayout.WEST);


        // Navigation Menu

        JPanel menuPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 35, 25));
        menuPanel.setBackground(dark);

        JLabel home = createNavLabel("Home");
        JLabel movies = createNavLabel("Movies");
        JLabel shows = createNavLabel("Shows");
        JLabel about = createNavLabel("About");
        JLabel login = createNavLabel("Login");

        menuPanel.add(home);
        menuPanel.add(movies);
        menuPanel.add(shows);
        menuPanel.add(about);
        menuPanel.add(login);

        navPanel.add(menuPanel, BorderLayout.EAST);


        
        // MAIN PANEL
        

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(background);


        
        // HERO PANEL
        

        JPanel heroPanel = new JPanel();
        heroPanel.setLayout(new BoxLayout(heroPanel, BoxLayout.Y_AXIS));
        heroPanel.setBackground(new Color(55, 55, 55));
        heroPanel.setBorder(BorderFactory.createEmptyBorder(40, 20, 40, 20));

        JLabel quote = new JLabel("Where every movie becomes a memory");
        quote.setFont(new Font("Arial", Font.BOLD, 30));
        quote.setForeground(Color.WHITE);
        quote.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Experience the magic of cinema");
        subtitle.setFont(new Font("Arial", Font.PLAIN, 16));
        subtitle.setForeground(new Color(210, 210, 210));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel heroButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        heroButtonPanel.setBackground(new Color(55, 55, 55));

        JButton todayButton = createButton("View Today's Shows");
        JButton aboutButton = createButton("About Us");

        heroButtonPanel.add(todayButton);
        heroButtonPanel.add(aboutButton);

        heroPanel.add(quote);
        heroPanel.add(Box.createVerticalStrut(10));
        heroPanel.add(subtitle);
        heroPanel.add(Box.createVerticalStrut(15));
        heroPanel.add(heroButtonPanel);

        mainPanel.add(heroPanel, BorderLayout.NORTH);


       
        // CONTENT PANEL
       

        JPanel contentPanel = new JPanel(new BorderLayout(20, 0));
        contentPanel.setBackground(background);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));


        
        // NOW SHOWING
       

        JPanel nowShowingPanel = createNowShowingPanel();

        contentPanel.add(nowShowingPanel, BorderLayout.CENTER);


        
        // TODAY'S SHOWS
        

        JPanel todaysShowPanel = createTodaysShowsPanel();

        contentPanel.add(todaysShowPanel, BorderLayout.EAST);

        mainPanel.add(contentPanel, BorderLayout.CENTER);


        
        // FOOTER
        

        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footerPanel.setBackground(dark);
        footerPanel.setPreferredSize(new Dimension(0, 40));

        JLabel footerText = new JLabel("© 2026 Cinema - All Rights Reserved");
        footerText.setForeground(new Color(190, 190, 190));
        footerText.setFont(new Font("Arial", Font.PLAIN, 13));

        footerPanel.add(footerText);


        
        // ADD TO FRAME
        

        add(navPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
        add(footerPanel, BorderLayout.SOUTH);

        setVisible(true);
    }


    
    // NOW SHOWING PANEL
    

    private JPanel createNowShowingPanel() {

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(background);

        JLabel heading = new JLabel("Now Showing");
        heading.setFont(new Font("Arial", Font.BOLD, 23));
        heading.setForeground(dark);
        heading.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        panel.add(heading, BorderLayout.NORTH);


        // Movie Grid

        JPanel movieGrid = new JPanel(new GridLayout(2, 3, 15, 15));
        movieGrid.setBackground(background);

        movieGrid.add(createMovieCard("Movie 1", "Action", "₹200"));
        movieGrid.add(createMovieCard("Movie 2", "Drama", "₹180"));
        movieGrid.add(createMovieCard("Movie 3", "Comedy", "₹150"));
        movieGrid.add(createMovieCard("Movie 4", "Thriller", "₹220"));
        movieGrid.add(createMovieCard("Movie 5", "Adventure", "₹200"));
        movieGrid.add(createMovieCard("Movie 6", "Romance", "₹180"));

        panel.add(movieGrid, BorderLayout.CENTER);

        return panel;
    }


   
    // MOVIE CARD
    

    private JPanel createMovieCard(String movieName, String type, String price) {

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(cardColor);
        card.setBorder(BorderFactory.createLineBorder(new Color(210, 210, 210)));


        // Movie Image

        JPanel imagePanel = new JPanel(new GridBagLayout());
        imagePanel.setBackground(new Color(225, 225, 225));

        JLabel imageLabel = new JLabel("IMAGE");
        imageLabel.setForeground(gray);
        imageLabel.setFont(new Font("Arial", Font.BOLD, 15));

        imagePanel.add(imageLabel);


        // Movie Information

        JPanel informationPanel = new JPanel();
        informationPanel.setLayout(new BoxLayout(informationPanel, BoxLayout.Y_AXIS));
        informationPanel.setBackground(cardColor);
        informationPanel.setBorder(BorderFactory.createEmptyBorder(8, 10, 10, 10));

        JLabel nameLabel = new JLabel(movieName);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 16));
        nameLabel.setForeground(dark);

        JLabel typeLabel = new JLabel(type);
        typeLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        typeLabel.setForeground(gray);

        JLabel priceLabel = new JLabel(price);
        priceLabel.setFont(new Font("Arial", Font.BOLD, 14));
        priceLabel.setForeground(red);

        informationPanel.add(nameLabel);
        informationPanel.add(Box.createVerticalStrut(3));
        informationPanel.add(typeLabel);
        informationPanel.add(Box.createVerticalStrut(3));
        informationPanel.add(priceLabel);

        card.add(imagePanel, BorderLayout.CENTER);
        card.add(informationPanel, BorderLayout.SOUTH);

        return card;
    }


    
    // TODAY'S SHOWS PANEL
   

    private JPanel createTodaysShowsPanel() {

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setPreferredSize(new Dimension(370, 0));
        panel.setBorder(BorderFactory.createLineBorder(new Color(210, 210, 210)));


        JLabel heading = new JLabel("Today's Shows");
        heading.setFont(new Font("Arial", Font.BOLD, 21));
        heading.setForeground(dark);
        heading.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));

        panel.add(heading, BorderLayout.NORTH);


        // Shows Panel

        JPanel showsPanel = new JPanel();
        showsPanel.setLayout(new BoxLayout(showsPanel, BoxLayout.Y_AXIS));
        showsPanel.setBackground(Color.WHITE);
        showsPanel.setBorder(BorderFactory.createEmptyBorder(5, 15, 15, 15));

        addShow(showsPanel, "Movie 1", "10:00 AM", "01:00 PM", "04:00 PM");
        addShow(showsPanel, "Movie 2", "11:00 AM", "02:00 PM", "06:00 PM");
        addShow(showsPanel, "Movie 3", "12:00 PM", "03:00 PM", "08:00 PM");
        addShow(showsPanel, "Movie 4", "01:00 PM", "05:00 PM", "09:00 PM");

        panel.add(showsPanel, BorderLayout.CENTER);

        return panel;
    }


        // SHOW ROW
    

    private void addShow(JPanel parent, String movieName, String time1, String time2, String time3) {

        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(Color.WHITE);
        row.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));

        JLabel movieLabel = new JLabel(movieName);
        movieLabel.setFont(new Font("Arial", Font.BOLD, 14));
        movieLabel.setForeground(dark);

        JPanel timePanel = new JPanel(new GridLayout(1, 3, 5, 0));
        timePanel.setBackground(Color.WHITE);

        timePanel.add(createTimeLabel(time1));
        timePanel.add(createTimeLabel(time2));
        timePanel.add(createTimeLabel(time3));

        row.add(movieLabel, BorderLayout.NORTH);
        row.add(timePanel, BorderLayout.CENTER);

        parent.add(row);
    }


   
    // TIME LABEL
   
    private JLabel createTimeLabel(String time) {

        JLabel label = new JLabel(time, SwingConstants.CENTER);

        label.setOpaque(true);
        label.setBackground(new Color(240, 240, 240));
        label.setForeground(dark);
        label.setFont(new Font("Arial", Font.PLAIN, 11));

        label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 210, 210)),
                BorderFactory.createEmptyBorder(6, 4, 6, 4)
        ));

        return label;
    }

       // NAVIGATION LABEL
    private JLabel createNavLabel(String text) {

        JLabel label = new JLabel(text);

        label.setFont(new Font("Arial", Font.PLAIN, 16));
        label.setForeground(Color.WHITE);

        return label;
    }

   // BUTTON
    private JButton createButton(String text) {

        JButton button = new JButton(text);

        button.setFont(new Font("Arial", Font.BOLD, 13));
        button.setForeground(Color.WHITE);
        button.setBackground(red);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));

        return button;
    }


   
    // MAIN
     public static void main(String[] args) {

       new HomeAditya();
    }
}
