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

package org.netbeans.modules.j2ee.dd.impl.webservices.annotation;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.j2ee.dd.api.webservices.WebservicesMetadata;
import org.netbeans.modules.j2ee.dd.spi.MetadataUnit;
import org.netbeans.modules.j2ee.dd.spi.webservices.WebservicesMetadataModelFactory;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.support.JavaSourceTestCase;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Andrei Badea, Milan Kuchtiak
 */
public class WebServicesTestCase extends JavaSourceTestCase {
    
    public WebServicesTestCase(String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        List<URL> roots = new ArrayList<URL>();
        java.security.CodeSource codeSource = javax.ejb.Stateless.class.getProtectionDomain().getCodeSource();
        if (codeSource != null) {
            roots.add(FileUtil.getArchiveRoot(codeSource.getLocation()));
        }
        codeSource = javax.jws.WebService.class.getProtectionDomain().getCodeSource();
        if (codeSource != null) {
            roots.add(FileUtil.getArchiveRoot(codeSource.getLocation()));
        }
        addCompileRoots(roots);
    }
    
    protected MetadataModel<WebservicesMetadata> createModel() throws IOException, InterruptedException {
        IndexingManager.getDefault().refreshIndexAndWait(srcFO.getURL(), null);
        MetadataUnit metadataUnit = MetadataUnit.create(
                ClassPath.getClassPath(srcFO, ClassPath.BOOT),
                ClassPath.getClassPath(srcFO, ClassPath.COMPILE),
                ClassPath.getClassPath(srcFO, ClassPath.SOURCE),
                null
                );
        return WebservicesMetadataModelFactory.createMetadataModel(metadataUnit);
    }
}
