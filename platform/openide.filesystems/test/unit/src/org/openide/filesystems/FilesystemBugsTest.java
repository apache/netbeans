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
package org.openide.filesystems;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author  pz97949
 */
public class FilesystemBugsTest extends NbTestCase {

    final String FOLDER1 = "F";
    final String FO1 = "testNFOADelete";
    private int counter;

    /** Creates a new instance of FilesystemBugs */
    public FilesystemBugsTest(String name) {
        super(name);
    }

    /**
     * Let's have a file hierarchy A/B/C. If you decide to delete the folder A then listeners registered to
     * file objects B and C do not have any notion these file objects were deleted. IMO it is incorrect
     * behavior.
     * It also hurts the org.netbeans.core.xml.FileEntityResolver.update implementation.
     */
    public void testNotifyOfSubFoldersAfterDelete23929() throws Exception {
        counter = 0;
        // store references to FileObject to prevent garbage collection
        ArrayList<FileObject> garbageCollectorDefense = new ArrayList<FileObject>();
        if (canGenWriteFolder()) {
            // create tree an register listener
            //
            FileObject folder = getWriteFolder();
            log(folder.toString());
            folder = getSubFolder(folder, FOLDER1);
            FileObject tmpFolder = folder;
            garbageCollectorDefense.add(tmpFolder);
            for (int i = 0; i < 10; i++) {
                FileObject subFolder = getSubFolder(tmpFolder, FOLDER1 + i);
                garbageCollectorDefense.add(subFolder);
                subFolder.addFileChangeListener(new TestFileChangeListener());
                for (int j = 0; j < 10; j++) {
                    FileObject fo = getFileObject(tmpFolder, FO1 + j);
                    garbageCollectorDefense.add(fo);
                    fo.addFileChangeListener(new TestFileChangeListener());
                }
                tmpFolder = subFolder;
            }
            // delete tree and check counts of calls (must be 209 - event fired
            // from FileObject itself and its parent)
            try {
                folder.delete();
                assertTrue("test failed, deleted  " + counter + " != 209", counter == 209);
            } catch (Exception e) {
                fail("Cannot delete folder " + folder);
            }
        } else {
            log("[OK]  cannot get write folder on " + getFSType());
        }
    }

    /** get/create subfolder in folder. */
    protected FileObject getSubFolder(FileObject folder, String name) {
        try {
            FileObject fo = folder.getFileObject(name);
            if (fo == null) {
                return folder.createFolder(name);
            }
            return fo;
        } catch (IOException e) {
            e.printStackTrace();
            assertTrue("cannot get subFolder " + name + " in  " + folder.toString(), false);
        }
        return null;
    }

    /** get/create FilObject in folder */
    protected FileObject getFileObject(FileObject folder, String name) {
        try {
            FileObject fo = folder.getFileObject(name);
            if (fo == null) {
                return folder.createData(name);
            }
            return fo;
        } catch (Exception e) {
            assertTrue("cannot get subFolder " + name + " in  " + folder.toString(), false);
        }
        return null;
    }

    private class TestFileChangeListener extends FileChangeAdapter {

        @Override
        public void fileDeleted(FileEvent ev) {
            log("Delete: " + ev.getFile().getPath());
            counter++;
        }
    }

    protected boolean canGenWriteFolder() {
        return true;
    }

    protected FileObject getWriteFolder() throws Exception {
        clearWorkDir();
        LocalFileSystem lfs = new LocalFileSystem();
        lfs.setRootDirectory(getWorkDir());
        return lfs.getRoot();
    }

    protected String getFSType() {
        return "LocalFileSystem";
    }

