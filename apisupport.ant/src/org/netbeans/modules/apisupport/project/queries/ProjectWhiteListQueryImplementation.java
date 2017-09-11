/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.apisupport.project.queries;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.whitelist.WhiteListQuery;
import org.netbeans.modules.apisupport.project.ModuleDependency;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.ProjectXMLManager;
import org.netbeans.spi.whitelist.WhiteListQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakSet;

/**
 *
 * @author mkozeny
 */
//@ProjectServiceProvider(service = WhiteListQueryImplementation.class, projectType="org-netbeans-modules-ant")
public class ProjectWhiteListQueryImplementation implements WhiteListQueryImplementation {

    private NbModuleProject project;
    private SoftReference<TreeSet<String>> cachedPrivatePackages;
    private boolean isCached = false;
    private static final WhiteListQuery.Result OK = new WhiteListQuery.Result();
    private final Set<ProjectWhiteListImplementation> results = Collections.synchronizedSet(new WeakSet<ProjectWhiteListImplementation>());
    private static final RequestProcessor RP = new RequestProcessor(ProjectWhiteListQueryImplementation.class.getName(), 3);

    /**
     * Constructor
     * @param project 
     */
    public ProjectWhiteListQueryImplementation(NbModuleProject project) {
        this.project = project;
    }

    /**
     * Getting white list
     * @param file
     * @return 
     */
    @Override
    public WhiteListImplementation getWhiteList(FileObject file) {

        ProjectXMLManager pxm = new ProjectXMLManager(this.project);
        
        if (System.getProperty("Enable-Whitelist") == null || !System.getProperty("Enable-Whitelist").equals("true")) {
            return null;
        }
        
        TreeSet<String> privatePackages = null;
        
        if(isCached)
        {
           privatePackages = cachedPrivatePackages!=null? cachedPrivatePackages.get():null;
        }
        if(privatePackages==null)
        {
            cachedPrivatePackages = new SoftReference<TreeSet<String>>(privatePackages = calculatePrivatePackageList(pxm, this.project.getProjectDirectory()));
            isCached = true;
        }
        
        fireChangeAllExistingResults(privatePackages);
        
        ProjectWhiteListImplementation pwi = new ProjectWhiteListImplementation(privatePackages);
        results.add(pwi);
        
        return pwi;
    }
    
    private void fireChangeAllExistingResults(final TreeSet<String> privatePackages) {
            final Set<ProjectWhiteListImplementation> set;
            synchronized (results) {
                set = new HashSet(results);
            }

            RP.post(new Runnable() {

                 @Override  
                 public void run() {
                    for (ProjectWhiteListImplementation res : set) {
                        if (res != null) {
                            res.changeData(privatePackages);
                        }
                    }
                }
            });
        }



    private Manifest getManifest(FileObject root) {
        FileObject manifestFo = root.getFileObject("META-INF/MANIFEST.MF");
        if (manifestFo != null) {
            InputStream is = null;
            try {
                is = manifestFo.getInputStream();
                return new Manifest(is);
            } catch (IOException ex) {
            }

        }
        return null;
    }

    private TreeSet<String> getAllPackages(FileObject root) {

        TreeSet<String> toRet = new TreeSet<String>();
        processFolder(root, root, toRet);
        toRet.remove("");

        return toRet;
    }

    private void processFolder(FileObject root, FileObject folder, TreeSet<String> foundPackages) {

        Enumeration<? extends FileObject> it = folder.getData(false);
        while (it.hasMoreElements()) {
            FileObject fileObject = it.nextElement();
            if (fileObject.hasExt("class")) {
                foundPackages.add(folder.getPath().replace('/', '.'));
                break;
            }
        }

        it = folder.getFolders(false);

        while (it.hasMoreElements()) {
            FileObject fileObject = it.nextElement();
            processFolder(root, fileObject, foundPackages);

        }
    }
    
