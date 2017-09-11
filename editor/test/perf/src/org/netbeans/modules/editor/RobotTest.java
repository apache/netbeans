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
