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

package org.netbeans.modules.cnd.makeproject.api.configurations;

import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.makeproject.api.configurations.LibraryItem;

public abstract class Platform {
    
    private final String name;
    private final String displayName;
    private final int id;
    
    public Platform(String name, String displayName, int id) {
        this.name = name;
        this.displayName = displayName;
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public int getId() {
        return id;
    }
    
    public abstract LibraryItem.StdLibItem[] getStandardLibraries();
    
    public String getLibraryName(String baseName) {
        return getLibraryNameWithoutExtension(baseName) + "." + getLibraryExtension(); // NOI18N
    }    
    
    public abstract String getLibraryNameWithoutExtension(String baseName);
    
    public abstract String getLibraryExtension();
       
    /**
     * File name that qmake would generate on current platform
     * given <code>TARGET=baseName</code> and <code>VERSION=version</code>.
     *
     * @param baseName
     * @param version
     * @return
     */
    public String getQtLibraryName(String baseName, String version) {
        return getLibraryName(baseName) + "." + version; // NOI18N
    }

    public abstract String getLibraryLinkOption(String libName, String libDir, String libPath, String libSearchPath, CompilerSet compilerSet);
    
    public LibraryItem.StdLibItem getStandardLibrarie(String name) {
        for (int i = 0; i < getStandardLibraries().length; i++) {
            if (getStandardLibraries()[i].getName().equals(name)) {
                return getStandardLibraries()[i];
            }
        }
        return null;
    }
}
