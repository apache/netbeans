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
package org.netbeans.modules.masterfs.filebasedfs.fileobjects;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;

public class ExternalTouchTest extends NbTestCase {
    private Logger LOG;
    private FileObject testFolder;

    public ExternalTouchTest(String testName) {
        super(testName);
    }

    @Override
    protected Level logLevel() {
        return Level.FINE;
    }
    
    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        
        LOG = Logger.getLogger("test." + getName());
        Logger.getLogger("org.openide.util.Mutex").setUseParentHandlers(false);

        File dir = new File(getWorkDir(), "test");
        dir.mkdirs();
        testFolder = FileUtil.toFileObject(dir);
        assertNotNull("Test folder created", testFolder);

    }

    public void testChangeInChildrenNoticed() throws Exception {
        long lm = System.currentTimeMillis();
        FileObject fileObject1 = testFolder.createData("fileObject1");
        assertNotNull("Just to initialize the stamp", lm);
        FileObject[] arr = testFolder.getChildren();
        assertEquals("One child", 1, arr.length);
        assertEquals("Right child", fileObject1, arr[0]);

        File file = FileUtil.toFile(fileObject1);
        assertNotNull("File found", file);
        Reference<FileObject> ref = new WeakReference<FileObject>(fileObject1);
        arr = null;
        fileObject1 = null;
        assertGC("File Object can disappear", ref);


        class L extends FileChangeAdapter {
            int cnt;
            FileEvent event;
            
            @Override
            public void fileChanged(FileEvent fe) {
                LOG.info("file change " + fe.getFile());
                cnt++;
                event = fe;
            }
        }
        L listener = new L();
        testFolder.addRecursiveListener(listener);

        Thread.sleep(1000);

        FileOutputStream os = new FileOutputStream(file);
        os.write(10);
        os.close();

        if (lm > file.lastModified() - 50) {
            fail("New modification time shall be at last 50ms after the original one: " + (file.lastModified() - lm));
        }

        testFolder.refresh();

        assertEquals("Change notified", 1, listener.cnt);
        assertEquals("Right file", file, FileUtil.toFile(listener.event.getFile()));
        assertEquals("Right source", file.getParentFile(), FileUtil.toFile((FileObject)listener.event.getSource()));
    }
    public void testFindResourceDoesNotRefresh() throws Exception {
        FileObject fileObject1 = testFolder.createData("fileObject1");
        FileObject[] arr = testFolder.getChildren();
        assertEquals("One child", 1, arr.length);
        assertEquals("Right child", fileObject1, arr[0]);
        
        File testFile = FileUtil.toFile(testFolder);
        assertNotNull("Folder File found", testFile);
        final String path = testFolder.getPath() + "/file1.txt";
        final FileSystem fs = testFolder.getFileSystem();
        
        File newCh = new File(testFile, "file1.txt");
        newCh.createNewFile();
        
        FileObject fromResource = fs.findResource(path);
        FileObject fromToFO = FileUtil.toFileObject(newCh);
        FileObject fromSndResource = fs.findResource(path);
        
        assertNotNull("toFileObject does refresh", fromToFO);
        assertNull("fromResource does not refresh", fromResource);
        assertEquals("after refresh the result reflects reality", fromToFO, fromSndResource);
    }
    public void testNewChildNoticed() throws Exception {
        FileObject fileObject1 = testFolder.createData("fileObject1");
        FileObject[] arr = testFolder.getChildren();
        assertEquals("One child", 1, arr.length);
        assertEquals("Right child", fileObject1, arr[0]);

        File file = FileUtil.toFile(fileObject1);
        assertNotNull("File found", file);
        arr = null;
        fileObject1 = null;
        Reference<FileObject> ref = new WeakReference<FileObject>(fileObject1);
        assertGC("File Object can disappear", ref);

        Thread.sleep(100);

        class L extends FileChangeAdapter {
            int cnt;
            FileEvent event;

            @Override
            public void fileDataCreated(FileEvent fe) {
                cnt++;
                event = fe;
            }

        }
        L listener = new L();
        testFolder.addRecursiveListener(listener);

        File nfile = new File(file.getParentFile(), "new.txt");
        nfile.createNewFile();

        testFolder.refresh();

        assertEquals("Change notified", 1, listener.cnt);
        assertEquals("Right file", nfile, FileUtil.toFile(listener.event.getFile()));
    }
    public void testDeleteOfAChildNoticed() throws Exception {
        FileObject fileObject1 = testFolder.createData("fileObject1");
        FileObject[] arr = testFolder.getChildren();
        assertEquals("One child", 1, arr.length);
        assertEquals("Right child", fileObject1, arr[0]);

        File file = FileUtil.toFile(fileObject1);
        assertNotNull("File found", file);
        arr = null;
        fileObject1 = null;
        Reference<FileObject> ref = new WeakReference<FileObject>(fileObject1);
        assertGC("File Object can disappear", ref);

        Thread.sleep(100);

        class L extends FileChangeAdapter {
            int cnt;
            FileEvent event;

            @Override
            public void fileDeleted(FileEvent fe) {
                cnt++;
                event = fe;
            }

        }
        L listener = new L();
        testFolder.addRecursiveListener(listener);

        file.delete();

        testFolder.refresh();

        assertEquals("Change notified", 1, listener.cnt);
        assertEquals("Right file", file, FileUtil.toFile(listener.event.getFile()));
    }

    public void testRecursiveListener() throws Exception {
        FileObject sub;
        File fobj;
        File fsub;
        {
            FileObject obj = FileUtil.createData(testFolder, "my/sub/children/children.java");
            fobj = FileUtil.toFile(obj);
            assertNotNull("File found", fobj);
            sub = obj.getParent().getParent();
            fsub = FileUtil.toFile(sub);

            WeakReference<Object> ref = new WeakReference<>(obj);
            obj = null;
            assertGC("File object can disappear", ref);
        }

        class L implements FileChangeListener {
            StringBuilder sb = new StringBuilder();

            public void fileFolderCreated(FileEvent fe) {
                LOG.info("FolderCreated: " + fe.getFile());
                sb.append("FolderCreated");
            }

            public void fileDataCreated(FileEvent fe) {
                LOG.info("DataCreated: " + fe.getFile());
                sb.append("DataCreated");
            }

            public void fileChanged(FileEvent fe) {
                LOG.info("Changed: " + fe.getFile());
                sb.append("Changed");
            }

            public void fileDeleted(FileEvent fe) {
                LOG.info("Deleted: " + fe.getFile());
                sb.append("Deleted");
            }

            public void fileRenamed(FileRenameEvent fe) {
                LOG.info("Renamed: " + fe.getFile());
                sb.append("Renamed");
            }

            public void fileAttributeChanged(FileAttributeEvent fe) {
                LOG.info("AttributeChanged: " + fe.getFile());
                sb.append("AttributeChanged");
            }

            public void assertMessages(String txt, String msg) {
                assertEquals(txt, msg, sb.toString());
                sb.setLength(0);
            }
        }
        L recursive = new L();
        L flat = new L();

        sub.addFileChangeListener(flat);
        LOG.info("Adding listener");
        sub.addRecursiveListener(recursive);
        LOG.info("Adding listener finished");

        Thread.sleep(1000);

        File fo = new File(fobj.getParentFile(), "sibling.java");
        fo.createNewFile();
        LOG.info("sibling created, now refresh");
        FileUtil.refreshAll();
        LOG.info("sibling refresh finished");

        recursive.assertMessages("Creation", "DataCreated");
        flat.assertMessages("No messages in flat mode", "");

        Thread.sleep(1000);

        final OutputStream os = new FileOutputStream(fo);
        os.write(10);
        os.close();
        LOG.info("Before refresh");
        FileUtil.refreshAll();
        LOG.info("After refresh");

        flat.assertMessages("No messages in flat mode", "");
        recursive.assertMessages("written", "Changed");
        
        fo.setReadOnly();
        LOG.info("Read-only refresh before");
        FileUtil.refreshAll();
        LOG.info("Read-only refresh after");
        
        flat.assertMessages("No messages in flat mode", "");
        recursive.assertMessages("attribute changed", "AttributeChanged");
        
        fo.setWritable(true);

        fo.delete();
        FileUtil.refreshAll();

        flat.assertMessages("No messages in flat mode", "");
        recursive.assertMessages("gone", "Deleted");

        new File(fsub, "testFolder").mkdirs();
        FileUtil.refreshAll();

        flat.assertMessages("Direct Folder notified", "FolderCreated");
        recursive.assertMessages("Direct Folder notified", "FolderCreated");

        new File(fsub.getParentFile(), "unimportant.txt").createNewFile();
        FileUtil.refreshAll();

        flat.assertMessages("No messages in flat mode", "");
        recursive.assertMessages("No messages in recursive mode", "");

        File deepest = new File(new File(new File(fsub, "deep"), "deeper"), "deepest");
        deepest.mkdirs();
        FileUtil.refreshAll();

        flat.assertMessages("Folder in flat mode", "FolderCreated");
        recursive.assertMessages("Folder detected", "FolderCreated");

        File hidden = new File(deepest, "hide.me");
        hidden.createNewFile();
        FileUtil.refreshAll();

        flat.assertMessages("No messages in flat mode", "");
        recursive.assertMessages("Folder detected", "DataCreated");


        sub.removeRecursiveListener(recursive);

        new File(fsub, "test.data").createNewFile();
        FileUtil.refreshAll();

        flat.assertMessages("Direct file notified", "DataCreated");
        recursive.assertMessages("No longer active", "");

        WeakReference<L> ref = new WeakReference<L>(recursive);
        recursive = null;
        assertGC("Listener can be GCed", ref);
    }

}
