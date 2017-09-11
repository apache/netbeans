/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.spi.java.project.support;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.filesystems.test.TestFileUtils;

public class JavadocAndSourceRootDetectionTest extends NbTestCase {

    public JavadocAndSourceRootDetectionTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
    }

    public void testFindJavadocRoot() throws Exception {
        FileObject root = FileUtil.toFileObject(getWorkDir());
        TestFileUtils.writeFile(root, "javadoc-and-sources-detection/dist/javadoc/package-list", "some content");
        FileObject javadocRoot = JavadocAndSourceRootDetection.findJavadocRoot(root);
        assertNotNull(javadocRoot);
        assertEquals(root.getFileObject("javadoc-and-sources-detection/dist/javadoc"), javadocRoot);
        
        TestFileUtils.writeFile(root, "javadoc-and-sources-detection/src/org/netbeans/testpackage/Main.java", 
            "/*\n"+
            " * comment\n"+
            " */\n"+
            "package org.netbeans.testpackage;\n"+
            "public class Main {\n"+
            "}\n");

        FileObject sourcesRoot = JavadocAndSourceRootDetection.findSourceRoot(root);
        assertNotNull(sourcesRoot);
        assertEquals(root.getFileObject("javadoc-and-sources-detection/src"), sourcesRoot);

        URL url = JavadocAndSourceRootDetectionTest.class.getResource("../classpath/support/ProjectClassPathImplementationTest.class");
        assertNotNull(url);
        FileObject fo2 = URLMapper.findFileObject(url);
        assertNotNull(fo2);
        FileObject packageRoot = JavadocAndSourceRootDetection.findPackageRoot(fo2);
        assertNotNull(packageRoot);
        assertEquals("org/netbeans/spi/java/project/classpath/support/ProjectClassPathImplementationTest.class".
                replace('/', File.separatorChar), FileUtil.getRelativePath(packageRoot, fo2));

        FileObject libZip = FileUtil.createData(root, "a-lib.zip");
        TestFileUtils.writeZipFile(root, "a-lib.zip", 
                "a-library-version-1.0/docs/api/package-list:some content",
                "a-library-version-1.0/src/org/netbeans/foo/Main.java:package org.netbeans.foo;");
        FileObject lib = FileUtil.getArchiveRoot(libZip);
        sourcesRoot = JavadocAndSourceRootDetection.findSourceRoot(lib);
        assertNotNull(sourcesRoot);
        assertEquals(lib.getFileObject("a-library-version-1.0/src"), sourcesRoot);
        javadocRoot = JavadocAndSourceRootDetection.findJavadocRoot(lib);
        assertNotNull(javadocRoot);
        assertEquals(lib.getFileObject("a-library-version-1.0/docs/api"), javadocRoot);
    }

    public void testFindJavadocRoots() throws Exception {
        FileObject root = FileUtil.toFileObject(getWorkDir());
        final Set<String> expected = new TreeSet<>();
        expected.add(TestFileUtils.writeFile(root, "lib1/dist/javadoc1/package-list", "some content").getParent().getPath());
        expected.add(TestFileUtils.writeFile(root, "lib2/dist/javadoc2/package-list", "some content").getParent().getPath());
        expected.add(TestFileUtils.writeFile(root, "lib2/dist/javadoc3/package-list", "some content").getParent().getPath());
        expected.add(TestFileUtils.writeFile(root, "other/lib/dist/javadoc4/package-list", "some content").getParent().getPath());
        final Collection< ? extends FileObject> javadocRoots = JavadocAndSourceRootDetection.findJavadocRoots(root, null);
        final Set<String> result = new TreeSet<>();
        for (FileObject jr : javadocRoots) {
            result.add(jr.getPath());
        }
        assertEquals(expected, result);
    }

    public void testParsing() throws Exception {
        assertParse("/**\n * Some license here\n */\n\npackage foo;\n\npublic class Foo {}\n", false, "foo");
        assertParse("package foo;", false, "foo");
        assertParse("package foo; ", false, "foo");
        assertParse("/**/package foo;", false, "foo");
        assertParse("/***/package foo;", false, "foo");
        assertParse("/*****/package foo;", false, "foo");
        // would like to test stack overflow from #154894, but never managed to reproduce it in a unit test (only in standalone j2seproject)
    }


    public void testFindAllSourceRoots() throws Exception {
        FileUtil.setMIMEType("java", "text/x-java");
        final FileObject wd = FileUtil.toFileObject(getWorkDir());
        final FileObject root1 = FileUtil.createFolder(wd,"root1");
        final FileObject root2 = FileUtil.createFolder(wd,"foo/root2");
        final FileObject root3 = FileUtil.createFolder(wd,"foo/test/root3");
        final FileObject root4 = FileUtil.createFolder (wd,"root4");
        TestFileUtils.writeFile(root1,"org/me/Test1.java","package org.me; class Test1{}");
        TestFileUtils.writeFile(root2,"org/me/Test2.java","package org.me; class Test2{}");
        TestFileUtils.writeFile(root3,"org/me/Test3.java","package org.me; class Test3{}");
        TestFileUtils.writeFile(root4,"org/me/Test4.java","package org.me; class Test4{}");
        final List<FileObject> result = new ArrayList(JavadocAndSourceRootDetection.findSourceRoots(wd, null));
        final List<FileObject> expected = new ArrayList(Arrays.asList(
                root1,
                root2,
                root3,
                root4));
        final Comparator<FileObject> c = new Comparator<FileObject>() {
            public int compare(FileObject o1, FileObject o2) {
                return o1.getNameExt().compareToIgnoreCase(o2.getNameExt());
            }
        };
        Collections.sort(expected,c);
        Collections.sort(result,c);
        assertEquals (expected.toString(), result.toString());
    }
    
    public void testFindPackageRoot() throws Exception {
        final String testFileContent =
            "package org.netbeans.testpackage;\n"+      //NOI18N
            "public class Test {\n"+                    //NOI18N
            "}\n";                                      //NOI18N
        final FileObject workdir = FileUtil.toFileObject(getWorkDir());
        final FileObject folderRoot = workdir.createFolder("src");  //NOI18N
        FileObject testFile = TestFileUtils.writeFile(
            folderRoot,
            "org/netbeans/testpackage/Test.java",   //NOI18N
            testFileContent);
        assertEquals(folderRoot, JavadocAndSourceRootDetection.findPackageRoot(testFile));
        
        final FileObject zipRoot1 = createZipFile(
                workdir,
                "zipRoot1.zip",                         //NOI18N
                "org/netbeans/testpackage/Test.java",   //NOI18N
                testFileContent);
        testFile = zipRoot1.getFileObject("org/netbeans/testpackage/Test.java");  //NOI18N
        assertEquals(zipRoot1, JavadocAndSourceRootDetection.findPackageRoot(testFile));
        
        final FileObject zipRoot2 = createZipFile(
                workdir,
                "zipRoot2.zip",                             //NOI18N
                "src/org/netbeans/testpackage/Test.java",   //NOI18N
                testFileContent);
        testFile = zipRoot2.getFileObject("src/org/netbeans/testpackage/Test.java");  //NOI18N
        FileObject expected = zipRoot2.getFileObject("src");    //NOI18N
        assertNotNull(expected);
        assertEquals(expected, JavadocAndSourceRootDetection.findPackageRoot(testFile));
    }

    private void assertParse(String text, boolean packageInfo, String expectedPackage) {
        Matcher m = (packageInfo ? JavadocAndSourceRootDetection.PACKAGE_INFO : JavadocAndSourceRootDetection.JAVA_FILE).matcher(text);
        assertEquals("Misparse of:\n" + text, expectedPackage, m.matches() ? m.group(1) : null);
    }
    
    private static FileObject createZipFile (
            @NonNull final FileObject folder,
            @NonNull final String name,
            @NonNull final String path,
            @NonNull final String content) throws IOException {
        final FileObject zipRoot1 = folder.createData(name);
        final FileLock lock = zipRoot1.lock();
        try {
            final ZipOutputStream zout = new ZipOutputStream(zipRoot1.getOutputStream(lock));  //NOI18N            
            try {
                final String[] pathElements = path.split("/");  //NOI18N
                final StringBuilder currentPath = new StringBuilder();
                for (String element : pathElements) {
                    if (currentPath.length() > 0) {
                        currentPath.append('/');                //NOI18N
                    }
                    currentPath.append(element);
                    zout.putNextEntry(new ZipEntry(currentPath.toString()));
                }
                zout.write(content.getBytes(Charset.defaultCharset()));
            } finally {
                zout.close();
            }
        } finally {
            lock.releaseLock();
        }
        return FileUtil.getArchiveRoot(zipRoot1);
    }

}
