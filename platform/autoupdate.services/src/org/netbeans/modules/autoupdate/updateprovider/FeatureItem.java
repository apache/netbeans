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

package org.netbeans.modules.autoupdate.updateprovider;

import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.autoupdate.services.UpdateLicenseImpl;
import org.openide.modules.Dependency;

/**
 *
 * @author Jiri Rechtacek
 */
public class FeatureItem extends UpdateItemImpl {
    
    private String codeName;
    private String specificationVersion;
    private Set<String> dependenciesToModules;
    private Set<String> moduleCodeNames;
    private String displayName;
    private String description;
    private String category;

    public FeatureItem (
            String codeName,
            String specificationVersion,
            Set<String> dependencies,
            String displayName,
            String description,
            String category) {
        if (dependencies == null) {
            throw new IllegalArgumentException ("Cannot create FeatureItem " + codeName + " with null modules."); // NOI18N
        }
        this.codeName = codeName;
        this.specificationVersion = specificationVersion;
        this.dependenciesToModules = dependencies;
        this.displayName = displayName;
        this.description = description;
        this.category = category;
    }
    
    @Override
    public String getCodeName () {
        return this.codeName;
    }
    
    public String getSpecificationVersion () {
        return this.specificationVersion;
    }
    
    public String getDisplayName () {
        return this.displayName;
    }
    
    public String getDescription () {
        return this.description;
    }
    
    public Set<String> getDependenciesToModules () {
        return this.dependenciesToModules;
    }
    
    public Set<String> getModuleCodeNames () {
        if (moduleCodeNames == null) {
            moduleCodeNames = new HashSet<String> ();
            for (String depSpec : dependenciesToModules) {
                Set<Dependency> deps = Dependency.create (Dependency.TYPE_MODULE, depSpec);
                assert deps.size () == 1 : "Only one dependency for " + depSpec;
                Dependency dep = deps.iterator ().next ();
                assert Dependency.TYPE_MODULE == dep.getType () : "Only Dependency.TYPE_MODULE supported, but " + dep;
                String name = dep.getName ();
                // trim release impl.
                if (name.indexOf ('/') != -1) {
                    int to = name.indexOf ('/');
                    name = name.substring (0, to);
                }
                moduleCodeNames.add (name);
            }
        }
        return moduleCodeNames;
    }
    
    @Override
    public UpdateLicenseImpl getUpdateLicenseImpl () {
        assert false : "Not provided yet";
        return null;
    }

    @Override
    public String getCategory () {
        return category;
    }

    @Override
    public void setUpdateLicenseImpl (UpdateLicenseImpl licenseImpl) {
        assert false : "Not provided yet";
    }
    
    @Override
    public String toString() {
        return "FeatureItem[" + this.getCodeName() + "/" + this.getSpecificationVersion() + "]";
    }

    @Override
    public void setNeedsRestart(Boolean needsRestart) {
        // do nothing
    }
}
