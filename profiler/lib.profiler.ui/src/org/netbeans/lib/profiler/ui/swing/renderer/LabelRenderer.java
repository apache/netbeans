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

package org.netbeans.lib.profiler.ui.swing.renderer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.plaf.LabelUI;
import javax.swing.plaf.basic.BasicLabelUI;

/**
 * JLabel subclass to be used as a high-performance Table/Tree/List renderer.
 * Make sure you call setOpaque(true) for painting background.
 * For custom non-label Table/Tree/List renderer extend BaseRenderer.
 *
 * @author Jiri Sedlacek
 */
public class LabelRenderer extends JLabel implements ProfilerRenderer {
    
    // --- Constructor ---------------------------------------------------------
    
    public LabelRenderer() {
        this(false);
    }

    public LabelRenderer(boolean plain) {
        setHorizontalAlignment(LEADING);
        setVerticalAlignment(TOP);
        setSize(Integer.MAX_VALUE, Integer.MAX_VALUE);

        if (plain) {
            setOpaque(false);
        } else {
            setOpaque(true);
            setMargin(3, 3, 3, 3);
        }

        iconTextGap = super.getIconTextGap();
    }
    
    // --- Renderer ------------------------------------------------------------
    
    public void setValue(Object value, int row) {
        if (value == null) setText(""); // NOI18N
        else setText(value.toString());
    }
    
    public JComponent getComponent() {
        return this;
    }

    @Override
    public String toString() {
        return getText();
    }
    
    // --- Appearance ----------------------------------------------------------
    
    private static final LabelRendererUI UI = new LabelRendererUI();
    private static final int DIRTY = Integer.MIN_VALUE;
    
    private Dimension preferredSize;
    
    private int iconWidth;
    private int iconHeight;
    private int iconTextGap;
    private int textWidth;
    private int fontAscent;

    @Override
    public void setUI(LabelUI ui) {
        super.setUI(UI);
    }

    @Override
    public Dimension getPreferredSize() {
        return sharedDimension(getPreferredSizeImpl());
    }
    
    public Dimension getPreferredSizeImpl() {
        if (preferredSize == null) preferredSize = new Dimension(DIRTY, DIRTY);
        
        if (preferredSize.width == DIRTY) {
            textWidth = text == null || text.isEmpty() ? 0 : fontMetrics.stringWidth(text);
            preferredSize.width = iconWidth + textWidth;
            preferredSize.width += margin.left + margin.right;
            if (iconWidth > 0 && textWidth > 0) preferredSize.width += iconTextGap;
        }

        if (preferredSize.height == DIRTY) {
            fontAscent = fontMetrics.getAscent();
            preferredSize.height = fontAscent + fontMetrics.getDescent();
            preferredSize.height += margin.top + margin.bottom;
        }
    
        return preferredSize;
    }

    private void resetPreferredSize(boolean width, boolean height) {
        if (preferredSize == null) return;
        if (width) preferredSize.width = DIRTY;
        if (height) preferredSize.height = DIRTY;
    }

    @Override
    public void paint(Graphics g) {
        int xx = location.x;
        int h = size.height;
        int hh = getPreferredSizeImpl().height; // lazily computes dirty metrics
        
        if (background != null && isOpaque()) {
            g.setColor(background);
            g.fillRect(xx, location.y, size.width, h);
        }
        
        g.setFont(getFont());
        
        int hAlign = getHorizontalAlignment();
        if (hAlign == LEADING) {
            xx += margin.left;
        } else if (hAlign == CENTER) {
            int w = size.width - textWidth - iconWidth;
            if (textWidth > 0 && iconWidth > 0 ) w -= iconTextGap;
            xx += Math.max(margin.left, w / 2);
        } else {
            xx += size.width - margin.right - textWidth;
            if (iconWidth > 0 ) xx += - iconWidth - iconTextGap;
        }
        
        if (iconWidth > 0) {
            int yy = (h - iconHeight) / 2;
            icon.paintIcon(this, g, xx, location.y + yy);
            xx += iconWidth + iconTextGap;
        }
        
        if (textWidth > 0) {
            int yy = (h - hh - fontSizeDiff) / 2 + margin.top;
            UI.paintEnabledText(this, g, text, xx, location.y + yy + fontAscent);
        }
    }

    // --- Tools ---------------------------------------------------------------
    
    private Point sharedPoint;
    private Dimension sharedDimension;
    private Rectangle sharedRectangle;
    
    protected final Point sharedPoint(int x, int y) {
        if (sharedPoint == null) sharedPoint = new Point();
        sharedPoint.x = x;
        sharedPoint.y = y;
        return sharedPoint;
    }
    
    protected final Point sharedPoint(Point point) {
        return sharedPoint(point.x, point.y);
    }
    
    protected final Dimension sharedDimension(int width, int height) {
        if (sharedDimension == null) sharedDimension = new Dimension();
        sharedDimension.width = width;
        sharedDimension.height = height;
        return sharedDimension;
    }
    
    protected final Dimension sharedDimension(Dimension dimension) {
        return sharedDimension(dimension.width, dimension.height);
    }
    
