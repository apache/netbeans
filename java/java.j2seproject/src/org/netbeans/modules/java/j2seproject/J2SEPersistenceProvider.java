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
package org.netbeans.modules.java.j2seproject;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
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
//retouche:
import org.netbeans.modules.j2ee.persistence.api.EntityClassScope;
//import org.netbeans.modules.j2ee.metadata.ClassPathSupport;
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
import org.netbeans.modules.java.api.common.classpath.ClassPathProviderImpl;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Andrei Badea
 */
public class J2SEPersistenceProvider implements PersistenceLocationProvider, PersistenceScopeProvider, PersistenceScopesProvider, EntityClassScopeProvider, PropertyChangeListener {
    private static final RequestProcessor RP = new RequestProcessor(J2SEPersistenceProvider.class.getName(), 1, false, false);

    private final J2SEProject project;
    private final ClassPathProviderImpl cpProvider;
    private final ScopeImpl scopeImpl = new ScopeImpl();
    private final PersistenceScope persistenceScope = PersistenceScopeFactory.createPersistenceScope(scopeImpl);
    private final EntityClassScope entityClassScope = EntityClassScopeFactory.createEntityClassScope(scopeImpl);
    private final PersistenceScopesHelper scopesHelper = new PersistenceScopesHelper();
    private final EntityMappingsMetadataModelHelper modelHelper;
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

    public J2SEPersistenceProvider(J2SEProject project, ClassPathProviderImpl cpProvider) {
        this.project = project;
        this.cpProvider = cpProvider;
        modelHelper = createEntityMappingsHelper();
        project.getSourceRoots().addPropertyChangeListener(this);
        sourcesChanged();
    }

    @Override
    public FileObject getLocation() {
        return getMetaInfFolder();
    }

    @Override
    public FileObject createLocation() throws IOException {
        FileObject root = getFirstSourceRoot();
        if (root == null) {
            throw new IOException("There are no source roots in the project or the first source root does not exist."); // NOI18N
        }
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
            J2SEPersistenceProvider provider = proj.getLookup().lookup(J2SEPersistenceProvider.class);
            return provider.getPersistenceScope();
        }
        return null;
    }

    @Override
    public EntityClassScope findEntityClassScope(FileObject fo) {
        Project proj = FileOwnerQuery.getOwner(fo);
        if (proj != null) {
            J2SEPersistenceProvider provider = proj.getLookup().lookup(J2SEPersistenceProvider.class);
            return provider.getEntityClassScope();
        }
        return null;
    }

    @Override
    public PersistenceScopes getPersistenceScopes() {
        return scopesHelper.getPersistenceScopes();
    }

    private FileObject getFirstSourceRoot() {
        for (URL url : project.getSourceRoots().getRootURLs()) {
            FileObject fo = URLMapper.findFileObject(url);
            if (fo != null) {
                return fo;
            }
        }
        return null;
    }

    private FileObject getMetaInfFolder() {
        for (URL url : project.getSourceRoots().getRootURLs()) {
            FileObject fo = URLMapper.findFileObject(url);
            if (fo != null) {
                FileObject metaInf = fo.getFileObject("META-INF"); // NOI18N
                if (metaInf != null && metaInf.isFolder()) {
                    return metaInf;
                }
            }
        }
        return null;
    }

    private PersistenceScope getPersistenceScope() {
        FileObject persistenceXml = persistenceScope.getPersistenceXml();
        if (persistenceXml != null && persistenceXml.isValid()) {
            return persistenceScope;
        }
        return null;
    }

    private EntityClassScope getEntityClassScope() {
        return entityClassScope;
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
        sourcesChanged();
    }

    private void sourcesChanged() {
        File persistenceXmlFile = null;
        FileObject metaInf = getMetaInfFolder();
        if (metaInf != null) {
            // if there is a META-INF folder, expect persistence.xml to be created there
            File metaInfFile = FileUtil.toFile(metaInf);
            if (metaInfFile != null) {
                persistenceXmlFile = new File(metaInfFile, "persistence.xml"); //NOI18N
            }
        } else {
            FileObject firstSourceRoot = getFirstSourceRoot();
            if (firstSourceRoot != null) {
                File sourceRootFile = FileUtil.toFile(firstSourceRoot);
                persistenceXmlFile = new File(sourceRootFile, "META-INF/persistence.xml"); // NOI18N
            }
        }
        synchronized (this) {
            if (persistenceXmlFile != null) {
                scopesHelper.changePersistenceScope(persistenceScope, persistenceXmlFile);
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

        @Override
        public FileObject getPersistenceXml() {
            FileObject location = getLocation();
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
            return modelHelper.getEntityMappingsModel(persistenceUnitName);
        }

        @Override
        public MetadataModel<EntityMappingsMetadata> getEntityMappingsModel(boolean withDeps) {
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
                        if (!J2SEProjectUtil.isTrue(prop.getProperty(ProjectProperties.ANNOTATION_PROCESSING_ENABLED))) {
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
