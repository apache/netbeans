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

package org.openide.nodes;

import java.awt.event.ActionEvent;
import java.beans.*;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import javax.swing.AbstractAction;
import javax.swing.Action;
import junit.textui.TestRunner;
import org.netbeans.junit.Log;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.openide.util.Exceptions;
import org.openide.util.actions.SystemAction;

/** Checking some of the behaviour of Node (and AbstractNode).
 * @author Jaroslav Tulach, Jesse Glick
 */
public class NodeTest extends NbTestCase {

    public NodeTest(String name) {
        super(name);
    }
    
    public static void main(String[] args) {
        TestRunner.run(new NbTestSuite(NodeTest.class));
    }

    public void testGetActions () throws Exception {
        final SystemAction[] arr1 = {
            SystemAction.get (PropertiesAction.class)
        };
        final SystemAction[] arr2 = {
        };
        
        AbstractNode an = new AbstractNode (Children.LEAF) {
            @Override
            public SystemAction[] getActions () {
                return arr1;
            }
            
            @Override
            public SystemAction[] getContextActions () {
                return arr2;
            }
        };
        
        
        assertEquals ("getActions(false) properly delegates to getActions()", arr1, an.getActions (false));
        assertEquals ("getActions(true) properly delegates to getContextActions()", arr2, an.getActions (true));
        
    }
    
    public void testCanCallNodeSetChildrenFromReadAccess() throws Exception {
        CharSequence log = Log.enable("global`", Level.WARNING);
        class Mn extends AbstractNode implements Runnable {
            public Mn() {
                super(Children.LEAF);
            }

            public void run() {
                setChildren(new Children.Array());
            }
        }
        Mn mn = new Mn();
        
        Children.MUTEX.readAccess(mn);
        
        assertEquals("Log is empty", "", log.toString());
    }

    public void testCanCreateNodeWithoutChildrenMutex() {
        final CountDownLatch l1 = new CountDownLatch(1);
        final CountDownLatch l2 = new CountDownLatch(1);

        Thread t1 = new Thread() {

            @Override
            public void run() {
                if (!Children.MUTEX.isWriteAccess()){
                    Children.MUTEX.writeAccess(this);
                    return;
                }
                try {
                    l2.countDown();
                    l1.await();
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        };
        Thread t2 = new Thread() {

            @Override
            public void run() {
                try {
                    l2.await();
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
                Node n = new AbstractNode(new Children.Array());
                l1.countDown();
            }
        };

        t1.setDaemon(true);
        t2.setDaemon(true);
        t1.start();
        t2.start();
        for (int i = 0; i < 1000; i++) {
            try {
                t1.join(10);
                t2.join(10);
                if (!t1.isAlive() && !t2.isAlive()) {
                    return;
                }
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        fail("It seems Node creation requires Children.MUTEX");
    }

    public void testPreferredAction() throws Exception {
        final SystemAction a1 = SystemAction.get(PropertiesAction.class);
        final Action a2 = new AbstractAction() {
            public void actionPerformed(ActionEvent ev) {}
        };
        final SystemAction a3 = SystemAction.get(OpenAction.class);
        final Action a4 = new AbstractAction() {
            public void actionPerformed(ActionEvent ev) {}
        };
        // Old code:
        Node n1 = new AbstractNode(Children.LEAF) {
            {
                setDefaultAction(a1);
            }
        };
        Node n2 = new AbstractNode(Children.LEAF) {
            @Override
            public SystemAction getDefaultAction() {
                return a1;
            }
        };
        // New code:
        Node n4 = new AbstractNode(Children.LEAF) {
            @Override
            public Action getPreferredAction() {
                return a1;
            }
        };
        // emulation of DataNode
        Node n5 = new AbstractNode(Children.LEAF) {
            {
                setDefaultAction (a1);
            }
            
            @Override
            public SystemAction getDefaultAction () {
                return super.getDefaultAction ();
            }
        };
        Node n6 = new AbstractNode(Children.LEAF) {
            @Override
            public Action getPreferredAction() {
                return a2;
            }
        };
        // Wacko code:
        Node n7 = new AbstractNode(Children.LEAF) {
            {
                setDefaultAction(a1);
            }
            @Override
            public SystemAction getDefaultAction() {
                return a3;
            }
        };
        assertEquals(a1, n1.getDefaultAction());
        assertEquals(a1, n1.getPreferredAction());
        assertEquals(a1, n2.getDefaultAction());
        assertEquals(a1, n2.getPreferredAction());
        assertEquals(a1, n4.getDefaultAction());
        assertEquals(a1, n4.getPreferredAction());
        assertEquals(a1, n5.getPreferredAction());
        assertEquals(a1, n5.getDefaultAction());
        assertEquals(null, n6.getDefaultAction());
        assertEquals(a2, n6.getPreferredAction());
        assertEquals(a3, n7.getDefaultAction());
        assertEquals(a3, n7.getPreferredAction());
    }

    public void testShortDescriptionCanBeSetToNull () {
        class PCL extends NodeAdapter {
            public int cnt;
            
            @Override
            public void propertyChange (PropertyChangeEvent ev) {
                if (Node.PROP_SHORT_DESCRIPTION.equals (ev.getPropertyName ())) {
                    cnt++;
                }
            }
        }
        
        AbstractNode an = new AbstractNode (Children.LEAF);
        an.setDisplayName ("My name");
        
        PCL pcl = new PCL ();
        an.addNodeListener (pcl);
        assertEquals ("My name", an.getShortDescription ());
        
        an.setShortDescription ("Ahoj");
        assertEquals ("Ahoj", an.getShortDescription ());
        assertEquals ("One change", 1, pcl.cnt);
        
        an.setShortDescription (null);
        assertEquals ("My name", an.getShortDescription ());
        assertEquals ("Second change", 2, pcl.cnt);
    }
    
    /** Another sample action */
    public static final class PropertiesAction extends OpenAction {
        
    }
}
