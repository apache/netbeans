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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.junit.Log;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class FileUtilAddRecursiveListenerStopTest extends NbTestCase
implements Callable<Boolean>, FileChangeListener {
    private FileObject root;
    private List<FileEvent> events = new ArrayList<FileEvent>();
    private int cnt;
    @SuppressWarnings("NonConstantLogger")
    private Logger LOG;

    public FileUtilAddRecursiveListenerStopTest(String n) {
        super(n);
    }

    @Override
    protected Level logLevel() {
        return Level.FINEST;
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        
        LOG = Logger.getLogger("test." + getName());
        
        root = FileUtil.toFileObject(getWorkDir());
        assertNotNull("Root found", root);

        for (int i = 0; i < 10; i++) {
            if (i % 2 == 0) {
                root.createData("" + i, "txt");
            } else {
                root.createFolder("" + i);
            }
        }
    }

    public void testAddListenerGetsFiveCallbacks() throws IOException {
        cnt = 10;
        FileUtil.addRecursiveListener(this, getWorkDir(), this);
        assertEquals("Counter decreased five times to five", 5, cnt);

        FileObject fourth = root.getFileObject("5");
        assertNotNull("Folder found", fourth);
        fourth.createData("Ahoj");
        assertEquals("One event delivered: " + events, 1, events.size());
    }

    @Override
    public Boolean call() throws Exception {
        LOG.log(Level.INFO, "callback {0}", cnt);
        if (--cnt == 0) {
            LOG.info("Returning true to stop invocations");
            return Boolean.TRUE;
        }
        return null;
    }

    @Override
    public void fileFolderCreated(FileEvent fe) {
        LOG.log(Level.INFO, "fileFolderCreated: {0}", fe.getFile());
        LOG.log(Level.INFO, "Thread dump", new Exception());
        events.add(fe);
    }

    @Override
    public void fileDataCreated(FileEvent fe) {
        LOG.log(Level.INFO, "fileDataCreated: {0}", fe.getFile());
        LOG.log(Level.INFO, "Thread dump", new Exception());
        events.add(fe);
    }

    @Override
    public void fileChanged(FileEvent fe) {
        LOG.log(Level.INFO, "fileChanged: {0}", fe.getFile());
        LOG.log(Level.INFO, "Thread dump", new Exception());
        events.add(fe);
    }

    @Override
    public void fileDeleted(FileEvent fe) {
        LOG.log(Level.INFO, "fileDeleted: {0}", fe.getFile());
        LOG.log(Level.INFO, "Thread dump", new Exception());
        events.add(fe);
    }

    @Override
    public void fileRenamed(FileRenameEvent fe) {
        LOG.log(Level.INFO, "fileRenamed: {0}", fe.getFile());
        LOG.log(Level.INFO, "Thread dump", new Exception());
        events.add(fe);
    }

    @Override
    public void fileAttributeChanged(FileAttributeEvent fe) {
        LOG.log(Level.INFO, "fileAttributeChanged: {0}", fe.getFile());
        LOG.log(Level.INFO, "Thread dump", new Exception());
        events.add(fe);
    }

}
