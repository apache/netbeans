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

import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.JComponent;
import javax.swing.SwingConstants;

/**
 *
 * @author Jiri Sedlacek
 */
public class HideableBarRenderer extends MultiRenderer {
    
    private static final int BAR_MARGIN = 20;
    private static final int MIN_BAR_WIDTH = 20;
    private static final int MAX_BAR_WIDTH = 100;
    private static final int OPT_BAR_WIDTH = 50;
    
    private int maxRendererWidth;
    
    private final BarRenderer barRenderer;
    private final ProfilerRenderer mainRenderer;
    private final NumberPercentRenderer numberPercentRenderer;
    
    private final ProfilerRenderer[] valueRenderers;
    
    
    public HideableBarRenderer(ProfilerRenderer renderer) {
        this(renderer, renderer.getComponent().getPreferredSize().width);
    }
    
    public HideableBarRenderer(ProfilerRenderer renderer, int maxWidth) {
        maxRendererWidth = maxWidth;
        
        barRenderer = new BarRenderer();
        mainRenderer = renderer;
        numberPercentRenderer = renderer instanceof NumberPercentRenderer ?
                                (NumberPercentRenderer)renderer : null;
        
        valueRenderers = new ProfilerRenderer[] { barRenderer, mainRenderer };
        
        setOpaque(true);
        setHorizontalAlignment(SwingConstants.TRAILING);
    }
    
    
    public void setMaxValue(long maxValue) {
        int oldDigits = Long.toString(barRenderer.getMaxValue()).length();
        int newDigits = Long.toString(maxValue).length();
        
        barRenderer.setMaxValue(maxValue);
        if (numberPercentRenderer != null) numberPercentRenderer.setMaxValue(maxValue);
        
        if (oldDigits < newDigits) {
            // Number of the same pow10 created using only digit '9'
//            int ref = (int)Math.pow(10, Math.ceil(Math.log10(maxValue + 1))) - 1;
            mainRenderer.setValue((long)Math.pow(10, newDigits) - 1, -1);
            int mainWidth = mainRenderer.getComponent().getPreferredSize().width;
            maxRendererWidth = Math.max(maxRendererWidth, mainWidth);
        }
    }
    
    public void setValue(Object value, int row) {
        barRenderer.setValue(value, row);
        mainRenderer.setValue(value, row);
    }
    

    protected ProfilerRenderer[] valueRenderers() {
        return valueRenderers;
    }
    
    protected int renderersGap() {
        return BAR_MARGIN;
    }
    
    
    public Dimension getPreferredSize() {
        return mainRenderer.getComponent().getPreferredSize();
    }
    
    public int getOptimalWidth() {
        return maxRendererWidth + renderersGap() + OPT_BAR_WIDTH;
    }
    
    public int getMaxNoBarWidth() {
        return maxRendererWidth + renderersGap() + MIN_BAR_WIDTH - 1;
    }
    
    public int getNoBarWidth() {
        return maxRendererWidth;
    }
    
    public void paint(Graphics g) {
        g.setColor(getBackground());
        g.fillRect(location.x, location.y, size.width, size.height);
        
        JComponent component = mainRenderer.getComponent();
        int componentWidth = component.getPreferredSize().width;
        int componentX = size.width - componentWidth;
        
        mainRenderer.move(location.x + componentX, location.y);
        component.setSize(componentWidth, size.height);
        component.paint(g);
        
        if (numberPercentRenderer == null || numberPercentRenderer.valueRenderers()[1].getComponent().isVisible()) {
            int freeWidth = size.width - maxRendererWidth - renderersGap();
            if (freeWidth >= MIN_BAR_WIDTH) {
                barRenderer.setSize(Math.min(freeWidth, MAX_BAR_WIDTH), size.height);
                barRenderer.move(location.x, location.y);
                barRenderer.paint(g);
            }
        }
    }
    
    public String toString() {
        return mainRenderer.toString();
    }
    
}
