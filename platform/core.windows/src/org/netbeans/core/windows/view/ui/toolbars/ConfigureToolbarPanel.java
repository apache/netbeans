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

package org.netbeans.core.windows.view.ui.toolbars;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.Actions;
import org.openide.awt.Mnemonics;
import org.openide.awt.ToolbarPool;
import org.openide.cookies.InstanceCookie;
import org.openide.explorer.ExplorerManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFilter;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.CallbackSystemAction;
import org.openide.util.datatransfer.ExTransferable;

/**
 * Toolbar Customizer showing a tree of all available actions. Users can drag actions
 * to toolbars to add new toolbar buttons.
 *
 * @author  Stanislav Aubrecht
 */
public class ConfigureToolbarPanel extends javax.swing.JPanel implements Runnable, ExplorerManager.Provider {

    private static final Logger LOG = Logger.getLogger(ConfigureToolbarPanel.class.getName());
    private static WeakReference<Dialog> dialogRef; // is weak reference necessary?
    
    private Node root;

    private final ExplorerManager explorerManager = new ExplorerManager();

    /** Creates new form ConfigureToolbarPanel */
    private ConfigureToolbarPanel() {
        initComponents();
        
        if (checkSmallIcons.getText().isEmpty()) {
            checkSmallIcons.setVisible(false);
        }
        
        setCursor( Cursor.getPredefinedCursor( Cursor.WAIT_CURSOR ) );
        
        FileObject paletteFolder = FileUtil.getConfigFile( "Actions" ); // NOI18N
        DataFolder df = DataFolder.findFolder( paletteFolder );
        root = createFolderActionNode(df);

        final JLabel lblWait = new JLabel( getBundleString("LBL_PleaseWait") );
        lblWait.setHorizontalAlignment( JLabel.CENTER );
        palettePanel.setPreferredSize( new Dimension( 440, 350 ) );
        palettePanel.add( lblWait );
        getAccessibleContext().setAccessibleDescription( getBundleString("ACSD_ToolbarCustomizer") );
    }

    static FolderActionNode createFolderActionNode(DataFolder df) {
        return new FolderActionNode(new AbstractNode(df.createNodeChildren(new ActionIconDataFilter())));
    }
    
    @Override
    public void run() {
        final ActionsTree tree = new ActionsTree();
        tree.getAccessibleContext().setAccessibleDescription( getBundleString("ACSD_ActionsTree") );
        tree.getAccessibleContext().setAccessibleName( getBundleString("ACSN_ActionsTree") );
        palettePanel.removeAll();
        palettePanel.setBorder( BorderFactory.createEtchedBorder() );
        palettePanel.add( tree, BorderLayout.CENTER );
        lblHint.setLabelFor( tree );
        invalidate();
        validate();
        repaint();
        setCursor( Cursor.getDefaultCursor() );
        explorerManager.setRootContext( root );
        tree.expandAll();
    }
    
    public static void showConfigureDialog() {
        java.awt.Dialog dialog = null;
        if (dialogRef != null)
            dialog = dialogRef.get();
        if (dialog == null) {
            JButton closeButton = new JButton();
            closeButton.getAccessibleContext().setAccessibleDescription( getBundleString("ACSD_Close") );
            org.openide.awt.Mnemonics.setLocalizedText(
                closeButton, getBundleString("CTL_Close")); 
            DialogDescriptor dd = new DialogDescriptor(
                new ConfigureToolbarPanel(),
                getBundleString("CustomizerTitle"), 
                false,
                new Object[] { closeButton },
                closeButton,
                DialogDescriptor.DEFAULT_ALIGN,
                new HelpCtx( ConfigureToolbarPanel.class ),
                null);
            dialog = DialogDisplayer.getDefault().createDialog(dd);
            dialogRef = new WeakReference<Dialog>(dialog);
        }
        dialog.setVisible(true);
    }
    
    /** @return returns string from bundle for given string pattern */
    static final String getBundleString (String bundleStr) {
        return NbBundle.getMessage(ConfigureToolbarPanel.class, bundleStr);
    }

