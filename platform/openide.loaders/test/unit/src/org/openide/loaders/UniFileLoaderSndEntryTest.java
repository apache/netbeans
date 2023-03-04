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
import java.util.Enumeration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.MultiDataObject.Entry;
import org.openide.util.Enumerations;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

/** Teasing UniFileLoader with recognizing entry during move operation.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class UniFileLoaderSndEntryTest extends NbTestCase {
    private FileObject two;
    private FileObject one;
    private static FileObject data;
    
    public UniFileLoaderSndEntryTest(String name) {
        super(name);
    }

    @Override
    protected int timeOut() {
        return 30000;
    }

    @Override
    protected void setUp() throws Exception {
        MockServices.setServices(Pool.class);
        
        clearWorkDir();
        File tw = new File(new File(getWorkDir(), "one"), "two");
        tw.mkdirs();
        
        this.one = FileUtil.toFileObject(tw.getParentFile());
        this.two = FileUtil.toFileObject(tw);
        this.data = one.createData("I.knowYou");
    }
    
    public void testMoveAndQueryInMiddle() throws Throwable {
        DataFolder fTwo = DataFolder.findFolder(two);
        DataObject obj = DataObject.find(data);
        assertEquals(CntrlUniLoader.class, obj.getLoader().getClass());
        
        obj.move(fTwo);
        
        assertEquals("Parent has changed", fTwo, obj.getFolder());
        
        assertNotNull("Teaser recognizer started", Teaser.task);
        Teaser.task.waitFinished();
        
        assertNotNull("Data object found by teaser thread", Teaser.obj);
        if (Teaser.obj instanceof DataObjectNotFoundException) {
            return;
        }
        if (Teaser.obj instanceof Throwable) {
            throw (Throwable)Teaser.obj;
        } else {
            fail("Surprising content of obj: " + Teaser.obj);
        }
    }

    public static final class Pool extends DataLoaderPool {
        @Override
        protected Enumeration<? extends DataLoader> loaders() {
            return Enumerations.singleton(CntrlUniLoader.me());
        }
    }
    
    private static class Teaser implements Runnable {
        private static CountDownLatch WAIT;
        private static CountDownLatch WAITING;
        
        private static Object obj;
        private static Task task;
        
        @Override
        public void run() {
            try {
                obj = DataObject.find(data);
            } catch (Throwable ex) {
                obj = ex;
                // OK, finish the waiting
                WAITING.countDown();
                WAIT.countDown();
            }
        }
    }
    
    
    public static final class CntrlUniLoader extends UniFileLoader {
        
        public CntrlUniLoader() {
            super(MultiDataObject.class.getName());
        }
        
        public static CntrlUniLoader me() {
            return getLoader(CntrlUniLoader.class);
        }

        @Override
        protected FileObject findPrimaryFile(FileObject fo) {
            if (Teaser.WAIT != null) {
                Teaser.WAITING.countDown();
                try {
                    Teaser.WAIT.await();
                } catch (InterruptedException ex) {
                    throw new IllegalStateException(ex);
                }
            }
            return fo.hasExt("knowYou") ? fo : null;
        }
        
        @Override
        protected MultiDataObject createMultiObject(FileObject primaryFile) 
        throws DataObjectExistsException, IOException {
            return new MultiDataObject(primaryFile, this);
        }

        @Override
        protected Entry createPrimaryEntry(MultiDataObject obj, FileObject primaryFile) {
            return new FileEntry(obj, primaryFile) {
                @Override
                public FileObject move(FileObject f, String suffix) throws IOException {
                    try {
                        Teaser.WAIT = new CountDownLatch(1);
                        Teaser.WAITING = new CountDownLatch(1);
                        FileObject fo;
                        synchronized (DataObjectPool.getPOOL()) {
                            Teaser.task = RequestProcessor.getDefault().post(new Teaser());
                            Teaser.WAITING.await(300, TimeUnit.MILLISECONDS);
                            fo = super.move(f, suffix); 
                        }
                        Teaser.WAITING.await();
                        Teaser.WAIT.countDown();
                        return fo;
                    } catch (InterruptedException ex) {
                        throw new IllegalStateException(ex);
                    }
                }
                
            };
        }
        
        
    }
}
