/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
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
