/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.masterfs.filebasedfs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.test.TestFileUtils;

/**
 * @author Jiri Skrivanek
 */
public class FileUtilTest extends NbTestCase {
    static {
        System.setProperty("org.netbeans.modules.masterfs.watcher.disable", "true");
    }

    public FileUtilTest(String name) {
        super(name);
    }

    /** Test performance of FileUtil.copy(FileObject.getInputStream(), FileObject.getOutputStream())
     * against FileUtil.copy(FileInputStream, FileOutputStream). It should be the same.
     */
    @RandomlyFails // NB-Core-Build #1738
    public void testCopy136308() throws Exception {
        File file = new File(getWorkDir(), "input");
        FileWriter writer = new FileWriter(file);
        for (int i = 0; i < 1000000; i++) {
            writer.write(new Random(i).nextInt(255));
        }
        writer.close();
        FileObject fi = FileUtil.toFileObject(file);
        File fileOut = new File(getWorkDir(), "output");
        fileOut.createNewFile();
        FileObject fo = FileUtil.toFileObject(fileOut);
        InputStream is = fi.getInputStream();
        OutputStream os = fo.getOutputStream();
        long start = System.currentTimeMillis();
        FileUtil.copy(is, os);
        long end = System.currentTimeMillis();
        long timeDefault = end - start + 1;
        is.close();
        os.close();
        // now measure FileOutputStream
        is = new FileInputStream(file);
        fileOut.delete();
        fileOut.createNewFile();
        os = new FileOutputStream(fileOut);
        start = System.currentTimeMillis();
        FileUtil.copy(is, os);
        end = System.currentTimeMillis();
        is.close();
        os.close();
        long timeFileStreams = end - start + 1;
        assertTrue("Time of FileUtil.copy(FileObject.getInputStream(), FileObject.getOutputStream()) " + timeDefault + " should not be extremly bigger than time of FileUtil.copy(FileInputStream, FileOutputStream) " + timeFileStreams + ".", timeFileStreams * 100 > timeDefault);
    }

