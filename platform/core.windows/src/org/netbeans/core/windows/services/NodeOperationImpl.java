/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.core.windows.services;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.FocusTraversalPolicy;
import java.awt.Window;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;
import java.util.WeakHashMap;
import java.util.HashSet;
import java.util.Set;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.text.DefaultEditorKit;
import org.netbeans.core.windows.view.ui.NbSheet;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.Node;
import org.openide.nodes.NodeAcceptor;
import org.openide.nodes.NodeAdapter;
import org.openide.nodes.NodeEvent;
import org.openide.nodes.NodeOperation;
import org.openide.util.HelpCtx;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.UserCancelException;
import org.openide.util.Utilities;
import org.openide.util.WeakSet;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/** Class that provides operations on nodes. Any part of system can
 * ask for opening a customizer or explorer on any node. These actions
 * are accessible thru this class.
 *
 * @author  Ian Formanek
 */
@ServiceProvider(service=NodeOperation.class, supersedes="org.netbeans.modules.openide.explorer.NodeOperationImpl")
public final class NodeOperationImpl extends NodeOperation {

    /** Shows an explorer on the given root Node.
    * @param n the Node that will be the rootContext of the explored hierarchy
    */
    public void explore (final Node n) {
        Mutex.EVENT.readAccess (new Runnable () {
                public void run () {
                    TopComponent et = new ExplorerPanel(n);

                    Mode target = WindowManager.getDefault().findMode("explorer");
                    if (target != null) {
                        target.dockInto(et);
                    }
                    et.open();
                    et.requestActive();
                }
            });
    }

    private static class ExplorerPanel extends TopComponent implements ExplorerManager.Provider {
        private ExplorerManager manager = new ExplorerManager();
        public ExplorerPanel(Node n) {
            manager = new ExplorerManager();
            manager.setRootContext(n);
            ActionMap map = getActionMap();
            map.put(DefaultEditorKit.copyAction, ExplorerUtils.actionCopy(manager));
            map.put(DefaultEditorKit.cutAction, ExplorerUtils.actionCut(manager));
            map.put(DefaultEditorKit.pasteAction, ExplorerUtils.actionPaste(manager));
            map.put("delete", ExplorerUtils.actionDelete(manager, true));
            associateLookup(ExplorerUtils.createLookup (manager, map));
            setLayout(new BorderLayout());
            add(new BeanTreeView());
            setName(n.getDisplayName());
        }
        public ExplorerManager getExplorerManager() {
            return manager;
        }
        public @Override void addNotify() {
            super.addNotify();
            ExplorerUtils.activateActions(manager, true);
        }
        public @Override void removeNotify() {
            ExplorerUtils.activateActions(manager, false);
            super.removeNotify();
        }
        public @Override int getPersistenceType() {
            return TopComponent.PERSISTENCE_NEVER;
        }
    }

    /** Tries to open customization for specified node. The dialog is
    * open in modal mode and the function returns after successful
    * customization.
    *
    * @param n the node to customize
    * @return <CODE>true</CODE> if the node has customizer,
    * <CODE>false</CODE> if not
    */
    public boolean customize (Node n) {
        final Component customizer = n.getCustomizer ();
        if (customizer == null) return false;
        return Mutex.EVENT.readAccess (new Mutex.Action<Boolean> () {
                public Boolean run () {
                    if (customizer instanceof NbPresenter) { // #9466
                        ((NbPresenter) customizer).pack ();
                        ((NbPresenter) customizer).show ();
                        return Boolean.TRUE;
                    }
                    if (customizer instanceof Window) {
                        ((Window) customizer).pack ();
                        customizer.setVisible (true);
                        return Boolean.TRUE;
                    }
                    
                    // preserve help context and explorer provider of customizer
                    JPanel p = null;
                    if (customizer instanceof ExplorerManager.Provider) {
                        p = new ExplorerProviderFwd(customizer, (ExplorerManager.Provider)customizer);
                    } else {
                        p = new HelpFwdPanel(customizer);
                    }
                    p.setLayout(new BorderLayout());
                    p.getAccessibleContext().setAccessibleDescription(
                        NbBundle.getMessage(NodeOperationImpl.class, "CTL_Customizer_dialog_title"));
                    
                    // #21547 adjust for XML that relies on container managed borders
                    // please DELETE after #19821 is fixed, immediatelly
                    if (customizer.getClass().getName().startsWith("org.netbeans.modules.xml.catalog")) {  // NOI18N
                        p.setBorder(BorderFactory.createEmptyBorder(12, 12, 0, 11));
                    }
                    // end of future DELETE
                    
                    p.add(customizer, BorderLayout.CENTER);
                    
                    // present it
                    DialogDescriptor dd = new DialogDescriptor
                        (p, 
                         NbBundle.getMessage(NodeOperationImpl.class, "CTL_Customizer_dialog_title"));
                    dd.setOptions(new Object[] { DialogDescriptor.CLOSED_OPTION });

                    Dialog dialog = org.openide.DialogDisplayer.getDefault ().createDialog(dd);
                    dialog.pack();
                    dialog.setVisible(true);
                    return Boolean.TRUE;
                }
            }).booleanValue ();
    }