    protected final Rectangle sharedRectangle(int x, int y, int width, int height) {
        if (sharedRectangle == null) sharedRectangle = new Rectangle();
        sharedRectangle.x = x;
        sharedRectangle.y = y;
        sharedRectangle.width = width;
        sharedRectangle.height = height;
        return sharedRectangle;
    }
    
    protected final Rectangle sharedRectangle(Rectangle rectangle) {
        return sharedRectangle(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }
    
    // --- Geometry ------------------------------------------------------------
    
    protected final Point location = new Point();
    protected final Dimension size = new Dimension();

    @Override
    public void move(int x, int y) {
        location.x = x;
        location.y = y;
    }

    @Override
    public Point getLocation() {
        return sharedPoint(location);
    }

    @Override
    public int getX() {
        return location.x;
    }

    @Override
    public int getY() {
        return location.y;
    }

    @Override
    public void setSize(int w, int h) {
        size.width = w;
        size.height = h;
    }

    @Override
    public Dimension getSize() {
        return sharedDimension(size);
    }

    @Override
    public int getWidth() {
        return size.width;
    }

    @Override
    public int getHeight() {
        return size.height;
    }

    @Override
    public Rectangle getBounds() {
        return sharedRectangle(location.x, location.y, size.width, size.height);
    }

    @Override
    public void reshape(int x, int y, int w, int h) {
        // ignore x, y: used only for move(x, y)
//        location.x = x;
//        location.y = y;
        size.width = w;
        size.height = h;
    }

    // --- Margins -------------------------------------------------------------
    
    private final Insets insets = new Insets(0, 0, 0, 0);
    private final Insets margin = new Insets(0, 0, 0, 0);

    @Override
    public Insets getInsets() {
        return insets;
    }

    @Override
    public Insets getInsets(Insets insets) {
        return this.insets;
    }
    
    public void setMargin(int top, int left, int bottom, int right) {
        margin.top = top;
        margin.left = left;
        margin.bottom = bottom;
        margin.right = right;
        resetPreferredSize(true, true);
    }
    
    public Insets getMargin() {
        return margin;
    }

    // --- Other peformance tweaks ---------------------------------------------
    
    private FontMetrics fontMetrics;
    private int fontSizeDiff;
    private String text;
    private Icon icon;
    private Color foreground;
    private Color background;
    private boolean enabled = true;

    @Override
    public void setText(String text) {
        this.text = text;
        resetPreferredSize(true, false);
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public void setIcon(Icon icon) {
        this.icon = icon;
        iconWidth = icon == null ? 0 : icon.getIconWidth();
        iconHeight = icon == null ? 0 : icon.getIconHeight();
        resetPreferredSize(true, false); // Icon likely won't change height
    }

    @Override
    public Icon getIcon() {
        return icon;
    }

    @Override
    public void setForeground(Color foreground) {
        this.foreground = foreground;
    }

    @Override
    public Color getForeground() {
        return foreground;
    }

    @Override
    public void setBackground(Color background) {
        this.background = background;
    }

    @Override
    public Color getBackground() {
        return background;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public int getDisplayedMnemonicIndex() {
        return -1;
    }

    @Override
    public FontMetrics getFontMetrics(Font font) {
        return fontMetrics;
    }

    @Override
    public void setFont(Font font) {
        super.setFont(font);
        fontMetrics = super.getFontMetrics(font);
        resetPreferredSize(true, true);
    }
    
    // Use to keep the baseline for various font-sized instances
    public void changeFontSize(int diff) {
        fontSizeDiff = diff;
        Font font = getFont();
        setFont(font.deriveFont(font.getSize2D() + diff));
    }

    @Override
    public int getIconTextGap() {
        return iconTextGap;
    }


    @Override
    public void setIconTextGap(int iconTextGap) {
        this.iconTextGap = iconTextGap;
        resetPreferredSize(true, false);
    }
    
    // --- Painting / Layout ---------------------------------------------------

    @Override
    public void validate() {}

    @Override
    public void revalidate() {}

    @Override
    public void repaint(long tm, int x, int y, int width, int height) {}

    @Override
    public void repaint(Rectangle r) {}

    @Override
    public void repaint() {}

    @Override
    public void setDisplayedMnemonic(int key) {}

    @Override
    public void setDisplayedMnemonic(char aChar) {}

    @Override
    public void setDisplayedMnemonicIndex(int index) {}
    
    // --- Events --------------------------------------------------------------

    @Override
    public void firePropertyChange(String propertyName, byte oldValue, byte newValue) {}

    @Override
    public void firePropertyChange(String propertyName, char oldValue, char newValue) {}

    @Override
    public void firePropertyChange(String propertyName, short oldValue, short newValue) {}

    @Override
    public void firePropertyChange(String propertyName, int oldValue, int newValue) {}

    @Override
    public void firePropertyChange(String propertyName, long oldValue, long newValue) {}

    @Override
    public void firePropertyChange(String propertyName, float oldValue, float newValue) {}

    @Override
    public void firePropertyChange(String propertyName, double oldValue, double newValue) {}

    @Override
    public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {}

    @Override
    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {}


    private static final class LabelRendererUI extends BasicLabelUI {
        @Override
        protected void paintEnabledText(JLabel l, Graphics g, String s, int textX, int textY) {
            super.paintEnabledText(l, g, s, textX, textY);
        }
    }

}
