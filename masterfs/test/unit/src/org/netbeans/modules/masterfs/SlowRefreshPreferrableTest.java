/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
