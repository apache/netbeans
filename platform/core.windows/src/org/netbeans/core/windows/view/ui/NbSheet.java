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

package org.netbeans.core.windows.view.ui;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.propertysheet.PropertySheet;
import org.openide.nodes.Node;
import org.openide.nodes.NodeAdapter;
import org.openide.nodes.NodeEvent;
import org.openide.nodes.NodeListener;
import org.openide.nodes.NodeOp;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Mutex;
import org.openide.util.Utilities;
import org.openide.util.io.NbMarshalledObject;
import org.openide.util.io.SafeException;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Default view for properties.
 */
public final class NbSheet extends TopComponent {
    private static final Logger LOG = Logger.getLogger(NbSheet.class.getName());
    
    /**
     * Name of a property that can be passed in a Node instance. The value
     * of the property must be String and can be an alternative to displayName.
     */
    private static final String PROP_LONGER_DISPLAY_NAME = "longerDisplayName"; // NOI18N

    /** generated Serialized Version UID */
    static final long serialVersionUID = 7807519514644165460L;

    /** shared sheet */
    private static NbSheet sharedSheet;
    /** listener to the property changes */
    private final transient Listener listener;
    /** listener to the node changes, especially their destruction */
    private final transient SheetNodesListener snListener;
    /** Should property sheet listen to the global changes ? */
    boolean global;
    /** the property sheet that is used to display nodes */
    private PropertySheet propertySheet;
    /** the nodes that are displayed in the property sheet */
    private Node[] nodes = new Node[0];

    /** Constructor for new sheet.
    * The sheet does not listen to global changes */
    public NbSheet () {
        this (false);
    }

    /** @param global should the content change when global properties changes?
    */
    public NbSheet (boolean global) {
        this.global = global;
        this.propertySheet = new PropertySheet ();

        // Instructs winsys to name this mode as single if only property sheet
        // is docked in this mode
        // it's workaround, should be solved throgh some Mode API in future
        // # 16888. Properties sheet is in single mode in SDI only.
//            putClientProperty(ModeImpl.NAMING_TYPE, ModeImpl.SDI_ONLY_COMP_NAME); // TEMP
        //Bugfix #36087: Fix naming type
        putClientProperty("NamingType", "BothOnlyCompName"); // NOI18N

        setLayout (new BorderLayout ());
        add(propertySheet, BorderLayout.CENTER);

        setIcon (ImageUtilities.loadImage("org/netbeans/core/windows/resources/properties.gif", true)); // NOI18N

        // #36738 Component has to have a name from begining.
        updateTitle();
        // XXX - please rewrite to regular API when available - see issue #55955
        putClientProperty("SlidingName", NbBundle.getMessage(NbSheet.class, "CTL_PropertiesWindow")); //NOI18N 

        // name listener and node listener
        listener = new Listener ();

        snListener = new SheetNodesListener();

        // set accessiblle description
        getAccessibleContext ().setAccessibleName (
            NbBundle.getBundle(NbSheet.class).getString ("ACSN_PropertiesSheet"));
        getAccessibleContext ().setAccessibleDescription (
            NbBundle.getBundle(NbSheet.class).getString ("ACSD_PropertiesSheet"));
        setActivatedNodes(null);
    }
    
    /* Singleton accessor. As NbSheet is persistent singleton this
     * accessor makes sure that NbSheet is deserialized by window system.
     * Uses known unique TopComponent ID "properties" to get NbSheet instance
     * from window system. "properties" is name of settings file defined in module layer.
     */
    public static NbSheet findDefault () {
        if (sharedSheet == null) {
            TopComponent tc = WindowManager.getDefault().findTopComponent("properties"); // NOI18N
            if (tc != null) {
                if (tc instanceof NbSheet) {
                    sharedSheet = (NbSheet) tc;
                } else {
                    //Incorrect settings file?
                    IllegalStateException exc = new IllegalStateException
                    ("Incorrect settings file. Unexpected class returned." // NOI18N
                    + " Expected:" + NbSheet.class.getName() // NOI18N
                    + " Returned:" + tc.getClass().getName()); // NOI18N
                    Logger.getLogger(NbSheet.class.getName()).log(Level.WARNING, null, exc);
                    //Fallback to accessor reserved for window system.
                    NbSheet.getDefault();
                }
            } else {
                //NbSheet cannot be deserialized
                //Fallback to accessor reserved for window system.
                NbSheet.getDefault();
            }
        }
        return sharedSheet;
    }
    
