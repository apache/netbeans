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

package org.netbeans.upgrade;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.MultiFileSystem;
import org.openide.filesystems.XMLFileSystem;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;

/** Tests to check that copy of files works.
 *
 * @author Jaroslav Tulach
 */
public final class CopyTest extends org.netbeans.junit.NbTestCase {
    public CopyTest (String name) {
        super (name);
    }
    
    @Override
    protected void setUp() throws java.lang.Exception {
        super.setUp();
        
        clearWorkDir();
    }    
    
    public void testCopy () throws Exception {
        copyTest ("path/X.txt");        
    }
    
    public void testDoesDeepCopy () throws Exception {
        copyTest ("path/dir/subdir/deepDir/X.txt");
    }
    
    public void testCopyAttributes () throws Exception {
        copyTest (true, "path/X.txt");
    }    

    public void testCopyFolderAttributes () throws Exception {
        copyTest (true, new String[]{"path/folder/","path/folder/f.txt"});
    }    
    
    public void testDoNotCopyEmptyDirs () throws Exception {
        copyTest("path/emptyDir/");
    }
    
    public void testDoNotCopyEmptyDirs2 () throws Exception {
        copyTest(new String[] {"path/emptyDir/", "path/emptyDir/emptyDir2/"});
    }
    
    public void testDoesCopyHiddenFiles () throws Exception {
        String[] res = {
            "root/Yes.txt", 
            "root/X.txt_hidden", 
        };
        LocalFileSystem fs = createLocalFileSystem (res);
        URL url = getClass().getResource("layer5.5.xml");
        assertNotNull("found sample layer", url);
        XMLFileSystem xfs = new XMLFileSystem(url);
        
        MultiFileSystem mfs = new MultiFileSystem(new org.openide.filesystems.FileSystem[]{fs, xfs}) {
            {
                setPropagateMasks(true);
            }
        };
        
        FileObject fo = mfs.findResource ("root");
        
        FileSystem original = FileUtil.createMemoryFileSystem();
        
        MultiFileSystem tgfs = new MultiFileSystem(new FileSystem[] { fs, original });
        FileObject tg = tgfs.getRoot().createFolder ("target");
        FileObject toBeHidden = FileUtil.createData(original.getRoot(), "target/X.txt");
        
        assertEquals ("One file is there", 1, tg.getChildren().length);
        assertEquals ("X.txt", tg.getChildren()[0].getNameExt());
        
        
        Set<String> set = Set.of("root/Yes.txt", "root/X.txt_hidden");
        Copy.copyDeep (fo, tg, set);
        
        assertEquals("After the copy there is still one file", 1, tg.getFileObject("root").getChildren().length);
        assertEquals ("but the file is Yes.txt, as X.txt is hidden by txt_hidden", "Yes.txt", tg.getFileObject("root").getChildren()[0].getNameExt());
    }
    
    private static void writeTo (FileSystem fs, String res, String content) throws java.io.IOException {
        FileObject fo = org.openide.filesystems.FileUtil.createData (fs.getRoot (), res);
        org.openide.filesystems.FileLock lock = fo.lock ();
        try (OutputStream os = fo.getOutputStream(lock)) {
            os.write(content.getBytes());
        }
        lock.releaseLock ();
    }
    
    public LocalFileSystem createLocalFileSystem(String[] resources) throws IOException {
        File mountPoint = new File(getWorkDir(), "tmpfs"); 
        mountPoint.mkdir();
        
        for (int i = 0; i < resources.length; i++) {                        
            File f = new File (mountPoint,resources[i]);
            if (f.isDirectory() || resources[i].endsWith("/")) {
              FileUtil.createFolder(f);
            } else {
              FileUtil.createData(f);  
            }
        }
        
        LocalFileSystem lfs = new LocalFileSystem();
        try {
            lfs.setRootDirectory(mountPoint);
        } catch (Exception ex) {}
        
        return lfs;
    }

