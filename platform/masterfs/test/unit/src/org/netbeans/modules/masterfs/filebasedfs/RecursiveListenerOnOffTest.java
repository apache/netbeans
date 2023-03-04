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
import java.io.OutputStream;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.netbeans.modules.masterfs.filebasedfs.FileUtilTest.TestFileChangeListener;
import org.netbeans.modules.masterfs.filebasedfs.fileobjects.FolderObj;
import org.netbeans.modules.masterfs.providers.ProvidedExtensionsTest;
import org.openide.filesystems.*;

/**
 * @author Jaroslav Tulach
 */
public class RecursiveListenerOnOffTest extends NbTestCase {

    private static final long TIMEOUT = 5;

    static {
        System.getProperties().put("org.netbeans.modules.masterfs.watcher.disable", "true");
        MockServices.setServices(ProvidedExtensionsTest.AnnotationProviderImpl.class);
    }

    private final Logger LOG;

    public RecursiveListenerOnOffTest(String name) {
        super(name);
        LOG = Logger.getLogger("TEST." + name);
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
    }

    @Override
    protected Level logLevel() {
        return Level.FINER;
    }

    public void testRecursiveListenerIsOn() throws Exception {
        clearWorkDir();
        
        final File rootF = getWorkDir();
        final File dirF = new File(rootF, "dir");
        File fileF = new File(dirF, "file1");
        File subdirF = new File(dirF, "subdir");
        File subfileF = new File(subdirF, "subfile");
        File subsubdirF = new File(subdirF, "subsubdir");
        File subsubfileF = new File(subsubdirF, "subsubfile");
        subsubdirF.mkdirs();

        TestFileChangeListener fcl = new TestFileChangeListener();
        FileUtil.addRecursiveListener(fcl, dirF);
        
        FileObject fo = FileUtil.toFileObject(subsubdirF);
        assertNotNull("Found", fo);
        assertEquals("It is folder", FolderObj.class, fo.getClass());
        FolderObj obj = (FolderObj)fo;
        
        assertTrue("There is a listener around", obj.hasRecursiveListener());
        
        FileUtil.addRecursiveListener(fcl, subdirF);
        
        assertTrue("There is still a listener around", obj.hasRecursiveListener());
        
        FileUtil.removeRecursiveListener(fcl, dirF);

        assertTrue("Listener still remains around", obj.hasRecursiveListener());
        
        FileUtil.removeRecursiveListener(fcl, subdirF);
        assertFalse("No Listener anymore", obj.hasRecursiveListener());

        LOG.info("OK");
    }

    public void testRecursiveListenerInsideRenamedFolder() throws Exception {

        final FileObject wd = FileUtil.toFileObject(getWorkDir());
        final FileObject prj = wd.createFolder("prj");          //NOI18N
        final FileObject src = prj.createFolder("src");         //NOI18N
        final FileObject pkg = src.createFolder("pkg");         //NOI18N
        final FileObject file = pkg.createData("Test","java");  //NOI18N

        class FL extends FileChangeAdapter {

            private final Semaphore sem = new Semaphore(0);
            private final Queue<File> waitFor = new ArrayDeque<File>();

            public synchronized void expect(final File... files) {
                waitFor.addAll(Arrays.asList(files));
            }

            public boolean await() throws InterruptedException {
                final int size;
                synchronized (this) {
                    size = waitFor.size();
                }
                return sem.tryAcquire(size, TIMEOUT, TimeUnit.SECONDS);
            }

            @Override
            public void fileChanged(FileEvent fe) {
                final File f = FileUtil.toFile(fe.getFile());
                final boolean remove;
                synchronized (this) {
                    remove = waitFor.remove(f);
                }
                if (remove) {
                    sem.release();
                }
            }

        }

        final FL fl = new FL();
        final File srcDir = FileUtil.toFile(src);
        FileUtil.addRecursiveListener(fl, srcDir);
        FileLock lck = prj.lock();
        try {
            prj.rename(lck, "prj2", null);      //NOI18N
        } finally {
            lck.releaseLock();
        }
        FileUtil.removeRecursiveListener(fl, srcDir);
        final File newSrcDir = FileUtil.toFile(src);
        FileUtil.addRecursiveListener(fl, newSrcDir);
        fl.expect(FileUtil.toFile(file));
        lck = file.lock();
        try {
            final OutputStream out = file.getOutputStream(lck);
            out.write(1);
            out.close();
        } finally {
            lck.releaseLock();
        }
        assertTrue(fl.await());
    }
}
