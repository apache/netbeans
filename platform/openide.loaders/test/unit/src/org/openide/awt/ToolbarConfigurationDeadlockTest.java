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
import java.util.Arrays;
import java.util.logging.Level;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.InstanceDataObject;

/** Simulates:

"AWT-EventQueue-1" prio=10 tid=0xb1bc1c00 nid=0x515 in Object.wait() [0xb1405000]
 * 
at java.lang.Thread.State: TIMED_WAITING (on object monitor)
at java.lang.Object.wait(Native Method)
- waiting on <0x67390108> (a org.openide.util.RequestProcessor$Task)
at org.openide.util.Task.waitFinished(Task.java:161)
- locked <0x67390108> (a org.openide.util.RequestProcessor$Task)
at org.openide.util.RequestProcessor$Task.waitFinished(RequestProcessor.java:1722)
at org.openide.util.Task.waitFinished(Task.java:195)
at org.openide.loaders.FolderInstance.waitFinished(FolderInstance.java:342)
at org.openide.awt.ToolbarPool.waitFinished(ToolbarPool.java:197)
at org.openide.awt.ToolbarPool.getToolbars(ToolbarPool.java:337)
at org.netbeans.core.windows.view.ui.toolbars.ToolbarConfiguration.refresh(ToolbarConfiguration.java:253)
at org.netbeans.core.windows.view.ui.toolbars.ToolbarConfiguration.activate(ToolbarConfiguration.java:374)
at org.openide.awt.ToolbarPool.activate(ToolbarPool.java:235)
- locked <0x71cb8130> (a org.openide.awt.ToolbarPool)
at org.openide.awt.ToolbarPool.setConfiguration(ToolbarPool.java:309)
at org.openide.awt.ToolbarPool$TPTaskListener.taskFinished(ToolbarPool.java:390)
at org.openide.util.Task.notifyFinished(Task.java:231)
at org.openide.loaders.FolderInstance.defaultProcessObjectsFinal(FolderInstance.java:893)
at org.openide.loaders.FolderInstance$1R.run(FolderInstance.java:738)
at org.openide.util.Task.run(Task.java:248)
at org.netbeans.modules.openide.loaders.AWTTask.run(AWTTask.java:73)
at org.netbeans.modules.openide.loaders.AWTTask$Processor.run(AWTTask.java:114)
at java.awt.event.InvocationEvent.dispatch(InvocationEvent.java:226)
at java.awt.EventQueue.dispatchEvent(EventQueue.java:602)
at org.netbeans.core.TimableEventQueue.dispatchEvent(TimableEventQueue.java:137)
at java.awt.EventDispatchThread.pumpOneEventForFilters(EventDispatchThread.java:275)
at java.awt.EventDispatchThread.pumpEventsForFilter(EventDispatchThread.java:200)
at java.awt.EventDispatchThread.pumpEventsForHierarchy(EventDispatchThread.java:190)
at java.awt.EventDispatchThread.pumpEvents(EventDispatchThread.java:185)
at java.awt.EventDispatchThread.pumpEvents(EventDispatchThread.java:177)
at java.awt.EventDispatchThread.run(EventDispatchThread.java:138)

 */
public class ToolbarConfigurationDeadlockTest extends NbTestCase {
    FileObject toolbars;
    DataFolder toolbarsFolder;
    
    public ToolbarConfigurationDeadlockTest (String testName) {
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
        toolbars.createFolder("Tool1");
        toolbarsFolder = DataFolder.findFolder (toolbars);
        InstanceDataObject.create(toolbarsFolder, null, MyConf.class);
    }

    public void testConfigurationActivated () throws Exception {
        ToolbarPool tp = ToolbarPool.getDefault ();
        tp.setConfiguration("Test");
        tp.waitFinished ();
        
        assertEquals("One configuration activated", 1, MyConf.activate);
    }

    public static final class MyConf implements ToolbarPool.Configuration {
        static int activate;
        
        
        private JPanel panel = new JPanel();
        
        @Override
        public Component activate() {
            activate++;
            Toolbar[] arr = ToolbarPool.getDefault().getToolbars();
            assertEquals("One array: " + Arrays.toString(arr), 1, arr.length);
            assertEquals("Name is right", "Tool1", arr[0].getName());
            return panel;
        }

        @Override
        public String getName() {
            return "Test";
        }

        @Override
        public JPopupMenu getContextMenu() {
            return new JPopupMenu();
        }
        
    }
}
