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

package org.openide.windows;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.beans.FeatureDescriptor;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.openide.windows.GlobalActionContextImpl;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Result;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/** Tests behaviour of GlobalContextProviderImpl
 * and its cooperation with activated and current nodes.
 *
 * @author Jaroslav Tulach
 */
public class GlobalContextImplTest extends NbTestCase
implements org.openide.util.LookupListener {
    private static Object KEY = new Object();

    private javax.swing.Action sampleAction = new AbstractActionImpl ();
    private TopComponent tc;
    private Lookup lookup;
    private Lookup.Result result;
    private int cnt;
    
    
    public GlobalContextImplTest(java.lang.String testName) {
        super(testName);
    }
    
    @Override
    protected int timeOut() {
        return 15000;
    }
    
    @Override
    protected void setUp () throws Exception {
        tc = new TopComponent ();
        tc.getActionMap ().put (KEY, sampleAction);
        tc.requestActive();
        
        
        Lookup global = Lookup.getDefault();
        
        Object p = global.lookup (org.openide.util.ContextGlobalProvider.class);
        assertNotNull ("There is one", p);
        assertEquals ("Action context provider is our as well", org.netbeans.modules.openide.windows.GlobalActionContextImpl.class, p.getClass ());
        
        
        lookup = org.openide.util.Utilities.actionsGlobalContext();
        result = lookup.lookup (new Lookup.Template<Node> (Node.class));
        result.addLookupListener (this);
        result.allItems();
    }
    
    private void assertActionMap () {
        ActionMap map = lookup.lookup(ActionMap.class);
        assertNotNull ("Map has to be there", map);
        
        javax.swing.Action action = map.get (KEY);
        assertEquals ("It is really our action", sampleAction, action);
    }
        
    
    public void testCurrentNodes () throws Exception {
        tc.setActivatedNodes(new Node[] {Node.EMPTY});

        assertEquals ("This fires change", 1, cnt);

        assertEquals ("One item in result", 1, result.allItems ().size ());
        Lookup.Item item = (Lookup.Item)result.allItems ().iterator ().next ();
        assertEquals ("Item should return Node.EMPTY", Node.EMPTY, item.getInstance());
        assertActionMap ();
        
        tc.setActivatedNodes (null);
        assertEquals ("One change", 2, cnt);
        
        assertEquals ("One empty item in result", 1, result.allItems ().size ());
        item = (Lookup.Item)result.allItems ().iterator ().next ();
        assertEquals ("Item should return null", null, item.getInstance());
        assertEquals ("Name is null", "none", item.getId ());
        assertActionMap ();
        
        Result<MyNode> subclass = lookup.lookup (new Lookup.Template<MyNode> (MyNode.class));
        assertTrue("No items are returned", subclass.allItems().isEmpty());
        
        Result<FeatureDescriptor> superclass = lookup.lookup (new Lookup.Template<FeatureDescriptor>(FeatureDescriptor.class));
        assertEquals("One item is returned", 1, superclass.allItems().size());
        item = (Lookup.Item)superclass.allItems ().iterator ().next ();
        assertEquals ("Item should return null", null, item.getInstance());
        
        tc.setActivatedNodes (new Node[0]);
        assertEquals ("No change", 3, cnt);
        
        assertEquals ("No items in lookup", 0, result.allItems ().size ());
        assertActionMap ();
    }
    
    public void testRequestVisibleBlinksTheActionMapForAWhile () throws Exception {
        doRequestVisibleBlinksTheActionMapForAWhile(new TopComponent(), true,false);
    }

    public void testSetParentMapBlinks () throws Exception {
        doRequestVisibleBlinksTheActionMapForAWhile(new TopComponent(), false,false);
    }

    public void testSetParentMapBlinksRecursive () throws Exception {
        doRequestVisibleBlinksTheActionMapForAWhile(new TopComponent(), false, true);
    }

    private void doRequestVisibleBlinksTheActionMapForAWhile(TopComponent my, boolean requestVisible, final boolean recursive) throws Exception {
        final org.openide.nodes.Node n = new org.openide.nodes.AbstractNode (org.openide.nodes.Children.LEAF);
        tc.setActivatedNodes(new Node[] { n });
        
        assertActionMap ();
        
        class L implements org.openide.util.LookupListener {
            Lookup.Result<ActionMap> res = lookup.lookup (new Lookup.Template<ActionMap> (ActionMap.class));
            ArrayList<ActionMap> maps = new ArrayList<ActionMap> ();
            int cnt;
            
            public void resultChanged (org.openide.util.LookupEvent ev) {
                assertTrue("Changes are comming from AWT thread only", EventQueue.isDispatchThread());
                assertEquals ("Still only one", 1, res.allItems ().size ());
                Lookup.Item<ActionMap> i = res.allItems ().iterator ().next ();
                assertNotNull (i);
                
                maps.add (i.getInstance ());
                
                assertNode ();

                if (recursive && cnt++ == 0) {
                    GlobalActionContextImpl.blickActionMap(new ActionMap());
                }
            }
            
            public void assertNode () {
                assertEquals ("The node is available", n, lookup.lookup (Node.class));
            }
        }
        L myListener = new L ();
        assertEquals ("One action map", 1, myListener.res.allItems ().size ());
        myListener.assertNode ();
        
        ActionMap m1, m2;
        myListener.res.addLookupListener (myListener);
        try {
            if (requestVisible) {
                my.requestVisible ();
            } else {
                my.getActionMap().setParent(new ActionMap());
            }
            waitEQ();
            if (recursive) {
                return;
            }

            if (myListener.maps.size () != 2) {
                fail ("Expected two changes in the ActionMaps: " + myListener.maps);
            }

            myListener.assertNode ();

            m1 = myListener.maps.get(0);
            m2 = myListener.maps.get(1);

            assertNull ("Our action is not in first map", m1.get (KEY));
            assertEquals ("Our action is in second map", sampleAction, m2.get (KEY));

            assertActionMap ();
        } finally {
            myListener.res.removeLookupListener(myListener);
        }

        my.close();
        
     //   new TopComponent ().open();
        
        Reference<Object> ref = new WeakReference<Object>(my);
        my = null;
        m1 = null;
        m2 = null;
        myListener.maps.clear();

        //this.tc = null;
        //this.lookup = null;
        //this.result = null;
        //myListener.res = null;
        //myListener = null;
        //this.sampleAction = null;
        
        assertGC("Can be GCed", ref);
    }
    
    private static class OwnTopComponent extends TopComponent {
        public OwnTopComponent() {
            getActionMap().put("ahoj", new OwnAction());
        }

        class OwnAction extends AbstractAction {

            public void actionPerformed(ActionEvent e) {
                OwnTopComponent.this.open();
            }

        }
    }
    
    public void testRequestVisibleBlinksTheActionMapForAWhileWithOwnComponentAndAction() throws Exception {
        doRequestVisibleBlinksTheActionMapForAWhile(new OwnTopComponent(), true,false);
    }

    public void testActionMapSetParentWithOwnComponentAndAction() throws Exception {
        doRequestVisibleBlinksTheActionMapForAWhile(new OwnTopComponent(), false,false);
    }
    
    public void testComponentChangeActionMapIsPropagatedToGlobalLookup() throws Exception {
        assertEquals("test1", 0, cnt);

        
        InstanceContent ic = new InstanceContent();
        AbstractLookup al = new AbstractLookup(ic);
        tc = new TopComponent (al);

        assertEquals("test2", 0, cnt);
        
        
        ActionMap myMap = new ActionMap();
        myMap.put (KEY, sampleAction);
        assertEquals("test3", 0, cnt);

        tc.requestActive();
        
        assertEquals("test4", 1, cnt);
        
        result = lookup.lookup (new Lookup.Template<ActionMap> (ActionMap.class));
        result.addLookupListener (this);
        result.allItems();
        
        assertEquals("test5", 1, cnt);
        
        ic.set(Collections.singleton(new ActionMap()), null);
        
        assertEquals("One change in ActiomMap delivered", 2, cnt);
    }
    
    
    public void resultChanged(org.openide.util.LookupEvent ev) {
        cnt++;
    }

    private void waitEQ() throws Exception {
        EventQueue.invokeAndWait(new Runnable() {
            public void run() {
            }
        });
    }

    private static class AbstractActionImpl extends AbstractAction {

        private AbstractActionImpl() {
        }

        public void actionPerformed(java.awt.event.ActionEvent ev) {
        }
    }

    private static final class MyNode extends AbstractNode {
        public MyNode() {
            super(Children.LEAF);
        }
    }
}