    /** Tests FileChangeListener on File.
     * @see FileUtil#addFileChangeListener(org.openide.filesystems.FileChangeListener, java.io.File)
     */
    @RandomlyFails // "Event not fired when file was modified." in NB-Core-Build #3605
    public void testAddFileChangeListener() throws IOException, InterruptedException {
        clearWorkDir();
        File rootF = getWorkDir();
        File dirF = new File(rootF, "dir");
        File fileF = new File(dirF, "file");

        // adding listeners
        TestFileChangeListener fcl = new TestFileChangeListener();
        FileUtil.addFileChangeListener(fcl, fileF);
        try {
            FileUtil.addFileChangeListener(fcl, fileF);
            fail("Should not be possible to add listener for the same path.");
        } catch (IllegalArgumentException iae) {
            // ok
        }
        TestFileChangeListener fcl2 = new TestFileChangeListener();
        try {
            FileUtil.removeFileChangeListener(fcl2, fileF);
            fail("Should not be possible to remove listener which is not registered.");
        } catch (IllegalArgumentException iae) {
            // ok
        }
        FileUtil.addFileChangeListener(fcl2, fileF);

        // creation
        final FileObject rootFO = FileUtil.toFileObject(rootF);
        FileObject dirFO = rootFO.createFolder("dir");
        assertEquals("Event fired when just parent dir created.", 0, fcl.checkAll());
        FileObject fileFO = dirFO.createData("file");
        assertEquals("Event not fired when file was created.", 1, fcl.check(EventType.DATA_CREATED));
        assertEquals("Event not fired when file was created.", 1, fcl2.check(EventType.DATA_CREATED));
        FileObject fileAFO = dirFO.createData("fileA");
        assertEquals("No other events should be fired.", 0, fcl.checkAll());

        // remove listener
        FileUtil.removeFileChangeListener(fcl2, fileF);

        // modification
        fileFO.getOutputStream().close();
        fileFO.getOutputStream().close();
        assertEquals("Event not fired when file was modified.", 2, fcl.check(EventType.CHANGED));
        // no event fired when other file modified
        fileAFO.getOutputStream().close();
        assertEquals("No other events should be fired.", 0, fcl.checkAll());

        // deletion
        fileFO.delete();
        assertEquals("Event not fired when file deleted.", 1, fcl.check(EventType.DELETED));
        dirFO.delete();
        assertEquals("Event fired when parent dir deleted and file already deleted.", 0, fcl.checkAll());
        dirFO = rootFO.createFolder("dir");
        fileFO = dirFO.createData("file");
        assertEquals("Event not fired when file was created.", 1, fcl.check(EventType.DATA_CREATED));
        dirFO.delete();
        assertEquals("Event not fired when parent dir deleted.", 1, fcl.check(EventType.DELETED));
        assertEquals("No other events should be fired.", 0, fcl.checkAll());

        // atomic action
        FileUtil.runAtomicAction(new Runnable() {
            @Override
            public void run() {
                FileObject dirFO;
                try {
                    dirFO = rootFO.createFolder("dir");
                    rootFO.createFolder("fakedir");
                    rootFO.setAttribute("fake", "fake");
                    rootFO.createData("fakefile");
                    dirFO.createData("file");
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }

            }
        });
        assertEquals("Wrong number of events fired when file was created in atomic action.", 1, fcl.check(EventType.DATA_CREATED));
        assertEquals("No other events should be fired.", 0, fcl.checkAll());

        // rename
        dirFO = FileUtil.toFileObject(dirF);
        fileFO = FileUtil.toFileObject(fileF);
        FileLock lock = dirFO.lock();
        dirFO.rename(lock, "dirRenamed", null);
        lock.releaseLock();
        assertEquals("Event fired when parent dir renamed.", 0, fcl.checkAll());
        lock = fileFO.lock();
        fileFO.rename(lock, "fileRenamed", null);
        lock.releaseLock();
        assertEquals("Renamed event not fired.", 1, fcl.check(EventType.RENAMED));
        assertEquals("No other events should be fired.", 0, fcl.checkAll());

        // disk changes
        dirF.mkdir();
        assertTrue(fileF.createNewFile());
        FileUtil.refreshAll();
        assertEquals("Event not fired when file was created.", 1, fcl.check(EventType.DATA_CREATED));
        TestFileUtils.touch(fileF, null);
        FileUtil.refreshAll();
        assertEquals("Event not fired when file was modified.", 1, fcl.check(EventType.CHANGED));
        // 1 if FileObject for dirF is already collected, 2 otherwise
        int eventCount = fcl.check(EventType.ATTRIBUTE_CHANGED);
        assertEquals("Attribute change event shall not be fired", 0, eventCount);
        fileF.delete();
        dirF.delete();
        FileUtil.refreshAll();
        assertEquals("Event not fired when file deleted.", 1, fcl.check(EventType.DELETED));
        assertEquals("No other events should be fired.", 0, fcl.checkAll());

        // disk changes #66444
        for (int cntr = 0; cntr < 5; cntr++) {
            dirF.mkdir();
            fileF.createNewFile();
            TestFileUtils.touch(fileF, null);
            FileUtil.refreshAll();
            assertEquals("Event not fired when file was created; count=" + cntr, 1, fcl.check(EventType.DATA_CREATED));
            fileF.delete();
            dirF.delete();
            FileUtil.refreshAll();
            assertEquals("Event not fired when file deleted; count=" + cntr, 1, fcl.check(EventType.DELETED));
        }

        // removed listener
        assertEquals("No other events should be fired in removed listener.", 0, fcl2.checkAll());

        // weakness
        WeakReference<FileChangeListener> ref = new WeakReference<FileChangeListener>(fcl);
        fcl = null;
        assertGC("FileChangeListener not collected.", ref);
    }