    private boolean firstTimeInit = true;
    @Override
    public void paint(java.awt.Graphics g) {
        super.paint(g);
        if( firstTimeInit ) {
            //this is not very nice but some Actions insist on being accessed
            //from the event queue only so let's wait till the dialog window is 
            //painted before filtering out Actions without an icon
            firstTimeInit = false;
            new RequestProcessor( "ToolbarPanelConfigWarmUp").post( new Runnable() { //NOI18N

                @Override
                public void run() {
                    //warm up action nodes so that 'expand all' in actions tree is fast
                    Node[] categories = root.getChildren().getNodes( true );
                    for( int i=0; i<categories.length; i++ ) {
                        final Node category = categories[i];
                        SwingUtilities.invokeLater( new Runnable() {
                            @Override
                            public void run() {
                                category.getChildren().getNodes( true );
                            }
                        });
                    }
                    //replace 'please wait' message with actions tree
                    SwingUtilities.invokeLater( ConfigureToolbarPanel.this );
                }
            });
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        lblHint = new javax.swing.JLabel();
        palettePanel = new javax.swing.JPanel();
        checkSmallIcons = new javax.swing.JCheckBox();
        btnNewToolbar = new javax.swing.JButton();
        btnReset = new javax.swing.JButton();

        FormListener formListener = new FormListener();

        setLayout(new java.awt.GridBagLayout());

        setMinimumSize(new java.awt.Dimension(453, 68));
        org.openide.awt.Mnemonics.setLocalizedText(lblHint, getBundleString("CTL_TreeLabel")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 1, 10);
        add(lblHint, gridBagConstraints);

        palettePanel.setLayout(new java.awt.BorderLayout());

        palettePanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(1, 10, 5, 10);
        add(palettePanel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(checkSmallIcons, getBundleString("CTL_SmallIcons")); // NOI18N
        checkSmallIcons.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        checkSmallIcons.setMargin(new java.awt.Insets(0, 0, 0, 0));
        checkSmallIcons.setSelected( ToolbarPool.getDefault().getPreferredIconSize() == 16 );
        checkSmallIcons.addActionListener(formListener);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 10);
        add(checkSmallIcons, gridBagConstraints);
        checkSmallIcons.getAccessibleContext().setAccessibleDescription(getBundleString("ACSD_SmallIcons")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(btnNewToolbar, getBundleString("CTL_NewToolbar")); // NOI18N
        btnNewToolbar.addActionListener(formListener);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 10);
        add(btnNewToolbar, gridBagConstraints);
        btnNewToolbar.getAccessibleContext().setAccessibleDescription(getBundleString("ACSD_NewToolbar")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(btnReset, getBundleString("CTL_ResetToolbarsButton")); // NOI18N
        btnReset.addActionListener(formListener);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 10);
        add(btnReset, gridBagConstraints);

    }

    // Code for dispatching events from components to event handlers.

    private class FormListener implements java.awt.event.ActionListener {
        FormListener() {}
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            if (evt.getSource() == checkSmallIcons) {
                ConfigureToolbarPanel.this.switchIconSize(evt);
            }
            else if (evt.getSource() == btnNewToolbar) {
                ConfigureToolbarPanel.this.newToolbar(evt);
            }
            else if (evt.getSource() == btnReset) {
                ConfigureToolbarPanel.this.resetToolbars(evt);
            }
        }
    }// </editor-fold>//GEN-END:initComponents

    private void resetToolbars(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetToolbars
        new ResetToolbarsAction().actionPerformed( evt );
    }//GEN-LAST:event_resetToolbars

    private void newToolbar(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newToolbar
        JPanel panel = new JPanel( new BorderLayout( 5, 5 ) );
        JLabel lbl = new JLabel();
        Mnemonics.setLocalizedText( lbl, NbBundle.getMessage(ConfigureToolbarPanel.class, "PROP_newToolbarLabel") );
        panel.add( lbl, BorderLayout.WEST );
        JTextField inputField = new JTextField( NbBundle.getMessage(ConfigureToolbarPanel.class, "PROP_newToolbar") );
        inputField.setColumns( 25 );
        panel.add( inputField, BorderLayout.CENTER );
        panel.setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );
        DialogDescriptor dd = new DialogDescriptor( panel, NbBundle.getMessage(ConfigureToolbarPanel.class, "PROP_newToolbarDialog"), 
                true, DialogDescriptor.OK_CANCEL_OPTION, null, DialogDescriptor.DEFAULT_ALIGN, new HelpCtx( ConfigureToolbarPanel.class), null );

