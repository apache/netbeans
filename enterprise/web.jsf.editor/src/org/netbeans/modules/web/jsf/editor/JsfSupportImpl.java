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
package org.netbeans.modules.web.jsf.editor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.jsf.api.facesmodel.JsfVersionUtils;
import org.netbeans.modules.web.jsf.editor.facelets.FaceletsLibrarySupport;
import org.netbeans.modules.web.jsf.editor.index.JsfIndex;
import org.netbeans.modules.web.jsfapi.api.JsfSupport;
import org.netbeans.modules.web.jsfapi.api.JsfVersion;
import org.netbeans.modules.web.jsfapi.api.Library;
import org.netbeans.modules.web.jsfapi.api.NamespaceUtils;
import org.netbeans.modules.web.jsfapi.spi.JsfSupportProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 * per web-module instance
 *
 * @author marekfukala
 */
public class JsfSupportImpl implements JsfSupport {

	private static final Logger LOG = Logger.getLogger(JsfSupportImpl.class.getSimpleName());

    public static JsfSupportImpl findFor(Source source) {
        return getOwnImplementation(JsfSupportProvider.get(source));
    }

    public static JsfSupportImpl findFor(FileObject file) {
        return getOwnImplementation(JsfSupportProvider.get(file));
    }

    private static JsfSupportImpl getOwnImplementation(JsfSupport instance) {
        if(instance instanceof JsfSupportImpl) {
            return (JsfSupportImpl)instance;
        } else {
            //the jsf editor requires its own implementation of the JsfSupport for the time being
            return null;
        }
    }

     //synchronized in JsfSupportProvider.get(Project project)
    static JsfSupportImpl findForProject(Project project) {
        WebModule webModule = WebModule.getWebModule(project.getProjectDirectory());
        if(webModule != null) {
            // #217213 - prevent NPE:
            if (webModule.getDocumentBase() == null) {
                LOG.log(Level.INFO, "project ''{0}'' does not have valid documentBase", project); // NOI18N
                return null;
            }
            //web project
            ClassPath sourceCP = ClassPath.getClassPath(webModule.getDocumentBase(), ClassPath.SOURCE);
            ClassPath compileCP = ClassPath.getClassPath(webModule.getDocumentBase(), ClassPath.COMPILE);
            ClassPath executeCP = ClassPath.getClassPath(webModule.getDocumentBase(), ClassPath.EXECUTE);
            if (!validateUpdateClasspaths(project, webModule, sourceCP, compileCP, executeCP)) {
                return null;
            }
            ClassPath bootCP = ClassPath.getClassPath(webModule.getDocumentBase(), ClassPath.BOOT);

            return new JsfSupportImpl(project, webModule, sourceCP, compileCP, executeCP, bootCP);
        } else {
            //non-web project
            Sources sources = ProjectUtils.getSources(project);
            SourceGroup[] sourceGroups = sources.getSourceGroups( JavaProjectConstants.SOURCES_TYPE_JAVA );
            if(sourceGroups.length > 0) {
                Collection<ClassPath> sourceCps = new HashSet<>();
                Collection<ClassPath> compileCps = new HashSet<>();
                Collection<ClassPath> executeCps = new HashSet<>();
                Collection<ClassPath> bootCps = new HashSet<>();
                for(SourceGroup sg : sourceGroups) {
                    sourceCps.add(ClassPath.getClassPath(sg.getRootFolder(), ClassPath.SOURCE));
                    compileCps.add(ClassPath.getClassPath(sg.getRootFolder(), ClassPath.COMPILE));
                    executeCps.add(ClassPath.getClassPath(sg.getRootFolder(), ClassPath.EXECUTE));
                    bootCps.add(ClassPath.getClassPath(sg.getRootFolder(), ClassPath.BOOT));
                }
                return new JsfSupportImpl(project, null,
                        ClassPathSupport.createProxyClassPath(sourceCps.toArray(new ClassPath[]{})),
                        ClassPathSupport.createProxyClassPath(compileCps.toArray(new ClassPath[]{})),
                        ClassPathSupport.createProxyClassPath(executeCps.toArray(new ClassPath[]{})),
                        ClassPathSupport.createProxyClassPath(bootCps.toArray(new ClassPath[]{})));
            }

        }

        //no jsf support for this project
        return null;
    }

    private static boolean validateUpdateClasspaths(Project project, WebModule webModule, ClassPath sourceCP, ClassPath compileCP, ClassPath executeCP) {
        // #217213 - prevent NPE; not sure what's causing it:
        if (compileCP == null) {
            LOG.log(Level.INFO, "project ''{0}'' does not have compilation classpath; documentBase={1}", new Object[]{project, webModule.getDocumentBase()});
            return false;
        }
        // #237991 - prevent NPE; not sure what's causing it:
        if (sourceCP == null) {
            LOG.log(Level.INFO, "project ''{0}'' does not have source classpath; documentBase={1}", new Object[]{project, webModule.getDocumentBase()});
            return false;
        }
        // #236831 - prevent NPE; not sure what's causing it:
        if (executeCP == null) {
            LOG.log(Level.INFO, "project ''{0}'' does not have execution classpath; documentBase={1}", new Object[]{project, webModule.getDocumentBase()});
            return false;
        }
        return true;
    }


