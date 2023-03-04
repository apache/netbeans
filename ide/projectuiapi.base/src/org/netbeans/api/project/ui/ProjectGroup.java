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
package org.netbeans.api.project.ui;

import java.util.prefs.Preferences;
import org.netbeans.modules.project.uiapi.BaseUtilities;

/**
 * Object describing a project group, in most cases the currently active project group.
 * @author mkleint
 * @since 1.61
 */
public final class ProjectGroup {
    private final Preferences prefs;
    private final String name;

    ProjectGroup(String name, Preferences prefs) {
        this.name = name;
        this.prefs = prefs;
    }
    
    /**
     * name of the project group as given by user
     * @return 
     */
    public String getName() {
        return name;
    }
    
    /**
     * use this method to store and retrieve preferences related to project groups.
     * @param clazz
     * @return 
     */
    public Preferences preferencesForPackage(Class clazz) {
        return prefs.node(clazz.getPackage().getName().replace(".", "/"));
    }
    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 11 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ProjectGroup other = (ProjectGroup) obj;
        if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
            return false;
        }
        return true;
    }
    
    static {
        AccessorImpl impl = new AccessorImpl();
        impl.assign();
    }
 
    
    static class AccessorImpl extends BaseUtilities.ProjectGroupAccessor {
        
        
         public void assign() {
             if (BaseUtilities.ACCESSOR == null) {
                 BaseUtilities.ACCESSOR = this;
             }
         }
    
        @Override
        public ProjectGroup createGroup(String name, Preferences prefs) {
            return new ProjectGroup(name, prefs);
        }
    }    
    
    
}
