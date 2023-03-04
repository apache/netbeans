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

package org.netbeans.modules.java.api.common.queries;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.queries.BinaryForSourceQuery;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.api.java.queries.UnitTestForSourceQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.queries.FileBuiltQuery;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.api.common.TestProject;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.test.TestFileUtils;
import org.openide.util.BaseUtilities;
import org.openide.util.test.MockLookup;

/**
 * Test for #105645 functionality: build/generated-sources/NAME/ roots.
 */
public class GeneratedSourceRootTest extends NbTestCase {

    public GeneratedSourceRootTest(String n) {
        super(n);
    }

    @Override
    protected void setUp() throws IOException {
        FileObject fo = FileUtil.getConfigFile("Services");
        if (fo != null) {
            fo.delete();
        }
        clearWorkDir();
        MockLookup.setInstances(TestProject.createProjectType());
    }

    public void testSourceRoots() throws Exception {
        Project p = createTestProject(true);
        FileObject d = p.getProjectDirectory();
        FileObject src = d.getFileObject("src");
        FileObject stuff = d.getFileObject("build/generated-sources/stuff");
        SourceGroup[] groups = ProjectUtils.getSources(p).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        assertEquals(2, groups.length);
        assertEquals(src, groups[0].getRootFolder());
        assertEquals(d.getFileObject("test"), groups[1].getRootFolder());
        ClassPath sourcePath = ClassPath.getClassPath(src, ClassPath.SOURCE);
        assertEquals(Arrays.asList(src, stuff), Arrays.asList(sourcePath.getRoots()));
        FileObject moreStuff = FileUtil.createFolder(d, "build/generated-sources/morestuff");
        final Set<FileObject> expected = new TreeSet<FileObject>(new FOComparator());
        expected.addAll(Arrays.asList(src, stuff, moreStuff));
        final Set<FileObject> result = new TreeSet<FileObject>(new FOComparator());
        result.addAll(Arrays.asList(sourcePath.getRoots()));
        assertEquals(expected, result);
        ClassPath compile = ClassPath.getClassPath(src, ClassPath.COMPILE);
        assertEquals(compile, ClassPath.getClassPath(stuff, ClassPath.COMPILE));
        assertEquals(compile, ClassPath.getClassPath(moreStuff, ClassPath.COMPILE));
        assertEquals(ClassPath.getClassPath(src, ClassPath.EXECUTE), ClassPath.getClassPath(stuff, ClassPath.EXECUTE));
        assertEquals(ClassPath.getClassPath(src, ClassPath.BOOT), ClassPath.getClassPath(stuff, ClassPath.BOOT));
        d.getFileObject("build").delete();
        assertEquals(Arrays.asList(src), Arrays.asList(sourcePath.getRoots()));
    }

    public void testMiscellaneousQueries() throws Exception {
        final Project p = createTestProject(true);
        ProjectManager.mutex().writeAccess(() -> {
            try {
                final UpdateHelper h = p.getLookup().lookup(TestProject.class).getUpdateHelper();
                final EditableProperties ep = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                ep.setProperty("encoding", "ISO-8859-2");   //NOI18N
                h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
                ProjectManager.getDefault().saveProject(p);
            } catch (IOException ioe) {
                throw new RuntimeException(ioe);
            }
        });
        FileObject d = p.getProjectDirectory();
        FileObject src = d.getFileObject("src");
        FileObject test = d.getFileObject("test");
        FileObject stuff = d.getFileObject("build/generated-sources/stuff");
        URL classes = new URL(d.toURL(), "build/classes/");
        URL testClasses = new URL(d.toURL(), "build/test/classes/");
        URL distJar = FileUtil.getArchiveRoot(BaseUtilities.toURI(FileUtil.normalizeFile(
                new File(FileUtil.toFile(d), "dist/x.jar".replace('/', File.separatorChar)))).toURL()); //NOI18N
        FileObject xgen = stuff.getFileObject("net/nowhere/XGen.java");
        assertEquals(Arrays.asList(src, stuff), Arrays.asList(SourceForBinaryQuery.findSourceRoots(classes).getRoots()));
        assertEquals(Arrays.asList(test), Arrays.asList(SourceForBinaryQuery.findSourceRoots(testClasses).getRoots()));
        assertEquals(Arrays.asList(classes, distJar), Arrays.asList(BinaryForSourceQuery.findBinaryRoots(src.toURL()).getRoots()));
        assertEquals(Collections.singletonList(testClasses), Arrays.asList(BinaryForSourceQuery.findBinaryRoots(test.toURL()).getRoots()));
        assertEquals(Arrays.asList(classes, distJar), Arrays.asList(BinaryForSourceQuery.findBinaryRoots(stuff.toURL()).getRoots()));
        assertEquals(Collections.singletonList(src.toURL()), Arrays.asList(UnitTestForSourceQuery.findSources(test)));
        assertEquals(Collections.singletonList(test.toURL()), Arrays.asList(UnitTestForSourceQuery.findUnitTests(src)));
        assertEquals("1.6", SourceLevelQuery.getSourceLevel(stuff));
        FileBuiltQuery.Status status = FileBuiltQuery.getStatus(xgen);
        assertNotNull(status);
        assertFalse(status.isBuilt());
        FileUtil.createData(d, "build/classes/net/nowhere/XGen.class");
        assertTrue(status.isBuilt());
        assertEquals("ISO-8859-2", FileEncodingQuery.getEncoding(xgen).name());
        // check also dynamic changes in set of gensrc roots:
        FileObject moreStuff = FileUtil.createFolder(d, "build/generated-sources/morestuff");
        FileObject ygen = FileUtil.createData(moreStuff, "net/nowhere/YGen.java");
        assertEquals(new HashSet<FileObject>(Arrays.asList(src, stuff, moreStuff)),
                new HashSet<FileObject>(Arrays.asList(SourceForBinaryQuery.findSourceRoots(classes).getRoots())));
        assertEquals(Arrays.asList(classes, distJar), Arrays.asList(BinaryForSourceQuery.findBinaryRoots(moreStuff.toURL()).getRoots()));
        // XXX should previously created Result objects fire changes? ideally yes, but probably unnecessary
        assertEquals("1.6", SourceLevelQuery.getSourceLevel(moreStuff));
        status = FileBuiltQuery.getStatus(ygen);
        assertNotNull(status);
        assertFalse(status.isBuilt());
        FileUtil.createData(d, "build/classes/net/nowhere/YGen.class");
        assertTrue(status.isBuilt());
        assertEquals("ISO-8859-2", FileEncodingQuery.getEncoding(ygen).name());
        d.getFileObject("build").delete();
        assertEquals(Arrays.asList(src), Arrays.asList(SourceForBinaryQuery.findSourceRoots(classes).getRoots()));
    }

