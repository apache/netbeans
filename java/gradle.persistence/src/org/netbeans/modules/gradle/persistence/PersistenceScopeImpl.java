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

package org.netbeans.modules.gradle.persistence;

import org.netbeans.modules.gradle.java.api.ProjectSourcesClassPathProvider;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappingsMetadata;
import org.netbeans.modules.j2ee.persistence.spi.PersistenceLocationProvider;
import org.netbeans.modules.j2ee.persistence.spi.PersistenceScopeImplementation;
import org.netbeans.modules.j2ee.persistence.spi.support.EntityMappingsMetadataModelHelper;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Gradle Implementation of <CODE>org.netbeans.modules.j2ee.persistence.spi.PersistenceScopeImplementation</CODE>
 *
 * @author Daniel Mohni
 */
public class PersistenceScopeImpl implements PersistenceScopeImplementation {

    private PersistenceLocationProvider locationProvider = null;
    private final EntityMappingsMetadataModelHelper modelHelper;
    private final ProjectSourcesClassPathProvider cpProvider;
    private ClassPath projectSourcesClassPath;

    /**
     * Creates a new instance of PersistenceScopeImpl
     *
     * @param locProvider the PersistenceLocationProvider instance to use for lookups
     * @param cpProvider the PersistenceClasspathProvider instance to use for lookups
     */
    public PersistenceScopeImpl(PersistenceLocationProvider locProvider,
            ProjectSourcesClassPathProvider imp) {
        this.locationProvider = locProvider;
        cpProvider = imp;
        modelHelper = createEntityMappingsHelper();
    }

    /**
     * property access to the project's persistence.xml
     *
     * @return the persistence.xml file used in this project or null
     */
    @Override
    public FileObject getPersistenceXml() {
        FileObject location = locationProvider.getLocation();
        return location != null ? location.getFileObject("persistence.xml") : null; // NOI18N
    }

    /**
     * property access to the persistence project classpath
     *
     * @return the classpath provided by the PersistenceClasspathProvider
     */
    @Override
    public ClassPath getClassPath() {
        return getProjectSourcesClassPath();
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

    @Override
    public MetadataModel<EntityMappingsMetadata> getEntityMappingsModel(String persistenceUnitName) {
        MetadataModel<EntityMappingsMetadata> metadataModel = modelHelper.getEntityMappingsModel(persistenceUnitName);
        if (metadataModel == null) {
            FileObject puFo = getPersistenceXml();
            if (puFo != null) {
                modelHelper.changePersistenceXml(FileUtil.toFile(puFo));
                metadataModel = modelHelper.getEntityMappingsModel(persistenceUnitName);
            }
        }
        return metadataModel;
    }

    private EntityMappingsMetadataModelHelper createEntityMappingsHelper() {
        return EntityMappingsMetadataModelHelper.create(
                cpProvider.getProjectSourcesClassPath(ClassPath.BOOT),
                cpProvider.getProjectSourcesClassPath(ClassPath.COMPILE),
                cpProvider.getProjectSourcesClassPath(ClassPath.SOURCE));
    }

}
