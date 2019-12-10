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
package org.netbeans.modules.java.j2semodule;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.JavaClassPathConstants;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.persistence.api.EntityClassScope;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.modules.j2ee.persistence.api.PersistenceScope;
import org.netbeans.modules.j2ee.persistence.api.PersistenceScopes;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappingsMetadata;
import org.netbeans.modules.j2ee.persistence.spi.EntityClassScopeFactory;
import org.netbeans.modules.j2ee.persistence.spi.EntityClassScopeImplementation;
import org.netbeans.modules.j2ee.persistence.spi.EntityClassScopeProvider;
import org.netbeans.modules.j2ee.persistence.spi.PersistenceLocationProvider;
import org.netbeans.modules.j2ee.persistence.spi.PersistenceScopeFactory;
import org.netbeans.modules.j2ee.persistence.spi.PersistenceScopeImplementation;
import org.netbeans.modules.j2ee.persistence.spi.PersistenceScopeProvider;
import org.netbeans.modules.j2ee.persistence.spi.PersistenceScopesProvider;
import org.netbeans.modules.j2ee.persistence.spi.support.EntityMappingsMetadataModelHelper;
import org.netbeans.modules.j2ee.persistence.spi.support.PersistenceScopesHelper;
import org.netbeans.modules.java.api.common.classpath.MultiModuleClassPathProvider;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.Pair;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Andrei Badea, Dusan Balek
 */
public class J2SEModularPersistenceProvider implements PersistenceLocationProvider, PersistenceScopeProvider, PersistenceScopesProvider, EntityClassScopeProvider, PropertyChangeListener {
    private static final RequestProcessor RP = new RequestProcessor(J2SEModularPersistenceProvider.class.getName(), 1, false, false);

