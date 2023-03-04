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

package org.netbeans.modules.websvc.wsitconf.spi;

import javax.swing.undo.UndoManager;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.wsitmodelext.versioning.ConfigVersion;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;

/**
 * Security Profile
 *
 * @author Martin Grebac
 */
public abstract class SecurityProfile {
    
    public static final String CFG_KEYSTORE="cfgkeystore";
    public static final String CFG_TRUSTSTORE="cfgtruststore";
    public static final String CFG_VALIDATORS="cfgvalidators";
            
    /**
     * Returns display name to be presented in UI.
     * @return 
     */
    public abstract String getDisplayName();

    /**
     * Returns a longer description of the profile to be presented in the UI.
     * @return 
     */
    public abstract String getDescription();

    /**
     * Returns id for sorting the profiles. WSIT default profiles have ids 10, 20, 30, ... to keep space for additional profiles
     * @return 
     */
    public abstract int getId();
    
    /**
     * Called when the profile is selected in the combo box.
     * @param component 
     */
    public abstract void profileSelected(WSDLComponent component, boolean updateServiceURL, ConfigVersion configVersion);

    /**
     * Called when there's another profile selected, or security is disabled at all.
     * @param component 
     */ 
    public abstract void profileDeselected(WSDLComponent component, ConfigVersion configVersion);

    /**
     * Should return true if the profile is supported for specific component in the wsdl
     * @param p 
     * @param component 
     * @return 
     */
    public boolean isProfileSupported(Project p, WSDLComponent component, boolean sts) {
        return true;
    }

    /**
     * Should return true if the profile is set on component, false otherwise
     * @param component 
     * @return 
     */
    public abstract boolean isCurrentProfile(WSDLComponent component);
    
    /**
     * Should open configuration UI and block until user doesn't close it.
     * @param component 
     * @param undoManager 
     */
    public void displayConfig(WSDLComponent component, UndoManager undoManager) { }
}
