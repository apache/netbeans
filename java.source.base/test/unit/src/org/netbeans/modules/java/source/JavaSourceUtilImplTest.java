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
package org.netbeans.modules.java.source;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.classfile.ClassFile;
import org.netbeans.modules.java.source.parsing.FileObjects;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Tomas Zezula
 */
public class JavaSourceUtilImplTest extends NbTestCase {
    
    private FileObject wd;
    private FileObject root;
    private FileObject java;
    
    public JavaSourceUtilImplTest(String name) {
        super(name);
    }
    
    
    @Before
    @Override
    public void setUp() throws Exception {
        clearWorkDir();
        wd = FileUtil.toFileObject(FileUtil.normalizeFile(getWorkDir()));
        root = FileUtil.createFolder(wd, "src");    //NOI18N
        java = createFile(root, "org/nb/A.java","package nb;\n class A {}");    //NOI18N
    }
    
    @Test
    public void testGenerate() throws Exception {
        assertNotNull(root);
        assertNotNull(java);
        final Map<String, byte[]> res = new JavaSourceUtilImpl().generate(root, java, "package nb;\n class A { void foo(){}}", null);   //NOI18N
        assertNotNull(res);
        assertEquals(1, res.size());
        Map.Entry<String,byte[]> e = res.entrySet().iterator().next();
        assertEquals("nb.A", e.getKey());   //NOI18N
        final ClassFile cf = new ClassFile(new ByteArrayInputStream(e.getValue()));
        assertEquals(2, cf.getMethodCount());
        final Set<String> methods = cf.getMethods().stream()
                .map((m) -> m.getName())
                .collect(Collectors.toSet());
        assertEquals(
                new HashSet<>(Arrays.asList(new String[]{
                    "<init>",   //NOI18N
                    "foo"       //NOI18N
                })),
                methods);
    }

    private static FileObject createFile(
            final FileObject root,
            final String path,
            final String content) throws Exception {
        FileObject file = FileUtil.createData(root, path);
        TestUtilities.copyStringToFile(file, content);
        return file;
    }
    
    private static void dump(
            final FileObject wd,
            final Map<String,byte[]> clzs) throws IOException {
        for (Map.Entry<String,byte[]> clz : clzs.entrySet()) {
            final String extName = FileObjects.convertPackage2Folder(clz.getKey());
            final FileObject data = FileUtil.createData(wd, String.format(
                    "%s.class", //NOI18N
                    extName));
            FileLock l = data.lock();
            try (final OutputStream out = data.getOutputStream(l)) {
                out.write(clz.getValue());
            }finally {
                l.releaseLock();
            }
        }
        System.out.printf("Dumped into: %s%n", FileUtil.getFileDisplayName(wd));
    }
}
