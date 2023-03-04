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
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.spi.MetadataModelImplementation;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappingsMetadata;

/**
 *
 * @author Andrei Badea
 */
public class EntityMappingsMetadataModelImpl implements MetadataModelImplementation<EntityMappingsMetadata> {

    private final AnnotationModelHelper helper;
    private final EntityMappingsImpl root;
    private final EntityMappingsMetadata metadata;

    public EntityMappingsMetadataModelImpl(ClassPath bootPath, ClassPath moduleBootPath, ClassPath compilePath, ClassPath moduleCompilePath, ClassPath moduleClassPath, ClassPath sourcePath, ClassPath moduleSourcePath) {
        ClasspathInfo cpi = new ClasspathInfo.Builder(bootPath)
                .setModuleBootPath(moduleBootPath)
                .setClassPath(compilePath)
                .setModuleCompilePath(moduleCompilePath)
                .setModuleClassPath(moduleClassPath)
                .setSourcePath(sourcePath)
                .setModuleSourcePath(moduleSourcePath)
                .build();
        helper = AnnotationModelHelper.create(cpi);
        root = new EntityMappingsImpl(helper);
        metadata = new EntityMappingsMetadataImpl(cpi, root);
    }

    public <R> R runReadAction(final MetadataModelAction<EntityMappingsMetadata, R> action) throws IOException {
        return helper.runJavaSourceTask(new Callable<R>() {
            public R call () throws Exception {
                return action.run(metadata);
            }
        });
    }

    public boolean isReady() {
        return !helper.isJavaScanInProgress();
    }

    public <R> Future<R> runReadActionWhenReady(final MetadataModelAction<EntityMappingsMetadata, R> action) throws IOException {
        return helper.runJavaSourceTaskWhenScanFinished(new Callable<R>() {
            public R call() throws Exception {
                return action.run(metadata);
            }
        });
    }
}
