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
