/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
        runDependencies = deps.toArray(new String[deps.size()]);
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
