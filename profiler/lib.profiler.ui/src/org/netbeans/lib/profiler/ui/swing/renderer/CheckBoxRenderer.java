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

    @Override
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

    // --- Insets --------------------------------------------------------------
    
    private final Insets insets = new Insets(0, 0, 0, 0);

    @Override
    public Insets getInsets() {
        return insets;
    }

    @Override
    public Insets getInsets(Insets insets) {
        return this.insets;
    }

    // --- Other peformance tweaks ---------------------------------------------
    
    private Color foreground;
    private Color background;
    private boolean enabled = true;

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
    
}