    @RandomlyFails // NB-Core-Build #3398
    public void testAddRecursiveListener() throws IOException, InterruptedException {
        clearWorkDir();
        File rootF = getWorkDir();
        File dirF = new File(rootF, "dir");
        File fileF = new File(dirF, "subdir");

        // adding listeners
        TestFileChangeListener fcl = new TestFileChangeListener();
        FileUtil.addRecursiveListener(fcl, fileF);
        try {
            FileUtil.addRecursiveListener(fcl, fileF);
            fail("Should not be possible to add listener for the same path.");
        } catch (IllegalArgumentException iae) {
            // ok
        }
        TestFileChangeListener fcl2 = new TestFileChangeListener();
        try {
            FileUtil.removeRecursiveListener(fcl2, fileF);
            fail("Should not be possible to remove listener which is not registered.");
        } catch (IllegalArgumentException iae) {
            // ok
        }
        FileUtil.addRecursiveListener(fcl2, fileF);

        // creation
        final FileObject rootFO = FileUtil.toFileObject(rootF);
        FileObject dirFO = rootFO.createFolder("dir");
        assertEquals("Event fired when just parent dir created.", 0, fcl.checkAll());
        FileObject fileFO = FileUtil.createData(dirFO, "subdir/subsubdir/file");
        assertEquals("Event not fired when file was created.", 2, fcl.check(EventType.FOLDER_CREATED));
        assertEquals("Event not fired when file was created.", 2, fcl2.check(EventType.FOLDER_CREATED));
        FileObject fileFO2 = FileUtil.createData(dirFO, "subdir/subsubdir/file2");
        assertEquals("Event not fired when file was created.", 2, fcl.check(EventType.DATA_CREATED));
        assertEquals("Event not fired when file was created.", 2, fcl2.check(EventType.DATA_CREATED));
        FileObject fileAFO = FileUtil.createData(dirFO, "fileA");
        assertEquals("No other events should be fired.", 0, fcl.checkAll());

        // remove listener
        FileUtil.removeRecursiveListener(fcl2, fileF);
        fcl2.disabled = true;
        assertEquals("No other events should be fired.", 0, fcl2.checkAll());

        // modification
        fileFO.getOutputStream().close();
        fileFO.getOutputStream().close();
        assertEquals("Event not fired when file was modified.", 2, fcl.check(EventType.CHANGED));
        // no event fired when other file modified
        fileAFO.getOutputStream().close();
        assertEquals("No other events should be fired.", 0, fcl.checkAll());

        // deletion
        fileFO.delete();
        assertEquals("Event not fired when file deleted.", 1, fcl.check(EventType.DELETED));
        dirFO.delete();
        assertEquals("Event not fired when parent dir deleted.", 1, fcl.checkAll());
        dirFO = rootFO.createFolder("dir");
        fileFO = FileUtil.createData(dirFO, "subdir/subsubdir/file");
        assertEquals("Event not fired when file was created.", 1, fcl.check(EventType.DATA_CREATED));
        assertEquals("Event not fired when dirs created.", 2, fcl.check(EventType.FOLDER_CREATED));
        dirFO.delete();
        assertEquals("Event not fired when parent dir deleted.", 1, fcl.check(EventType.DELETED));
        assertEquals("No other events should be fired.", 0, fcl.checkAll());

        // atomic action
        FileUtil.runAtomicAction(new Runnable() {
            @Override
            public void run() {
                FileObject dirFO;
                try {
                    dirFO = rootFO.createFolder("dir");
                    rootFO.createFolder("fakedir");
                    rootFO.setAttribute("fake", "fake");
                    rootFO.createData("fakefile");
                    FileUtil.createData(dirFO, "subdir/subsubdir/file");
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }

            }
        });
        assertEquals("Notifying the folder creation only.", 1, fcl.check(EventType.FOLDER_CREATED));
        assertEquals("No other events should be fired.", 0, fcl.checkAll());

        // rename
        dirFO = FileUtil.toFileObject(dirF);
        fileFO = FileUtil.toFileObject(fileF);
        FileLock lock = dirFO.lock();
        dirFO.rename(lock, "dirRenamed", null);
        lock.releaseLock();
        assertEquals("Event fired when parent dir renamed.", 0, fcl.checkAll());
        lock = fileFO.lock();
        fileFO.rename(lock, "fileRenamed", null);
        lock.releaseLock();
        assertEquals("Renamed event not fired.", 1, fcl.check(EventType.RENAMED));
        assertEquals("No other events should be fired.", 0, fcl.checkAll());

        // disk changes
        dirF.mkdir();
        final File subdir = new File(fileF, "subdir");
        subdir.mkdirs();
        final File newfile = new File(subdir, "newfile");
        assertTrue(newfile.createNewFile());
        FileUtil.refreshAll();
        assertEquals("Event not fired when file was created.", 1, fcl.check(EventType.FOLDER_CREATED));
        TestFileUtils.touch(newfile, null);
        FileUtil.refreshAll();
        assertEquals("Event not fired when file was modified.", 1, fcl.check(EventType.CHANGED));
        assertEquals("No other events", 0, fcl.checkAll());
        newfile.delete();
        FileUtil.refreshAll();
        assertEquals("Event not fired when file deleted.", 1, fcl.check(EventType.DELETED));
        assertEquals("No other events should be fired.", 0, fcl.checkAll());

        // disk changes #66444
        File fileX = new File(subdir, "oscilating.file");
        for (int cntr = 0; cntr < 5; cntr++) {
            fileX.getParentFile().mkdirs();
            fileX.createNewFile();
            TestFileUtils.touch(fileX, null);
            FileUtil.refreshAll();
            assertEquals("Event not fired when file was created; count=" + cntr, 1, fcl.check(EventType.DATA_CREATED));
            fileX.delete();
            FileUtil.refreshAll();
            assertEquals("Event not fired when file deleted; count=" + cntr, 1, fcl.check(EventType.DELETED));
        }

        // removed listener
        assertEquals("No other events should be fired in removed listener.", 0, fcl2.checkAll());

        // weakness
        WeakReference<FileChangeListener> ref = new WeakReference<FileChangeListener>(fcl);
        fcl = null;
        assertGC("FileChangeListener not collected.", ref);
    }

