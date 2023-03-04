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

package org.netbeans.modules.profiler.snaptracer.impl.probes;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.UIManager;
import org.netbeans.lib.profiler.charts.swing.Utils;
import org.netbeans.lib.profiler.ui.UIUtils;
import org.netbeans.modules.profiler.snaptracer.TracerProbe;
import org.netbeans.modules.profiler.snaptracer.TracerProbeDescriptor;

/**
 *
 * @author Jiri Sedlacek
 */
public final class ProbePresenter extends JLabel {

    private static final Color SELECTED_FILTER = new Color(0, 0, 200, 40);
    private static final float[] FRACTIONS = new float[] { 0.0f, 0.49f, 0.51f, 1.0f };
    private static final Color[] COLORS = !UIUtils.isDarkResultsBackground() ?
        new Color[] { new Color(250, 251, 252, 120),
                      new Color(237, 240, 242, 120),
                      new Color(229, 233, 236, 125),
                      new Color(215, 221, 226, 130) } : 
        new Color[] { new Color(050, 051, 052, 220),
                      new Color(037, 040, 042, 220),
                      new Color(29, 033, 036, 225),
                      new Color(015, 021, 026, 230) };
    
    private static final Color BACKGROUND = !UIUtils.isDarkResultsBackground() ?
            UIManager.getColor("Panel.background") : new Color(30, 30, 30); // NOI18N

    private LinearGradientPaint gradientPaint;

    private static final boolean GRADIENT = !Utils.forceSpeed();
    private boolean isSelected = false;

    public ProbePresenter(TracerProbe p, TracerProbeDescriptor d) {
        super(d.getProbeName(), d.getProbeIcon(), JLabel.LEADING);
        
        // --- ToolTips support
        // Let's store the tooltip in client property and resolve it from parent
        putClientProperty("ToolTipHelper", d.getProbeDescription()); // NOI18N
        // ---
        
        setIconTextGap(7);
        setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 1, Color.LIGHT_GRAY),
                    BorderFactory.createEmptyBorder(5, 10, 5, 10)
                ));
    }
    

    public void setSelected(boolean selected) {
        if (isSelected == selected) return;
        isSelected = selected;
        repaint();
    }
    
    public boolean isSelected() {
        return isSelected;
    }


    public void reshape(int x, int y, int w, int h) {
        if (GRADIENT) gradientPaint = new LinearGradientPaint(0, 0, 0, h - 1,
                                                              FRACTIONS, COLORS);
        super.reshape(x, y, w, h);
    }


    protected void paintComponent(Graphics g) {
        int y = getHeight() - 1;

        ((Graphics2D)g).setPaint(GRADIENT ? gradientPaint : BACKGROUND);
        g.fillRect(0, 0, getWidth(), y);
        
        if (isSelected) {
            g.setColor(SELECTED_FILTER);
            g.fillRect(0, 0, getWidth(), y);
        }

        super.paintComponent(g);
    }

}