    /** Panel, decorates given inner panel with forwarding of help context.
     * It will probably also decorate customizer with border (when #19821 is fixed)
     */
     private static class HelpFwdPanel extends JPanel implements HelpCtx.Provider {
        private Component innerComp;
        private boolean active = false;
        
        /** Not instantiatable outside */
        private HelpFwdPanel (Component innerComp) {
            this.innerComp = innerComp;
        }
        
        public HelpCtx getHelpCtx () {
            try {
                //??? eliminate recursion it delegates to parent (this)
                if (active) return null;
                active = true;
                return HelpCtx.findHelp(innerComp);  
            } finally  {
                active = false;
            }
        }
                                                   
    } // end of HelpFwdPanel
    
    /** Decorates given panel with explorer provider functionality, forwarding
     * to given original provider. */
    private static final class ExplorerProviderFwd extends HelpFwdPanel
                implements ExplorerManager.Provider {
        private ExplorerManager.Provider explProvider;
        
        /** Not instantiatable outside */
        private ExplorerProviderFwd (Component innerComp, ExplorerManager.Provider explProvider) {
            super(innerComp);
            this.explProvider = explProvider;
        }
                                                      
        /** Forwards to original explorer provider.
         */
        public ExplorerManager getExplorerManager() {
            return explProvider.getExplorerManager();
        }
        
     } // end of CustomizerDecorator

    /** Opens a modal propertySheet on given Node
    * @param n the node to show properties for
    */
    public void showProperties (Node n) {
        Dialog d = findCachedPropertiesDialog( n );
        if( null == d ) {
            Node[] nds = new Node[] { n };
            openProperties(new NbSheet(), nds);
        } else {
            d.setVisible( true );
            //#131724 - PropertySheet clears its Nodes in removeNotify and keeps
            //only a weakref which is being reused in subsequent addNotify
            //so we should set the Nodes again in case the weakref got garbage collected
            //pls note that PropertySheet code checks for redundant calls of setNodes
            NbSheet sheet = findCachedSheet( d );
            if( null != sheet )
                sheet.setNodes(new Node[] { n });
            d.toFront();
            FocusTraversalPolicy ftp = d.getFocusTraversalPolicy();
            if( null != ftp && null != ftp.getDefaultComponent(d) ) {
                ftp.getDefaultComponent(d).requestFocusInWindow();
            } else {
                d.requestFocusInWindow();
            }
        }
    }

    /** Opens a modal propertySheet on given set of Nodes
    * @param n the array of nodes to show properties for
    */
    public void showProperties (Node[] nodes) {
        Dialog d = findCachedPropertiesDialog( nodes );
        if( null == d ) {
            openProperties(new NbSheet(), nodes);
        } else {
            d.setVisible( true );
            //#131724 - PropertySheet clears its Nodes in removeNotify and keeps
            //only a weakref which is being reused in subsequent addNotify
            //so we should set the Nodes again in case the weakref got garbage collected
            //pls note that PropertySheet code checks for redundant calls of setNodes
            NbSheet sheet = findCachedSheet( d );
            if( null != sheet )
                sheet.setNodes(nodes);
            d.toFront();
            FocusTraversalPolicy ftp = d.getFocusTraversalPolicy();
            if( null != ftp && null != ftp.getDefaultComponent(d) ) {
                ftp.getDefaultComponent(d).requestFocusInWindow();
            } else {
                d.requestFocusInWindow();
            }
        }
    }
    
    private NbSheet findCachedSheet( Container c ) {
        NbSheet res = null;
        int childrenCount = c.getComponentCount();
        for( int i=0; i<childrenCount && res == null; i++ ) {
            Component child = c.getComponent(i);
            if( child instanceof NbSheet ) {
                res = (NbSheet)child;
            } else if( child instanceof Container ) {
                res = findCachedSheet((Container)child);
            } 
        }
        return res;
    }
    
    //#79126 - cache the open properties windows and reuse them if the Nodes 
    //are the same
    private static WeakSet<Node[]> nodeCache = new WeakSet<Node[]>();
    private static WeakHashMap<Node[], Dialog> dialogCache = new WeakHashMap<Node[], Dialog>();
    
    private static Dialog findCachedPropertiesDialog( Node n ) {
        return findCachedPropertiesDialog( new Node[] { n } );
    }
    
