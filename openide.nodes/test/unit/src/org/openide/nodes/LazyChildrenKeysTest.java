/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.openide.nodes;

import java.util.List;
import org.netbeans.junit.NbTestCase;
import org.openide.nodes.Children.Keys;

/**
 *
 * @author Tomas Holy
 */
public class LazyChildrenKeysTest extends NbTestCase {
    
    public LazyChildrenKeysTest(String testName) {
        super(testName);
    }

    public void testFFNWithEmptyEntries() {
        LazyKeys keys = new LazyKeys();
        keys.keys("a", "-b");
        FilterNode fn = new FilterNode(new FilterNode(new AbstractNode(keys)));
        Node[] nodes = fn.getChildren().getNodes(true);
        assertEquals(1, nodes.length);
        assertEquals("a", nodes[0].getName());
    }

    public void testCreateNodesIsNotCalledForDummyNode() {
        class FCh extends FilterNode.Children {

            public FCh(Node or) {
                super(or);
            }

            @Override
            protected Node[] createNodes(Node key) {
                if (EntrySupportLazy.isDummyNode(key)) {
                    fail("Should not call createNodes() for DummyNode");
                }
                return super.createNodes(key);
            }
        }

        LazyKeys keys = new LazyKeys();
        keys.keys("a", "-b", "b");
        Node or = new AbstractNode(keys);
        FilterNode fn = new FilterNode(or, new FCh(or));
        fn.getChildren().getNodesCount();
        List<Node> snapshot = fn.getChildren().snapshot();
        assertEquals(3, snapshot.size());
        assertEquals("a", snapshot.get(0).getName());
        assertEquals("", snapshot.get(1).getName());
        assertEquals("b", snapshot.get(2).getName());

        Node[] nodes = fn.getChildren().getNodes();
        assertEquals(2, nodes.length);
        assertEquals("a", nodes[0].getName());
        assertEquals("b", nodes[1].getName());
    }

    private static class LazyKeys extends Keys<String> {

        public LazyKeys() {
            super(true);
        }

        public void keys(String... args) {
            super.setKeys(args);
        }

        @Override
        protected Node[] createNodes(String key) {
            if (key.startsWith("-")) {
                return null;
            } else {
                AbstractNode n = new AbstractNode(Children.LEAF);
                n.setName(key);
                return new Node[]{n};
            }
        }
    }
}
