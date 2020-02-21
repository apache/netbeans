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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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


package org.netbeans.modules.cnd.api.toolchain.ui;

/** Manage the data for the ToolsPanel */
public class LocalToolsPanelModel extends ToolsPanelModel {
    
    private String compilerSetName;
    private String selectedCompilerSetName;
    private boolean enableDevelopmentHostChange = true;
    private boolean makeRequired;
    private boolean gdbRequired;
    private boolean cRequired;
    private boolean cppRequired;
    private boolean fortranRequired;
    private boolean qmakeRequired;
    private boolean asRequired;
    private boolean showBuildTools;
    private boolean showDebugTools;
    private boolean enableRequiredCompilersCB;
    
    public LocalToolsPanelModel() {
        compilerSetName = null;
        selectedCompilerSetName = null;
        makeRequired = false;
        gdbRequired = false;
        cRequired = false;
        cppRequired = false;
        fortranRequired = false;
        qmakeRequired = false;
        asRequired = false;
        showBuildTools = false;
        showDebugTools = false;
        enableRequiredCompilersCB = true;
    }
    
    @Override
    public void setCompilerSetName(String name) {
        compilerSetName = name;
    }
    
    @Override
    public String getCompilerSetName() {
        if (compilerSetName == null) {
            compilerSetName = super.getCompilerSetName();
        }
        return compilerSetName;
    }
    
    @Override
    public void setSelectedCompilerSetName(String name) {
        selectedCompilerSetName = name;
    }
    
    @Override
    public String getSelectedCompilerSetName() {
        return selectedCompilerSetName;
    }
    
    @Override
    public boolean isMakeRequired() {
        return makeRequired;
    }
    
    @Override
    public void setMakeRequired(boolean enabled) {
        makeRequired = enabled;
    }
    
    @Override
    public boolean isDebuggerRequired() {
        return gdbRequired;
    }
    
    @Override
    public void setDebuggerRequired(boolean enabled) {
        gdbRequired = enabled;
    }
    
    @Override
    public boolean isCRequired() {
        return cRequired;
    }
    
    @Override
    public void setCRequired(boolean enabled) {
        cRequired = enabled;
    }
    
    @Override
    public boolean isCppRequired() {
        return cppRequired;
    }
    
    @Override
    public void setCppRequired(boolean enabled) {
        cppRequired = enabled;
    }
    
    @Override
    public boolean isFortranRequired() {
        return fortranRequired;
    }
    
    @Override
    public void setFortranRequired(boolean enabled) {
        fortranRequired = enabled;
    }

    @Override
    public void setQMakeRequired(boolean value) {
        qmakeRequired = value;
    }

    @Override
    public boolean isQMakeRequired() {
        return qmakeRequired;
    }

    @Override
    public boolean isAsRequired() {
        return asRequired;
    }

    @Override
    public void setAsRequired(boolean enabled) {
        asRequired = enabled;
    }
    
    @Override
    public boolean showRequiredTools() {
        return true;
    }
    
    @Override
    public void setShowRequiredBuildTools(boolean enabled) {
        showBuildTools = enabled;
    }
    
    @Override
    public boolean showRequiredBuildTools() {
        return showBuildTools;
    }
    
    @Override
    public void setShowRequiredDebugTools(boolean enabled) {
        showDebugTools = enabled;
    }
    
    @Override
    public boolean showRequiredDebugTools() {
        return showDebugTools;
    }
    
    @Override
    public void setEnableRequiredCompilerCB(boolean enabled) {
        enableRequiredCompilersCB = enabled;
    }
    
    @Override
    public boolean enableRequiredCompilerCB() {
        return enableRequiredCompilersCB;
    }

    @Override
    public void setEnableDevelopmentHostChange(boolean value) {
        enableDevelopmentHostChange = value;
    }

    @Override
    public boolean getEnableDevelopmentHostChange() {
        return enableDevelopmentHostChange;
    }
}
