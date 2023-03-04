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

package org.netbeans.modules.java.j2seplatform.platformdefinition;

import org.netbeans.modules.java.platform.queries.PlatformJavadocForBinaryQuery;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.queries.JavadocForBinaryQuery;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Utilities;
import org.netbeans.core.startup.layers.ArchiveURLMapper;
import org.netbeans.junit.MockServices;
import org.netbeans.modules.masterfs.MasterURLMapper;

/**
 * JavadocForBinaryQueryPlatformImpl test
 *
 * @author  David Konecny
 */
public class J2SEPlatformJavadocForBinaryQueryTest extends NbTestCase {
    
    public J2SEPlatformJavadocForBinaryQueryTest(java.lang.String testName) {
        super(testName);
        MockServices.setServices(ArchiveURLMapper.class,
                PlatformJavadocForBinaryQuery.class,
                MasterURLMapper.class,
                JavaPlatformProviderImpl.class);
    }
    
    protected @Override void setUp() throws Exception {
        System.setProperty("netbeans.user", getWorkDirPath()); 
        super.setUp();
        clearWorkDir();                
    }
    
    private File getBaseDir() throws Exception {
        File dir = getWorkDir();
        if (Utilities.isWindows()) {
            dir = new File(dir.getCanonicalPath());
        }
        return dir;
    }


    public void testQuery() throws Exception {
        JavaPlatform platform = JavaPlatform.getDefault();
        ClassPath cp = platform.getBootstrapLibraries();
        FileObject pfo = cp.getRoots()[0];
        URL u = URLMapper.findURL(pfo, URLMapper.EXTERNAL);
        URL urls[] = JavadocForBinaryQuery.findJavadoc(u).getRoots();
        assertEquals(1, urls.length);
        assertTrue(urls[0].toString(), urls[0].toString().startsWith("https://docs.oracle.com/"));

        List<URL> l = new ArrayList<URL>();
        File javadocFile = getBaseDir();
        File api = new File (javadocFile,"api");
        File index = new File (api,"index-files");
        FileUtil.toFileObject(index);
        index.mkdirs();
        l.add(Utilities.toURI(javadocFile).toURL());
        J2SEPlatformImpl platformImpl = (J2SEPlatformImpl)platform;
        platformImpl.setJavadocFolders(l);
        urls = JavadocForBinaryQuery.findJavadoc(u).getRoots();
        assertEquals(1, urls.length);
        assertEquals(Utilities.toURI(api).toURL(), urls[0]);
    }
}
