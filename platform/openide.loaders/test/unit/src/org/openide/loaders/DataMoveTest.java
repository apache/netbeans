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
package org.openide.loaders;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import org.openide.filesystems.FileObject;
import java.util.Enumeration;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import org.netbeans.junit.*;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 * Test things about node delegates. Note: if you mess with file status changes
 * in this test, you may effectively break the testLeakAfterStatusChange test.
 *
 * @author Jesse Glick
 * @author Jaroslav Havlin
 */
public class DataMoveTest extends NbTestCase {

    private DataObject my;
    private FileObject root;
    private File dir;

    public DataMoveTest(String name) {
        super(name);
    }

    @Override
    protected Level logLevel() {
        return Level.WARNING;
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        Log.enable(DataLoader.ERR.getName(), Level.FINE);

        MockServices.setServices(Pool.class);
        assertEquals(Pool.class,
                Lookup.getDefault().lookup(DataLoaderPool.class).getClass());
        dir = new File(getWorkDir(), "dir");
        dir.mkdirs();
        File fJava = new File(dir, "F.java");
        fJava.createNewFile();

        //LocalFileSystem lfs = new LocalFileSystem();
        //lfs.setRootDirectory(getWorkDir());
        root = FileUtil.toFileObject(getWorkDir());
        //FileObject root = lfs.getRoot();
        assertNotNull("root found", root);

        my = DataObject.find(root.getFileObject("dir/F.java"));
    }

    /**
     * Test for bug 303620.
     *
     * Move a data object in the middle of execution of
     * MultiFileLoader.handleFindDataObject.
     *
     * @throws java.lang.Exception
     */
    public void testMove() throws Exception {
        final Throwable thrown[] = new Throwable[1];
        Handler h = new LogHandlerAdapter() {

            @Override
            public void publish(LogRecord record) {
                if (record.getMessage() != null
                        && record.getMessage().matches(".*get existing data object for: .*")
                        && record.getParameters()[1].toString().matches(".*F.java.*")) {
                    try {
                        DataFolder folder = DataFolder.findFolder(root);
                        my.move(folder);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                } else if (record.getThrown() instanceof UnsupportedOperationException) {
                    thrown[0] = record.getThrown();
                }
            }
        };
        DataLoader.ERR.addHandler(h);
        try {
            FolderList l = FolderList.find(my.getPrimaryFile().getParent(),
                    true);
            l.getChildren();
            l.refresh();
            l.waitProcessingFinished();
        } finally {
            DataLoader.ERR.removeHandler(h);
        }
        if (thrown[0] != null) {
            throw new Exception(thrown[0]);
        }
    }

    public static final class Pool extends DataLoaderPool {

        @Override
        protected Enumeration<DataLoader> loaders() {
            return org.openide.util.Enumerations.<DataLoader>singleton(
                    MyLoader.getLoader(MyLoader.class));
        }
    }

    public static final class MyLoader extends UniFileLoader {

        public MyLoader() {
            super(MyObject.class.getName());
            getExtensions().addExtension("java");
        }

        protected String displayName() {
            return "TwoPart";
        }

        @Override
        public FileObject findPrimaryFile(FileObject fo) {
            if (fo.getNameExt().equals("F.java")) {
                return fo;
            } else {
                return super.findPrimaryFile(fo);
            }
        }

        @Override
        protected MultiDataObject createMultiObject(FileObject primaryFile)
                throws DataObjectExistsException, IOException {
            return new MyObject(this, primaryFile);
        }

        @Override
        protected MultiDataObject.Entry createPrimaryEntry(MultiDataObject obj,
                FileObject primaryFile) {
            return new MyEntry(obj, primaryFile);
        }

    }

    public static final class MyObject extends MultiDataObject {

        public MyObject(MyLoader l, FileObject folder) throws
                DataObjectExistsException {
            super(folder, l);
        }
    }

    public static final class MyEntry extends FileEntry {

        public MyEntry(MultiDataObject obj, FileObject fo) {
            super(obj, fo);
        }

        @Override
        public FileObject move(FileObject f, String suffix) throws IOException {
            FileObject orig = getFile();

            FileObject ret = super.move(f, suffix);

            DataObject obj = MyLoader.getLoader(MyLoader.class).findDataObject(
                    orig, new DataLoader.RecognizedFiles() {
                        @Override
                        public void markRecognized(FileObject fo) {
                        }
                    });
            assertNotNull(ret);
            assertNull(obj);

            return ret;
        }
    }

    private static class LogHandlerAdapter extends Handler {

        @Override
        public void publish(LogRecord record) {
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }
    }
}
