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


package org.netbeans.modules.cnd.toolchain.ui.options;

import org.netbeans.modules.cnd.api.toolchain.ui.ToolsPanelModel;

/** Manage the data for the ToolsPanel */
/*package-local*/ final class GlobalToolsPanelModel extends ToolsPanelModel {
    
    @Override
    public void setMakeRequired(boolean value) {
        
    }
    
    @Override
    public boolean isMakeRequired() {
        return false;
    }
    
    @Override
    public boolean isDebuggerRequired() {
        return false;
    }
    
    @Override
    public void setDebuggerRequired(boolean value) {
    }
    
    @Override
    public boolean isCRequired() {
        return false;
    }
    
    @Override
    public void setCRequired(boolean value) {
    }
    
    @Override
    public boolean isCppRequired() {
        return false;
    }
    
    @Override
    public void setCppRequired(boolean value) {
    }
    
    @Override
    public boolean isFortranRequired() {
        return false;
    }
    
    @Override
    public void setFortranRequired(boolean value) {
    }

    @Override
    public void setQMakeRequired(boolean value) {
    }

    @Override
    public boolean isQMakeRequired() {
       return false;
    }

    @Override
    public boolean isAsRequired() {
        return false;
    }

    @Override
    public void setAsRequired(boolean value) {
    }
    
    @Override
    public boolean showRequiredTools() {
        return false;
    }
    
    public void setRequiredBuildTools(boolean enabled) {
 
    }
    
    @Override
    public void setShowRequiredBuildTools(boolean enabled) {
        
    }
    
    @Override
    public boolean showRequiredBuildTools() {
        return false;
    }
    
    @Override
    public void setShowRequiredDebugTools(boolean enabled) {
        
    }
    
    @Override
    public boolean showRequiredDebugTools() {
        return false;
    }

    @Override
    public void setEnableDevelopmentHostChange(boolean value) {
    }

    @Override
    public boolean getEnableDevelopmentHostChange() {
        return true;
    }
}
