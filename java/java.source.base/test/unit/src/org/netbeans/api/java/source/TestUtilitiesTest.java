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
package org.netbeans.api.java.source;

import java.io.File;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.source.BootClassPathUtil;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.java.queries.SourceLevelQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Utilities;

/**
 *
 * @author Tomas Zezula
 */
public class TestUtilitiesTest extends NbTestCase {
    
    public TestUtilitiesTest (final String name) {
        super (name);
    }
    
    private static ClassPath boot;
    private static ClassPath compile;
    private static ClassPath source;
    
    public void testWaitScanFinished () throws Exception {        
        final File wf = getWorkDir();
        final File cache = FileUtil.normalizeFile(new File (wf,"cache"));
        cache.mkdirs();
        final File sourceDir = FileUtil.normalizeFile(new File(wf,"src"));
        sourceDir.mkdirs();        
        boot = BootClassPathUtil.getBootClassPath();
        compile = ClassPathSupport.createClassPath(new URL[0]);
        source = ClassPathSupport.createClassPath(new URL[]{Utilities.toURI(sourceDir).toURL()});
        TestUtilities.setCacheFolder(cache);
        IndexingManager.getDefault().refreshIndexAndWait(sourceDir.toURL(), null);
        assertTrue(TestUtilities.waitScanFinished(10, TimeUnit.SECONDS));
    }
    
    
    public static class CPProvider implements ClassPathProvider {

        public ClassPath findClassPath(FileObject file, String type) {
            if (ClassPath.BOOT.equals(type)) {
                return boot;
            }
            else if (ClassPath.COMPILE.equals(type)) {
                return compile;
            }
            else if (ClassPath.SOURCE.equals(type)) {
                return source;
            }
            return null;
        }
    }
    
    public static class SFBQ implements SourceLevelQueryImplementation {

        public String getSourceLevel(FileObject javaFile) {
            return "1.5";
        }
        
    }

}