    public void testFirstGenSrcAddedDynamically() throws Exception {
        Project p = createTestProject(false);
        FileObject d = p.getProjectDirectory();
        FileObject src = d.getFileObject("src");
        URL classes = new URL(d.toURL(), "build/classes/");
        URL distJar = FileUtil.getArchiveRoot(BaseUtilities.toURI(FileUtil.normalizeFile(
                new File(FileUtil.toFile(d), "dist/x.jar".replace('/', File.separatorChar)))).toURL()); //NOI18N
        ClassPath sourcePath = ClassPath.getClassPath(src, ClassPath.SOURCE);
        assertEquals(Arrays.asList(src), Arrays.asList(sourcePath.getRoots()));
        assertEquals(Arrays.asList(src), Arrays.asList(SourceForBinaryQuery.findSourceRoots(classes).getRoots()));
        // now add the first gensrc root:
        FileObject stuff = FileUtil.createFolder(d, "build/generated-sources/stuff");
        FileObject xgen = FileUtil.createData(stuff, "net/nowhere/XGen.java");
        assertEquals(Arrays.asList(src, stuff), Arrays.asList(sourcePath.getRoots()));
        assertEquals(ClassPath.getClassPath(src, ClassPath.COMPILE), ClassPath.getClassPath(stuff, ClassPath.COMPILE));
        assertEquals(Arrays.asList(src, stuff), Arrays.asList(SourceForBinaryQuery.findSourceRoots(classes).getRoots()));
        assertEquals(Arrays.asList(classes, distJar), Arrays.asList(BinaryForSourceQuery.findBinaryRoots(stuff.toURL()).getRoots()));
        FileBuiltQuery.Status status = FileBuiltQuery.getStatus(xgen);
        assertNotNull(status);
        assertFalse(status.isBuilt());
        FileUtil.createData(d, "build/classes/net/nowhere/XGen.class");
        assertTrue(status.isBuilt());
    }


    private Project createTestProject(boolean initGenRoot) throws Exception {
        final FileObject dir = FileUtil.toFileObject(getWorkDir());
        TestFileUtils.writeFile(dir, "src/net/nowhere/X.java",
                "package net.nowhere; public class X {}");
        TestFileUtils.writeFile(dir, "test/net/nowhere/XTest.java",
                "package net.nowhere; public class XTest {}");
        if (initGenRoot) {
            TestFileUtils.writeFile(dir, "build/generated-sources/stuff/net/nowhere/XGen.java",
                    "package net.nowhere; public class XGen {}");
        }
        final FileObject srcRoot = dir.getFileObject("src");
        assertNotNull(srcRoot);
        final FileObject testRoot = dir.getFileObject("test");
        assertNotNull(testRoot);
        return TestProject.createProject(dir,srcRoot, testRoot);
    }


    private static class FOComparator implements Comparator<FileObject> {
        @Override
        public int compare(FileObject o1, FileObject o2) {
            return o1.getName().compareTo(o2.getName());
        }

    }

}
