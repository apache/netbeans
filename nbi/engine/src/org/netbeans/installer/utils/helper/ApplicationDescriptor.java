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