    /** Tests FileChangeListener on folder. As declared in
     * {@link FileUtil#addFileChangeListener(org.openide.filesystems.FileChangeListener, java.io.File) }
     * - fileFolderCreated event is fired when the folder is created or a child folder created
     * - fileDataCreated event is fired when a child file is created
     * - fileDeleted event is fired when the folder is deleted or a child file/folder removed
     * - fileChanged event is fired when a child file is modified
     * - fileRenamed event is fired when the folder is renamed or a child file/folder is renamed
     * - fileAttributeChanged is fired when FileObject's attribute is changed
     */
    public void testAddFileChangeListenerFolder() throws IOException {
        clearWorkDir();
        // test files: dir/file1, dir/subdir/subfile
        File rootF = getWorkDir();
        File dirF = new File(rootF, "dir");
        TestFileChangeListener fcl = new TestFileChangeListener();
        FileUtil.addFileChangeListener(fcl, dirF);
        // create dir
        FileObject dirFO = FileUtil.createFolder(dirF);
        assertEquals("Event not fired when folder created.", 1, fcl.check(EventType.FOLDER_CREATED));
        FileObject subdirFO = dirFO.createFolder("subdir");
        assertEquals("Event not fired when sub folder created.", 1, fcl.check(EventType.FOLDER_CREATED));

        // create file
        FileObject file1FO = dirFO.createData("file1");
        assertEquals("Event not fired when data created.", 1, fcl.check(EventType.DATA_CREATED));
        FileObject subfileFO = subdirFO.createData("subfile");
        assertEquals("Event fired when data in sub folder created.", 0, fcl.checkAll());

        // modify
        file1FO.getOutputStream().close();
        assertEquals("fileChanged event not fired.", 1, fcl.check(EventType.CHANGED));
        subfileFO.getOutputStream().close();
        assertEquals("Event fired when file sub folder modified.", 0, fcl.checkAll());

        // delete
        file1FO.delete();
        assertEquals("Event not fired when child file deleted.", 1, fcl.check(EventType.DELETED));
        subfileFO.delete();
        assertEquals("Event fired when child file in sub folder deleted.", 0, fcl.checkAll());
        subdirFO.delete();
        assertEquals("Event not fired when sub folder deleted.", 1, fcl.check(EventType.DELETED));
        dirFO.delete();
        assertEquals("Event not fired when folder deleted.", 1, fcl.check(EventType.DELETED));

        // rename
        dirFO = FileUtil.createFolder(dirF);
        file1FO = dirFO.createData("file1");
        subdirFO = dirFO.createFolder("subdir");
        subfileFO = subdirFO.createData("subfile");
        fcl.checkAll();
        FileLock lock = file1FO.lock();
        file1FO.rename(lock, "file1Renamed", null);
        lock.releaseLock();
        assertEquals("Event not fired when child file renamed.", 1, fcl.check(EventType.RENAMED));
        lock = subfileFO.lock();
        subfileFO.rename(lock, "subfileRenamed", null);
        lock.releaseLock();
        assertEquals("Event fired when child file in sub folder renamed.", 0, fcl.check(EventType.RENAMED));
        lock = subdirFO.lock();
        subdirFO.rename(lock, "subdirRenamed", null);
        lock.releaseLock();
        assertEquals("Event not fired when sub folder renamed.", 1, fcl.check(EventType.RENAMED));
        lock = dirFO.lock();
        dirFO.rename(lock, "dirRenamed", null);
        lock.releaseLock();
        assertEquals("Event not fired when sub folder renamed.", 1, fcl.check(EventType.RENAMED));
        fcl.printAll();
        assertEquals("No other events should be fired.", 0, fcl.checkAll());
    }

