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

import java.awt.EventQueue;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStatusEvent;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MultiFileSystem;
import org.openide.filesystems.StatusDecorator;
import org.openide.loaders.DataFolder;
import org.openide.loaders.InstanceDataObject;
import org.openide.nodes.Node;
import org.openide.nodes.NodeEvent;
import org.openide.nodes.NodeListener;
import org.openide.nodes.NodeMemberEvent;
import org.openide.nodes.NodeReorderEvent;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.Utilities;

/**
 *
 * Simulates deadlock as in issue 163201. Folder Instance Processor is blocked
 * and AWT needlessly waits for it to finish.
 *
 * 
    Thread AWT-EventQueue-0
        at java.lang.Object.wait(Object.java:-2)
        at org.openide.util.Task.waitFinished(Task.java:158)
        at org.openide.util.RequestProcessor$Task.waitFinished(RequestProcessor.java:799)
        at org.openide.util.Task.waitFinished(Task.java:192)
        at org.openide.loaders.FolderInstance.waitFinished(FolderInstance.java:339)
        at org.openide.awt.MenuBar$LazyMenu$MenuFolder.waitFinishedSuper(MenuBar.java:623)
        at org.openide.awt.MenuBar$LazyMenu.doInitialize(MenuBar.java:581)
        at org.openide.awt.MenuBar$LazyMenu.stateChanged(MenuBar.java:555)
        at javax.swing.DefaultButtonModel.fireStateChanged(DefaultButtonModel.java:333)
        at javax.swing.DefaultButtonModel.setMnemonic(DefaultButtonModel.java:274)
        at javax.swing.AbstractButton.setMnemonic(AbstractButton.java:1548)
        at org.openide.awt.Mnemonics.setMnemonic(Mnemonics.java:279)
        at org.openide.awt.Mnemonics.setLocalizedText2(Mnemonics.java:84)
        at org.openide.awt.Mnemonics.setLocalizedText(Mnemonics.java:137)
        at org.openide.awt.MenuBar$LazyMenu.updateProps(MenuBar.java:512)
        at org.openide.awt.MenuBar$LazyMenu.run(MenuBar.java:524)
        at java.awt.event.InvocationEvent.dispatch(InvocationEvent.java:209)
        at java.awt.EventQueue.dispatchEvent(EventQueue.java:597)
        at java.awt.EventDispatchThread.pumpOneEventForFilters(EventDispatchThread.java:273)
        at java.awt.EventDispatchThread.pumpEventsForFilter(EventDispatchThread.java:183)
        at java.awt.EventDispatchThread.pumpEventsForHierarchy(EventDispatchThread.java:173)
        at java.awt.EventDispatchThread.pumpEvents(EventDispatchThread.java:168)
        at java.awt.EventDispatchThread.pumpEvents(EventDispatchThread.java:160)
        at java.awt.EventDispatchThread.run(EventDispatchThread.java:121)
  Thread Folder Instance Processor
        at java.lang.Object.wait(Object.java:-2)
        at java.lang.Object.wait(Object.java:485)
        at org.openide.awt.MenuBarDeadlock163201Test$BlockingAction.<init>(MenuBarDeadlock163201Test.java:209)
        at java.lang.reflect.Constructor.newInstance(Constructor.java:513)
        at java.lang.Class.newInstance0(Class.java:355)
        at java.lang.Class.newInstance(Class.java:308)
        at org.openide.loaders.InstanceSupport.instanceCreate(InstanceSupport.java:217)
        at org.openide.loaders.InstanceDataObject$Ser.instanceCreate(InstanceDataObject.java:1298)
        at org.openide.loaders.InstanceDataObject.instanceCreate(InstanceDataObject.java:760)
        at org.openide.loaders.FolderInstance.instanceForCookie(FolderInstance.java:572)
        at org.openide.loaders.FolderInstance$HoldInstance.instanceCreate(FolderInstance.java:1122)
        at org.openide.loaders.FolderInstance$1R.instances(FolderInstance.java:692)
        at org.openide.loaders.FolderInstance$1R.run(FolderInstance.java:713)
        at org.openide.util.RequestProcessor$Task.run(RequestProcessor.java:573)
        at org.openide.util.RequestProcessor$Processor.run(RequestProcessor.java:1005)
 *
 * @author Jaroslav Tulach
 */
public class MenuBarDeadlock163201Test extends NbTestCase {
    private DataFolder df;
    private MenuBar mb;
    private DataFolder df2;
    private MFS mfs;
    private Logger LOG;

    public MenuBarDeadlock163201Test(String testName) {
        super(testName);
    }

    @Override
    protected Level logLevel() {
        return Level.INFO;
    }

    @Override
    protected boolean runInEQ() {
        return false;
    }

    @Override
    protected int timeOut() {
        return 15000;
    }

