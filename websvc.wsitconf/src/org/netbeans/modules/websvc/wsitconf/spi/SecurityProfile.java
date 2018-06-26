/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
