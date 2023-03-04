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

package org.openide.nodes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.swing.Action;
import org.netbeans.junit.NbTestCase;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CallbackSystemAction;
import org.openide.util.actions.SystemAction;

/**
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class LazyNodeTest extends NbTestCase {

    public LazyNodeTest(String n) {
        super(n);
    }

    @Override
    protected Level logLevel() {
        return Level.ALL;
    }

    public void testCreateOriginalAfterNodeExpansion() {
        doCreateOriginal(true, false);
    }

    public void testCreateOriginalAfterGetActions() {
        doCreateOriginal(false, false);
        doCreateOriginal(false, true);
    }

    private void doCreateOriginal(boolean askForChildren, boolean underReadAccess) {
        AbstractNode realNode = new AbstractNode(new Children.Array()) {
            @Override
            public Action[] getActions(boolean context) {
                return getActions();
            }

            @SuppressWarnings("deprecation")
            @Override
            public SystemAction[] getActions() {
                return new SystemAction[] {
                    A1.get(A1.class),
                    A2.get(A2.class),
                    A3.get(A3.class)
                };
            }
        };
        realNode.setName("RealNode");
        realNode.setDisplayName("Real Node");
        realNode.setShortDescription("Real Node for Test");
        realNode.getChildren().add(new Node[] {
            new AbstractNode(Children.LEAF),
            new AbstractNode(Children.LEAF),
            new AbstractNode(Children.LEAF),
        });

        CntHashMap chm = new CntHashMap("original");
        chm.put("name", "ANode");
        chm.put("displayName", "A Node");
        chm.put("shortDescription", "A Node for Test");
        chm.put("iconResource", "org/openide/nodes/beans.gif");
        chm.put("original", realNode);

        Node instance = NodeOp.factory(chm);
        assertEquals("ANode", instance.getName());
        assertEquals("A Node", instance.getDisplayName());
        assertEquals("A Node for Test", instance.getShortDescription());
        assertEquals("No real node queried yet", 0, chm.cnt);

        if (askForChildren) {
            Node[] arr = instance.getChildren().getNodes(true);
            assertEquals("Three children", 3, arr.length);
        } else {
            if (underReadAccess) {
                try {
                    Children.PR.enterReadAccess();
                    Action[] arr = instance.getActions(true);
                    assertEquals("Three actions", 3, arr.length);
                } finally {
                    Children.PR.exitReadAccess();
                }
            } else {
                Action[] arr = instance.getActions(true);
                assertEquals("Three actions", 3, arr.length);
            }
        }
        
        assertEquals("Real node queried now", 1, chm.cnt);

        assertEquals("RealNode", instance.getName());
        assertEquals("Real Node", instance.getDisplayName());
        assertEquals("Real Node for Test", instance.getShortDescription());
    }

    private static class CntHashMap extends HashMap<String,Object> {
        private final String keyToWatch;
        int cnt;

        public CntHashMap(String keyToWatch) {
            this.keyToWatch = keyToWatch;
        }

        @Override
        public Object get(Object key) {
            if (keyToWatch.equals(key)) {
                cnt++;
            }
            return super.get((String) key);
        }

    }

    public static class A1 extends CallbackSystemAction {
        @Override
        public String getName() {
            return "A1";
        }

        @Override
        public HelpCtx getHelpCtx() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }
    public static final class A2 extends A1 {
        @Override
        public String getName() {
            return "A2";
        }
    }
    public static final class A3 extends A1 {
        @Override
        public String getName() {
            return "A3";
        }
    }

    public void testFindChild() throws Exception {
        Map<String,Object> m = new HashMap<String,Object>();
        m.put("original", new AbstractNode(Children.create(new ChildFactory<String>() {
            protected boolean createKeys(List<String> toPopulate) {
                toPopulate.add("one");
                toPopulate.add("two");
                toPopulate.add("three");
                return true;
            }
            protected @Override Node createNodeForKey(String key) {
                Node n = new AbstractNode(Children.LEAF);
                n.setName(key);
                return n;
            }
        }, true)));
        Node lazy = NodeOp.factory(m);
        Node kid = NodeOp.findChild(lazy, "two");
        assertNotNull(kid);
        assertEquals("two", kid.getName());
    }

}
