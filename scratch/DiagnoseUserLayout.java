package scratch;

import com.cinemats.data.MovieMockData;
import com.cinemats.model.Movie;
import com.cinemats.ui.staff.OrderBookingPage;
import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public class DiagnoseUserLayout {
    public static void main(String[] args) throws Exception {
        JPanel parent = new JPanel(new BorderLayout());
        parent.setBounds(0, 0, 1920, 1080);

        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(210, 1080));
        parent.add(sidebar, BorderLayout.WEST);

        OrderBookingPage page = new OrderBookingPage(parent);
        parent.add(page, BorderLayout.CENTER);
        parent.doLayout();
        page.doLayout();

        List<Movie> mock18 = MovieMockData.getInitialMovies();
        Field allMoviesField = OrderBookingPage.class.getDeclaredField("allMoviesList");
        allMoviesField.setAccessible(true);
        allMoviesField.set(page, mock18);

        Field filteredField = OrderBookingPage.class.getDeclaredField("filteredMoviesList");
        filteredField.setAccessible(true);
        filteredField.set(page, mock18);

        Method renderMethod = OrderBookingPage.class.getDeclaredMethod("renderMovieList");
        renderMethod.setAccessible(true);
        renderMethod.invoke(page);

        layoutRecursive(parent);

        Field cardsScrollField = OrderBookingPage.class.getDeclaredField("cardsScroll");
        cardsScrollField.setAccessible(true);
        JScrollPane cardsScroll = (JScrollPane) cardsScrollField.get(page);

        Field movieListPanelField = OrderBookingPage.class.getDeclaredField("movieListPanel");
        movieListPanelField.setAccessible(true);
        JPanel movieListPanel = (JPanel) movieListPanelField.get(page);

        System.out.println("=== DIAGNOSIS WITH 1920x1080 ===");
        printDetails(cardsScroll, movieListPanel);
    }

    private static void printDetails(JScrollPane scroll, JPanel panel) {
        System.out.println("scroll size: " + scroll.getSize());
        System.out.println("viewport size: " + scroll.getViewport().getSize());
        System.out.println("panel size: " + panel.getSize());
        System.out.println("panel prefSize: " + panel.getPreferredSize());
        JScrollBar vBar = scroll.getVerticalScrollBar();
        System.out.println("vBar min: " + vBar.getMinimum() + ", max: " + vBar.getMaximum() + ", extent: " + vBar.getVisibleAmount() + ", val: " + vBar.getValue());
        System.out.println("Card count in panel: " + panel.getComponentCount());
        for (int i = 0; i < panel.getComponentCount(); i++) {
            Component c = panel.getComponent(i);
            if (i % 4 == 0 || i >= 12) {
                System.out.println("  Card " + i + ": " + c.getBounds());
            }
        }
    }

    private static void layoutRecursive(Container c) {
        c.doLayout();
        for (Component child : c.getComponents()) {
            if (child instanceof Container) {
                layoutRecursive((Container) child);
            }
        }
    }
}
