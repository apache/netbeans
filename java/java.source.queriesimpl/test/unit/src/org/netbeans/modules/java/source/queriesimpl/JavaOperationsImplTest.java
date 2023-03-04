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
package org.netbeans.modules.java.source.queriesimpl;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.java.source.BootClassPathUtil;
import org.netbeans.modules.java.source.parsing.JavacParserFactory;
import org.netbeans.modules.java.source.queries.spi.QueryOperationsTestBase;
import org.netbeans.modules.parsing.impl.indexing.CacheFolder;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
/**
 *
 * @author Tomas Zezula
 */
public class JavaOperationsImplTest extends QueryOperationsTestBase {

    private File wd;

    public JavaOperationsImplTest(final String name) {
        super(name);
    }

    public static Test suite() {
        final TestSuite suite = new NbTestSuite(JavaOperationsImplTest.class);
        return suite;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        File cacheFolder = new File (getWorkDir(), "cache"); //NOI18N
        cacheFolder.mkdirs();
        CacheFolder.setCacheFolder(FileUtil.toFileObject(cacheFolder));
        CPProvider.srcPath = ClassPathSupport.createClassPath(srcRoot);
        CPProvider.bootPath = BootClassPathUtil.getBootClassPath();
        CPProvider.compilePath = ClassPathSupport.createClassPath(new URL[0]);
        MockServices.setServices(CPProvider.class);
        MockMimeLookup.setInstances(MimePath.parse("text/x-java"), new JavacParserFactory());
    }

    public static class CPProvider implements ClassPathProvider {
        private static ClassPath srcPath;
        private static ClassPath bootPath;
        private static ClassPath compilePath;
        @Override
        public ClassPath findClassPath(
                FileObject file,
                String type) {
            for (FileObject srcRoot : srcPath.getRoots()) {
                if (srcRoot.equals(file) || FileUtil.isParentOf(srcRoot, file)) {
                    if (type == ClassPath.SOURCE) {
                        return srcPath;
                    } else if (type == ClassPath.BOOT) {
                        return bootPath;
                    } else if (type == ClassPath.COMPILE) {
                        return compilePath;
                    }
                }
            }
            return null;
        }
    }

    @Override
    protected final File getWorkDir() throws IOException {
        if (wd == null) {
            wd = new NbTestCase(getName()){}.getWorkDir();
        }
        return wd;
    }
}
