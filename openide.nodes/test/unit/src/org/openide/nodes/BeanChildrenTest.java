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

import junit.framework.*;
import junit.textui.TestRunner;
import java.beans.*;
import java.beans.beancontext.*;
import java.util.*;
import org.openide.util.Mutex;

import org.netbeans.junit.*;

/** Test updating of bean children in proper circumstances, e.g.
 * deleting nodes or beans.
 * @author Jesse Glick
 */
public class BeanChildrenTest extends NbTestCase {

    public BeanChildrenTest(String name) {
        super(name);
    }

    public static void main(String[] args) {
        TestRunner.run(new NbTestSuite(BeanChildrenTest.class));
    }
    
    @SuppressWarnings("unchecked")
    private static BeanContext makeContext() {
        BeanContext bc = new BeanContextSupport();
        bc.add("one");
        bc.add("two");
        bc.add("three");
        return bc;
    }
    
    private static String[] nodes2Names(Node[] nodes) {
        String[] names = new String[nodes.length];
        for (int i = 0; i < nodes.length; i++) {
            names[i] = nodes[i].getName();
        }
        return names;
    }
    
    public void testNodesAreCorrect() throws Exception {
        BeanContext bc = makeContext();
        Children c = new BeanChildren(bc, new SimpleFactory());
        // Note that BeanContextSupport keeps a HashMap of children
        // so the order is not deterministic.
        assertEquals("correct subnodes",
            new HashSet<String>(Arrays.asList(new String[] {"one", "two", "three"})),
            new HashSet<String>(Arrays.asList(nodes2Names(c.getNodes()))));
    }
    
    public void testRemoveBeanRemovesChild() throws Exception {
        BeanContext bc = makeContext();
        final Children c = new BeanChildren(bc, new SimpleFactory());
        bc.remove("two");
        assertEquals("correct beans",
            new HashSet<String>(Arrays.asList(new String[] {"one", "three"})),
            new HashSet<Object>(Arrays.asList(bc.toArray())));
        // Make sure we let the children thread run to completion.
        // Check the result in the reader.
        // First make sure it is initialized. Otherwise Children.Keys.getNodes
        // from within the mutex immediately returns no nodes, then when
        // next asked has them all. Checking outside the mutex seems to block
        // until the nodes have been initialized.
        Node[] nodes = c.getNodes(true);
        nodes = Children.MUTEX.readAccess(new Mutex.Action<Node[]>() {
            public Node[] run() {
                return c.getNodes();
            }
        });
        assertEquals("correct subnodes",
            new HashSet<String>(Arrays.asList(new String[] {"one", "three"})),
            new HashSet<String>(Arrays.asList(nodes2Names(nodes))));
    }
    
    // Cf. #7925.
    public void testDeleteChildRemovesBean() throws Exception {
        BeanContext bc = makeContext();
        Children c = new BeanChildren(bc, new SimpleFactory());
        Node n = c.findChild("two");
        assertNotNull(n);
        assertEquals("two", n.getName());
        n.destroy();
        // Wait for changes, maybe:
        Children.MUTEX.readAccess(new Mutex.Action<Void>() {
            public Void run() {
                return null;
            }
        });
        assertEquals("correct beans",
            new HashSet<String>(Arrays.asList(new String[] {"one", "three"})),
            new HashSet<Object>(Arrays.asList(bc.toArray())));
    }
    
    private static final class SimpleFactory implements BeanChildren.Factory {
        public Node createNode(Object bean) throws IntrospectionException {
            Node n = new AbstractNode(Children.LEAF);
            n.setName((String)bean);
            return n;
        }
    }
    
}
