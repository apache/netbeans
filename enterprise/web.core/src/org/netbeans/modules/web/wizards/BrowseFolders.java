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

package org.netbeans.modules.web.wizards;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import org.netbeans.api.project.SourceGroup;
import org.openide.util.NbBundle;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.DataFolder;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.FilterNode;
import org.openide.nodes.NodeNotFoundException;
import org.openide.nodes.NodeOp;
import org.openide.util.NbCollections;

// XXX I18N

/**
 *
 * @author  phrebejk, mkuchtiak
 */
public class BrowseFolders extends javax.swing.JPanel implements ExplorerManager.Provider {
    
    private ExplorerManager manager;
    private SourceGroup[] folders;
    private Class target;
    BeanTreeView btv;
    
    private static JScrollPane SAMPLE_SCROLL_PANE = new JScrollPane();
    
    /** Creates new form BrowseFolders */
    public BrowseFolders( SourceGroup[] folders, Class target, String preselectedFileName  ) {
        initComponents();
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(BrowseFolders.class, (target == DataFolder.class?"ACSD_BrowseFolders":"ACSD_BrowseFiles")));

        this.folders = folders;
        this.target = target;
        manager = new ExplorerManager();        
        AbstractNode rootNode = new AbstractNode( new SourceGroupsChildren( folders ) );
        manager.setRootContext( rootNode );
        
