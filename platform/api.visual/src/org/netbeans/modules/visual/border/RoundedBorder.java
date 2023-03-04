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
package org.netbeans.modules.visual.border;

import org.netbeans.api.visual.border.Border;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.api.visual.widget.ResourceTable;
import org.netbeans.api.visual.widget.Widget;

/**
 * @author David Kaspar
 */
public final class RoundedBorder implements Border {

    private int arcWidth;
    private int arcHeight;
    private int insetWidth;
    private int insetHeight;
    private Color fillColor;
    private Color drawColor;
    private DrawResourceTableListener drawListener = null;
    private FillResourceTableListener fillListener = null;

    public RoundedBorder (int arcWidth, int arcHeight, int insetWidth, int insetHeight, Color fillColor, Color drawColor) {
        this.arcWidth = arcWidth;
        this.arcHeight = arcHeight;
        this.insetWidth = insetWidth;
        this.insetHeight = insetHeight;
        this.fillColor = fillColor;
        this.drawColor = drawColor;
    }
    
    public RoundedBorder (int arcWidth, int arcHeight, int insetWidth, int insetHeight, 
                          String fillProperty,  String drawProperty,  
                          Widget assocaited) {
        this(arcWidth, arcHeight, insetWidth, insetHeight, 
             fillProperty, drawProperty, assocaited.getResourceTable());
    }
    
    public RoundedBorder (int arcWidth, int arcHeight, int insetWidth, int insetHeight, 
                          String fillProperty,  String drawProperty,  
                          ResourceTable table) {
        this.arcWidth = arcWidth;
        this.arcHeight = arcHeight;
        this.insetWidth = insetWidth;
        this.insetHeight = insetHeight;
        
        
        Object value = table.getProperty(fillProperty);
        if(value instanceof Color)
        {
            this.fillColor = (Color)value;
        }
        
        value = table.getProperty(drawProperty);
        if(value instanceof Color)
        {
            this.drawColor = (Color)value;
        }
        
        drawListener = new DrawResourceTableListener();
        fillListener = new FillResourceTableListener();
        table.addPropertyChangeListener(fillProperty, fillListener);
        table.addPropertyChangeListener(drawProperty, drawListener);
    }
  
    public Insets getInsets () {
        return new Insets (insetHeight, insetWidth, insetHeight, insetWidth);
    }

    public void paint (Graphics2D gr, Rectangle bounds) {
        if (fillColor != null) {
            gr.setColor (fillColor);
            gr.fill (new RoundRectangle2D.Float (bounds.x, bounds.y, bounds.width, bounds.height, arcWidth, arcHeight));
        }
        if (drawColor != null) {
            gr.setColor (drawColor);
            gr.draw (new RoundRectangle2D.Float (bounds.x + 0.5f, bounds.y + 0.5f, bounds.width - 1, bounds.height - 1, arcWidth, arcHeight));
        }
    }

    public boolean isOpaque () {
        return false;
    }
    
    public class DrawResourceTableListener implements PropertyChangeListener
    {
        public void propertyChange(PropertyChangeEvent event)
        {
            drawColor = (Color)event.getNewValue();
        }
    }
    
    public class FillResourceTableListener implements PropertyChangeListener
    {
        public void propertyChange(PropertyChangeEvent event)
        {
            fillColor = (Color)event.getNewValue();
        }
    }
    
}
