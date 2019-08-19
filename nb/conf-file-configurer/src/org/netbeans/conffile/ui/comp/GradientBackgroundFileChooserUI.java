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

import static org.netbeans.conffile.ui.comp.GradientBackgroundPanelUI.GRADIENT_FIRST;
import static org.netbeans.conffile.ui.comp.GradientBackgroundPanelUI.GRADIENT_SECOND;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.plaf.metal.MetalFileChooserUI;

/**
 * A file chooser with antialiasing and a pretty background.
 *
 * @author Tim Boudreau
 */
public class GradientBackgroundFileChooserUI extends MetalFileChooserUI {

    private int lastW = -1;
    private int lastH = -1;
    private Paint lastPaint;

    public GradientBackgroundFileChooserUI(JFileChooser filechooser) {
        super(filechooser);
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

    public void update(Graphics g, JComponent c) {
        int w = c.getWidth();
        int h = c.getHeight();
        ((Graphics2D) g).setPaint(gradient(w, h));
        g.fillRect(0, 0, c.getWidth(), c.getHeight());
        paint(g, c);
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        UIUtils.withTextAntialiasing(g, (gg) -> {
            super.paint(gg, c);
        });
    }

}
