/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.java.j2seplatform.platformdefinition;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.j2seplatform.queries.DefaultSourceLevelQueryImpl;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;

/**
 * @author Tomas Zezula
 */
public class DefaultSourceLevelQueryImplTest extends NbTestCase {

    public DefaultSourceLevelQueryImplTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        MockServices.setServices(new Class[] {JavaPlatformProviderImpl.class});
    }

    public void testGetSourceLevel() throws Exception {
        FileObject root = FileUtil.toFileObject(this.getWorkDir());
        assertNotNull ("Cannot convert File to FileObject, missing master-fs?",root);    //NOI18N
        FileObject javaFile = createTestFile (root,"test","Test.java","package test;\n class Test {}");    //NOI18N
        assertEquals(JavaPlatform.getDefault().getSpecification().getVersion().toString(), SourceLevelQuery.getSourceLevel(javaFile));
    }

    public void testGetSourceLevelForModuleInfo() throws Exception {
        FileObject root = FileUtil.toFileObject(this.getWorkDir());
        assertNotNull ("Cannot convert File to FileObject, missing master-fs?",root);    //NOI18N
        FileObject javaFile = createTestFile (root, null, "module-info.java", "module Foo {}");    //NOI18N
        assertEquals(
                expectedSourceLevel().toString(),
                SourceLevelQuery.getSourceLevel(javaFile));
    }

    public void testGetSourceLevelForModularSources() throws Exception {
        final FileObject root = FileUtil.toFileObject(this.getWorkDir());
        assertNotNull ("Cannot convert File to FileObject, missing master-fs?",root);    //NOI18N
        final FileObject module = createTestFile (root, null, "module-info.java", "module Foo {}");    //NOI18N
        final FileObject java = createTestFile (root, "org/nb/test", "Test.java", "package org.nb.test;\nclass Test {}");    //NOI18N
        assertEquals(
                expectedSourceLevel().toString(),
                SourceLevelQuery.getSourceLevel(java));
    }

    public void testRootCache() throws IOException {
        final FileObject wd = FileUtil.toFileObject(FileUtil.normalizeFile(getWorkDir()));
        final FileObject root1 = FileUtil.createFolder(wd, "root1");
        final FileObject module1 = createTestFile (root1, null, "module-info.java", "module Foo {}");    //NOI18N
        final FileObject java1 = createTestFile (root1, "org/nb/test", "Test.java", "package org.nb.test;\nclass Test {}");    //NOI18N
        final FileObject root2 = FileUtil.createFolder(wd, "root2");
        final FileObject module2 = createTestFile (root2, null, "module-info.java", "module Boo {}");    //NOI18N
        final FileObject java2 = createTestFile (root2, "org/nb/test", "Test2.java", "package org.nb.test;\nclass Test2 {}");    //NOI18N
        final Logger l = Logger.getLogger(DefaultSourceLevelQueryImpl.class.getName());
        final Level origLogLevel = l.getLevel();
        final H h = new H();
        l.setLevel(Level.FINE);
        l.addHandler(h);
        try {
            assertEquals(
                    expectedSourceLevel().toString(),
                    SourceLevelQuery.getSourceLevel(java1));
            List<? extends FileObject> roots = h.getRoots();
            assertEquals(1, roots.size());
            assertEquals(root1, roots.get(0));
            h.reset();
            assertEquals(
                    expectedSourceLevel().toString(),
                    SourceLevelQuery.getSourceLevel(java1));
            roots = h.getRoots();
            assertEquals(0, roots.size());
            h.reset();
            assertEquals(
                    expectedSourceLevel().toString(),
                    SourceLevelQuery.getSourceLevel(java2));
            roots = h.getRoots();
            assertEquals(1, roots.size());
            assertEquals(root2, roots.get(0));
        } finally {
            l.removeHandler(h);
            l.setLevel(origLogLevel);
        }
    }

    public void testIsModuleCache() throws IOException {
        final FileObject wd = FileUtil.toFileObject(FileUtil.normalizeFile(getWorkDir()));
        final FileObject root1 = FileUtil.createFolder(wd, "root1");
        final FileObject module1 = createTestFile (root1, null, "module-info.java", "module Foo {}");    //NOI18N
        final FileObject java1 = createTestFile (root1, "org/nb/test", "Test.java", "package org.nb.test;\nclass Test {}");    //NOI18N
        final FileObject root2 = FileUtil.createFolder(wd, "root2");
        final FileObject java2 = createTestFile (root2, "org/nb/test", "Test2.java", "package org.nb.test;\nclass Test2 {}");    //NOI18N
        final Logger l = Logger.getLogger(DefaultSourceLevelQueryImpl.class.getName());
        final Level origLogLevel = l.getLevel();
        final H h = new H();
        l.setLevel(Level.FINE);
        l.addHandler(h);
        try {
            assertEquals(
                    expectedSourceLevel().toString(),
                    SourceLevelQuery.getSourceLevel(java1));
            List<? extends Boolean> isMods = h.getIsModules();
            assertEquals(1, isMods.size());
            assertEquals(Boolean.TRUE, isMods.get(0));
            h.reset();
            assertEquals(
                    expectedSourceLevel().toString(),
                    SourceLevelQuery.getSourceLevel(java1));
            isMods = h.getIsModules();
            assertEquals(0, isMods.size());
            h.reset();
            assertEquals(
                    JavaPlatform.getDefault().getSpecification().getVersion().toString(),
                    SourceLevelQuery.getSourceLevel(java2));
            isMods = h.getIsModules();
            assertEquals(1, isMods.size());
            assertEquals(Boolean.FALSE, isMods.get(0));
        } finally {
            l.removeHandler(h);
            l.setLevel(origLogLevel);
        }
    }

    public void testIsModuleCache_ModuleInfoDeleted() throws IOException {
        final FileObject wd = FileUtil.toFileObject(FileUtil.normalizeFile(getWorkDir()));
        final FileObject root1 = FileUtil.createFolder(wd, "root1");
        FileObject module1 = createTestFile (root1, null, "module-info.java", "module Foo {}");    //NOI18N
        final FileObject java1 = createTestFile (root1, "org/nb/test", "Test.java", "package org.nb.test;\nclass Test {}");    //NOI18N
        final Logger l = Logger.getLogger(DefaultSourceLevelQueryImpl.class.getName());
        final Level origLogLevel = l.getLevel();
        final H h = new H();
        l.setLevel(Level.FINE);
        l.addHandler(h);
        try {
            assertEquals(
                    expectedSourceLevel().toString(),
                    SourceLevelQuery.getSourceLevel(java1));
            List<? extends Boolean> isMods = h.getIsModules();
            assertEquals(1, isMods.size());
            assertEquals(Boolean.TRUE, isMods.get(0));
            h.reset();
            module1.delete();
            assertEquals(
                    JavaPlatform.getDefault().getSpecification().getVersion().toString(),
                    SourceLevelQuery.getSourceLevel(java1));
            isMods = h.getIsModules();
            assertEquals(1, isMods.size());
            assertEquals(Boolean.FALSE, isMods.get(0));
            h.reset();
            assertEquals(
                    JavaPlatform.getDefault().getSpecification().getVersion().toString(),
                    SourceLevelQuery.getSourceLevel(java1));
            isMods = h.getIsModules();
            assertEquals(0, isMods.size());
        } finally {
            l.removeHandler(h);
            l.setLevel(origLogLevel);
        }
    }

    public void testIsModuleCache_ModuleInfoCreated() throws IOException {
        final FileObject wd = FileUtil.toFileObject(FileUtil.normalizeFile(getWorkDir()));
        final FileObject root1 = FileUtil.createFolder(wd, "root1");
        final FileObject java1 = createTestFile (root1, "org/nb/test", "Test.java", "package org.nb.test;\nclass Test {}");    //NOI18N
        final Logger l = Logger.getLogger(DefaultSourceLevelQueryImpl.class.getName());
        final Level origLogLevel = l.getLevel();
        final H h = new H();
        l.setLevel(Level.FINE);
        l.addHandler(h);
        try {
            assertEquals(
                    JavaPlatform.getDefault().getSpecification().getVersion().toString(),
                    SourceLevelQuery.getSourceLevel(java1));
            List<? extends Boolean> isMods = h.getIsModules();
            assertEquals(1, isMods.size());
            assertEquals(Boolean.FALSE, isMods.get(0));
            h.reset();
            FileObject module1 = createTestFile (root1, null, "module-info.java", "module Foo {}");    //NOI18N
            assertEquals(
                    expectedSourceLevel().toString(),
                    SourceLevelQuery.getSourceLevel(java1));
            isMods = h.getIsModules();
            assertEquals(1, isMods.size());
            assertEquals(Boolean.TRUE, isMods.get(0));
            h.reset();
            assertEquals(
                    expectedSourceLevel().toString(),
                    SourceLevelQuery.getSourceLevel(java1));
            isMods = h.getIsModules();
            assertEquals(0, isMods.size());
        } finally {
            l.removeHandler(h);
            l.setLevel(origLogLevel);
        }
    }

    private static FileObject createTestFile (FileObject root, String path, String fileName, String content) throws IOException {
        FileObject pkg = path != null ?
                FileUtil.createFolder(root, path) :
                root;
        assertNotNull (pkg);
        FileObject data = pkg.createData(fileName);
        FileLock lock = data.lock();
        try {
            PrintWriter out = new PrintWriter (new OutputStreamWriter (data.getOutputStream(lock)));
            try {
                out.println (content);
            } finally {
                out.close();
            }
        } finally {
            lock.releaseLock();
        }
        return data;
    }

    @NonNull
    private static SpecificationVersion expectedSourceLevel() {
        final SpecificationVersion java9 = new SpecificationVersion("9");   //NOI18N
        final SpecificationVersion defPlat = JavaPlatform.getDefault().getSpecification().getVersion();
        final SpecificationVersion expected = defPlat.compareTo(java9) < 0 ?
                java9 :
                defPlat;
        return expected;
    }

    private static final class H extends Handler {

        private final List<FileObject> roots = new ArrayList<>();
        private final List<Boolean> isModular = new ArrayList<>();

        H() {
        }

        void reset() {
            roots.clear();
            isModular.clear();
        }

        @NonNull
        List< ? extends FileObject> getRoots() {
            return new ArrayList<>(roots);
        }

        @NonNull
        List< ? extends Boolean> getIsModules() {
            return new ArrayList<>(isModular);
        }

        @Override
        public void publish(LogRecord record) {
            final String msg = record.getMessage();
            if (msg != null) {
                switch (msg) {
                    case "rootCache updated: {0}":
                        roots.add((FileObject)record.getParameters()[0]);
                        break;
                    case "modCache updated: {0}":
                        isModular.add((Boolean)record.getParameters()[0]);
                        break;
                }
            }
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }
    }
}
