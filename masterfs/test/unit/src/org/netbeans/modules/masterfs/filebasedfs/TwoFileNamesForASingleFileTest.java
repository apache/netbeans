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
package org.netbeans.modules.masterfs.filebasedfs;

import java.io.File;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.masterfs.filebasedfs.fileobjects.FolderObj;
import org.netbeans.modules.masterfs.filebasedfs.utils.FileChangedManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class TwoFileNamesForASingleFileTest extends NbTestCase {
    private FileObject fo;

    public TwoFileNamesForASingleFileTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        System.setSecurityManager(FileChangedManager.getInstance());
    }
    
    public void testTwoFileNames() throws Exception {
        
        File f = getWorkDir();
        fo = FileUtil.toFileObject(f);
        
        FileObject[] empty = fo.getChildren();
        assertEquals("Empty is empty", 0, empty.length);
        
        final File n = new File(f, "x.txt");
        n.createNewFile();
        FileObject fn = FileUtil.toFileObject(f);
        assertNotNull("File object found", fn);

        refresh();
        FileObject[] notEmpty = fo.getChildren();
        assertEquals("One file found", 1, notEmpty.length);

        class R implements Runnable {
            FileObject[] none;
            FileObject[] one;
            @Override
            public void run() {
                refresh();
                n.delete();
                none = fo.getChildren();
                assertTrue("Directory creation succeeds", n.mkdirs());
                refresh();
                one = fo.getChildren();
            }
        }
        R r = new R();

        Task task = RequestProcessor.getDefault().create(r);
        final Logger LOG = Logger.getLogger(FolderObj.class.getName());
        DelayingHandler delayer = new DelayingHandler(task);
        delayer.setLevel(Level.ALL);
        LOG.setLevel(Level.ALL);
        LOG.addHandler(delayer);
        task.schedule(0);
        FileObject[] block = fo.getChildren();
        assertTrue("Delayer was active", delayer.delayed);
        
        assertEquals("None is empty", 0, r.none.length);
        assertEquals("One is not empty", 1, r.one.length);
        assertEquals("One children in block as well", 1, block.length);
        assertTrue("Very likely it is a folder", block[0].isFolder());
    }
    
    final void refresh() {
        fo.refresh();
    }
    
    private class DelayingHandler extends Handler {
        volatile boolean delayed;
        final Task waitFor;
        final Thread threadToDelay;
                
        public DelayingHandler(Task waitFor) {
            this.waitFor = waitFor;
            this.threadToDelay = Thread.currentThread();
        }
        
        @Override
        public void publish(LogRecord record) {
            if (
                record.getMessage() == null || 
                !record.getMessage().contains("computeChildren, filenames")
            ) {
                return;
            }
            
            if (Thread.currentThread() == threadToDelay) {
                if (!delayed) {
                    waitFor.waitFinished();
                    delayed = true;
                }
            }
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }
        
    }
}
