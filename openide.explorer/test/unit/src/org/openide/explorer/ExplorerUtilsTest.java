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

package org.openide.explorer;

import java.awt.EventQueue;
import java.beans.PropertyVetoException;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.KeyStroke;
import org.netbeans.api.actions.Openable;
import org.netbeans.api.actions.Savable;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Result;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 * Check the behaviour of the ExplorerUtils
 *
 * @author Petr Nejedly
 */
public class ExplorerUtilsTest extends NbTestCase {
    public ExplorerUtilsTest(String testName) {
        super(testName);
    }

    @RandomlyFails // NB-Core-Build #8019 hung in waitActionsFinished
    public void testIsEnabledOnDelete() {
        ExplorerManager em = new ExplorerManager();
        Action a = ExplorerUtils.actionDelete(em, true);
        em.waitActionsFinished();
        assertFalse("No AWT", EventQueue.isDispatchThread());
        assertFalse("Is disabled", a.isEnabled());
    }

    public void testGetHelpCtx() throws Exception {
        HelpCtx DEF = new HelpCtx("default");
        
        assertEquals("Use default help for no nodes",
                DEF,
                ExplorerUtils.getHelpCtx(new Node[0], DEF));
        
        assertEquals("Use default help for single node w/o help",
                DEF,
                ExplorerUtils.getHelpCtx(new Node[] {new NoHelpNode()}, DEF));
        
        assertEquals("Use provided help for single node with help",
                new HelpCtx("foo"),
                ExplorerUtils.getHelpCtx(new Node[] {new WithHelpNode("foo")}, DEF));
        
        assertEquals("Use default help for more nodes w/o help",
                DEF,
                ExplorerUtils.getHelpCtx(new Node[] {new NoHelpNode(), new NoHelpNode()}, DEF));
        
        assertEquals("Use provided help if only one node has help",
                new HelpCtx("foo"),
                ExplorerUtils.getHelpCtx(new Node[] {new NoHelpNode(), new WithHelpNode("foo")}, DEF));
        
        assertEquals("Use provided help if more nodes have the same help",
                new HelpCtx("foo"),
                ExplorerUtils.getHelpCtx(new Node[] {new WithHelpNode("foo"), new WithHelpNode("foo")}, DEF));
        
        assertEquals("Use default help if nodes have different helps",
                DEF,
                ExplorerUtils.getHelpCtx(new Node[] {new WithHelpNode("foo"), new WithHelpNode("bar")}, DEF));
    }
    
    public void testUseBigLettersInJavaDocIssue46615() throws Exception {
        assertNotNull(KeyStroke.getKeyStroke("control C"));
        assertNotNull(KeyStroke.getKeyStroke("control X"));
        assertNotNull(KeyStroke.getKeyStroke("control V"));
    }
    
    public void testAssertFilteringIsLazy() throws Exception {
        LazyOpenNode n = new LazyOpenNode();
        ExplorerManager em = new ExplorerManager();
        Lookup lkp = ExplorerUtils.createLookup(em, new ActionMap());
        em.setRootContext(n);
        em.setSelectedNodes(new Node[] { n });
        
        waitAWT();
        
        assertSame("My node", n, lkp.lookup(Node.class));
        assertEquals("One node", 1, lkp.lookupAll(Node.class).size());
        assertTrue("No savable", lkp.lookupAll(Savable.class).isEmpty());
        
        Result<Openable> res = lkp.lookupResult(Openable.class);
        assertEquals("One item", 1, res.allItems().size());
        n.assertNoOpen();
        assertFalse("There is some openable", res.allInstances().isEmpty());
        n.assertOpen(lkp.lookup(Openable.class));
    }

    private void waitAWT() throws Exception {
        EventQueue.invokeAndWait(new Runnable() {
            @Override
            public void run() {
            }
        });
    }
    
    private static final class NoHelpNode extends AbstractNode {
        public NoHelpNode() {
            super(Children.LEAF);
        }
    }
    
    private static final class WithHelpNode extends AbstractNode {
        private final String id;
        public WithHelpNode(String id) {
            super(Children.LEAF);
            this.id = id;
        }
        public HelpCtx getHelpCtx() {
            return new HelpCtx(id);
        }
    }
    
    private static final class LazyOpenNode extends AbstractNode
    implements InstanceContent.Convertor<LazyOpenNode, Openable> {
        private Openable open;
        
        public LazyOpenNode() {
            this(new InstanceContent());
        }
        
        private LazyOpenNode(InstanceContent ic) {
            super(Children.LEAF, new AbstractLookup(ic));
            ic.add(this, this);
            ic.add(this);
        }
        
        final void assertNoOpen() {
            assertNull("No open", open);
        }
        final void assertOpen(Openable o) {
            assertSame("Now open", open, o);
        }

        @Override
        public synchronized Openable convert(LazyOpenNode obj) {
            if (open == null) {
                open = new Openable() {
                    @Override
                    public void open() {
                    }
                };
            }
            return open;
        }

        @Override
        public Class<? extends Openable> type(LazyOpenNode obj) {
            return Openable.class;
        }

        @Override
        public String id(LazyOpenNode obj) {
            return "open";
        }

        @Override
        public String displayName(LazyOpenNode obj) {
            return "Open";
        }
    }
    
}
