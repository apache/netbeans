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
package org.netbeans.modules.masterfs.filebasedfs;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.modules.masterfs.filebasedfs.fileobjects.BaseFileObj;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.test.TestFileUtils;
import org.netbeans.modules.masterfs.filebasedfs.FileUtilTest.EventType;
import org.netbeans.modules.masterfs.filebasedfs.FileUtilTest.TestFileChangeListener;
import org.netbeans.modules.masterfs.filebasedfs.fileobjects.FileObjectFactory;
import org.netbeans.modules.masterfs.filebasedfs.fileobjects.TestUtils;
import org.netbeans.modules.masterfs.providers.ProvidedExtensionsTest;

/**
 * @author Jiri Skrivanek
 */
public class FileUtilAddRecursiveListenerTest extends NbTestCase {
    static {
        MockServices.setServices(ProvidedExtensionsTest.AnnotationProviderImpl.class);
    }

    private final Logger LOG;

    public FileUtilAddRecursiveListenerTest(String name) {
        super(name);
        LOG = Logger.getLogger("TEST." + name);
    }

    @Override
    protected void setUp() throws Exception {
        System.getProperties().put("org.netbeans.modules.masterfs.watcher.disable", "true");
        clearWorkDir();
    }

    @Override
    protected Level logLevel() {
        return Level.FINER;
    }

    /** Tests FileObject.addRecursiveListener on folder as declared in
     * {@link FileObject#addRecursiveListener(org.openide.filesystems.FileChangeListener) }.
     * It is expected that all events from sub folders are delivered just once.
     */
    @RandomlyFails // NB-Core-Build #3874: Wrong number of events when file deleted. expected:<5> but was:<4>
    public void testAddRecursiveListenerToFileObjectFolder() throws Exception {
        checkFolderRecursiveListener(false);
    }

    /** Tests FileUtil.addRecursiveListener on folder as declared in
     * {@link FileUtil#addRecursiveListener(org.openide.filesystems.FileChangeListener, java.io.File) }.
     * It is expected that all events from sub folders are delivered just once.
     */
    @RandomlyFails // NB-Core-Build #4077: Wrong number of events when file was modified. expected:<3> but was:<1>
    public void testAddRecursiveListenerToFileFolder() throws Exception {
        checkFolderRecursiveListener(true);
    }

