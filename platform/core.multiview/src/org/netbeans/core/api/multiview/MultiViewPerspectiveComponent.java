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

package org.netbeans.core.api.multiview;

import java.util.TooManyListenersException;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JToolBar;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.openide.util.Lookup;

/** View element for multi view, provides the UI components to the multiview component.
 * Gets notified by the enclosing component about the changes in the lifecycle.
 *
 * @author  Milos Kleint
 */

// IS NOT PUBLIC, SHOULD NOT BE USED, ONLY IN EMERGENCY CASE!
final class MultiViewPerspectiveComponent {

    static {
        AccessorImpl.createAccesor();
    }
    
    private MultiViewElement element;
    
    MultiViewPerspectiveComponent(MultiViewElement elem) {
        element = elem;
    }
    
    // package private, access through Accessor
    MultiViewElement getElement() {
        return element;
    }

    /** Returns Swing visual representation of this multi view element. Should be relatively fast
     * and always return the same component.
     */
    public JComponent getVisualRepresentation () {
        return element.getVisualRepresentation();
    }
    
    /**
     * Returns the visual component with the multi view element's toolbar.Should be relatively fast as it's called
     * everytime the current perspective is switched.
     */
    public JComponent getToolbarRepresentation () {
        return element.getToolbarRepresentation();
    }
    
    /** Gets the actions which will appear in the popup menu of this component.
     * <p>Subclasses are encouraged to use add the default TopComponent actions to 
     * the array of their own. These are accessible by calling MultiViewElementCallback.createDefaultActions()
     * @return array of actions for this component
     */
    public Action[] getActions() {
        return element.getActions();
    }

    /**
     * Lookup for the MultiViewElement. Will become part of the TopComponent's lookup.
     * @return the lookup to use when the MultiViewElement is active.
     */
    public Lookup getLookup() {
        return element.getLookup();
    }
    
   
}
