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
package org.netbeans.modules.j2ee.deployment.devmodules.api;

import java.awt.EventQueue;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import org.netbeans.api.server.CommonServerUIs;
import org.netbeans.api.server.ServerInstance;
import org.netbeans.modules.j2ee.deployment.impl.ServerRegistry;
import org.netbeans.modules.j2ee.deployment.impl.bridge.BridgingServerInstanceProvider;
import org.netbeans.modules.j2ee.deployment.impl.bridge.ServerInstanceProviderLookup;
import org.netbeans.modules.j2ee.deployment.impl.ui.wizard.AddServerInstanceWizard;
import org.netbeans.modules.j2ee.deployment.plugins.api.CommonServerBridge;

/**
 * ServerManager class provides access to the Server Manager dialog.
 *
 * @author sherold
 * @since  1.7
 */
public final class ServerManager {

    /** Do not allow to create instances of this class */
    private ServerManager() {
    }
    
    /**
     * Display the modal Server Manager dialog with the specified server instance 
     * preselected. This method should be called form the AWT event dispatch 
     * thread.
     *
     * @param serverInstanceID server instance which should be preselected, if 
     *        null the first server instance will be preselected.
     * 
     * @throws IllegalThreadStateException if the method is not called from the 
     *         event dispatch thread.
     * @deprecated use {@link org.netbeans.api.server.CommonServerUIs#showCustomizer} instead
     */
    public static void showCustomizer(String serverInstanceID) {
        // bridge to new infrastructure (common server)
        ServerInstance bridgingInstance = CommonServerBridge.getCommonInstance(serverInstanceID);
        CommonServerUIs.showCustomizer(bridgingInstance);
    }
    
    /**
     * Displays the add server instance wizard and returns the ID of the added
     * server instance. It is intended for J2EE related code only.
     * 
     * @return server instance ID of the new server instance, or <code>null</code>
     *         if the wizard was canceled.
     * 
     * @throws IllegalThreadStateException if the method is not called from the 
     *         event dispatch thread.
     * 
     * @since  1.28
     */
    public static String showAddServerInstanceWizard() {
        return showAddServerInstanceWizard(Collections.<String, String>emptyMap());
    }
    
    /**
     * Displays the add server instance wizard and returns the ID of the added
     * server instance. It is intended for J2EE related code only.
     * 
     * @param props properties which will initialize wizard descriptor properties
     * @return server instance ID of the new server instance, or <code>null</code>
     *         if the wizard was canceled.
     * 
     * @throws IllegalThreadStateException if the method is not called from the 
     *         event dispatch thread.
     * 
     * @since  1.83
     */
    public static String showAddServerInstanceWizard(Map<String, String> props) {
        checkDispatchThread();
        return AddServerInstanceWizard.showAddServerInstanceWizard(props);
    }
    
    private static void checkDispatchThread() {
	if (!EventQueue.isDispatchThread()) {
	    throw new IllegalThreadStateException("Can only be called from the event dispatch thread."); // NOI18N
	}
    }
}
