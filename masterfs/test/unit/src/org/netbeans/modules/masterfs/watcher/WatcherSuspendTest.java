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
package org.netbeans.modules.masterfs.watcher;

import java.io.File;
import org.netbeans.modules.masterfs.providers.Notifier;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.masterfs.filebasedfs.fileobjects.FileObjectFactory;
import org.netbeans.modules.masterfs.filebasedfs.naming.NamingFactory;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

/** Test the behavior of Watcher.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class WatcherSuspendTest extends NbTestCase {
    private TestNotifier notify;
    private L listener;
    private Watcher watcher;
    
    public WatcherSuspendTest(String s) {
        super(s);
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        MockServices.setServices(TestNotifier.class);
        listener = new L();
        watcher = Lookup.getDefault().lookup(Watcher.class);
        notify = Lookup.getDefault().lookup(TestNotifier.class);
        notify.start();
    }

    @Override
    protected void tearDown() throws Exception {
    }
    
    public void testSuspendRequests() throws Exception {
        FileObject root = FileUtil.toFileObject(getWorkDir());
        FileObject folder = root.createFolder("dir");
        File dir = FileUtil.toFile(folder);
        
        FileObject[] arr = folder.getChildren();
        folder.addFileChangeListener(listener);
        assertEquals("Empty ", 0, arr.length);
        
        final String prop = "org.netbeans.io.suspend".intern();
        synchronized (prop) {
            int prev = Integer.getInteger(prop, 0);
            System.setProperty(prop, "" + (prev + 1));
            prop.notifyAll();
        }
        
        new File(dir, "data.txt").createNewFile();
        notify.event.offer(dir.getPath());
        
        assertProperty("One path waiting", "1", "org.netbeans.io.pending");
        listener.assertEvents("No event yet", 0, 200);

        synchronized (prop) {
            int prev = Integer.getInteger(prop, 0);
            System.setProperty(prop, "" + (prev - 1));
            prop.notifyAll();
        }
        assertProperty("Pending status is cleared", null, "org.netbeans.io.pending");
        
        listener.assertEvents("One event delivered", 1, 5000);
        
        arr = folder.getChildren();
        assertEquals("One child", 1, arr.length);
        assertEquals("data.txt", arr[0].getNameExt());
    }

    private void assertProperty(String msg, String expVal, String propName) throws InterruptedException {
        String val = null;
        for (int i = 0; i < 100; i++) {
            val = System.getProperty(propName);
            if (expVal == null && val == null) {
                return;
            }
            if (expVal != null && expVal.equals(val)) {
                return;
            }
            Thread.sleep(100);
        }
        fail(msg + " exp: " + expVal + " was: " + val);
    }

    
    private static final class L extends FileChangeAdapter {
        private int cnt;

        @Override
        public synchronized void fileDataCreated(FileEvent fe) {
            cnt++;
        }

        @Override
        public void fileFolderCreated(FileEvent fe) {
            cnt++;
        }

        private synchronized void assertEvents(
            String msg, int cnt, int timeOut
        ) throws InterruptedException {
            if (this.cnt != cnt) {
                wait(timeOut);
            }
            assertEquals(msg, cnt, this.cnt);
        }
        
    }
    
    public static final class TestNotifier extends Notifier<Integer> {
        final List<String> registered = new LinkedList<String>();
        final BlockingQueue<String> event = new ArrayBlockingQueue<String>(10);

        @Override
        public Integer addWatch(String path) throws IOException {
            int size = registered.size();
            registered.add(path);
            return size;
        }

        @Override
        public void removeWatch(Integer key) throws IOException {
            registered.set(key, null);
        }

        @Override
        public String nextEvent() throws IOException, InterruptedException {
            return event.take();
        }

        @Override
        protected void start() throws IOException {
            registered.clear();
        }
    }
}
