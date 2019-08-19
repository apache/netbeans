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

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import javax.swing.BoundedRangeModel;
import javax.swing.JSlider;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicSliderUI;

/**
 *
 * @author Tim Boudreau
 */
public class AASlider extends JSlider {

    @Override
    public void updateUI() {
        setUI(new AASliderUI(this));
    }

    @Override
    public void paint(Graphics g) {
        UIUtils.withAntialiasing(g, super::paint);
    }

    @Override
    public Point getToolTipLocation(MouseEvent event) {
        return new Point(0, getHeight());
    }

    static class AASliderUI extends BasicSliderUI implements MouseMotionListener, MouseWheelListener {

        public AASliderUI(JSlider b) {
            super(b);
        }

        @Override
        protected Color getShadowColor() {
            return UIManager.getColor("controlShadow");
        }

        @Override
        protected Color getHighlightColor() {
            return getShadowColor();
        }

        @Override
        protected void installDefaults(JSlider slider) {
            super.installDefaults(slider); //To change body of generated methods, choose Tools | Templates.
            slider.putClientProperty("Slider.paintThumbArrowShape", true);
            slider.setOpaque(false);
        }

        @Override
        protected void uninstallListeners(JSlider slider) {
            super.uninstallListeners(slider); //To change body of generated methods, choose Tools | Templates.
            slider.removeMouseMotionListener(this);
            slider.removeMouseWheelListener(this);
        }

        @Override
        protected void installListeners(JSlider slider) {
            super.installListeners(slider); //To change body of generated methods, choose Tools | Templates.
            slider.addMouseMotionListener(this);
            slider.addMouseWheelListener(this);
        }

        @Override
        public void mouseDragged(MouseEvent e) {
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            setArmed(thumbRect.contains(e.getPoint()));
        }

        private boolean armed;

        private void setArmed(boolean val) {
            if (armed != val) {
                armed = val;
                if (val) {
                    slider.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                } else {
                    slider.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }
            }
        }

        @Override
        protected Dimension getThumbSize() {
            return new Dimension(21, 21);
        }

        @Override
        protected TrackListener createTrackListener(JSlider slider) {
            return new TL();
        }

        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            int units = e.getUnitsToScroll() > 0 ? 1 : -1;
            BoundedRangeModel model = slider.getModel();
            int value = slider.getModel().getValue() + units;
            if (value < model.getMaximum() && value >= model.getMinimum()) {
                e.consume();
                model.setValue(value);
            }
        }

        final class TL extends TrackListener {

            @Override
            public void mouseDragged(MouseEvent e) {
                Rectangle old = new Rectangle(trackRect);
                super.mouseDragged(e);
                slider.repaint(old.x, old.y - 1, old.width, old.height + 2);
                e.consume();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                Rectangle old = new Rectangle(trackRect);
                super.mousePressed(e);
                slider.repaint(old.x, old.y - 1, old.width, old.height + 2);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                Rectangle old = new Rectangle(trackRect);
                super.mouseReleased(e);
                slider.repaint(old.x, old.y = 1, old.width, old.height + 2);
            }
        }

        @Override
        public void paintTrack(Graphics g) {
            Rectangle trackBounds = trackRect;
            Color c = getShadowColor();
            g.setColor(c);
            if (!slider.hasFocus()) {
                if (slider.getOrientation() == JSlider.HORIZONTAL) {
                    int cy = (trackBounds.height / 2) - 4;
                    int cw = trackBounds.width;
                    g.translate(trackBounds.x, trackBounds.y + cy);
                    g.drawLine(0, 0, cw, 0);
                    g.setColor(new Color(255, 255, 255, 180));
                    g.drawLine(1, 1, cw, 1);

                    g.translate(-trackBounds.x, -(trackBounds.y + cy));
                } else {
                    int cx = (trackBounds.width / 2) - 5;
                    int ch = trackBounds.height;

                    g.translate(trackBounds.x + cx, trackBounds.y);
                    g.drawLine(0, 0, 0, ch);
                    g.translate(-(trackBounds.x + cx), -trackBounds.y);
                }
                return;
            }

            if (slider.getOrientation() == JSlider.HORIZONTAL) {
                int cy = (trackBounds.height / 2) - 5;
                int cw = trackBounds.width;

                g.translate(trackBounds.x, trackBounds.y + cy);

                g.drawLine(0, 0, cw - 1, 0);
                g.drawLine(0, 1, 0, 2);
                g.drawLine(0, 3, cw, 3);
                g.drawLine(cw, 0, cw, 3);
                g.drawLine(1, 1, cw - 2, 1);

                g.translate(-trackBounds.x, -(trackBounds.y + cy));
            } else {
                int cx = (trackBounds.width / 2) - 5;
                int ch = trackBounds.height;

                g.translate(trackBounds.x + cx, trackBounds.y);

                g.drawLine(0, 0, 0, ch - 1);
                g.drawLine(1, 0, 2, 0);
                g.drawLine(3, 0, 3, ch);
                g.drawLine(0, ch, 3, ch);
                g.drawLine(1, 1, 1, ch - 2);
                g.translate(-(trackBounds.x + cx), -trackBounds.y);
            }
        }

        @Override
        public void paintThumb(Graphics g) {
            Rectangle knobBounds = thumbRect;
            int w = knobBounds.width;
            int h = knobBounds.height;
            if (w % 2 == 0) {
                w++;
            }
            if (h % 2 == 0) {
                h++;
            }

            g.translate(knobBounds.x, knobBounds.y - 1);

            if (slider.isEnabled()) {
                if (slider.hasFocus()) {
                    g.setColor(getShadowColor());
                } else {
                    g.setColor(UIManager.getColor("textText").brighter());
                }
            } else {
                g.setColor(Color.GRAY);
            }

            if (slider.getOrientation() == JSlider.HORIZONTAL) {
                int x = w / 2;
                int y = 0;
                if (!slider.hasFocus()) {
                    y += 1;
                    h -= 2;
                }
                int[] xs = new int[]{0, x, w};
                int[] ys = new int[]{y + (h / 2), y + h, y + (h / 2)};
                Color old = g.getColor();
                Polygon poly = new Polygon(xs, ys, 3);
                g.drawLine(x, y, x, y + h);
                g.drawLine(x + 1, y, x + 1, y + h);
                if (slider.hasFocus()) {
                    g.setColor(Color.ORANGE);
                }
                g.fillPolygon(poly);
                g.setColor(old);
                g.drawPolygon(poly);
            } else {
                int x = 0;
                int y = h / 2;
                if (!slider.hasFocus()) {
                    x += 1;
                    w -= 2;
                }
                g.drawLine(x, y, x + w, y);
                g.drawLine(x, y + 1, x + w, y + 1);
            }
        }

        @Override
        public void paintFocus(Graphics g) {
//            super.paintFocus(g); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        protected Color getFocusColor() {
            return getShadowColor();
        }
    }
}
