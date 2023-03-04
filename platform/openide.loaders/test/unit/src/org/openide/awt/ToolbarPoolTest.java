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

package org.openide.awt;

import java.awt.Component;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.logging.Level;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.openide.awt.MenuBarTest.MyAction;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.InstanceDataObject;

/** Mostly to test the correct behaviour of AWTTask.waitFinished.
 *
 * @author Jaroslav Tulach
 */
public class ToolbarPoolTest extends NbTestCase {
    FileObject toolbars;
    DataFolder toolbarsFolder;
    
    public ToolbarPoolTest (String testName) {
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

    protected void setUp() throws Exception {
        FileObject root = FileUtil.getConfigRoot();
        toolbars = FileUtil.createFolder (root, "Toolbars");
        toolbarsFolder = DataFolder.findFolder (toolbars);
        FileObject[] arr = toolbars.getChildren ();
        for (int i = 0; i < arr.length; i++) {
            arr[i].delete ();
        }
        
        ToolbarPool tp = ToolbarPool.getDefault ();
    }

    public void testGetConf () throws Exception {
        ToolbarPool tp = ToolbarPool.getDefault ();
        
        String conf = tp.getConfiguration ();
        assertEquals ("By default there is no config", "", conf);
        
    }
    
    public void testCreateConf () throws Exception {
        JLabel conf = new JLabel ();
        conf.setName ("testCreateConf");
        
        conf = (JLabel)writeInstance (toolbars, "conf1.ser", conf);
        
        ToolbarPool tp = ToolbarPool.getDefault ();
        String[] myConfs = tp.getConfigurations ();
        assertEquals ("One", 1, myConfs.length);
        assertEquals ("By default there is the one", "testCreateConf", myConfs[0]);
        
    }

    public void testCreateFolderTlbs () throws Exception {
        FileUtil.createFolder (toolbars, "tlb2");
        
        ToolbarPool tp = ToolbarPool.getDefault ();
        Toolbar[] myTlbs = tp.getToolbars ();
        assertEquals ("One", 1, myTlbs.length);
        assertEquals ("By default there is the one", "tlb2", myTlbs[0].getName ());
        
    }

    @RandomlyFails // NB-Core-Build #1337
    public void testWaitsForToolbars () throws Exception {
        FileObject tlb = FileUtil.createFolder (toolbars, "tlbx");
        DataFolder f = DataFolder.findFolder (tlb);
        InstanceDataObject.create (f, "test1", JLabel.class);
        
        ToolbarPool tp = ToolbarPool.getDefault ();
        Toolbar[] myTlbs = tp.getToolbars ();
        assertEquals ("One", 1, myTlbs.length);
        assertEquals ("By default there is the one", "tlbx", myTlbs[0].getName ());
        
        assertLabels ("One subcomponent", 1, myTlbs[0]);
        
        InstanceDataObject.create (f, "test2", JLabel.class);
        
        tp.waitFinished ();
        
        assertLabels ("Now there are two", 2, myTlbs[0]);
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