    private static Dialog findCachedPropertiesDialog( Node[] nodes ) {
        for( Iterator<Node[]> it=nodeCache.iterator(); it.hasNext(); ) {
            Node[] cached = it.next();
            if( cached.length != nodes.length )
                continue;
            boolean match = true;
            for( int i=0; i<cached.length; i++ ) {
                if( !cached[i].equals( nodes[i] ) ) {
                    match = false;
                    break;
                }
            }
            if( match ) {
                return dialogCache.get( cached );
            }
        }
        return null;
    }

    /** Opens explorer for specified root in modal mode. The set
    * of selected components is returned as a result. The acceptor
    * should be asked each time selected nodes changes to accept or
    * reject the current result. This should affect for example the
    * <EM>OK</EM> button.
    *
    * @param title is a title that will be displayed as a title of the window
    * @param root the root to explore
    * @param acceptor the class that is asked for accepting or rejecting
    *    current selection
    * @param top is a component that will be displayed on the top
    * @return array of selected (and accepted) nodes
    *
    * @exception UserCancelException selection interrupted by user
    */
    public Node[] select (String title, String rootTitle, Node root, NodeAcceptor acceptor, Component top)
    throws UserCancelException {
        final FileSelector selector = new FileSelector(rootTitle, root, acceptor, top);
        selector.setBorder(new EmptyBorder(12, 12, 0, 12));
        DialogDescriptor dd = new DialogDescriptor(selector, title, true, 
                                                   selector.getOptions(), 
                                                   selector.getSelectOption(), DialogDescriptor.DEFAULT_ALIGN,
                                                   HelpCtx.DEFAULT_HELP, null);
        Object ret = DialogDisplayer.getDefault().notify(dd);
        if (ret != selector.getSelectOption()) {
            throw new UserCancelException ();
        }
        return selector.getNodes ();
    }

    /** Helper method, opens properties top component in single mode
    * and requests a focus for it */
    private static void openProperties (final NbSheet sheet, final Node[] nds) {
        // XXX #36492 in NbSheet the name is set asynch from setNodes.
//        Mutex.EVENT.readAccess (new Runnable () { // PENDING
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
                public void run () {
                    boolean modal = Utilities.isModalDialogOpen();
                    
                    Dialog dlg = org.openide.DialogDisplayer.getDefault().createDialog(new DialogDescriptor (
                        sheet,
                        sheet.getName(),
                        modal,
                        new Object [] {DialogDescriptor.CLOSED_OPTION},
                        DialogDescriptor.CLOSED_OPTION,
                        DialogDescriptor.BOTTOM_ALIGN,
                        new HelpCtx( "org.netbeans.core.windows.view.ui.NbSheet" ), //NOI18N
                        null
                    ));
                    sheet.setNodes(nds);
                    //fix for issue #40323
                    SheetNodesListener listener = new SheetNodesListener(dlg, sheet);
                    listener.attach(nds);
                    
                    nodeCache.add( nds );
                    dialogCache.put( nds, dlg );
                    
                    dlg.setVisible(true);
                }
            });
    }

    /**
     * fix for issue #40323 the prop dialog needs to be closed when the nodes it displayes are destroyed.
     */
    private static class SheetNodesListener extends NodeAdapter implements PropertyChangeListener {

        private Dialog dialog;
        private Set<Node> listenerSet;
        /** top component we listen to for name changes */ 
        private TopComponent tc;
        
        SheetNodesListener(Dialog dialog, TopComponent tc) {
            this.dialog = dialog;
            this.tc = tc;
            tc.addPropertyChangeListener(this);
        }
        
        @Override
        public void propertyChange (PropertyChangeEvent pce) {
            if ("name".equals(pce.getPropertyName())) {
                dialog.setTitle((String) pce.getNewValue());
            }
        }
        
        public void attach(Node[] nodes) {
            listenerSet = new HashSet<Node>(nodes.length * 2);
            for (int i = 0; i < nodes.length; i++) {
                listenerSet.add(nodes[i]);
                nodes[i].addNodeListener(this);
            }
        }

        /** Fired when the node is deleted.
         * @param ev event describing the node
         */
        @Override
        public void nodeDestroyed(NodeEvent ev) {
            Node destroyedNode = ev.getNode();
            // stop to listen to destroyed node
            destroyedNode.removeNodeListener(this);
            listenerSet.remove(destroyedNode);
            // close top component (our outer class) if last node was destroyed
            if (listenerSet.isEmpty()) {
                // #68943 - stop to listen, as we are waving goodbye :-)
                tc.removePropertyChangeListener(this);
                Mutex.EVENT.readAccess(new Runnable() {
                    @Override
                    public void run() {
                        if (dialog != null) {
                            dialog.setVisible(false);
                            dialog.dispose();
                            dialog = null;
                        }
                    }
                });
            }
        }
    }
    
}
