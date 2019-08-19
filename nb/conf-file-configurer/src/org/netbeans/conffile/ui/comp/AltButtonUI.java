/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.conffile.ui.comp;

import static org.netbeans.conffile.ui.comp.UIUtils.CLIENT_PROP_NO_MNEMONIC;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FocusTraversalPolicy;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.GeneralPath;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JRootPane;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.ButtonUI;
import javax.swing.plaf.ComponentInputMapUIResource;

/**
 * A modern-looking button UI from scratch. Background logic is handled by the
 * ButtonStates class, which computes a master state to paint the background and
 * allows other states to decorate the button. Colors are currently hard-coded
 * in the ButtonStates enum and UIManager only minimally used.
 *
 * @author Tim Boudreau
 */
public class AltButtonUI extends ButtonUI implements MouseListener, FocusListener, KeyListener, PropertyChangeListener {

    static final Stroke TWO_STROKE = new BasicStroke(2);
    static final Stroke ONE_STROKE = new BasicStroke(1);
    static AltButtonUI INSTANCE = new AltButtonUI();
    private static final int pad = 0;
    private static final int MARK_INSET = 9;

    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        AbstractButton b = (AbstractButton) c;
        b.setRolloverEnabled(true);
        b.setContentAreaFilled(true);
        c.setOpaque(false);
        c.addMouseListener(this);
        c.addFocusListener(this);
        c.addKeyListener(this);
        CE ce = new CE(c);
        ((AbstractButton) c).getModel().addChangeListener(ce);
        c.putClientProperty("_ce", ce); // NOI18N
        c.addPropertyChangeListener(this);
        b.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        b.setFocusable(true);
        b.setMargin(new Insets(0, 18, 0, 18));
        updateMnemonicBinding(b);
    }

    static class CE implements ChangeListener {

        private final JComponent comp;

        public CE(JComponent comp) {
            this.comp = comp;
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            comp.repaint();
        }
    }

    @Override
    public void uninstallUI(JComponent c) {
        CE ce = (CE) c.getClientProperty("_ce"); // NOI18N
        if (ce != null) {
            ((AbstractButton) c).getModel().removeChangeListener(ce);
        }
        c.removeMouseListener(this);
        c.removeFocusListener(this);
        c.removeKeyListener(this);
        c.removePropertyChangeListener(this);
    }

    @Override
    public boolean contains(JComponent c, int x, int y) {
        Shape shape = buttonShape((AbstractButton) c);
        return shape.contains(x, y);
    }

    private Shape buttonShape(AbstractButton b) {
        return buttonShape(b, 0);
    }

    private boolean isMarkable(JComponent comp) {
        return comp instanceof MarkableAAButton || comp instanceof JToggleButton;
    }

    private boolean isMarked(JComponent comp) {
        return isMarkable(comp)
                && comp instanceof MarkableAAButton ? ((MarkableAAButton) comp).isMarked()
                        : comp instanceof JToggleButton ? ((JToggleButton) comp).isSelected() : false;
    }

    static boolean isInFocusedWindow(JComponent comp) {
        Window w = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
        return w == null ? false : w.isAncestorOf(comp);
    }

    @SuppressWarnings({"BoxedValueEquality", "NumberEquality"}) // NOI18N
    private static Shape buttonShape(AbstractButton b, int expandBy) {
        if (Boolean.TRUE == b.getClientProperty("notop")) { // NOI18N
            FontMetrics fm = fontMetrics(b);
            float arc = (float) (fm.getMaxAscent() + fm.getMaxDescent()) * 0.75F;
            float x = 0;
            float maxx = b.getWidth() - 1;
            float maxy = b.getHeight() - (1 + pad - (expandBy * 2));
            GeneralPath gp = new GeneralPath();
            gp.moveTo(x, 0);
            gp.lineTo(x, maxy - arc);
            gp.quadTo(x, maxy, x + arc, maxy);
            gp.lineTo(maxx - arc, maxy);
            gp.quadTo(maxx, maxy, maxx, maxy - arc);
            gp.lineTo(maxx, 0);
            gp.closePath();
            return gp;
        }
        FontMetrics fm = fontMetrics(b);
        float arc = (float) (fm.getMaxAscent() + fm.getMaxDescent()) * 0.75F;
        int p = ((pad * 2) - (expandBy * 2)) + 2;
        return new RoundRectangle2D.Float(pad - expandBy, pad - expandBy,
                b.getWidth() - p,
                b.getHeight() - p,
                arc, arc);
    }

    private void clearCachedFontMetrics(AbstractButton button) {
        GraphicsConfiguration config = button.getGraphicsConfiguration();
        if (config != null) {
            String gid = "_cfm-" + config.getDevice().getIDstring(); // NOI18N
            FontMetrics fm = (FontMetrics) button.getClientProperty(gid);
            if (fm != null) {
                button.putClientProperty(gid, null);
            }
        }
    }

    private static FontMetrics fontMetrics(JComponent c) {
        if (c.getGraphicsConfiguration() == null) {
            BufferedImage img = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
                    .getDefaultConfiguration().createCompatibleImage(1, 1);
            Graphics2D g = img.createGraphics();
            try {
                return g.getFontMetrics(font(c));
            } finally {
                g.dispose();
            }
        }
        String gid = "_cfm-" + c.getGraphicsConfiguration().getDevice().getIDstring(); // NOI18N
        FontMetrics fm = (FontMetrics) c.getClientProperty(gid);
        if (fm == null) {
            Graphics2D g = (Graphics2D) c.getGraphics();
            try {
                fm = g.getFontMetrics(font(c));
            } finally {
                g.dispose();
            }
            c.putClientProperty(gid, fm);
        }
        return fm;
    }

    static Insets margin(AbstractButton b) {
        Insets result = b.getMargin();
        if (result == null) {
            result = EMPTY;
        }
        return result;
    }

    private static final Insets EMPTY = new Insets(0, 0, 0, 0);

    @Override
    public Dimension getPreferredSize(JComponent c) {
        AbstractButton b = (AbstractButton) c;
        FontMetrics fm = fontMetrics(c);
        Insets ins = c.getInsets();
        Insets margin = margin(b);
        int w = ins.left + ins.right + margin.left + margin.right;
        int h = ins.top + ins.bottom + margin.top + margin.bottom + fm.getMaxAscent() + fm.getMaxDescent();
        String txt = b.getText();
        if (txt != null) {
            w += fm.stringWidth(txt);
        }
        Icon icon = b.getIcon();
        if (icon != null) {
            w += icon.getIconWidth() + b.getIconTextGap();
            h = Math.max(h, icon.getIconHeight());
        }
        h = Math.max(fm.getHeight() * 2, h);
        h += pad * 2;
        w += pad * 2;
        if (h % 2 == 0) {
            // ensure centering
            h--;
        }
        if (w % 2 == 0) {
            w--;
        }
        if (isMarkable(c)) {
            w += MARK_INSET;
        }
        return new Dimension(w, h);
    }

    private static Font font(JComponent c) {
        Font f = c.isFontSet() ? c.getFont() : UIManager.getFont("Button.font"); // NOI18N
        return f;
    }

    @Override
    public void update(Graphics g, JComponent c) {
        UIUtils.withAntialiasing(g, g1 -> {
            AbstractButton b = (AbstractButton) c;
            Graphics2D gg = (Graphics2D) g1;
            Shape shape = buttonShape(b);
            FontMetrics fm = fontMetrics(b);
            ButtonStates.paintBackground(b, gg, fm, shape);
            paint(g1, c);
        });
    }

    static enum ButtonStates {
        DISABLED,
        DEFAULT_BUTTON,
        PLAIN,
        FOCUSED,
        HOVERED,
        PRESSED;

        private static final Map<GKey, Paint> PAINT_CACHE = new HashMap<>();
        private static final Map<String, GKey> KEY_CACHE = new HashMap<>();

        private static final class GKey implements Comparable<GKey> {

            private final String value;
            private long touched = System.currentTimeMillis();

            public GKey(String value) {
                this.value = value;
            }

            public String toString() {
                return value;
            }

            @Override
            public boolean equals(Object obj) {
                return obj instanceof GKey && obj.toString().equals(toString());
            }

            public int hashCode() {
                return value.hashCode();
            }

            void touch() {
                touched = System.currentTimeMillis();
            }

            @Override
            public int compareTo(GKey o) {
                return Long.compare(touched, o.touched);
            }
        }

        /**
         * Cache of radial paints. Keys maintain a last used timestamp, and when
         * the cache gets too large, some items are evicted in LRU order.
         *
         * @param shape A shape to use for bounds
         * @param a The first color
         * @param b The second color
         * @return A paint
         */
        private static Paint radialPaint(Shape shape, Color a, Color b) {
            Rectangle r = shape.getBounds();
            int width4 = Math.max(1, r.width / 3);
            GKey key = new GKey("r" + width4 + "-" // NOI18N
                    + a.getRGB() + "," + a.getAlpha() // NOI18N
                    + "," + b.getRGB() + "," + b.getAlpha()); // NOI18N
            if (KEY_CACHE.containsKey(key.value)) {
                key = KEY_CACHE.get(key.value);
                key.touch();
            }
            Paint result = PAINT_CACHE.get(key);
            if (result == null) {
                result = new RadialGradientPaint(r.x + (r.width / 2), r.y + (r.height / 2),
                        r.width - 4, new float[]{0.01F, 1}, new Color[]{a, b});
                PAINT_CACHE.put(key, result);
                if (PAINT_CACHE.size() > 12) {
                    List<GKey> all = new ArrayList<>(PAINT_CACHE.keySet());
                    Collections.sort(all);
                    for (int i = 0; i < 5; i++) {
                        PAINT_CACHE.remove(all.get(i));
                    }
                }
            }
            return result;
        }

        @SuppressWarnings({"BoxedValueEquality", "NumberEquality"})
        void fill(Graphics2D g, Shape shape, FontMetrics fm, AbstractButton button, Set<ButtonStates> allStates) {
            g.setStroke(TWO_STROKE);
            switch (this) {
                case DISABLED:
                    g.setPaint(new Color(210, 210, 210));
                    g.fill(shape);
                    g.draw(shape);
                    break;
                case DEFAULT_BUTTON:
                    g.setPaint(new Color(128, 128, 255));
                    g.fill(shape);
                    g.setPaint(new Color(100, 100, 200));
                    g.draw(shape);
                    break;
                case PLAIN:
                    g.setPaint(new Color(255, 255, 255, 255));
                    g.fill(shape);
                    if (Boolean.TRUE != button.getClientProperty("notop")) { // NOI18N
                        g.setStroke(ONE_STROKE);
                        g.setPaint(UIManager.getColor("controlShadow")); // NOI18N
                    } else {
                        g.setStroke(ONE_STROKE);
                        g.setPaint(new Color(200, 200, 180));
                    }
                    g.draw(shape);
                    break;
                case FOCUSED:
                    g.setColor(new Color(255, 200, 40, 50));
                    g.fill(shape);
                    break;
                case HOVERED:
                    g.setColor(Color.WHITE);
                    g.fill(shape);
                    break;
                case PRESSED:
                    g.setColor(new Color(255, 240, 0, 160));
                    g.fill(shape);
                    break;
            }
        }

        @SuppressWarnings({"BoxedValueEquality", "NumberEquality"}) // NOI18N
        void decorate(Graphics2D g, Shape shape, FontMetrics fm, AbstractButton button, Set<ButtonStates> all) {
            switch (this) {
                case FOCUSED:
                    Paint p;
                    if (all.contains(DEFAULT_BUTTON)) {
                        p = radialPaint(shape, new Color(0, 0, 255, 0),
                                new Color(0, 0, 255, 128));
                    } else {
                        p = radialPaint(shape, new Color(0, 0, 255, 0),
                                new Color(0, 0, 255, 80));
                    }
                    g.setPaint(p);
                    g.fill(shape);
                    if (Boolean.TRUE != button.getClientProperty("notop")) { // NOI18N
                        g.setStroke(ONE_STROKE);
                        g.setColor(new Color(0, 0, 0, 96));
                        g.draw(shape);
                    } else {
                        g.setStroke(TWO_STROKE);
                        g.setColor(UIManager.getColor("controlShadow")); // NOI18N
                        g.draw(shape);
                    }
                    break;
                case HOVERED:
                    Paint p1;
                    if (all.contains(PLAIN)) {
                        p1 = radialPaint(shape,
                                new Color(255, 255, 0, 48),
                                new Color(255, 255, 255, 0)
                        );
                    } else {
                        p1 = radialPaint(shape, new Color(255, 255, 255, 0),
                                new Color(80, 80, 80, 128));
                    }
                    g.setPaint(p1);
                    g.fill(shape);
                    break;
                case PLAIN:
                    if (Boolean.TRUE == button.getClientProperty("notop")) { // NOI18N
                        g.setStroke(TWO_STROKE);
                    } else {
                        g.setStroke(ONE_STROKE);
                    }
                    g.setColor(UIManager.getColor("controlShadow")); // NOI18N
                    g.draw(shape);
                    break;

            }
        }

        static void paintBackground(AbstractButton button, Graphics2D graphics, FontMetrics fm, Shape shape) {
            Set<ButtonStates> all = states(button);
            ButtonStates fillState = fillState(button);
            fillState.fill(graphics, shape, fm, button, all);
            for (ButtonStates state : all) {
                state.decorate(graphics, shape, fm, button, all);
            }
        }

        Paint foreground() {
            switch (this) {
                case DISABLED:
                    return new Color(180, 180, 180);
                case DEFAULT_BUTTON:
                    return Color.WHITE;
                case PRESSED:
                    return new Color(160, 140, 0);
                case FOCUSED:
                case PLAIN:
                case HOVERED:
                    return Color.darkGray;
                default:
                    return Color.darkGray;
            }
        }

        static ButtonStates fillState(AbstractButton button) {
            if (!button.isEnabled()) {
                return DISABLED;
            } else if (button.getRootPane() != null && button.getRootPane().getDefaultButton() == button) {
                return DEFAULT_BUTTON;
            } else if (button.getModel().isPressed() || button.getModel().isArmed()) {
                return PRESSED;
            } else if (button.getModel().isRollover()) {
                return isInFocusedWindow(button) ? HOVERED : PLAIN;
            } else if (button.hasFocus()) {
                return isInFocusedWindow(button) ? FOCUSED : PLAIN;
            }
            return PLAIN;
        }

        static Set<ButtonStates> states(AbstractButton button) {
            Set<ButtonStates> result = EnumSet.noneOf(ButtonStates.class);
            result.add(PLAIN);
            if (button.getRootPane() != null && button.getRootPane().getDefaultButton() == button) {
                result.add(DEFAULT_BUTTON);
                result.remove(PLAIN);
            }
            if (button.hasFocus() && isInFocusedWindow(button)) {
                result.add(FOCUSED);
                result.remove(PLAIN);
            }
            if (button.getModel().isPressed()) {
                result.add(PRESSED);
                result.remove(PLAIN);
            }
            if (button.getModel().isRollover() && isInFocusedWindow(button)) {
                result.add(HOVERED);
            }
            return result;
        }
    }

    private static Color dotColor(AbstractButton b) {
        JRootPane root = b.getRootPane();
        ButtonModel mdl = b.getModel();
        if (!b.isEnabled()) {
            return Color.GRAY;
        } else if (root != null && root.getDefaultButton() == b) {
            if (b.hasFocus()) {
                return Color.WHITE;
            } else {
                return new Color(100, 100, 255);
            }
        } else if (mdl.isRollover()) {
            return new Color(180, 160, 80);
        } else if (mdl.isPressed()) {
            return new Color(220, 0, 0);
        } else {
            return new Color(128, 128, 255);
        }
    }

    private static void paintMark(int x, int y, AbstractButton b, Graphics2D g) {
        Rectangle r = new Rectangle(x, y, 10, 10);
        g.setColor(b.getForeground());
        g.drawRoundRect(r.x, r.y, r.width, r.height, 11, 11);
        g.setColor(dotColor(b));
        g.fillRoundRect(r.x, r.y, r.width, r.height, 11, 11);
    }

    static void paintMark(AbstractButton b, Graphics2D g) {
        Insets ins = b.getInsets();
        Insets mar = margin(b);
        int h = b.getHeight() - (ins.top + ins.bottom + mar.top + mar.bottom);
        int y = (ins.top + mar.top) + ((h / 2) - 5);
        int x = 8;
        paintMark(x, y, b, g);
    }

    @Override
    @SuppressWarnings({"BoxedValueEquality", "NumberEquality"}) //NOI18N
    public void paint(Graphics g, JComponent c) {
        AbstractButton button = (AbstractButton) c;
        String text = button.getText();
        Graphics2D gg = (Graphics2D) g;
        Paint foreground = ButtonStates.fillState(button).foreground();
        gg.setPaint(foreground);
        Font f = c.isFontSet() ? c.getFont() : UIManager.getFont("Button.font"); // NOI18N
        if (c.getRootPane() != null && c.getRootPane().getDefaultButton() == c) {
            f = f.deriveFont(Font.BOLD);
        }
        g.setFont(f);
        FontMetrics fm = g.getFontMetrics();
        Insets a = c.getInsets();
        Insets b = margin(button);
        int width = text == null ? 0 : fm.stringWidth(text);
        int y;
        int minY = a.top + b.top;
        int maxY = c.getHeight() - (a.bottom + b.bottom);
        int valign = button.getVerticalAlignment();
        switch (valign) {
            case SwingConstants.TOP:
                y = minY + fm.getMaxAscent();
                break;
            case SwingConstants.BOTTOM:
                y = maxY - (fm.getMaxAscent() + fm.getMaxDescent());
                break;
            case SwingConstants.CENTER:
            default:
                y = ((c.getHeight() / 2) + (fm.getAscent() / 2)) - 1;
                break;
        }
        int minX = a.left + b.left;
        boolean marked = isMarked(c);
        if (marked) {
            minX += MARK_INSET;
        }
        int maxX = c.getWidth() - (a.right + b.right);
        int halign = button.getHorizontalAlignment();
        int x;
        switch (halign) {
            case SwingConstants.CENTER:
                int middle = minX + ((maxX - minX) / 2);
                x = middle - (width / 2);
                break;
            case SwingConstants.RIGHT:
            case SwingConstants.TRAILING:
                x = maxX - width;
                break;
            case SwingConstants.LEFT:
            case SwingConstants.LEADING:
            default:
                x = minX;
                break;
        }
        Icon icon = button.getIcon();
        if (icon != null) {
            int iconY = y + ((maxY - y) / 2) - (icon.getIconHeight() / 2);
            x += icon.getIconWidth() + button.getIconTextGap();
            icon.paintIcon(c, g, x, iconY);
        }
        if (marked) {
            paintMark(button, gg);
        }
        if (text != null) {
            gg.setPaint(foreground);
            g.drawString(text, x, y);
            int ix = button.getDisplayedMnemonicIndex();
            if (ix >= 0 && ix < text.length() && Boolean.TRUE != button.getClientProperty(CLIENT_PROP_NO_MNEMONIC)) {
                gg.setStroke(ONE_STROKE);
                String sub = text.substring(0, ix);
                int offx = x + fm.stringWidth(sub);
                String ch = text.substring(ix, ix + 1);
                int mnemWIdth = fm.stringWidth(ch);
                int mnemY = y + 2;
                g.drawLine(offx, mnemY, offx + mnemWIdth, mnemY);
            }
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        AbstractButton button = (AbstractButton) e.getSource();
        if (!e.isPopupTrigger() && e.getClickCount() == 1 && button.isEnabled()) {

            if (contains(button, e.getX(), e.getY())) {
                ActionEvent ae = new ActionEvent(e.getSource(), ActionEvent.ACTION_PERFORMED,
                        button.getActionCommand());
                for (ActionListener al : button.getActionListeners()) {
                    al.actionPerformed(ae);
                }
                e.consume();
            }
            if (button.isDisplayable()) {
                button.requestFocus();
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        AbstractButton button = (AbstractButton) e.getSource();
        if (button.isEnabled()) {
            button.getModel().setPressed(true);
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        AbstractButton button = (AbstractButton) e.getSource();
        if (button.isEnabled()) {
            button.getModel().setPressed(false);
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        AbstractButton button = (AbstractButton) e.getSource();
        if (button.isEnabled()) {
            button.getModel().setRollover(true);
        }
    }

    @Override
    public void mouseExited(MouseEvent e) {
        AbstractButton button = (AbstractButton) e.getSource();
        if (button.isEnabled()) {
            button.getModel().setRollover(false);
        }
    }

    private static void sendFocusToNext(AbstractButton button) {
        Container top = button.getFocusCycleRootAncestor();
        if (top != null) {
            FocusTraversalPolicy pol = top.getFocusTraversalPolicy();
            if (pol != null) {
                Component curr = button;
                Set<Component> seen = new HashSet<>(20);
                while (curr != null && !curr.isEnabled()) {
                    curr = pol.getComponentAfter(top, curr);
                    if (seen.contains(curr)) {
                        break;
                    }
                    seen.add(curr);
                }
                if (curr == null) {
                    curr = pol.getInitialComponent((Window) SwingUtilities.getWindowAncestor(button));
                }
                if (curr == null || curr == button || !curr.isEnabled()) {
                    curr = pol.getFirstComponent(top);
                }
                if (curr != null && curr != button && curr.isEnabled()) {
                    curr.requestFocus();
                }
            }
        }
        button.repaint();
    }

    @Override
    public void focusGained(FocusEvent e) {
        AbstractButton button = (AbstractButton) e.getSource();
        if (!button.isEnabled() && button.hasFocus()) {
            sendFocusToNext(button);
        }
        button.repaint();
    }

    @Override
    public void focusLost(FocusEvent e) {
        AbstractButton button = (AbstractButton) e.getSource();
        button.repaint();
    }

    @Override
    public void keyTyped(KeyEvent e) {
        AbstractButton b = (AbstractButton) e.getSource();

        if (b.isEnabled()) {
            e.consume();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        AbstractButton b = (AbstractButton) e.getSource();
        if (b.isEnabled()) {
            if (e.getKeyCode() == KeyEvent.VK_SPACE && e.getModifiersEx() == 0) {
                b.getModel().setArmed(true);
                e.consume();
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        AbstractButton b = (AbstractButton) e.getSource();
        if (b.isEnabled()) {
            if (e.getKeyCode() == KeyEvent.VK_SPACE && e.getModifiersEx() == 0) {
                ActionEvent evt = new ActionEvent(b, ActionEvent.ACTION_PERFORMED, b.getActionCommand());
                for (ActionListener ae : b.getActionListeners()) {
                    ae.actionPerformed(evt);
                }
                b.getModel().setArmed(false);
                e.consume();
            }
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        AbstractButton button = (AbstractButton) evt.getSource();
        switch (evt.getPropertyName()) {
            case "ancestor": // NOI18N
                if (!button.isDisplayable()) {
                    Container top = (Container) evt.getOldValue();
                    if (top != null) {
                        top = top.getFocusCycleRootAncestor();
                        FocusTraversalPolicy p = top.getFocusTraversalPolicy();
                        if (p != null) {
                            Window w = top instanceof Window ? (Window) top
                                    : (Window) SwingUtilities.getAncestorOfClass(Window.class, top);
                            if (w != null) {
                                Component c = p.getInitialComponent(w);
                                if (c != null && c.isEnabled()) {
                                    c.requestFocus();
                                }
                            }
                        }
                    }
                    clearCachedFontMetrics(button);
                }
                break;
            case "font": // NOI18N
                clearCachedFontMetrics(button);
            case "icon": // NOI18N
            case "text": // NOI18N
            case "margin": // NOI18N
            case "border": // NOI18N
                button.invalidate();
                button.revalidate();
                button.repaint();
                break;
            case AbstractButton.MNEMONIC_CHANGED_PROPERTY:
                updateMnemonicBinding(button);
            case "enabled":
                if (Boolean.FALSE.equals(evt.getNewValue())) {
                    ButtonModel mdl = button.getModel();
                    mdl.setArmed(false);
                    mdl.setRollover(false);
                    mdl.setPressed(false);
                    mdl.setSelected(false);
                    if (button.hasFocus()) {
                        sendFocusToNext(button);
                    }
                }
            default:
                button.repaint();
        }
    }

    void updateMnemonicBinding(AbstractButton b) {
        int m = b.getMnemonic();
        if (m != 0) {
            InputMap map = SwingUtilities.getUIInputMap(
                    b, JComponent.WHEN_IN_FOCUSED_WINDOW);

            if (map == null) {
                map = new ComponentInputMapUIResource(b);
                SwingUtilities.replaceUIInputMap(b,
                        JComponent.WHEN_IN_FOCUSED_WINDOW, map);
            }
            map.clear();
            map.put(KeyStroke.getKeyStroke(m, KeyEvent.ALT_DOWN_MASK, false),
                    "pressed"); // NOI18N
            map.put(KeyStroke.getKeyStroke(m, KeyEvent.ALT_DOWN_MASK, true),
                    "released"); // NOI18N
            b.getActionMap().put("pressed", new AbstractAction() { // NOI18N
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (b.isEnabled()) {
                        b.getModel().setArmed(true);
                    }
                }
            });
            b.getActionMap().put("released", new AbstractAction() { // NOI18N
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (b.isEnabled()) {
                        b.doClick();
                        b.getModel().setArmed(false);
                    }
                }
            });
        } else {
            InputMap map = SwingUtilities.getUIInputMap(b, JComponent.WHEN_IN_FOCUSED_WINDOW);
            if (map != null) {
                map.clear();
            }
        }
    }
}
