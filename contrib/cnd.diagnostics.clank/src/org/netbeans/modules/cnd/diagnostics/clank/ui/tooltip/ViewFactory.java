/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.cnd.diagnostics.clank.ui.tooltip;

import java.beans.PropertyChangeListener;
import javax.swing.JComponent;
import org.netbeans.modules.cnd.diagnostics.clank.ui.views.ViewComponent;


/**
 * Factory that produces debugger views created from registered view models
 * (see {@link org.netbeans.spi.viewmodel.Model} and it's extension interfaces).
 * 
 * @since 2.34
 */
public class ViewFactory {
    
    private static ViewFactory vf;
    
    private ViewFactory() {}
    
    /**
     * Get the default implementation of view factory.
     * 
     * @return The view factory.
     */
    public static synchronized ViewFactory getDefault() {
        if (vf == null) {
            vf = new ViewFactory();
        }
        return vf;
    }
    

    /**
     * Create {@link JComponent} view from models registered under 'name' path.
     * @param icon The icon resource, possibly used by the button toolbar in the view
     * @param name Name of the view, under which are the models registered
     * @param helpID The helpID of the created TopComponent
     * @param propertiesHelpID The helpID of properties displayed in the view
     * @return Component containing the view created from registered models.
     */
    public JComponent createViewComponent(String icon, String name, String helpID, String propertiesHelpID) {
        JComponent c = new ViewComponent(icon, name, helpID, propertiesHelpID);
        return c;
    }
        
    
    /**
     * Create a tooltip with additional actions.
     * The tooltip can be expandable to a structured tooltip view, or pinnable as a pin watch.
     * @param toolTipText The text to display as a tooltip
     * @param expandable Description of an expanded tooltip,
     *                   or <code>null</code> when the tooltip is not expandable
     * @param pinnable Description of a pin watch created from this tooltip,
     *                 or <code>null</code> when the tooltip is not pinnable
     * @return A tooltip UI to be shown.
     * @since 2.54
     */
    public ToolTipUI createToolTip(String toolTipText,
                                   ToolTipUI.Expandable expandable,
                                   ToolTipUI.Pinnable pinnable, PropertyChangeListener l) {
        return new ToolTipUI(toolTipText, expandable, pinnable, l);
    }

}
