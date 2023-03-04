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

package org.netbeans.modules.apisupport.project.suite;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.apisupport.project.spi.BrandingSupport;
import org.netbeans.modules.apisupport.project.universe.ModuleEntry;
import org.netbeans.modules.apisupport.project.universe.ModuleList;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.netbeans.spi.project.support.ant.PropertyUtils;

class SuiteBrandingSupport extends BrandingSupport {

    private final SuiteProject project;
    private NbPlatform platform;

    SuiteBrandingSupport(SuiteProject project, String brandingPath, Locale locale) throws IOException {
        super(project, brandingPath);
        this.project = project;
        this.locale = locale;
    }

    private NbPlatform getActivePlatform() {
        return project.getPlatform(true);
    }

    @Override protected BrandableModule findBrandableModule(String moduleCodeNameBase) {
        NbPlatform p = getActivePlatform();
        if (p == null) {
            return null;
        }
        ModuleEntry me = p.getModule(moduleCodeNameBase);
        return me != null ? new ModuleEntryWrapper(me) : null;
    }

    @Override public Set<File> getBrandableJars() {
        NbPlatform platf = getActivePlatform();
        Set<ModuleEntry> modules = platf.getModules();
        Set<File> jars = new HashSet<File>(modules.size());
        for (ModuleEntry m : modules) {
            File j = m.getJarLocation();
            if (j != null) {
                jars.add(j);
            }
        }
        return jars;
    }

    @Override protected Set<BrandableModule> loadModules() {
        NbPlatform newPlatform = getActivePlatform();
        if (newPlatform == null) {
            return null;
        }
        if (newPlatform.equals(platform)) {
            return null;
        }
        platform = newPlatform;
        Set<BrandableModule> r = new HashSet<BrandableModule>();
        for (ModuleEntry me : platform.getModules()) {
            r.add(new ModuleEntryWrapper(me));
        }
        return r;
    }

    @Override protected Map<String,String> localizingBundle(BrandableModule wrapper) {
        ModuleEntry mEntry = ((ModuleEntryWrapper) wrapper).mEntry;
        return ModuleList.loadBundleInfo(mEntry.getSourceLocation()).toEditableProperties();
    }

    private static class ModuleEntryWrapper implements BrandableModule {

        private final ModuleEntry mEntry;

        ModuleEntryWrapper(ModuleEntry mEntry) {
            this.mEntry = mEntry;
        }
        
        @Override public String getCodeNameBase() {
            return mEntry.getCodeNameBase();
        }

        @Override public File getJarLocation() {
            return mEntry.getJarLocation();
        }

        @Override public String getRelativePath() {
            return PropertyUtils.relativizeFile(mEntry.getClusterDirectory(), mEntry.getJarLocation());
        }

        @Override public boolean equals(Object obj) {
            if (!(obj instanceof ModuleEntryWrapper)) {
                return false;
            }
            return mEntry == ((ModuleEntryWrapper) obj).mEntry;
        }

        @Override public int hashCode() {
            return mEntry.hashCode();
        }

        @Override public String toString() {
            return mEntry.toString();
        }

    }

}
