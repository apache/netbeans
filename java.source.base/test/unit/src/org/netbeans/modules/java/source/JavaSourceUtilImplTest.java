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
