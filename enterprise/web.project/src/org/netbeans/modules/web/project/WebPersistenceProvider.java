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
package org.netbeans.modules.web.project;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
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
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.modules.web.project.classpath.ClassPathProviderImpl;
import org.netbeans.modules.web.project.ui.customizer.WebProjectProperties;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 * Provides persistence location and scope delegating to this project's WebModule.
 *
 * @author Andrei Badea
 */
public class WebPersistenceProvider implements PersistenceLocationProvider, PersistenceScopeProvider, PersistenceScopesProvider, EntityClassScopeProvider, PropertyChangeListener {

    private final WebProject project;
    private final PropertyEvaluator evaluator;
    private final ClassPathProviderImpl cpProvider;
    private final ScopeImpl scopeImpl = new ScopeImpl();
    private final PersistenceScope persistenceScope = PersistenceScopeFactory.createPersistenceScope(scopeImpl);
    private final EntityClassScope entityClassScope = EntityClassScopeFactory.createEntityClassScope(scopeImpl);
    private final PersistenceScopesHelper scopesHelper = new PersistenceScopesHelper();
    private EntityMappingsMetadataModelHelper modelHelper;
    private static final RequestProcessor RP = new RequestProcessor();
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

    public WebPersistenceProvider(WebProject project, PropertyEvaluator evaluator, ClassPathProviderImpl cpProvider) {
        this.project = project;
        this.evaluator = evaluator;
        this.cpProvider = cpProvider;
        evaluator.addPropertyChangeListener(this);
        locationChanged();
    }

    // initialize modelHelper lazily to avoid 232272:
    private synchronized EntityMappingsMetadataModelHelper getModelHelper() {
        if (modelHelper == null) {
            modelHelper = createEntityMappingsHelper();
            locationChanged();
        }
        return modelHelper;
    }



    @Override
    public FileObject getLocation() {
        return project.getWebModule().getPersistenceXmlDir();
    }

    @Override
    public FileObject createLocation() throws IOException {
        // the folder should have been created when the project was generated
        FileObject location = project.getWebModule().getPersistenceXmlDir();

        if (location == null) {
            // But possibly the folder got deleted by the user
            // or missing for whatever reason (see issue 134870)
            location = FileUtil.createFolder(project.getWebModule().getPersistenceXmlDirAsFile());

        }
        return location;
    }

    @Override
    public PersistenceScope findPersistenceScope(FileObject fo) {
        Project proj = FileOwnerQuery.getOwner(fo);
        if (proj != null) {
            WebPersistenceProvider provider = proj.getLookup().lookup(WebPersistenceProvider.class);
            return provider.getPersistenceScope();
        }
        return null;
    }

    @Override
    public EntityClassScope findEntityClassScope(FileObject fo) {
        Project proj = FileOwnerQuery.getOwner(fo);
        if (proj != null) {
            WebPersistenceProvider provider = proj.getLookup().lookup(WebPersistenceProvider.class);
            return provider.getEntityClassScope();
        }
        return null;
    }

    @Override
    public PersistenceScopes getPersistenceScopes() {
        return scopesHelper.getPersistenceScopes();
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
                ClassPathProviderImpl cpProv = project.getClassPathProvider();
                projectSourcesClassPath = ClassPathSupport.createProxyClassPath(new ClassPath[]{
                            cpProv.getProjectSourcesClassPath(ClassPath.SOURCE),
                            cpProv.getProjectSourcesClassPath(ClassPath.COMPILE),});
            }
            return projectSourcesClassPath;
        }
    }

    private EntityMappingsMetadataModelHelper createEntityMappingsHelper() {
        return EntityMappingsMetadataModelHelper.create(
                cpProvider.getProjectSourcesClassPath(ClassPath.BOOT),
                cpProvider.getProjectSourcesClassPath(ClassPath.COMPILE),
                cpProvider.getProjectSourcesClassPath(ClassPath.SOURCE));
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        String propName = event.getPropertyName();
        if (propName == null || propName.equals(WebProjectProperties.PERSISTENCE_XML_DIR)) {
            locationChanged();
        }
    }

    private synchronized void locationChanged() {
        File persistenceXmlDirFile = project.getWebModule().getPersistenceXmlDirAsFile();
        if (persistenceXmlDirFile != null) {
            File persistenceXmlFile = new File(persistenceXmlDirFile, "persistence.xml"); // NOI18N
            scopesHelper.changePersistenceScope(persistenceScope, persistenceXmlFile);
            // update modelHelper only if it already exists:
            if (modelHelper != null) {
                modelHelper.changePersistenceXml(persistenceXmlFile);
            }
            scopesHelper.getPersistenceScopes().addPropertyChangeListener(scopeListener);
        } else {
            scopesHelper.changePersistenceScope(null, null);
            // update modelHelper only if it already exists:
            if (modelHelper != null) {
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
            return getModelHelper().getEntityMappingsModel(persistenceUnitName);
        }

        @Override
        public MetadataModel<EntityMappingsMetadata> getEntityMappingsModel(boolean withDeps) {
            return getModelHelper().getDefaultEntityMappingsModel(withDeps);
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

                        if (ap == null) {
                            ap = "";
                        }
                        String approperties = prop.getProperty(ProjectProperties.ANNOTATION_PROCESSING_PROCESSOR_OPTIONS);
                        if(approperties == null){
                            approperties = "";
                        }

                        boolean changed = false;
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
                            if (compile != null && compile.findResource("org/eclipse/persistence/internal/jpa/modelgen/CanonicalModelProcessor.class") != null) {//NOI18N
                                ap = ap.trim();
                                boolean turnOn = ap.length()==0;//we will switch generation on only if there was no processors even by default properties "save" have case on existence of ap
                                ap = ap + (ap.length() > 0 ? "," : "") + "org.eclipse.persistence.internal.jpa.modelgen.CanonicalModelProcessor"; //NOI18N
                                prop.setProperty(ProjectProperties.ANNOTATION_PROCESSING_PROCESSORS_LIST, ap);
                                changed = true;
                                if( turnOn ) {
                                    prop.setProperty(ProjectProperties.ANNOTATION_PROCESSING_RUN_ALL_PROCESSORS, "false");//NOI18N
                                }
                            }
                        }
                        if (!"true".equals(prop.getProperty(ProjectProperties.ANNOTATION_PROCESSING_ENABLED_IN_EDITOR))) {
                            prop.setProperty(ProjectProperties.ANNOTATION_PROCESSING_ENABLED_IN_EDITOR, "true");    //NOI18N
                            changed = true;
                        }
                        if(changed) {
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
