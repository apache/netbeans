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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.api.visual.border.Border;

import java.awt.*;
import org.netbeans.api.visual.widget.ResourceTable;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.visual.laf.DefaultLookFeel;

/**
 * @author alex_grk
 */
public class DashedBorder implements Border {

    private static final BasicStroke BASIC_STROKE = new BasicStroke ();

    protected int thickness = 1;
    protected Color color;

    private BasicStroke stroke = BASIC_STROKE;
    private ResourceTableListener listener = null;

    public DashedBorder (Color color, float l1, float l2) {
        this (color, new float[] { l1, l2 }, 1);
    }
    
    public DashedBorder (String property, Widget associated, float l1, float l2) {
        this (property, associated.getResourceTable(), new float[] { l1, l2 }, 1);
    }
    
    public DashedBorder (String property, ResourceTable table, float l1, float l2) {
        this (property, table, new float[] { l1, l2 }, 1);
    }
    
    public DashedBorder (Color color, float[] dash, int thickness) {
        if (thickness < 1) {
            throw new IllegalArgumentException ("Invalid thickness: " + thickness);
        }
        this.thickness = thickness;
        this.color = color != null ? color : (new DefaultLookFeel()).getForeground()/*Color.BLACK*/;
        stroke = new BasicStroke (thickness, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, BasicStroke.JOIN_MITER, dash, 0);
    }
    
    public DashedBorder (String property, ResourceTable table, float[] dash, int thickness) {
        if (thickness < 1) {
            throw new IllegalArgumentException ("Invalid thickness: " + thickness);
        }
        this.thickness = thickness;
        stroke = new BasicStroke (thickness, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, BasicStroke.JOIN_MITER, dash, 0);
        
        Object value = table.getProperty(property);
        if(value instanceof Color)
        {
            this.color = (Color)value;
        }
        
        listener = new ResourceTableListener();
        table.addPropertyChangeListener(property, listener);
    }
    
    public Insets getInsets () {
        return new Insets (thickness, thickness, thickness, thickness);
    }

    public void paint (Graphics2D g, Rectangle bounds) {
        Stroke s = g.getStroke ();
        g.setColor (color);
        g.setStroke (stroke);
        g.drawRect (bounds.x, bounds.y, bounds.width - thickness, bounds.height - thickness);
        g.setStroke (s);
    }

    public boolean isOpaque () {
        return true;
    }
    
    public class ResourceTableListener implements PropertyChangeListener
    {
        public void propertyChange(PropertyChangeEvent event)
        {
            color = (Color)event.getNewValue();
        }
    }
}
