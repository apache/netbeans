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

package org.netbeans.modules.web.freeform;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Collections;
import java.util.Map;
import org.netbeans.modules.j2ee.dd.api.web.WebAppMetadata;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.openide.filesystems.FileStateInvalidException;
import org.w3c.dom.Element;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileObject;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.j2ee.dd.spi.MetadataUnit;
import org.netbeans.modules.j2ee.dd.spi.web.WebAppMetadataModelFactory;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.spi.webmodule.WebModuleFactory;
import org.netbeans.modules.web.spi.webmodule.WebModuleProvider;
import org.netbeans.modules.web.spi.webmodule.WebModuleImplementation;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.util.Mutex;
import org.openide.xml.XMLUtil;

/**
 * Web module implementation on top of freeform project.
 *
 * @author  Pavel Buzek
 */
public class WebModules implements WebModuleProvider, AntProjectListener, ClassPathProvider {
    
    private List<FFWebModule> modules;
    private Map<FFWebModule, WebModule> cache;
    private final Project project;
    private final AntProjectHelper helper;
    private final PropertyEvaluator evaluator;
    private final AuxiliaryConfiguration aux;
    private MetadataModel<WebAppMetadata> webAppMetadataModel;
    
    public WebModules (Project project, AntProjectHelper helper, PropertyEvaluator evaluator, AuxiliaryConfiguration aux) {
        assert project != null;
        this.project = project;
        this.helper = helper;
        this.evaluator = evaluator;
        this.aux = aux;
        helper.addAntProjectListener(this);
    }
    
    public WebModule findWebModule (final FileObject file) {
        Project owner = FileOwnerQuery.getOwner (file);
        if (project.equals (owner)) {
            // read modules under project READ access to prevent issues like #119734
            return ProjectManager.mutex().readAccess(new Mutex.Action<WebModule>() {
                public WebModule run() {
                    synchronized (WebModules.this) {
                        List<FFWebModule> mods = getModules();
                        for (FFWebModule ffwm : mods) {
                            if (ffwm.contains (file)) {
                                WebModule wm = cache.get (ffwm);
                                if (wm == null) {
                                    wm = WebModuleFactory.createWebModule (ffwm);
                                    cache.put (ffwm, wm);
                                }
                                return wm;
                            }
                        }
                        return null;
                    }
                }});
        }
        return null;
    }

    public ClassPath findClassPath (final FileObject file, final String type) {
        Project owner = FileOwnerQuery.getOwner (file);
        if (owner != null && owner.equals (project)) {
            // read modules under project READ access to prevent issues like #119734
            return ProjectManager.mutex().readAccess(new Mutex.Action<ClassPath>() {
                public ClassPath run() {
                    synchronized (WebModules.this) {
                        List<FFWebModule> mods = getModules();
                        for (FFWebModule ffwm : mods) {
                            if (ffwm.contains (file)) {
                                return ffwm.findClassPath (file, type);
                            }
                        }
                    }
                    return null;
                }
            });
        }
        return null;
    }
    
    private synchronized List<FFWebModule> getModules()
    {
        if (modules == null) {
            modules = readAuxData();
            cache = new HashMap<FFWebModule, WebModule>();
        }
        return modules;
    }
    
    private List<FFWebModule> readAuxData () {
        List<FFWebModule> mods = new ArrayList<FFWebModule>();
        Element web = aux.getConfigurationFragment(WebProjectNature.EL_WEB, WebProjectNature.NS_WEB_2, true);
        if (web == null) {
            return mods;
        }
        List<Element> webModules = XMLUtil.findSubElements(web);
        Iterator<Element> it = webModules.iterator();
        while (it.hasNext()) {
            Element webModulesEl = it.next();
            assert webModulesEl.getLocalName().equals("web-module") : webModulesEl;
            FileObject docRootFO = getFile (webModulesEl, "doc-root"); //NOI18N
            Element j2eeSpecEl = XMLUtil.findElement (webModulesEl, "j2ee-spec-level", WebProjectNature.NS_WEB_2);
            String j2eeSpec = j2eeSpecEl == null ? null : evaluator.evaluate (XMLUtil.findText (j2eeSpecEl));
            Element contextPathEl = XMLUtil.findElement (webModulesEl, "context-path", WebProjectNature.NS_WEB_2);
            String contextPathText = contextPathEl == null ? null : XMLUtil.findText (contextPathEl);
            String contextPath = contextPathText == null ? null : evaluator.evaluate (contextPathText);
            Element classpathEl = XMLUtil.findElement (webModulesEl, "classpath", WebProjectNature.NS_WEB_2);
            FileObject [] sources = getSources ();
            ClassPath cp = classpathEl == null ? null : createClasspath (classpathEl, sources);
            Element webInfEl = XMLUtil.findElement (webModulesEl, "web-inf", WebProjectNature.NS_WEB_2);
            FileObject webInf = null;
            if (webInfEl != null) {
                webInf = getFile (webModulesEl, "web-inf"); //NOI18N
            }
            mods.add (new FFWebModule (docRootFO, j2eeSpec, contextPath, sources, cp, webInf));
        }
        return mods;
    }
    