    /** Tests recursive FileChangeListener on File.
     * @see FileUtil#addRecursiveListener(org.openide.filesystems.FileChangeListener, java.io.File)
     */
    @RandomlyFails // NB-Core-Build #3632: Wrong number of events when file was modified. expected:<1> but was:<0>
    public void testAddRecursiveListenerToFile() throws IOException, InterruptedException {
        clearWorkDir();
        File rootF = getWorkDir();
        File dirF = new File(rootF, "dir");
        File fileF = new File(dirF, "file");

        // adding listeners
        TestFileChangeListener fcl = new TestFileChangeListener();
        FileUtil.addRecursiveListener(fcl, fileF);
        try {
            FileUtil.addRecursiveListener(fcl, fileF);
            fail("Should not be possible to add listener for the same path.");
        } catch (IllegalArgumentException iae) {
            // ok
        }
        TestFileChangeListener fcl2 = new TestFileChangeListener();
        try {
            FileUtil.removeRecursiveListener(fcl2, fileF);
            fail("Should not be possible to remove listener which is not registered.");
        } catch (IllegalArgumentException iae) {
            // ok
        }
        FileUtil.addRecursiveListener(fcl2, fileF);

        // creation
        final FileObject rootFO = FileUtil.toFileObject(rootF);
        FileObject dirFO = rootFO.createFolder("dir");
        assertEquals("Event fired when just parent dir created.", 0, fcl.checkAll());
        FileObject fileFO = dirFO.createData("file");
        assertEquals("Wrong number of events when file was created.", 1, fcl.check(EventType.DATA_CREATED));
        assertEquals("Wrong number of events when file was created.", 1, fcl2.check(EventType.DATA_CREATED));
        FileObject fileAFO = dirFO.createData("fileA");
        assertEquals("No other events should be fired.", 0, fcl.checkAll());

        // remove listener
        FileUtil.removeRecursiveListener(fcl2, fileF);

        // modification
        fileFO.getOutputStream().close();
        fileFO.getOutputStream().close();
        assertEquals("Wrong number of events when file was modified.", 2, fcl.check(EventType.CHANGED));
        // no event fired when other file modified
        fileAFO.getOutputStream().close();
        assertEquals("No other events should be fired.", 0, fcl.checkAll());

        // deletion
        fileFO.delete();
        assertEquals("Wrong number of events when file deleted.", 1, fcl.check(EventType.DELETED));
        dirFO.delete();
        assertEquals("Event fired when parent dir deleted and file already deleted.", 0, fcl.checkAll());
        dirFO = rootFO.createFolder("dir");
        fileFO = dirFO.createData("file");
        assertEquals("Wrong number of events when file was created.", 1, fcl.check(EventType.DATA_CREATED));
        dirFO.delete();
        assertEquals("Wrong number of events when parent dir deleted.", 1, fcl.check(EventType.DELETED));
        assertEquals("No other events should be fired.", 0, fcl.checkAll());

        // atomic action
        FileUtil.runAtomicAction(new Runnable() {
            @Override
            public void run() {
                FileObject dirFO;
                try {
                    dirFO = rootFO.createFolder("dir");
                    rootFO.createFolder("fakedir");
                    rootFO.setAttribute("fake", "fake");
                    rootFO.createData("fakefile");
                    dirFO.createData("file");
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }

            }
        });
        assertEquals("Wrong number of events fired when file was created in atomic action.", 1, fcl.check(EventType.DATA_CREATED));
        assertEquals("No other events should be fired.", 0, fcl.checkAll());

        // rename
        dirFO = FileUtil.toFileObject(dirF);
        fileFO = FileUtil.toFileObject(fileF);
        FileLock lock = dirFO.lock();
        dirFO.rename(lock, "dirRenamed", null);
        lock.releaseLock();
        assertEquals("Event fired when parent dir renamed.", 0, fcl.checkAll());
        lock = fileFO.lock();
        fileFO.rename(lock, "fileRenamed", null);
        lock.releaseLock();
        assertEquals("Renamed event not fired.", 1, fcl.check(EventType.RENAMED));
        assertEquals("No other events should be fired.", 0, fcl.checkAll());

        // disk changes
        dirF.mkdir();
        assertTrue(fileF.createNewFile());
        FileUtil.refreshAll();
        assertEquals("Wrong number of events when file was created.", 1, fcl.check(EventType.DATA_CREATED));
        TestFileUtils.touch(fileF, null);
        FileUtil.refreshAll();
        assertEquals("Wrong number of events when file was modified.", 1, fcl.check(EventType.CHANGED));
        assertEquals("No other events", 0, fcl.checkAll());
        fileF.delete();
        dirF.delete();
        FileUtil.refreshAll();
        assertEquals("Wrong number of events when file deleted.", 1, fcl.check(EventType.DELETED));
        assertEquals("No other events should be fired.", 0, fcl.checkAll());

        // disk changes #66444
        for (int cntr = 0; cntr < 5; cntr++) {
            dirF.mkdir();
            fileF.createNewFile();
            TestFileUtils.touch(fileF, null);
            FileUtil.refreshAll();
            assertEquals("Event not fired when file was created; count=" + cntr, 1, fcl.check(EventType.DATA_CREATED));
            fileF.delete();
            dirF.delete();
            FileUtil.refreshAll();
            assertEquals("Event not fired when file deleted; count=" + cntr, 1, fcl.check(EventType.DELETED));
        }

        // removed listener
        assertEquals("No other events should be fired in removed listener.", 0, fcl2.checkAll());

        // weakness
        WeakReference<FileChangeListener> ref = new WeakReference<FileChangeListener>(fcl);
        fcl = null;
        assertGC("FileChangeListener not collected.", ref);
    }
    
