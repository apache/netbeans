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
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicPanelUI;

/**
 *
 * @author Tim Boudreau
 */
public class GradientBackgroundPanelUI extends BasicPanelUI implements PropertyChangeListener {

    static final Color GRADIENT_FIRST = Color.WHITE;
    static final Color GRADIENT_SECOND = new Color(212, 212, 253);

    static final Color GRADIENT_THIRD = new Color(255, 255, 255, 0);
    static final Color GRADIENT_FOURTH = new Color(255, 255, 200, 128);

    private int lastW = -1;
    private int lastH = -1;
    private Paint lastPaint;
    private Paint lastPaint2;

    public static ComponentUI createUI(JComponent c) {
        return new GradientBackgroundPanelUI();
    }

    @Override
    public void uninstallUI(JComponent c) {
        super.uninstallUI(c);
        c.addPropertyChangeListener("ancestor", this);
    }

    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        c.addPropertyChangeListener("ancestor", this);
    }

    private Paint gradient(int w, int h) {
        if (w <= 0 || h <= 0) {
            return Color.BLACK;
        }
        if (lastPaint == null || (w != lastW || h != lastH)) {
            lastPaint = new GradientPaint(0, 0, GRADIENT_FIRST,
                    w / 2, h / 2, GRADIENT_SECOND, true);
            lastW = w;
            lastH = h;
        }
        return lastPaint;
    }

    private Paint gradient2(int w, int h) {
        if (w <= 0 || h <= 0) {
            return Color.BLACK;
        }
        if (lastPaint2 == null || (w != lastW || h != lastH)) {
            lastPaint2 = new GradientPaint(0, h, GRADIENT_THIRD,
                    w / 2, 0, GRADIENT_FOURTH, true);
            lastW = w;
            lastH = h;
        }
        return lastPaint2;
    }

    @Override
    public void update(Graphics g, JComponent c) {
        if (c.isOpaque() && SwingUtilities.getAncestorOfClass(JPanel.class, c) == null) {
            int w = c.getWidth();
            int h = c.getHeight();
            ((Graphics2D) g).setPaint(gradient(w, h));
            g.fillRect(0, 0, c.getWidth(), c.getHeight());
            ((Graphics2D) g).setPaint(gradient2(w, h));
            g.fillRect(0, 0, c.getWidth(), c.getHeight());
            paint(g, c);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        JComponent c = (JComponent) evt.getSource();
        if (evt.getNewValue() != null) {
            c.setOpaque(SwingUtilities.getAncestorOfClass(JPanel.class, c) == null);
        }
    }

}
