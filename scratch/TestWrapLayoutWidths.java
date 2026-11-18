package scratch;

import com.cinemats.util.WrapLayout;
import javax.swing.*;
import java.awt.*;

public class TestWrapLayoutWidths {
    public static void main(String[] args) {
        JPanel panel = new JPanel(new WrapLayout(FlowLayout.LEFT, 10, 12));
        for (int i = 0; i < 12; i++) {
            JPanel card = new JPanel();
            card.setPreferredSize(new Dimension(144, 232));
            panel.add(card);
        }

        int[] widths = {0, 300, 450, 600, 620, 630, 640, 700, 800, 1000};
        for (int w : widths) {
            panel.setSize(w, 0);
            Dimension pref = panel.getPreferredSize();
            System.out.println("12 cards, width=" + w + " -> prefSize=" + pref);
        }
    }
}