    private void copyTest(String... pathXtxt) throws IOException {    
        copyTest(false, pathXtxt);
    }
    private void copyTest(boolean testAttribs, String... allPath) throws IOException {
        String atribName = "attribName";
        String testPath = allPath[0];
        ArrayList<String> fileList = new ArrayList<>();
        fileList.addAll(Arrays.asList(allPath));
        fileList.addAll(Arrays.asList("path/Yes.txt", "path/No.txt", "path/Existing.txt"));
        
        FileSystem fs = createLocalFileSystem(fileList.toArray(String[]::new));

        
        FileObject path = fs.findResource("path");
        assertNotNull(path);
        FileObject tg = fs.getRoot().createFolder("target");
        assertNotNull(tg);
        FileObject existing = FileUtil.createData(tg, "path/Existing.txt");
        assertNotNull(existing);
        writeTo (fs, "target/path/Existing.txt", "existing-content");
        
        FileObject toCopyOne = fs.findResource (testPath);
        boolean isFolder = toCopyOne.isFolder();
        boolean isEmptyFolder = isFolder && !toCopyOne.getData(true).hasMoreElements();
        assertNotNull(toCopyOne);
        if (testAttribs) {
            toCopyOne.setAttribute (atribName, atribName);
        }
        
        Set<String> set = new HashSet<>();
        for (String currentPath : allPath) {
            currentPath = currentPath.endsWith("/") ? currentPath.substring(0, currentPath.length()-1) : currentPath;
            set.add(currentPath);
        }

        set.add("path/Yes.txt");
        set.add("path/Existing.txt");
        org.netbeans.upgrade.Copy.copyDeep(path, tg, set);

        assertNotNull("file not copied: " + "path/Existing.txt", tg.getFileObject("path/Existing.txt"));        
        assertNotNull("file not copied: " + "path/Yes.txt", tg.getFileObject("path/Yes.txt"));
        assertNull("file not copied: " + "path/No.txt", tg.getFileObject("path/No.txt"));
        org.openide.filesystems.FileObject copiedOne = tg.getFileObject(testPath);

        FileObject copiedPath = tg.getFileObject("path");
        assertNotNull("file copied: " + "path", copiedPath);        
        assertEquals("file copied: " + testPath, isEmptyFolder, copiedOne == null);
        if (!isEmptyFolder) {
            String expected = testPath.endsWith("/") ? testPath.substring(0, testPath.length()-1) : testPath;
            assertEquals("file copied: " + testPath,  expected, org.openide.filesystems.FileUtil.getRelativePath(tg, copiedOne));
            if (testAttribs) {
                assertEquals ("attribute copied", atribName, copiedOne.getAttribute(atribName));                
            }            
        }
        
        
        byte[] arr = new byte[300];
        int len = existing.getInputStream ().read (arr);
        String content = new String (arr, 0, len);

        //testDoNotOverwriteFiles
        assertEquals ("The content is kept from project", content, "existing-content");
        
    }

    public void testDoUpgrade() throws Exception {
        File wrkDir = getWorkDir();
        clearWorkDir();
        File old = new File(wrkDir, "old");
        old.mkdir();
        File config = new File(old, "config");
        config.mkdir();
        
        LocalFileSystem lfs = new LocalFileSystem();
        lfs.setRootDirectory(config);
        // filesystem must not be empty, otherwise .nbattrs file will be deleted :(
        lfs.getRoot().createFolder("test");
        
        String oldVersion = "foo";
        
        URL url = AutoUpgradeTest.class.getResource("layer" + oldVersion + ".xml");
        XMLFileSystem xmlfs = new XMLFileSystem(url);
        
        MultiFileSystem mfs = new MultiFileSystem(
                new FileSystem[] { lfs, xmlfs }
        );
        
        String fooBar = "/foo/bar";
        
        FileObject fooBarFO = mfs.findResource(fooBar);
        String attrName = "color";
        String attrValue = "black";
        fooBarFO.setAttribute(attrName, attrValue);
        
        System.setProperty("netbeans.user", new File(wrkDir, "new").getAbsolutePath());
        
        doUpgrade(old, oldVersion);
        
        FileSystem dfs = FileUtil.getConfigRoot().getFileSystem();
        
        MultiFileSystem newmfs = new MultiFileSystem(
                new FileSystem[] { dfs, xmlfs }
        );
        
        FileObject newFooBarFO = newmfs.findResource(fooBar);
        assertNotNull(newFooBarFO);
        assertEquals(attrValue, newFooBarFO.getAttribute(attrName));
    }

    // method used to be part of AutoUpgrade but isn't used anymore
    // it improves coverage a bit but could be removed at some point
    private static void doUpgrade(File source, String oldVersion) throws Exception {        

        Set<String> includeExclude;
        try (Reader r = new InputStreamReader(AutoUpgradeTest.class.getResourceAsStream("copy" + oldVersion), StandardCharsets.UTF_8)) {
            includeExclude = IncludeExclude.create(r);
        }

        LocalFileSystem lfs = new LocalFileSystem();
        lfs.setRootDirectory(new File(source, "config"));

        XMLFileSystem xmlfs = new XMLFileSystem(AutoUpgradeTest.class.getResource("layer" + oldVersion + ".xml"));
        FileSystem old = createLayeredSystem(lfs, xmlfs);

        Copy.copyDeep(old.getRoot(), FileUtil.getConfigRoot(), includeExclude, PathTransformation.getInstance(oldVersion));
    }
    
    private static MultiFileSystem createLayeredSystem(final LocalFileSystem lfs, final XMLFileSystem xmlfs) {
        return new MultiFileSystem(new FileSystem[]{lfs, xmlfs}) {
            {
                setPropagateMasks(true);
            }
        };
    }
 }
