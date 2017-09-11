/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
 * Microsystems, Inc. All Rights Reserved.
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
  
  
  
