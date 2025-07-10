
import javax.swing.*;
import java.awt.*;

// Custom Button UI for rounded corners
class StyledButtonUI1 extends javax.swing.plaf.basic.BasicButtonUI {
    @Override
    public void paint(Graphics g, JComponent c) {
        AbstractButton button = (AbstractButton) c;
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw rounded rectangle background
        g2d.setColor(button.getBackground());
        g2d.fillRoundRect(0, 0, button.getWidth(), button.getHeight(), 20, 20);

        // Draw button text
        super.paint(g, c);
    }
}