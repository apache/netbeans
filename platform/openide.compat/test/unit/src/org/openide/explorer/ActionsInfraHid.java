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

package org.openide.explorer;

import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.windows.TopComponent;
import org.openide.nodes.Node;
import java.util.Set;
import org.openide.util.actions.SystemAction;
import java.awt.event.ActionEvent;
import javax.swing.Action;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import org.openide.windows.WindowManager;
import javax.swing.JFrame;
import java.awt.Frame;
import org.openide.windows.Workspace;
import org.openide.util.NotImplementedException;
import java.awt.Image;
import java.beans.PropertyChangeSupport;
import org.openide.util.Lookup;
import java.util.ArrayList;
import org.netbeans.modules.openide.util.NbMutexEventProvider;

/** Utilities for actions tests.
 * @author Jesse Glick
 */
public abstract class ActionsInfraHid {
    
    private ActionsInfraHid() {}
    
    public static final UsefulThings UT;
    static {
        String lookup = System.getProperty("org.openide.util.Lookup");
        if (lookup != null && !lookup.equals(UsefulLookup.class.getName())) throw new IllegalStateException("Already had a Lookup installed: " + lookup);
        System.setProperty("org.openide.util.Lookup", UsefulLookup.class.getName());
        UT = new UsefulThings();
        Lookup l = Lookup.getDefault();
        if (!(l instanceof UsefulLookup)) throw new IllegalStateException(Lookup.getDefault().toString());
        if (l.lookup(TopComponent.Registry.class) == null) throw new IllegalStateException("no TC.R");
        //if (l.lookup(WindowManager.class) == null) throw new IllegalStateException("no WindowManager");
        //if (CallbackSystemAction.getRegistry() == null) throw new IllegalStateException("no TC.R again!");
    }
    public static void main(String[] args) {
        System.err.println("ActionsInfraHid OK.");
    }
    
    /** Lookup which provides a TC.Registry and ActionManager.
     */
    public static final class UsefulLookup extends AbstractLookup {
        public UsefulLookup() {
            super(getContent());
        }
        private static AbstractLookup.Content getContent() {
            InstanceContent c = new InstanceContent();
            c.add(UT);
            c.add(ActionsInfraHid.class.getClassLoader());
            c.add(new NbMutexEventProvider());
            return c;
        }
    }
    
    /** An action manager and top component registry.
     */
    public static final class UsefulThings implements TopComponent.Registry, org.openide.util.ContextGlobalProvider {
        // Registry:
        private TopComponent activated;
        /** instances to keep */
        private InstanceContent ic = new InstanceContent ();
        /** lookup */
        private Lookup lookup = new AbstractLookup (ic);
        /** changes */
        private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
        
        public void addPropertyChangeListener(PropertyChangeListener l) {
            pcs.addPropertyChangeListener(l);
        }
        
        public void removePropertyChangeListener(PropertyChangeListener l) {
            pcs.removePropertyChangeListener(l);
        }
        
        private void firePropertyChange(String p, Object o, Object n) {
            pcs.firePropertyChange(p, o, n);
        }
        
        
        public TopComponent getActivated() {
            return activated;
        }
        
        public void setActivated(TopComponent nue) {
            TopComponent old = activated;
            activated = nue;
            firePropertyChange(PROP_ACTIVATED, old, nue);
            updateLookup ();
        }
        
        private Node[] activatedNodes = new Node[0];
        private Node[] currentNodes = null;
        
        public Node[] getActivatedNodes() {
            return activatedNodes;
        }
        
        public Node[] getCurrentNodes() {
            return currentNodes;
        }
        
        public void setCurrentNodes(Node[] nue) {
            if (nue != null) {
                Node[] old = activatedNodes;
                activatedNodes = nue;
                firePropertyChange(PROP_ACTIVATED_NODES, old, nue);
            }
            Node[] old = currentNodes;
            currentNodes = nue;
            firePropertyChange(PROP_CURRENT_NODES, old, nue);
            updateLookup ();
        }
        
        private Set opened = null;
        
        public Set getOpened() {
            return opened;
        }
        
        public void setOpened(Set nue) {
            Set old = opened;
            opened = nue;
            firePropertyChange(PROP_OPENED, old, nue);
        }
        
        private void updateLookup () {
            ArrayList items = new ArrayList ();
            if (currentNodes != null) {
                for (int i = 0; i < currentNodes.length; i++) {
                    items.add (new IPair (currentNodes[i]));
                }
            } else {
                items.add (IPair.NULL_NODES);
            }
            if (activated != null) {
                items.add (new IPair (activated.getActionMap ()));
            }
            ic.setPairs (items);
        }
                
        //
        // ContextGlobalProvider
        //
        public Lookup createGlobalContext() {
            return lookup;
        }
    }
    
    /** Prop listener that will tell you if it gets a change.
     */
    public static final class WaitPCL implements PropertyChangeListener {
        /** whether a change has been received, and if so count */
        public int gotit = 0;
        /** optional property name to filter by (if null, accept any) */
        private final String prop;
        public WaitPCL(String p) {
            prop = p;
        }
        public synchronized void propertyChange(PropertyChangeEvent evt) {
            if (prop == null || prop.equals(evt.getPropertyName())) {
                gotit++;
                notifyAll();
            }
        }
        public boolean changed() {
            return changed(1500);
        }
        public synchronized boolean changed(int timeout) {
            if (gotit > 0) {
                return true;
            }
            try {
                wait(timeout);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
            return gotit > 0;
        }
    }

    // Stolen from RequestProcessorTest.
    public static void doGC() {
        doGC(10);
    }
    public static void doGC(int count) {
        ArrayList l = new ArrayList(count);
        while (count-- > 0) {
            System.gc();
            System.runFinalization();
            l.add(new byte[1000]);
        }
    }

    private static final class IPair extends AbstractLookup.Pair {
        private Object obj;
        
        public static final IPair NULL_NODES = new IPair (
            new org.openide.nodes.AbstractNode (org.openide.nodes.Children.LEAF)
        );
        
        public IPair (Object obj) {
            this.obj = obj;
        }
        
        protected boolean creatorOf(Object obj) {
            return this.obj == obj;
        }
        
        public String getDisplayName() {
            return obj.toString ();
        }
        
        public String getId() {
            if (this == NULL_NODES) {
                return "none"; // NOI18N
            }
            return obj.toString ();
        }
        
        public Object getInstance() {
            if (this == NULL_NODES) {
                return null;
            }
            return obj;
        }
        
        public Class getType() {
            return obj.getClass();
        }
        
        protected boolean instanceOf(Class c) {
            return c.isInstance(obj);
        }
        
    } // end of IPair
}
