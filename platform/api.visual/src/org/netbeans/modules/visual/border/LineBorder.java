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
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.visual.util.RenderUtil;
import org.netbeans.api.visual.border.Border;

import java.awt.*;
import java.beans.PropertyChangeListener;
import org.netbeans.api.visual.widget.ResourceTable;

/**
 * @author David Kaspar
 */
public final class LineBorder implements Border {

    private Insets insets;
    private Color color;
    private ResourceTableListener listener = null;

    public LineBorder (int top, int left, int bottom, int right, Color color) {
        insets = new Insets (top, left, bottom, right);
        this.color = color;
    }
    
    public LineBorder (int top, int left, int bottom, int right, 
                       String property, Widget attachedWidget) {
        this(top, left, bottom, right, property, attachedWidget.getResourceTable());
    }

    public LineBorder (int top, int left, int bottom, int right, 
                       String property, ResourceTable table)
    {
        insets = new Insets (top, left, bottom, right);
        
        Object value = table.getProperty(property);
        if(value instanceof Color)
        {
            this.color = (Color)value;
        }
        
        listener = new ResourceTableListener();
        table.addPropertyChangeListener(property, listener);
    }

    public Insets getInsets () {
        return insets;
    }

    public void paint (Graphics2D gr, Rectangle bounds) {
        gr.setColor (color);
        RenderUtil.drawRect (gr, bounds);
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
