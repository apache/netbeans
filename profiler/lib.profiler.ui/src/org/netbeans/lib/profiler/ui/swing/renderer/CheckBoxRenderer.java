/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
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
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import javax.swing.JCheckBox;
import javax.swing.JComponent;

/**
 *
 * @author Jiri Sedlacek
 */
public class CheckBoxRenderer extends JCheckBox implements ProfilerRenderer {
    
    // --- Constructor ---------------------------------------------------------
    
    public CheckBoxRenderer() {
        setOpaque(true);
        setHorizontalAlignment(CENTER);
    }
    
    // --- Renderer ------------------------------------------------------------

    public void setValue(Object value, int row) {
        if (value == null) setSelected(false);
        else setSelected(((Boolean)value).booleanValue());
    }

    public JComponent getComponent() {
        return this;
    }
    
    public String toString() {
        return Boolean.toString(isSelected());
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
    
    public void move(int x, int y) {
        location.x = x;
        location.y = y;
    }
    
    public Point getLocation() {
        return sharedPoint(location);
    }
    
    public int getX() {
        return location.x;
    }
    
    public int getY() {
        return location.y;
    }
    
    public void setSize(int w, int h) {
        size.width = w;
        size.height = h;
    }
    
    public Dimension getSize() {
        return sharedDimension(size);
    }
    
    public int getWidth() {
        return size.width;
    }
    
    public int getHeight() {
        return size.height;
    }
    
    public Rectangle getBounds() {
        return sharedRectangle(location.x, location.y, size.width, size.height);
    }
    
    public void reshape(int x, int y, int w, int h) {
        // ignore x, y: used only for move(x, y)
//        location.x = x;
//        location.y = y;
        size.width = w;
        size.height = h;
    }

    // --- Insets --------------------------------------------------------------
    
    private final Insets insets = new Insets(0, 0, 0, 0);
    
    public Insets getInsets() {
        return insets;
    }

    public Insets getInsets(Insets insets) {
        return this.insets;
    }

    // --- Other peformance tweaks ---------------------------------------------
    
    private Color foreground;
    private Color background;
    private boolean enabled = true;

    public void setForeground(Color foreground) {
        this.foreground = foreground;
    }

    public Color getForeground() {
        return foreground;
    }
    
    public void setBackground(Color background) {
        this.background = background;
    }

    public Color getBackground() {
        return background;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }
    
    // --- Painting / Layout ---------------------------------------------------

    public void validate() {}

    public void revalidate() {}

    public void repaint(long tm, int x, int y, int width, int height) {}

    public void repaint(Rectangle r) {}

    public void repaint() {}

    public void setDisplayedMnemonicIndex(int index) {}
    
    // --- Events --------------------------------------------------------------

    public void firePropertyChange(String propertyName, byte oldValue, byte newValue) {}

    public void firePropertyChange(String propertyName, char oldValue, char newValue) {}

    public void firePropertyChange(String propertyName, short oldValue, short newValue) {}

    public void firePropertyChange(String propertyName, int oldValue, int newValue) {}

    public void firePropertyChange(String propertyName, long oldValue, long newValue) {}

    public void firePropertyChange(String propertyName, float oldValue, float newValue) {}

    public void firePropertyChange(String propertyName, double oldValue, double newValue) {}

    public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {}

    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {}
    
}
