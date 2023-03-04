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

package org.netbeans.modules.j2ee.persistenceapi.metadata.orm.annotation;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import javax.persistence.spi.PersistenceProvider;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.JavaClassPathConstants;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.support.JavaSourceTestCase;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Embeddable;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Entity;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappingsMetadata;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.MappedSuperclass;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Andrei Badea
 */
public class EntityMappingsTestCase extends JavaSourceTestCase {

    public EntityMappingsTestCase(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
        URL root = FileUtil.getArchiveRoot(PersistenceProvider.class.getProtectionDomain().getCodeSource().getLocation());
        addCompileRoots(Collections.singletonList(root));
    }

    /**
     * Used to allow other classes in this package which are not subclasses
     * of this one to call tearDown() directly.
     */
    protected void tearDown() {
        super.tearDown();
    }

    protected MetadataModel<EntityMappingsMetadata> createModel() throws IOException, InterruptedException {
        IndexingManager.getDefault().refreshIndexAndWait(srcFO.getURL(), null);
        return EntityMappingsMetadataModelFactory.createMetadataModel(
                ClassPath.getClassPath(srcFO, ClassPath.BOOT),
                ClassPath.getClassPath(srcFO, JavaClassPathConstants.MODULE_BOOT_PATH),
                ClassPath.getClassPath(srcFO, ClassPath.COMPILE),
                ClassPath.getClassPath(srcFO, JavaClassPathConstants.MODULE_COMPILE_PATH),
                ClassPath.getClassPath(srcFO, JavaClassPathConstants.MODULE_CLASS_PATH),
                ClassPath.getClassPath(srcFO, ClassPath.SOURCE),
                ClassPath.getClassPath(srcFO, JavaClassPathConstants.MODULE_SOURCE_PATH));
    }

    protected static Entity getEntityByName(Entity[] entityList, String name) {
        for (Entity entity : entityList) {
            if (name.equals(entity.getName())) {
                return entity;
            }
        }
        return null;
    }

    protected static Embeddable getEmbeddableByClass(Embeddable[] embeddableList, String clazz) {
        for (Embeddable embeddable : embeddableList) {
            if (clazz.equals(embeddable.getClass2())) {
                return embeddable;
            }
        }
        return null;
    }

    protected static MappedSuperclass getMappedSuperclassByClass(MappedSuperclass[] MappedSuperclassList, String clazz) {
        for (MappedSuperclass mappedSuperclass : MappedSuperclassList) {
            if (clazz.equals(mappedSuperclass.getClass2())) {
                return mappedSuperclass;
            }
        }
        return null;
    }
}
