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

package org.netbeans.modules.project.libraries.ui;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.beans.Customizer;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import static org.netbeans.modules.project.libraries.ui.Bundle.*;
import org.netbeans.spi.project.libraries.LibraryCustomizerContext;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.netbeans.spi.project.libraries.LibraryStorageArea;
import org.netbeans.spi.project.libraries.LibraryTypeProvider;
import org.netbeans.spi.project.libraries.support.LibrariesSupport;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.NodeNotFoundException;
import org.openide.nodes.NodeOp;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Parameters;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;

public final class LibrariesCustomizer extends JPanel implements ExplorerManager.Provider, HelpCtx.Provider {

    private static final Pattern VALID_LIBRARY_NAME = Pattern.compile("[-._a-zA-Z0-9]+"); // NOI18N
    private ExplorerManager manager;
    private LibrariesModel model;
    private BeanTreeView libraries;
    private LibraryStorageArea libraryStorageArea;

    public LibrariesCustomizer (@NonNull final LibraryStorageArea libraryStorageArea) {
        Parameters.notNull("libraryStorageArea", libraryStorageArea);   //NOI18N
        this.model = new LibrariesModel ();
        this.libraryStorageArea = libraryStorageArea;
        initComponents();
        postInitComponents ();
    }
    
    private void expandTree() {
        expandAllNodes(this.libraries,this.getExplorerManager().getRootContext());
        //Select first library if nothing selected
        if (this.getExplorerManager().getSelectedNodes().length == 0) {
            final Node firstLibraryNode = findFirstLibrary(getExplorerManager().getRootContext());
            if (firstLibraryNode != null) {
                try {
                    getExplorerManager().setSelectedNodes(new Node[] {firstLibraryNode});
                } catch (PropertyVetoException ex) {
                    //Ignore - just don't select
                }
            }
        }
    }
    
    private static Node findFirstLibrary(final Node node) {
        if (node == null) {
            return null;
        }
        if (node.getLookup().lookup(LibraryImplementation.class)!=null) {
            return node;
        }
        final Node[] subNodes = node.getChildren().getNodes(true);
        for (Node subNode : subNodes) {
            Node result = findFirstLibrary(subNode);
            if (result != null) {
                return result;
            }
        }
        return null;
    }
    
    public void setLibraryStorageArea(@NonNull final LibraryStorageArea libraryStorageArea) {
        Parameters.notNull("libraryStorageArea", libraryStorageArea);   //NOI18N
        this.libraryStorageArea = libraryStorageArea;
        forceTreeRecreation();
        expandTree();
    }
    
    public LibrariesModel getModel() {
        return model;
    }
    
    public void hideLibrariesList() {
        libsPanel.setVisible(false);
        jLabel2.setVisible(false);
        createButton.setVisible(false);
        deleteButton.setVisible(false);
        jLabel3.setVisible(true);
        libraryLocation.setVisible(true);
    }
    
    /**
     * Force nodes recreation after LibrariesModel change. The nodes listen on
     * model and eventually refresh themselves but usually it is too late.
     * So forcing recreation makes sure that any subsequent call to 
     * NodeOp.findPath is successful and selects just created library node.
     */
    public void forceTreeRecreation() {
        getExplorerManager().setRootContext(buildTree());
    }

    public void setSelectedLibrary (LibraryImplementation library) {
        if (library == null) {
            return;
        }
        ExplorerManager currentManager = this.getExplorerManager();
        Node root = currentManager.getRootContext();        
        String[] path = {library.getType(), library.getName()};
        try {
            Node node = NodeOp.findPath(root, path);
            if (node != null) {
                currentManager.setSelectedNodes(new Node[] {node});
            }
        } catch (NodeNotFoundException e) {
            //Ignore it
        }
        catch (PropertyVetoException e) {
            //Ignore it
        }
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.project.libraries.ui.LibrariesCustomizer");
    }
    
    public boolean apply () {
        try {
            this.model.apply();
            return true;
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
            return false;
        }
    }

    @Override
    public void addNotify() {
        super.addNotify();
        expandTree();
        this.libraries.requestFocus();
    }    
    
