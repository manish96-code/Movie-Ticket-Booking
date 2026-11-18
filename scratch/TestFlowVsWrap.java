package scratch;

import com.cinemats.util.WrapLayout;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class TestFlowVsWrap {
    public static void main(String[] args) {
        JPanel panel = new JPanel(new WrapLayout(FlowLayout.LEFT, 10, 12));
        panel.setBorder(new EmptyBorder(10, 10, 48, 10));
        for (int i = 0; i < 16; i++) {
            JPanel card = new JPanel();
            card.setPreferredSize(new Dimension(144, 232));
            panel.add(card);
        }

        for (int w = 630; w <= 660; w += 5) {
            panel.setSize(w, 1000);
            panel.doLayout();
            Dimension pref = panel.getPreferredSize();
            int numRows = 0;
            int lastY = -1;
            for (int i = 0; i < 16; i++) {
                if (panel.getComponent(i).getY() != lastY) {
                    numRows++;
                    lastY = panel.getComponent(i).getY();
                }
            }
            System.out.println("16 cards, Width=" + w + " -> numRows=" + numRows + ", prefSize=" + pref);
        }
    }
}
