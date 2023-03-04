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

package org.netbeans.modules.gradle.persistence;

import org.netbeans.modules.gradle.java.api.ProjectSourcesClassPathProvider;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.persistence.api.PersistenceScope;
import org.netbeans.modules.j2ee.persistence.spi.PersistenceLocationProvider;
import org.netbeans.modules.j2ee.persistence.spi.PersistenceScopeFactory;
import org.netbeans.modules.j2ee.persistence.spi.PersistenceScopeImplementation;
import org.netbeans.modules.j2ee.persistence.spi.PersistenceScopeProvider;
import org.openide.filesystems.FileObject;


/**
 * Gradle Implementation of 
 * <CODE>org.netbeans.modules.j2ee.persistence.spi.PersistenceScopeProvider</CODE> 
 * @author Daniel Mohni
 */
public class PersistenceScopeProviderImpl implements PersistenceScopeProvider 
{

    private final PersistenceLocationProvider locProvider;
    private final Project project;
    private PersistenceScope persistenceScope = null;

    /**
     * Creates a new instance of PersistenceScopeProviderImpl
     * @param locProvider the PersistenceLocationProvider instance to use for lookups
     * @param cpProvider the PersistenceClasspathProvider instance to use for lookups
     */
    public PersistenceScopeProviderImpl(PersistenceLocationProvider locProvider,
            Project project)
    {
        this.locProvider = locProvider;
        this.project = project;
    }

    /**
     * validated access to the current PersistenceScope instance by checking
     * the presence of a persistence.xml file
     * @param fileObject file to check for persistence scope, not used !
     * @return a valid PersistenceScope instance or null
     */
    @Override public synchronized PersistenceScope findPersistenceScope(FileObject fileObject)
    {
        if (persistenceScope == null) {
            ProjectSourcesClassPathProvider classpath = project.getLookup().lookup(ProjectSourcesClassPathProvider.class);
            PersistenceScopeImplementation persistenceScopeImpl = new PersistenceScopeImpl(locProvider, classpath);
            persistenceScope = PersistenceScopeFactory.createPersistenceScope(persistenceScopeImpl);
        }
        FileObject persistenceXml = persistenceScope.getPersistenceXml();
        if (persistenceXml != null && persistenceXml.isValid()) {
            return persistenceScope;
        }
        return null;
    }
}
