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
/*
 * ContainerProvider.java
 *
 * Created on January 24, 2004, 2:32 PM
 */

package org.netbeans.actions.spi;

import java.util.Map;

/** Provides the names of available action containers in the system, such
 * as toolbars and menus.  Uses a predefined context name for the context
 * menu context, of which there is only ever one.
 *
 * @author  Tim Boudreau
 */
public abstract class ContainerProvider {
    public static final String CONTEXTMENU_CONTEXT = "contextMenu"; //NOI18N
    public static final Object TYPE_TOOLBAR = new Integer (1);
    public static final Object TYPE_MENU = new Integer (2);

    protected ContainerProvider() {
    }

    /** Return the names of all the menu container contexts in the system. */
    public abstract String[] getMenuContainerContexts();

    /** Return the names of all the menu container contexts in the system. */
    public abstract String[] getToolbarContainerContexts();

    /** returns the predefined name for the context menu container context */
    public final String getContextMenuContainerContext() {
        return CONTEXTMENU_CONTEXT;
    }
    
    /** Determine if the contents of the context can change over the life of
     * the application.  Return true <strong>only</strong> if items which are
     * truly unknown at startup will be added.  If there's a known set of items,
     * but some appear and disappear, simply return that the hidden items are
     * invisible from your ActionProvider's getState method */
    public boolean isDynamicContext (Object containerType, String containerCtx) {
        //XXX Support for dynamic contexts pending
        return false;
    }
    
    /** Get the enablement/visibility of the named container context.
     * @param containerType The type of container - either TYPE_MENU or TYPE_TOOLBAR,
     *  or some other object that the implementation and caller agree is a valid
     *  context type.  
     * @param containerCtx The programmatic, unique name of the container.
     * @param context The user context (selected object, active window, etc.,
     *  as agreed upon between the implementation and application).
     */
    public abstract int getContainerState (Object containerType, 
        String containerCtx, Map context);
    
    
    public abstract String getDisplayName (Object containerType, String containerCtx);
}
