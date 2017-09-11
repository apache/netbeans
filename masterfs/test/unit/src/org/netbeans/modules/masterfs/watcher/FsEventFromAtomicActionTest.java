package org.netbeans.modules.masterfs.watcher;

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

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
