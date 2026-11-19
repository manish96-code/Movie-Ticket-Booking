package scratch;

import com.cinemats.data.MovieMockData;
import com.cinemats.model.Movie;
import com.cinemats.ui.staff.OrderBookingPage;
import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public class TestScrollLimits {
    public static void main(String[] args) throws Exception {
        JPanel parent = new JPanel(new BorderLayout());
        parent.setBounds(0, 0, 1024, 576);

        OrderBookingPage page = new OrderBookingPage(parent);
        parent.add(page, BorderLayout.CENTER);

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

        JScrollBar vBar = cardsScroll.getVerticalScrollBar();
        System.out.println("Initial: min=" + vBar.getMinimum() + ", max=" + vBar.getMaximum() + ", extent=" + vBar.getVisibleAmount() + ", val=" + vBar.getValue());

        int[] scrollTargets = {0, 60, 200, 500, 1000, 1500, 2000, 2500};
        for (int target : scrollTargets) {
            vBar.setValue(target);
            cardsScroll.getViewport().setViewPosition(new Point(0, vBar.getValue()));
            Rectangle viewRect = cardsScroll.getViewport().getViewRect();
            System.out.println("\n--- Scroll value: " + vBar.getValue() + " (viewRect: " + viewRect + ") ---");
            // Which cards are visible in viewRect?
            for (int i = 0; i < movieListPanel.getComponentCount(); i++) {
                Component c = movieListPanel.getComponent(i);
                Rectangle b = c.getBounds();
                if (viewRect.intersects(b)) {
                    Rectangle intersection = viewRect.intersection(b);
                    int visiblePercent = (int) ((intersection.getHeight() / b.getHeight()) * 100);
                    System.out.println("  Card " + i + " visible " + visiblePercent + "% (y=" + b.y + ", relY=" + (b.y - viewRect.y) + ")");
                }
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

