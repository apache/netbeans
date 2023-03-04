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

package org.netbeans.modules.options.classic;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.table.JTableHeader;
import org.netbeans.core.startup.layers.SessionManager;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.cookies.InstanceCookie;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.propertysheet.PropertySheetView;
import org.openide.explorer.view.NodeTableModel;
import org.openide.explorer.view.TreeTableView;
import org.openide.explorer.view.TreeView;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataShadow;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.util.HelpCtx;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.util.actions.CallableSystemAction;

/** Action that opens explorer view which displays global
* options of the IDE.
 *
 * @author Dafe Simonek
 */
public class OptionsAction extends CallableSystemAction {

    public OptionsAction() {
        putValue("noIconInMenu", Boolean.TRUE); //NOI18N
    }
    
    private static final String HELP_ID = "org.netbeans.core.actions.OptionsAction"; // NOI18N 
    
    /** Weak reference to the dialog showing singleton options. */
    private Reference<Dialog> dialogWRef = new WeakReference<Dialog>(null);
    

    public void performAction () {
        final OptionsPanel optionPanel = OptionsPanel.singleton ();
        
        Mutex.EVENT.readAccess(new Runnable() {
            public void run() {
                Dialog dialog = dialogWRef.get();

                if(dialog == null || !dialog.isShowing()) {
                    JButton closeButton = new JButton();
                    Mnemonics.setLocalizedText(closeButton, NbBundle.getMessage(OptionsAction.class, "CTL_close_button"));
                    closeButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(OptionsAction.class, "ACSD_close_button"));
                    String title = (String) OptionsAction.this.getValue 
                        ("optionsDialogTitle");
                    DialogDescriptor dd = new DialogDescriptor(
                        InitPanel.getDefault(optionPanel),
                        title == null ? optionPanel.getName() : title,
                        false,
                        new Object[] {closeButton},
                        closeButton,
                        DialogDescriptor.DEFAULT_ALIGN,
                        getHelpCtx (),
                        null);
                    
                    // HACK: switch back to new options dialog hack
                    String name = (String) OptionsAction.this.getValue 
                        ("additionalActionName");
                    if (name != null) {
                        ActionListener actionListener = (ActionListener) 
                            OptionsAction.this.getValue ("additionalActionListener");
                        JButton additionalButton = new JButton ();
                        Mnemonics.setLocalizedText (additionalButton, name);
                        additionalButton.addActionListener (new ActionListener () {
                            public void actionPerformed (ActionEvent e) {
                                Dialog dialog = dialogWRef.get ();
                                dialog.setVisible (false);
                            }
                        });
                        additionalButton.addActionListener (actionListener);
                        dd.setAdditionalOptions (new Object[] {additionalButton});
                    }
                    // end of HACK

                    // #37673
                    optionPanel.setDialogDescriptor(dd);
                        
                    dialog = DialogDisplayer.getDefault().createDialog(dd);
                    dialog.setVisible(true);
                    dialogWRef = new WeakReference<Dialog>(dialog);
                } else {
                    dialog.toFront();
                }
            }
        }); // EQ.iL
    }
    
    protected boolean asynchronous() {
        return false;
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx (HELP_ID);
    }

    public String getName() {
        return NbBundle.getBundle(OptionsAction.class).getString("Options");
    }

    /** Options panel. Uses singleton pattern. */
    public static final class OptionsPanel extends NbMainExplorer.ExplorerTab
    implements PropertyChangeListener {
        /** Name of mode in which options panel is docked by default */
        public static final String MODE_NAME = "options";
        /** Singleton instance of options panel */
        private static OptionsPanel singleton;
        
        private static String TEMPLATES_DISPLAY_NAME = NbBundle.getBundle(OptionsAction.class).getString("CTL_Templates_name"); // NOI18N
        
        /** list of nodes that should be expanded when the tree is shown */
        private Collection<Node> toExpand;
        private transient boolean expanded;
        /** root node to use */
        private transient Node rootNode;
        
        // XXX #37673
        private transient Reference<DialogDescriptor> descriptorRef = new WeakReference<DialogDescriptor>(null);
        

        private OptionsPanel () {
            validateRootContext ();
            
            getExplorerManager().addPropertyChangeListener(this);
        }
        
        protected String preferredID () {
            return "options"; //NOI18N
        }
        
        // #37673 It was requested to update helpCtx according to node selection in explorer.
        public void propertyChange(PropertyChangeEvent evt) {
            if(ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
                DialogDescriptor dd = descriptorRef.get();
                if(dd != null) {
                    dd.setHelpCtx(getHelpCtx());
                }
            }
        }
        // #37673
        public void setDialogDescriptor(DialogDescriptor dd) {
            descriptorRef = new WeakReference<DialogDescriptor>(dd);
        }
        
        public HelpCtx getHelpCtx () {
            HelpCtx defaultHelp = new HelpCtx (HELP_ID);
            HelpCtx help = org.openide.explorer.ExplorerUtils.getHelpCtx (
                getExplorerManager ().getSelectedNodes (),
                defaultHelp
            );
            // bugfix #23551, add help id to subnodes of Templates category
            // this check prevents mixed help ids on more selected nodes
            if (!defaultHelp.equals (help)) {
                // try if selected node isn't template
                Node node = getExplorerManager ().getSelectedNodes ()[0];
                HelpCtx readHelpId = getHelpId (node);
                if (readHelpId != null) return readHelpId;
                
                // next bugfix #23551, children have same helpId as parent if no specific is declared
                while (node != null && !TEMPLATES_DISPLAY_NAME.equals (node.getDisplayName ())) {
                    readHelpId = getHelpId (node);
                    if (readHelpId != null) return readHelpId;
                    node = node.getParentNode ();
                }
                if (node != null && TEMPLATES_DISPLAY_NAME.equals (node.getDisplayName ())) {
                    return new HelpCtx ("org.netbeans.core.actions.OptionsAction$TemplatesSubnode"); // NOI18N
                }
            }
            return help;
        }
        
        private HelpCtx getHelpId (Node node) {
            // it's template, return specific help id
            DataObject dataObj = (DataObject)node.getCookie (DataObject.class);
            if (dataObj != null) {
                Object o = dataObj.getPrimaryFile ().getAttribute ("helpID"); // NOI18N
                if (o != null) {
                    return new HelpCtx (o.toString ());
                }
            }
            return null;
        }

        /** Accessor to the singleton instance */
        static OptionsPanel singleton () {
            if (singleton == null) {
                singleton = new OptionsPanel();
            }
            return singleton;
        }
        
        private transient JSplitPane split=null;
        protected TreeView initGui () {
            TTW retVal = new TTW () ;
            
            
            split = new JSplitPane (JSplitPane.HORIZONTAL_SPLIT);
            PropertySheetView propertyView = new PropertySheetView();
            
            split.setLeftComponent(retVal);
            split.setRightComponent(propertyView);
            // install proper border for split pane
            split.setBorder((Border)UIManager.get("Nb.ScrollPane.border")); // NOI18N

            setLayout (new java.awt.GridBagLayout ());

            GridBagConstraints gridBagConstraints = new GridBagConstraints ();
            gridBagConstraints.fill = GridBagConstraints.BOTH;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.weighty = 1.0;
            gridBagConstraints.gridwidth = 2;
            add (split, gridBagConstraints);

            return retVal;
        }
        
        /** Overridden to provide a larger preferred size if the default font
         *  is larger, for locales that require this.   */
        public Dimension getPreferredSize() {
            //issue 34104, bad sizing/split location for Chinese locales that require
            //a larger default font size
            Dimension result = super.getPreferredSize();
            Font treeFont = UIManager.getFont("Tree.font"); // NOI18N
            int fontsize = treeFont != null ? treeFont.getSize() : 11;
            if (fontsize > 11) {
                int factor = fontsize - 11;
                result.height += 15 * factor;
                result.width += 50 * factor;
                Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
                if (result.height > screen.height) {
                    result.height = screen.height -30;
                }
                if (result.width > screen.width) {
                    result.width = screen.width -30;
                }
            } else {
                result.width += 20;
                result.height +=20;
            }
            
            return result;
        }

        /** Called when the explored context changes.
        * Overriden - we don't want title to chnage in this style.
        */
        protected void updateTitle () {
            // empty to keep the title unchanged
        }

        boolean isPrepared () {
            return toExpand != null;
        }
        
        public void prepareNodes() {
            if (toExpand == null) {                        
                List<Node> arr = new ArrayList<Node> (101);
                expandNodes(getRootContext (), 2, arr);               
                toExpand = arr;
            }
        }


        public @Override void addNotify() {
            super.addNotify();
            if (!expanded) {
                ((TTW)view).expandTheseNodes (toExpand, getExplorerManager ().getRootContext ());                
                expanded = true;
            }
            // initialize divider location
            split.setDividerLocation(getPreferredSize().width / 2);
        }
        

        protected void validateRootContext () {
            Node n = initRC ();
            setRootContext (n);
        }
        
        private synchronized Node initRC () {
            if (rootNode == null) {
                rootNode = new OptionsFilterNode ();
            }
            return rootNode;
        }

        /** Expands the node in explorer.
         */
        private static void expandNodes (Node n, final int depth, final Collection<Node> list) {
            if (depth == 0) {
                return;
            }
            
            DataObject obj = (DataObject)n.getCookie(DataObject.class);
            if (obj instanceof DataShadow) {
                obj = ((DataShadow)obj).getOriginal();
            }
            
            if (obj != null) {
                if (!obj.getPrimaryFile().getPath().startsWith ("UI/Services")) { // NOI18N
                    return;
                }

                InstanceCookie ic = (InstanceCookie)obj.getCookie (InstanceCookie.class);
                if (ic != null) {

                    if (ic instanceof InstanceCookie.Of) {
                        if (((InstanceCookie.Of)ic).instanceOf (Node.class)) {
                            return;
                        }
                    } 
                }
            }
            
            // ok, expand this node
            if (!list.contains (n)) {
                list.add (n);
            }
         
            Node[] arr = n.getChildren().getNodes(true);
            for (int i = 0; i < arr.length; i++) {
                final Node p = arr[i];
                expandNodes(p, depth - 1, list);
            }
        }
        
        //
        // Model to implement the special handling of SettingChildren.* properties
        //
        
        /** Model that tries to extract properties from the node.getValue 
         * instead of creating its getPropertySets.
         */
        private static class NTM extends NodeTableModel {
            public NTM () {
                super ();
            }
            
            protected Node.Property getPropertyFor(Node node, Node.Property prop) {
                Object value = node.getValue (prop.getName());
                if (value instanceof Node.Property) {
                    return (Node.Property)value;
                }
                
                return null;
            }
        }

        private static class TTW extends TreeTableView implements MouseListener, PropertyChangeListener, java.awt.event.ActionListener {
            /** Dummy property that can be expanded or collapsed. */
            private final Node.Property indicator = new IndicatorProperty();
            /** Session layer state indicator property */
            private final Node.Property session = new SettingChildren.FileStateProperty (SettingChildren.PROP_LAYER_SESSION);
            /** Modules layer state indicator property */
            private final Node.Property modules = new SettingChildren.FileStateProperty (SettingChildren.PROP_LAYER_MODULES);
            
            /** Active set of properties (columns) */
            private Node.Property active_set [] = null;
            PropertyChangeListener weakL = null;
            
            public TTW () {
                super (new NTM ());
                
                refreshColumns (true);
                addMouseListener (this);
                weakL = WeakListeners.propertyChange(this, SessionManager.getDefault ());
                SessionManager.getDefault ().addPropertyChangeListener (weakL);
                
                registerKeyboardAction(
                    this,
                    KeyStroke.getKeyStroke('+'),
                    JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
                );

                getAccessibleContext().setAccessibleName(NbBundle.getBundle(OptionsAction.class).getString("ACSN_optionsTree"));
                getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(OptionsAction.class).getString("ACSD_optionsTree"));
            }
            public void mouseExited (MouseEvent evt) {
            }
            public void mouseReleased (MouseEvent evt) {
            }
            public void mousePressed (MouseEvent evt) {
            }
            public void mouseClicked (MouseEvent evt) {
                Component c = evt.getComponent ();
                if (c instanceof JTableHeader) {
                    JTableHeader h = (JTableHeader)c;
                    
                    // show/hide additional properties
                    if (1 == h.columnAtPoint (evt.getPoint ())) {
                        refreshColumns (true);
                    }
                }
            }
            public void mouseEntered (MouseEvent evt) {
            }
            public void propertyChange(PropertyChangeEvent evt) {
                if (SessionManager.PROP_OPEN.equals (evt.getPropertyName ())) {
                    refreshColumns (false);
                }
            }
            private void refreshColumns (boolean changeSets) {
                Node.Property new_set [] = active_set;
                int length = active_set == null ? 0 : active_set.length;

                if ((changeSets && length == 1) || (!changeSets && length > 1)) {
                    // build full_set
                    new_set = new Node.Property[] {indicator, session, modules};

                    indicator.setDisplayName (
                        NbBundle.getMessage(OptionsAction.class, "LBL_IndicatorProperty_Name_Expanded")); //NOI18N
                    indicator.setShortDescription (
                        NbBundle.getMessage(OptionsAction.class, "LBL_IndicatorProperty_Description_Expanded")); //NOI18N
                }
                else {
                    if (changeSets) {
                        new_set = new Node.Property[] {indicator};
                        indicator.setDisplayName (
                            NbBundle.getMessage(OptionsAction.class, "LBL_IndicatorProperty_Name")); //NOI18N
                        indicator.setShortDescription (
                            NbBundle.getMessage(OptionsAction.class, "LBL_IndicatorProperty_Description")); //NOI18N
                    }
                }
                
                if (active_set != new_set) {
                    // setup new columns
                    final Node.Property set [] = new_set;
                    if (SwingUtilities.isEventDispatchThread()) {
                        setNewSet(set);
                    } else {
                        SwingUtilities.invokeLater (new Runnable () {
                            public void run () {
                                setNewSet(set);
                            }
                        });
                    }
                    // remeber the last set of columns
                    active_set = new_set;
                }
            }
            
            private void setNewSet (Node.Property[] set) {
                // change columns
                setProperties (set);
                // set preferred colunm sizes
                setTreePreferredWidth(set.length == 1 ? 480 : 300);
                setTableColumnPreferredWidth (0, 20);
                for (int i = 1; i < set.length; i++) {
                    setTableColumnPreferredWidth (i, 60);
                }
            }
            
            public void actionPerformed(ActionEvent e) {
                refreshColumns(true);
            }
            
            public void expandTheseNodes (Collection<Node> paths, Node root) {
                Iterator<Node> it = paths.iterator();
                
                Node first = null;
                while (it.hasNext()) {
                    Node n = it.next();
                    if (first == null) {
                        first = n;
                    }
                    
                    this.expandNode(n);
                }

                if (first != null) {
                    collapseNode (first);
                    expandNode (first);
                }
                
                // move to top
                tree.scrollRowToVisible(0);
            }

            /** Dummy placeholder property. */
            private static final class IndicatorProperty extends PropertySupport.ReadOnly<String> {

                public IndicatorProperty() {
                    super("indicator", String.class, "", ""); // NOI18N
                }

                public String getValue() {
                    return ""; // NOI18N
                }

            }

        }
            
       
        private static class OptionsFilterNode extends FilterNode {
            public OptionsFilterNode () {
                super (
                    NbPlaces.getDefault().session(),
                    new SettingChildren (NbPlaces.getDefault().session())
                );
            }
            public HelpCtx getHelpCtx () {
                return new HelpCtx (OptionsFilterNode.class);
            }
            
            public Node.Handle getHandle () {
                return new H ();
            }
            
            private static class H implements Node.Handle {
                H() {}
                
                private static final long serialVersionUID = -5158460093499159177L;
                
                public Node getNode () throws java.io.IOException {
                    return new OptionsFilterNode ();
                }
            }
        }
        
    } // end of inner class OptionsPanel    
}
