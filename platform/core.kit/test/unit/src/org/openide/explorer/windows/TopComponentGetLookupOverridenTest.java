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

import java.util.Arrays;
import java.util.logging.Logger;
import javax.swing.ActionMap;
import javax.swing.text.DefaultEditorKit;


import org.openide.explorer.*;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;



/** Tests behaviour of GlobalContextProviderImpl
 * and its cooperation with activated and current nodes when TopComponent is
 * using its own lookup as in examples of ExplorerUtils...
 *
 * @author Jaroslav Tulach
 */
public class TopComponentGetLookupOverridenTest extends TopComponentGetLookupTest {
    private Logger LOG = Logger.getLogger(TopComponentGetLookupOverridenTest.class + ".TEST-" + getName());

    public TopComponentGetLookupOverridenTest (java.lang.String testName) {
        super(testName);
    }
    
    /** Setup component with lookup.
     */
    protected void setUp () {
        ListingYourComponent tc = new ListingYourComponent (LOG);
        top = tc;
        get = tc.delegate;
        lookup = tc.delegate.getLookup ();
    }


    private static class ListingYourComponent extends TopComponent
    implements java.beans.PropertyChangeListener {
        YourComponent delegate;
        private Logger LOG;

        public ListingYourComponent (Logger l) {
            delegate = new YourComponent();
            LOG = l;
            
            addPropertyChangeListener (this);
            delegate.getExplorerManager ().setRootContext (new AbstractNode (new Children.Array ()));
            java.lang.ref.SoftReference ref = new java.lang.ref.SoftReference (new Object ());
            assertGC ("Trying to simulate issue 40842, to GC TopComponent$SynchronizeNodes", ref);
            
            delegate.getExplorerManager().addPropertyChangeListener(this);
        }
        
        private ThreadLocal callbacks = new ThreadLocal ();
        public void propertyChange (java.beans.PropertyChangeEvent ev) {
            ExplorerManager manager = delegate.getExplorerManager ();

            LOG.info("propertyChange: " + ev.getPropertyName());

            if ("activatedNodes".equals (ev.getPropertyName())) {
                if (Boolean.TRUE.equals (callbacks.get ())) {
                    LOG.info("  it was callback");
                    return;
                }
                try {
                    callbacks.set (Boolean.TRUE);
                    Node[] arr = getActivatedNodes ();

                    LOG.info("settings ndoes to zero");
                    // first of all clear the previous values otherwise
                    // we will not test SynchronizeNodes (associateLookup (..., true))
                    setActivatedNodes (ownNode());


                    Children.Array ch = (Children.Array)manager.getRootContext ().getChildren ();
                    for (int i = 0; i < arr.length; i++) {
                        if (arr[i].getParentNode() != manager.getRootContext()) {
                            assertTrue ("If this fails we are in troubles", ch.add (new Node[] { arr[i] }));
                        }
                    }
                    LOG.info("em setSelectedNodes: " + Arrays.asList(arr));
                    manager.setSelectedNodes (arr);
                    LOG.info("em setSelectedNodes done: " + Arrays.asList(arr));
                } catch (java.beans.PropertyVetoException ex) {
                    ex.printStackTrace();
                    fail (ex.getMessage());
                } finally {
                    callbacks.set (null);
                }
            }

        }

        public String toString() {
            return "ListingYourComponent";
        }


        private static Node[] ownNode() {
            AbstractNode a = new AbstractNode(Children.LEAF);
            a.setName("ownNode");
            return new Node[] { a };
        }
    } // end of ListingYourComponent
    
    // The following class is copied from example in ExplorerUtils:
    //
    public static class YourComponent extends TopComponent
    implements ExplorerManager.Provider, Lookup.Provider {
        private ExplorerManager manager;
        public YourComponent() {
            this.manager = new ExplorerManager ();
            ActionMap map = getActionMap ();
            map.put(DefaultEditorKit.copyAction, ExplorerUtils.actionCopy(manager));
            map.put(DefaultEditorKit.cutAction, ExplorerUtils.actionCut(manager));
            map.put(DefaultEditorKit.pasteAction, ExplorerUtils.actionPaste(manager));
            map.put("delete", ExplorerUtils.actionDelete(manager, true)); // or false
            
            associateLookup (ExplorerUtils.createLookup (manager, map));
        }
        public ExplorerManager getExplorerManager() {
            return manager;
        }
        // It is good idea to switch all listeners on and off when the
        // component is shown or hidden. In the case of TopComponent use:
        protected void componentActivated() {
            ExplorerUtils.activateActions(manager, true);
        }
        protected void componentDeactivated() {
            ExplorerUtils.activateActions(manager, false);
        }
        public String toString() {
            return "YourComponent";
        }
    } // end of YourComponent
}  
    
