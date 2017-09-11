/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
