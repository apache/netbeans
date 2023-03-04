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
package org.netbeans.modules.masterfs.filebasedfs.fileobjects;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.attribute.UserDefinedFileAttributeView;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.masterfs.filebasedfs.FileUtilTest.EventType;
import org.netbeans.modules.masterfs.filebasedfs.FileUtilTest.TestFileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;

/**
 * 
 * @author Jiri Skrivanek
 */
public class FileObjTest extends NbTestCase {

    public FileObjTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
    }

    @Override
    protected int timeOut() {
        return 60000;
    }

    /** Tests it is not possible to create duplicate FileObject for the same path.
     * - create FO1
     * - create FO2
     * - delete FO1 => FO1 is invalid now
     * - rename FO2 to FO1
     * - rename FO1 to FO1 => FO1 still invalid
     * - try to write to FO1.getOutputStream() => it should not be possible because FO1 is still invalid
     */
    public void testDuplicateFileObject130998() throws IOException {
        clearWorkDir();
        FileObject testFolder = FileUtil.toFileObject(getWorkDir());
        FileObject fileObject1 = testFolder.createData("fileObject1");
        FileObject fileObject2 = testFolder.createData("fileObject2");
        fileObject1.delete();
        assertFalse("fileObject1 should be invalid after delete.", fileObject1.isValid());

        FileLock lock = fileObject2.lock();
        fileObject2.rename(lock, fileObject1.getName(), null);
        lock.releaseLock();
        assertTrue("fileObject2 should be valid.", fileObject2.isValid());

        lock = fileObject1.lock();
        fileObject1.rename(lock, fileObject1.getName(), null);
        lock.releaseLock();
        assertFalse("fileObject1 should remain invalid after rename.", fileObject1.isValid());
        
        OutputStream os = fileObject1.getOutputStream();
        assertTrue("Valid file", FileUtil.toFile(fileObject1).exists());
        assertFalse("Invalid file object", fileObject1.isValid());
        assertNotNull("Since #211483 it is possible to obtain OutputStream for valid file/invalid fo", os);
    }

    /** #165406 - tests that only one event is fired for single change. */
    public void testChangeEvents165406() throws Exception {
        clearWorkDir();
        File workdir = getWorkDir();
        File file = new File(workdir, "testfile");
        file.createNewFile();
        final FileObject fo = FileUtil.toFileObject(file);
        fo.refresh(); // to set lastModified field
        final long beforeModification = fo.lastModified().getTime();
        Thread.sleep(1000);
        Handler handler = new Handler() {

            @Override
            public void publish(LogRecord record) {
                if (record.getMessage().equals("getOutputStream-close")) {
                    // wait for physical change of timestamp after stream was closed
                    while (beforeModification == fo.lastModified().getTime()) {
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    }
                    // call concurrent refresh
                    fo.refresh();
                }
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() throws SecurityException {
            }
        };

        Logger logger = Logger.getLogger(FileObj.class.getName());
        logger.addHandler(handler);
        logger.setLevel(Level.FINEST);

        TestFileChangeListener listener = new TestFileChangeListener();
        fo.addFileChangeListener(listener);
        
        OutputStream os = fo.getOutputStream();
        os.write("Ahoj everyone!\n".getBytes(StandardCharsets.UTF_8));
        os.close();

        assertEquals("Only one change event should be fired.", 1, listener.check(EventType.CHANGED));
    }

    public void testReadOnlyFile() throws Exception {
        clearWorkDir();
        File f = new File(getWorkDir(), "read-only.txt");
        f.createNewFile();
        FileObject fo = FileUtil.toFileObject(f);
        f.setWritable(false);
        try {
            OutputStream os = fo.getOutputStream();
            fail("Can't get the output stream for read-only file: " + os);
        } catch (IOException ex) {
            String msg = Exceptions.findLocalizedMessage(ex);
            assertNotNull("The exception comes with a localized message", msg);
        } finally {
            f.setWritable(true);
        }
    }

    public void testFileObjectForBrokenLinkWithGetChildren() throws Exception {
        if (!Utilities.isUnix()) {
            return;
        }
        doFileObjectForBrokenLink(true);
    }
//    public void testFileObjectForBrokenLink() throws Exception {
//        if (!Utilities.isUnix()) {
//            return;
//        }
//        doFileObjectForBrokenLink(false);
//    }
    
    private void doFileObjectForBrokenLink (boolean listFirst) throws Exception {
        clearWorkDir();
        File wd = new File(getWorkDir(), "wd");
        wd.mkdirs();

        File original = new File(wd, "original");
//        original.createNewFile();
        File lockFile = new File(wd, "wlock");
        for (int i = 1; i <= 2; ++i) {
            try {
                lockFile.delete();
                FileUtil.toFileObject(wd).refresh();
                ProcessBuilder pb = new ProcessBuilder().directory(wd).command(
                    new String[] { "ln", "-s", original.getName(), lockFile.getName() }
                );
                pb.start().waitFor();
                final List<String> names = Arrays.asList(lockFile.getParentFile().list());
                assertEquals("One file", 1, names.size());
                // file exists, or at least dir.listFiles lists the file
                assertTrue(names.contains(lockFile.getName()));
                // java.io.File.exists returns false
                assertFalse(lockFile.exists());

                if (listFirst) {
                    FileObject root = FileUtil.toFileObject(wd);
                    root.refresh();
                    List<FileObject> arr = Arrays.asList(root.getChildren());
                    assertEquals("Round " + i + " One files: " + arr, 1, arr.size());
                    assertEquals("Round " + i + "Has the right name", lockFile.getName(), arr.get(0).getName());
                }

                // and no FileObject is reated for such a file
                assertNotNull(FileUtil.toFileObject(lockFile));
            } finally {
                lockFile.delete();
            }
        }
    }

    /**
     * Test for bug 240953 - Netbeans Deletes User Defined Attributes.
     *
     * @throws java.io.IOException
     */
    public void testWritingKeepsFileAttributes() throws IOException {

        final String attName = "User_Attribute";
        final String attValue = "User_Attribute_Value";

        if (Utilities.isWindows()) {
            clearWorkDir();
            File f = new File(getWorkDir(), "fileWithAtts.txt");
            f.createNewFile();
            UserDefinedFileAttributeView attsView = Files.getFileAttributeView(
                    f.toPath(), UserDefinedFileAttributeView.class);
            ByteBuffer buffer = Charset.defaultCharset().encode(attValue);
            attsView.write(attName, buffer);

            buffer.rewind();
            attsView.read(attName, buffer);
            buffer.flip();
            String val = Charset.defaultCharset().decode(buffer).toString();
            assertEquals(attValue, val);

            FileObject fob = FileUtil.toFileObject(f);
            OutputStream os = fob.getOutputStream();
            try {
                os.write(55);
            } finally {
                os.close();
            }

            buffer.rewind();
            attsView.read(attName, buffer);
            buffer.flip();
            String val2 = Charset.defaultCharset().decode(buffer).toString();
            assertEquals(attValue, val2);
        }
    }

    /**
     * Test for bug 240180 - AssertionError: Need to normalize - different file
     * name case.
     *
     * @throws IOException
     */
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    public void testNormalizedAfterCaseChange() throws IOException {

        File wd = getWorkDir();
        FileObject wdFo = FileUtil.toFileObject(wd);
        File inner = new File(wd, "test.txt");
        inner.createNewFile();
        if (!new File(wd, "Test.txt").exists()) {
            System.out.println("Skipping test " + getName()
                    + " on a case-sensitive filesystem");
            return;
        }
        wdFo.refresh();
        FileObject innerFo = FileUtil.toFileObject(inner);
        assertEquals("test.txt", FileUtil.toFile(innerFo).getName());
        inner.delete(); // delete...
        File inner2 = new File(wd, "Test.txt"); //...and create with uppercase T
        inner2.createNewFile();
        assertTrue(Files.exists(inner.toPath()));
        assertEquals("Test.txt", inner.getCanonicalFile().getName());
        wdFo.refresh();
        new FileEvent(innerFo); // clear cache for normalized files

        File toFile = FileUtil.toFile(innerFo); // AssertionError here.
        assertNotNull(toFile);
    }
}
