/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.utilities.project;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.Test;
import org.netbeans.api.project.Project;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataShadow;
import org.openide.util.Lookup;

/**
 *
 * @author jaras
 */
public class CopyPathToClipboardActionTest extends NbTestCase {

    List<DataObject> dataObjects;
    CopyPathToClipboardAction action;
    List<Project> projects = new ArrayList<Project>();

    public CopyPathToClipboardActionTest(String name) {
        super(name);
    }

    @Override
    public void setUp() throws IOException, PropertyVetoException {

        // create projects
        projects.add(createProject("projectDir1"));
        projects.add(createProject("projectDir2"));

        List l = new LinkedList<DataObject>();
        clearWorkDir();
        FileObject root = FileUtil.toFileObject(getWorkDir());
        FileObject test1 = root.createFolder("test1");
        FileObject test2 = root.createFolder("test2");
        FileObject test1a = test1.createFolder("A");
        FileObject test1aTestClassJava = test1a.createData("TestClass.java");
        FileObject test2DataTxt = test2.createData("data.txt");
        createZipFile(test2);
        DataObject shadowFile = createShadowFile(test2DataTxt);

        FileObject archiveZip = test2.getFileObject("archive.zip");
        assertTrue(FileUtil.isArchiveFile(archiveZip));
        FileObject archiveRoot = FileUtil.getArchiveRoot(archiveZip);
        assertNotNull(archiveRoot);
        FileObject test2ArchiveZipBTxt = archiveRoot.getFileObject("b.txt");

        // test1/A/TestClass.java
        l.add(DataObject.find(test1aTestClassJava));
        // test2/data.txt
        l.add(DataObject.find(test2DataTxt));
        // test2/archive.zip:b.txt
        l.add(DataObject.find(test2ArchiveZipBTxt));
        // /testDataShadows/testShadowFile -> test2/data.txt
        l.add(shadowFile);
        dataObjects = l;
        List<Lookup.Provider> context = new LinkedList<Lookup.Provider>();
        context.addAll(projects);
        context.addAll(dataObjects);
        action = new CopyPathToClipboardAction(context);
    }

    @Override
    public void tearDown() throws IOException {
        dataObjects = null;
        FileObject configRoot = FileUtil.getConfigRoot();
        FileObject shadowFile = configRoot.getFileObject(
                "testDataShadows/testShadowFile");
        if (shadowFile != null) {
            shadowFile.delete();
        }
        FileObject testDataShadowsFolder = configRoot.getFileObject(
                "testDataShadows");
        if (testDataShadowsFolder != null) {
            testDataShadowsFolder.delete();
        }
    }

    /**
     * Test of getAbsolutePath method, of class CopyPathToClipboardAction.
     */
    @Test
    public void testGetAbsolutePath() {
        assertTrue(action.getAbsolutePath(dataObjects.get(0)).matches(
                ".*test1[/\\\\]A[/\\\\]TestClass\\.java$")); //TestClass.java
        assertTrue(action.getAbsolutePath(dataObjects.get(1)).matches(
                ".*test2[/\\\\]data\\.txt$")); // data.txt
        assertTrue(action.getAbsolutePath(dataObjects.get(2)).matches(
                ".*test2[/\\\\]archive.zip" + File.pathSeparator
                + "b\\.txt$")); // ZIP file
        assertTrue(action.getAbsolutePath(dataObjects.get(3)).matches(
                ".*test2[/\\\\]data\\.txt$")); // Shadow File for data.txt
    }

    @Test
    public void testGetSelectedPathsForProjects() {
        List<String> paths = new ArrayList<String>(
                action.getSelectedPathsForProjects());
        assertTrue(paths.get(0).endsWith("projectDir1")); // path for project 1
        assertTrue(paths.get(1).endsWith("projectDir2")); // path for project 2
    }

    /**
     * Creates ZIP file archive.zip that contains three files, a.txt, b.txt and
     * c.txt.
     */
    private void createZipFile(FileObject parentFolder) throws IOException {
        OutputStream outStream = parentFolder.createAndOpen("archive.zip");
        ZipOutputStream zipStream = new ZipOutputStream(outStream);
        zipStream.putNextEntry(new ZipEntry("a.txt"));
        zipStream.write(new byte[]{1, 2, 4, 5, 6, 7, 8});
        zipStream.closeEntry();
        zipStream.putNextEntry(new ZipEntry("b.txt"));
        zipStream.write(new byte[]{1, 2, 4, 5, 6, 7, 8});
        zipStream.closeEntry();
        zipStream.putNextEntry(new ZipEntry("c.txt"));
        zipStream.write(new byte[]{1, 2, 4, 5, 6, 7, 8});
        zipStream.closeEntry();
        zipStream.close();
        outStream.close();
    }

    /**
     * Create shadow file /testDataShadows/testShadowFile in system filesystem
     * that references passed file object.
     */
    private DataObject createShadowFile(FileObject referencedFile)
            throws IOException {
        FileObject configRoot = FileUtil.getConfigRoot();
        FileObject testDataShadows = configRoot.createFolder("testDataShadows");
        DataFolder testDataShodowsFldr = DataFolder.findFolder(testDataShadows);
        DataObject testShadowFile = DataShadow.create(testDataShodowsFldr,
                "testShadowFile", DataObject.find(referencedFile));
        return testShadowFile;
    }

    public void testGetSelectedDataObjectPaths() {
        Collection<String> paths = action.getSelectedPathsForDataObjects();
        assertEquals("Duplicate shadow file should be ignored",
                3, paths.size());
        String[] pathsArray = paths.toArray(new String[paths.size()]);
        // check that collection is sorted
        assertTrue(pathsArray[0].contains("test1")); //test1/A/TestClass.java
        assertTrue(pathsArray[1].contains("archive.zip")); //test2/archive.zip?
        assertTrue(pathsArray[2].contains("data.txt")); //test2/data.txt
    }

    /**
     * Creates a mocked {@link Project} with the given dir.
     *
     * @param projectDir
     * @return
     */
    private Project createProject(String projectDir) {
        try {
            FileObject root = FileUtil.toFileObject(getWorkDir());
            final FileObject dir = root.createFolder(projectDir);
            return new Project() {
                @Override
                public FileObject getProjectDirectory() {
                    return dir;
                }

                @Override
                public Lookup getLookup() {
                    throw new UnsupportedOperationException("Not supported.");
                }
            };
        } catch (IOException ex) {
            throw new IllegalArgumentException("should not happen in test");
        }
    }
}
