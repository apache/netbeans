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
package org.netbeans.modules.team.commons.treelist;

import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import javax.swing.Icon;
import javax.swing.Timer;
import org.netbeans.modules.team.ide.spi.IDEServices;
import static org.netbeans.modules.team.ide.spi.IDEServices.BusyIcon;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;

/**
 *
 * @author jpeska
 */
public final class ProgressLabel extends TreeLabel {

    private Timer t;
    private BusyIcon busyIcon;
    private Reference<TreeListNode> refNode = new WeakReference<TreeListNode>(null);
    private Reference<Component> refComp = new WeakReference<Component>(null);

    public ProgressLabel(String text, Component comp) {
        super(text);
        refComp = new WeakReference<Component>(comp);
        setupProgress();
    }

    public ProgressLabel(String text, TreeListNode nd) {
        super(text);
        refNode = new WeakReference<TreeListNode>(nd);
        setupProgress();
    }

    private void setupProgress() {
        setIcon(createProgressIcon());
        t = new Timer(100, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Component comp = refComp.get();
                TreeListNode nd = refNode.get();
                if (nd == null && comp == null) {
                    t.stop();
                    Container p = getParent();
                    if (p != null) {
                        p.remove(ProgressLabel.this);
                    }
                } else {
                    busyIcon.tick();
                    ProgressLabel.this.repaint();
                    if (nd != null) {
                        nd.fireContentChanged();
                    } else {
                        comp.repaint();
                    }
                }
            }
        });
        t.setRepeats(true);
        super.setVisible(false);
    }

    private Icon createProgressIcon() {
        busyIcon = Lookup.getDefault().lookup(IDEServices.class).createBusyIcon();
        if (busyIcon == null) { // fallback
            busyIcon = new RotatingImageBusyIcon(ImageUtilities.loadImage("org/netbeans/swing/tabcontrol/resources/busy_icon.png")); // NOI18N
        }
        return busyIcon;
    }

    @Override
    public void setVisible(boolean visible) {
        boolean old = isVisible();
        super.setVisible(visible);
        if (old != visible) {
            if (visible) {
                t.start();
            } else {
                t.stop();
            }
        }
    }

    /**
     * Stop the timer. Make sure to call this method if you do not explicitly call setVisible(false) on this label. Otherwise, its timer will keep running and it will be referenced forever.
     */
    public void stop() {
        t.stop();
    }

    //The usual cell-renderer performance overrides
    @Override
    public void repaint() {
        //do nothing
    }

    @Override
    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        //do nothing
    }

    @Override
    public void validate() {
        //do nothing
    }

    @Override
    public void invalidate() {
        //do nothing
    }

    private static class RotatingImageBusyIcon implements BusyIcon {

        private final Image img;
        private final int width;
        private final int height;
        private int state = 0;
        private AffineTransform at;
        private static final int STEP = 15;

        public RotatingImageBusyIcon(Image img) {
            this.img = img;
            this.width = img.getWidth(null);
            this.height = img.getHeight(null);
        }

        @Override
        public void tick() {
            state += STEP;
            if (state >= 360) {
                state = 0;
            }
            at = new AffineTransform();
            at.rotate(state * Math.PI / 180.0, width/2, height/2);
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            if( g instanceof Graphics2D ) {
                Graphics2D g2d = (Graphics2D) g;
                //turn on high quality bitmap rendering
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
                g2d.translate(x, y);
                g2d.drawImage(img, at, null);
                g2d.translate(-x, -y);
            }
        }

        @Override
        public final int getIconWidth() {
            return width;
        }

        @Override
        public final int getIconHeight() {
            return height;
        }
    }
}
