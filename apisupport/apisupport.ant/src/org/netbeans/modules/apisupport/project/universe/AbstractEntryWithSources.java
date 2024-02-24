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
        return deps.toArray(new String[0]);
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