        // Create the templates view
        btv = new BeanTreeView();
        btv.setRootVisible( false );
        btv.setSelectionMode( javax.swing.tree.TreeSelectionModel.SINGLE_TREE_SELECTION );
        btv.setBorder( SAMPLE_SCROLL_PANE.getBorder() );
        expandSelection( preselectedFileName );
        folderPanel.add( btv, java.awt.BorderLayout.CENTER );
    }
        
    // ExplorerManager.Provider implementation ---------------------------------
    
    public ExplorerManager getExplorerManager() {
        return manager;
    }
    
    private void expandSelection( String preselectedFileName ) {
        Node root = manager.getRootContext();
        Children ch = root.getChildren();
        if ( ch == Children.LEAF ) {
            return;
        }
        Node nodes[] = ch.getNodes( true );
        
        Node sel = null;        
        
        if ( preselectedFileName != null && preselectedFileName.length() > 0 ) {
             // Try to find the node
             for ( int i = 0; i < nodes.length; i++ ) {            
                try { 
                    sel = NodeOp.findPath( nodes[i], NbCollections.checkedEnumerationByFilter( new java.util.StringTokenizer( preselectedFileName, "/" ), String.class, false ) );
                    break;
                }
                catch ( NodeNotFoundException e ) {
                    // Will select the first node
                }
             }
        }
                        
        if ( sel == null ) {
            // Node not found => expand first level
            btv.expandNode( root );
            for ( int i = 0; i < nodes.length; i++ ) {            
                btv.expandNode( nodes[i] );
                if ( i == 0 ) {
                    sel = nodes[i];
                }
            }
        }
        
        if ( sel != null ) {
            // Select the node
            try {
                manager.setSelectedNodes( new Node[] { sel } );
            }
            catch ( java.beans.PropertyVetoException e ) {
                // No selection for some reason
            }
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

        jLabel1 = new javax.swing.JLabel();
        folderPanel = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(12, 12, 12, 12)));
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(BrowseFolders.class, "LBL_Folders"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 0);
        add(jLabel1, gridBagConstraints);

        folderPanel.setLayout(new java.awt.BorderLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(folderPanel, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel folderPanel;
    private javax.swing.JLabel jLabel1;
    // End of variables declaration//GEN-END:variables
        
    public static FileObject showDialog( SourceGroup[] folders, Class target, String preselectedFileName) {
        BrowseFolders bf = new BrowseFolders( folders, target, preselectedFileName);

        JButton selectButton = new JButton( NbBundle.getMessage(BrowseFolders.class,(target == DataFolder.class?"LBL_SelectFolder":"LBL_SelectFile"))); 
        selectButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(BrowseFolders.class, (target == DataFolder.class?"ACSD_SelectFolder":"ACSD_SelectFile")));
        JButton cancelButton = new JButton( NbBundle.getMessage(BrowseFolders.class,"LBL_Cancel") ); 
        cancelButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(BrowseFolders.class, "ACSD_Cancel"));
        JButton options[] = new JButton[] { 
            //new JButton( NbBundle.getMessage( BrowseFolders.class, "LBL_BrowseFolders_Select_Option") ), // NOI18N
            //new JButton( NbBundle.getMessage( BrowseFolders.class, "LBL_BrowseFolders_Cancel_Option") ), // NOI18N
            selectButton, 
            cancelButton, 
        };
                
        OptionsListener optionsListener = new OptionsListener( bf, target );
        
        options[ 0 ].setActionCommand( OptionsListener.COMMAND_SELECT );
        options[ 0 ].addActionListener( optionsListener );
        options[ 1 ].setActionCommand( OptionsListener.COMMAND_CANCEL );
        options[ 1 ].addActionListener( optionsListener );    
        
        DialogDescriptor dialogDescriptor = new DialogDescriptor( 
            bf,                                     // innerPane
            NbBundle.getMessage(BrowseFolders.class, (target == DataFolder.class? "LBL_BrowseFolders":"LBL_BrowseFiles")), // displayName
            true,                                   // modal
            options,                                // options
            options[ 0 ],                           // initial value
            DialogDescriptor.BOTTOM_ALIGN,          // options align
            null,                                   // helpCtx
            null );                                 // listener 

        dialogDescriptor.setClosingOptions( new Object[] { options[ 0 ], options[ 1 ] } );
            
        Dialog dialog = DialogDisplayer.getDefault().createDialog( dialogDescriptor );
        dialog.setVisible(true);
        
        return optionsListener.getResult();
    }
    
    
    // Innerclasses ------------------------------------------------------------
    
    /** Children to be used to show FileObjects from given SourceGroups
     */
    private final class SourceGroupsChildren extends Children.Keys {
        
        private SourceGroup[] groups;
        private SourceGroup group;
        private FileObject fo;
        
        public SourceGroupsChildren( SourceGroup[] groups ) {
            this.groups = groups;
        }
        
        public SourceGroupsChildren( FileObject fo, SourceGroup group ) {            
            this.fo = fo;
            this.group = group;
        }
        
        @Override
        protected void addNotify() {
            super.addNotify();
            setKeys( getKeys() );
        }
        
        @Override
        protected void removeNotify() {
            setKeys( Collections.emptySet());
            super.removeNotify();
        }
        
        protected Node[] createNodes(Object key) {
            FileObject fObj = null;
            SourceGroup group = null;
            boolean isFile=false;
            
            if ( key instanceof SourceGroup ) {
                fObj = ((SourceGroup)key).getRootFolder();
                group = (SourceGroup)key;
            }
            else if ( key instanceof Key ) {
                fObj = ((Key)key).folder;
                group = ((Key)key).group;
                if (!fObj.isFolder()) isFile=true;
            }

            try {
                DataObject dobj = DataObject.find( fObj );
                FilterNode fn = (isFile?new FilterNode(dobj.getNodeDelegate(),Children.LEAF):
                                        new FilterNode(dobj.getNodeDelegate(), new SourceGroupsChildren( fObj, group )));
            
                if ( key instanceof SourceGroup ) {
                    fn.setDisplayName( group.getDisplayName() );
                }
            
                return new Node[] { fn };            
            }
            catch ( DataObjectNotFoundException e ) {
                return null;
            }
        }
                
        // FIXME: Collection is filled with objects of different types!
        // (Arrays.asList( groups ) versus ArrayList children)
        private Collection getKeys() {
            if ( groups != null ) {
                return Arrays.asList( groups );                
            }
            else {
                FileObject files[] = fo.getChildren();
                Arrays.sort(files,new BrowseFolders.FileObjectComparator());
                ArrayList children = new ArrayList( files.length );
                
                if (BrowseFolders.this.target==org.openide.loaders.DataFolder.class)
                    for( int i = 0; i < files.length; i++ ) {
                        if ( files[i].isFolder() && group.contains( files[i] ) ) {
                            children.add( new Key( files[i], group ) );
                        }
                    }
                else {
                    // add folders
                    for( int i = 0; i < files.length; i++ ) {
                        if ( group.contains( files[i]) && files[i].isFolder() ) children.add( new Key( files[i], group ) );
                    }
                    // add files
                    for( int i = 0; i < files.length; i++ ) {
                        if ( group.contains( files[i]) && !files[i].isFolder() ) children.add( new Key( files[i], group ) );
                    }
                }
                
                return children;
            }
        }
        
        private class Key {
            private FileObject folder;
            private SourceGroup group;
            
            private Key ( FileObject folder, SourceGroup group ) {
                this.folder = folder;
                this.group = group;
            }
        }
        
    }

    private static class FileObjectComparator implements java.util.Comparator<FileObject> {
        public int compare(FileObject fo1, FileObject fo2) {
            return fo1.getName().compareTo(fo2.getName());
        }
    }
    
    private static final class OptionsListener implements ActionListener {
    
        public static final String COMMAND_SELECT = "SELECT"; //NOI18N
        public static final String COMMAND_CANCEL = "CANCEL"; //NOI18N
            
        private BrowseFolders browsePanel;
        
        private FileObject result;
        private Class target;
        
        public OptionsListener( BrowseFolders browsePanel, Class target ) {
            this.browsePanel = browsePanel;
            this.target=target;
        }
        
        public void actionPerformed( ActionEvent e ) {
            String command = e.getActionCommand();

            if ( COMMAND_SELECT.equals( command ) ) {
                Node selection[] = browsePanel.getExplorerManager().getSelectedNodes();
                
                if ( selection != null && selection.length > 0 ) {
                    DataObject dobj = (DataObject)selection[0].getLookup().lookup( DataObject.class );
                    if (dobj!=null && dobj.getClass().isAssignableFrom(target)) {
                        result = dobj.getPrimaryFile();
                    }
                    /*
                    if ( dobj != null ) {
                        FileObject fo = dobj.getPrimaryFile();
                        if ( fo.isFolder() ) {
                            result = fo;
                        }
                    }
                    */
                }
            }
        }
        
        public FileObject getResult() {
            return result;
        }
    }
    
    
}