    private final J2SEModularProject project;
    private final MultiModuleClassPathProvider cpProvider;
    private final Map<FileObject, Pair<PersistenceScope, EntityClassScope>> scopes = new HashMap<>();
    private final Map<FileObject, PersistenceScopesHelper> scopesHelpers = new HashMap<>();
    private final Map<FileObject, EntityMappingsMetadataModelHelper> modelHelpers = new HashMap<>();
//    private final PersistenceXmlChangeListener puChangeListener = new PersistenceXmlChangeListener();
    private final PropertyChangeListener scopeListener = new PropertyChangeListener() {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            Object newV = evt.getNewValue();
            if (Boolean.TRUE.equals(newV)) {
                puChanged();
            }
        }
    };
    private ClassPath projectSourcesClassPath;

    public J2SEModularPersistenceProvider(J2SEModularProject project, MultiModuleClassPathProvider cpProvider) {
        this.project = project;
        this.cpProvider = cpProvider;
        project.getSourceRoots().addPropertyChangeListener(this);
        propertyChange(null);
    }

    @Override
    public FileObject getLocation() {
        return null;
    }

    @Override
    public FileObject getLocation(FileObject fo) {
        for (FileObject root : findSourceRoots(fo)) {
            FileObject location = getMetaInfFolder(root);
            if (location != null) {
                return location;
            }
        }
        return null;
    }

    @Override
    public FileObject createLocation() throws IOException {
        return null;
    }

    @Override
    public FileObject createLocation(FileObject fo) throws IOException {
        List<FileObject> roots = findSourceRoots(fo);
        if (roots.isEmpty()) {
            throw new IOException("There is no target source root specified."); // NOI18N
        }
        FileObject root = roots.get(0);
        FileObject metaInf = root.getFileObject("META-INF"); // NOI18N
        if (metaInf != null) {
            if (!metaInf.isFolder()) {
                throw new IOException("The META-INF directory exists, but is not a folder."); // NOI18N
            }
        } else {
            metaInf = root.createFolder("META-INF"); // NOI18N
        }
//        //
//        FileUtil.addFileChangeListener(puChangeListener, new File(FileUtil.toFile(metaInf), "persistence.xml"));
        //
        return metaInf;
    }

    @Override
    public PersistenceScope findPersistenceScope(FileObject fo) {
        Project proj = FileOwnerQuery.getOwner(fo);
        if (proj != null) {
            J2SEModularPersistenceProvider provider = proj.getLookup().lookup(J2SEModularPersistenceProvider.class);
            return provider.getPersistenceScope(getSourceRoot(fo));
        }
        return null;
    }

    @Override
    public EntityClassScope findEntityClassScope(FileObject fo) {
        Project proj = FileOwnerQuery.getOwner(fo);
        if (proj != null) {
            J2SEModularPersistenceProvider provider = proj.getLookup().lookup(J2SEModularPersistenceProvider.class);
            return provider.getEntityClassScope(getSourceRoot(fo));
        }
        return null;
    }

    @Override
    public PersistenceScopes getPersistenceScopes() {
        return null;
    }

    @Override
    public PersistenceScopes getPersistenceScopes(FileObject fo) {
        List<FileObject> roots = findSourceRoots(fo);
        if (roots.isEmpty()) {
            return null;
        }
        return scopesHelpers.computeIfAbsent(roots.get(0), f -> new PersistenceScopesHelper()).getPersistenceScopes();
    }
    
    private FileObject getSourceRoot(FileObject fo) {
        final ClassPath cp = fo != null ? ClassPath.getClassPath(fo, ClassPath.SOURCE) : null;
        return cp != null ? cp.findOwnerRoot(fo) : null;
    }

    private List<FileObject> findSourceRoots(FileObject fo) {
        final FileObject root = getSourceRoot(fo);
        if (root != null) {
            return Collections.singletonList(root);                    
        }
        final ArrayList<FileObject> roots = new ArrayList<>();
        if (fo != null) {
            for (URL url : project.getSourceRoots().getRootURLs()) {
                final FileObject r = URLMapper.findFileObject(url);
                if (r != null && FileUtil.isParentOf(fo, r)) {
                    roots.add(r);
                }
            }
        }
        return roots;
    }

    private FileObject getMetaInfFolder(final FileObject root) {
        if (root != null) {
            final FileObject metaInf = root.getFileObject("META-INF"); // NOI18N
            if (metaInf != null && metaInf.isFolder()) {
                return metaInf;
            }
        }
        return null;
    }

    private PersistenceScope getPersistenceScope(FileObject root) {
        Pair<PersistenceScope, EntityClassScope> scope = scopes.get(root);
        final FileObject persistenceXml = scope != null ? scope.first().getPersistenceXml() : null;
        if (persistenceXml != null && persistenceXml.isValid()) {
            return scope.first();
        }
        return null;
    }

    private EntityClassScope getEntityClassScope(FileObject root) {
        Pair<PersistenceScope, EntityClassScope> scope = scopes.get(root);
        return scope != null ? scope.second() : null;
    }
    
    private ClassPath getProjectSourcesClassPath() {
        synchronized (this) {
            if (projectSourcesClassPath == null) {
                projectSourcesClassPath = ClassPathSupport.createProxyClassPath(new ClassPath[]{
                            cpProvider.getProjectSourcesClassPath(ClassPath.SOURCE),
                            cpProvider.getProjectSourcesClassPath(ClassPath.COMPILE),});
            }
            return projectSourcesClassPath;
        }
    }

    private EntityMappingsMetadataModelHelper createEntityMappingsHelper() {
        return new EntityMappingsMetadataModelHelper.Builder(cpProvider.getProjectSourcesClassPath(ClassPath.BOOT))
                .setModuleBootPath(cpProvider.getProjectSourcesClassPath(JavaClassPathConstants.MODULE_BOOT_PATH))
                .setClassPath(cpProvider.getProjectSourcesClassPath(ClassPath.COMPILE))
                .setModuleCompilePath(cpProvider.getProjectSourcesClassPath(JavaClassPathConstants.MODULE_COMPILE_PATH))
                .setModuleClassPath(cpProvider.getProjectSourcesClassPath(JavaClassPathConstants.MODULE_CLASS_PATH))
                .setSourcePath(cpProvider.getProjectSourcesClassPath(ClassPath.SOURCE))
                .build();
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        for (FileObject root : project.getSourceRoots().getRoots()) {
            sourcesChanged(root);
        }
    }

    private void sourcesChanged(FileObject root) {
        File persistenceXmlFile = null;
        final FileObject metaInf = getMetaInfFolder(root);
        if (metaInf != null) {
            // if there is a META-INF folder, expect persistence.xml to be created there
            File metaInfFile = FileUtil.toFile(metaInf);
            if (metaInfFile != null) {
                persistenceXmlFile = new File(metaInfFile, "persistence.xml"); //NOI18N
            }
        } else {
            if (root != null) {
                File sourceRootFile = FileUtil.toFile(root);
                persistenceXmlFile = new File(sourceRootFile, "META-INF/persistence.xml"); // NOI18N
            }
        }
        synchronized (this) {
            final PersistenceScopesHelper scopesHelper = scopesHelpers.computeIfAbsent(root, f -> new PersistenceScopesHelper());
            final EntityMappingsMetadataModelHelper modelHelper = modelHelpers.computeIfAbsent(root, f -> createEntityMappingsHelper());
            final Pair<PersistenceScope, EntityClassScope> scope = scopes.computeIfAbsent(root, f -> {
                final ScopeImpl impl = new ScopeImpl(f);
                return Pair.of(PersistenceScopeFactory.createPersistenceScope(impl), 
                        EntityClassScopeFactory.createEntityClassScope(impl));
            });
            if (persistenceXmlFile != null) {
                scopesHelper.changePersistenceScope(scope.first(), persistenceXmlFile);
                modelHelper.changePersistenceXml(persistenceXmlFile);
                scopesHelper.getPersistenceScopes().addPropertyChangeListener(scopeListener);
            } else {
                scopesHelper.changePersistenceScope(null, null);
                modelHelper.changePersistenceXml(null);
            }
        }
    }

    /**
     * Implementation of PersistenceScopeImplementation and EntityClassScopeImplementation.
     */
    private final class ScopeImpl implements PersistenceScopeImplementation, EntityClassScopeImplementation {

        private final FileObject root;

        private ScopeImpl(FileObject root) {
            this.root = root;
        }
        
        @Override
        public FileObject getPersistenceXml() {
            FileObject location = getLocation(root);
            if (location == null) {
                return null;
            }
            return location.getFileObject("persistence.xml"); // NOI18N
        }

        @Override
        public ClassPath getClassPath() {
            return getProjectSourcesClassPath();
        }

        @Override
        public MetadataModel<EntityMappingsMetadata> getEntityMappingsModel(String persistenceUnitName) {
            EntityMappingsMetadataModelHelper modelHelper = modelHelpers.get(root);
            return modelHelper.getEntityMappingsModel(persistenceUnitName);
        }

        @Override
        public MetadataModel<EntityMappingsMetadata> getEntityMappingsModel(boolean withDeps) {
            EntityMappingsMetadataModelHelper modelHelper = modelHelpers.get(root);
            return modelHelper.getDefaultEntityMappingsModel(withDeps);
        }
    }

    private void puChanged() {
        RP.post(new Runnable() {

            @Override
            public void run() {
                ProjectManager.mutex().writeAccess(new Runnable() {
                    @Override
                    public void run() {
                        EditableProperties prop = project.getUpdateHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                        String ap = prop.getProperty(ProjectProperties.ANNOTATION_PROCESSING_PROCESSORS_LIST);
                        boolean changed = false;
                        if (ap == null) {
                            ap = "";
                        }
                        String approperties = prop.getProperty(ProjectProperties.ANNOTATION_PROCESSING_PROCESSOR_OPTIONS);
                        if(approperties == null){
                            approperties = "";
                        }
                        //we need to workaround issue 187653 before we have stable jdk with fix and or fix in eclipselink
                        if(approperties.indexOf("eclipselink.canonicalmodel.use_static_factory")==-1){//NOI18N
                            String toadd = (approperties.length()>0 ? " " : "") + "-Aeclipselink.canonicalmodel.use_static_factory=false";//NOI18N
                            approperties = approperties + toadd;
                            prop.setProperty(ProjectProperties.ANNOTATION_PROCESSING_PROCESSOR_OPTIONS, approperties);
                            changed = true;
                        }
                        //TODO: consider add dependency on j2ee.persistence and get class from persistence provider
                        if (ap.length()>0 && ap.indexOf("org.eclipse.persistence.internal.jpa.modelgen.CanonicalModelProcessor") == -1) {//NOI18N
                            Sources sources = ProjectUtils.getSources(project);
                            SourceGroup[] groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
                            SourceGroup firstGroup = groups[0];
                            FileObject fo = firstGroup.getRootFolder();
                            ClassPath compile = ClassPath.getClassPath(fo, JavaClassPathConstants.PROCESSOR_PATH);
                            if (compile.findResource("org/eclipse/persistence/internal/jpa/modelgen/CanonicalModelProcessor.class") != null) {//NOI18N
                                ap = ap.trim();
                                boolean turnOn = ap.length()==0;//we will switch generation on only if there was no processors even by default properties "save" have case on existence of ap
                                ap = ap + (ap.length() > 0 ? "," : "") + "org.eclipse.persistence.internal.jpa.modelgen.CanonicalModelProcessor"; //NOI18N
                                prop.setProperty(ProjectProperties.ANNOTATION_PROCESSING_PROCESSORS_LIST, ap);
                                if( turnOn ) {
                                    prop.setProperty(ProjectProperties.ANNOTATION_PROCESSING_RUN_ALL_PROCESSORS, "false");//NOI18N
                                }
                                changed = true;
                            }
                        }
                        if (!J2SEModularProjectUtil.isTrue(prop.getProperty(ProjectProperties.ANNOTATION_PROCESSING_ENABLED))) {
                            prop.setProperty(ProjectProperties.ANNOTATION_PROCESSING_ENABLED, "true");    //NOI18N
                            changed = true;
                        }
                        if (changed) {
                            project.getUpdateHelper().putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, prop);
                            try {
                                ProjectManager.getDefault().saveProject(project);
                            } catch (IOException ex) {
                                Exceptions.printStackTrace(ex);
                            } catch (IllegalArgumentException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                    }
                });
            }
        });
    }
}
