/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright © 2008-2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.testng.ui.actions;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.JavaDataLoader;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.SharedClassObject;

/**
 *
 * @author lukas
 */
public class RetoucheTestBase extends NbTestCase {

    private FileObject testFO;

    public RetoucheTestBase(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        final FileObject sd = SourceUtilsTestUtil.makeScratchDir(this);
        final FileObject src = sd.createFolder("src");
        ClassPathProvider cpp = new ClassPathProvider() {

            public ClassPath findClassPath(FileObject file, String type) {
                if (type.equals(ClassPath.SOURCE)) {
                    return ClassPathSupport.createClassPath(new FileObject[]{src});
                }
                if (type.equals(ClassPath.COMPILE)) {
                    return ClassPathSupport.createClassPath(new FileObject[0]);
                }
                if (type.equals(ClassPath.BOOT)) {
                    return createClassPath(System.getProperty("sun.boot.class.path"));
                }
                return null;
            }
        };
        SharedClassObject loader = JavaDataLoader.findObject(JavaDataLoader.class, true);

        SourceUtilsTestUtil.prepareTest(src, sd.createFolder("build"), sd.createFolder("cache"));
        SourceUtilsTestUtil.prepareTest(
                new String[]{},
                new Object[]{loader, cpp});
        testFO = FileUtil.createFolder(src, "sample/pkg/").createData("Test.java");
        TestUtilities.copyStringToFile(testFO,
                "package sample.pkg;\n" +
                "\n" +
                "public class Test {\n" +
                "\n" +
                "    @Deprecated\n" +
                "    void method() {\n" +
                "    }\n" +
                "\n" +
                "}\n");
    }

    protected FileObject getTestFO() {
        return testFO;
    }

    private static ClassPath createClassPath(String classpath) {
        StringTokenizer tokenizer = new StringTokenizer(classpath, File.pathSeparator);
        List<PathResourceImplementation> list = new ArrayList<PathResourceImplementation>();
        while (tokenizer.hasMoreTokens()) {
            String item = tokenizer.nextToken();
            File f = FileUtil.normalizeFile(new File(item));
            URL url = getRootURL(f);
            if (url != null) {
                list.add(ClassPathSupport.createResource(url));
            }
        }
        return ClassPathSupport.createClassPath(list);
    }

    private static URL getRootURL(File f) {
        URL url = null;
        try {
            if (isArchiveFile(f)) {
                url = FileUtil.getArchiveRoot(f.toURI().toURL());
            } else {
                url = f.toURI().toURL();
                String surl = url.toExternalForm();
                if (!surl.endsWith("/")) {
                    url = new URL(surl + "/");
                }
            }
        } catch (MalformedURLException e) {
            throw new AssertionError(e);
        }
        return url;
    }

    private static boolean isArchiveFile(File f) {
        // the f might not exist and so you cannot use e.g. f.isFile() here
        String fileName = f.getName().toLowerCase();
        return fileName.endsWith(".jar") || fileName.endsWith(".zip");    //NOI18N
    }
}