    @Override
    protected String preferredID () {
        return "properties"; //NOI18N
    }
    
    /* Singleton accessor reserved for window system ONLY. Used by window system to create
     * NbSheet instance from settings file when method is given. Use <code>findDefault</code>
     * to get correctly deserialized instance of NbSheet. */
    public static NbSheet getDefault () {
        if (sharedSheet == null) {
            sharedSheet = new NbSheet(true);
        }
        return sharedSheet;
    }

    @Override
    public String getShortName() {
        return NbBundle.getMessage(NbSheet.class, "CTL_PropertiesWindow"); //NOI18N
    }
    
    /** Overriden to explicitely set persistence type of NbSheet
     * to PERSISTENCE_ALWAYS */
    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }
    
    @Override
    public HelpCtx getHelpCtx () {
        // #40372 fix - for non-global properties display (assumed to be in a dialog), don't show the help button
        return (global ? org.openide.explorer.ExplorerUtils.getHelpCtx (nodes, new HelpCtx (NbSheet.class)) : null);
    }

    /** Transfer the focus to the property sheet.
     */
    @Deprecated
    @Override
    public void requestFocus () {
        super.requestFocus();
        propertySheet.requestFocus();
    }
    
    /** Transfer the focus to the property sheet.
     */
    @Deprecated
    @Override
    public boolean requestFocusInWindow () {
        super.requestFocusInWindow();
        return propertySheet.requestFocusInWindow();
    }

    @Override
    public void open () {
        super.open();

        if(global) {
            // Set the nodes when opening.
            SwingUtilities.invokeLater(listener);
        }
    }

    /** cache the title formatters, they are used frequently and are slow to construct */
    private static MessageFormat globalPropertiesFormat = null;
    private static MessageFormat localPropertiesFormat = null;

    /** Changes name of the component to reflect currently displayed nodes.
    * Called when set of displayed nodes has changed.
    */
    protected void updateTitle () {
        // different naming for global and local sheets
        Mode ourMode = WindowManager.getDefault().findMode(this);
        String nodeTitle =  null;

        // Fix a bug #12890, copy the nodes to prevent race condition.
        List<Node> copyNodes = new ArrayList<Node>(Arrays.asList(nodes));

        Node node = null;

        if (!copyNodes.isEmpty()) {
            node = copyNodes.get(0);
        }

        if(node == null) {
            nodeTitle = "";  // NOI18N
        } else {
            nodeTitle = node.getDisplayName();
            Object alternativeDisplayName = node.getValue(PROP_LONGER_DISPLAY_NAME);
            if (alternativeDisplayName instanceof String) {
                nodeTitle = (String)alternativeDisplayName;
            }
        }
        Object[] titleParams = new Object[] {
            Integer.valueOf(copyNodes.size()),
            nodeTitle
        };
        // different naming if docked in properties mode
        if ((ourMode != null) && 
            ("properties".equals(ourMode.getName()))) { // NOI18N
            if (globalPropertiesFormat == null) {
                globalPropertiesFormat = new MessageFormat(NbBundle.getMessage(NbSheet.class, "CTL_FMT_GlobalProperties"));
            }
            setName(globalPropertiesFormat.format(titleParams));
        } else {
            if (localPropertiesFormat == null) {
                localPropertiesFormat = new MessageFormat(NbBundle.getMessage(NbSheet.class, "CTL_FMT_LocalProperties"));
            }
            setName(localPropertiesFormat.format(titleParams));
        }
        setToolTipText(getName());
    }

    /** Nodes to display.
    */
    public void setNodes (Node[] nodes) {
        setNodes(nodes, true, "setNodes"); // NOI18N
    }
    final void setNodes(Node[] nodes, boolean reattach, String from) {
        LOG.log(
            Level.FINE, "setNodes({0}, {1}, {2})",
            new Object[] { Arrays.asList(nodes), reattach, from }
        );
        this.nodes = nodes;
        propertySheet.setNodes(nodes);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                updateTitle();
            }
        });
        if (reattach) {
            // re-attach to listen to new nodes
            snListener.detach();
            snListener.attach(nodes);
        }
        LOG.log(
            Level.FINE, "finished setNodes({0}, {1}, {2})",
            new Object[]{Arrays.asList(nodes), reattach, from}
        );
    }
    
    final Node[] getNodes() {
        return nodes;
    }

    /** Serialize this property sheet */
    @Override
    public void writeExternal (ObjectOutput out)
    throws IOException {
        super.writeExternal(out);

        if (global) {
            // write dummy array
            out.writeObject (null);
        } else {
            Node.Handle[] arr = NodeOp.toHandles (nodes);
            out.writeObject(arr);
        }

        out.writeBoolean(global);
    }

    /** Deserialize this property sheet. */
    @Override
    public void readExternal (ObjectInput in)
    throws IOException, ClassNotFoundException {
        try {
            super.readExternal(in);
        } catch (SafeException se) {
            // ignore--we really do not care about the explorer manager that much
            //System.err.println("ignoring a SafeException: " + se.getLocalizedMessage ());
        }
        Object obj = in.readObject ();

        if (obj instanceof NbMarshalledObject || obj instanceof ExplorerManager) {
            // old version read the Boolean
            global = ((Boolean)in.readObject()).booleanValue();
        } else {
            Node[] ns;

            if (obj == null) {
                // handles can also be null for global 
                // property sheet
                ns = TopComponent.getRegistry().getActivatedNodes();
            } else {
                // new version, first read the nodes and then the global boolean
                Node.Handle[] arr = (Node.Handle[])obj;

                try {
                    ns = NodeOp.fromHandles (arr);
                } catch (IOException ex) {
                    Exceptions.attachLocalizedMessage(ex,
                                                      NbBundle.getBundle(NbSheet.class).getString("EXC_CannotLoadNodes"));
                    Logger.getLogger(NbSheet.class.getName()).log(Level.WARNING, null, ex);
                    ns = new Node[0];
                }
            }

            global = in.readBoolean ();

            setNodes(ns, true, "readExternal"); // NOI18N
        }

        /*
              if (obj instanceof Boolean) {
                global = (Boolean)in.readObject ()

              global = ((Boolean)in.readObject()).booleanValue();
        /*
              // start global listening if needed, but wait until
              // deserialization is done (ExplorerManager is uses
              // post-deserialization validating too, so we are forced
              // to use it)
              ((ObjectInputStream)in).registerValidation(
                new ObjectInputValidation () {
                  public void validateObject () {
                    updateGlobalListening(false);
                  }
                }, 0
              );
        */
        // JST: I guess we are not and moreover the type casting is really ugly
        //      updateGlobalListening (global);
    }

    /** Resolve to singleton instance, if needed. */
    public Object readResolve ()
    throws ObjectStreamException {
        if (global) {
            return getDefault();
        } else {
            if ((nodes == null) || (nodes.length <= 0)) {
                return null;
            }
        }
        return this;
    }

    @Override
    protected Object writeReplace() throws ObjectStreamException {
        if (global) {
            return new Replacer();
        } else {
            return super.writeReplace();
        }
    }

    private static final class Replacer implements Serializable {
        static final long serialVersionUID=-7897067133215740572L;
        Replacer() {}
        private Object readResolve() throws ObjectStreamException {
            return NbSheet.getDefault();
        }
    }

    /** Helper, listener variable must be initialized before
    * calling this */
    private void updateGlobalListening(boolean listen) {
        if (global) {
            if (listen) {
                TopComponent.getRegistry().addPropertyChangeListener(
                    listener);
            } else {
                TopComponent.getRegistry().removePropertyChangeListener (listener);
            }
        }
    }
    
    @Override
    protected void componentOpened() {
        updateGlobalListening (true);
    }
    
    @Override
    protected void componentClosed() {
        updateGlobalListening (false);
        setNodes(new Node[0], true, "componentClosed"); // NOI18N
    }
    
    @Override
    protected void componentDeactivated() {
        super.componentDeactivated();
        if (Utilities.isMac()) {
            propertySheet.firePropertyChange("MACOSX", true, false);
        }
    }
    
    /** Change listener to changes in selected nodes. And also
    * nodes listener to listen to global changes of the nodes.
    */
    private class Listener extends Object implements Runnable, PropertyChangeListener {
        Listener() {}
        public void propertyChange (PropertyChangeEvent ev) {
            if (TopComponent.Registry.PROP_ACTIVATED_NODES.equals( ev.getPropertyName() )) {
                activate();
            }
            /*
            if ((ev.getPropertyName().equals(TopComponent.Registry.PROP_ACTIVATED)) && 
                (ev.getNewValue() == Sheet.this)) {
                return; // we do not want to call setNodes if we are
                        // the activated window
            }
            activate ();
             */
        }

        public void run() {
            activate();
        }

        public void activate () {
            TopComponent tc = TopComponent.getRegistry().getActivated();
            Node[] arr = TopComponent.getRegistry ().getActivatedNodes();
            LOG.log(Level.FINE, "Active component {0}", tc);
            setNodes (arr, true, "activate"); // NOI18N
        }

    }
    /** Change listener to changes in selected nodes. And also
    * nodes listener to listen to global changes of the nodes.
    */
    private class SheetNodesListener extends NodeAdapter implements Runnable {

        /* maps nodes to their listeners (Node, WeakListener) */
        private HashMap<Node,NodeListener> listenerMap;

        /* maps nodes to their proeprty change listeners (Node, WeakListener)*/
        private HashMap<Node,PropertyChangeListener> pListenerMap;

        SheetNodesListener() {}

        /** Fired when the node is deleted.
         * @param ev event describing the node
         */
        @Override
        public void nodeDestroyed(final NodeEvent ev) {
            Mutex.EVENT.readAccess(new Runnable() {
                @Override
                public void run() {
                    handleNodeDestroyed(ev);
                }
            });
        }

        final void handleNodeDestroyed(NodeEvent ev) {
            assert EventQueue.isDispatchThread();
            
            Node destroyedNode = ev.getNode();
            NodeListener listener = listenerMap.get(destroyedNode);
            PropertyChangeListener pListener = pListenerMap.get(destroyedNode);
            // stop to listen to destroyed node
            destroyedNode.removeNodeListener(listener);
            destroyedNode.removePropertyChangeListener(pListener);
            listenerMap.remove(destroyedNode);
            pListenerMap.remove(destroyedNode);
            // close top component (our outer class) if last node was destroyed
            if (listenerMap.isEmpty() && !global) {
                //fix #39251 start - posting the closing of TC to awtevent thread
                close();
                //fix #39251 end
            } else {
                setNodes(
                    (listenerMap.keySet().toArray(new Node[listenerMap.size()])), false, "handleNodeDestroyed" // NOI18N
                );
            }
        }

        public void attach (Node[] nodes) {
            assert EventQueue.isDispatchThread();
            listenerMap = new HashMap<Node,NodeListener>(nodes.length * 2);
            pListenerMap = new HashMap<Node,PropertyChangeListener>(nodes.length * 2);
            NodeListener curListener = null;
            PropertyChangeListener pListener = null;
            // start to listen to all given nodes and map nodes to
            // their listeners
            for (Node n : nodes) {
                curListener = org.openide.nodes.NodeOp.weakNodeListener(this, n);
                pListener = org.openide.util.WeakListeners.propertyChange(this, n);
                listenerMap.put(n, curListener);
                pListenerMap.put(n, pListener);
                n.addNodeListener(curListener);
                n.addPropertyChangeListener(pListener);
            }
        }

        public void detach () {
            assert EventQueue.isDispatchThread();
            if (listenerMap == null) {
                return;
            }
            // stop to listen to all nodes
            for (Iterator iter = listenerMap.entrySet().iterator(); iter.hasNext(); ) {
                Map.Entry curEntry = (Map.Entry)iter.next();
                ((Node)curEntry.getKey()).removeNodeListener((NodeListener)curEntry.getValue());
            }
            for (Iterator iter = pListenerMap.entrySet().iterator(); iter.hasNext(); ) {
                Map.Entry curEntry = (Map.Entry)iter.next();
                ((Node)curEntry.getKey()).removePropertyChangeListener((PropertyChangeListener)curEntry.getValue());
            }
            // destroy the map
            listenerMap = null;
            pListenerMap = null;
        }

        @Override
        public void propertyChange(PropertyChangeEvent pce) {
            if (Node.PROP_DISPLAY_NAME.equals(pce.getPropertyName())) {
                SwingUtilities.invokeLater(this);
            }
        }

        @Override
        public void run() {
            assert EventQueue.isDispatchThread();
            updateTitle();
        }

    } // End of SheetNodesListener.

}
