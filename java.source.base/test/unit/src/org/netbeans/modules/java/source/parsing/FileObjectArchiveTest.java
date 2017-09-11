/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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
 */
package org.netbeans.modules.java.source.parsing;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
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
