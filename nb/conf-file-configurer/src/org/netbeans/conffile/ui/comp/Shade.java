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

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentListener;
import java.awt.event.ContainerListener;
import java.awt.event.FocusListener;
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.RepaintManager;
import javax.swing.Timer;
import javax.swing.event.AncestorListener;

/**
 * A component that can replace the glass pane on a window and animates fading
 * the window contents.
 *
 * @author Tim Boudreau
 */
final class Shade extends JComponent implements ActionListener {

    private final Dimension size = new Dimension(100, 100);
    private final Timer timer = new Timer(5, this);
    private int ticks;
    private static final int MAX_TICKS = 280;
    private static final AffineTransform IDENTITY = new AffineTransform();
    BufferedImage backingBuffer;
    private static final Color SHADE_COLOR = new Color(130, 130, 155);

    Shade() {
        timer.setRepeats(true);
        timer.setCoalesce(false);
        setOpaque(false);
    }

    @Override
    public synchronized void addComponentListener(ComponentListener l) {
        // performance - do nothing
    }

    @Override
    public synchronized void addContainerListener(ContainerListener l) {
        // performance - do nothing
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        // performance - do nothing
    }

    @Override
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        // performance - do nothing
    }

    @Override
    public void addAncestorListener(AncestorListener listener) {
        // performance - do nothing
    }

    @Override
    public void addHierarchyBoundsListener(HierarchyBoundsListener l) {
        // performance - do nothing
    }

    @Override
    public synchronized void addFocusListener(FocusListener l) {
        // performance - do nothing
    }

    @Override
    public void addHierarchyListener(HierarchyListener l) {
        // performance - do nothing
    }

    @Override
    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        // performance - do nothing
    }

    @Override
    public void invalidate() {
        // performance - do nothing
    }

    @Override
    public void revalidate() {
        // performance - do nothing
    }

    @Override
    public void repaint() {
        // performance - do nothing
    }

    @Override
    public void addNotify() {
        ticks = 0;
        super.addNotify();
        setDoubleBuffered(false);
        timer.start();
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        if (timer.isRunning()) {
            timer.stop();
            setOpaque(false);
            backingBuffer = null;
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (ticks >= MAX_TICKS) {
            timer.stop();
        }
        RepaintManager mgr = RepaintManager.currentManager(this);
        mgr.markCompletelyDirty(this);
        mgr.paintDirtyRegions();
        Toolkit.getDefaultToolkit().sync();
    }

    @Override
    public void update(Graphics g) {
        paint(g);
    }

    private void createBackingImage() {
        Container top = getTopLevelAncestor();
        if (top != null) {
            Container content = top instanceof JFrame ? ((JFrame) top).getContentPane() : top instanceof JDialog ? ((JDialog) top).getContentPane() : null;
            if (content != null) {
                BufferedImage img = getGraphicsConfiguration().createCompatibleImage(content.getWidth(), content.getHeight());
                Graphics2D g = img.createGraphics();
                content.paint(g);
                g.dispose();
                backingBuffer = img;
                setOpaque(true);
            }
        }
    }

    float alpha() {
        float max = MAX_TICKS;
        float t = Math.min(ticks, max);
        float factor = (float) Math.sin(((t) / (max + 1F)) * 0.5F);
        return factor;
    }

    @Override
    public void paint(Graphics g) {
        if (ticks == 0 && timer.isRunning()) {
            // Using our own backingBuffer buffer is vastly better performing
            // than letting the components behind us paint
            createBackingImage();
        }
        ticks++;
        Graphics2D gg = (Graphics2D) g;
        Composite old = gg.getComposite();
        try {
            if (backingBuffer != null) {
                gg.drawRenderedImage(backingBuffer, IDENTITY);
            }
            gg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha()));
            gg.setColor(SHADE_COLOR);
            gg.fillRect(0, 0, getWidth(), getHeight());
        } finally {
            gg.setComposite(old);
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return size;
    }

    @Override
    public Dimension getMinimumSize() {
        return size;
    }

    @Override
    public Dimension getMaximumSize() {
        return size;
    }

    private static boolean parentNotWindowOrFrame(Component c) {
        return !(c.getParent() instanceof Window || c.getParent() instanceof Dialog || c.getParent() instanceof Frame);
    }

    @Override
    public void doLayout() {
        if (isDisplayable()) {
            Component target = this;
            while (parentNotWindowOrFrame(target)) {
                target = target.getParent();
            }
            if (target != null) {
                size.setSize(target.getSize());
            }
        }
    }

}
