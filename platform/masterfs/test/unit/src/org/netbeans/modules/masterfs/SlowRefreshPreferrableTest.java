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

package org.netbeans.modules.masterfs;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.masterfs.filebasedfs.fileobjects.TestUtils;
import org.netbeans.modules.masterfs.filebasedfs.utils.FileChangedManager;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.RequestProcessor;

public class SlowRefreshPreferrableTest extends NbTestCase {
    static {
        System.setProperty("org.netbeans.modules.masterfs.watcher.disable", "true");
    }
    private Logger LOG;
    private FileObject testFolder;

    public SlowRefreshPreferrableTest(String testName) {
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

        System.setSecurityManager(new FileChangedManager());
    }

    public void testRefreshPrefersSuggestedFolders() throws Exception {
        long lm = System.currentTimeMillis();
        List<FileObject> all = new ArrayList<FileObject>();
        FileObject fld = testFolder;
        for (int i = 0; i < 20; i++) {
            all.add(fld.createData("text" + i + ".txt"));
            fld = fld.createFolder("fld" + i);
            all.add(fld);
        }
        final FileObject parent = fld;
        FileObject fileObject1 = parent.createData("fileObject1");
        assertNotNull("Just to initialize the stamp", lm);
        FileObject[] arr = testFolder.getChildren();
        assertEquals("Two children", 2, arr.length);

        File file = FileUtil.toFile(fileObject1);
        assertNotNull("File found", file);
        Reference<FileObject> ref = new WeakReference<FileObject>(fileObject1);
        arr = null;
        fileObject1 = null;
        assertGC("File Object can disappear", ref);

        class L extends FileChangeAdapter {
            volatile int cnt;
            FileEvent event;

            @Override
            public void fileDataCreated(FileEvent fe) {
                changedOrCreated(fe); // See bug 231600.
            }

            @Override
            public void fileChanged(FileEvent fe) {
                changedOrCreated(fe);
            }

            private void changedOrCreated(FileEvent fe) {
                LOG.log(Level.INFO, "file change {0}", fe.getFile());
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

        Object obj = TestUtils.findSlowRefresh(testFolder);
        assertNotNull("Refresh attribute found", obj);
        assertTrue("It is instance of runnable:  " + obj, obj instanceof Runnable);

        final Runnable r = (Runnable)obj;
        class AE extends ActionEvent implements Runnable {
            List<FileObject> files = new ArrayList<FileObject>();
            int goingIdle;
            int cnt;
            
            public AE() {
                super("", 0, "");
            }

            @Override
            public void setSource(Object newSource) {
                cnt++;
                LOG.log(Level.INFO, "Set source called: {0}", Thread.interrupted());
                assertTrue(newSource instanceof Object[]);
                Object[] arr = (Object[])newSource;
                //assertEquals("At least five", 4 < arr.length);
                assertTrue("first is int", arr[0] instanceof Integer);
                assertTrue("2nd is int", arr[1] instanceof Integer);
                assertTrue("3rd is fileobject", arr[2] instanceof FileObject);
                assertTrue("4th is cancel value", arr[3] instanceof AtomicBoolean);
                files.add((FileObject)arr[2]);
                super.setSource(newSource);

                if (cnt == 1 && arr.length > 4) {
                    // prefer refresh of parent
                    arr[4] = parent;
                }

                if (cnt == 2) {
                    AtomicBoolean ab = (AtomicBoolean)arr[3];
                    assertTrue("The boolean is set to 'go on': ", ab.get());
                    assertSame("boolean and runnable are the same right now", ab, r);
                    LOG.info("Cancelling task");
                    ab.set(false);
                }
            }

            @Override
            public void run() {
                goingIdle++;
            }
        }
        final AE counter = new AE();

        LOG.info("Posting AE into RP");
        // starts 5s of disk checking
        RequestProcessor RP = new RequestProcessor("Interrupt!", 1, false);
        RequestProcessor.Task task = RP.create(r);

        // connect together
        r.equals(counter);

        assertTrue("Runnable is also 'go on' identifier", r instanceof AtomicBoolean);

        LOG.info("Starting refresh");
        // do the refresh
        task.schedule(0);
        task.waitFinished();
        LOG.info("Refresh finished");

        assertEquals("Two files checked: " + counter.files, 2, counter.files.size());
        assertEquals("Change in preferred file detected", 1, listener.cnt);
    }
}
