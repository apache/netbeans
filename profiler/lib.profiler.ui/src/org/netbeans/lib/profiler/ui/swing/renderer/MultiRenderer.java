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
import java.awt.Graphics;
import javax.swing.JComponent;
import javax.swing.SwingConstants;

/**
 *
 * @author Jiri Sedlacek
 */
public abstract class MultiRenderer extends BaseRenderer implements RelativeRenderer {
    
    private Dimension preferredSize;
    
    
    protected int renderersGap() { return 0; }
    
    protected abstract ProfilerRenderer[] valueRenderers();
    
    
    public void setDiffMode(boolean diffMode) {
        ProfilerRenderer[] valueRenderers = valueRenderers();
        if (valueRenderers == null) return;
        
        for (ProfilerRenderer renderer : valueRenderers)
            if (renderer instanceof RelativeRenderer)
                ((RelativeRenderer)renderer).setDiffMode(diffMode);
    }

    public boolean isDiffMode() {
        ProfilerRenderer[] valueRenderers = valueRenderers();
        if (valueRenderers == null) return false;
        
        for (ProfilerRenderer renderer : valueRenderers)
            if (renderer instanceof RelativeRenderer)
                return ((RelativeRenderer)renderer).isDiffMode();
        
        return false;
    }
    
    
    public void setOpaque(boolean isOpaque) {
        super.setOpaque(isOpaque);
        
        ProfilerRenderer[] valueRenderers = valueRenderers();
        if (valueRenderers == null) return;
        
        for (ProfilerRenderer renderer : valueRenderers)
            renderer.getComponent().setOpaque(isOpaque);
    }
    
    public void setForeground(Color foreground) {
        super.setForeground(foreground);
        
        ProfilerRenderer[] valueRenderers = valueRenderers();
        if (valueRenderers == null) return;
        
        for (ProfilerRenderer renderer : valueRenderers)
            renderer.getComponent().setForeground(foreground);
    }
    
    public void setBackground(Color background) {
        super.setBackground(background);
        
        ProfilerRenderer[] valueRenderers = valueRenderers();
        if (valueRenderers == null) return;
        
        for (ProfilerRenderer renderer : valueRenderers)
            renderer.getComponent().setBackground(background);
    }
    
    public Dimension getPreferredSize() {
        if (preferredSize == null) preferredSize = new Dimension();
        else preferredSize.setSize(0, 0);
        
        ProfilerRenderer[] valueRenderers = valueRenderers();
        if (valueRenderers != null) {
            int visible = 0;
            for (ProfilerRenderer renderer : valueRenderers) {
                JComponent component = renderer.getComponent();
                if (component.isVisible()) {
                    Dimension rendererSize = component.getPreferredSize();
                    preferredSize.width += rendererSize.width;
                    preferredSize.height = Math.max(preferredSize.height, rendererSize.height);
                    visible++;
                }
            }
            preferredSize.width += renderersGap() * (visible - 1);
        }
        
        return sharedDimension(preferredSize);
    }
    
    
    public void paint(Graphics g) {
        super.paint(g);
        
        int alignment = getHorizontalAlignment();
        int renderersGap = renderersGap();
        
        if (alignment == SwingConstants.LEADING || alignment == SwingConstants.LEFT) {
            
            int xx = location.x;
            
            for (ProfilerRenderer renderer : valueRenderers()) {
                JComponent component = renderer.getComponent();
                if (component.isVisible()) {
                    int componentWidth = component.getPreferredSize().width;
                    component.setSize(componentWidth, size.height);
                    renderer.move(xx, location.y);
                    component.paint(g);
                    xx += componentWidth + renderersGap;
                }
            }
            
        } else {
            
            int xx = location.x + size.width;
            
            ProfilerRenderer[] valueRenderers = valueRenderers();
            for (int i = valueRenderers.length - 1; i >= 0; i--) {
                ProfilerRenderer renderer = valueRenderers[i];
                JComponent component = renderer.getComponent();
                if (component.isVisible()) {
                    int componentWidth = component.getPreferredSize().width;
                    component.setSize(componentWidth, size.height);
                    xx -= componentWidth;
                    renderer.move(xx, location.y);
                    component.paint(g);
                    xx -= renderersGap;
                }
            }
            
        }
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        ProfilerRenderer[] renderers = valueRenderers();
        if (renderers != null) for (ProfilerRenderer renderer : renderers) sb.append(renderer.toString());
        return sb.toString();
    }
    
}
