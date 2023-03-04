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


package org.netbeans.modules.form.fakepeer;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.peer.ComponentPeer;


/**
 *
 * @author Tran Duc Trung
 * @author Jan Stola
 */

abstract class FakeComponentPeer
{
    Component _delegate;
    Component _target;

    FakeComponentPeer(Component target) {
        _target = target;
        _delegate = createDelegate();
        initDelegate();
    }

    void initDelegate() {
        Rectangle r = _target.getBounds();

        setBounds(r.x, r.y, r.width, r.height);
        setVisible(_target.isVisible());
        setCursor(_target.getCursor());
        setEnabled(_target.isEnabled());

        // how to recognize that the color was set to target explicitly?
        Container parent = _target.getParent();
        Color color = _target.getBackground();
        if (color != null && (parent == null || parent.getBackground() != color))
            _delegate.setBackground(color);
        else
            _target.setBackground(_delegate.getBackground());

        color = _target.getForeground();
        if (color != null && (parent == null || parent.getForeground() != color))
            _delegate.setForeground(color);
        else
            _target.setForeground(_delegate.getForeground());

        Font font = _target.getFont();
        if (font == null || (parent != null && parent.getFont() == font))
            font = FakePeerSupport.getDefaultAWTFont();
        _delegate.setFont(font);
            
        _delegate.setName(_target.getName());
        //    _delegate.setLocale(_target.getLocale());
        _delegate.setDropTarget(_target.getDropTarget());
        _delegate.setComponentOrientation(_target.getComponentOrientation());

        repaint();
    }

    abstract Component createDelegate();

    // ---------------

    // JDK 1.4
    public boolean isObscured() {
        return false;
    }

    // JDK 1.4
    public boolean canDetermineObscurity() {
        return false;
    }

    public void setVisible(boolean visible) {
        _delegate.setVisible(visible);
    }

    public void setEnabled(boolean enabled) {
        _delegate.setEnabled(enabled);
    }

    public void paint(Graphics g) {
        Font oldFont = g.getFont();
        Color oldColor = g.getColor();
        try {
            _delegate.paint(g);
            _target.paint(g);
        }
        finally {
            g.setColor(oldColor);
            g.setFont(oldFont);
        }
    }

    public void repaint(long tm, int x, int y, int w, int h) {
        _delegate.repaint(tm, x, y, w, h);
    }

    public void print(Graphics g1) {
    }

    public void setBounds(int x, int y, int width, int height) {
        _delegate.setBounds(x, y, width, height);
    }

    // JDK 1.5
    public void setBounds(int x, int y, int width, int height, int op) {
        _delegate.setBounds(x, y, width, height);
    }

    // JDK 1.5
    public Rectangle getBounds() {
        return _delegate.getBounds();
    }

    public void handleEvent(AWTEvent e) {
    }

    // JDK 1.3
    public void coalescePaintEvent(PaintEvent e) {
    }

    public Point getLocationOnScreen() {
        // this is called from target
        return null;
    }

    public Dimension getPreferredSize() {
        return getMinimumSize();
    }

    public Dimension getMinimumSize() {
        return _delegate.getMinimumSize();
    }

    public ColorModel getColorModel() {
        return _delegate.getColorModel();
    }

    public Toolkit getToolkit() {
        return _delegate.getToolkit();
    }

    public Graphics getGraphics() {
        Component parent = _target.getParent();
        if (parent != null) {
            Graphics g = parent.getGraphics();
            if (g != null) {
                Rectangle bounds = _target.getBounds();
                g.translate(bounds.x, bounds.y);
                g.setClip(0, 0, bounds.width, bounds.height);
            }
            return g;
        }
        return null;
    }

    public FontMetrics getFontMetrics(Font font) {
        // this is called from target
        return null;
    }

    public void dispose() {
        _target = null;
        _delegate = null;
    }

    public void setForeground(Color color) {
        _delegate.setForeground(color);
    }

    public void setBackground(Color color) {
        _delegate.setBackground(color);
    }

    public void setFont(Font font) {
        _delegate.setFont(font);
    }

    // not in JDK 1.4
    public void setCursor(Cursor cursor) {
        _delegate.setCursor(cursor);
    }

    // JDK 1.4
    public void updateCursorImmediately() {
    }

    // not in JDK 1.4
    public void requestFocus() {
        // this is called from target
    }

