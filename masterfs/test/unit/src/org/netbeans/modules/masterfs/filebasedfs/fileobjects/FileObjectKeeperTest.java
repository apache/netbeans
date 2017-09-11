/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.masterfs.filebasedfs.fileobjects;

import java.io.IOException;
import java.util.concurrent.Semaphore;
import org.junit.Test;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author jhavlin
 */
public class FileObjectKeeperTest extends NbTestCase {

    /**
     * FileObject for this test's working directory.
     */
    private FileObject workDirFO;

    public FileObjectKeeperTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        workDirFO = FileUtil.toFileObject(getWorkDir());
    }

    /**
     * Test for bug 235928 - Deadlock scanning after change name of project with
     * folder.
     *
     * @throws java.io.IOException
     * @throws java.lang.InterruptedException
     */
    @Test
    public void testBug235928() throws IOException, InterruptedException {

        final Semaphore s = new Semaphore(0);

        /**
         * Listener that enables running of thread t2 while the events are
         * processed in the main thread.
         */
        final FileChangeListener fcl = new FileChangeAdapter() {
            @Override
            public void fileFolderCreated(FileEvent fe) {
                s.release(); // Thread t2 can run.
                try {
                    s.acquire(2); // Wait until thread t2 is finished.
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        };

        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                FileChangeListener l = new FileChangeAdapter();
                try {
                    s.acquire();
                    // Try to add a listener, to ensure that the keeper object
                    // is not locked.
                    workDirFO.addRecursiveListener(l);
                    s.release(2);
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                } finally {
                    workDirFO.removeRecursiveListener(l);
                }
            }
        });

        workDirFO.addRecursiveListener(fcl);
        try {
            t2.start();
            // Create folder and process events.
            workDirFO.createFolder("test");
            t2.join();
        } finally {
            workDirFO.removeRecursiveListener(fcl);
        }
    }
}
