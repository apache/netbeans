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

package org.netbeans.swing.tabcontrol;

import java.awt.Component;
import org.netbeans.swing.tabcontrol.customtabs.TabbedComponentFactory;
import org.openide.windows.TopComponent;


/**
 * Interface that provides external information (provided by window system)
 * that TabbedContainers need to know in order to work fully.<p>
 *
 * Tab control uses info to provide for example tab buttons reacting on 
 * the position of the container or on maximization state.
 *
 * @see TabbedContainer#TabbedContainer
 * @see TabbedComponentFactory
 *
 * @author S. Aubrecht
 */
public abstract class WinsysInfoForTabbedContainer implements WinsysInfoForTabbed {

    /**
     * 
     * @return True if TopComponents can be slided out, false to remove Minimize button
     * from TopComponent's header.
     */
    public boolean isTopComponentSlidingEnabled() {
        return true;
    }
    
    /**
     * 
     * @return True if TopComponents can be closed, false to remove Close button
     * from TopComponent's header.
     */
    public boolean isTopComponentClosingEnabled() {
        return true;
    }

    /**
     * 
     * @return True if TopComponents can be maximized, false to remove Maximize/Restore
     * button from TopComponent's header./
     */
    public boolean isTopComponentMaximizationEnabled() {
        return true;
    }
    
    /**
     * @return True if given TopComponent can be closed, false to remove Close button
     * from TopComponent's header.
     * @since 1.15
     */
    public boolean isTopComponentClosingEnabled( TopComponent tc ) {
        return true;
    }

    /**
     * @return True if given TopComponent can be slided-out, false to remove 'Minimize Window'
     * action from TopComponent's popup menu.
     * @since 1.15
     */
    public boolean isTopComponentSlidingEnabled( TopComponent tc ) {
        return true;
    }

    /**
     * @return True if given TopComponent can be maximized.
     * @since 1.15
     */
    public boolean isTopComponentMaximizationEnabled( TopComponent tc ) {
        return true;
    }

    /**
     * @return True if it is possible to minimize the whole group of TopCOmponents.
     * @since 1.27
     */
    public boolean isModeSlidingEnabled() {
        return true;
    }
    
    /**
     * @return True if this container is currently slided out (and contains a single window)
     * @since 1.27
     */
    public boolean isSlidedOutContainer() {
        return false;
    }

    /**
     * Check if the header of given TopComponent should be painted in a special
     * way to indicate that some process is running in that TopComponent.
     * @param tc
     * @return True to indicate process being run in the TopComponent, false otherwise.
     * @since 1.34
     */
    public boolean isTopComponentBusy( TopComponent tc ) {
        return false;
    }

    public static WinsysInfoForTabbedContainer getDefault( WinsysInfoForTabbed winsysInfo ) {
        return new DefaultWinsysInfoForTabbedContainer( winsysInfo );
    }

    private static class DefaultWinsysInfoForTabbedContainer extends WinsysInfoForTabbedContainer {
        
        private WinsysInfoForTabbed delegate;
        
        public DefaultWinsysInfoForTabbedContainer( WinsysInfoForTabbed winsysInfo ) {
            this.delegate = winsysInfo;
        }

        public Object getOrientation(Component comp) {
            return null == delegate ? TabDisplayer.ORIENTATION_CENTER : delegate.getOrientation(comp);
        }

        public boolean inMaximizedMode(Component comp) {
            return null == delegate ? false : delegate.inMaximizedMode(comp);
        }
    }
}