    private TreeSet<String> calculatePrivatePackageList(ProjectXMLManager pxm, FileObject project)
    {
        TreeSet<String> privatePackages = new TreeSet<String>();
        TreeSet<String> eqPublicPackages = new TreeSet<String>();
        TreeSet<String> subPublicPackages = new TreeSet<String>();
        TreeSet<String> allPackages = new TreeSet<String>();
        
        try {
            for (ModuleDependency depIter : pxm.getDirectDependencies()) {
                if (depIter.hasImplementationDependency()) {
                    for (String pkgNameIter : depIter.getModuleEntry().getAllPackageNames()) {
                        eqPublicPackages.add(pkgNameIter);
                    }
                }
            }
        } catch (IOException ex) {
        }

        ClassPath compileClassPath = ClassPath.getClassPath(project, ClassPath.COMPILE);
        
        for (FileObject rootIter : compileClassPath.getRoots()) {
            String publicPackagesStr = null;
            Manifest mf = getManifest(rootIter);

            if (mf != null && mf.getMainAttributes() != null) {
                Attributes attrs = mf.getMainAttributes();

                publicPackagesStr = attrs.getValue("OpenIDE-Module-Public-Packages");
                if(publicPackagesStr != null && !"".equals(publicPackagesStr)) {
                    publicPackagesStr = publicPackagesStr.replaceAll(" ", "");
                }

                if (publicPackagesStr != null && !"-".equals(publicPackagesStr)) {
                    StringTokenizer tokenizer = new StringTokenizer(publicPackagesStr, ",");
                    while (tokenizer.hasMoreElements()) {
                        String packageIter = tokenizer.nextToken();
                        if (packageIter.endsWith(".**")) {
                            String sub = packageIter.substring(0, packageIter.length() - ".**".length());
                            subPublicPackages.add(sub);
                        } else if (packageIter.endsWith(".*")) {
                            String eq = packageIter.substring(0, packageIter.length() - ".*".length());
                            if (!eqPublicPackages.contains(packageIter)) {
                                eqPublicPackages.add(eq);
                            }
                        }
                    }
                }
                allPackages.addAll(getAllPackages(rootIter));
            }
        }
        
        

        for (String allPkgIter : allPackages) {
            boolean contains = false;
            for (String publicPkgIter : eqPublicPackages) {
                if (allPkgIter.equals(publicPkgIter)) {
                    contains = true;
                    break;
                }
            }

            if (!contains) {
                for (String publicPkgIter : subPublicPackages) {
                    if (allPkgIter.startsWith(publicPkgIter)) {
                        contains = true;
                        break;
                    }
                }
            }
            if (!contains) {
                privatePackages.add(allPkgIter);
            }

        }
        
        ClassPath bootClassPath = ClassPath.getClassPath(project, ClassPath.BOOT);
        TreeSet<String> bootClassPathPkgs = new TreeSet<String>();
        for (FileObject rootIter : bootClassPath.getRoots()) {
            bootClassPathPkgs.addAll(getAllPackages(rootIter));
        }
        
        privatePackages.removeAll(bootClassPathPkgs);
        return privatePackages;
    }
    
    private static class ProjectWhiteListImplementation implements WhiteListImplementation
    {
        
        private TreeSet<String> privatePackages;
        
        private final List<ChangeListener> listeners = new ArrayList<ChangeListener>();
        
        private final Object IMPL_LOCK = new Object();
        
        public ProjectWhiteListImplementation(TreeSet<String> privatePackages)
        {
            this.privatePackages = privatePackages;
        }

        @Override
        public WhiteListQuery.Result check(ElementHandle<?> element, WhiteListQuery.Operation operation) {
            if (!operation.equals(WhiteListQuery.Operation.USAGE)) {
                return OK;
            }

                if (element != null && (element.getKind().isClass() || element.getKind().isInterface())) {
                    String qualifiedName = element.getQualifiedName();
                    if (qualifiedName!=null && qualifiedName.lastIndexOf(".") > 0) {
                        qualifiedName = qualifiedName.substring(0, qualifiedName.lastIndexOf("."));
                        synchronized (IMPL_LOCK) {
                            if (privatePackages.contains(qualifiedName)) {
                                List<WhiteListQuery.RuleDescription> descs = new ArrayList<WhiteListQuery.RuleDescription>();
                                descs.add(new WhiteListQuery.RuleDescription("Private package dependency access", "Element comes from private package of spec version dependency", null));
                                return new WhiteListQuery.Result(descs);
                            }
                        }
                    }
                }
                return OK;
        }

        @Override
        public void addChangeListener(ChangeListener listener) {
            this.listeners.add(listener);
        }

        @Override
        public void removeChangeListener(ChangeListener listener) {
            this.listeners.remove(listener);
        }
        
        public void changeData(@NonNull TreeSet<String> privatePackages) {
            synchronized (IMPL_LOCK) {
                this.privatePackages = privatePackages;
            }

            ArrayList<ChangeListener> changes = new ArrayList<ChangeListener>();

            synchronized (listeners) {
                changes.addAll(listeners);
            }

            for (ChangeListener change : changes) {
                change.stateChanged(new ChangeEvent(this));
            }

        }
    }
}
