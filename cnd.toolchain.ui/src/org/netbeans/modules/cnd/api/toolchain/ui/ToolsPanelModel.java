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

import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.util.NbPreferences;

/** Manage the data for the ToolsPanel */
public abstract class ToolsPanelModel {
    
    private static final String PROP_COMPILER_SET_NAME = "compilerSetName"; // NOI18N

    public abstract void setMakeRequired(boolean value);
    
    public abstract boolean isMakeRequired();
    
    public abstract boolean isDebuggerRequired();
    
    public abstract void setDebuggerRequired(boolean value);
    
    public abstract boolean isCRequired();
    
    public abstract void setCRequired(boolean value);
    
    public abstract boolean isCppRequired();
    
    public abstract void setCppRequired(boolean value);
    
    public abstract boolean isFortranRequired();
    
    public abstract void setFortranRequired(boolean value);

    public abstract boolean isQMakeRequired();

    public abstract void setQMakeRequired(boolean value);

    public abstract boolean isAsRequired();

    public abstract void setAsRequired(boolean value);
    
    public String getCompilerSetName() {
        return getCompilerSetNameImpl();
    }

    private static String getCompilerSetNameImpl() {
        String name = NbPreferences.forModule(ToolsPanelModel.class).get(PROP_COMPILER_SET_NAME, null);
        if (name == null) {
            return "";
        } else {
            return name;
        }
    }

    public void setCompilerSetName(String name) {
        resetCompilerSetName(name);
    }

    public static void resetCompilerSetName(String name) {
        String n = getCompilerSetNameImpl();
        if (n == null || !n.equals(name)) {
            NbPreferences.forModule(ToolsPanelModel.class).put(PROP_COMPILER_SET_NAME, name);
            //firePropertyChange(PROP_COMPILER_SET_NAME, n, name);
        }
    }

    public void setSelectedCompilerSetName(String name) {};
    
    public String getSelectedCompilerSetName() {return null;}
    
    public abstract boolean showRequiredTools();
    
    public abstract void setShowRequiredBuildTools(boolean value);
    
    public abstract boolean showRequiredBuildTools();
    
    public abstract void setShowRequiredDebugTools(boolean value);
    
    public abstract boolean showRequiredDebugTools();
    
    public void setEnableRequiredCompilerCB(boolean enabled) {}
    
    public boolean enableRequiredCompilerCB() {return true;}

    private ExecutionEnvironment selectedDevelopmentHost = null;

    public void setSelectedDevelopmentHost(ExecutionEnvironment env) {
        selectedDevelopmentHost = env;
    }

    public ExecutionEnvironment getSelectedDevelopmentHost() {
        return selectedDevelopmentHost;
    }

    public abstract void setEnableDevelopmentHostChange(boolean value);

    public abstract boolean getEnableDevelopmentHostChange();
}