    /** #8124 When attributes are deleted from file objects, so that no attributes remain set
     *  on any file objects in a folder, the .nbattrs file should be deleted. When no
     *   attributes remain on a particular file object, that <fileobject> tag should be
     *   deleted from the .nbattrs even if others remain.
     */
    public void testDeleteAttrsFileAfterDeleteAttrib() throws Exception {
        FileObject folder = getWriteFolder();
        FileObject fo = folder.getFileObject("testAttr");
        if (fo == null) {
            fo = folder.createData("testAttr");
        }
        // set any attribute
        fo.setAttribute("blbost", "blbost");
        // flush
        System.gc();
        System.gc();
        // delete all attributes
        FileObject fos[] = folder.getChildren();
        for (int i = 0; i < fos.length; i++) {
            Enumeration keys = fos[i].getAttributes();
            while (keys.hasMoreElements()) {
                fos[i].setAttribute((String) keys.nextElement(), null);
            }
        }
        // flush
        System.gc();
        System.gc();
        // test if exists .nbattrs
        File nbattrs = new File(FileUtil.toFile(folder), ".nbattrs");
        assertTrue("Empty nbattrs exists in folder:" + FileUtil.toFile(folder), nbattrs.exists() == false);


    }

    /** #30397 FileURL.encodeFileObject(FileSystem fs, FileObject
    fo) breakes the public contract of java.net.URL by
    setting the hostname as null value. Even if the
    URL constructor implementation permits passing
    null value the javadoc does not. It has fatal
    impact on comparisons of URL objects then (see
    URLStreamHandler.hostsEqual). Following code
    always fails
     */
    public void testURLContract() throws Exception {
        FileObject fo = getWriteFolder();
        URL u = fo.toURL();
        assertEquals(u, new URL(u.toExternalForm()));
    }

    /** http://installer.netbeans.org/issues/show_bug.cgi?id=26400
    If I have a multifilesystem and set an attribute
    on one of its fileobjects, I will get strange new
    attributes on all parent folders of this fileobject.

    Example will make it clear:

    If I set attribute "version" on fileobject fileA
    which lies in folders /folder1/folder2, then if
    you ask for all attributes on folder2, you will
    get an attribute with name "fileA\version".
    Similarly on folder1 you will get attribute
    "folder2\fileA\version". If I look into .nbattrs
    file there is just one attribute with name
    "folder1\folder2\fileA\version".
     */
    public void testMultiAttrsBug26400() throws Exception {
        File f1, f2;
        clearWorkDir();
        File dir = new File(getWorkDir(), "aret");
        dir.mkdirs();
        assertTrue(dir.isDirectory());
        f1 = new File(dir, "tm26400a");
        f2 = new File(dir, "tm26400b");
        f1.mkdir();
        f2.mkdir();
        LocalFileSystem lfs1 = new LocalFileSystem();
        LocalFileSystem lfs2 = new LocalFileSystem();
        lfs1.setRootDirectory(f1);
        lfs2.setRootDirectory(f2);
        ///

        MultiFileSystem mfs = new MultiFileSystem(lfs1, lfs2);
        FileObject rootMfs = mfs.getRoot();
        //    FileObject fomc = rootMfs.createData("c");
        FileObject folder = getSubFolder(rootMfs, "a");
        FileObject fo = getFileObject(folder, "b");
        fo.setAttribute("attr", "value");
        assertTrue("folder contains attribute", folder.getAttributes().hasMoreElements() == false);
        assertTrue("FileObject doesn't contain attribute attr.", fo.getAttribute("attr").equals("value"));
    }

    /**
    URL returned must be terminated by "/" if fileobject represents folder.
    Othervise it violated URL specs and it causes troubles while contructing contexted URL:

    new URL(folder.getURL(), "test.txt");

    is now always searched in parent folder.
     */
    public void testFolderSlashUrl() throws Exception {
        URL u = getWriteFolder().toURL();
        assertTrue("invalid url of directory", u.getPath().endsWith("/"));
    }

    public void testBackSlashAttribute33459() throws Exception {
        FileObject fo = getWriteFolder();
        String attribName = "y\\u2dasfas";
        System.gc();
        System.gc();
        try {
            fo.setAttribute(attribName, attribName);
            System.gc();
            System.gc();
            assertTrue("Attribute is not equal", fo.getAttribute(attribName).equals(attribName));
        } catch (Exception e) {
            log(e.toString());
            e.printStackTrace();
            fail("Exception :" + e);
        }
    }
}