    /** Tests addRecursiveListener on folder either added to FileObject or File.
     * @param isOnFile true to add listener to java.io.File, false to FileObject
     */
    private void checkFolderRecursiveListener(boolean isOnFile) throws Exception {
        clearWorkDir();
        // test files: dir/file1, dir/subdir/subfile, dir/subdir/subsubdir/subsubfile
        final File rootF = getWorkDir();
        final File dirF = new File(rootF, "dir");
        File fileF = new File(dirF, "file1");
        File subdirF = new File(dirF, "subdir");
        File subfileF = new File(subdirF, "subfile");
        File subsubdirF = new File(subdirF, "subsubdir");
        File subsubfileF = new File(subsubdirF, "subsubfile");

        TestFileChangeListener fcl = new TestFileChangeListener();
        FileObject dirFO;
        if (isOnFile) {
            FileUtil.addRecursiveListener(fcl, dirF);
            dirFO = FileUtil.createFolder(dirF);
            assertEquals("Wrong number of events fired when folder created.", 1, fcl.check(EventType.FOLDER_CREATED));
        } else {
            dirFO = FileUtil.createFolder(dirF);
            dirFO.addRecursiveListener(fcl);
        }
//        TestUtils.gcAll();

        // create dir
        FileObject subdirFO = dirFO.createFolder("subdir");
        assertEquals("Wrong number of events fired when sub folder created.", 1, fcl.check(EventType.FOLDER_CREATED));
        FileObject subsubdirFO = subdirFO.createFolder("subsubdir");
        assertEquals("Wrong number of events when sub sub folder created.", 1, fcl.check(EventType.FOLDER_CREATED));

        // create file
        FileObject file1FO = dirFO.createData("file1");
        assertEquals("Wrong number of events when data created.", 1, fcl.check(EventType.DATA_CREATED));
        FileObject subfileFO = subdirFO.createData("subfile");
        assertEquals("Wrong number of events when data in sub folder created.", 1, fcl.check(EventType.DATA_CREATED));
        FileObject subsubfileFO = subsubdirFO.createData("subsubfile");
        assertEquals("Wrong number of events when data in sub sub folder created.", 1, fcl.check(EventType.DATA_CREATED));

        // modify
        file1FO.getOutputStream().close();
        assertEquals("Wrong number of events when file folder modified.", 1, fcl.check(EventType.CHANGED));
        subfileFO.getOutputStream().close();
        assertEquals("Wrong number of events when file in sub folder modified.", 1, fcl.check(EventType.CHANGED));
        subsubfileFO.getOutputStream().close();
        assertEquals("Wrong number of events when file in sub sub folder modified.", 1, fcl.check(EventType.CHANGED));

        // delete
        file1FO.delete();
        assertEquals("Wrong number of events when child file deleted.", 1, fcl.check(EventType.DELETED));
        subsubfileFO.delete();
        assertEquals("Wrong number of events when child file in sub sub folder deleted.", 1, fcl.check(EventType.DELETED));
        subsubdirFO.delete();
        assertEquals("Wrong number of events when sub sub folder deleted.", 1, fcl.check(EventType.DELETED));
        subfileFO.delete();
        assertEquals("Wrong number of events when child file in sub folder deleted.", 1, fcl.check(EventType.DELETED));
        subdirFO.delete();
        assertEquals("Wrong number of events when sub folder deleted.", 1, fcl.check(EventType.DELETED));

        // atomic action
        FileUtil.runAtomicAction(new Runnable() {

            public @Override void run() {
                try {
                    FileObject rootFO = FileUtil.toFileObject(rootF);
                    rootFO.createFolder("fakedir");  // no events
                    rootFO.setAttribute("fake", "fake");  // no events
                    rootFO.createData("fakefile");  // no events
                    FileObject dirFO = FileUtil.toFileObject(dirF);
                    dirFO.createData("file1");
                    FileObject subdirFO = dirFO.createFolder("subdir");
                    subdirFO.createData("subfile");
                    FileObject subsubdirFO = subdirFO.createFolder("subsubdir");
                    subsubdirFO.createData("subsubfile");
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }

            }
        });
        // TODO - should be 3
        assertEquals("Wrong number of events fired when file was created in atomic action.", 1, fcl.check(EventType.DATA_CREATED));
        // TODO - should be 2
        assertEquals("Wrong number of events fired when file was created in atomic action.", 1, fcl.check(EventType.FOLDER_CREATED));
        assertEquals("No other events should be fired.", 0, fcl.checkAll());

        // rename
        file1FO = dirFO.getFileObject("file1");
        subdirFO = dirFO.getFileObject("subdir");
        subfileFO = subdirFO.getFileObject("subfile");
        subsubdirFO = subdirFO.getFileObject("subsubdir");
        subsubfileFO = subsubdirFO.getFileObject("subsubfile");
        fcl.clearAll();
        FileLock lock = file1FO.lock();
        file1FO.rename(lock, "file1Renamed", null);
        lock.releaseLock();
        assertEquals("Wrong number of events when child file renamed.", 1, fcl.check(EventType.RENAMED));
        lock = subfileFO.lock();
        subfileFO.rename(lock, "subfileRenamed", null);
        lock.releaseLock();
        assertEquals("Wrong number of events when child file in sub folder renamed.", 1, fcl.check(EventType.RENAMED));
        lock = subsubfileFO.lock();
        subsubfileFO.rename(lock, "subsubfileRenamed", null);
        lock.releaseLock();
        assertEquals("Wrong number of events when child file in sub sub folder renamed.", 1, fcl.check(EventType.RENAMED));
        lock = subsubdirFO.lock();
        subsubdirFO.rename(lock, "subsubdirRenamed", null);
        lock.releaseLock();
        assertEquals("Wrong number of events when sub sub folder renamed.", 1, fcl.check(EventType.RENAMED));
        lock = subdirFO.lock();
        subdirFO.rename(lock, "subdirRenamed", null);
        lock.releaseLock();
        assertEquals("Wrong number of events when sub folder renamed.", 1, fcl.check(EventType.RENAMED));
        lock = dirFO.lock();
        dirFO.rename(lock, "dirRenamed", null);
        lock.releaseLock();
        assertEquals("Wrong number of events when sub folder renamed.", 1, fcl.check(EventType.RENAMED));
        lock = dirFO.lock();
        dirFO.rename(lock, "dir", null);
        lock.releaseLock();
        /* According to jskrivanek in http://www.netbeans.org/nonav/issues/showattachment.cgi/86910/X.diff, the rename back does not need to
         * fire an event. Instead the support delivers FOLDER_CREATED event:
        assertEquals("Wrong number of events when sub folder renamed.", 1, fcl.check(EventType.RENAMED));
        assertEquals("Wrong number of events when sub folder renamed.", 1, fcl.check(EventType.FOLDER_CREATED));
        fcl.printAll();
        assertEquals("No other events should be fired.", 0, fcl.checkAll());
         */
        // cleanup after rename
        dirFO.getFileObject("file1Renamed").delete();
        dirFO.getFileObject("subdirRenamed").delete();
        fcl.clearAll();

        // disk changes
        LOG.log(Level.INFO, "Going to sleep {0}", System.currentTimeMillis());
        Thread.sleep(1000); // give OS same time
        LOG.log(Level.INFO, "Waking up {0}", System.currentTimeMillis());
        assertTrue(subsubdirF.mkdirs());
        assertTrue(fileF.createNewFile());
        assertTrue(subfileF.createNewFile());
        assertTrue(subsubfileF.createNewFile());
        TestFileUtils.touch(subsubfileF, null);
        TestFileUtils.touch(subfileF, null);
        TestFileUtils.touch(fileF, null);
        LOG.log(Level.INFO, "After refresh {0} to {1}", new Object[]{subsubfileF, subsubfileF.lastModified()});
        LOG.log(Level.INFO, "After refresh {0} to {1}", new Object[]{subfileF, subfileF.lastModified()});
        LOG.log(Level.INFO, "After refresh {0} to {1}", new Object[]{fileF, fileF.lastModified()});
        FileUtil.refreshAll();
        fcl.printAll(LOG);
        // TODO - should be 3
        assertEquals("Wrong number of events when file was created.", 1, fcl.check(EventType.DATA_CREATED));
        // TODO - should be 2
        assertEquals("Wrong number of events when folder created.", 1, fcl.check(EventType.FOLDER_CREATED));
        assertEquals("No other events should be fired.", 0, fcl.checkAll());

//        TestUtils.gcAll();

        TestFileUtils.touch(subsubfileF, null);
        TestFileUtils.touch(subfileF, null);
        TestFileUtils.touch(fileF, null);
        LOG.log(Level.INFO, "Touched {0} to {1}", new Object[]{subsubfileF, subsubfileF.lastModified()});
        LOG.log(Level.INFO, "Touched {0} to {1}", new Object[]{subfileF, subfileF.lastModified()});
        LOG.log(Level.INFO, "Touched {0} to {1}", new Object[]{fileF, fileF.lastModified()});
        FileUtil.refreshAll();
        fcl.printAll(LOG);
        final int expect = fcl.check(EventType.CHANGED);
        if (expect != 3) {
            TestUtils.logAll();
        }
        assertEquals("Wrong number of events when file was modified.", 3, expect);
        fcl.clearAll();

        assertTrue(subsubfileF.delete());
        assertTrue(subsubdirF.delete());
        assertTrue(subfileF.delete());
        assertTrue(subdirF.delete());
        assertTrue(fileF.delete());
        FileUtil.refreshAll();
        assertEquals("Wrong number of events when file deleted.", 5, fcl.check(EventType.DELETED));

        // delete folder itself
        dirFO.delete();
        assertEquals("Wrong number of events when folder deleted.", 1, fcl.check(EventType.DELETED));

        LOG.info("OK");
    }

