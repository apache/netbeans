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

package org.netbeans.modules.editor;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.*;
import javax.swing.JEditorPane;
import javax.swing.text.*;
import org.openide.cookies.*;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.windows.*;

/**
 * Test benchmarking code completion in editor. It must be executed using
 * internal execution.
 *
 * @author  Petr Kuzel
 */
public class RobotTest {

    /** Creates a new instance of RobotTest */
    public RobotTest() {
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {

        Robot robot = new Robot();
        
        Repository repo = Repository.getDefault();
        FileObject fo = repo.findResource("org/netbeans/modules/editor/data/Robot.java");
        
        DataObject dobj = DataObject.find(fo);
        EditorCookie editor = (EditorCookie) dobj.getCookie(EditorCookie.class);
        TopComponent.Registry reg = WindowManager.getDefault().getRegistry();        
        WaitPCL waitPCL = new WaitPCL();        
        reg.addPropertyChangeListener(waitPCL);
        try {        
            editor.openDocument();
            editor.open();
            waitPCL.waitUntilOpened().requestFocus();
            robot.waitForIdle();  // try to eliminate null panes bellow
        } finally {
            reg.removePropertyChangeListener(waitPCL);
        }
                
        Document doc = editor.openDocument();
        JEditorPane[] panes = editor.getOpenedPanes();
        if (panes == null) throw new IllegalStateException("Null panes " + editor.getClass());
        if (panes.length == 0) throw new IllegalStateException("No pane " + editor.getClass());
        String string = doc.getText(0, doc.getLength()-1);
        int dot = string.indexOf("//java.awt.Robot");
        if (dot < 0) throw new IllegalStateException("Mark not found: " + string);
        Caret caret = panes[0].getCaret();
        caret.setDot(dot);  // CCE
        
        System.gc();
        robot.waitForIdle();        
        
        long start = System.currentTimeMillis();

        robot.keyPress(KeyEvent.VK_SHIFT);
        robot.keyPress(KeyEvent.VK_S);
        robot.keyRelease(KeyEvent.VK_S);
        robot.keyRelease(KeyEvent.VK_SHIFT);
        robot.delay(100);
        robot.keyPress(KeyEvent.VK_Y);
        robot.delay(100);
        robot.keyPress(KeyEvent.VK_S);
        robot.delay(100);
        robot.keyPress(KeyEvent.VK_T);
        robot.delay(100);
        robot.keyPress(KeyEvent.VK_E);
        robot.delay(100);
        robot.keyPress(KeyEvent.VK_M);
        robot.delay(100);
        robot.keyPress(KeyEvent.VK_DECIMAL);
        robot.delay(100);
        robot.keyPress(KeyEvent.VK_G);
        robot.delay(100);
        robot.keyPress(KeyEvent.VK_CONTROL);
        robot.keyPress(KeyEvent.VK_SPACE);
        robot.keyRelease(KeyEvent.VK_SPACE);
        robot.keyRelease(KeyEvent.VK_CONTROL);
        
        robot.waitForIdle();
        
        AWTMonitor monitor = new AWTMonitor();
        
        try {
            Toolkit.getDefaultToolkit().addAWTEventListener(monitor, AWTEvent.PAINT_EVENT_MASK);
            robot.delay(2000);            
        } finally {
            Toolkit.getDefaultToolkit().removeAWTEventListener(monitor);
        }
        
        long end = monitor.getLastTime();
        
        
        System.out.println("Total time " + (end - start) + "ms");
    }
    
    private static class AWTMonitor implements AWTEventListener {
        
        long last = System.currentTimeMillis();
        
        public synchronized void eventDispatched(AWTEvent e) {
            System.err.println("AWT event " + e);
            last = System.currentTimeMillis();
        }
        
        public synchronized long getLastTime() {
            return last;
        }
        
    }
    
    private static class WaitPCL implements  PropertyChangeListener {
            
        private TopComponent opened = null;
        
        public WaitPCL() {
        }
        
        public void propertyChange(PropertyChangeEvent e) {
            if (TopComponent.Registry.PROP_OPENED.equals(e.getPropertyName())) {
                TopComponent.Registry reg = WindowManager.getDefault().getRegistry();
                Set opened = new HashSet((Set)e.getNewValue());
                opened.removeAll((Set)e.getOldValue());
                notifyOpened((TopComponent)opened.iterator().next());
            }
        }

        private synchronized void notifyOpened(TopComponent comp) {
            opened = comp;
            notify();
        }

        public synchronized TopComponent waitUntilOpened() throws InterruptedException{
            while (null == opened) wait();
            return opened;
        }
    }
    
}
