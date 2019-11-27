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

package org.netbeans.modules.masterfs.filebasedfs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.JarOutputStream;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import javax.swing.filechooser.FileSystemView;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.modules.masterfs.filebasedfs.fileobjects.FileObj;
import org.netbeans.modules.masterfs.filebasedfs.fileobjects.FileObjectFactory;
import org.netbeans.modules.masterfs.filebasedfs.fileobjects.WriteLockUtils;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileObjectTestHid;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.JarFileSystem;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.Repository;
import org.openide.filesystems.TestBaseHid;
import org.openide.filesystems.URLMapper;
import org.openide.filesystems.test.StatFiles;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;
import org.openide.util.io.NbMarshalledObject;


public class BaseFileObjectTestHid extends TestBaseHid{
    public static final HashSet<String> AUTOMOUNT_SET = new HashSet<String>(Arrays.asList("set", "shared", "net", "java", "share", "home", "ws", "ade_autofs"));
    private static final Set<String> REMOTE_FSTYPES = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
    "nfs", "nfs4","autofs")));  //NOI18N
    private static final boolean CHECK_REMOTE_FSTYPES = true;
    private FileObject root;
    private Logger LOG;
    

    public BaseFileObjectTestHid(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        LOG = Logger.getLogger(BaseFileObjectTestHid.class.getName() + "." + getName());
        root = testedFS.findResource(getResourcePrefix());
    }

    @Override
    protected Level logLevel() {
        return Level.INFO;
    }

    @Override
    protected String[] getResources(String testName) {
        return new String[] {"testdir/ignoredir/nextdir/", 
                             "testdir/mountdir/nextdir/",
                             "testdir/mountdir2/nextdir/",
                             "testdir/mountdir2/fname/notestfile",
                             "testdir/mountdir2/fname2/testfile",
                             "testdir/mountdir4/file.ext",
                             "testdir/mountdir5/file.ext",
                             "testdir/mountdir6/file.ext",
                             "testdir/mountdir6/file2.ext",
                             "testdir/mountdir7/file2.ext",
                             "testdir/mountdir8/",
                             "testdir/mountdir9/",                
                             "testdir/mountdir10/",
        };
    }
    public void testLinks() throws Exception {
        if (Utilities.isWindows()) {
            return;
        }
        FileObject fo = root.getFileObject("testdir/mountdir9");
        
        File dir = FileUtil.toFile(fo);
        assertNotNull("Dir exists", dir);

        File file = new File(dir, "origFile");
        file.createNewFile();
        File dirLink = new File(dir.getAbsolutePath() + "_link");
        Process exec = Runtime.getRuntime().exec(new String[]{"ln", "-s",
                    dir.getAbsolutePath(), dirLink.getAbsolutePath()});
        exec.waitFor();
        exec.destroy();
        if (exec.exitValue() != 0) {
            assertFalse("May fail on not Unix", Utilities.isUnix());
            return;
        }
        if (!dirLink.exists()) {
            assertFalse("Link may not be created on not Unix", Utilities.isUnix());
            return;
        }
        
        FileObject linkDirFO = FileUtil.toFileObject(dirLink);
        String selfName = "../" + dirLink.getName() + "/" + file.getName();
        FileObject fileObject = linkDirFO.getFileObject(selfName);
        FileObject findResource =
                fileObject.getFileSystem().findResource(dirLink.getAbsolutePath() + "/"
                + selfName);
        assertEquals(fileObject, findResource);
        assertEquals(linkDirFO, fileObject.getParent());
    }

    public void testFileTypeNotRemembered() throws Exception {
        String newFileName = "test";

        FileObject parent = root.getFileObject("testdir/mountdir10");
        assertNotNull(parent);
        assertTrue(parent.isFolder());
        parent.getChildren();

        // create a folder
        assertTrue(parent.createFolder(newFileName).isFolder());

        File parentFile = FileUtil.toFile(parent);
        assertNotNull(parentFile);
        assertTrue(parentFile.getAbsolutePath(),parentFile.exists());
        File newFile = new File(parentFile, newFileName);
        assertTrue(newFile.getAbsolutePath(), newFile.exists());

        // externally delete the folder
        assertTrue(newFile.getAbsolutePath(), newFile.delete());
        assertFalse(newFile.exists());

        // create a file with the same name as the deleted folder
        assertTrue(newFile.getAbsolutePath(), new File(parentFile, newFileName).createNewFile());
        assertTrue(newFile.exists());

        parent.refresh();

        FileObject fo = FileUtil.toFileObject(newFile);
        assertTrue(newFile.getAbsolutePath(), fo.isData());
    }

    public void testFinePathsAroundRoot() throws IOException {
        FileObject fo = FileUtil.toFileObject(getWorkDir()).getFileSystem().getRoot();
        StringBuilder sb = new StringBuilder();
        deep(fo, 3, sb, true, AUTOMOUNT_SET);
        if (sb.indexOf("\\") >= 0) {
            fail("\\ is not allowed in getPath()s:\n" + sb);
        }
        if (sb.indexOf("//") >= 0) {
            fail("Two // are not allowed\n" + sb);
        }
    }

    public void testFineNamesAroundRoot() throws IOException {
        FileObject fo = FileUtil.toFileObject(getWorkDir()).getFileSystem().getRoot();
        StringBuilder sb = new StringBuilder();
        deep(fo, 3, sb, false, AUTOMOUNT_SET);
        if (sb.indexOf("\\") >= 0) {
            fail("\\ is not allowed in getName()s:\n" + sb);
        }
        if (sb.indexOf("//") >= 0) {
            fail("Two // are not allowed\n" + sb);
        }
        if (sb.indexOf("/\n") >= 0) {
            fail("There is a slash at end of line, which is not allowed\n" + sb);
        }
    }
    
    private static void deep(FileObject fo, int depth, StringBuilder sb, boolean path, Set<String> skipChildren) {
        if (depth-- == 0) {
            return;
        }
        sb.append("  ");
        String n;
        if (path) {
            n = fo.getPath();
        } else {
            n = fo.getNameExt();
        }
        if (n.length() > 1) {
            sb.append(n);
        }
        sb.append("\n");
        for (FileObject ch : fo.getChildren()) {
            if (!skipChildren.contains(ch.getNameExt()) && !isRemoteFS(ch)) {
                deep(ch, depth, sb, path, Collections.<String>emptySet());
            }
        }
    }
    
    public void testCaseSensitiveFolderRename() throws Exception {
        FileObject parent = root.getFileObject("testdir/mountdir10");
        List<FileObject> arr = Arrays.asList(parent.getChildren());
        final String up = parent.getName().toUpperCase();
        FileLock lock = parent.lock();
        try {
            parent.rename(lock, up, null);
        } finally {
            lock.releaseLock();
        }
        assertEquals("Capital name", up, parent.getNameExt());
        File real = FileUtil.toFile(parent);
        assertNotNull("Real file exists", real);
        assertEquals("It is capitalized too", up, real.getName());
        
        List<FileObject> now = Arrays.asList(parent.getChildren());
        assertEquals("Same children: ", arr, now);
    }
    public void testCaseSensitiveRenameEventForMasterFS() throws Exception {
        FileObject parent = root.getFileObject("testdir").createFolder("parent");
        FileObject file = parent.createData("origi.nal");
        file.addFileChangeListener(new FileChangeAdapter() {
            @Override
            public void fileRenamed(FileRenameEvent fe) {
                assertEquals("origi", fe.getName());
                assertEquals("nal", fe.getExt());
            }
        });
        FileLock lock = file.lock();
        file.rename(lock, "Origi", "nal");
        lock.releaseLock();
    }
    
    public void testRootToFileObject() throws Exception {
        FileObjectFactory fs = FileObjectFactory.getInstance(getWorkDir());
        assertNotNull(fs);
        FileObject root1 = fs.getRoot();
        assertNotNull(root1);
        assertNotNull(FileUtil.toFile(root1));
    }

    public void testMoveOfAFolderDoesNotTouchSubhierarchy() throws Exception {
        FileObjectFactory fs = FileObjectFactory.getInstance(getWorkDir());
        assertNotNull(fs);
        FileObject root1 = fs.getValidFileObject(getWorkDir(), FileObjectFactory.Caller.Others);

        FileObject where = root1.createFolder("else").createFolder("sub").createFolder("subsub");
        FileObject fo = root1.createFolder("something");
        FileObject kidTxt = fo.createData("kid.txt");
        FileObject kid = fo.createFolder("kid");
        File kidFile = FileUtil.toFile(kid);
        File kidTxtFile = FileUtil.toFile(kidTxt);

        accessMonitor = new StatFiles();
        FileLock lock = fo.lock();
        FileObject newFolder;
        try {
            newFolder = fo.move(lock, where, fo.getNameExt(), null);
        } finally {
            lock.releaseLock();
        }
        assertEquals("Subfolder", where, newFolder.getParent());

        assertNotNull("Folder found", newFolder.getFileObject("kid"));
        assertNotNull("File found", newFolder.getFileObject("kid.txt"));
        assertFalse("No longer valid file", kidTxt.isValid());
        assertFalse("No longer valid dir", kid.isValid());

        String msg = 
            accessMonitor.getResults().statResultStack(kidFile, StatFiles.ALL) + "\n" +
            accessMonitor.getResults().statResultStack(kidTxtFile, StatFiles.ALL) + "\n";
        final Set<File> files = accessMonitor.getResults().getFiles();
        if (files.contains(kidFile) || files.contains(kidTxtFile)) {
            fail(msg);
        }
    }
    
    public void testMoveKeepsLastModifiedDate() throws Exception {
        FileObjectFactory fs = FileObjectFactory.getInstance(getWorkDir());
        assertNotNull(fs);
        FileObject root1 = fs.getValidFileObject(getWorkDir(),
                FileObjectFactory.Caller.Others);

        FileObject where = root1.createFolder("else").createFolder("sub").createFolder(
                "subsub");
        FileObject fo = root1.createFolder("something");
        FileObject nestedTxt = fo.createData("nested.txt");
        FileObject simpleTxt = root1.createData("simple.txt");
        File nestedTxtFile = FileUtil.toFile(nestedTxt);
        File simpleTxtFile = FileUtil.toFile(simpleTxt);

        long origLastModifiedNested = nestedTxtFile.lastModified();
        long origLastModifiedSimple = simpleTxtFile.lastModified();

        Thread.sleep(1100);

        FileLock folderLock = fo.lock();
        try {
            fo.move(folderLock, where, fo.getNameExt(), null);
        } finally {
            folderLock.releaseLock();
        }
        FileLock simpleLock = simpleTxt.lock();
        try {
            simpleTxt.move(simpleLock, where, simpleTxt.getNameExt(), null);
        } finally {
            simpleLock.releaseLock();
        }

        FileObject nestedTarget = root1.getFileObject(
                "else/sub/subsub/something/nested.txt");
        FileObject simpleTarget = root1.getFileObject(
                "else/sub/subsub/simple.txt");

        assertEquals("LastModified date should be kept", origLastModifiedNested,
                FileUtil.toFile(nestedTarget).lastModified());
        assertEquals("LastModified date should be kept", origLastModifiedSimple,
                FileUtil.toFile(simpleTarget).lastModified());
    }

    public void testReadRecreatedFile() throws Exception {
        FileObject fo = root.getFileObject("testdir/mountdir4/file.ext");
        assertNotNull("File found properly", fo);
        String txt = fo.asText("UTF-8");
        
        File f = FileUtil.toFile(fo);
        
        f.delete();
        
        
        fo.getParent().refresh();
        
        assertFalse("No longer valid", fo.isValid());
        try {
            InputStream is = fo.getInputStream();
            fail("Should throw an exception: " + is);
        } catch (FileNotFoundException ex) {
            // ok
        }
        
        f.createNewFile();
        
        String newTxt = fo.asText("UTF-8");
        assertEquals("Empty text read even the file object is not valid anymore", "", newTxt);
        assertFalse("Still invalid", fo.isValid());
    }
    public void testWriteRecreatedFile() throws Exception {
        FileObject fo = root.getFileObject("testdir/mountdir4/file.ext");
        assertNotNull("File found properly", fo);
        String txt = fo.asText("UTF-8");
        
        File f = FileUtil.toFile(fo);
        
        f.delete();
        
        fo.getParent().refresh();
        
        assertFalse("No longer valid", fo.isValid());
        
        f.createNewFile();
        
        OutputStream os = fo.getOutputStream();
        os.write("Ahoj".getBytes());
        os.close();
        
        String newTxt = fo.asText("UTF-8");
        assertEquals("Text read even the file object is not valid anymore", "Ahoj", newTxt);
        assertFalse("Still invalid", fo.isValid());
    }
    
    public void testRefresh109490() throws Exception {
        final File wDir = new File(getWorkDir(), getName());
        wDir.mkdir();
        final FileObject wDirFo = FileUtil.toFileObject(wDir);
        final List<FileEvent> fileEvents = new ArrayList<FileEvent>();
        FileSystem fs = wDirFo.getFileSystem();
        FileChangeListener fListener = new FileChangeAdapter(){
            @Override
                public void fileDataCreated(FileEvent fe) {
                    super.fileDataCreated(fe);
                    fileEvents.add(fe);
                }            
            };
        try {
            fs.refresh(true); // catch and skip changes made in VCS metadata, they are not part of this test
            fs.addFileChangeListener(fListener);

            File file = new File(wDir, "testao.f");
            File file2 = new File(wDir, "testc1.f");
            assertEquals(file.hashCode(), file2.hashCode());
            wDirFo.getChildren();
            assertTrue(file.createNewFile());
            assertTrue(file2.createNewFile());
            assertEquals(0, fileEvents.size());
            fs.refresh(true);
            assertEquals(2, fileEvents.size());
            assertEquals(Arrays.asList(wDirFo.getChildren()).toString(), 2,wDirFo.getChildren().length);
            assertTrue(Arrays.asList(wDirFo.getChildren()).toString().indexOf(file.getName()) != -1);            
            assertTrue(Arrays.asList(wDirFo.getChildren()).toString().indexOf(file2.getName()) != -1);                        
            
        } finally {
            fs.removeFileChangeListener(fListener);
        }
    }
    public void testOnWindowsIssue118874 () throws Exception {
        if (!Utilities.isWindows()) return;
        File f = getWorkDir();
        FileObject fo = FileUtil.toFileObject(f);
        FileObject[] childs = fo.getChildren();
        assertNotNull(fo);

        FileSystem fs = fo.getFileSystem();
        assertNotNull(fs);
        final Set<FileObject> s = new HashSet<FileObject>();
        fs.addFileChangeListener(new FileChangeAdapter(){
            @Override
            public void fileFolderCreated(FileEvent fe) {
                s.add(fe.getFile());
            }            
        });
        
        File test = new File(f, getName());
        assertFalse(test.exists());
        test = new File(test.getAbsolutePath().toLowerCase());
        assertFalse(test.exists());
        FileObject foTest = FileUtil.toFileObject(test);
        assertNull(foTest);
                
        FileObject testFo = FileUtil.createFolder(test);
        assertNotNull(testFo);
        assertEquals(1, s.size()); 
    }
    
    public void testExternalDelete96433 () throws Exception {
        File f = getWorkDir();
        FileObject fo = FileUtil.toFileObject(f);
        assertNotNull(fo);
        File testFolder = new File(f,"testfold");
        FileObject testFo = FileUtil.createFolder(testFolder);
        FileUtil.createData(testFo, "a");
        FileUtil.createData(testFo, "b");
        FileUtil.createData(testFo, "c");        
        FileUtil.createData(testFo, "d");
        FileUtil.createData(testFo, "e");        
        FileUtil.createData(testFo, "f");        
        FileObject[] childs = testFo.getChildren();
        assertEquals(6, childs.length);
        final List<FileObject> l = new ArrayList<FileObject>();
        FileChangeListener fclFS = new FileChangeAdapter(){
            @Override
            public void fileDeleted(FileEvent fe) {
                l.add(fe.getFile());
            }            
        };
        FileChangeListener fclFo = new FileChangeAdapter(){
            @Override
            public void fileDeleted(FileEvent fe) {
                fe.getFile().getChildren();
                fe.getFile().getParent().getChildren();
                Enumeration<? extends FileObject> en =  fe.getFile().getParent().getChildren(true);
                while(en.hasMoreElements()) {
                    if (fe.getFile().equals(en.nextElement())) {
                        fail(fe.getFile().getPath());
                    }
                }
            }            
        };
        testFo.refresh();
        testFo.getFileSystem().refresh(false);                
        for (int i = 0; i < childs.length; i++) {
            FileObject fileObject = childs[i];
            fileObject.refresh();
        }

        testFo.addFileChangeListener(fclFo);               
        testFo.getFileSystem().addFileChangeListener(fclFS);
        try {
            File[] files = testFolder.listFiles();
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                assertTrue(file.delete());
            }
            assertEquals(0,testFolder.list().length);
            assertTrue(testFolder.delete());
            fo.getFileSystem().refresh(false);
            assertEquals(7,l.size());
            fo.getFileSystem().refresh(false);        
            assertEquals(7,l.size());
        } finally {
            testFo.getFileSystem().removeFileChangeListener(fclFo);
            testFo.getFileSystem().removeFileChangeListener(fclFS);
            testFo.refresh();
            testFo.getFileSystem().refresh(false);
            for (int i = 0; i < childs.length; i++) {
                FileObject fileObject = childs[i];
                fileObject.refresh();
            }            
        }
    }
    
    public void  testCreateNotExistingFolderOrDataFile() throws IOException {
        final File wDir = getWorkDir();
        final File fold = new File(wDir,"a/b/c");
        final File data = new File(fold,"c.data");
        implCreateFolderOrDataFile(fold, data);        
    }

    public void  testCreateExistingFolderOrDataFile() throws IOException {
        final File wDir = getWorkDir();
        final File fold = new File(wDir,"a/b/c");
        final File data = new File(fold,"c.data");

        assertTrue(fold.mkdirs());
        assertTrue(data.createNewFile());                
                
        implCreateFolderOrDataFile(fold, data);        
    }

    public void  testCreateFolderOrDataFileExternalChange() throws IOException {
        final File wDir = getWorkDir();
        final File fold = new File(wDir,"a/b/c");
        final File data = new File(fold,"c.data");
        createFiles(data, fold);                
                
        implCreateFolderOrDataFile(fold, data);        
        
        FileObject foldFo = FileUtil.toFileObject(fold);
        FileObject dataFo = FileUtil.toFileObject(data);
        assertNotNull(foldFo);
        assertNotNull(dataFo);
        deleteFiles(data, wDir);
        
        implCreateFolderOrDataFile(fold, data);                

        deleteFiles(data, wDir);       
        foldFo.getFileSystem().refresh(false);
        foldFo = FileUtil.toFileObject(fold);
        dataFo = FileUtil.toFileObject(data);
        assertNull(foldFo);
        assertNull(dataFo);
        createFiles(data, fold);                

        implCreateFolderOrDataFile(fold, data);                        
    }
    //disabled because may cause that dialog is poped up on Windows promting user to put floppy disk in
    /*public void  testCreateFolderOrDataFileWithNotExistingRoot() throws Exception {
        File wDir = null;
        for (char d = 'A'; d < 'Z'; d++ ) {
            wDir = new File(String.valueOf(d)+":/");
            if (!wDir.exists()) {
                break;
            }
        }
        final File fold = new File(wDir,"a/b/c");
        final File data = new File(fold,"c.data");
        try {
            implCreateFolderOrDataFile(fold, data);        
            fail();
        } catch (IOException ex) {            
        }        
    }*/

    public void testCreateFolderOrDataFile_ReadOnly() throws Exception {
        clearWorkDir();
        final File wDir = getWorkDir();
        final File fold = new File(new File(new File(wDir,"a"), "b"), "c");
        final File data = new File(new File(new File(fold,"a"), "b"), "c.data");
        final boolean makeReadOnly = wDir.setReadOnly();
        if (!makeReadOnly && Utilities.isWindows()) {
            // According to bug 6728842: setReadOnly() only prevents the 
            // directory to be deleted on windows, does not prevent files
            // to be create in it. Thus the test cannot work on Windows.
            return;
        }
        assertTrue("Can change directory to read only: " + wDir, makeReadOnly);
        assertFalse("Cannot write", wDir.canWrite());
        try {
            implCreateFolderOrDataFile(fold, data);        
            fail("Creating folder or data should not be allowed: " + data);
        } catch (IOException ex) {            
        } finally {
            assertTrue(wDir.setWritable(true));
        }
    }

    public void testCannotLockReadOnlyFile() throws Exception {
        clearWorkDir();
        final File wDir = getWorkDir();
        final File data = new File(wDir,"c.data");
        data.createNewFile();
        data.setReadOnly();
        FileObject fd = FileUtil.toFileObject(data);
        try {
            FileLock lock = fd.lock();
            fail("Shall not be possible to create a lock: " + lock);
        } catch (IOException ex) {
        }
    }
    
    private static void createFiles(final File data, final File fold) throws IOException {
        assertTrue(fold.mkdirs());
        assertTrue(data.createNewFile());                
    }

    private static void deleteFiles(final File data, final File wDir) {        
        File tmp = data;
        while(!tmp.equals(wDir)) {
            assertTrue(tmp.delete());    
            tmp = tmp.getParentFile(); 
        }                        
    }
        
    private void implCreateFolderOrDataFile(final File fold, final File data) throws IOException {        
        FileObject foldFo = FileUtil.createFolder(fold);
        assertNotNull(foldFo);        
        assertTrue(foldFo.isValid());
        assertNotNull(FileUtil.toFile(foldFo));        
        assertEquals(FileUtil.toFile(foldFo),fold);                
        assertTrue(foldFo.isFolder());        
        
        FileObject dataFo = FileUtil.createData(data);         
        assertNotNull(dataFo);        
        assertTrue(dataFo.isValid());                
        assertNotNull(FileUtil.toFile(dataFo));        
        assertEquals(FileUtil.toFile(dataFo),data);                
        assertTrue(dataFo.isData());        
    }
    
    public void  testGetNameExt2() throws IOException {
        FileObject fold1 = FileUtil.createFolder(
                FileBasedFileSystem.getFileObject(getWorkDir()),getName());
        assertNotNull(fold1);
        testComposeNameImpl(fold1.createData("a.b"));
        testComposeNameImpl(fold1.createData(".b"));
        if (!Utilities.isWindows()) {
            testComposeNameImpl(fold1.createData("a."));
        }
    }

    private void testComposeNameImpl(FileObject fo) throws IOException {
        assertTrue(fo.isValid() && fo.isData());
        String fullName = fo.getNameExt();
        String ext = fo.getExt();
        String name = fo.getName();
        FileObject parent = fo.getParent();
        fo.delete();
        FileObject fo2 = parent.createData(name, ext);
        assertEquals(fullName, fo2.getNameExt());
        assertEquals(name, fo2.getName());
        assertEquals(ext, fo2.getExt());
    }

    public void testFileUtilToFileObjectIsValid() throws Exception {
        char SEP = File.separatorChar;
        final File fileF = new File(FileUtil.toFile(root).getAbsolutePath() + SEP + "dir" + SEP + "file2");
        File dirF = fileF.getParentFile();
        
        for (int cntr = 0; cntr < 10; cntr++) {
            boolean res = dirF.mkdir();
            LOG.log(Level.INFO, "Created directory {0} res: {1} round {2}", new Object[]{dirF, res, cntr});
            new FileOutputStream(fileF).close();
            LOG.log(Level.INFO, "Created file {0} exists: {1}", new Object[]{fileF, fileF.exists()});
            root.getFileSystem().refresh(false);
            final int validCntr = cntr;
            final List<Boolean> valid = Collections.synchronizedList(new ArrayList<Boolean>());
            LOG.log(Level.INFO, "Valid for round {0} allocated {1}", new Object[]{validCntr, valid});
            FileObject fo = FileUtil.toFileObject(fileF);
            LOG.log(Level.INFO, "file object {0} for {1} found", new Object[]{fo, fileF});
            fo.addFileChangeListener(new FileChangeListener() {
                @Override
                public void fileAttributeChanged(FileAttributeEvent fe) {
                    update(fe);
                }
                @Override
                public void fileChanged(FileEvent fe) {
                    update(fe);
                }
                @Override
                public void fileDataCreated(FileEvent fe) {
                    update(fe);
                }
                @Override
                public void fileDeleted(FileEvent fe) {
                    update(fe);
                }
                @Override
                public void fileFolderCreated(FileEvent fe) {
                    update(fe);
                }
                @Override
                public void fileRenamed(FileRenameEvent fe) {
                    update(fe);
                }
                
                private void update(FileEvent ev) {
                    LOG.log(Level.INFO, "event {0} arrived for {1} source: {2}", 
                        new Object[]{ev.getClass().getName(), ev.getFile(), ev.getSource()}
                    );
//                    LOG.log(Level.INFO, "created as ", ev.created);
                    FileObject fo;
                    File f = fileF;
                    
                    for (;;) {
                        fo = FileUtil.toFileObject(f);
                        LOG.log(Level.INFO, "Converting {0} to {1}", new Object[]{f, fo});
                        if (fo != null) {
                            break;
                        }
                        f = f.getParentFile();
                        LOG.log(Level.INFO, "Checking parent file {0}", f);
                    }
                    LOG.log(Level.INFO, "Is valid {0} for {1}", new Object[]{fo.isValid(), fo});
                    valid.add(fo.isValid());
                    LOG.log(Level.INFO, "Valid for round {1} is now: {0}", new Object[] { valid, validCntr });
                }
            });
            LOG.log(Level.INFO, "Listener attached to {0}", fo);
            LOG.info("About to perform delete");
            fileF.delete();
            dirF.delete();
            LOG.log(Level.INFO, "Delete of {0} and {1} is done", new Object[]{fileF, dirF});
            root.getFileSystem().refresh(false);
            LOG.log(Level.INFO, "Refresh finished and fo is: {0}", fo);
            
            List<Boolean> validClone = Arrays.asList(valid.toArray(new Boolean[0]));
            LOG.log(Level.INFO, "Valid for round {0} is now {1}", new Object[]{validCntr, validClone});
            LOG.log(Level.INFO, "Existing file objects {0}", FileObjectFactory.getInstance(fileF).dumpObjects());
            assertFalse("at least one event: " + validClone, validClone.isEmpty());
            
            for (boolean item : validClone) {
                assertTrue("valid=" + validClone + ", count=" + cntr, item);
            }
        }
    }
    
    public void testRefresh69744() throws Exception {
        File thisTest = new File(getWorkDir(),"thisFolder/thisFolder");
        thisTest.mkdirs();
        thisTest = new File(thisTest,"thisTest");
        thisTest.createNewFile();
        FileObject testf = FileUtil.toFileObject(thisTest);
        assertNotNull(testf);
        assertGC("",new WeakReference<FileObject>(testf.getParent()));
        modifyFileObject(testf, "abc");
        FileSystem fs = testf.getFileSystem();
        final List<FileEvent> l = new ArrayList<FileEvent>();
        FileChangeListener fcl = new FileChangeAdapter() {
            @Override
            public void fileChanged(FileEvent fe) {
                l.add(fe);
            }
        };
        Thread.sleep(1500);
        fs.addFileChangeListener(fcl);
        try {
            modifyFileObject(testf, "def");
            assertFalse(l.isEmpty());
        } finally {
            fs.removeFileChangeListener(fcl);
        }
    }
    
    private void modifyFileObject(final FileObject testf, String content) throws IOException {
        FileLock lock = null;
        OutputStream os = null;
        try {
            lock = testf.lock();
            os = testf.getOutputStream(lock);
            os.write(content.getBytes());
        } finally {
            if (os != null) os.close();
            if (lock != null) lock.releaseLock();            
        }
    }
    
    public void testCaseInsensitivity() throws Exception {
        if (!Utilities.isWindows()) return;
        
        File testa = new File(getWorkDir(), "a");
        File testA = new File(getWorkDir(), "A");
        
        if (testA.exists()) {
            assertTrue(testA.delete());
        }
        if (!testa.exists()) {
            assertTrue(testa.createNewFile());
        }

        //FileBasedFileSystem's case sensitivity depends on platform. This is different behaviour
        // than originally provided by AbstractFileSystem.
        FileObject A = root.getFileObject("A");
        assertNotNull(A);
        assertNotNull(root.getFileObject("a"));
        assertSame(root.getFileObject("A"), root.getFileObject("a"));
        assertSame(URLMapper.findFileObject(Utilities.toURI(testa).toURL()),
                URLMapper.findFileObject(Utilities.toURI(testA).toURL()));
        
        //but 
        root.getChildren();
        assertEquals("A",root.getFileObject("A").getName());        
        assertEquals("A",root.getFileObject("a").getName());        
    }

    private class TestListener extends FileChangeAdapter {
        private List<FileObject> fileObjects;
        TestListener(List<FileObject> fileObjects) {
            this.fileObjects = fileObjects;
        }
        @Override
        public void fileFolderCreated(FileEvent fe) {
            assertTrue(fileObjects.remove(fe.getFile())); 
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            assertTrue(fileObjects.remove(fe.getFile())); 
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
            assertTrue(fileObjects.remove(fe.getFile())); 
        }        
    }

    @RandomlyFails // NB-Core-Build #8165 (from FileBasedFileSystemWithExtensionsTest, in implOfTestGetFileObjectForSubversion): Expected: <null> but was: masterfs/build/test/unit/work/o.n.m.m.f.B/srr/subpackage@...
    public void testSimulatesRefactoringRename() throws Exception {
        assertNotNull(root);
        FileSystem fs = root.getFileSystem();
        assertNotNull(fs);        
        FileObject main = root.createData("Main.java");
        FileUtil.createData(root,"subpackage/newclass.java");
        final List<FileObject> fileObjects = new ArrayList<FileObject>();
        final Set<FileObject> allSubPackages = new HashSet<FileObject>();
        final TestListener tl = new TestListener(fileObjects);
        fs.addFileChangeListener(tl);
        try {
            fs.runAtomicAction(new FileSystem.AtomicAction(){
                @Override
                public void run() throws IOException {
                    FileObject subpackage = root.getFileObject("subpackage");
                    allSubPackages.add(subpackage);
                    FileObject newclass = subpackage.getFileObject("newclass.java");
                    FileObject subpackage1 = root.createFolder("subpackage1");
                    fileObjects.add(subpackage1);
                    allSubPackages.add(subpackage1);                    
                    FileObject newclass1 = subpackage1.createData("newclass.java");
                    fileObjects.add(newclass1);
                    subpackage.delete();
                    fileObjects.add(subpackage);
                    fileObjects.add(newclass);
                }
            });
        } finally {
            fs.removeFileChangeListener(tl);
        }
        assertTrue(fileObjects.isEmpty());
        assertNotNull(root.getFileObject("Main.java"));
        assertNotNull(root.getFileObject("subpackage1"));
        assertNotNull(root.getFileObject("subpackage1/newclass.java"));
        FileObjectTestHid.implOfTestGetFileObjectForSubversion(root, "subpackage");                                
        final String subpackageName = Utilities.isWindows() || Utilities.isMac() ?
            "subpackage2" : "Subpackage";
        fs.addFileChangeListener(tl);
        try {
             fs.runAtomicAction(new FileSystem.AtomicAction(){
                @Override
                public void run() throws IOException {
                    FileObject subpackage1 = root.getFileObject("subpackage1");
                    FileObject newclass = root.getFileObject("subpackage1/newclass.java");
                    FileObject Subpackage = root.createFolder(subpackageName);
                    allSubPackages.add(Subpackage);
                    assertEquals(3,allSubPackages.size());
                    
                    fileObjects.add(Subpackage);
                    FileObject newclass1 = Subpackage.createData("newclass.java");
                    fileObjects.add(newclass1);
                    subpackage1.delete();
                    fileObjects.add(subpackage1);
                    fileObjects.add(newclass);
                }
            });
        } finally {
            fs.removeFileChangeListener(tl);
        }
        assertTrue(fileObjects.isEmpty());
        assertNotNull(root.getFileObject("Main.java"));
        assertNotNull(root.getFileObject(subpackageName+"/newclass.java"));
        FileObjectTestHid.implOfTestGetFileObjectForSubversion(root, "subpackage1");                                        
        assertEquals(3,allSubPackages.size());
    }
    
    public void testRefresh60479 () throws Exception {
        final List<FileEvent> l = new ArrayList<FileEvent>();
        File rootFile = FileUtil.toFile(root);
        assertTrue(rootFile.exists());
        
        File testFile = new File (rootFile, "testRefresh60479.txt");
        assertTrue(testFile.createNewFile());
        assertTrue(testFile.exists());
        
        FileObject testFo = FileUtil.toFileObject(testFile);
        assertNotNull(testFo);
        FileLock lock = testFo.lock();        
        OutputStream os = null;
        
        try {
            os = testFo.getOutputStream(lock);
            os.write("abcdefgh".getBytes());
            lock.releaseLock();
            os.close();
            Thread.sleep(3000);
            os = new FileOutputStream(testFile);
            os.write("ijkl".getBytes());            
            os.close();            
        } finally {            
            if (lock != null && lock.isValid()) {
                lock.releaseLock();
            }
            if (os != null) {
                os.close();
            }
        }
        
        testFo.addFileChangeListener(new FileChangeAdapter(){
            @Override
            public void fileChanged(FileEvent fe) {
                l.add(fe);
            }
            
        });
        
        testFo.refresh(true);
        assertEquals(1,l.size());
    }
            
    public void testNormalization51910 () throws Exception {
        if (!Utilities.isWindows()) return;
        
        File rootFile = FileUtil.toFile(root);
        assertTrue(rootFile.exists());
        
        File testFile = new File (rootFile, "abc.txt");
        assertTrue(testFile.createNewFile());
        assertTrue(testFile.exists());
        
        File testFile2 = new File (rootFile, "ABC.TXT");
        assertTrue(testFile2.exists());
        
        
        assertEquals(Utilities.toURI(FileUtil.normalizeFile(testFile)).toURL(), Utilities.toURI(FileUtil.normalizeFile(testFile2)).toURL());
    }   

    @RandomlyFails // NB-Core-Build #7927 (from FileBasedFileSystemWithUninitializedExtensionsTest): Didn't receive a FileEvent on the parent.
    public void testEventsAfterCreatedFiles55550() throws Exception {
        FileObject parent = root.getFileObject("testdir/mountdir8");  
        assertNotNull(parent);
        assertTrue(parent.isFolder());
        parent.getChildren();
        
        File parentFile = FileUtil.toFile(parent);
        assertNotNull(parentFile);
        assertTrue(parentFile.getAbsolutePath(),parentFile.exists());
        File newFile = new File(parentFile, "sun-web.xml");
        assertFalse(newFile.getAbsolutePath(),newFile.exists());
                        
        class FCLImpl extends FileChangeAdapter {
            boolean created;
            @Override
            public void fileDataCreated(FileEvent e) {
                created = true;
                synchronized(BaseFileObjectTestHid.this) {
                    BaseFileObjectTestHid.this.notifyAll();
                }
            }
        }        
        FCLImpl fl = new FCLImpl();        
        parent.addFileChangeListener(fl);
        
        assertTrue(newFile.getAbsolutePath(), newFile.createNewFile());
        assertTrue(newFile.exists());
        
        // !!!! This is the source of the problem !!!
        // ask for the new file
        // remove this line ans the test passes
        FileUtil.toFileObject(newFile);
        
        
        parent.refresh();
        synchronized(this) {
            wait(1000);
        }
        parent.removeFileChangeListener(fl);
        assertTrue("Didn't receive a FileEvent on the parent.", fl.created);
    }
    
    public void testIssue49037 () throws Exception {
        assertNotNull(root);
        FileObject fo = root.getFileObject("testdir/");
        assertNotNull(fo);
        
        File f = FileUtil.toFile (fo);
        assertNotNull(f);
        
        File newFile = new File (f, "absolutelyNewFile");
        assertFalse(newFile.exists());
        
        new FileOutputStream (newFile).close();
        assertTrue(newFile.exists());
        assertNotNull(FileUtil.toFileObject(newFile));        
    }
    
        
    @SuppressWarnings("deprecation")
    public void testFileUtilFromFile () throws Exception {        
        assertNotNull(root);
        
        File f = FileUtil.normalizeFile(getWorkDir());
        IgnoreDirFileSystem ifs = new IgnoreDirFileSystem();
        ifs.setRootDirectory(f);
        
        Repository.getDefault().addFileSystem(ifs);
        Repository.getDefault().addFileSystem(testedFS);
        
        FileObject[] fos = FileUtil.fromFile(f);
        assertTrue(fos.length > 0);
        assertEquals(fos[0].getFileSystem(), testedFS );
        
    }
    
    public void testIssue45485 () {
        assertNotNull(root);        
        FileObject testdir = root.getFileObject("testdir.");        
        assertNull(testdir);
        // #176032
        testdir = root.getFileObject(".");
        assertNotNull(testdir);
        testdir = root.getParent();
        assertNotNull(testdir);
    }
    
    public void testDeleteNoCaptureExternalChanges () throws Exception {
        String externalName = "newfile.external";        
        assertNotNull(root);
        FileObject fileObject = root.getFileObject("testdir/mountdir5/file.ext");        
        assertNotNull(fileObject);
        File f = FileUtil.toFile(fileObject);
                        
        assertNotNull(f);
        f = FileUtil.normalizeFile(f);
        assertTrue(f.exists());
        assertTrue (f.delete());
        fileObject.refresh();
        
        assertFalse(fileObject.isValid());
    }
        
    public void testFindResourceNoCaptureExternalChanges () throws Exception {
        String externalName = "newfile.external";        
        assertNotNull(root);
        FileObject fileObject = root.getFileObject("testdir");        
        assertNotNull(fileObject);
        File f = FileUtil.toFile(fileObject);
        
        assertNull(testedFS.findResource(new File (f, externalName).getAbsolutePath().replace('\\',File.separatorChar)));
        assertNull(fileObject.getFileObject(externalName));
        
        assertNotNull(f);
        f = FileUtil.normalizeFile(f);
        assertTrue(f.exists());
        f = new File (f, externalName);
        assertTrue(!f.exists());       
        assertTrue(f.getAbsolutePath(),f.createNewFile());
        fileObject.refresh();
        assertNotNull(FileBasedFileSystem.getFileObject(f));
    }

    public void testGetFileObjectNoCaptureExternalChanges () throws Exception {
        String externalName = "newfile.external2";        
        assertNotNull(root);
        FileObject fileObject = root.getFileObject("testdir");        
        assertNotNull(fileObject);
        File f = FileUtil.toFile(fileObject);
        
        assertNull(FileBasedFileSystem.getFileObject(new File (f, externalName)));
        assertNull(fileObject.getFileObject(externalName));
        
        assertNotNull(f);
        f = FileUtil.normalizeFile(f);
        assertTrue(f.exists());
        f = new File (f, externalName);
        assertTrue(!f.exists());        
        assertTrue(f.getAbsolutePath(),f.createNewFile());
        fileObject.refresh();
        assertNotNull(fileObject.getFileObject(externalName));        
    }
    
    public void testToFileObjectCaptureExternalChanges () throws Exception {
        FileObject testFolder_Fo = FileUtil.toFileObject(getWorkDir()).createFolder(getName());        
        assertNotNull(testFolder_Fo);
        File testFolder = FileUtil.normalizeFile(FileUtil.toFile(testFolder_Fo));
        assertNotNull(testFolder);
        assertTrue(testFolder.exists());

        String externalName = "newfile.external3";                
        File newFile = new File (testFolder, externalName);
        assertFalse(newFile.exists());        
        assertNull(FileBasedFileSystem.getFileObject(newFile));
        assertNull(testFolder_Fo.getFileObject(newFile.getName()));
        assertNull(FileUtil.toFileObject(newFile));        
        
        assertTrue(newFile.createNewFile());
        assertNotNull(FileUtil.toFileObject(newFile));        
    }
    

    public void testGetFileObject47885 () throws Exception {
        assertNotNull(root);
        
        FileObject fileObject = root.getFileObject("testdir/mountdir4/file.ext");        
        assertNotNull(fileObject);
        
        fileObject = root.getFileObject("testdir/mountdir4/file", "ext");        
        assertNull(fileObject);
        
        fileObject = root.getFileObject("testdir\\mountdir4\\file.ext");        
        assertNull(fileObject);
    }
    
    
    public void testValidRoots () throws Exception {
        assertNotNull(testedFS.getRoot());    
        assertTrue(testedFS.getRoot().isValid());            
        
        FileSystemView fsv = FileSystemView.getFileSystemView();                
        File[] roots = File.listRoots();
        boolean validRoot = false;
        for (int i = 0; i < roots.length; i++) {
            FileObject root1 = FileUtil.toFileObject(roots[i]);
            if (!roots[i].exists()) {
               assertNull(root1);
               continue; 
            }
            
            assertNotNull(roots[i].getAbsolutePath (),root1);
            assertTrue(root1.isValid());
            if (testedFS == root1.getFileSystem()) {
                validRoot = true;
            }
        }
        assertTrue(validRoot);
    }
    
    public void testDeserializationOfMasterFSLeadsToTheSameFileSystem () throws Exception {
        NbMarshalledObject stream = new NbMarshalledObject (testedFS);
        Object obj = stream.get ();
        assertNotNull(obj);
        //assertSame ("After deserialization it is still the same", testedFS, obj);
    }
    

    public void testNormalizeDrivesOnWindows48681 () {
        if ((Utilities.isWindows () || (Utilities.getOperatingSystem () == Utilities.OS_OS2))) {
            File[] roots = File.listRoots();
            for (int i = 0; i < roots.length; i++) {
                File file = roots[i];
                if (FileSystemView.getFileSystemView().isFloppyDrive(file) || !file.exists()) {
                    continue;
                }
                File normalizedFile = FileUtil.normalizeFile(file);
                File normalizedFile2 = FileUtil.normalizeFile(new File (file, "."));
                
                assertEquals (normalizedFile.getPath(), normalizedFile2.getPath());
            }
            
        }
    }
    
    public void testJarFileSystemDelete () throws Exception {
        assertNotNull(root);
        FileObject folderFo = root.getFileObject("testdir/mountdir7");
        File folder = FileUtil.toFile(folderFo);
        assertNotNull(folder);
        
        File f = new File (folder,"jfstest.jar");        
        if (!f.exists()) {
            f.getParentFile().mkdirs();
            f.createNewFile();
        }
        JarOutputStream jos = new JarOutputStream (new FileOutputStream (f));        
        jos.putNextEntry(new ZipEntry ("a/b/c/c.txt"));
        jos.putNextEntry(new ZipEntry ("a/b/d/d.txt"));
                        
       jos.close();        

        FileObject parent = FileUtil.toFileObject(f.getParentFile());
        parent.getChildren();
        JarFileSystem jfs = new JarFileSystem  ();
        try {
            jfs.setJarFile(f);
        } catch (Exception ex) {
            fail ();
        }
        

        ArrayList<FileObject> all = new ArrayList<FileObject>();
        FileObject jfsRoot = jfs.getRoot();
        Enumeration<? extends FileObject> en = jfsRoot.getChildren(true);
        while (en.hasMoreElements()) {
            all.add ((FileObject) en.nextElement());                        
        }

        assertTrue (all.size() > 0); 
        
        final ArrayList<FileObject> deleted = new ArrayList<FileObject>();
        jfs.addFileChangeListener(new FileChangeAdapter() {
            @Override
            public void fileDeleted(FileEvent fe) {
                super.fileDeleted(fe);
                deleted.add (fe.getFile());
            }
        });
        
        Thread.sleep(1000);
        assertTrue (f.getAbsolutePath(), f.delete());
        parent.refresh();
        assertEquals (deleted.size(), all.size());

        for (int i = 0; i < all.size(); i++) {
            FileObject fileObject = all.get(i);
            assertFalse (fileObject.isValid());
        }
        
        
        assertFalse (jfsRoot.isValid());        
    }
    
        

    public void testLockFileAfterCrash() throws Exception {
        FileObject testFo = FileUtil.createData(root,"/testAfterCrash/testfile.data");
        File testFile = FileUtil.toFile(testFo);
  
        
        File lockFile = WriteLockUtils.getAssociatedLockFile(testFile);
        if (!lockFile.exists()) {
            assertTrue(lockFile.createNewFile());
        }
                
        assertTrue(lockFile.exists());

        FileObject lockFo = FileUtil.toFileObject(lockFile);        
        assertNull(lockFo);        
        testFo.delete();        
        
        
        lockFo = FileUtil.toFileObject(lockFile);        
        String msg = (lockFo != null) ? lockFo.toString() : "";
        assertNull(msg,lockFo);
    }

    public void testDeletedFileDoesNotReturnInputStream() throws Exception {
        final FileObject testFo = FileUtil.createData(root,"testfile.data");
        final File testFile = FileUtil.toFile(testFo);
        final Logger LOGGER = Logger.getLogger(FileObj.class.getName());
        final Handler handler = new Handler() {
            @Override
            public void publish(LogRecord record) {
                if ("FileObj.getInputStream_after_is_valid".equals(record.getMessage())) {
                    testFile.delete();
                }
            }
            @Override
            public void flush() {
            }
            @Override
            public void close() throws SecurityException {
            }
        };
        final Level originalLevel = LOGGER.getLevel();
        LOGGER.setLevel(Level.FINEST);
        try {
            LOGGER.addHandler(handler);
            try {
                testFo.getInputStream();
                assertTrue("Exception not thrown by deleted file getInputStream()", false);
            } catch (FileNotFoundException e) {
                //pass - expected exception
            } finally {
                LOGGER.removeHandler(handler);
            }
        } finally {
            LOGGER.setLevel(originalLevel);
        }
    }
    
    public void testDeepStructureDelete() throws Exception {
        clearWorkDir();
        
        final File rf = new File(getWorkDir(), "wd");
        rf.mkdirs();
        
        FileObject root = FileUtil.toFileObject(rf);
        FileObject next = root;
        for (int i = 0; i < 10; i++) {
            next = next.createFolder("i" + i);
        }
        
        assertTrue("Is valid", root.isValid());
        assertTrue("Is valid leaft", next.isValid());
        
        clearWorkDir();
        assertFalse("Root file is gone", rf.exists());
        
        root.refresh();
        
        assertFalse("Became invalid", root.isValid());
        assertFalse("Leaf is invalid as well", next.isValid());
    }

    private static boolean isRemoteFS (FileObject fo) {
        if (!CHECK_REMOTE_FSTYPES) {
            return false;
        }
        if (!fo.isFolder()) {
            return false;
        }
        final File f = FileUtil.toFile(fo);
        if (f == null) {
            return false;
        }
        final Path p = f.toPath();
        try {
            final String fsType = Files.getFileStore(p).type();
            return REMOTE_FSTYPES.contains(fsType);
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
            return false;
        }
    }
    
    private class IgnoreDirFileSystem extends LocalFileSystem {
        org.openide.filesystems.StatusDecorator status = new org.openide.filesystems.StatusDecorator() {
            @Override
            public String annotateName (String name, java.util.Set files) {
                StringBuilder sb = new StringBuilder (name);
                Iterator it = files.iterator ();
                while (it.hasNext()) {                    
                    FileObject fo = (FileObject)it.next();
                    try {
                        if (fo.getFileSystem() instanceof IgnoreDirFileSystem) {
                            sb.append(",").append (fo.getNameExt());//NOI18N
                        }
                    } catch (Exception ex) {
                        fail ();
                    }
                }
                                
                return sb.toString () ;
            }

            @Override
            public String annotateNameHtml(String name, Set files) {
                return annotateName (name, files);
            }            
            
        };        
        
        @Override
        public org.openide.filesystems.StatusDecorator getDecorator() {
            return status;
        }
        
        @Override
        protected String[] children(String name) {
            String[] strings = super.children(name);
            return strings;
        }
    }
}
