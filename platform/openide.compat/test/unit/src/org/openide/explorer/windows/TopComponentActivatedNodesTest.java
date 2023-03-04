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

package org.openide.explorer.windows;

import java.awt.GraphicsEnvironment;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.Collection;
import java.util.Iterator;
import javax.swing.SwingUtilities;
import junit.framework.Test;
import junit.framework.TestSuite;


import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.openide.util.NbMutexEventProvider;

import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerPanel;
import org.openide.explorer.view.BeanTreeView;
import org.openide.explorer.view.TreeView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openide.windows.TopComponent;




/** Check the synchronization TC.getActivatedNodes and ExplorerManager.getSelectedNodes.
 *  Test should assure the fix of issue 31244.
 *
 * @author Jiri Rechtacek
 */
public class TopComponentActivatedNodesTest extends NbTestCase {

    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(TopComponentActivatedNodesTest.class);
    }

    /** top component we work on */
    private TopComponent top;
    
    public TopComponentActivatedNodesTest(java.lang.String testName) {
        super(testName);
    }
    
    protected boolean runInEQ() {
        return true;
    }    
    
    private ExplorerPanel p;
    private ExplorerManager em;
    private Node[] nodes;
    private PropertyChangeListener listenerEM, listenerTC;
    
    protected void setUp () {        
        System.setProperty("org.openide.util.Lookup", Lkp.class.getName()); // no lookup
        
        
        p = new ExplorerPanel ();
        em = p.getExplorerManager ();
        
        TreeView tv = new BeanTreeView ();
        p.add (tv);
        Children ch = new Children.Array ();
        nodes = new Node[10];
        for (int i = 0; i < 10; i++) {
            nodes[i] = new AbstractNode (Children.LEAF);
            nodes[i].setName ("Node" + i);
        }
        ch.add (nodes);
        Node root = new AbstractNode (ch);
        em.setRootContext (root);
        
        // check synchronixzation before
        assertArrays ("INIT: getSelectedNodes equals getActivatedNodes.",
            em.getSelectedNodes (), p.getActivatedNodes ());
    }
    
    private void initListeners () {
        listenerTC = new PropertyChangeListener () {
            public void propertyChange (PropertyChangeEvent ev) {
                System.out.println("TC: PROP_ACTIVATED_NODES change!");
//                try {
//                    Thread.sleep (1000);
//                } catch (Exception e) {
//                }
                assertArrays ("FIRED TC CHANGE: getSelectedNodes equals PROP_ACTIVATED_NODES",
                    em.getSelectedNodes (), p.getActivatedNodes ());
            }
        };
        
        p.addPropertyChangeListener (TopComponent.Registry.PROP_ACTIVATED_NODES, listenerTC);
        
        listenerEM = new PropertyChangeListener () {
            public void propertyChange (PropertyChangeEvent ev) {
                if (ExplorerManager.PROP_SELECTED_NODES.equals (ev.getPropertyName ())) {
                    System.out.println("EM: PROP_SELECTED_NODES change!");
                    assertArrays ("FIRED EM CHANGE: PROP_SELECTED_NODES equals getActivatedNodes",
                        ((Object[])ev.getNewValue ()), p.getActivatedNodes ());
                }
            }
        };
        
        em.addPropertyChangeListener (listenerEM);
        
    }
    
    private void removeListeners () {
        em.removePropertyChangeListener (listenerEM);
        p.removePropertyChangeListener (TopComponent.Registry.PROP_ACTIVATED_NODES, listenerTC);
    }
    
    public void testOnceChange () {
        SwingUtilities.invokeLater (new Runnable () {
            public void run () {
                
                initListeners ();

                // select a node
                try {
                    em.setSelectedNodes (new Node[] { nodes[3], nodes[5] });

                    Node[] activatedNodes = p.getActivatedNodes ();

                    // check synchronixzation after
                    assertArrays ("ONCE CHANGE: getSelectedNodes equals getActivatedNodes.",
                        em.getSelectedNodes (), p.getActivatedNodes ());

                    // lookup
                    Lookup.Result result = p.getLookup ().lookup (new Lookup.Template (Node.class));
                    Collection col = result.allInstances ();
                    Iterator it = col.iterator ();
                    Node[] lookupNodes = new Node[col.size ()];
                    int i = 0;
                    while (it.hasNext ()) {
                        lookupNodes[i] = (Node)it.next ();
                        i++;
                    }

                    // check nodes in lookup with acivated nodes
                    assertArrays ("LOOKUP AFTER INTENSIVE CHANGES: nodes in lookup == activated nodes",
                        lookupNodes, activatedNodes);

                } catch (PropertyVetoException pve) {
                    fail ("Caught PropertyVetoException. msg:" + pve.getMessage ());
                } finally {
                    removeListeners ();
                }
                
            }
        });
    }
    
    public void testIntensiveChange () {
        SwingUtilities.invokeLater (new Runnable () {
            public void run () {
                
                initListeners ();

                // select a node
                try {
                    for (int i = 3; i < 8; i++)
                    em.setSelectedNodes (new Node[] { nodes[i] });

                    Node[] activatedNodes = p.getActivatedNodes ();

                    // check synchronixzation after
                    assertArrays ("INTENSIVE CHANGES: getSelectedNodes equals getActivatedNodes.",
                        em.getSelectedNodes (), activatedNodes);

                    // lookup
                    Lookup.Result result = p.getLookup ().lookup (new Lookup.Template (Node.class));
                    Collection col = result.allInstances ();
                    Iterator it = col.iterator ();
                    Node[] lookupNodes = new Node[col.size ()];
                    int i = 0;
                    while (it.hasNext ()) {
                        lookupNodes[i] = (Node)it.next ();
                        i++;
                    }

                    // check nodes in lookup with acivated nodes
                    assertArrays ("LOOKUP AFTER INTENSIVE CHANGES: nodes in lookup == activated nodes",
                        lookupNodes, em.getSelectedNodes ());

                } catch (PropertyVetoException pve) {
                    fail ("Caught PropertyVetoException. msg:" + pve.getMessage ());
                } finally {
                    removeListeners ();
                }
            }
        });
    }
    
    public void testIntensiveChangeWithLookup () {
        SwingUtilities.invokeLater (new Runnable () {
            public void run () {
                
                initListeners ();
                // select a node
                try {
                    for (int i = 3; i < 8; i++)
                    em.setSelectedNodes (new Node[] { nodes[i] });
                } catch (PropertyVetoException pve) {
                    fail ("Caught PropertyVetoException. msg:" + pve.getMessage ());
                } finally {
                    removeListeners ();
                }

                // get nodes from lookup
                Lookup.Result result = p.getLookup ().lookup (new Lookup.Template (Node.class));
                Collection col = result.allInstances ();
                Iterator it = col.iterator ();
                Node[] lookupNodes = new Node[col.size ()];
                int i = 0;
                while (it.hasNext ()) {
                    lookupNodes[i] = (Node)it.next ();
                    i++;
                }

                // check nodes in lookup with acivated nodes
                assertArrays ("LOOKUP AFTER INTENSIVE CHANGES: nodes in lookup == activated nodes",
                    lookupNodes, p.getActivatedNodes ());

                // check nodes in lookup with selected nodes
                assertArrays ("LOOKUP AFTER INTENSIVE CHANGES: nodes in lookup == activated nodes",
                    lookupNodes, em.getSelectedNodes ());
            }
        });
    }
    
    
    public void testInteroperabilityWithTopComponentRegistry () throws Exception {
        final TopComponent tc = new TopComponent ();
        final Lookup.Result res = tc.getLookup ().lookup (new Lookup.Template (Node.class));
        
        assertNull ("Empty arrays", tc.getActivatedNodes());
        assertEquals ("Empty list of nodes", 0, res.allInstances().size ());
        
        class L implements PropertyChangeListener, org.openide.util.LookupListener {
            public Object[] expectedArray;
            public java.util.ArrayList events = new java.util.ArrayList ();
            
            public void resultChanged (org.openide.util.LookupEvent ev) {
                events.add (ev);
            }
            
            public void propertyChange (PropertyChangeEvent ev) {
                if (TopComponent.Registry.PROP_CURRENT_NODES.equals (ev.getPropertyName ())) {
                    assertArrays ("Should be the same", tc.getActivatedNodes(), expectedArray);
                    assertArrays (
                        "Also in lookup. ", 
                        res.allInstances ().toArray (),
                        expectedArray
                    );
                }
                events.add (ev);
            }
        }
        L l = new L ();
        res.addLookupListener(l);

        tc.requestActive ();        
        assertEquals ("Really activated", tc, TopComponent.getRegistry ().getActivated ());
        try {
            TopComponent.getRegistry ().addPropertyChangeListener (l);
            
            Node[] arr = { Node.EMPTY };
            l.expectedArray = arr;
            tc.setActivatedNodes (arr);
            
            Object[] ev = l.events.toArray ();
            assertEquals ("Three events", 3, ev.length);
            assertEquals ("First is lookup change", org.openide.util.LookupEvent.class, ev[0].getClass ());
            assertEquals ("Second is prop change", PropertyChangeEvent.class, ev[1].getClass ());
            assertEquals ("Third is prop change", PropertyChangeEvent.class, ev[2].getClass ());
            
            assertEquals (TopComponent.Registry.PROP_ACTIVATED_NODES, ((PropertyChangeEvent)ev[1]).getPropertyName());
            assertEquals (TopComponent.Registry.PROP_CURRENT_NODES, ((PropertyChangeEvent)ev[2]).getPropertyName());
        } finally {
            TopComponent.getRegistry ().removePropertyChangeListener (l);
        }
    }
    
    private void assertArrays (String msg, Object[] arr1, Object[] arr2) {
        // DEBUG MSG log content of arrays
//        System.out.println("do ["+msg+"]: ");
//        if (arr1 != null) for (int i = 0; i < arr1.length; i++) System.out.println("Arr1: " + i + ". " + arr1[i]);
//        if (arr2 != null) for (int i = 0; i < arr2.length; i++) System.out.println("Arr2: " + i + ". " + arr2[i]);
//        System.out.println("done!");
        // END OF DEBUG MSG
        if (arr1 == null && arr2 == null) return ;
        if (arr1 == null) {
            if (arr2.length == 0) {
                return ;
            } else {
                fail (msg + " BUT: Array1 was null Array2 was " + java.util.Arrays.asList (arr2));
            }
        }
        if (arr2 == null) {
            if (arr1.length == 0) {
                return ;
            } else {
                fail (msg + " BUT: Array2 was null Array1 was " + java.util.Arrays.asList (arr1));
            }
        }
        if (arr1.length != arr2.length) fail (msg + "Arrays have a diferent size. First: " + java.util.Arrays.asList (arr1) + " second: " + java.util.Arrays.asList (arr2));
        //Arrays.sort (arr1);
        //Arrays.sort (arr2);
        for (int i = 0; i < arr1.length; i++) {
            if (! arr1[i].equals (arr2[i]) ) {
                fail (msg + " BUT: excepted: " + arr1[i] + ", was: " + arr2[i]);
            }
        }
    }

    public static final class Lkp extends ProxyLookup {
        public Lkp() {
            setLookups(Lookups.singleton(new NbMutexEventProvider()));
        }
    }
}