        Dialog dlg = org.openide.DialogDisplayer.getDefault ().createDialog( dd );
        dlg.setVisible( true );
        if (dd.getValue() != NotifyDescriptor.OK_OPTION)
            return;
        String s = inputField.getText().trim();
        if( s.length() == 0 )
            return;

        DataFolder folder = ToolbarPool.getDefault().getFolder();
        FileObject toolbars = folder.getPrimaryFile();
        try {
            FileObject newToolbar = toolbars.getFileObject(s);
            if (newToolbar == null) {
                DataObject[] oldKids = folder.getChildren();
                newToolbar = toolbars.createFolder(s);

                // #13015. Set new item as last one.
                DataObject[] newKids = new DataObject[oldKids.length + 1];
                System.arraycopy(oldKids, 0, newKids, 0, oldKids.length);
                newKids[oldKids.length] = DataObject.find(newToolbar);
                folder.setOrder(newKids);
                ToolbarPool.getDefault().waitFinished();
                ToolbarConfiguration.findConfiguration(ToolbarPool.getDefault().getConfiguration()).repaint();
            } else {
                NotifyDescriptor.Message msg = new NotifyDescriptor.Message(
                        NbBundle.getMessage(ConfigureToolbarPanel.class, "MSG_ToolbarExists", s ) ); // NOI18N
                org.openide.DialogDisplayer.getDefault().notify( msg );
            }
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        }
    }//GEN-LAST:event_newToolbar

    private void switchIconSize(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_switchIconSize
          boolean state = checkSmallIcons.isSelected();
          if (state) {
              ToolbarPool.getDefault().setPreferredIconSize(16);
          } else {
              ToolbarPool.getDefault().setPreferredIconSize(24);
          }
          //Rebuild toolbar panel
          //#43652: Find current toolbar configuration
          String name = ToolbarPool.getDefault().getConfiguration();
          ToolbarConfiguration tbConf = ToolbarConfiguration.findConfiguration(name);
          if (tbConf != null) {
              tbConf.refresh();
          }
    }//GEN-LAST:event_switchIconSize
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnNewToolbar;
    private javax.swing.JButton btnReset;
    private javax.swing.JCheckBox checkSmallIcons;
    private javax.swing.JLabel lblHint;
    private javax.swing.JPanel palettePanel;
    // End of variables declaration//GEN-END:variables

    @Override
    public void addNotify() {
        super.addNotify();

        ToolbarPool pool = ToolbarPool.getDefault();
        checkSmallIcons.setSelected( pool.getPreferredIconSize() == 16 );

        ToolbarConfiguration tc = ToolbarConfiguration.findConfiguration( pool.getConfiguration() );
        if( null != tc ) {
            tc.setToolbarButtonDragAndDropAllowed( true );
        }
    }

    @Override
    public void removeNotify() {
        super.removeNotify();

        ToolbarPool pool = ToolbarPool.getDefault();
        final ToolbarConfiguration tc = ToolbarConfiguration.findConfiguration( pool.getConfiguration() );
        if( null != tc ) {
            tc.setToolbarButtonDragAndDropAllowed( false );
        }
        //remove empty toolbars
        DataFolder folder = pool.getFolder();
        DataObject[] children = folder.getChildren();
        for( int i=0; i<children.length; i++ ) {
            final DataFolder subFolder = children[i].getCookie( DataFolder.class );
            if( null != subFolder && subFolder.getChildren().length == 0 ) {
                SwingUtilities.invokeLater( new Runnable() {

                    @Override
                    public void run() {
                        try {
                            subFolder.delete();
                            ToolbarPool.getDefault().waitFinished();
                            if( null != tc ) {
                                tc.removeEmptyRows();
                                tc.save();
                            }
                        }
                        catch (IOException e) {
                            LOG.log(Level.WARNING, null, e);
                        }
                    }
                });
            }
        }
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return explorerManager;
    }
    
    private static class FolderActionNode extends FilterNode {
        public FolderActionNode( Node original  ) {
            super( original, new MyChildren( original ) );
        }

        @Override
        public String getDisplayName() {
            return Actions.cutAmpersand( super.getDisplayName() );
        }

        @Override
        public Transferable drag() throws IOException {
            return Node.EMPTY.drag();
        }

        @Override
        public Transferable clipboardCut() throws IOException {
            return Node.EMPTY.clipboardCut();
        }

        @Override
        public Transferable clipboardCopy() throws IOException {
            return Node.EMPTY.clipboardCopy();
        }

        @Override
        public Action[] getActions( boolean context ) {
            return new Action[0];
        }

        private static class MyChildren extends FilterNode.Children {

            public MyChildren(Node original) {
                super(original);
            }

            @Override
            protected Node copyNode(Node node) {
                FileObject fo = node.getLookup().lookup( FileObject.class );
                if( null != fo && fo.isData() )
                    return new ItemActionNode( node );
                return new FolderActionNode( node );
            }
        }
    }
    
    private static class BlankAction extends CallbackSystemAction {
        static final Icon BLANK_ICON;
        static {
            BLANK_ICON = get(BlankAction.class).getIcon();
        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public HelpCtx getHelpCtx() {
            return null;
        }
    }

    private static class ItemActionNode extends FilterNode {
        
        private static DataFlavor nodeDataFlavor = new DataFlavor( Node.class, "Action Node" ); // NOI18N
        
        public ItemActionNode( Node original  ) {
            super( original, Children.LEAF );
        }

        @Override
        public Transferable drag() throws IOException {
            return new ExTransferable.Single( nodeDataFlavor ) {
                public Object getData() {
                   return ItemActionNode.this;
                }
            };
        }

        @Override
        public String getDisplayName() {
            return Actions.cutAmpersand( super.getDisplayName() );
        }

        @Override
        public Action[] getActions( boolean context ) {
            return new Action[0];
        }
    }
    
    /**
     * A filter that does not allow Action instances without an icon.
     */
    private static class ActionIconDataFilter implements DataFilter {
        private InstanceCookie instanceCookie;
        
        public boolean acceptDataObject( DataObject obj ) {
             boolean a = doAcceptDataObject(obj);
             LOG.log(Level.FINE, "{0} {1}", new Object[] {a ? '+' : '-', obj.getPrimaryFile().getPath().replace("Actions/", "")});
             return a;
        }
        private boolean doAcceptDataObject(DataObject obj) {
            instanceCookie = obj.getCookie( InstanceCookie.class );
            if( null != instanceCookie ) {
                try {
                    Object instance = instanceCookie.instanceCreate();
                    if( null != instance ) {
                        if( instance instanceof Action ) {
                            Action action = (Action)instance;
                            boolean noIconBase = false;
                            try {
                                noIconBase = null == action.getValue( "iconBase" );
                            } catch( AssertionError aE ) {
                                //hack: some action do not allow access outside
                                //event queue - so let's ignore their assertions
                                LOG.log(Level.FINE, null, aE);
                            }
                            boolean smallIcon = false;
                            if (noIconBase) {
                                try {
                                    final Object icon = action.getValue(Action.SMALL_ICON);
                                    smallIcon = icon != null && icon != BlankAction.BLANK_ICON;
                                } catch (AssertionError aE) {
                                    //hack: some action do not allow access outside
                                    //event queue - so let's ignore their assertions
                                    LOG.log(Level.FINE, null, aE);
                                }
                            }
                            Object allowedInToolbar = action.getValue("CanBePlacedOnMainToolbar");
                            if ((noIconBase && !smallIcon) || Boolean.FALSE.equals(allowedInToolbar)) {
                                return false;
                            }
                        } else if( instance instanceof JSeparator ) {
                            return false;
                        }
                    }
                } catch( AssertionError aE ) {
                    //hack: some action do not allow access outside
                    //event queue - so let's ignore their assertions
                    LOG.log(Level.FINE, null, aE);
                    return false;
                } catch( Throwable e ) {
                    LOG.log(Level.WARNING, null, e);
                }
                return true;
            } else {
                FileObject fo = obj.getPrimaryFile();
                if( fo.isFolder() ) {
                    boolean hasChildWithIcon = false;
                    FileObject[] children = fo.getChildren();
                    for( int i=0; i<children.length; i++ ) {
                        DataObject child = null;
                        try {
                            child = DataObject.find( children[i] );
                        } catch (DataObjectNotFoundException e) {
                            continue;
                        }
                        if( null != child && acceptDataObject( child ) ) {
                            hasChildWithIcon = true;
                            break;
                        }
                    }
                    return hasChildWithIcon;
                }
            }
            return true;
        }
    }
}