    // JDK 1.4
    public boolean requestFocus(Component lightweightChild,
                                boolean temporary,
                                boolean focusedWindowChangeAllowed,
                                long time)
    {
        return false;
    }
    
    // not in JDK 1.4
    public boolean isFocusTraversable() {
        return false;
    }

    // JDK 1.4
    public boolean isFocusable() {
        return false;
    }

    public Image createImage(ImageProducer producer) {
        return getToolkit().createImage(producer);
    }

    public Image createImage(int width, int height) {
        return new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    }

    // JDK 1.4 (VolatileImage not before 1.4)
    public VolatileImage createVolatileImage(int width, int height) {
        GraphicsConfiguration gc = getGraphicsConfiguration();
        return gc != null ? gc.createCompatibleVolatileImage(width, height) : null;
    }

    public boolean prepareImage(Image img, int w, int h,
                                ImageObserver imageObserver)
    {
        return _delegate.prepareImage(img, w, h, imageObserver);
    }

    public int checkImage(Image img, int w, int h,
                          ImageObserver imageObserver)
    {
        return _delegate.checkImage(img, w, h, imageObserver);
    }

    // JDK 1.3
    public GraphicsConfiguration getGraphicsConfiguration() {
        return _target.getGraphicsConfiguration();
    }

    // JDK 1.4
    public boolean handlesWheelScrolling() {
        return false;
    }

    // JDK 1.4 (BufferCapabilities not before 1.4)
    public void createBuffers(int numBuffers, BufferCapabilities caps)
        throws AWTException
    {
    }

    // JDK 1.4
    public Image getBackBuffer() {
        return null;
    }

    // JDK 1.4 (BufferCapabilities not before 1.4)
    public void flip(BufferCapabilities.FlipContents flipAction) {
    }

    // JDK 1.4
    public void destroyBuffers() {
    }

    // JDK 1.5
    public void reparent(java.awt.peer.ContainerPeer newContainer) {
    }

    // JDK 1.5
    public boolean isReparentSupported() {
        return false;
    }

    // JDK 1.5
    public void layout() {
    }

    // JDK 1.7
    public boolean updateGraphicsData(GraphicsConfiguration gc) {
        return false;
    }

    // JDK 1.7
    public void setZOrder(ComponentPeer above) {
    }

    // deprecated
    public Dimension preferredSize() {
        return getPreferredSize();
    }

    // deprecated
    public Dimension minimumSize() {
        return getMinimumSize();
    }

    // deprecated
    public void show() {
        setVisible(true);
    }

    // deprecated
    public void hide() {
        setVisible(false);
    }

    // deprecated
    public void enable() {
        setEnabled(true);
    }

    // deprecated
    public void disable() {
        setEnabled(false);
    }

    // deprecated
    public void reshape(int x, int y, int width, int height) {
        setBounds(x, y, width, height);
    }

    //
    // helpers
    //

    void clearRectBeforePaint(Graphics g, Rectangle r) {
        g.clearRect(r.x, r.y, r.width, r.height);
    }

    void repaint() {
        Dimension sz = _target.getSize();
        repaint(0, 0, 0, sz.width, sz.height);
    }

    //
    //
    //

    protected class Delegate extends Component
    {
        @Override
        public void paint(Graphics g) {
            Dimension sz = _target.getSize();

            Color c = _target.getBackground();
            if (c == null)
                c = SystemColor.window;
            g.setColor(c);
            FakePeerUtils.drawLoweredBox(g,0,0,sz.width,sz.height);

            // by default display the class name
            Font origFont = g.getFont(); 
            g.setFont(origFont.deriveFont(Font.BOLD, origFont.getSize() + 1)); 

            String className = _target.getClass().getName();
            className = className.substring(className.lastIndexOf('.') + 1);

            FontMetrics fm = g.getFontMetrics();
            int w = fm.stringWidth(className);
            int h = fm.getHeight() - fm.getDescent();

            int x = (sz.width - w) / 2;

            g.setColor(SystemColor.text);
            g.drawString(className, x,(sz.height - h) / 2 + h - 1);
        }

        @Override
        public Dimension getMinimumSize() {
            String className = _target.getClass().getName();
            className = className.substring(className.lastIndexOf('.') + 1);

            FontMetrics fm = this.getFontMetrics(
                new Font("Dialog", Font.BOLD, 12)); // NOI18N
            int w = fm.stringWidth(className);
            int h = fm.getHeight();

            return new Dimension(w + 10, h + 4);
        }
    }
}
