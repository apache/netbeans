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

package org.netbeans.modules.j2ee.dd.impl.common.annotation;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import javax.ejb.Stateless;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.j2ee.dd.api.client.AppClientMetadata;
import org.netbeans.modules.j2ee.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata;
import org.netbeans.modules.j2ee.dd.api.web.WebAppMetadata;
import org.netbeans.modules.j2ee.dd.spi.MetadataUnit;
import org.netbeans.modules.j2ee.dd.spi.client.AppClientMetadataModelFactory;
import org.netbeans.modules.j2ee.dd.spi.ejb.EjbJarMetadataModelFactory;
import org.netbeans.modules.j2ee.dd.spi.web.WebAppMetadataModelFactory;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.support.JavaSourceTestCase;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Andrei Badea
 * @author Martin Adamek
 */
public class CommonTestCase extends JavaSourceTestCase {
    
    public CommonTestCase(String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        URL root = FileUtil.getArchiveRoot(Stateless.class.getProtectionDomain().getCodeSource().getLocation());
        addCompileRoots(Collections.singletonList(root));
    }
    
    public MetadataModel<EjbJarMetadata> createEjbJarModel() throws IOException, InterruptedException {
        IndexingManager.getDefault().refreshIndexAndWait(srcFO.getURL(), null);
        MetadataUnit metadataUnit = MetadataUnit.create(
                ClassPath.getClassPath(srcFO, ClassPath.BOOT),
                ClassPath.getClassPath(srcFO, ClassPath.COMPILE),
                ClassPath.getClassPath(srcFO, ClassPath.SOURCE),
                null
                );
        return EjbJarMetadataModelFactory.createMetadataModel(metadataUnit);
    }
    
    public MetadataModel<WebAppMetadata> createWebAppModel() throws IOException, InterruptedException {
        return createWebAppModel(true);
    }

    public MetadataModel<WebAppMetadata> createWebAppModel(boolean withDD) throws IOException, InterruptedException {
        IndexingManager.getDefault().refreshIndexAndWait(srcFO.getURL(), null);
        MetadataUnit metadataUnit = MetadataUnit.create(
                ClassPath.getClassPath(srcFO, ClassPath.BOOT),
                ClassPath.getClassPath(srcFO, ClassPath.COMPILE),
                ClassPath.getClassPath(srcFO, ClassPath.SOURCE),
                withDD ? new File(getDataDir(), "web_org.xml") : null
                );
        return WebAppMetadataModelFactory.createMetadataModel(metadataUnit, true);
    }
    
    public MetadataModel<AppClientMetadata> createAppClientModel() throws IOException, InterruptedException {
        IndexingManager.getDefault().refreshIndexAndWait(srcFO.getURL(), null);
        MetadataUnit metadataUnit = MetadataUnit.create(
                ClassPath.getClassPath(srcFO, ClassPath.BOOT),
                ClassPath.getClassPath(srcFO, ClassPath.COMPILE),
                ClassPath.getClassPath(srcFO, ClassPath.SOURCE),
                null);
        return AppClientMetadataModelFactory.createMetadataModel(metadataUnit);
    }

    protected static Ejb getEjbByEjbName(Ejb[] ejbs, String name) {
        for (Ejb ejb : ejbs) {
            if (name.equals(ejb.getEjbName())) {
                return ejb;
            }
        }
        return null;
    }

}
