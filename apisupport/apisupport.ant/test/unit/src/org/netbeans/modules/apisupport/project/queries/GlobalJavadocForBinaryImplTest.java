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

package org.netbeans.modules.apisupport.project.queries;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import org.netbeans.api.java.queries.JavadocForBinaryQuery.Result;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.test.TestFileUtils;
import org.openide.util.Utilities;

public class GlobalJavadocForBinaryImplTest extends TestBase {

    public GlobalJavadocForBinaryImplTest(String testName) {
        super(testName);
    }

    public void testFindJavadoc() throws Exception {
        File nbDocZip = generateNbDocZip();
        URL nbDocZipURL = FileUtil.urlForArchiveOrDir(nbDocZip);
        NbPlatform.getDefaultPlatform().addJavadocRoot(nbDocZipURL);
        doTestFindJavadoc(Utilities.toURI(file("platform/openide.loaders/src")).toURL(), nbDocZipURL);
        doTestFindJavadoc(FileUtil.urlForArchiveOrDir(file("nbbuild/netbeans/platform/modules/org-openide-loaders.jar")), nbDocZipURL);
    }
    private void doTestFindJavadoc(URL binRoot, URL nbDocZipURL) throws Exception {
        Result res = new GlobalJavadocForBinaryImpl().findJavadoc(binRoot);
        assertNotNull("result is not null", res);
        
        URL[] expected = {
            nbDocZipURL,
            new URL(nbDocZipURL, "org-openide-loaders/")
        };
        
        assertEquals("right javadoc for loaders", Arrays.asList(expected), Arrays.asList(res.getRoots()));

    }

    private File generateNbDocZip() throws IOException {
        return FileUtil.toFile(TestFileUtils.writeZipFile(FileUtil.toFileObject(getWorkDir()), "nbdoc.zip", "org-openide-loaders/index.html:"));
    }
}
