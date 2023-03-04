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
import org.netbeans.modules.apisupport.project.ApisupportAntUtils;
import org.netbeans.modules.apisupport.project.api.ManifestManager;
import org.openide.filesystems.FileUtil;

final class NetBeansOrgEntry extends AbstractEntryWithSources {

    private final File nball;
    private final String cnb;
    private final String path;
    private final File cluster;
    private final String module;
    private final String cpext;
    private final String releaseVersion;
    private final String[] providedTokens;
    private final ManifestManager.PackageExport[] publicPackages;
    private final String[] friends;
    private final boolean deprecated;
    private URL javadoc;
    private File sourceLocation;
    
    public NetBeansOrgEntry(File nball, String cnb, String path, File cluster,
            String module, String cpext, String releaseVersion,
            String[] providedTokens, ManifestManager.PackageExport[] publicPackages,
            String[] friends, boolean deprecated, String src) {
        super(src);
        this.nball = nball;
        this.cnb = cnb;
        this.path = path;
        this.cluster = cluster;
        this.module = module;
        this.cpext = cpext;
        this.releaseVersion = releaseVersion;
        this.providedTokens = providedTokens;
        this.publicPackages = publicPackages;
        this.friends = friends;
        this.deprecated = deprecated;
    }
    
    public File getSourceLocation() {
        if (sourceLocation == null)
            sourceLocation = FileUtil.normalizeFile(new File(nball, path.replace('/', File.separatorChar)));
        return sourceLocation;
    }
    
    public String getNetBeansOrgPath() {
        return path;
    }
    
    public File getJarLocation() {
        return new File(getClusterDirectory(), module.replace('/', File.separatorChar));
    }
    
    public String getCodeNameBase() {
        return cnb;
    }
    
    public File getClusterDirectory() {
        return cluster;
    }
    
    public String getClassPathExtensions() {
        return cpext;
    }
    
    public String getReleaseVersion() {
        return releaseVersion;
    }
    
    public String[] getProvidedTokens() {
        return providedTokens;
    }
    
    public ManifestManager.PackageExport[] getPublicPackages() {
        return publicPackages;
    }
    
    public boolean isDeclaredAsFriend(String cnb) {
        return isDeclaredAsFriend(friends, cnb);
    }
    
    public boolean isDeprecated() {
        return deprecated;
    }

    public @Override String toString() {
        return "NetBeansOrgEntry[" + getSourceLocation() + "]"; // NOI18N
    }

    public URL getJavadoc(final NbPlatform platform) {
        if (javadoc == null)
            javadoc = findJavadocForNetBeansOrgModules(this, ModuleList.findNetBeansOrgDestDir(nball));
        return javadoc;
    }

    /**
     * Find Javadoc URL for NetBeans.org modules. May return <code>null</code>.
     */
    static URL findJavadocForNetBeansOrgModules(final ModuleEntry entry, File destDir) {
        File nbOrg = null;
        if (destDir.getParent() != null) {
            nbOrg = destDir.getParentFile().getParentFile();
        }
        if (nbOrg == null) {
            throw new IllegalArgumentException("ModuleEntry " + entry +  // NOI18N
                    " doesn't represent nb.org module"); // NOI18N
        }
        File builtJavadoc = new File(nbOrg, "nbbuild/build/javadoc"); // NOI18N
        URL[] javadocURLs = null;
        if (builtJavadoc.exists()) {
            File[] javadocs = builtJavadoc.listFiles();
            javadocURLs = new URL[javadocs.length];
            for (int i = 0; i < javadocs.length; i++) {
                javadocURLs[i] = FileUtil.urlForArchiveOrDir(javadocs[i]);
            }
        }
        return javadocURLs == null ? null : ApisupportAntUtils.findJavadocURL(
                entry.getCodeNameBase().replace('.', '-'), javadocURLs);
    }
}
