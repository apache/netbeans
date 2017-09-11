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
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.ApisupportAntUtils;
import org.netbeans.modules.apisupport.project.api.ManifestManager;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.api.Util;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Element;

abstract class AbstractEntryWithSources extends AbstractEntry {
    
    private LocalizedBundleInfo bundleInfo;
    private Set<String> allPackageNames;
    private final String src;
    
    protected AbstractEntryWithSources(final String src) {
        this.src = src;
    }
    
    protected LocalizedBundleInfo getBundleInfo() {
        if (bundleInfo == null) {
            bundleInfo = ModuleList.loadBundleInfo(getSourceLocation());
        }
        return bundleInfo;
    }
    
    protected Set<String> computePublicClassNamesInMainModule() throws IOException {
        Set<String> result = new HashSet<String>();
        File srcF = new File(getSourceLocation(), src);
        for (ManifestManager.PackageExport p : getPublicPackages()) {
            String pkg = p.getPackage();
            scanForClasses(result, pkg, new File(srcF, pkg.replace('.', File.separatorChar)), p.isRecursive());
        }
        return result;
    }
    
    public synchronized Set<String> getAllPackageNames() {
        if (allPackageNames == null) {
            allPackageNames = ApisupportAntUtils.scanProjectForPackageNames(getSourceLocation());
        }
        return allPackageNames;
    }

    private FileObject sourceFO;
    private FileObject getSourceLocationFileObject() {
        if (sourceFO == null || ! sourceFO.isValid())
            sourceFO = FileUtil.toFileObject(getSourceLocation());
        return sourceFO;
    }
    
    private void scanForClasses(Set<String> result, String pkg, File dir, boolean recurse) throws IOException {
        if (!dir.isDirectory()) {
            return;
        }
        File[] kids = dir.listFiles();
        if (kids == null) {
            throw new IOException(dir.getAbsolutePath());
        }
        for (File kid : kids) {
            String name = kid.getName();
            if (name.endsWith(".java")) { // NOI18N
                String basename = name.substring(0, name.length() - 5);
                result.add(pkg + '.' + basename);
                // no inner classes scanned, too slow
            }
            if (recurse && kid.isDirectory()) {
                scanForClasses(result, pkg + '.' + name, kid, true);
            }
        }
    }

    public String[] getRunDependencies() {
        Set<String> deps = new TreeSet<String>();
        FileObject source = getSourceLocationFileObject();
        if (source == null) { // ??
            return new String[0];
        }
        NbModuleProject project;
        try {
            Project p = ProjectManager.getDefault().findProject(source);
            project = p == null ? null : p.getLookup().lookup(NbModuleProject.class);
        } catch (IOException e) {
            Util.err.notify(ErrorManager.INFORMATIONAL, e);
            return new String[0];
        }
        if (project == null) { // #106351
            return new String[0];
        }
        Element data = project.getPrimaryConfigurationData();
        Element moduleDependencies = XMLUtil.findElement(data,
            "module-dependencies", NbModuleProject.NAMESPACE_SHARED); // NOI18N
        assert moduleDependencies != null : "Malformed metadata in " + project;
        for (Element dep : XMLUtil.findSubElements(moduleDependencies)) {
            if (XMLUtil.findElement(dep, "run-dependency", // NOI18N
                    NbModuleProject.NAMESPACE_SHARED) == null) {
                continue;
            }
            Element cnbEl = XMLUtil.findElement(dep, "code-name-base", // NOI18N
                NbModuleProject.NAMESPACE_SHARED);
            String cnb = XMLUtil.findText(cnbEl);
            deps.add(cnb);
        }
        return deps.toArray(new String[deps.size()]);
    }

    public String getSpecificationVersion() {
        FileObject source = getSourceLocationFileObject();
        if (source != null) {
            NbModuleProject project;
            try {
                project = (NbModuleProject) ProjectManager.getDefault().findProject(source);
                if (project != null) {
                    return project.getSpecVersion();
                }
            } catch (IOException e) {
                Util.err.notify(ErrorManager.INFORMATIONAL, e);
            }
        }
        return null;
    }

}
