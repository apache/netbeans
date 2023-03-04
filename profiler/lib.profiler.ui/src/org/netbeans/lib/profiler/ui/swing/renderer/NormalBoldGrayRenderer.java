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
import java.awt.Font;
import java.util.Objects;
import javax.swing.Icon;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import org.netbeans.lib.profiler.ui.UIUtils;

/**
 *
 * @author Jiri Sedlacek
 */
public class NormalBoldGrayRenderer extends MultiRenderer {
    
    private static final Color REPLACEABLE_FOREGROUND = new JTable().getForeground();
    
    private final LabelRenderer normalRenderer;
    private final LabelRenderer boldRenderer;
    private final LabelRenderer grayRenderer;
    
    private final ProfilerRenderer[] renderers;
    
    private Color customForeground;
    private Color replaceableForeground = REPLACEABLE_FOREGROUND;
    
    
    public NormalBoldGrayRenderer() {
        normalRenderer = new LabelRenderer(true) {
            public void setForeground(Color foreground) {
                if (customForeground != null && Objects.equals(foreground, replaceableForeground)) foreground = customForeground;
                super.setForeground(foreground);
            }
        };
        normalRenderer.setMargin(3, 3, 3, 0);
        
        boldRenderer = new LabelRenderer(true) {
            public void setForeground(Color foreground) {
                if (customForeground != null && Objects.equals(foreground, replaceableForeground)) foreground = customForeground;
                super.setForeground(foreground);
            }
        };
        boldRenderer.setMargin(3, 0, 3, 0);
        Font font = boldRenderer.getFont();
        boldRenderer.setFont(font.deriveFont(Font.BOLD));
        
        grayRenderer = new LabelRenderer(true) {
            public void setForeground(Color foreground) {
                if (Objects.equals(foreground, replaceableForeground)) {
                    if (customForeground != null) super.setForeground(customForeground);
                    else super.setForeground(UIUtils.getDisabledForeground(foreground == null ? Color.BLACK : foreground));
                } else {
                    super.setForeground(foreground);
                }
            }
        };
        grayRenderer.setMargin(3, 0, 3, 3);
        
        renderers = new ProfilerRenderer[] { normalRenderer, boldRenderer, grayRenderer };
        
        setOpaque(true);
        setHorizontalAlignment(SwingConstants.LEADING);
    }
    
    
    protected void setCustomForeground(Color foreground) {
        customForeground = foreground;
    }
    
    public void setReplaceableForeground(Color foreground) {
        replaceableForeground = foreground;
    }

    
    protected ProfilerRenderer[] valueRenderers() {
        return renderers;
    }
    
    
    protected void setNormalValue(String value) {
        normalRenderer.setText(value);
    }
    
    protected final String getNormalValue() {
        return normalRenderer.getText();
    }
    
    protected void setBoldValue(String value) {
        boldRenderer.setText(value);
    }
    
    protected final String getBoldValue() {
        return boldRenderer.getText();
    }
    
    protected void setGrayValue(String value) {
        grayRenderer.setText(value);
    }
    
    protected final String getGrayValue() {
        return grayRenderer.getText();
    }
    
    // Invoke after values are set!
    protected void setIcon(Icon icon) {
        String text = normalRenderer.getText();
        if (text == null || text.isEmpty()) {
            normalRenderer.setIcon(null);
            text = boldRenderer.getText();
            if (text == null || text.isEmpty()) {
                boldRenderer.setIcon(null);
                grayRenderer.setIcon(icon);
            } else {
                boldRenderer.setIcon(icon);
                grayRenderer.setIcon(null);
            }
        } else {
            normalRenderer.setIcon(icon);
            boldRenderer.setIcon(null);
            grayRenderer.setIcon(null);
        }
    }
    
    public Icon getIcon() {
        Icon icon = normalRenderer.getIcon();
        if (icon == null) icon = boldRenderer.getIcon();
        if (icon == null) icon = grayRenderer.getIcon();
        return icon;
    }
    
    // Invoke after values are set!
    protected void setIconTextGap(int gap) {
        String text = normalRenderer.getText();
        if (text == null || text.isEmpty()) {
//            normalRenderer.setIcon(null);
            text = boldRenderer.getText();
            if (text == null || text.isEmpty()) {
//                boldRenderer.setIcon(null);
                grayRenderer.setIconTextGap(gap);
            } else {
                boldRenderer.setIconTextGap(gap);
//                grayRenderer.setIcon(null);
            }
        } else {
            normalRenderer.setIconTextGap(gap);
//            boldRenderer.setIcon(null);
//            grayRenderer.setIcon(null);
        }
    }
        
}
