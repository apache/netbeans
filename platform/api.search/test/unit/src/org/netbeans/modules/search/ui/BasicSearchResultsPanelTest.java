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
            List<TextDetail> keys = new LinkedList<>();
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
