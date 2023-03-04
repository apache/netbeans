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
package org.netbeans.modules.websvc.design.view.widget;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Stroke;
import org.netbeans.api.visual.border.Border;
import java.awt.geom.Area;
import java.awt.geom.RoundRectangle2D;
import org.netbeans.api.visual.widget.Widget;

/**
 * @author David Kaspar
 */
public class RoundedBorder3D implements Border {
    
    private static final Color SHADOW_COLOR = new Color(208,208,208);
    private static final Color SELECTED_BORDER_COLOR = new Color(255,153,0);
   
    private Widget widget;
    private int radius;
    private int insetWidth;
    private int insetHeight;
    private Color drawColor;
    private int depth = 3;
    
    /**
     *
     * @param radius
     * @param depth
     * @param insetWidth
     * @param insetHeight
     * @param drawColor
     */
    public RoundedBorder3D(Widget widget, int radius, int depth, int insetWidth, int insetHeight, Color drawColor) {
        this.widget = widget;
        this.radius = radius;
        this.depth = depth;
        this.insetWidth = insetWidth;
        this.insetHeight = insetHeight;
        this.drawColor = drawColor;
    }
    
    public Insets getInsets() {
        return new Insets(insetHeight, insetWidth, insetHeight+depth, insetWidth+depth);
    }
    
    public void paint(Graphics2D gr, Rectangle bounds) {
        Paint oldPaint = gr.getPaint();
        RoundRectangle2D rect = new RoundRectangle2D.Double(bounds.x+0.5f,
                bounds.y + 0.5f, bounds.width - depth - 1, 
                bounds.height - depth - 1, radius, radius);
        if (drawColor != null) {
            RoundRectangle2D outerRect = new RoundRectangle2D.Double(
                    bounds.x + depth + 0.5f, bounds.y + depth + 0.5f,
                    bounds.width - depth - 1, bounds.height - depth - 1, radius, radius);
            Area raisedArea = new Area(outerRect);
            raisedArea.subtract(new Area(rect));
            gr.setPaint(SHADOW_COLOR);
            gr.fill(raisedArea);
            gr.setPaint(widget.getState().isSelected()?SELECTED_BORDER_COLOR:drawColor);
            Stroke s = gr.getStroke ();
            if(widget.getState().isFocused())
                gr.setStroke (new BasicStroke(1, BasicStroke.CAP_BUTT, 
                        BasicStroke.JOIN_ROUND, BasicStroke.JOIN_MITER, new float[] {2,2}, 0));
            gr.draw(rect);
            gr.setStroke (s);
        }
        gr.setPaint(oldPaint);
    }
    
    public boolean isOpaque() {
        return true;
    }

}
