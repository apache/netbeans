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

package org.netbeans.modules.apisupport.project.universe;

import java.io.File;
import java.net.URL;
import java.util.Collections;
import java.util.Set;
import org.netbeans.modules.apisupport.project.api.ManifestManager.PackageExport;

/**
 * A placeholder for a module which cannot be found.
 */
public class NonexistentModuleEntry implements ModuleEntry {

    private final String cnb;

    public NonexistentModuleEntry(String cnb) {
        this.cnb = cnb;
    }

    public @Override String getNetBeansOrgPath() {return null;}

    public @Override File getSourceLocation() {return null;}

    public @Override String getCodeNameBase() {return cnb;}
    
    public @Override File getClusterDirectory() {return new File(".");}
    
    public @Override File getJarLocation() {return new File(cnb.replace('.', '-') + ".jar");}
    
    public @Override String getClassPathExtensions() {return "";}
    
    public @Override String getReleaseVersion() {return null;}
    
    public @Override String getSpecificationVersion() {return null;}
    
    public @Override String[] getProvidedTokens() {return new String[0];}
    
    public @Override String getLocalizedName() {return cnb;}
    
    public @Override String getCategory() {return null;}
    
    public @Override String getLongDescription() {return null;}
    
    public @Override String getShortDescription() {return null;}
    
    public @Override PackageExport[] getPublicPackages() {return new PackageExport[0];}
    
    public @Override URL getJavadoc(NbPlatform platform) {return null;}
    
    public @Override Set<String> getAllPackageNames() {return Collections.emptySet();}
    
    public @Override boolean isDeclaredAsFriend(String cnb) {return false;}
    
    public @Override Set<String> getPublicClassNames() {return Collections.emptySet();}
    
    public @Override boolean isDeprecated() {return false;}
    
    public @Override String[] getRunDependencies() {return new String[0];}
    
    public @Override int compareTo(ModuleEntry o) {return cnb.compareTo(o.getCodeNameBase());}

}
