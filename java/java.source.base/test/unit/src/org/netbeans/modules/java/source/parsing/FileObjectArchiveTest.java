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
package org.netbeans.modules.java.source.parsing;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import static junit.framework.TestCase.assertTrue;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Tomas Zezula
 */
public class FileObjectArchiveTest extends NbTestCase {

    private File root;

    public FileObjectArchiveTest(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        final File wd = FileUtil.normalizeFile(getWorkDir());
        root = createTestData(new File(wd,"root")); //NOI18N
    }

    public void testList() throws IOException {
        final FileObjectArchive archive = new FileObjectArchive(FileUtil.toFileObject(root));
        Iterable<JavaFileObject> res = archive.getFiles(
                "org/me",   //NOI18N
                null,
                EnumSet.of(JavaFileObject.Kind.CLASS),
                null,
                false);
        assertEquals(Arrays.asList("org.me.A", "org.me.B"), toInferedName(res));    //NOI18N
        res = archive.getFiles(
                "non-package/org/me",   //NOI18N
                null,
                EnumSet.of(JavaFileObject.Kind.CLASS),
                null,
                false);
        //Explicit list of non-package returns FileObejcts with prefix
        assertEquals(Arrays.asList("non-package.org.me.X", "non-package.org.me.Y"), toInferedName(res));    //NOI18N
    }

    public void testListRecursive() throws IOException {
        final FileObjectArchive archive = new FileObjectArchive(FileUtil.toFileObject(root));
        Iterable<JavaFileObject> res = archive.getFiles(
                "",   //NOI18N
                null,
                EnumSet.of(JavaFileObject.Kind.CLASS),
                null,
                true);
        assertEquals(Arrays.asList("org.me.A", "org.me.B"), toInferedName(res));    //NOI18N
        res = archive.getFiles(
                "non-package",   //NOI18N
                null,
                EnumSet.of(JavaFileObject.Kind.CLASS),
                null,
                true);
        //Explicit list of non-package returns FileObejcts with prefix
        assertEquals(Arrays.asList("non-package.org.me.X", "non-package.org.me.Y"), toInferedName(res));    //NOI18N
    }

    public void testGetDirectory() throws IOException {
        File dir1 = new File(new File(root, "dir1"), "a");
        assertTrue(dir1.mkdirs());
        File dir2 = new File(new File(root, "dir2"), "a");
        assertTrue(dir2.mkdirs());
        new FileOutputStream(new File(dir2, "test.txt")).close();
        final Archive a = new FileObjectArchive(FileUtil.toFileObject(root));
        assertEquals(dir1.toURI(), a.getDirectory("dir1/a"));
        assertEquals(dir2.toURI(), a.getDirectory("dir2/a"));
    }

    private static List<String> toInferedName(
            final Iterable<? extends JavaFileObject> jfos) {
        return StreamSupport.stream(jfos.spliterator(), false)
                .map((jfo) -> ((InferableJavaFileObject)jfo).inferBinaryName())
                .sorted()
                .collect(Collectors.toList());
    }

    private static File createTestData(@NonNull final File dest) throws IOException {
        dest.mkdir();
        final File om = new File(dest, "org/me");   //NOI18N
        om.mkdirs();
        final File np = new File(dest, "non-package/org/me");   //NOI18N
        np.mkdirs();
        new File(om,"A.class").createNewFile();
        new File(om,"B.class").createNewFile();
        new File(np,"X.class").createNewFile();
        new File(np,"Y.class").createNewFile();
        return dest;
    }
}
