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
import java.util.*;
import javax.swing.AbstractAction;
import javax.swing.Action;

import junit.textui.TestRunner;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

import org.openide.nodes.*;
import org.openide.util.actions.SystemAction;

/** Checking some of the behaviour of Node.
 * @author Jaroslav Tulach
 */
public class NodeOpTest extends NbTestCase {

    public NodeOpTest(String name) {
        super(name);
    }

    public static void main(String[] args) {
        TestRunner.run(new NbTestSuite(NodeOpTest.class));
    }


    private static final class A extends AbstractAction {
        public void actionPerformed(ActionEvent ev) {
        }
    }
    
    public void testFindActions () throws Exception {
        class N extends AbstractNode {
            private Action[] arr;
            
            N (Action[] arr) {
                super (org.openide.nodes.Children.LEAF);
                this.arr = arr;
            }
            
            public Action[] getActions (boolean f) {
                return arr;
            }
        }
        
        Action[] arr = { new A(), new A(), new A(), new A() };
        
        assertArray (
            "Finding actions for one node is simple",
            arr, 
            NodeOp.findActions(new Node[] { new N (arr) })
        );

        assertArray (
            "Finding actions for two nodes with same actions",
            arr, 
            NodeOp.findActions(new Node[] { new N (arr), new N (arr) })
        );
            
        assertArray (
            "Finding actions for three nodes with same actions",
            arr, 
            NodeOp.findActions(new Node[] { new N (arr), new N (arr), new N (arr) })
        );
          
            
        assertArray (
            "Otherwise only common actions are taken",
            new Action[] { arr[3] }, 
            NodeOp.findActions(new Node[] { new N (arr), new N (new Action[] { arr[3], null }) })
        );
            
    }
    
    /**
     * Test that it is OK to return a different list each time.
     * There was a bug in NodeOp preventing this.
     */
    public void testFindActions2() throws Exception {
        class N extends AbstractNode {
            private Action a1 = new A();
            private Action a3 = new A();
            N() {
                super (org.openide.nodes.Children.LEAF);
            }
            public Action[] getActions(boolean f) {
                return new Action[] {
                    a1,
                    new A(),
                    a3,
                };
            }
        }
        Action[] actions = NodeOp.findActions(new Node[] {new N()});
        assertEquals("NodeOp.findActions does not gratuitously remove nonconstant actions", 3, actions.length);
    }
    
    private static void assertArray (String msg, Object[] a1, Object[] a2) {
        assertEquals(msg, Arrays.asList(a1), Arrays.asList(a2));
    }
}
