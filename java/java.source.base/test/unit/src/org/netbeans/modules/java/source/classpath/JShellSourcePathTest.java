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
package org.netbeans.modules.java.source.classpath;

import java.io.IOException;
import java.util.Collections;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.modules.parsing.impl.indexing.CacheFolder;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

/**
 *
 * @author Tomas Zezula
 */
public class JShellSourcePathTest extends NbTestCase {

    private FileObject src;
    private ClassPathImplementation sourcesImpl;
    private ClassPath sources;
    private ClassPath boot;
    private ClassPath compile;

    public JShellSourcePathTest(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockServices.setServices(MockClassPathProvider.class);
        clearWorkDir();
        final FileObject wd = FileUtil.toFileObject(FileUtil.normalizeFile(getWorkDir()));
        assertNotNull(wd);
        final FileObject cache = FileUtil.createFolder(wd, "cache");    //NOI18N
        assertNotNull(cache);
        CacheFolder.setCacheFolder(cache);
    }

    public void testMasterFs() throws IOException {
        init(FileUtil.toFileObject(FileUtil.normalizeFile(getWorkDir())));
        performTest();
    }

    public void testMemFs() throws IOException {
        init(FileUtil.createMemoryFileSystem().getRoot());
        performTest();
    }

    private void init(final FileObject workDir) throws IOException {
        src = FileUtil.createFolder(
                workDir,
                "src"); //NOI18N
        sourcesImpl = ClassPathSupport.createClassPathImplementation(
                Collections.singletonList(ClassPathSupport.createResource(src.toURL())));
        sources = ClassPathFactory.createClassPath(sourcesImpl);
        boot = JavaPlatform.getDefault().getBootstrapLibraries();
        compile = ClassPath.EMPTY;
        final MockClassPathProvider cpp = Lookup.getDefault().lookup(MockClassPathProvider.class);
        if (cpp == null) {
            throw new IllegalStateException("No ClasspathProvider");    //NOI18N
        }
        cpp.setUp(src, boot, compile, sources);
    }

    private void performTest() {
        assertNotNull(src);
        final ClassPath scp = ClassPathFactory.createClassPath(
                SourcePath.filtered(sourcesImpl, false));
        assertEquals(0, scp.entries().size());
        IndexingManager.getDefault().refreshIndexAndWait(src.toURL(), null, true);
        assertEquals(1, scp.entries().size());
    }

    public static final class MockClassPathProvider implements ClassPathProvider {
        private FileObject root;
        private ClassPath[] cps;

        synchronized void setUp(
            final FileObject root,
            final ClassPath... cps) {
            this.root = root;
            this.cps = cps;
            if (this.cps.length != 3) {
                throw new IllegalArgumentException("Wrong length: " + this.cps.length); //NOI18N
            }
        }

        @Override
        public synchronized ClassPath findClassPath(
                final FileObject file,
                final String type) {
            if (root != null && file != null && (root.equals(file) || FileUtil.isParentOf(root, file))) {
                switch (type) {
                    case ClassPath.BOOT:
                        return cps[0];
                    case ClassPath.COMPILE:
                        return cps[1];
                    case ClassPath.SOURCE:
                        return cps[2];
                }
            }
            return null;
        }

    }
}
