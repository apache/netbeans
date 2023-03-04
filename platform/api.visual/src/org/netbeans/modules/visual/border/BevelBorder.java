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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.api.visual.widget.ResourceTable;
import org.netbeans.api.visual.widget.Widget;
import java.awt.*;

/**
 * @author David Kaspar
 */
public final class BevelBorder implements Border {

    private boolean raised;
    private Color color;
    private ResourceTableListener listener = null;

    public BevelBorder (boolean raised, Color color) {
        this.raised = raised;
        this.color = color;
    }
    
    public BevelBorder(boolean raised, String property, Widget associated)
    {
        this(raised, property, associated.getResourceTable());
    }
    
    public BevelBorder (boolean raised, String property, ResourceTable table) {
        this.raised = raised;
        
        Object value = table.getProperty(property);
        if(value instanceof Color)
        {
            this.color = (Color)value;
        }
        
        listener = new ResourceTableListener();
        table.addPropertyChangeListener(property, listener);
    }
    
    public Insets getInsets () {
        return new Insets (2, 2, 2, 2);
    }

    public void paint (Graphics2D gr, Rectangle bounds) {
        gr.setColor (color);
        int h = bounds.height;
        int w = bounds.width;

        gr.translate (bounds.x, bounds.y);

        gr.setColor (raised ? color.brighter ().brighter () : color.darker ().darker ());
        gr.drawLine (0, 0, 0, h - 2);
        gr.drawLine (1, 0, w - 2, 0);

        gr.setColor (raised ? color.brighter () : color.darker ());
        gr.drawLine (1, 1, 1, h - 3);
        gr.drawLine (2, 1, w - 3, 1);

        gr.setColor (raised ? color.darker ().darker () : color.brighter ().brighter ());
        gr.drawLine (0, h - 1, w - 1, h - 1);
        gr.drawLine (w - 1, 0, w - 1, h - 2);

        gr.setColor (raised ? color.darker () : color.brighter ());
        gr.drawLine (1, h - 2, w - 2, h - 2);
        gr.drawLine (w - 2, 1, w - 2, h - 3);

        gr.translate (- bounds.x, - bounds.y);
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
