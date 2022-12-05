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
package org.openide.text;

import java.io.IOException;
import java.io.OutputStream;


import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.openide.util.NbMutexEventProvider;
import org.openide.cookies.EditCookie;

import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.nodes.CookieSet;

/**
 */
public class FileSizeThreshholdExceptionTest extends NbTestCase {
    
    private final String BIG_FILE_PROP = "org.openide.text.big.file.size";

    static {
        System.setProperty("org.openide.windows.DummyWindowManager.VISIBLE", "false");
    }
    // for file object support
    String content = "";
    long expectedSize = -1;
    java.util.Date date = new java.util.Date();
    FileObject fileObject;
    org.openide.filesystems.FileSystem fs;
    static FileSizeThreshholdExceptionTest RUNNING;

    static {
        System.setProperty("org.openide.util.Lookup", "org.openide.text.FileSizeThreshholdExceptionTest$Lkp");
    }

    public FileSizeThreshholdExceptionTest(String s) {
        super(s);
    }

    @Override
    protected void setUp() throws Exception {
        RUNNING = this;

        fs = org.openide.filesystems.FileUtil.createMemoryFileSystem();
        org.openide.filesystems.Repository.getDefault().addFileSystem(fs);
        org.openide.filesystems.FileObject root = fs.getRoot();
        fileObject = org.openide.filesystems.FileUtil.createData(root, "my.obj");
    }

    @Override
    protected void tearDown() throws Exception {
        waitEQ();

        RUNNING = null;
        org.openide.filesystems.Repository.getDefault().removeFileSystem(fs);
    }

    @Override
    protected boolean runInEQ() {
        return false;
    }

    private void waitEQ() throws Exception {
        javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
            }
        });
    }

    DES support() throws Exception {
        DataObject obj = DataObject.find(fileObject);

        assertEquals("My object was created", MyDataObject.class, obj.getClass());
        Object cookie = obj.getLookup().lookup(org.openide.cookies.OpenCookie.class);
        assertNotNull("Our object has this cookie", cookie);
        assertEquals("It is my cookie", DES.class, cookie.getClass());

        return (DES) cookie;
    }

    public void doTestThreshholdValue(int sizeInMb) throws Exception {
        final int size = 1024 * 1024 * sizeInMb;
        DES des = support();
        DataEditorSupport.Env env = des.getEnv();
        OutputStream out = fileObject.getOutputStream();
        out.write(new byte[size + 1]);
        out.close();
        try {
            env.inputStream();
            assertTrue("File size is greater than limit, but no exception appeared", false);
        } catch (IOException ex) {
            // ignoring, as this is expected exception
        }        
        out = fileObject.getOutputStream();
        out.write(new byte[size]);
        out.close();
        try {
            env.inputStream();
        } catch (IOException ex) {
            assertTrue("File size is lower than limit, but exception appeared", false);
        }
    }
    
    public void testThreshholdDefaultValue() throws Exception {
        System.getProperties().remove(BIG_FILE_PROP);
        doTestThreshholdValue(5);
    }
    
    public void testThreshholdNotDefaultValue() throws Exception {
        final int SIZE = 2;
        System.setProperty(BIG_FILE_PROP, "" + SIZE);
        doTestThreshholdValue(SIZE);
    }
    
    /**
     * Implementation of the DES
     */
    private static final class DES extends DataEditorSupport
            implements OpenCookie, EditCookie {

        private final Env env;

        public DES(DataObject obj, Env env) {
            super(obj, env);
            this.env = env;
        }

        public Env getEnv() {
            return env;
        }
    }

    /**
     * MyEnv that uses DataEditorSupport.Env
     */
    private static final class MyEnv extends DataEditorSupport.Env {

        static final long serialVersionUID = 1L;

        public MyEnv(MyDataObject obj) {
            super(obj);
        }

        @Override
        protected FileObject getFile() {
            return super.getDataObject().getPrimaryFile();
        }

        @Override
        protected FileLock takeLock() throws IOException {
            MyDataObject my = (MyDataObject) getDataObject();
            return my.getPrimaryEntry().takeLock();
        }
    }

    public static final class Lkp extends org.openide.util.lookup.AbstractLookup {

        public Lkp() {
            this(new org.openide.util.lookup.InstanceContent());
        }

        private Lkp(org.openide.util.lookup.InstanceContent ic) {
            super(ic);

            ic.add(new Pool());
            ic.add(new NbMutexEventProvider());
        }
    } // end of Lkp

    private static final class Pool extends org.openide.loaders.DataLoaderPool {

        @Override
        protected java.util.Enumeration loaders() {
            return org.openide.util.Enumerations.singleton(MyLoader.get());
        }
    }

    public static final class MyLoader extends org.openide.loaders.UniFileLoader {

        public int primary;

        public static MyLoader get() {
            return MyLoader.findObject(MyLoader.class, true);
        }

        public MyLoader() {
            super(MyDataObject.class.getName());
            getExtensions().addExtension("obj");
        }

        protected String displayName() {
            return "MyPart";
        }

        @Override
        protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
            return new MyDataObject(this, primaryFile);
        }

        @Override
        protected MultiDataObject.Entry createPrimaryEntry(MultiDataObject obj, FileObject primaryFile) {
            primary++;
            return new org.openide.loaders.FileEntry(obj, primaryFile);
        }
    }

    public static final class MyDataObject extends MultiDataObject
            implements CookieSet.Factory {

        public MyDataObject(MyLoader l, FileObject folder) throws DataObjectExistsException {
            super(folder, l);
            getCookieSet().add(OpenCookie.class, this);
        }

        @Override
        public org.openide.nodes.Node.Cookie createCookie(Class klass) {
            return new DES(this, new MyEnv(this));
        }
    }
}
