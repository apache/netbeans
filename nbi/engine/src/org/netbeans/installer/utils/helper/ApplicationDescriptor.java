/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Oracle
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
 */

package org.netbeans.installer.utils.helper;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Kirill Sorokin
 */
public class ApplicationDescriptor {
    private String uid;
    
    private String displayName;
    private String icon;
    
    private String installPath;
    
    private String [] uninstallCommand;
    private String [] modifyCommand;
    
    private Map<String, Object> parameters;
    
    public ApplicationDescriptor(
            final String uid,
            final String displayName,
            final String icon,
            final String installPath,
            final String [] uninstallCommand,
            final String [] modifyCommand) {
        this.uid = uid;
        
        this.displayName = displayName;
        this.icon = icon;
        
        this.installPath = installPath;
        
        this.uninstallCommand = uninstallCommand;
        this.modifyCommand = modifyCommand;
        
        this.parameters = new HashMap<String, Object>();
    }
    
    public ApplicationDescriptor(
            final String uid,
            final String displayName,
            final String icon,
            final String installPath,
            final String [] uninstallCommand,
            final String [] modifyCommand,
            final Map<String, Object> parameters) {
        this(uid,
            displayName,
            icon,
            installPath,
            uninstallCommand,
            modifyCommand);
        
        this.parameters.putAll(parameters);
    }
    
    public String getUid() {
        return uid;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getIcon() {
        return icon;
    }
    
    public String getInstallPath() {
        return installPath;
    }
    
    public String [] getUninstallCommand() {
        return uninstallCommand;
    }
    
    public String [] getModifyCommand() {
        return modifyCommand;
    }
    
    public Map<String, Object> getParameters() {
        return parameters;
    }
}
