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
package org.netbeans.core.startup.layers;

import java.io.File;
import java.io.IOException;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.core.startup.layers.LocalFileSystemEx;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MultiFileSystem;
import org.openide.filesystems.Repository;
import org.openide.filesystems.TestUtilHid;
import org.openide.util.Lookup;
import org.openide.util.lookup.InstanceContent;

/**
 * Simulates deadlock in issue 133616.
 *
 * @author Jiri Skrivanek
 */
public class LocalFileSystemEx133616Test extends NbTestCase {
    static {
        System.setProperty("org.openide.util.Lookup", LocalFileSystemEx133616Test.class.getName() + "$Lkp");
    }

    /** Tested FileSystem to be registered in Lookup. */
    private static FileSystem testedFS = null;

    public LocalFileSystemEx133616Test(String name) {
        super(name);
    }

    /** Simulates deadlock issue 133616
     * - create MultiFileSystem
     * - create lookup to set our MultiFileSystem and system filesystem
     * - create handler to manage threads
     * - put test FileObject to 'potentialLock' set
     * - call hasLocks
     *   - it call LocalFileSystemEx.getInvalid which ends in our DeadlockHandler
     *   - it starts lockingThread which calls FileObject.lock which locks our FileObject
     *   - when we in LocalFileSystemEx.lock, we notify main thread which continues
     *     in getInvalid and tries to accuire lock on FileObject and it dead locks
     */
    public void testLocalFileSystemEx133616() throws Exception {
        System.setProperty("workdir", getWorkDirPath());
        clearWorkDir();

        FileSystem lfs = TestUtilHid.createLocalFileSystem("mfs1" + getName(), new String[]{"/fold/file1"});
        LocalFileSystemEx exfs = new LocalFileSystemEx();
        exfs.setRootDirectory(FileUtil.toFile(lfs.getRoot()));
        FileSystem xfs = TestUtilHid.createXMLFileSystem(getName(), new String[]{});
        FileSystem mfs = new MultiFileSystem(exfs, xfs);
        testedFS = mfs;
        Lookup l = Lookup.getDefault();
        if (!(l instanceof Lkp)) {
            fail("Wrong lookup: " + l);
        }
        ((Lkp)l).init();

        final FileObject file1FO = mfs.findResource("/fold/file1");
        File file1File = FileUtil.toFile(file1FO);

        Logger.getLogger(LocalFileSystemEx.class.getName()).setLevel(Level.FINEST);
        Logger.getLogger(LocalFileSystemEx.class.getName()).addHandler(new DeadlockHandler(file1FO));
        LocalFileSystemEx.potentialLock(file1FO.getPath());
        LocalFileSystemEx.hasLocks();
    }

    class DeadlockHandler extends Handler {

        private FileObject fileObject;

        public DeadlockHandler(FileObject fo) {
            super();
            this.fileObject = fo;
        }

        public synchronized void publish(LogRecord rec) {
            if ("133616 - checking invalid".equals(rec.getMessage())) {
                Thread lockingThread = new Thread(new Runnable() {

                    public void run() {
                        try {
                            fileObject.lock().releaseLock();
                        } catch (IOException ex) {
                            fail(ex.getMessage());
                        }
                    }
                }, "Locking");
                lockingThread.start();
                try {
                    wait();
                } catch (InterruptedException ex) {
                    fail(ex.getMessage());
                }
            }
            if ("133616 - in lock".equals(rec.getMessage())) {
                notify();
            }
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() {
        }
    }

    public static final class Lkp extends org.openide.util.lookup.AbstractLookup {
        private final InstanceContent ic;

        public Lkp() {
            this(new org.openide.util.lookup.InstanceContent());
        }

        private Lkp(org.openide.util.lookup.InstanceContent ic) {
            super(ic);
            this.ic = ic;
        }
        
        public void init() {
            ic.add(new Repository(testedFS));
        }
    }
}
  
  
  
