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
import java.awt.Graphics;
import java.awt.Rectangle;
import org.netbeans.lib.profiler.ui.UIUtils;
import org.netbeans.lib.profiler.ui.swing.ProfilerTable;

/**
 *
 * @author Jiri Sedlacek
 */
public class BarRenderer extends BaseRenderer implements RelativeRenderer {
    
    private static final Color COLOR_POS = new Color(225, 130, 130);
    private static final Color COLOR_NEG = new Color(130, 225, 130);
    
    private static final int X_MARGIN = 2;
    private static final int Y_MARGIN = 3;
    
    private static final Rectangle BAR_RECT = new Rectangle();
    
    private long maxValue;
    private float value;
    
    protected boolean renderingDiff;
    
    
    public BarRenderer() {
        maxValue = 100;
        value = 0;
        
        setOpaque(true);
        putClientProperty(ProfilerTable.PROP_NO_HOVER, this);
    }
    
    
    public void setMaxValue(long maxValue) {
        this.maxValue = maxValue;
    }
    
    public long getMaxValue() {
        return maxValue;
    }
    
    public void setValue(Object value, int row) {
        if (value == null) this.value = 0;
        else this.value = maxValue == 0 ? 0 : ((Number)value).floatValue() / maxValue;
    }
    
    
    public void setDiffMode(boolean diffMode) {
        renderingDiff = diffMode;
    }

    public boolean isDiffMode() {
        return renderingDiff;
    }
    
    
    public void paint(Graphics g) {
        super.paint(g);
        
        BAR_RECT.x = location.x + X_MARGIN;
        BAR_RECT.y = location.y + Y_MARGIN;
        BAR_RECT.height = size.height - Y_MARGIN * 2;
        
        int width = size.width - X_MARGIN * 2;
        
        if (renderingDiff) {
            Color color = value < 0 ? COLOR_NEG : COLOR_POS;
            int width2 = width / 2;
            
            if (value <= -1) {
                g.setColor(color);
                g.fillRect(BAR_RECT.x, BAR_RECT.y, width2, BAR_RECT.height);
                
                g.setColor(alternate(color));
                g.fillRect(BAR_RECT.x + width2, BAR_RECT.y, width - width2, BAR_RECT.height);
            } else if (value >= 1) {
                g.setColor(alternate(color));
                g.fillRect(BAR_RECT.x, BAR_RECT.y, width2, BAR_RECT.height);
                
                g.setColor(color);
                g.fillRect(BAR_RECT.x + width2, BAR_RECT.y, width - width2, BAR_RECT.height);
            } else {
                g.setColor(alternate(color));
                g.fillRect(BAR_RECT.x, BAR_RECT.y, width, BAR_RECT.height);

                BAR_RECT.width = (int)(width2 * Math.min(Math.abs(value), 1));
                if (BAR_RECT.width > 0) {
                    g.setColor(color);
                    if (value < 0) {
                        g.fillRect(BAR_RECT.x + width2 - BAR_RECT.width, BAR_RECT.y, BAR_RECT.width, BAR_RECT.height);
                    } else {
                        g.fillRect(BAR_RECT.x + width2, BAR_RECT.y, BAR_RECT.width, BAR_RECT.height);
                    }
                }
            }
        } else {
            BAR_RECT.width = (int)(width * Math.min(value, 1));
            if (BAR_RECT.width > 0) {
                g.setColor(COLOR_POS);
                g.fillRect(BAR_RECT.x, BAR_RECT.y, BAR_RECT.width, BAR_RECT.height);
            }

            if (BAR_RECT.width < width) {
                BAR_RECT.x += BAR_RECT.width;
                BAR_RECT.width = width - BAR_RECT.width;
                g.setColor(alternate(COLOR_POS));
                g.fillRect(BAR_RECT.x, BAR_RECT.y, BAR_RECT.width, BAR_RECT.height);
            }
        }
    }
    
    private static final double FACTOR = 0.55d;
//    private static final double FACTOR = 0.20d;
    
    private static Color alternate(Color c) {
        return !UIUtils.isDarkResultsBackground() ? brighter(c) : darker(c);
    }
    
    private static Color brighter(Color c) {
        int r = c.getRed();
        int g = c.getGreen();
        int b = c.getBlue();

        int i = (int)(1.0/(1.0-FACTOR));
        if ( r > 0 && r < i ) r = i;
        if ( g > 0 && g < i ) g = i;
        if ( b > 0 && b < i ) b = i;

        return new Color(Math.min((int)(r/FACTOR), 255),
                         Math.min((int)(g/FACTOR), 255),
                         Math.min((int)(b/FACTOR), 255));
    }
    
    private static Color darker(Color c) {
        return new Color(Math.max((int)(c.getRed()  *FACTOR), 0),
                         Math.max((int)(c.getGreen()*FACTOR), 0),
                         Math.max((int)(c.getBlue() *FACTOR), 0));
    }
    
}
