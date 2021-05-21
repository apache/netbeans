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
                set = new HashSet<>(results);
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
