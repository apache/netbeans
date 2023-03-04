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

package org.netbeans.modules.masterfs.watcher;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem.AtomicAction;
import org.openide.filesystems.FileUtil;

public class FsEventFromAtomicActionTest extends NbTestCase {

    static final String FILE_PREFIX = "myUniqueFileName";
    // 10000 is ok for test to fail; lower number fails just sometimes
    static final int RUNS = 10000;


    public FsEventFromAtomicActionTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
    }

    // this test fails regularly
    public void testFiredFromManyAtomicActions() throws Exception {
        final File workDir = getWorkDir();
        final FileObject workDirFo = FileUtil.toFileObject(workDir);

        final MyAtomicAction myAtomicAction = new MyAtomicAction();
        MyFileChangeListener myChangeListener = new MyFileChangeListener(myAtomicAction);
        FileUtil.addRecursiveListener(myChangeListener, workDir);

        assertEquals("files before", 0, workDir.list().length);

        for (int i = 0; i < RUNS; ++i) {
            final int j = i;
            myAtomicAction.runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        FileUtil.createData(workDirFo, FILE_PREFIX + j);
                    } catch (IOException ex) {
                        // checked later
                    }
                }
            };
            FileUtil.runAtomicAction(myAtomicAction);
        }

        assertEquals("files after", RUNS, workDir.list().length);
        assertEquals(printEvents(myChangeListener.notFromAtomicAction), 0, myChangeListener.notFromAtomicAction.size());
        assertEquals("events", RUNS, myChangeListener.events.get());
    }

    // this test fails sometimes
    public void testFiredFromOneAtomicAction() throws Exception {
        final File workDir = getWorkDir();
        final FileObject workDirFo = FileUtil.toFileObject(workDir);

        final AtomicAction myAtomicAction = new AtomicAction() {
            @Override
            public void run() throws IOException {
                try {
                    for (int i = 0; i < RUNS; ++i) {
                        FileUtil.createData(workDirFo, FILE_PREFIX + i);
                    }
                } catch (IOException ex) {
                    // checked later
                }
            }
        };
        MyFileChangeListener myChangeListener = new MyFileChangeListener(myAtomicAction);
        FileUtil.addRecursiveListener(myChangeListener, workDir);

        assertEquals("files before", 0, workDir.list().length);

        FileUtil.runAtomicAction(myAtomicAction);

        assertEquals("files after", RUNS, workDir.list().length);
        assertEquals(printEvents(myChangeListener.notFromAtomicAction), 0, myChangeListener.notFromAtomicAction.size());
        assertEquals("events", RUNS, myChangeListener.events.get());
    }

    private String printEvents(Collection<FileEvent> notFromAtomicAction) {
        StringBuilder sb = new StringBuilder("not all events from atomic action");
        // uncomment for more details
//        for (FileEvent event : notFromAtomicAction) {
//            sb.append("\n");
//            sb.append(event);
//        }
        return sb.toString();
    }

    //~ Inner classes

    private static final class MyFileChangeListener extends FileChangeAdapter {

        final AtomicAction atomicAction;
        final AtomicInteger events = new AtomicInteger();
        final Collection<FileEvent> notFromAtomicAction = Collections.synchronizedList(new LinkedList<FileEvent>());


        public MyFileChangeListener(AtomicAction atomicAction) {
            this.atomicAction = atomicAction;
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
            testFileEvent(fe);
        }

        private void testFileEvent(FileEvent event) {
            if (event.getFile().getNameExt().startsWith(FILE_PREFIX)) {
                events.incrementAndGet();
                if (!event.firedFrom(atomicAction)) {
                    notFromAtomicAction.add(event);
//                        throw new RuntimeException(event.toString());
                }
            }
        }

    }

    private static final class MyAtomicAction implements AtomicAction {

        Runnable runnable;

        @Override
        public void run() throws IOException {
            if (runnable != null) {
                runnable.run();
            }
        }

    }

}