    public void testNestedRecursiveListener() throws IOException {
        doNestedRL(false);
    }
    
    public void testNestedRecursiveListenerReverse() throws IOException {
        doNestedRL(true);
    }
    
    private void doNestedRL(boolean reverse) throws IOException {
        clearWorkDir();
        FileObject folder1 = FileUtil.toFileObject(getWorkDir());
        
        FileObject obj = FileUtil.createData(folder1, "my/sub/children/children.java");
        final FileObject children = obj.getParent();
        final FileObject sub = children.getParent();
        final FileObject my = sub.getParent();

        File fMy = FileUtil.toFile(my);
        File fSub = FileUtil.toFile(sub);
        File fChildren = FileUtil.toFile(children);

        class L implements FileChangeListener {
            StringBuilder sb = new StringBuilder();

            public void fileFolderCreated(FileEvent fe) {
                sb.append("FolderCreated");
            }

            public void fileDataCreated(FileEvent fe) {
                sb.append("DataCreated");
            }

            public void fileChanged(FileEvent fe) {
                sb.append("Changed");
            }

            public void fileDeleted(FileEvent fe) {
                sb.append("Deleted");
            }

            public void fileRenamed(FileRenameEvent fe) {
                sb.append("Renamed");
            }

            public void fileAttributeChanged(FileAttributeEvent fe) {
                sb.append("AttributeChanged");
            }

            public void assertMessages(String txt, String msg) {
                assertEquals(txt, msg, sb.toString());
                sb.setLength(0);
            }
        }
        L recursive = new L();

        FileUtil.addRecursiveListener(recursive, fMy);
        FileUtil.addRecursiveListener(recursive, fSub);
        FileUtil.addRecursiveListener(recursive, fChildren);

        FileObject fo = obj.getParent().createData("sibling.java");

        recursive.assertMessages("3x of Creation", "DataCreatedDataCreatedDataCreated");

        File[] removalOrder = { fMy, fSub, fChildren };
        
        if (reverse) {
            Collections.reverse(Arrays.asList(removalOrder));
        }
        
        FileUtil.removeRecursiveListener(recursive, removalOrder[0]);

        FileLock lck = fo.lock();
        fo.rename(lck, "ibling", "stava");
        lck.releaseLock();

        recursive.assertMessages("2x renames", "RenamedRenamed");

        FileUtil.removeRecursiveListener(recursive, removalOrder[1]);

        lck = fo.lock();
        fo.rename(lck, "dibling", "trava");
        lck.releaseLock();

        recursive.assertMessages("1x rename", "Renamed");

        FileUtil.removeRecursiveListener(recursive, removalOrder[2]);

        fo.delete();

        recursive.assertMessages("Nothing", "");
        
    }
    

