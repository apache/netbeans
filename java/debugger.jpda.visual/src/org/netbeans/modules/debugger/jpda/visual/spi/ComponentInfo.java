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
package org.netbeans.modules.debugger.jpda.visual.spi;

import java.awt.Rectangle;
import java.beans.PropertyChangeListener;
import javax.swing.Action;
import org.openide.nodes.Node.PropertySet;

/**
 * This interface provides information about a remote component.
 * 
 * @author Martin Entlicher
 */
public interface ComponentInfo {
    
    /**
     * Provides the display name of the component.
     * @return The component display name.
     */
    String getDisplayName();

    /**
     * Return a variant of the component display name containing HTML markup
     * conforming to the limited subset of font-markup HTML supported by
     * the lightweight HTML renderer <code>org.openide.awt.HtmlRenderer</code>
     * (font color, bold, italic and strike-through supported; font
     * colors can be UIManager color keys if they are prefixed with
     * a ! character, i.e. <samp>&lt;font color='!controlShadow'&gt;</samp>).
     * Enclosing <samp>&lt;html&gt;</samp> tags are not needed. If returning non-null, HTML
     * markup characters that should be literally rendered must be
     * escaped (<samp>&gt;</samp> becomes <samp>&amp;gt;</samp> and so forth).
     * <p><strong>This method should return either an HTML display name
     * or null; it should not return the non-HTML display name.</strong>
     *
     * @see org.openide.awt.HtmlRenderer
     * @return a String containing conformant HTML markup which
     *  represents the display name, or <code>null</code>.
     */
    String getHtmlDisplayName();

    /**
     * Provides the actions that are available on the component.
     * @param context
     * @return 
     */
    Action[] getActions(boolean context);

    /**
     * Get the component bounds relative to it's parent component.
     * @return The component bounds.
     */
    Rectangle getBounds();

    /**
     * Get the component bounds relative to the window.
     * @return The component bounds.
     */
    Rectangle getWindowBounds();

    /**
     * Get property sets of the component.
     * @return The property sets.
     */
    PropertySet[] getPropertySets();

    /**
     * Get the list of sub-components.
     * @return The sub-components.
     */
    ComponentInfo[] getSubComponents();

    /**
     * Retrieves a component occupying the given position
     * @param x Horizontal position
     * @param y Vertical position
     * @return Returns a subcomponent residing at the given position or this component itself
     */
    ComponentInfo findAt(int x, int y);
    
    /**
     * Add a property change listener to listen on changes in component properties.
     * 
     * @param propertyChangeListener The property change listener
     */
    void addPropertyChangeListener(PropertyChangeListener propertyChangeListener);

    /**
     * Remove a property change listener.
     * 
     * @param propertyChangeListener The property change listener
     */
    void removePropertyChangeListener(PropertyChangeListener propertyChangeListener);

}