    @Override
    protected void setUp() throws Exception {
        LOG = Logger.getLogger("test." + getName());
        mfs = new MFS();
        FileObject fo = FileUtil.createFolder(
            mfs.getRoot(),
            "Folder" + getName() + "/Old"
        );
        FileObject fo2 = FileUtil.createFolder(
            mfs.getRoot(),
            "Folder2" + getName() + "/Old"
        );
        df = DataFolder.findFolder(fo.getParent());
        df2 = DataFolder.findFolder(fo2.getParent());
        mb = new MenuBar(df);
        mb.waitFinished();
        assertEquals("One submenu", 1, mb.getMenuCount());
        /* actually "":
        assertEquals("Named Old", "Old", mb.getMenu(0).getText());
        */
    }

    @RandomlyFails // NB-Core-Build #2723
    public void testChangeInNameOfFolderDoesNotDeadlock() throws Throwable {
        class R implements Runnable {
            MenuBar mb2;
            Throwable t;
            private DataFolder ch;

            public void run() {
                try {
                    FileUtil.createData(df.getPrimaryFile(), "some.change");

                    ch = (DataFolder)df2.getChildren()[0];
                    InstanceDataObject.create(ch, null, BlockingAction.class);
                    mb2 = new MenuBar(df2);
                    mb2.waitFinished();
                    assertEquals("One menu", 1, mb2.getMenuCount());
                    // MenuBar.LazyMenu
                    if (Utilities.isMac()) {
                        ChangeListener l = (ChangeListener) mb2.getMenu(0);
                        l.stateChanged(new ChangeEvent(this));
                    } else {
                        mb2.getMenu(0).setPopupMenuVisible(true);
                    }
                    assertEquals("One action", 1, mb2.getMenu(0).getMenuComponentCount());
                } catch (Throwable ex) {
                    this.t = ex;
                }
            }
        }

        R run = new R();
        Task t = RequestProcessor.getDefault().post(run);
        t.waitFinished(1000);
        assertTrue("Blocking action created", BlockingAction.created);
        if (run.t != null) {
            throw run.t;
        }

        class Rename implements Runnable, NodeListener {
            private boolean ok;
            public void run() {
                mfs.startMorph();
            }

            public void childrenAdded(NodeMemberEvent ev) {
            }

            public void childrenRemoved(NodeMemberEvent ev) {
            }

            public void childrenReordered(NodeReorderEvent ev) {
            }

            public void nodeDestroyed(NodeEvent ev) {
            }

            public synchronized void propertyChange(PropertyChangeEvent evt) {
                if (Node.PROP_DISPLAY_NAME.equals(evt.getPropertyName())) {
                    ok = true;
                    LOG.info("Property change");
                    notifyAll();
                }
            }

            public synchronized void waitOK() throws InterruptedException {
                while (!ok) {
                    LOG.info("waiting for node name");
                    wait();
                }
                LOG.info("node name is OK");
            }
        }
        Node node = df.getChildren()[0].getNodeDelegate();
        Rename name = new Rename();
        node.addNodeListener(name);
        EventQueue.invokeAndWait(name);
        name.waitOK();
        assertEquals("Node name changed", "New", node.getDisplayName());

        for (int i = 0; i < 15; i++) {
            LOG.info("checking round " + i);
            EventQueue.invokeAndWait(new Runnable() {
                public void run() {
                    LOG.info("invokeAndWait");
                }
            });
            if (mb.getMenuCount() != 1) {
                LOG.log(Level.INFO, "wrong count {0}", mb.getMenuCount());
                Thread.sleep(100);
                continue;
            }
            if (!mb.getMenu(0).getText().equals("New")) {
                LOG.log(Level.INFO, "Wrong name {0}", mb.getMenu(0).getText());
                Thread.sleep(100);
                continue;
            }
            break;
        }

        assertEquals("One submenu", 1, mb.getMenuCount());
        assertEquals("Named New", "New", mb.getMenu(0).getText());
    }

    public static final class BlockingAction extends AbstractAction {
        private static boolean created;
        public BlockingAction() {
            created = true;
            synchronized (this) {
                for (;;) {
                    try {
                        wait();
                    } catch (InterruptedException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        }

        public void actionPerformed(ActionEvent e) {
        }
    }

    private static final class MFS extends MultiFileSystem implements StatusDecorator {
        private boolean morph;

        public MFS() {
            setDelegates(FileUtil.createMemoryFileSystem());
        }


        public String annotateName(String name, Set<? extends FileObject> files) {
            if (morph && name.equals("Old")) {
                return "New";
            }
            return name;
        }

        @Override
        public String annotateNameHtml(String name, Set<? extends FileObject> files) {
            return null;
        }

        public void startMorph() {
            morph = true;
            fireFileStatusChanged(new FileStatusEvent(this, false, true));
        }

        @Override
        public StatusDecorator getDecorator() {
            return this;
        }
    }
}