    public void testProvideExtensionsRefreshRecursively() throws Exception {
        File root = new File(getWorkDir(), "root");
        final File sub = new File(root, "sub");
        final File subsub = new File(sub, "subsub");
        File subsubdir = new File(subsub, "dir");
        subsubdir.mkdirs();
        File subfile = new File(sub, "file");
        subfile.createNewFile();
        File deepfile = new File(subsubdir, "deep");
        deepfile.createNewFile();

        ProvidedExtensionsTest.ProvidedExtensionsImpl.nextRefreshCall(
            sub,
            Long.MAX_VALUE - 10,
            subfile
        );


        TestFileChangeListener fcl = new TestFileChangeListener();
        FileObject rf = FileUtil.toFileObject(root);
        rf.addRecursiveListener(fcl);

        BaseFileObj noFO = FileObjectFactory.getInstance(root).getCachedOnly(subsubdir);
        assertNull("subsub directory has been skipped", noFO);

        assertEquals("No events", 0, fcl.checkAll());
        LOG.log(Level.INFO, "Touching subfile: {0}", deepfile);
        TestFileUtils.touch(deepfile, null);
        LOG.log(Level.INFO, "Will do refresh, lastModified: {0}", deepfile.lastModified());
        FileUtil.refreshFor(root);
        LOG.info("Refresh done");
        fcl.check(EventType.ATTRIBUTE_CHANGED); // ignore if any

        fcl.printAll(LOG);
        assertEquals("No other events", 0, fcl.checkAll());
    }
}
