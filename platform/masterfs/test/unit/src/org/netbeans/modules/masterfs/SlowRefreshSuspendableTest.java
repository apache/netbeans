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

package org.netbeans.modules.masterfs;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.modules.masterfs.filebasedfs.children.ChildrenSupportTest;
import org.netbeans.modules.masterfs.filebasedfs.fileobjects.TestUtils;
import org.netbeans.modules.masterfs.filebasedfs.utils.FileChangedManager;
import org.netbeans.modules.masterfs.filebasedfs.utils.FileChangedManagerTest;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.RequestProcessor;

public class SlowRefreshSuspendableTest extends NbTestCase {
    static {
        System.setProperty("org.netbeans.modules.masterfs.watcher.disable", "true");
    }
    private Logger LOG;
    private FileObject testFolder;

    static {
        // Just pre load the classes
        FileChangedManagerTest.assertNoLock();
        ChildrenSupportTest.assertNoLock();
    }

    public SlowRefreshSuspendableTest(String testName) {
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

        FileChangedManager.install();
    }

    @RandomlyFails // NB-Core-Build #4386: Background I/O access needs to stop before we finish our task
    public void testRefreshCanBeSuspended() throws Exception {
        long lm = System.currentTimeMillis();
        LOG.info("starting testRefreshCanBeSuspended " + lm);
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
            volatile int cnt;
            volatile FileEvent event;

            @Override
            public void fileDataCreated(FileEvent fe) {
                changedOrCreated(fe); // See bug 231600.
            }

            @Override
            public void fileChanged(FileEvent fe) {
                changedOrCreated(fe);
            }

            private void changedOrCreated(FileEvent fe) {
                cnt++;
                event = fe;
                LOG.log(Level.INFO, "file change {0} cnt: {1}", new Object[]{fe.getFile(), cnt});
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

        Object obj = TestUtils.findSlowRefresh(testFolder);
        assertNotNull("Refresh attribute found", obj);
        assertTrue("It is instance of runnable:  " + obj, obj instanceof Runnable);

        Runnable r = (Runnable)obj;
        class AE extends ActionEvent implements Runnable {
            List<FileObject> files = new ArrayList<FileObject>();
            volatile boolean boosted;
            volatile boolean finished;
            int goingIdle;
            
            public AE() {
                super("", 0, "");
            }

            @Override
            public void setSource(Object newSource) {
                LOG.log(Level.INFO, "Set source called: {0}", newSource);
                assertTrue(newSource instanceof Object[]);
                Object[] arr = (Object[])newSource;
                assertTrue("Three elements at leat ", 3 <= arr.length);
                assertTrue("first is int", arr[0] instanceof Integer);
                assertTrue("2nd is int", arr[1] instanceof Integer);
                assertTrue("3rd is fileobject", arr[2] instanceof FileObject);
                files.add((FileObject)arr[2]);
                super.setSource(newSource);
            }

            @Override
            public void run() {
                FileChangedManagerTest.assertNoLock();
                ChildrenSupportTest.assertNoLock();
                goingIdle++;
            }

            void doWork() {
                try {
                    File busyFile = File.createTempFile("xyz", ".abc");
                    LOG.log(Level.INFO, "Created {0}", busyFile);
                    for (int i = 0; i  < 2000; i ++) {
                        assertTrue("Can be read", busyFile.canRead());
                        LOG.log(Level.INFO, "Touched {0}", i);
                        if (i > 100) {
                            synchronized (this) {
                                boosted = true;
                                notifyAll();
                            }
                        }
                    }
                    busyFile.delete();
                    LOG.log(Level.INFO, "deleted {0}", busyFile);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                finished = true;
                LOG.info("finished");
            }

            public synchronized void waitBoosted() throws Exception {
                while (!boosted) {
                    wait();
                }
            }
        }
        final AE counter = new AE();

        LOG.info("Posting AE into RP");
        // starts 5s of disk checking
        RequestProcessor.Task task = RequestProcessor.getDefault().post(new Runnable() {
            @Override
            public void run() {
                counter.doWork();
            }
        });

        // connect together
        r.equals(counter);

        LOG.info("Waiting for I/O boost");
        counter.waitBoosted();
        LOG.info("Starting refresh");
        // do the refresh
        r.run();
        LOG.log(Level.INFO, "Refresh finished {0} cnt: {1}", new Object[]{counter.finished, listener.cnt});

        assertTrue("Background I/O access needs to stop before we finish our task", counter.finished);

        assertTrue("At least one Change notified", 1 <= listener.cnt);
        assertEquals("Right file", file, FileUtil.toFile(listener.event.getFile()));
        assertEquals("Right source", file.getParentFile(), FileUtil.toFile((FileObject)listener.event.getSource()));
        if (counter.goingIdle == 0) {
            fail("The I/O subsystem shall notify the action that it went idle at least once");
        }
    }
}