    private FileObject getFile (Element parent, String fileElName) {
        Element el = XMLUtil.findElement (parent, fileElName, WebProjectNature.NS_WEB_2);
        String fname = XMLUtil.findText (el);
        if (fname == null) {
            // empty element => cannot find fileobject
            return null;
        }
        String locationEval = evaluator.evaluate(fname);
        if (locationEval != null) {
            File locationFile = helper.resolveFile(locationEval);
            return FileUtil.toFileObject(locationFile);
        }
        return null;
    }

    private FileObject [] getSources () {
        SourceGroup sg [] = ProjectUtils.getSources (project).getSourceGroups (JavaProjectConstants.SOURCES_TYPE_JAVA);
        Set<FileObject> srcRootSet = new HashSet<FileObject>();
        for (int i = 0; i < sg.length; i++) {
            URL entry = sg[i].getRootFolder().toURL();
            // There is important calling this. Withouth calling this, will not work java cc in Jsp editor correctly.
            SourceForBinaryQuery.Result res = SourceForBinaryQuery.findSourceRoots (entry);
            FileObject srcForBin [] = res.getRoots ();
            for (int j = 0; j < srcForBin.length; j++) {
                srcRootSet.add (srcForBin [j]);
            }
        }
        
        FileObject[] roots = new FileObject [sg.length];
        for (int i = 0; i < sg.length; i++) {
            roots[i] = sg[i].getRootFolder();
        }
        return roots;
    }
    
    /**
     * Create a classpath from a &lt;classpath&gt; element.
     */
    private ClassPath createClasspath(Element classpathEl, FileObject[] sources) {
//        System.out.println("creating classpath for " + classpathEl);
        String cp = XMLUtil.findText(classpathEl);
        if (cp == null) {
            cp = "";
        }
        String cpEval = evaluator.evaluate(cp);
        if (cpEval == null) {
            return null;
        }
        String[] path = PropertyUtils.tokenizePath(cpEval);
        Set<File> entries = new HashSet<File>();
        for (int i = 0; i < path.length; i++) {
            entries.add(helper.resolveFile(path[i]));
        }
        if (entries.size() == 0) {
            // if the classpath element was empty then the classpath
            // should contain all source roots
            for (int i = 0; i < sources.length; i++) {
                entries.add(FileUtil.toFile(sources[i]));
            }
        }
        URL[] pathURL = new URL[entries.size()];
        int i = 0;
        for (File entryFile : entries) {
            URL entry;
            try {
                entry = entryFile.toURI().toURL();
                if (FileUtil.isArchiveFile(entry)) {
                    entry = FileUtil.getArchiveRoot(entry);
                } else {
                    String s = entry.toExternalForm();
                    if (!s.endsWith("/")) { // NOI18N
                        // Folder which is not built.
                        entry = new URL(s + '/');
                    }
                }
            } catch (MalformedURLException x) {
                throw new AssertionError(x);
            }
            pathURL[i++] = entry;
        }
        return ClassPathSupport.createClassPath(pathURL);
    }
    
    public synchronized void configurationXmlChanged(AntProjectEvent ev) {
        // reset modules list; will be recreated next time somebody
        // asks for module or classpath
        modules = null;
    }
    
    public void propertiesChanged(AntProjectEvent ev) {
        // ignore
    }
    
    private final class FFWebModule implements WebModuleImplementation {
        
        public static final String FOLDER_WEB_INF = "WEB-INF";//NOI18N
        public static final String FILE_DD        = "web.xml";//NOI18N
    
        private final FileObject docRootFO;
        private final FileObject [] sourcesFOs;
        private final ClassPath webClassPath;
        private final ClassPath javaSourcesClassPath;
        private final Map<String, ClassPath> composedClassPath = new HashMap<String, ClassPath>();
        private final String j2eeSpec;
        private final String contextPath;
        private FileObject webInf;
        private ClassPath compileClasspath;
        
        FFWebModule (FileObject docRootFO, String j2eeSpec, String contextPath, FileObject sourcesFOs[], ClassPath classPath, FileObject webInf) {
            this.docRootFO = docRootFO;
            this.j2eeSpec = j2eeSpec;
            this.contextPath = (contextPath == null ? "" : contextPath);
            this.sourcesFOs = sourcesFOs;
            this.compileClasspath = classPath;
            this.webClassPath = (classPath ==  null ? 
                ClassPathSupport.createClassPath(Collections.<PathResourceImplementation>emptyList()) :
                classPath);
            this.webInf = webInf;
            javaSourcesClassPath = (sourcesFOs == null ? 
                ClassPathSupport.createClassPath(Collections.<PathResourceImplementation>emptyList()) :
                ClassPathSupport.createClassPath(sourcesFOs)); 
        }
        
