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
package org.netbeans.modules.versioning.spi;

import java.io.IOException;
import javax.swing.event.ChangeEvent;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileObject;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ChangeListener;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.versioning.spi.testvcs.TestVCS;
import org.netbeans.modules.versioning.spi.testvcs.TestVCSVisibilityQuery;
import org.netbeans.spi.queries.VisibilityQueryChangeEvent;

/**
 * Versioning SPI unit tests of VCSVisibilityQuery.
 * 
 * @author Tomas Stupka
 */
public class VCSVisibilityQueryTest extends NbTestCase {
    

    public VCSVisibilityQueryTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        File userdir = new File(getWorkDir() + "userdir");
        userdir.mkdirs();
        System.setProperty("netbeans.user", userdir.getAbsolutePath());
        VisibilityQuery.getDefault().isVisible(userdir); // whatever file. just ensure all VQ impls are alive
        super.setUp();
    }

    public void testVQ() throws FileStateInvalidException, IOException, Exception {
        VQChangeListener cl = new VQChangeListener();
        VisibilityQuery.getDefault().addChangeListener(cl);
        File visible = createVersionedFile("this-file-is-visible", true);
        FileObject visibleFO = FileUtil.toFileObject(visible);
        cl.testVisibility(true, visible, visibleFO);
        assertTrue(VisibilityQuery.getDefault().isVisible(visible));
        assertTrue(VisibilityQuery.getDefault().isVisible(visibleFO));

        File invisible = createVersionedFile("this-file-is-", false);
        FileObject invisibleFO = FileUtil.toFileObject(invisible);
        cl.testVisibility(false, invisible, invisibleFO);
        assertFalse(VisibilityQuery.getDefault().isVisible(invisible));
        assertFalse(VisibilityQuery.getDefault().isVisible(invisibleFO));
        VisibilityQuery.getDefault().removeChangeListener(cl);
    }
    
    public void testFireForAll() {
        final boolean [] received = new boolean[] {false};
        ChangeListener list;
        VisibilityQuery.getDefault().addChangeListener(list = new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent ce) {
                received[0] = true;
            }
        });
        try {
            TestVCS.getInstance().getVisibilityQuery().fireVisibilityChanged();
            assertTrue(received[0]);
        } finally {
            VisibilityQuery.getDefault().removeChangeListener(list);
        }
    }
    
    public void testFireForFiles() throws IOException {
        File f1 = createVersionedFile("f1", true);
        File f2 = createVersionedFile("f2", true);
        
        final List<String> received = new ArrayList<String>();
        ChangeListener list;
        VisibilityQuery.getDefault().addChangeListener(list = new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent ce) {
                assertTrue(ce instanceof VisibilityQueryChangeEvent);
                FileObject[] fos = ((VisibilityQueryChangeEvent)ce).getFileObjects();
                assertEquals(2, fos.length);
                received.add(fos[0].getName());
                received.add(fos[1].getName());
            }
        });
        try {
            TestVCS.getInstance().getVisibilityQuery().fireVisibilityChanged(new File[] {f1, f2});
            assertTrue(received.contains(f1.getName()));
            assertTrue(received.contains(f2.getName()));
        } finally {
            VisibilityQuery.getDefault().removeChangeListener(list);
        }
    }

    private File createVersionedFile(String name, boolean visible) throws IOException {
        File folder = new File(getWorkDir(), TestVCS.VERSIONED_FOLDER_SUFFIX);
        folder.mkdirs();
        File f = new File(folder, name + (!visible ? TestVCSVisibilityQuery.INVISIBLE_FILE_SUFFIX : ""));
        f.createNewFile();
        return f;
    }

    private class VQChangeListener implements ChangeListener {
        private static final long MAXTIME = 30000;
        private static final long STABLETIME = 10000;

        @Override
        public void stateChanged(ChangeEvent e) {
            synchronized(this) {
                notifyAll();
            }
        }

        void testVisibility (boolean expectedVisibility, Object... files) throws Exception {
            boolean ok = false;
            long maxTime = System.currentTimeMillis() + MAXTIME;
            long stableFor = 0;
            boolean cont = true;
            while (cont) {
                ok = true;
                synchronized(this) {
                    for (Object o : files) {
                        assert o instanceof File || o instanceof FileObject;
                        ok &= expectedVisibility == (o instanceof File
                                ? VisibilityQuery.getDefault().isVisible((File) o)
                                : VisibilityQuery.getDefault().isVisible((FileObject) o));
                    }
                    if (ok) {
                        long t = System.currentTimeMillis();
                        wait(STABLETIME - stableFor); // stable state for these files should take 10 seconds
                        stableFor += System.currentTimeMillis() - t;
                    }
                }
                if (!ok) {
                    stableFor = 0;
                }
                cont = stableFor < STABLETIME && System.currentTimeMillis() < maxTime; // continue until stable state is reached
            }
            long t = System.currentTimeMillis();
            assertTrue("Takes too long: " + (t - maxTime + MAXTIME), t < maxTime);
            assertTrue(ok);
            assertTrue(stableFor >= 10000);
        }
    }
}
