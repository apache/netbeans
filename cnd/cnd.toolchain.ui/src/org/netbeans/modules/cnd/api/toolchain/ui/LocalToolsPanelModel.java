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