    public static enum EventType {

        DATA_CREATED, FOLDER_CREATED, DELETED, CHANGED, RENAMED, ATTRIBUTE_CHANGED
    };

    public static class TestFileChangeListener implements FileChangeListener {
        boolean disabled;

        private final Map<EventType, List<FileEvent>> type2Event = new HashMap<EventType, List<FileEvent>>();

        public TestFileChangeListener() {
            super();
            for (EventType eventType : EventType.values()) {
                type2Event.put(eventType, new ArrayList<FileEvent>());
            }
        }

        public void clearAll() {
            for (EventType type : EventType.values()) {
                type2Event.get(type).clear();
            }
        }

        /** Returns number of events and clears counter. */
        public int check(EventType type) {
            int size = type2Event.get(type).size();
            type2Event.get(type).clear();
            return size;
        }

        public int checkAll() {
            int sum = 0;
            for (EventType type : EventType.values()) {
                sum += type2Event.get(type).size();
                type2Event.get(type).clear();
            }
            return sum;
        }

        public void printAll() {
            for (EventType type : EventType.values()) {
                for (FileEvent fe : type2Event.get(type)) {
                    System.out.println(type + "=" + fe);
                }
            }
        }
        public void printAll(Logger log) {
            for (EventType type : EventType.values()) {
                for (FileEvent fe : type2Event.get(type)) {
                    log.log(Level.WARNING, "{0} = {1} @ {2}", new Object[]{type, fe.getFile(), fe.getTime()});
                }
            }
        }

        @Override
        public void fileFolderCreated(FileEvent fe) {
            assertFalse("No changes expected", disabled);
            type2Event.get(EventType.FOLDER_CREATED).add(fe);
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
            assertFalse("No changes expected", disabled);
            type2Event.get(EventType.DATA_CREATED).add(fe);
        }

        @Override
        public void fileChanged(FileEvent fe) {
            assertFalse("No changes expected", disabled);
            type2Event.get(EventType.CHANGED).add(fe);
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            assertFalse("No changes expected", disabled);
            type2Event.get(EventType.DELETED).add(fe);
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            assertFalse("No changes expected", disabled);
            type2Event.get(EventType.RENAMED).add(fe);
        }

        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) {
            assertFalse("No changes expected", disabled);
            type2Event.get(EventType.ATTRIBUTE_CHANGED).add(fe);
        }
    }
}
