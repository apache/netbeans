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

package org.netbeans.modules.apisupport.project.universe;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import org.netbeans.modules.apisupport.project.ApisupportAntUtils;
import org.netbeans.modules.apisupport.project.api.ManifestManager;
import org.netbeans.modules.apisupport.project.api.Util;
import org.openide.modules.Dependency;

/**
 * Common predecessor for binary entries from platform
 * and external clusters.
 * @author Richard Michalsky
 */
abstract class AbstractBinaryEntry extends AbstractEntry {
    private final String cnb;
    private final File jar;
    private final String cpext;
    private final File clusterDir;
    private final String releaseVersion;
    private final String specVersion;
    private final String[] providedTokens;
    private LocalizedBundleInfo bundleInfo;
    private final ManifestManager.PackageExport[] publicPackages;
    private final String[] friends;
    private final boolean deprecated;
    private final String[] runDependencies;
    private Set<String> allPackageNames;

    AbstractBinaryEntry(String cnb, File jar, File[] exts, File clusterDir,
            String releaseVersion, String specVersion, String[] providedTokens,
            ManifestManager.PackageExport[] publicPackages, String[] friends,
            boolean deprecated, Set<Dependency> moduleDependencies) {
        this.cnb = cnb;
        this.jar = jar;
        this.clusterDir = clusterDir;
        StringBuilder _cpext = new StringBuilder();
        for (int i = 0; i < exts.length; i++) {
            _cpext.append(File.pathSeparatorChar);
            _cpext.append(exts[i].getAbsolutePath());
        }
        cpext = _cpext.toString();
        this.releaseVersion = releaseVersion;
        this.specVersion = specVersion;
        this.providedTokens = providedTokens;
        this.publicPackages = publicPackages;
        this.friends = friends;
        this.deprecated = deprecated;
        Set<String> deps = new TreeSet<String>();
        for (Dependency d : moduleDependencies) {
            String codename = d.getName();
            int slash = codename.lastIndexOf('/');
            if (slash == -1) {
                deps.add(codename);
            } else {
                deps.add(codename.substring(0, slash));
            }
        }
        runDependencies = deps.toArray(new String[0]);
    }

    public String getNetBeansOrgPath() {
        return null;
    }

    public File getJarLocation() {
        return jar;
    }

    public String getCodeNameBase() {
        return cnb;
    }

    public File getClusterDirectory() {
        return clusterDir;
    }

    public String getClassPathExtensions() {
        return cpext;
    }

    public String getReleaseVersion() {
        return releaseVersion;
    }

    public String getSpecificationVersion() {
        return specVersion;
    }

    public String[] getProvidedTokens() {
        return providedTokens;
    }

    protected LocalizedBundleInfo getBundleInfo() {
        if (bundleInfo == null) {
            bundleInfo = ApisupportAntUtils.findLocalizedBundleInfoFromJAR(getJarLocation());
            if (bundleInfo == null) {
                bundleInfo = LocalizedBundleInfo.EMPTY;
            }
        }
        return bundleInfo;
    }

    public ManifestManager.PackageExport[] getPublicPackages() {
        return publicPackages;
    }

    public synchronized Set<String> getAllPackageNames() {
        if (allPackageNames == null) {
            allPackageNames = new TreeSet<String>();
            ApisupportAntUtils.scanJarForPackageNames(allPackageNames, getJarLocation());
        }
        return allPackageNames;
    }

    public boolean isDeclaredAsFriend(String cnb) {
        return isDeclaredAsFriend(friends, cnb);
    }

    public boolean isDeprecated() {
        return deprecated;
    }

    protected Set<String> computePublicClassNamesInMainModule() throws IOException {
        Set<String> result = new HashSet<String>();
        scanJarForPublicClassNames(result, jar);
        return result;
    }

    public String[] getRunDependencies() {
        return runDependencies;
    }
}
