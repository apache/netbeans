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

package org.netbeans.modules.cnd.discovery.wizard.tree;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.cnd.discovery.wizard.api.NodeConfiguration;

/**
 *
 */
public abstract class NodeConfigurationImpl implements NodeConfiguration {
    private boolean isOverrideIncludes;
    private boolean isOverrideFiles;
    private boolean isOverrideMacros;
    private boolean isOverrideUndefinedMacros;
    private NodeConfigurationImpl parent;
    private final Set<String> userIncludes;
    private final Set<String> userFiles;
    private final Map<String, String> userMacros;
    private final Set<String> undefinedMacros;

    public NodeConfigurationImpl() {
        userIncludes = new LinkedHashSet<>();
        userFiles = new HashSet<>();
        userMacros = new HashMap<>();
        undefinedMacros = new LinkedHashSet<>();
    }

    @Override
    public boolean overrideIncludes() {
        return isOverrideIncludes;
    }

    public void setOverrideIncludes(boolean overrideIncludes) {
        isOverrideIncludes = overrideIncludes;
    }

    @Override
    public boolean overrideFiles() {
        return isOverrideFiles;
    }

    public void setOverrideFiles(boolean overrideFiles) {
        isOverrideFiles = overrideFiles;
    }

    @Override
    public boolean overrideMacros() {
        return isOverrideMacros;
    }

    public void setOverrideMacros(boolean overrideMacros) {
        isOverrideMacros = overrideMacros;
    }

    @Override
    public boolean overrideUndefinedMacros() {
        return isOverrideUndefinedMacros;
    }

    public void setOverrideUndefinedMacros(boolean isOverrideUndefinedMacros) {
        this.isOverrideUndefinedMacros = isOverrideUndefinedMacros;
    }

    public void setParent(NodeConfigurationImpl parent) {
        this.parent = parent;
    }

    public NodeConfigurationImpl getParent() {
        return parent;
    }

    @Override
    public Set<String> getUserInludePaths(boolean resulting) {
        if (resulting) {
            return countUserInludePaths();
        } else {
            return userIncludes;
        }
    }

    public void setUserInludePaths(Collection<String> set) {
         userIncludes.clear();
         if (set != null) {
            userIncludes.addAll(set);
         }
    }

    @Override
    public Set<String> getUserInludeFiles(boolean resulting) {
        if (resulting) {
            return countUserInludeFiles();
        } else {
            return userFiles;
        }
    }

    public void setUserInludeFiles(Collection<String> set) {
         userFiles.clear();
         if (set != null) {
            userFiles.addAll(set);
         }
    }

    @Override
    public Map<String, String> getUserMacros(boolean resulting) {
        if (resulting) {
            return countUserMacros();
        } else {
            return userMacros;
        }
    }

    public void setUserMacros(Map<String, String> map) {
        userMacros.clear();
        if (map != null) {
            userMacros.putAll(map);
        }
    }

    @Override
    public Set<String> getUndefinedMacros(boolean resulting) {
        if (resulting) {
            return countUndefinedMacros();
        } else {
            return undefinedMacros;
        }
    }

    public void setUndefinedMacros(Collection<String> set) {
         undefinedMacros.clear();
         if (set != null) {
            undefinedMacros.addAll(set);
         }
    }
    
    public Set<String> countUserInludePaths() {
        if (overrideIncludes()) {
            return userIncludes;
        }
        Set<String> result = new LinkedHashSet<>();
        NodeConfigurationImpl current = this;
        while(current != null){
            result.addAll(current.getUserInludePaths(false));
             if (current.overrideIncludes()) {
                break;
             }
            current = current.getParent();
        }
        return result;
    }
    
    public Set<String> countUserInludeFiles() {
        if (overrideFiles()) {
            return userFiles;
        }
        Set<String> result = new LinkedHashSet<>();
        NodeConfigurationImpl current = this;
        while(current != null){
            result.addAll(current.getUserInludeFiles(false));
             if (current.overrideFiles()) {
                break;
             }
            current = current.getParent();
        }
        return result;
    }
    
    public Map<String, String> countUserMacros() {
        if (overrideMacros()) {
            return userMacros;
        }
        Map<String, String> result =  new HashMap<>();
        NodeConfigurationImpl current = this;
        while(current != null){
            result.putAll(current.getUserMacros(false));
            if (current.overrideMacros()){
                break;
            }
            current = current.getParent();
        }
        return result;
    }

    public Set<String> countUndefinedMacros() {
        if (overrideUndefinedMacros()) {
            return undefinedMacros;
        }
        Set<String> result = new LinkedHashSet<>();
        NodeConfigurationImpl current = this;
        while(current != null){
            result.addAll(current.getUndefinedMacros(false));
             if (current.overrideUndefinedMacros()) {
                break;
             }
            current = current.getParent();
        }
        return result;
    }
}
