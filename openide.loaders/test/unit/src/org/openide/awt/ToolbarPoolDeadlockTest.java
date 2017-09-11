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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.openide.awt;

import java.awt.Component;
import java.awt.EventQueue;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.logging.Level;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import org.netbeans.junit.NbTestCase;
import org.openide.awt.MenuBarTest.MyAction;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.InstanceDataObject;
import org.openide.util.Exceptions;

/** Mostly to test the correct behaviour of AWTTask.waitFinished.
 *
 * @author Jaroslav Tulach
 */
public class ToolbarPoolDeadlockTest extends NbTestCase {
    FileObject toolbars;
    DataFolder toolbarsFolder;
    
    public ToolbarPoolDeadlockTest (String testName) {
        super (testName);
    }

    @Override
    protected int timeOut() {
        return 30000;
    }
    
    @Override
    protected Level logLevel() {
        return Level.FINE;
    }

    @Override
    protected void setUp() throws Exception {
        FileObject root = FileUtil.getConfigRoot();
        toolbars = FileUtil.createFolder (root, "Toolbars");
        toolbarsFolder = DataFolder.findFolder (toolbars);
        FileObject[] arr = toolbars.getChildren ();
        for (int i = 0; i < arr.length; i++) {
            arr[i].delete ();
        }
        
        ToolbarPool tp = ToolbarPool.getDefault ();
        tp.waitFinished ();
    }

    public void testWaitsForToolbars () throws Exception {
        assertFalse("Not in AWT thread", EventQueue.isDispatchThread());
        class Block implements Runnable {
            int cnt;
            Toolbar[] myTlbs;
            public void run() {
                if (cnt++ > 0) {
                    return;
                }
                init();
            }
            private synchronized void init() {
                try {
                    notifyAll();
                    wait();
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }

                ToolbarPool.getDefault().waitFinished();
                myTlbs = ToolbarPool.getDefault().getToolbars ();
            }

            public synchronized void waitAWTBlocked() throws InterruptedException {
                EventQueue.invokeLater(this);
                wait();
            }

            public void finish() throws Exception {
                synchronized (this) {
                    notifyAll();
                }
                Thread.sleep(200);
                EventQueue.invokeAndWait(this);
                assertNotNull("My toolbars has been obtained", myTlbs);
            }
        }
        Block block = new Block();
        block.waitAWTBlocked();

        FileObject tlb = FileUtil.createFolder (toolbars, "tlbx");
        DataFolder f = DataFolder.findFolder (tlb);
        InstanceDataObject.create (f, "test1", JLabel.class);

        block.finish();

        assertEquals ("One", 1, block.myTlbs.length);
        assertEquals ("By default there is the one", "tlbx", block.myTlbs[0].getName ());
        assertLabels ("One subcomponent", 1, block.myTlbs[0]);
    }

    public void testWhoCreatesConstructor() throws Exception {
        FileObject root = FileUtil.getConfigRoot();
        FileObject fo = FileUtil.createFolder (root, "ToolbarsWhoCreates");
        final DataFolder df = DataFolder.findFolder(fo);
        ToolbarPool pool = new ToolbarPool(df);

        assertEquals("No children now", 0, pool.getToolbars().length);

        class Atom implements FileSystem.AtomicAction {

            FileObject m1, m2;

            public void run() throws IOException {
                m1 = FileUtil.createFolder(df.getPrimaryFile(), "m1");
                DataFolder f1 = DataFolder.findFolder(m1);
                InstanceDataObject.create(f1, "X", MyAction.class);
            }
        }
        Atom atom = new Atom();
        df.getPrimaryFile().getFileSystem().runAtomicAction(atom);
        pool.waitFinished();

        assertEquals("One toolbar is there", 1, pool.getToolbars().length);
        Toolbar tb = pool.getToolbars()[0];
        assertEquals("Pool name", "m1", tb.getName());
        assertEquals("Has one subcomponent", 1, tb.getComponents().length);
        Object o1 = tb.getComponent(0);
        if (!(o1 instanceof JButton)) {
            fail("Need JPanel " + o1);
        }
        assertEquals("And now the action is created", 1, MyAction.counter);
    }
    
    private static Object writeInstance (final FileObject folder, final String name, final Object inst) throws IOException {
        class W implements FileSystem.AtomicAction {
            public Object create;
            
            public void run () throws IOException {
                FileObject fo = FileUtil.createData (folder, name);
                FileLock lock = fo.lock ();
                ObjectOutputStream oos = new ObjectOutputStream (fo.getOutputStream (lock));
                oos.writeObject (inst);
                oos.close ();
                lock.releaseLock ();
                
                DataObject obj = DataObject.find (fo);
                InstanceCookie ic = obj.getCookie(InstanceCookie.class);
                
                assertNotNull ("Cookie created", ic);
                try {
                    create = ic.instanceCreate ();
                    assertEquals ("The same instance class", inst.getClass(), create.getClass ());
                } catch (ClassNotFoundException ex) {
                    fail (ex.getMessage ());
                }
            }
        }
        W w = new W ();
        folder.getFileSystem ().runAtomicAction (w);
        return w.create;
    }
    
    private static void assertLabels (String msg, int cnt, Component c) {
        int real = countLabels (c);
        assertEquals (msg, cnt, real);
    }
    
    private static int countLabels (Component c) {
        if (c instanceof JLabel) return 1;
        if (! (c instanceof JComponent)) return 0;
        int cnt = 0;
        Component[] arr = ((JComponent)c).getComponents ();
        for (int i = 0; i < arr.length; i++) {
            cnt += countLabels (arr[i]);
        }
        return cnt;
    }
}
