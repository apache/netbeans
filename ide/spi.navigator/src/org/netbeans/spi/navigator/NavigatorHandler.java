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

package org.netbeans.spi.navigator;

import org.netbeans.modules.navigator.NavigatorController;
import org.netbeans.modules.navigator.NavigatorTC;
import org.openide.util.Lookup;

/**
 * Set of methods for driving navigator behaviour.
 *
 * @author Dafe Simonek
 */
public final class NavigatorHandler {

    private static NavigatorController controller;

    /** No external instantiation allowed.
     */
    private NavigatorHandler () {
    }
    
    /** 
     * Activates and shows given panel in navigator view. Panel must be one of  
     * available panels at the time this method is called, which means that 
     * panel must be registered (either through mime type in xml layer or NavigatorLookupHint)
     * for currently activated node in the system.  
     * Previously activated panel is deactivated and hidden.
     * <p>
     * Typical use case is to set preferred navigator panel in a situation 
     * when multiple panels are registered for multiple data types.   
     * <p>
     * This method must be called from EventQueue thread.
     * 
     * @param panel Navigator panel to be activated
     * @throws IllegalArgumentException if given panel is not available 
     */ 
    public static void activatePanel (NavigatorPanel panel) {
        getController().activatePanel(panel);
    }

    /**
     * If there is a custom {@link NavigatorDisplayer} implementation, it should call
     * this method just before its UI shows up (before the enclosing
     * TopComponent is opened) to actually initialize the navigator. From this
     * point the navigator observes the TopComponent and once it is opened, it
     * starts collecting panels from the providers and passing them to the
     * displayer.
     * <p>
     * If there is no custom displayer registered, the navigator's own
     * (default) TopComponent will be used and it also takes care of
     * initializing the navigator automatically. No need to call this method then.
     * <p>
     * @since 1.19
     */
    public static void activateNavigator() {
        getController();
    }

    private static NavigatorController getController() {
        if (controller == null) {
            NavigatorDisplayer display = Lookup.getDefault().lookup(NavigatorDisplayer.class);
            if (display != null) {
                controller = new NavigatorController(display);
            } else { // use the navigator's own TopComponent
                controller = NavigatorTC.getInstance().getController();
            }
        }
        return controller;
    }
}