    private FaceletsLibrarySupport faceletsLibrarySupport;
    private Project project;
    private WebModule wm;
    private ClassPath sourceClassPath, compileClasspath, executeClassPath, bootClassPath;
    private JsfIndex index;
    private MetadataModel<org.netbeans.modules.web.beans.api.model.WebBeansModel> webBeansModel;
    private MetadataModel<org.netbeans.modules.jakarta.web.beans.api.model.WebBeansModel> webBeansModelJakarta;
    private Lookup lookup;

    private JsfSupportImpl(Project project, WebModule wm, ClassPath sourceClassPath, ClassPath compileClassPath, ClassPath executeClassPath, ClassPath bootClassPath) {
        this.project = project;
        this.wm = wm;
        this.sourceClassPath = sourceClassPath;
        this.compileClasspath = compileClassPath;
        this.executeClassPath = executeClassPath;
        this.bootClassPath = bootClassPath;
        this.faceletsLibrarySupport = new FaceletsLibrarySupport(this);

        //adds a classpath listener which invalidates the index instance after classpath change
        //and also invalidates the facelets library descriptors and tld caches
        this.compileClasspath.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                synchronized (JsfSupportImpl.this) {
                    index = null;
                }
            }
        });

        JsfVersion jsfVersion = getJsfVersion();
        
        //TODO do it lazy so it creates the web beans model lazily once looked up
        InstanceContent ic = new InstanceContent();
        if(jsfVersion.isAtLeast(JsfVersion.JSF_3_0)){
            webBeansModelJakarta = new org.netbeans.modules.jakarta.web.beans.MetaModelSupport(project).getMetaModel();
            ic.add(webBeansModelJakarta);
        } else {
            webBeansModel = new org.netbeans.modules.web.beans.MetaModelSupport(project).getMetaModel();
            ic.add(webBeansModel);
        }
        
        //init lookup
        this.lookup = new AbstractLookup(ic);
    }

    @Override
    public Project getProject() {
        return project;
    }

    @Override
    public ClassPath getClassPath() {
        return compileClasspath;
    }

    public FileObject[] getClassPathRoots() {
        Collection<FileObject> roots = new ArrayList<>();
        roots.addAll(Arrays.asList(sourceClassPath.getRoots()));
        roots.addAll(Arrays.asList(compileClasspath.getRoots()));
        roots.addAll(Arrays.asList(executeClassPath.getRoots()));
        return roots.toArray(new FileObject[0]);
    }

    /**
     * @return can return null if this supports wraps a project of non-web type.
     */
    @Override
    public WebModule getWebModule() {
        return wm;
    }

    @Override
    public Lookup getLookup() {
        return lookup;
    }

    @Override
    public Library getLibrary(String namespace) {
        return NamespaceUtils.getForNs(faceletsLibrarySupport.getNamespaceLibraryMapping(), namespace);
    }

    /** Library's uri to library map
     * Please note that a composite components library can be preset twice in the values.
     * Once under the declared namespace key and once under the default cc namespace key.
     */
    @Override
    public Map<String, Library> getLibraries() {
        return faceletsLibrarySupport.getNamespaceLibraryMapping();
    }

    public boolean isFileOnClasspath(FileObject file) {
        return sourceClassPath.contains(file)
                || compileClasspath.contains(file)
                || executeClassPath.contains(file);
    }

    /**
     * Called by the JSF indexers.
     */
    public void indexedContentPossiblyChanged() {
        faceletsLibrarySupport.indexedContentPossiblyChanged();
    }

    //garbage methods below, needs cleanup!
    public synchronized JsfIndex getIndex() {
        if(index == null) {
	    this.index = JsfIndex.create(sourceClassPath, compileClasspath, executeClassPath, bootClassPath);
        }
        return this.index;
    }

    public FaceletsLibrarySupport getFaceletsLibrarySupport() {
	return faceletsLibrarySupport;
    }

    public synchronized MetadataModel<org.netbeans.modules.web.beans.api.model.WebBeansModel> getWebBeansModel() {
	return webBeansModel;
    }
    
    public synchronized MetadataModel<org.netbeans.modules.jakarta.web.beans.api.model.WebBeansModel> getJakartaWebBeansModel() {
        return webBeansModelJakarta;
    }

    @Override
    public JsfVersion getJsfVersion() {
        if (wm != null) {
            JsfVersion jsfVersion = JsfVersionUtils.forWebModule(wm);
            if (jsfVersion != null) {
                return jsfVersion;
            }
        }

        return JsfVersion.latest();
    }

    @Override
    public String toString() {
        return String.format("JsfSupportImpl[%s]", getBaseFile().getPath()); //NOI18N
    }

    private FileObject getBaseFile() {
        return wm != null ? wm.getDocumentBase() : project.getProjectDirectory();
    }

}
