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

package org.netbeans.modules.maven.persistence;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.JavaClassPathConstants;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.persistence.api.EntityClassScope;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappingsMetadata;
import org.netbeans.modules.j2ee.persistence.spi.EntityClassScopeFactory;
import org.netbeans.modules.j2ee.persistence.spi.EntityClassScopeImplementation;
import org.netbeans.modules.j2ee.persistence.spi.EntityClassScopeProvider;
import org.netbeans.modules.j2ee.persistence.spi.support.EntityMappingsMetadataModelHelper;
import org.netbeans.modules.maven.api.classpath.ProjectSourcesClassPathProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;

/**
 *
 * @author mkleint
 */
@ProjectServiceProvider(service=EntityClassScopeProvider.class, projectType="org-netbeans-modules-maven")
public class EntityClassScopeProviderImpl implements EntityClassScopeProvider {
    
    Project project;
    EntityMappingsMetadataModelHelper helper;
    EntityClassScope scope;
    
    public EntityClassScopeProviderImpl(Project prj) {
        project = prj;
    }
    
    private synchronized EntityMappingsMetadataModelHelper getHelper() {
        if (helper == null) {
            ProjectSourcesClassPathProvider cp = project.getLookup().lookup(ProjectSourcesClassPathProvider.class);
            helper = new EntityMappingsMetadataModelHelper.Builder(cp.getProjectSourcesClassPath(ClassPath.BOOT))
                .setModuleBootPath(cp.getProjectSourcesClassPath(JavaClassPathConstants.MODULE_BOOT_PATH))
                .setClassPath(cp.getProjectSourcesClassPath(ClassPath.COMPILE))
                .setModuleCompilePath(cp.getProjectSourcesClassPath(JavaClassPathConstants.MODULE_COMPILE_PATH))
                .setModuleClassPath(cp.getProjectSourcesClassPath(JavaClassPathConstants.MODULE_CLASS_PATH))
                .setSourcePath(cp.getProjectSourcesClassPath(ClassPath.SOURCE))
                //The CP provider does not support: JavaClassPathConstants.MODULE_SOURCE_PATH
                .build();
        }
        return helper;
    }
    
    
    @Override
    public synchronized EntityClassScope findEntityClassScope(FileObject fo) {
        if (scope == null) {
            scope = EntityClassScopeFactory.createEntityClassScope(new ScopeImpl());
        }
        return scope;
    }

    private class ScopeImpl implements EntityClassScopeImplementation {

        @Override
        public MetadataModel<EntityMappingsMetadata> getEntityMappingsModel(boolean withDeps) {
            return getHelper().getDefaultEntityMappingsModel(withDeps);
        }

    }


}
