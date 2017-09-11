/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.search.ui;

import java.util.LinkedList;
import java.util.List;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.modules.search.TextDetail;
import org.openide.explorer.view.OutlineView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author jhavlin
 */
public class BasicSearchResultsPanelTest {

    private Node a;
    private Node b;
    private Node c;
    private Node a1;
    private Node b2;
    private Node c3;
    private RootNode rootNode;

    private AbstractSearchResultsPanel resultsPanel;

    @Before
    public void setUp() {
        rootNode = new RootNode();
        a = rootNode.getChildren().getNodeAt(0);
        b = rootNode.getChildren().getNodeAt(1);
        c = rootNode.getChildren().getNodeAt(2);
        a1 = a.getChildren().getNodeAt(0);
        b2 = b.getChildren().getNodeAt(1);
        c3 = c.getChildren().getNodeAt(2);
        resultsPanel = new AbstractSearchResultsPanel(null, null) {

            @Override
            protected OutlineView getOutlineView() {
                return null;
            }

            @Override
            protected boolean isDetailNode(Node n) {
                return n.getLookup().lookup(TextDetail.class) != null;
            }
        };
    }

    @Test
    public void testMockTree() {
        assertEquals("a", a.getDisplayName());
        assertEquals("a1", a1.getDisplayName());
        assertEquals("b2", b2.getDisplayName());
        assertEquals("c3", c3.getDisplayName());
    }

    @Test
    public void testNext() {
        assertEquals(a1, next(rootNode));
        assertEquals(a1, next(a));
        assertEquals(b2, next(b.getChildren().getNodeAt(0)));
        assertEquals(c3, next(c.getChildren().getNodeAt(1)));
        assertEquals(b.getChildren().getNodeAt(0), next(b));
        assertNull(next(c3));
    }

    @Test
    public void testPrev() {
        assertEquals(a1, prev(a.getChildren().getNodeAt(1)));
        assertEquals(b2, prev(b.getChildren().getNodeAt(2)));
        assertEquals(a.getChildren().getNodeAt(2),
                prev(b.getChildren().getNodeAt(0)));
        assertNull(prev(a));
        assertNull(prev(a1));
        assertNull(prev(rootNode));
    }

    private Node next(Node fromNode) {
        return resultsPanel.findDetailNode(fromNode, 1, null,
                false);
    }

    private Node prev(Node fromNode) {
        return resultsPanel.findDetailNode(fromNode, -1, null,
                false);
    }

    private static class RootNode extends AbstractNode {

        public RootNode() {
            super(new FileChildren());
        }
    }

    private static class FileChildren extends Children.Keys<String> {

        public FileChildren() {
            String[] keys = {"a", "b", "c"};                            //NOI18N
            setKeys(keys);
        }

        @Override
        protected Node[] createNodes(final String key) {
            return new Node[]{new AbstractNode(
                        new TextDetailChilren(key, 3)) {
                    @Override
                    public String getDisplayName() {
                        return key;

                    }
                }};
        }
    }

    private static class TextDetailChilren extends Children.Keys<TextDetail> {

        public TextDetailChilren(String prefix, int count) {
            List<TextDetail> keys = new LinkedList<TextDetail>();
            for (int i = 1; i <= count; i++) {
                TextDetail td = new TextDetail(null, null);
                td.setLineText(prefix + i);
                keys.add(td);
            }
            setKeys(keys);
        }

        @Override
        protected Node[] createNodes(final TextDetail key) {
            return new Node[]{new AbstractNode(LEAF, Lookups.singleton(key)) {
                    @Override
                    public String getDisplayName() {
                        return key.getLineText().toString();
                    }
                }};
        }
    }
}
