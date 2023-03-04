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

package org.netbeans.modules.j2ee.persistence.spi.support;

import java.io.File;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappingsMetadata;
import org.netbeans.modules.j2ee.persistenceapi.metadata.orm.annotation.EntityMappingsMetadataModelFactory;
import org.openide.filesystems.FileUtil;
import org.openide.util.Parameters;

/**
 *
 * @author Andrei Badea
 */
public class EntityMappingsMetadataModelHelper {

    private final ClassPath bootPath;
    private final ClassPath moduleBootPath;
    private final ClassPath compilePath;
    private final ClassPath moduleCompilePath;
    private final ClassPath moduleClassPath;
    private final ClassPath sourcePath;
    private final ClassPath moduleSourcePath;
    private final Object modelLock;

    private File persistenceXml;

    private MetadataModel<EntityMappingsMetadata> model;

    public static EntityMappingsMetadataModelHelper create(ClassPath bootPath, ClassPath compilePath, ClassPath sourcePath) {
        return new EntityMappingsMetadataModelHelper(bootPath, ClassPath.EMPTY, compilePath, ClassPath.EMPTY, ClassPath.EMPTY, sourcePath, ClassPath.EMPTY);
    }

    private EntityMappingsMetadataModelHelper(ClassPath bootPath, ClassPath moduleBootPath, ClassPath compilePath, ClassPath moduleCompilePath, ClassPath moduleClassPath, ClassPath sourcePath, ClassPath moduleSourcePath) {
        this.bootPath = bootPath;
        this.moduleBootPath = moduleBootPath;
        this.compilePath = compilePath;
        this.moduleCompilePath = moduleCompilePath;
        this.moduleClassPath = moduleClassPath;
        this.sourcePath = sourcePath;
        this.moduleSourcePath = moduleSourcePath;
        modelLock = new Object();
    }

    public synchronized void changePersistenceXml(File newPersistenceXml) {
        persistenceXml = newPersistenceXml;
    }

    public MetadataModel<EntityMappingsMetadata> getEntityMappingsModel(String puName) {
        File pXml;
        synchronized (this) {
            pXml = this.persistenceXml;
        }
        if (pXml == null || FileUtil.toFileObject(pXml) == null) {
            return null;
        }
        // XXX trivial implementation which is not affected by the contents of
        // the persistence unit (i.e., the list of orm.xml files and the list
        // of entity classes)
        return getDefaultEntityMappingsModel(false);
    }
    
    public MetadataModel<EntityMappingsMetadata> getDefaultEntityMappingsModel(boolean withDeps) {
        synchronized (modelLock) {
            if (model == null) {
                model = EntityMappingsMetadataModelFactory.createMetadataModel(bootPath, moduleBootPath, compilePath, moduleCompilePath, moduleClassPath, sourcePath, moduleSourcePath);
            }
            return model;
        }
    }

    /**
     * Builder for {@link EntityMappingsMetadataModelHelper}.
     * @since 1.36
     */
    public static final class Builder {
        private final ClassPath bootPath;
        private ClassPath moduleBootPath = ClassPath.EMPTY;
        private ClassPath classPath = ClassPath.EMPTY;
        private ClassPath moduleCompilePath = ClassPath.EMPTY;
        private ClassPath moduleClassPath = ClassPath.EMPTY;
        private ClassPath sourcePath = ClassPath.EMPTY;
        private ClassPath moduleSourcePath = ClassPath.EMPTY;

        public Builder(@NonNull final ClassPath bootPath) {
            Parameters.notNull("bootPath", bootPath);   //NOI18N
            this.bootPath = bootPath;
        }

        @NonNull
        public Builder setModuleBootPath(@NullAllowed ClassPath moduleBootPath) {
            if (moduleBootPath == null) {
                moduleBootPath = ClassPath.EMPTY;
            }
            this.moduleBootPath = moduleBootPath;
            return this;
        }

        @NonNull
        public Builder setClassPath(@NullAllowed ClassPath classPath) {
            if (classPath == null) {
                classPath = ClassPath.EMPTY;
            }
            this.classPath = classPath;
            return this;
        }

        @NonNull
        public Builder setModuleCompilePath(@NullAllowed ClassPath moduleCompilePath) {
            if (moduleCompilePath == null) {
                moduleCompilePath = ClassPath.EMPTY;
            }
            this.moduleCompilePath = moduleCompilePath;
            return this;
        }

        @NonNull
        public Builder setModuleClassPath(@NullAllowed ClassPath moduleClassPath) {
            if (moduleClassPath == null) {
                moduleClassPath = ClassPath.EMPTY;
            }
            this.moduleClassPath = moduleClassPath;
            return this;
        }

        @NonNull
        public Builder setSourcePath(@NullAllowed ClassPath sourcePath) {
            if (sourcePath == null) {
                sourcePath = ClassPath.EMPTY;
            }
            this.sourcePath = sourcePath;
            return this;
        }

        @NonNull
        public Builder setModuleSourcePath(@NullAllowed ClassPath moduleSourcePath) {
            if (moduleSourcePath == null) {
                moduleSourcePath = ClassPath.EMPTY;
            }
            this.moduleSourcePath = moduleSourcePath;
            return this;
        }

        /**
         * Creates a new {@link ClasspathInfo}.
         * @return the {@link ClasspathInfo}
         */
        @NonNull
        public EntityMappingsMetadataModelHelper build() {
            return new EntityMappingsMetadataModelHelper(
                    bootPath,
                    moduleBootPath,
                    classPath,
                    moduleCompilePath,
                    moduleClassPath,
                    sourcePath,
                    moduleSourcePath);
        }
    }
}
