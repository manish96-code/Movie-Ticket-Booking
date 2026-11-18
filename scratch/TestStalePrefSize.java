package scratch;

import com.cinemats.util.WrapLayout;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class TestStalePrefSize {
    static class ScrollableWrapPanel extends JPanel implements Scrollable {
        public ScrollableWrapPanel(LayoutManager layout) { super(layout); }
        @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        @Override public int getScrollableUnitIncrement(Rectangle r, int o, int d) { return 36; }
        @Override public int getScrollableBlockIncrement(Rectangle r, int o, int d) { return 144; }
        @Override public boolean getScrollableTracksViewportWidth() { return true; }
        @Override public boolean getScrollableTracksViewportHeight() { return false; }
    }

    public static void main(String[] args) {
        JPanel panel = new ScrollableWrapPanel(new WrapLayout(FlowLayout.LEFT, 10, 12));
        panel.setBorder(new EmptyBorder(10, 10, 48, 10));
        for (int i = 0; i < 16; i++) {
            JPanel card = new JPanel();
            card.setPreferredSize(new Dimension(144, 232));
            panel.add(card);
        }

        JScrollPane scroll = new JScrollPane(panel);

        System.out.println("1. Before layout, panel.getWidth() = " + panel.getWidth());
        System.out.println("   panel.getPreferredSize() = " + panel.getPreferredSize());

        scroll.setSize(640, 500);
        scroll.doLayout();
        scroll.getViewport().doLayout();

        System.out.println("2. After scroll.setSize(640, 500) and doLayout:");
        System.out.println("   panel.getWidth() = " + panel.getWidth());
        System.out.println("   panel.getHeight() = " + panel.getHeight());
        System.out.println("   panel.getPreferredSize() = " + panel.getPreferredSize());
        System.out.println("   vBar.getMaximum() = " + scroll.getVerticalScrollBar().getMaximum());
        System.out.println("   vBar.getVisibleAmount() = " + scroll.getVerticalScrollBar().getVisibleAmount());
    }
}
