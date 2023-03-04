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
import java.text.Format;
import javax.swing.SwingConstants;
import org.netbeans.lib.profiler.ui.UIUtils;

/**
 *
 * @author Jiri Sedlacek
 */
public class NumberPercentRenderer extends MultiRenderer {
    
    private final ProfilerRenderer valueRenderer;
    private final PercentRenderer percentRenderer;
    
    private final ProfilerRenderer[] renderers;
    
    private Dimension percentSize;
    
    
    public NumberPercentRenderer() {
        this((Format)null);
    }
    
    public NumberPercentRenderer(Format customFormat) {
        this(createNumberRenderer(customFormat));
    }
    
    public NumberPercentRenderer(ProfilerRenderer renderer) {
        valueRenderer = renderer;
        
        percentRenderer = new PercentRenderer() {
            public void setForeground(Color foreground) {
                if (foreground == null) foreground = Color.BLACK;
                super.setForeground(UIUtils.getDisabledForeground(foreground));
            }
            public Dimension getPreferredSize() {
                if (percentSize == null) percentSize = super.getPreferredSize();
                return percentSize;
            }
        };
        percentRenderer.changeFontSize(-1);
        percentRenderer.setMargin(3, 0, 3, 3);
        percentRenderer.setHorizontalAlignment(SwingConstants.TRAILING);
        
        percentRenderer.setMaxValue(100);
        percentRenderer.setValue(9999, -1);
        int fixedWidth = percentRenderer.getPreferredSize().width;
        percentSize.width = fixedWidth;
        
        renderers = new ProfilerRenderer[] { valueRenderer, percentRenderer };
        
        setOpaque(true);
        setHorizontalAlignment(SwingConstants.TRAILING);
    }
    
    
    protected ProfilerRenderer[] valueRenderers() {
        return renderers;
    }
    
    
    public void setMaxValue(long maxValue) {
        percentRenderer.setMaxValue(maxValue);
    }
    
    public void setValue(Object value, int row) {
        valueRenderer.setValue(value, row);
        percentRenderer.setValue(value, row);
    }
    
    
    public void setDiffMode(boolean diffMode) {
        percentRenderer.setVisible(!diffMode);
        super.setDiffMode(diffMode);
    }
    
    
    public Dimension getPreferredSize() {
        Dimension dim = valueRenderer.getComponent().getPreferredSize();
        if (percentRenderer.isVisible()) dim.width += percentRenderer.getPreferredSize().width;
        return sharedDimension(dim);
    }
    
    
    private static ProfilerRenderer createNumberRenderer(Format customFormat) {
        NumberRenderer numberRenderer = new NumberRenderer(customFormat);
        numberRenderer.setMargin(3, 3, 3, 3);
        return numberRenderer;
    }
    
    
    public String toString() {
        if (!percentRenderer.isVisible()) return valueRenderer.toString();
        else return valueRenderer.toString() + " " + percentRenderer.toString(); // NOI18N
    }
    
}
