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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