    @Override
    public ExplorerManager getExplorerManager () {
        if (this.manager == null) {
            this.manager = new ExplorerManager ();
            this.manager.addPropertyChangeListener (new PropertyChangeListener() {
                @Override
                public void propertyChange (PropertyChangeEvent event) {
                    if (ExplorerManager.PROP_SELECTED_NODES.equals(event.getPropertyName())) {
                        Node[] nodes = (Node[]) event.getNewValue ();
                        selectLibrary(nodes);                            
                        libraries.requestFocus();
                    }                    
                }
            });
            this.manager.addVetoableChangeListener(new VetoableChangeListener() {
                @Override
                public void vetoableChange(PropertyChangeEvent event) throws PropertyVetoException {
                    if (ExplorerManager.PROP_SELECTED_NODES.equals(event.getPropertyName())) {
                        Node[] nodes = (Node[]) event.getNewValue();
                        if (nodes.length > 1) {
                            throw new PropertyVetoException ("Invalid length", event);  //NOI18N
                        }
                    }
                }
            });            
            manager.setRootContext(buildTree());
        }
        return this.manager;
    }

    private void postInitComponents () {
        this.libraries = new LibrariesView ();
        this.libsPanel.setLayout(new BorderLayout());
        this.libsPanel.add(this.libraries);
        this.libraries.setPreferredSize(new Dimension (200,334));
        this.libraryName.setColumns(25);
        this.libraryName.setEnabled(false);
        this.libraryName.addActionListener(new ActionListener () {
            @Override
            public void actionPerformed(final ActionEvent e) {
                nameChanged();
            }
        });
        this.libraryName.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(final FocusEvent e) {
                nameChanged();
            }
        });
        jLabel3.setVisible(false);
        libraryLocation.setVisible(false);
        createButton.setEnabled(LibrariesSupport.getLibraryTypeProviders().length>0);
    }

    @Messages({
        "ERR_InvalidName=The library name is not valid.",
        "# {0} - library name", "ERR_ExistingName=Library {0} already exists."
    })
    private void nameChanged () {
        Node[] nodes = this.getExplorerManager().getSelectedNodes();
        if (nodes.length == 1) {
            final LibraryImplementation lib = nodes[0].getLookup().lookup(LibraryImplementation.class);
            if (lib == null) {
                return;
            }
            final String newName = this.libraryName.getText();
            if (newName.equals(LibrariesSupport.getLocalizedName(lib))) {
                return;
            }
            if (newName.length () == 0) {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(ERR_InvalidName(), NotifyDescriptor.ERROR_MESSAGE));
            } else if (isExistingDisplayName(model, newName, model.getArea(lib))) {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(ERR_ExistingName(newName), NotifyDescriptor.ERROR_MESSAGE));
            } else {
                LibrariesSupport.setDisplayName(lib, newName);
            }
        }
    }

    private void selectLibrary (Node[] nodes) {
        int tabCount = this.properties.getTabCount();
        for (int i=0; i<tabCount; i++) {
            this.properties.removeTabAt(0);
        }
        this.libraryName.setEnabled(false);
        this.libraryName.setText("");   //NOI18N
        this.jLabel1.setVisible(false);
        this.libraryName.setVisible(false);
        this.properties.setVisible(false);
        this.deleteButton.setEnabled(false);
        if (nodes.length != 1) {
            return;
        }
        LibraryImplementation impl = nodes[0].getLookup().lookup(LibraryImplementation.class);
        if (impl == null) {
            return;
        }
        this.jLabel1.setVisible(true);
        this.libraryName.setVisible(true);
        this.properties.setVisible(true);
        boolean editable = model.isLibraryEditable (impl);
        this.libraryName.setEnabled(editable && LibrariesSupport.supportsDisplayName(impl));
        this.deleteButton.setEnabled(editable);
        this.libraryName.setText (LibrariesSupport.getLocalizedName(impl));
        LibraryTypeProvider provider = nodes[0].getLookup().lookup(LibraryTypeProvider.class);
        if (provider == null) {
            return;
        }
        LibraryCustomizerContextWrapper customizerContext;
        LibraryStorageArea area = nodes[0].getLookup().lookup(LibraryStorageArea.class);
        if (area != LibraryStorageArea.GLOBAL) {
            customizerContext = new LibraryCustomizerContextWrapper(impl, area);
            File f = Utilities.toFile(URI.create(area.getLocation().toExternalForm()));
            this.libraryLocation.setText(f.getPath());
        } else {
            customizerContext = new LibraryCustomizerContextWrapper(impl, null);
            this.libraryLocation.setText(LABEL_Global_Libraries());
        }

        String[] volumeTypes = provider.getSupportedVolumeTypes();
        for (int i=0; i< volumeTypes.length; i++) {
            Customizer c = provider.getCustomizer (volumeTypes[i]);
            if (c instanceof JComponent) {
                c.setObject (customizerContext);
                JComponent component = (JComponent) c;
                component.setEnabled (editable);
                String tabName = component.getName();
                if (tabName == null) {
                    tabName = volumeTypes[i];
                }
                this.properties.addTab(tabName, component);
            }
        }        
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        libraryName = new javax.swing.JTextField();
        createButton = new javax.swing.JButton();
        deleteButton = new javax.swing.JButton();
        libsPanel = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        properties = new javax.swing.JTabbedPane();
        jLabel3 = new javax.swing.JLabel();
        libraryLocation = new javax.swing.JTextField();

        setMinimumSize(new java.awt.Dimension(642, 395));

        jLabel1.setLabelFor(libraryName);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/project/libraries/ui/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, bundle.getString("CTL_CustomizerLibraryName")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(createButton, bundle.getString("CTL_NewLibrary")); // NOI18N
        createButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createLibrary(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(deleteButton, bundle.getString("CTL_DeleteLibrary")); // NOI18N
        deleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteLibrary(evt);
            }
        });

        libsPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        javax.swing.GroupLayout libsPanelLayout = new javax.swing.GroupLayout(libsPanel);
        libsPanel.setLayout(libsPanelLayout);
        libsPanelLayout.setHorizontalGroup(
            libsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 190, Short.MAX_VALUE)
        );
        libsPanelLayout.setVerticalGroup(
            libsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 382, Short.MAX_VALUE)
        );

        jLabel2.setLabelFor(libsPanel);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, bundle.getString("TXT_LibrariesPanel")); // NOI18N

        properties.setPreferredSize(new java.awt.Dimension(400, 300));

        jLabel3.setLabelFor(libraryLocation);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, bundle.getString("CTL_CustomizerLibraryLocationName")); // NOI18N

        libraryLocation.setEditable(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(createButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(deleteButton))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(libsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel3)
                                    .addComponent(jLabel1))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(libraryLocation, javax.swing.GroupLayout.DEFAULT_SIZE, 388, Short.MAX_VALUE)
                                    .addComponent(libraryName, javax.swing.GroupLayout.DEFAULT_SIZE, 388, Short.MAX_VALUE)))
                            .addComponent(properties, javax.swing.GroupLayout.DEFAULT_SIZE, 503, Short.MAX_VALUE)))
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 186, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(libraryName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(libraryLocation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(properties, javax.swing.GroupLayout.DEFAULT_SIZE, 319, Short.MAX_VALUE))
                    .addComponent(libsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(createButton)
                    .addComponent(deleteButton)))
        );

        libraryName.getAccessibleContext().setAccessibleDescription(bundle.getString("AD_LibraryName")); // NOI18N
        createButton.getAccessibleContext().setAccessibleDescription(bundle.getString("AD_NewLibrary")); // NOI18N
        deleteButton.getAccessibleContext().setAccessibleDescription(bundle.getString("AD_DeleteLibrary")); // NOI18N
        libsPanel.getAccessibleContext().setAccessibleDescription(bundle.getString("AD_libsPanel")); // NOI18N
        properties.getAccessibleContext().setAccessibleName(bundle.getString("AN_LibrariesCustomizerProperties")); // NOI18N
        properties.getAccessibleContext().setAccessibleDescription(bundle.getString("AD_LibrariesCustomizerProperties")); // NOI18N
        jLabel3.getAccessibleContext().setAccessibleDescription("Edit Library");
        libraryLocation.getAccessibleContext().setAccessibleDescription(bundle.getString("AD_LibraryLocation")); // NOI18N

        getAccessibleContext().setAccessibleDescription(bundle.getString("AD_LibrariesCustomizer")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void deleteLibrary(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteLibrary
        Node[] nodes = this.getExplorerManager().getSelectedNodes();
        if (nodes.length == 1) {
            LibraryImplementation library = nodes[0].getLookup().lookup(LibraryImplementation.class);
            if (library == null) {
                return;
            }
            Node[] sib = nodes[0].getParentNode().getChildren().getNodes(true);            
            Node selNode = null;
            for (int i=0; i < sib.length; i++) {
                if (nodes[0].equals(sib[i])) {
                    if (i>0) {
                        selNode = sib[i-1];
                    }
                    else if (i<sib.length-1){
                        selNode = sib[i+1];
                    }
                }
            }            
            model.removeLibrary(library);
            try {
                if (selNode != null) {
                    this.getExplorerManager().setSelectedNodes(new Node[] {selNode});            
                }
            } catch (PropertyVetoException e) {
                //Ignore it
            }
            this.libraries.requestFocus();
        }
    }//GEN-LAST:event_deleteLibrary

    @Messages("CTL_CreateLibrary=New Library")
    private void createLibrary(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createLibrary
        Dialog dlg = null;
        try {
            String preselectedLibraryType = null;
            LibraryStorageArea area = null;
            Node[] preselectedNodes = this.getExplorerManager().getSelectedNodes();
            if (preselectedNodes.length == 1) {
                LibraryTypeProvider provider = preselectedNodes[0].getLookup().lookup(LibraryTypeProvider.class);
                if (provider != null) {
                    preselectedLibraryType = provider.getLibraryType();
                }
                area = preselectedNodes[0].getLookup().lookup(LibraryStorageArea.class);
            }
            NewLibraryPanel p = new NewLibraryPanel(model, preselectedLibraryType, area);
            DialogDescriptor dd = new DialogDescriptor(p, CTL_CreateLibrary(), true, DialogDescriptor.OK_CANCEL_OPTION, null, null);
            p.setDialogDescriptor(dd);
            dlg = DialogDisplayer.getDefault().createDialog (dd);
            dlg.setVisible(true);
            if (dd.getValue() == DialogDescriptor.OK_OPTION) {
                final String libraryType = p.getLibraryType();
                final String currentLibraryName = p.getLibraryName();
                final String antLibraryName = createFreeAntLibraryName(currentLibraryName, model, area);
                LibraryImplementation impl;
                if (area != LibraryStorageArea.GLOBAL) {
                    impl = model.createArealLibrary(libraryType, currentLibraryName, area);
                } else {
                    LibraryTypeProvider provider = LibrariesSupport.getLibraryTypeProvider(libraryType);
                    if (provider == null) {
                        return;
                    }
                    impl = provider.createLibrary();
                    impl.setName(antLibraryName);
                }
                LibrariesSupport.setDisplayName(impl, currentLibraryName);
                model.addLibrary (impl);                
                forceTreeRecreation();
                String[] path = {impl.getType(), impl.getName()};
                ExplorerManager mgr = this.getExplorerManager();
                try {
                    Node node = NodeOp.findPath(mgr.getRootContext(),path);
                    if (node != null) {
                        mgr.setSelectedNodes(new Node[] {node});
                    }
                } catch (PropertyVetoException e) {
                    //Ignore it
                }
                catch (NodeNotFoundException e) {
                    //Ignore it
                }
                this.libraryName.requestFocus();
                this.libraryName.selectAll();
            }
            else {
                this.libraries.requestFocus();
            }
        }
        finally {
            if (dlg != null) {
                dlg.dispose();
            }
        }
    }//GEN-LAST:event_createLibrary

    static boolean isExistingDisplayName(
            final @NonNull LibrariesModel model,
            final @NonNull String name,
            final @NullAllowed LibraryStorageArea area) {
        for (LibraryImplementation lib : model.getLibraries()) {
            if (LibrariesSupport.getLocalizedName(lib).equals(name) && Utilities.compareObjects(model.getArea(lib), area)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isValidName(
            final @NonNull LibrariesModel model,
            final @NonNull String name,
            final @NullAllowed LibraryStorageArea area) {
        for (LibraryImplementation lib : model.getLibraries()) {
            if (lib.getName().equals(name) && Utilities.compareObjects(model.getArea(lib), area)) {
                return false;
            }
        }
        return true;
    }


    public static String createFreeAntLibraryName(
            @NonNull String name,
            final @NonNull LibrariesModel model,
            final @NullAllowed LibraryStorageArea area) {
        // XXX: there is method in PropertyUtils
        // which should be used here but that would create dependency
        // on ant/project modules which is not desirable.
        if (!VALID_LIBRARY_NAME.matcher(name).matches()) {
            final StringBuilder sb = new StringBuilder(name);
            for (int i=0; i<sb.length(); i++) {
                if (!VALID_LIBRARY_NAME.matcher(sb.substring(i,i+1)).matches()) {
                    sb.replace(i,i+1,"_");
                }
            }
            name = sb.toString();
        }        
        String uniqueName = name;
        for (int i=2; !isValidName(model, uniqueName, area); i++) {
            uniqueName = String.format("%s_%d", name,i);    //NOI18N
        }
        return uniqueName;
    }

        
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton createButton;
    private javax.swing.JButton deleteButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JTextField libraryLocation;
    private javax.swing.JTextField libraryName;
    private javax.swing.JPanel libsPanel;
    private javax.swing.JTabbedPane properties;
    // End of variables declaration//GEN-END:variables

    private static void expandAllNodes (BeanTreeView btv, Node node) {
        btv.expandNode (node);
        Children ch = node.getChildren();
        if ( ch == Children.LEAF ) {            
            return;
        }
        Node nodes[] = ch.getNodes( true );
        for ( int i = 0; i < nodes.length; i++ ) {
            expandAllNodes( btv, nodes[i]);
        }

    }
    
    private static class LibrariesView extends BeanTreeView {
        
        @Messages("AD_Libraries=N/A")
        LibrariesView() {
            this.setRootVisible(false);
            this.setPopupAllowed(false);
            this.setDefaultActionAllowed(false);
            this.tree.setEditable (false);
            this.tree.setShowsRootHandles (false);
            this.getAccessibleContext().setAccessibleDescription(AD_Libraries());
        }
        
    }

    private class TypeChildren extends Children.Keys<LibraryTypeProvider> {

        private final LibraryStorageArea area;

        TypeChildren(LibraryStorageArea area) {
            this.area = area;
        }

        @Override
        public void addNotify () {
            // Could also filter by area (would then need to listen to model too)
            this.setKeys(LibrariesSupport.getLibraryTypeProviders());
        }
        
        @Override
        public void removeNotify () {
            this.setKeys(new LibraryTypeProvider[0]);
        }
        
        @Override protected Node[] createNodes(LibraryTypeProvider provider) {
            return new Node[] {new CategoryNode(provider, area)};
        }
        
    }
    
    private class CategoryNode extends AbstractNode {
        
        private LibraryTypeProvider provider;
        private Node iconDelegate;
                
        @org.netbeans.api.annotations.common.SuppressWarnings("SIC_INNER_SHOULD_BE_STATIC_NEEDS_THIS") // since CategoryChildren is nonstatic
        CategoryNode(LibraryTypeProvider provider, LibraryStorageArea area) {
            super(new CategoryChildren(provider, area), Lookups.fixed(provider, area));
            this.provider = provider;       
            this.iconDelegate = DataFolder.findFolder (FileUtil.getConfigRoot()).getNodeDelegate();
        }
        
        @Override public String getName() {
            return provider.getLibraryType ();
        }
        
        @Override public String getDisplayName() {
            return this.provider.getDisplayName();
        }
        
        @Override public Image getIcon(int type) {
            return this.iconDelegate.getIcon (type);
        }        
        
        @Override public Image getOpenedIcon(int type) {
            return this.iconDelegate.getOpenedIcon (type);
        }        
                        
    }    

    private class CategoryChildren extends Children.Keys<LibraryImplementation> implements ChangeListener {
        
        private LibraryTypeProvider provider;
        private final LibraryStorageArea area;
        
        CategoryChildren(LibraryTypeProvider provider, LibraryStorageArea area) {
            this.provider = provider;
            this.area = area;
            model.addChangeListener(this);
        }
        
        @Override public void addNotify() {
            Collection<LibraryImplementation> keys = new ArrayList<LibraryImplementation>();
            for (LibraryImplementation impl : model.getLibraries()) {
                if (provider.getLibraryType().equals(impl.getType()) && model.getArea(impl).equals(area)) {
                    keys.add (impl);
                }
            }
            this.setKeys(keys);
        }
        
        @Override public void removeNotify() {
            this.setKeys(new LibraryImplementation[0]);
        }
        
        @Override protected Node[] createNodes(LibraryImplementation impl) {
            return new Node[] {new LibraryNode(impl, provider, area)};
        }
        
        @Override public void stateChanged(ChangeEvent e) {
            EventQueue.invokeLater(new Runnable() {
                @Override public void run() {
                    addNotify();
                }
            });
        }
        
    }
    
    private static class LibraryNode extends AbstractNode implements PropertyChangeListener {

        private static final String ICON = "org/netbeans/modules/project/libraries/resources/libraries.gif";  //NOI18N

        private LibraryImplementation lib;

        @SuppressWarnings("LeakingThisInConstructor")
        LibraryNode(LibraryImplementation lib, LibraryTypeProvider provider, LibraryStorageArea area) {
            super(Children.LEAF, Lookups.fixed(lib, provider, area));
            this.lib = lib;
            this.setIconBaseWithExtension(ICON);
            this.lib.addPropertyChangeListener(this);
        }

        @Override
        public String getName () {
            return this.lib.getName ();
        }

        @Override
        public String getDisplayName () {
            return LibrariesSupport.getLocalizedName(this.lib);
        }

        @Override
        public void propertyChange(final PropertyChangeEvent event) {
            this.fireDisplayNameChange(null,null);
        }
    }

    private Node buildTree() {
        return new AbstractNode(new TypeChildren(libraryStorageArea));
    }
    
    /**
     * This is backward compatible wrapper which can be passed to libraries customizer
     * via JComponent.setObject and which provides to customizer both LibraryImplementation
     * (old contract) and LibraryCustomizerContext (new contract).
     */
    private static class LibraryCustomizerContextWrapper extends LibraryCustomizerContext implements LibraryImplementation {
        
        LibraryCustomizerContextWrapper(LibraryImplementation lib, LibraryStorageArea area) {
            super(lib, area);
        }

        @Override public String getType() {
            return getLibraryImplementation().getType();
        }

        @Override public String getName() {
            return getLibraryImplementation().getName();
        }

        @Override public String getDescription() {
            return getLibraryImplementation().getDescription();
        }

        @Override public String getLocalizingBundle() {
            return getLibraryImplementation().getLocalizingBundle();
        }

        @Override public List<URL> getContent(String volumeType) throws IllegalArgumentException {
            return getLibraryImplementation().getContent(volumeType);
        }

        @Override public void setName(String name) {
            getLibraryImplementation().setName(name);
        }

        @Override public void setDescription(String text) {
            getLibraryImplementation().setDescription(text);
        }

        @Override public void setLocalizingBundle(String resourceName) {
            getLibraryImplementation().setLocalizingBundle(resourceName);
        }

        @Override public void addPropertyChangeListener(PropertyChangeListener l) {
            getLibraryImplementation().addPropertyChangeListener(l);
        }

        @Override public void removePropertyChangeListener(PropertyChangeListener l) {
            getLibraryImplementation().removePropertyChangeListener(l);
        }

        @Override public void setContent(String volumeType, List<URL> path) throws IllegalArgumentException {
            getLibraryImplementation().setContent(volumeType, path);
        }
    }
    
}
