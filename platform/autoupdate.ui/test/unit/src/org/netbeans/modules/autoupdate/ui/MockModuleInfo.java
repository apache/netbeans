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

package org.netbeans.modules.autoupdate.ui;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.Manifest;
import org.netbeans.spi.autoupdate.UpdateItem;
import org.netbeans.spi.autoupdate.UpdateLicense;
import org.openide.modules.Dependency;
import org.openide.modules.ModuleInfo;
import org.openide.modules.SpecificationVersion;

final class MockModuleInfo extends ModuleInfo {
    private final String cnb;
    private final String spec;
    private final Dependency[] deps;
    private final boolean enabled;

    private MockModuleInfo(String cnb, String spec, boolean enabled, Dependency... deps) {
        this.cnb = cnb;
        this.spec = spec;
        this.deps = deps;
        this.enabled = enabled;
    }
    
    public static MockModuleInfo create(String cnb, String spec, boolean enabled, MockModuleInfo... deps) {
        Dependency[] arr = new Dependency[deps.length];
        int i = 0;
        for (MockModuleInfo mmi : deps) {
            arr[i++] = Dependency.create(
                Dependency.TYPE_MODULE, 
                mmi.getCodeName() + " > " + mmi.getSpecificationVersion()
            ).iterator().next();
        }
        return new MockModuleInfo(cnb, spec, enabled, arr);
    }

    @Override
    public String getCodeNameBase() {
        return cnb;
    }

    @Override
    public int getCodeNameRelease() {
        return -1;
    }

    @Override
    public String getCodeName() {
        return cnb;
    }

    @Override
    public SpecificationVersion getSpecificationVersion() {
        return new SpecificationVersion(spec);
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public Object getAttribute(String attr) {
        return null;
    }

    @Override
    public Object getLocalizedAttribute(String attr) {
        return null;
    }

    @Override
    public Set<Dependency> getDependencies() {
        return new HashSet<Dependency>(Arrays.asList(deps));
    }

    @Override
    public boolean owns(Class<?> clazz) {
        return false;
    }

    UpdateItem toUpdateItem(String version) throws MalformedURLException {
        Manifest man = new Manifest();
        man.getMainAttributes().putValue("OpenIDE-Module", cnb);
        man.getMainAttributes().putValue("OpenIDE-Module-Specification-Version", version);
        return UpdateItem.createModule(
            getCodeNameBase(), 
            version, 
            new URL("http://unknown.com"), 
            "author", 
            "333", 
            "http://unknown.com", 
            "10/12/2010", 
            "mycategory", 
            man, 
            false, 
            false, 
            false, 
            false, 
            "unknown", 
            UpdateLicense.createUpdateLicense("mine", "use!")
        );
    }

}