        boolean contains (FileObject fo) {
            if (docRootFO == null) {
                return false;
            }
            if (docRootFO == fo || FileUtil.isParentOf (docRootFO , fo))
                return true;
            for (int i = 0; i < sourcesFOs.length; i++) {
                if (sourcesFOs [i] == fo || FileUtil.isParentOf (sourcesFOs [i], fo))
                    return true;
            }
            return false;
        }
        
        public FileObject getDocumentBase () {
            return docRootFO;
        }
        
        public ClassPath findClassPath(FileObject file, String type) {
           // because of composedClassPath, caller has to do: synchronized(this){}
           assert Thread.holdsLock(WebModules.this);
           
           int fileType = getType(file);
           if (fileType == 0) {
               if (!type.equals(ClassPath.SOURCE)) {
                   return null;
               }
               return javaSourcesClassPath;
            } else if (fileType == 1) {
                ClassPath classPath = composedClassPath.get(type);
                if (classPath != null) {
                    return classPath;
                }
                Set<FileObject> all = new HashSet<FileObject>();
                FileObject[] javaRoots = null;
                for (int i = 0; i < sourcesFOs.length; i++) {
                    ClassPath cp = ClassPath.getClassPath(sourcesFOs[i], type);
                    if (cp != null) {
                        javaRoots = cp.getRoots();
                        for (int j = 0; j < javaRoots.length; j++) {
                            if (!all.contains(javaRoots[j])) {
                                all.add(javaRoots[j]);
                            }
                        }
                    }
                }
                
                // #122200
                if (all.isEmpty() && ClassPath.BOOT.equals(type)) {
                    // we don't have any possibility how to find out which source level/platform should be used
                    //  so get the actual platform
                    ClassPath bootCP = JavaPlatformManager.getDefault().getDefaultPlatform().getBootstrapLibraries();
                    all.addAll(Arrays.asList(bootCP.getRoots()));
                }

                for (int i = 0; i < webClassPath.getRoots().length; i++) {
                    if (!all.contains(webClassPath.getRoots()[i])) {
                        all.add(webClassPath.getRoots()[i]);
                    }
                }

                FileObject[] roots = new FileObject[all.size()];
                int i = 0;
                for (Iterator<FileObject> it = all.iterator(); it.hasNext();) {
                    roots[i++] = it.next();
                }

                classPath = ClassPathSupport.createClassPath(roots);
                composedClassPath.put(type, classPath);
                return classPath;
            }
            return webClassPath;
        }
        
        public String getJ2eePlatformVersion () {
            return j2eeSpec;
        }
        
        public String getContextPath () {
            return contextPath;
        }
        
        public String toString () {
            StringBuffer sb = new StringBuffer ("web module in freeform project" +
                "\n\tdoc root:" + docRootFO.getPath () + 
                "\n\tcontext path:" + contextPath +
                "\n\tj2ee version:" + j2eeSpec);
            for (int i = 0; i < sourcesFOs.length; i++) {
                sb.append ("\n\tsource root:" + sourcesFOs [i].getPath ());
            }
            return sb.toString ();
        }
        
        public FileObject getDeploymentDescriptor () {
            FileObject winf = getWebInf ();
            if (winf == null) {
                return null;
            }
            return winf.getFileObject (FILE_DD);
        }
        
        public FileObject getWebInf () {
            //NetBeans 5.x and older projects (WEB-INF is placed under Web Pages)
            if (webInf == null && getDocumentBase() != null) {
                webInf = getDocumentBase().getFileObject(FOLDER_WEB_INF);
            }
            return webInf;
        }
        
        @Deprecated
        public FileObject[] getJavaSources() {
            return sourcesFOs;
        }
        
        public MetadataModel<WebAppMetadata> getMetadataModel() {
            if (webAppMetadataModel == null) {
                FileObject ddFO = getDeploymentDescriptor();
                File ddFile = ddFO != null ? FileUtil.toFile(ddFO) : null;
                MetadataUnit metadataUnit = MetadataUnit.create(
                    JavaPlatformManager.getDefault().getDefaultPlatform().getBootstrapLibraries(),
                    compileClasspath,
                    javaSourcesClassPath,
                    // XXX: add listening on deplymentDescriptor
                    ddFile);
                webAppMetadataModel = WebAppMetadataModelFactory.createMetadataModel(metadataUnit, true);
            }
            return webAppMetadataModel;
        }
        
        /**
         * Find what a given file represents.
         * @param file a file in the project
         * @return one of: <dl>
         *         <dt>0</dt> <dd>java source</dd>
         *         <dt>1</dt> <dd>web pages</dd>
         *         <dt>-1</dt> <dd>something else</dd>
         *         </dl>
         */
        private int getType(FileObject file) {
            //test java source roots
            for (int i=0; i < sourcesFOs.length; i++) {
                FileObject root = sourcesFOs[i];
                if (root.equals(file) || FileUtil.isParentOf(root, file)) {
                    return 0;
                }
            } 
            
            //test if the file is under the web root
            FileObject dir = getDocumentBase();
            if (dir != null && (dir.equals(file) || FileUtil.isParentOf(dir,file))) {
                return 1;
            }
            
            return -1;
        }
    }
}
